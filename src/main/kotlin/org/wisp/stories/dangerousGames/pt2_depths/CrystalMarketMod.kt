package org.wisp.stories.dangerousGames.B_depths

import com.fs.starfarer.api.impl.campaign.econ.BaseHazardCondition
import com.fs.starfarer.api.impl.campaign.ids.Commodities
import com.fs.starfarer.api.impl.campaign.ids.Industries
import com.fs.starfarer.api.ui.TooltipMakerAPI
import wisp.questgiver.addPara

class CrystalMarketMod : BaseHazardCondition() {
    private val DRUG_MULTIPLIER = 0.10f

    override fun apply(id: String?) {
        super.apply(id)
        market.getIndustry(Industries.LIGHTINDUSTRY)
            ?.getSupply(Commodities.DRUGS)
            ?.quantity
            ?.modifyMult(id, DRUG_MULTIPLIER)
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
// todo change to increasing production by one?
        tooltip?.addPara {
            "Drug production increased by " + mark("$DRUG_MULTIPLIER%")
        }
    }
}