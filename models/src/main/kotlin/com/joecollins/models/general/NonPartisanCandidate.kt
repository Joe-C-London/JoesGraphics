package com.joecollins.models.general

import java.awt.Color

data class NonPartisanCandidate(
    val fullName: String,
    val description: String? = null,
    val shortDisplayName: String = fullName.split(" ").last(),
    val incumbencyType: IncumbencyType? = null,
    val color: Color = Party.OTHERS.color,
) : CanOverrideSortOrder() {
    constructor(
        fullName: String,
        description: String? = null,
        shortDisplayName: String = fullName.split(" ").last(),
        incumbent: Boolean,
        color: Color = Party.OTHERS.color,
    ) : this(fullName, description, shortDisplayName, if (incumbent) IncumbencyType.DEFAULT else null, color)

    val incumbent = incumbencyType != null
}
