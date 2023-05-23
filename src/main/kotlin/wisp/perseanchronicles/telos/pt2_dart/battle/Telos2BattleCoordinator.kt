package wisp.perseanchronicles.telos.pt2_dart.battle

import com.fs.starfarer.api.PluginPick
import com.fs.starfarer.api.campaign.BaseCampaignPlugin
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.characters.FullName
import com.fs.starfarer.api.combat.BattleCreationContext
import com.fs.starfarer.api.fleet.FleetGoal
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes
import com.fs.starfarer.api.impl.campaign.ids.Personalities
import com.fs.starfarer.api.mission.FleetSide
import org.magiclib.util.MagicCampaign
import wisp.perseanchronicles.common.BattleSide
import wisp.perseanchronicles.common.PerseanChroniclesNPCs
import wisp.perseanchronicles.game
import wisp.perseanchronicles.telos.TelosCommon
import wisp.perseanchronicles.telos.boats.ShipPalette
import wisp.perseanchronicles.telos.pt2_dart.Telos2HubMission
import wisp.questgiver.wispLib.addShipVariant
import wisp.questgiver.wispLib.findFirst
import wisp.questgiver.wispLib.refit
import wisp.questgiver.wispLib.swapFleets
import java.util.*


object Telos2BattleCoordinator {
    class CampaignPlugin : BaseCampaignPlugin() {
        override fun pickBattleCreationPlugin(opponent: SectorEntityToken?): PluginPick<com.fs.starfarer.api.campaign.BattleCreationPlugin> =
            PluginPick(
                Telos2BattleCreationPlugin(),
                com.fs.starfarer.api.campaign.CampaignPlugin.PickPriority.MOD_SPECIFIC
            )
    }

    val telosCommanders = listOf(
        MagicCampaign.createCaptainBuilder(TelosCommon.FACTION_TELOS_ID)
            .setFirstName("Titania").setLastName("Leblanc").setGender(FullName.Gender.FEMALE).setPersonality(Personalities.AGGRESSIVE).setLevel(4)
            .create()
            .apply {
                addTag(ShipPalette.DEFAULT.name)
            },
        MagicCampaign.createCaptainBuilder(TelosCommon.FACTION_TELOS_ID)
            .setFirstName("Kemi").setLastName("Qadri").setGender(FullName.Gender.FEMALE).setPersonality(Personalities.AGGRESSIVE).setLevel(5)
            .create()
            .apply {
                addTag(ShipPalette.BLUE.name)
            },
        MagicCampaign.createCaptainBuilder(TelosCommon.FACTION_TELOS_ID)
            .setFirstName("Hercules").setLastName("Eridani").setGender(FullName.Gender.MALE).setPersonality(Personalities.AGGRESSIVE).setLevel(4)
            .create()
            .apply {
                addTag(ShipPalette.TEAL.name)
            },
        MagicCampaign.createCaptainBuilder(TelosCommon.FACTION_TELOS_ID)
            .setFirstName("Fescue").setLastName("Dust").setGender(FullName.Gender.MALE).setPersonality(Personalities.AGGRESSIVE).setLevel(3)
            .create()
            .apply {
                addTag(ShipPalette.SEASERPENT.name)
            },
        MagicCampaign.createCaptainBuilder(TelosCommon.FACTION_TELOS_ID)
            .setFirstName("Hom").setLastName("Imran").setGender(FullName.Gender.FEMALE).setPersonality(Personalities.AGGRESSIVE).setLevel(4)
            .create()
            .apply {
                addTag(ShipPalette.WHITE.name)
            })

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
        val combatEngine = game.combatEngine!!
        combatEngine.getFleetManager(FleetSide.ENEMY).isCanForceShipsToEngageWhenBattleClearlyLost = true
        combatEngine.getFleetManager(FleetSide.PLAYER).isCanForceShipsToEngageWhenBattleClearlyLost = true

