package wisp.perseanchronicles.telos.pt2_dart.battle

import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.mission.FleetSide
import wisp.perseanchronicles.common.BattleSide
import wisp.perseanchronicles.game
import wisp.perseanchronicles.telos.pt2_dart.Telos2HubMission
import wisp.questgiver.wispLib.findFirst
import wisp.questgiver.wispLib.say
import wisp.questgiver.wispLib.swapFleets

class Telos2BattleScript(private val playerFleetHolder: CampaignFleetAPI) : BaseEveryFrameCombatPlugin() {
    private val telos2HubMission = game.intelManager.findFirst<Telos2HubMission>()

    private val secsBeforeWave3Arrives = 15
    private var secsSinceWave2Arrived: Float? = null
    private var secsSinceWave3Arrived: Float? = null

    private val hegFleet = Telos2Battle.createHegemonyFleetReinforcements()
    private val captEugeneShip = hegFleet.flagship
    private val wave2 = hegFleet.fleetData.membersListCopy.filter { it.isFlagship }
    private val wave3 = hegFleet.fleetData.membersListCopy.filter { !it.isFlagship }

    private val quotes = telos2HubMission?.getBattleQuotes() ?: emptyList()
    private val quotesItr = quotes.iterator()
    private var secsSinceLastQuote: Float? = null
    private var saidLastQuote = false

    override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {
        if (game.combatEngine.isPaused)
            return

        val enemyFleetManager = game.combatEngine.getFleetManager(FleetSide.ENEMY)
        val hasDestroyedEnoughOfEnemy = enemyFleetManager.destroyedCopy.size > 5

        secsSinceWave2Arrived = secsSinceWave2Arrived?.plus(amount)
        secsSinceWave3Arrived = secsSinceWave3Arrived?.plus(amount)
        secsSinceLastQuote = secsSinceLastQuote?.plus(amount)

        // Spawn wave 2 after player defeats initial fleet.
        if (secsSinceWave2Arrived == null && (game.combatEngine.isEnemyInFullRetreat || hasDestroyedEnoughOfEnemy)) {
            game.combatEngine.combatNotOverFor = 10f // seconds
            wave2.forEach { reinforcement ->
                enemyFleetManager.addToReserves(reinforcement)
            }

            secsSinceWave2Arrived = 0f
        }

        // Eugel starts spouting quotes after he arrives
        val eugelInBattle = game.combatEngine.ships.firstOrNull { it.fleetMemberId == captEugeneShip.id }

        if (eugelInBattle != null) {
            if (!game.combatEngine.isCombatOver) {
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
            } else if (game.combatEngine.winningSideId != BattleSide.PLAYER && !saidLastQuote) {
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
        if (secsSinceWave2Arrived != null && game.combatEngine.isCombatOver) {
            onTelosBattleEnded(
                didPlayerWin = game.combatEngine.winningSideId == BattleSide.PLAYER,
                originalPlayerFleet = playerFleetHolder
            )
            game.combatEngine.removePlugin(this)
        }
    }

    /**
     * Call after the battle ends.
     */
    private fun onTelosBattleEnded(
        didPlayerWin: Boolean,
        originalPlayerFleet: CampaignFleetAPI
    ) {
        if (didPlayerWin) {
            game.logger.i { "Cheater cheater pumpkin eater!" }
            // How tf did player win? hax
            // todo
        }

        // Give the player back their fleet.
        game.sector.playerFleet.swapFleets(
            otherFleet = originalPlayerFleet
        )
    }
}