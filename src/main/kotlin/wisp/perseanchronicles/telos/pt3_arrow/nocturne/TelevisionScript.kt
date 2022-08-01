package wisp.perseanchronicles.telos.pt3_arrow.nocturne

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.*
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.impl.campaign.abilities.BaseToggleAbility
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.campaign.CampaignEngine
import com.fs.starfarer.campaign.CustomCampaignEntity
import org.lwjgl.opengl.GL11
import org.lwjgl.util.vector.Vector2f
import wisp.perseanchronicles.game
import wisp.questgiver.wispLib.equalsAny
import java.awt.Color
import java.util.*
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

class TelevisionScript : BaseToggleAbility() {
    //    private var isDone = false
    private var secsElapsed = 0f
    private val originalMaxZoom = game.settings.getFloat("maxCampaignZoom")

    override fun runWhilePaused() = true

    protected var phaseAngle = 0f

    override fun getActiveLayers(): EnumSet<CampaignEngineLayers?>? {
        return EnumSet.of(CampaignEngineLayers.ABOVE)
    }

    override fun advance(amount: Float) {
        super.advance(amount)

        if (!game.sector.isPaused) {
            secsElapsed += amount
        }

        val isEffectOver = secsElapsed > 10

        val days = Global.getSector().clock.convertToDays(if (game.sector.isPaused) 0f else amount)
        phaseAngle += days * 360f * 10f
        phaseAngle = Misc.normalizeAngle(phaseAngle)

//
//        if (isEffectOver) {
//            game.logger.i { "Ending TeleVision effect." }
//            isDone = true
//        }
    }

    override fun activateImpl() {
        CampaignEngine.getInstance().uiData.campaignMapZoom = 5f
    }

    /**
     * Will be called once when level is 0 and consistently when level >0.
     * @param level
     */
    override fun applyEffect(amount: Float, level: Float) {
        setMaxZoom(10f)
    }

    override fun deactivateImpl() {
        setMaxZoom(originalMaxZoom)
    }

    override fun cleanupImpl() {

    }

    override fun hasTooltip() = true

    override fun createTooltip(tooltip: TooltipMakerAPI?, expanded: Boolean) {
        val bad = Misc.getNegativeHighlightColor()
        val gray = Misc.getGrayColor()
        val highlight = Misc.getHighlightColor()

        var status = " (off)"
        if (turnedOn) {
            status = " (on)"
        }

        val title = tooltip!!.addTitle(spec.name + status)
        title.highlightLast(status)
        title.setHighlightColor(gray)

        val pad = 10f
        
        tooltip.addPara(
            "By focusing, you are able to observe stellar objects and fleets with perfect detail.", pad
        )
        tooltip.addPara(
            "Fleets with a harmful intention show red.", pad
        )
    }

    fun setMaxZoom(zoomMult: Float) {
        // Update the in-memory setting and then call obf method to reload settings.
//        game.settings.setFloat("maxCampaignZoom", zoomMult)
//        StarfarerSettings.ÕÔ0000()
    }

    fun getRingRadius(obj: SectorEntityToken): Float {
        return obj.radius - 25f
        //return obj.getRadius() + 25f;
    }

    override fun render(layer: CampaignEngineLayers, viewport: ViewportAPI) {
        CampaignEngine.getInstance().uiData.campaignZoom = 30f
        CampaignEngine.getInstance().uiData.campaignMapZoom = 30f
//        val mult = 1.1f
//        viewport.set(viewport.llx * mult, viewport.lly * mult, viewport.visibleWidth * mult, viewport.visibleHeight * mult)

        game.sector.currentLocation.allEntities
            .filterNot { obj ->
                obj is RingBandAPI ||
                        obj.tags.any { it.equalsAny(Tags.TERRAIN, Tags.ORBITAL_JUNK) } ||
                        (obj is CustomCampaignEntity && obj.sprite == null)
            }
            .forEach { render(it, game.sector.viewport) }
    }

    fun getEntityColor(obj: SectorEntityToken): Color = when (obj) {
        is PlanetAPI -> obj.spec.iconColor
        is CampaignFleetAPI -> when {
            obj.isHostileTo(game.sector.playerFleet) -> Color.RED
            else -> obj.faction.color
        }
        is JumpPointAPI -> Color(128, 100, 255)
        else -> obj.indicatorColor
    }

