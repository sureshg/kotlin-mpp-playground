package dev.suresh

import jdk.jfr.*

@Name("dev.suresh.agent.AgentType")
@Label("JVM Agent Type")
@Description("JVM agent attach type")
@Category("JVM Agent", "Agent")
@Period("5 s")
@StackTrace(false)
class AgentType(@Label("Agent Type") val type: String = "static") : Event()
