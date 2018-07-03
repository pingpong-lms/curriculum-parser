package org.edtech.curriculum

import com.fasterxml.jackson.databind.ObjectMapper
import java.io.File

class SubjectFileGenerator(private val destDir: File, private val archiveDir: File, private val cache: Boolean = true) {
    /**
     * Generates json files for all syllabi
     */
    fun generateAll() {
        // Generate all curriculum files
        for (syllabusType in SyllabusType.values()) {
            val subjectDir = destDir.resolve(syllabusType.name)
            if (!subjectDir.exists()) {
                subjectDir.mkdirs()
            }
            Syllabus(syllabusType, archiveDir, cache).getSubjects().forEach {
                writeSubjectToFile(it, subjectDir.resolve("${it.code}.json"))
            }
        }
    }

    /**
     * Update only existing files
     */
    fun regenerate() {
        for (syllabusType in SyllabusType.values()) {
            val subjectMap = Syllabus(syllabusType, archiveDir, cache)
                    .getSubjects()
                    .map { Pair(it.code, it) }
                    .toMap()
            val subjectDir = destDir.resolve(syllabusType.name)
            if (subjectDir.isDirectory) {
                for (file in subjectDir.listFiles()) {
                    if (!file.name.endsWith(".json")) continue

                    val subjectCode = file.nameWithoutExtension
                    val parsedSubject = subjectMap[subjectCode]
                    if (parsedSubject == null) {
                        throw RuntimeException("ERROR: No subject $subjectCode for file ${file.absolutePath}")
                    } else {
                        writeSubjectToFile(parsedSubject, file)
                    }
                }
            }
        }
    }

    /**
     * Update one specific subject
     */
    fun generateOneSubject(syllabusType: SyllabusType, subjectCode: String) {
        val subject = Syllabus(syllabusType, archiveDir, cache)
                .getSubjects()
                .firstOrNull { it.code == subjectCode }
        if (subject != null) {

            writeSubjectToFile(subject,  destDir.resolve("/$syllabusType/$subjectCode.json"))
        } else {
            throw RuntimeException("ERROR: cannot find subject $String in syllabus $syllabusType.")
        }
    }

    private fun writeSubjectToFile(subject: Subject, file: File) {
        println("writing to: $file")
        file.writeText(ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(subject))
    }
}