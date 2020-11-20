package org.wisp.stories.dangerousGames.pt1_dragons

import org.wisp.stories.game
import wisp.questgiver.InteractionDefinition
import wisp.questgiver.wispLib.lastName

class DragonsPart2_EndingDialog : InteractionDefinition<DragonsPart2_EndingDialog>(
    onInteractionStarted = {},
    pages = listOf(
        Page(
            id = 1,
            onPageShown = {
                addPara { game.words["dg_dr_stg3_pg1_para1"] }
            },
            options = listOf(
                Option(
                    text = { game.words["dg_dr_stg3_pg1_opt1"] },
                    onOptionSelected = { it.goToPage(2) }
                )
            )
        ),
        Page(
            id = 2,
            onPageShown = {
                addPara { game.words["dg_dr_stg3_pg2_para1"] }
                addPara {
                    game.words.fmt(
                        "dg_dr_stg3_pg2_para2",
                        mapOf("playerLastName" to game.sector.playerPerson.lastName)
                    )
                }
            },
            options = listOf(
                Option(
                    text = { game.words["dg_dr_stg3_pg2_opt1"] },
                    onOptionSelected = { it.goToPage(3) }
                ),
                Option(
                    text = { game.words["dg_dr_stg3_pg2_opt2"] },
                    onOptionSelected = {
                        addPara { game.words["dg_dr_stg3_pg2_opt2_para1"] }
                        it.goToPage(3)
                    }
                ),
                Option(
                    text = { game.words["dg_dr_stg3_pg2_opt3"] },
                    onOptionSelected = {
                        addPara { game.words["dg_dr_stg3_pg2_opt3_para1"] }
                        it.goToPage(3)
                    }
                )
            )
        ),
        Page(
            id = 3,
            onPageShown = {
                addPara {
                    game.words.fmt(
                        "dg_dr_stg3_pg3_para1",
                        mapOf("rewardCredits" to DragonsQuest.rewardCredits)
                    )
                }
            },
            options = listOf(
                Option(
                    text = { game.words["dg_dr_stg3_pg3_opt1"] },
                    onOptionSelected = { it.goToPage(4) }
                ),
                Option(
                    text = { game.words["dg_dr_stg3_pg3_opt2"] },
                    onOptionSelected = {
                        addPara { game.words["dg_dr_stg3_pg3_opt2_para1"] }
                        it.goToPage(4)
                    }
                )
            )
        ),
        Page(
            id = 4,
            onPageShown = {
                addPara { game.words["dg_dr_stg3_pg4_para1"] }
            },
            options = listOf(
                Option(
                    text = { game.words["dg_dr_stg3_pg4_para1_opt1"] },
                    onOptionSelected = {
                        DragonsQuest.finishStage2()
                        val interactionTarget = dialog.interactionTarget
                        it.close(doNotOfferAgain = true)
                        // Show normal planet dialog after quest finishes
                        game.sector.campaignUI.showInteractionDialog(interactionTarget)
                    }
                )
            )
        )
    )
) {
    override fun createInstanceOfSelf() = DragonsPart2_EndingDialog()
}