package wisp.perseanchronicles

import com.fs.starfarer.api.BaseModPlugin
import com.fs.starfarer.api.characters.FullName
import com.thoughtworks.xstream.XStream
import org.apache.log4j.Level
import org.dark.shaders.util.ShaderLib
import org.dark.shaders.util.TextureData
import org.json.JSONObject
import wisp.perseanchronicles.dangerousGames.pt1_dragons.DragonsBarEventWiring
import wisp.perseanchronicles.dangerousGames.pt1_dragons.DragonsHubMission
import wisp.perseanchronicles.dangerousGames.pt1_dragons.Dragons_Stage1_BarEvent
import wisp.perseanchronicles.dangerousGames.pt2_depths.*
import wisp.perseanchronicles.laborer.*
import wisp.perseanchronicles.nirvana.*
import wisp.perseanchronicles.riley.*
import wisp.perseanchronicles.telos.pt1_deliveryToEarth.Telos1BarEventWiring
import wisp.perseanchronicles.telos.pt1_deliveryToEarth.Telos1HubMission
import wisp.perseanchronicles.telos.pt2_dart.Telos2HubMission
import wisp.perseanchronicles.telos.pt3_arrow.Telos3HubMission
import wisp.questgiver.Configuration
import wisp.questgiver.Questgiver
import wisp.questgiver.wispLib.firstName
import wisp.questgiver.wispLib.lastName
import wisp.questgiver.wispLib.toStringList
import wisp.questgiver.wispLib.tryGet
import java.util.*

class LifecyclePlugin : BaseModPlugin() {
    init {
        Questgiver.init(modPrefix = MOD_ID)
    }

    override fun onApplicationLoad() {
        super.onApplicationLoad()
        addTextToServiceLocator()
    }

    override fun onGameLoad(newGame: Boolean) {
        super.onGameLoad(newGame)
        Questgiver.onGameLoad()

        // When the game (re)loads, we want to grab the new instances of everything, especially the new sector.
        game = SpaceTalesServiceLocator(Questgiver.game, CampaignPlugin())
        game.logger.level = Level.ALL // try to remember to change this for release


        addTextToServiceLocator()

        val settings = game.settings
            .getMergedJSONForMod(
                "data/config/modSettings.json",
                "MagicLib"
            )
            .getJSONObject(MOD_ID)

        Questgiver.loadQuests(
            questFacilitators = listOfNotNull(
                if (settings.tryGet("isLaborerQuestEnabled") { true }) LaborerQuest else null,
            ),
            creators = listOfNotNull(
                if (settings.tryGet("isTelosQuestEnabled") { true })
                    Telos1BarEventWiring()
                else null,
                if (settings.tryGet("isDragonsQuestEnabled") { true }) DragonsBarEventWiring() else null,
                if (settings.tryGet("isDepthsQuestEnabled") { true }) DepthsBarEventWiring() else null,
                if (settings.tryGet("isRileyQuestEnabled") { true }) RileyBarEventWiring() else null,
                if (settings.tryGet("isNirvanaQuestEnabled") { true }) NirvanaBarEventWiring() else null,
            ),
            configuration = readConfiguration(settings),
        )

        game.text.globalReplacementGetters["playerFirstName"] = { game.sector.playerPerson.firstName }
        game.text.globalReplacementGetters["playerLastName"] = { game.sector.playerPerson.lastName }
        game.text.globalReplacementGetters["playerPronoun"] = {
            when (game.sector.playerPerson.gender) {
                FullName.Gender.MALE -> game.text["playerPronounHim"]
                FullName.Gender.FEMALE -> game.text["playerPronounHer"]
                else -> game.text["playerPronounThey"]
            }
        }
        game.text.globalReplacementGetters["playerPronounHeShe"] = {
            when (game.sector.playerPerson.gender) {
                FullName.Gender.MALE -> game.text["playerPronounHe"]
                FullName.Gender.FEMALE -> game.text["playerPronounShe"]
                else -> game.text["playerPronounThey"]
            }
        }
        game.text.globalReplacementGetters["playerFlagshipName"] = { game.sector.playerFleet.flagship?.shipName }

        // Register this so we can intercept and replace interactions
        game.sector.registerPlugin(game.campaignPlugin)

        initGraphicsLib()
    }

