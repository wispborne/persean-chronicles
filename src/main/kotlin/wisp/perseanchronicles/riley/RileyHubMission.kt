package wisp.perseanchronicles.riley

import com.fs.starfarer.api.PluginPick
import com.fs.starfarer.api.campaign.*
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.ui.SectorMapAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import wisp.perseanchronicles.common.PersChronCharacters
import wisp.perseanchronicles.game
import wisp.questgiver.InteractionDefinition
import wisp.questgiver.spriteName
import wisp.questgiver.starSystemsAllowedForQuests
import wisp.questgiver.v2.QGHubMissionWithBarEvent
import wisp.questgiver.wispLib.*
import java.awt.Color
import java.util.*
import kotlin.math.roundToInt

// TODO: lower the credit reward to < 5k, but give the location of a blueprint or two on success because "my gramma's a historian"
class RileyHubMission : QGHubMissionWithBarEvent(missionId = MISSION_ID) {
    companion object {
        const val MISSION_ID = "riley"
        val tags = listOf(Tags.INTEL_STORY, Tags.INTEL_ACCEPTED)

        const val BOUNTY_CREDITS = 20000
        const val TIME_LIMIT_DAYS = 30
        const val DAYS_UNTIL_DIALOG = 3

        // Both Hegemony and VIC would have cause to work on subservient AI
        private val govtsSponsoringSafeAi = listOf(Factions.HEGEMONY, "vic")
        val icon = InteractionDefinition.Portrait(category = "wisp_perseanchronicles_riley", id = "icon")

        val isFatherWorkingWithGovt: Boolean
            get() = state.destinationPlanet?.faction?.id?.lowercase() in govtsSponsoringSafeAi

        val state = State(PersistentMapData<String, Any?>(key = "rileyState").withDefault { null })
        val choices: Choices = Choices(PersistentMapData<String, Any?>(key = "rileyChoices").withDefault { null })
        val riley: PersonAPI
            get() = PersChronCharacters.riley
    }

    class State(val map: MutableMap<String, Any?>) {
        var seed: Random? by map
        var startDateMillis: Long? by map
        var completeDateInMillis: Long? by map
        var startLocation: SectorEntityToken? by map
        var destinationPlanet: SectorEntityToken? by map
    }

    val choices: Choices =
        Choices(PersistentMapData<String, Any?>(key = "rileyChoices").withDefault { null })

    /**
     * All choices that can be made.
     * Leave `map` public and accessible so it can be cleared if the quest is restarted.
     */
    class Choices(val map: MutableMap<String, Any?>) {
        var askedWhyNotBuyOwnShip by map
        var refusedPayment by map
        var askedAboutDJingPay by map
        var visitedFather by map
        var movedCloserToRiley by map
        var heldRiley by map
        var askedIfLegal by map
        var askedWhatRileyThinks by map
        var triedToConvinceToJoinYou by map
        var leftRileyWithFather by map
        var destroyedTheCore by map
        var turnedInForABounty by map
    }

    override fun updateTextReplacements(text: Text) {
        text.globalReplacementGetters["rileyDestPlanet"] = { state.destinationPlanet?.name }
        text.globalReplacementGetters["rileyCredits"] = { Misc.getDGSCredits(creditsReward.toFloat()) }
        text.globalReplacementGetters["rileyTimeLimitDays"] = { TIME_LIMIT_DAYS }
        text.globalReplacementGetters["rileyDestSystem"] = { state.destinationPlanet?.starSystem?.name }
        text.globalReplacementGetters["rileyDestPlanetDistanceLY"] = {
            if (state.destinationPlanet == null) String.empty
            else state.destinationPlanet?.starSystem?.let { dest ->
                state.startLocation?.starSystem?.distanceFrom(dest)
                    ?.roundToInt()
                    ?.coerceAtLeast(1)
                    .toString()
            }
        }
        text.globalReplacementGetters["rileyDestPlanetControllingFaction"] =
            { state.destinationPlanet?.faction?.displayNameWithArticle }
        text.globalReplacementGetters["rileyOriginPlanet"] = { state.startLocation?.name }
        text.globalReplacementGetters["rileyBountyCredits"] = { Misc.getDGSCredits(BOUNTY_CREDITS.toFloat()) }
    }

    override fun shouldShowAtMarket(market: MarketAPI?): Boolean {
        market ?: return false

        return market.size > 5 // Lives on a populous world
                && market.starSystem != null // No hyperspace markets
                && market.factionId.lowercase() !in listOf("luddic_church", "luddic_path")
                && market.connectedEntities.none { it?.id == state.destinationPlanet?.id }
                && state.destinationPlanet != null
    }

