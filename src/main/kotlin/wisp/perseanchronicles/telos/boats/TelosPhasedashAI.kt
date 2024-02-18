package wisp.perseanchronicles.telos.boats

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.impl.campaign.ids.Personalities
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.combat.AIUtils
import org.lwjgl.util.vector.Vector2f
import wisp.perseanchronicles.common.StarficzAIUtils
import wisp.perseanchronicles.game
import wisp.perseanchronicles.telos.TelosCommon.SYSTEM_PHASE_DASH_ID
import java.awt.Color
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

class TelosPhasedashAI : ShipSystemAIScript {
    var ship: ShipAPI? = null
    var regenSystem: Boolean = false

    // from ninmah
    var enemyTracker: IntervalUtil = IntervalUtil(0.8f, 1f)
    var damageTracker: IntervalUtil = IntervalUtil(0.2f, 0.3f)

    var target: ShipAPI? = null
    var nearbyEnemies: MutableMap<ShipAPI, Map<String, Float>> = mutableMapOf()
    var targetRange: Float = 0f
    var ventingHardFlux: Boolean = false
    var ventingSoftFlux: Boolean = false
    var lastUpdatedTime: Float = 0f
    var incomingProjectiles: List<StarficzAIUtils.FutureHit> = mutableListOf()
    var predictedWeaponHits: List<StarficzAIUtils.FutureHit> = mutableListOf()
    var combinedHits: MutableList<StarficzAIUtils.FutureHit> = mutableListOf()

    override fun init(ship: ShipAPI, system: ShipSystemAPI, flags: ShipwideAIFlags, engine: CombatEngineAPI) {
        this.ship = ship
    }

    override fun advance(amount: Float, missileDangerDir: Vector2f?, collisionDangerDir: Vector2f?, target: ShipAPI?) {
        ninmahAiScriptMagic(amount)
    }

