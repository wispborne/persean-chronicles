import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/////////////////
// ATTN: CHANGE ME
val starsectorDirectory = "C:/Program Files (x86)/Fractal Softworks/Starsector"
/////////////////
val perseanChroniclesVersion = "0.11.0"
val questgiverVersion = "2.0.0"
val jarFileName = "PerseanChronicles.jar"
/////////////////

val starsectorCoreDirectory = "$starsectorDirectory/starsector-core"
val starsectorModDirectory = "$starsectorDirectory/mods"

plugins {
    kotlin("jvm") version "1.3.60"
    java
}

version = perseanChroniclesVersion

repositories {
    maven(url = uri("$projectDir/libs"))
    jcenter()
}

dependencies {
    val kotlinVersionInLazyLib = "1.4.21"

    // Questgiver lib
    implementation(fileTree("libs")
    {
        include("Questgiver-$questgiverVersion*")
    })

    // Get kotlin sdk from LazyLib during runtime, only use it here during compile time
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersionInLazyLib")
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlinVersionInLazyLib")


    compileOnly(fileTree("$starsectorModDirectory/LazyLib/jars") { include("*.jar") })
    compileOnly(fileTree("$starsectorModDirectory/Console Commands/jars") { include("*.jar") })

    // Starsector jars and dependencies
    implementation(fileTree(starsectorCoreDirectory) {
        include(
            "starfarer.api.jar",
            "starfarer.api-sources.jar",
            "starfarer_obf.jar",
            "json.jar",
            "xstream-1.4.10.jar",
            "log4j-1.2.9.jar",
            "lwjgl.jar"
        )
//        exclude("*_obf.jar")
    })
}

tasks {
    named<Jar>("jar")
    {
        destinationDirectory.set(file("$rootDir/jars"))
        archiveFileName.set(jarFileName)
    }

    register("debug-starsector", Exec::class) {
        println("Starting debugger for Starsector...")
        workingDir = file(starsectorCoreDirectory)


        commandLine = if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            listOf("cmd", "/C", "debug-starsector.bat")
        } else {
            listOf("./starsectorDebug.bat")
        }
    }

    register("run-starsector", Exec::class) {
        println("Starting Starsector...")
        workingDir = file(starsectorCoreDirectory)

        commandLine = if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            listOf("cmd", "/C", "starsector.bat")
        } else {
            listOf("./starsector.bat")
        }
    }

    register("create-metadata-files") {
        val version = perseanChroniclesVersion.split(".").let { javaslang.Tuple3(it[0], it[1], it[2]) }
        val modId = "wisp_perseanchronicles"
        val modName = "Persean Chronicles"
        val author = "Wispborne"
        val description = "Adds a small collection of quests to bars around the Persean Sector."
        val gameVersion = "0.9.1a"
        val jars = arrayOf("jars/PerseanChronicles.jar", "libs/Questgiver-$questgiverVersion.jar")
        val modPlugin = "org.wisp.stories.LifecyclePlugin"
        val isUtilityMod = false
        val masterVersionFile = "https://raw.githubusercontent.com/davidwhitman/stories/master/$modId.version"
        val modThreadId = "000000"

        File(projectDir, "mod_info.json")
            .writeText(
                """
                    {
                        "id": "$modId",
                        "name": "$modName",
                        "author": "$author",
                        "utility": "$isUtilityMod",
                        "version": "${listOf(version._1, version._2, version._3).joinToString(separator = ".")}",
                        "description": "$description",
                        "gameVersion": "$gameVersion",
                        "jars":[${jars.joinToString() { "\"$it\"" }}],
                        "modPlugin":"$modPlugin"
                    }
                """.trimIndent()
            )

        File(projectDir, "$modId.version")
            .writeText(
                """
                    {
                        "masterVersionFile":"$masterVersionFile",
                        "modName":"$modName",
                        "modThreadId":$modThreadId,
                        "modVersion":
                        {
                            "major":${version._1},
                            "minor":${version._2},
                            "patch":${version._3}
                        }
                    }
                """.trimIndent()
            )
    }
}

// Compile to Java 6 bytecode so that Starsector can use it
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.6"
}