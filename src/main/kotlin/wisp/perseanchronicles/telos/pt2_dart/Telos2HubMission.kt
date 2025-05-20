package wisp.perseanchronicles.telos.pt2_dart

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.PluginPick
import com.fs.starfarer.api.campaign.*
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.characters.FullName
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.fleet.FleetMemberType
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.campaign.missions.hub.MissionTrigger
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity
import com.fs.starfarer.api.ui.SectorMapAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.campaign.Faction
import org.json.JSONArray
import org.json.JSONObject
import org.magiclib.util.MagicCampaign
import wisp.perseanchronicles.MOD_ID
import wisp.perseanchronicles.common.PerseanChroniclesNPCs
import wisp.perseanchronicles.game
import wisp.perseanchronicles.telos.TelosCommon
import wisp.perseanchronicles.telos.pt1_deliveryToEarth.Telos1HubMission
import wisp.perseanchronicles.telos.pt2_dart.battle.Telos2PirateFleetInteractionDialogPluginImpl
import wisp.questgiver.InteractionDefinition
import wisp.questgiver.spriteName
import wisp.questgiver.v2.IQGHubMission
import wisp.questgiver.v2.QGHubMission
import wisp.questgiver.v2.json.query
import wisp.questgiver.wispLib.*
import java.awt.Color
import java.util.*
import kotlin.math.roundToInt

class Telos2HubMission : QGHubMission(), IQGHubMission {
    companion object {
        // Hardcode because it's being used in rules.csv.
        val MISSION_ID = "wisp_perseanchronicles_telosPt2"

        var part2Json: JSONObject =
            TelosCommon.readJson().query("/$MOD_ID/telos/part2_dart") as JSONObject
            private set

        val tags = setOf(Tags.INTEL_STORY, Tags.INTEL_ACCEPTED)

        val state = State(PersistentMapData<String, Any?>(key = "telosPt2State").withDefault { null })
        val choices = Choices(PersistentMapData<String, Any?>(key = "telosPt2Choices").withDefault { null })
        const val badFleetDefeatTrigger = "wisp_perseanchronicles_telosPt2_badfleetdefeated"

        private val PIRATE_FLEET_TAG = MISSION_ID + "_pirateFleet"

        /**
         * Quotes from Captain Eugel in battle. In chronological order.
         */
        fun getEugelBattleQuotes(): List<String> = part2Json.query<JSONArray>("/stages/battle/quotes").toStringList()
        fun getAllyPhase1BattleQuotes(): List<String> = part2Json.query<JSONArray>("/stages/battle/telosQuotesPhase1").toStringList()
        fun getAllyPhase2BattleQuotes(): List<String> = part2Json.query<JSONArray>("/stages/battle/telosQuotesPhase2").toStringList()
        fun getBattleVictoryQuote(): String = part2Json.query("/stages/battle/victoryQuote")
        fun getEugelShipName(): String = part2Json.query("/stages/battle/flagshipName")
    }

    class State(val map: MutableMap<String, Any?>) {
        var startDateMillis: Long? by map
        var completeDateInMillis: Long? by map

        var talkedToPirateFleet: Boolean? by map

        // If they won this, they cheated.
        var wonRecordedBattle: Boolean? by map
    }

    class Choices(val map: MutableMap<String, Any?>) {
        var askedForMoreEtherInfo: Boolean? by map
        var toldKarengoToTakeEtherFirst: Boolean? by map
        var checkedKarengo: Boolean? by map
        var queriedSystem: Boolean? by map
        var injectedSelf: Boolean? by map // Null if choice not made yet.
    }

    init {
        missionId = MISSION_ID
    }

    override fun onGameLoad(isNewGame: Boolean) {
        super.onGameLoad(isNewGame)

        // Reload json if devmode reload.
        if (isDevMode())
            part2Json = TelosCommon.readJson()
                .query("/$MOD_ID/telos/part2_dart") as JSONObject
    }