    // from knights of ludd ninmah ai, gpl3
    private fun ninmahAiScriptMagic(amount: Float) {
        val ship = ship ?: return
        val engine = Global.getCombatEngine() ?: return
        if (!ship.isAlive || ship.parentStation != null || !engine.isEntityInPlay(ship)) return

        // Vanilla bug, isUIAutopilotOn returns the opposite of what it says.
        if (engine.playerShip == this.ship && engine.isUIAutopilotOn) {
            return
        }

        val phaseSystemCommand = ShipCommand.USE_SYSTEM // Normally this is ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK
        val phaseDashSystem = game.settings.getShipSystemSpec(SYSTEM_PHASE_DASH_ID)
        val phaseDashLength = phaseDashSystem.active
        val phaseDashCooldown = phaseDashSystem.getCooldown(ship.mutableStats)
        val phaseDashTimeElapsed = engine.getTotalElapsedTime(false) - (TelosPhaseDashSystem.dashStartTracker[ship.id] ?: 0f)

        if (ship.captain?.personalityAPI?.id != Personalities.AGGRESSIVE) {
            ship.captain?.setPersonality(Personalities.AGGRESSIVE)
        }

        // Calculate Decision Flags
        enemyTracker.advance(amount)
        if (enemyTracker.intervalElapsed() || target == null || !target!!.isAlive) {
            getOptimalTarget()
            if (target == null || !target!!.isAlive) return
        }

        damageTracker.advance(amount)

        if (damageTracker.intervalElapsed()) {
            lastUpdatedTime = Global.getCombatEngine().getTotalElapsedTime(false)
            incomingProjectiles = StarficzAIUtils.incomingProjectileHits(this.ship, ship.location)
            val timeToPredict = (phaseDashLength + damageTracker.maxInterval).coerceAtMost(3f)
            predictedWeaponHits = StarficzAIUtils.generatePredictedWeaponHits(this.ship, ship.location, timeToPredict)
            combinedHits = mutableListOf()
            combinedHits.addAll(incomingProjectiles)
            combinedHits.addAll(predictedWeaponHits)
        }

        // calculate how much damage the ship would take if unphased/vent/used system
        val currentTime = Global.getCombatEngine().getTotalElapsedTime(false)
        val timeElapsed = currentTime - lastUpdatedTime
        val bufferTime = 0.2f // 0.2 sec of buffer time before getting hit
        val armorBase = StarficzAIUtils.getCurrentArmorRating(this.ship)
        val armorMax = ship.armorGrid.armorRating
        val armorMinLevel = ship.mutableStats.minArmorFraction.modifiedValue
        var armorUnphase = armorBase
        var armorSystem = armorBase
        var armorVent = armorBase
        val phaseTime = if (ship.isPhased) (ship.system.cooldownRemaining) else 0f

        var hullDamageIfUnphased = 0f
        var empDamageIfUnphased = 0f

        var hullDamageIfSystem = 0f
        var empDamageIfSystem = 0f

        var hullDamageIfVent = 0f
        var empDamageIfVent = 0f

        for (hit in combinedHits) {
            val timeToHit = (hit.timeToHit - timeElapsed)
            if (timeToHit < -0.1f) continue  // skip hits that have already happened

            if (timeToHit < phaseTime + bufferTime) {
                val trueDamage = StarficzAIUtils.damageAfterArmor(
                    hit.damageType, hit.damage, hit.hitStrength, armorUnphase,
                    this.ship
                )
                hullDamageIfUnphased += trueDamage.two
                empDamageIfUnphased += hit.empDamage
                armorUnphase = max((armorUnphase - trueDamage.one), (armorMinLevel * armorMax))
            }
            if (timeToHit < phaseTime + bufferTime && hit.enemyId != target!!.id) {
                val trueDamage = StarficzAIUtils.damageAfterArmor(
                    hit.damageType, hit.damage, hit.hitStrength, armorSystem,
                    this.ship
                )
                hullDamageIfSystem += trueDamage.two
                empDamageIfSystem += hit.empDamage
                armorSystem = max((armorSystem - trueDamage.one), (armorMinLevel * armorMax))
            }
            if (timeToHit < ship.fluxTracker.timeToVent + bufferTime) {
                val trueDamage = StarficzAIUtils.damageAfterArmor(hit.damageType, hit.damage, hit.hitStrength, armorVent, this.ship)
                hullDamageIfVent += trueDamage.two
                empDamageIfVent += hit.empDamage
                armorVent = max((armorVent - trueDamage.one), (armorMinLevel * armorMax))
            }
        }


        val armorDamageLevel = (armorBase - armorUnphase) / armorMax
        val hullDamageLevel = hullDamageIfUnphased / (ship.hitpoints * ship.hullLevel)
        val armorDamageLevelSystem = (armorBase - armorSystem) / armorMax
        val hullDamageLevelSystem = hullDamageIfSystem / (ship.hitpoints * ship.hullLevel)
        val armorDamageLevelVent = (armorBase - armorVent) / armorMax
        val hullDamageLevelVent = hullDamageIfVent / (ship.hitpoints * ship.hullLevel)

        val mountHP = ship.allWeapons.sumOf { it.currHealth.toDouble() }.toFloat()
        val empDamageLevel = empDamageIfUnphased / mountHP
        val empDamageLevelSystem = empDamageIfSystem / mountHP
        val empDamageLevelVent = empDamageIfVent / mountHP

        var test = Color.blue

        if (StarficzAIUtils.DEBUG_ENABLED) {
            test = if ((armorDamageLevel > 0.03f || hullDamageLevel > 0.03f || empDamageLevel > 0.3f)) Color.green else test
            test = if ((armorDamageLevel > 0.05f || hullDamageLevel > 0.05f || empDamageLevel > 0.5f)) Color.yellow else test
            test = if ((armorDamageLevel > 0.07f || hullDamageLevel > 0.07f || empDamageLevel > 0.7f)) Color.red else test
            engine.addSmoothParticle(ship.location, ship.velocity, 200f, 100f, 0.1f, test)
        }


        // Set decision flags
        val totalFlux = ship.currFlux
        val hardFlux = ship.fluxTracker.hardFlux
        val maxFlux = ship.maxFlux
        val softFluxLevel = (totalFlux - hardFlux) / (maxFlux - hardFlux)

        // Phase Decision Tree starts here:
        var wantToPhase = false
        if (MathUtils.getDistance(ship.location, target!!.location) < targetRange + Misc.getTargetingRadius(ship.location, target, false)) {
            // if ship is in weapon range decide to phase based on incoming damage, accounting for the reduction in damage if the system overloads an enemy
            if (AIUtils.canUseSystemThisFrame(this.ship) || ship.system.isActive || target!!.fluxTracker.isOverloadedOrVenting) {
                if ((armorDamageLevelSystem > 0.07f || hullDamageLevelSystem > 0.07f || empDamageLevelSystem > 0.7f))
                    wantToPhase = true
                else ship.useSystem()
            } else {
                if ((armorDamageLevel > 0.07f || hullDamageLevel > 0.07f || empDamageLevel > 0.7f)) wantToPhase = true
            }

            // if the ship is not in immense danger of damage, but needs to reload/vent soft flux, also phase. (unless the enemy is overloaded, to maximize dps in that window)
            val maximiseDPS = target!!.fluxTracker.isOverloaded || ship.system.isActive
            if ((ventingSoftFlux) && !maximiseDPS) wantToPhase = true

            // phase to avoid getting nuked by enemy ship explosion
            if (target!!.hullLevel < 0.15f && MathUtils.getDistanceSquared(
                    ship.location,
                    target!!.location
                ) < (target!!.shipExplosionRadius + ship.collisionRadius).pow(2.0f)
            )
                wantToPhase = true
        } else { // if the ship is not in range, the acceptable damage threshold is much lower,
            if ((armorDamageLevel > 0.03f || hullDamageLevel > 0.03f || empDamageLevel > 0.3f) || ventingSoftFlux || ship.engineController.isFlamedOut)
                wantToPhase = true
            if (ship.isPhased && ship.hardFluxLevel < 0.1f)
                wantToPhase = true
            if (!ship.isPhased && ship.hardFluxLevel < 0.01f)
                wantToPhase = true
        }

        // execute commands
        // phase control
        if (ship.isPhased xor wantToPhase)
            ship.giveCommand(phaseSystemCommand, null, 0)
        else ship.blockCommandForOneFrame(phaseSystemCommand)


        // vent control
//        if (ventingHardFlux && armorDamageLevelVent < 0.03f && hullDamageLevelVent < 0.03f && empDamageLevelVent < 0.5f) {
//            ship.giveCommand(ShipCommand.VENT_FLUX, null, 0)
//        } else {
//            ship.blockCommandForOneFrame(ShipCommand.VENT_FLUX)
//        }

        // movement control
//        val fleetManager = engine.getFleetManager(ship.owner)
//        val taskManager = if ((fleetManager != null)) fleetManager.getTaskManager(ship.isAlly) else null
//        val assignmentInfo = if ((taskManager != null)) taskManager.getAssignmentFor(this.ship) else null
//        val assignmentType = if ((assignmentInfo != null)) assignmentInfo.type else null

//        if (shipTargetPoint != null && (assignmentType == CombatAssignmentType.SEARCH_AND_DESTROY || assignmentType == null)) {
//            val shipStrafePoint = MathUtils.getPointOnCircumference(ship.location, ship.collisionRadius, shipStrafeAngle)
//            StarficzAIUtils.strafeToPoint(this.ship, shipStrafePoint)
//            //turnToPoint(target.getLocation());
//            ship.shipTarget = target
//        }
    }

