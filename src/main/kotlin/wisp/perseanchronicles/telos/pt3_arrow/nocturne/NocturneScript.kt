package wisp.perseanchronicles.telos.pt3_arrow.nocturne

import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.campaign.CampaignEngine
import org.lazywizard.console.Console
import org.lazywizard.lazylib.FastTrig
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.opengl.ColorUtils
import org.lazywizard.lazylib.opengl.DrawUtils
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL12
import org.lwjgl.opengl.GL13
import org.lwjgl.opengl.GL30
import wisp.perseanchronicles.common.SpriteBatch
import wisp.perseanchronicles.game
import java.awt.Color


class NocturneScript : EveryFrameScript {
    private val screenWidth = Display.getWidth() * Display.getPixelScaleFactor()
    private val screenHeight = Display.getHeight() * Display.getPixelScaleFactor()
    private var minimapWidth = 240f
    private var minimapHeight = 240f
    private val minimapX = (screenWidth - minimapWidth).toInt()
    private val minimapY = (screenHeight - minimapHeight).toInt()
    private val bgTextureId = 0//glGenTextures()

    private var isDone = false
    private var secsElapsed = 0f

    override fun isDone() = isDone

    override fun runWhilePaused() = true

    override fun advance(amount: Float) {
        if (!game.sector.isPaused) {
            secsElapsed += amount
        }
//        Global.getSector().viewport.alphaMult = 0.01f
        game.sector.viewport.alphaMult = 0.5f
        minimapWidth = 220f
        minimapHeight = 220f
//        renderMinimapBlur()


        kotlin.runCatching {
            val radius = Global.getSettings().getFloat("campaignRadarRadius")
            val ratio = screenWidth / screenHeight
            val viewport = game.sector.viewport

//            glEnable(GL_TEXTURE_2D)
            glPushAttrib(GL_ALL_ATTRIB_BITS)
            glViewport(0, 0, (screenWidth.toInt()).toInt(), (screenHeight * ratio).toInt())
            glMatrixMode(GL_PROJECTION)
            glPushMatrix()
            glLoadIdentity()
//            glOrtho(
//                viewport.llx.toDouble(),
//                (viewport.llx + viewport.visibleWidth).toDouble(),
//                viewport.lly.toDouble(),
//                (viewport.lly + viewport.visibleHeight).toDouble(),
//                -1.0,
//                1.0
//            )
            glOrtho(
                0.0,
                0.0,
                0.0,
                0.0,
                -1.0,
                1.0
            )
            glMatrixMode(GL_MODELVIEW)
            glPushMatrix()
            glLoadIdentity()
            glDisable(GL_TEXTURE_2D)
//            glEnable(GL_BLEND)
//            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
            glTranslatef(0.01f, 0.01f, 0f)

//            glEnable(GL_TEXTURE_2D)
            // todo multiply CampaignEngine.getInstance().getPlayerFaction().getDarkUIColor() by 0.33f
            var color = CampaignEngine.getInstance().playerFaction.darkUIColor
            val alpha = .33f
            color = Color(
                (color.red * alpha).coerceIn(0f, 255f).toInt(),
                (color.green * alpha).coerceIn(0f, 255f).toInt(),
                (color.blue * alpha).coerceIn(0f, 255f).toInt()
            )
            ColorUtils.glColor(color, 1f)
//            ColorUtils.glColor(Color(10, 31, 36, 1), 1f)
            DrawUtils.drawCircle(.821f, -0.856f, .1088f, 80, true)
//            drawFilledCircle(.0f, 0f, .11f)
//            glColor3f(10f, 31f, 36f)
//            drawFilledCircle(.821f, -0.856f, .11f)
//            glDisable(GL_TEXTURE_2D)

            // Clear OpenGL flags
//            glDisable(GL_BLEND);

            glMatrixMode(GL_MODELVIEW);
            glPopMatrix();
            glMatrixMode(GL_PROJECTION);
            glPopMatrix();
            glPopAttrib();


//            glColor4f(10f, 31f, 36f, 1f) // lower rgb to make dim
////            glEnd()
//            glDisable(GL_TEXTURE_2D)
        }
            .onFailure { game.logger.i(it) }

        if (secsElapsed > 10) {
            game.sector.viewport.alphaMult = 1f
//            glDeleteTextures(bgTextureId)
            game.logger.i { "Ending Nocturne effect." }
            isDone = true
        }
    }

    fun vanillaRadarRender() {
        // com.fs.starfarer.coreui.map.H.class
//        val var5: Float
//        val var6: Float
//        val var11 = this.getPosition()
//        var3 = var11.getWidth()
//        var4 = var11.getHeight()
//        var5 = var11.getX()
//        var6 = var11.getY()
//        val var7 = StarfarerSettings.Õ00000("systemMap", "radar_mask")
//        var7.color = Color.white
//        var7.setSize(var3, var4)
//        super(var7, var5 + var3 / 2.0f, var6 + var4 / 2.0f, 1)
//        var var8 = Color.black
//        Color(0, 50, 0, 255)
//        var8 = CampaignEngine.getInstance().playerFaction.darkUIColor
//        var8 = OoOO.Õ00000(var8, 0.33f)
//        var8 = OoOO.Ò00000(var8, 255)
//        oo0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO.Ó00000(
//            var5,
//            var6,
//            var3,
//            var4,
//            var8,
//            var1
//        )

    }

