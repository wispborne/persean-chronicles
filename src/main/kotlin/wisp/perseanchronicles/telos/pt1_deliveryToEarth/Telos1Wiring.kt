package wisp.perseanchronicles.telos.pt1_deliveryToEarth

import wisp.questgiver.Questgiver

class Telos1Wiring : Questgiver.QGWiring<Telos1HubMission>(Telos1HubMission.MISSION_ID) {
    override fun createBarEventLogic() = Telos1BarEventLogic()
    override fun createMission() = Telos1HubMission()
}