package wisp.perseanchronicles.telos.pt3_arrow

import com.fs.starfarer.api.EveryFrameScriptWithCleanup
import wisp.perseanchronicles.game
import wisp.perseanchronicles.telos.TelosCommon
import wisp.perseanchronicles.telos.pt3_arrow.nocturne.NocturneScript

class TelosFightOrFlightScript : EveryFrameScriptWithCleanup {
    private var done = false

    @Transient
    private var didSoundPlayerFail = false
    private var enabledVisionAbility = false
    private var blindedPlayer = false

    override fun isDone() = done

    override fun runWhilePaused() = true

    override fun advance(amount: Float) {
        // "nothing" if music is disabled/volume 0
        if (!didSoundPlayerFail && listOf("TelosEvasion.ogg", "nothing").none { it == game.soundPlayer.currentMusicId }) {
            kotlin.runCatching { TelosCommon.playEvasionMusic(fadeOutSecs = 0, fadeInSecs = 1, loop = true) }
                .onFailure {
                    game.logger.w(it)
                    didSoundPlayerFail = true
                }
        }

        if (!blindedPlayer&& !game.sector.hasTransientScript(NocturneScript::class.java)) {
            game.sector.addTransientScript(NocturneScript())
            blindedPlayer = true
        }

        if (!enabledVisionAbility && !game.sector.campaignUI.isShowingDialog) {
            enabledVisionAbility = true

            if (TelosCommon.ETHER_SIGHT_ID !in game.sector.characterData.abilities) {
                game.sector.characterData.addAbility(TelosCommon.ETHER_SIGHT_ID)
                game.sector.playerFleet.addAbility(TelosCommon.ETHER_SIGHT_ID)
            }

            game.sector.playerFleet.getAbility(TelosCommon.ETHER_SIGHT_ID)?.activate()
        }
    }

    override fun cleanup() {
    }
}