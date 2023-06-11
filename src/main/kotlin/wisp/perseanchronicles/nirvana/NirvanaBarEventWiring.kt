package wisp.perseanchronicles.nirvana

import wisp.perseanchronicles.game
import wisp.questgiver.v2.BarEventWiring
import wisp.questgiver.v2.QGBarEventCreator

class NirvanaBarEventWiring :
    BarEventWiring<NirvanaHubMission>(missionId = NirvanaHubMission.MISSION_ID, isPriority = false) {
    override fun createBarEventLogic() = Nirvana_Stage1_BarEvent()
    override fun createMission() = NirvanaHubMission()
    override fun shouldBeAddedToBarEventPool(): Boolean {
        return game.sector.playerStats.level >= 8
                && NirvanaHubMission.state.completeDateInMillis == null
                && NirvanaHubMission.state.startDateMillis == null
    }

    override fun createBarEventCreator() = NirvanaBarEventCreator(this)
}

class NirvanaBarEventCreator(wiring: NirvanaBarEventWiring) : QGBarEventCreator<NirvanaHubMission>(wiring)