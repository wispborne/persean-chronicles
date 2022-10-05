package wisp.perseanchronicles.telos.boats

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.CombatEngineLayers
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.util.IntervalUtil
import org.dark.shaders.distortion.DistortionShader
import org.dark.shaders.distortion.RippleDistortion
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
        val interval = IntervalUtil(0.1f, 0.11f)
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

        // Fix ripples on the ship
        val activeRipples = ship.customData["ripples"] as? MutableList<RippleDistortion>

        activeRipples?.forEach {
            if (it.remainingLifetime <= 0f) {
                activeRipples.remove(it)
            } else {
                it.location = ship.location
            }
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

        val negativeColor =
            Color(24, 254, 109).modify(green = 255, alpha = (1 * alphaMult * alphaScale).roundToInt().coerceIn(0..255))
        val nebulaColor =
            Color.decode("#5F78CC").modify(alpha = (70 * alphaMult * alphaScale).roundToInt().coerceIn(0..255))
        val swirlyNebulaColor =
            Color.decode("#3DAECC").modify(alpha = (25 * alphaMult * alphaScale).roundToInt().coerceIn(0..255))

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

        if (!ship.customData.containsKey("ripples")) {
            ship.setCustomData("ripples", mutableListOf<RippleDistortion>())
        }

        createGfxLibRippleDistortion(
            location = ship.location,
            velocity = ship.velocity,
            size = ship.spriteAPI.width - 60f,
            intensity = 3f,
            flip = false,
            angle = 0f,
            arc = 360f,
            edgeSmooth = 0f,
            fadeIn = 1f,
            last = 3f,
            fadeOut = 2f,
            growthTime = 0.1f,
            shrinkTime = 1f
        )?.let { (ship.customData["ripples"] as MutableList<RippleDistortion>).add(it) }
    }

    // From Seeker, with modifications. Originally `data.scripts.util.CustomRippleDistortion`.
    fun createGfxLibRippleDistortion(
        location: Vector2f?,
        velocity: Vector2f?,
        size: Float,
        intensity: Float,
        flip: Boolean,
        angle: Float,
        arc: Float,
        edgeSmooth: Float = 0f,
        fadeIn: Float = 0f,
        last: Float,
        fadeOut: Float = 0f,
        growthTime: Float = 0f,
        shrinkTime: Float = 0f,
    ): RippleDistortion? {
        if (!game.settings.modManager.isModEnabled("shaderLib")) return null

        val ripple = RippleDistortion(location, velocity)
        ripple.intensity = intensity
        ripple.size = size
        ripple.setArc(angle - arc / 2, angle + arc / 2)

        if (edgeSmooth != 0f) {
            ripple.arcAttenuationWidth = edgeSmooth
        }

        if (fadeIn != 0f) {
            ripple.fadeInIntensity(fadeIn)
        }

        if (fadeOut != 0f) {
            ripple.autoFadeIntensityTime = fadeOut
        }

        if (growthTime != 0f) {
            ripple.fadeInSize(growthTime)
        }

        if (shrinkTime != 0f) {
            ripple.autoFadeSizeTime = shrinkTime
        }

        ripple.flip(flip)
        ripple.setLifetime(last)
        ripple.frameRate = 60f
        DistortionShader.addDistortion(ripple)
        return ripple
    }
}