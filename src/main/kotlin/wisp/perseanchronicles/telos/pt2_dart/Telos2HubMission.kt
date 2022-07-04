package wisp.perseanchronicles.telos.pt2_dart

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.PluginPick
import com.fs.starfarer.api.campaign.CampaignPlugin
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.InteractionDialogPlugin
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.json.JSONObject
import wisp.perseanchronicles.MOD_ID
import wisp.perseanchronicles.dangerousGames.pt1_dragons.DragonsQuest
import wisp.perseanchronicles.game
import wisp.perseanchronicles.telos.pt1_deliveryToEarth.Telos1HubMission
import wisp.questgiver.InteractionDefinition
import wisp.questgiver.addPara
import wisp.questgiver.spriteName
import wisp.questgiver.v2.QGHubMission
import wisp.questgiver.v2.json.query
import wisp.questgiver.wispLib.PersistentMapData
import wisp.questgiver.wispLib.Text
import wisp.questgiver.wispLib.qgFormat
import wisp.questgiver.wispLib.trigger
import java.awt.Color

class Telos2HubMission : QGHubMission() {
    companion object {
        // Hardcode because it's being used in rules.csv.
        val MISSION_ID = "wisp_perseanchronicles_telosPt2"

        var part2Json: JSONObject =
            Global.getSettings().getMergedJSONForMod("data/strings/telos.hjson", MOD_ID)
                .query("/$MOD_ID/telos/part2_dart") as JSONObject
            private set

        val tags = listOf(Tags.INTEL_STORY, Tags.INTEL_ACCEPTED)

        val state = State(PersistentMapData<String, Any?>(key = "telosPt2State").withDefault { null })
        val choices = Choices(PersistentMapData<String, Any?>(key = "telosPt2Choices").withDefault { null })
        const val badFleetDefeatTrigger = "wisp_perseanchronicles_telosPt2_badfleetdefeated"

        fun startBattle() = Telos2Battle.startBattle()
    }

    class State(val map: MutableMap<String, Any?>) {
        var startDateMillis: Long? by map
        var completeDateInMillis: Long? by map
    }

    class Choices(val map: MutableMap<String, Any?>) {
        var askedForMorePsiconInfo: Boolean? by map
        var toldKarengoToTakePsiconFirst: Boolean? by map
        var injectedSelf: Boolean? by map // Null if choice not made yet.
    }


    init {
        missionId = MISSION_ID
    }

    override fun onGameLoad() {
        super.onGameLoad()

        if (isDevMode())
            part2Json = Global.getSettings().getMergedJSONForMod("data/strings/telos.hjson", MOD_ID)
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
        super.create(createdAt, barEvent)
        setGenRandom(Telos1HubMission.state.seed ?: Misc.random)

        setStartingStage(Stage.DestroyFleet)
        setSuccessStage(Stage.Completed)

        name = part2Json.query("/strings/title")
        personOverride = DragonsQuest.karengo // Shows on intel, needed for rep reward or else crash.

        // todo change me
        setIconName(InteractionDefinition.Portrait(category = "intel", id = "red_planet").spriteName(game))

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
            triggerAutoAdjustFleetStrengthModerate()
            triggerPickLocationAroundEntity(Telos1HubMission.state.karengoPlanet, 1f)
            triggerSpawnFleetAtPickedLocation(badFleetFlag, null)
            triggerOrderFleetPatrol(false, Telos1HubMission.state.karengoPlanet)
            triggerFleetMakeImportant(badFleetImportantFlag, Stage.DestroyFleet)
            triggerFleetAddDefeatTrigger(badFleetDefeatTrigger)
        }

        return true
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
            Stage.LandOnPlanetSecondPsicon,
            Stage.LandOnPlanetSecondNoPsicon,
        )
        makePrimaryObjective(Telos1HubMission.state.karengoPlanet)
    }

    override fun endSuccessImpl(dialog: InteractionDialogAPI?, memoryMap: MutableMap<String, MemoryAPI>?) {
        super.endSuccessImpl(dialog, memoryMap)

        state.completeDateInMillis = game.sector.clock.timestamp
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
        return if (interactionTarget.id == Telos1HubMission.state.karengoPlanet?.id) {
            when (currentStage) {
                Stage.LandOnPlanetFirst -> PluginPick(
                    Telos2FirstLandingDialog().build(),
                    CampaignPlugin.PickPriority.MOD_SPECIFIC
                )
                Stage.LandOnPlanetSecondPsicon,
                Stage.LandOnPlanetSecondNoPsicon -> PluginPick(
                    Telos2SecondLandingDialog().build(),
                    CampaignPlugin.PickPriority.MOD_SPECIFIC
                )
                else -> null
            }
        } else null
    }

    override fun endAbandonImpl() {
        super.endAbandonImpl()
        game.logger.i { "Restarting ${this.name} quest." }

        state.map.clear()
        currentStage = null
    }

    /**
     * Bullet points on left side of intel.
     */
    override fun addNextStepText(info: TooltipMakerAPI, tc: Color?, pad: Float): Boolean {
        return when (currentStage) {
            Stage.DestroyFleet -> {
                info.addPara(padding = 3f, textColor = Misc.getGrayColor()) {
                    part2Json.query<String>("/stages/destroyFleet/intel/subtitle").qgFormat()
                }
                true
            }
            Stage.LandOnPlanetFirst -> {
                info.addPara(padding = 3f, textColor = Misc.getGrayColor()) {
                    part2Json.query<String>("/stages/landOnPlanetFirst/intel/subtitle").qgFormat()
                }
                true
            }
            Stage.LandOnPlanetSecondPsicon -> {
                info.addPara(padding = 3f, textColor = Misc.getGrayColor()) {
                    part2Json.query<String>("/stages/landOnPlanetSecondPsicon/intel/subtitle").qgFormat()
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
            Stage.DestroyFleet -> {
                info.addPara { part2Json.query<String>("/stages/destroyFleet/intel/desc").qgFormat() }
            }
            Stage.LandOnPlanetFirst -> {
                info.addPara { part2Json.query<String>("/stages/landOnPlanetFirst/intel/desc").qgFormat() }
            }
            Stage.LandOnPlanetSecondPsicon -> {
                info.addPara { part2Json.query<String>("/stages/landOnPlanetSecondPsicon/intel/desc").qgFormat() }
            }
        }
    }

    enum class Stage {
        DestroyFleet,
        LandOnPlanetFirst,
        LandOnPlanetSecondPsicon,
        LandOnPlanetSecondNoPsicon,
        PostBattle,
        Completed,
    }
}