package org.wisp.stories.dangerousGames

import com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager
import org.lazywizard.console.BaseCommand
import org.lazywizard.console.Console
import org.wisp.stories.dangerousGames.A_dragons.DragonsPart1_BarEventCreator
import org.wisp.stories.dangerousGames.A_dragons.DragonsQuest
import org.wisp.stories.dangerousGames.A_dragons.DragonsQuest_Intel
import org.wisp.stories.wispLib.di
import org.wisp.stories.wispLib.findFirst
import org.wisp.stories.wispLib.removeBarEventCreator

class ResetDragonsQuestCommand : BaseCommand {
    override fun runCommand(args: String?, context: BaseCommand.CommandContext): BaseCommand.CommandResult {
        if (!context.isCampaignAccessible) {
            return BaseCommand.CommandResult.WRONG_CONTEXT
        }

        DragonsQuest.stage = DragonsQuest.Stage.NotStarted
        di.intelManager.findFirst(DragonsQuest_Intel::class.java)?.endImmediately()
        DragonsQuest.clearDragonPlanetTag()
        BarEventManager.getInstance().removeBarEventCreator(DragonsPart1_BarEventCreator::class.java)
        Console.showMessage("Quest reset. You didn't ditch those guys, right?")
        return BaseCommand.CommandResult.SUCCESS
    }
}