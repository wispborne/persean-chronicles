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
import wisp.perseanchronicles.game
import wisp.questgiver.wispLib.addShipVariant
import wisp.questgiver.wispLib.findFirst
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
        swapFleets(
            leftFleet = playerFleetHolder,
            rightFleet = game.sector.playerFleet
        )
        // Set player fleet to Telos fleet.
        swapFleets(
            leftFleet = game.sector.playerFleet,
            rightFleet = createTelosFleet()
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
        game.combatEngine.addPlugin(object : BaseEveryFrameCombatPlugin() {
            var hasReinforced = false

            override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {
                val enemyFleetManager = game.combatEngine.getFleetManager(FleetSide.ENEMY)
                val hasDestroyedEnoughOfEnemy = enemyFleetManager.destroyedCopy.size > 5

                if (!hasReinforced && (game.combatEngine.isEnemyInFullRetreat || hasDestroyedEnoughOfEnemy)) {
                    game.combatEngine.combatNotOverFor = 10f // seconds
                    createHegemonyFleetReinforcements().fleetData.membersListCopy.forEach { reinforcement ->
                        reinforcement.owner = 1 // Vanilla hardcodes 1 for enemy and 0 for player ahhhhhhhhhhhhhhhhhh
                        enemyFleetManager
                            .addToReserves(reinforcement)
                    }
                    hasReinforced = true
                }

                if (hasReinforced && game.combatEngine.isCombatOver) {
                    onTelosBattleEnded(
                        didPlayerWin = game.combatEngine.winningSideId == 0,
                        originalPlayerFleet = playerFleetHolder
                    )
                    game.combatEngine.removePlugin(this)
                }
            }
        })
    }

    /**
     * Call after the battle ends.
     */
    private fun onTelosBattleEnded(
        didPlayerWin: Boolean,
        originalPlayerFleet: CampaignFleetAPI
    ) {
        if (didPlayerWin) {
            game.logger.i { "Cheater cheater pumpkin eater!" }
            // How tf did player win? hax
            // todo
        }

        // Give the player back their fleet.
        swapFleets(
            leftFleet = game.sector.playerFleet,
            rightFleet = originalPlayerFleet
        )
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

    /**
     * Swaps two fleets' ships and captains. Does not swap other things eg. cargo.
     */
    fun swapFleets(leftFleet: CampaignFleetAPI, rightFleet: CampaignFleetAPI) {
        val originalLeftFleetShips = leftFleet.fleetData.membersListCopy
        val originalRightFleetShips = rightFleet.fleetData.membersListCopy

        // Move left to right.
        originalLeftFleetShips
            .forEach { ship ->
                leftFleet.fleetData.removeFleetMember(ship)
                rightFleet.fleetData.addFleetMember(ship)
            }

        // Move right to left.
        originalRightFleetShips.forEach { ship ->
            rightFleet.fleetData.removeFleetMember(ship)
            leftFleet.fleetData.addFleetMember(ship)
        }

        // Set fleet flagships based upon the original flagships.
        originalLeftFleetShips.firstOrNull { it.isFlagship }?.run { rightFleet.fleetData.setFlagship(this) }
        originalRightFleetShips.firstOrNull { it.isFlagship }?.run { leftFleet.fleetData.setFlagship(this) }

        // If one fleet is player, set them as captain of the flagship.
        if (leftFleet.isPlayerFleet) {
            leftFleet.flagship?.captain = game.sector.playerPerson
        } else if (rightFleet.isPlayerFleet) {
            rightFleet.flagship?.captain = game.sector.playerPerson
        }
    }
}