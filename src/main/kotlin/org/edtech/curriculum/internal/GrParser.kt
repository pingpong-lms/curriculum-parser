package org.edtech.curriculum.internal

import org.edtech.curriculum.*

internal fun getCentralContent(html: CentralContentGrHtml): CentralContentsGr {

    return CentralContentsGr(html.heading,
            toYearGroup(html.year)!!,
            typeOfCentralContentParser(html.typeOfCentralContent),
            toCentralContent(html.text)
    )
}

internal fun getKnowledgeRequirementParagraphs(html: KnowledgeRequirementGrHtml): List<KnowledgeRequirementParagraphGr> {
    return KnowledgeRequirementConverter()
            .getKnowledgeRequirements(html.data)
            .map {
                KnowledgeRequirementParagraphGr(
                        html.heading,
                        html.year.toInt(),
                        typeOfRequirementParser(html.typeOfRequirement),
                        it.knowledgeRequirements
                )
            }
}

fun typeOfRequirementParser(type: String?): TypeOfRequirement? {
    return if (type.isNullOrBlank()) {
        null
    } else {
        TypeOfRequirement.valueOf(type!!)
    }

}

internal fun typeOfCentralContentParser(type: String?): TypeOfCentralContent? {
    return if (type.isNullOrBlank()) {
        null
    } else {
        TypeOfCentralContent.valueOf(type!!)
    }
}
