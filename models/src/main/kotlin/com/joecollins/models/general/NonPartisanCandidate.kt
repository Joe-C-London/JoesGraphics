package com.joecollins.models.general

import java.awt.Color

data class NonPartisanCandidate(
    val fullName: String,
    val description: String? = null,
    val surname: String = fullName.split(" ").last(),
    val color: Color = Party.OTHERS.color,
) : CanOverrideSortOrder()
