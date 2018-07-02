package org.edtech.curriculum

import java.io.File



fun main(args: Array<String>) {
    val useCache = false
    val testResources = File("$TEST_RESOURCES_PATH/opendata/")

    SchoolType.values()
            .distinctBy { it.filename }
            .forEach {
        Curriculum(it, testResources, useCache)
        println("Downloaded ${it.filename}")
    }
    println("Done!")
}
