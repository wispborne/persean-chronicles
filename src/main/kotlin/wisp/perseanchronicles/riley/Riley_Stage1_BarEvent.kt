package wisp.perseanchronicles.riley

import com.fs.starfarer.api.util.Misc
import wisp.perseanchronicles.game
import wisp.questgiver.v2.BarEventLogic
import wisp.questgiver.v2.IInteractionLogic

class Riley_Stage1_BarEvent : BarEventLogic<RileyHubMission>(
    createInteractionPrompt = {
        para { game.text["riley_stg1_prompt"] }
    },
    textToStartInteraction = {
        Option(
            text = game.text["riley_stg1_startBarEvent"],
            textColor = Misc.getHighlightColor()
        )
    },
    onInteractionStarted = {},
    people = { listOf(RileyHubMission.riley) },
    pages = listOf(
        IInteractionLogic.Page(
            id = 1,
            onPageShown = {
                para { game.text["riley_stg1_pg1_para1"] }
                para { game.text["riley_stg1_pg1_para2"] }
                para { game.text["riley_stg1_pg1_para3"] }
                dialog.visualPanel.showMapMarker(RileyHubMission.state.destinationPlanet, null, null, false, null, null, RileyHubMission.tags)
            },
            options = listOf(
                IInteractionLogic.Option(
                    // accept
                    text = { game.text["riley_stg1_pg1_opt1"] },
                    onOptionSelected = {
                        para { game.text["riley_stg1_pg1_opt1_onSelected"] }
                        mission.accept(dialog, null)
                        navigator.promptToContinue(game.text["riley_stg1_pg1_opt1_onSelected_continue"]) {
                            it.close(doNotOfferAgain = true)
                        }
                    }
                ),
                IInteractionLogic.Option(
                    // why not buy your own ship?
                    showIf = { RileyHubMission.choices.askedWhyNotBuyOwnShip != true },
                    text = { game.text["riley_stg1_pg1_opt2"] },
                    onOptionSelected = { navigator ->
                        para { game.text["riley_stg1_pg1_opt2_onSelected"] }
                        RileyHubMission.choices.askedWhyNotBuyOwnShip = true
                        navigator.refreshOptions()
                    }
                ),
                IInteractionLogic.Option(
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
    )
)
