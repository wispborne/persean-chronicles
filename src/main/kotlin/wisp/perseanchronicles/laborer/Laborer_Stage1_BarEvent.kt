package wisp.perseanchronicles.laborer

import com.fs.starfarer.api.util.Misc
import wisp.perseanchronicles.common.PerseanChroniclesNPCs
import wisp.perseanchronicles.game
import wisp.questgiver.v2.BarEventLogic
import wisp.questgiver.v2.IInteractionLogic

class Laborer_Stage1_BarEvent : BarEventLogic<LaborerHubMission>(
    createInteractionPrompt = {
        para { game.text["lab_stg1_prompt"] }
    },
    onInteractionStarted = {},
    textToStartInteraction = {
        Option(
            text = game.text["lab_stg1_startBarEvent"],
            textColor = Misc.getButtonTextColor()
        )
    },
    pages = listOf(
        IInteractionLogic.Page(
            id = 1,
            people = { listOf(LaborerHubMission.dale)},
            onPageShown = {
                para { game.text["lab_stg1_pg1_para1"] }
                para { game.text["lab_stg1_pg1_para2"] }
                para { game.text["lab_stg1_pg1_para3"] }
            },
            options = listOf(
                IInteractionLogic.Option(
                    text = { game.text["lab_stg1_pg1_opt1"] },
                    onOptionSelected = {
                        it.goToPage(2)
                    }
                )
            )
        ),
        IInteractionLogic.Page(
            id = 2,
            onPageShown = {
                para { game.text["lab_stg1_pg2_para1"] }
            },
            options = listOf(
                IInteractionLogic.Option(
                    // what's your offer?
                    text = { game.text["lab_stg1_pg2_opt1"] },
                    onOptionSelected = {
                        it.goToPage(3)
                    }
                ),
                IInteractionLogic.Option(
                    // Work dried up on the whole planet?
                    showIf = { mission.choices.askedAllWorkDriedUp != true },
                    text = { game.text["lab_stg1_pg2_opt2"] },
                    onOptionSelected = {
                        para { game.text["lab_stg1_pg2_opt2_onSelected"] }
                        mission.choices.askedAllWorkDriedUp = true
                        it.refreshOptions()
                    }
                )
            )
        ),
        IInteractionLogic.Page(
            id = 3,
            onPageShown = {
                para { game.text["lab_stg1_pg3_para1"] }
                dialog.visualPanel.showMapMarker(LaborerHubMission.state.destPlanet, null, null, false, null, null, emptySet())
            },
            options = listOf(
                IInteractionLogic.Option(
                    // Accept
                    text = { game.text["lab_stg1_pg3_opt1"] },
                    onOptionSelected = {
                        para { game.text["lab_stg1_pg3_opt1_onSelected"] }
                        mission.accept(dialog, emptyMap())
                        navigator.promptToContinue(game.text["leave"]) {
                            it.close(doNotOfferAgain = true)
                        }
                    }
                ),
                IInteractionLogic.Option(
                    // How do I know you'll pay?
                    showIf = { mission.choices.askedHowDoIKnowYoullPay != true },
                    text = { game.text["lab_stg1_pg3_opt2"] },
                    onOptionSelected = {
                        para { game.text["lab_stg1_pg3_opt2_onSelected"] }
                        mission.choices.askedHowDoIKnowYoullPay = true
                        it.refreshOptions()
                    }
                ),
                IInteractionLogic.Option(
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
    )
)