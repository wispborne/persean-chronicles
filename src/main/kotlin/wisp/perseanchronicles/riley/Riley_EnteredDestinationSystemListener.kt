package wisp.perseanchronicles.riley

import com.fs.starfarer.api.campaign.BaseCampaignEventListener
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.JumpPointAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import wisp.perseanchronicles.game
import wisp.questgiver.wispLib.equalsAny

class Riley_EnteredDestinationSystemListener(val mission: RileyHubMission) : BaseCampaignEventListener(false) {
    override fun reportFleetJumped(
        fleet: CampaignFleetAPI?,
        from: SectorEntityToken?,
        to: JumpPointAPI.JumpDestination?
    ) {
        if (mission.currentStage.equalsAny(
                RileyHubMission.Stage.InitialTraveling,
                RileyHubMission.Stage.TravellingToSystem
            )
            && fleet == game.sector.playerFleet
            && to?.destination?.starSystem == RileyHubMission.state.destinationPlanet?.starSystem
        ) {
            game.sector.removeListener(this)
            mission.showEnteredDestSystemDialog()
        }
    }
}