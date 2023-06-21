package wisp.perseanchronicles.telos.pt1_deliveryToEarth

import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin
import com.fs.starfarer.api.util.Misc
import wisp.questgiver.v2.BarEvent

class Telos1OfferQuest : BaseCommandPlugin() {
    override fun execute(
        ruleId: String?,
        dialog: InteractionDialogAPI?,
        params: MutableList<Misc.Token>?,
        memoryMap: MutableMap<String, MemoryAPI>?
    ): Boolean {
        val wiring = Telos1BarEventWiring()
        val event: BarEvent<Telos1HubMission> = wiring.createBarEventCreator().createBarEvent()

        // Creates the HubMission.
        if (!event.shouldShowAtMarket(dialog?.interactionTarget?.market)) {
            return false
        }

        event.addPromptAndOption(dialog!!, memoryMap!! as MutableMap<String, MemoryAPI?>)
//        dialog.plugin.memoryMap[] = event
        return true
    }
}