    override fun updateTextReplacements(text: Text) {
        text.globalReplacementGetters["telosPt1Stg1DestPlanet"] = { Telos1HubMission.state.karengoPlanet?.name }
        text.globalReplacementGetters["telosPt1Stg1DestSystem"] = { Telos1HubMission.state.karengoSystem?.name }
        text.globalReplacementGetters["telosStarName"] =
            { Telos1HubMission.state.karengoPlanet?.starSystem?.star?.name }
    }

    override fun create(createdAt: MarketAPI?, barEvent: Boolean): Boolean {
        // if already accepted by the player, abort
        if (!setGlobalReference("$$MISSION_ID")) {
            return false
        }

        // Ignore warning, there are two overrides and it's complaining about just one of them.
        @Suppress("ABSTRACT_SUPER_CALL_WARNING")
        super<IQGHubMission>.create(createdAt, barEvent)
        setGenRandom(Telos1HubMission.state.seed ?: Misc.random)

        setStartingStage(Stage.DestroyFleet)
        setSuccessStage(Stage.Completed)
        setAbandonStage(Stage.Abandoned)

        name = part2Json.query("/strings/title")
        personOverride = PerseanChroniclesNPCs.karengo // Shows on intel, needed for rep reward or else crash.

        setIconName(InteractionDefinition.Portrait(category = "wisp_perseanchronicles_telos", id = "intel").spriteName(game))

        val badFleetFlag = "$${MISSION_ID}_badfleet"
        val badFleetImportantFlag = "${badFleetFlag}_important"

        trigger {
            beginStageTrigger(Stage.DestroyFleet)
            triggerCreateFleet(
                FleetSize.MEDIUM,
                FleetQuality.LOWER,
                Factions.PIRATES,
                FleetTypes.SCAVENGER_MEDIUM,
                Telos1HubMission.state.karengoPlanet
            )
            triggerMakeHostile()
            triggerMakeFleetIgnoreOtherFleetsExceptPlayer()
            triggerFleetNoAutoDespawn()
            triggerFleetNoJump()
            triggerMakeFleetIgnoredByOtherFleets()
            triggerSetPirateFleet()
            triggerAutoAdjustFleetStrengthModerate()
            triggerPickLocationAroundEntity(Telos1HubMission.state.karengoPlanet, 1f)
            triggerSpawnFleetAtPickedLocation(badFleetFlag, null)
            triggerOrderFleetPatrol(false, Telos1HubMission.state.karengoPlanet)
            triggerFleetMakeImportant(badFleetImportantFlag, Stage.DestroyFleet)
            triggerFleetAddDefeatTrigger(badFleetDefeatTrigger)
            triggerFleetAddTags(PIRATE_FLEET_TAG)
        }

        // When you land on the planet, Karengo joins your fleet.
        trigger {
            beginStageTrigger(Stage.LandOnPlanetFirst)
            triggerCustomAction(SetKarengoInFleetAction())
        }

        return true
    }

    class SetKarengoInFleetAction : MissionTrigger.TriggerAction {
        override fun doAction(context: MissionTrigger.TriggerActionContext?) {
            PerseanChroniclesNPCs.isKarengoInFleet = true
        }
    }

    override fun acceptImpl(dialog: InteractionDialogAPI?, memoryMap: MutableMap<String, MemoryAPI>?) {
        super.acceptImpl(dialog, memoryMap)

        state.startDateMillis = game.sector.clock.timestamp
        setCurrentStage(Stage.DestroyFleet, null, null)
        makeImportant(
            Telos1HubMission.state.karengoPlanet,
            null,
            Stage.DestroyFleet,
            Stage.LandOnPlanetFirst,
            Stage.LandOnPlanetSecondEther,
            Stage.LandOnPlanetSecondNoEther,
        )
        makePrimaryObjective(Telos1HubMission.state.karengoPlanet)
    }

