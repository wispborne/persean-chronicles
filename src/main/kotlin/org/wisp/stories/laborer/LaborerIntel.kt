package org.wisp.stories.laborer

import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.util.Misc
import org.wisp.stories.game
import wisp.questgiver.IntelDefinition
import wisp.questgiver.Padding
import wisp.questgiver.addPara
import wisp.questgiver.wispLib.preferredConnectedEntity

class LaborerIntel(startLocation: SectorEntityToken, endLocation: SectorEntityToken) : IntelDefinition(
//    iconPath = { game.settings.getSpriteName(LaborerQuest.icon.category, LaborerQuest.icon.id) },
    title = {
        if (!LaborerQuest.stage.isCompleted)
            game.text["nirv_intel_title"]
        else
            game.text["nirv_intel_title_completed"]
    },
    subtitleCreator = { info ->
        if (!LaborerQuest.stage.isCompleted) {
            info?.addPara(
                padding = 0f,
                textColor = Misc.getGrayColor()
            ) { game.text["nirv_intel_subtitle"] }
        }
    },
    descriptionCreator = { info, width, _ ->
//        info.addImage(
//            LaborerQuest.background.spriteName(game),
//            width,
//            0f
//        )
        val textColor = textColorOrElseGrayIf {
            LaborerQuest.stage.isCompleted
        }
        info.addPara(
            padding = Padding.DESCRIPTION_PANEL,
            textColor = textColor
        ) {
            game.text["nirv_intel_description_para1"]
        }
        info.addPara(
            padding = Padding.DESCRIPTION_PANEL,
            textColor = textColor
        ) {
            game.text["nirv_intel_description_para2"]
        }

        if (LaborerQuest.stage == LaborerQuest.Stage.Completed) {
            info.addPara(
                padding = Padding.DESCRIPTION_PANEL,
                textColor = Misc.getGrayColor()
            ) {
                game.text["nirv_intel_description_completed_para1"]
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