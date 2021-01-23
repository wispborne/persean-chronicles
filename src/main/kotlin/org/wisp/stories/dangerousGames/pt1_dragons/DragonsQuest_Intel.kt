package org.wisp.stories.dangerousGames.pt1_dragons

import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.wisp.stories.game
import wisp.questgiver.IntelDefinition
import wisp.questgiver.Padding
import wisp.questgiver.addPara
import wisp.questgiver.spriteName
import wisp.questgiver.wispLib.empty
import wisp.questgiver.wispLib.preferredConnectedEntity

class DragonsQuest_Intel(startLocation: SectorEntityToken, endLocation: SectorEntityToken) : IntelDefinition(
    iconPath = { DragonsQuest.icon.spriteName((game)) },
    title = {
        game.text["dg_dr_intel_title"] +
                when (DragonsQuest.stage) {
                    DragonsQuest.Stage.FailedByAbandoning -> game.text["dg_dr_intel_title_failed"]
                    DragonsQuest.Stage.Done -> game.text["dg_dr_intel_title_completed"]
                    else -> String.empty
                }
    },
    subtitleCreator = { info: TooltipMakerAPI? ->
        if (DragonsQuest.stage == DragonsQuest.Stage.GoToPlanet) {
            info?.addPara(
                padding = Padding.SUBTITLE,
                textColor = Misc.getGrayColor()
            ) {
                game.text["dg_dr_intel_subtitle_stg1"]
            }
        } else if (DragonsQuest.stage == DragonsQuest.Stage.ReturnToStart) {
            info?.addPara(
                padding = Padding.SUBTITLE,
                textColor = Misc.getGrayColor()
            ) {
                game.text["dg_dr_intel_subtitle_stg2"]
            }
        }
    },
    descriptionCreator = { info, width, _ ->
        val part1Color =
            if (DragonsQuest.stage > DragonsQuest.Stage.GoToPlanet) Misc.getGrayColor()
            else Misc.getTextColor()

        info.addImage(DragonsQuest.intelDetailHeaderImage.spriteName(game), width, Padding.DESCRIPTION_PANEL)
        info.addPara(textColor = part1Color) {
            game.text["dg_dr_intel_desc_para1"]
        }
        val part2Color =
            if (DragonsQuest.stage > DragonsQuest.Stage.ReturnToStart) Misc.getGrayColor()
            else Misc.getTextColor()

        if (DragonsQuest.stage == DragonsQuest.Stage.ReturnToStart || DragonsQuest.stage == DragonsQuest.Stage.Done) {
            info.addPara(textColor = part2Color) {
                game.text["dg_dr_intel_desc_stg2"]
            }
        }

        if (DragonsQuest.stage == DragonsQuest.Stage.Done) {
            info.addPara { game.text["dg_dr_intel_desc_stg3"] }
        }

        if (DragonsQuest.stage == DragonsQuest.Stage.FailedByAbandoning) {
            info.addPara(textColor = part2Color) {
                game.text["dg_dr_intel_desc_stg-failedByAbandon"]
            }
        }
    },
    startLocation = startLocation.market,
    endLocation = endLocation.market,
    important = true,
    intelTags = listOf(
        Tags.INTEL_STORY
    )
) {
    override fun createInstanceOfSelf() =
        DragonsQuest_Intel(startLocation!!.preferredConnectedEntity!!, endLocation!!.preferredConnectedEntity!!)
}