package wisp.perseanchronicles.commands

import com.fs.starfarer.api.campaign.OrbitAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator
import com.fs.starfarer.api.util.Misc
import org.lazywizard.console.BaseCommand
import org.lazywizard.console.Console
import wisp.perseanchronicles.game

class SpawnGate : BaseCommand {
    override fun runCommand(args: String, context: BaseCommand.CommandContext): BaseCommand.CommandResult {
        if (!context.isInCampaign || game.sector.playerFleet.isInHyperspace) {
            return BaseCommand.CommandResult.WRONG_CONTEXT
        }

        return if (spawnGateAtLocation(game.sector.playerFleet) != null) {
            Console.showMessage("Gate created!")
            BaseCommand.CommandResult.SUCCESS
        } else {
            BaseCommand.CommandResult.ERROR
        }
    }

    fun spawnGateAtLocation(location: SectorEntityToken?): SectorEntityToken? {
        if (location == null) {
            game.errorReporter.reportCrash(NullPointerException("Tried to spawn gate but target location was null!"))
            return null
        }

        val newGate = BaseThemeGenerator.addNonSalvageEntity(
            location.starSystem,
            BaseThemeGenerator.EntityLocation()
                .apply {
                    this.location = location.location
                    this.orbit = createOrbit(location)
                },
            "inactive_gate",
            Factions.DERELICT
        )

        return newGate.entity
    }

    fun createOrbit(
        targetLocation: SectorEntityToken,
        orbitCenter: SectorEntityToken = targetLocation.starSystem.center
    ): OrbitAPI {
        val orbitRadius = Misc.getDistance(targetLocation, orbitCenter)

        return game.factory.createCircularOrbit(
            orbitCenter,
            Misc.getAngleInDegrees(orbitCenter.location, targetLocation.location),
            orbitRadius,
            orbitRadius / (20f + StarSystemGenerator.random.nextFloat() * 5f) // taken from StarSystemGenerator:1655
        )
    }
}