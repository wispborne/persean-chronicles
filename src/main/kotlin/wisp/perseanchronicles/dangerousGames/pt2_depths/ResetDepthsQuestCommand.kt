package wisp.perseanchronicles.dangerousGames.pt2_depths

import org.lazywizard.console.BaseCommand
import org.lazywizard.console.Console
import wisp.perseanchronicles.common.PerseanChroniclesNPCs
import wisp.perseanchronicles.game
import wisp.questgiver.wispLib.findFirst

class ResetDepthsQuestCommand : BaseCommand {
    override fun runCommand(args: String, context: BaseCommand.CommandContext): BaseCommand.CommandResult {
        if (!context.isCampaignAccessible) {
            return BaseCommand.CommandResult.WRONG_CONTEXT
        }

        PerseanChroniclesNPCs.isKarengoInFleet = false
        val mission: DepthsHubMission? = game.intelManager.findFirst()
        mission?.setCurrentStage(DepthsHubMission.Stage.Abandoned, null, null)
        mission?.setCurrentStage(DepthsHubMission.Stage.NotStarted, null, null)
        Console.showMessage(if (mission == null) "Quest not found" else "Quest reset.")
        return BaseCommand.CommandResult.SUCCESS
    }
}