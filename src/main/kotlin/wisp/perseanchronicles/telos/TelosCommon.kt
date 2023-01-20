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
            ),
            masterMod = MOD_ID
        )

    fun onGameLoad() {
        ColorVariables.colors["pc_telos"] = game.sector.getFaction(FACTION_TELOS).color
        ColorVariables.colors["pc_karengo"] = Color.decode("#359FFF")
        ColorVariables.colors["pc_computer"] = Color.decode("#87C6FF")
        ColorVariables.colors["pc_krypta"] = Color.decode("#0F8BFB")
    }

    val FACTION_TELOS
        get() = "perseanchronicles_telos"

    const val JAVELIN_ID = "wisp_perseanchronicles_avalok"

    const val DART_NAME = "Vara"

    fun playThemeMusic(fadeOutSeconds: Int = 3, fadeInSeconds: Int = 3) {
        val musicSetId = "wisp_perseanchronicles_telosThemeMusic"

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
