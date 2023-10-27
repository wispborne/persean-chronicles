package wisp.perseanchronicles.achievements

import org.magiclib.achievements.MagicAchievement
import org.magiclib.achievements.MagicAchievementRarity
import org.magiclib.achievements.MagicAchievementSpec
import org.magiclib.achievements.MagicAchievementSpoilerLevel
import wisp.perseanchronicles.MOD_ID
import wisp.perseanchronicles.MOD_NAME

internal object Achievements {
    class PignutsAchievementSpec : MagicAchievementSpec(
        modId = MOD_ID,
        modName = MOD_NAME,
        id = "pignuts",
        name = "PG-13",
        description = "Uttered a curse word during Karengo's adventures.",
        tooltip = null,
        script = PignutsAchievement::class.java.name,
        image = null,
        spoilerLevel = MagicAchievementSpoilerLevel.Hidden,
        rarity = MagicAchievementRarity.Common
    )

    /**
     * Completed in [wisp.perseanchronicles.dangerousGames.pt1_dragons.Dragons_Stage2_Dialog].
     */
    class PignutsAchievement : MagicAchievement()
}