package wisp.perseanchronicles.riley

import wisp.perseanchronicles.game
import wisp.questgiver.v2.IInteractionLogic
import wisp.questgiver.v2.InteractionDialogLogic
import wisp.questgiver.wispLib.findFirst

/**
 * Triggered after 3 days pass.
 */
class Riley_Stage2_Dialog(
    val mission: RileyHubMission = game.intelManager.findFirst()!!
) : InteractionDialogLogic() {
    override fun onInteractionStarted() {
        mission.setCurrentStage(RileyHubMission.Stage.TravellingToSystem, null, emptyMap())
    }

    override fun people() = { listOf(RileyHubMission.riley) }
    override fun pages() = listOf(
        IInteractionLogic.Page(
            id = 1,
            image = RileyHubMission.icon,
            onPageShown = {
                // In the days since Riley came aboard, she has relaxed and cleaned up. She looks nearly unrecognizable from the tired, desperate woman you first saw.
                para { game.text["riley_stg2_pg1_para1"] }
                navigator.promptToContinue(game.text["continue"]) {
                    para { game.text["riley_stg2_pg1_para2"] }
                    navigator.promptToContinue(game.text["continue"]) {
                        para { game.text["riley_stg2_pg1_para3"] }
                    }
                }
            },
            options = listOf(
                IInteractionLogic.Option(
                    text = { game.text["riley_stg2_pg1_opt1"] },
                    onOptionSelected = { it.close(doNotOfferAgain = true) }
                )
            )
        )
    )
}