    override fun endSuccessImpl(dialog: InteractionDialogAPI?, memoryMap: MutableMap<String, MemoryAPI>?) {
        super.endSuccessImpl(dialog, memoryMap)

        state.completeDateInMillis = game.sector.clock.timestamp
    }

    override fun endAbandonImpl() {
        super.endAbandonImpl()
        game.logger.i { "Abandoning ${this.name} quest." }
        PerseanChroniclesNPCs.isKarengoInFleet = false

        state.map.clear()
    }


    override fun callAction(
        action: String?,
        ruleId: String?,
        dialog: InteractionDialogAPI?,
        params: MutableList<Misc.Token>?,
        memoryMap: MutableMap<String, MemoryAPI>?
    ): Boolean {
        // Trigger set in `create` (triggerFleetAddDefeatTrigger), fired off to rules.csv when fleet dies, listen for it here.
        if (action == badFleetDefeatTrigger) {
            setCurrentStage(Stage.LandOnPlanetFirst, null, null)
            return true
        }

        return super.callAction(action, ruleId, dialog, params, memoryMap)
    }

    override fun pickInteractionDialogPlugin(interactionTarget: SectorEntityToken): PluginPick<InteractionDialogPlugin>? {
        return when {
            interactionTarget.hasTag(PIRATE_FLEET_TAG) ->
                PluginPick(Telos2PirateFleetInteractionDialogPluginImpl(), CampaignPlugin.PickPriority.MOD_SPECIFIC)

            interactionTarget.id == Telos1HubMission.state.karengoPlanet?.id -> {
                when (currentStage) {
                    Stage.LandOnPlanetFirst -> PluginPick(
                        Telos2FirstLandingDialog().build(),
                        CampaignPlugin.PickPriority.MOD_SPECIFIC
                    )

                    Stage.LandOnPlanetSecondEther,
                    Stage.LandOnPlanetSecondNoEther -> PluginPick(
                        Telos2SecondLandingDialog().build(),
                        CampaignPlugin.PickPriority.MOD_SPECIFIC
                    )

                    else -> null
                }
            }

            else -> null
        }
    }

