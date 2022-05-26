package wisp.perseanchronicles.telos.pt1_deliveryToEarth

import com.fs.starfarer.api.PluginPick
import com.fs.starfarer.api.campaign.BaseCampaignPlugin
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.combat.BattleCreationContext
import com.fs.starfarer.api.fleet.FleetGoal
import com.fs.starfarer.api.fleet.FleetMemberType
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes
import wisp.perseanchronicles.game

object Telos_Battle {
    class CampaignPlugin : BaseCampaignPlugin() {
        override fun pickBattleCreationPlugin(opponent: SectorEntityToken?): PluginPick<com.fs.starfarer.api.campaign.BattleCreationPlugin> =
            PluginPick(
                TelosBattleCreationPlugin(),
                com.fs.starfarer.api.campaign.CampaignPlugin.PickPriority.MOD_SPECIFIC
            )
    }

    class Context : BattleCreationContext(
        createTelosFleet(),
        FleetGoal.ATTACK,
        createTriTachFleet(),
        FleetGoal.ATTACK
    )

    fun createTelosFleet(): CampaignFleetAPI {
        return FleetFactoryV3.createEmptyFleet(Factions.INDEPENDENT, FleetTypes.TASK_FORCE, null)
            .apply {
                fleetData.addFleetMember(
                    game.factory.createFleetMember(
                        FleetMemberType.SHIP,
                        game.settings.getVariant("wolf_Strike")
                    )
                )
            }
    }

    fun createTriTachFleet(): CampaignFleetAPI {
        return FleetFactoryV3.createEmptyFleet(Factions.TRITACHYON, FleetTypes.TASK_FORCE, null)
            .apply {
                fleetData.addFleetMember(
                    game.factory.createFleetMember(
                        FleetMemberType.SHIP,
                        game.settings.getVariant("wolf_Strike")
                    )
                )
            }
    }
}