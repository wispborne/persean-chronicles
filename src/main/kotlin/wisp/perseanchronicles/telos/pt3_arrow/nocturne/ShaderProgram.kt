package wisp.perseanchronicles.telos.pt3_arrow.nocturne

import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL20

class ShaderProgram() {
    var programID: Int
    var vertexShaderID = 0
    var fragmentShaderID = 0

    init {
        programID = GL20.glCreateProgram()
    }

    fun createVertexShader(shaderCode: String?): ShaderProgram {
        // Create the shader and set the source
        vertexShaderID = GL20.glCreateShader(GL20.GL_VERTEX_SHADER)
        GL20.glShaderSource(vertexShaderID, shaderCode)

        // Compile the shader
        GL20.glCompileShader(vertexShaderID)

        // Check for errors
        if (GL20.glGetShaderi(vertexShaderID, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) throw RuntimeException(
            "Error creating vertex shader\n"
                    + GL20.glGetShaderInfoLog(
                vertexShaderID,
                GL20.glGetShaderi(vertexShaderID, GL20.GL_INFO_LOG_LENGTH)
            )
        )

        // Attach the shader
        GL20.glAttachShader(programID, vertexShaderID)
        return this
    }

    fun createFragmentShader(shaderCode: String?): ShaderProgram {
        // Create the shader and set the source
        fragmentShaderID = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER)
        GL20.glShaderSource(fragmentShaderID, shaderCode)

        // Compile the shader
        GL20.glCompileShader(fragmentShaderID)

        // Check for errors
        if (GL20.glGetShaderi(fragmentShaderID, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) throw RuntimeException(
            ("Error creating fragment shader\n"
                    + GL20.glGetShaderInfoLog(
                fragmentShaderID,
                GL20.glGetShaderi(fragmentShaderID, GL20.GL_INFO_LOG_LENGTH)
            ))
        )

        // Attach the shader
        GL20.glAttachShader(programID, fragmentShaderID)
        return this
    }

    fun link(): ShaderProgram {
        GL20.glLinkProgram(programID)
        if (GL20.glGetProgrami(programID, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            throw RuntimeException("Unable to link shader program: " + GL20.glGetProgramInfoLog(programID, 1024))
        }
        return this
    }

    fun bind() {
        GL20.glUseProgram(programID)
    }

    fun unbind() {
        GL20.glUseProgram(0)
    }

    fun dispose() {
        // Unbind the program
        unbind()

        // Detach the shaders
        GL20.glDetachShader(programID, vertexShaderID)
        GL20.glDetachShader(programID, fragmentShaderID)

        // Delete the shaders
        GL20.glDeleteShader(vertexShaderID)
        GL20.glDeleteShader(fragmentShaderID)

        // Delete the program
        GL20.glDeleteProgram(programID)
    }
}