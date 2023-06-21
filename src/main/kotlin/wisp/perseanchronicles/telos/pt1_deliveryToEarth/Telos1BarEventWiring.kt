package wisp.perseanchronicles.telos.pt1_deliveryToEarth

import wisp.perseanchronicles.dangerousGames.pt2_depths.DepthsHubMission
import wisp.perseanchronicles.game
import wisp.questgiver.v2.BarEventWiring
import wisp.questgiver.v2.QGBarEventCreator

class Telos1BarEventWiring :
    BarEventWiring<Telos1HubMission>(missionId = Telos1HubMission.MISSION_ID, isPriority = false) {
    override fun createBarEventLogic() = Telos1BarEventLogic()
    override fun createMission() = Telos1HubMission()

    /**
     * Add to bar event pool if we haven't started this one yet and we've completed Depths at least a month ago.
     */
    override fun shouldBeAddedToBarEventPool() =
        Telos1HubMission.state.startDateMillis == null &&
                (DepthsHubMission.state.completeDateInMillis != null)
                && (game.sector.clock.getElapsedDaysSince(DepthsHubMission.state.completeDateInMillis!!) >= 30 || game.settings.isDevMode)

    override fun createBarEventCreator() = Telo1BarEventCreator(this)
    class Telo1BarEventCreator(wiring: Telos1BarEventWiring) : QGBarEventCreator<Telos1HubMission>(wiring)
}