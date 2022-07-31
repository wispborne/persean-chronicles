package wisp.perseanchronicles.telos.pt3_arrow.nocturne

import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignEngineLayers
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.impl.campaign.abilities.GraviticScanAbility
import com.fs.starfarer.api.impl.campaign.abilities.GraviticScanData
import com.fs.starfarer.api.impl.campaign.ids.Abilities
import com.fs.starfarer.api.util.Misc
import org.lwjgl.opengl.GL11
import org.lwjgl.util.vector.Vector2f
import wisp.perseanchronicles.game
import java.awt.Color
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

class TelevisionScript : EveryFrameScript {
    private var isDone = false
    private var secsElapsed = 0f

    override fun isDone() = isDone

    override fun runWhilePaused() = true

    protected var phaseAngle = 0f

    override fun advance(amount: Float) {
        if (!game.sector.isPaused) {
            secsElapsed += amount
        }

        val isEffectOver = secsElapsed > 10

        val days = Global.getSector().clock.convertToDays(amount)
        phaseAngle += days * 360f * 10f
        phaseAngle = Misc.normalizeAngle(phaseAngle)

        game.sector.currentLocation.allEntities.forEach { render(it, game.sector.viewport) }

        if (isEffectOver) {
            game.logger.i { "Ending TeleVision effect." }
            isDone = true
        }
    }


    fun getRingRadius(obj: SectorEntityToken): Float {
        return obj.radius + 75f
        //return obj.getRadius() + 25f;
    }

    @Transient
    protected var texture: SpriteAPI? = null

