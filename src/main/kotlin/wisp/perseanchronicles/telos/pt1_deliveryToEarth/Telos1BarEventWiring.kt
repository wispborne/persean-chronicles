package wisp.perseanchronicles.telos.pt1_deliveryToEarth

import wisp.questgiver.BarEventWiring

class Telos1BarEventWiring : BarEventWiring<Telos1HubMission>(Telos1HubMission.MISSION_ID) {
    override fun createBarEventLogic() = Telos1BarEventLogic()
    override fun createMission() = Telos1HubMission()
    override fun shouldOfferQuest() = Telos1HubMission.state.startDateMillis == null // ugh...
}