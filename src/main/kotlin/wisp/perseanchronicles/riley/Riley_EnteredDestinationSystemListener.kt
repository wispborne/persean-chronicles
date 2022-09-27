package wisp.perseanchronicles.riley

import com.fs.starfarer.api.campaign.BaseCampaignEventListener
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.JumpPointAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import wisp.perseanchronicles.game
import wisp.questgiver.wispLib.equalsAny
import wisp.questgiver.wispLib.findFirst

class Riley_EnteredDestinationSystemListener(
    val mission: RileyHubMission = game.intelManager.findFirst()!!
) : BaseCampaignEventListener(false) {
    override fun reportFleetJumped(
        fleet: CampaignFleetAPI?,
        from: SectorEntityToken?,
        to: JumpPointAPI.JumpDestination?
    ) {
        if (mission.currentStage.equalsAny(
                RileyHubMission.Stage.TravellingToSystem,
                RileyHubMission.Stage.InitialTraveling
            )
            && fleet == game.sector.playerFleet
            && to?.destination?.starSystem == RileyHubMission.state.destinationPlanet?.starSystem
        ) {
            game.sector.removeListener(this)
            mission.showEnteredDestSystemDialog()
        }
    }
}