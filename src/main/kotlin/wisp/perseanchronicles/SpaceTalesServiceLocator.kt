package wisp.perseanchronicles

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.SoundPlayerAPI
import wisp.questgiver.Questgiver
import wisp.questgiver.wispLib.CrashReporter
import wisp.questgiver.wispLib.ServiceLocator


/**
 * Singleton instance of the service locator. Set a new one of these for unit tests.
 */
var game: SpaceTalesServiceLocator = SpaceTalesServiceLocator(Questgiver.game)

class SpaceTalesServiceLocator(
    serviceLocator: ServiceLocator
) :
    ServiceLocator by serviceLocator {
    val errorReporter: CrashReporter =
        CrashReporter(modName = MOD_NAME, modAuthor = MOD_AUTHOR, game = this)

    inline val soundPlayer: SoundPlayerAPI
        get() = Global.getSoundPlayer()

//    val jsonText: IText = JsonText(modId = MOD_ID, jsonPaths = listOf("data/strings/telos.hjson"))
}