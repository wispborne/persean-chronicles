package wisp.perseanchronicles.riley

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager
import com.fs.starfarer.api.impl.campaign.intel.bar.events.ScientistAICoreBarEvent
import com.fs.starfarer.api.impl.campaign.intel.bar.events.ScientistAICoreIntel
import org.lazywizard.console.BaseCommand
import org.lazywizard.console.Console
import wisp.perseanchronicles.common.PerseanChroniclesNPCs
import wisp.perseanchronicles.game
import wisp.questgiver.wispLib.findFirst

class ResetRileyQuestCommand : BaseCommand {
    override fun runCommand(args: String, context: BaseCommand.CommandContext): BaseCommand.CommandResult {
        if (!context.isCampaignAccessible) {
            return BaseCommand.CommandResult.WRONG_CONTEXT
        }

        runCatching { RileyHubMission.choices.map.clear() }.onFailure { game.logger.w(it) }
        runCatching { RileyHubMission.state.map.clear() }.onFailure { game.logger.w(it) }
        PerseanChroniclesNPCs.isRileyInFleet = false

        runCatching {
            val mission: RileyHubMission = game.intelManager.findFirst()!!
            mission.setCurrentStage(RileyHubMission.Stage.NotStarted, null, null)
        }.onFailure { game.logger.w(it) }

        Console.showMessage("Quest reset.")

        val event: ScientistAICoreIntel =  Global.getSector().getIntelManager().getFirstIntel(
            ScientistAICoreIntel::class.java) as ScientistAICoreIntel
        BarEventManager.getInstance().active.items.filterIsInstance<ScientistAICoreBarEvent>().firstOrNull()?.person

            return BaseCommand.CommandResult.SUCCESS
    }
}