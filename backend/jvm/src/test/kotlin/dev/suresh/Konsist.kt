package dev.suresh

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.enumConstants
import com.lemonappdev.konsist.api.ext.list.modifierprovider.withEnumModifier
import com.lemonappdev.konsist.api.ext.list.withAnnotationOf
import com.lemonappdev.konsist.api.verify.assertTrue
import kotlin.test.Test
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class AppTests {

  @Test
  fun `make sure the enum classes have serial annotation`() {
    Konsist.scopeFromSourceSet("main")
        .classes()
        .withEnumModifier()
        .withAnnotationOf(Serializable::class)
        .enumConstants
        .assertTrue { it.hasAnnotationOf(SerialName::class) }
  }
}