    private fun getSpikiness(obj: SectorEntityToken) = when (obj) {
        is CampaignFleetAPI -> 15f
        is JumpPointAPI -> 4f
        else -> 1f
    }

    @Transient
    protected var texture: SpriteAPI? = null

    fun render(obj: SectorEntityToken, viewport: ViewportAPI) {
        if (!this.isActive) return

        val level: Float = .524f//1f
        val alphaMult = level

        val bandWidthInTexture = 256f // vanilla 256
        var bandIndex: Float
        val radStart = getRingRadius(obj)
        val radEnd = radStart + 75f
        val circ = (Math.PI * 2f * (radStart + radEnd) / 2f).toFloat()
        val pixelsPerSegment = circ / 360f
        val segments = (circ / pixelsPerSegment).roundToInt().toFloat()

        val startRad = Math.toRadians(0.0).toFloat()
        val endRad = Math.toRadians(360.0).toFloat()
        val spanRad = abs(endRad - startRad)
        val anglePerSegment = spanRad / segments
        val loc: Vector2f = obj.location
        val x = loc.x
        val y = loc.y
        GL11.glPushMatrix()
        GL11.glTranslatef(x, y, 0f)

        GL11.glEnable(GL11.GL_TEXTURE_2D)
        if (texture == null) texture = Global.getSettings().getSprite("abilities", "neutrino_detector")
        texture!!.bindTexture()
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

        val thickness = (radEnd - radStart) //* (1 + 1 / radStart)
        var texProgress = 0f
        val texHeight = texture!!.textureHeight
        val imageHeight = texture!!.height
        var texPerSegment = pixelsPerSegment * texHeight / imageHeight * bandWidthInTexture / thickness
        texPerSegment *= 1f
        val totalTex = (texPerSegment * segments).roundToInt().toFloat().coerceAtLeast(1f)
        texPerSegment = totalTex / segments
        val texWidth = texture!!.textureWidth //* 3
        val imageWidth = texture!!.width
        val color = getEntityColor(obj)

        repeat(times = 2) { iter ->
            if (iter == 0) {
                bandIndex = 1f
            } else {
                bandIndex = 0f
                texProgress = segments / 2f * texPerSegment
            }

            if (iter == 1) {
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE)
            }

            val leftTX = bandIndex * texWidth * bandWidthInTexture / imageWidth
            val rightTX = (bandIndex + 1f) * texWidth * bandWidthInTexture / imageWidth - 0.001f
            GL11.glBegin(GL11.GL_QUAD_STRIP)

            repeat(times = segments.toInt() + 1) { i ->
                val segIndex = i % segments.toInt()

                val phaseAngleRad: Float = if (iter == 0) {
                    Math.toRadians(phaseAngle.toDouble()).toFloat() + (segIndex * anglePerSegment * 29f)
                } else {
                    Math.toRadians(-phaseAngle.toDouble()).toFloat() + (segIndex * anglePerSegment * 17f)
                }

                val pulseSin = Math.sin(phaseAngleRad.toDouble()).toFloat()
                val pulseMax = getSpikiness(obj)


                val pulseAmount = pulseSin * pulseMax
                val pulseInner = pulseAmount * 0.1f

                val theta = anglePerSegment * segIndex
                val cos = cos(theta.toDouble()).toFloat()
                val sin = sin(theta.toDouble()).toFloat()
                val rInner = radStart - pulseInner

                var rOuter = radStart + thickness - pulseAmount

                // Adds the spikes
                // var grav = GraviticScanData(graviticScanAbility).apply { advance(.5f) }.getDataAt(angle)  //data.getDataAt(angle)
                var grav = 0f.coerceAtMost(750f)
                grav *= 250f / 750f
                grav *= level
                rOuter += grav

                var alpha = alphaMult
                alpha *= 0.25f + (grav / 100).coerceAtMost(0.75f)

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
                    Byte.MAX_VALUE
//                    (color.alpha.toFloat() * alphaMult * alpha).toInt().toByte()
                )
                GL11.glTexCoord2f(leftTX, texProgress)
                GL11.glVertex2f(x1, y1)
                GL11.glTexCoord2f(rightTX, texProgress)
                GL11.glVertex2f(x2, y2)
                texProgress += texPerSegment * 1f
            }
            GL11.glEnd()
        }
        GL11.glPopMatrix()
    }
}