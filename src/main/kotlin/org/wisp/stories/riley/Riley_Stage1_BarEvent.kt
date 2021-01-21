package org.wisp.stories.riley

import com.fs.starfarer.api.characters.FullName
import com.fs.starfarer.api.impl.campaign.ids.Ranks
import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarEvent
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEventCreator
import org.wisp.stories.game
import wisp.questgiver.BarEventDefinition
import wisp.questgiver.wispLib.empty
import wisp.questgiver.wispLib.preferredConnectedEntity

class Riley_Stage1_BarEvent : BarEventDefinition<Riley_Stage1_BarEvent>(
    shouldShowEvent = { market -> RileyQuest.shouldMarketOfferQuest(market) },
    interactionPrompt = {
        para { game.text["riley_stg1_prompt"] }
    },
    textToStartInteraction = { game.text["riley_stg1_startBarEvent"] },
    onInteractionStarted = {
        RileyQuest.init(dialog.interactionTarget.market.preferredConnectedEntity!!)
    },
    pages = listOf(
        Page(
            id = 1,
            onPageShown = {
                para { game.text["riley_stg1_pg1_para1"] }
                para { game.text["riley_stg1_pg1_para2"] }
                para { game.text["riley_stg1_pg1_para3"] }
            },
            options = listOf(
                Option(
                    // accept
                    text = { game.text["riley_stg1_pg1_opt1"] },
                    onOptionSelected = {
                        para { game.text["riley_stg1_pg1_opt1_onSelected"] }
                        RileyQuest.start(dialog.interactionTarget.market.preferredConnectedEntity!!)
                        it.close(doNotOfferAgain = true)
                    }
                ),
                Option(
                    // why not buy your own ship?
                    showIf = { RileyQuest.choices.askedWhyNotBuyOwnShip != true },
                    text = { game.text["riley_stg1_pg1_opt2"] },
                    onOptionSelected = { navigator ->
                        para { game.text["riley_stg1_pg1_opt2_onSelected"] }
                        RileyQuest.choices.askedWhyNotBuyOwnShip = true
                        navigator.refreshOptions()
                    }
                ),
                Option(
                    // decline
                    text = { game.text["riley_stg1_pg1_opt3"] },
                    onOptionSelected = { navigator ->
                        para { game.text["riley_stg1_pg1_opt3_onSelected"] }
                        navigator.promptToContinue(game.text["continue"]) {
                            navigator.close(doNotOfferAgain = true)
                        }
                    }
                )
            )
        )
    ),
    personName = FullName(game.text["riley_name"], String.empty, FullName.Gender.FEMALE),
    personRank = Ranks.CITIZEN,
    personPost = Ranks.CITIZEN,
    personPortrait = game.settings.getSpriteName(RileyQuest.icon.category, RileyQuest.icon.id)
) {
    override fun createInstanceOfSelf() = Riley_Stage1_BarEvent()
}

class Riley_Stage1_BarEventCreator : BaseBarEventCreator() {
    override fun createBarEvent(): PortsideBarEvent = Riley_Stage1_BarEvent().buildBarEvent()
}