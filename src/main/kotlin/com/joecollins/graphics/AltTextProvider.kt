package com.joecollins.graphics

import java.util.concurrent.Flow

interface AltTextProvider {

    val altText: Flow.Publisher<String?>
}
