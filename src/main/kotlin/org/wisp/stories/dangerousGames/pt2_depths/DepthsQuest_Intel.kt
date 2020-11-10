package org.wisp.stories.dangerousGames.pt2_depths

import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.impl.campaign.ids.Tags
import wisp.questgiver.wispLib.empty
import wisp.questgiver.IntelDefinition

class DepthsQuest_Intel(startLocation: SectorEntityToken, endLocation: SectorEntityToken) : IntelDefinition(
    title = {
        when (DepthsQuest.stage) {
            DepthsQuest.Stage.NotStarted -> String.empty
            DepthsQuest.Stage.GoToPlanet -> "Bring Karengo and crew to ${endLocation.name}"
            DepthsQuest.Stage.ReturnToStart -> {
                "Bring Karengo ${if (!DepthsQuest.didAllCrewDie) "and crew " else String.empty}" +
                        "back to ${startLocation.name}"
            }
            DepthsQuest.Stage.Done -> "Finished: Brought Karengo and crew to search for underwater treasure."
        }
    },
    startLocation = startLocation.market,
    endLocation = endLocation.market,
    intelTags = listOf(Tags.INTEL_EXPLORATION, Tags.INTEL_STORY)
) {
    override fun createInstanceOfSelf(): IntelDefinition =
        DepthsQuest_Intel(startLocation!!.planetEntity, endLocation!!.planetEntity)
}