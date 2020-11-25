package org.wisp.stories

import com.fs.starfarer.api.PluginPick
import com.fs.starfarer.api.campaign.BaseCampaignPlugin
import com.fs.starfarer.api.campaign.CampaignPlugin
import com.fs.starfarer.api.campaign.InteractionDialogPlugin
import com.fs.starfarer.api.campaign.SectorEntityToken
import org.wisp.stories.dangerousGames.pt1_dragons.DragonsQuest
import org.wisp.stories.dangerousGames.pt2_depths.DepthsQuest
import wisp.questgiver.wispLib.QuestGiver
import wisp.questgiver.wispLib.QuestGiver.MOD_PREFIX

/**
 * Instead of using `rules.csv`, use this plugin to trigger dialog choices and conversations.
 */
class CampaignPlugin : BaseCampaignPlugin() {
    init {
        QuestGiver.initialize(modPrefix = "stories")
    }

    override fun getId() = "${MOD_PREFIX}_CampaignPlugin"

    // No need to add to saves
    override fun isTransient(): Boolean = true

    /**
     * When the player interacts with a dialog, override the default interaction with a
     * mod-specific one if necessary.
     */
    override fun pickInteractionDialogPlugin(interactionTarget: SectorEntityToken): PluginPick<InteractionDialogPlugin>? {
        return when {
            // Land on planet with dragons
            interactionTarget.id == DragonsQuest.dragonPlanet?.id
                    && DragonsQuest.stage == DragonsQuest.Stage.GoToPlanet ->
                PluginPick(
                    org.wisp.stories.dangerousGames.pt1_dragons.Dragons_Stage2_Dialog().build(),
                    CampaignPlugin.PickPriority.MOD_SPECIFIC
                )
            // Finish Dragonriders by landing at quest-giving planet
            interactionTarget.id == DragonsQuest.startingPlanet?.id
                    && DragonsQuest.stage == DragonsQuest.Stage.ReturnToStart ->
                PluginPick(
                    org.wisp.stories.dangerousGames.pt1_dragons.Dragons_Stage3_Dialog().build(),
                    CampaignPlugin.PickPriority.MOD_SPECIFIC
                )
            // Land on ocean planet for Depths quest
            interactionTarget.id == DepthsQuest.depthsPlanet?.id
                    && DepthsQuest.stage == DepthsQuest.Stage.GoToPlanet ->
                PluginPick(
                    org.wisp.stories.dangerousGames.pt2_depths.Depths_Stage2_Dialog().build(),
                    CampaignPlugin.PickPriority.MOD_SPECIFIC
                )
            // Finish Depths by landing at quest-giving planet
            interactionTarget.id == DepthsQuest.startingPlanet?.id
                    && DepthsQuest.stage == DepthsQuest.Stage.ReturnToStart ->
                PluginPick(
                    org.wisp.stories.dangerousGames.pt2_depths.Depths_Stage3_Dialog().build(),
                    CampaignPlugin.PickPriority.MOD_SPECIFIC
                )
            else -> null
        }
    }
}