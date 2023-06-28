package wisp.perseanchronicles.dangerousGames.pt2_depths

import wisp.perseanchronicles.game
import wisp.questgiver.v2.IInteractionLogic
import wisp.questgiver.v2.InteractionDialogLogic
import wisp.questgiver.wispLib.findFirst

class Depths_Stage2_EndDialog(val mission: DepthsHubMission = game.intelManager.findFirst()!!) :
    InteractionDialogLogic<Depths_Stage2_EndDialog>(
        pages = listOf(
            IInteractionLogic.Page(
                id = 1,
                onPageShown = {
                    para {
                        game.text["dg_de_stg3_backToStart_pg1_para1"]
                    }
                    para {
                        val gatesAwakenedRemainingCodesMemKey = "\$GatesAwakened_activation_codes_remaining"
                        val gatesAwakenedSecondQuestDoneMemKey = "\$GatesAwakened_mid_quest_done"

                        // RIP
                        if (game.settings.modManager.isModEnabled("gates_awakened") &&
                            game.memory[gatesAwakenedSecondQuestDoneMemKey] == true &&
                            game.memory[gatesAwakenedRemainingCodesMemKey] as? Int != null
                        ) {
                            when (mission.riddleSuccessesCount) {
                                0, 1, 2 -> {
                                    // Give one Gate Code
                                    game.memory[gatesAwakenedRemainingCodesMemKey] =
                                        game.memory[gatesAwakenedRemainingCodesMemKey] as Int + 1
                                    game.text["dg_de_stg3_backToStart_pg1_para2_ifFailedSomeRiddles"]
                                }

                                3 -> {
                                    // Give two Gate Codes
                                    game.memory[gatesAwakenedRemainingCodesMemKey] =
                                        game.memory[gatesAwakenedRemainingCodesMemKey] as Int + 2
                                    game.text["dg_de_stg3_backToStart_pg1_para2_ifFailedNoRiddles"]
                                }

                                else -> {
                                    "Unexpected success count ${mission.riddleSuccessesCount}"
                                }
                            }
                        } else
                            game.text["dg_de_stg3_backToStart_pg1_para2_ifGatesAwakenedMidQuestNotDone"]
                    }
                    para { game.text["dg_de_stg3_backToStart_pg1_para3"] }
                    para { game.text["dg_de_stg3_backToStart_pg1_para4"] }
                    para { game.text["dg_de_stg3_backToStart_pg1_para5"] }

                    navigator.promptToContinue(game.text["continue"]) {
                        para { game.text["dg_de_stg3_backToStart_pg1_para6"] }
                        mission.setCurrentStage(DepthsHubMission.Stage.Done, dialog, null)
                    }
                },
                options = listOf(
                    // Leave
                    IInteractionLogic.Option(text = { game.text["dg_de_stg3_backToStart_pg1_opt1"] },
                        onOptionSelected = {
                            it.close(doNotOfferAgain = true)
                        }
                    )
                )
            ))
    )