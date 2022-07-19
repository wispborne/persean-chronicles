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

        game.settings.getSprite("wisp_perseanchronicles_telos", "nocturneRadar").apply {
            val size = .5f
//                width = size
//                height = size
//            setSize(size, size)
            renderAtCenter(0f, 0f)
        }
//        jumpAnimation?.render(entity.location)
    }
}