package wisp.perseanchronicles.telos.pt1_deliveryToEarth

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.characters.FullName
import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.impl.campaign.ids.Conditions
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.ids.Ranks
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.campaign.missions.hub.ReqMode
import com.fs.starfarer.api.util.Misc
import org.json.JSONObject
import wisp.perseanchronicles.MOD_ID
import wisp.perseanchronicles.game
import wisp.questgiver.InteractionDefinition
import wisp.questgiver.json.query
import wisp.questgiver.spriteName
import wisp.questgiver.v2.QGHubMissionWithBarEvent
import wisp.questgiver.wispLib.*
import java.util.*

class Telos1HubMission : QGHubMissionWithBarEvent() {
    companion object {
        val MISSION_ID = "telosPt1"

        val json: JSONObject by lazy {
            Global.getSettings().getMergedJSONForMod("data/strings/telos.hjson", MOD_ID)
                .query("/$MOD_ID/telos/part1_deliveryToEarth") as JSONObject
        }
        val state = State(PersistentMapData<String, Any?>(key = "telosState").withDefault { null })
        val tags = listOf(Tags.INTEL_STORY, Tags.INTEL_ACCEPTED)
        lateinit var seed: Random
            private set
    }

    val stage1Engineer: PersonAPI =
        Global.getSector().getFaction(Factions.INDEPENDENT)
            .createRandomPerson(FullName.Gender.FEMALE, getGenRandom()).apply {
                this.name = FullName("Kelly", "McDonald", FullName.Gender.FEMALE)
                this.setFaction(Factions.INDEPENDENT)
                this.postId = Ranks.CITIZEN
                this.rankId = Ranks.CITIZEN
                this.portraitSprite = portraitSprite
            }

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
        text.globalReplacementGetters["telosCredits"] = { Misc.getDGSCredits(creditsReward.toFloat()) }
        text.globalReplacementGetters["telosPt1Stg1DestPlanet"] = { state.karengoPlanet?.name }
        text.globalReplacementGetters["telosPt1Stg1DestSystem"] = { state.karengoSystem?.name }
        text.globalReplacementGetters["telosStarName"] = { state.karengoPlanet?.starSystem?.star?.name }
    }

    override fun create(createdAt: MarketAPI?, barEvent: Boolean): Boolean {
        // Ignore warning, there are two overrides and it's complaining about just one of them.
        @Suppress("ABSTRACT_SUPER_CALL_WARNING")
        super.create(createdAt, barEvent)
        Telos1HubMission.seed = genRandom

        thisExt.startingStage = Stage.GoToSectorEdge
        thisExt.setSuccessStage(Stage.Completed)

        // 95k ish, we want the player to take this and it's gonna be far away.
        thisExt.setCreditReward(CreditReward.VERY_HIGH)

        thisExt.name = json.query("/strings/intel/title") as String
        thisExt.personOverride = stage1Engineer

        // todo change me
        thisExt.setIconName(InteractionDefinition.Portrait(category = "intel", id = "red_planet").spriteName(game))

        state.startLocation = createdAt?.primaryEntity

        state.karengoPlanet = SystemFinder()
            .requireSystemOnFringeOfSector()
            .requireSystemHasAtLeastNumJumpPoints(min = 1)
            .requirePlanetNotGasGiant()
            .preferEntityUndiscovered()
            .preferMarketConditions(ReqMode.ALL, Conditions.HABITABLE)
            .preferPlanetWithRuins()
            .preferPlanetInDirectionOfOtherMissions()
            .preferSystemNotPulsar()
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

        // Sets the system as the map objective.
        thisExt.makeImportant(state.karengoSystem?.hyperspaceAnchor, null, Stage.GoToSectorEdge)
        thisExt.makePrimaryObjective(state.karengoSystem?.hyperspaceAnchor)

        // Complete Part 1, show conclusion dialog.
        trigger {
            beginWithinHyperspaceRangeTrigger(state.karengoSystem, 1f, true, Stage.GoToSectorEdge)

            triggerCustomAction {
                val interactionDialog = Telo1CompleteDialog().build()
                dialog.plugin = interactionDialog
                interactionDialog.show(game.sector.campaignUI, game.sector.playerFleet)
                thisExt.setCurrentStage(Stage.Completed, dialog, null)
                currentStage = Stage.Completed
                game.sector.playerFleet.clearAssignments()
            }
        }
    }

    override fun endSuccessImpl(dialog: InteractionDialogAPI?, memoryMap: MutableMap<String, MemoryAPI>?) {
        super.endSuccessImpl(dialog, memoryMap)

        state.completeDateInMillis = game.sector.clock.timestamp

        // Credit reward is automatically given and shown.
    }

    override fun endAbandonImpl() {
        super.endAbandonImpl()
        game.logger.i { "Abandoned ${this.name} quest." }

        state.map.clear()
        thisExt.setCurrentStage(null, null, null)
    }

    enum class Stage {
        GoToSectorEdge,
        Completed,
    }
}