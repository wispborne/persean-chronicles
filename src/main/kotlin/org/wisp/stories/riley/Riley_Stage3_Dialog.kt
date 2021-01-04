package org.wisp.stories.riley

import org.wisp.stories.game
import wisp.questgiver.InteractionDefinition

class Riley_Stage3_Dialog : InteractionDefinition<Riley_Stage3_Dialog>(
    onInteractionStarted = {
        RileyQuest.startStage4()
    },
    pages = listOf(
        Page(
            id = 1,
            onPageShown = {
                para { game.text["riley_stg3_pg1_para1"] }
                para { game.text["riley_stg3_pg1_para2"] }
            },
            options = listOf(
                Option(
                    text = { game.text["riley_stg3_pg1_opt1"] },
                    onOptionSelected = { it.close(doNotOfferAgain = true) }
                )
            )
        )
    )
) {
    override fun createInstanceOfSelf(): InteractionDefinition<Riley_Stage3_Dialog> = Riley_Stage3_Dialog()
}