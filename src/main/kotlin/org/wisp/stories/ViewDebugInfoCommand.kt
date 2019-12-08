package org.wisp.stories

import org.lazywizard.console.BaseCommand
import org.lazywizard.console.Console
import org.wisp.stories.dangerousGames.A_dragons.DragonsQuest

class ViewDebugInfoCommand : BaseCommand {
    override fun runCommand(args: String?, context: BaseCommand.CommandContext): BaseCommand.CommandResult {
        if (!context.isCampaignAccessible) {
            return BaseCommand.CommandResult.WRONG_CONTEXT
        }

        val info = StringBuilder()

        info.appendln("Stories Debug Info")
        info.appendln("-------")
        info.appendln("Dragonriders planet: ${DragonsQuest.dragonPlanet?.fullName} in ${DragonsQuest?.dragonPlanet?.starSystem?.baseName}")
        info.appendln("Dragonriders quest stage: ${DragonsQuest.stage}")

        Console.showMessage(info.toString())

        return BaseCommand.CommandResult.SUCCESS
    }
}