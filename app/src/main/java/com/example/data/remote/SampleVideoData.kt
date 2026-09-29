package com.example.data.remote

import com.example.data.model.CommentItem
import com.example.data.model.Playlist
import com.example.data.model.VideoChapter
import com.example.data.model.VideoItem

object SampleVideoData {

    val sampleVideos: List<VideoItem> = listOf(
        VideoItem(
            id = "vid_1",
            title = "Khám Phá Vũ Trụ & Trí Tuệ Nhân Tạo 2026: Kỷ Nguyên Siêu Trí Tuệ (4K 60fps HDR)",
            description = "Tài liệu khoa học chuyên sâu về sự phát triển vượt bậc của AGI, mạng nơ-ron đa lượng tử và hành trình con người mở rộng vào vũ trụ sâu. Video được biên tập với độ phân giải siêu nét 4K 60FPS không gián đoạn bởi quảng cáo.",
            channelName = "Khoa Học Vũ Trụ & AI",
            channelAvatarUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=150&auto=format&fit=crop&q=80",
            subscriberCount = "2.8M",
            thumbnailUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=800&auto=format&fit=crop&q=80",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            durationSeconds = 596,
            durationFormatted = "09:56",
            viewCount = "1.4M lượt xem",
            publishedTime = "2 ngày trước",
            category = "Tech",
            isShort = false,
            likesCount = "128K",
            transcript = "Kỷ nguyên AI 2026 đánh dấu bước nhảy vọt toàn diện từ các mô hình ngôn ngữ đơn thuần sang các hệ thống nhận thức đa phương thức tự suy luận sâu. Các siêu máy tính lượng tử kết hợp cùng mạng nơ-ron nhân tạo cho phép mô phỏng lại cấu trúc vật chất tối trong vũ trụ...",
            chapters = listOf(
                VideoChapter(0, "Mở đầu & Bối cảnh AGI 2026"),
                VideoChapter(120, "Mạng nơ-ron lượng tử và tính toán đa chiều"),
                VideoChapter(280, "Khám phá không gian liên sao bằng vệ tinh tự hành"),
                VideoChapter(450, "Tương lai loài người và kỷ nguyên tiếp theo")
            )
        ),
        VideoItem(
            id = "vid_2",
            title = "Lo-Fi Hip Hop Chill Beats - Âm Nhạc Học Tập & Thư Giãn Đêm Khuya 🎧 [Ad-Free]",
            description = "Tuyển tập những giai điệu Lo-fi êm dịu nhất giúp bạn tập trung học tập, lập trình hoặc thư giãn đi ngủ. Hỗ trợ phát ngầm khi tắt màn hình mà không bị gián đoạn.",
            channelName = "Lofi Girl Vietnam",
            channelAvatarUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=150&auto=format&fit=crop&q=80",
            subscriberCount = "5.1M",
            thumbnailUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=800&auto=format&fit=crop&q=80",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
            durationSeconds = 653,
            durationFormatted = "10:53",
            viewCount = "4.9M lượt xem",
            publishedTime = "1 tuần trước",
            category = "Music",
            isShort = false,
            likesCount = "390K",
            transcript = "Danh sách nhạc chill lo-fi thư giãn: 01. Midnight Coffee 02. Rainy Study Session 03. Street Lights In Tokyo 04. Coding Through Dawn. Tất cả bản nhạc đều được tối ưu âm thanh lossless 320kbps.",
            chapters = listOf(
                VideoChapter(0, "01. Midnight Coffee (Lo-Fi Nostalgia)"),
                VideoChapter(180, "02. Rainy Street Lights"),
                VideoChapter(360, "03. Gentle Coding & Focus Vibe"),
                VideoChapter(500, "04. Sleep & Dream Melodies")
            )
        ),
        VideoItem(
            id = "vid_3",
            title = "Thiên Nhiên Việt Nam Hùng Vĩ 4K HDR: Sơn Đoòng, Hà Giang & Vịnh Hạ Long",
            description = "Trải nghiệm thước phim tài liệu du lịch chuẩn điện ảnh 4K HDR Dolby Vision về những kỳ quan thiên nhiên ngoạn mục nhất của dải đất hình chữ S.",
            channelName = "Vietnam Discovery TV",
            channelAvatarUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=150&auto=format&fit=crop&q=80",
            subscriberCount = "920K",
            thumbnailUrl = "https://images.unsplash.com/photo-1528127269322-539801943592?w=800&auto=format&fit=crop&q=80",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
            durationSeconds = 90,
            durationFormatted = "01:30",
            viewCount = "890K lượt xem",
            publishedTime = "3 ngày trước",
            category = "4K HDR",
            isShort = false,
            likesCount = "95K",
            transcript = "Việt Nam - vùng đất của những cảnh quan kỳ vĩ. Hang Sơn Đoòng, hang động tự nhiên lớn nhất thế giới, chứa đựng cả một hệ sinh thái rừng rậm và dòng sông ngầm độc nhất vô nhị. Cao nguyên đá Đồng Văn Hà Giang sừng sững uốn lượn...",
            chapters = listOf(
                VideoChapter(0, "Sơn Đoòng: Kỳ quan lòng đất"),
                VideoChapter(30, "Hà Giang: Đèo Mã Pí Lèng mây ngàn"),
                VideoChapter(60, "Hạ Long: Vịnh di sản thế giới")
            )
        ),
        VideoItem(
            id = "vid_4",
            title = "Lập Trình Android Hiện Đại 2026: Jetpack Compose, Clean Architecture & AI Co-pilot",
            description = "Khóa học thực chiến xây dựng ứng dụng Android cao cấp với Jetpack Compose, Coroutines Flow, Room Database và tích hợp trợ lý AI Gemini.",
            channelName = "Android Dev Pro VN",
            channelAvatarUrl = "https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?w=150&auto=format&fit=crop&q=80",
            subscriberCount = "450K",
            thumbnailUrl = "https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=800&auto=format&fit=crop&q=80",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
            durationSeconds = 120,
            durationFormatted = "02:00",
            viewCount = "310K lượt xem",
            publishedTime = "5 ngày trước",
            category = "Tech",
            isShort = false,
            likesCount = "42K",
            transcript = "Trong bài học này, chúng ta sẽ tìm hiểu cách kiến trúc một ứng dụng Android quy mô lớn theo Clean Architecture, tách biệt rõ Data, Domain và UI layers, kết hợp cùng StateFlow và Jetpack Compose Material 3...",
            chapters = listOf(
                VideoChapter(0, "Giới thiệu & Tổng quan kiến trúc"),
                VideoChapter(40, "Thiết kế UI Declarative với Compose"),
                VideoChapter(80, "Tối ưu hóa Recomposition & State")
            )
        ),
        VideoItem(
            id = "vid_5",
            title = "Podcast #88: Tương Lai Tài Chính & Đầu Tư Công Nghệ Cùng Shark Bình & Chuyên Gia",
            description = "Trò chuyện sâu sắc về xu hướng kinh tế vĩ mô, cơ hội đầu tư trong làn sóng AI và quản lý tài chính cá nhân thông minh trong năm 2026.",
            channelName = "Việt Success Podcast",
            channelAvatarUrl = "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=150&auto=format&fit=crop&q=80",
            subscriberCount = "1.8M",
            thumbnailUrl = "https://images.unsplash.com/photo-1478737270239-2f02b77fc618?w=800&auto=format&fit=crop&q=80",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
            durationSeconds = 180,
            durationFormatted = "03:00",
            viewCount = "750K lượt xem",
            publishedTime = "1 ngày trước",
            category = "Podcasts",
            isShort = false,
            likesCount = "68K",
            transcript = "Chào mừng quý khán thính giả đến với tập 88 của Việt Success. Hôm nay chúng ta sẽ mổ xẻ câu chuyện về cách các quỹ đầu tư mạo hiểm định giá các startup AI và các chiến lược bảo toàn vốn trong kỷ nguyên biến động nhanh...",
            chapters = listOf(
                VideoChapter(0, "Chào đầu & Giới thiệu khách mời"),
                VideoChapter(60, "Tác động của AI đến thị trường lao động"),
                VideoChapter(120, "Chiến lược phân bổ danh mục đầu tư")
            )
        ),
        VideoItem(
            id = "vid_6",
            title = "Chung Kết Thế Giới Esports 2026: Trận Chiến Siêu Kinh Điển 5 Ván Đỉnh Cao",
            description = "Highlight trận đấu nghẹt thở giữa hai đội tuyển hàng đầu thế giới với những pha combat đỉnh cao và chiến thuật biến ảo không ngờ.",
            channelName = "Esports VN Official",
            channelAvatarUrl = "https://images.unsplash.com/photo-1542751371-adc38448a05e?w=150&auto=format&fit=crop&q=80",
            subscriberCount = "3.4M",
            thumbnailUrl = "https://images.unsplash.com/photo-1542751371-adc38448a05e?w=800&auto=format&fit=crop&q=80",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyBlazes.mp4",
            durationSeconds = 240,
            durationFormatted = "04:00",
            viewCount = "2.1M lượt xem",
            publishedTime = "4 ngày trước",
            category = "Gaming",
            isShort = false,
            likesCount = "210K",
            transcript = "Pha tranh chấp Baron sinh tử ở phút thứ 32! Cả 5 thành viên của đội đỏ dồn toàn lực, nhưng người đi rừng đội xanh đã có pha Tốc Biến Trừng Phạt cướp mục tiêu không tưởng...",
            chapters = listOf(
                VideoChapter(0, "Giai đoạn đi đường ván 5"),
                VideoChapter(90, "Giao tranh rồng ngàn tuổi"),
                VideoChapter(180, "Pha đẩy nhà chính định đoạt ngôi vương")
            )
        ),
        // Shorts
        VideoItem(
            id = "short_1",
            title = "Mẹo làm việc với AI giúp bạn tiết kiệm 4 tiếng mỗi ngày! ⚡ #shorts #tips",
            description = "Thủ thuật prompt đỉnh cao trong năm 2026.",
            channelName = "Tech Hacks VN",
            channelAvatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150&auto=format&fit=crop&q=80",
            subscriberCount = "800K",
            thumbnailUrl = "https://images.unsplash.com/photo-1519389950473-47ba0277781c?w=600&auto=format&fit=crop&q=80",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
            durationSeconds = 45,
            durationFormatted = "00:45",
            viewCount = "1.9M lượt xem",
            publishedTime = "12 giờ trước",
            category = "Shorts",
            isShort = true,
            likesCount = "240K"
        ),
        VideoItem(
            id = "short_2",
            title = "Cảnh hoàng hôn triệu view tại đỉnh Fansipan Sapa 🌄 #shorts #vietnam",
            description = "Biển mây bồng bềnh lúc hoàng hôn buông xuống.",
            channelName = "Travel Passion",
            channelAvatarUrl = "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=150&auto=format&fit=crop&q=80",
            subscriberCount = "620K",
            thumbnailUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=600&auto=format&fit=crop&q=80",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4",
            durationSeconds = 30,
            durationFormatted = "00:30",
            viewCount = "3.2M lượt xem",
            publishedTime = "1 ngày trước",
            category = "Shorts",
            isShort = true,
            likesCount = "450K"
        ),
        VideoItem(
            id = "short_3",
            title = "Review bàn phím cơ Custom âm thanh gõ cực đã tai ⌨️🎧 #shorts #asmr",
            description = "Sound test bàn phím cơ lube switch tactile cực mịn.",
            channelName = "Custom Keyboards",
            channelAvatarUrl = "https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?w=150&auto=format&fit=crop&q=80",
            subscriberCount = "340K",
            thumbnailUrl = "https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=600&auto=format&fit=crop&q=80",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WhatCarCanYouGetForAGrand.mp4",
            durationSeconds = 40,
            durationFormatted = "00:40",
            viewCount = "890K lượt xem",
            publishedTime = "3 ngày trước",
            category = "Shorts",
            isShort = true,
            likesCount = "115K"
        )
    )

