package wisp.perseanchronicles.telos

import com.fs.starfarer.api.util.Misc
import org.lazywizard.console.BaseCommand
import wisp.perseanchronicles.game
import wisp.perseanchronicles.telos.pt1_deliveryToEarth.Telos1HubMission

class StartTelosQuestCommand : BaseCommand {
    override fun runCommand(args: String, context: BaseCommand.CommandContext): BaseCommand.CommandResult {
        if (!context.isInCampaign) {
            return BaseCommand.CommandResult.WRONG_CONTEXT
        }

        Telos1HubMission().apply {
            val market = Misc.findNearestLocalMarket(game.sector.playerFleet, 10000f, null)
            if (create(market, false)) {
                accept(null, null)
            } else {
                return BaseCommand.CommandResult.ERROR
            }
        }
        return BaseCommand.CommandResult.SUCCESS
    }
}