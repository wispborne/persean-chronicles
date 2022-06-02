package wisp.perseanchronicles.telos.pt1_deliveryToEarth

import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarEvent
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEventCreator
import com.fs.starfarer.api.util.Misc
import org.json.JSONObject
import wisp.perseanchronicles.game
import wisp.perseanchronicles.telos.TelosQuest
import wisp.questgiver.AutoBarEventDefinition
import wisp.questgiver.json.InteractionPromptFromJson
import wisp.questgiver.json.PagesFromJson
import wisp.questgiver.json.TextToStartInteractionFromJson
import wisp.questgiver.spritePath

class Telos_Stage1_BarEvent(
    stageJson: JSONObject = TelosQuest.json.getJSONArray("stages").getJSONObject(0)
) : AutoBarEventDefinition<Telos_Stage1_BarEvent>(
    questFacilitator = TelosQuest,
    createInteractionPrompt = InteractionPromptFromJson(stageJson = stageJson),
    onInteractionStarted = {
        dialog.visualPanel.showMapMarker(
            TelosQuest.state.destPlanet,
            TextToStartInteractionFromJson<Telos_Stage1_BarEvent>(stageJson = stageJson).invoke(this),
            TelosQuest.state.destPlanet?.indicatorColor ?: Misc.getTextColor(),
            true,
            TelosQuest.icon.spritePath(game = game),
            null,
            TelosQuest.tags.minus(Tags.INTEL_ACCEPTED).toSet()
        )
    },
    textToStartInteraction = TextToStartInteractionFromJson(stageJson = stageJson),
    pages = PagesFromJson(
        pagesJson = stageJson.getJSONArray("pages"),
        onPageShownHandlersByPageId = emptyMap(),
        onOptionSelectedHandlersByPageId = mapOf(
            "accept" to {
                it.close(doNotOfferAgain = true)
            }
        )
    ),
    people = listOf(TelosQuest.stage1Engineer)
) {
    override fun createInstanceOfSelf() = Telos_Stage1_BarEvent()
}

class Telos_Stage1_BarEventCreator : BaseBarEventCreator() {
    override fun isPriority(): Boolean = true

    override fun createBarEvent(): PortsideBarEvent = Telos_Stage1_BarEvent().buildBarEvent()
}