package wisp.perseanchronicles.telos.pt2_dart.battle

import org.lazywizard.console.BaseCommand
import wisp.perseanchronicles.telos.pt2_dart.battle.Telos2Battle

class Telos2TestBattleCommand : BaseCommand {
    override fun runCommand(args: String, context: BaseCommand.CommandContext): BaseCommand.CommandResult {
        if (!context.isInCampaign) {
            return BaseCommand.CommandResult.WRONG_CONTEXT
        }

        Telos2Battle.startBattle()

        return BaseCommand.CommandResult.SUCCESS
    }
}