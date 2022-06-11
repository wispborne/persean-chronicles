package wisp.perseanchronicles.telos.pt1_deliveryToEarth

import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.mission.FleetSide
import org.lazywizard.console.BaseCommand
import org.lwjgl.util.vector.Vector2f
import wisp.perseanchronicles.game

class TestBattleCommand : BaseCommand {
    override fun runCommand(args: String, context: BaseCommand.CommandContext): BaseCommand.CommandResult {
        if (!context.isInCampaign) {
            return BaseCommand.CommandResult.WRONG_CONTEXT
        }

        game.sector.registerPlugin(Telos1Battle.CampaignPlugin())
        game.sector.campaignUI.startBattle(Telos1Battle.Context())

        game.combatEngine.addPlugin(object : BaseEveryFrameCombatPlugin() {
            override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {
                if (game.combatEngine.getTotalElapsedTime(false) > 5) {
                    Telos1Battle.createTriTachFleet().fleetData.membersListCopy.forEach {
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