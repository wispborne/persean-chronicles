package wisp.perseanchronicles.telos.pt2_dart

import com.fs.starfarer.api.PluginPick
import com.fs.starfarer.api.campaign.BaseCampaignPlugin
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.BattleCreationContext
import com.fs.starfarer.api.fleet.FleetGoal
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.mission.FleetSide
import org.lwjgl.util.vector.Vector2f
import wisp.perseanchronicles.game
import wisp.questgiver.wispLib.addShipVariant
import wisp.questgiver.wispLib.findFirst
import java.util.*

object Telos2Battle {
    class CampaignPlugin : BaseCampaignPlugin() {
        override fun pickBattleCreationPlugin(opponent: SectorEntityToken?): PluginPick<com.fs.starfarer.api.campaign.BattleCreationPlugin> =
            PluginPick(
                Telos2BattleCreationPlugin(),
                com.fs.starfarer.api.campaign.CampaignPlugin.PickPriority.MOD_SPECIFIC
            )
    }

    class Context : BattleCreationContext(
        createTelosFleet(),
        FleetGoal.ATTACK,
        createInitialHegemonyFleet(),
        FleetGoal.ATTACK
    ) {

    }

    fun startBattle() {
        game.sector.registerPlugin(CampaignPlugin())
        game.sector.campaignUI.startBattle(Context())

        game.combatEngine.addPlugin(object : BaseEveryFrameCombatPlugin() {
            override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {
                if (game.combatEngine.getTotalElapsedTime(false) > 5) {
                    createInitialHegemonyFleet().fleetData.membersListCopy.forEach {
                        game.combatEngine.getFleetManager(FleetSide.ENEMY)
                            .spawnFleetMember(
                                it,
                                Vector2f(game.combatEngine.mapWidth / 2f, game.combatEngine.mapHeight / 2f),
                                0f,
                                3f
                            )
                    }
                    game.combatEngine.removePlugin(this)
                }
            }
        })
    }

    /**
     * The side the player is fighting on, with just enough firepower to take out the initial fleet.
     */
    fun createTelosFleet(): CampaignFleetAPI {
        return FleetFactoryV3.createEmptyFleet(Factions.INDEPENDENT, FleetTypes.TASK_FORCE, null)
            .apply {
                this.addShipVariant(variantId = "shrike_Attack", count = 1)
                this.addShipVariant(variantId = "kite_original_Stock", count = 3)
                // todo add some Telos ships.
                this.fleetData.sort()
                this.fleetData.setSyncNeeded()
                this.fleetData.syncIfNeeded()
            }
    }

    /**
     * Create the initial, easy force for the player to defeat in their new Dart.
     */
    fun createInitialHegemonyFleet(): CampaignFleetAPI {
        return FleetFactoryV3.createEmptyFleet(Factions.HEGEMONY, FleetTypes.TASK_FORCE, null).apply {
            this.addShipVariant(variantId = "hound_hegemony_Standard", count = 2)
            this.addShipVariant(variantId = "kite_hegemony_Interceptor", count = 3)
            this.addShipVariant(variantId = "condor_Support", count = 1)
            this.addShipVariant(variantId = "brawler_Assault", count = 1).single().apply {
                this.isFlagship = true
            }

            FleetFactoryV3.addCommanderAndOfficersV2(
                this,
                FleetParamsV3(
                    /* source = */ null,
                    /* locInHyper = */ null,
                    /* factionId = */ Factions.HEGEMONY,
                    /* qualityOverride = */ 1f,
                    /* fleetType = */ FleetTypes.TASK_FORCE,
                    /* combatPts = */ this.fleetPoints.toFloat(),
                    /* freighterPts = */ 0f,
                    /* tankerPts = */ 0f,
                    /* transportPts = */ 0f,
                    /* linerPts = */ 0f,
                    /* utilityPts = */ 0f,
                    /* qualityMod = */ 1f
                ),
                game.intelManager.findFirst<Telos2HubMission>()?.genRandom
                    ?: Random(game.sector.seedString.hashCode().toLong())
            )
            this.fleetData.sort()
            this.fleetData.setSyncNeeded()
            this.fleetData.syncIfNeeded()
        }
    }

    /**
     * Create the impossible reinforcement fleet to wipe out the Telos.
     */
    fun createHegemonyFleetReinforcements(): CampaignFleetAPI {
        return FleetFactoryV3.createEmptyFleet(Factions.HEGEMONY, FleetTypes.TASK_FORCE, null)
            .apply {
                // good luck, kid
                this.addShipVariant(variantId = "brawler_Assault", count = 13)
                this.addShipVariant(variantId = "condor_Support", count = 12)
                this.addShipVariant(variantId = "eagle_Assault", count = 8)
                this.addShipVariant(variantId = "legion_Assault", count = 10)
                this.addShipVariant(variantId = "onslaught_Elite", count = 10)


                FleetFactoryV3.addCommanderAndOfficersV2(
                    this,
                    FleetParamsV3(
                        /* source = */ null,
                        /* locInHyper = */ null,
                        /* factionId = */ Factions.HEGEMONY,
                        /* qualityOverride = */ 1f,
                        /* fleetType = */ FleetTypes.TASK_FORCE,
                        /* combatPts = */ this.fleetPoints.toFloat(),
                        /* freighterPts = */ 0f,
                        /* tankerPts = */ 0f,
                        /* transportPts = */ 0f,
                        /* linerPts = */ 0f,
                        /* utilityPts = */ 0f,
                        /* qualityMod = */ 1f
                    ),
                    game.intelManager.findFirst<Telos2HubMission>()?.genRandom
                        ?: Random(game.sector.seedString.hashCode().toLong())
                )
                this.fleetData.sort()
                this.fleetData.setSyncNeeded()
                this.fleetData.syncIfNeeded()
            }
    }
}