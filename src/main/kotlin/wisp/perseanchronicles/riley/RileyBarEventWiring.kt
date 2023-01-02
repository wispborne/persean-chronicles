package wisp.perseanchronicles.riley

import wisp.perseanchronicles.dangerousGames.pt1_dragons.DragonsHubMission
import wisp.perseanchronicles.game
import wisp.questgiver.BarEventWiring
import wisp.questgiver.QGBarEventCreator

class RileyBarEventWiring :
    BarEventWiring<RileyHubMission>(missionId = RileyHubMission.MISSION_ID, isPriority = false) {
    override fun createBarEventLogic() = Riley_Stage1_BarEvent()
    override fun createMission() = RileyHubMission()
    override fun shouldBeAddedToBarEventPool(): Boolean {
        return DragonsHubMission.state.completeDateInMillis != null
                && game.sector.clock.getElapsedDaysSince(DragonsHubMission.state.completeDateInMillis!!) >= 30
    }

    override fun createBarEventCreator() = RileyBarEventCreator(this)
}

class RileyBarEventCreator(wiring: RileyBarEventWiring) : QGBarEventCreator<RileyHubMission>(wiring)