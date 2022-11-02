package wisp.perseanchronicles.telos.boats

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript
import com.fs.starfarer.api.plugins.ShipSystemStatsScript

class TelosLungeSystem : BaseShipSystemScript() {
//    val cooldownMillis = 1000f
    val speedBoost = 200f

//    val cooldownTimer = IntervalUtil(cooldownMillis, cooldownMillis)

    override fun apply(
        stats: MutableShipStatsAPI,
        id: String,
        state: ShipSystemStatsScript.State,
        effectLevel: Float
    ) {
//        val ship = stats.entity as ShipAPI

        stats.maxSpeed.modifyFlat(id, speedBoost)

        if (state == ShipSystemStatsScript.State.OUT) {
            stats.maxSpeed.unmodifyFlat(id)
        }
    }

    override fun unapply(stats: MutableShipStatsAPI, id: String) {
        stats.maxSpeed.unmodifyFlat(id)
    }
}