package wisp.perseanchronicles.telos.pt3_arrow

import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import wisp.perseanchronicles.game
import wisp.perseanchronicles.telos.TelosCommon
import wisp.perseanchronicles.telos.pt3_arrow.nocturne.EthersightAbility
import wisp.perseanchronicles.telos.pt3_arrow.nocturne.NocturneScript
import wisp.questgiver.wispLib.IntervalUtil

class TelosFightOrFlightScript : EveryFrameScript {
    var done: Boolean = false
        set(value) {
            if (value)
                cleanup()
            field = value
        }

    private var enabledVisionAbility = false
    private var hasRun = false
    private var pauseTimer = IntervalUtil(.5f)
    private var hasPausedGame = false

    private var supplies = 0f

    override fun isDone() = done

    override fun runWhilePaused() = true

    override fun advance(amount: Float) {
        // Blind the player and prevent them from using most abilities
        if (!hasRun) {
            game.sector.addTransientScript(NocturneScript())
        }

        if (!enabledVisionAbility && !game.sector.campaignUI.isShowingDialog) {
            enabledVisionAbility = true

            if (TelosCommon.ETHER_SIGHT_ID !in game.sector.characterData.abilities) {
                game.sector.characterData.addAbility(TelosCommon.ETHER_SIGHT_ID)
                game.sector.playerFleet.addAbility(TelosCommon.ETHER_SIGHT_ID)
            }

            game.sector.playerFleet.getAbility(TelosCommon.ETHER_SIGHT_ID)?.activate()
        }

        // Wait a moment so that Ethersight has a moment to appear.
        // Pause the game.
        // Then, zoom out slowly.
        if (!hasPausedGame && !Global.getSector().isPaused) {
            pauseTimer.advance(amount)

            if (pauseTimer.intervalElapsed()) {
                hasPausedGame = true
                Global.getSector().isPaused = true

                game.sector.addTransientScript(SmoothScrollPlayerCampaignZoomScript(endingZoom = EthersightAbility.BOOSTED_MAX_ZOOM, duration = 2f))
            }
        }

        // Damage fleet (just once)
        if (!hasRun) {
            game.sector.playerFleet.fleetData.membersListCopy
                .filter { !it.isMothballed }
                .filter { it.hullId != TelosCommon.ITESH_ID } // Itesh is in flight, not damaged by attack.
                .forEach {
                    // Lower to max 25% CR
                    it.repairTracker.cr = it.repairTracker.cr.coerceAtMost(0.25f)
                }
        }

        // Don't consume supplies, be nice to them
        val fleet = game.sector.playerFleet
        val currentSupplies: Float = fleet.cargo.supplies

        if (!Global.getSector().isPaused && !(currentSupplies > supplies)) {
            fleet.cargo.addSupplies(supplies - fleet.cargo.supplies)
        } else {
            supplies = currentSupplies
        }

        hasRun = true
    }

    fun cleanup() {
        game.sector.playerFleet.fleetData.membersListCopy
            .forEach { ship ->
                ship.repairTracker.cr = ship.repairTracker.maxCR
                ship.repairTracker.isSuspendRepairs = false
                ship.fleetData.setSyncNeeded()
                ship.fleetData.syncIfNeeded()
            }


        kotlin.runCatching {
            game.jukebox.stopAllCustomMusic()
        }.onFailure { game.logger.w(it) }

        game.sector.removeTransientScriptsOfClass(NocturneScript::class.java)
    }
}