package org.wisp.stories.wispLib

import org.apache.log4j.Logger

typealias DebugLogger = Logger

internal fun DebugLogger.w(message: () -> String) {
    this.warn(message())
}

internal fun DebugLogger.i(message: () -> String) {
    if (isDebugModeEnabled) {
        this.info(message())
    }
}