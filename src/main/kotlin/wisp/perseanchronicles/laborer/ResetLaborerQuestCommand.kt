package wisp.perseanchronicles.laborer

import org.lazywizard.console.BaseCommand
import org.lazywizard.console.Console
import wisp.perseanchronicles.common.PerseanChroniclesNPCs
import wisp.perseanchronicles.game
import wisp.questgiver.wispLib.findFirst

class ResetLaborerQuestCommand : BaseCommand {
    override fun runCommand(args: String, context: BaseCommand.CommandContext): BaseCommand.CommandResult {
        if (!context.isCampaignAccessible) {
            return BaseCommand.CommandResult.WRONG_CONTEXT
        }


        runCatching { LaborerHubMission.state.map.clear() }.onFailure { game.logger.w(it) }

        runCatching {
            val nirv = game.intelManager.findFirst<LaborerHubMission>()
            nirv?.setCurrentStage(LaborerHubMission.Stage.NotStarted, null, null)
            PerseanChroniclesNPCs.isLaborerInFleet = false
        }.onFailure { game.logger.w(it) }

        Console.showMessage("Quest reset.")
        return BaseCommand.CommandResult.SUCCESS
    }
}