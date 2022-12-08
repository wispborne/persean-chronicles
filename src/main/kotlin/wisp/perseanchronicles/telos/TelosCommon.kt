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
                "data/strings/compiled/telos_pt2_psicon.hjson",
                "data/strings/compiled/telos_pt2_noPsicon.hjson",
                "data/strings/compiled/telos_pt3_psicon.hjson",
                "data/strings/compiled/telos_pt3_noPsicon.hjson",
            ),
            masterMod = MOD_ID
        )

    val telepathyColor: Color
        get() = game.sector.getFaction(FACTION_TELOS).color
    private val FACTION_TELOS
        get() = "perseanchronicles_telos"

    const val JAVELIN_ID = "wisp_perseanchronicles_avalok"

    const val DART_NAME = "Dart"
}
