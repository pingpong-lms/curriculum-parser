package org.edtech.curriculum

import java.time.Instant

data class SubjectHtml(val name: String,
                   val description: String,
                   val code: String,
                   val designation: String,
                   val skolfsId: String,
                   val purposes: String,
                   val courses: List<CourseHtml>?,
                   val centralContent: List<String>?,
                   val knowledgeRequirement: List<Map<GradeStep, String>>?,
                   val applianceDate: Instant?
)
data class CourseHtml(val name: String,
                  val description: String,
                  val code: String,
                  val year: String,
                  val point: String,
                  val centralContent: String,
                  val knowledgeRequirement: Map<GradeStep, String>
)


data class SubjectHtmlGr(val name: String,
                       val description: String,
                       val code: String,
                       val designation: String,
                       val skolfsId: String,
                       val purposes: String,
                       val courses: List<CourseHtml>,
                       val applianceDate: Instant?
)

data class CentralContentGrHtml(val year: String, val text: String)

data class KnowledgeRequirementGrHtml(val year: String, val data: Map<GradeStep, String>)

data class GrData(
        val centralContents: List<CentralContentGrHtml>,
        val knowledgeRequirement: List<KnowledgeRequirementGrHtml>
)
