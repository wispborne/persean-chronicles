package org.wisp.stories

import com.fs.starfarer.api.util.Misc
import org.lazywizard.console.BaseCommand
import org.lazywizard.console.Console
import org.wisp.stories.dangerousGames.pt1_dragons.DragonsQuest
import org.wisp.stories.dangerousGames.pt2_depths.DepthsQuest
import org.wisp.stories.laborer.LaborerQuest
import org.wisp.stories.nirvana.NirvanaQuest
import org.wisp.stories.riley.RileyQuest

class ViewDebugInfoCommand : BaseCommand {
    override fun runCommand(args: String, context: BaseCommand.CommandContext): BaseCommand.CommandResult {
        if (!context.isCampaignAccessible) {
            return BaseCommand.CommandResult.WRONG_CONTEXT
        }

        val info = StringBuilder()

        info.appendln("Persean Chronicles Debug Info (SPOILERS)")
        info.appendln("-------")
        info.appendln("Dragonriders origin planet: ${DragonsQuest.state.startingPlanet?.fullName} in ${DragonsQuest.state.startingPlanet?.starSystem?.baseName}")
        info.appendln("Dragonriders destination planet: ${DragonsQuest.state.dragonPlanet?.fullName} in ${DragonsQuest.state.dragonPlanet?.starSystem?.baseName}")
        info.appendln("Dragonriders quest stage: ${DragonsQuest.stage}")
        info.appendln()
        info.appendln("Depths origin planet: ${DepthsQuest.state.startingPlanet?.fullName} in ${DepthsQuest.state.startingPlanet?.starSystem?.baseName}")
        info.appendln("Depths destination planet: ${DepthsQuest.state.depthsPlanet?.fullName} in ${DepthsQuest.state.depthsPlanet?.starSystem?.baseName}")
        info.appendln("Depths quest stage: ${DepthsQuest.stage}")
        info.appendln()
        info.appendln("Riley destination planet: ${RileyQuest.state.destinationPlanet?.fullName} in ${RileyQuest.state.destinationPlanet?.starSystem?.baseName}")
        info.appendln("Riley quest stage: ${RileyQuest.stage}")
        info.appendln("Riley quest choices: ${RileyQuest.choices.map.entries.joinToString()}")
        info.appendln()
        info.appendln("Nirvana destination planet: ${NirvanaQuest.state.destPlanet?.fullName} in ${NirvanaQuest.state.destPlanet?.starSystem?.baseName}")
        info.appendln("Nirvana quest stage: ${NirvanaQuest.stage}")
        info.appendln()
        info.appendln("Laborer destination planet: ${LaborerQuest.state.destPlanet?.fullName} in ${LaborerQuest.state.destPlanet?.starSystem?.baseName}")
        info.appendln("Laborer quest stage: ${LaborerQuest.stage}")
        val daysUntilPayout = game.sector.scripts
            .filterIsInstance(LaborerQuest.PayoutScript::class.java)
            .firstOrNull()
            ?.intervalUtil
            ?.intervalDuration
            ?.let { game.sector.clock.convertToDays(it) }
        info.appendln("Laborer payout: ${Misc.getDGSCredits(LaborerQuest.state.payout.toFloat())} in $daysUntilPayout days")

        Console.showMessage(info.toString())

        return BaseCommand.CommandResult.SUCCESS
    }
}