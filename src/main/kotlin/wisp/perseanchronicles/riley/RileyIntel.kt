package wisp.perseanchronicles.riley

import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.util.Misc
import wisp.perseanchronicles.game
import wisp.questgiver.*
import wisp.questgiver.wispLib.asList
import wisp.questgiver.wispLib.preferredConnectedEntity

class RileyIntel(startLocation: SectorEntityToken?, endLocation: SectorEntityToken?) : IntelDefinition(
    iconPath = { game.settings.getSpriteName(RileyQuest.icon.category, RileyQuest.icon.id) },
    title = {
        if (RileyQuest.stage.progress != AutoQuestFacilitator.Stage.Progress.Completed)
            game.text["riley_intel_title"]
        else
            game.text["riley_intel_title_completed"]
    },
    subtitleCreator = { info ->
        if (RileyQuest.stage.progress != AutoQuestFacilitator.Stage.Progress.Completed) {
            bullet(info)
            info.addPara(
                padding = 0f,
                textColor = Misc.getGrayColor()
            ) { game.text["riley_intel_subtitle"] }
            info.addPara(
                padding = 0f,
                textColor = Misc.getGrayColor()
            ) {
                game.text.getf(
                    "riley_intel_subtitle_daysLeft",
                    "daysLeft" to (RileyQuest.TIME_LIMIT_DAYS - daysSincePlayerVisible).toInt()
                )
            }
        }
    },
    descriptionCreator = { info, width, _ ->
        info.addImage(
            RileyQuest.icon.spriteName(game),
            width,
            128f,
            Padding.DESCRIPTION_PANEL
        )
        info.addPara(
            padding = Padding.DESCRIPTION_PANEL,
            textColor = textColorOrElseGrayIf { RileyQuest.stage.progress == AutoQuestFacilitator.Stage.Progress.Completed }) {
            game.text["riley_intel_description"]
        }

        if (!RileyQuest.stage.isCompleted) {
            info.addPara(
                padding = Padding.DESCRIPTION_PANEL
            ) {
                game.text.getf(
                    "riley_intel_subtitle_daysLeft",
                    "daysLeft" to (RileyQuest.TIME_LIMIT_DAYS - daysSincePlayerVisible).toInt()
                )
            }
        }

        if (RileyQuest.stage.isCompleted) {
            when {
                RileyQuest.choices.destroyedTheCore == true -> {
                    info.addPara(padding = Padding.DESCRIPTION_PANEL) {
                        game.text["riley_intel_description_done_destroyed"]
                    }
                }
                RileyQuest.choices.turnedInForABounty == true -> {
                    info.addPara(padding = Padding.DESCRIPTION_PANEL) {
                        game.text["riley_intel_description_done_bounty"]
                    }
                }
                RileyQuest.choices.leftRileyWithFather == true -> {
                    info.addPara(padding = Padding.DESCRIPTION_PANEL) {
                        game.text["riley_intel_description_done_leftAlone"]
                    }
                }
            }
        }
    },
    startLocation = startLocation?.market,
    endLocation = endLocation?.market,
    durationInDays = RileyQuest.TIME_LIMIT_DAYS.toFloat(),
    removeIntelIfAnyOfTheseEntitiesDie = endLocation.asList(),
    important = true,
    intelTags = listOf(Tags.INTEL_STORY, Tags.INTEL_ACCEPTED)
) {
    override fun createInstanceOfSelf() =
        RileyIntel(startLocation?.preferredConnectedEntity, endLocation?.preferredConnectedEntity)
}