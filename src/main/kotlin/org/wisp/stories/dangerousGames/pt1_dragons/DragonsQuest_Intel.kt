package org.wisp.stories.dangerousGames.pt1_dragons

import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.wisp.stories.game
import wisp.questgiver.IntelDefinition
import wisp.questgiver.addPara
import wisp.questgiver.wispLib.empty

class DragonsQuest_Intel(startLocation: SectorEntityToken, endLocation: SectorEntityToken) : IntelDefinition(
    iconPath = { DragonsQuest.iconPath },
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
            info?.addPara {
                game.text.getf(
                    "dg_dr_intel_subtitle_stg-goToPlanet",
                    mapOf("dragonPlanet" to DragonsQuest.dragonPlanet?.name)
                )
            }
        } else if (DragonsQuest.stage == DragonsQuest.Stage.ReturnToStart) {
            info?.addPara {
                game.text.getf(
                    "dg_dr_intel_subtitle_stg-returnToStart",
                    mapOf(
                        "startPlanet" to startLocation.name,
                        "startSystem" to startLocation.starSystem?.baseName
                    )
                )
            }
        }
    },
    descriptionCreator = { info, width, _ ->
        val part1Color =
            if (DragonsQuest.stage > DragonsQuest.Stage.GoToPlanet) Misc.getGrayColor()
            else Misc.getTextColor()

        info.addPara(textColor = part1Color) {
            game.text.getf(
                "dg_dr_intel_desc_para1",
                mapOf(
                    "dragonSystem" to DragonsQuest.dragonPlanet?.starSystem?.baseName
                )
            )
        }
        val part2Color =
            if (DragonsQuest.stage > DragonsQuest.Stage.ReturnToStart) Misc.getGrayColor()
            else Misc.getTextColor()

        if (DragonsQuest.stage == DragonsQuest.Stage.ReturnToStart || DragonsQuest.stage == DragonsQuest.Stage.Done) {
            info.addPara(textColor = part2Color) {
                game.text.getf(
                    "dg_dr_intel_desc_stg-returnToStart",
                    mapOf(
                        "startPlanet" to startLocation.name,
                        "startSystem" to startLocation.starSystem?.baseName
                    )
                )
            }
        }

        if (DragonsQuest.stage == DragonsQuest.Stage.Done) {
            info.addPara { game.text["dg_dr_intel_desc_stg-done"] }
        }

        if (DragonsQuest.stage == DragonsQuest.Stage.FailedByAbandoning) {
            info.addPara(textColor = part2Color) {
                game.text["dg_dr_intel_desc_stg-failedByAbandon"]
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