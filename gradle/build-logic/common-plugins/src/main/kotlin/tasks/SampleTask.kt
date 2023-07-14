package tasks

import org.gradle.api.*
import org.gradle.api.file.*
import org.gradle.api.provider.*
import org.gradle.api.tasks.*

abstract class SampleTask : DefaultTask() {

  @get:Input abstract val greeting: Property<String>

  @get:Input @get:Optional abstract val versions: MapProperty<String, String>

  @get:InputDirectory @get:Optional abstract val inputDirectory: DirectoryProperty

  @get:InputFile @get:Optional abstract val inputFile: RegularFileProperty

  @get:Internal val type = "Sample Task"

  init {
    greeting.convention("Hello Kotlin!")
  }

  @TaskAction
  fun execute() {
    println("Executing task $type. Greeting: ${greeting.get()}")
  }
}