    /**
     * Bullet points on left side of intel.
     */
    override fun addNextStepText(info: TooltipMakerAPI, tc: Color, pad: Float): Boolean {
        return when (currentStage) {
            Stage.DestroyFleet -> {
                info.addPara(padding = pad, textColor = tc) {
                    part2Json.query<String>("/stages/destroyFleet/intel/subtitle").qgFormat()
                }
                true
            }

            Stage.LandOnPlanetFirst -> {
                info.addPara(padding = pad, textColor = tc) {
                    part2Json.query<String>("/stages/landOnPlanetFirst/intel/subtitle").qgFormat()
                }
                true
            }

            Stage.LandOnPlanetSecondEther -> {
                info.addPara(padding = pad, textColor = tc) {
                    part2Json.query<String>("/stages/landOnPlanetSecondEther/intel/subtitle").qgFormat()
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
        if (currentStage == Stage.Abandoned) {
            info.addPara { game.text["abandoned"] }
            return
        }

        when (currentStage) {
            Stage.DestroyFleet -> {
                info.addPara { part2Json.query<String>("/stages/destroyFleet/intel/desc").qgFormat() }
                val fleet = Telos1HubMission.state.karengoPlanet?.starSystem?.getEntitiesWithTag(PIRATE_FLEET_TAG)?.firstOrNull()
                if (fleet is CampaignFleetAPI) {
                    appendFleetInfoToIntel(info, fleet, width)
                }
            }

            Stage.LandOnPlanetFirst -> {
                info.addPara { part2Json.query<String>("/stages/landOnPlanetFirst/intel/desc").qgFormat() }
            }

            Stage.LandOnPlanetSecondEther -> {
                info.addPara { part2Json.query<String>("/stages/landOnPlanetSecondEther/intel/desc").qgFormat() }
            }

            Stage.PostBattle,
            Stage.Completed -> {
                info.addPara { part2Json.query<String>("/stages/completed/intel/subtitle").qgFormat() }

                if (TelosCommon.isPhase1) {
                    info.addPara(textColor = Misc.getHighlightColor()) { "Karengo and the Telos will continue in Persean Chronicles v3.2." }
                }
            }
        }
    }

    override fun getIntelTags(map: SectorMapAPI?) =
        (super.getIntelTags(map) + tags)


    /**
     * From [com.fs.starfarer.api.impl.campaign.intel.PersonBountyIntel.createSmallDescription].
     */
    fun appendFleetInfoToIntel(info: TooltipMakerAPI, fleet: CampaignFleetAPI, width: Float) {
        val h = Misc.getHighlightColor()
        val g = Misc.getGrayColor()
        val pad = 3.0f
        val opad = 10.0f
        val cols = 7
        val iconSize: Float = width / cols.toFloat()

        var deflate = false
        if (!fleet.isInflated) {
            fleet.setFaction("pirates", true)
            fleet.inflateIfNeeded()
            deflate = true
        }

        val list: MutableList<FleetMemberAPI?> = mutableListOf()
        val random = Random((person.nameString.hashCode() * 170000).toLong())
        val members: MutableList<FleetMemberAPI> = fleet.fleetData.membersListCopy
        val max = 7

        for (member in members) {
            if (list.size >= max) {
                break
            }

            if (!member.isFighterWing) {
                var prob = member.fleetPointCost.toFloat() / 20.0f
                prob += max.toFloat() / members.size.toFloat()
                if (member.isFlagship) {
                    prob = 1.0f
                }

                if (!(random.nextFloat() > prob)) {
                    val copy = Global.getFactory().createFleetMember(FleetMemberType.SHIP, member.variant)
                    if (member.isFlagship) {
                        copy.captain = person
                    }

                    list.add(copy)
                }
            }
        }

        if (!list.isEmpty()) {
            var her = "her"
            if (person.gender == FullName.Gender.MALE) {
                her = "his"
            }

            info.addPara("The distress call also contains partial intel on some of the pirate ships.", opad)
            info.addShipList(cols, 1, iconSize, factionForUIColors.baseUIColor, list, opad)
            var num = members.size - list.size
            num = (num.toFloat() * (1.0f + random.nextFloat() * 0.5f)).roundToInt()
            num = when {
                num < 5 -> 0
                num < 10 -> 5
                num < 20 -> 10
                else -> 20
            }

            if (num > 1) {
                info.addPara(
                    "The fleet may contain upwards of %s other ships of lesser significance.",
                    opad,
                    h,
                    *arrayOf("" + num)
                )
            } else {
                info.addPara("The fleet may contain several other ships of lesser significance.", opad)
            }
        }

        if (deflate) {
            fleet.deflate()
        }
    }

    fun giveShipOrPutInOrbit(dialog: InteractionDialogAPI) {
        val ship = game.factory.createFleetMember(FleetMemberType.SHIP, TelosCommon.AVALOK_ID)

        if (game.sector.playerFleet.numShips >= game.settings.maxShipsInFleet) {
            val fleet =
                game.factory.createEmptyFleet(Faction.NO_FACTION.id, "Telos ${TelosCommon.DART_NAME}", false).apply {
                    fleetData.addFleetMember(ship)
                }

            val planet = Telos1HubMission.state.karengoPlanet!!
            fleet.containingLocation = planet.containingLocation
            fleet.setLocation(planet.location.x, planet.location.y)
            MagicCampaign.placeOnStableOrbit(fleet, false)
        } else {
            game.sector.playerFleet.fleetData.addFleetMember(ship)
            AddRemoveCommodity.addFleetMemberGainText(ship, dialog.textPanel)
        }
    }

    enum class Stage {
        Abandoned,
        DestroyFleet,
        LandOnPlanetFirst,
        LandOnPlanetSecondEther,
        LandOnPlanetSecondNoEther,
        PostBattle,
        Completed,
    }
}