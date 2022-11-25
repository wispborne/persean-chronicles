package wisp.perseanchronicles.telos.boats

import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.util.IntervalTracker
import org.lazywizard.lazylib.ext.getFacing
import org.lazywizard.lazylib.ext.rotate

class ReconsiderInertiaHullmod : BaseHullMod() {
    private val intervalTimer = IntervalTracker(.001f, .001f)

    override fun advanceInCombat(ship: ShipAPI, amount: Float) {
        intervalTimer.advance(amount)
        if (!intervalTimer.intervalElapsed()) return
        if (ship.facing == 0f) return // because the velocity does something weird then
//        if (ship.ad <= 0) return

        val effectAmount = .15f

        // https://stackoverflow.com/a/7869457/1622788
        val math = ((ship.facing - ship.velocity.getFacing()) + 180) % 360 - 180

        ship.velocity.rotate(math * effectAmount)
    }
}