package wisp.perseanchronicles.telos.pt3_arrow.nocturne;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ViewportAPI;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;

import java.io.IOException;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

/**
 * This class is not actually used in favour of instanced rendering, was used for shader testing
 */
public class NocturneShaderRenderer {
    private int vao;
    private final ShaderProgram program;

    public NocturneShaderRenderer(String vert, String frag) {
        this.program = new ShaderProgram();
        try {
            vert = Global.getSettings().loadText(vert);
            frag = Global.getSettings().loadText(frag);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        program.createVertexShader(vert);
        program.createFragmentShader(frag);
        program.link();

        //configure vao and vbos
        float[] vertices = new float[] {
                0f, 1f,
                1f, 0f,
                0f, 0f,

                0f, 1f,
                1f, 1f,
                1f, 0f,
        };

        FloatBuffer verticesBuffer = BufferUtils.createFloatBuffer(vertices.length);
        verticesBuffer.put(vertices).flip();

        // Create the VAO and bind to it
        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        // Create the VBO and bind to it
        int vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW);

        glEnableVertexAttribArray(0);

        int size = 2;
        glVertexAttribPointer(0, size, GL_FLOAT, false, size * Float.SIZE / Byte.SIZE, 0);



        glBindVertexArray(0);
    }

    public void render(ViewportAPI viewport, Matrix4f modelView) {
        glBindVertexArray(vao);

        program.bind();

        FloatBuffer projectionBuffer = BufferUtils.createFloatBuffer(16);
        Matrix4f projection = orthogonal(viewport.getVisibleWidth() / viewport.getViewMult(), viewport.getVisibleHeight() / viewport.getViewMult());

        projection = Matrix4f.mul(projection, modelView, null);

        projection.store(projectionBuffer);
        projectionBuffer.flip();

        int loc = glGetUniformLocation(program.getProgramID(), "projection");
        glUniformMatrix4(loc, false, projectionBuffer);

        /*//color vector
        FloatBuffer colorBuffer = BufferUtils.createFloatBuffer(4);
        color.store(colorBuffer);
        colorBuffer.flip();
        glUniform4(glGetUniformLocation(program.getProgramID(), "modColor"), colorBuffer);

        //time float
        FloatBuffer timeBuffer = BufferUtils.createFloatBuffer(1);
        timeBuffer.put(time);
        timeBuffer.flip();
        glUniform1(glGetUniformLocation(program.getProgramID(), "iTime"), timeBuffer);*/

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE);

        glDrawArrays(GL_TRIANGLES, 0, 6);

        program.unbind();
        glBindVertexArray(0);

        GL11.glDisable(GL11.GL_BLEND);
    }

    private Matrix4f orthogonal(float right, float top) {
        Matrix4f matrix = new Matrix4f();

        float left = 0f;
        float bottom = 0f;
        float zNear = -100f;
        float zFar = 100f;

        matrix.m00 = 2f / (right - left);

        matrix.m11 = 2f / (top - bottom);
        matrix.m22 = 2f / (zNear - zFar);

        matrix.m30 = -(right + left) / (right - left);
        matrix.m31 = -(top + bottom) / (top - bottom);
        matrix.m32 = -(zFar + zNear) / (zFar - zNear);

        matrix.m33 = 1f;

        return matrix;
    }

    public void dispose() {
        program.dispose();
    }
}