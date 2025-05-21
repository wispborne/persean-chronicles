package wisp.tools

import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import java.io.File

fun main() {
    // Define the project directory. System.getProperty("user.dir") usually works if running from IntelliJ project root.
    // You can adjust this path if needed, e.g., File(".") for current dir, or an absolute path.
    val projectDir = File(System.getProperty("user.dir"))
    val inputDir = projectDir.resolve("data/strings")
    val outputDirName = "compiled" // The subdirectory for compiled files

    println("Looking for YAML files in: ${inputDir.absolutePath}")

    // Find .yaml files, ensuring they are files and the directory exists
    val yamlFiles = inputDir.listFiles { file -> file.isFile && file.name.endsWith(".yaml") }
        ?.toList()
        ?: emptyList()

    if (yamlFiles.isEmpty()) {
        println("No YAML files found to transpile in ${inputDir.absolutePath}.")
        return
    }

    println("Transpiling the following YAML files:\n${yamlFiles.joinToString(separator = "\n") { " - ${it.name}" }}")

    val yamlMapper = YAMLMapper()
    val jsonWriter = JsonMapper.builder().build().writerWithDefaultPrettyPrinter()

    var successCount = 0
    var failCount = 0

    yamlFiles.forEach { yamlFile ->
        try {
            val yamlNode = yamlMapper.readTree(yamlFile)

            // Determine output directory and create it if it doesn't exist
            val outputParentDir = yamlFile.parentFile.resolve(outputDirName)
            outputParentDir.mkdirs() // Create 'compiled' subdirectory

            val jsonOutputFile = File(outputParentDir, "${yamlFile.nameWithoutExtension}.hjson")

            jsonWriter.writeValue(jsonOutputFile, yamlNode)
            println("Successfully transpiled ${yamlFile.name} to ${jsonOutputFile.absolutePath}")
            successCount++
        } catch (e: Exception) {
            System.err.println("ERROR: Failed to transpile ${yamlFile.name}: ${e.message}")
            // e.printStackTrace() // Uncomment for full stack trace
            failCount++
        }
    }

    println("\nTranspilation summary: $successCount succeeded, $failCount failed.")
}