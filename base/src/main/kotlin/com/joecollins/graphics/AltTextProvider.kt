package com.joecollins.graphics

import java.util.concurrent.Flow

interface AltTextProvider {
    companion object {
        const val ALT_TEXT_MAX_LENGTH = 1000
    }

    val altText: Flow.Publisher<String?>
}
