package wisp.perseanchronicles.telos

import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f
import java.awt.Color
import kotlin.math.min
import kotlin.math.pow

/**
 * Originally by Nia.
 */
class TelosGlowPlugin : EveryFrameWeaponEffectPlugin {
    companion object {
        private const val MAX_JITTER_DISTANCE = 0.5f
        private const val ALPHA_MULT = 3f
    }

    private var runOnce = true

    override fun advance(amount: Float, engine: CombatEngineAPI, weapon: WeaponAPI) {
        val ship = weapon.ship
        val fluxColor = Color.decode("#39a3ff")

        if (ship == null || !ship.isAlive) {
            if (runOnce) {
                weapon.sprite.color = Color.black
                runOnce = false
            }
            return
        }

        val scalar = ship.velocity.length() / ship.maxSpeed
        var baseColor = Color.CYAN

        if (ship.shield != null) {
            baseColor = ship.shield.innerColor
        }

        val flux = ship.hardFluxLevel.pow(2)

        // set up glow color according to flux level
        val newColor = Misc.interpolateColor(baseColor, fluxColor, flux)
        val red = newColor.red.toFloat() / 255f
        val green = newColor.green.toFloat() / 255f
        val blue = newColor.blue.toFloat() / 255f
        val alpha = min(1f, scalar * ALPHA_MULT)

        // switch to actual sprite if > 0 alpha (to avoid showing in refit screen)
        if (alpha > 0f) {
            weapon.animation.frame = 1
        } else {
            weapon.animation.frame = 0
        }

        // actually set color
        val colorToUse = Color(red, green, blue, alpha)
        weapon.sprite.color = colorToUse

        // jitter
        if (scalar > 0.666) {
            val randomOffset = MathUtils.getRandomPointInCircle(
                Vector2f(weapon.sprite.width / 2f, weapon.sprite.height / 2f),
                MAX_JITTER_DISTANCE
            )
            weapon.sprite.setCenter(randomOffset.x, randomOffset.y)
        }
    }
}