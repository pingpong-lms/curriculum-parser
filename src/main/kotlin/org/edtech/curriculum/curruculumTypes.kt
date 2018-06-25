package org.edtech.curriculum

enum class PurposeType {
    PARAGRAPH, BULLET
}

enum class GradeStep {
    F, E, D, C, B, A, G, X
}

data class Subject(
    val name: String,
    val description: String,
    val code: String,
    val designation: String?,
    val skolfsId: String,
    val purposes: List<Purpose>,
    val courses: List<Course>?,
    val centralContents: List<CentralContentsGr>?,
    val knowledgeRequirementParagraphs: List<KnowledgeRequirementParagraphGr>?
)

enum class TypeOfCentralContent {
    WITHIN_LANGUAGE_CHOICE_CHINESE,
    WITHIN_STUDENT_CHOICE,
    WITHIN_LANGUAGE_CHOICE,
    BLOCK_SYLLABUS,
    FIRST_LANGUAGE,
    SIGN_LANGUAGE_FOR_BEGINNERS,
    SECOND_LANGUAGE,
    WITHIN_STUDENT_CHOICE_CHINESE,
    MASTERY_GRADING,
    FIN_LANGUAGE_FIRST,
    FIN_LANGUAGE_SECOND,
    MEANKIELI_LANGUAGE_FIRST,
    MEANKIELI_LANGUAGE_SECOND,
    JIDDISH_LANGUAGE_FIRST,
    JIDDISH_LANGUAGE_SECOND,
    ROMANI_LANGUAGE_FIRST,
    ROMANI_LANGUAGE_SECOND
}

enum class TypeOfRequirement {
    WITHIN_LANGUAGE_CHOICE_CHINESE,
    WITHIN_STUDENT_CHOICE,
    WITHIN_LANGUAGE_CHOICE,
    BLOCK_SYLLABUS,
    FIRST_LANGUAGE,
    SIGN_LANGUAGE_FOR_BEGINNERS,
    SECOND_LANGUAGE,
    WITHIN_STUDENT_CHOICE_CHINESE,
    MASTERY_GRADING,
    MEANKIELI_LANGUAGE_FIRST,
    MEANKIELI_LANGUAGE_SECOND,
    FIN_LANGUAGE_FIRST,
    FIN_LANGUAGE_SECOND,
    JIDDISH_LANGUAGE_FIRST,
    JIDDISH_LANGUAGE_SECOND,
    ROMANI_LANGUAGE_FIRST,
    ROMANI_LANGUAGE_SECOND
}

data class CentralContentsGr(
        val heading: String,
        val year: YearGroup,
        val type: TypeOfCentralContent?,
        val centralContents: List<CentralContent>
)

data class KnowledgeRequirementParagraphGr(
        val heading: String,
        val year: Int,
        val type: TypeOfRequirement?,
        val knowledgeRequirements: List<KnowledgeRequirement>
)

data class Purpose(
    val type: PurposeType,
    val heading: String,
    val lines: List<String>
)

data class Course(
        val name: String,
        val description: String,
        val code: String,
        val centralContent: List<CentralContent>,
        val knowledgeRequirementParagraphs: List<KnowledgeRequirementParagraph>,
        val point: Int? = null,
        val year: YearGroup? = null
)

data class YearGroup(
    val start: Int?,
    val end: Int
)

data class CentralContent(
        val heading: String,
        val lines: List<String>
)


data class KnowledgeRequirementParagraph(
    val heading: String,
    val knowledgeRequirements: List<KnowledgeRequirement>
)

data class KnowledgeRequirement(
    val text: String,
    val knowledgeRequirementChoice: Map<GradeStep, String>
)
