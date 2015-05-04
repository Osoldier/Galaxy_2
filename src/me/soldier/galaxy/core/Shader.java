package me.soldier.galaxy.core;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL32.*;

import java.io.*;

import me.soldier.galaxy.renderer.*;

/**
 * @author Osoldier
 * @Project OSEngine
 * @since 7 oct. 2014
 */
public final class Shader {

	private int program;
	private boolean used = false;

	public Shader(String pVertexShader, String pFragmentShader) {
		this(pVertexShader, pFragmentShader, null);
	}
	
	public Shader(String pVertexShader, String pFragmentShader, String pGeometricShader) {
		try {
			program = glCreateProgram();

			int vertexShader = createShader(pVertexShader, GL_VERTEX_SHADER);
			int fragmentShader = createShader(pFragmentShader, GL_FRAGMENT_SHADER);
			if(pGeometricShader != null) {
				int geometricShader = createShader(pGeometricShader, GL_GEOMETRY_SHADER);
				glAttachShader(program, geometricShader);
			}

			glAttachShader(program, vertexShader);
			glAttachShader(program, fragmentShader);

			glLinkProgram(program);
			glValidateProgram(program);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void useShader() {
		if (!used)
			glUseProgram(program);
		used = true;
	}

	public void releaseShader() {
		used = false;
		glUseProgram(0);
	}

	public static void releaseShaders() {
		glUseProgram(0);
	}
	
	public int getAttributeLocation(String pName) {
		useShader();
		return glGetAttribLocation(program, pName);
	}

	public void setUniform(String pName, float value) {
		useShader();
		glUniform1f(glGetUniformLocation(program, pName), value);
		releaseShader();
	}

	public void setUniform(String pName, Vector3f value) {
		useShader();
		glUniform3f(glGetUniformLocation(program, pName), value.x, value.y, value.z);
		releaseShader();
	}

	public void setUniform(String pName, int value) {
		useShader();
		glUniform1i(glGetUniformLocation(program, pName), value);
		releaseShader();
	}

	public void setUniformMat4f(String pName, Matrix4f matrix) {
		useShader();
		glUniformMatrix4(glGetUniformLocation(program, pName), false, matrix.toFloatBuffer());
		releaseShader();
	}
	
	public void setUniformColor(String pName, Color color) {
		useShader();
		glUniform4f(glGetUniformLocation(program, pName), color.r, color.g, color.b, color.a);
		releaseShader();
	}

	private int createShader(String filename, int shaderType) throws Exception {
		int shader = 0;
		try {
			shader = glCreateShader(shaderType);

			glShaderSource(shader, readFileAsString(filename));
			glCompileShader(shader);

			System.out.println(getLogInfo(shader));

			if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE)
				throw new RuntimeException("Error creating shader: " + getLogInfo(shader));

			return shader;
		} catch (Exception exc) {
			glDeleteShader(shader);
			throw exc;
		}
	}

	private static String getLogInfo(int obj) {
		return glGetShaderInfoLog(obj, 2048);
	}

	private String readFileAsString(String filename) throws Exception {
		StringBuilder source = new StringBuilder();

		FileInputStream in = new FileInputStream(filename);

		Exception exception = null;

		BufferedReader reader;
		try {
			reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));

			Exception innerExc = null;
			try {
				String line;
				while ((line = reader.readLine()) != null)
					source.append(line).append('\n');
			} catch (Exception exc) {
				exception = exc;
			} finally {
				try {
					reader.close();
				} catch (Exception exc) {
					if (innerExc == null)
						innerExc = exc;
					else
						exc.printStackTrace();
				}
			}

			if (innerExc != null)
				throw innerExc;
		} catch (Exception exc) {
			exception = exc;
		} finally {
			try {
				in.close();
			} catch (Exception exc) {
				if (exception == null)
					exception = exc;
				else
					exc.printStackTrace();
			}

			if (exception != null)
				throw exception;
		}

		return source.toString();
	}

}
