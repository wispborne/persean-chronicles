package wisp.perseanchronicles.telos.pt3_arrow

import org.lazywizard.console.BaseCommand
import wisp.perseanchronicles.game
import wisp.perseanchronicles.telos.pt3_arrow.nocturne.NocturneScript

class BlindMeCommand : BaseCommand {
    override fun runCommand(args: String, context: BaseCommand.CommandContext): BaseCommand.CommandResult {
        if (!context.isInCampaign) {
            return BaseCommand.CommandResult.WRONG_CONTEXT
        }

        game.sector.addScript(NocturneScript().apply { this.millisRemaining = 15000f })
        return BaseCommand.CommandResult.SUCCESS
    }
}