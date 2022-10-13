package wisp.perseanchronicles.nirvana

import wisp.perseanchronicles.game
import wisp.questgiver.BarEventWiring
import wisp.questgiver.QGBarEventCreator

class NirvanaBarEventWiring :
    BarEventWiring<NirvanaHubMission>(missionId = NirvanaHubMission.MISSION_ID, isPriority = false) {
    override fun createBarEventLogic() = Nirvana_Stage1_BarEvent()
    override fun createMission() = NirvanaHubMission()
    override fun shouldBeAddedToBarEventPool(): Boolean {
        return game.sector.playerStats.level >= 10
    }

    override fun createBarEventCreator() = NirvanaBarEventCreator(this)
}

class NirvanaBarEventCreator(wiring: NirvanaBarEventWiring) : QGBarEventCreator<NirvanaHubMission>(wiring)