package wisp.perseanchronicles.telos.pt3_arrow.nocturne

import com.fs.starfarer.api.campaign.CampaignEngineLayers
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.impl.campaign.BaseCustomEntityPlugin
import wisp.perseanchronicles.game

class NocturneCustomEntity : BaseCustomEntityPlugin() {
    override fun init(entity: SectorEntityToken?, pluginParams: Any?) {
        super.init(entity, pluginParams)

    }

    override fun advance(amount: Float) {
        super.advance(amount)
    }

    override fun render(layer: CampaignEngineLayers, viewport: ViewportAPI) {
        super.render(layer, viewport)

        game.settings.getSprite("wisp_perseanchronicles_telos", "nocturneBlackout").apply {
            setSize(viewport.visibleWidth * 1.5f, viewport.visibleHeight * 1.5f)
            alphaMult = 0.65f
            renderAtCenter(entity.location.x, entity.location.y)
        }
    }
}