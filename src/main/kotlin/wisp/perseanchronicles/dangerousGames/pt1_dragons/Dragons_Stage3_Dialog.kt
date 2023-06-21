package wisp.perseanchronicles.dangerousGames.pt1_dragons

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.util.Misc
import wisp.perseanchronicles.common.PerseanChroniclesNPCs
import wisp.perseanchronicles.game
import wisp.questgiver.v2.IInteractionLogic
import wisp.questgiver.v2.InteractionDialogLogic
import wisp.questgiver.wispLib.findFirst

class Dragons_Stage3_Dialog(val dragons: DragonsHubMission = Global.getSector().intelManager.findFirst()!!) :
    InteractionDialogLogic<Dragons_Stage3_Dialog>(
        onInteractionStarted = {},
        pages = listOf(
            IInteractionLogic.Page(
                id = 1,
                people = { listOf(PerseanChroniclesNPCs.karengo) },
                onPageShown = {
                    para {
                        if (dialog.interactionTarget.faction.isHostileTo(Factions.PLAYER)) {
                            game.text["dg_dr_stg3_pg1_para1_ifHostile"]
                        } else {
                            game.text["dg_dr_stg3_pg1_para1"]
                        }
                    }
                },
                options = listOf(
                    IInteractionLogic.Option(
                        text = { game.text["dg_dr_stg3_pg1_opt1"] },
                        onOptionSelected = { it.goToPage(2) }
                    )
                )
            ),
            IInteractionLogic.Page(
                id = 2,
                onPageShown = {
                    para { game.text["dg_dr_stg3_pg2_para1"] }
                    para {
                        game.text["dg_dr_stg3_pg2_para2"]
                    }
                },
                options = listOf(
                    IInteractionLogic.Option(
                        text = { game.text["dg_dr_stg3_pg2_opt1"] },
                        onOptionSelected = { it.goToPage(3) }
                    ),
                    IInteractionLogic.Option(
                        text = { game.text["dg_dr_stg3_pg2_opt2"] },
                        onOptionSelected = {
                            para { game.text["dg_dr_stg3_pg2_opt2_para1"] }
                            it.goToPage(3)
                        }
                    ),
                    IInteractionLogic.Option(
                        text = { game.text["dg_dr_stg3_pg2_opt3"] },
                        onOptionSelected = {
                            para { game.text["dg_dr_stg3_pg2_opt3_para1"] }
                            it.goToPage(3)
                        }
                    )
                )
            ),
            IInteractionLogic.Page(
                id = 3,
                onPageShown = {
                    para {
                        game.text.getf(
                            key = "dg_dr_stg3_pg3_para1",
                            values = mapOf("rewardCredits" to Misc.getDGSCredits(dragons.creditsReward.toFloat()))
                        )
                    }
                },
                options = listOf(
                    IInteractionLogic.Option(
                        text = { game.text["dg_dr_stg3_pg3_opt1"] },
                        onOptionSelected = { it.goToPage(4) }
                    ),
                    IInteractionLogic.Option(
                        text = { game.text["dg_dr_stg3_pg3_opt2"] },
                        onOptionSelected = {
                            para { game.text["dg_dr_stg3_pg3_opt2_para1"] }
                            it.goToPage(4)
                        }
                    )
                )
            ),
            IInteractionLogic.Page(
                id = 4,
                onPageShown = {
                    para { game.text["dg_dr_stg3_pg4_para1"] }
                    dragons.setCurrentStage(DragonsHubMission.Stage.Done, dialog, null)
                },
                options = listOf(
                    IInteractionLogic.Option(
                        text = { game.text["dg_dr_stg3_pg4_para1_opt1"] },
                        onOptionSelected = {
                            val interactionTarget = dialog.interactionTarget
                            it.close(doNotOfferAgain = true)
                            // Show normal planet dialog after quest finishes
                            game.sector.campaignUI.showInteractionDialog(interactionTarget)
                        }
                    )
                )
            )
        )
    )