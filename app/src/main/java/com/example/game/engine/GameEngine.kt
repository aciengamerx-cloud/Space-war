package com.example.game.engine

import androidx.compose.ui.graphics.Color
import com.example.game.audio.SoundManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class PlayerState(
    val pos: Vector3 = Vector3(0f, 0f, 0f),
    val pitch: Float = 0f,    // Pitch angle (radians) - pitch loop up/down
    val yaw: Float = 0f,      // Yaw angle (radians) - horizontal turn
    val roll: Float = 0f,     // Roll angle (radians) - banking roll
    val pitchVel: Float = 0f,
    val yawVel: Float = 0f,
    val rollVel: Float = 0f,
    val shield: Float = 100f,
    val maxShield: Float = 100f,
    val hull: Float = 100f,
    val maxHull: Float = 100f,
    val energy: Float = 100f,
    val maxEnergy: Float = 100f,
    val velocity: Float = 35f,
    val heat: Float = 0f,
    val isOverheated: Boolean = false,
    val missileCount: Int = 12,
    val maxMissiles: Int = 12,
    val weaponPower: Float = 0.35f,
    val shieldPower: Float = 0.35f,
    val enginePower: Float = 0.30f,
    val shipId: String = "starstreak_mk1",
    val primaryColor: Color = Color(0xFF00E5FF),
    val accentColor: Color = Color(0xFFFF3D00)
) {
    fun getForwardVector(): Vector3 {
        val fx = sin(yaw) * cos(pitch)
        val fy = sin(pitch)
        val fz = cos(yaw) * cos(pitch)
        return Vector3(fx, fy, fz).normalized()
    }

    fun getUpVector(): Vector3 {
        val ux = -sin(yaw) * sin(pitch)
        val uy = cos(pitch)
        val uz = -cos(yaw) * sin(pitch)
        return Vector3(ux, uy, uz).normalized()
    }
}

data class AsteroidEntity(
    val id: String = UUID.randomUUID().toString(),
    var pos: Vector3,
    val velocity: Vector3,
    var rotX: Float,
    var rotY: Float,
    var rotZ: Float,
    val rotSpeed: Vector3,
    val radius: Float,
    val mesh: AsteroidMesh,
    var health: Float
)

data class EnemyEntity(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: String, // "drone", "interceptor", "stealth", "boss"
    var pos: Vector3,
    var velocity: Vector3,
    var rotX: Float = 0f,
    var rotY: Float = 0f,
    var health: Float,
    val maxHealth: Float,
    var shield: Float,
    val maxShield: Float,
    var fireTimer: Float = 0f,
    val mesh: ShipMesh,
    val isBoss: Boolean = false
)

data class ProjectileEntity(
    val id: String = UUID.randomUUID().toString(),
    val isPlayer: Boolean,
    var pos: Vector3,
    val velocity: Vector3,
    val color: Color,
    val damage: Float,
    var lifeSpan: Float,
    val isMissile: Boolean = false,
    var targetId: String? = null
)

data class ParticleEntity(
    var pos: Vector3,
    val velocity: Vector3,
    val color: Color,
    val size: Float,
    var lifeSpan: Float,
    val maxLifeSpan: Float
)

data class PlanetState(
    val name: String = "Ares Primus",
    val pos: Vector3 = Vector3(400f, 150f, 1200f),
    val radius: Float = 380f,
    val primaryColor: Color = Color(0xFFD84315),
    val atmosphereColor: Color = Color(0xFFFFAB91),
    val ringRadius: Float = 0f
)

enum class GameStateMode {
    MENU,
    PLAYING,
    SECTOR_CLEAR,
    GAME_OVER
}

data class SectorConfig(
    val id: Int,
    val name: String,
    val planetName: String,
    val planetColor: Color,
    val planetAtmosphere: Color,
    val targetKillCount: Int,
    val asteroidDensity: Int,
    val enemyTypes: List<String>,
    val hasBoss: Boolean = false
)

