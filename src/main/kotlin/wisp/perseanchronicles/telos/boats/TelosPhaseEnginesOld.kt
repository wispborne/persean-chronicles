package wisp.perseanchronicles.telos.boats

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.CombatEngineLayers
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.util.IntervalUtil
import data.scripts.util.MagicRender
import org.lazywizard.lazylib.MathUtils
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
class TelosPhaseEnginesOld : EveryFrameWeaponEffectPlugin {
    companion object {
        val interval = IntervalUtil(0.06f, 0.07f)
    }

    private var alphaMult = 0f

    override fun advance(amount: Float, engine: CombatEngineAPI, weapon: WeaponAPI) {
        interval.advance(amount)

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

        val vel = Vector2f(100f, 0f)
        VectorUtils.rotate(vel, ship.facing + 180f)
        val combatEngine = Global.getCombatEngine() ?: return
        val sizeScale = 5f
        val durationScale = 1.5f
        val rampUpScale = 4.5f

        val negativeColor = Color(24, 254, 109).modify(green = 255, alpha = (1 * alphaMult).roundToInt())
        val nebulaColor = Color.decode("#5F78CC").modify(alpha = (70 * alphaMult).roundToInt())
        val swirlyNebulaColor = Color.decode("#3DAECC").modify(alpha = (50 * alphaMult).roundToInt())

        val negativeNebulaSprite = game.settings.getSprite("misc", "nebula_particles")
        val nebulaSprite = game.settings.getSprite("misc", "nebula_particles")
        val swirlyNebulaSprite = game.settings.getSprite("misc", "fx_particles2")

        for (emitterPoints in listOf(ship)) {
//            MagicRender.battlespace(
//                /* sprite = */ negativeNebulaSprite,
//                /* loc = */ emitterPoints.location,
//                /* vel = */ vel,
//                /* size = */ ((40f..60f).random() * sizeScale).let { Vector2f(it, it) },
//                /* growth = */ 1.5f.let { Vector2f(it, it) },
//                /* angle = */ (0f..360f).random(),
//                /* spin = */ 0f,
//                /* color = */ negativeColor,
//                /* additive = */ true,
//                /* jitterRange = */ 0f,
//                /* jitterTilt = */ 0f,
//                /* flickerRange = */ 0f,
//                /* flickerMedian = */ 0f,
//                /* maxDelay = */ 0f,
//                /* fadein = */ 0.1f,
//                /* full = */ (1.2f..1.5f).random() * durationScale,
//                /* fadeout = */ 0.1f,
//                /* layer = */ CombatEngineLayers.UNDER_SHIPS_LAYER,
//            )
// original
            combatEngine.addNegativeNebulaParticle(
                /* loc = */ emitterPoints.location,
                /* vel = */ vel,
                /* size = */ (40f..60f).random() * sizeScale,
                /* endSizeMult = */ 1.5f,
                /* rampUpFraction = */ 0.1f * rampUpScale,
                /* fullBrightnessFraction = */ 0.5f,
                /* totalDuration = */ (1.2f..1.5f).random() * durationScale,
                /* color = */ negativeColor
            )

//            MagicRender.battlespace(
//                /* sprite = */ nebulaSprite,
//                /* loc = */ emitterPoints.location,
//                /* vel = */ vel,
//                /* size = */ ((30f..50f).random() * sizeScale).let { Vector2f(it, it) },
//                /* growth = */ 1.5f.let { Vector2f(it, it) },
//                /* angle = */ (0f..360f).random(),
//                /* spin = */ 0f,
//                /* color = */ nebulaColor,
//                /* additive = */ true,
//                /* jitterRange = */ 0f,
//                /* jitterTilt = */ 0f,
//                /* flickerRange = */ 0f,
//                /* flickerMedian = */ 0f,
//                /* maxDelay = */ 0f,
//                /* fadein = */ 0.1f,
//                /* full = */ (1f..1.3f).random() * durationScale,
//                /* fadeout = */ 0.1f,
//                /* layer = */ CombatEngineLayers.UNDER_SHIPS_LAYER,
//            )

// original
            combatEngine.addNebulaParticle(
                /* loc = */ emitterPoints.location,
                /* vel = */ vel,
                /* size = */ (30f..50f).random() * sizeScale,
                /* endSizeMult = */ 1.5f,
                /* rampUpFraction = */ 0.1f * rampUpScale,
                /* fullBrightnessFraction = */ 0.5f,
                /* totalDuration = */ (1f..1.3f).random() * durationScale,
                /* color = */ nebulaColor
            )

//            MagicRender.battlespace(
//                /* sprite = */ swirlyNebulaSprite,
//                /* loc = */ emitterPoints.location,
//                /* vel = */ vel,
//                /* size = */ ((20f..40f).random() * sizeScale).let { Vector2f(it, it) },
//                /* growth = */ 1.5f.let { Vector2f(it, it) },
//                /* angle = */ (0f..360f).random(),
//                /* spin = */ 0f,
//                /* color = */ swirlyNebulaColor,
//                /* additive = */ true,
//                /* jitterRange = */ 0f,
//                /* jitterTilt = */ 0f,
//                /* flickerRange = */ 0f,
//                /* flickerMedian = */ 0f,
//                /* maxDelay = */ 0f,
//                /* fadein = */ 0.1f,
//                /* full = */ (1f..1.3f).random() * durationScale,
//                /* fadeout = */ 0.1f,
//                /* layer = */ CombatEngineLayers.UNDER_SHIPS_LAYER,
//            )

            // original
            combatEngine.addSwirlyNebulaParticle(
                /* loc = */ MathUtils.getRandomPointInCircle(emitterPoints.location, 2f),
                /* vel = */ vel,
                /* size = */ (20f..40f).random() * sizeScale,
                /* endSizeMult = */ 1.3f,
                /* rampUpFraction = */ 0.1f * rampUpScale,
                /* fullBrightnessFraction = */ 0.5f,
                /* totalDuration = */ (0.8f..1.1f).random() * durationScale,
                /* color = */ swirlyNebulaColor,
                /* expandAsSqrt = */ false
            )
        }
    }
}