    private fun findMinShipWeaponRange(ship: ShipAPI): Float {
        // update ranges and block firing if system is active
        var minRange = Float.POSITIVE_INFINITY

        for (weapon in ship.allWeapons) {
            if (!weapon.isDecorative && !weapon.hasAIHint(WeaponAPI.AIHints.PD) && weapon.type != WeaponAPI.WeaponType.MISSILE) {
                val currentRange = weapon.range
                minRange = min(currentRange.toDouble(), minRange.toDouble()).toFloat()
                if (ship.system.isChargeup) {
                    weapon.setForceNoFireOneFrame(true)
                }
            }
        }

        return minRange
    }


    private fun getOptimalTarget() {
        // Cache any newly detected enemies, getShipStats is expensive
        val foundEnemies = AIUtils.getNearbyEnemies(ship, 3000f)
        for (foundEnemy in foundEnemies) {
            if (!nearbyEnemies.containsKey(foundEnemy) && foundEnemy.isAlive && !foundEnemy.isFighter) {
                val shipStats = StarficzAIUtils.getShipStats(foundEnemy, targetRange)
                nearbyEnemies[foundEnemy] = shipStats
            }
        }

        val deadEnemies: MutableSet<ShipAPI> = HashSet()
        for (enemy in nearbyEnemies.keys) {
            if (!enemy.isAlive) deadEnemies.add(enemy)
            if (!MathUtils.isWithinRange(enemy, ship, 3500f)) deadEnemies.add(enemy)
        }
        nearbyEnemies.keys.removeAll(deadEnemies)

        // Calculate ship strafe locations
        if (nearbyEnemies.isNotEmpty()) {
            if (ventingHardFlux) {
                if (target == null || !target!!.isAlive)
                    target = AIUtils.getNearestEnemy(ship)
//                shipTargetPoint = StarficzAIUtils.getBackingOffStrafePoint(ship)
            } else {
                val targetReturn = StarficzAIUtils.getLowestDangerTargetInRange(ship, nearbyEnemies, 120f, targetRange, true)
                val targetAttackPoint = targetReturn.one
                target = targetReturn.two

                if (target == null || !target!!.isAlive)
                    target = AIUtils.getNearestEnemy(ship)
            }
        }
    }

    private fun scaleValueBetweenRanges(minOut: Float, maxOut: Float, minIn: Float, maxIn: Float, input: Float): Float {
        if (input > maxIn) return maxOut
        if (input < minIn) return minOut
        return minOut + (input - minIn) * (maxOut - minOut) / (maxIn - minIn)
    }
}
