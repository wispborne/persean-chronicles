package org.wisp.stories.riley

import org.wisp.stories.game
import wisp.questgiver.InteractionDefinition

class Riley_Stage4_Dialog : InteractionDefinition<Riley_Stage4_Dialog>(
    onInteractionStarted = { },
    pages = listOf(
        Page(
            id = 1,
            image = RileyQuest.icon,
            onPageShown = {
                para { game.text["riley_stg4_pg1_para1"] }
                para { game.text["riley_stg4_pg1_para2"] }
            },
            options = listOf(
                Option(
                    text = {
                        // Ask to visit father
                        if (RileyQuest.choices.refusedPayment == true)
                            game.text["riley_stg4_pg1_opt1_ifNotPaid"]
                        else
                            game.text["riley_stg4_pg1_opt1_ifPaid"]
                    },
                    onOptionSelected = { navigator -> navigator.goToPage(2) }
                ),
                Option(
                    // Ask if DJing pays well
                    showIf = { RileyQuest.choices.askedAboutDJingPay == null },
                    text = { game.text["riley_stg4_pg1_opt2"] },
                    onOptionSelected = { navigator ->
                        para { game.text["riley_stg4_pg1_opt2_onSelected"] }
                        RileyQuest.choices.askedAboutDJingPay = true
                        navigator.refreshOptions()
                    }
                ),
                Option(
                    // Tell her to keep the money
                    showIf = { RileyQuest.choices.refusedPayment == null },
                    text = { game.text["riley_stg4_pg1_opt3"] },
                    onOptionSelected = { navigator ->
                        RileyQuest.choices.refusedPayment = true
                        para { game.text["riley_stg4_pg1_opt3_onSelected"] }
                        navigator.refreshOptions()
                    }
                ),
                Option(
                    // Leave without going to house
                    text = { game.text["riley_stg4_pg1_opt4"] },
                    onOptionSelected = { navigator ->
                        RileyQuest.choices.visitedFather = false
                        RileyQuest.complete()
                        navigator.close(doNotOfferAgain = true)
                    }
                )
            )
        ),
        Page(
            id = 2,
            onPageShown = {
                para { game.text["riley_stg4_pg2_para1"] }
                para { game.text["riley_stg4_pg2_para2"] }
                para { game.text["riley_stg4_pg2_para3"] }
            },
            options = listOf(
                Option(
                    // Cordial thanks
                    text = { game.text["riley_stg4_pg2_opt1"] },
                    onOptionSelected = { navigator -> navigator.goToPage(3) }
                ),
                Option(
                    // Romance thanks
                    text = { game.text["riley_stg4_pg2_opt2"] },
                    onOptionSelected = { navigator ->
                        RileyQuest.choices.movedCloserToRiley = true
                        para { game.text["riley_stg4_pg2_opt2_onSelected"] }
                        navigator.goToPage(3)
                    }
                )
            )
        ),
        Page(
            id = 3,
            onPageShown = {
                para { game.text["riley_stg4_pg3_para1"] }
                navigator.promptToContinue(game.text["continue"]) {
                    para {
                        if (RileyQuest.isFatherWorkingWithGovt)
                            game.text["riley_stg4_pg3_para2_ifGovtInvolved"]
                        else
                            game.text["riley_stg4_pg3_para2_ifGovtNotInvolved"]
                    }
                    para { game.text["riley_stg4_pg3_para3"] }
                    navigator.promptToContinue(game.text["continue"]) {
                        para { game.text["riley_stg4_pg3_para4"] }
                        navigator.promptToContinue(game.text["continue"]) {
                            para { game.text["riley_stg4_pg3_para5"] }
                        }
                    }
                }
            },
            options = listOf(
                Option(
                    // Cordial comfort
                    text = { game.text["riley_stg4_pg3_opt1"] },
                    onOptionSelected = { navigator ->
                        para { game.text["riley_stg4_pg3_opt1_onSelected"] }
                        navigator.goToPage(4)
                    }
                ),
                Option(
                    // Hold her (needed for romance, but really this is a perfectly normal response)
                    text = { game.text["riley_stg4_pg3_opt2"] },
                    onOptionSelected = { navigator ->
                        RileyQuest.choices.heldRiley = true
                        para { game.text["riley_stg4_pg3_opt2_onSelected"] }
                        navigator.goToPage(4)
                    }
                )
            )
        ),
        Page(
            id = 4,
            onPageShown = {
                navigator.promptToContinue(game.text["continue"]) {
                    para { game.text["riley_stg4_pg4_para1"] }
                    navigator.promptToContinue(game.text["continue"]) {
                        para { game.text["riley_stg4_pg4_para2"] }
                    }
                }
            },
            options = listOf(
                Option(
                    // Ask if legal
                    showIf = { RileyQuest.choices.askedIfLegal == null },
                    text = { game.text["riley_stg4_pg4_opt1"] },
                    onOptionSelected = { navigator ->
                        RileyQuest.choices.askedIfLegal = true
                        if (RileyQuest.isFatherWorkingWithGovt) {
                            para { game.text["riley_stg4_pg4_opt1_onSelected_ifIllegal_para1"] }
                            para { game.text["riley_stg4_pg4_opt1_onSelected_ifIllegal_para2"] }
                        } else {
                            para { game.text["riley_stg4_pg4_opt1_onSelected_ifLegal"] }
                        }
                        navigator.refreshOptions()
                    }
                ),
                Option(
                    // Ask how Riley feels
                    showIf = { RileyQuest.choices.askedWhatRileyThinks == null },
                    text = { game.text["riley_stg4_pg4_opt2"] },
                    onOptionSelected = { navigator ->
                        RileyQuest.choices.askedWhatRileyThinks = true
                        para { game.text["riley_stg4_pg4_opt2_onSelected_para1"] }
                        para { game.text["riley_stg4_pg4_opt2_onSelected_para2"] }

                        navigator.promptToContinue(game.text["continue"]) {
                            para { game.text["riley_stg4_pg4_opt2_onSelected_para3"] }
                            navigator.refreshOptions() // fucked up here
                        }
                    }
                ),
                Option(
                    // Try to convince her to come with you
                    showIf = {
                        RileyQuest.choices.askedWhatRileyThinks == true
                                && RileyQuest.choices.triedToConvinceToJoinYou == null
                    },
                    text = { game.text["riley_stg4_pg4_opt3"] },
                    onOptionSelected = { navigator ->
                        RileyQuest.choices.triedToConvinceToJoinYou = true
                        para { game.text["riley_stg4_pg4_opt3_onSelected"] }
                        navigator.refreshOptions()
                    }
                ),
                Option(
                    // Leave
                    showIf = {
                        RileyQuest.choices.askedWhatRileyThinks == true
                    },
                    text = { game.text["riley_stg4_pg4_opt4"] },
                    onOptionSelected = { navigator ->
                        RileyQuest.choices.leftRileyWithFather = true
                        para { game.text["riley_stg4_pg4_opt4_onSelected_para1"] }

                        if (RileyQuest.choices.movedCloserToRiley == true
                            && RileyQuest.choices.heldRiley == true
                        ) {
                            para { game.text["riley_stg4_pg4_opt4_onSelected_ifRomanced"] }
                        }

                        para { game.text["riley_stg4_pg4_opt4_onSelected_para2"] }
                        navigator.promptToContinue(game.text["leave"]) {
                            RileyQuest.complete()
                            navigator.close(doNotOfferAgain = true)
                        }
                    }
                ),
                Option(
                    // Destroy the Core
                    text = { game.text["riley_stg4_pg4_opt5"] },
                    onOptionSelected = { navigator ->
                        RileyQuest.choices.destroyedTheCore = true
                        para { game.text["riley_stg4_pg4_opt5_onSelected_para1"] }
                        para { game.text["riley_stg4_pg4_opt5_onSelected_para2"] }
                        navigator.promptToContinue(game.text["leave"]) {
                            RileyQuest.complete()
                            navigator.close(doNotOfferAgain = true)
                        }
                    }
                ),
                Option(
                    // Turn in for a bounty
                    showIf = {
                        RileyQuest.choices.askedIfLegal == true
                                && RileyQuest.isFatherWorkingWithGovt
                    },
                    text = { game.text["riley_stg4_pg4_opt6"] },
                    onOptionSelected = { navigator ->
                        RileyQuest.choices.turnedInForABounty = true
                        game.sector.playerFleet.cargo.credits.add(RileyQuest.BOUNTY_CREDITS.toFloat())
                        para {
                            game.text.getf(
                                "riley_stg4_pg4_opt6_onSelected_para1",
                                "rileyDestPlanetControllingFactionWithoutArticle" to RileyQuest.state.destinationPlanet?.faction?.displayName
                            )
                        }
                        para { game.text["riley_stg4_pg4_opt6_onSelected_para2"] }
                        navigator.promptToContinue(game.text["leave"]) {
                            RileyQuest.complete()
                            navigator.close(doNotOfferAgain = true)
                        }
                    }
                )
            )
        )
    )
) {
    override fun createInstanceOfSelf(): InteractionDefinition<Riley_Stage4_Dialog> = Riley_Stage4_Dialog()
}