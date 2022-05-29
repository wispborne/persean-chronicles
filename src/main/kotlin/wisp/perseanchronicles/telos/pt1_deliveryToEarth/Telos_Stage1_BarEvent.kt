package wisp.perseanchronicles.telos.pt1_deliveryToEarth

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.characters.FullName
import com.fs.starfarer.api.impl.campaign.ids.Ranks
import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarEvent
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEventCreator
import wisp.perseanchronicles.MOD_ID
import wisp.perseanchronicles.telos.TelosQuest
import wisp.questgiver.AutoBarEventDefinition
import wisp.questgiver.PagesFromJson

class Telos_Stage1_BarEvent : AutoBarEventDefinition<Telos_Stage1_BarEvent>(
    questFacilitator = TelosQuest,
    createInteractionPrompt = {
        para { "test" }
//        para { game.jsonText["telos.stage1-deliveryToEarth.bar-event.blurb"] }
    },
    onInteractionStarted = { },
    textToStartInteraction = {
        "test"
//        game.jsonText["telos.stage1-deliveryToEarth.bar-event.prompt"]
    },
    pages = PagesFromJson(
        Global.getSettings().getMergedJSONForMod("data/strings/telos.hjson", MOD_ID)
            .getJSONObject(MOD_ID)
            .getJSONObject("telos").getJSONArray("stages").getJSONObject(0).getJSONArray("pages"),
        onPageShownHandlersByPageId = emptyMap(),
        onOptionSelectedHandlersByPageId = mapOf(
            "ASPIRARDs?" to {
                para { "Yup!" }
            },
            "accept" to {
                it.close(doNotOfferAgain = true)
            }
        )
    ),
//    listOf(
//        Page(
//            id = 1,
//            onPageShown = {
//                para { game.jsonText["telos.stage1-deliveryToEarth.page-1.paras[0]"] }
//                para { game.jsonText["telos.stage1-deliveryToEarth.page-1.paras[1]"] }
//            },
//            options = listOf(
//                Option(
//                    // accept
//                    text = { game.jsonText["telos.stage1-deliveryToEarth.page-1.options[0].text"] },
//                    onOptionSelected = {
//                        it.goToPage(2)
//                    }
//                ),
//                Option(
//                    // decline
//                    text = { game.jsonText["telos.stage1-deliveryToEarth.page-1.options[1].text"] },
//                    onOptionSelected = { navigator ->
//                        navigator.close(doNotOfferAgain = false)
//                    }
//                )
//            )
//        )
//    )
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