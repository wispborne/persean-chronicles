//package wisp.perseanchronicles.telos.boats
//
//import com.fs.starfarer.api.combat.BaseHullMod
//import com.fs.starfarer.api.combat.MutableShipStatsAPI
//import com.fs.starfarer.api.combat.ShipAPI.HullSize
//
//class DamagedHullMod : BaseHullMod() {
//    private val maxCrPenalty = 0.8f
//
//    override fun applyEffectsBeforeShipCreation(hullSize: HullSize?, stats: MutableShipStatsAPI, id: String?) {
//        if (isInPlayerFleet(stats)) {
//            stats.maxCombatReadiness.modifyFlat(id, -maxCrPenalty, "Ship damaged ")
//        }
//    }
//}