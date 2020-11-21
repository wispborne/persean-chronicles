package org.wisp.stories.dangerousGames.pt2_depths

import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.impl.campaign.ids.Tags
import org.wisp.stories.game
import wisp.questgiver.IntelDefinition
import wisp.questgiver.wispLib.empty

class DepthsQuest_Intel(startLocation: SectorEntityToken, endLocation: SectorEntityToken) : IntelDefinition(
    title = {
        when (DepthsQuest.stage) {
            DepthsQuest.Stage.NotStarted ->
                String.empty
            DepthsQuest.Stage.GoToPlanet ->
                game.text.getf(
                    "dd_de_intel_title_stg-goToPlanet",
                    mapOf("endLocation" to endLocation.name)
                )
            DepthsQuest.Stage.ReturnToStart ->
                game.text.getf(
                    "dd_de_intel_title_stg-returnToStart",
                    mapOf(
                        "ifCrewAlive" to
                                if (!DepthsQuest.didAllCrewDie)
                                    game.text["dd_de_intel_title_stg-returnToStart_ifCrewAlive"]
                                else String.empty,
                        "startLocation" to startLocation.name
                    )
                )
            DepthsQuest.Stage.Done ->
                game.text["dd_de_intel_title_stg-done"]
        }
    },
    startLocation = startLocation.market,
    endLocation = endLocation.market,
    intelTags = listOf(Tags.INTEL_EXPLORATION, Tags.INTEL_STORY)
) {
    override fun createInstanceOfSelf(): IntelDefinition =
        DepthsQuest_Intel(startLocation!!.planetEntity, endLocation!!.planetEntity)
}