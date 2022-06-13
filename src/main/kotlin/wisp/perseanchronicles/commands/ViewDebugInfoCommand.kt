package wisp.perseanchronicles.commands

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.util.Misc
import org.lazywizard.console.BaseCommand
import org.lazywizard.console.Console
import wisp.perseanchronicles.dangerousGames.pt1_dragons.DragonsQuest
import wisp.perseanchronicles.dangerousGames.pt2_depths.DepthsQuest
import wisp.perseanchronicles.game
import wisp.perseanchronicles.laborer.LaborerQuest
import wisp.perseanchronicles.nirvana.NirvanaQuest
import wisp.perseanchronicles.riley.RileyQuest
import wisp.perseanchronicles.telos.pt1_deliveryToEarth.Telos1HubMission
import wisp.questgiver.wispLib.findFirst

class ViewDebugInfoCommand : BaseCommand {
    override fun runCommand(args: String, context: BaseCommand.CommandContext): BaseCommand.CommandResult {
        if (!context.isCampaignAccessible) {
            return BaseCommand.CommandResult.WRONG_CONTEXT
        }

        val info = StringBuilder()

        info.appendLine("Persean Chronicles Debug Info (SPOILERS)")
        info.appendLine("-------")
        info.appendLine("Dragonriders origin planet: ${DragonsQuest.state.startingPlanet?.fullName} in ${DragonsQuest.state.startingPlanet?.starSystem?.baseName}")
        info.appendLine("Dragonriders destination planet: ${DragonsQuest.state.dragonPlanet?.fullName} in ${DragonsQuest.state.dragonPlanet?.starSystem?.baseName}")
        info.appendLine("Dragonriders quest stage: ${DragonsQuest.stage}")
        info.appendLine()
        info.appendLine("Depths origin planet: ${DepthsQuest.state.startingPlanet?.fullName} in ${DepthsQuest.state.startingPlanet?.starSystem?.baseName}")
        info.appendLine("Depths destination planet: ${DepthsQuest.state.depthsPlanet?.fullName} in ${DepthsQuest.state.depthsPlanet?.starSystem?.baseName}")
        info.appendLine("Depths quest stage: ${DepthsQuest.stage}")
        info.appendLine()
        info.appendLine("Riley destination planet: ${RileyQuest.state.destinationPlanet?.fullName} in ${RileyQuest.state.destinationPlanet?.starSystem?.baseName}")
        info.appendLine("Riley quest stage: ${RileyQuest.stage}")
        info.appendLine("Riley quest choices: ${RileyQuest.choices.map.entries.joinToString()}")
        info.appendLine()
        info.appendLine("Nirvana destination planet: ${NirvanaQuest.state.destPlanet?.fullName} in ${NirvanaQuest.state.destPlanet?.starSystem?.baseName}")
        info.appendLine("Nirvana quest stage: ${NirvanaQuest.stage}")
        info.appendLine()
        info.appendLine("Laborer destination planet: ${LaborerQuest.state.destPlanet?.fullName} in ${LaborerQuest.state.destPlanet?.starSystem?.baseName}")
        info.appendLine("Laborer quest stage: ${LaborerQuest.stage}")
        val daysUntilPayout = game.sector.scripts
            .filterIsInstance(LaborerQuest.PayoutScript::class.java)
            .firstOrNull()
            ?.intervalUtil
            ?.intervalDuration
            ?.let { game.sector.clock.convertToDays(it) }
        info.appendLine("Laborer payout: ${Misc.getDGSCredits(LaborerQuest.state.payout.toFloat())} in $daysUntilPayout days")
        info.appendLine()

        val telos1 = Global.getSector().intelManager.findFirst<Telos1HubMission>()
        info.appendLine("Telos source planet: ${Telos1HubMission.state.startLocation?.fullName} in ${Telos1HubMission.state.startLocation?.starSystem?.baseName}")
        info.appendLine("Telos destination planet: ${Telos1HubMission.state.karengoPlanet?.fullName} in ${Telos1HubMission.state.karengoPlanet?.starSystem?.baseName}")
        info.appendLine("Telos quest stage: ${telos1?.currentStage}")

        Console.showMessage(info.toString())

        return BaseCommand.CommandResult.SUCCESS
    }
}