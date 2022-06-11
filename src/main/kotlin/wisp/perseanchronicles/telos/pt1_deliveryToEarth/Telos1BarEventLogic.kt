package wisp.perseanchronicles.telos.pt1_deliveryToEarth

import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEventCreator
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithBarEvent
import com.fs.starfarer.api.util.Misc
import org.json.JSONObject
import wisp.perseanchronicles.game
import wisp.questgiver.spritePath
import wisp.questgiver.v2.BarEvent
import wisp.questgiver.v2.BarEventLogic
import wisp.questgiver.v2.json.InteractionPromptFromJson
import wisp.questgiver.v2.json.PagesFromJson
import wisp.questgiver.v2.json.TextToStartInteractionFromJson
import wisp.questgiver.v2.json.query

class Telos1BarEventLogic(
    stageJson: JSONObject = Telos1HubMission.json.query("/stages/0") as JSONObject
) : BarEventLogic<Telos1BarEventLogic>(
    createInteractionPrompt = InteractionPromptFromJson(stageJson = stageJson),
    onInteractionStarted = {
        dialog.visualPanel.showMapMarker(
            Telos1HubMission.state.karengoPlanet,
            TextToStartInteractionFromJson<Telos1BarEventLogic>(stageJson = stageJson).invoke(this),
            Telos1HubMission.state.karengoPlanet?.indicatorColor ?: Misc.getTextColor(),
            true,
            Telos1HubMission.icon.spritePath(game = game),
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
                Telos1HubMission.start(startLocation = this.dialog.interactionTarget)
                it.close(doNotOfferAgain = true)
            },
            "decline" to {
                it.close(doNotOfferAgain = false)
            }
        )
    ),
    people = listOf(Telos1HubMission.stage1Engineer)
)

class Telos1BarEvent : BarEvent<Telos1BarEventLogic>(Telos1HubMission.missionId) {
    override fun createBarEventLogic(): BarEventLogic<Telos1BarEventLogic> =
        Telos1BarEventLogic()

    override fun createMission(): HubMissionWithBarEvent = Telos1HubMission
}

class Telos1BarEventCreator : BaseBarEventCreator() {
    override fun createBarEvent() = Telos1BarEvent()

    override fun isPriority(): Boolean {
        return true // todo remove
    }
}