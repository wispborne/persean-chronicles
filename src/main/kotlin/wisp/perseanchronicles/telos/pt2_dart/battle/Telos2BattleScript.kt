package wisp.perseanchronicles.telos.pt2_dart.battle

import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.mission.FleetSide
import com.fs.starfarer.api.util.Misc
import wisp.perseanchronicles.common.BattleSide
import wisp.perseanchronicles.game
import wisp.perseanchronicles.telos.TelosCommon
import wisp.perseanchronicles.telos.pt2_dart.Telos2HubMission
import wisp.questgiver.wispLib.TextExtensions
import wisp.questgiver.wispLib.findFirst
import wisp.questgiver.wispLib.say
import wisp.questgiver.wispLib.swapFleets

class Telos2BattleScript(private val playerRealFleetHolder: CampaignFleetAPI, val onBattleFinished: () -> Unit) : BaseEveryFrameCombatPlugin() {
    private val telos2HubMission = game.intelManager.findFirst<Telos2HubMission>()

    private var totalTimeElapsed = 0f
    private var secsSinceWave1WasDefeated: Float? = null
    private val secsBeforeWave2Arrives = 4
    private val secsBeforeWave3Arrives = 15
    private var secsSinceWave2Arrived: Float? = null
    private var secsSinceWave3Arrived: Float? = null

    private val hegFleet = Telos2BattleCoordinator.createEugelFleetReinforcements()
    private val captEugeneShip = hegFleet.flagship
    private val wave2 = hegFleet.fleetData.membersListCopy.filter { it.isFlagship }
    private val wave3 = hegFleet.fleetData.membersListCopy.filter { !it.isFlagship }

    private val quotes = Telos2HubMission.getEugelBattleQuotes()
    private val quotesItr = quotes.iterator()
    private var secsSinceLastEugelQuote: Float? = null
    private var secsSinceLastAllyQuote: Float? = null
    private var saidLastQuote = false
    private var startedThemeMusic = false
    private var startedDoomedMusic = false
    private val phase1AllyQuotes = Telos2HubMission.getAllyPhase1BattleQuotes().toMutableList() // Mutable, remove after using.
    private val phase2AllyQuotes = Telos2HubMission.getAllyPhase2BattleQuotes().toMutableList() // Mutable, remove after using.

    override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {
        val combatEngine = game.combatEngine!!
        if (combatEngine.isPaused)
            return

        // Disable Combat Chatter.
        combatEngine.ships.orEmpty().forEach { it.captain?.memoryWithoutUpdate?.set("\$chatterChar", "none") }

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
        secsSinceLastEugelQuote = secsSinceLastEugelQuote?.plus(amount)
        secsSinceLastAllyQuote = secsSinceLastAllyQuote?.plus(amount)
        secsSinceWave1WasDefeated = secsSinceWave1WasDefeated?.plus(amount)
        val playerFleet = combatEngine.getFleetManager(FleetSide.PLAYER)

        // Spawn wave 2 five seconds after player defeats initial fleet.
        if (secsSinceWave1WasDefeated == null && (combatEngine.isEnemyInFullRetreat || hasDestroyedEnoughOfEnemy)
        ) {
            secsSinceWave1WasDefeated = 0f
            combatEngine.combatNotOverFor = 30f // seconds. Prevents player from claiming victory after they think they've won.
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

        // Ally ships say stuff if you took Ether.
        if (!combatEngine.isCombatOver && Telos2HubMission.choices.injectedSelf == true) {
            val quotesToUse =
                (if (secsSinceWave3Arrived == null) phase1AllyQuotes
                else phase2AllyQuotes)
            val quotesWithHighlights = quotesToUse
                .map { TextExtensions.getTextHighlightData(it) }
                .toMutableList()

            if (quotesToUse.isNotEmpty()) {
                if ((secsSinceLastAllyQuote ?: Float.MAX_VALUE) > (30..60).random()) {
                    val quote = quotesWithHighlights.first()
                    // Pick a ship to say the quote, basing the ship on the number of quotes left so that there aren't repeats.
                    playerFleet.deployedCopy
                        .getOrElse(quotesToUse.size.coerceAtMost(playerFleet.deployedCopy.size)) { null }
                        ?.let { fm -> combatEngine.ships.firstOrNull { it.fleetMemberId == fm.id } }
                        ?.say(
                            text = quote.newString,
                            prependShipNameInCorner = true,
                            textColor = quote.replacements.firstOrNull()?.highlightColor ?: Misc.getTextColor()
                        )
                    quotesToUse.removeFirst()
                    secsSinceLastAllyQuote = 0f
                }
            }
        }

        // Eugel starts spouting quotes after he arrives
        val eugelInBattle = combatEngine.ships.firstOrNull { it.fleetMemberId == captEugeneShip.id }

        if (eugelInBattle != null && eugelInBattle.isAlive) {
            if (!combatEngine.isCombatOver) {
                // Spout quotes periodically.
                if (secsSinceWave2Arrived != null && quotesItr.hasNext()) {
                    if ((secsSinceLastEugelQuote ?: Float.MAX_VALUE) > (30..60).random()) {
                        eugelInBattle.say(
                            text = quotesItr.next(),
                            prependShipNameInCorner = true
                        )
                        secsSinceLastEugelQuote = 0f
                    }
                }
            }

            if (playerFleet.deployedCopy.isEmpty() && playerFleet.reservesCopy.isEmpty() && !saidLastQuote) {
                // Speak final quote on victory.
                eugelInBattle.say(
                    text = Telos2HubMission.getBattleVictoryQuote(),
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

        // Battle is over if either the player lost (at any point) or if the player won *after* the reinforcements arrived.
        // If the player won before the reinforcements arrived, the battle is not over yet ;)
        if ((secsSinceWave2Arrived != null || combatEngine.winningSideId == BattleSide.ENEMY) && combatEngine.isCombatOver) {
            onTelosBattleEnded(
                didPlayerWin = combatEngine.winningSideId == BattleSide.PLAYER,
                originalPlayerFleet = playerRealFleetHolder
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
        game.logger.i { "Telos battle ended. Did player win? $didPlayerWin" }
        Telos2HubMission.state.wonRecordedBattle = didPlayerWin

        if (didPlayerWin) {
            game.logger.i { "Cheater cheater pumpkin eater!" }
        }

        game.combatEngine?.endCombat(0f)

        // Give the player back their fleet.
        game.sector.playerFleet.swapFleets(
            otherFleet = originalPlayerFleet
        )

        onBattleFinished()
    }
}