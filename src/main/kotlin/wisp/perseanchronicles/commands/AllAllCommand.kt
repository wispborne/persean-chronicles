package wisp.perseanchronicles.commands

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.FleetDataAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.fleet.FleetMemberType
import org.lazywizard.console.BaseCommand
import org.lazywizard.console.BaseCommand.CommandContext
import org.lazywizard.console.BaseCommand.CommandResult
import org.lazywizard.console.CommandUtils
import org.lazywizard.console.CommonStrings
import org.lazywizard.console.Console
import org.lazywizard.console.commands.*

class AllAllCommand : BaseCommand {
    override fun runCommand(args: String, context: CommandContext): CommandResult {
        if (!context.isInCampaign) {
            Console.showMessage(CommonStrings.ERROR_CAMPAIGN_ONLY)
            return CommandResult.WRONG_CONTEXT
        }

        AllHullsFewerModulesCommand().runCommand("", context)
        AllWeapons().runCommand("", context)
        AllWings().runCommand("", context)
        AllCommodities().runCommand("", context)
        AllBlueprints().runCommand("", context)
        AllHullmods().runCommand("", context)

        Global.getSector().playerFleet.cargo.credits.set(1000000000f)
        AddCrew().runCommand("", context)
        AddFuel().runCommand("", context)
        AddSupplies().runCommand("", context)

        return CommandResult.SUCCESS
    }
}

private class AllHullsFewerModulesCommand : BaseCommand {

    override fun runCommand(args: String, context: CommandContext): CommandResult {
        return if (!context.isInCampaign) {
            Console.showMessage("Error: This command is campaign-only.")
            CommandResult.WRONG_CONTEXT
        } else {
            var total = 0
            val target: FleetDataAPI
            val targetName: String

            if (args == null || args.isEmpty()) {
                target = Storage.retrieveStorageFleetData()
                targetName = "storage (use 'storage' to retrieve)"
            } else if ("player".equals(args, ignoreCase = true)) {
                target = Global.getSector().playerFleet.fleetData
                targetName = "player fleet"
            } else {
                val token = CommandUtils.findTokenInLocation(args, Global.getSector().currentLocation)
                if (token == null) {
                    Console.showMessage("$args not found!")
                    return CommandResult.ERROR
                }
                target = if (token is FleetMemberAPI) {
                    (token as FleetMemberAPI).fleetData
                } else {
                    val cargo = CommandUtils.getUsableCargo(token)
                    if (cargo.mothballedShips == null) {
                        cargo.initMothballedShips(token.faction.id)
                    }
                    cargo.mothballedShips
                }
                targetName = token.fullName
            }

            val ids: MutableSet<String?> = LinkedHashSet(Global.getSector().allEmptyVariantIds)

            for (tmp in target.membersListCopy) {
                if (!tmp.isFighterWing && tmp.variant.isEmptyHullVariant) {
                    ids.remove(tmp.variant.hullVariantId)
                }
            }

            for (id in ids) {
                val tmp = Global.getFactory().createFleetMember(FleetMemberType.SHIP, id)

                // Filter out some modules, at least.
                if (tmp.hullSpec.tags.any { it.contains("module", ignoreCase = true) })
                    continue

                tmp.repairTracker.isMothballed = true
                target.addFleetMember(tmp)
                ++total
            }
            Console.showMessage("Added $total ships to $targetName.")
            CommandResult.SUCCESS
        }
    }
}