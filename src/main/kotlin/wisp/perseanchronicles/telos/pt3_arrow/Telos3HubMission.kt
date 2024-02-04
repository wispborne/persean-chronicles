package wisp.perseanchronicles.telos.pt3_arrow

import com.fs.starfarer.api.PluginPick
import com.fs.starfarer.api.campaign.*
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.fleet.FleetMemberType
import com.fs.starfarer.api.impl.campaign.ids.Conditions
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes
import com.fs.starfarer.api.impl.campaign.ids.MemFlags
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithSearch.PlanetIsPopulatedReq
import com.fs.starfarer.api.impl.campaign.missions.hub.ReqMode
import com.fs.starfarer.api.ui.SectorMapAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.json.JSONObject
import wisp.perseanchronicles.Jukebox
import wisp.perseanchronicles.MOD_ID
import wisp.perseanchronicles.common.PerseanChroniclesNPCs
import wisp.perseanchronicles.game
import wisp.perseanchronicles.telos.TelosCommon
import wisp.perseanchronicles.telos.pt1_deliveryToEarth.Telos1HubMission
import wisp.questgiver.v2.IInteractionLogic
import wisp.questgiver.v2.QGHubMission
import wisp.questgiver.v2.json.query
import wisp.questgiver.v2.spriteName
import wisp.questgiver.wispLib.*
import java.awt.Color


class Telos3HubMission : QGHubMission() {
    companion object {
        // Hardcode because it's being used in rules.csv.
        val MISSION_ID = "wisp_perseanchronicles_telosPt3"

        var part3Json: JSONObject =
            TelosCommon.readJson().query("/$MOD_ID/telos/part3_arrow") as JSONObject
            private set

        val tags = setOf(Tags.INTEL_STORY, Tags.INTEL_ACCEPTED)

        val state = State(PersistentMapData<String, Any?>(key = "telosPt3State").withDefault { null })

        //        val chaseFleetFlag = "$${MISSION_ID}chaseFleet"
        val eugelChaseFleetTag = "${MISSION_ID}eugelChaseFleet"
    }

    class State(val map: MutableMap<String, Any?>) {
        var startDateMillis: Long? by map
        var completeDateInMillis: Long? by map
        var primaryTelosPlanet: SectorEntityToken? by map

        var visitedPrimaryPlanet: Boolean? by map
        var etherVialChoice: EtherVialsChoice? by map
        var retrievedSupplies: Boolean? by map
        var searchedForSurvivors: Boolean? by map
        var visitedLabs: Boolean? by map
        var sawKryptaDaydream: Boolean? by map
        var viewedWho: Boolean? by map
        var viewedWhat: Boolean? by map
        var viewedWhen: Boolean? by map
        var viewedWhere: Boolean? by map

        var talkedWithEugel: Boolean? by map
        var scuttledTelosShips: Boolean? by map
        var defeatedEugel: Boolean? by map
    }

    enum class EtherVialsChoice {
        Took,
        Destroyed
    }

    init {
        missionId = MISSION_ID
    }

    override fun onGameLoad(isNewGame: Boolean) {
        super.onGameLoad(isNewGame)

        // Reload json if devmode reload.
        if (isDevMode()) {
            part3Json = TelosCommon.readJson()
                .query("/$MOD_ID/telos/part3_arrow") as JSONObject
        }
    }

    override fun updateTextReplacements(text: Text) {
        text.globalReplacementGetters["telosPt1Stg1DestPlanet"] = { Telos1HubMission.state.karengoPlanet?.name }
        text.globalReplacementGetters["telosPt1Stg1DestSystem"] = { Telos1HubMission.state.karengoSystem?.name }
        text.globalReplacementGetters["telosStarName"] =
            { Telos1HubMission.state.karengoPlanet?.starSystem?.star?.name }
        text.globalReplacementGetters["telosPt3RuinsSystem"] = { state.primaryTelosPlanet?.starSystem?.name }
        text.globalReplacementGetters["telosPt3RuinsPlanet"] = { state.primaryTelosPlanet?.name }
        text.globalReplacementGetters["cyclesSinceTelosDestroyed"] = { game.sector.clock.cycle - 105 }
    }

