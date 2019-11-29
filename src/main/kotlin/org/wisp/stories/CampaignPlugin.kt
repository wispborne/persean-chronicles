package org.wisp.stories

import com.fs.starfarer.api.PluginPick
import com.fs.starfarer.api.campaign.BaseCampaignPlugin
import com.fs.starfarer.api.campaign.CampaignPlugin
import com.fs.starfarer.api.campaign.InteractionDialogPlugin
import com.fs.starfarer.api.campaign.SectorEntityToken
import org.wisp.stories.dangerousGames.A_dragons.DragonsPart1_EndingDialog
import org.wisp.stories.dangerousGames.A_dragons.DragonsQuest
import org.wisp.stories.wispLib.MOD_PREFIX

/**
 * Instead of using `rules.csv`, use this plugin to trigger dialog choices and conversations.
 */
class CampaignPlugin : BaseCampaignPlugin() {

    override fun getId() = "${MOD_PREFIX}_GateInteractionPlugin"

    // No need to add to saves
    override fun isTransient(): Boolean = true

    /**
     * When the player interacts with a dialog, override the default interaction with a
     * mod-specific one if necessary.
     */
    override fun pickInteractionDialogPlugin(interactionTarget: SectorEntityToken): PluginPick<InteractionDialogPlugin>? {
        // Interacting with a gate
        return when {
            interactionTarget.id == DragonsQuest.dragonPlanet?.id -> {
                // Show dialog to complete the intro quest
                PluginPick(
                    DragonsPart1_EndingDialog().build(),
                    CampaignPlugin.PickPriority.MOD_SPECIFIC
                )
            }
            else -> null
        }
    }
}