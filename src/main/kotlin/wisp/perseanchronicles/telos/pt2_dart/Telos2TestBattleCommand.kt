package wisp.perseanchronicles.telos.pt2_dart

import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.mission.FleetSide
import org.lazywizard.console.BaseCommand
import org.lwjgl.util.vector.Vector2f
import wisp.perseanchronicles.game

class Telos2TestBattleCommand : BaseCommand {
    override fun runCommand(args: String, context: BaseCommand.CommandContext): BaseCommand.CommandResult {
        if (!context.isInCampaign) {
            return BaseCommand.CommandResult.WRONG_CONTEXT
        }

        game.sector.registerPlugin(Telos2Battle.CampaignPlugin())
        game.sector.campaignUI.startBattle(Telos2Battle.Context())

        game.combatEngine.addPlugin(object : BaseEveryFrameCombatPlugin() {
            override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {
                if (game.combatEngine.getTotalElapsedTime(false) > 5) {
                    Telos2Battle.createTriTachFleet().fleetData.membersListCopy.forEach {
                        game.combatEngine.getFleetManager(FleetSide.ENEMY)
                            .spawnFleetMember(it, Vector2f(game.combatEngine.mapWidth / 2f, game.combatEngine.mapHeight / 2f), 0f, 3f)
                    }
                    game.combatEngine.removePlugin(this)
                }
            }
        })
        return BaseCommand.CommandResult.SUCCESS
    }
}