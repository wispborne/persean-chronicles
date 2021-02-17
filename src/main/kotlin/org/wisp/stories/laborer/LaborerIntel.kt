package org.wisp.stories.laborer

import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.util.Misc
import org.wisp.stories.game
import wisp.questgiver.*
import wisp.questgiver.wispLib.preferredConnectedEntity

class LaborerIntel(startLocation: SectorEntityToken, endLocation: SectorEntityToken) : IntelDefinition(
    title = {
        if (!LaborerQuest.stage.isCompleted)
            game.text["lab_intel_title"]
        else
            game.text["lab_intel_title_completed"]
    },
    iconPath = { LaborerQuest.portrait.spritePath(game) },
    subtitleCreator = { info ->
        if (!LaborerQuest.stage.isCompleted) {
            info?.addPara(
                padding = 0f,
                textColor = Misc.getGrayColor()
            ) { game.text["lab_intel_subtitle"] }
        }
    },
    descriptionCreator = { info, width, _ ->
        info.addImage(
            LaborerQuest.portrait.spriteName(game),
            width,
            LaborerQuest.portrait.height,
            Padding.DESCRIPTION_PANEL
        )
        val textColor = textColorOrElseGrayIf {
            LaborerQuest.stage.isCompleted
        }
        info.addPara(
            padding = Padding.DESCRIPTION_PANEL,
            textColor = textColor
        ) {
            game.text["lab_intel_description_para1"]
        }
        info.addPara(
            padding = Padding.DESCRIPTION_PANEL,
            textColor = textColor
        ) {
            game.text["lab_intel_description_para2"]
        }

        if (LaborerQuest.stage == LaborerQuest.Stage.Completed) {
            info.addPara(
                padding = Padding.DESCRIPTION_PANEL
            ) {
                game.text["lab_intel_description_completed_para1"]
            }
        }
    },
    startLocation = startLocation.market,
    endLocation = endLocation.market,
    removeIntelIfAnyOfTheseEntitiesDie = listOf(endLocation),
    important = true,
    intelTags = listOf(Tags.INTEL_STORY)
) {
    override fun createInstanceOfSelf() =
        LaborerIntel(startLocation!!.preferredConnectedEntity!!, endLocation!!.preferredConnectedEntity!!)
}