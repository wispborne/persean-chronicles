package wisp.perseanchronicles.telos.pt3_arrow.nocturne

import com.fs.starfarer.api.graphics.SpriteAPI
import wisp.perseanchronicles.game

data class NebulaSprite(
    val index: Int = (0..15).random(),
    val spriteFile: String = "graphics/telos/ethersight/perseanchronicles_ethersight_nebula_${index}.png",
) {
    val sprite: SpriteAPI by lazy {
        game.settings.loadTexture(spriteFile)
        game.settings.getSprite(spriteFile)
    }
}