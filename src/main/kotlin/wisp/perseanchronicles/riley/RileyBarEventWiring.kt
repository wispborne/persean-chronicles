package wisp.perseanchronicles.riley

import wisp.questgiver.v2.BarEventWiring
import wisp.questgiver.v2.QGBarEventCreator

class RileyBarEventWiring :
    BarEventWiring<RileyHubMission>(missionId = RileyHubMission.MISSION_ID, isPriority = false) {
    override fun createBarEventLogic() = Riley_Stage1_BarEvent()
    override fun createMission() = RileyHubMission()
    override fun shouldBeAddedToBarEventPool(): Boolean {
        return RileyHubMission.state.completeDateInMillis == null
                && RileyHubMission.state.startDateMillis == null
    }

    override fun createBarEventCreator() = RileyBarEventCreator(this)
}

class RileyBarEventCreator(wiring: RileyBarEventWiring) : QGBarEventCreator<RileyHubMission>(wiring)