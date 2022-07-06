package wisp.perseanchronicles.telos.pt2_dart

import wisp.perseanchronicles.MOD_ID
import wisp.perseanchronicles.game
import wisp.questgiver.wispLib.getMergedJSONForMod

object TelosCommon {
    fun readJson() =
        game.settings.getMergedJSONForMod(
            paths = listOf(
                "data/strings/telos_pt1.hjson",
                "data/strings/telos_pt2.hjson"
            ),
            masterMod = MOD_ID
        )
}