package wisp.perseanchronicles.telos.boats

import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.util.Misc
import java.awt.Color

enum class Scalar {
    SPEED,
    FLUX
}

class TelosSpeedGlowPlugin : EveryFrameWeaponEffectPlugin {
    private val plugin = TelosGlowPlugin(Scalar.SPEED)
    override fun advance(amount: Float, engine: CombatEngineAPI, weapon: WeaponAPI) =
        plugin.advance(amount, engine, weapon)
}

class TelosFluxGlowPlugin : EveryFrameWeaponEffectPlugin {
    private val plugin = TelosGlowPlugin(Scalar.FLUX)
    override fun advance(amount: Float, engine: CombatEngineAPI, weapon: WeaponAPI) =
        plugin.advance(amount, engine, weapon)
}

/**
 * Originally by Nia.
 */
class TelosGlowPlugin(
    val scalar: Scalar
) {
    companion object {
        private const val ALPHA_MULT = 1f
    }

    private var runOnce = true
    var scalarAlpha = 0f
    var engineAlpha = 0f

    fun advance(amount: Float, engine: CombatEngineAPI, weapon: WeaponAPI) {
        val ship = weapon.ship

        if (ship == null || !ship.isAlive) {
            if (runOnce) {
                weapon.sprite.color = Color.black
                runOnce = false
            }
            return
        }

        val palette =
            ship.allWeapons
                .orEmpty()
                .mapNotNull { it.effectPlugin }
                .filterIsInstance<TelosEngineEffects>()
                .firstOrNull()
                ?.currentPalette ?: defaultShipPalette
        val baseColor = palette.glowBase

        // Set shield color too, why not.
        if (palette != ShipPalette.PLAYER) {
            ship.shield.innerColor = palette.baseSwirlyNebula
            ship.shield.ringColor = palette.baseNebula
        }

        // These must add up to 255
        val scalarMaxValue = 180f
        val engineAccelMaxValue = 255f - scalarMaxValue

        // How slow/fast the glow transition should be, higher is faster
        val smoothing = 500f

        val ec = ship.engineController

        val fluxColor = when (scalar) {
            Scalar.SPEED -> palette.speedGlow
            Scalar.FLUX -> palette.fluxGlow
        }

        val scalar = when (scalar) {
            Scalar.SPEED -> ship.velocity.length() / ship.maxSpeed * scalarMaxValue
            Scalar.FLUX -> ship.fluxLevel * scalarMaxValue
        }

        // set up glow color according to flux level
        val newColor = Misc.interpolateColor(baseColor, fluxColor, ship.fluxLevel)
        val red = newColor.red.toFloat() / 255f
        val green = newColor.green.toFloat() / 255f
        val blue = newColor.blue.toFloat() / 255f

        scalarAlpha = if (scalarAlpha > scalar) {
            (scalarAlpha - (smoothing * amount)).coerceAtLeast(scalar)
        } else {
            (scalarAlpha + (smoothing * amount)).coerceAtMost(scalar)
        }

        engineAlpha = if (ec.isAccelerating || ec.isAcceleratingBackwards || ec.isAcceleratingBackwards || ec.isStrafingLeft || ec.isStrafingRight) {
            (engineAlpha + (smoothing * amount)).coerceAtMost(engineAccelMaxValue)
        } else {
            (engineAlpha - (smoothing * amount)).coerceAtLeast(0f)
        }

        val alpha = (scalarAlpha + engineAlpha).let { alpha ->
            (alpha * ALPHA_MULT).coerceIn(0f, 255f) / 255f
        }

        // switch to actual sprite if > 0 alpha (to avoid showing in refit screen)
        if (alpha > 0f) {
            weapon.animation.frame = 1
        } else {
            weapon.animation.frame = 0
        }

        // actually set color
        val colorToUse = Color(red, green, blue, alpha)
        weapon.sprite.color = colorToUse
    }
}