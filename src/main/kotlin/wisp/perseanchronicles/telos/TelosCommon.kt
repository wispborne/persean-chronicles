package wisp.perseanchronicles.telos

import wisp.perseanchronicles.MOD_ID
import wisp.perseanchronicles.game
import wisp.questgiver.wispLib.getMergedJSONForMod

object TelosCommon {
    fun readJson() =
        game.settings.getMergedJSONForMod(
            paths = listOf(
                "data/strings/telos_pt1.hjson",
                "data/strings/telos_pt2_common.hjson",
                "data/strings/telos_pt2_psicon.hjson",
                "data/strings/telos_pt2_noPsicon.hjson",
            ),
            masterMod = MOD_ID
        )
}