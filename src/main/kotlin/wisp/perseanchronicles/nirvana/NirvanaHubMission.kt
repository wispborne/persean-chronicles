package wisp.perseanchronicles.nirvana

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
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.json.JSONObject
import org.lwjgl.util.vector.Vector2f
import wisp.perseanchronicles.MOD_ID
import wisp.perseanchronicles.game
import wisp.perseanchronicles.telos.TelosCommon
import wisp.questgiver.*
import wisp.questgiver.v2.QGHubMissionWithBarEvent
import wisp.questgiver.v2.json.optQuery
import wisp.questgiver.v2.json.query
import wisp.questgiver.wispLib.*
import java.awt.Color
import java.util.*

class NirvanaHubMission : QGHubMissionWithBarEvent(MISSION_ID) {
    companion object {
        val MISSION_ID = "nirvana"

        const val CARGO_TYPE = Commodities.HEAVY_MACHINERY
        const val CARGO_WEIGHT = 5
        val icon = InteractionDefinition.Portrait(category = "wisp_perseanchronicles_nirvana", id = "davidRengel")
        val background = InteractionDefinition.Illustration(category = "wisp_perseanchronicles_nirvana", id = "background")

        val state = State(PersistentMapData<String, Any?>(key = "nirvanaState").withDefault { null })

        val tags = listOf(Tags.INTEL_STORY, Tags.INTEL_ACCEPTED)
    }

    val david: PersonAPI by lazy {
        Global.getSettings().createPerson().apply {
            this.name = FullName("David", "Rengel", FullName.Gender.MALE)
            this.setFaction(Factions.INDEPENDENT)
            this.postId = Ranks.CITIZEN
            this.rankId = Ranks.CITIZEN
            this.portraitSprite = NirvanaHubMission.icon.spriteName(game)
        }
    }

    class State(val map: MutableMap<String, Any?>) {
        var seed: Random? by map
        var startDateMillis: Long? by map
        var startLocation: SectorEntityToken? by map
        var destPlanet: SectorEntityToken? by map
        var completeDateInMillis: Long? by map
        var secretCompleteDateInMillis: Long? by map

        val destSystem: StarSystemAPI?
            get() = destPlanet?.starSystem
    }

    init {
        missionId = MISSION_ID
    }

    override fun shouldShowAtMarket(market: MarketAPI?): Boolean {
        return state.startDateMillis == null // todo
    }

    override fun onGameLoad() {
        super.onGameLoad()
    }

    override fun updateTextReplacements(text: Text) {
        text.globalReplacementGetters["nirvanaCredits"] = { Misc.getDGSCredits(creditsReward.toFloat()) }
        text.globalReplacementGetters["nirvanaDestPlanet"] = { state.destPlanet?.name }
        text.globalReplacementGetters["nirvanaDestSystem"] = { state.destSystem?.name }
        text.globalReplacementGetters["nirvanaCargoTons"] = { CARGO_WEIGHT.toString() }
        text.globalReplacementGetters["nirvanaStarName"] = { state.destPlanet?.starSystem?.star?.name }
    }

    override fun create(createdAt: MarketAPI?, barEvent: Boolean): Boolean {
        // Ignore warning, there are two overrides and it's complaining about just one of them.
        @Suppress("ABSTRACT_SUPER_CALL_WARNING")
        super.create(createdAt, barEvent)
        state.seed = genRandom

        startingStage = Stage.GoToPlanet
        setSuccessStage(Stage.Completed)
        setAbandonStage(Stage.Abandoned)

        name = game.text["nirv_intel_title"]
        setCreditReward(CreditReward.VERY_HIGH) // 95k ish, we want the player to take this.
        setGiverFaction(stage1Engineer.faction.id) // Rep reward.
        personOverride = stage1Engineer // Shows on intel, needed for rep reward or else crash.

        // todo change me
        setIconName(game.settings.getSpriteName(NirvanaHubMission.icon.category, NirvanaHubMission.icon.id))

        state.startLocation = createdAt?.primaryEntity

        findOrCreateAndSetDestination(interactionTarget, createdAt)



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

        game.logger.i { "Nirvana start location set to ${startLocation.fullName} in ${startLocation.starSystem.baseName}" }
        NirvanaQuest.stage = AutoQuestFacilitator.Stage.GoToPlanet
        game.sector.playerFleet.cargo.addCommodity(CARGO_TYPE, CARGO_WEIGHT.toFloat())
        state.startDateMillis = game.sector.clock.timestamp

        state.startLocation = startLocation
        game.logger.i { "${this.name} start location set to ${startLocation.fullName} in ${startLocation.starSystem.baseName}" }
        state.startDateMillis = game.sector.clock.timestamp

        // Sets the system as the map objective.
        makeImportant(state.karengoSystem?.hyperspaceAnchor, null, Stage.GoToSectorEdge)
        makePrimaryObjective(state.karengoSystem?.hyperspaceAnchor)

        // Complete Part 1, show conclusion dialog.
        trigger {
            beginWithinHyperspaceRangeTrigger(state.karengoSystem, 1f, true, Stage.GoToSectorEdge)

            triggerCustomAction {
                Telo1CompleteDialog().build().show(game.sector.campaignUI, game.sector.playerFleet)
                game.sector.playerFleet.clearAssignments()
            }
        }
    }

