package wisp.perseanchronicles.nirvana

import org.lazywizard.console.BaseCommand
import org.lazywizard.console.Console
import wisp.perseanchronicles.game
import wisp.questgiver.wispLib.findFirst

class ResetNirvanaQuestCommand : BaseCommand {
    override fun runCommand(args: String, context: BaseCommand.CommandContext): BaseCommand.CommandResult {
        if (!context.isCampaignAccessible) {
            return BaseCommand.CommandResult.WRONG_CONTEXT
        }

        runCatching { NirvanaHubMission.state.map.clear() }.onFailure { game.logger.w(it) }

        runCatching {
            val nirv = game.intelManager.findFirst<NirvanaHubMission>()
            nirv?.setCurrentStage(NirvanaHubMission.Stage.NotStarted, null, null)
        }.onFailure { game.logger.w(it) }

        Console.showMessage("Quest reset.")
        return BaseCommand.CommandResult.SUCCESS
    }
}