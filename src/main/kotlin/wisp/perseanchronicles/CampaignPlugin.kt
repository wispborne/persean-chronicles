package wisp.perseanchronicles

import com.fs.starfarer.api.PluginPick
import com.fs.starfarer.api.campaign.BaseCampaignPlugin
import com.fs.starfarer.api.campaign.CampaignPlugin
import com.fs.starfarer.api.campaign.InteractionDialogPlugin
import com.fs.starfarer.api.campaign.SectorEntityToken
import wisp.perseanchronicles.dangerousGames.pt1_dragons.DragonsQuest
import wisp.perseanchronicles.dangerousGames.pt2_depths.DepthsQuest
import wisp.perseanchronicles.laborer.LaborerQuest
import wisp.perseanchronicles.laborer.Laborer_Stage2_Dialog
import wisp.perseanchronicles.nirvana.NirvanaQuest
import wisp.perseanchronicles.nirvana.Nirvana_Stage2_Dialog
import wisp.perseanchronicles.nirvana.Nirvana_Stage3_Dialog
import wisp.perseanchronicles.riley.RileyQuest
import wisp.perseanchronicles.telos.pt1_deliveryToEarth.Telos1HubMission
import wisp.perseanchronicles.telos.pt2_dart.Telos2FirstLandingDialog
import wisp.perseanchronicles.telos.pt2_dart.Telos2HubMission
import wisp.questgiver.wispLib.equalsAny
import wisp.questgiver.wispLib.findFirst
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
            // Land on planet with dragons
            interactionTarget.hasSameMarketAs(DragonsQuest.state.dragonPlanet)
                    && DragonsQuest.stage == DragonsQuest.Stage.GoToPlanet -> {
                PluginPick(
                    wisp.perseanchronicles.dangerousGames.pt1_dragons.Dragons_Stage2_Dialog().build(),
                    CampaignPlugin.PickPriority.MOD_SPECIFIC
                )
            }

            // Finish Dragonriders by landing at quest-giving planet
            interactionTarget.hasSameMarketAs(DragonsQuest.state.startingPlanet)
                    && DragonsQuest.stage == DragonsQuest.Stage.ReturnToStart -> {
                PluginPick(
                    wisp.perseanchronicles.dangerousGames.pt1_dragons.Dragons_Stage3_Dialog().build(),
                    CampaignPlugin.PickPriority.MOD_SPECIFIC
                )
            }

            // Land on ocean planet for Depths quest
            interactionTarget.hasSameMarketAs(DepthsQuest.state.depthsPlanet)
                    && DepthsQuest.stage == DepthsQuest.Stage.GoToPlanet -> {
                PluginPick(
                    wisp.perseanchronicles.dangerousGames.pt2_depths.Depths_Stage2_RiddleDialog().build(),
                    CampaignPlugin.PickPriority.MOD_SPECIFIC
                )
            }

            // Finish Depths by landing at quest-giving planet
            interactionTarget.hasSameMarketAs(DepthsQuest.state.startingPlanet)
                    && DepthsQuest.stage == DepthsQuest.Stage.ReturnToStart -> {
                PluginPick(
                    wisp.perseanchronicles.dangerousGames.pt2_depths.Depths_Stage2_EndDialog().build(),
                    CampaignPlugin.PickPriority.MOD_SPECIFIC
                )
            }

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

            // Telos 2 - Land on planet first time
            interactionTarget.id == Telos1HubMission.state.karengoPlanet?.id
                    && game.intelManager.findFirst<Telos2HubMission>()?.currentStage == Telos2HubMission.Stage.LandOnPlanetFirst ->
                PluginPick(
                    Telos2FirstLandingDialog().build(),
                    CampaignPlugin.PickPriority.MOD_SPECIFIC
                )
            else -> null
        }
    }
}