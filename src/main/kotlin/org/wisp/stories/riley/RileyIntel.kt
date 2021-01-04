package org.wisp.stories.riley

import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.util.Misc
import org.wisp.stories.game
import wisp.questgiver.IntelDefinition
import wisp.questgiver.Padding
import wisp.questgiver.addPara

class RileyIntel(startLocation: SectorEntityToken, endLocation: SectorEntityToken) : IntelDefinition(
    iconPath = { RileyQuest.iconPath },
    title = {
        if (RileyQuest.stage < RileyQuest.Stage.Completed)
            game.text["riley_intel_title"]
        else
            game.text["riley_intel_title_completed"]
    },
    subtitleCreator = { info ->
        info?.addPara(
            padding = Padding.SUBTITLE,
            textColor = Misc.getGrayColor()
        ) { game.text["riley_intel_subtitle"] }
        info?.addPara(
            padding = Padding.SUBTITLE,
            textColor = Misc.getGrayColor()
        ) {
            game.text.getf(
                "riley_intel_subtitle_daysLeft",
                "daysLeft" to RileyQuest.TIME_LIMIT_DAYS - daysSincePlayerVisible
            )
        }
    },
    descriptionCreator = { info, width, height ->
        info.addPara(padding = Padding.DESCRIPTION_PANEL) {
            game.text["riley_intel_description"]
        }
        info.addPara(
            padding = Padding.DESCRIPTION_PANEL,
            textColor = Misc.getGrayColor()
        ) {
            game.text.getf(
                "riley_intel_subtitle_daysLeft",
                "daysLeft" to RileyQuest.TIME_LIMIT_DAYS - daysSincePlayerVisible
            )
        }
    },
    startLocation = startLocation.market,
    endLocation = endLocation.market,
    durationInDays = RileyQuest.TIME_LIMIT_DAYS.toFloat(),
    removeIntelIfAnyOfTheseEntitiesDie = listOf(endLocation),
    intelTags = listOf(Tags.INTEL_STORY)
) {
    override fun createInstanceOfSelf() = RileyIntel(startLocation!!.planetEntity, endLocation!!.planetEntity)
}