class GameEngine(
    private val soundManager: SoundManager? = null
) {
    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private val _gameStateMode = MutableStateFlow(GameStateMode.MENU)
    val gameStateMode: StateFlow<GameStateMode> = _gameStateMode.asStateFlow()

    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score.asStateFlow()

    private val _currentSector = MutableStateFlow(1)
    val currentSector: StateFlow<Int> = _currentSector.asStateFlow()

    private val _kills = MutableStateFlow(0)
    val kills: StateFlow<Int> = _kills.asStateFlow()

    private val _asteroidsDestroyed = MutableStateFlow(0)
    val asteroidsDestroyed: StateFlow<Int> = _asteroidsDestroyed.asStateFlow()

    private val _shotsFired = MutableStateFlow(0)
    val shotsFired: StateFlow<Int> = _shotsFired.asStateFlow()

    private val _shotsHit = MutableStateFlow(0)
    val shotsHit: StateFlow<Int> = _shotsHit.asStateFlow()

    private val _targetEnemy = MutableStateFlow<EnemyEntity?>(null)
    val targetEnemy: StateFlow<EnemyEntity?> = _targetEnemy.asStateFlow()

    // Control inputs
    private var targetPitchVel = 0f
    private var targetYawVel = 0f
    private var targetRollVel = 0f

    // World Entities
    val asteroids = mutableListOf<AsteroidEntity>()
    val enemies = mutableListOf<EnemyEntity>()
    val projectiles = mutableListOf<ProjectileEntity>()
    val particles = mutableListOf<ParticleEntity>()
    val starfield = mutableListOf<Vector3>()
    var currentPlanet = PlanetState()

    private var sectorConfig = getSectorConfig(1)
    private var isFiringPrimary = false
    private var fireCooldown = 0f

    init {
        initStarfield()
    }

    private fun initStarfield() {
        starfield.clear()
        val rand = Random(42)
        for (i in 0..220) {
            starfield.add(
                Vector3(
                    (rand.nextFloat() - 0.5f) * 1600f,
                    (rand.nextFloat() - 0.5f) * 1600f,
                    (rand.nextFloat() - 0.5f) * 1600f
                )
            )
        }
    }

    fun startNewGame(shipId: String, primaryHex: String, sector: Int = 1) {
        val color = try {
            Color(android.graphics.Color.parseColor(primaryHex))
        } catch (e: Exception) {
            Color(0xFF00E5FF)
        }

        _playerState.value = PlayerState(
            shipId = shipId,
            primaryColor = color,
            pos = Vector3(0f, 0f, 0f)
        )

        _score.value = 0
        _kills.value = 0
        _asteroidsDestroyed.value = 0
        _shotsFired.value = 0
        _shotsHit.value = 0
        _currentSector.value = sector

        targetPitchVel = 0f
        targetYawVel = 0f
        targetRollVel = 0f

        loadSector(sector)
        _gameStateMode.value = GameStateMode.PLAYING
    }

    fun loadNextSector() {
        val next = _currentSector.value + 1
        _currentSector.value = next
        loadSector(next)
        _gameStateMode.value = GameStateMode.PLAYING
    }

    private fun loadSector(sectorId: Int) {
        sectorConfig = getSectorConfig(sectorId)
        asteroids.clear()
        enemies.clear()
        projectiles.clear()
        particles.clear()

        currentPlanet = PlanetState(
            name = sectorConfig.planetName,
            primaryColor = sectorConfig.planetColor,
            atmosphereColor = sectorConfig.planetAtmosphere,
            pos = Vector3(400f, 150f, 1200f),
            radius = 380f
        )

        val rand = Random(System.currentTimeMillis())
        for (i in 0 until sectorConfig.asteroidDensity) {
            val radius = 15f + rand.nextFloat() * 35f
            val seed = rand.nextLong()
            val mesh = AsteroidGenerator.create3DAsteroid(radius, seed)
            asteroids.add(
                AsteroidEntity(
                    pos = Vector3(
                        (rand.nextFloat() - 0.5f) * 1000f,
                        (rand.nextFloat() - 0.5f) * 800f,
                        rand.nextFloat() * 1200f + 200f
                    ),
                    velocity = Vector3(
                        (rand.nextFloat() - 0.5f) * 10f,
                        (rand.nextFloat() - 0.5f) * 10f,
                        -15f - rand.nextFloat() * 20f
                    ),
                    rotX = rand.nextFloat() * 360f,
                    rotY = rand.nextFloat() * 360f,
                    rotZ = rand.nextFloat() * 360f,
                    rotSpeed = Vector3(
                        rand.nextFloat() * 2f - 1f,
                        rand.nextFloat() * 2f - 1f,
                        rand.nextFloat() * 2f - 1f
                    ),
                    radius = radius,
                    mesh = mesh,
                    health = radius * 3f
                )
            )
        }

        spawnEnemyWave()
    }

    private fun spawnEnemyWave() {
        val rand = Random(System.currentTimeMillis())
        val count = 3 + sectorConfig.id
        for (i in 0 until count) {
            val type = sectorConfig.enemyTypes.random()
            val isBoss = sectorConfig.hasBoss && i == 0 && enemies.none { it.isBoss }

            val enemyMesh = if (isBoss) {
                StarshipModels.createBossDreadnought()
            } else if (type == "drone") {
                StarshipModels.createEnemyDrone()
            } else {
                StarshipModels.createPlayerShip("starstreak_mk1", Color(0xFFFF1744), Color.Yellow)
            }

            val maxH = if (isBoss) 800f else if (type == "interceptor") 120f else 60f
            val maxS = if (isBoss) 400f else if (type == "interceptor") 60f else 20f

            enemies.add(
                EnemyEntity(
                    name = if (isBoss) "Imperial Battlecruiser Boss" else if (type == "interceptor") "Enemy Interceptor" else "Enemy Drone",
                    type = type,
                    pos = Vector3(
                        (rand.nextFloat() - 0.5f) * 600f,
                        (rand.nextFloat() - 0.5f) * 400f,
                        rand.nextFloat() * 800f + 300f
                    ),
                    velocity = Vector3(
                        (rand.nextFloat() - 0.5f) * 15f,
                        (rand.nextFloat() - 0.5f) * 15f,
                        -10f
                    ),
                    health = maxH,
                    maxHealth = maxH,
                    shield = maxS,
                    maxShield = maxS,
                    mesh = enemyMesh,
                    isBoss = isBoss
                )
            )
        }
    }

    private val _flightSensitivity = MutableStateFlow(0.20f) // 20% default (buttery smooth controls)
    val flightSensitivity: StateFlow<Float> = _flightSensitivity.asStateFlow()

    fun setFlightSensitivity(value: Float) {
        _flightSensitivity.value = value.coerceIn(0.05f, 1.0f)
    }

    // CONTINUOUS 3D STEERING / SCROLL CONTROL (Unreal Engine 3D Flight Loops)
    fun onDragSteer(deltaX: Float, deltaY: Float) {
        if (_gameStateMode.value != GameStateMode.PLAYING) return

        val mult = _flightSensitivity.value
        val pitchSensitivity = 0.012f * mult
        val yawSensitivity = 0.009f * mult
        val rollSensitivity = 0.015f * mult

        val clampedY = if (kotlin.math.abs(deltaY) < 0.8f) 0f else deltaY
        val clampedX = if (kotlin.math.abs(deltaX) < 0.8f) 0f else deltaX

        targetPitchVel = -clampedY * pitchSensitivity
        targetYawVel = clampedX * yawSensitivity
        targetRollVel = -clampedX * rollSensitivity
    }

    fun setPrimaryFiring(firing: Boolean) {
        isFiringPrimary = firing
    }

    fun fireHomingMissile() {
        val player = _playerState.value
        if (player.missileCount <= 0 || _gameStateMode.value != GameStateMode.PLAYING) return

        _playerState.value = player.copy(missileCount = player.missileCount - 1)
        soundManager?.playMissileSound()

        val forward = player.getForwardVector()
        val target = _targetEnemy.value

        projectiles.add(
            ProjectileEntity(
                isPlayer = true,
                pos = player.pos + forward * 10f,
                velocity = forward * 240f,
                color = Color(0xFFFF9100),
                damage = 120f,
                lifeSpan = 4f,
                isMissile = true,
                targetId = target?.id
            )
        )
    }

    fun setPowerAllocation(weap: Float, shld: Float, eng: Float) {
        val player = _playerState.value
        _playerState.value = player.copy(
            weaponPower = weap,
            shieldPower = shld,
            enginePower = eng,
            velocity = 25f + eng * 30f
        )
    }

    fun updateGameLoop(deltaTime: Float) {
        if (_gameStateMode.value != GameStateMode.PLAYING) return

        val p = _playerState.value

        // 1. Continuous Angular Motion Updates (Pitch Loop, Yaw Turn, Roll Bank)
        val pitchVel = p.pitchVel + (targetPitchVel - p.pitchVel) * 12f * deltaTime
        val yawVel = p.yawVel + (targetYawVel - p.yawVel) * 12f * deltaTime
        val rollVel = p.rollVel + (targetRollVel - p.rollVel) * 12f * deltaTime

        val newPitch = p.pitch + pitchVel * deltaTime
        val newYaw = p.yaw + yawVel * deltaTime
        // Auto-center roll gently when not turning hard
        val newRoll = (p.roll + rollVel * deltaTime) * (1f - 2f * deltaTime)

        // Smoothly decay input targets
        targetPitchVel *= (1f - 6f * deltaTime).coerceAtLeast(0f)
        targetYawVel *= (1f - 6f * deltaTime).coerceAtLeast(0f)
        targetRollVel *= (1f - 6f * deltaTime).coerceAtLeast(0f)

        // 2. Compute Forward Trajectory Vector & Motion
        val tempPlayer = p.copy(pitch = newPitch, yaw = newYaw, roll = newRoll)
        val forwardVec = tempPlayer.getForwardVector()
        val flightSpeed = p.velocity * 8f * (1f + p.enginePower * 0.5f)
        val newPos = p.pos + forwardVec * (flightSpeed * deltaTime)

        // 3. Weapon Heat & Fire Logic
        var newHeat = p.heat
        var isOverheated = p.isOverheated

        val heatCoolRate = 25f + (p.weaponPower * 40f)
        newHeat = (newHeat - heatCoolRate * deltaTime).coerceAtLeast(0f)
        if (newHeat <= 10f) isOverheated = false

        fireCooldown -= deltaTime
        if (isFiringPrimary && fireCooldown <= 0f && !isOverheated) {
            _shotsFired.value = _shotsFired.value + 1
            fireCooldown = 0.12f - (p.weaponPower * 0.05f)
            newHeat += 12f
            if (newHeat >= 100f) isOverheated = true

            soundManager?.playLaserSound()

            val upVec = tempPlayer.getUpVector()
            val rightVec = forwardVec.cross(upVec).normalized()

            val leftCannonPos = newPos + forwardVec * 8f - rightVec * 10f
            val rightCannonPos = newPos + forwardVec * 8f + rightVec * 10f

            projectiles.add(
                ProjectileEntity(
                    isPlayer = true,
                    pos = leftCannonPos,
                    velocity = forwardVec * 380f,
                    color = p.primaryColor,
                    damage = 25f + (p.weaponPower * 20f),
                    lifeSpan = 2.5f
                )
            )
            projectiles.add(
                ProjectileEntity(
                    isPlayer = true,
                    pos = rightCannonPos,
                    velocity = forwardVec * 380f,
                    color = p.primaryColor,
                    damage = 25f + (p.weaponPower * 20f),
                    lifeSpan = 2.5f
                )
            )
        }

        // 4. Shield & Energy Regeneration
        var newShield = p.shield
        var newEnergy = p.energy

        val shieldRegen = 2f + (p.shieldPower * 12f)
        newShield = (newShield + shieldRegen * deltaTime).coerceAtMost(p.maxShield)

        val energyRegen = 5f + (p.enginePower * 15f)
        newEnergy = (newEnergy + energyRegen * deltaTime).coerceAtMost(p.maxEnergy)

        _playerState.value = tempPlayer.copy(
            pos = newPos,
            pitchVel = pitchVel,
            yawVel = yawVel,
            rollVel = rollVel,
            heat = newHeat,
            isOverheated = isOverheated,
            shield = newShield,
            energy = newEnergy
        )

        // 5. Update Target Lock
        updateTargetLock()

        // 6. Update Projectiles
        val projIter = projectiles.iterator()
        while (projIter.hasNext()) {
            val proj = projIter.next()
            proj.lifeSpan -= deltaTime
            if (proj.lifeSpan <= 0f) {
                projIter.remove()
                continue
            }

            if (proj.isMissile && proj.targetId != null) {
                val target = enemies.find { it.id == proj.targetId }
                if (target != null) {
                    val dir = (target.pos - proj.pos).normalized()
                    proj.pos = proj.pos + dir * 260f * deltaTime
                } else {
                    proj.pos = proj.pos + proj.velocity * deltaTime
                }
            } else {
                proj.pos = proj.pos + proj.velocity * deltaTime
            }

            // Spawn missile smoke trail
            if (proj.isMissile && Random.nextFloat() < 0.6f) {
                particles.add(
                    ParticleEntity(
                        pos = proj.pos,
                        velocity = Vector3((Random.nextFloat() - 0.5f) * 10f, (Random.nextFloat() - 0.5f) * 10f, -20f),
                        color = Color.LightGray.copy(alpha = 0.5f),
                        size = 6f,
                        lifeSpan = 0.4f,
                        maxLifeSpan = 0.4f
                    )
                )
            }

            // Hit Detection (Player Projectiles vs Enemies & Asteroids)
            if (proj.isPlayer) {
                var hit = false
                for (enemy in enemies) {
                    if (proj.pos.distanceTo(enemy.pos) < if (enemy.isBoss) 60f else 25f) {
                        hit = true
                        _shotsHit.value = _shotsHit.value + 1

                        if (enemy.shield > 0f) {
                            enemy.shield = (enemy.shield - proj.damage).coerceAtLeast(0f)
                            soundManager?.playShieldHitSound()
                        } else {
                            enemy.health -= proj.damage
                        }

                        spawnExplosionParticles(proj.pos, Color(0xFFFF5252), 8)

                        if (enemy.health <= 0f) {
                            soundManager?.playExplosionSound()
                            spawnExplosionParticles(enemy.pos, Color.Yellow, 25)
                            _kills.value = _kills.value + 1
                            _score.value = _score.value + if (enemy.isBoss) 2500 else 250
                        }
                        break
                    }
                }

                if (!hit) {
                    for (ast in asteroids) {
                        if (proj.pos.distanceTo(ast.pos) < ast.radius) {
                            hit = true
                            _shotsHit.value = _shotsHit.value + 1
                            ast.health -= proj.damage
                            spawnExplosionParticles(proj.pos, Color.Gray, 6)
                            if (ast.health <= 0f) {
                                soundManager?.playExplosionSound()
                                spawnExplosionParticles(ast.pos, Color(0xFFFFAB40), 18)
                                _asteroidsDestroyed.value = _asteroidsDestroyed.value + 1
                                _score.value = _score.value + 100
                            }
                            break
                        }
                    }
                }

                if (hit) projIter.remove()
            } else {
                if (proj.pos.distanceTo(newPos) < 22f) {
                    projIter.remove()
                    applyDamageToPlayer(proj.damage)
                }
            }
        }

        // Clean up dead enemies & asteroids
        enemies.removeAll { it.health <= 0f }
        asteroids.removeAll { it.health <= 0f }

        if (enemies.isEmpty()) {
            if (_kills.value >= sectorConfig.targetKillCount) {
                _gameStateMode.value = GameStateMode.SECTOR_CLEAR
            } else {
                spawnEnemyWave()
            }
        }

        // 7. Update Enemy Logic & AI Attacks
        for (enemy in enemies) {
            val distToPlayer = enemy.pos.distanceTo(newPos)
            if (distToPlayer > 200f) {
                val dir = (newPos - enemy.pos).normalized()
                enemy.pos = enemy.pos + dir * (40f + sectorConfig.id * 10f) * deltaTime
            } else {
                enemy.pos = enemy.pos + Vector3(sin(System.currentTimeMillis() * 0.002f) * 20f, 0f, -15f) * deltaTime
            }

            enemy.fireTimer += deltaTime
            if (enemy.fireTimer >= (if (enemy.isBoss) 0.8f else 1.8f)) {
                enemy.fireTimer = 0f
                projectiles.add(
                    ProjectileEntity(
                        isPlayer = false,
                        pos = enemy.pos,
                        velocity = (newPos - enemy.pos).normalized() * 220f,
                        color = Color.Red,
                        damage = if (enemy.isBoss) 20f else 10f,
                        lifeSpan = 4f
                    )
                )
            }
        }

        // 8. Update Asteroid Physics & Collision
        for (ast in asteroids) {
            ast.rotX += ast.rotSpeed.x * deltaTime * 20f
            ast.rotY += ast.rotSpeed.y * deltaTime * 20f
            ast.pos = ast.pos + ast.velocity * deltaTime

            if (ast.pos.distanceTo(newPos) > 1500f) {
                ast.pos = newPos + forwardVec * 1000f + Vector3((Random.nextFloat() - 0.5f) * 500f, (Random.nextFloat() - 0.5f) * 500f, 0f)
            }

            if (ast.pos.distanceTo(newPos) < ast.radius + 14f) {
                applyDamageToPlayer(40f)
                spawnExplosionParticles(newPos, Color.Red, 15)
                ast.health = 0f
            }
        }

        // 9. Update Particles
        val partIter = particles.iterator()
        while (partIter.hasNext()) {
            val part = partIter.next()
            part.lifeSpan -= deltaTime
            if (part.lifeSpan <= 0f) {
                partIter.remove()
            } else {
                part.pos = part.pos + part.velocity * deltaTime
            }
        }
    }

    private fun applyDamageToPlayer(amount: Float) {
        val p = _playerState.value
        soundManager?.playShieldHitSound()

        var currentShield = p.shield
        var currentHull = p.hull

        if (currentShield > 0f) {
            currentShield -= amount
            if (currentShield < 0f) {
                currentHull += currentShield
                currentShield = 0f
            }
        } else {
            currentHull -= amount
        }

        if (currentHull <= 0f) {
            currentHull = 0f
            soundManager?.playExplosionSound()
            _gameStateMode.value = GameStateMode.GAME_OVER
        }

        _playerState.value = p.copy(
            shield = currentShield,
            hull = currentHull
        )
    }

    private fun updateTargetLock() {
        val p = _playerState.value
        val nearest = enemies.minByOrNull { it.pos.distanceTo(p.pos) }
        if (nearest != _targetEnemy.value) {
            if (nearest != null) soundManager?.playLockOnBeep()
            _targetEnemy.value = nearest
        }
    }

    private fun spawnExplosionParticles(pos: Vector3, color: Color, count: Int) {
        val rand = Random
        for (i in 0 until count) {
            val vel = Vector3(
                (rand.nextFloat() - 0.5f) * 120f,
                (rand.nextFloat() - 0.5f) * 120f,
                (rand.nextFloat() - 0.5f) * 120f
            )
            particles.add(
                ParticleEntity(
                    pos = pos,
                    velocity = vel,
                    color = color,
                    size = 4f + rand.nextFloat() * 8f,
                    lifeSpan = 0.6f + rand.nextFloat() * 0.4f,
                    maxLifeSpan = 1f
                )
            )
        }
    }

    fun getSectorConfig(sectorId: Int): SectorConfig {
        return when (sectorId) {
            1 -> SectorConfig(
                id = 1,
                name = "Sector 1: Orbit of Ares",
                planetName = "Ares Primus",
                planetColor = Color(0xFFD84315),
                planetAtmosphere = Color(0xFFFFAB91),
                targetKillCount = 8,
                asteroidDensity = 14,
                enemyTypes = listOf("drone", "interceptor")
            )
            2 -> SectorConfig(
                id = 2,
                name = "Sector 2: Saturn Ring Belt",
                planetName = "Cronus Titan",
                planetColor = Color(0xFFFFA000),
                planetAtmosphere = Color(0xFFFFE082),
                targetKillCount = 12,
                asteroidDensity = 22,
                enemyTypes = listOf("drone", "interceptor", "stealth")
            )
            3 -> SectorConfig(
                id = 3,
                name = "Sector 3: Deep Orion Nebula",
                planetName = "Orion Nexus",
                planetColor = Color(0xFF7B1FA2),
                planetAtmosphere = Color(0xFFE1BEE7),
                targetKillCount = 16,
                asteroidDensity = 16,
                enemyTypes = listOf("interceptor", "stealth")
            )
            else -> SectorConfig(
                id = 4,
                name = "Sector 4: Dreadnought Outpost",
                planetName = "Titan Stronghold",
                planetColor = Color(0xFF37474F),
                planetAtmosphere = Color(0xFF90A4AE),
                targetKillCount = 20,
                asteroidDensity = 18,
                enemyTypes = listOf("interceptor", "stealth"),
                hasBoss = true
            )
        }
    }
}
