package wisp.perseanchronicles.commands

import com.fs.starfarer.api.Global
import org.lazywizard.console.BaseCommand
import org.lazywizard.console.CommonStrings
import org.lazywizard.console.Console
import org.lazywizard.console.commands.*

class AllAllCommand : BaseCommand {
    override fun runCommand(args: String, context: BaseCommand.CommandContext): BaseCommand.CommandResult {
        if (!context.isInCampaign) {
            Console.showMessage(CommonStrings.ERROR_CAMPAIGN_ONLY)
            return BaseCommand.CommandResult.WRONG_CONTEXT
        }

        AllHulls().runCommand("", context)
        AllWeapons().runCommand("", context)
        AllWings().runCommand("", context)
        AllCommodities().runCommand("", context)
        AllBlueprints().runCommand("", context)
        AllHullmods().runCommand("", context)

        Global.getSector().playerFleet.cargo.credits.set(1000000000f)
        AddCrew().runCommand("", context)
        AddFuel().runCommand("", context)
        AddSupplies().runCommand("", context)

        return BaseCommand.CommandResult.SUCCESS
    }
}