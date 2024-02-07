package wisp.perseanchronicles.telos

import com.fs.starfarer.api.impl.campaign.ids.Factions
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
                // "data/strings/compiled/telos_pt3_escape.hjson", moved into common.
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
    const val ETHERNETWORKED_HULLMOD_ID = "wisp_perseanchronicles_catv"
    const val ETHER_OFFICER_TAG = "wisp_perseanchronicles_etherNetworked"

    /**
     * Don't let player progress past Phase 1 of the questline (unless playername includes wisp or test)
     */
    @Deprecated("Let's goooo")
    val isPhase1
        get() = false

    val isPhase2
        get() = true

    val eugelFactionId: String
        get() =
            if (isKnightsOfLuddEnabled)
                "knights_of_selkie"
            else Factions.LUDDIC_CHURCH

    val knightsOfLuddFactionId = "knights_of_selkie"
    val isKnightsOfLuddEnabled = game.settings.modManager.isModEnabled("knights_of_ludd")
}