    fun render(obj: SectorEntityToken, viewport: ViewportAPI) {
        val graviticScanAbility = game.sector.playerFleet.getAbility(Abilities.GRAVITIC_SCAN) as? GraviticScanAbility
        graviticScanAbility?.apply {
//            this.activate()
//            this.advance(.5f)
//            render(CampaignEngineLayers.ABOVE, viewport)
        }
                ?.run {
                    game.sector.playerFleet.addAbility(Abilities.GRAVITIC_SCAN)
                }

//        if (data == null) return
        val level: Float = 1f
//        if (level <= 0) return

//        val alphaMult = viewport.alphaMult * level
        val alphaMult = level

//		float x = obj.getLocation().x;
//		float y = obj.getLocation().y;
//
//		GL11.glPushMatrix();
//		GL11.glTranslatef(x, y, 0);
//
//		GL11.glDisable(GL11.GL_TEXTURE_2D);
//		Misc.renderQuad(30, 30, 100, 100, Color.green, alphaMult * level);
//
//
//		GL11.glPopMatrix();


        //float noiseLevel = data.getNoiseLevel();
        val bandWidthInTexture = 256f
        var bandIndex: Float
        val radStart = getRingRadius(obj)
        val radEnd = radStart + 75f
        val circ = (Math.PI * 2f * (radStart + radEnd) / 2f).toFloat()
        //float pixelsPerSegment = 10f;
        val pixelsPerSegment = circ / 360f
        //float pixelsPerSegment = circ / 720;
        val segments = (circ / pixelsPerSegment).roundToInt().toFloat()

        val startRad = Math.toRadians(0.0).toFloat()
        val endRad = Math.toRadians(360.0).toFloat()
        val spanRad = abs(endRad - startRad)
        val anglePerSegment = spanRad / segments
        val loc: Vector2f = game.sector.playerFleet.location//obj.location
        val x = loc.x
        val y = loc.y
        GL11.glPushMatrix()
        GL11.glTranslatef(x, y, 0f)

        GL11.glEnable(GL11.GL_TEXTURE_2D)
        if (texture == null) texture = Global.getSettings().getSprite("abilities", "neutrino_detector")
        texture!!.bindTexture()
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        var outlineMode = false
        outlineMode = false // todo
        if (outlineMode) {
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glDisable(GL11.GL_BLEND)
            GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE)
            //GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
        }
        val thickness = (radEnd - radStart) * 1f
        var texProgress = 0f
        val texHeight = texture!!.textureHeight
        val imageHeight = texture!!.height
        var texPerSegment = pixelsPerSegment * texHeight / imageHeight * bandWidthInTexture / thickness
        texPerSegment *= 1f
        val totalTex = (texPerSegment * segments).roundToInt().toFloat().coerceAtLeast(1f)
        texPerSegment = totalTex / segments
        val texWidth = texture!!.textureWidth
        val imageWidth = texture!!.width
        val color = Color(25, 215, 255, 255)
        //Color color = new Color(255,25,255,155);
        for (iter in 0..1) {
            if (iter == 0) {
                bandIndex = 1f
            } else {
                //color = new Color(255,215,25,255);
                //color = new Color(25,255,215,255);
                bandIndex = 0f
                texProgress = segments / 2f * texPerSegment
                //GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
            }
            if (iter == 1) {
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE)
            }
            //bandIndex = 1;
            val leftTX = bandIndex * texWidth * bandWidthInTexture / imageWidth
            val rightTX = (bandIndex + 1f) * texWidth * bandWidthInTexture / imageWidth - 0.001f
            GL11.glBegin(GL11.GL_QUAD_STRIP)
            var i = 0f
            while (i < segments + 1) {
                val segIndex = i % segments.toInt()

                //float phaseAngleRad = (float) Math.toRadians(phaseAngle + segIndex * 10) + (segIndex * anglePerSegment * 10f);
                val phaseAngleRad: Float = if (iter == 0) {
                    Math.toRadians(phaseAngle.toDouble()).toFloat() + segIndex * anglePerSegment * 29f
                } else { //if (iter == 1) {
                    Math.toRadians(-phaseAngle.toDouble()).toFloat() + segIndex * anglePerSegment * 17f
                }
                val angle = Math.toDegrees((segIndex * anglePerSegment).toDouble()).toFloat()
                //if (iter == 1) angle += 180;
                val pulseSin = Math.sin(phaseAngleRad.toDouble()).toFloat()
                var pulseMax = thickness * 0.5f
                pulseMax = thickness * 0.2f
                pulseMax = 10f

                //pulseMax *= 0.25f + 0.75f * noiseLevel;
                val pulseAmount = pulseSin * pulseMax
                //float pulseInner = pulseAmount * 0.1f;
                val pulseInner = pulseAmount * 0.1f

//				float thicknessMult = delegate.getAuroraThicknessMult(angle);
//				float thicknessFlat = delegate.getAuroraThicknessFlat(angle);
                val theta = anglePerSegment * segIndex
                val cos = cos(theta.toDouble()).toFloat()
                val sin = sin(theta.toDouble()).toFloat()
                val rInner = radStart - pulseInner
                //if (rInner < r * 0.9f) rInner = r * 0.9f;

                //float rOuter = (r + thickness * thicknessMult - pulseAmount + thicknessFlat);
                var rOuter = radStart + thickness - pulseAmount


                //rOuter += noiseLevel * 25f;
                var grav: Float = 500f //GraviticScanData(graviticScanAbility).apply { advance(.5f) }.getDataAt(angle)  //data.getDataAt(angle) // todo
                //if (grav > 500) System.out.println(grav);
                //if (grav > 300) grav = 300;
                if (grav > 750) grav = 750f
                grav *= 250f / 750f
                grav *= level
                //grav *= 0.5f;
                //rInner -= grav * 0.25f;

                //rInner -= grav * 0.1f;
                rOuter += grav
                //				rInner -= grav * 3f;
//				rOuter -= grav * 3f;
                //System.out.println(grav);
                var alpha = alphaMult
                alpha *= 0.25f + (grav / 100).coerceAtMost(0.75f)
                //alpha *= 0.75f;

//
//
//
//				phaseAngleWarp = (float) Math.toRadians(phaseAngle - 180 * iter) + (segIndex * anglePerSegment * 1f);
//				float warpSin = (float) Math.sin(phaseAngleWarp);
//				rInner += thickness * 0.5f * warpSin;
//				rOuter += thickness * 0.5f * warpSin;
                val x1 = cos * rInner
                val y1 = sin * rInner
                var x2 = cos * rOuter
                var y2 = sin * rOuter
                x2 += (cos(phaseAngleRad.toDouble()) * pixelsPerSegment * 0.33f).toFloat()
                y2 += (sin(phaseAngleRad.toDouble()) * pixelsPerSegment * 0.33f).toFloat()
                GL11.glColor4ub(
                    color.red.toByte(),
                    color.green.toByte(),
                    color.blue.toByte(),
                    (color.alpha.toFloat() * alphaMult * alpha).toInt().toByte()
                )
                GL11.glTexCoord2f(leftTX, texProgress)
                GL11.glVertex2f(x1, y1)
                GL11.glTexCoord2f(rightTX, texProgress)
                GL11.glVertex2f(x2, y2)
                texProgress += texPerSegment * 1f
                i++
            }
            GL11.glEnd()

            //GL11.glRotatef(180, 0, 0, 1);
        }
        GL11.glPopMatrix()
        if (outlineMode) {
            GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL)
        }
    }
}