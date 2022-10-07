package wisp.perseanchronicles.telos.boats

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.CombatEngineLayers
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.util.IntervalUtil
import org.dark.shaders.distortion.DistortionShader
import org.dark.shaders.distortion.RippleDistortion
import org.lazywizard.lazylib.ext.plus
import org.lazywizard.lazylib.ext.rotate
import org.lwjgl.util.vector.Vector2f
import wisp.perseanchronicles.game
import wisp.questgiver.wispLib.modify
import wisp.questgiver.wispLib.random
import java.awt.Color
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Originally `tahlan_PhaseEngines`, thank you Nia.
 */
class TelosPhaseEngines : EveryFrameWeaponEffectPlugin {
    companion object {
        val interval = IntervalUtil(0.07f, 0.08f)
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
        val sizeScale = 1.7f
        val durationScale = 1.1f
        val rampUpScale = 1.0f
        val alphaScale = .25f
        val endSizeScale = 1.55f
        val densityInverted = 0.07f // Lower is more dense
        val distortionIntensity = 7f
        val trailMomentumScale = .45f

        if (interval.minInterval != densityInverted) {
            interval.setInterval(densityInverted, densityInverted * 0.2f)
        }


        val vel = Vector2f(100f * velocityScale, 100f * velocityScale)
            .rotate(Random.nextFloat() * 360f)
            .let { dest ->
                val shipVel = ship.velocity.let { Vector2f(it.x * trailMomentumScale, it.y * trailMomentumScale) }
                dest.plus(shipVel)
            }

//        VectorUtils.rotate(vel, ship.facing + 180f)

        val negativeColor =
            Color(24, 254, 109).modify(green = 255, alpha = (1 * alphaMult * alphaScale).roundToInt().coerceIn(0..255))
        val nebulaColor =
            Color.decode("#374676").modify(alpha = (70 * alphaMult * alphaScale).roundToInt().coerceIn(0..255))
        val swirlyNebulaColor =
            Color.decode("#3DAECC").modify(alpha = (25 * alphaMult * alphaScale).roundToInt().coerceIn(0..255))

        val negativeNebulaSprite = game.settings.getSprite("misc", "nebula_particles")
        val nebulaSprite = game.settings.getSprite("misc", "nebula_particles")
        val swirlyNebulaSprite = game.settings.getSprite("misc", "fx_particles2")

        val emitters = ship.hullSpec.allWeaponSlotsCopy
            .map { Vector2f(it.location).translate(ship.location.x, ship.location.y) }
            .plus(Vector2f(ship.location))

        for (location in emitters) {
            // Negative swirl under
            CustomRender.addNebula(
                location = location,
                velocity = vel,
                size = (40f..60f).random() * sizeScale,
                endSizeMult = endSizeScale,
                duration = (1.2f..1.5f).random() * durationScale,
                inFraction = 0.1f * rampUpScale,
                outFraction = 0.5f,
                color = negativeColor,
                layer = CombatEngineLayers.UNDER_SHIPS_LAYER,
                type = CustomRender.NebulaType.SWIRLY,
                negative = true
            )

            // Swirl under
            CustomRender.addNebula(
                location = location,
                velocity = vel,
                size = (30f..50f).random() * sizeScale,
                endSizeMult = endSizeScale,
                duration = (1f..1.3f).random() * durationScale,
                inFraction = 0.1f * rampUpScale,
                outFraction = 0.5f,
                color = nebulaColor,
                layer = CombatEngineLayers.UNDER_SHIPS_LAYER,
                type = CustomRender.NebulaType.SWIRLY,
                negative = false
            )

            // Normal on top
            CustomRender.addNebula(
                location = location,
                velocity = vel,
                size = (30f..50f).random() * sizeScale,
                endSizeMult = endSizeScale,
                duration = (1f..1.3f).random() * durationScale,
                inFraction = 0.1f * rampUpScale,
                outFraction = 0.5f,
                color = nebulaColor.modify(alpha = (nebulaColor.alpha * .5f).roundToInt()),
                layer = CombatEngineLayers.ABOVE_SHIPS_AND_MISSILES_LAYER,
                type = CustomRender.NebulaType.NORMAL,
                negative = false
            )

            // Swirl on top
            CustomRender.addNebula(
                location = location,
                velocity = vel,
                size = (30f..50f).random() * sizeScale,
                endSizeMult = endSizeScale,
                duration = (1f..1.3f).random() * durationScale,
                inFraction = 0.1f * rampUpScale,
                outFraction = 0.5f,
                color = swirlyNebulaColor,
                layer = CombatEngineLayers.ABOVE_SHIPS_AND_MISSILES_LAYER,
                type = CustomRender.NebulaType.SWIRLY,
                negative = false
            )
        }

        if (!ship.customData.containsKey("ripples")) {
            ship.setCustomData("ripples", mutableListOf<RippleDistortion>())
        }

        createGfxLibRippleDistortion(
            location = ship.location,
            velocity = ship.velocity,
            size = ship.spriteAPI.width - 60f,
            intensity = distortionIntensity,
            flip = false,
            angle = ship.facing + 180f,
            arc = 140f,
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