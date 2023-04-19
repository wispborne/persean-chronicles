package wisp.perseanchronicles.telos.pt1_deliveryToEarth

import wisp.questgiver.BarEventWiring
import wisp.questgiver.QGBarEventCreator

class Telos1BarEventWiring :
    BarEventWiring<Telos1HubMission>(missionId = Telos1HubMission.MISSION_ID, isPriority = false) {
    override fun createBarEventLogic() = Telos1BarEventLogic()
    override fun createMission() = Telos1HubMission()
    override fun shouldBeAddedToBarEventPool() = Telos1HubMission.shouldAddToBarEventPool()
    override fun createBarEventCreator() = Telo1BarEventCreator(this)
    class Telo1BarEventCreator(wiring: Telos1BarEventWiring) : QGBarEventCreator<Telos1HubMission>(wiring)
}