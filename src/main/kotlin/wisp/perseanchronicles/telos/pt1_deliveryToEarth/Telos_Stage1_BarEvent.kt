package wisp.perseanchronicles.telos.pt1_deliveryToEarth

import com.fs.starfarer.api.characters.FullName
import com.fs.starfarer.api.impl.campaign.ids.Ranks
import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarEvent
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEventCreator
import wisp.perseanchronicles.game
import wisp.perseanchronicles.telos.TelosQuest
import wisp.questgiver.AutoBarEventDefinition

class Telos_Stage1_BarEvent : AutoBarEventDefinition<Telos_Stage1_BarEvent>(
    questFacilitator = TelosQuest,
    createInteractionPrompt = {
        para { game.jsonText["telos.stage1-deliveryToEarth.bar-event.blurb"] }
    },
    onInteractionStarted = { },
    textToStartInteraction = { game.jsonText["telos.stage1-deliveryToEarth.bar-event.prompt"] },
    pages = listOf(
        Page(
            id = 1,
            onPageShown = {
                para { game.jsonText["telos.stage1-deliveryToEarth.page-1.paras[0]"] }
                para { game.jsonText["telos.stage1-deliveryToEarth.page-1.paras[1]"] }
            },
            options = listOf(
                Option(
                    // accept
                    text = { game.jsonText["telos.stage1-deliveryToEarth.page-1.options[0].text"] },
                    onOptionSelected = {
                        it.goToPage(2)
                    }
                ),
                Option(
                    // decline
                    text = { game.jsonText["telos.stage1-deliveryToEarth.page-1.options[1].text"] },
                    onOptionSelected = { navigator ->
                        navigator.close(doNotOfferAgain = false)
                    }
                )
            )
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