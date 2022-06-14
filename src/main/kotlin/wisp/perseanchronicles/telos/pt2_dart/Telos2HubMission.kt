package wisp.perseanchronicles.telos.pt2_dart

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.characters.FullName
import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes
import com.fs.starfarer.api.impl.campaign.ids.Ranks
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMission
import org.json.JSONObject
import wisp.perseanchronicles.MOD_ID
import wisp.perseanchronicles.game
import wisp.perseanchronicles.telos.pt1_deliveryToEarth.Telos1HubMission
import wisp.questgiver.InteractionDefinition
import wisp.questgiver.json.query
import wisp.questgiver.spriteName
import wisp.questgiver.v2.QGHubMission
import wisp.questgiver.v2.QGHubMissionWithBarEvent
import wisp.questgiver.wispLib.PersistentMapData
import wisp.questgiver.wispLib.Text
import wisp.questgiver.wispLib.trigger

class Telos2HubMission : QGHubMission() {
    companion object {
        val MISSION_ID = "telosPt2"

        val json: JSONObject by lazy {
            Global.getSettings().getMergedJSONForMod("data/strings/telos.hjson", MOD_ID)
                .query("/$MOD_ID/telos/part2_dart") as JSONObject
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

    class State(val map: MutableMap<String, Any?>) {
        var startDateMillis: Long? by map
        var completeDateInMillis: Long? by map
    }

    init {
        missionId = MISSION_ID
    }

    override fun updateTextReplacements(text: Text) {
    }

    override fun create(createdAt: MarketAPI?, barEvent: Boolean): Boolean {
//        if (!isEnabled) return false

        // Ignore warning, there are two overrides and it's complaining about just one of them.
        @Suppress("ABSTRACT_SUPER_CALL_WARNING")
        super.create(createdAt, barEvent)
        setGenRandom(Telos1HubMission.seed)

        setStartingStage(Stage.GoToPlanet)
        setSuccessStage(Stage.Completed)

        name = json.query("/strings/intel/title") as String
        personOverride = stage1Engineer

        // todo change me
        setIconName(InteractionDefinition.Portrait(category = "intel", id = "red_planet").spriteName(game))

        return true
    }

    override fun acceptImpl(dialog: InteractionDialogAPI?, memoryMap: MutableMap<String, MemoryAPI>?) {
        super.acceptImpl(dialog, memoryMap)

        state.startDateMillis = game.sector.clock.timestamp
        makeImportant(Telos1HubMission.state.karengoPlanet, null, Stage.GoToPlanet)
        makePrimaryObjective(Telos1HubMission.state.karengoPlanet)

        trigger {
            beginCustomTrigger({ true })
            triggerCreateFleet(
                FleetSize.MEDIUM,
                FleetQuality.LOWER,
                Factions.PIRATES,
                FleetTypes.SCAVENGER_MEDIUM,
                Telos1HubMission.state.karengoPlanet
            )
            triggerMakeHostileAndAggressive()
            triggerAutoAdjustFleetStrengthModerate()
            triggerPickLocationAroundEntity(Telos1HubMission.state.karengoPlanet, 1f)
            triggerSpawnFleetAtPickedLocation()
        }
    }

    override fun endSuccessImpl(dialog: InteractionDialogAPI?, memoryMap: MutableMap<String, MemoryAPI>?) {
        super.endSuccessImpl(dialog, memoryMap)

        currentStage = Stage.Completed
        state.completeDateInMillis = game.sector.clock.timestamp
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