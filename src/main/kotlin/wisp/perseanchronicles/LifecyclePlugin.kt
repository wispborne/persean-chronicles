package wisp.perseanchronicles

import com.fs.starfarer.api.BaseModPlugin
import com.fs.starfarer.api.characters.FullName
import com.thoughtworks.xstream.XStream
import org.apache.log4j.Level
import org.json.JSONObject
import wisp.perseanchronicles.dangerousGames.pt1_dragons.DragonsPart1_BarEventCreator
import wisp.perseanchronicles.dangerousGames.pt1_dragons.DragonsQuest
import wisp.perseanchronicles.dangerousGames.pt1_dragons.DragonsQuest_Intel
import wisp.perseanchronicles.dangerousGames.pt1_dragons.Dragons_Stage1_BarEvent
import wisp.perseanchronicles.dangerousGames.pt2_depths.*
import wisp.perseanchronicles.laborer.*
import wisp.perseanchronicles.nirvana.*
import wisp.perseanchronicles.riley.*
import wisp.perseanchronicles.telos.pt1_deliveryToEarth.Telos1HubMission
import wisp.questgiver.Configuration
import wisp.questgiver.QuestFacilitator
import wisp.questgiver.Questgiver
import wisp.questgiver.wispLib.firstName
import wisp.questgiver.wispLib.lastName
import wisp.questgiver.wispLib.toStringList
import wisp.questgiver.wispLib.tryGetBoolean
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
//        Locale.setDefault(Locale.GERMAN)

        // When the game (re)loads, we want to grab the new instances of everything, especially the new sector.
        game = SpaceTalesServiceLocator(Questgiver.game)
        game.logger.level = Level.ALL // try to remember to change this for release


        addTextToServiceLocator()

        val settings = game.settings
            .getMergedJSONForMod(
                "data/config/modSettings.json",
                "MagicLib"
            )
            .getJSONObject(MOD_ID)

        Questgiver.loadQuests(
            configuration = readConfiguration(settings),
            questFacilitators = listOf<QuestFacilitator?>(
                if (settings.tryGetBoolean("isDragonsQuestEnabled") { true }) DragonsQuest else null,
                if (settings.tryGetBoolean("isDepthsQuestEnabled") { true }) DepthsQuest else null,
                if (settings.tryGetBoolean("isRileyQuestEnabled") { true }) RileyQuest else null,
                if (settings.tryGetBoolean("isNirvanaQuestEnabled") { true }) NirvanaQuest else null,
                if (settings.tryGetBoolean("isLaborerQuestEnabled") { true }) LaborerQuest else null,
                if (settings.tryGetBoolean("isTelosQuestEnabled") { true }) Telos1HubMission else null,
//                Telos1HubMission.isEnabled = settings.tryGetBoolean("isTelosQuestEnabled") { true }
            )
                .filterNotNull()
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
        game.sector.registerPlugin(CampaignPlugin())
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
            DragonsQuest_Intel::class to "DragonsQuest_Intel",
            Dragons_Stage1_BarEvent::class to "DragonsPart1_BarEvent",
            DragonsPart1_BarEventCreator::class to "DragonsPart1_BarEventCreator",
            CampaignPlugin::class to "CampaignPlugin",
            DragonsQuest.Stage::class to "DragonsQuest_Stage",
            DepthsQuest.Stage::class to "DepthsQuest_Stage",
            DepthsQuest_Intel::class to "DepthsQuest_Intel",
            Depths_Stage1_BarEvent::class to "Depths_Stage1_BarEvent",
            Depths_Stage1_BarEventCreator::class to "Depths_Stage1_BarEventCreator",
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
            RileyIntel::class to "RileyIntel",
            Riley_Stage1_BarEvent::class to "Riley_Stage1_BarEvent",
            Riley_Stage1_BarEventCreator::class to "Riley_Stage1_BarEventCreator",
            Riley_Stage2_TriggerDialogScript::class to "Riley_Stage2_TriggerDialogScript",
            Riley_Stage2_TriggerDialogScript::class to "Riley_Stage2_Dialog",
            Riley_EnteredDestinationSystemListener::class to "Riley_EnteredDestinationSystemListener",
            Riley_Stage3_Dialog::class to "Riley_Stage3_Dialog",
            Riley_Stage4_Dialog::class to "Riley_Stage4_Dialog",
            Nirvana_Stage1_BarEvent::class to "Nirvana_Stage1_BarEvent",
            Nirvana_Stage1_BarEventCreator::class to "Nirvana_Stage1_BarEventCreator",
            NirvanaIntel::class to "NirvanaIntel",
            Nirvana_Stage2_Dialog::class to "Nirvana_Stage2_Dialog",
            Nirvana_Stage3_Dialog::class to "Nirvana_Stage3_Dialog",
            LaborerIntel::class to "LaborerIntel",
            Laborer_Stage1_BarEvent::class to "Laborer_Stage1_BarEvent",
            Laborer_Stage1_BarEventCreator::class to "Laborer_Stage1_BarEventCreator",
            Laborer_Stage2_Dialog::class to "Laborer_Stage2_Dialog"
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
}