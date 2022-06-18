package wisp.perseanchronicles.telos.pt1_deliveryToEarth

import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.util.Misc
import org.json.JSONObject
import wisp.questgiver.v2.BarEventLogic
import wisp.questgiver.v2.json.InteractionPromptFromJson
import wisp.questgiver.v2.json.PagesFromJson
import wisp.questgiver.v2.json.TextToStartInteractionFromJson
import wisp.questgiver.v2.json.query

class Telos1BarEventLogic(
    stageJson: JSONObject = Telos1HubMission.part1Json.query("/stages/deliveryToEarth")
) : BarEventLogic<Telos1HubMission>(
    createInteractionPrompt = InteractionPromptFromJson(barEventJson = stageJson.getJSONObject("barEvent")),
    onInteractionStarted = {
        dialog.visualPanel.showMapMarker(
            Telos1HubMission.state.karengoSystem?.hyperspaceAnchor,
            TextToStartInteractionFromJson<Telos1BarEventLogic>(barEventJson = stageJson.getJSONObject("barEvent")).invoke(
                this as Telos1BarEventLogic
            ),
            Misc.getTextColor(),
            true,
            mission.icon,
            null,
            Telos1HubMission.tags.minus(Tags.INTEL_ACCEPTED).toSet()
        )
    },
    textToStartInteraction = TextToStartInteractionFromJson(barEventJson = stageJson.getJSONObject("barEvent")),
    pages = PagesFromJson(
        pagesJson = stageJson.getJSONArray("pages"),
        onPageShownHandlersByPageId = emptyMap(),
        onOptionSelectedHandlersByOptionId = mapOf(
            "done" to {
                mission.accept(this.dialog, null)
                it.close(doNotOfferAgain = true)
            },
            "decline" to {
                it.close(doNotOfferAgain = false)
            }
        )
    ),
    people = { listOf(mission.stage1Engineer) }
)