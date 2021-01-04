package org.wisp.stories.riley

import com.fs.starfarer.api.campaign.BaseCampaignEventListener
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.JumpPointAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import org.wisp.stories.game

class EnteredDestinationSystemListener : BaseCampaignEventListener(false) {
    override fun reportFleetJumped(
        fleet: CampaignFleetAPI?,
        from: SectorEntityToken?,
        to: JumpPointAPI.JumpDestination?
    ) {
        if (RileyQuest.stage == RileyQuest.Stage.TravellingToSystem
            &&fleet == game.sector.playerFleet
            && to?.destination == RileyQuest.destinationPlanet
        ) {
            game.sector.removeListener(this)
            RileyQuest.showEnteredDestSystemDialog()
        }
    }
}