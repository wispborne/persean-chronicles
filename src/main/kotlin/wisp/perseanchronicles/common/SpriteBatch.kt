package wisp.perseanchronicles.common

import com.fs.starfarer.api.graphics.SpriteAPI
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.roundToLong


/**
 * Used to efficiently render the same sprite many times. Each instance only
 * handles one unique sprite, and does not modify the underlying
 * [SpriteAPI]. All scaling is based on the state of the [SpriteAPI]
 * at the time of `SpriteBatch` instantiation.
 *
 * @author LazyWizard
 * @since 2.2
 * @see <a href="https://github.com/LazyWizard/combat-radar/blob/e540ec69fb522677d2576b0ab1a4e9785fb59238/src/org/lazywizard/radar/util/SpriteBatch.java">GitHub</a>
 */
// TODO: Rewrite to handle multiple SpriteAPIs
// TODO: Rewrite to use buffers (clone of DrawQueue?)
class SpriteBatch @JvmOverloads constructor(
    sprite: SpriteAPI,
    private val blendSrc: Int = GL11.GL_SRC_ALPHA,
    internal val blendDest: Int = GL11.GL_ONE_MINUS_SRC_ALPHA
) {
    private val textureId: Int = sprite.textureId
    private val textureWidth: Float = sprite.textureWidth
    private val textureHeight: Float = sprite.textureHeight
    private val offsetScaleX: Float = if (sprite.centerX > 0f) sprite.centerX / (sprite.width * .5f) else 1f
    private val offsetScaleY: Float = if (sprite.centerY > 0f) sprite.centerY / (sprite.height * .5f) else 1f
    private val hScale: Float = sprite.width / sprite.height
    private val toDraw: MutableList<DrawCall> = ArrayList()
    private var finished = false

    // Size is height of sprite, width is automatically calculated
    fun add(x: Float, y: Float, angle: Float, size: Float, color: Color, alphaMod: Float) =
        add(x, y, angle, size * hScale, size, color, alphaMod)

    fun add(x: Float, y: Float, angle: Float, width: Float, height: Float, color: Color, alphaMod: Float) {
        if (finished) {
            clear()
        }
        toDraw.add(DrawCall(x, y, angle, width, height, color, alphaMod))
        finished = false
    }

    fun size(): Int {
        return toDraw.size
    }

    fun clear() {
        toDraw.clear()
        finished = false
    }

    val isEmpty: Boolean
        get() = toDraw.isEmpty()

    // Does nothing for now, usage is enforced for later move to buffers
    fun finish() {
        if (finished) {
            throw RuntimeException("SpriteBatch is already finished!")
        }
        finished = true
    }

    @Deprecated("Call `SpriteBatch.drawAll` instead.")
    fun draw() {
        if (!finished) {
            throw RuntimeException("Must call finish() before drawing!")
        }
        if (toDraw.isEmpty()) {
            return
        }
        GL11.glBlendFunc(blendSrc, blendDest)
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId)
        for (call in toDraw) {
            GL11.glPushMatrix()
            if (DEBUG_MODE) {
                GL11.glDisable(GL11.GL_TEXTURE_2D)
                GL11.glPointSize(3f)
                GL11.glColor4f(1f, 1f, 1f, 1f)
                GL11.glBegin(GL11.GL_POINTS)
                GL11.glVertex2f(call.x, call.y)
                GL11.glEnd()
                GL11.glEnable(GL11.GL_TEXTURE_2D)
            }
            GL11.glTranslatef(call.x, call.y, 0f)
            GL11.glRotatef(call.angle, 0f, 0f, 1f)
            GL11.glTranslatef(-call.width * 0.5f * offsetScaleX, -call.height * 0.5f * offsetScaleY, 0f)
            GL11.glColor4ub(call.color[0], call.color[1], call.color[2], call.color[3])
            GL11.glBegin(GL11.GL_QUADS)
            GL11.glTexCoord2f(0f, 0f)
            GL11.glVertex2f(0f, 0f)
            GL11.glTexCoord2f(textureWidth, 0f)
            GL11.glVertex2f(call.width, 0f)
            GL11.glTexCoord2f(textureWidth, textureHeight)
            GL11.glVertex2f(call.width, call.height)
            GL11.glTexCoord2f(0f, textureHeight)
            GL11.glVertex2f(0f, call.height)
            GL11.glEnd()
            GL11.glPopMatrix()
        }
    }

    internal class DrawCall internal constructor(
        internal val x: Float,
        internal val y: Float,
        angle: Float,
        internal val width: Float,
        internal val height: Float,
        color: Color,
        alphaMod: Float
    ) {
        internal val angle: Float = angle - 90f
        internal val color: ByteArray = getColorBytes(color, alphaMod)

        companion object {
            internal fun getColorBytes(color: Color, alphaMod: Float): ByteArray {
                val value = color.rgb

                return byteArrayOf(
                    (value shr 16 and 0xFF).toByte(),
                    (value shr 8 and 0xFF).toByte(),
                    (value and 0xFF).toByte(),
                    ((value shr 24 and 0xFF.toFloat().toInt()).toDouble().roundToLong() * alphaMod).toInt().toByte()
                )
            }
        }
    }

    companion object {
        //internal static final Logger Log = Logger.getLogger(SpriteBatch.class);
        internal const val DEBUG_MODE = false

        fun drawAll(vararg spriteBatches: SpriteBatch) {
            GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS)
            GL11.glEnable(GL11.GL_TEXTURE_2D)
            GL11.glEnable(GL11.GL_BLEND)
            spriteBatches.forEach {
                it.draw()
            }
            GL11.glPopAttrib()
        }
    }
}