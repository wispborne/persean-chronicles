package org.wisp.stories.nirvana

import org.wisp.stories.game
import wisp.questgiver.InteractionDefinition

class Nirvana_Stage3_Dialog : InteractionDefinition<Nirvana_Stage3_Dialog>(
    onInteractionStarted = {
        NirvanaQuest.completeSecret()
    },
    pages = listOf(
        Page(
            id = 1,
            onPageShown = {
                para { game.text["nirv_stg3_pg1_para1"] }
                navigator.promptToContinue(game.text["continue"]) {
                    para { game.text["nirv_stg3_pg1_para2"] }
                    para { game.text["nirv_stg3_pg1_para3"] }
                }
            },
            options = listOf(
                Option(
                    // And what of the observation of other sectors?
                    text = { game.text["nirv_stg3_pg1_opt1"] },
                    onOptionSelected = { it.goToPage(2) }
                )
            )
        ),
        Page(
            id = 2,
            onPageShown = {
                para { game.text["nirv_stg3_pg2_para1"] }
                navigator.promptToContinue(game.text["continue"]) {
                    para { game.text["nirv_stg3_pg2_para3"] }
                }
            },
            options = listOf(
                Option(
                    // leave
                    text = { game.text["nirv_stg3_pg2_opt1"] },
                    onOptionSelected = {
                        it.close(doNotOfferAgain = true)
                    }
                )
            )
        )
    )
) {
    override fun createInstanceOfSelf(): InteractionDefinition<Nirvana_Stage3_Dialog> = Nirvana_Stage3_Dialog()
}