    override fun create(createdAt: MarketAPI?, barEvent: Boolean): Boolean {
        // if already accepted by the player, abort
        if (!setGlobalReference("$$MISSION_ID") && !TelosCommon.isDevMode()) {
            return false
        }

        // Ignore warning, there are two overrides and it's complaining about just one of them.
        @Suppress("ABSTRACT_SUPER_CALL_WARNING")
        super.create(createdAt, barEvent)
        // Set his sprite in case he was created during Phase 1, before the new sprite was set.
        PerseanChroniclesNPCs.captainEugel.portraitSprite =
            IInteractionLogic.Portrait(category = "wisp_perseanchronicles_telos", id = "eugel_portrait").spriteName(game)
        setGenRandom(Telos1HubMission.state.seed ?: Misc.random)

        setStartingStage(Stage.GoToPlanet)
        addSuccessStages(Stage.Completed, Stage.CompletedSacrificeShips)
        setAbandonStage(Stage.Abandoned)

        name = part3Json.query("/strings/title")
        personOverride = PerseanChroniclesNPCs.karengo // Shows on intel, needed for rep reward or else crash.

        setIconName(IInteractionLogic.Portrait(category = "wisp_perseanchronicles_telos", id = "intel").spriteName(game))

        val allRingFoci = game.sector.starSystems.asSequence()
            .flatMap { it.allEntities }
            .filterIsInstance<RingBandAPI>()
            .map { it to it.focus }
            .distinct()
            .toList()

        // TODO create planet if it doesn't exist
        // Must have rings
        state.primaryTelosPlanet = MenriSystemCreator.createMenriSystem()
            ?: SystemFinder()
                .requireSystemTags(mode = ReqMode.NOT_ANY, Tags.THEME_CORE)
                .preferSystemOutsideRangeOf(Telos1HubMission.state.karengoSystem?.location, 5f)
                .requireSystemHasAtLeastNumJumpPoints(min = 1)
                .requirePlanetNotGasGiant()
                .requirePlanetNotStar()
                .requirePlanet { planet -> allRingFoci.map { (_, focus) -> focus.id }.contains(planet.id) }
                .requirePlanet(PlanetIsPopulatedReq(true))
                .preferEntityUndiscovered()
                .preferPlanet { planet -> planet.hasCondition(Conditions.HABITABLE) }
                .preferSystemNotPulsar()
                .preferPlanetWithRuins()
                .preferPlanetInDirectionOfOtherMissions()
                // Prefer a ring close to the planet
                .preferPlanet { planet ->
                    ((allRingFoci.firstOrNull { (_, focus) -> focus.id == planet.id }?.first?.middleRadius ?: 0f) - planet.radius) < 500f
                }
                .pickPlanet()
                    ?: kotlin.run {
                setCurrentStage(Stage.Abandoned, null, null)
                return false
            }

        // Spawn Eugel's fleet near player
        trigger {
            beginStageTrigger(Stage.EscapeSystem)
            val spawnLocation = game.sector.playerFleet
            triggerCreateFleet(
                FleetSize.LARGER,
                FleetQuality.SMOD_1,
                TelosCommon.eugelFactionId,
                FleetTypes.TASK_FORCE,
                spawnLocation
            )
            triggerSetFleetFaction(TelosCommon.eugelFactionId)
            triggerMakeNoRepImpact()
//            triggerAutoAdjustFleetStrengthModerate()
            triggerPickLocationAroundEntity(spawnLocation, 1000f, 1000f, 1000f)
            triggerSpawnFleetAtPickedLocation(null, null)
            triggerFleetSetName("Eugel's Fleet")
//            triggerFleetNoJump()
//            triggerFleetSetNoFactionInName()
            triggerSpawnFleetAtPickedLocation(null, null)
            triggerCustomAction { context ->
                // thank you DR https://bitbucket.org/modmafia/underworld/commits/3cdb860a7222d40f2d0d94e5bca0eaf672f5ab6c
                val firebrand = game.factory.createFleetMember(FleetMemberType.SHIP, "wisp_perseanchronicles_firebrand_Standard")

                val fleet = context.fleet
                val oldFlagship: FleetMemberAPI = fleet.flagship
                fleet.fleetData.addFleetMember(firebrand)

                firebrand.captain = PerseanChroniclesNPCs.captainEugel
                oldFlagship.isFlagship = false
                fleet.fleetData.setFlagship(firebrand)
                fleet.fleetData.removeFleetMember(oldFlagship)

                fleet.fleetData.sort()
                fleet.updateCounts()
                fleet.fleetData.syncIfNeeded()
                firebrand.repairTracker.cr = firebrand.repairTracker.maxCR
//                context.fleet?.flagship?.shipName = Telos2HubMission.getEugelShipName()
                context.fleet.sensorStrength = Float.MAX_VALUE
                val mem = context.fleet.memoryWithoutUpdate
                mem.set(MemFlags.MEMORY_KEY_STICK_WITH_PLAYER_IF_ALREADY_TARGET, true)
                mem.set(MemFlags.MEMORY_KEY_ALLOW_LONG_PURSUIT, true)
            }
            triggerMakeFleetIgnoredByOtherFleets()
            triggerMakeFleetIgnoreOtherFleetsExceptPlayer()
//            triggerFleetAddTags(eugelChaseFleetTag)
            triggerFleetMakeImportant(null, Stage.EscapeSystem)
            triggerSetFleetAlwaysPursue()
            triggerFleetMakeFaster(true, 2, true)
            triggerFleetSetCommander(PerseanChroniclesNPCs.captainEugel)
            triggerSetFleetMemoryValue(MemFlags.MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_ON, true)
            triggerOrderFleetInterceptPlayer(true, true)
        }

        // Spawn fleet jump point 1
        trigger {
            beginStageTrigger(Stage.EscapeSystem)
            val spawnLocation = state.primaryTelosPlanet?.starSystem?.jumpPoints?.first()
            triggerCreateFleet(
                FleetSize.MEDIUM,
                FleetQuality.SMOD_1,
                TelosCommon.eugelFactionId,
                FleetTypes.TASK_FORCE,
                spawnLocation
            )
            triggerMakeHostile()
            triggerMakeFleetIgnoreOtherFleetsExceptPlayer()
            triggerAutoAdjustFleetStrengthModerate()
            triggerMakeFleetIgnoredByOtherFleets()
            triggerMakeNoRepImpact()
            triggerFleetMakeImportant(null, Stage.EscapeSystem)
//            triggerFleetAddTags(chasingFleetTag)
            triggerFleetNoJump()
            triggerPickLocationAroundEntity(spawnLocation, 1f)
            triggerSpawnFleetAtPickedLocation(null, null)
            triggerOrderFleetPatrol(spawnLocation)
            triggerFleetInterceptPlayerOnSight(false, Stage.EscapeSystem)
            triggerSetFleetAlwaysPursue()
        }

        // Spawn fleet jump point 2
        trigger {
            beginStageTrigger(Stage.EscapeSystem)
            val spawnLocation = state.primaryTelosPlanet?.starSystem?.jumpPoints?.get(1)
            triggerCreateFleet(
                FleetSize.MEDIUM,
                FleetQuality.SMOD_1,
                TelosCommon.eugelFactionId,
                FleetTypes.TASK_FORCE,
                spawnLocation
            )
            triggerMakeHostile()
            triggerMakeFleetIgnoreOtherFleetsExceptPlayer()
            triggerAutoAdjustFleetStrengthModerate()
            triggerMakeNoRepImpact()
            triggerFleetNoJump()
            triggerFleetMakeImportant(null, Stage.EscapeSystem)
            triggerMakeFleetIgnoredByOtherFleets()
//            triggerFleetAddTags(chasingFleetTag)
            triggerPickLocationAroundEntity(spawnLocation, 1f)
            triggerSpawnFleetAtPickedLocation(null, null)
            triggerOrderFleetPatrol(spawnLocation)
            triggerFleetInterceptPlayerOnSight(false, Stage.EscapeSystem)
            triggerSetFleetAlwaysPursue()
        }

        // Make jump points the targets and start the script
        trigger {
            beginStageTrigger(Stage.EscapeSystem)
            // Make jump points important
            val jumpPoints = state.primaryTelosPlanet!!.containingLocation.jumpPoints.orEmpty()
            jumpPoints.forEach { jumpPoint ->
                triggerCustomAction { context -> context.entity = jumpPoint }
                triggerEntityMakeImportant("$${jumpPoint.id}_importantFlag", Stage.EscapeSystem)
            }

            triggerCustomAction {

                // "nothing" if music is disabled/volume 0
                if (listOf("TelosEvasion.ogg", "nothing").none { it == game.soundPlayer.currentMusicId }) {
                    kotlin.runCatching { game.jukebox.playSong(Jukebox.Song.EVASION) }
                        .onFailure {
                            game.logger.w(it)
                        }
                }
                game.sector.addScript(TelosFightOrFlightScript())
            }
        }

        trigger {
            beginStageTrigger(Stage.Completed, Stage.CompletedSacrificeShips)
            triggerCustomAction {
                game.sector.scripts.filterIsInstance<TelosFightOrFlightScript>()
                    .forEach {
                        it.done = true
                        game.sector.scripts.remove(it)
                    }
            }
            triggerCustomAction {
                game.sector.getStarSystem(MenriSystemCreator.systemBaseName)?.fleets.orEmpty()
                    .filter { !it.isPlayerFleet }
                    .forEach {
                        Misc.clearFlag(it.memoryWithoutUpdate, MemFlags.MEMORY_KEY_MAKE_HOSTILE)
                        Misc.makeNonHostileToFaction(it, game.sector.playerFaction.id, Float.POSITIVE_INFINITY)
                    }
            }
        }

        return true
    }