    override fun endSuccessImpl(dialog: InteractionDialogAPI?, memoryMap: MutableMap<String, MemoryAPI>?) {
        super.endSuccessImpl(dialog, memoryMap)

        state.completeDateInMillis = game.sector.clock.timestamp
        game.sector.playerFleet.cargo.removeCommodity(CARGO_TYPE, CARGO_WEIGHT.toFloat())

        // Credit reward is automatically given and shown.
    }

    override fun endAbandonImpl() {
        super.endAbandonImpl()
        game.logger.i { "Abandoning ${this.name} quest." }

        state.map.clear()
        setCurrentStage(null, null, null)
    }

    fun shouldShowStage2Dialog() =
        NirvanaQuest.stage == AutoQuestFacilitator.Stage.GoToPlanet
                && game.sector.playerFleet.cargo.getCommodityQuantity(CARGO_TYPE) >= CARGO_WEIGHT

    /**
     * Cycles after quest was completed.
     */
    fun shouldShowStage3Dialog(): Boolean {
        // If complete date is set, use that. If not (happens if quest was completed prior to the field being added)
        // then use startDate. If neither exist, use "0" just to avoid null, since the stage needs to be Completed anyway
        // so it won't trigger before then.
        val timestampQuestCompletedInSeconds = (state.completeDateInMillis ?: state.startDateMillis ?: 0)
        return (currentStage == Stage.Completed
                && game.sector.clock.getElapsedDaysSince(timestampQuestCompletedInSeconds) > (365 * 10))
    }

    private fun findOrCreateAndSetDestination(
        interactionTarget: SectorEntityToken,
        market: MarketAPI?
    ) {
        val system = game.sector.starSystemsAllowedForQuests
            .filter { sys -> sys.star?.spec?.isPulsar == true }
            .prefer { it.distanceFromPlayerInHyperspace >= 18 } // 18+ LY away
            .ifEmpty {
                NirvanaQuest.createPulsarSystem()
                NirvanaQuest.regenerateQuest(interactionTarget, market)
                return
            }
            .let { pulsarSystems ->
                val pulsarSystemsWithPlanet =
                    pulsarSystems
                        .filter { sys -> sys.solidPlanets.any { NirvanaQuest.isValidPlanetForDestination(it) } }

                return@let if (pulsarSystemsWithPlanet.isEmpty()) {
                    val system = pulsarSystems.random()
                    NirvanaQuest.addPlanetToSystem(system)
                    system
                } else {
                    pulsarSystemsWithPlanet
                        .prefer { system ->
                            system.solidPlanets.any { planet -> planet.faction?.isHostileTo(game.sector.playerFaction) != true }
                        }
                        .random()
                }
            }

        val planet = system.solidPlanets
            .filter { NirvanaQuest.isValidPlanetForDestination(it) }
            .prefer { planet ->
                planet.faction?.isHostileTo(game.sector.playerFaction) != true
            }
            .minByOrNull { it.market?.hazardValue ?: 500f }
            ?: kotlin.run {
                // This should never happen, the system should be generated by this point.
                game.errorReporter.reportCrash(NullPointerException("No planet found in ${system.name} for Nirvana quest."))
                return
            }

        // Change the planet to be tidally locked so there's a realistic place to set up a base camp.
        planet.spec.rotation = 0f
        planet.applySpecChanges()

        state.destPlanet = planet
    }

    /**
     * Bullet points on left side of intel.
     */
    override fun addNextStepText(info: TooltipMakerAPI, tc: Color, pad: Float): Boolean {
        return when (currentStage) {
            Stage.GoToSectorEdge -> {
                info.addPara(pad, tc) {
                    part1Json.optQuery<String>("/stages/deliveryToEarth/intel/subtitle")?.qgFormat() ?: ""
                }
                true
            }

            else -> false
        }
    }

    /**
     * Description on right side of intel.
     */
    override fun addDescriptionForCurrentStage(info: TooltipMakerAPI, width: Float, height: Float) {
        when (currentStage) {
            Stage.GoToSectorEdge -> {
                info.addPara { part1Json.query<String>("/stages/deliveryToEarth/intel/desc").qgFormat() }
            }

            Stage.Completed -> {
                info.addPara { part1Json.query<String>("/stages/deliveryDropoff/intel/desc").qgFormat() }
            }
        }
    }

    enum class Stage {
        NotStarted,
        GoToPlanet,
        Completed,
        CompletedSecret,
        Abandoned,
    }
}