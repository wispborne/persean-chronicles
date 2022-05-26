package wisp.perseanchronicles.nirvana

import org.lazywizard.console.BaseCommand
import org.lazywizard.console.Console

class ResetNirvanaQuestCommand : BaseCommand {
    override fun runCommand(args: String, context: BaseCommand.CommandContext): BaseCommand.CommandResult {
        if (!context.isCampaignAccessible) {
            return BaseCommand.CommandResult.WRONG_CONTEXT
        }

        NirvanaQuest.restartQuest()
        Console.showMessage("Quest reset.")
        return BaseCommand.CommandResult.SUCCESS
    }
}