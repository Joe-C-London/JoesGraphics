import com.joecollins.graphics.utils.ColorUtils
import com.joecollins.models.general.CandidateResult
import com.joecollins.models.general.PartyResult
import java.awt.Color

@Suppress("UNUSED_PARAMETER")
object ResultColorUtils {

    fun CandidateResult?.getColor(toForceNamedParams: Unit = Unit, default: Color): Color {
        return when {
            this == null -> default
            elected -> candidate.party.color
            else -> ColorUtils.lighten(candidate.party.color)
        }
    }

    fun PartyResult?.getColor(toForceNamedParams: Unit = Unit, default: Color): Color {
        return when {
            this == null -> default
            this.elected -> party.color
            else -> ColorUtils.lighten(party.color)
        }
    }
}
