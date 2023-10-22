package wisp.perseanchronicles.telos.pt3_arrow

import com.fs.starfarer.api.PluginPick
import com.fs.starfarer.api.campaign.*
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.impl.campaign.ids.Conditions
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithSearch.PlanetIsPopulatedReq
import com.fs.starfarer.api.impl.campaign.missions.hub.ReqMode
import com.fs.starfarer.api.ui.SectorMapAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.json.JSONObject
import wisp.perseanchronicles.MOD_ID
import wisp.perseanchronicles.common.PerseanChroniclesNPCs
import wisp.perseanchronicles.game
import wisp.perseanchronicles.telos.TelosCommon
import wisp.perseanchronicles.telos.pt1_deliveryToEarth.Telos1HubMission
import wisp.perseanchronicles.telos.pt2_dart.Telos2HubMission
import wisp.questgiver.InteractionDefinition
import wisp.questgiver.spriteName
import wisp.questgiver.v2.QGHubMission
import wisp.questgiver.v2.json.query
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

        val chaseFleetFlag = "$${MISSION_ID}chaseFleet"
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
    }

    enum class EtherVialsChoice {
        Took,
        Destroyed
    }

    init {
        missionId = MISSION_ID
    }

    override fun onGameLoad() {
        super.onGameLoad()

        // Reload json if devmode reload.
        if (isDevMode())
            part3Json = TelosCommon.readJson()
                .query("/$MOD_ID/telos/part3_arrow") as JSONObject
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
        if (!setGlobalReference("$$MISSION_ID")) {
            return false
        }

        // Ignore warning, there are two overrides and it's complaining about just one of them.
        @Suppress("ABSTRACT_SUPER_CALL_WARNING")
        super.create(createdAt, barEvent)
        setGenRandom(Telos1HubMission.state.seed ?: Misc.random)

        setStartingStage(Stage.GoToPlanet)
        setSuccessStage(Stage.Completed)
        setAbandonStage(Stage.Abandoned)

        name = part3Json.query("/strings/title")
        personOverride = PerseanChroniclesNPCs.karengo // Shows on intel, needed for rep reward or else crash.

        setIconName(InteractionDefinition.Portrait(category = "wisp_perseanchronicles_telos", id = "intel").spriteName(game))

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
                Factions.LUDDIC_CHURCH,
                FleetTypes.TASK_FORCE,
                spawnLocation
            )
            triggerMakeHostile()
            triggerAutoAdjustFleetStrengthModerate()
            triggerMakeFleetIgnoredByOtherFleets()
//            triggerFleetAddTags(chasingFleetTag)
            triggerPickLocationAroundEntity(spawnLocation, 4000f, 3000f, 5000f)
            triggerSpawnFleetAtPickedLocation(chaseFleetFlag, null)
            triggerFleetAddTags(eugelChaseFleetTag)
            triggerOrderFleetInterceptPlayer(true, true)
            triggerFleetSetFlagship("wisp_perseanchronicles_firebrand_Standard")
            triggerFleetSetCommander(PerseanChroniclesNPCs.captainEugel)
            triggerCustomAction { context ->
                context.fleet?.flagship?.shipName = Telos2HubMission.getEugelShipName()
                context.fleet.name = "Eugel's Fleet"
            }
        }

        // Spawn fleet jump point 1
        trigger {
            beginStageTrigger(Stage.EscapeSystem)
            val spawnLocation = state.primaryTelosPlanet?.starSystem?.jumpPoints?.first()
            triggerCreateFleet(
                FleetSize.MEDIUM,
                FleetQuality.SMOD_1,
                Factions.LUDDIC_CHURCH,
                FleetTypes.TASK_FORCE,
                spawnLocation
            )
            triggerMakeHostile()
            triggerAutoAdjustFleetStrengthModerate()
            triggerMakeFleetIgnoredByOtherFleets()
//            triggerFleetAddTags(chasingFleetTag)
            triggerPickLocationAroundEntity(spawnLocation, 1f)
            triggerSpawnFleetAtPickedLocation(chaseFleetFlag, null)
            triggerOrderFleetPatrol(spawnLocation)
            triggerFleetInterceptPlayerOnSight(false, Stage.EscapeSystem)
        }

        // Spawn fleet jump point 2
        trigger {
            beginStageTrigger(Stage.EscapeSystem)
            val spawnLocation = state.primaryTelosPlanet?.starSystem?.jumpPoints?.get(1)
            triggerCreateFleet(
                FleetSize.MEDIUM,
                FleetQuality.SMOD_1,
                Factions.LUDDIC_CHURCH,
                FleetTypes.TASK_FORCE,
                spawnLocation
            )
            triggerMakeHostile()
            triggerAutoAdjustFleetStrengthModerate()
            triggerMakeFleetIgnoredByOtherFleets()
//            triggerFleetAddTags(chasingFleetTag)
            triggerPickLocationAroundEntity(spawnLocation, 1f)
            triggerSpawnFleetAtPickedLocation(chaseFleetFlag, null)
            triggerOrderFleetPatrol(spawnLocation)
            triggerFleetInterceptPlayerOnSight(false, Stage.EscapeSystem)
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
                game.sector.addScript(TelosFightOrFlightScript())
            }
        }

//        trigger {
//            beginEnteredLocationTrigger(game.sector.hyperspace, Stage.GoToPlanet)
//
//        }

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
            interactionTarget.hasTag(eugelChaseFleetTag) -> PluginPick(
                EugelFleetInteractionDialogPlugin(),
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
        return when (currentStage) {
            Stage.GoToPlanet -> {
                info.addPara(padding = pad, textColor = Misc.getGrayColor()) {
                    part3Json.query<String>("/stages/goToPlanet/intel/subtitle").qgFormat()
                }
                true
            }

            Stage.EscapeSystem -> {
                info.addPara(padding = pad, textColor = Misc.getGrayColor()) {
                    part3Json.query<String>("/stages/escape/intel/subtitle").qgFormat()
                }
                true
            }

            Stage.Completed -> {
                info.addPara(padding = pad, textColor = Misc.getGrayColor()) {
                    part3Json.query<String>("/stages/escape/intel/subtitle").qgFormat()
                }
                true
            }

            else -> false
        }
    }

    override fun addDescriptionForCurrentStage(info: TooltipMakerAPI, width: Float, height: Float) {
        when (currentStage) {
            Stage.GoToPlanet -> {
                info.addPara { part3Json.query<String>("/stages/goToPlanet/intel/desc").qgFormat() }
            }

            Stage.EscapeSystem -> {
                info.addPara { part3Json.query<String>("/stages/escape/intel/desc").qgFormat() }
            }

            Stage.Completed -> {
                info.addPara { part3Json.query<String>("/stages/escape/intel/desc").qgFormat() }
            }
        }
    }

    override fun getIntelTags(map: SectorMapAPI?) =
        (super.getIntelTags(map) + tags)

    enum class Stage {
        GoToPlanet,
        EscapeSystem,
        Completed,
        Abandoned,
    }
}