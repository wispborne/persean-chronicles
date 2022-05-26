package wisp.perseanchronicles.riley

import org.lazywizard.console.BaseCommand
import org.lazywizard.console.Console

class ResetRileyQuestCommand : BaseCommand {
    override fun runCommand(args: String, context: BaseCommand.CommandContext): BaseCommand.CommandResult {
        if (!context.isCampaignAccessible) {
            return BaseCommand.CommandResult.WRONG_CONTEXT
        }

        RileyQuest.restartQuest()
        Console.showMessage("Quest reset.")
        return BaseCommand.CommandResult.SUCCESS
    }
}