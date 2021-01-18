package org.wisp.stories.dangerousGames.pt2_depths

import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.util.Misc
import org.wisp.stories.game
import wisp.questgiver.IntelDefinition
import wisp.questgiver.Padding
import wisp.questgiver.addPara
import wisp.questgiver.wispLib.empty
import wisp.questgiver.wispLib.preferredConnectedEntity
import wisp.questgiver.wispLib.spriteName

class DepthsQuest_Intel(startLocation: SectorEntityToken, endLocation: SectorEntityToken) : IntelDefinition(
    title = {
        if (DepthsQuest.stage != DepthsQuest.Stage.Done) {
            game.text["dg_de_intel_title"]
        } else {
            game.text["dg_de_intel_title_completed"]
        }
    },
    iconPath = { DepthsQuest.icon.spriteName(game) },
    subtitleCreator = { info ->
        when (DepthsQuest.stage) {
            DepthsQuest.Stage.GoToPlanet ->
                game.text["dg_de_intel_subtitle_stg1"]
            DepthsQuest.Stage.ReturnToStart ->
                game.text.getf(
                    "dg_de_intel_subtitle_stg2",
                    "ifCrewAlive" to
                            if (!DepthsQuest.Stage2.didAllCrewDie)
                                game.text["dg_de_intel_subtitle_stg2_ifCrewAlive"]
                            else String.empty
                )
            else -> null
        }
            ?.also { text ->
                info?.addPara(
                    padding = Padding.SUBTITLE,
                    textColor = Misc.getGrayColor()
                ) { text }
            }
    },
    descriptionCreator = { info, width, _ ->
        val stg1TextColor = if (DepthsQuest.stage > DepthsQuest.Stage.GoToPlanet) Misc.getGrayColor()
        else Misc.getTextColor()

        info.addPara(textColor = stg1TextColor) { game.text["dg_de_intel_desc_stg1"] }

        if (DepthsQuest.stage >= DepthsQuest.Stage.ReturnToStart) {
            val stg2TextColor = if (DepthsQuest.stage > DepthsQuest.Stage.ReturnToStart) Misc.getGrayColor()
            else Misc.getTextColor()

            info.addPara(textColor = stg2TextColor) { game.text["dg_de_intel_desc_stg2"] }
        }

        if (DepthsQuest.stage == DepthsQuest.Stage.Done) {
            info.addPara { game.text["dg_de_intel_desc_stg3"] }
        }
    },
    startLocation = startLocation.market,
    endLocation = endLocation.market,
    important = true,
    intelTags = listOf(Tags.INTEL_EXPLORATION, Tags.INTEL_STORY)
) {
    override fun createInstanceOfSelf(): IntelDefinition =
        DepthsQuest_Intel(startLocation!!.preferredConnectedEntity!!, endLocation!!.preferredConnectedEntity!!)
}