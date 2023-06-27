package wisp.perseanchronicles.laborer

import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.PluginPick
import com.fs.starfarer.api.campaign.*
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.ids.Industries
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.ui.SectorMapAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.api.util.WeightedRandomPicker
import wisp.perseanchronicles.common.PerseanChroniclesNPCs
import wisp.perseanchronicles.game
import wisp.questgiver.starSystemsAllowedForQuests
import wisp.questgiver.v2.QGHubMissionWithBarEvent
import wisp.questgiver.wispLib.*
import java.awt.Color

class LaborerHubMission : QGHubMissionWithBarEvent(missionId = MISSION_ID) {
    companion object {
        const val MISSION_ID = "laborer"
        val tags = setOf(Tags.INTEL_STORY, Tags.INTEL_ACCEPTED)

        val state = State(PersistentMapData<String, Any?>(key = "laborerState").withDefault { null })
        val choices: Choices = Choices(PersistentMapData<String, Any?>(key = "laborerChoices").withDefault { null })
        val dale: PersonAPI
            get() = PerseanChroniclesNPCs.dale

        fun calculatePayout(): Int =
            WeightedRandomPicker<Int>()
                .apply {
                    this.add(100000, 1f) // 4.88%
                    this.add(80000, 1.5f) // 7.32%
                    this.add(50000, 2f) // 9.76%
                    this.add(30000, 3f) // 14.63%
                    this.add(10000, 4f) // 19.51%
                    this.add(5000, 4f) // 19.51%
                    this.add(1000, 4f) // 19.51%
                    this.add(0, 2f) // 9.76%
                }
                .pick()
    }

    class State(val map: MutableMap<String, Any?>) {
        var startDateMillis: Long? by map
        var startLocation: MarketAPI? by map
        var destPlanet: SectorEntityToken? by map
        var payout: Int by map.setDefault { calculatePayout() }
        var completeDateInMillis: Long? by map

        val destSystem: StarSystemAPI?
            get() = destPlanet?.starSystem
    }

    val choices: Choices =
        Choices(PersistentMapData<String, Any?>(key = "laborerChoices").withDefault { null })

    class Choices(val map: MutableMap<String, Any?>) {
        var askedAllWorkDriedUp by map
        var askedHowDoIKnowYoullPay by map
    }

    override fun updateTextReplacements(text: Text) {
        text.globalReplacementGetters["laborerDestPlanet"] = { state.destPlanet?.name }
        text.globalReplacementGetters["laborerDestSystem"] = { state.destSystem?.name }
        text.globalReplacementGetters["laborerCredits"] = { Misc.getDGSCredits(state.payout.toFloat()) }
    }

    override fun shouldShowAtMarket(market: MarketAPI?): Boolean {
        market ?: return false

        return market.factionId in listOf(Factions.INDEPENDENT)
                && market.starSystem != null // No hyperspace markets
                && market.size > 2
                && market.hasIndustry(Industries.MINING)
                && LaborerBarEventWiring().shouldBeAddedToBarEventPool()
    }

    override fun create(createdAt: MarketAPI?, barEvent: Boolean): Boolean {
        super.create(createdAt, barEvent)
//        state.seed = genRandom

        createdAt ?: return false
        state.startLocation = createdAt

        state.destPlanet = game.sector.starSystemsAllowedForQuests
            .filter { it.distanceFromPlayerInHyperspace > 3f }
            .flatMap { it.solidPlanets }
            .filter { planet ->
                (planet.faction?.isHostileTo(game.sector.playerFaction) != true)
                        && planet.market?.factionId?.toLowerCase() !in listOf("luddic_church", "luddic_path")
                        && planet.market?.hasIndustry(Industries.MINING) == true
                        && planet.market.size > 2
            }
            .ifEmpty { null }
            ?.random()
            ?: kotlin.run {
                game.logger.i { "No planet found for Laborer quest." }
                return false
            }

        game.logger.i { "Laborer start location set to ${state.startLocation?.name} in ${state.startLocation?.starSystem?.baseName}" }

        startingStage = Stage.GoToPlanet
        setSuccessStage(Stage.Completed)
        setAbandonStage(Stage.Abandoned)

        name = game.text["lab_intel_title"]
        setGiverFaction(Factions.INDEPENDENT) // Rep reward.
        personOverride = dale // Shows on intel, needed for rep reward or else crash.
        setIconName(dale.portraitSprite)
        setCreditReward(0)
        setNoRepChanges()

        if (game.sector.hasScript(PayoutScript::class.java)) {
            game.sector.removeScript(PayoutScript(game.sector.clock))
        }

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

        state.startDateMillis = game.sector.clock.timestamp
        state.startLocation = startLocation.market
        game.logger.i { "${this.name} start location set to ${startLocation.fullName} in ${startLocation.starSystem.baseName}" }
        state.startDateMillis = game.sector.clock.timestamp
        PerseanChroniclesNPCs.isLaborerInFleet = true

        // Sets the system as the map objective.
        makeImportant(state.destPlanet, null, Stage.GoToPlanet)
        makePrimaryObjective(state.destPlanet)
    }

