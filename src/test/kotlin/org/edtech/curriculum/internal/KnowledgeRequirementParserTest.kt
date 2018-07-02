package org.edtech.curriculum.internal

import com.fasterxml.jackson.databind.ObjectMapper
import org.edtech.curriculum.*
import org.jsoup.Jsoup
import org.junit.Assert.*
import org.junit.Test
import java.io.File


class KnowledgeRequirementParserTest {

    private val hasMissingRequirementsFromSkolverket = setOf("BYPRIT0", "RINRID02", "SPEIDT0", "TESPRO01", "TEYPRO01", "HAVFIN05S")
    private val dataDir = File(DOWNLOADED_ARCHIVES_PATH)


    @Test
    fun testAgainstJsonFilesGR() {
        testAgainstJsonFiles(SchoolType.GR)
    }
    @Test
    fun testAgainstJsonFilesGRS() {
        testAgainstJsonFiles(SchoolType.GRS)
    }
    @Test
    fun testAgainstJsonFilesGY() {
        testAgainstJsonFiles(SchoolType.GY)
    }
    @Test
    fun testAgainstJsonFilesGYS() {
        testAgainstJsonFiles(SchoolType.GYS)
    }
    @Test
    fun testAgainstJsonFilesVUXGR() {
        testAgainstJsonFiles(SchoolType.VUXGR)
    }
/*    @Test
    fun testAgainstJsonFilesSFI() {
        testAgainstJsonFiles(SchoolType.SFI)
    }
*/

    private fun testAgainstJsonFiles(schoolType: SchoolType) {
        val mapper = ObjectMapper()
        val subjectMap: MutableMap<String, Subject> = HashMap()

        for (subject in Curriculum(schoolType, dataDir).getSubjects()) {
            subjectMap[subject.code] = subject
        }

        val subjectDir = File(VALID_JSONS_PATH + schoolType.name)
        if (!subjectDir.isDirectory) fail("${subjectDir.absolutePath} is not a directory")

        for (file in subjectDir.listFiles()) {
            if (!file.name.endsWith(".json")) continue
            val parsedSubject = subjectMap[file.nameWithoutExtension]
            if (parsedSubject == null) {
                fail("No subject ${file.nameWithoutExtension} for file ${file.absolutePath}")
            } else {
                val expected = file.readText()
                val actual = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(parsedSubject)
                assertEquals("Difference for subject ${schoolType.name}/${file.nameWithoutExtension}", expected, actual)
            }
        }
    }

    @Test
    fun matchParsedKnowledgeRequirementTextWithOriginalGR() {
        matchParsedKnowledgeRequirementTextWithOriginal(SchoolType.GR)
    }
    @Test
    fun matchParsedKnowledgeRequirementTextWithOriginalGRS() {
        matchParsedKnowledgeRequirementTextWithOriginal(SchoolType.GRS)
    }
    @Test
    fun matchParsedKnowledgeRequirementTextWithOriginalGRSAM() {
        matchParsedKnowledgeRequirementTextWithOriginal(SchoolType.GRSAM)
    }
    @Test
    fun matchParsedKnowledgeRequirementTextWithOriginalGY() {
        matchParsedKnowledgeRequirementTextWithOriginal(SchoolType.GY)
    }
    @Test
    fun matchParsedKnowledgeRequirementTextWithOriginalGYS() {
        matchParsedKnowledgeRequirementTextWithOriginal(SchoolType.GYS)
    }
    @Test
    fun matchParsedKnowledgeRequirementTextWithOriginalVUXGR() {
        matchParsedKnowledgeRequirementTextWithOriginal(SchoolType.VUXGR)
    }
    @Test
    fun matchParsedKnowledgeRequirementTextWithOriginalSFI() {
        matchParsedKnowledgeRequirementTextWithOriginal(SchoolType.SFI)
    }

    private fun matchParsedKnowledgeRequirementTextWithOriginal(schoolType: SchoolType) {
        for (subject in Curriculum(schoolType, dataDir).subjectHtml) {
            for (course in subject.courses) {
                // Get the fully parsed course
                val combined: MutableMap<GradeStep, StringBuilder> = HashMap()
                val knowledgeRequirements = KnowledgeRequirementConverter()
                        .getKnowledgeRequirements(course.knowledgeRequirement)
                for (knp in knowledgeRequirements) {
                    for (kn in knp.knowledgeRequirements) {
                        for ((g, s) in kn.knowledgeRequirementChoice) {
                            if (combined.containsKey(g)) {
                                combined[g]?.append(" ")?.append(s)
                            } else {
                                combined[g] = StringBuilder(s)
                            }
                        }
                    }
                }

                for ((gradeStep, text) in combined) {
                    val textExpected = Jsoup.parse(fixCurriculumErrors(course.knowledgeRequirement.getOrDefault(gradeStep, "")))
                            .select("p")
                            .text()
                            .trim()
                            .replace("  ", " ")
                            .replace(Regex("\\.([A-zåäö])"), ". \$1")
                    val textActual = Jsoup.parse(text.toString()).text().trim()
                    assertEquals("course: ${subject.name}/${course.name} GradeStep: ${gradeStep.name}", textExpected, textActual)
                }
            }
        }
    }

    @Test
    fun noEmptyKnowledgeRequirementChoicesGR() {
        testSubjects(Curriculum(SchoolType.GR, dataDir).getSubjects())
    }
    @Test
    fun noEmptyKnowledgeRequirementChoicesGRS() {
        testSubjects(Curriculum(SchoolType.GRS, dataDir).getSubjects())
    }
    @Test
    fun noEmptyKnowledgeRequirementChoicesGY() {
        testSubjects(Curriculum(SchoolType.GY, dataDir).getSubjects())
    }
    @Test
    fun noEmptyKnowledgeRequirementChoicesGYS() {
        testSubjects(Curriculum(SchoolType.GYS, dataDir).getSubjects())
    }
    @Test
    fun noEmptyKnowledgeRequirementChoicesVUXGR() {
        testSubjects(Curriculum(SchoolType.VUXGR, dataDir).getSubjects())
    }
/*    @Test
    fun noEmptyKnowledgeRequirementChoicesSFI() {
        testSubjects(Curriculum(SchoolType.SFI, dataDir).getSubjects())
    }
*/
    private fun testSubjects(subjects: List<Subject>) {
        for (subject in subjects) {
            for (course in subject.courses) {
                // Get the fully parsed course
                if (course.year != YearGroup(1, 3)) {
                    assertNotEquals("Knowledge Requirements is empty in  ${subject.name}/${course.name}", 0, course.knowledgeRequirementParagraphs.size)
                }
                // Make sure tha all requirements are set, exclude errors from skolverket.
                if (!hasMissingRequirementsFromSkolverket.contains(course.code)) {
                    course.knowledgeRequirementParagraphs.forEach {
                        it.knowledgeRequirements.forEach {
                            val gradeSteps = it.knowledgeRequirementChoice
                            if (!gradeSteps.keys.containsAll(setOf(GradeStep.A, GradeStep.C, GradeStep.E)) &&
                                    !gradeSteps.keys.contains(GradeStep.G)) {
                                fail("Knowledge Requirement Choices should be either E,C,A or G failed for: ${subject.name}/${course.name}")
                            }
                            gradeSteps.forEach { gradeStep ->
                                if (gradeStep.value.isBlank())
                                    fail("Found empty knowledge requirement critera in ${subject.name}/${course.name} [${course.code}]")
                            }
                        }
                    }
                }

            }
        }
    }
}
