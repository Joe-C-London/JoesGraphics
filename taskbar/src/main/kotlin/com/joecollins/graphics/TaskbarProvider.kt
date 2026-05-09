package com.joecollins.graphics

import java.awt.Image
import java.util.concurrent.Flow

interface TaskbarProvider {

    val taskbarIcon: Flow.Publisher<Image>?
}