    override fun endSuccessImpl(dialog: InteractionDialogAPI?, memoryMap: MutableMap<String, MemoryAPI>?) {
        super.endSuccessImpl(dialog, memoryMap)
        state.completeDateInMillis = game.sector.clock.timestamp
        PerseanChroniclesNPCs.isLaborerInFleet = false

        if (!game.sector.hasScript(PayoutScript::class.java)) {
            game.sector.addScript(PayoutScript(game.sector.clock))
        }
    }

    override fun endAbandonImpl() {
        super.endAbandonImpl()
        game.logger.i { "Abandoning ${this.name} quest." }
        PerseanChroniclesNPCs.isLaborerInFleet = false

        state.map.clear()
        choices.map.clear()
        setCurrentStage(null, null, null)
    }

    override fun pickInteractionDialogPlugin(interactionTarget: SectorEntityToken): PluginPick<InteractionDialogPlugin>? {
        return when {
            // Finish laborer quest
            interactionTarget.hasSameMarketAs(state.destPlanet)
                    && currentStage == Stage.GoToPlanet -> {
                PluginPick(
                    Laborer_Stage2_Dialog().build(),
                    CampaignPlugin.PickPriority.MOD_SPECIFIC
                )
            }

            else -> super.pickInteractionDialogPlugin(interactionTarget)
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
            ) { game.text["lab_intel_subtitle"] }
        }

        return true
    }

    /**
     * Description on right side of intel.
     */
    override fun addDescriptionForCurrentStage(info: TooltipMakerAPI, width: Float, height: Float) {
        val textColor = textColorOrElseGrayIf {
            currentStage == Stage.Completed
        }
        info.addPara(
            padding = Padding.DESCRIPTION_PANEL,
            textColor = textColor
        ) {
            game.text["lab_intel_description_para1"]
        }
        info.addPara(
            padding = Padding.DESCRIPTION_PANEL,
            textColor = textColor
        ) {
            game.text["lab_intel_description_para2"]
        }

        if (currentStage == Stage.Completed) {
            info.addPara(
                textColor = textColorOrElseGrayIf { currentStage == Stage.Paid },
                padding = Padding.DESCRIPTION_PANEL
            ) {
                game.text["lab_intel_description_completed_para1"]
            }
        }

        if (currentStage == Stage.Paid) {
            info.addPara(
                padding = Padding.DESCRIPTION_PANEL
            ) {
                if (state.payout == 0) {
                    game.text["lab_intel_description_paid_noPayment_para1"]
                } else {
                    game.text["lab_intel_description_paid_sentPayment_para1"]
                }
            }
        }
    }

    override fun getIntelTags(map: SectorMapAPI?) = super.getIntelTags(map) + tags

    enum class Stage {
        NotStarted,
        GoToPlanet,
        Completed,
        Paid,
        Abandoned
    }
}

internal class PayoutScript(clock: CampaignClockAPI) : EveryFrameScript {
    private var isDone = false
    val intervalUtil = com.fs.starfarer.api.util.IntervalUtil(
        clock.convertToSeconds(14f), clock.convertToSeconds(180f)
    )

    override fun isDone(): Boolean = isDone

    override fun runWhilePaused(): Boolean = false

    override fun advance(amount: Float) {
        intervalUtil.advance(amount)

        if (intervalUtil.intervalElapsed() && !isDone) {
            payPlayer()
            isDone = true
        }
    }

    private fun payPlayer() {
        val mission: LaborerHubMission = Global.getSector().intelManager.findFirst() ?: return

        mission.setCurrentStage(LaborerHubMission.Stage.Paid, null, null)
        game.sector.playerFleet.cargo.credits.add(LaborerHubMission.state.payout.toFloat())
        mission.sendUpdateIfPlayerHasIntel(null, false)

        if (game.sector.hasScript(PayoutScript::class.java)) {
            game.sector.removeScript(PayoutScript(game.sector.clock))
        }
    }
}