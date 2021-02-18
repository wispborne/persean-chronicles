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
        info.appendln("Dragonriders origin planet: ${DragonsQuest.startingPlanet?.fullName} in ${DragonsQuest.startingPlanet?.starSystem?.baseName}")
        info.appendln("Dragonriders destination planet: ${DragonsQuest.dragonPlanet?.fullName} in ${DragonsQuest.dragonPlanet?.starSystem?.baseName}")
        info.appendln("Dragonriders quest stage: ${DragonsQuest.stage}")
        info.appendln()
        info.appendln("Depths origin planet: ${DepthsQuest.startingPlanet?.fullName} in ${DepthsQuest.startingPlanet?.starSystem?.baseName}")
        info.appendln("Depths destination planet: ${DepthsQuest.depthsPlanet?.fullName} in ${DepthsQuest.depthsPlanet?.starSystem?.baseName}")
        info.appendln("Depths quest stage: ${DepthsQuest.stage}")
        info.appendln()
        info.appendln("Riley destination planet: ${RileyQuest.destinationPlanet?.fullName} in ${RileyQuest.destinationPlanet?.starSystem?.baseName}")
        info.appendln("Riley quest stage: ${RileyQuest.stage}")
        info.appendln("Riley quest choices: ${RileyQuest.choices.map.entries.joinToString()}")
        info.appendln()
        info.appendln("Nirvana destination planet: ${NirvanaQuest.destPlanet?.fullName} in ${NirvanaQuest.destPlanet?.starSystem?.baseName}")
        info.appendln("Nirvana quest stage: ${NirvanaQuest.stage}")
        info.appendln()
        info.appendln("Laborer destination planet: ${LaborerQuest.destPlanet?.fullName} in ${LaborerQuest.destPlanet?.starSystem?.baseName}")
        info.appendln("Laborer quest stage: ${LaborerQuest.stage}")
        val daysUntilPayout = game.sector.scripts
            .filterIsInstance(LaborerQuest.PayoutScript::class.java)
            .firstOrNull()
            ?.intervalUtil
            ?.intervalDuration
            ?.let { game.sector.clock.convertToDays(it) }
        info.appendln("Laborer payout: ${Misc.getDGSCredits(LaborerQuest.payout.toFloat())} in $daysUntilPayout days")

        Console.showMessage(info.toString())

        return BaseCommand.CommandResult.SUCCESS
    }
}