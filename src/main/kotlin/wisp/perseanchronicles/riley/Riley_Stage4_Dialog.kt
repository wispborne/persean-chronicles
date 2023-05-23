package wisp.perseanchronicles.riley

import com.fs.starfarer.api.Global
import wisp.perseanchronicles.game
import wisp.questgiver.InteractionDefinition
import wisp.questgiver.wispLib.findFirst

class Riley_Stage4_Dialog(
    val mission: RileyHubMission = Global.getSector().intelManager.findFirst()!!
) : InteractionDefinition<Riley_Stage4_Dialog>(
    onInteractionStarted = { },
    pages = listOf(
        Page(
            id = 1,
            image = RileyHubMission.icon,
            onPageShown = {
                para { game.text["riley_stg4_pg1_para1"] }
                para { game.text["riley_stg4_pg1_para2"] }
            },
            options = listOf(
                Option(
                    text = {
                        // Ask to visit father
                        if (RileyHubMission.choices.refusedPayment == true)
                            game.text["riley_stg4_pg1_opt1_ifNotPaid"]
                        else
                            game.text["riley_stg4_pg1_opt1_ifPaid"]
                    },
                    onOptionSelected = { navigator -> navigator.goToPage(2) }
                ),
                Option(
                    // Ask if DJing pays well
                    showIf = { RileyHubMission.choices.askedAboutDJingPay == null },
                    text = { game.text["riley_stg4_pg1_opt2"] },
                    onOptionSelected = { navigator ->
                        para { game.text["riley_stg4_pg1_opt2_onSelected"] }
                        RileyHubMission.choices.askedAboutDJingPay = true
                        navigator.refreshOptions()
                    }
                ),
                Option(
                    // Tell her to keep the money
                    showIf = { RileyHubMission.choices.refusedPayment == null },
                    text = { game.text["riley_stg4_pg1_opt3"] },
                    onOptionSelected = { navigator ->
                        RileyHubMission.choices.refusedPayment = true
                        mission.setCreditReward(0)
                        para { game.text["riley_stg4_pg1_opt3_onSelected"] }
                        navigator.refreshOptions()
                    }
                ),
                Option(
                    // Leave without going to house
                    text = { game.text["riley_stg4_pg1_opt4"] },
                    onOptionSelected = { navigator ->
                        RileyHubMission.choices.visitedFather = false
                        // TODO show this on success screen
                        mission.setCurrentStage(RileyHubMission.Stage.Completed, dialog, null)
                        navigator.close(doNotOfferAgain = true)
                    }
                )
            )
        ),
        Page(
            id = 2,
            image = Illustration("wisp_perseanchronicles_riley", "house"),
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
                        RileyHubMission.choices.movedCloserToRiley = true
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
                        if (RileyHubMission.isFatherWorkingWithGovt)
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
                        RileyHubMission.choices.heldRiley = true
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
                    showIf = { RileyHubMission.choices.askedIfLegal == null },
                    text = { game.text["riley_stg4_pg4_opt1"] },
                    onOptionSelected = { navigator ->
                        RileyHubMission.choices.askedIfLegal = true
                        if (RileyHubMission.isFatherWorkingWithGovt) {
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
                    showIf = { RileyHubMission.choices.askedWhatRileyThinks == null },
                    text = { game.text["riley_stg4_pg4_opt2"] },
                    onOptionSelected = { navigator ->
                        RileyHubMission.choices.askedWhatRileyThinks = true
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
                        RileyHubMission.choices.askedWhatRileyThinks == true
                                && RileyHubMission.choices.triedToConvinceToJoinYou == null
                    },
                    text = { game.text["riley_stg4_pg4_opt3"] },
                    onOptionSelected = { navigator ->
                        RileyHubMission.choices.triedToConvinceToJoinYou = true
                        para { game.text["riley_stg4_pg4_opt3_onSelected"] }
                        navigator.refreshOptions()
                    }
                ),
                Option(
                    // Leave
                    showIf = {
                        RileyHubMission.choices.askedWhatRileyThinks == true
                    },
                    text = { game.text["riley_stg4_pg4_opt4"] },
                    onOptionSelected = { navigator ->
                        RileyHubMission.choices.leftRileyWithFather = true
                        para { game.text["riley_stg4_pg4_opt4_onSelected_para1"] }

                        if (RileyHubMission.choices.movedCloserToRiley == true
                            && RileyHubMission.choices.heldRiley == true
                        ) {
                            para { game.text["riley_stg4_pg4_opt4_onSelected_ifRomanced"] }
                        }

                        para { game.text["riley_stg4_pg4_opt4_onSelected_para2"] }
                        navigator.promptToContinue(game.text["leave"]) {
                            // TODO show this on success screen
                            mission.setCurrentStage(RileyHubMission.Stage.Completed, dialog, null)
                            navigator.close(doNotOfferAgain = true)
                        }
                    }
                ),
                Option(
                    // Destroy the Core
                    text = { game.text["riley_stg4_pg4_opt5"] },
                    onOptionSelected = { navigator ->
                        RileyHubMission.choices.destroyedTheCore = true
                        para { game.text["riley_stg4_pg4_opt5_onSelected_para1"] }
                        para { game.text["riley_stg4_pg4_opt5_onSelected_para2"] }
                        navigator.promptToContinue(game.text["leave"]) {
                            // TODO show this on success screen
                            mission.setCurrentStage(RileyHubMission.Stage.Completed, dialog, null)
                            navigator.close(doNotOfferAgain = true)
                        }
                    }
                ),
                Option(
                    // Turn in for a bounty
                    showIf = {
                        RileyHubMission.choices.askedIfLegal == true
                                && RileyHubMission.isFatherWorkingWithGovt
                    },
                    text = { game.text["riley_stg4_pg4_opt6"] },
                    onOptionSelected = { navigator ->
                        RileyHubMission.choices.turnedInForABounty = true
                        game.sector.playerFleet.cargo.credits.add(RileyHubMission.BOUNTY_CREDITS.toFloat())
                        para {
                            game.text.getf(
                                "riley_stg4_pg4_opt6_onSelected_para1",
                                "rileyDestPlanetControllingFactionWithoutArticle" to RileyHubMission.state.destinationPlanet?.faction?.displayName
                            )
                        }
                        para { game.text["riley_stg4_pg4_opt6_onSelected_para2"] }
                        navigator.promptToContinue(game.text["leave"]) {
                            // TODO show this on success screen
                            mission.setCurrentStage(RileyHubMission.Stage.Completed, dialog, null)
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