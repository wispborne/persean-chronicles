package wisp.perseanchronicles.telos.pt3_arrow.nocturne

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.*
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.impl.campaign.abilities.BaseToggleAbility
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.campaign.CampaignEngine
import org.lazywizard.lazylib.ext.rotate
import org.lwjgl.opengl.GL11
import org.lwjgl.util.vector.Vector2f
import wisp.perseanchronicles.common.fx.CampaignCustomRenderer
import wisp.perseanchronicles.common.fx.CustomRenderer
import wisp.perseanchronicles.game
import wisp.questgiver.wispLib.distanceFromPlayerInHyperspace
import wisp.questgiver.wispLib.equalsAny
import wisp.questgiver.wispLib.random
import java.awt.Color
import java.util.*
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class TelevisionScript : BaseToggleAbility() {
    private val HYPERSPACE_RANGE = 20000f

    private val originalMaxZoom = game.settings.getFloat("maxCampaignZoom")

    override fun runWhilePaused() = true

    protected var phaseAngle = 0f

    @Transient
    var customRenderer: CampaignCustomRenderer? = null

    override fun getActiveLayers(): EnumSet<CampaignEngineLayers?>? = EnumSet.of(CampaignEngineLayers.ABOVE)

    override fun advance(amount: Float) {
        super.advance(amount)

        val days = Global.getSector().clock.convertToDays(if (game.sector.isPaused) 0f else amount)
        phaseAngle += days * 360f * 10f
        phaseAngle = Misc.normalizeAngle(phaseAngle)

        if (customRenderer == null)
            customRenderer = Global.getSector().playerFleet.containingLocation.addCustomEntity(
                "perseanchronicles_ethersight",
                "YOU SHOULD NOT SEE THIS",
                "PerseanChronicles_CustomRenderer_Nebula",
                Factions.INDEPENDENT,
                this
            ).customPlugin as CampaignCustomRenderer
        customRenderer?.advance(amount)
        customRenderer?.render(CampaignEngineLayers.ABOVE, game.sector.viewport)
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

        if (fleet.isInHyperspace) {
            game.sector.currentLocation.fleets
                .filter { it.locationInHyperspace.distanceFromPlayerInHyperspace < HYPERSPACE_RANGE }
//                .run { render(this, viewport) }
                .run { renderUsingClouds(this, viewport) }
        } else {
            game.sector.currentLocation.allEntities
                .asSequence()
//                .filter { Misc.getDistance(it, game.sector.playerFleet) <= maxOf(viewport.visibleHeight, viewport.visibleWidth) }
                .filterNot { obj ->
                    obj is RingBandAPI
                            || obj.tags.any { it.equalsAny(Tags.TERRAIN, Tags.ORBITAL_JUNK) }
//                            || (obj is CustomCampaignEntity && obj.sprite == null) // This decides whether things like derelicts are shown.
                }
                .run {
                    renderUsingClouds(this.toList(), viewport)
//                    render(this.toList(), viewport)
                }
        }
    }

    fun getEntityColor(obj: SectorEntityToken): Color = when (obj) {
        is PlanetAPI -> obj.spec.iconColor
        is CampaignFleetAPI -> when {
            obj.isHostileTo(game.sector.playerFleet) -> Color.RED
            else -> obj.faction.color
        }

        is JumpPointAPI -> Color(128, 100, 255) // ideally would get color from the sprite for recolor mods.
        else -> obj.indicatorColor
    }

    fun getEntitySpikeColor(obj: SectorEntityToken): Color =
        when {
            obj is CampaignFleetAPI && obj.isHostileTo(game.sector.playerFleet) -> Color.RED
            else -> getEntityColor(obj)
        }

    private fun getSpikiness(obj: SectorEntityToken) =
        when (obj) {
            game.sector.playerFleet -> 0f
            is CampaignFleetAPI -> 8f
            is JumpPointAPI -> 3f
            else -> .5f
        }

    private fun getSpikeSize(obj: SectorEntityToken): Float =
        when (obj) {
            game.sector.playerFleet -> 0f
            is CampaignFleetAPI -> (Misc.getDangerLevel(obj) / 5f) * 750f
            else -> 0f
        }

    @Transient
    protected var texture: SpriteAPI? = null

    private fun renderUsingClouds(objs: List<SectorEntityToken>, view: ViewportAPI) {
        val velocityScale = .000f
        val sizeScale = 0.001f
        val durationScale = 1.0f
        val rampUpScale = 1.0f
        val alphaScale = .45f
        val topLayerAlphaScale = .15f
        val bottomLayerAlphaScale = .40f
        val endSizeScale = 1.55f
        val densityInverted = 0.03f // Lower is more dense
        val vel = Vector2f(100f * velocityScale, 100f * velocityScale)
            .rotate(Random.nextFloat() * 360f)

        objs.forEach { obj ->
            val scale = game.settings.screenScaleMult
            customRenderer?.addNebula(
                location = Vector2f(view.convertWorldXtoScreenX(obj.location.x) * scale, view.convertWorldYtoScreenY(obj.location.y) * scale),
                anchorLocation = view.center.negate() as Vector2f,//obj.starSystem?.center?.location ?: obj.containingLocation?.location ?: Vector2f(),
                velocity = vel,
                size = (40f..60f).random() * sizeScale,
                endSizeMult = endSizeScale,
                duration = (1.2f..1.5f).random() * durationScale,
                inFraction = 0.1f * rampUpScale,
                outFraction = 0.5f,
                color = getEntityColor(obj),
//                layer = CombatEngineLayers.UNDER_SHIPS_LAYER,
                type = CustomRenderer.NebulaType.NORMAL,
                negative = false
            )
        }
    }

    private fun render(objs: List<SectorEntityToken>, viewport: ViewportAPI) {
        if (!this.isActive) return

        val level: Float = .524f//1f
        val alphaMult = level

        val bandWidthInTexture = 256f // vanilla 256
        var bandIndex: Float
//        val radStart = getRingRadius(obj)
//        val radEnd = radStart + 75f
        val radStart = 0f
        val startRad = Math.toRadians(0.0).toFloat()
        val endRad = Math.toRadians(360.0).toFloat()
        val spanRad = abs(endRad - startRad)

        GL11.glEnable(GL11.GL_TEXTURE_2D)
        if (texture == null) texture = Global.getSettings().getSprite("wisp_perseanchronicles_telos", "television")
        texture!!.bindTexture()
        GL11.glEnable(GL11.GL_BLEND)

        objs.forEach { obj ->
            val spikiness = getSpikiness(obj)
            val radius = getRingRadius(obj)
            val radEnd = radius * 2 + 75f
            val circ = (Math.PI * 2f * (radStart + radEnd) / 2f).toFloat()
            // magic number that fixes all performance problems, thank you tomatopaste
            val segmentAdjustment = when {
                radius < 0f -> 8f
                spikiness < 8f -> 30f
                spikiness < 10f -> 60f
                else -> 80f
            }
            val pixelsPerSegment = circ / segmentAdjustment
            val segments = (circ / pixelsPerSegment).toInt().toFloat()

            val anglePerSegment = spanRad / segments
            val loc: Vector2f = obj.location
            val x = loc.x
            val y = loc.y
            GL11.glPushMatrix()
//            GL11.glTranslatef(x, y, 0f)

            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

            val thickness = (radEnd - radStart) //* (1 + 1 / radStart)
            var texProgress = 0f
            val texHeight = texture!!.textureHeight
            val imageHeight = texture!!.height
            var texPerSegment = pixelsPerSegment * texHeight / imageHeight * bandWidthInTexture / thickness
            val totalTex = (texPerSegment * segments).toInt().toFloat().coerceAtLeast(1f)
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

                    val pulseSin = sin(phaseAngleRad.toDouble()).toFloat()
                    val pulseMax = spikiness

                    val pulseAmount = pulseSin * pulseMax
                    val pulseInner = pulseAmount * 0.1f

                    val theta = anglePerSegment * segIndex
                    val cos = cos(theta.toDouble()).toFloat()
                    val sin = sin(theta.toDouble()).toFloat()
                    val rInner = radStart - pulseInner

                    var rOuter = radStart + thickness - pulseAmount

                    // Adds the spikes
                    // var grav = GraviticScanData(graviticScanAbility).apply { advance(.5f) }.getDataAt(angle)  //data.getDataAt(angle)
                    var grav = getSpikeSize(obj).coerceAtMost(750f)
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
                    )
                    GL11.glTexCoord2f(leftTX, texProgress)
                    GL11.glVertex2f(x1 + x, y1 + y)
                    GL11.glTexCoord2f(rightTX, texProgress)
                    GL11.glVertex2f(x2 + x, y2 + y)
                    texProgress += texPerSegment
                }

                GL11.glEnd()
            }
//            GL11.glPopMatrix()
        }
    }
}