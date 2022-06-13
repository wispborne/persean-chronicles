package wisp.perseanchronicles.telos.pt1_deliveryToEarth

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.characters.FullName
import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.impl.campaign.ids.*
import com.fs.starfarer.api.impl.campaign.missions.hub.ReqMode
import com.fs.starfarer.api.util.Misc
import org.json.JSONObject
import wisp.perseanchronicles.MOD_ID
import wisp.perseanchronicles.game
import wisp.questgiver.InteractionDefinition
import wisp.questgiver.Questgiver
import wisp.questgiver.calculateCreditReward
import wisp.questgiver.json.query
import wisp.questgiver.spriteName
import wisp.questgiver.v2.QGHubMissionWithBarEvent
import wisp.questgiver.wispLib.PersistentMapData
import wisp.questgiver.wispLib.SystemFinder
import wisp.questgiver.wispLib.Text
import wisp.questgiver.wispLib.trigger

class Telos1HubMission : QGHubMissionWithBarEvent() {
    companion object {
//    var isEnabled = true

        val MISSION_ID = "telosPt1"

        val json: JSONObject by lazy {
            Global.getSettings().getMergedJSONForMod("data/strings/telos.hjson", MOD_ID)
                .query("/$MOD_ID/telos/part1") as JSONObject
        }

        val tags = listOf(Tags.INTEL_STORY, Tags.INTEL_ACCEPTED)

        val state = State(PersistentMapData<String, Any?>(key = "telosState").withDefault { null })
    }

    val stage1Engineer: PersonAPI by lazy {
        Global.getSector().getFaction(Factions.INDEPENDENT)
            .createRandomPerson(FullName.Gender.FEMALE, getGenRandom()).apply {
                this.name = FullName("Kelly", "McDonald", FullName.Gender.FEMALE)
                this.setFaction(Factions.INDEPENDENT)
                this.postId = Ranks.CITIZEN
                this.rankId = Ranks.CITIZEN
                this.portraitSprite = portraitSprite
            }
    }

    val REWARD_CREDITS: Float
        get() = Questgiver.calculateCreditReward(state.startLocation, state.karengoPlanet, scaling = 1.3f)

    val background = InteractionDefinition.Illustration(category = "wisp_perseanchronicles_telos", id = "background")

    class State(val map: MutableMap<String, Any?>) {
        var startDateMillis: Long? by map
        var startLocation: SectorEntityToken? by map
        var karengoPlanet: SectorEntityToken? by map
        var completeDateInMillis: Long? by map

        val karengoSystem: StarSystemAPI?
            get() = karengoPlanet?.starSystem
    }

    init {
        missionId = MISSION_ID
    }

    override fun shouldShowAtMarket(market: MarketAPI?): Boolean {
        return true // todo
    }

    override fun updateTextReplacements(text: Text) {
        text.globalReplacementGetters["telosCredits"] = { Misc.getDGSCredits(REWARD_CREDITS) }
        text.globalReplacementGetters["telosDestPlanet"] = { state.karengoPlanet?.name }
        text.globalReplacementGetters["telosDestSystem"] = { state.karengoSystem?.name }
        text.globalReplacementGetters["telosStarName"] = { state.karengoPlanet?.starSystem?.star?.name }
    }

    override fun create(createdAt: MarketAPI?, barEvent: Boolean): Boolean {
//        if (!isEnabled) return false

        // Ignore warning, there are two overrides and it's complaining about just one of them.
        @Suppress("ABSTRACT_SUPER_CALL_WARNING")
        super.create(createdAt, barEvent)

        setStartingStage(Stage.GoToPlanet)
        setSuccessStage(Stage.Completed)

        name = json.query("/strings/intel/title") as String
        personOverride = stage1Engineer

        // todo change me
        setIconName(InteractionDefinition.Portrait(category = "intel", id = "red_planet").spriteName(game))

        state.startLocation = createdAt?.primaryEntity

        state.karengoPlanet = SystemFinder()
            .requireSystemOnFringeOfSector()
            .preferEntityUndiscovered()
            .preferMarketConditions(ReqMode.ALL, Conditions.HABITABLE)
            .preferPlanetWithRuins()
            .requireSystemHasAtLeastNumJumpPoints(min = 1)
            .pickPlanet()
            ?: kotlin.run { game.logger.w { "Unable to find a planet for ${this.name}." }; return false }


        return true
    }

    override fun acceptImpl(dialog: InteractionDialogAPI?, memoryMap: MutableMap<String, MemoryAPI>?) {
        super.acceptImpl(dialog, memoryMap)

        val startLocation = dialog?.interactionTarget
            ?: kotlin.run {
                game.logger.e { "Aborting acceptance of ${this.name} because dialog was null." }
                abort()
                return
            }

        game.logger.i { "${this.name} start location set to ${startLocation.fullName} in ${startLocation.starSystem.baseName}" }
        state.startDateMillis = game.sector.clock.timestamp
        makeImportant(state.karengoSystem?.hyperspaceAnchor, null, Stage.GoToPlanet)
        makePrimaryObjective(state.karengoSystem?.hyperspaceAnchor)

        trigger {
            beginWithinHyperspaceRangeTrigger(state.karengoSystem, 3f, true, Stage.GoToPlanet)
            triggerCreateFleet(
                FleetSize.MEDIUM,
                FleetQuality.LOWER,
                Factions.PIRATES,
                FleetTypes.SCAVENGER_MEDIUM,
                state.karengoPlanet
            )
            triggerMakeHostileAndAggressive()
            triggerAutoAdjustFleetStrengthModerate()
            triggerPickLocationAroundEntity(state.karengoPlanet, 1f)
            triggerSpawnFleetAtPickedLocation()

            triggerCustomAction {
//                setCurrentStage()
            }
        }
    }

    override fun endSuccessImpl(dialog: InteractionDialogAPI?, memoryMap: MutableMap<String, MemoryAPI>?) {
        super.endSuccessImpl(dialog, memoryMap)

        currentStage = Stage.Completed
        state.completeDateInMillis = game.sector.clock.timestamp

        game.sector.playerFleet.cargo.credits.add(REWARD_CREDITS)
    }

    override fun endAbandonImpl() {
        super.endAbandonImpl()
        game.logger.i { "Restarting ${this.name} quest." }

        state.map.clear()
        currentStage = null
    }

    enum class Stage {
        GoToPlanet,
        Completed,
    }
}