package wisp.perseanchronicles.dangerousGames.pt2_depths

import wisp.perseanchronicles.dangerousGames.pt1_dragons.DragonsHubMission
import wisp.perseanchronicles.game
import wisp.questgiver.v2.BarEventWiring
import wisp.questgiver.v2.QGBarEventCreator

class DepthsBarEventWiring :
    BarEventWiring<DepthsHubMission>(missionId = DepthsHubMission.MISSION_ID, isPriority = false) {
    override fun createBarEventLogic() = DepthsBarEventLogic()
    override fun createMission() = DepthsHubMission()
    override fun shouldBeAddedToBarEventPool() = DepthsHubMission.shouldBeAddedToBarEventPool()

    override fun createBarEventCreator() = DepthsBarEventCreator(this)
}

class DepthsBarEventCreator(wiring: DepthsBarEventWiring) : QGBarEventCreator<DepthsHubMission>(wiring)