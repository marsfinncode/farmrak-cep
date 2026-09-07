package com.farmpulse.app.data

enum class LogKind { WATER, FERTILIZER, WARNING, CONNECTION, INFO }

data class LogEntry(
    val kind: LogKind,
    val message: String,
    val timestampMillis: Long
)
