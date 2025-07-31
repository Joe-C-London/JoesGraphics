package com.joecollins.models.general

import java.awt.Color

data class NonPartisanCandidate(
    val fullName: String,
    val description: String? = null,
    val surname: String = fullName.split(" ").last(),
    val incumbencyType: IncumbencyType? = null,
    val color: Color = Party.OTHERS.color,
) : CanOverrideSortOrder() {
    constructor(
        fullName: String,
        description: String? = null,
        surname: String = fullName.split(" ").last(),
        incumbent: Boolean,
        color: Color = Party.OTHERS.color,
    ) : this(fullName, description, surname, if (incumbent) IncumbencyType.DEFAULT else null, color)

    val incumbent = incumbencyType != null
}
