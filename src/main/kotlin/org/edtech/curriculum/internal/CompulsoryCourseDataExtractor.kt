package org.edtech.curriculum.internal

import org.edtech.curriculum.*
import org.jsoup.nodes.Document
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * Parses the open data supplied by skolverket for the compulsory subjects
 *
 * The parser takes an XML file as supplied by http://opendata.skolverket.se/data/compulsary.tgz
 * Extracts the information to be used for processing
 *
 *
 * @param subjectDocument to extract information from
 */
class CompulsoryCourseDataExtractor(private val subjectDocument: Document) {

    fun getData(): GrData {
        val code = subjectDocument.select("code").first().text()
        val centralContents = subjectDocument.select("centralContent")
                .map {
                    CentralContentGrHtml(it.select("year").text(),
                            convertDashListToList(fixHtmlEncoding(it.select("text").text())))
                }.toList()

        val knowledgeRequirements = getKnowledgeRequirements()

        return GrData(centralContents, knowledgeRequirements)
    }

    private fun getKnowledgeRequirements(): List<KnowledgeRequirementGrHtml> {
        val reqs = listOf<KnowledgeRequirementGrHtml>()

        val krTags = subjectDocument
                // Get the subject code element
                .select("knowledgeRequirement")
                .map {
                    val gradeStepText = it.select("gradeStep").text()
                    // Lower years has not grade steps, convert to G level
                    val gradeStep = if (gradeStepText.isEmpty()) GradeStep.G else GradeStep.valueOf(gradeStepText)
                    Triple(
                            it.select("year").text(),
                            gradeStep,
                            it.select("text").text())
                }
                .groupBy(
                        {
                            it.first
                        },
                        {
                            Pair(it.second, it.third)
                        }
                )
                .mapValues {
                    it.value.groupBy(
                            { it.first },
                            { it.second }
                    )
                }
                .map {
                    // FIXME: it.value är en List!!! wtf...
                    KnowledgeRequirementGrHtml(it.key, it.value)

                }.toList()
        return reqs
    }

    /**
     * Check if the supplied targetYear is equal or in the range [min]-[max] as described by the string.
     */
    private fun compareYearString(targetYear: Int, year: String): Boolean {
        val yearParts = year.split("-")
        return if (yearParts.size > 1) {
            yearParts.getOrNull(0)?.toIntOrNull() ?: 0 <= targetYear
            yearParts.getOrNull(1)?.toIntOrNull() ?: 0 >= targetYear
        } else {
            yearParts.getOrNull(0)?.toIntOrNull() ?: 0 >= targetYear
        }
    }
}