    override fun acceptImpl(dialog: InteractionDialogAPI?, memoryMap: MutableMap<String, MemoryAPI>?) {
        super.acceptImpl(dialog, memoryMap)

        state.startDateMillis = game.sector.clock.timestamp
        setCurrentStage(Stage.GoToPlanet, null, null)
        makeImportant(
            state.primaryTelosPlanet,
            null,
            Stage.GoToPlanet,
        )
        makePrimaryObjective(state.primaryTelosPlanet)
    }

    override fun endSuccessImpl(dialog: InteractionDialogAPI?, memoryMap: MutableMap<String, MemoryAPI>?) {
        super.endSuccessImpl(dialog, memoryMap)

        state.completeDateInMillis = game.sector.clock.timestamp
    }

    override fun advanceImpl(amount: Float) {
        super.advanceImpl(amount)

        if (currentStage == Stage.EscapeSystem) {
            // Detect when player escapes
            if (game.sector.playerFleet.containingLocation != getMenriSystem()) {
                if (Misc.getNearbyFleets(game.sector.playerFleet, 1000f).none()) {
                    game.sector.campaignUI.showInteractionDialog(Telos3EscapedDialog().build(), game.sector.playerFleet)
                }
            }

            // Detect Eugel fleet destruction
            val eugelFleet = getMenriSystem().fleets.firstOrNull { it.tags.contains(eugelChaseFleetTag) }
            if (eugelFleet != null && eugelFleet.commander?.id != PerseanChroniclesNPCs.captainEugel.id) {
                state.defeatedEugel = true
                setCurrentStage(Stage.CompletedDefeatedEugel, null, null)
            }
        }
    }