        // Script to call in reinforcements when player starts to win.
        combatEngine.addPlugin(Telos2BattleScript(playerFleetHolder))
    }

    /**
     * The side the player is fighting on, with just enough firepower to take out the initial fleet.
     */
    fun createTelosFleet(): CampaignFleetAPI {
        return FleetFactoryV3.createEmptyFleet(Factions.INDEPENDENT, FleetTypes.TASK_FORCE, null)
            .apply {
                telosCommanders.forEach { cmdr ->
                    this.addShipVariant(variantOrHullId = "wisp_perseanchronicles_vara_Standard", count = 1).first().apply {
                        cmdr.setPersonality(Personalities.AGGRESSIVE)
                        cmdr.addTag(TelosCommon.ETHER_OFFICER_TAG)
                        this.captain.memoryWithoutUpdate["\$chatterChar"] = "none"
                        this.captain = cmdr
                        this.shipName = cmdr.nameString
                        try {
                            this.refit(
                                shouldUpgrade = true,
                                shouldStrip = true,
                                averageSMods = 1
                            )
                        } catch (e: Exception) {
                            game.logger.w(e) { "Unable to refit ${this.shipName} ${this.id}." }
                        }
                    }
                }
                this.fleetData.membersListCopy.first().isFlagship = true
                this.fleetData.membersListCopy.forEach { it.repairTracker.cr = it.repairTracker.maxCR }
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
            this.addShipVariant(variantOrHullId = "hound_hegemony_Standard", count = 1)
            this.addShipVariant(variantOrHullId = "kite_hegemony_Interceptor", count = 1)
            this.addShipVariant(variantOrHullId = "condor_Support", count = 1)
            this.addShipVariant(variantOrHullId = "eradicator_Overdriven", count = 1).single().apply {
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
            this.membersWithFightersCopy.forEach {
                try {
                    it.refit(
                        shouldUpgrade = true,
                        shouldStrip = true
                    )
                } catch (e: Exception) {
                    game.logger.w(e) { "Unable to refit ${it.shipName} ${it.id}." }
                }
            }
            this.fleetData.sort()
            this.fleetData.setSyncNeeded()
            this.fleetData.syncIfNeeded()
            this.fleetData.fleet.commander.memoryWithoutUpdate["\$chatterChar"] = "none"
        }
    }

    /**
     * Create the impossible reinforcement fleet to wipe out the Telos.
     */
    fun createHegemonyFleetReinforcements(): CampaignFleetAPI {
        val telos2HubMission = game.intelManager.findFirst<Telos2HubMission>()

        // good luck, kid
        return MagicCampaign.createFleetBuilder()
            .setFleetName("Hegemony Attack Fleet")
            .setFleetFaction(Factions.HEGEMONY)
            .setFleetType(FleetTypes.TASK_FORCE)
            .setFlagshipName(telos2HubMission?.getEugelShipName())
            .setFlagshipVariant("onslaught_xiv_Elite")
            .setFlagshipAlwaysRecoverable(false)
            .setFlagshipAutofit(true)
            .setCaptain(PerseanChroniclesNPCs.captainEugel)
            .setSupportFleet(
                mapOf(
                    "brawler_Assault" to 7,
                    "eradicator_Overdriven" to 7,
                    "condor_Support" to 12,
                    "eagle_Assault" to 8,
                    "legion_Assault" to 10,
                    "onslaught_Elite" to 10,
                )
            )
            .setSupportAutofit(true)
            .setReinforcementFaction(Factions.HEGEMONY)
            .setQualityOverride(1f)
            .create()
            .apply {
                this.fleetData.membersListCopy.forEach { it.owner = BattleSide.ENEMY }
                this.fleetData.fleet.membersWithFightersCopy.forEach {
                    try {
                        it.refit(
                            shouldUpgrade = true,
                            shouldStrip = true,
                            averageSMods = 1
                        )
                    } catch (e: Exception) {
                        game.logger.w(e) { "Unable to refit ${it.shipName} ${it.id}." }
                    }
                }
                this.fleetData.sort()
                this.fleetData.setSyncNeeded()
                this.fleetData.syncIfNeeded()
                this.fleetData.fleet.commander.memoryWithoutUpdate["\$chatterChar"] = "none"
            }
    }
}