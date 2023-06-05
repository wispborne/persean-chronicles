package wisp.perseanchronicles.commands

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.util.Misc
import org.lazywizard.console.BaseCommand
import org.lazywizard.console.Console
import wisp.perseanchronicles.dangerousGames.pt1_dragons.DragonsHubMission
import wisp.perseanchronicles.dangerousGames.pt2_depths.DepthsHubMission
import wisp.perseanchronicles.game
import wisp.perseanchronicles.laborer.LaborerHubMission
import wisp.perseanchronicles.laborer.PayoutScript
import wisp.perseanchronicles.nirvana.NirvanaHubMission
import wisp.perseanchronicles.riley.RileyHubMission
import wisp.perseanchronicles.telos.pt1_deliveryToEarth.Telos1HubMission
import wisp.perseanchronicles.telos.pt2_dart.Telos2HubMission
import wisp.perseanchronicles.telos.pt3_arrow.Telos3HubMission
import wisp.questgiver.wispLib.findFirst

class ViewDebugInfoCommand : BaseCommand {
    override fun runCommand(args: String, context: BaseCommand.CommandContext): BaseCommand.CommandResult {
        if (!context.isCampaignAccessible) {
            return BaseCommand.CommandResult.WRONG_CONTEXT
        }

        val info = StringBuilder()

        info.appendLine("Persean Chronicles Debug Info (SPOILERS)")
        info.appendLine("-------")
        val dragons = Global.getSector().intelManager.findFirst<DragonsHubMission>()
        info.appendLine("Dragonriders origin planet: ${DragonsHubMission.state.startingPlanet?.fullName} in ${DragonsHubMission.state.startingPlanet?.starSystem?.baseName}")
        info.appendLine("Dragonriders destination planet: ${DragonsHubMission.state.dragonPlanet?.fullName} in ${DragonsHubMission.state.dragonPlanet?.starSystem?.baseName}")
        info.appendLine("Dragonriders quest stage: ${dragons?.currentStage}")
        info.appendLine()
        val depths = Global.getSector().intelManager.findFirst<DepthsHubMission>()
        info.appendLine("Depths origin planet: ${DepthsHubMission.state.startingPlanet?.fullName} in ${DepthsHubMission.state.startingPlanet?.starSystem?.baseName}")
        info.appendLine("Depths destination planet: ${DepthsHubMission.state.depthsPlanet?.fullName} in ${DepthsHubMission.state.depthsPlanet?.starSystem?.baseName}")
        info.appendLine("Depths quest stage: ${depths?.currentStage}")
        info.appendLine()
        val riley = Global.getSector().intelManager.findFirst<RileyHubMission>()
        info.appendLine("Riley start planet: ${RileyHubMission.state.startLocation?.fullName} in ${RileyHubMission.state.startLocation?.starSystem?.baseName}")
        info.appendLine("Riley destination planet: ${RileyHubMission.state.destinationPlanet?.fullName} in ${RileyHubMission.state.destinationPlanet?.starSystem?.baseName}")
        info.appendLine("Riley quest stage: ${riley?.currentStage}")
        info.appendLine("Riley quest choices: ${riley?.choices?.map?.entries?.joinToString()}")
        info.appendLine()
        val nirv = Global.getSector().intelManager.findFirst<NirvanaHubMission>()
        info.appendLine("Nirvana destination planet: ${NirvanaHubMission.state.destPlanet?.fullName} in ${NirvanaHubMission.state.destPlanet?.starSystem?.baseName}")
        info.appendLine("Nirvana quest stage: ${nirv?.currentStage}")
        info.appendLine()
        val lab = Global.getSector().intelManager.findFirst<LaborerHubMission>()
        info.appendLine("Laborer destination planet: ${LaborerHubMission.state.destPlanet?.fullName} in ${LaborerHubMission.state.destPlanet?.starSystem?.baseName}")
        info.appendLine("Laborer quest stage: ${lab?.currentStage}")
        val daysUntilPayout = game.sector.scripts
            .filterIsInstance(PayoutScript::class.java)
            .firstOrNull()
            ?.intervalUtil
            ?.intervalDuration
            ?.let { game.sector.clock.convertToDays(it) }
        info.appendLine("Laborer payout: ${Misc.getDGSCredits(LaborerHubMission.state.payout.toFloat())} in $daysUntilPayout days")
        info.appendLine()

        val telos1 = Global.getSector().intelManager.findFirst<Telos1HubMission>()
        info.appendLine("Telos Pt.1 source planet: ${Telos1HubMission.state.startLocation?.fullName} in ${Telos1HubMission.state.startLocation?.starSystem?.baseName}")
        info.appendLine("Telos Pt.1+2 destination planet: ${Telos1HubMission.state.karengoPlanet?.fullName} in ${Telos1HubMission.state.karengoPlanet?.starSystem?.baseName}")
        info.appendLine("Telos Pt.1 quest stage: ${telos1?.currentStage}, state: ${Telos1HubMission.state.map.toList()}")
        val telos2 = Global.getSector().intelManager.findFirst<Telos2HubMission>()
        info.appendLine("Telos Pt.2 quest stage: ${telos2?.currentStage}, state: ${Telos2HubMission.state.map.toList()}")
        val telos3 = Global.getSector().intelManager.findFirst<Telos3HubMission>()
        info.appendLine("Telos Pt.3 quest stage: ${telos3?.currentStage}, state: ${Telos3HubMission.state.map.toList()}")

        Console.showMessage(info.toString())

        return BaseCommand.CommandResult.SUCCESS
    }
}