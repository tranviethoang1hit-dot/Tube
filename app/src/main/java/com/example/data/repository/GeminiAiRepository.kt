package com.example.data.repository

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.example.data.model.VideoItem
import com.example.data.remote.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

data class ThinkingAnalysisResult(
    val success: Boolean,
    val text: String,
    val thinkingProcess: String = "",
    val errorMessage: String? = null
)

data class ImageGenerationResult(
    val success: Boolean,
    val bitmap: Bitmap? = null,
    val base64Data: String? = null,
    val textDescription: String? = null,
    val errorMessage: String? = null
)

class GeminiAiRepository {

    private val apiService = GeminiApiClient.service

    /**
     * Performs deep AI analysis using gemini-3.1-pro-preview with ThinkingLevel.HIGH
     */
    suspend fun analyzeVideoWithHighThinking(
        video: VideoItem,
        userQuery: String = ""
    ): ThinkingAnalysisResult = withContext(Dispatchers.IO) {
        try {
            val apiKey = GeminiApiClient.getApiKey()
            if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                return@withContext ThinkingAnalysisResult(
                    success = false,
                    text = "",
                    errorMessage = "Chưa tìm thấy Gemini API Key. Vui lòng cấu hình GEMINI_API_KEY trong AI Studio Secrets."
                )
            }

            val promptText = buildString {
                appendLine("Bạn là Chuyên gia Phân Tích Nội Dung Video Cao Cấp của YouTube Premium.")
                appendLine("Hãy sử dụng tư duy sâu sắc (Thinking Mode HIGH) để phân tích toàn diện video sau:")
                appendLine("---")
                appendLine("Tiêu đề: ${video.title}")
                appendLine("Kênh: ${video.channelName}")
                appendLine("Thời lượng: ${video.durationFormatted}")
                appendLine("Danh mục: ${video.category}")
                appendLine("Mô tả: ${video.description}")
                if (video.transcript.isNotBlank()) {
                    appendLine("Nội dung/Transcript: ${video.transcript}")
                }
                appendLine("---")
                if (userQuery.isNotBlank()) {
                    appendLine("Yêu cầu/Câu hỏi từ người xem: $userQuery")
                } else {
                    appendLine("Hãy tạo bản phân tích gồm 4 phần mạch lạc bằng tiếng Việt:")
                    appendLine("1. 💡 **Tóm tắt cốt lõi & Luận điểm chính** (Executive Summary)")
                    appendLine("2. ⏱️ **Phân đoạn nội dung chi tiết** (Smart Chapters Breakdown)")
                    appendLine("3. 🧠 **Phân tích chiều sâu & Đánh giá chuyên môn** (Deep Reasoning & Insights)")
                    appendLine("4. 🎯 **Bài học & Ứng dụng thực tế** (Key Takeaways)")
                }
            }

            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent(
                        parts = listOf(GeminiPart(text = promptText))
                    )
                ),
                generationConfig = GeminiGenerationConfig(
                    temperature = 0.4f,
                    thinkingConfig = GeminiThinkingConfig(thinkingLevel = "high")
                    // Note: Do not set maxOutputTokens per instructions
                ),
                systemInstruction = GeminiContent(
                    parts = listOf(
                        GeminiPart(text = "Bạn là trợ lý AI thông minh YouTube Premium với khả năng suy luận cấp cao, trả lời súc tích, định dạng markdown đẹp mắt, khoa học và cuốn hút.")
                    )
                )
            )

            val response = apiService.generateContent(
                model = "gemini-3.1-pro-preview",
                apiKey = apiKey,
                request = request
            )

            val firstCandidate = response.candidates?.firstOrNull()
            val text = firstCandidate?.content?.parts?.mapNotNull { it.text }?.joinToString("\n") ?: ""

            if (text.isNotBlank()) {
                ThinkingAnalysisResult(
                    success = true,
                    text = text,
                    thinkingProcess = "Đã hoàn tất chuỗi suy luận sâu (High Thinking Chain) qua mô hình gemini-3.1-pro-preview."
                )
            } else {
                ThinkingAnalysisResult(
                    success = false,
                    text = "",
                    errorMessage = response.error?.message ?: "Không nhận được phản hồi từ mô hình AI."
                )
            }
        } catch (e: Exception) {
            ThinkingAnalysisResult(
                success = false,
                text = "",
                errorMessage = "Lỗi kết nối AI: ${e.localizedMessage ?: e.message}"
            )
        }
    }

    /**
     * Generates or edits thumbnails using gemini-3.1-flash-image-preview
     */
    suspend fun generateOrEditImage(
        prompt: String,
        aspectRatio: String = "16:9", // "16:9", "1:1", "9:16"
        sourceBitmap: Bitmap? = null
    ): ImageGenerationResult = withContext(Dispatchers.IO) {
        try {
            val apiKey = GeminiApiClient.getApiKey()
            if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                return@withContext ImageGenerationResult(
                    success = false,
                    errorMessage = "Chưa tìm thấy Gemini API Key. Vui lòng cấu hình GEMINI_API_KEY trong AI Studio Secrets."
                )
            }

            val parts = mutableListOf<GeminiPart>()
            parts.add(GeminiPart(text = prompt))

            if (sourceBitmap != null) {
                val base64 = sourceBitmap.toBase64()
                parts.add(GeminiPart(inlineData = GeminiInlineData(mimeType = "image/jpeg", data = base64)))
            }

            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent(parts = parts)
                ),
                generationConfig = GeminiGenerationConfig(
                    imageConfig = GeminiImageConfig(
                        aspectRatio = aspectRatio,
                        imageSize = "1K"
                    ),
                    responseModalities = listOf("TEXT", "IMAGE")
                )
            )

            val response = apiService.generateContent(
                model = "gemini-3.1-flash-image-preview",
                apiKey = apiKey,
                request = request
            )

            val candidate = response.candidates?.firstOrNull()
            val imagePart = candidate?.content?.parts?.firstOrNull { it.inlineData != null }
            val textPart = candidate?.content?.parts?.firstOrNull { it.text != null }?.text

            if (imagePart?.inlineData != null) {
                val imageBytes = Base64.decode(imagePart.inlineData.data, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                ImageGenerationResult(
                    success = true,
                    bitmap = bitmap,
                    base64Data = imagePart.inlineData.data,
                    textDescription = textPart
                )
            } else if (textPart != null) {
                ImageGenerationResult(
                    success = true,
                    textDescription = textPart
                )
            } else {
                ImageGenerationResult(
                    success = false,
                    errorMessage = response.error?.message ?: "Không thể tạo ảnh từ câu lệnh đã cho."
                )
            }
        } catch (e: Exception) {
            ImageGenerationResult(
                success = false,
                errorMessage = "Lỗi tạo ảnh: ${e.localizedMessage ?: e.message}"
            )
        }
    }

    private fun Bitmap.toBase64(): String {
        val stream = ByteArrayOutputStream()
        this.compress(Bitmap.CompressFormat.JPEG, 85, stream)
        val byteArray = stream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }
}
