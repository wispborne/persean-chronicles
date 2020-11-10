package org.wisp.stories

import com.fs.starfarer.api.BaseModPlugin
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager
import com.thoughtworks.xstream.XStream
import org.wisp.stories.dangerousGames.pt1_dragons.DragonsPart1_BarEvent
import org.wisp.stories.dangerousGames.pt1_dragons.DragonsPart1_BarEventCreator
import org.wisp.stories.dangerousGames.pt1_dragons.DragonsQuest
import org.wisp.stories.dangerousGames.pt1_dragons.DragonsQuest_Intel
import wisp.questgiver.wispLib.QuestGiver
import wisp.questgiver.wispLib.QuestGiver.MOD_PREFIX
import wisp.questgiver.wispLib.i

class LifecyclePlugin : BaseModPlugin() {
    override fun onApplicationLoad() {
        super.onApplicationLoad()
        QuestGiver.initialize(modPrefix = org.wisp.stories.MOD_PREFIX)
    }

    override fun onGameLoad(newGame: Boolean) {
        super.onGameLoad(newGame)
        // When the game (re)loads, we want to grab the new instances of everything, especially the new sector.
        game = SpaceTalesServiceLocator()
        applyBlacklistTagsToSystems()

        val barEventManager = BarEventManager.getInstance()

        if (DragonsQuest.stage == DragonsQuest.Stage.NotStarted
            && !barEventManager.hasEventCreator(DragonsPart1_BarEventCreator::class.java)
        ) {
            barEventManager.addEventCreator(DragonsPart1_BarEventCreator())
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
        val aliases = listOf(
            DragonsQuest_Intel::class to "DragonsQuest_Intel",
            DragonsPart1_BarEvent::class to "DragonsPart1_BarEvent",
            DragonsPart1_BarEventCreator::class to "DragonsPart1_BarEventCreator",
            CampaignPlugin::class to "CampaignPlugin",
            DragonsQuest.Stage::class to "DragonsQuest_Stage"
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