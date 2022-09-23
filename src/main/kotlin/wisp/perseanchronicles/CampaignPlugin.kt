package wisp.perseanchronicles

import com.fs.starfarer.api.PluginPick
import com.fs.starfarer.api.campaign.BaseCampaignPlugin
import com.fs.starfarer.api.campaign.CampaignPlugin
import com.fs.starfarer.api.campaign.InteractionDialogPlugin
import com.fs.starfarer.api.campaign.SectorEntityToken
import wisp.perseanchronicles.laborer.LaborerQuest
import wisp.perseanchronicles.laborer.Laborer_Stage2_Dialog
import wisp.perseanchronicles.nirvana.NirvanaQuest
import wisp.perseanchronicles.nirvana.Nirvana_Stage2_Dialog
import wisp.perseanchronicles.nirvana.Nirvana_Stage3_Dialog
import wisp.perseanchronicles.riley.RileyQuest
import wisp.questgiver.wispLib.equalsAny
import wisp.questgiver.wispLib.hasSameMarketAs

typealias InteractionPluginPick = (interactionTarget: SectorEntityToken) -> PluginPick<InteractionDialogPlugin>?

/**
 * Instead of using `rules.csv`, use this plugin to trigger dialog choices and conversations.
 */
class CampaignPlugin : BaseCampaignPlugin() {
    override fun getId() = "${MOD_ID}_CampaignPlugin"

    // No need to add to saves
    override fun isTransient(): Boolean = true

    /**
     * When the player interacts with a dialog, override the default interaction with a
     * mod-specific one if necessary.
     */
    override fun pickInteractionDialogPlugin(interactionTarget: SectorEntityToken): PluginPick<InteractionDialogPlugin>? {
        return when {
            // Finish Riley quest by landing at father's planet
            interactionTarget.hasSameMarketAs(RileyQuest.state.destinationPlanet)
                    && RileyQuest.stage.equalsAny(
                RileyQuest.Stage.InitialTraveling,
                RileyQuest.Stage.LandingOnPlanet
            ) -> {
                PluginPick(
                    wisp.perseanchronicles.riley.Riley_Stage4_Dialog().build(),
                    CampaignPlugin.PickPriority.MOD_SPECIFIC
                )
            }

            // Finish Nirvana quest by landing at pulsar planet
            interactionTarget.hasSameMarketAs(NirvanaQuest.state.destPlanet)
                    && NirvanaQuest.shouldShowStage2Dialog() -> {
                PluginPick(
                    Nirvana_Stage2_Dialog().build(),
                    CampaignPlugin.PickPriority.MOD_SPECIFIC
                )
            }

            // Shhhhh
            interactionTarget.hasSameMarketAs(NirvanaQuest.state.destPlanet)
                    && NirvanaQuest.shouldShowStage3Dialog() -> {
                PluginPick(
                    Nirvana_Stage3_Dialog().build(),
                    CampaignPlugin.PickPriority.MOD_SPECIFIC
                )
            }

            // Finish laborer quest
            interactionTarget.hasSameMarketAs(LaborerQuest.state.destPlanet)
                    && LaborerQuest.shouldShowStage2Dialog() -> {
                PluginPick(
                    Laborer_Stage2_Dialog().build(),
                    CampaignPlugin.PickPriority.MOD_SPECIFIC
                )
            }

            else -> null
        }
    }
}