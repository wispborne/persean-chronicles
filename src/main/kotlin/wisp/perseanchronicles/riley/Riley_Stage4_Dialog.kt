package wisp.perseanchronicles.riley

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.util.Misc
import data.scripts.utils.SotfMisc
import org.magiclib.kotlin.addCreditsGainText
import org.magiclib.kotlin.adjustReputationWithPlayer
import wisp.perseanchronicles.common.PerseanChroniclesNPCs
import wisp.perseanchronicles.game
import wisp.questgiver.v2.IInteractionLogic
import wisp.questgiver.v2.InteractionDialogLogic
import wisp.questgiver.wispLib.findFirst

class Riley_Stage4_Dialog(
    val mission: RileyHubMission = Global.getSector().intelManager.findFirst()!!
) : InteractionDialogLogic<Riley_Stage4_Dialog>(
    onInteractionStarted = {
        // Increase rep since you've been traveling together
        Misc.adjustRep(PerseanChroniclesNPCs.riley, 0.2f, null)
        // Manually handle rep changes from here on out
        mission.setNoRepChanges()
    },
    pages = listOf(
        IInteractionLogic.Page(
            id = 1,
            people = { listOf(PerseanChroniclesNPCs.riley) },
            onPageShown = {
                para { game.text["riley_stg4_pg1_para1"] }
                para { game.text["riley_stg4_pg1_para2"] }
            },
            options = listOf(
                IInteractionLogic.Option(
                    text = {
                        // Ask to visit father
                        if (RileyHubMission.choices.refusedPayment == true)
                            game.text["riley_stg4_pg1_opt1_ifNotPaid"]
                        else
                            game.text["riley_stg4_pg1_opt1_ifPaid"]
                    },
                    onOptionSelected = { navigator ->
                        if (RileyHubMission.choices.refusedPayment != true) {
                            dialog.textPanel.addCreditsGainText(mission.creditsReward)
                            game.sector.playerFleet.cargo.credits.add(mission.creditsReward.toFloat())
                            mission.setCreditReward(0)
                        }
                        navigator.goToPage(2)
                    }
                ),
                IInteractionLogic.Option(
                    // Ask if DJing pays well
                    showIf = { RileyHubMission.choices.askedAboutDJingPay == null },
                    text = { game.text["riley_stg4_pg1_opt2"] },
                    onOptionSelected = { navigator ->
                        para { game.text["riley_stg4_pg1_opt2_onSelected"] }
                        RileyHubMission.choices.askedAboutDJingPay = true
                        navigator.refreshOptions()
                    }
                ),
                IInteractionLogic.Option(
                    // Tell her to keep the money
                    showIf = { RileyHubMission.choices.refusedPayment == null },
                    text = { game.text["riley_stg4_pg1_opt3"] },
                    onOptionSelected = { navigator ->
                        RileyHubMission.choices.refusedPayment = true
                        mission.setCreditReward(0)
                        dialog.textPanel.adjustReputationWithPlayer(PerseanChroniclesNPCs.riley, .20f)
                        para { game.text["riley_stg4_pg1_opt3_onSelected"] }
                        navigator.refreshOptions()
                    }
                ),
                IInteractionLogic.Option(
                    // Leave without going to house
                    text = { game.text["riley_stg4_pg1_opt4"] },
                    onOptionSelected = { navigator ->
                        RileyHubMission.choices.visitedFather = false
                        mission.setCurrentStage(RileyHubMission.Stage.Completed, dialog, null)
                        navigator.promptToContinue(game.text["leave"]) {
                            navigator.close(doNotOfferAgain = true)
                        }
                    }
                )
            )
        ),
        IInteractionLogic.Page(
            id = 2,
            image = IInteractionLogic.Illustration("wisp_perseanchronicles_riley", "house"),
            people = { listOf(PerseanChroniclesNPCs.riley, PerseanChroniclesNPCs.riley_dad) },
            onPageShown = {
                // She is taken aback at first, but agrees to let you join, and you wind your way away from the landing pads, across rural, wooded countryside, and finally to a modest house nestled along a valley edge. Riley knocks, and a nurse lets you in.
                para { game.text["riley_stg4_pg2_para1"] }
                para { game.text["riley_stg4_pg2_para2"] }
                para { game.text["riley_stg4_pg2_para3"] }
            },
            options = listOf(
                // "Riley chartered a flight here aboard my ship. I heard so much about you on the way; it's a pleasure to meet you."
                IInteractionLogic.Option(
                    text = { game.text["riley_stg4_pg2_opt1"] },
                    onOptionSelected = { navigator ->
                        navigator.goToPage(3)
                        RileyHubMission.choices.complimentedRiley = false
                    }
                ),
                // "Riley and I have gotten to know each other on the long flight here. You've raised an incredible woman."
                IInteractionLogic.Option(
                    text = { game.text["riley_stg4_pg2_opt2"] },
                    onOptionSelected = { navigator ->
                        para { game.text["riley_stg4_pg2_opt2_onSelected"] }
                        RileyHubMission.choices.complimentedRiley = true
                        navigator.goToPage(3)
                    }
                )
            )
        ),
        IInteractionLogic.Page(
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
                            // His body relaxes, and he's gone.
                            dialog.visualPanel.hideSecondPerson()
                            para { game.text["riley_stg4_pg3_para5"] }
                        }
                    }
                }
            },
            options = listOf(
                IInteractionLogic.Option(
                    // Take a seat
                    text = { game.text["riley_stg4_pg3_opt1"] },
                    onOptionSelected = { navigator ->
                        para { game.text["riley_stg4_pg3_opt1_onSelected"] }
                        RileyHubMission.choices.heldRiley = false
                        navigator.goToPage(4)
                    }
                ),
                IInteractionLogic.Option(
                    // Hold her (needed for romance because it shows that you're a normal human being capable of empathy)
                    // Comfort her
                    text = { game.text["riley_stg4_pg3_opt2"] },
                    onOptionSelected = { navigator ->
                        RileyHubMission.choices.heldRiley = true
                        para { game.text["riley_stg4_pg3_opt2_onSelected"] }
                        dialog.textPanel.adjustReputationWithPlayer(PerseanChroniclesNPCs.riley, .2f)
                        navigator.goToPage(4)
                    }
                )
            )
        ),
        IInteractionLogic.Page(
            id = 4,
            onPageShown = {
                navigator.promptToContinue(game.text["continue"]) {
                    dialog.visualPanel.showSecondPerson(PerseanChroniclesNPCs.riley_dad2)
                    // Without warning, you hear Church's voice from a corner of the room. It's coming from an AI Core, held aloft by hundreds of cables dangling from the ceiling.
                    para { game.text["riley_stg4_pg4_para1"] }
                    navigator.promptToContinue(game.text["continue"]) {
                        para { game.text["riley_stg4_pg4_para2"] }
                    }
                }
            },
            options = listOf(
                IInteractionLogic.Option(
                    // Ask if legal
                    showIf = { RileyHubMission.choices.askedIfLegal == null },
                    // "Is this ...legal?"
                    text = { game.text["riley_stg4_pg4_opt1"] },
                    onOptionSelected = { navigator ->
                        RileyHubMission.choices.askedIfLegal = true
                        if (RileyHubMission.isFatherWorkingWithGovt) {
                            // "Well, no," he replies. "Not under ${rileyDestPlanetControllingFaction} law.
                            para { game.text["riley_stg4_pg4_opt1_onSelected_ifIllegal_para1"] }
                            // You recall that local laws grant a ==${rileyBountyCredits} reward== for turning in a mind upload.
                            para { game.text["riley_stg4_pg4_opt1_onSelected_ifIllegal_para2"] }
                        } else {
                            // "Here?" he replies. "There's no law against it. My only intention is to continue my work; nothing more."
                            para { game.text["riley_stg4_pg4_opt1_onSelected_ifLegal"] }
                        }
                        navigator.refreshOptions()
                    }
                ),
                IInteractionLogic.Option(
                    // Ask how Riley feels
                    showIf = { RileyHubMission.choices.askedWhatRileyThinks == null },
                    // "Riley, what do you think?"
                    text = { game.text["riley_stg4_pg4_opt2"] },
                    onOptionSelected = { navigator ->
                        RileyHubMission.choices.askedWhatRileyThinks = true
                        // "Dad, is that really you? How do you feel?"
                        para { game.text["riley_stg4_pg4_opt2_onSelected_para1"] }
                        para { game.text["riley_stg4_pg4_opt2_onSelected_para2"] }

                        navigator.promptToContinue(game.text["continue"]) {
                            para { game.text["riley_stg4_pg4_opt2_onSelected_para3"] }
                            navigator.refreshOptions() // fucked up here
                        }
                    }
                ),
                IInteractionLogic.Option(
                    // Try to convince her to come with you, after you've seen Riley talking to dad
                    showIf = {
                        RileyHubMission.choices.askedWhatRileyThinks == true
                                && RileyHubMission.choices.triedToConvinceToJoinYou == null
                    },
                    // "Are you sure? There's enough room for all of us on my ship. You can stay for as long as you want."
                    text = { game.text["riley_stg4_pg4_opt3"] },
                    onOptionSelected = { navigator ->
                        RileyHubMission.choices.triedToConvinceToJoinYou = true
                        para { game.text["riley_stg4_pg4_opt3_onSelected"] }
                        navigator.refreshOptions()
                    }
                ),
                IInteractionLogic.Option(
                    // Ask Sierra, after you've seen Riley talking to dad
                    showIf = {
                        runCatching {
                            game.settings.modManager.isModEnabled("secretsofthefrontier")
                                    && SotfMisc.playerHasSierra()
                                    && RileyHubMission.choices.askedWhatRileyThinks == true
                                    && RileyHubMission.choices.askedWhatSierraThinks == null
                        }.onFailure { game.logger.w(it) { "Not a crash. Error when checking if Sierra exists." } }
                            .getOrDefault(false)
                    },
                    // "Sierra, your thoughts?"
                    text = { game.text["riley_stg4_pg4_opt7_sierra"] },
                    onOptionSelected = { navigator ->
                        RileyHubMission.choices.askedWhatSierraThinks = true
                        para { game.text["riley_stg4_pg4_opt7_sierra_onSelected_para1"] }
                        para { game.text["riley_stg4_pg4_opt7_sierra_onSelected_para2"] }
                        navigator.refreshOptions()
                    }
                ),
                IInteractionLogic.Option(
                    // Leave
                    showIf = {
                        RileyHubMission.choices.askedWhatRileyThinks == true
                    },
                    // Say goodbye (leave)
                    text = { game.text["riley_stg4_pg4_opt4"] },
                    onOptionSelected = { navigator ->
                        RileyHubMission.choices.leftRileyWithFather = true
                        para { game.text["riley_stg4_pg4_opt4_onSelected_para1"] }

                        if (RileyHubMission.choices.complimentedRiley == true
                            && RileyHubMission.choices.heldRiley == true
                        ) {
                            // She catches your hand and pulls you in for a quick peck on the cheek, then a soft punch on the chest.
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
                IInteractionLogic.Option(
                    // Draw your gun and destroy the Core
                    text = { game.text["riley_stg4_pg4_opt5"] },
                    onOptionSelected = { navigator ->
                        RileyHubMission.choices.destroyedTheCore = true
                        // You advance on the Core and start firing at it.
                        para { game.text["riley_stg4_pg4_opt5_onSelected_para1"] }
                        para { game.text["riley_stg4_pg4_opt5_onSelected_para2"] }
                        dialog.visualPanel.hideSecondPerson()
                        // You blew up her fake dad
                        dialog.textPanel.adjustReputationWithPlayer(PerseanChroniclesNPCs.riley, -10f)
                        mission.setCurrentStage(RileyHubMission.Stage.Completed, dialog, null)
                        navigator.promptToContinue(game.text["leave"]) {
                            navigator.close(doNotOfferAgain = true)
                        }
                    }
                ),
                IInteractionLogic.Option(
                    // Turn the Core in for a ${rileyBountyCredits} bounty
                    showIf = {
                        RileyHubMission.choices.askedIfLegal == true
                                && RileyHubMission.isFatherWorkingWithGovt
                    },
                    text = { game.text["riley_stg4_pg4_opt6"] },
                    onOptionSelected = { navigator ->
                        RileyHubMission.choices.turnedInForABounty = true
                        para {
                            game.text.getf(
                                "riley_stg4_pg4_opt6_onSelected_para1",
                                "rileyDestPlanetControllingFactionWithoutArticle" to RileyHubMission.state.destinationPlanet?.faction?.displayName
                            )
                        }
                        para { game.text["riley_stg4_pg4_opt6_onSelected_para2"] }
                        dialog.visualPanel.hideSecondPerson()

                        // You condemned her fake dad to god knows what
                        dialog.textPanel.adjustReputationWithPlayer(PerseanChroniclesNPCs.riley, -10f)

                        // Rep reward from the faction that controls the planet
                        if (RileyHubMission.state.destinationPlanet?.market?.factionId != null) {
                            dialog.textPanel.adjustReputationWithPlayer(RileyHubMission.state.destinationPlanet?.market?.factionId!!, .05f)
                        }

                        game.sector.playerFleet.cargo.credits.add(RileyHubMission.BOUNTY_CREDITS.toFloat())
                        dialog.textPanel.addCreditsGainText(RileyHubMission.BOUNTY_CREDITS)
                        mission.setCurrentStage(RileyHubMission.Stage.Completed, dialog, null)
                        navigator.promptToContinue(game.text["leave"]) {
                            navigator.close(doNotOfferAgain = true)
                        }
                    }
                )
            )
        )
    )
)