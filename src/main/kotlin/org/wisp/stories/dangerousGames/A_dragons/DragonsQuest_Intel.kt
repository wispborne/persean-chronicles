package org.wisp.stories.dangerousGames.A_dragons

import com.fs.starfarer.api.campaign.PlanetAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.wisp.stories.questLib.IntelDefinition
import org.wisp.stories.questLib.addPara
import org.wisp.stories.wispLib.empty

class DragonsQuest_Intel(startLocation: SectorEntityToken) : IntelDefinition(
    iconPath = { (DragonsQuest.dragonPlanet as? PlanetAPI)?.customEntitySpec?.iconName ?: String.empty },
    title = {
        "Dangerous Games: The Dragonriders" +
                when (DragonsQuest.stage) {
                    DragonsQuest.Stage.FailedByAbandoning -> " - Failed"
                    DragonsQuest.Stage.Done -> " - Done"
                    else -> String.empty
                }
    },
    subtitleCreator = { info: TooltipMakerAPI? ->

        if (DragonsQuest.stage == DragonsQuest.Stage.GoToPlanet) {
            info?.addPara {
                "Take the group to ${DragonsQuest.dragonPlanet?.fullName}"
            }
        } else if (DragonsQuest.stage == DragonsQuest.Stage.ReturnToStart) {
            info?.addPara {
                "Return the group back to ${startLocation.fullName} in ${startLocation.starSystem?.baseName}"
            }
        }
    },
    descriptionCreator = { info, width, _ ->
        val part1Color =
            if (DragonsQuest.stage != DragonsQuest.Stage.GoToPlanet) Misc.getGrayColor()
            else Misc.getTextColor()

        info.addPara(textColor = part1Color) {
            "A lively man named Karengo and a few passionate youth are traveling with you to see the draconic animals " +
                    "on \"${DragonsQuest.dragonPlanet?.fullName} in the ${DragonsQuest.dragonPlanet?.starSystem?.baseName} system."
        }

        if (DragonsQuest.stage == DragonsQuest.Stage.ReturnToStart) {
            info.addPara {
                "Return to ${startLocation.fullName} in ${startLocation.starSystem?.baseName}"
            }
        }
    },
    startLocation = startLocation,
    intelTags = listOf(
        Tags.INTEL_STORY
    )
) {
    override fun createInstanceOfSelf() = DragonsQuest_Intel(startLocationCopy!!)
}