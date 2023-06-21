package wisp.perseanchronicles.riley

import org.lazywizard.console.BaseCommand
import org.lazywizard.console.Console
import wisp.perseanchronicles.game
import wisp.questgiver.wispLib.findFirst

class ResetRileyQuestCommand : BaseCommand {
    override fun runCommand(args: String, context: BaseCommand.CommandContext): BaseCommand.CommandResult {
        if (!context.isCampaignAccessible) {
            return BaseCommand.CommandResult.WRONG_CONTEXT
        }

        val mission: RileyHubMission = game.intelManager.findFirst()!!
        mission.setCurrentStage(null, null, null)
        Console.showMessage("Quest reset.")
        return BaseCommand.CommandResult.SUCCESS
    }
}