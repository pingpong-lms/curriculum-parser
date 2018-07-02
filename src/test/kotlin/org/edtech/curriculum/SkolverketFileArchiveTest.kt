package org.edtech.curriculum

import org.junit.Assert
import org.junit.Test
import java.io.File

class SkolverketFileArchiveTest {

    @Test
    fun testGR() {
        val sf = SkolverketFileArchive( File(DOWNLOADED_ARCHIVES_PATH + "compulsory.tgz"))
        Assert.assertEquals(SchoolType.GR, sf.getType())
        Assert.assertEquals(105, sf.getFileStreams("").size)
    }

    @Test
    fun testGY() {
        val sf = SkolverketFileArchive( File(DOWNLOADED_ARCHIVES_PATH + "syllabus.tgz"))
        Assert.assertEquals(SchoolType.GY, sf.getType())
        Assert.assertEquals(313, sf.getFileStreams("").size)
    }

    @Test
    fun testVUXGR() {
        val sf = SkolverketFileArchive( File(DOWNLOADED_ARCHIVES_PATH + "vuxgr.tgz"))
        Assert.assertEquals(SchoolType.VUXGR, sf.getType())
        Assert.assertEquals(25, sf.getFileStreams("").size)
    }

    @Test
    fun testGYS() {
        val sf = SkolverketFileArchive( File(DOWNLOADED_ARCHIVES_PATH + "gys.tgz"))
        Assert.assertEquals(SchoolType.GYS, sf.getType())
        Assert.assertEquals(91, sf.getFileStreams("").size)
    }

    @Test
    fun testSFI() {
        val sf = SkolverketFileArchive( File(DOWNLOADED_ARCHIVES_PATH + "sfi.tgz"))
        Assert.assertEquals(SchoolType.SFI, sf.getType())
        Assert.assertEquals(2, sf.getFileStreams("").size)
    }

}