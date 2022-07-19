package wisp.perseanchronicles.telos.pt3_arrow

import org.lazywizard.console.BaseCommand
import wisp.perseanchronicles.game
import wisp.perseanchronicles.telos.pt3_arrow.nocturne.NocturneScript

class BlindMe : BaseCommand {
    override fun runCommand(args: String, context: BaseCommand.CommandContext): BaseCommand.CommandResult {
        if (!context.isInCampaign || game.sector.playerFleet.isInHyperspace) {
            return BaseCommand.CommandResult.WRONG_CONTEXT
        }

        game.sector.addScript(NocturneScript())

        return BaseCommand.CommandResult.SUCCESS
    }
}