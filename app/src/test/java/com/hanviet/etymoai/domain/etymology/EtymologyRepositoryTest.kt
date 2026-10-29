package com.hanviet.etymoai.domain.etymology

import com.hanviet.etymoai.data.db.core.CoreDatabaseHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

@RunWith(RobolectricTestRunner::class)
class EtymologyRepositoryTest {

    private lateinit var helper: CoreDatabaseHelper
    private lateinit var repository: EtymologyRepository

    @Before
    fun setUp() {
        val dbFile = File("src/main/assets/dictionary_core.db")
        assertTrue("File dictionary_core.db phải tồn tại", dbFile.exists())
        helper = CoreDatabaseHelper.fromFile(dbFile)
        repository = DefaultEtymologyRepository(helper)
    }

    @After
    fun tearDown() {
        helper.close()
    }

    @Test
    fun testGetEtymologyForWord_sinoKoreanGibun() = runBlocking {
        // Lexeme ID 1 là "기분" (氣分 - Khí Phân)
        val etymology = repository.getEtymologyForWord(1)
        assertNotNull("Phải lấy được hồ sơ từ nguyên của '기분'", etymology)
        assertEquals("기분", etymology!!.headword)
        assertEquals(OriginType.SINO_KOREAN, etymology.originType)
        assertEquals(VerificationStatus.VERIFIED_PRIMARY, etymology.verificationStatus)
        assertEquals("ki-bun", etymology.phoneticVi)

        assertEquals("Phải có 2 phân đoạn chiết tự cho 2 âm tiết", 2, etymology.segments.size)

        val seg0 = etymology.segments[0]
        assertEquals(0, seg0.segmentIndex)
        assertEquals("기", seg0.syllableKo)
        assertEquals("氣", seg0.hanjaChar)
        assertEquals("khí", seg0.readingVi)
        assertTrue(seg0.meaningVi?.contains("khí") == true)

        val seg1 = etymology.segments[1]
        assertEquals(1, seg1.segmentIndex)
        assertEquals("분", seg1.syllableKo)
        assertEquals("分", seg1.hanjaChar)
        assertEquals("phân", seg1.readingVi)
        assertTrue(seg1.meaningVi?.contains("phần") == true)
    }

    @Test
    fun testGetEtymologyForWord_nativeKorean() = runBlocking {
        // Lexeme ID 11 là "가다" (Thuần Hàn)
        val etymology = repository.getEtymologyForWord(11)
        assertNotNull(etymology)
        assertEquals("가다", etymology!!.headword)
        assertEquals(OriginType.NATIVE_KOREAN, etymology.originType)
        assertTrue("Từ thuần Hàn không có phân đoạn chữ Hán", etymology.segments.isEmpty())
    }

    @Test
    fun testGetEtymologyFamily_hak() = runBlocking {
        // Chữ '學' (Học) phải liên kết đến '학생' (Học sinh) và '학교' (Trường học)
        val family = repository.getEtymologyFamily("學")
        assertNotNull("Phải trích xuất được cây phả hệ cho chữ '學'", family)
        assertEquals("學", family!!.hanjaChar)
        assertEquals("học", family.readingVi)
        assertEquals("子", family.radical)
        assertEquals(16, family.strokeCount)

        val words = family.relatedWords
        assertFalse("Danh sách từ họ hàng không được rỗng", words.isEmpty())
        assertTrue("Phải chứa ít nhất 2 từ họ hàng (학생, 학교)", words.size >= 2)

        val haksaeng = words.find { it.headword == "학생" }
        assertNotNull("Phải chứa '학생'", haksaeng)
        assertEquals(OriginType.SINO_KOREAN, haksaeng!!.originType)
        assertTrue(haksaeng.meaningVi.contains("học sinh"))

        val hakgyo = words.find { it.headword == "학교" }
        assertNotNull("Phải chứa '학교'", hakgyo)
        assertTrue(hakgyo!!.meaningVi.contains("trường học"))
    }

    @Test
    fun testGetHanjaMaster() = runBlocking {
        val master = repository.getHanjaMaster("氣")
        assertNotNull(master)
        assertEquals("氣", master!!.hanjaChar)
        assertEquals("气", master.radical)
        assertEquals(10, master.strokeCount)
        assertTrue(master.meaningVi?.contains("khí") == true)
    }

    @Test
    fun testNonExistentWordAndHanja() = runBlocking {
        val nullWord = repository.getEtymologyForWord(999999L)
        assertNull("ID không tồn tại phải trả về null", nullWord)

        val nullFamily = repository.getEtymologyFamily("𠮷")
        assertNull("Hán tự không tồn tại phải trả về null", nullFamily)
    }

    @Test
    fun testFlowMethods() = runBlocking {
        val flowResult = repository.getEtymologyForWordFlow(1).first()
        val syncResult = repository.getEtymologyForWord(1)
        assertEquals(syncResult, flowResult)

        val flowFamily = repository.getEtymologyFamilyFlow("學").first()
        val syncFamily = repository.getEtymologyFamily("學")
        assertEquals(syncFamily, flowFamily)
    }
}
