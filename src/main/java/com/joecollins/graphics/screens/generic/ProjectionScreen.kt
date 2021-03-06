package com.joecollins.graphics.screens.generic

import com.joecollins.bindings.Binding
import com.joecollins.bindings.BindingReceiver
import com.joecollins.graphics.components.ProjectionFrame
import java.awt.Color
import java.awt.GridLayout
import java.awt.Image
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

class ProjectionScreen private constructor(
    text: Binding<String?>,
    color: Binding<Color>,
    image: Binding<Image?>,
    imageAlignment: Binding<ProjectionFrame.Alignment>
) : JPanel() {

    companion object {
        @JvmStatic fun createScreen(
            text: Binding<String?>,
            color: Binding<Color>,
            image: Binding<Image?>
        ): ProjectionScreen {
            return ProjectionScreen(
                    text, color, image, Binding.fixedBinding(ProjectionFrame.Alignment.BOTTOM))
        }

        @JvmStatic fun createScreen(
            text: Binding<String?>,
            color: Binding<Color>,
            image: Binding<Image?>,
            imageAlignment: Binding<ProjectionFrame.Alignment>
        ): ProjectionScreen {
            return ProjectionScreen(text, color, image, imageAlignment)
        }
    }

    init {
        layout = GridLayout(1, 1)
        background = Color.WHITE
        border = EmptyBorder(5, 5, 5, 5)
        val colorReceiver = BindingReceiver(color)
        val frame = ProjectionFrame(
            headerBinding = Binding.fixedBinding("PROJECTION"),
            borderColorBinding = colorReceiver.getBinding(),
            imageBinding = image,
            backColorBinding = colorReceiver.getBinding(),
            footerTextBinding = text,
            imageAlignmentBinding = imageAlignment
        )
        add(frame)
    }
}
