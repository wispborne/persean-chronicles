package wisp.perseanchronicles.dangerousGames.pt2_depths

import com.fs.starfarer.api.impl.campaign.econ.BaseHazardCondition
import com.fs.starfarer.api.impl.campaign.ids.Commodities
import com.fs.starfarer.api.impl.campaign.ids.Industries
import com.fs.starfarer.api.ui.TooltipMakerAPI
import wisp.perseanchronicles.game
import wisp.questgiver.wispLib.addPara

class CrystalMarketMod : BaseHazardCondition() {
    companion object {
        /**
         * +50%
         */
        @Transient
        private val DRUG_MULTIPLIER = 50f

        const val CONDITION_ID = "wispQuests_crystallineCatalyst"
    }

    override fun apply(id: String?) {
        super.apply(id)
        market.getIndustry(Industries.LIGHTINDUSTRY)
            ?.getSupply(Commodities.DRUGS)
            ?.quantity
            ?.apply {
                this.unmodify(id) // Prevent using our own mod when calculating value to add
                modifyFlat(id, this.modifiedValue * (DRUG_MULTIPLIER / 100), game.text["dg_de_modifier_crystals_name"])
            }
    }

    override fun unapply(id: String?) {
        super.unapply(id)
        market.getIndustry(Industries.LIGHTINDUSTRY)
            ?.getSupply(Commodities.DRUGS)
            ?.quantity
            ?.unmodify(id)
    }

    override fun createTooltipAfterDescription(tooltip: TooltipMakerAPI?, expanded: Boolean) {
        super.createTooltipAfterDescription(tooltip, expanded)

        tooltip?.addPara {
            game.text.getf("dg_de_modifier_crystals_tooltip", "modifierAmount" to DRUG_MULTIPLIER)
        }
    }
}