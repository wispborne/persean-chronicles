package org.wisp.stories.wispLib

import com.fs.starfarer.api.FactoryAPI
import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.SettingsAPI
import com.fs.starfarer.api.campaign.SectorAPI
import com.fs.starfarer.api.campaign.comm.IntelManagerAPI
import com.fs.starfarer.api.combat.CombatEngineAPI

class Di {
    val sector: SectorAPI
        get() = Global.getSector()

    val memory: Memory
        get() = Memory(sector.memoryWithoutUpdate)

    val intelManager: IntelManagerAPI
        get() = sector.intelManager

    val persistentData: PersistentDataWrapper
        get() = PersistentDataWrapper

    val settings: SettingsAPI
        get() = Global.getSettings()

    val logger: DebugLogger
        get() = Global.getLogger(Di::class.java)

    val combatEngine: CombatEngineAPI
        get() = Global.getCombatEngine()

    val currentState: GameState
        get() = Global.getCurrentState()

    val factory: FactoryAPI
        get() = Global.getFactory()

    val errorReporter: CrashReporter =
        CrashReporter(modName = "Stories", modAuthor = "Wisp (aka Wispborne)", di = this)
}

/**
 * Singleton instance of the service locator. Set a new one of these for unit tests.
 */
var di: Di = Di()