package org.wisp.stories

import com.fs.starfarer.api.PluginPick
import com.fs.starfarer.api.campaign.BaseCampaignPlugin
import com.fs.starfarer.api.campaign.CampaignPlugin
import com.fs.starfarer.api.campaign.InteractionDialogPlugin
import com.fs.starfarer.api.campaign.SectorEntityToken
import org.wisp.stories.dangerousGames.A_dragons.DragonsPart1_EndingDialog
import org.wisp.stories.dangerousGames.A_dragons.DragonsPart2_EndingDialog
import org.wisp.stories.dangerousGames.A_dragons.DragonsQuest
import org.wisp.stories.dangerousGames.A_dragons.DragonsQuest_Intel
import wisp.questgiver.wispLib.QuestGiver
import wisp.questgiver.wispLib.QuestGiver.MOD_PREFIX
import wisp.questgiver.wispLib.findFirst
import wisp.questgiver.wispLib.game

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
                    DragonsPart1_EndingDialog().build(),
                    CampaignPlugin.PickPriority.MOD_SPECIFIC
                )
            // Finish Dragonriders by landing at quest-giving planet
            interactionTarget.id == game.intelManager.findFirst(DragonsQuest_Intel::class.java)?.endLocation?.planetEntity?.id
                    && DragonsQuest.stage == DragonsQuest.Stage.ReturnToStart ->
                PluginPick(
                    DragonsPart2_EndingDialog().build(),
                    CampaignPlugin.PickPriority.MOD_SPECIFIC
                )
            else -> null
        }
    }
}