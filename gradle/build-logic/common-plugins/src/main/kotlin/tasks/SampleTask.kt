package tasks

import org.gradle.api.*
import org.gradle.api.file.*
import org.gradle.api.provider.*
import org.gradle.api.tasks.*
import org.gradle.api.tasks.options.Option

@CacheableTask
abstract class SampleTask : DefaultTask() {

  @get:Input
  @get:Option(option = "greeting", description = "Greetings Task Property")
  abstract val greeting: Property<String>

  @get:[Input Optional]
  abstract val versions: MapProperty<String, String>

  @get:[InputDirectory Optional]
  abstract val inputDirectory: DirectoryProperty

  @get:[InputFile Optional]
  abstract val inputFile: RegularFileProperty

  @get:Internal val type = "Sample Task"

  init {
    greeting.convention("Hello Kotlin!")
  }

  @TaskAction
  fun execute() {
    println("Executing task $type. Greeting: ${greeting.get()}")
  }
}
