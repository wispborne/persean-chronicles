package wisp.perseanchronicles.telos.pt1_deliveryToEarth

import com.fs.starfarer.api.characters.FullName
import com.fs.starfarer.api.impl.campaign.ids.Ranks
import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarEvent
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEventCreator
import org.json.JSONObject
import wisp.perseanchronicles.telos.TelosQuest
import wisp.questgiver.AutoBarEventDefinition
import wisp.questgiver.InteractionPromptFromJson
import wisp.questgiver.PagesFromJson
import wisp.questgiver.TextToStartInteractionFromJson

class Telos_Stage1_BarEvent(
    stageJson: JSONObject = TelosQuest.json.getJSONArray("stages").getJSONObject(0)
) : AutoBarEventDefinition<Telos_Stage1_BarEvent>(
    questFacilitator = TelosQuest,
    createInteractionPrompt = InteractionPromptFromJson(stageJson = stageJson),
    onInteractionStarted = { },
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
    personName = FullName("David", "Rengel", FullName.Gender.MALE),
    personRank = Ranks.CITIZEN,
    personPost = Ranks.CITIZEN,
//    personPortrait = NirvanaQuest.icon.spriteName(game)
) {
    override fun createInstanceOfSelf() = Telos_Stage1_BarEvent()
}

class Telos_Stage1_BarEventCreator : BaseBarEventCreator() {
    override fun isPriority(): Boolean {
        return true
    }

    override fun createBarEvent(): PortsideBarEvent = Telos_Stage1_BarEvent().buildBarEvent()
}