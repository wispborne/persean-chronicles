package org.wisp.stories.riley

import com.fs.starfarer.api.campaign.PlanetAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.characters.FullName
import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.util.Misc
import org.wisp.stories.QuestFacilitator
import org.wisp.stories.dangerousGames.Utilities
import org.wisp.stories.game
import wisp.questgiver.wispLib.*
import kotlin.math.roundToInt

object RileyQuest : QuestFacilitator {
    private const val REWARD_CREDITS = 100000
    private const val BOUNTY_CREDITS = 20000
    const val TIME_LIMIT_DAYS = 30
    const val DAYS_UNTIL_DIALOG = 2
    private val govtsSponsoringSafeAi = listOf("hegemony", "vic")
    const val iconPath = "graphics/riley/riley.png"

    var startDate: Long? by PersistentNullableData("rileyStartDate")
        private set

    var startPlanet: SectorEntityToken? by PersistentNullableData("rileyStartPlanet")
        private set

    var destinationPlanet: SectorEntityToken? by PersistentNullableData("rileyDestinationPlanet")
        private set

    var stage: Stage by PersistentData(key = "rileyStage", defaultValue = { Stage.NotStarted })
        private set

    var choices: MutableMap<String, Any?> by PersistentData(
        key = "rileyChoices",
        defaultValue = { mutableMapOf<String, Any?>() })

    val isFatherWorkingWithGovt: Boolean
        get() = destinationPlanet?.faction?.id?.toLowerCase() in govtsSponsoringSafeAi

    object ChoiceKey {
        const val askedWhyNotBuyOwnShip = "askedWhyNotBuyOwnShip"
        const val tookPayment = "tookPayment"
        const val askedAboutDJingPay = "askedAboutDJingPay"
        const val visitedFather = "visitedFather"
        const val movedCloserToRiley = "movedCloserToRiley"
        const val heldRiley = "heldRiley"
        const val askedIfLegal = "askedIfLegal"
        const val askedWhatRileyThinks = "askedWhatRileyThinks"
        const val triedToConvinceToJoinYou = "triedToConvinceToJoinYou"
        const val leftRileyWithFather = "leftRileyWithFather"
        const val destroyedTheCore = "destroyedTheCore"
        const val turnedInForABounty = "turnedInForABounty"
    }

    fun shouldMarketOfferQuest(marketAPI: MarketAPI): Boolean =
        stage == Stage.NotStarted
                && marketAPI.size > 5 // Lives on a populous world
                && marketAPI.factionId.toLowerCase() !in listOf("luddic_church", "luddic_path")
                && marketAPI.starSystem in Utilities.getSystemsForQuestTarget() // Valid system, not blacklisted

    override fun updateTextReplacements() {
        game.text.globalReplacementGetters["rileyDestPlanet"] = { destinationPlanet?.name }
        game.text.globalReplacementGetters["rileyCredits"] = { Misc.getDGSCredits(REWARD_CREDITS.toFloat()) }
        game.text.globalReplacementGetters["rileyTimeLimitDays"] = { TIME_LIMIT_DAYS }
        game.text.globalReplacementGetters["rileyDestSystem"] = { destinationPlanet?.starSystem?.baseName }
        game.text.globalReplacementGetters["rileyDestPlanetDistanceLY"] = {
            if (destinationPlanet == null) String.empty
            else startPlanet?.starSystem?.distanceFrom(destinationPlanet!!.starSystem)
                ?.roundToInt()
                ?.coerceAtLeast(1)
                .toString()
        }
        game.text.globalReplacementGetters["rileyDestPlanetControllingFaction"] =
            { destinationPlanet?.faction?.displayNameWithArticle }
        game.text.globalReplacementGetters["rileyOriginPlanet"] = { startPlanet?.name }
        game.text.globalReplacementGetters["rileyBountyCredits"] = { Misc.getDGSCredits(BOUNTY_CREDITS.toFloat()) }
    }

    /**
     * On player interacting with bar event prompt. Chooses the destination planet.
     */
    fun init(startingPlanet: PlanetAPI) {
        startPlanet = startingPlanet
        findAndTagDestinationPlanetIfNeeded(startingPlanet)
        updateTextReplacements()
    }

    /**
     * On player accepting the quest.
     */
    fun start(startingPlanet: PlanetAPI) {
        game.logger.i { "Riley start planet set to ${startingPlanet.fullName} in ${startingPlanet.starSystem.baseName}" }
        startPlanet = startingPlanet
        updateTextReplacements()
        stage = Stage.InitialTraveling
        startDate = game.sector.clock.timestamp
        game.sector.addScript(Riley_Stage2_TriggerDialogScript())
        game.sector.intelManager.addIntel(RileyIntel(startingPlanet, destinationPlanet!!))
    }

    /**
     * Randomly choose a planet that is far from starting point and owned by certain factions.
     */
    private fun findAndTagDestinationPlanetIfNeeded(startPlanet: PlanetAPI) {
        if (destinationPlanet == null) {
            val planets = Utilities.getSystemsForQuestTarget()
                .sortedByDescending { it.distanceFrom(startPlanet.starSystem) }
                .flatMap { it.planets }

            // Both Hegemony and VIC would have cause to work on subservient AI
            destinationPlanet = planets
                .filter { it.market?.factionId?.toLowerCase() in govtsSponsoringSafeAi }
                .ifEmpty { planets }
                .take(5)
                .random()
                .also { planet ->
                    game.logger.i { "Riley destination planet set to ${planet?.fullName} in ${planet?.starSystem?.baseName}" }
                }
        }
    }

    fun showDaysPassedDialog() {
        game.sector.campaignUI.showInteractionDialog(Riley_Stage2_Dialog().build(), game.sector.playerFleet)
    }

    fun startStage3() {
        stage = Stage.TravellingToSystem
        game.sector.addListener(EnteredDestinationSystemListener())
    }

    fun showEnteredDestSystemDialog() {
        game.sector.campaignUI.showInteractionDialog(Riley_Stage3_Dialog().build(), game.sector.playerFleet)
    }

    fun startStage4() {
        stage = Stage.LandingOnPlanet
    }

    fun complete() {
        stage = Stage.Completed
        game.intelManager.findFirst(RileyIntel::class.java)
            ?.apply {
                endAfterDelay()
                sendUpdateIfPlayerHasIntel(null, false)
            }
    }

    enum class Stage {
        NotStarted,
        InitialTraveling,
        TravellingToSystem,
        LandingOnPlanet,
        Completed
    }
}