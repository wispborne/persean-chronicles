package wisp.perseanchronicles.dangerousGames.pt1_dragons

import com.fs.starfarer.api.Global
import org.lazywizard.console.BaseCommand
import org.lazywizard.console.Console
import wisp.perseanchronicles.common.PerseanChroniclesNPCs
import wisp.questgiver.wispLib.findFirst

class ResetDragonsQuestCommand : BaseCommand {
    override fun runCommand(args: String, context: BaseCommand.CommandContext): BaseCommand.CommandResult {
        if (!context.isCampaignAccessible) {
            return BaseCommand.CommandResult.WRONG_CONTEXT
        }

        PerseanChroniclesNPCs.isKarengoInFleet = false
        val dragons: DragonsHubMission? = Global.getSector().intelManager.findFirst()
        dragons?.setCurrentStage(DragonsHubMission.Stage.Abandoned, null, null)
        dragons?.setCurrentStage(DragonsHubMission.Stage.NotStarted, null, null)
        Console.showMessage("Quest reset. You didn't ditch those guys, right?")
        return BaseCommand.CommandResult.SUCCESS
    }
}