package wisp.perseanchronicles.riley

import com.fs.starfarer.api.Global
import wisp.perseanchronicles.game
import wisp.questgiver.InteractionDefinition
import wisp.questgiver.wispLib.findFirst

class Riley_Stage3_Dialog(
    val mission: RileyHubMission = Global.getSector().intelManager.findFirst()!!
) : InteractionDefinition<Riley_Stage3_Dialog>(
    onInteractionStarted = { },
    pages = listOf(
        Page(
            id = 1,
            image = RileyHubMission.icon,
            onPageShown = {
                para { game.text["riley_stg3_pg1_para1"] }
                para { game.text["riley_stg3_pg1_para2"] }
                mission.setCurrentStage(RileyHubMission.Stage.LandingOnPlanet, dialog, null)
            },
            options = listOf(
                Option(
                    // Close
                    text = { game.text["riley_stg3_pg1_opt1"] },
                    onOptionSelected = { it.close(doNotOfferAgain = true) }
                )
            )
        )
    )
) {
    override fun createInstanceOfSelf(): InteractionDefinition<Riley_Stage3_Dialog> = Riley_Stage3_Dialog()
}