package org.wisp.stories.riley

import org.wisp.stories.game
import wisp.questgiver.InteractionDefinition
import wisp.questgiver.wispLib.add

class Riley_Stage4_Dialog : InteractionDefinition<Riley_Stage4_Dialog>(
    onInteractionStarted = { },
    pages = listOf(
        Page(
            id = 1,
            onPageShown = {
                para { game.text["riley_stg4_pg1_para1"] }
                para { game.text["riley_stg4_pg1_para2"] }
            },
            options = listOf(
                Option(
                    text = {
                        // Ask to visit father
                        if (RileyQuest.choices[RileyQuest.ChoiceKey.tookPayment] == false)
                            game.text["riley_stg4_pg1_opt1_ifPaid"]
                        else
                            game.text["riley_stg4_pg1_opt1_ifNotPaid"]
                    },
                    onOptionSelected = { navigator -> navigator.goToPage(2) }
                ),
                Option(
                    // Ask if DJing pays well
                    showIf = { RileyQuest.choices[RileyQuest.ChoiceKey.askedAboutDJingPay] == null },
                    text = { game.text["riley_stg4_pg1_opt2"] },
                    onOptionSelected = { navigator ->
                        para { game.text["riley_stg4_pg1_opt2_onSelected"] }
                        RileyQuest.choices = RileyQuest.choices.add(RileyQuest.ChoiceKey.askedAboutDJingPay, true)
                        navigator.refreshOptions()
                    }
                ),
                Option(
                    // Tell her to keep the money
                    showIf = { RileyQuest.choices[RileyQuest.ChoiceKey.tookPayment] == null },
                    text = { game.text["riley_stg4_pg1_opt3"] },
                    onOptionSelected = { navigator ->
                        RileyQuest.choices.set(RileyQuest.ChoiceKey.tookPayment, false)
                        para { game.text["riley_stg4_pg1_opt3_onSelected"] }
                        navigator.refreshOptions()
                    }
                ),
                Option(
                    // Leave without going to house
                    text = { game.text["riley_stg4_pg1_opt4"] },
                    onOptionSelected = { navigator ->
                        RileyQuest.choices[RileyQuest.ChoiceKey.visitedFather] = false
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
                        RileyQuest.choices[RileyQuest.ChoiceKey.movedCloserToRiley] = true
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
                para {
                    if (RileyQuest.isFatherWorkingWithGovt)
                        game.text["riley_stg4_pg3_para2_ifGovtInvolved"]
                    else
                        game.text["riley_stg4_pg3_para2_ifGovtNotInvolved"]
                }
                para { game.text["riley_stg4_pg3_para3"] }
                para { game.text["riley_stg4_pg3_para4"] }
                navigator.promptToContinue(game.text["continue"]) {
                    para { game.text["riley_stg4_pg3_para5"] }
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
                        RileyQuest.choices[RileyQuest.ChoiceKey.heldRiley] = true
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
                    para { game.text["riley_stg4_pg4_para2"] }
                }
            },
            options = listOf(
                Option(
                    // Ask if legal
                    showIf = { RileyQuest.choices[RileyQuest.ChoiceKey.askedIfLegal] == null },
                    text = { game.text["riley_stg4_pg4_opt1"] },
                    onOptionSelected = { navigator ->
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
                    showIf = { RileyQuest.choices[RileyQuest.ChoiceKey.askedWhatRileyThinks] == null },
                    text = { game.text["riley_stg4_pg4_opt2"] },
                    onOptionSelected = { navigator ->
                        RileyQuest.choices[RileyQuest.ChoiceKey.askedWhatRileyThinks] = true
                        para { game.text["riley_stg4_pg4_opt2_onSelected_para1"] }
                        para { game.text["riley_stg4_pg4_opt2_onSelected_para2"] }
                        para { game.text["riley_stg4_pg4_opt2_onSelected_para3"] }
                        navigator.refreshOptions()
                    }
                ),
                Option(
                    // Try to convince her to come with you
                    showIf = {
                        RileyQuest.choices[RileyQuest.ChoiceKey.askedWhatRileyThinks] == true
                                && RileyQuest.choices[RileyQuest.ChoiceKey.triedToConvinceToJoinYou] == null
                    },
                    text = { game.text["riley_stg4_pg4_opt3"] },
                    onOptionSelected = { navigator ->
                        RileyQuest.choices[RileyQuest.ChoiceKey.triedToConvinceToJoinYou] = true
                        para { game.text["riley_stg4_pg4_opt3_onSelected"] }
                        navigator.refreshOptions()
                    }
                ),
                Option(
                    // Leave
                    showIf = {
                        RileyQuest.choices[RileyQuest.ChoiceKey.askedWhatRileyThinks] == true
                    },
                    text = { game.text["riley_stg4_pg4_opt4"] },
                    onOptionSelected = { navigator ->
                        RileyQuest.choices[RileyQuest.ChoiceKey.leftRileyWithFather] = true
                        para { game.text["riley_stg4_pg4_opt4_onSelected_para1"] }

                        if (RileyQuest.choices[RileyQuest.ChoiceKey.movedCloserToRiley] == true
                            && RileyQuest.choices[RileyQuest.ChoiceKey.heldRiley] == true
                        ) {
                            para { game.text["riley_stg4_pg4_opt4_onSelected_ifRomanced"] }
                        }

                        para { game.text["riley_stg4_pg4_opt4_onSelected_para2"] }
                        RileyQuest.complete()
                        navigator.close(doNotOfferAgain = true)
                    }
                ),
                Option(
                    // Destroy the Core
                    text = { game.text["riley_stg4_pg4_opt5"] },
                    onOptionSelected = { navigator ->
                        RileyQuest.choices[RileyQuest.ChoiceKey.destroyedTheCore] = true
                        para { game.text["riley_stg4_pg4_opt5_onSelected_para1"] }
                        para { game.text["riley_stg4_pg4_opt5_onSelected_para2"] }
                        RileyQuest.complete()
                        navigator.close(doNotOfferAgain = true)
                    }
                ),
                Option(
                    // Turn in for a bounty
                    showIf = {
                        RileyQuest.choices[RileyQuest.ChoiceKey.askedIfLegal] == true
                                && RileyQuest.isFatherWorkingWithGovt
                    },
                    text = { game.text["riley_stg4_pg4_opt6"] },
                    onOptionSelected = { navigator ->
                        RileyQuest.choices[RileyQuest.ChoiceKey.turnedInForABounty] = true
                        para { game.text["riley_stg4_pg4_opt6_onSelected_para1"] }
                        para { game.text["riley_stg4_pg4_opt6_onSelected_para2"] }
                        RileyQuest.complete()
                        navigator.close(doNotOfferAgain = true)
                    }
                )
            )
        )
    )
) {
    override fun createInstanceOfSelf(): InteractionDefinition<Riley_Stage4_Dialog> = Riley_Stage4_Dialog()
}