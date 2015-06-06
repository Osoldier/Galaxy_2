package me.soldier.galaxy.renderer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;

import java.nio.*;
import java.util.*;

import me.soldier.galaxy.core.*;
import me.soldier.galaxy.elements.*;

import org.lwjgl.*;
import org.lwjgl.opengl.*;

//TODO Ne pas recréer les floatBuffer ?
public class Renderer {

	int m_colNum = 200;
	double m_t0 = 1000;
	double m_t1 = 10000;

	Galaxy galaxy;

	// OGL
	Shader main;
	int vertexBuffer;
	int colorBuffer;
	int pointSizeBuffer;
	Texture particleTexture;
	ProjectionMatrix pr_matrix;

	int vbo;

	public Renderer(Galaxy pGalaxy) {
		this.galaxy = pGalaxy;
		initGL();
		initVBO();
		main = new Shader("main.vert", "main.frag");
		main.setUniformMat4f("pr_matrix", pr_matrix);
	}
	
	private void initGL() {
		GLContext glc = GLContext.createFromCurrent();
		if(!glc.getCapabilities().OpenGL20) {
			System.out.println("Error");
			return;
		}
		glEnable(GL_TEXTURE_2D);
		glEnable(GL_BLEND);
		glEnable(GL_VERTEX_PROGRAM_POINT_SIZE);
		glEnable(GL_POINT_SPRITE);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE);
		glClearColor(0, 0, 0.03f, 1);

		particleTexture = new Texture("/particle.png");
		float ffr = (float) (galaxy.GetFarFieldRad());
		pr_matrix = new ProjectionMatrix(-ffr, ffr, -ffr, ffr, -1, 10);
	}


	public void RenderScene() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		main.setUniform("us2dTexture", particleTexture.getId());
		glActiveTexture(GL_TEXTURE0+particleTexture.getId());
		particleTexture.bind();

		main.useShader();

		RenderStars();
		RenderDust();
		RenderH2();
	}
	
	private void initVBO() {
		vertexBuffer = createVBOID();
		colorBuffer = createVBOID();
		pointSizeBuffer = createVBOID();
	}

	private void RenderSprites(float[] vertices, float[] colors, float[] sizes) {
		glEnableVertexAttribArray(main.getAttributeLocation("av3VertexPosition"));
		vertexBufferData(vertexBuffer, createFloatBuffer(vertices));
		glVertexAttribPointer(main.getAttributeLocation("av3VertexPosition"), 3, GL_FLOAT, false, 0, 0);

		glEnableVertexAttribArray(main.getAttributeLocation("av3VertexColor"));
		vertexBufferData(colorBuffer, createFloatBuffer(colors));
		glVertexAttribPointer(main.getAttributeLocation("av3VertexColor"), 3, GL_FLOAT, false, 0, 0);

		glEnableVertexAttribArray(main.getAttributeLocation("afPointSize"));
		vertexBufferData(pointSizeBuffer, createFloatBuffer(sizes));
		glVertexAttribPointer(main.getAttributeLocation("afPointSize"), 1, GL_FLOAT, false, 0, 0);

		glDrawArrays(GL_POINTS, 0, vertices.length / 3);
	}

	private void RenderStars() {
		ArrayList<Star> stars = galaxy.GetStars();
		int num = galaxy.GetNumStars();
		float size = 4;
		float[] sizes = new float[num];
		float[] vertices = new float[3 * num];
		float[] colors = new float[3 * num];
		for (int i = 0; i < num; i++) {
			Star star = stars.get(i);
			vertices[i * 3 + 0] = star.m_pos.x;
			vertices[i * 3 + 1] = star.m_pos.y;
			vertices[i * 3 + 2] = 0;

			sizes[i] = size;

			double mag = star.m_mag;
			Color color = colorFromTemperature(star.m_temp);
			colors[i * 3 + 0] = (float) (color.r * mag);
			colors[i * 3 + 1] = (float) (color.g * mag);
			colors[i * 3 + 2] = (float) (color.b * mag);
		}

		RenderSprites(vertices, colors, sizes);
	}

	private void RenderDust() {
		ArrayList<Star> dusts = galaxy.GetDust();
		int num = galaxy.GetNumDust();

		float[] sizes = new float[num];
		float[] vertices = new float[3 * num];
		float[] colors = new float[3 * num];
		for (int i = 0; i < num; i++) {
			Star dust = dusts.get(i);
			vertices[i * 3 + 0] = dust.m_pos.x;
			vertices[i * 3 + 1] = dust.m_pos.y;
			vertices[i * 3 + 2] = 0;

			sizes[i] = 64.0f;

			double mag = dust.m_mag;
			Color color = colorFromTemperature(dust.m_temp);
			colors[i * 3 + 0] = (float) (color.r * mag);
			colors[i * 3 + 1] = (float) (color.g * mag);
			colors[i * 3 + 2] = (float) (color.b * mag);
		}

		RenderSprites(vertices, colors, sizes);
	}

	private void RenderH2() {
		ArrayList<Star> stars = galaxy.GetH2();
		int num = galaxy.GetNumH2();
		float size = 64.0f;

		ArrayList<Float> sizes = new ArrayList<Float>();
		ArrayList<Float> vertices = new ArrayList<Float>();
		ArrayList<Float> colors = new ArrayList<Float>();
		for (int i = 0; i < num; i++) {
			int k1 = 2 * i;
			int k2 = 2 * i + 1;

			Vector2f p1 = stars.get(k1).m_pos;
			Vector2f p2 = stars.get(k2).m_pos;

			double dst = Math.sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y));
			size = (float) (((1000 - dst) / 10) - 50);
			if (size < 1)
				continue;

			Star star = stars.get(i);
			vertices.add(star.m_pos.x);
			vertices.add(star.m_pos.y);
			vertices.add(0.0f);

			sizes.add(2 * size);

			double mag = star.m_mag;
			Color col3f = colorFromTemperature(stars.get(k1).m_temp);
			colors.add((float) (col3f.r * mag * 2));
			colors.add((float) (col3f.g * mag));
			colors.add((float) (col3f.b * mag));

			vertices.add(p1.x);
			vertices.add(p1.y);
			vertices.add(0.0f);
			sizes.add(size / 6);
			colors.add(1.0f);
			colors.add(1.0f);
			colors.add(1.0f);
		}

		RenderSprites(toFloatArray(vertices), toFloatArray(colors), toFloatArray(sizes));
	}

	private float[] toFloatArray(ArrayList<Float> e) {
		float[] floatArray = new float[e.size()];
		int i = 0;

		for (Float f : e) {
			floatArray[i++] = (f != null ? f : Float.NaN);
		}

		return floatArray;
	}

	private int createVBOID() {
		IntBuffer buffer = BufferUtils.createIntBuffer(1);
		glGenBuffers(buffer);
		return buffer.get(0);
	}

	private void vertexBufferData(int id, FloatBuffer data) {
		glBindBuffer(GL_ARRAY_BUFFER, id);
		glBufferData(GL_ARRAY_BUFFER, data, GL_DYNAMIC_DRAW);
	}
	
	public static FloatBuffer createFloatBuffer(float[] array) {
		FloatBuffer result = ByteBuffer.allocateDirect(array.length << 2).order(ByteOrder.nativeOrder()).asFloatBuffer();
		result.put(array).flip();
		return result;
	}
	
	private Color colorFromTemperature(double temp) {
		int idx = (int) Math.floor((temp - m_t0) / (m_t1-m_t0) * m_colNum);
		idx = Math.min(m_colNum-1, idx);
		idx = Math.max(0, idx);
		return StarColor.ColorsByTemp[idx];
	}

}
