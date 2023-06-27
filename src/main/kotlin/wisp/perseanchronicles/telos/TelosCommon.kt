package wisp.perseanchronicles.telos

import wisp.perseanchronicles.MOD_ID
import wisp.perseanchronicles.game
import wisp.questgiver.wispLib.ColorVariables
import wisp.questgiver.wispLib.getMergedJSONForMod
import java.awt.Color

object TelosCommon {
    fun readJson() =
        game.settings.getMergedJSONForMod(
            paths = listOf(
                "data/strings/compiled/telos_pt1.hjson",
                "data/strings/compiled/telos_pt2_common.hjson",
                "data/strings/compiled/telos_pt2_ether.hjson",
                "data/strings/compiled/telos_pt2_noEther.hjson",
                "data/strings/compiled/telos_pt3_ether.hjson",
                "data/strings/compiled/telos_pt3_noEther.hjson",
                "data/strings/compiled/telos_pt3_common.hjson",
            ),
            masterMod = MOD_ID
        )

    fun onGameLoad() {
        ColorVariables.colors["pc_telos"] = game.sector.getFaction(FACTION_TELOS_ID).color
        ColorVariables.colors["pc_player"] = game.sector.getFaction(FACTION_TELOS_ID).color // #5eb2ff
        ColorVariables.colors["pc_karengo"] = Color.decode("#57C9CE")
        ColorVariables.colors["pc_computer"] = Color.decode("#87C6FF")
        ColorVariables.colors["pc_krypta"] = Color.decode("#9894FB")

        if (game.settings.isDevMode) {
            readJson() // testing
        }
    }

    val FACTION_TELOS_ID
        get() = "perseanchronicles_telos"

    const val VARA_ID = "wisp_perseanchronicles_vara"
    const val ITESH_ID = "wisp_perseanchronicles_itesh"
    const val AVALOK_ID = "wisp_perseanchronicles_avalok"
    const val DART_NAME = "Vara"
    const val ETHER_SIGHT_ID = "wisp_perseanchronicles_ethersight"
    const val ETHER_OFFICER_TAG = "wisp_perseanchronicles_etherNetworked"

    /**
     * Don't let player progress past Phase 1 of the questline (unless playername includes wisp or test)
     */
    val isPhase1
        get() = game.sector?.playerPerson?.nameString?.contains(Regex("""wisp|test""", RegexOption.IGNORE_CASE)) != true

    fun playThemeMusic(fadeOutSeconds: Int = 3, fadeInSeconds: Int = 3) {
        val musicSetId = "wisp_perseanchronicles_telosThemeMusic"
        game.logger.d { "Starting Telos - Theme/Exploration." }

        kotlin.runCatching {
            game.soundPlayer.playCustomMusic(
                /* fadeOutIfAny = */ fadeOutSeconds,
                /* fadeIn = */ fadeInSeconds,
                /* musicSetId = */ musicSetId,
                /* looping = */ true
            )
        }
            .onFailure { game.logger.e(it) }
    }

    fun playDoomedMusic(fadeOutSecs: Int, fadeInSecs: Int, loop: Boolean = false) {
        val musicSetId = "wisp_perseanchronicles_telosDoomedMusic"
        game.logger.d { "Starting Telos - Doomed." }
        kotlin.runCatching {
            game.soundPlayer.playCustomMusic(
                /* fadeOutIfAny = */ fadeOutSecs,
                /* fadeIn = */ fadeInSecs,
                /* musicSetId = */ musicSetId,
                /* looping = */ loop
            )
        }
            .onFailure { game.logger.e(it) }
    }

    fun playEvasionMusic(fadeOutSecs: Int, fadeInSecs: Int, loop: Boolean = false) {
        val musicSetId = "wisp_perseanchronicles_telosEvasionMusic"
        game.logger.d { "Starting Telos - Evasion." }
        kotlin.runCatching {
            game.soundPlayer.playCustomMusic(
                /* fadeOutIfAny = */ fadeOutSecs,
                /* fadeIn = */ fadeInSecs,
                /* musicSetId = */ musicSetId,
                /* looping = */ loop
            )
        }
            .onFailure { game.logger.e(it) }
    }

    fun stopAllCustomMusic() {
        game.logger.d { "Stopping custom music." }
        kotlin.runCatching {
            game.soundPlayer.playCustomMusic(
                /* fadeOutIfAny = */ 0,
                /* fadeIn = */ 5,
                /* musicSetId = */ null,
                /* looping = */ false
            )
        }
            .onFailure { game.logger.e(it) }
    }
}
