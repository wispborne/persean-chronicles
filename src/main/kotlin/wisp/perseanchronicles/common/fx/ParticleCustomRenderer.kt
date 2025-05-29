package wisp.perseanchronicles.common.fx

import com.fs.starfarer.api.campaign.CampaignEngineLayers
import org.lwjgl.util.vector.Vector2f
import particleengine.Particles
import java.awt.Color
import kotlin.math.atan2
import kotlin.random.Random

/**
 * Renders nebula effects using the ParticleEngine library.
 */
object ParticleCustomRenderer {
    /**
     * Spawns a nebula particle with the ParticleEngine.
     *
     * @param location World position of the particle.
     * @param velocity Initial velocity vector.
     * @param size Starting size of the particle.
     * @param endSizeMult Multiplier for final particle size.
     * @param duration Lifetime of the particle in seconds.
     * @param inFraction Fraction of duration used for fade-in.
     * @param outFraction Fraction of duration used for fade-out.
     * @param color Initial RGBA color of the particle.
     * @param outColor RGBA color at the end of the particle’s life.
     * @param pixelRadiusToRenderOffscreen Set this to prevent particles from being drawn offscreen.
     * @param layer Combat layer to render on.
     */
    fun addNebula(
        location: Vector2f,
        velocity: Vector2f,
        size: Float,
        endSizeMult: Float,
        duration: Float,
        inFraction: Float,
        outFraction: Float,
        color: Color,
        outColor: Color = color,
        pixelRadiusToRenderOffscreen: Float = 5000f,
        layer: CampaignEngineLayers = CampaignEngineLayers.ABOVE
    ) {
        // pick one of the 16 pre-split PNGs
        val index = Random.nextInt(16)
        val spritePath = "graphics/telos/ethersight/perseanchronicles_ethersight_nebula_$index.png"

        // create and configure emitter
        val emitter = Particles.initialize(Vector2f(location), spritePath)
        emitter.campaignLayer = layer

        // match original velocity vector
        val angleDeg = Math.toDegrees(atan2(velocity.y.toDouble(), velocity.x.toDouble())).toFloat()
        emitter.facing(angleDeg, angleDeg)
        emitter.velocity(Vector2f(velocity), Vector2f(velocity))

        // lifetime + fade
        emitter.life(duration, duration)
        emitter.fadeTime(
            duration * inFraction, duration * inFraction,
            duration * outFraction, duration * outFraction
        )

        emitter.setInactiveBorder(pixelRadiusToRenderOffscreen)

        // size interpolation
        emitter.size(size, size)
        emitter.growthRate((size * endSizeMult - size) / duration, (size * endSizeMult - size) / duration)


        // color HSVA conversion
        val startHsv = FloatArray(3)
        Color.RGBtoHSB(color.red, color.green, color.blue, startHsv)
        val startHsva = floatArrayOf(startHsv[0], startHsv[1], startHsv[2], color.alpha / 255f)
        emitter.color(color)

        val outHsv = FloatArray(3)
        Color.RGBtoHSB(outColor.red, outColor.green, outColor.blue, outHsv)
        val outHsva = floatArrayOf(outHsv[0], outHsv[1], outHsv[2], outColor.alpha / 255f)

        val dh = (outHsva[0] - startHsva[0]) / duration
        val ds = (outHsva[1] - startHsva[1]) / duration
        val dv = (outHsva[2] - startHsva[2]) / duration
        val da = (outHsva[3] - startHsva[3]) / duration
        emitter.colorShiftHSVA(dh, dh, ds, ds, dv, dv, da, da)

        // spawn it instantly
        Particles.burst(emitter, 1)
    }
}
