package wisp.perseanchronicles.telos.pt1_deliveryToEarth

import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEventCreator
import com.fs.starfarer.api.util.Misc
import org.json.JSONObject
import wisp.questgiver.v2.BarEvent
import wisp.questgiver.v2.BarEventLogic
import wisp.questgiver.v2.json.InteractionPromptFromJson
import wisp.questgiver.v2.json.PagesFromJson
import wisp.questgiver.v2.json.TextToStartInteractionFromJson
import wisp.questgiver.v2.json.query

class Telos1BarEventLogic(
    stageJson: JSONObject = Telos1HubMission.json.query("/stages/0") as JSONObject
) : BarEventLogic<Telos1HubMission>(
    createInteractionPrompt = InteractionPromptFromJson(stageJson = stageJson),
    onInteractionStarted = {
        dialog.visualPanel.showMapMarker(
            mission.state.destLocation,
            TextToStartInteractionFromJson<Telos1BarEventLogic>(stageJson = stageJson).invoke(this as Telos1BarEventLogic),
            Misc.getTextColor(),
            true,
            mission.icon,
            null,
            Telos1HubMission.tags.minus(Tags.INTEL_ACCEPTED).toSet()
        )
    },
    textToStartInteraction = TextToStartInteractionFromJson(stageJson = stageJson),
    pages = PagesFromJson(
        pagesJson = stageJson.getJSONArray("pages"),
        onPageShownHandlersByPageId = emptyMap(),
        onOptionSelectedHandlersByOptionId = mapOf(
            "accept" to {
                mission.accept(this.dialog, null)
                it.close(doNotOfferAgain = true)
            },
            "decline" to {
                it.close(doNotOfferAgain = false)
            }
        )
    ),
    people = listOf(Telos1HubMission.stage1Engineer)
)

class Telos1BarEvent : BarEvent<Telos1HubMission>(Telos1HubMission.MISSION_ID) {
    override fun createBarEventLogic(): BarEventLogic<Telos1HubMission> =
        Telos1BarEventLogic()

    override fun createMission() = Telos1HubMission()
}

class Telos1BarEventCreator : BaseBarEventCreator() {
    override fun createBarEvent() = Telos1BarEvent()

    override fun isPriority(): Boolean {
        return true // todo remove
    }
}