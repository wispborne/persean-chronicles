package org.wisp.stories.dangerousGames

import org.lazywizard.console.BaseCommand
import org.lazywizard.console.Console
import org.wisp.stories.dangerousGames.pt1_dragons.DragonsQuest

class ResetDragonsQuestCommand : BaseCommand {
    override fun runCommand(args: String, context: BaseCommand.CommandContext): BaseCommand.CommandResult {
        if (!context.isCampaignAccessible) {
            return BaseCommand.CommandResult.WRONG_CONTEXT
        }

        DragonsQuest.restartQuest()
        Console.showMessage("Quest reset. You didn't ditch those guys, right?")
        return BaseCommand.CommandResult.SUCCESS
    }
}