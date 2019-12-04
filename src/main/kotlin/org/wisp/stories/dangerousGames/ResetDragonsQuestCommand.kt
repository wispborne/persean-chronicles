package org.wisp.stories.dangerousGames

import org.lazywizard.console.BaseCommand
import org.lazywizard.console.Console
import org.wisp.stories.dangerousGames.A_dragons.DragonsQuest
import org.wisp.stories.dangerousGames.A_dragons.DragonsQuest_Intel
import org.wisp.stories.wispLib.di
import org.wisp.stories.wispLib.findFirst

class ResetDragonsQuestCommand : BaseCommand {
    override fun runCommand(args: String?, context: BaseCommand.CommandContext): BaseCommand.CommandResult {
        if (!context.isCampaignAccessible) {
            return BaseCommand.CommandResult.WRONG_CONTEXT
        }

        DragonsQuest.stage = DragonsQuest.Stage.NotStarted
        di.intelManager.findFirst(DragonsQuest_Intel::class.java)?.endImmediately()
        DragonsQuest.findAndTagDragonPlanetIfNeeded(forceTagPlanet = true)
        Console.showMessage("Quest reset. You didn't ditch those guys, right?")
        return BaseCommand.CommandResult.SUCCESS
    }
}