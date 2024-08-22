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
        fun toMainBarHeader(key: KT, forceSingleLine: Boolean): String
        fun winnerShape(forceSingleLine: Boolean): Shape
        fun runoffShape(forceSingleLine: Boolean): Shape
    }

    class PartyTemplate<P : PartyOrCoalition> : KeyTemplate<P, P> {
        override fun toParty(key: P): P {
            return key
        }

        override fun toMainBarHeader(key: P, forceSingleLine: Boolean): String {
            return key.name.uppercase()
        }

        override fun winnerShape(forceSingleLine: Boolean): Shape {
            return ImageGenerator.createTickShape()
        }

        override fun runoffShape(forceSingleLine: Boolean): Shape {
            return ImageGenerator.createRunoffShape()
        }
    }

    class PartyOrCandidateTemplate : KeyTemplate<PartyOrCandidate, Party> {
        override fun toParty(key: PartyOrCandidate): Party {
            return key.party
        }

        override fun toMainBarHeader(key: PartyOrCandidate, forceSingleLine: Boolean): String {
            return key.name.uppercase()
        }

        override fun winnerShape(forceSingleLine: Boolean): Shape {
            return ImageGenerator.createTickShape()
        }

        override fun runoffShape(forceSingleLine: Boolean): Shape {
            return ImageGenerator.createRunoffShape()
        }
    }

    class CandidateTemplate : KeyTemplate<Candidate, Party> {
        private val incumbentMarker: String

        constructor() {
            incumbentMarker = ""
        }

        constructor(incumbentMarker: String?) {
            this.incumbentMarker = if (incumbentMarker == null) "" else " [$incumbentMarker]"
        }

        override fun toParty(key: Candidate): Party {
            return key.party
        }

        override fun toMainBarHeader(key: Candidate, forceSingleLine: Boolean): String {
            return if (key === Candidate.OTHERS) {
                key.party.name.uppercase()
            } else {
                ("${key.name}${if (key.isIncumbent()) incumbentMarker else ""}${if (forceSingleLine) (" (" + key.party.abbreviation + ")") else ("\n" + key.party.name)}")
                    .uppercase()
            }
        }

        override fun winnerShape(forceSingleLine: Boolean): Shape {
            return if (forceSingleLine) ImageGenerator.createTickShape() else ImageGenerator.createHalfTickShape()
        }

        override fun runoffShape(forceSingleLine: Boolean): Shape {
            return if (forceSingleLine) ImageGenerator.createRunoffShape() else ImageGenerator.createHalfRunoffShape()
        }
    }

    class CurrDiff<CT>(val curr: CT, val diff: CT)

    fun <T> partyMapToResultMap(m: Map<T, PartyOrCoalition?>): Map<T, PartyResult?> {
        return m.mapValues { e -> e.value?.let { PartyResult.elected(it.toParty()) } }
    }
}
