package org.wisp.stories.dangerousGames.A_dragons

import com.fs.starfarer.api.campaign.PlanetAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.impl.campaign.ids.Tags
import org.wisp.stories.questLib.IntelDefinition
import org.wisp.stories.questLib.addPara
import org.wisp.stories.wispLib.empty

class DragonsQuest_Intel(startLocation: SectorEntityToken) : IntelDefinition(
    title = {
        when {
            DragonsQuest.isDragonQuestPart2Started != true -> "Take Karengo to ${DragonsQuest.dragonPlanet?.fullName}"
            DragonsQuest.didFailByLeavingOthersToGetEaten == true -> {
                "Take Karengo to ${DragonsQuest.dragonPlanet?.fullName} - Failed"
            }
            else -> {
                "Return the remaining Dragonriders back to ${startLocation.fullName}"
            }
        }

    },
    iconPath = { (DragonsQuest.dragonPlanet as? PlanetAPI)?.customEntitySpec?.iconName ?: String.empty },
    smallDescriptionCreator = { info, width, _ ->

        if (DragonsQuest.isDragonQuestPart1Complete == true)
            info.addPara {
                "A lively man named Karengo and a few passionate youth are traveling with you to see the dragons on the planet " +
                        "${DragonsQuest.dragonPlanet?.fullName} in ${DragonsQuest.dragonPlanet?.starSystem?.baseName}"
            }
        else info.addPara {
            "Return the \"Dragonriders\" back to ${startLocation.fullName} in ${startLocation.starSystem?.baseName}"
        }
    },
    startLocation = startLocation,
    intelTags = listOf(
        Tags.INTEL_STORY
    )
) {
    override fun createInstanceOfSelf() = DragonsQuest_Intel(startLocationCopy!!)
}