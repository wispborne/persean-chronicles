package org.wisp.stories

import com.fs.starfarer.api.BaseModPlugin
import com.fs.starfarer.api.characters.FullName
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager
import com.thoughtworks.xstream.XStream
import org.wisp.stories.dangerousGames.pt1_dragons.Dragons_Stage1_BarEvent
import org.wisp.stories.dangerousGames.pt1_dragons.DragonsPart1_BarEventCreator
import org.wisp.stories.dangerousGames.pt1_dragons.DragonsQuest
import org.wisp.stories.dangerousGames.pt1_dragons.DragonsQuest_Intel
import org.wisp.stories.dangerousGames.pt2_depths.*
import org.wisp.stories.riley.*
import wisp.questgiver.wispLib.QuestGiver
import wisp.questgiver.wispLib.QuestGiver.MOD_PREFIX
import wisp.questgiver.wispLib.firstName
import wisp.questgiver.wispLib.i
import wisp.questgiver.wispLib.lastName

class LifecyclePlugin : BaseModPlugin() {
    override fun onApplicationLoad() {
        super.onApplicationLoad()
        QuestGiver.initialize(modPrefix = org.wisp.stories.MOD_PREFIX)
    }

    override fun onGameLoad(newGame: Boolean) {
        super.onGameLoad(newGame)
        // When the game (re)loads, we want to grab the new instances of everything, especially the new sector.
        game = SpaceTalesServiceLocator()
        QuestGiver.onGameLoad()

        game.text.globalReplacementGetters["playerFirstName"] = { game.sector.playerPerson.firstName }
        game.text.globalReplacementGetters["playerLastName"] = { game.sector.playerPerson.lastName }
        game.text.globalReplacementGetters["playerPronoun"] = {
            when (game.sector.playerPerson.gender) {
                FullName.Gender.MALE -> game.text["playerPronounHim"]
                FullName.Gender.FEMALE -> game.text["playerPronounHer"]
                else -> game.text["playerPronounThey"]
            }
        }
        listOf(DragonsQuest, DepthsQuest)
            .forEach { it.updateTextReplacements() }

        applyBlacklistTagsToSystems()

        val barEventManager = BarEventManager.getInstance()

        if (DragonsQuest.stage == DragonsQuest.Stage.NotStarted
            && !barEventManager.hasEventCreator(DragonsPart1_BarEventCreator::class.java)
        ) {
            barEventManager.addEventCreator(DragonsPart1_BarEventCreator())
        }

        if (DepthsQuest.stage == DepthsQuest.Stage.NotStarted
            && !barEventManager.hasEventCreator(Depths_Stage1_BarEventCreator::class.java)
        ) {
            barEventManager.addEventCreator(Depths_Stage1_BarEventCreator())
        }

        if (RileyQuest.stage == RileyQuest.Stage.NotStarted
            && !barEventManager.hasEventCreator(Riley_Stage1_BarEventCreator::class.java)
        ) {
            barEventManager.addEventCreator(Riley_Stage1_BarEventCreator())
        }

        // Register this so we can intercept and replace interactions
        game.sector.registerPlugin(CampaignPlugin())
    }

    override fun beforeGameSave() {
        super.beforeGameSave()
    }

    /**
     * Tell the XML serializer to use custom naming, so that moving or renaming classes doesn't break saves.
     */
    override fun configureXStream(x: XStream) {
        super.configureXStream(x)

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
            Riley_Stage2_Dialog::class to "Riley_Stage2_Dialog",
            Riley_Stage3_Dialog::class to "Riley_Stage3_Dialog",
            Riley_Stage4_Dialog::class to "Riley_Stage4_Dialog"
        )

        // Prepend with mod prefix so the classes don't conflict with anything else getting serialized
        aliases.forEach { x.alias("${MOD_PREFIX}_${it.second}", it.first.java) }
    }


    private fun applyBlacklistTagsToSystems() {
        val blacklistedSystems = try {
            val jsonArray = game.settings
                .getMergedSpreadsheetDataForMod(
                    "id",
                    "data/config/stories_system_blacklist.csv",
                    MOD_NAME
                )
            val blacklist = mutableListOf<BlacklistEntry>()

            for (i in 0 until jsonArray.length()) {
                val jsonObj = jsonArray.getJSONObject(i)

                blacklist += BlacklistEntry(
                    id = jsonObj.getString("id"),
                    systemId = jsonObj.getString("systemId"),
                    isBlacklisted = jsonObj.optBoolean("isBlacklisted", true),
                    priority = jsonObj.optInt("priority", 0)
                )
            }

            // Sort so that the highest priorities are first
            // Then run distinctBy, which will always keep only the first element it sees for a key
            blacklist
                .sortedByDescending { it.priority }
                .distinctBy { it.systemId }
                .filter { it.isBlacklisted }
        } catch (e: Exception) {
            game.logger.error(e.message, e)
            emptyList<BlacklistEntry>()
        }

        val systems = game.sector.starSystems

        // Mark all blacklisted systems as blacklisted, remove tags from ones that aren't
        for (system in systems) {
            if (blacklistedSystems.any { it.systemId == system.id }) {
                game.logger.i { "Blacklisting system: ${system.id}" }
                system.addTag(Tags.TAG_BLACKLISTED_SYSTEM)
            } else {
                system.removeTag(Tags.TAG_BLACKLISTED_SYSTEM)
            }
        }
    }

    private data class BlacklistEntry(
        val id: String,
        val systemId: String,
        val isBlacklisted: Boolean = true,
        val priority: Int? = 0
    )
}