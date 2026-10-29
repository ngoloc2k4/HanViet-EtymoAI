package com.hanviet.etymoai.domain.morphology

/**
 * Kết quả phân tích ứng viên từ gốc
 */
data class CandidateLemma(
    val lemma: String,
    val ruleId: String,
    val group: IrregularGroup,
    val confidenceScore: Float,
    val explanationVi: String
)

/**
 * Bộ quy tắc giải phân tích hình thái cho 7 nhóm động từ & tính từ bất quy tắc tiếng Hàn
 * (7 Irregular Verb & Adjective Rules Engine)
 */
object IrregularVerbRules {

    /**
     * Danh sách toàn bộ các quy tắc bất quy tắc
     */
    val rules: List<MorphologyRule> = listOf(
        // ------------------------------------------------------------------
        // 1. Nhóm BIEUP (ㅂ bất quy tắc): 워/와/우 -> ㅂ
        // ------------------------------------------------------------------
        MorphologyRule(
            ruleId = "IRR_BIEUP_WO_WA",
            group = IrregularGroup.BIEUP,
            patternDescription = "Biến đổi -워/-와/-우 ngược về gốc có patchim ㅂ (추워 -> 춥다, 도와 -> 돕다)",
            confidenceWeight = 0.95f,
            explanationVi = "Bất quy tắc ㅂ: Nguyên âm 워/와 biến đổi ngược về patchim ㅂ"
        ) { surface ->
            val list = mutableListOf<String>()
            val trimmed = stripPoliteEnding(surface)

            // Dạng 1: Kết thúc bằng '워' (e.g. 추워, 더워, 아름다워)
            if (trimmed.endsWith("워") && trimmed.length >= 2) {
                val stemPrefix = trimmed.substring(0, trimmed.length - 2)
                val targetChar = trimmed[trimmed.length - 2]
                if (HangulUtil.isHangulSyllable(targetChar) && !HangulUtil.hasPatchim(targetChar)) {
                    val restoredChar = HangulUtil.withPatchim(targetChar, HangulUtil.JONG_BIEUP)
                    list.add(stemPrefix + restoredChar + "다")
                }
            }

            // Dạng 2: Kết thúc bằng '와' (e.g. 도와, 고와)
            if (trimmed.endsWith("와") && trimmed.length >= 2) {
                val stemPrefix = trimmed.substring(0, trimmed.length - 2)
                val targetChar = trimmed[trimmed.length - 2]
                if (HangulUtil.isHangulSyllable(targetChar) && !HangulUtil.hasPatchim(targetChar)) {
                    val restoredChar = HangulUtil.withPatchim(targetChar, HangulUtil.JONG_BIEUP)
                    list.add(stemPrefix + restoredChar + "다")
                }
            }

            // Dạng 3: Dạng quá khứ 웠/왔 (e.g. 추웠다, 도왔다, 더웠어요)
            for (pastMarker in listOf("웠", "왔")) {
                val idx = trimmed.indexOf(pastMarker)
                if (idx > 0) {
                    val stemPrefix = trimmed.substring(0, idx - 1)
                    val targetChar = trimmed[idx - 1]
                    if (HangulUtil.isHangulSyllable(targetChar) && !HangulUtil.hasPatchim(targetChar)) {
                        val restoredChar = HangulUtil.withPatchim(targetChar, HangulUtil.JONG_BIEUP)
                        list.add(stemPrefix + restoredChar + "다")
                    }
                }
            }

            // Dạng 4: Liên từ điều kiện '우면 / 우니' (e.g. 추우면, 도우면)
            for (suffix in listOf("우면", "우니", "운")) {
                if (trimmed.endsWith(suffix) && trimmed.length > suffix.length) {
                    val stemPrefix = trimmed.substring(0, trimmed.length - suffix.length - 1)
                    val targetChar = trimmed[trimmed.length - suffix.length - 1]
                    if (HangulUtil.isHangulSyllable(targetChar) && !HangulUtil.hasPatchim(targetChar)) {
                        val restoredChar = HangulUtil.withPatchim(targetChar, HangulUtil.JONG_BIEUP)
                        list.add(stemPrefix + restoredChar + "다")
                    }
                }
            }

            list.distinct()
        },

        // ------------------------------------------------------------------
        // 2. Nhóm DIGEUT (ㄷ bất quy tắc): ㄹ trước nguyên âm -> ㄷ
        // ------------------------------------------------------------------
        MorphologyRule(
            ruleId = "IRR_DIGEUT_RIEUL",
            group = IrregularGroup.DIGEUT,
            patternDescription = "Biến đổi patchim ㄹ trước nguyên âm trở về ㄷ (들어요 -> 듣다, 걸어요 -> 걷다)",
            confidenceWeight = 0.94f,
            explanationVi = "Bất quy tắc ㄷ: Patchim ㄹ biến đổi ngược về ㄷ khi đứng trước nguyên âm"
        ) { surface ->
            val list = mutableListOf<String>()
            val trimmed = stripPoliteEnding(surface)

            // Tìm âm tiết có patchim ㄹ đứng trước 어/아/으 hoặc 었/았
            for (i in 0 until trimmed.length - 1) {
                val currentChar = trimmed[i]
                val nextChar = trimmed[i + 1]

                if (HangulUtil.isHangulSyllable(currentChar) && HangulUtil.getPatchim(currentChar) == HangulUtil.JONG_RIEUL) {
                    val nextDec = HangulUtil.decompose(nextChar)
                    // Nếu âm tiết tiếp theo bắt đầu bằng nguyên âm (Chosung ㅇ)
                    if (nextDec != null && nextDec.cho == 11) { // 11 là index của 'ㅇ'
                        val prefix = trimmed.substring(0, i)
                        val restoredChar = HangulUtil.withPatchim(currentChar, HangulUtil.JONG_DIGEUT)
                        list.add(prefix + restoredChar + "다")
                    }
                }
            }
            list.distinct()
        },

        // ------------------------------------------------------------------
        // 3. Nhóm SIOT (ㅅ bất quy tắc): Khôi phục patchim ㅅ bị rụng
        // ------------------------------------------------------------------
        MorphologyRule(
            ruleId = "IRR_SIOT_DROP",
            group = IrregularGroup.SIOT,
            patternDescription = "Khôi phục patchim ㅅ khi bị rụng trước nguyên âm (지어요 -> 짓다, 나아요 -> 낫다)",
            confidenceWeight = 0.92f,
            explanationVi = "Bất quy tắc ㅅ: Patchim ㅅ bị rụng trước nguyên âm được khôi phục"
        ) { surface ->
            val list = mutableListOf<String>()
            val trimmed = stripPoliteEnding(surface)

            for (i in 0 until trimmed.length - 1) {
                val currentChar = trimmed[i]
                val nextChar = trimmed[i + 1]

                if (HangulUtil.isHangulSyllable(currentChar) && !HangulUtil.hasPatchim(currentChar)) {
                    val nextDec = HangulUtil.decompose(nextChar)
                    if (nextDec != null && nextDec.cho == 11) { // bắt đầu bằng 'ㅇ'
                        val prefix = trimmed.substring(0, i)
                        val restoredChar = HangulUtil.withPatchim(currentChar, HangulUtil.JONG_SIOT)
                        list.add(prefix + restoredChar + "다")
                    }
                }
            }
            list.distinct()
        },

        // ------------------------------------------------------------------
        // 4. Nhóm HIEUT (ㅎ bất quy tắc): 애/얘 -> ㅏ/ㅓ + ㅎ
        // ------------------------------------------------------------------
        MorphologyRule(
            ruleId = "IRR_HIEUT_VOWEL_MERGE",
            group = IrregularGroup.HIEUT,
            patternDescription = "Xử lý biến âm 애/얘 trở về ㅎ (파래 -> 파랗다, 하얘 -> 하얗다, 그래 -> 그렇다)",
            confidenceWeight = 0.95f,
            explanationVi = "Bất quy tắc ㅎ: Nguyên âm kết hợp 애/얘 khôi phục về patchim ㅎ"
        ) { surface ->
            val list = mutableListOf<String>()
            val trimmed = stripPoliteEnding(surface)

            // Quét âm tiết cuối (hoặc âm tiết trước ㅆ trong quá khứ 랬/갰)
            var targetIndex = trimmed.length - 1
            if (trimmed.endsWith("다") && trimmed.length >= 2) {
                targetIndex = trimmed.length - 2
            }

            val targetChar = trimmed[targetIndex]
            val dec = HangulUtil.decompose(targetChar)
            if (dec != null) {
                val prefix = trimmed.substring(0, targetIndex)

                // Vowel ㅐ (JUNG_AE) -> khôi phục về ㅏ + ㅎ (hoặc ㅓ + ㅎ cho 그렇다/어떻다)
                if (dec.jung == HangulUtil.JUNG_AE) {
                    // Ưu tiên 1: ㅏ + ㅎ (e.g. 파랗다, 노랗다, 빨갛다, 까맣다)
                    val restoredA = HangulUtil.compose(dec.cho, HangulUtil.JUNG_A, HangulUtil.JONG_HIEUT)
                    list.add(prefix + restoredA + "다")

                    // Ưu tiên 2: ㅓ + ㅎ (e.g. 그렇다, 어떻다, 이렇다)
                    val restoredEO = HangulUtil.compose(dec.cho, HangulUtil.JUNG_EO, HangulUtil.JONG_HIEUT)
                    list.add(prefix + restoredEO + "다")
                }

                // Vowel ㅒ (JUNG_YAE) -> khôi phục về ㅑ + ㅎ (e.g. 하얗다)
                if (dec.jung == HangulUtil.JUNG_YAE) {
                    val restoredYA = HangulUtil.compose(dec.cho, 2 /* ㅑ */, HangulUtil.JONG_HIEUT)
                    list.add(prefix + restoredYA + "다")
                }
            }

            // Quét trường hợp quá khứ: e.g. 그랬어요, 파랬다
            for (marker in listOf("랬", "갰", "앴", "얬")) {
                val idx = trimmed.indexOf(marker)
                if (idx >= 0) {
                    val mChar = trimmed[idx]
                    val mDec = HangulUtil.decompose(mChar)
                    if (mDec != null) {
                        val prefix = trimmed.substring(0, idx)
                        val restoredA = HangulUtil.compose(mDec.cho, HangulUtil.JUNG_A, HangulUtil.JONG_HIEUT)
                        val restoredEO = HangulUtil.compose(mDec.cho, HangulUtil.JUNG_EO, HangulUtil.JONG_HIEUT)
                        list.add(prefix + restoredA + "다")
                        list.add(prefix + restoredEO + "다")
                    }
                }
            }

            list.distinct()
        },

        // ------------------------------------------------------------------
        // 5. Nhóm REU (르 bất quy tắc): ㄹ + 라/러 -> 르
        // ------------------------------------------------------------------
        MorphologyRule(
            ruleId = "IRR_REU_DOUBLING",
            group = IrregularGroup.REU,
            patternDescription = "Xử lý lặp phụ âm ㄹ라/ㄹ러 trở về nguyên thể 르 (몰라요 -> 모르다, 빨라 -> 빠르다)",
            confidenceWeight = 0.96f,
            explanationVi = "Bất quy tắc 르: Dạng nhân đôi ㄹ라/ㄹ러 khôi phục về âm tiết 르"
        ) { surface ->
            val list = mutableListOf<String>()
            val trimmed = stripPoliteEnding(surface)

            for (i in 0 until trimmed.length - 1) {
                val c1 = trimmed[i]
                val c2 = trimmed[i + 1]

                // c1 có patchim ㄹ, c2 là '라' hoặc '러' (hoặc '랐'/'렀' trong quá khứ)
                if (HangulUtil.isHangulSyllable(c1) && HangulUtil.getPatchim(c1) == HangulUtil.JONG_RIEUL) {
                    val c2Dec = HangulUtil.decompose(c2)
                    if (c2Dec != null && c2Dec.cho == 5) { // Chosung 'ㄹ'
                        val prefix = trimmed.substring(0, i)
                        val restoredC1 = HangulUtil.withoutPatchim(c1)
                        list.add(prefix + restoredC1 + "르다")
                    }
                }
            }
            list.distinct()
        },

        // ------------------------------------------------------------------
        // 6. Nhóm EUU (으 bất quy tắc / 탈락): ㅏ/ㅓ (không patchim) -> ㅡ
        // ------------------------------------------------------------------
        MorphologyRule(
            ruleId = "IRR_EUU_DROP",
            group = IrregularGroup.EUU,
            patternDescription = "Khôi phục nguyên âm ㅡ bị nuốt bởi 아/어 (바빠 -> 바쁘다, 예뻐 -> 예쁘다, 아파 -> 아프다)",
            confidenceWeight = 0.93f,
            explanationVi = "Bất quy tắc 으: Nguyên âm ㅡ bị nuốt được khôi phục"
        ) { surface ->
            val list = mutableListOf<String>()
            val trimmed = stripPoliteEnding(surface)

            if (trimmed.isNotEmpty()) {
                var targetIdx = trimmed.length - 1
                if (trimmed.endsWith("다") && trimmed.length >= 2) {
                    targetIdx = trimmed.length - 2
                }

                val lastChar = trimmed[targetIdx]
                val dec = HangulUtil.decompose(lastChar)

                // Nếu âm tiết cuối không có patchim và có nguyên âm ㅏ hoặc ㅓ
                if (dec != null && dec.jong == HangulUtil.JONG_NONE && (dec.jung == HangulUtil.JUNG_A || dec.jung == HangulUtil.JUNG_EO)) {
                    val prefix = trimmed.substring(0, targetIdx)
                    val restoredChar = HangulUtil.compose(dec.cho, HangulUtil.JUNG_EU, HangulUtil.JONG_NONE)
                    list.add(prefix + restoredChar + "다")
                }

                // Xử lý dạng quá khứ: e.g. 바빴어요 -> 바쁘다, 예뻤다 -> 예쁘다
                for (pastMarker in listOf("빴", "뻤", "팠", "펐", "컸", "썼", "껐")) {
                    val idx = trimmed.indexOf(pastMarker)
                    if (idx >= 0) {
                        val pChar = trimmed[idx]
                        val pDec = HangulUtil.decompose(pChar)
                        if (pDec != null) {
                            val prefix = trimmed.substring(0, idx)
                            val restoredChar = HangulUtil.compose(pDec.cho, HangulUtil.JUNG_EU, HangulUtil.JONG_NONE)
                            list.add(prefix + restoredChar + "다")
                        }
                    }
                }
            }

            list.distinct()
        },

        // ------------------------------------------------------------------
        // 7. Nhóm RIEUL (ㄹ biến đổi / rơi rụng): Trước ㄴ, ㅂ, ㅅ
        // ------------------------------------------------------------------
        MorphologyRule(
            ruleId = "IRR_RIEUL_DROP",
            group = IrregularGroup.RIEUL,
            patternDescription = "Khôi phục patchim ㄹ bị rơi trước ㄴ, ㅂ, ㅅ (만듭니다 -> 만들다, 아는 -> 알다, 사는 -> 살다)",
            confidenceWeight = 0.94f,
            explanationVi = "Biến đổi rơi ㄹ: Phụ âm ㄹ bị rơi trước ㄴ, ㅂ, ㅅ được hoàn nguyên"
        ) { surface ->
            val list = mutableListOf<String>()
            val clean = surface.trim()

            // Dạng 1: Đuôi trang trọng kính ngữ '-ㅂ니다 / -ㅂ니까' (e.g. 만듭니다, 삽니다, 압니다)
            for (ending in listOf("니다", "니까", "니")) {
                if (clean.endsWith(ending) && clean.length > ending.length) {
                    val stem = clean.substring(0, clean.length - ending.length)
                    val lastChar = stem.last()
                    val dec = HangulUtil.decompose(lastChar)
                    if (dec != null && dec.jong == HangulUtil.JONG_BIEUP) {
                        val restoredLast = HangulUtil.compose(dec.cho, dec.jung, HangulUtil.JONG_RIEUL)
                        list.add(stem.dropLast(1) + restoredLast + "다")
                    }
                }
            }

            // Dạng 2: Đuôi định từ hiện tại '-는' (e.g. 만드는, 사는, 아는, 여는)
            if (clean.endsWith("는") && clean.length >= 2) {
                val stem = clean.dropLast(1)
                val lastChar = stem.last()
                if (!HangulUtil.hasPatchim(lastChar)) {
                    val restoredLast = HangulUtil.withPatchim(lastChar, HangulUtil.JONG_RIEUL)
                    list.add(stem.dropLast(1) + restoredLast + "다")
                }
            }

            // Dạng 3: Đuôi tôn kính '-세요 / -십니까' (e.g. 만드세요, 사세요, 아세요)
            for (ending in listOf("세요", "셔요", "시", "셔")) {
                if (clean.endsWith(ending) && clean.length > ending.length) {
                    val stem = clean.substring(0, clean.length - ending.length)
                    val lastChar = stem.last()
                    if (!HangulUtil.hasPatchim(lastChar)) {
                        val restoredLast = HangulUtil.withPatchim(lastChar, HangulUtil.JONG_RIEUL)
                        list.add(stem.dropLast(1) + restoredLast + "다")
                    }
                }
            }

            val trimmed = stripPoliteEnding(surface)
            if (trimmed != clean) {
                for (ending in listOf("세", "셔")) {
                    if (trimmed.endsWith(ending) && trimmed.length > ending.length) {
                        val stem = trimmed.substring(0, trimmed.length - ending.length)
                        val lastChar = stem.last()
                        if (!HangulUtil.hasPatchim(lastChar)) {
                            val restoredLast = HangulUtil.withPatchim(lastChar, HangulUtil.JONG_RIEUL)
                            list.add(stem.dropLast(1) + restoredLast + "다")
                        }
                    }
                }
            }

            list.distinct()
        }
    )

    /**
     * Thực hiện deconjugation cho một từ bề mặt (surface form), trả về danh sách các ứng viên
     */
    fun deconjugate(surfaceForm: String): List<CandidateLemma> {
        val clean = surfaceForm.trim()
        if (clean.isEmpty()) return emptyList()

        val candidates = mutableListOf<CandidateLemma>()

        for (rule in rules) {
            val lemmas = rule.transformFn(clean)
            for (lemma in lemmas) {
                if (lemma.isNotBlank() && lemma != clean) {
                    candidates.add(
                        CandidateLemma(
                            lemma = lemma,
                            ruleId = rule.ruleId,
                            group = rule.group,
                            confidenceScore = rule.confidenceWeight,
                            explanationVi = rule.explanationVi
                        )
                    )
                }
            }
        }

        return candidates.distinctBy { it.lemma }
    }

    private fun stripPoliteEnding(str: String): String {
        return when {
            str.endsWith("요") -> str.dropLast(1)
            str.endsWith("다") -> str.dropLast(1)
            else -> str
        }
    }
}
