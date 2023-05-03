package wisp.perseanchronicles.telos.boats

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipAPI.HullSize
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import wisp.perseanchronicles.telos.TelosCommon
import wisp.questgiver.wispLib.addPara
import wisp.questgiver.wispLib.equalsAny
import kotlin.math.absoluteValue

class TelosEtherNetworkedHullmod : BaseHullMod() {
    companion object {
        const val MANUV_PERCENT = -50
        const val SPEED_PERCENT = -50
        const val PPT_PERCENT = -20
    }

    override fun applyEffectsBeforeShipCreation(hullSize: HullSize?, stats: MutableShipStatsAPI?, id: String?) {
        super.applyEffectsBeforeShipCreation(hullSize, stats, id)
        stats ?: return

        if (TelosCommon.isPhase1) {
            return
        }

        val isDebuffed = stats.fleetMember?.captain?.hasTag(TelosCommon.ETHER_OFFICER_TAG) ?: true

        if (isDebuffed) {
            // Manuverability
            stats.acceleration.modifyPercent(id, MANUV_PERCENT.toFloat())
            stats.deceleration.modifyPercent(id, MANUV_PERCENT.toFloat())
            stats.turnAcceleration.modifyPercent(id, MANUV_PERCENT.toFloat())
            stats.maxTurnRate.modifyPercent(id, MANUV_PERCENT.toFloat())
            // Max Speed
            stats.maxSpeed.modifyPercent(id, SPEED_PERCENT.toFloat())
            // PPT
            stats.peakCRDuration.modifyPercent(id, PPT_PERCENT.toFloat())
        }
    }

    @Transient
    private var rotation: Float? = null

    override fun advanceInCombat(ship: ShipAPI?, amount: Float) {
        super.advanceInCombat(ship, amount)

        val engine = Global.getCombatEngine() ?: return

        // I really think waiting for the rest of the questline to be finished
        // will be more rewarding than cheating in access to these unfinished ships early,
        // but if you absolutely can't wait, comment this out or change it to `if (false)`.
        if (TelosCommon.isPhase1) {
            if (ship?.hullSpec?.hullId?.equalsAny(TelosCommon.AVALOK_ID, TelosCommon.ITESH_ID) == true) {
                rotation = if (rotation == null || engine.getTotalElapsedTime(false) < 5f) {
                    0f
                } else {
                    (amount + rotation!!).coerceAtMost(100f)
                }

                ship.angularVelocity = (ship.angularVelocity + rotation!!).coerceAtMost(50f)
            }
        }
    }
    //
//    override fun getDescriptionParam(index: Int, hullSize: HullSize?, ship: ShipAPI?): String? {
//        var effectModifier = 1f
//        if (ship != null) effectModifier = ship.mutableStats.dynamic.getValue(Stats.DMOD_EFFECT_MULT)
//
//        val isDebuffed = ship?.captain?.hasTag(TelosCommon.ETHER_OFFICER_TAG) ?: true
//
//        return when (index) {
//            0 -> ""
//            1 -> ""
//            2 -> ""
//            else -> ""
//        }
//    }

    override fun addPostDescriptionSection(tooltip: TooltipMakerAPI?, hullSize: HullSize?, ship: ShipAPI?, width: Float, isForModSpec: Boolean) {
        super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec)
        tooltip ?: return

        // Disable during phase 1 because players not taking Ether will never be able to remove the debuff, which means the only reasonable decision
        // will be them always taking the Ether fork.
        if (TelosCommon.isPhase1) {
            if (ship?.hullSpec?.hullId?.equalsAny(TelosCommon.AVALOK_ID, TelosCommon.ITESH_ID) == true) {
                tooltip.addPara(textColor = Misc.getNegativeHighlightColor()) { "WARNING: side effects of accessing hidden content include dizzyness." }
            } else {
                tooltip.addPara { "Does nothing...yet." }
            }
            return
        }

        val isDebuffed = ship?.captain?.hasTag(TelosCommon.ETHER_OFFICER_TAG) != true
        val tc = if (isDebuffed) Misc.getTextColor() else Misc.getGrayColor()

        tooltip.addSectionHeading(
            /* str = */ "Penalty",
            /* textColor = */
            if (isDebuffed) Misc.getNegativeHighlightColor() else Misc.getGrayColor(),
            /* bgColor = */
            if (isDebuffed) Misc.setAlpha(Misc.scaleColorOnly(Misc.getNegativeHighlightColor(), 0.4f), 175) else Misc.setAlpha(
                Misc.scaleColorOnly(
                    Misc.getGrayColor(),
                    0.4f
                ), 175
            ),
            /* align = */
            Alignment.MID,
            /* pad = */
            10f
        )
        tooltip.addPara(textColor = tc, highlightColor = if (isDebuffed) Misc.getHighlightColor() else tc) {
            "Reduces maneuverability by ==${MANUV_PERCENT.absoluteValue}%==, top speed by ==${SPEED_PERCENT.absoluteValue}%==, and PPT by ==${PPT_PERCENT.absoluteValue}%==.\n"
        }
    }

    override fun getUnapplicableReason(ship: ShipAPI?) = "Requires a Telos ship."
}