package dev.suresh

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.enumConstants
import com.lemonappdev.konsist.api.ext.list.modifierprovider.withEnumModifier
import com.lemonappdev.konsist.api.ext.list.withAnnotationOf
import com.lemonappdev.konsist.api.verify.assertTrue
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.junit.jupiter.api.condition.EnabledIfSystemProperty
import org.slf4j.LoggerFactory
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@Testcontainers
@EnabledIfSystemProperty(named = "ktorTest", matches = "true")
class AppTests {

  companion object {

    val logger = LoggerFactory.getLogger("postgres")

    @Container
    val pg =
        PostgreSQLContainer(DockerImageName.parse("postgres:alpine"))
            .withDatabaseName("test")
            .withUsername("postgres")
            .withPassword("test")
            .withExposedPorts(5432)
            .withLogConsumer(Slf4jLogConsumer(logger, false))
            .withReuse(true)
    //  .withClasspathResourceMapping("db/migration/..sql", "/docker-entrypoint-initdb.d/..sql",
    // BindMode.READ_ONLY)
  }

  @Test
  fun testDB() {
    assertTrue(pg.isRunning())
    println(pg.getJdbcUrl())
  }

  fun `make sure the enum classes have serial annotation`() {
    Konsist.scopeFromSourceSet("main")
        .classes()
        .withEnumModifier()
        .withAnnotationOf(Serializable::class)
        .enumConstants
        .assertTrue { it.hasAnnotationOf(SerialName::class) }
  }
}
