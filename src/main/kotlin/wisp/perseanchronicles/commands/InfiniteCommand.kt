package wisp.perseanchronicles.commands

import org.lazywizard.console.BaseCommand
import org.lazywizard.console.CommonStrings
import org.lazywizard.console.Console
import org.lazywizard.console.commands.InfiniteFuel
import org.lazywizard.console.commands.InfiniteSupplies

class InfiniteCommand : BaseCommand {
    override fun runCommand(args: String, context: BaseCommand.CommandContext): BaseCommand.CommandResult {
        if (!context.isInCampaign) {
            Console.showMessage(CommonStrings.ERROR_CAMPAIGN_ONLY)
            return BaseCommand.CommandResult.WRONG_CONTEXT
        }

        InfiniteFuel().runCommand("", context)
        InfiniteSupplies().runCommand("", context)

        return BaseCommand.CommandResult.SUCCESS
    }
}