package wisp.perseanchronicles.telos.pt1_deliveryToEarth

import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin
import com.fs.starfarer.api.util.Misc
import wisp.perseanchronicles.game

@Deprecated("Unused, I think.")
class ShouldOfferTelos1 : BaseCommandPlugin() {
    override fun execute(
        ruleId: String?,
        dialog: InteractionDialogAPI?,
        params: MutableList<Misc.Token>?,
        memoryMap: MutableMap<String, MemoryAPI>?
    ): Boolean {
        // TODO don't offer at all planets with no preconditions.
        return Telos1HubMission.state.startDateMillis == null || game.settings.isDevMode
    }
}