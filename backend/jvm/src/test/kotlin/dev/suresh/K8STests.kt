package dev.suresh

import io.kubernetes.client.openapi.apis.CoreV1Api
import io.kubernetes.client.util.Config
import kotlin.test.Test
import kotlin.test.assertTrue
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.slf4j.LoggerFactory
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.k3s.K3sContainer
import org.testcontainers.utility.DockerImageName

@Testcontainers
@EnabledIfEnvironmentVariable(named = "K8S_TEST_ENABLED", matches = "true")
class K8STests {

  companion object {

    val logger = LoggerFactory.getLogger("K8S")

    @Container
    val k3s =
        K3sContainer(DockerImageName.parse("rancher/k3s:latest"))
            .withLogConsumer(Slf4jLogConsumer(logger))
            .withReuse(true)
    // .withCommand("server", "--disable=traefik",
    // "--tls-san=${DockerClientFactory.instance().dockerHostIpAddress()}")
  }

  @Test
  fun testK8S() {
    assertTrue(k3s.isRunning)
  }

  @Test
  fun testK8SClient() {
    val client = Config.fromConfig(k3s.kubeConfigYaml.reader())
    val api = CoreV1Api(client)
    api.listNode().execute().items.forEach { println("K8S NodeName: ${it.metadata?.name}") }
  }
}
