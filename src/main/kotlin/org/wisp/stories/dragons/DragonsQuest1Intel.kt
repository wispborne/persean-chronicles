package org.wisp.stories.dragons

import com.fs.starfarer.api.campaign.PlanetAPI
import com.fs.starfarer.api.impl.campaign.ids.Tags
import org.wisp.stories.questLib.IntelDefinition
import org.wisp.stories.questLib.addPara
import org.wisp.stories.wispLib.empty

class DragonsQuest1Intel : IntelDefinition(
    title = { "Take Karengo to ${DragonsQuest.dragonPlanet?.fullName}" },
    iconPath = { (DragonsQuest.dragonPlanet as? PlanetAPI)?.customEntitySpec?.iconName ?: String.empty },
    smallDescriptionCreator = { info, width, _ ->
        info.addPara {
            "A lively man named Karengo and a few passionate youth are traveling with you to see the dragons on the planet " +
                    "${DragonsQuest.dragonPlanet?.fullName} in ${DragonsQuest.dragonPlanet?.starSystem?.baseName}"
        }
    },
    intelTags = listOf(
        Tags.INTEL_STORY
    )
) {
    override fun createInstanceOfSelf() = DragonsQuest1Intel()
}