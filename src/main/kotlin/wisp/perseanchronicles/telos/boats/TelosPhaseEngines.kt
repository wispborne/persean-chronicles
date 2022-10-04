package wisp.perseanchronicles.telos.boats

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.CombatEngineLayers
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.util.IntervalUtil
import org.lazywizard.lazylib.VectorUtils
import org.lwjgl.util.vector.Vector2f
import wisp.perseanchronicles.game
import wisp.questgiver.wispLib.modify
import wisp.questgiver.wispLib.random
import java.awt.Color
import kotlin.math.roundToInt

/**
 * Originally `tahlan_PhaseEngines`, thank you Nia.
 */
class TelosPhaseEngines : EveryFrameWeaponEffectPlugin {
    companion object {
        val interval = IntervalUtil(0.06f, 0.07f)
    }

    private var alphaMult = 0f

    init {
        Global.getCombatEngine()?.addPlugin(CustomRender())
    }

    override fun advance(amount: Float, engine: CombatEngineAPI, weapon: WeaponAPI) {
        interval.advance(amount)
        val combatEngine = Global.getCombatEngine() ?: return

        // we calculate our alpha every frame since we smoothly shift it
        val ship = weapon.ship
        val ec = ship.engineController
        alphaMult = if (ec.isAccelerating || ec.isStrafingLeft || ec.isStrafingRight) {
            (alphaMult + amount * 2f).coerceAtMost(1f)
        } else if (ec.isDecelerating || ec.isAcceleratingBackwards) {
            if (alphaMult < 0.5f) (alphaMult + amount * 2f).coerceAtMost(0.5f)
            else (alphaMult - amount * 2f).coerceAtLeast(0.5f)
        } else {
            (alphaMult - amount * 2f).coerceAtLeast(0f)
        }

        // jump out if interval hasn't elapsed yet
        if (!interval.intervalElapsed()) return

        val velocityScale = .3f
        val sizeScale = 1.5f
        val durationScale = .6f
        val rampUpScale = 1f
        val alphaScale = .75f
        val endSizeScale = 1.55f

        val vel = Vector2f(100f * velocityScale, 0f * velocityScale)
        VectorUtils.rotate(vel, ship.facing + 180f)

        val negativeColor = Color(24, 254, 109).modify(green = 255, alpha = (1 * alphaMult * alphaScale).roundToInt().coerceIn(0..255))
        val nebulaColor = Color.decode("#5F78CC").modify(alpha = (70 * alphaMult * alphaScale).roundToInt().coerceIn(0..255))
        val swirlyNebulaColor = Color.decode("#3DAECC").modify(alpha = (25 * alphaMult * alphaScale).roundToInt().coerceIn(0..255))

        val negativeNebulaSprite = game.settings.getSprite("misc", "nebula_particles")
        val nebulaSprite = game.settings.getSprite("misc", "nebula_particles")
        val swirlyNebulaSprite = game.settings.getSprite("misc", "fx_particles2")

        for (emitterPoints in ship.hullSpec.allWeaponSlotsCopy) {
            val location = emitterPoints.location.let { Vector2f(it) }.translate(ship.location.x, ship.location.y)
            CustomRender.addNebula(
                location,
                vel,
                (40f..60f).random() * sizeScale,
                endSizeScale,
                (1.2f..1.5f).random() * durationScale,
                0.1f * rampUpScale,
                0.5f,
                negativeColor,
                CombatEngineLayers.UNDER_SHIPS_LAYER,
                CustomRender.NebulaType.SWIRLY,
                true
            )

            CustomRender.addNebula(
                location,
                vel,
                (30f..50f).random() * sizeScale,
                endSizeScale,
                (1f..1.3f).random() * durationScale,
                0.1f * rampUpScale,
                0.5f,
                nebulaColor,
                CombatEngineLayers.UNDER_SHIPS_LAYER,
                CustomRender.NebulaType.SWIRLY,
                false
            )

            CustomRender.addNebula(
                location,
                vel,
                (30f..50f).random() * sizeScale,
                endSizeScale,
                (1f..1.3f).random() * durationScale,
                0.1f * rampUpScale,
                0.5f,
                nebulaColor.modify(alpha = (nebulaColor.alpha * .5f).roundToInt()),
                CombatEngineLayers.ABOVE_SHIPS_AND_MISSILES_LAYER,
                CustomRender.NebulaType.SWIRLY,
                false
            )

            CustomRender.addNebula(
                location,
                vel,
                (30f..50f).random() * sizeScale,
                endSizeScale,
                (1f..1.3f).random() * durationScale,
                0.1f * rampUpScale,
                0.5f,
                swirlyNebulaColor,
                CombatEngineLayers.ABOVE_SHIPS_AND_MISSILES_LAYER,
                CustomRender.NebulaType.SWIRLY,
                false
            )
        }
    }
}