    /*
    * Function that handles the drawing of a circle using the triangle fan
    * method. This will create a filled circle.
    *
    * Params:
    *	x (GLFloat) - the x position of the center point of the circle
    *	y (GLFloat) - the y position of the center point of the circle
    *	radius (GLFloat) - the radius that the painted circle will have
    */
    fun drawFilledCircle(x: Float, y: Float, radius: Float) {
        val triangleAmount = 80 //# of triangles used to draw circle

        val twicePi: Float = 2.0f * kotlin.math.PI.toFloat()
        glBegin(GL_TRIANGLE_FAN)
//        glColor3f(10f, 31f, 36f) // lower rgb to make dim
        glVertex2f(x, y) // center of circle
        var i = 0
        while (i <= triangleAmount) {
//            glColor3f(10f, 31f, 36f) // lower rgb to make dim
            glVertex2f(
                x + radius * FastTrig.cos(i * twicePi / triangleAmount.toDouble()).toFloat(),
                y + radius * FastTrig.sin(i * twicePi / triangleAmount.toDouble()).toFloat()
            )
            i++
        }
        glEnd()
    }

    fun renderMinimapBlur() {
        kotlin.runCatching {
//            drawNoise(x = minimapX.toFloat(), y = minimapY.toFloat(), width = minimapWidth, height = minimapHeight)
            storeScreenTexture()
            startFlags()
            drawBackground()
            endFlags()
        }
            .onFailure { game.logger.w(it) }
    }

    private val noise = kotlin.run {
        game.settings.loadTexture("graphics/fx/noise.png")
        game.settings.getSprite("graphics/fx/noise.png")
    }
    private val noiseSprites = SpriteBatch(noise)

    fun drawNoise(x: Float, y: Float, width: Float, height: Float) {
        noise.setTexHeight(minimapHeight)
        noise.setTexWidth(minimapWidth)
        noiseSprites.clear()

        while (noiseSprites.size() < 3) {
            noiseSprites.add(1f, -1f, MathUtils.getRandomNumberInRange(0f, 360f), 1f, Color(150, 150, 150), 1f)
        }

        noiseSprites.finish()
        SpriteBatch.drawAll(noiseSprites)
//        noise.renderAtCenter(minimapX.toFloat(), minimapY.toFloat())
    }

    fun storeScreenTexture() {
        glGetError() // Clear existing error flag, if any
        val buffer = BufferUtils.createByteBuffer(screenWidth.toInt() * screenHeight.toInt() * 3)
        // Subtract 2 from the height to create a nightmare dimension.
        glReadPixels(
            minimapX,
            (1).toInt(),
            minimapWidth.toInt(),
            minimapHeight.toInt(),
            GL_RGB,
            GL_UNSIGNED_BYTE,
            buffer
        )
        GL13.glActiveTexture(GL13.GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, bgTextureId)
        glTexParameteri(GL_TEXTURE_2D, GL12.GL_TEXTURE_BASE_LEVEL, 0)
        glTexParameteri(GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, 0)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
        GL30.glGenerateMipmap(GL_TEXTURE_2D)
        glTexImage2D(
            GL_TEXTURE_2D,
            0,
            GL_R3_G3_B2,
            minimapWidth.toInt(),
            minimapHeight.toInt(),
            0,
            GL_RGB,
            GL_UNSIGNED_BYTE,
            buffer
        )

        // Fallback in case generating background fails: free memory and disable until manually re-enabled
        val err = glGetError();
        if (err != GL_NO_ERROR) {
            glDeleteTextures(bgTextureId)
//            settings.showBackground = false
            Console.showMessage("Failed to size buffer for background image! Disabling console background (can be re-enabled with Settings command)...")
//            Console.showMessage("Error id: " + getErrorString(err))
        }
    }

    fun startFlags() {
//        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT or GL_STENCIL_BUFFER_BIT)
//        glClearColor(0f, 0f, 0f, 1f)

        // Set up OpenGL flags
        glPushAttrib(GL_ALL_ATTRIB_BITS)
        glMatrixMode(GL_PROJECTION)
        glPushMatrix()
        glLoadIdentity()
        glViewport(0, 0, screenWidth.toInt(), screenHeight.toInt())
        glOrtho(0.0, screenWidth.toDouble(), 0.0, screenHeight.toDouble(), -1.0, 1.0)
        glMatrixMode(GL_MODELVIEW)
        glPushMatrix()
        glLoadIdentity()
        glTranslatef(0.01f, 0.01f, 0f)

//        glEnable(GL_TEXTURE_2D)
    }

    fun endFlags() {
        glDisable(GL_TEXTURE_2D)

        // Clear OpenGL flags
        glPopMatrix()
        glMatrixMode(GL_PROJECTION)
        glPopMatrix()
        glPopAttrib()
    }

    fun drawBackground() {
        glEnable(GL_BLEND)
        glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA)
        GL13.glActiveTexture(GL13.GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, bgTextureId)
        glPushMatrix()
        glBegin(GL_QUADS)
        val rgb = .5f
        glColor4f(10f, 31f, 36f, 1f) // lower rgb to make dim
        glTexCoord2f(0f, 0f)
        glVertex2f(minimapX.toFloat(), 0f)
        glTexCoord2f(1f, 0f)
        glVertex2f(screenWidth, 0f)
        glTexCoord2f(1f, 1f)
        glVertex2f(screenWidth, screenHeight - minimapY.toFloat())
        glTexCoord2f(0f, 1f)
        glVertex2f(minimapX.toFloat(), screenHeight - minimapY.toFloat())
        glEnd()
        glPopMatrix()
    }
}