    override fun create(createdAt: MarketAPI?, barEvent: Boolean): Boolean {
        super.create(createdAt, barEvent)
        state.seed = genRandom

        createdAt?.starSystem ?: return false
        state.destinationPlanet = findAndTagNewDestinationPlanet(createdAt.starSystem)
        val planet = state.destinationPlanet
        game.logger.i { "Set Riley quest destination to ${planet?.fullName} in ${planet?.starSystem?.baseName}" }

        startingStage = Stage.InitialTraveling
        setSuccessStage(Stage.Completed)
        setAbandonStage(Stage.Abandoned)
        addFailureStages(Stage.FailedTimeout)

        name = game.text["riley_intel_title"]
        setCreditReward(CreditReward.HIGH)
        setGiverFaction(riley.faction?.id) // Rep reward.
        personOverride = riley // Shows on intel, needed for rep reward or else crash.

        setIconName(RileyHubMission.icon.spriteName(game))

        state.startLocation = createdAt.primaryEntity

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
        state.startLocation = startLocation
        game.logger.i { "${this.name} start location set to ${startLocation.fullName} in ${startLocation.starSystem.baseName}" }
        state.startDateMillis = game.sector.clock.timestamp


        game.logger.i { "Riley start planet set to ${startLocation.fullName} in ${startLocation.starSystem.baseName}" }
        state.startLocation = startLocation
        state.startDateMillis = game.sector.clock.timestamp
        game.sector.addScript(Riley_Stage2_TriggerDialogScript())
        game.sector.addListener(Riley_EnteredDestinationSystemListener())

        setTimeLimit(
            /* failStage = */ Stage.FailedTimeout,
            /* days = */ TIME_LIMIT_DAYS.toFloat(),
            /* noLimitWhileInSystem = */ state.destinationPlanet?.starSystem
        )

        // Sets the system as the map objective.
        makeImportant(state.destinationPlanet, null, Stage.InitialTraveling)
        makePrimaryObjective(state.destinationPlanet)
    }


    fun showEnteredDestSystemDialog() {
        game.sector.campaignUI.showInteractionDialog(Riley_Stage3_Dialog().build(), game.sector.playerFleet)
    }

    override fun endSuccessImpl(dialog: InteractionDialogAPI?, memoryMap: MutableMap<String, MemoryAPI>?) {
        super.endSuccessImpl(dialog, memoryMap)
        state.completeDateInMillis = game.sector.clock.timestamp
        // Credit reward is automatically given and shown.
    }

    override fun endAbandonImpl() {
        super.endAbandonImpl()
        game.logger.i { "Abandoning ${this.name} quest." }

        state.map.clear()
        choices.map.clear()
        setCurrentStage(null, null, null)
    }

    override fun pickInteractionDialogPlugin(interactionTarget: SectorEntityToken): PluginPick<InteractionDialogPlugin>? {
        return when {
            // Finish Riley quest by landing at father's planet
            interactionTarget.hasSameMarketAs(state.destinationPlanet)
                    && currentStage.equalsAny(
                Stage.InitialTraveling,
                Stage.LandingOnPlanet
            ) -> {
                PluginPick(
                    Riley_Stage4_Dialog().build(),
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
            bullet(info)
            info.addPara(
                padding = 0f,
                textColor = Misc.getGrayColor()
            ) { game.text["riley_intel_subtitle"] }
            info.addPara(
                padding = 0f,
                textColor = Misc.getGrayColor()
            ) {
                game.text.getf(
                    "riley_intel_subtitle_daysLeft",
                    "daysLeft" to (TIME_LIMIT_DAYS - daysSincePlayerVisible).toInt()
                )
            }
        }

        return true
    }

    /**
     * Description on right side of intel.
     */
    override fun addDescriptionForCurrentStage(info: TooltipMakerAPI, width: Float, height: Float) {
        info.addImage(
            icon,
            width,
            128f,
            Padding.DESCRIPTION_PANEL
        )
        info.addPara(
            padding = Padding.DESCRIPTION_PANEL,
            textColor = textColorOrElseGrayIf { currentStage == Stage.Completed }) {
            game.text["riley_intel_description"]
        }

        if (currentStage != Stage.Completed) {
            info.addPara(
                padding = Padding.DESCRIPTION_PANEL
            ) {
                game.text.getf(
                    "riley_intel_subtitle_daysLeft",
                    "daysLeft" to (TIME_LIMIT_DAYS - daysSincePlayerVisible).toInt()
                )
            }
        }

        if (currentStage == Stage.Completed) {
            when {
                choices.destroyedTheCore == true -> {
                    info.addPara(padding = Padding.DESCRIPTION_PANEL) {
                        game.text["riley_intel_description_done_destroyed"]
                    }
                }

                choices.turnedInForABounty == true -> {
                    info.addPara(padding = Padding.DESCRIPTION_PANEL) {
                        game.text["riley_intel_description_done_bounty"]
                    }
                }

                choices.leftRileyWithFather == true -> {
                    info.addPara(padding = Padding.DESCRIPTION_PANEL) {
                        game.text["riley_intel_description_done_leftAlone"]
                    }
                }
            }
        }
    }

    fun showDaysPassedDialog() {
        game.sector.campaignUI.showInteractionDialog(Riley_Stage2_Dialog().build(), game.sector.playerFleet)
    }

    /**
     * Randomly choose a planet that is far from starting point and owned by certain factions.
     */
    private fun findAndTagNewDestinationPlanet(starSystem: StarSystemAPI): PlanetAPI? {
        return game.sector.starSystemsAllowedForQuests
            .sortedByDescending { it.center.distanceFrom(starSystem.center) }
            .filter { it.id != starSystem.id }
            .flatMap { it.solidPlanets }
            .prefer { it.market?.factionId?.lowercase() in govtsSponsoringSafeAi }
            .prefer { (it.market?.size ?: 0) > 2 }
            .getNonHostileOnlyIfPossible()
            .take(5)
            .ifEmpty { null }
            ?.random()
            .also { planet ->
                game.logger.i { "Riley destination planet set to ${planet?.fullName} in ${planet?.starSystem?.baseName}" }
            }
    }

    override fun getIntelTags(map: SectorMapAPI?) =
        (super.getIntelTags(map) + tags).distinct().toSet()

    enum class Stage {
        NotStarted,
        InitialTraveling,
        TravellingToSystem,
        LandingOnPlanet,
        Completed,
        Abandoned,
        FailedTimeout,
    }
}