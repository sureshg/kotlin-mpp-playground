package dev.suresh.plugins.custom

import io.ktor.server.sessions.SessionSerializer

/** Custom Cookie Session serializer sample. */
data class CookieSession(val text: String)

object CookieSessionSerializer : SessionSerializer<CookieSession> {
  override fun deserialize(text: String): CookieSession = CookieSession(text = text)

  override fun serialize(session: CookieSession): String = session.text
}
