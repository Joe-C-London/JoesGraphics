import com.joecollins.graphics.utils.ColorUtils
import com.joecollins.models.general.CandidateResult
import com.joecollins.models.general.PartyResult
import java.awt.Color

object ResultColorUtils {

    val CandidateResult?.color: Color
        get() {
            return when {
                this == null -> java.awt.Color.BLACK
                elected -> candidate.party.color
                else -> ColorUtils.lighten(candidate.party.color)
            }
        }

    val PartyResult?.color: Color get() {
        return when {
            this == null -> java.awt.Color.BLACK
            this.elected -> party.color
            else -> ColorUtils.lighten(party.color)
        }
    }
}
