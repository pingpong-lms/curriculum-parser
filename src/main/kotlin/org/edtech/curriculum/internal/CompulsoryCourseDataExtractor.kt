package org.edtech.curriculum.internal

import org.edtech.curriculum.CentralContentGrHtml
import org.edtech.curriculum.GrData
import org.edtech.curriculum.GradeStep
import org.edtech.curriculum.KnowledgeRequirementGrHtml
import org.jsoup.nodes.Document

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
                            subjectDocument.select("centralCntHeading").text(),
                            convertDashListToList(fixHtmlEncoding(it.select("text").text())),
                            it.select("typeOfCentralContent")?.text()
                    )
                }.toList()

        val knowledgeRequirements = getKnowledgeRequirements()

        return GrData(centralContents, knowledgeRequirements)
    }

    private fun getKnowledgeRequirements(): List<KnowledgeRequirementGrHtml> {
        return subjectDocument
                // Get the subject code element
                .select("knowledgeRequirement")
                .map {
                    val gradeStepText = it.select("gradeStep").text()
                    // Lower years has not grade steps, convert to G level
                    val gradeStep = if (gradeStepText.isEmpty()) GradeStep.G else GradeStep.valueOf(gradeStepText)
                    KnowledgeRequirementDataItem(it.select("year").text(),
                            gradeStep,
                            it.select("text").text(),
                            it.select("typeOfRequirement").text())
                }
                .groupBy {
                    DataItemKey(it.year, it.typeOfRequirement)
                }
                .mapValues {
                    it.value.map {
                        it.gradeStep to it.text
                    }.toMap()
                }
                .map {
                    KnowledgeRequirementGrHtml(
                            it.key.year,
                            subjectDocument.select("knowledgeReqsHeading").text(),
                            it.key.typeOfRequirement,
                            it.value
                    )
                }
                .toList()
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

private data class DataItemKey(
        val year: String,
        val typeOfRequirement: String?
)

private data class KnowledgeRequirementDataItem(
        val year: String,
        val gradeStep: GradeStep,
        val text: String,
        val typeOfRequirement: String?
)
