package com.hanviet.etymoai.data.hanja

data class HanjaMasterInfo(
    val hanjaChar: String,
    val readingVi: String,
    val strokeCount: Int,
    val radicalChar: String,
    val radicalMeaning: String,
    val gradeLevel: String,
    val meaningSummary: String,
    val derivedWords: List<HanjaDerivedWord>
)

data class HanjaDerivedWord(
    val lexemeId: Long,
    val headwordKo: String,
    val hanjaCombo: String,
    val readingVi: String,
    val meaningVi: String
)

object HanjaMasterData {

    val allHanjaList: List<HanjaMasterInfo> = listOf(
        HanjaMasterInfo(
            hanjaChar = "學",
            readingVi = "HỌC",
            strokeCount = 16,
            radicalChar = "子",
            radicalMeaning = "Tử (đứa trẻ)",
            gradeLevel = "Cấp 8 (Sơ cấp)",
            meaningSummary = "Bắt chước, học tập, tiếp thu tri thức và rèn luyện kỹ năng qua quá trình đào tạo",
            derivedWords = listOf(
                HanjaDerivedWord(101L, "학교", "學校", "Học hiệu", "Trường học, cơ sở giáo dục đào tạo"),
                HanjaDerivedWord(102L, "학생", "學生", "Học sinh", "Người theo học tại nhà trường"),
                HanjaDerivedWord(103L, "학습", "學習", "Học tập", "Sự tiếp thu tri thức và tự rèn luyện"),
                HanjaDerivedWord(104L, "학원", "學院", "Học viện", "Viện học thuật, trung tâm bồi dưỡng năng khiếu"),
                HanjaDerivedWord(105L, "학문", "學問", "Học vấn", "Tri thức học thuật chuyên sâu và nền tảng"),
                HanjaDerivedWord(106L, "장학금", "奬學金", "Tưởng học kim", "Học bổng khen thưởng động viên học tập")
            )
        ),
        HanjaMasterInfo(
            hanjaChar = "生",
            readingVi = "SINH",
            strokeCount = 5,
            radicalChar = "生",
            radicalMeaning = "Sinh (mầm sống vươn lên)",
            gradeLevel = "Cấp 8 (Sơ cấp)",
            meaningSummary = "Sinh nở, sinh ra, sự sống, tạo vật, người đang sống",
            derivedWords = listOf(
                HanjaDerivedWord(201L, "선생님", "先生님", "Tiên sinh", "Người sinh trước, thầy cô giáo đáng kính"),
                HanjaDerivedWord(202L, "학생", "學生", "Học sinh", "Người theo học tại nhà trường"),
                HanjaDerivedWord(203L, "생활", "生活", "Sinh hoạt", "Đời sống, nếp sống sinh hoạt thường nhật"),
                HanjaDerivedWord(204L, "생명", "生命", "Sinh mệnh", "Tính mạng, mạng sống thiêng liêng"),
                HanjaDerivedWord(205L, "생일", "生日", "Sinh nhật", "Ngày kỷ niệm ra đời của mỗi người")
            )
        ),
        HanjaMasterInfo(
            hanjaChar = "校",
            readingVi = "HIỆU",
            strokeCount = 10,
            radicalChar = "木",
            radicalMeaning = "Mộc (gỗ, cây cối)",
            gradeLevel = "Cấp 8 (Sơ cấp)",
            meaningSummary = "Trường học, nơi so sánh đối chiếu văn bản, cơ sở giáo dục",
            derivedWords = listOf(
                HanjaDerivedWord(301L, "학교", "學校", "Học hiệu", "Trường học, nơi giảng dạy"),
                HanjaDerivedWord(302L, "교장", "校長", "Hiệu trưởng", "Người đứng đầu quản lý nhà trường"),
                HanjaDerivedWord(303L, "교가", "校歌", "Hiệu ca", "Bài hát truyền thống của trường"),
                HanjaDerivedWord(304L, "교문", "校門", "Hiệu môn", "Cổng trường học")
            )
        ),
        HanjaMasterInfo(
            hanjaChar = "圖",
            readingVi = "ĐỒ",
            strokeCount = 14,
            radicalChar = "囗",
            radicalMeaning = "Vi (khung bao quanh)",
            gradeLevel = "Cấp 6 (Trung cấp)",
            meaningSummary = "Bản đồ, hình vẽ phác thảo, mưu tính, sách vở đồ họa",
            derivedWords = listOf(
                HanjaDerivedWord(401L, "도서관", "圖書館", "Đồ thư quán", "Thư viện, nơi lưu trữ sách và tài liệu"),
                HanjaDerivedWord(402L, "지도", "地圖", "Địa đồ", "Bản đồ địa lý chỉ đường"),
                HanjaDerivedWord(403L, "의도", "意圖", "Ý đồ", "Ý định, mục đích dự tính trước"),
                HanjaDerivedWord(404L, "그림/도안", "圖案", "Đồ án", "Bản vẽ thiết kế mẫu")
            )
        ),
        HanjaMasterInfo(
            hanjaChar = "書",
            readingVi = "THƯ",
            strokeCount = 10,
            radicalChar = "曰",
            radicalMeaning = "Viết (nói rằng)",
            gradeLevel = "Cấp 7 (Sơ cấp)",
            meaningSummary = "Sách vở, chữ viết, thư từ, văn bản ghi chép",
            derivedWords = listOf(
                HanjaDerivedWord(501L, "도서관", "圖書館", "Đồ thư quán", "Thư viện lưu trữ sách"),
                HanjaDerivedWord(502L, "교과서", "敎科書", "Giáo khoa thư", "Sách giáo khoa giảng dạy"),
                HanjaDerivedWord(503L, "서점", "書店", "Thư điếm", "Hiệu sách, cửa hàng bán sách"),
                HanjaDerivedWord(504L, "서류", "書類", "Thư loại", "Giấy tờ, tài liệu hồ sơ")
            )
        ),
        HanjaMasterInfo(
            hanjaChar = "館",
            readingVi = "QUÁN",
            strokeCount = 16,
            radicalChar = "食",
            radicalMeaning = "Thực (ăn uống)",
            gradeLevel = "Cấp 5 (Trung cấp)",
            meaningSummary = "Tòa nhà lớn, công sở công cộng, nơi đón tiếp khách hoặc lưu trữ",
            derivedWords = listOf(
                HanjaDerivedWord(601L, "도서관", "圖書館", "Đồ thư quán", "Thư viện chứa sách"),
                HanjaDerivedWord(602L, "박물관", "博物館", "Bác vật quán", "Bảo tàng lưu giữ hiện vật"),
                HanjaDerivedWord(603L, "대사관", "大使館", "Đại sứ quán", "Cơ quan ngoại giao của đại sứ"),
                HanjaDerivedWord(604L, "체육관", "體育館", "Thể dục quán", "Nhà thi đấu thể thao")
            )
        ),
        HanjaMasterInfo(
            hanjaChar = "人",
            readingVi = "NHÂN",
            strokeCount = 2,
            radicalChar = "人",
            radicalMeaning = "Nhân (con người)",
            gradeLevel = "Cấp 8 (Sơ cấp)",
            meaningSummary = "Con người, nhân loại, người khác",
            derivedWords = listOf(
                HanjaDerivedWord(701L, "인간", "人間", "Nhân gian", "Con người, nhân loại"),
                HanjaDerivedWord(702L, "인구", "人口", "Nhân khẩu", "Dân số"),
                HanjaDerivedWord(703L, "외국인", "外國人", "Ngoại quốc nhân", "Người nước ngoài"),
                HanjaDerivedWord(704L, "주인", "主人", "Chủ nhân", "Người làm chủ, chủ nhà")
            )
        ),
        HanjaMasterInfo(
            hanjaChar = "大",
            readingVi = "ĐẠI",
            strokeCount = 3,
            radicalChar = "大",
            radicalMeaning = "Đại (to lớn)",
            gradeLevel = "Cấp 8 (Sơ cấp)",
            meaningSummary = "To lớn, vĩ đại, quan trọng, bậc cao nhất",
            derivedWords = listOf(
                HanjaDerivedWord(801L, "대학교", "大學校", "Đại học hiệu", "Trường đại học"),
                HanjaDerivedWord(802L, "대사관", "大使館", "Đại sứ quán", "Đại sứ quán"),
                HanjaDerivedWord(803L, "대통령", "大統領", "Đại thống lĩnh", "Tổng thống"),
                HanjaDerivedWord(804L, "확대", "擴大", "Khuếch đại", "Mở rộng, phóng to")
            )
        ),
        HanjaMasterInfo(
            hanjaChar = "小",
            readingVi = "TIỂU",
            strokeCount = 3,
            radicalChar = "小",
            radicalMeaning = "Tiểu (nhỏ bé)",
            gradeLevel = "Cấp 8 (Sơ cấp)",
            meaningSummary = "Nhỏ bé, vụn vặt, ít ỏi, khiêm tốn",
            derivedWords = listOf(
                HanjaDerivedWord(901L, "초등학교", "初等學校", "Sơ đẳng học hiệu", "Trường tiểu học"),
                HanjaDerivedWord(902L, "소설", "小說", "Tiểu thuyết", "Tác phẩm truyện ngắn/tiểu thuyết"),
                HanjaDerivedWord(903L, "축소", "縮小", "Súc tiểu", "Thu nhỏ lại")
            )
        ),
        HanjaMasterInfo(
            hanjaChar = "中",
            readingVi = "TRUNG",
            strokeCount = 4,
            radicalChar = "丨",
            radicalMeaning = "Cổn (nét thẳng đứng)",
            gradeLevel = "Cấp 8 (Sơ cấp)",
            meaningSummary = "Ở giữa, trung tâm, trung bình, bên trong",
            derivedWords = listOf(
                HanjaDerivedWord(1001L, "중학교", "中學校", "Trung học hiệu", "Trường trung học cơ sở"),
                HanjaDerivedWord(1002L, "중심", "中心", "Trung tâm", "Điểm chính giữa, trọng tâm"),
                HanjaDerivedWord(1003L, "중국", "中國", "Trung Quốc", "Nước Trung Quốc")
            )
        ),
        HanjaMasterInfo(
            hanjaChar = "國",
            readingVi = "QUỐC",
            strokeCount = 11,
            radicalChar = "囗",
            radicalMeaning = "Vi (khung bao)",
            gradeLevel = "Cấp 8 (Sơ cấp)",
            meaningSummary = "Quốc gia, đất nước, quê hương",
            derivedWords = listOf(
                HanjaDerivedWord(1101L, "한국", "韓國", "Hàn Quốc", "Đất nước Hàn Quốc"),
                HanjaDerivedWord(1102L, "국제", "國際", "Quốc tế", "Giữa các quốc gia"),
                HanjaDerivedWord(1103L, "국가", "國家", "Quốc gia", "Nhà nước, tổ quốc"),
                HanjaDerivedWord(1104L, "외국", "外國", "Ngoại quốc", "Nước ngoài")
            )
        ),
        HanjaMasterInfo(
            hanjaChar = "語",
            readingVi = "NGỮ",
            strokeCount = 14,
            radicalChar = "言",
            radicalMeaning = "Ngôn (lời nói)",
            gradeLevel = "Cấp 6 (Trung cấp)",
            meaningSummary = "Ngôn ngữ, tiếng nói, lời bàn luận",
            derivedWords = listOf(
                HanjaDerivedWord(1201L, "한국어", "韓國語", "Hàn Quốc ngữ", "Tiếng Hàn"),
                HanjaDerivedWord(1202L, "언어", "言語", "Ngôn ngữ", "Tiếng nói và chữ viết"),
                HanjaDerivedWord(1203L, "단어", "單語", "Đơn ngữ", "Từ vựng"),
                HanjaDerivedWord(1204L, "영어", "英語", "Anh ngữ", "Tiếng Anh")
            )
        ),
        HanjaMasterInfo(
            hanjaChar = "日",
            readingVi = "NHẬT",
            strokeCount = 4,
            radicalChar = "日",
            radicalMeaning = "Nhật (mặt trời, ngày)",
            gradeLevel = "Cấp 8 (Sơ cấp)",
            meaningSummary = "Mặt trời, ban ngày, ngày trong tuần",
            derivedWords = listOf(
                HanjaDerivedWord(1301L, "일요일", "日曜日", "Nhật diệu nhật", "Chủ nhật"),
                HanjaDerivedWord(1302L, "일본", "日本", "Nhật Bản", "Nước Nhật Bản"),
                HanjaDerivedWord(1303L, "생일", "生日", "Sinh nhật", "Ngày sinh"),
                HanjaDerivedWord(1304L, "매일", "每日", "Mỗi nhật", "Mỗi ngày, hàng ngày")
            )
        ),
        HanjaMasterInfo(
            hanjaChar = "月",
            readingVi = "NGUYỆT",
            strokeCount = 4,
            radicalChar = "月",
            radicalMeaning = "Nguyệt (mặt trăng, tháng)",
            gradeLevel = "Cấp 8 (Sơ cấp)",
            meaningSummary = "Mặt trăng, tháng trong năm",
            derivedWords = listOf(
                HanjaDerivedWord(1401L, "월요일", "月曜日", "Nguyệt diệu nhật", "Thứ hai"),
                HanjaDerivedWord(1402L, "세월", "歲月", "Tuế nguyệt", "Năm tháng, thời gian"),
                HanjaDerivedWord(1403L, "매월", "每月", "Mỗi nguyệt", "Mỗi tháng")
            )
        ),
        HanjaMasterInfo(
            hanjaChar = "年",
            readingVi = "NIÊN",
            strokeCount = 6,
            radicalChar = "干",
            radicalMeaning = "Can (khiên chắn)",
            gradeLevel = "Cấp 8 (Sơ cấp)",
            meaningSummary = "Năm, tuổi tác, mùa vụ trong năm",
            derivedWords = listOf(
                HanjaDerivedWord(1501L, "학년", "學年", "Học niên", "Khối lớp, năm học"),
                HanjaDerivedWord(1502L, "내년", "來年", "Lai niên", "Năm sau"),
                HanjaDerivedWord(1503L, "작년", "昨年", "Tác niên", "Năm ngoái"),
                HanjaDerivedWord(1504L, "청년", "靑年", "Thanh niên", "Tuổi trẻ, thanh niên")
            )
        ),
        HanjaMasterInfo(
            hanjaChar = "時",
            readingVi = "THỜI",
            strokeCount = 10,
            radicalChar = "日",
            radicalMeaning = "Nhật (mặt trời)",
            gradeLevel = "Cấp 7 (Sơ cấp)",
            meaningSummary = "Thời gian, giờ giấc, mùa màng, thời cơ",
            derivedWords = listOf(
                HanjaDerivedWord(1601L, "시간", "時間", "Thời gian", "Khoảng thời gian"),
                HanjaDerivedWord(1602L, "시대", "時代", "Thời đại", "Giai đoạn lịch sử"),
                HanjaDerivedWord(1603L, "시계", "時計", "Thời kế", "Đồng hồ xem giờ")
            )
        ),
        HanjaMasterInfo(
            hanjaChar = "間",
            readingVi = "GIAN",
            strokeCount = 12,
            radicalChar = "門",
            radicalMeaning = "Môn (cửa)",
            gradeLevel = "Cấp 7 (Sơ cấp)",
            meaningSummary = "Khoảng giữa, không gian, phòng ốc",
            derivedWords = listOf(
                HanjaDerivedWord(1701L, "시간", "時間", "Thời gian", "Thời giờ trôi qua"),
                HanjaDerivedWord(1702L, "공간", "空間", "Không gian", "Khoảng không gian"),
                HanjaDerivedWord(1703L, "인간", "人間", "Nhân gian", "Con người")
            )
        ),
        HanjaMasterInfo(
            hanjaChar = "心",
            readingVi = "TÂM",
            strokeCount = 4,
            radicalChar = "心",
            radicalMeaning = "Tâm (trái tim)",
            gradeLevel = "Cấp 7 (Sơ cấp)",
            meaningSummary = "Trái tim, tấm lòng, tâm trí, trung tâm",
            derivedWords = listOf(
                HanjaDerivedWord(1801L, "관심", "關心", "Quan tâm", "Sự chú ý, lưu tâm"),
                HanjaDerivedWord(1802L, "안심", "安心", "An tâm", "Yên lòng, không lo lắng"),
                HanjaDerivedWord(1803L, "중심", "中心", "Trung tâm", "Điểm cốt lõi giữa"),
                HanjaDerivedWord(1804L, "심리", "心理", "Tâm lý", "Tâm tư, tinh thần")
            )
        ),
        HanjaMasterInfo(
            hanjaChar = "身",
            readingVi = "THÂN",
            strokeCount = 7,
            radicalChar = "身",
            radicalMeaning = "Thân (thân thể)",
            gradeLevel = "Cấp 6 (Trung cấp)",
            meaningSummary = "Thân thể, bản thân, địa vị con người",
            derivedWords = listOf(
                HanjaDerivedWord(1901L, "자신", "自身", "Tự thân", "Bản thân mình"),
                HanjaDerivedWord(1902L, "신체", "身體", "Thân thể", "Cơ thể con người"),
                HanjaDerivedWord(1903L, "출신", "出身", "Xuất thân", "Nguồn gốc xuất thân")
            )
        ),
        HanjaMasterInfo(
            hanjaChar = "動",
            readingVi = "ĐỘNG",
            strokeCount = 11,
            radicalChar = "力",
            radicalMeaning = "Lực (sức mạnh)",
            gradeLevel = "Cấp 6 (Trung cấp)",
            meaningSummary = "Chuyển động, hành động, dao động, khởi động",
            derivedWords = listOf(
                HanjaDerivedWord(2001L, "운동", "運動", "Vận động", "Tập thể dục, vận động"),
                HanjaDerivedWord(2002L, "행동", "行動", "Hành động", "Cách cư xử, hành vi"),
                HanjaDerivedWord(2003L, "동물", "動物", "Động vật", "Loài vật di chuyển được"),
                HanjaDerivedWord(2004L, "감동", "感動", "Cảm động", "Xúc động trong lòng")
            )
        ),
        HanjaMasterInfo(
            hanjaChar = "作",
            readingVi = "TÁC",
            strokeCount = 7,
            radicalChar = "亻",
            radicalMeaning = "Nhân (người)",
            gradeLevel = "Cấp 6 (Trung cấp)",
            meaningSummary = "Làm ra, sáng tác, chế tạo, hành vi",
            derivedWords = listOf(
                HanjaDerivedWord(2101L, "작업", "作業", "Tác nghiệp", "Công việc thao tác"),
                HanjaDerivedWord(2102L, "작품", "作品", "Tác phẩm", "Sản phẩm nghệ thuật"),
                HanjaDerivedWord(2103L, "시작", "始作", "Thủy tác", "Bắt đầu khởi sự")
            )
        ),
        HanjaMasterInfo(
            hanjaChar = "物",
            readingVi = "VẬT",
            strokeCount = 8,
            radicalChar = "牛",
            radicalMeaning = "Ngưu (con trâu, bò)",
            gradeLevel = "Cấp 6 (Trung cấp)",
            meaningSummary = "Vật thể, đồ vật, sinh vật, sự vật",
            derivedWords = listOf(
                HanjaDerivedWord(2201L, "동물", "動物", "Động vật", "Sinh vật sống"),
                HanjaDerivedWord(2202L, "식물", "植物", "Thực vật", "Cây cối"),
                HanjaDerivedWord(2203L, "물건", "物件", "Vật kiện", "Đồ đạc, đồ dùng"),
                HanjaDerivedWord(2204L, "건물", "建物", "Kiến vật", "Tòa nhà, công trình")
            )
        ),
        HanjaMasterInfo(
            hanjaChar = "事",
            readingVi = "SỰ",
            strokeCount = 8,
            radicalChar = "亅",
            radicalMeaning = "Quyết (móc câu)",
            gradeLevel = "Cấp 6 (Trung cấp)",
            meaningSummary = "Sự việc, việc làm, công tác, phụng sự",
            derivedWords = listOf(
                HanjaDerivedWord(2301L, "식사", "食事", "Thực sự", "Bữa ăn uống"),
                HanjaDerivedWord(2302L, "사건", "事件", "Sự kiện", "Vụ việc xảy ra"),
                HanjaDerivedWord(2303L, "사업", "事業", "Sự nghiệp", "Kinh doanh, sự nghiệp"),
                HanjaDerivedWord(2304L, "사실", "事實", "Sự thật", "Sự thật khách quan")
            )
        ),
        HanjaMasterInfo(
            hanjaChar = "業",
            readingVi = "NGHIỆP",
            strokeCount = 13,
            radicalChar = "木",
            radicalMeaning = "Mộc (gỗ)",
            gradeLevel = "Cấp 5 (Trung cấp)",
            meaningSummary = "Nghề nghiệp, công việc, thành tựu, sự nghiệp",
            derivedWords = listOf(
                HanjaDerivedWord(2401L, "수업", "授業", "Thụ nghiệp", "Buổi học, tiết học"),
                HanjaDerivedWord(2402L, "직업", "職業", "Chức nghiệp", "Nghề nghiệp chuyên môn"),
                HanjaDerivedWord(2403L, "졸업", "卒業", "Tốt nghiệp", "Tốt nghiệp khóa học"),
                HanjaDerivedWord(2404L, "영업", "營業", "Doanh nghiệp", "Bán hàng, kinh doanh")
            )
        ),
        HanjaMasterInfo(
            hanjaChar = "力",
            readingVi = "LỰC",
            strokeCount = 2,
            radicalChar = "力",
            radicalMeaning = "Lực (sức mạnh bắp tay)",
            gradeLevel = "Cấp 7 (Sơ cấp)",
            meaningSummary = "Sức lực, khả năng, năng lực, quyền lực",
            derivedWords = listOf(
                HanjaDerivedWord(2501L, "능력", "能力", "Năng lực", "Khả năng làm việc"),
                HanjaDerivedWord(2502L, "노력", "努力", "Nỗ lực", "Sự cố gắng hết mình"),
                HanjaDerivedWord(2503L, "체력", "體力", "Thể lực", "Sức khỏe thể chất")
            )
        ),
        HanjaMasterInfo(
            hanjaChar = "水",
            readingVi = "THỦY",
            strokeCount = 4,
            radicalChar = "水",
            radicalMeaning = "Thủy (nước)",
            gradeLevel = "Cấp 8 (Sơ cấp)",
            meaningSummary = "Nước, chất lỏng, sông suối",
            derivedWords = listOf(
                HanjaDerivedWord(2601L, "수요일", "水曜日", "Thủy diệu nhật", "Thứ tư"),
                HanjaDerivedWord(2602L, "음료수", "飮料水", "Ẩm liệu thủy", "Nước uống giải khát"),
                HanjaDerivedWord(2603L, "생수", "生水", "Sinh thủy", "Nước khoáng đóng chai"),
                HanjaDerivedWord(2604L, "수영", "水泳", "Thủy vịnh", "Bơi lội dưới nước")
            )
        ),
        HanjaMasterInfo(
            hanjaChar = "火",
            readingVi = "HỎA",
            strokeCount = 4,
            radicalChar = "火",
            radicalMeaning = "Hỏa (ngọn lửa)",
            gradeLevel = "Cấp 8 (Sơ cấp)",
            meaningSummary = "Ngọn lửa, sức nóng, giận dữ",
            derivedWords = listOf(
                HanjaDerivedWord(2701L, "화요일", "火曜日", "Hỏa diệu nhật", "Thứ ba"),
                HanjaDerivedWord(2702L, "화재", "火災", "Hỏa tai", "Vụ hỏa hoạn, cháy nhà"),
                HanjaDerivedWord(2703L, "소화기", "消火器", "Tiêu hỏa khí", "Bình chữa cháy")
            )
        ),
        HanjaMasterInfo(
            hanjaChar = "金",
            readingVi = "KIM",
            strokeCount = 8,
            radicalChar = "金",
            radicalMeaning = "Kim (kim loại, vàng)",
            gradeLevel = "Cấp 8 (Sơ cấp)",
            meaningSummary = "Vàng, kim loại quý, tiền tệ",
            derivedWords = listOf(
                HanjaDerivedWord(2801L, "금요일", "金曜日", "Kim diệu nhật", "Thứ sáu"),
                HanjaDerivedWord(2802L, "요금", "料金", "Liệu kim", "Tiền phí dịch vụ"),
                HanjaDerivedWord(2803L, "장학금", "奬學金", "Tưởng học kim", "Học bổng"),
                HanjaDerivedWord(2804L, "현금", "現金", "Hiện kim", "Tiền mặt")
            )
        ),
        HanjaMasterInfo(
            hanjaChar = "木",
            readingVi = "MỘC",
            strokeCount = 4,
            radicalChar = "木",
            radicalMeaning = "Mộc (cây cối)",
            gradeLevel = "Cấp 8 (Sơ cấp)",
            meaningSummary = "Cây gỗ, rừng cây, chất liệu gỗ",
            derivedWords = listOf(
                HanjaDerivedWord(2901L, "목요일", "木曜日", "Mộc diệu nhật", "Thứ năm"),
                HanjaDerivedWord(2902L, "목재", "木材", "Mộc tài", "Gỗ nguyên liệu"),
                HanjaDerivedWord(2903L, "수목원", "樹木園", "Thụ mộc viên", "Vườn thực vật, vườn ươm cây")
            )
        ),
        HanjaMasterInfo(
            hanjaChar = "土",
            readingVi = "THỔ",
            strokeCount = 3,
            radicalChar = "土",
            radicalMeaning = "Thổ (đất)",
            gradeLevel = "Cấp 8 (Sơ cấp)",
            meaningSummary = "Đất đai, mặt đất, bờ cõi thổ nhưỡng",
            derivedWords = listOf(
                HanjaDerivedWord(3001L, "토요일", "土曜日", "Thổ diệu nhật", "Thứ bảy"),
                HanjaDerivedWord(3002L, "토지", "土地", "Thổ địa", "Đất đai sử dụng"),
                HanjaDerivedWord(3003L, "국토", "國土", "Quốc thổ", "Lãnh thổ quốc gia")
            )
        ),
        HanjaMasterInfo(
            hanjaChar = "山",
            readingVi = "SƠN",
            strokeCount = 3,
            radicalChar = "山",
            radicalMeaning = "Sơn (ngọn núi)",
            gradeLevel = "Cấp 8 (Sơ cấp)",
            meaningSummary = "Núi non, gò đất cao, hùng vĩ",
            derivedWords = listOf(
                HanjaDerivedWord(3101L, "등산", "登山", "Đăng sơn", "Leo núi dã ngoại"),
                HanjaDerivedWord(3102L, "산림", "山林", "Sơn lâm", "Rừng núi rậm rạp"),
                HanjaDerivedWord(3103L, "화산", "火山", "Hỏa sơn", "Núi lửa phun trào")
            )
        ),
        HanjaMasterInfo(
            hanjaChar = "海",
            readingVi = "HẢI",
            strokeCount = 10,
            radicalChar = "氵",
            radicalMeaning = "Thủy (nước)",
            gradeLevel = "Cấp 6 (Trung cấp)",
            meaningSummary = "Biển cả, đại dương mênh mông",
            derivedWords = listOf(
                HanjaDerivedWord(3201L, "해외", "海外", "Hải ngoại", "Nước ngoài"),
                HanjaDerivedWord(3202L, "해안", "海岸", "Hải ngạn", "Bờ biển"),
                HanjaDerivedWord(3203L, "해수욕장", "海水浴場", "Hải thủy dục trường", "Bãi tắm biển")
            )
        ),
        HanjaMasterInfo(
            hanjaChar = "天",
            readingVi = "THIÊN",
            strokeCount = 4,
            radicalChar = "大",
            radicalMeaning = "Đại (to lớn)",
            gradeLevel = "Cấp 8 (Sơ cấp)",
            meaningSummary = "Bầu trời, thời tiết, tạo hóa tự nhiên",
            derivedWords = listOf(
                HanjaDerivedWord(3301L, "날씨/천기", "天氣", "Thiên khí", "Thời tiết"),
                HanjaDerivedWord(3302L, "천국", "天國", "Thiên quốc", "Thiên đường"),
                HanjaDerivedWord(3303L, "천연", "天然", "Thiên nhiên", "Tự nhiên, tự có")
            )
        ),
        HanjaMasterInfo(
            hanjaChar = "地",
            readingVi = "ĐỊA",
            strokeCount = 6,
            radicalChar = "土",
            radicalMeaning = "Thổ (đất)",
            gradeLevel = "Cấp 7 (Sơ cấp)",
            meaningSummary = "Mặt đất, địa điểm, khu vực, trái đất",
            derivedWords = listOf(
                HanjaDerivedWord(3401L, "지하철", "地下鐵", "Địa hạ thiết", "Tàu điện ngầm"),
                HanjaDerivedWord(3402L, "지도", "地圖", "Địa đồ", "Bản đồ"),
                HanjaDerivedWord(3403L, "지구", "地球", "Địa cầu", "Trái đất"),
                HanjaDerivedWord(3404L, "지역", "地域", "Địa vực", "Khu vực địa lý")
            )
        ),
        HanjaMasterInfo(
            hanjaChar = "道",
            readingVi = "ĐẠO",
            strokeCount = 13,
            radicalChar = "辶",
            radicalMeaning = "Sước (bước đi)",
            gradeLevel = "Cấp 6 (Trung cấp)",
            meaningSummary = "Con đường, đạo lý, phương pháp, tỉnh thành",
            derivedWords = listOf(
                HanjaDerivedWord(3501L, "도로", "道路", "Đạo lộ", "Đường sá giao thông"),
                HanjaDerivedWord(3502L, "지도", "指導", "Chỉ đạo", "Hướng dẫn, lãnh đạo"),
                HanjaDerivedWord(3503L, "도덕", "道德", "Đạo đức", "Phẩm chất đạo đức")
            )
        ),
        HanjaMasterInfo(
            hanjaChar = "文",
            readingVi = "VĂN",
            strokeCount = 4,
            radicalChar = "文",
            radicalMeaning = "Văn (hoa văn, chữ viết)",
            gradeLevel = "Cấp 8 (Sơ cấp)",
            meaningSummary = "Văn tự, chữ viết, văn hóa, bài văn",
            derivedWords = listOf(
                HanjaDerivedWord(3601L, "문화", "文化", "Văn hóa", "Nền văn hóa phong tục"),
                HanjaDerivedWord(3602L, "문장", "文章", "Văn chương", "Câu văn, đoạn văn"),
                HanjaDerivedWord(3603L, "외국어문", "文學", "Văn học", "Bộ môn văn học nghệ thuật"),
                HanjaDerivedWord(3604L, "문법", "文法", "Văn pháp", "Ngữ pháp câu")
            )
        ),
        HanjaMasterInfo(
            hanjaChar = "化",
            readingVi = "HÓA",
            strokeCount = 4,
            radicalChar = "匕",
            radicalMeaning = "Chủy (cái thìa, biến đổi)",
            gradeLevel = "Cấp 7 (Sơ cấp)",
            meaningSummary = "Biến hóa, thay đổi, cảm hóa, dạy dỗ",
            derivedWords = listOf(
                HanjaDerivedWord(3701L, "문화", "文化", "Văn hóa", "Nền văn minh phong tục"),
                HanjaDerivedWord(3702L, "화장품", "化粧品", "Hóa trang phẩm", "Mỹ phẩm làm đẹp"),
                HanjaDerivedWord(3703L, "변화", "變化", "Biến hóa", "Sự thay đổi chuyển biến"),
                HanjaDerivedWord(3704L, "화학", "化學", "Hóa học", "Môn hóa học")
            )
        ),
        HanjaMasterInfo(
            hanjaChar = "社",
            readingVi = "XÃ",
            strokeCount = 8,
            radicalChar = "示",
            radicalMeaning = "Kỳ (thần đất, đền tế)",
            gradeLevel = "Cấp 6 (Trung cấp)",
            meaningSummary = "Xã hội, hội đoàn, công ty đoàn thể",
            derivedWords = listOf(
                HanjaDerivedWord(3801L, "회사", "會社", "Hội xã", "Công ty doanh nghiệp"),
                HanjaDerivedWord(3802L, "사회", "社會", "Xã hội", "Cộng đồng xã hội loài người"),
                HanjaDerivedWord(3803L, "사원", "社員", "Xã viên", "Nhân viên công ty")
            )
        ),
        HanjaMasterInfo(
            hanjaChar = "會",
            readingVi = "HỘI",
            strokeCount = 13,
            radicalChar = "曰",
            radicalMeaning = "Viết (nói rằng)",
            gradeLevel = "Cấp 6 (Trung cấp)",
            meaningSummary = "Gặp gỡ, tụ họp, hội thảo, hiểu biết",
            derivedWords = listOf(
                HanjaDerivedWord(3901L, "회사", "會社", "Hội xã", "Công ty"),
                HanjaDerivedWord(3902L, "회의", "會議", "Hội nghị", "Cuộc họp bàn bạc"),
                HanjaDerivedWord(3903L, "기회", "機會", "Cơ hội", "Thời cơ thuận lợi"),
                HanjaDerivedWord(3904L, "회화", "會話", "Hội thoại", "Đàm thoại đối đáp")
            )
        ),
        HanjaMasterInfo(
            hanjaChar = "行",
            readingVi = "HÀNH",
            strokeCount = 6,
            radicalChar = "行",
            radicalMeaning = "Hành (bước đi)",
            gradeLevel = "Cấp 7 (Sơ cấp)",
            meaningSummary = "Đi lại, tiến hành, thi hành, ngân hàng",
            derivedWords = listOf(
                HanjaDerivedWord(4001L, "은행", "銀行", "Ngân hàng", "Nhà băng tài chính"),
                HanjaDerivedWord(4002L, "여행", "旅行", "Lữ hành", "Du lịch khám phá"),
                HanjaDerivedWord(4003L, "행동", "行動", "Hành động", "Cách cư xử, hành vi"),
                HanjaDerivedWord(4004L, "비행기", "飛行機", "Phi hành cơ", "Máy bay hàng không")
            )
        ),
        HanjaMasterInfo(
            hanjaChar = "車",
            readingVi = "XA",
            strokeCount = 7,
            radicalChar = "車",
            radicalMeaning = "Xa (xe cộ)",
            gradeLevel = "Cấp 7 (Sơ cấp)",
            meaningSummary = "Xe cộ, phương tiện bánh lăn",
            derivedWords = listOf(
                HanjaDerivedWord(4101L, "자동차", "自動車", "Tự động xa", "Xe ô tô"),
                HanjaDerivedWord(4102L, "자전거", "自轉車", "Tự chuyển xa", "Xe đạp"),
                HanjaDerivedWord(4103L, "주차장", "駐車場", "Trú xa trường", "Bãi đỗ xe"),
                HanjaDerivedWord(4104L, "기차", "汽車", "Khí xa", "Tàu hỏa chạy hơi nước")
            )
        ),
        HanjaMasterInfo(
            hanjaChar = "門",
            readingVi = "MÔN",
            strokeCount = 8,
            radicalChar = "門",
            radicalMeaning = "Môn (cửa lớn hai cánh)",
            gradeLevel = "Cấp 8 (Sơ cấp)",
            meaningSummary = "Cổng ra vào, cánh cửa, môn phái, ngành học",
            derivedWords = listOf(
                HanjaDerivedWord(4201L, "전문", "專門", "Chuyên môn", "Lĩnh vực chuyên sâu"),
                HanjaDerivedWord(4202L, "정문", "正門", "Chính môn", "Cổng chính tòa nhà"),
                HanjaDerivedWord(4203L, "교문", "校門", "Hiệu môn", "Cổng trường học"),
                HanjaDerivedWord(4204L, "동대문", "東大門", "Đông Đại Môn", "Cổng Đông lớn Seoul")
            )
        ),
        HanjaMasterInfo(
            hanjaChar = "話",
            readingVi = "THOẠI",
            strokeCount = 13,
            radicalChar = "言",
            radicalMeaning = "Ngôn (lời nói)",
            gradeLevel = "Cấp 6 (Trung cấp)",
            meaningSummary = "Nói chuyện, đàm thoại, lời kể, câu chuyện",
            derivedWords = listOf(
                HanjaDerivedWord(4301L, "전화", "電話", "Điện thoại", "Máy liên lạc bằng điện"),
                HanjaDerivedWord(4302L, "대화", "對話", "Đối thoại", "Trò chuyện hai chiều"),
                HanjaDerivedWord(4303L, "회화", "會話", "Hội thoại", "Giao tiếp đàm thoại")
            )
        ),
        HanjaMasterInfo(
            hanjaChar = "電",
            readingVi = "ĐIỆN",
            strokeCount = 13,
            radicalChar = "雨",
            radicalMeaning = "Vũ (mưa gió, sấm chớp)",
            gradeLevel = "Cấp 7 (Sơ cấp)",
            meaningSummary = "Điện năng, tia chớp, nhanh như chớp",
            derivedWords = listOf(
                HanjaDerivedWord(4401L, "전화", "電話", "Điện thoại", "Thiết bị gọi thoại"),
                HanjaDerivedWord(4402L, "전철", "電鐵", "Điện thiết", "Tàu điện"),
                HanjaDerivedWord(4403L, "전자", "電子", "Điện tử", "Đồ điện tử công nghệ"),
                HanjaDerivedWord(4404L, "전기", "電氣", "Điện khí", "Dòng điện, năng lượng")
            )
        ),
        HanjaMasterInfo(
            hanjaChar = "氣",
            readingVi = "KHÍ",
            strokeCount = 10,
            radicalChar = "气",
            radicalMeaning = "Khí (hơi thở, mây khói)",
            gradeLevel = "Cấp 6 (Trung cấp)",
            meaningSummary = "Không khí, khí phách, tâm trạng, khí hậu",
            derivedWords = listOf(
                HanjaDerivedWord(4501L, "기분", "氣分", "Khí phân", "Tâm trạng, cảm xúc"),
                HanjaDerivedWord(4502L, "감기", "感氣", "Cảm khí", "Cảm cúm thông thường"),
                HanjaDerivedWord(4503L, "인기", "人氣", "Nhân khí", "Sự hâm mộ, lòng yêu mến"),
                HanjaDerivedWord(4504L, "공기", "空氣", "Không khí", "Bầu dưỡng khí thở")
            )
        ),
        HanjaMasterInfo(
            hanjaChar = "分",
            readingVi = "PHÂN",
            strokeCount = 4,
            radicalChar = "刀",
            radicalMeaning = "Đao (dao rạch)",
            gradeLevel = "Cấp 7 (Sơ cấp)",
            meaningSummary = "Phân chia, phút đồng hồ, thành phần, bổn phận",
            derivedWords = listOf(
                HanjaDerivedWord(4601L, "십분", "十分", "Thập phân", "10 phút đồng hồ"),
                HanjaDerivedWord(4602L, "기분", "氣分", "Khí phân", "Tâm trạng"),
                HanjaDerivedWord(4603L, "부분", "部分", "Bộ phận", "Một phần nhỏ"),
                HanjaDerivedWord(4604L, "분석", "分析", "Phân tích", "Mổ xẻ phân tích kỹ")
            )
        ),
        HanjaMasterInfo(
            hanjaChar = "安",
            readingVi = "AN",
            strokeCount = 6,
            radicalChar = "宀",
            radicalMeaning = "Miên (mái nhà an ấm)",
            gradeLevel = "Cấp 7 (Sơ cấp)",
            meaningSummary = "Yên ổn, bình an, sắp xếp gọn gàng",
            derivedWords = listOf(
                HanjaDerivedWord(4701L, "안녕하세요", "安寧", "An ninh", "Lời chào chúc bình an"),
                HanjaDerivedWord(4702L, "안전", "安全", "An toàn", "Không gặp nguy hiểm"),
                HanjaDerivedWord(4703L, "불안", "不安", "Bất an", "Lo lắng bồn chồn")
            )
        ),
        HanjaMasterInfo(
            hanjaChar = "愛",
            readingVi = "ÁI",
            strokeCount = 13,
            radicalChar = "心",
            radicalMeaning = "Tâm (lòng trắc ẩn)",
            gradeLevel = "Cấp 5 (Trung cấp)",
            meaningSummary = "Yêu thương, trân quý, lòng bác ái",
            derivedWords = listOf(
                HanjaDerivedWord(4801L, "사랑/애정", "愛情", "Ái tình", "Tình thương mến sâu sắc"),
                HanjaDerivedWord(4802L, "애인", "愛人", "Ái nhân", "Người yêu, bạn đời"),
                HanjaDerivedWord(4803L, "연애", "戀愛", "Luyến ái", "Hẹn hò lãng mạn")
            )
        ),
        HanjaMasterInfo(
            hanjaChar = "信",
            readingVi = "TÍN",
            strokeCount = 9,
            radicalChar = "亻",
            radicalMeaning = "Nhân (người nói giữ lời)",
            gradeLevel = "Cấp 6 (Trung cấp)",
            meaningSummary = "Tin cậy, lòng tin, thư từ, tín ngưỡng",
            derivedWords = listOf(
                HanjaDerivedWord(4901L, "신용", "信用", "Tín dụng", "Sự uy tín, tín nhiệm"),
                HanjaDerivedWord(4902L, "자신감", "自信感", "Tự tín cảm", "Sự tự tin vào bản thân"),
                HanjaDerivedWord(4903L, "신호", "信號", "Tín hiệu", "Đèn hoặc dấu hiệu báo")
            )
        ),
        HanjaMasterInfo(
            hanjaChar = "同",
            readingVi = "ĐỒNG",
            strokeCount = 6,
            radicalChar = "口",
            radicalMeaning = "Khẩu (chung một lời)",
            gradeLevel = "Cấp 7 (Sơ cấp)",
            meaningSummary = "Cùng nhau, giống nhau, đồng nhất",
            derivedWords = listOf(
                HanjaDerivedWord(5001L, "동시", "同時", "Đồng thời", "Cùng một thời điểm"),
                HanjaDerivedWord(5002L, "동의", "同意", "Đồng ý", "Cùng nhất trí ý kiến"),
                HanjaDerivedWord(5003L, "동포", "同胞", "Đồng bào", "Cùng chung dòng dõi")
            )
        ),
        HanjaMasterInfo(
            hanjaChar = "美",
            readingVi = "MỸ",
            strokeCount = 9,
            radicalChar = "羊",
            radicalMeaning = "Dương (con dê béo tốt)",
            gradeLevel = "Cấp 6 (Trung cấp)",
            meaningSummary = "Xinh đẹp, tuyệt hảo, nước Mỹ",
            derivedWords = listOf(
                HanjaDerivedWord(5101L, "미술", "美術", "Mỹ thuật", "Nghệ thuật tạo hình"),
                HanjaDerivedWord(5102L, "미국", "美國", "Mỹ quốc", "Nước Hoa Kỳ"),
                HanjaDerivedWord(5103L, "미모", "美貌", "Mỹ mạo", "Nhan sắc xinh đẹp")
            )
        ),
        HanjaMasterInfo(
            hanjaChar = "法",
            readingVi = "PHÁP",
            strokeCount = 8,
            radicalChar = "氵",
            radicalMeaning = "Thủy (nước phẳng lặng)",
            gradeLevel = "Cấp 6 (Trung cấp)",
            meaningSummary = "Luật pháp, phương pháp, phép tắc chuẩn mực",
            derivedWords = listOf(
                HanjaDerivedWord(5201L, "방법", "方法", "Phương pháp", "Cách thức thực hiện"),
                HanjaDerivedWord(5202L, "문법", "文法", "Văn pháp", "Quy tắc ngữ pháp"),
                HanjaDerivedWord(5203L, "법률", "法律", "Pháp luật", "Quy định luật nhà nước"),
                HanjaDerivedWord(5204L, "프랑스/불란서", "佛/法", "Pháp quốc", "Nước Pháp")
            )
        )
    )

    fun findHanja(query: String): HanjaMasterInfo? {
        val q = query.trim()
        if (q.isBlank()) return null
        return allHanjaList.find {
            it.hanjaChar == q ||
                it.readingVi.equals(q, ignoreCase = true) ||
                it.derivedWords.any { w -> w.headwordKo == q || w.hanjaCombo.contains(q) }
        }
    }

    fun searchHanja(query: String): List<HanjaMasterInfo> {
        val q = query.trim().lowercase()
        if (q.isBlank()) return allHanjaList
        return allHanjaList.filter { item ->
            item.hanjaChar.contains(q) ||
                item.readingVi.lowercase().contains(q) ||
                item.meaningSummary.lowercase().contains(q) ||
                item.radicalMeaning.lowercase().contains(q) ||
                item.derivedWords.any { w ->
                    w.headwordKo.contains(q) ||
                        w.readingVi.lowercase().contains(q) ||
                        w.meaningVi.lowercase().contains(q)
                }
        }
    }
}
