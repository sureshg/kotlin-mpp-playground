package dev.suresh

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.enumConstants
import com.lemonappdev.konsist.api.ext.list.modifierprovider.withEnumModifier
import com.lemonappdev.konsist.api.ext.list.withAnnotationOf
import com.lemonappdev.konsist.api.verify.assertTrue
import io.ktor.client.*
import io.ktor.client.engine.java.*
import io.ktor.client.request.*
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.altindag.ssl.SSLFactory
import org.junit.jupiter.api.condition.EnabledIfSystemProperty
import org.slf4j.LoggerFactory
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.images.builder.Transferable
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@Testcontainers
@EnabledIfSystemProperty(named = "ktorTest", matches = "true")
class AppTests {

  companion object {

    val logger = LoggerFactory.getLogger("docker")

    @Container
    val pg =
        PostgreSQLContainer(DockerImageName.parse("postgres:alpine"))
            .withDatabaseName("test")
            .withUsername("postgres")
            .withPassword("test")
            .withExposedPorts(5432)
            .withLogConsumer(Slf4jLogConsumer(logger, false))
            .withReuse(true)
    // .withClasspathResourceMapping("db/migration/..sql",
    // "/docker-entrypoint-initdb.d/..sql", BindMode.READ_ONLY)
  }

  @Test
  fun testDB() {
    assertTrue(pg.isRunning())
    println(pg.getJdbcUrl())
  }

  @Test
  fun testGenericContainer() = runTest {
    val certDir = "/usr/share/app/config/certs"
    val cert = "$certDir/cert.pem"
    val key = "$certDir/key.pem"
    val docRoot = "/var/www/localhost/htdocs"
    val tlsPort = 443

    val nginx =
        GenericContainer(DockerImageName.parse("alpine:latest"))
            .withEnv("TLS_CERT_PATH", certDir)
            .withCopyToContainer(
                Transferable.of(
                    """
                    #!/bin/sh
                    set -e
                    echo "Entrypoint args: "\${'$'}@""
                    apk add nginx
                    echo "<h1>Hello world!</h1>" > $docRoot/index.html;
                    cat <<EOF > /etc/nginx/http.d/default.conf
                    server {
                            listen 80 default_server;
                            listen [::]:80 default_server;
                            listen 443 ssl http2 default_server;
                            listen [::]:443 ssl http2 default_server;
                            ssl_certificate $cert;
                            ssl_certificate_key $key;
                            location / {
                                    root $docRoot;
                            }

                            location = /404.html {
                                    internal;
                            }
                    }
                    EOF
                    exec nginx -g "daemon off;"
                    """
                        .trimIndent(),
                    365),
                "/entrypoint.sh")
            .withCopyToContainer(
                Transferable.of(
                    """
                    #!/bin/sh
                    apk add openssl
                    mkdir -p $certDir
                    openssl req -x509 \
                           -newkey rsa:4096 -sha256 -nodes \
                           -days 365 \
                           -subj "/CN=localhost" \
                           -addext "subjectAltName=DNS:localhost.com,IP:127.0.0.1" \
                           -keyout $key \
                           -out $cert
                    # -outform der -keyform der
                    """
                        .trimIndent(),
                    365),
                "$certDir/gen-certs.sh")
            .withCommand("sh", "-c", "$certDir/gen-certs.sh && /entrypoint.sh")
            .withExposedPorts(tlsPort)
            .withLogConsumer(Slf4jLogConsumer(logger, false))
            .waitingFor(Wait.forHttps("/").allowInsecure().forStatusCode(200))

    nginx.use {
      it.start()
      val endPoint = "https://${it.host}:${it.getMappedPort(tlsPort)}/"
      val client =
          HttpClient(Java) {
            engine {
              config {
                sslContext(SSLFactory.builder().withUnsafeTrustMaterial().build().sslContext)
              }
            }
          }

      val statusCode = client.get(endPoint).status
      assertTrue { statusCode.value == 200 }
    }
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
