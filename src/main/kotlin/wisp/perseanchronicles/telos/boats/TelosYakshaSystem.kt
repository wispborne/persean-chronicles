package wisp.perseanchronicles.telos.boats

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipSystemAPI
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript
import com.fs.starfarer.api.plugins.ShipSystemStatsScript

class TelosYakshaSystem : BaseShipSystemScript() {
    var applied = false
    override fun isUsable(system: ShipSystemAPI?, ship: ShipAPI): Boolean {
        if (!applied) {
            repeat(times = 4) {
                ship.useSystem()
            }

            applied = true
        }
        return super.isUsable(system, ship)
    }

    override fun apply(
        stats: MutableShipStatsAPI,
        id: String,
        state: ShipSystemStatsScript.State,
        effectLevel: Float
    ) {
        if (Global.getCombatEngine().isPaused) return
        val ship = stats.entity as ShipAPI


        if (state == ShipSystemStatsScript.State.IN) {
        }

        if (state == ShipSystemStatsScript.State.ACTIVE) {
        }

        if (state == ShipSystemStatsScript.State.OUT) {
        }
    }

    override fun unapply(stats: MutableShipStatsAPI, id: String) {
    }
}