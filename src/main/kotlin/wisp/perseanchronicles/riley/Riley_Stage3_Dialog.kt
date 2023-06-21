package wisp.perseanchronicles.riley

import com.fs.starfarer.api.Global
import wisp.perseanchronicles.game
import wisp.questgiver.v2.IInteractionLogic
import wisp.questgiver.v2.InteractionDialogLogic
import wisp.questgiver.wispLib.findFirst

class Riley_Stage3_Dialog(
    val mission: RileyHubMission = Global.getSector().intelManager.findFirst()!!
) : InteractionDialogLogic<Riley_Stage3_Dialog>(
    onInteractionStarted = { },
    pages = listOf(
        IInteractionLogic.Page(
            id = 1,
            image = RileyHubMission.icon,
            onPageShown = {
                para { game.text["riley_stg3_pg1_para1"] }
                para { game.text["riley_stg3_pg1_para2"] }
                dialog.visualPanel.hideSecondPerson()
                mission.setCurrentStage(RileyHubMission.Stage.LandingOnPlanet, null, null)
            },
            options = listOf(
                IInteractionLogic.Option(
                    // Close
                    text = { game.text["riley_stg3_pg1_opt1"] },
                    onOptionSelected = { it.close(doNotOfferAgain = true) }
                )
            )
        )
    )
)