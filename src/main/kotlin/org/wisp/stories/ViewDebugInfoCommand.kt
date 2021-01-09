package org.wisp.stories

import org.lazywizard.console.BaseCommand
import org.lazywizard.console.Console
import org.wisp.stories.dangerousGames.pt1_dragons.DragonsQuest
import org.wisp.stories.dangerousGames.pt2_depths.DepthsQuest
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
        info.appendln("Depths origin planet: ${DepthsQuest.startingPlanet?.fullName} in ${DepthsQuest.startingPlanet?.starSystem?.baseName}")
        info.appendln("Depths destination planet: ${DepthsQuest.depthsPlanet?.fullName} in ${DepthsQuest.depthsPlanet?.starSystem?.baseName}")
        info.appendln("Depths quest stage: ${DepthsQuest.stage}")
        info.appendln("Riley destination planet: ${RileyQuest.destinationPlanet?.fullName} in ${RileyQuest.destinationPlanet?.starSystem?.baseName}")
        info.appendln("Riley quest stage: ${RileyQuest.stage}")
        info.appendln("Riley quest choices: ${RileyQuest.choices.map.entries.joinToString()}")

        Console.showMessage(info.toString())

        return BaseCommand.CommandResult.SUCCESS
    }
}