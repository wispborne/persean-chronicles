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

    class CheatedFlashbackBattleAchievementSpec : MagicAchievementSpec(
        modId = MOD_ID,
        modName = MOD_NAME,
        id = "cheatedFlashbackBattle",
        name = "Pumpkin Eater",
        description = "\"Won\" the Telos flashback battle.",
        tooltip = null,
        script = CheatedFlashbackBattleAchievement::class.java.name,
        image = null,
        spoilerLevel = MagicAchievementSpoilerLevel.Hidden,
        rarity = MagicAchievementRarity.Common
    )

    class CheatedFlashbackBattleAchievement : MagicAchievement()

    class DefeatedEugelEarlyAchievementSpec : MagicAchievementSpec(
        modId = MOD_ID,
        modName = MOD_NAME,
        id = "defeatedEugelEarly",
        name = "Vengeance Incarnate",
        description = "Turned around Eugel's hunt, destroying his fleet at its strongest.",
        tooltip = null,
        script = DefeatedEugelEarlyAchievement::class.java.name,
        image = null,
        spoilerLevel = MagicAchievementSpoilerLevel.Hidden,
        rarity = MagicAchievementRarity.Epic
    )

    class DefeatedEugelEarlyAchievement : MagicAchievement()

    class DefeatedEugelAchievementSpec : MagicAchievementSpec(
        modId = MOD_ID,
        modName = MOD_NAME,
        id = "defeatedEugel",
        name = "Avenger of the Telos",
        description = "Defeated Knight-Captain Eugel in battle.",
        tooltip = null,
        script = DefeatedEugelAchievement::class.java.name,
        image = null,
        spoilerLevel = MagicAchievementSpoilerLevel.Spoiler,
        rarity = MagicAchievementRarity.Uncommon
    )

    class DefeatedEugelAchievement : MagicAchievement()
}