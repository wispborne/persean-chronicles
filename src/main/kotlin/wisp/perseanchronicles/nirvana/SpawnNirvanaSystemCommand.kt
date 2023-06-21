package wisp.perseanchronicles.nirvana

import org.lazywizard.console.BaseCommand
import org.lazywizard.console.Console

class SpawnNirvanaSystemCommand : BaseCommand {
    override fun runCommand(args: String, context: BaseCommand.CommandContext): BaseCommand.CommandResult {
        if (!context.isCampaignAccessible) {
            return BaseCommand.CommandResult.WRONG_CONTEXT
        }

        if (NirvanaQuest.createPulsarSystem()) {
            Console.showMessage("System created.")
        } else {
            Console.showMessage("System not created.")
        }
        return BaseCommand.CommandResult.SUCCESS
    }
}