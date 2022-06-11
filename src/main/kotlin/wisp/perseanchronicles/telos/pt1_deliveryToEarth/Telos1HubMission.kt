package wisp.perseanchronicles.telos.pt1_deliveryToEarth

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.characters.FullName
import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.impl.campaign.ids.Conditions
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.ids.Ranks
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.campaign.missions.hub.ReqMode
import com.fs.starfarer.api.util.Misc
import org.json.JSONObject
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import wisp.perseanchronicles.MOD_ID
import wisp.perseanchronicles.game
import wisp.questgiver.AutoQuestFacilitator
import wisp.questgiver.InteractionDefinition
import wisp.questgiver.Questgiver
import wisp.questgiver.calculateCreditReward
import wisp.questgiver.json.query
import wisp.questgiver.v2.QGHubMissionWithBarEvent
import wisp.questgiver.wispLib.PersistentMapData
import wisp.questgiver.wispLib.SystemFinder
import wisp.questgiver.wispLib.Text
import wisp.questgiver.wispLib.distanceFrom

object Telos1HubMission : QGHubMissionWithBarEvent(
    barEventCreator = Telos1BarEventCreator()
) {
//    companion object {
//    var isEnabled = true

        val REWARD_CREDITS: Float
            get() = Questgiver.calculateCreditReward(state.startLocation, state.karengoPlanet, scaling = 1.3f)

        val icon = InteractionDefinition.Portrait(category = "intel", id = "red_planet") // todo change me
        val background = InteractionDefinition.Illustration(category = "wisp_perseanchronicles_telos", id = "background")

        val state = State(PersistentMapData<String, Any?>(key = "telosState").withDefault { null })

        val json: JSONObject by lazy {
            Global.getSettings().getMergedJSONForMod("data/strings/telos.hjson", MOD_ID)
                .query("/$MOD_ID/telos/part1") as JSONObject
        }

        val tags = listOf(Tags.INTEL_STORY, Tags.INTEL_ACCEPTED)

        val stage1Engineer: PersonAPI by lazy {
            Global.getSettings().createPerson().apply {
                this.name = FullName("Kelly", "McDonald", FullName.Gender.FEMALE)
                this.setFaction(Factions.INDEPENDENT)
                this.postId = Ranks.CITIZEN
                this.rankId = Ranks.CITIZEN
                this.portraitSprite = portraitSprite
            }
        }
//    }

    class State(val map: MutableMap<String, Any?>) {
        var startDateMillis: Long? by map
        var startLocation: SectorEntityToken? by map
        var karengoPlanet: SectorEntityToken? by map
        var destLocation: SectorEntityToken? by map
        var completeDateInMillis: Long? by map

        val karengoSystem: StarSystemAPI?
            get() = karengoPlanet?.starSystem
    }

    init {
        missionId = "telosPt1"
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

        // Ignore deprecation,
        super.create(createdAt, barEvent)

        setStartingStage(Stage.NotStarted)

        state.startLocation = createdAt?.primaryEntity

        state.karengoPlanet = SystemFinder()
            .requireSystemOnFringeOfSector()
            .preferEntityUndiscovered()
            .preferMarketConditions(ReqMode.ALL, Conditions.HABITABLE)
            .preferPlanetWithRuins()
            .requireSystemHasAtLeastNumJumpPoints(min = 1)
            .pickPlanet()
            ?: kotlin.run { game.logger.w { "Unable to find a planet for Telo pt 1." }; return false }

        (state.karengoSystem ?: return false).let { system ->
            system.jumpPoints.maxByOrNull { it.locationInHyperspace.distanceFrom(system.location) }!!
        }
            .let { jumpPoint ->
                jumpPoint.locationInHyperspace + Vector2f(2f, 2f)
            }
            .let { nearbyLoc ->

            }

        return true
    }

    fun start(startLocation: SectorEntityToken) {
        game.logger.i { "Telos start location set to ${startLocation.fullName} in ${startLocation.starSystem.baseName}" }
        currentStage = Stage.GoToPlanet
        state.startDateMillis = game.sector.clock.timestamp
    }

    fun complete() {
        currentStage = Stage.Completed
        state.completeDateInMillis = game.sector.clock.timestamp

        game.sector.playerFleet.cargo.credits.add(REWARD_CREDITS)
    }

    fun restartQuest() {
        game.logger.i { "Restarting Telos quest." }

        state.map.clear()
        currentStage = Stage.NotStarted
    }

    abstract class Stage(progress: Progress) : AutoQuestFacilitator.Stage(progress) {
        object NotStarted : Stage(Progress.NotStarted)
        object GoToPlanet : Stage(Progress.InProgress)
        object Completed : Stage(Progress.Completed)
    }
}