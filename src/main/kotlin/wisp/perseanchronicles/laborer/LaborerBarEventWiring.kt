package wisp.perseanchronicles.laborer

import wisp.questgiver.BarEventWiring
import wisp.questgiver.QGBarEventCreator

class LaborerBarEventWiring :
    BarEventWiring<LaborerHubMission>(missionId = LaborerHubMission.MISSION_ID, isPriority = false) {
    override fun createBarEventLogic() = Laborer_Stage1_BarEvent()
    override fun createMission() = LaborerHubMission()
    override fun shouldBeAddedToBarEventPool(): Boolean {
        return LaborerHubMission.state.completeDateInMillis != null
    }

    override fun createBarEventCreator() = LaborerBarEventCreator(this)
}

class LaborerBarEventCreator(wiring: LaborerBarEventWiring) : QGBarEventCreator<LaborerHubMission>(wiring)