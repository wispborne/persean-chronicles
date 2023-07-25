package wisp.perseanchronicles.nirvana

import com.fs.starfarer.api.PluginPick
import com.fs.starfarer.api.campaign.*
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.impl.campaign.ids.Commodities
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.ui.SectorMapAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import wisp.perseanchronicles.common.PerseanChroniclesNPCs
import wisp.perseanchronicles.game
import wisp.questgiver.starSystemsAllowedForQuests
import wisp.questgiver.v2.IInteractionLogic
import wisp.questgiver.v2.QGHubMissionWithBarEvent
import wisp.questgiver.v2.spriteName
import wisp.questgiver.wispLib.*
import java.awt.Color
import java.util.*

class NirvanaHubMission : QGHubMissionWithBarEvent(MISSION_ID) {
    companion object {
        val MISSION_ID = "nirvana"

        const val CARGO_TYPE = Commodities.HEAVY_MACHINERY
        const val CARGO_WEIGHT = 5
        val icon = IInteractionLogic.Portrait(category = "wisp_perseanchronicles_nirvana", id = "davidRengel")
        val background =
            IInteractionLogic.Illustration(category = "wisp_perseanchronicles_nirvana", id = "background")

        val state = State(PersistentMapData<String, Any?>(key = "nirvanaState").withDefault { null })
        const val yearsBeforeHiddenEnding = 10

        val tags = setOf(Tags.INTEL_STORY, Tags.INTEL_ACCEPTED)
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
        return NirvanaBarEventWiring().shouldBeAddedToBarEventPool()
                && market != null
                && market.factionId.lowercase() in listOf(Factions.INDEPENDENT.lowercase(), Factions.TRITACHYON.lowercase())
                && market.starSystem != null // No prism freeport
                && market.size > 3
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
        setGiverFaction(PerseanChroniclesNPCs.davidRengal.faction.id) // Rep reward.
        personOverride = PerseanChroniclesNPCs.davidRengal // Shows on intel, needed for rep reward or else crash.

        setIconName(game.settings.getSpriteName(NirvanaHubMission.icon.category, NirvanaHubMission.icon.id))

        state.startLocation = createdAt?.primaryEntity

        findOrCreateAndSetDestination(createdAt!!.primaryEntity, createdAt)

        return true
    }

    override fun acceptImpl(dialog: InteractionDialogAPI?, memoryMap: MutableMap<String, MemoryAPI>?) {
        super.acceptImpl(dialog, memoryMap)

        val startLocation = dialog?.interactionTarget?.market?.preferredConnectedEntity
            ?: kotlin.run {
                game.logger.e { "Aborting acceptance of ${this.name} because dialog was null." }
                abort()
                return
            }

        game.logger.i { "Nirvana start location set to ${startLocation.fullName} in ${startLocation.starSystem.baseName}" }
        game.sector.playerFleet.cargo.addCommodity(CARGO_TYPE, CARGO_WEIGHT.toFloat())
        state.startDateMillis = game.sector.clock.timestamp

        state.startLocation = startLocation
        game.logger.i { "${this.name} start location set to ${startLocation.fullName} in ${startLocation.starSystem.baseName}" }
        state.startDateMillis = game.sector.clock.timestamp

        // Sets the system as the map objective.
        makeImportant(state.destPlanet, null, Stage.GoToPlanet)
        makePrimaryObjective(state.destPlanet)
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
        currentStage == Stage.GoToPlanet
                && game.sector.playerFleet.cargo.getCommodityQuantity(CARGO_TYPE) >= CARGO_WEIGHT

    /**
     * Cycles after quest was completed.
     */
    fun shouldShowStage3Dialog(): Boolean {
        // If complete date is set, use that. If not (happens if quest was completed prior to the field being added)
        // then use startDate. If neither exist, use "0" just to avoid null, since the stage needs to be Completed anyway
        // so it won't trigger before then.
        val timestampQuestCompletedInSeconds = (state.completeDateInMillis ?: state.startDateMillis)
        return (currentStage == Stage.Completed
                && timestampQuestCompletedInSeconds != null
                && game.sector.clock.getElapsedDaysSince(timestampQuestCompletedInSeconds) > (365 * yearsBeforeHiddenEnding))
    }

    private fun findOrCreateAndSetDestination(
        interactionTarget: SectorEntityToken,
        market: MarketAPI?,
        retries: Int = 3
    ) {
        if (retries == 0) return

        val system = game.sector.starSystemsAllowedForQuests
            .filter { sys -> sys.star?.spec?.isPulsar == true }
            .prefer { it.distanceFromPlayerInHyperspace >= 18 } // 18+ LY away
            .ifEmpty {
                NirvanaQuest.createPulsarSystem()
                findOrCreateAndSetDestination(interactionTarget, market, retries - 1)
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

    override fun pickInteractionDialogPlugin(interactionTarget: SectorEntityToken): PluginPick<InteractionDialogPlugin>? {
        return when {
            // Finish Nirvana quest by landing at pulsar planet
            interactionTarget.hasSameMarketAs(state.destPlanet)
                    && shouldShowStage2Dialog() -> {
                PluginPick(
                    Nirvana_Stage2_Dialog().build(),
                    CampaignPlugin.PickPriority.MOD_SPECIFIC
                )
            }

            // Shhhhh
            interactionTarget.hasSameMarketAs(state.destPlanet)
                    && shouldShowStage3Dialog() -> {
                PluginPick(
                    Nirvana_Stage3_Dialog().build(),
                    CampaignPlugin.PickPriority.MOD_SPECIFIC
                )
            }

            else -> null
        }
    }

    /**
     * Bullet points on left side of intel.
     */
    override fun addNextStepText(info: TooltipMakerAPI, tc: Color, pad: Float): Boolean {
        if (currentStage != Stage.Completed) {
            info.addPara(
                padding = 0f,
                textColor = Misc.getGrayColor()
            ) { game.text["nirv_intel_subtitle"] }
            return true
        }

        return false
    }

    /**
     * Description on right side of intel.
     */
    override fun addDescriptionForCurrentStage(info: TooltipMakerAPI, width: Float, height: Float) {
        info.addImage(
            NirvanaHubMission.background.spriteName(game),
            width,
            10f
        )

        when (currentStage) {
            Stage.GoToPlanet -> {
                info.addPara {
                    game.text["nirv_intel_description_para1"]
                }
                info.addPara {
                    game.text["nirv_intel_description_para2"]
                }
            }

            Stage.Completed -> {
                info.addPara {
                    game.text["nirv_intel_description_completed_para1"]
                }
            }
        }
    }

    override fun getIntelTags(map: SectorMapAPI?) = super.getIntelTags(map) + tags

    enum class Stage {
        NotStarted,
        GoToPlanet,
        Completed,
        Abandoned,
    }
}