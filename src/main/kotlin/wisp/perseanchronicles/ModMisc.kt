package wisp.perseanchronicles

import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.impl.campaign.ids.Tags

internal const val MOD_ID = "wisp_perseanchronicles"

// Scattered Stories
// The Perseans
// Persean Storytime
// Space Tales
// Stories
internal const val MOD_NAME = "Persean Chronicles"
internal const val MOD_AUTHOR = "Wisp (Wispborne)"

internal fun MarketAPI.isOkForQuest() =
    !isHidden
            && containingLocation != null
            && !containingLocation.hasTag(PCTags.TAG_BLACKLISTED_SYSTEM)
            && primaryEntity?.hasTag(Tags.NOT_RANDOM_MISSION_TARGET) != true

fun isDevMode() =
    game.settings.isDevMode && game.sector?.playerPerson?.nameString?.contains(Regex("""wisp|test""", RegexOption.IGNORE_CASE)) == true