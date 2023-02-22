package wisp.perseanchronicles.telos.pt2_dart.battle

import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.mission.FleetSide
import wisp.perseanchronicles.common.BattleSide
import wisp.perseanchronicles.game
import wisp.perseanchronicles.telos.TelosCommon
import wisp.perseanchronicles.telos.pt2_dart.Telos2HubMission
import wisp.questgiver.wispLib.findFirst
import wisp.questgiver.wispLib.say
import wisp.questgiver.wispLib.swapFleets

class Telos2BattleScript(private val playerFleetHolder: CampaignFleetAPI) : BaseEveryFrameCombatPlugin() {
    private val telos2HubMission = game.intelManager.findFirst<Telos2HubMission>()

    private var totalTimeElapsed = 0f
    private var secsSinceWave1WasDefeated: Float? = null
    private val secsBeforeWave2Arrives = 4
    private val secsBeforeWave3Arrives = 15
    private var secsSinceWave2Arrived: Float? = null
    private var secsSinceWave3Arrived: Float? = null

    private val hegFleet = Telos2BattleCoordinator.createHegemonyFleetReinforcements()
    private val captEugeneShip = hegFleet.flagship
    private val wave2 = hegFleet.fleetData.membersListCopy.filter { it.isFlagship }
    private val wave3 = hegFleet.fleetData.membersListCopy.filter { !it.isFlagship }

    private val quotes = telos2HubMission?.getBattleQuotes() ?: emptyList()
    private val quotesItr = quotes.iterator()
    private var secsSinceLastQuote: Float? = null
    private var saidLastQuote = false
    private var startedThemeMusic = false
    private var startedDoomedMusic = false

    override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {
        val combatEngine = game.combatEngine!!
        if (combatEngine.isPaused)
            return

        totalTimeElapsed += amount

        // Wait a moment after start because otherwise there's a race condition starting music.
        if (!startedThemeMusic && totalTimeElapsed > 1f) {
            game.soundPlayer.setSuspendDefaultMusicPlayback(true)
            TelosCommon.playThemeMusic(0, 0)
            startedThemeMusic = true
        }

        if (secsSinceWave1WasDefeated != null && !startedDoomedMusic) {
            TelosCommon.playDoomedMusic(fadeOutSecs = 3, fadeInSecs = 3)
            startedDoomedMusic = true
        }

        val enemyFleetManager = combatEngine.getFleetManager(FleetSide.ENEMY)
        val hasDestroyedEnoughOfEnemy = enemyFleetManager.destroyedCopy.size > 5

        secsSinceWave2Arrived = secsSinceWave2Arrived?.plus(amount)
        secsSinceWave3Arrived = secsSinceWave3Arrived?.plus(amount)
        secsSinceLastQuote = secsSinceLastQuote?.plus(amount)
        secsSinceWave1WasDefeated = secsSinceWave1WasDefeated?.plus(amount)

        // Spawn wave 2 five seconds after player defeats initial fleet.
        if (secsSinceWave1WasDefeated == null && (combatEngine.isEnemyInFullRetreat || hasDestroyedEnoughOfEnemy)
        ) {
            secsSinceWave1WasDefeated = 0f
            combatEngine.combatNotOverFor = 10f // seconds. Prevents player from claiming victory after they think they've won.
        }

        if (secsSinceWave2Arrived == null
            && secsSinceWave1WasDefeated != null
            && secsSinceWave1WasDefeated!! > secsBeforeWave2Arrives
        ) {
            wave2.forEach { reinforcement ->
                enemyFleetManager.addToReserves(reinforcement)
            }

            secsSinceWave2Arrived = 0f
        }

        // Eugel starts spouting quotes after he arrives
        val eugelInBattle = combatEngine.ships.firstOrNull { it.fleetMemberId == captEugeneShip.id }

        if (eugelInBattle != null && eugelInBattle.isAlive) {
            if (!combatEngine.isCombatOver) {
                // Spout quotes periodically.
                if (secsSinceWave2Arrived != null && quotesItr.hasNext()) {
                    if ((secsSinceLastQuote ?: Float.MAX_VALUE) > (30..60).random()) {
                        eugelInBattle.say(
                            text = quotesItr.next(),
                            prependShipNameInCorner = true
                        )
                        secsSinceLastQuote = 0f
                    }
                }
            }

            val playerFleet = combatEngine.getFleetManager(FleetSide.PLAYER)
            if (playerFleet.deployedCopy.isEmpty() && playerFleet.reservesCopy.isEmpty() && !saidLastQuote) {
                // Speak final quote on victory.
                eugelInBattle.say(
                    text = telos2HubMission?.getBattleVictoryQuote().orEmpty(),
                    prependShipNameInCorner = true
                )
                saidLastQuote = true
            }
        }

        // Spawn wave 3 a short time after wave 2.
        if (secsSinceWave3Arrived == null
            && secsSinceWave2Arrived != null
            && secsSinceWave2Arrived!! > secsBeforeWave3Arrives
        ) {
            wave3.forEach { reinforcement ->
                enemyFleetManager.addToReserves(reinforcement)
            }

            secsSinceWave3Arrived = 0f
        }

        // Battle is over!
        if (secsSinceWave2Arrived != null && combatEngine.isCombatOver) {
            onTelosBattleEnded(
                didPlayerWin = combatEngine.winningSideId == BattleSide.PLAYER,
                originalPlayerFleet = playerFleetHolder
            )
            combatEngine.removePlugin(this)
        }
    }

    /**
     * Call after the battle ends.
     */
    private fun onTelosBattleEnded(
        didPlayerWin: Boolean,
        originalPlayerFleet: CampaignFleetAPI
    ) {
        Telos2HubMission.state.wonRecordedBattle = didPlayerWin

        if (didPlayerWin) {
            game.logger.i { "Cheater cheater pumpkin eater!" }
            // How tf did player win? hax
            // todo
        }

        // Give the player back their fleet.
        game.sector.playerFleet.swapFleets(
            otherFleet = originalPlayerFleet
        )
        game.sector.reportBattleFinished(if (didPlayerWin) game.sector.playerFleet else hegFleet, null)
    }
}