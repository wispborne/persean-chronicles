package wisp.perseanchronicles.laborer

import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarEvent
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEventCreator
import wisp.perseanchronicles.game
import wisp.questgiver.AutoBarEventDefinition

class Laborer_Stage1_BarEvent : AutoBarEventDefinition<Laborer_Stage1_BarEvent>(
    questFacilitator = LaborerQuest,
    createInteractionPrompt = {
        para { game.text["lab_stg1_prompt"] }
    },
    onInteractionStarted = {},
    textToStartInteraction = { game.text["lab_stg1_startBarEvent"] },
    pages = listOf(
        Page(
            id = 1,
            onPageShown = {
                para { game.text["lab_stg1_pg1_para1"] }
                para { game.text["lab_stg1_pg1_para2"] }
                para { game.text["lab_stg1_pg1_para3"] }
            },
            options = listOf(
                Option(
                    text = { game.text["lab_stg1_pg1_opt1"] },
                    onOptionSelected = {
                        it.goToPage(2)
                    }
                )
            )
        ),
        Page(
            id = 2,
            onPageShown = {
                para { game.text["lab_stg1_pg2_para1"] }
            },
            options = listOf(
                Option(
                    // what's your offer?
                    text = { game.text["lab_stg1_pg2_opt1"] },
                    onOptionSelected = {
                        it.goToPage(3)
                    }
                ),
                Option(
                    // Work dried up on the whole planet?
                    showIf = { LaborerQuest.choices.askedAllWorkDriedUp != true },
                    text = { game.text["lab_stg1_pg2_opt2"] },
                    onOptionSelected = {
                        para { game.text["lab_stg1_pg2_opt2_onSelected"] }
                        LaborerQuest.choices.askedAllWorkDriedUp = true
                        it.refreshOptions()
                    }
                )
            )
        ),
        Page(
            id = 3,
            onPageShown = {
                para { game.text["lab_stg1_pg3_para1"] }
            },
            options = listOf(
                Option(
                    // Accept
                    text = { game.text["lab_stg1_pg3_opt1"] },
                    onOptionSelected = {
                        para { game.text["lab_stg1_pg3_opt1_onSelected"] }
                        navigator.promptToContinue(game.text["leave"]) {
                            LaborerQuest.start(startLocation = dialog.interactionTarget)
                            it.close(doNotOfferAgain = true)
                        }
                    }
                ),
                Option(
                    // How do I know you'll pay?
                    showIf = { LaborerQuest.choices.askedHowDoIKnowYoullPay != true },
                    text = { game.text["lab_stg1_pg3_opt2"] },
                    onOptionSelected = {
                        para { game.text["lab_stg1_pg3_opt2_onSelected"] }
                        LaborerQuest.choices.askedHowDoIKnowYoullPay = true
                        it.refreshOptions()
                    }
                ),
                Option(
                    // Sorry, money up front
                    text = { game.text["lab_stg1_pg3_opt3"] },
                    onOptionSelected = {
                        para { game.text["lab_stg1_pg3_opt3_onSelected"] }
                        navigator.promptToContinue(game.text["leave"]) {
                            it.close(doNotOfferAgain = false)
                        }
                    }
                )
            )
        )
    ),
    people = listOf(LaborerQuest.dale)
) {
    override fun createInstanceOfSelf() = Laborer_Stage1_BarEvent()
}

class Laborer_Stage1_BarEventCreator : BaseBarEventCreator() {
    override fun createBarEvent(): PortsideBarEvent = Laborer_Stage1_BarEvent().buildBarEvent()
    override fun isPriority() = true
}