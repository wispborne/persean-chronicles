package wisp.perseanchronicles.laborer

import com.fs.starfarer.api.Global
import wisp.perseanchronicles.game
import wisp.questgiver.v2.IInteractionLogic
import wisp.questgiver.v2.InteractionDialogLogic
import wisp.questgiver.wispLib.findFirst

class Laborer_Stage2_Dialog(
    val mission: LaborerHubMission = Global.getSector().intelManager.findFirst()!!
) : InteractionDialogLogic<Laborer_Stage2_Dialog>(
    onInteractionStarted = { },
    pages = listOf(
        IInteractionLogic.Page(
            id = 1,
            people = { listOf(LaborerHubMission.dale) },
            onPageShown = {
                para { game.text["lab_stg2_pg1_para1"] }
                para { game.text["lab_stg2_pg1_para2"] }
                para { game.text["lab_stg2_pg1_para3"] }
            },
            options = listOf(
                IInteractionLogic.Option(
                    // "You have my bank address, right?"
                    text = { game.text["lab_stg2_pg1_opt1"] },
                    onOptionSelected = {
                        // He shoots you a look of thinly masked annoyance. "Yeah. I'll send you the credits when I have them. Bye, now."
                        para { game.text["lab_stg2_pg1_opt1_onSelected"] }
                        para { game.text["lab_stg2_pg2_para1"] }
                        mission.setCurrentStage(LaborerHubMission.Stage.Completed, dialog, null)
                        navigator.promptToContinue(game.text["leave"]) {
                            it.close(doNotOfferAgain = true)
                        }
                    }
                ),
                IInteractionLogic.Option(
                    // "I need that money now."
                    text = { game.text["lab_stg2_pg1_opt2"] },
                    onOptionSelected = {
                        // He shoots you an accused look. "You said I could pay you later. As we discussed, I'll send you the payment when I have it."
                        para { game.text["lab_stg2_pg1_opt2_onSelected"] }
                        para { game.text["lab_stg2_pg2_para1"] }
                        mission.setCurrentStage(LaborerHubMission.Stage.Completed, dialog, null)
                        navigator.promptToContinue(game.text["leave"]) {
                            it.close(doNotOfferAgain = true)
                        }
                    }
                ),
                IInteractionLogic.Option(
                    // "You're welcome. Better luck on this world."
                    text = { game.text["lab_stg2_pg1_opt3"] },
                    onOptionSelected = {
                        // He bobs his head at you in farewell. "I'll send you the payment when I have it."
                        para { game.text["lab_stg2_pg1_opt3_onSelected"] }
                        para { game.text["lab_stg2_pg2_para1"] }
                        mission.setCurrentStage(LaborerHubMission.Stage.Completed, dialog, null)
                        navigator.promptToContinue(game.text["leave"]) {
                            it.close(doNotOfferAgain = true)
                        }
                    }
                )
            )
        )
    )
)