    fun getMenriSystem() = game.sector.getStarSystem(MenriSystemCreator.systemBaseName)

    override fun callAction(
        action: String?,
        ruleId: String?,
        dialog: InteractionDialogAPI?,
        params: MutableList<Misc.Token>?,
        memoryMap: MutableMap<String, MemoryAPI>?
    ): Boolean {
        // Trigger set in `create` (triggerFleetAddDefeatTrigger), fired off to rules.csv when fleet dies, listen for it here.
//        if (action == badFleetDefeatTrigger) {
//            setCurrentStage(Stage.LandOnPlanetFirst, null, null)
//            return true
//        }

        return super.callAction(action, ruleId, dialog, params, memoryMap)
    }

    override fun pickInteractionDialogPlugin(interactionTarget: SectorEntityToken): PluginPick<InteractionDialogPlugin>? {
        return when {
            interactionTarget.id == state.primaryTelosPlanet?.id -> {
                when (currentStage) {
                    Stage.GoToPlanet -> PluginPick(
                        Telos3LandingDialog().build(),
                        CampaignPlugin.PickPriority.MOD_SPECIFIC
                    )
                    //                Stage.LandOnPlanetSecondEther,
                    //                Stage.LandOnPlanetSecondNoEther -> PluginPick(
                    //                    Telos2SecondLandingDialog().build(),
                    //                    CampaignPlugin.PickPriority.MOD_SPECIFIC
                    //                )
                    else -> null
                }
            }

            // Interacting with Eugel's chasing fleet.
            interactionTarget is CampaignFleetAPI && interactionTarget.commander.id == PerseanChroniclesNPCs.captainEugel.id ->
                PluginPick(
                    EugelFleetInteractionDialogPlugin(this),
                    CampaignPlugin.PickPriority.MOD_SPECIFIC
                )

            else -> null
        }
    }

