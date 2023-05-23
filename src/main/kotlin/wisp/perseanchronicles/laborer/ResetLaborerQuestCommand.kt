package wisp.perseanchronicles.laborer

import com.fs.starfarer.api.Global
import org.lazywizard.console.BaseCommand
import org.lazywizard.console.Console
import wisp.questgiver.wispLib.findFirst

class ResetLaborerQuestCommand : BaseCommand {
    override fun runCommand(args: String, context: BaseCommand.CommandContext): BaseCommand.CommandResult {
        if (!context.isCampaignAccessible) {
            return BaseCommand.CommandResult.WRONG_CONTEXT
        }

        val lab = Global.getSector().intelManager.findFirst<LaborerHubMission>()
        lab?.setCurrentStage(null, null, null)
        Console.showMessage("Quest reset.")
        return BaseCommand.CommandResult.SUCCESS
    }
}