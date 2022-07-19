package wisp.perseanchronicles.telos.pt2_dart.battle

import com.fs.starfarer.api.PluginPick
import com.fs.starfarer.api.campaign.BaseCampaignPlugin
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.combat.BattleCreationContext
import com.fs.starfarer.api.fleet.FleetGoal
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes
import com.fs.starfarer.api.mission.FleetSide
import data.scripts.util.MagicCampaign
import wisp.perseanchronicles.common.BattleSide
import wisp.perseanchronicles.game
import wisp.perseanchronicles.telos.pt2_dart.Telos2HubMission
import wisp.questgiver.wispLib.addShipVariant
import wisp.questgiver.wispLib.findFirst
import wisp.questgiver.wispLib.swapFleets
import java.util.*

object Telos2Battle {
    private val flagshipVariantId = "shrike_Attack"

    class CampaignPlugin : BaseCampaignPlugin() {
        override fun pickBattleCreationPlugin(opponent: SectorEntityToken?): PluginPick<com.fs.starfarer.api.campaign.BattleCreationPlugin> =
            PluginPick(
                Telos2BattleCreationPlugin(),
                com.fs.starfarer.api.campaign.CampaignPlugin.PickPriority.MOD_SPECIFIC
            )
    }

    fun startBattle() {
        game.sector.registerPlugin(CampaignPlugin())

        val playerFleetHolder =
            game.factory.createEmptyFleet(game.sector.playerFaction.id, "[PersChron] Player Fleet Holder", true)

        // Save player fleet to dummy fleet.
        playerFleetHolder.swapFleets(
            otherFleet = game.sector.playerFleet
        )
        // Set player fleet to Telos fleet.
        game.sector.playerFleet.swapFleets(
            otherFleet = createTelosFleet()
        )

        // Can't use FleetEventListener because we aren't using a FleetInteractionDialogPluginImpl,
        // which is what triggers onBattleOccurred.
        // Have to rely on the calling dialog notifying us that the battle ended, which it knows because
        // of the player using the dialog (this is how FIDPI works).

        // Start battle
        game.sector.campaignUI.startBattle(object : BattleCreationContext(
            game.sector.playerFleet,
            FleetGoal.ATTACK,
            createInitialHegemonyFleet(),
            FleetGoal.ATTACK
        ) {
            init {
                fightToTheLast = true
                aiRetreatAllowed = false
                enemyDeployAll = true
                objectivesAllowed = false
            }
        })
        game.combatEngine.getFleetManager(FleetSide.ENEMY).isCanForceShipsToEngageWhenBattleClearlyLost = true

        // Call in reinforcements when player starts to win.
        game.combatEngine.addPlugin(Telos2BattleScript(playerFleetHolder))
    }

    /**
     * The side the player is fighting on, with just enough firepower to take out the initial fleet.
     */
    fun createTelosFleet(): CampaignFleetAPI {
        return FleetFactoryV3.createEmptyFleet(Factions.INDEPENDENT, FleetTypes.TASK_FORCE, null)
            .apply {
                this.addShipVariant(variantId = flagshipVariantId, count = 1).first().apply {
                    this.isFlagship = true
                }
                this.addShipVariant(variantId = "kite_original_Stock", count = 3)
                // todo add some Telos ships.
                this.fleetData.sort()
                this.fleetData.isOnlySyncMemberLists = true
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
        val telos2HubMission = game.intelManager.findFirst<Telos2HubMission>()

        // good luck, kid
        return MagicCampaign.createFleet(
            /* fleetName = */ "Hegemony Attack Fleet",
            /* fleetFaction = */ Factions.HEGEMONY,
            /* fleetType = */ FleetTypes.TASK_FORCE,
            /* flagshipName = */ telos2HubMission?.getEugelShipName(),
            /* flagshipVariant = */ "onslaught_xiv_Elite",
            /* flagshipRecovery = */ false,
            /* flagshipAutofit = */ true,
            /* captain = */ Telos2HubMission.captainEugel,
            /* supportFleet = */ mapOf(
                "brawler_Assault" to 13,
                "condor_Support" to 12,
                "eagle_Assault" to 8,
                "legion_Assault" to 10,
                "onslaught_Elite" to 10,
            ),
            /* supportAutofit = */ true,
            /* minFP = */ 0,
            /* reinforcementFaction = */ Factions.HEGEMONY,
            /* qualityOverride = */ 1f,
            /* spawnLocation = */ null,
            /* assignment = */ null,
            /* assignementTarget = */ null,
            /* isImportant = */ false,
            /* transponderOn = */ false,
            /* variantsPath = */ null
        )
            .apply {
                this.fleetData.membersListCopy.forEach { it.owner = BattleSide.ENEMY }
                this.fleetData.sort()
                this.fleetData.setSyncNeeded()
                this.fleetData.syncIfNeeded()
            }
    }
}