package wisp.perseanchronicles.telos.pt2_dart.battle

import org.lazywizard.console.BaseCommand

class Telos2TestBattleCommand : BaseCommand {
    override fun runCommand(args: String, context: BaseCommand.CommandContext): BaseCommand.CommandResult {
        if (!context.isInCampaign) {
            return BaseCommand.CommandResult.WRONG_CONTEXT
        }

        Telos2BattleCoordinator.startBattle()

        return BaseCommand.CommandResult.SUCCESS
    }
}