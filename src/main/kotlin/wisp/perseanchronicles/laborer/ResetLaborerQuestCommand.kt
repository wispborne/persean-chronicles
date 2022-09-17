package wisp.perseanchronicles.laborer

import org.lazywizard.console.BaseCommand
import org.lazywizard.console.Console

class ResetLaborerQuestCommand : BaseCommand {
    override fun runCommand(args: String, context: BaseCommand.CommandContext): BaseCommand.CommandResult {
        if (!context.isCampaignAccessible) {
            return BaseCommand.CommandResult.WRONG_CONTEXT
        }

        LaborerQuest.restartQuest()
        Console.showMessage("Quest reset.")
        return BaseCommand.CommandResult.SUCCESS
    }
}