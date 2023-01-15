package wisp.perseanchronicles.telos

import wisp.perseanchronicles.MOD_ID
import wisp.perseanchronicles.game
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

    val telepathyColor: Color
        get() = game.sector.getFaction(FACTION_TELOS).color
    val FACTION_TELOS
        get() = "perseanchronicles_telos"

    const val JAVELIN_ID = "wisp_perseanchronicles_avalok"

    const val DART_NAME = "Vara"

    /**
     * Idempotent.
     */
    fun playThemeMusic() {
        val musicId = "wisp_perseanchronicles_telosThemeMusic"

        kotlin.runCatching {
            if (game.soundPlayer.currentMusicId != musicId) {
                game.soundPlayer.playCustomMusic(
                    /* fadeOutIfAny = */ 0,
                    /* fadeIn = */ 0,
                    /* musicSetId = */ musicId,
                    /* looping = */ true
                )
            }
        }
            .onFailure { game.logger.e(it) }
    }

    /**
     * Idempotent.
     */
    fun playDoomedMusic(fadeOut: Int, fadeIn: Int) {
        val musicId = "wisp_perseanchronicles_telosDoomedMusic"
        kotlin.runCatching {
            if (game.soundPlayer.currentMusicId != musicId) {
                game.soundPlayer.playCustomMusic(
                    /* fadeOutIfAny = */ fadeOut,
                    /* fadeIn = */ fadeIn,
                    /* musicSetId = */ musicId,
                    /* looping = */ true
                )
            }
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
