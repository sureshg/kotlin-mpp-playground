package dev.suresh

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication class App

fun main(args: Array<String>) {
  System.setProperty("spring.classformat.ignore", "true")
  runApplication<App>(*args)
}
