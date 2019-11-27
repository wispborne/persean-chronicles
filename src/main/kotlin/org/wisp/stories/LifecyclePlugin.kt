package org.wisp.stories

import com.fs.starfarer.api.BaseModPlugin
import com.thoughtworks.xstream.XStream
import org.wisp.stories.dragons.DragonsQuest1BarEvent
import org.wisp.stories.dragons.DragonsQuest1Intel
import org.wisp.stories.wispLib.MOD_NAME
import org.wisp.stories.wispLib.di
import org.wisp.stories.wispLib.i

class LifecyclePlugin : BaseModPlugin() {

    override fun onNewGameAfterTimePass() {
        super.onNewGameAfterTimePass()
    }

    override fun onGameLoad(newGame: Boolean) {
        super.onGameLoad(newGame)
        applyBlacklistTagsToSystems()
    }

    override fun beforeGameSave() {
        super.beforeGameSave()
    }

    /**
     * Tell the XML serializer to use custom naming, so that moving or renaming classes doesn't break saves.
     */
    override fun configureXStream(x: XStream) {
        super.configureXStream(x)

        x.alias("DragonsQuest1Intel", DragonsQuest1Intel::class.java)
        x.alias("DragonsQuest1BarEvent", DragonsQuest1BarEvent::class.java)
    }


    private fun applyBlacklistTagsToSystems() {
        val blacklistedSystems = try {
            val jsonArray = di.settings
                .getMergedSpreadsheetDataForMod(
                    "id", "data/config/stories_system_blacklist.csv",
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
            di.logger.error(e.message, e)
            emptyList<BlacklistEntry>()
        }

        val systems = di.sector.starSystems

        // Mark all blacklisted systems as blacklisted, remove tags from ones that aren't
        for (system in systems) {
            if (blacklistedSystems.any { it.systemId == system.id }) {
                di.logger.i { "Blacklisting system: ${system.id}" }
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