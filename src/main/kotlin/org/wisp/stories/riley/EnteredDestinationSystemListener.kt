package org.wisp.stories.riley

import com.fs.starfarer.api.campaign.BaseCampaignEventListener
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.JumpPointAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import org.wisp.stories.game
import wisp.questgiver.wispLib.equalsAny

class EnteredDestinationSystemListener : BaseCampaignEventListener(false) {
    override fun reportFleetJumped(
        fleet: CampaignFleetAPI?,
        from: SectorEntityToken?,
        to: JumpPointAPI.JumpDestination?
    ) {
        if (RileyQuest.stage.equalsAny(RileyQuest.Stage.TravellingToSystem, RileyQuest.Stage.InitialTraveling)
            && fleet == game.sector.playerFleet
            && to?.destination?.starSystem == RileyQuest.destinationPlanet?.starSystem
        ) {
            game.sector.removeListener(this)
            RileyQuest.showEnteredDestSystemDialog()
        }
    }
}