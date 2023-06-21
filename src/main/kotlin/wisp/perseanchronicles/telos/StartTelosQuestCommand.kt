package wisp.perseanchronicles.telos

import com.fs.starfarer.api.util.Misc
import org.lazywizard.console.BaseCommand
import wisp.perseanchronicles.game
import wisp.perseanchronicles.telos.pt1_deliveryToEarth.Telos1HubMission
import wisp.questgiver.v2.HubMissionBarEventWrapperWithoutRules

class StartTelosQuestCommand : BaseCommand {
    override fun runCommand(args: String, context: BaseCommand.CommandContext): BaseCommand.CommandResult {
        if (!context.isInCampaign) {
            return BaseCommand.CommandResult.WRONG_CONTEXT
        }

        val market = Misc.findNearestLocalMarket(game.sector.playerFleet, 10000f, null)
        val wrapper = object : HubMissionBarEventWrapperWithoutRules<Telos1HubMission>(Telos1HubMission.MISSION_ID) {
            override fun createMission(): Telos1HubMission = Telos1HubMission()
        }

            wrapper.apply {
                this.shouldShowAtMarket(market)
//                this.addPromptAndOption(, memoryMap))
            }
            .mission.run { this as Telos1HubMission }
            .apply {
            if (create(market, false)) {
                accept(null, null)
            } else {
                return BaseCommand.CommandResult.ERROR
            }
        }
        return BaseCommand.CommandResult.SUCCESS
    }
}