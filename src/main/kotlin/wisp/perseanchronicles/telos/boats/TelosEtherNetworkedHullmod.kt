package wisp.perseanchronicles.telos.boats

import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipAPI.HullSize
import com.fs.starfarer.api.impl.campaign.ids.Stats
import kotlin.math.absoluteValue

class TelosEtherNetworkedHullmod : BaseHullMod() {
    companion object {
        const val MANUV_PERCENT = -50
        const val SPEED_PERCENT = -50
        const val PPT_PERCENT = -20
    }

    override fun applyEffectsAfterShipCreation(ship: ShipAPI?, id: String?) {
        super.applyEffectsAfterShipCreation(ship, id)
    }

    override fun applyEffectsBeforeShipCreation(hullSize: HullSize?, stats: MutableShipStatsAPI?, id: String?) {
        super.applyEffectsBeforeShipCreation(hullSize, stats, id)
        stats ?: return

        val isDebuffed = true // ETHER_OFFICER_TAG

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

    override fun getDescriptionParam(index: Int, hullSize: HullSize?, ship: ShipAPI?): String? {
        var effectModifier = 1f
        if (ship != null) effectModifier = ship.mutableStats.dynamic.getValue(Stats.DMOD_EFFECT_MULT)

        val isDebuffed = true // ETHER_OFFICER_TAG

        return when (index) {
            0 -> "${MANUV_PERCENT.absoluteValue}%"
            1 -> "${SPEED_PERCENT.absoluteValue}%"
            2 -> "${PPT_PERCENT.absoluteValue}%"
            else -> ""
        }
    }

    override fun getUnapplicableReason(ship: ShipAPI?): String? {
        return "Requires a Telos ship."
    }
}