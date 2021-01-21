package org.wisp.stories.riley

import com.fs.starfarer.api.EveryFrameScript
import org.wisp.stories.game

class Riley_Stage2_TriggerDialogScript : EveryFrameScript {
    override fun isDone(): Boolean = RileyQuest.stage > RileyQuest.Stage.InitialTraveling

    override fun runWhilePaused(): Boolean = false

    override fun advance(p0: Float) {
        val startDate = RileyQuest.startDate
        startDate ?: return

        if (game.sector.clock.getElapsedDaysSince(startDate) >= RileyQuest.DAYS_UNTIL_DIALOG) {
            RileyQuest.showDaysPassedDialog()
        }
    }

}