package org.edtech.curriculum.internal

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import org.edtech.curriculum.*
import org.jsoup.Jsoup
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.fail
import java.io.File
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule




class KnowledgeRequirementParserTest {

    private val hasMissingRequirementsFromSkolverket = setOf("BYPRIT0", "RINRID02", "SPEIDT0", "TESPRO01", "TEYPRO01", "HAVFIN05S")
    private val coursesWithSwitchedLines = setOf("SVESVE01")
    private val dataDir = File("./src/test/resources/opendata/")
    private val validDataDir = File("./src/test/resources/valid/")

    /**
     * Gradelevel 9 has requirements
     * Gradelevel 6 has requirements except in GRS
     * No gradelevel => has requirements
     */
    private fun hasRequirements(yearGroup: YearGroup?, schoolType: SchoolType): Boolean {
        return (yearGroup == null || yearGroup.end == 6 && schoolType != SchoolType.GRS || yearGroup.end == 9)
    }

    private fun getObjectMapper(): ObjectMapper {
        val mapper = ObjectMapper()
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        mapper.registerModule(JavaTimeModule())
        return mapper
    }

    @TestFactory
    fun testAgainstJsonFiles(): List<DynamicTest> {
        val mapper = getObjectMapper()
        return SchoolType.values().flatMap { schoolType ->

            File("$validDataDir/").listFiles().flatMap { versionDir ->
                val subjectMap = Curriculum(schoolType, dataDir.resolve(versionDir.name)).getSubjects().associateBy { it.code }
                val subjectDir = versionDir.resolve(schoolType.name)
                if (subjectDir.isDirectory) {
                    subjectDir.listFiles()
                            .filter { it.name.endsWith(".json") }
                            .map { file ->
                                DynamicTest.dynamicTest("${schoolType.name}/${versionDir.name} - ${file.nameWithoutExtension}") {
                                    val parsedSubject = subjectMap[file.nameWithoutExtension]
                                    if (parsedSubject == null) {
                                        fail("No subject ${file.nameWithoutExtension} for file ${file.absolutePath}")
                                    } else {
                                        val expected = file.readText()
                                        val actual = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(parsedSubject)
                                        assertEquals(expected, actual, "Difference for subject ${versionDir.name} - ${schoolType.name}/${file.nameWithoutExtension}")
                                    }
                                }
                            }
                } else {
                    listOf()
                }
            }
        }
    }

    @TestFactory
    fun matchParsedKnowledgeRequirementTextWithOriginal() = SchoolType.values().map { schoolType ->
        DynamicTest.dynamicTest(schoolType.name) {
            dataDir.listFiles().forEach { versionDir ->
                Curriculum(schoolType, versionDir).subjectHtml.forEach { subject ->
                    for (course in subject.courses) {
                        if (!coursesWithSwitchedLines.contains(course.code)) {
                            // Get the fully parsed course
                            val combined: MutableMap<GradeStep, StringBuilder> = HashMap()
                            val knowledgeRequirements = KnowledgeRequirementConverter()
                                    .getKnowledgeRequirements(course.knowledgeRequirementGroups)
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
                                val combinedString = course.knowledgeRequirementGroups.joinToString("") { it.knowledgeRequirements.getOrDefault(gradeStep, "") }
                                val textExpected = Jsoup.parse(fixCurriculumErrors(combinedString))
                                        .select("p")
                                        .text()
                                        .trim()
                                        .replace("  ", " ")
                                        .replace(Regex("\\.([A-zåäö])"), ". \$1")
                                val textActual = Jsoup.parse(text.toString()).text().trim()
                                assertEquals(textExpected, textActual, "course: ${subject.name}/${course.name} GradeStep: ${gradeStep.name}")
                            }
                        }
                    }
                }
            }
        }
    }

    @TestFactory
    fun noEmptyKnowledgeRequirementChoices() = SchoolType.values().map { schoolType ->
        DynamicTest.dynamicTest(schoolType.name) {
            dataDir.listFiles().forEach { versionDir ->
                Curriculum(schoolType, versionDir).getSubjects()
                        .forEach { subject ->
                            for (course in subject.courses) {
                                // Get the fully parsed course
                                if (hasRequirements(course.year, schoolType)) {
                                    assertNotEquals( 0, course.knowledgeRequirementParagraphs.size) { "Knowledge Requirements is empty in  ${subject.name}/${course.name}" }
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
    }
}
