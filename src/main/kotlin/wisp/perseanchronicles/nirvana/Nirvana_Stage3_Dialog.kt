package wisp.perseanchronicles.nirvana

import wisp.perseanchronicles.game
import wisp.questgiver.v2.IInteractionLogic
import wisp.questgiver.v2.InteractionDialogLogic

class Nirvana_Stage3_Dialog : InteractionDialogLogic<Nirvana_Stage3_Dialog>(
    onInteractionStarted = {
        NirvanaQuest.completeSecret()
    },
    pages = listOf(
        IInteractionLogic.Page(
            id = 1,
            onPageShown = {
                para { game.text["nirv_stg3_pg1_para1"] }
                navigator.promptToContinue(game.text["continue"]) {
                    para { game.text["nirv_stg3_pg1_para2"] }
                    para { game.text["nirv_stg3_pg1_para3"] }
                }
            },
            options = listOf(
                IInteractionLogic.Option(
                    // And what of the observation of other sectors?
                    text = { game.text["nirv_stg3_pg1_opt1"] },
                    onOptionSelected = { it.goToPage(2) }
                )
            )
        ),
        IInteractionLogic.Page(
            id = 2,
            onPageShown = {
                para { game.text["nirv_stg3_pg2_para1"] }
                navigator.promptToContinue(game.text["continue"]) {
                    para { game.text["nirv_stg3_pg2_para3"] }
                }
            },
            options = listOf(
                IInteractionLogic.Option(
                    // leave
                    text = { game.text["nirv_stg3_pg2_opt1"] },
                    onOptionSelected = {
                        it.close(doNotOfferAgain = true)
                    }
                )
            )
        )
    )
)