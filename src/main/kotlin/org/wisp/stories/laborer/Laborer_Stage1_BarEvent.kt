package org.wisp.stories.laborer

import com.fs.starfarer.api.characters.FullName
import com.fs.starfarer.api.impl.campaign.ids.Commodities
import com.fs.starfarer.api.impl.campaign.ids.Ranks
import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarEvent
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEventCreator
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity
import org.wisp.stories.game
import wisp.questgiver.AutoBarEventDefinition
import wisp.questgiver.BarEventDefinition
import wisp.questgiver.spriteName
import wisp.questgiver.wispLib.preferredConnectedEntity

class Laborer_Stage1_BarEvent : AutoBarEventDefinition<Laborer_Stage1_BarEvent>(
    questFacilitator = LaborerQuest,
    createInteractionPrompt = {
        paraSync { game.text["nirv_stg1_prompt"] }
    },
    onInteractionStarted = {},
    textToStartInteraction = { game.text["nirv_stg1_startBarEvent"] },
    pages = listOf(
        Page(
            id = 1,
            onPageShown = {
                para { game.text["nirv_stg1_pg1_para1"] }
                para { game.text["nirv_stg1_pg1_para2"] }
                para { game.text["nirv_stg1_pg1_para3"] }
            },
            options = listOf(
                Option(
                    // accept
                    text = { game.text["nirv_stg1_pg1_opt1"] },
                    onOptionSelected = {
                        it.goToPage(2)
                    }
                ),
                Option(
                    // decline
                    text = { game.text["nirv_stg1_pg1_opt2"] },
                    onOptionSelected = { navigator ->
                        navigator.close(doNotOfferAgain = false)
                    }
                )
            )
        ),
        Page(
            id = 2,
            onPageShown = {
                para { game.text["nirv_stg1_pg2_para1"] }
            },
            options = listOf(
                Option(
                    // fully accept
                    text = { game.text["nirv_stg1_pg2_opt1"] },
                    onOptionSelected = {
                        para { game.text["nirv_stg1_pg2_opt1_onSelected"] }
                        navigator.promptToContinue(game.text["continue"]) {
                            LaborerQuest.start(dialog.interactionTarget.market.preferredConnectedEntity!!)
                            navigator.close(doNotOfferAgain = true)
                        }
                    }
                ),
                Option(
                    // not enough space
                    text = { game.text["nirv_stg1_pg2_opt2"] },
                    onOptionSelected = {
                        navigator.close(doNotOfferAgain = false)
                    }
                ),
                Option(
                    // decline
                    text = { game.text["nirv_stg1_pg2_opt3"] },
                    onOptionSelected = {
                        navigator.close(doNotOfferAgain = false)
                    }
                )
            )
        )
    ),
    personName = FullName("David", "Rengel", FullName.Gender.MALE),
    personRank = Ranks.CITIZEN,
    personPost = Ranks.CITIZEN
) {
    override fun createInstanceOfSelf() = Laborer_Stage1_BarEvent()
}

class Laborer_Stage1_BarEventCreator : BaseBarEventCreator() {
    override fun createBarEvent(): PortsideBarEvent = Laborer_Stage1_BarEvent().buildBarEvent()
}