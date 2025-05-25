package wisp.perseanchronicles.telos.pt3_arrow.nocturne

import com.fs.starfarer.api.campaign.CampaignEngineLayers
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.impl.campaign.BaseCustomEntityPlugin
import org.lwjgl.opengl.GL11

class NocturneCustomEntity : BaseCustomEntityPlugin() {
    override fun init(entity: SectorEntityToken?, pluginParams: Any?) = super.init(entity, pluginParams)
    override fun advance(amount: Float) = super.advance(amount)

    override fun getRenderRange(): Float {
        return this.entity.radius
    }

    override fun render(layer: CampaignEngineLayers, viewport: ViewportAPI) {
        super.render(layer, viewport)
        val offset = 0f
        val alpha = 0.65f

//        game.settings.getSprite("wisp_perseanchronicles_telos", "nocturneBlackout").apply {
//            setSize(viewport.visibleWidth + offset, viewport.visibleHeight + offset)
//            alphaMult = alpha
//            render(viewport.llx - offset / 2, viewport.lly - offset / 2)
//        }

        val uly = viewport.lly + viewport.visibleHeight
        val urx = viewport.llx + viewport.visibleWidth

        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

        GL11.glDisable(GL11.GL_TEXTURE_2D)

        GL11.glColor4f(0f, 0f, 0f, alpha) // black with 65% opacity

        GL11.glBegin(GL11.GL_QUADS)
        GL11.glVertex2f(viewport.llx, viewport.lly)
        GL11.glVertex2f(viewport.llx, uly)
        GL11.glVertex2f(urx, uly)
        GL11.glVertex2f(urx, viewport.lly)
        GL11.glEnd()

        GL11.glEnable(GL11.GL_TEXTURE_2D) // re-enable for rest of rendering
        GL11.glColor4f(1f, 1f, 1f, 1f)     // reset color to white/opaque

    }
}