    val sampleComments: List<CommentItem> = listOf(
        CommentItem(
            id = "c_1",
            authorName = "Hoàng Nam Dev",
            authorAvatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100&auto=format&fit=crop&q=80",
            text = "Xem trên TubePremium thật sự đỉnh, không có một giây quảng cáo nào và vừa tắt màn hình đi bộ vừa nghe rất tiện! Cảm ơn đội ngũ phát triển!",
            timestamp = "2 giờ trước",
            likesCount = 428,
            isVipMember = true
        ),
        CommentItem(
            id = "c_2",
            authorName = "Mai Linh Vũ",
            authorAvatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=100&auto=format&fit=crop&q=80",
            text = "Tính năng AI Deep Thinking phân tích tóm tắt nội dung video chuẩn xác từng phút luôn, học bài cực nhanh!",
            timestamp = "5 giờ trước",
            likesCount = 215,
            isVipMember = true
        ),
        CommentItem(
            id = "c_3",
            authorName = "Khoa Học Vũ Trụ & AI",
            authorAvatarUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=100&auto=format&fit=crop&q=80",
            text = "Cảm ơn các bạn đã ủng hộ video 4K HDR! Hãy để lại câu hỏi để mình giải đáp ở các video tiếp theo nhé.",
            timestamp = "1 ngày trước",
            likesCount = 1040,
            isChannelOwner = true
        )
    )

    val samplePlaylists: List<Playlist> = listOf(
        Playlist(
            id = "pl_1",
            title = "Nhạc Chill Học Tập & Làm Việc Ban Đêm",
            description = "Playlist tổng hợp các bài nhạc không lời, Lo-fi chất lượng 320kbps phát liên tục không quảng cáo.",
            coverUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80",
            videoCount = 12
        ),
        Playlist(
            id = "pl_2",
            title = "Khoa Học & Trí Tuệ Nhân Tạo AGI 2026",
            description = "Tuyển tập những bài giảng, tài liệu khoa học khám phá tương lai công nghệ.",
            coverUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=600&auto=format&fit=crop&q=80",
            videoCount = 8
        ),
        Playlist(
            id = "pl_3",
            title = "Podcast Nâng Cao Tư Duy & Đầu Tư",
            description = "Những buổi trò chuyện cùng các chuyên gia hàng đầu.",
            coverUrl = "https://images.unsplash.com/photo-1478737270239-2f02b77fc618?w=600&auto=format&fit=crop&q=80",
            videoCount = 15
        )
    )
}
