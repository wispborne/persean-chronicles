package org.wisp.stories.dangerousGames.A_dragons

import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import wisp.questgiver.wispLib.empty
import wisp.questgiver.IntelDefinition
import wisp.questgiver.addPara

class DragonsQuest_Intel(startLocation: SectorEntityToken, endLocation: SectorEntityToken) : IntelDefinition(
    iconPath = { DragonsQuest.iconPath },
    title = {
        "Dangerous Games: The Dragonriders" +
                when (DragonsQuest.stage) {
                    DragonsQuest.Stage.FailedByAbandoning -> " - Failed"
                    DragonsQuest.Stage.Done -> " - Completed"
                    else -> String.empty
                }
    },
    subtitleCreator = { info: TooltipMakerAPI? ->
        if (DragonsQuest.stage == DragonsQuest.Stage.GoToPlanet) {
            info?.addPara {
                "Take the group to ${DragonsQuest.dragonPlanet?.name}"
            }
        } else if (DragonsQuest.stage == DragonsQuest.Stage.ReturnToStart) {
            info?.addPara {
                "Return the group back to ${startLocation.name} in ${startLocation.starSystem?.baseName}"
            }
        }
    },
    descriptionCreator = { info, width, _ ->
        val part1Color =
            if (DragonsQuest.stage > DragonsQuest.Stage.GoToPlanet) Misc.getGrayColor()
            else Misc.getTextColor()

        info.addPara(textColor = part1Color) {
            "A lively man named Karengo and a few passionate youth are traveling with you to see the draconic animals " +
                    "on ${DragonsQuest.dragonPlanet?.name} in the ${DragonsQuest.dragonPlanet?.starSystem?.baseName} system."
        }
        val part2Color =
            if (DragonsQuest.stage > DragonsQuest.Stage.ReturnToStart) Misc.getGrayColor()
            else Misc.getTextColor()

        if (DragonsQuest.stage == DragonsQuest.Stage.ReturnToStart || DragonsQuest.stage == DragonsQuest.Stage.Done) {
            info.addPara(textColor = part2Color) {
                "Return to ${startLocation.name} in ${startLocation.starSystem?.baseName}"
            }
        }

        if (DragonsQuest.stage == DragonsQuest.Stage.Done) {
            info.addPara { "The surviving men were returned home, having gotten more adventure than they bargained for." }
        }

        if (DragonsQuest.stage == DragonsQuest.Stage.FailedByAbandoning) {
            info.addPara(textColor = part2Color) {
                "You abandoned the Dragonriders to their deaths on ${startLocation.starSystem?.baseName}"
            }
        }
    },
    startLocation = startLocation.market,
    endLocation = endLocation.market,
    intelTags = listOf(
        Tags.INTEL_STORY
    )
) {
    override fun createInstanceOfSelf() = DragonsQuest_Intel(startLocation!!.planetEntity, endLocation!!.planetEntity)
}