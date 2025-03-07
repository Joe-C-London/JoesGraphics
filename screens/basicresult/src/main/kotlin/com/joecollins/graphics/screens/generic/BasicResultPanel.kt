package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.ImageGenerator
import com.joecollins.models.general.Candidate
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyOrCandidate
import com.joecollins.models.general.PartyOrCoalition
import com.joecollins.models.general.PartyResult
import java.awt.Shape

object BasicResultPanel {

    interface KeyTemplate<KT, KPT : PartyOrCoalition> {
        fun toParty(key: KT): KPT
        fun toMainBarHeader(key: KT, forceSingleLine: Boolean): List<String>
        fun toMainAltTextHeader(key: KT): String
        fun incumbentShape(key: KT, forceSingleLine: Boolean): Shape?
        fun winnerShape(forceSingleLine: Boolean): Shape
        fun runoffShape(forceSingleLine: Boolean): Shape
    }

    class PartyTemplate<P : PartyOrCoalition> : KeyTemplate<P, P> {
        override fun toParty(key: P): P = key

        override fun toMainBarHeader(key: P, forceSingleLine: Boolean): List<String> = listOf(key.name.uppercase())

        override fun toMainAltTextHeader(key: P): String = key.name.uppercase()

        override fun incumbentShape(key: P, forceSingleLine: Boolean): Shape? = null

        override fun winnerShape(forceSingleLine: Boolean): Shape = ImageGenerator.createTickShape()

        override fun runoffShape(forceSingleLine: Boolean): Shape = ImageGenerator.createRunoffShape()
    }

    class PartyOrCandidateTemplate : KeyTemplate<PartyOrCandidate, Party> {
        override fun toParty(key: PartyOrCandidate): Party = key.party

        override fun toMainBarHeader(key: PartyOrCandidate, forceSingleLine: Boolean): List<String> = listOf(key.name.uppercase())

        override fun toMainAltTextHeader(key: PartyOrCandidate): String = key.name.uppercase()

        override fun incumbentShape(key: PartyOrCandidate, forceSingleLine: Boolean): Shape? = null

        override fun winnerShape(forceSingleLine: Boolean): Shape = ImageGenerator.createTickShape()

        override fun runoffShape(forceSingleLine: Boolean): Shape = ImageGenerator.createRunoffShape()
    }

    class CandidateTemplate : KeyTemplate<Candidate, Party> {
        private val incumbentMarker: String?

        constructor() {
            incumbentMarker = null
        }

        constructor(incumbentMarker: String?) {
            this.incumbentMarker = incumbentMarker
        }

        override fun toParty(key: Candidate): Party = key.party

        override fun toMainBarHeader(key: Candidate, forceSingleLine: Boolean): List<String> = if (key.name.isBlank()) {
            listOf(key.party.name.uppercase())
        } else if (forceSingleLine) {
            listOf(("${key.name} (" + key.party.abbreviation + ")").uppercase())
        } else {
            listOf(key.name.uppercase(), key.party.name.uppercase())
        }

        override fun toMainAltTextHeader(key: Candidate): String = if (key.name.isBlank()) {
            key.party.name.uppercase()
        } else {
            ("${key.name}${if (incumbentMarker != null && key.incumbent) " [$incumbentMarker]" else ""} (${key.party.abbreviation})")
                .uppercase()
        }

        override fun incumbentShape(key: Candidate, forceSingleLine: Boolean): Shape? = if (incumbentMarker == null || !key.incumbent) {
            null
        } else {
            ImageGenerator.createBoxedTextShape(incumbentMarker)
        }

        override fun winnerShape(forceSingleLine: Boolean): Shape = ImageGenerator.createTickShape()

        override fun runoffShape(forceSingleLine: Boolean): Shape = ImageGenerator.createRunoffShape()
    }

    class CurrDiff<CT>(val curr: CT, val diff: CT)

    fun <T> partyMapToResultMap(m: Map<T, PartyOrCoalition?>): Map<T, PartyResult?> = m.mapValues { e -> e.value?.let { PartyResult.elected(it.toParty()) } }
}
