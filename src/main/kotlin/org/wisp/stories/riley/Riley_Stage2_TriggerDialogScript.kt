package org.wisp.stories.riley

import com.fs.starfarer.api.EveryFrameScript
import org.wisp.stories.game
import wisp.questgiver.wispLib.equalsAny

class Riley_Stage2_TriggerDialogScript : EveryFrameScript {
    override fun isDone(): Boolean =
        !RileyQuest.stage.equalsAny(RileyQuest.Stage.NotStarted, RileyQuest.Stage.InitialTraveling)

    override fun runWhilePaused(): Boolean = false

    override fun advance(p0: Float) {
        val startDate = RileyQuest.state.startDate
        startDate ?: return

        if (game.sector.clock.getElapsedDaysSince(startDate) >= RileyQuest.DAYS_UNTIL_DIALOG) {
            RileyQuest.showDaysPassedDialog()
        }
    }

}