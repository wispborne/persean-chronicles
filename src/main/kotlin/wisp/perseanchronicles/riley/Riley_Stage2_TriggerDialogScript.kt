package wisp.perseanchronicles.riley

import com.fs.starfarer.api.EveryFrameScript
import wisp.perseanchronicles.game
import wisp.questgiver.wispLib.equalsAny
import wisp.questgiver.wispLib.findFirst

class Riley_Stage2_TriggerDialogScript(
    val mission: RileyHubMission = game.intelManager.findFirst()!!
) : EveryFrameScript {
    override fun isDone(): Boolean =
        !mission.currentStage.equalsAny(RileyHubMission.Stage.NotStarted, RileyHubMission.Stage.InitialTraveling)

    override fun runWhilePaused(): Boolean = false

    override fun advance(p0: Float) {
        val startDate = RileyHubMission.state.startDateMillis
        startDate ?: return

        if (game.sector.clock.getElapsedDaysSince(startDate) >= RileyHubMission.DAYS_UNTIL_DIALOG) {
            mission.showDaysPassedDialog()
        }
    }
}