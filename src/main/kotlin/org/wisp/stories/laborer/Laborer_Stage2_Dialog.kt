package org.wisp.stories.laborer

import org.wisp.stories.game
import wisp.questgiver.InteractionDefinition

class Laborer_Stage2_Dialog : InteractionDefinition<Laborer_Stage2_Dialog>(
    onInteractionStarted = { },
    pages = listOf(
        Page(
            id = 1,
            onPageShown = {
                para { game.text["lab_stg2_pg1_para1"] }
                para { game.text["lab_stg2_pg1_para2"] }
                para { game.text["lab_stg2_pg1_para3"] }
            },
            options = listOf(
                Option(
                    text = { game.text["lab_stg2_pg1_opt1"] },
                    onOptionSelected = {
                        para { game.text["lab_stg2_pg1_opt1_onSelected"] }
                        para { game.text["lab_stg2_pg2_para1"] }
                        navigator.promptToContinue(game.text["leave"]) {
                            LaborerQuest.complete()
                            it.close(doNotOfferAgain = true)
                        }
                    }
                ),
                Option(
                    text = { game.text["lab_stg2_pg1_opt2"] },
                    onOptionSelected = {
                        para { game.text["lab_stg2_pg1_opt2_onSelected"] }
                        para { game.text["lab_stg2_pg2_para1"] }
                        navigator.promptToContinue(game.text["leave"]) {
                            LaborerQuest.complete()
                            it.close(doNotOfferAgain = true)
                        }
                    }
                ),
                Option(
                    text = { game.text["lab_stg2_pg1_opt3"] },
                    onOptionSelected = {
                        para { game.text["lab_stg2_pg1_opt3_onSelected"] }
                        para { game.text["lab_stg2_pg2_para1"] }
                        navigator.promptToContinue(game.text["leave"]) {
                            LaborerQuest.complete()
                            it.close(doNotOfferAgain = true)
                        }
                    }
                )
            )
        )
    )
) {
    override fun createInstanceOfSelf(): InteractionDefinition<Laborer_Stage2_Dialog> = Laborer_Stage2_Dialog()
}