    override fun endAbandonImpl() {
        super.endAbandonImpl()
        game.logger.i { "Abandoning ${this.name} quest." }

        state.map.clear()
        currentStage = null
    }

    override fun addNextStepText(info: TooltipMakerAPI, tc: Color, pad: Float): Boolean {
        return when (currentStage as Stage) {
            Stage.GoToPlanet -> {
                info.addPara(padding = pad, textColor = Misc.getGrayColor()) {
                    part3Json.query<String>("/stages/goToPlanet/intel/subtitle").qgFormat()
                }
                true
            }

            Stage.EscapeSystemForDisplay,
            Stage.EscapeSystem -> {
                info.addPara(padding = pad, textColor = Misc.getGrayColor()) {
                    part3Json.query<String>("/stages/escape/intel/subtitle").qgFormat()
                }
                true
            }

            Stage.CompletedSacrificeShips -> {
                info.addPara(padding = pad, textColor = Misc.getGrayColor()) {
                    part3Json.query<String>("/stages/completedSacrificeShips/intel/subtitle").qgFormat()
                }
                true
            }

            Stage.Completed -> {
                info.addPara(padding = pad, textColor = Misc.getGrayColor()) {
                    part3Json.query<String>("/stages/escape/intel/subtitle").qgFormat()
                }
                true
            }

            Stage.CompletedDefeatedEugel -> {
                info.addPara(padding = pad, textColor = Misc.getGrayColor()) {
                    part3Json.query<String>("/stages/defeatedEugel/intel/subtitle").qgFormat()
                }
                true
            }

            Stage.Abandoned -> {
                true
            }
        }
    }

    override fun addDescriptionForCurrentStage(info: TooltipMakerAPI, width: Float, height: Float) {
        when (currentStage as Stage) {
            Stage.GoToPlanet -> {
                info.addPara { part3Json.query<String>("/stages/goToPlanet/intel/desc").qgFormat() }
            }

            Stage.EscapeSystemForDisplay,
            Stage.EscapeSystem -> {
                info.addPara { part3Json.query<String>("/stages/escape/intel/desc").qgFormat() }
            }

            Stage.CompletedSacrificeShips -> {
                info.addPara { part3Json.query<String>("/stages/completedSacrificeShips/intel/desc").qgFormat() }
            }

            Stage.Completed -> {
                info.addPara { part3Json.query<String>("/stages/escape/intel/desc").qgFormat() }
            }

            Stage.CompletedDefeatedEugel -> {
                info.addPara { part3Json.query<String>("/stages/defeatedEugel/intel/desc").qgFormat() }
            }

            Stage.Abandoned -> {}
        }
            .also { } // force exhaustive when
    }

    override fun getIntelTags(map: SectorMapAPI?) =
        (super.getIntelTags(map) + tags)

    enum class Stage {
        GoToPlanet,
        EscapeSystemForDisplay,
        EscapeSystem,
        Completed,
        CompletedSacrificeShips,
        CompletedDefeatedEugel,
        Abandoned,
    }
}