    /**
     * Tell the XML serializer to use custom naming, so that moving or renaming classes doesn't break saves.
     */
    override fun configureXStream(x: XStream) {
        super.configureXStream(x)
        Questgiver.configureXStream(x)

        // DO NOT CHANGE THESE STRINGS, DOING SO WILL BREAK SAVE GAMES
        // No periods allowed in the serialized name, causes crash.
        val aliases = listOf(
            CampaignPlugin::class to "CampaignPlugin",
            Dragons_Stage1_BarEvent::class to "DragonsPart1_BarEvent",
            DragonsHubMission::class to "DragonsHubMission",
            DragonsHubMission.Stage::class to "DragonsHubMission_Stage",
            DragonsBarEventWiring::class to "DragonsBarEventWiring",
            DepthsHubMission::class to "DepthsHubMission",
            DepthsHubMission.Stage::class to "DepthsHubMission_Stage",
            DepthsBarEventLogic::class to "DepthsBarEventLogic",
            DepthsBarEventWiring::class to "DepthsBarEventWiring",
            DepthsBarEventCreator::class to "DepthsBarEventCreator",
            Depths_Stage2_RiddleDialog::class to "Depths_Stage2_RiddleDialog",
            Depths_Stage2_EndDialog::class to "Depths_Stage2_EndDialog",
            Depths_Stage2_RiddleDialog.RiddleChoice.Riddle1Choice.EastMorg::class to "1East",
            Depths_Stage2_RiddleDialog.RiddleChoice.Riddle1Choice.NorthSuccess::class to "1North",
            Depths_Stage2_RiddleDialog.RiddleChoice.Riddle1Choice.SouthSmoke::class to "1South",
            Depths_Stage2_RiddleDialog.RiddleChoice.Riddle1Choice.WestWall::class to "1West",
            Depths_Stage2_RiddleDialog.RiddleChoice.Riddle2Choice.EastSuccess::class to "2East",
            Depths_Stage2_RiddleDialog.RiddleChoice.Riddle2Choice.NorthVines::class to "2North",
            Depths_Stage2_RiddleDialog.RiddleChoice.Riddle2Choice.WestWall::class to "2West",
            Depths_Stage2_RiddleDialog.RiddleChoice.Riddle3Choice.NorthKoijuu::class to "3North",
            Depths_Stage2_RiddleDialog.RiddleChoice.Riddle3Choice.EastWall::class to "3East",
            Depths_Stage2_RiddleDialog.RiddleChoice.Riddle3Choice.SouthSuccess::class to "3South",
            RileyHubMission::class to "RileyHubMission",
            Riley_Stage1_BarEvent::class to "Riley_Stage1_BarEvent",
            RileyBarEventWiring::class to "RileyBarEventWiring",
            Riley_Stage2_TriggerDialogScript::class to "Riley_Stage2_TriggerDialogScript",
            Riley_Stage2_TriggerDialogScript::class to "Riley_Stage2_Dialog",
            Riley_EnteredDestinationSystemListener::class to "Riley_EnteredDestinationSystemListener",
            Riley_Stage3_Dialog::class to "Riley_Stage3_Dialog",
            Riley_Stage4_Dialog::class to "Riley_Stage4_Dialog",
            NirvanaHubMission::class to "NirvanaHubMission",
            Nirvana_Stage1_BarEvent::class to "Nirvana_Stage1_BarEvent",
            NirvanaBarEventCreator::class to "NirvanaBarEventCreator",
            Nirvana_Stage2_Dialog::class to "Nirvana_Stage2_Dialog",
            Nirvana_Stage3_Dialog::class to "Nirvana_Stage3_Dialog",
            LaborerIntel::class to "LaborerIntel",
            Laborer_Stage1_BarEvent::class to "Laborer_Stage1_BarEvent",
            Laborer_Stage1_BarEventCreator::class to "Laborer_Stage1_BarEventCreator",
            Laborer_Stage2_Dialog::class to "Laborer_Stage2_Dialog",
            Telos1BarEventWiring::class to "Telos1BarEventWiring",
            Telos1HubMission::class to "Telos1HubMission",
            Telos2HubMission::class to "Telos2HubMission",
            Telos3HubMission::class to "Telos3HubMission",
        )

        // Prepend with mod prefix so the classes don't conflict with anything else getting serialized
        aliases.forEach { x.alias("${MOD_ID}_${it.second}", it.first.java) }
    }

    private fun addTextToServiceLocator() {
        game.text.resourceBundles.addAll(
            listOf(
                ResourceBundle.getBundle("Stories_Shared"),
                ResourceBundle.getBundle("Stories_DangerousGames_Dragons"),
                ResourceBundle.getBundle("Stories_DangerousGames_Depths"),
                ResourceBundle.getBundle("Stories_Nirvana"),
                ResourceBundle.getBundle("Stories_Laborer"),
                ResourceBundle.getBundle("Stories_Riley")
            )
        )
    }

    private fun readConfiguration(modSettings: JSONObject): Configuration {
        val startTime = game.sector.clock.timestamp
        val blacklistedEntityTags = kotlin.runCatching {
            modSettings.getJSONArray("entity_tags_to_blacklist")
                .toStringList()
                .distinct()
        }
            .onFailure { game.logger.e(it) { it.message } }
            .getOrElse { emptyList() }

        val blacklistedMarketIds = kotlin.runCatching {
            modSettings.getJSONArray("market_ids_to_blacklist")
                .toStringList()
                .distinct()
        }
            .onFailure { game.logger.e(it) { it.message } }
            .getOrElse { emptyList() }

        val blacklistedSystemIds = kotlin.runCatching {
            modSettings.getJSONArray("system_ids_to_blacklist")
                .toStringList()
                .distinct()
        }
            .onFailure { game.logger.e(it) { it.message } }
            .getOrElse { emptyList() }

        val whitelistedFactions = kotlin.runCatching {
            modSettings.getJSONArray("faction_ids_to_whitelist")
                .toStringList()
                .distinct()
        }
            .onFailure { game.logger.e(it) { it.message } }
            .getOrElse { emptyList() }


        val conf = Configuration(
            blacklist = Configuration.Blacklist(
                systemIds = blacklistedSystemIds,
                marketIds = blacklistedMarketIds,
                systemTags = blacklistedEntityTags
            ),
            whitelist = Configuration.Whitelist(
                factionIds = whitelistedFactions
            )
        )

        game.logger.i { "Persean Chronicles system blacklist loaded in ${game.sector.clock.timestamp - startTime} seconds.\n$conf" }

        return conf
    }

    fun initGraphicsLib() {
        if (game.settings.modManager.isModEnabled("shaderLib")) {
            ShaderLib.init()
//            LightData.readLightDataCSV("data/lights/perseanchronicles_light_data.csv")
            TextureData.readTextureDataCSV("data/lights/perseanchronicles_texture_data.csv")
        }
    }
}