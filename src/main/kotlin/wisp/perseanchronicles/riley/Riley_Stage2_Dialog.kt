package wisp.perseanchronicles.riley

import wisp.perseanchronicles.game
import wisp.questgiver.InteractionDefinition

class Riley_Stage2_Dialog : InteractionDefinition<Riley_Stage2_Dialog>(
    onInteractionStarted = {
        RileyQuest.startStage3()
    },
    pages = listOf(
        Page(
            id = 1,
            image = RileyQuest.icon,
            onPageShown = {
                para { game.text["riley_stg2_pg1_para1"] }
                navigator.promptToContinue(game.text["continue"]) {
                    para { game.text["riley_stg2_pg1_para2"] }
                    navigator.promptToContinue(game.text["continue"]) {
                        para { game.text["riley_stg2_pg1_para3"] }
                    }
                }
            },
            options = listOf(
                Option(
                    text = { game.text["riley_stg2_pg1_opt1"] },
                    onOptionSelected = { it.close(doNotOfferAgain = true) }
                )
            )
        )
    )
) {
    override fun createInstanceOfSelf(): InteractionDefinition<Riley_Stage2_Dialog> = Riley_Stage2_Dialog()
}