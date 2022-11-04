package wisp.perseanchronicles.telos.boats

import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.plugins.ShipSystemStatsScript
import com.fs.starfarer.api.util.IntervalUtil
import wisp.perseanchronicles.game
import wisp.questgiver.wispLib.IntervalUtil
import java.awt.Color

class TelosLungeSystem : BaseShipSystemScript() {
    val speedBoost = 1000f


//    override fun isUsable(system: ShipSystemAPI, ship: ShipAPI): Boolean {
//        return super.isUsable(system, ship) && !ship.isPhased
//    }

    override fun apply(
        stats: MutableShipStatsAPI,
        id: String,
        state: ShipSystemStatsScript.State,
        effectLevel: Float
    ) {
        val ship = stats.entity as ShipAPI
        stats.maxSpeed.modifyFlat(id, speedBoost)
        stats.acceleration.modifyFlat(id, speedBoost)

        if (state == ShipSystemStatsScript.State.IN) {
            ship.allWeapons
                .orEmpty()
                .mapNotNull { it.effectPlugin }
                .filterIsInstance<TelosPhaseEngines>()
                .forEach { it.baseNebulaColorOverride = Color.decode("#F065FF") }
        }

        if (state == ShipSystemStatsScript.State.OUT) {
            stats.maxSpeed.unmodifyFlat(id)

            ship.allWeapons
                .orEmpty()
                .mapNotNull { it.effectPlugin }
                .filterIsInstance<TelosPhaseEngines>()
                .forEach { it.baseNebulaColorOverride = null }

            if (!ship.isPhased) {
                ship.isPhased = true
                game.combatEngine.addPlugin(object : BaseEveryFrameCombatPlugin() {
                    val phaseDuration = IntervalUtil(2f)
                    override fun advance(amount: Float, events: List<InputEventAPI>?) {
                        phaseDuration.advance(amount)
                        if (phaseDuration.intervalElapsed()) {
                            ship.isPhased = false
                            game.combatEngine.removePlugin(this)
                        }
                    }
                })
            }
        }
    }

    override fun unapply(stats: MutableShipStatsAPI, id: String) {
        stats.maxSpeed.unmodifyFlat(id)
        stats.acceleration.unmodifyFlat(id)
    }
}