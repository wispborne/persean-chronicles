package wisp.perseanchronicles.telos.pt1_deliveryToEarth

import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin
import com.fs.starfarer.api.util.Misc

class Telos1OfferSelected : BaseCommandPlugin() {
    override fun execute(
        ruleId: String?,
        dialog: InteractionDialogAPI?,
        params: MutableList<Misc.Token>?,
        memoryMap: MutableMap<String, MemoryAPI>?
    ): Boolean {
        val event = dialog?.plugin

        // Creates the HubMission.
//        if (!event.shouldShowAtMarket(dialog?.interactionTarget?.market)) {
//            return false
//        }
//
//        event.init(dialog!!, memoryMap)
        return true
    }
}