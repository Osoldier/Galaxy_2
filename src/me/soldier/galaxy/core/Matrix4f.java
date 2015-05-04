package me.soldier.galaxy.core;

import java.nio.*;

public class Matrix4f {

	public static final int SIZE = 4 * 4;
	/** [column + line * 4] */
	public float[] elements = new float[SIZE];

	public Matrix4f() {

	}

	public void Identity() {
		for (int i = 0; i < SIZE; i++) {
			this.elements[i] = 0.0f;
		}
		this.elements[0 + 0 * 4] = 1.0f;
		this.elements[1 + 1 * 4] = 1.0f;
		this.elements[2 + 2 * 4] = 1.0f;
		this.elements[3 + 3 * 4] = 1.0f;
	}

	protected void orthographic(float left, float right, float bottom, float top, float near, float far) {
		this.Identity();

		this.elements[0 + 0 * 4] = 2.0f / (right - left);

		this.elements[1 + 1 * 4] = 2.0f / (top - bottom);

		this.elements[2 + 2 * 4] = 2.0f / (near - far);

		this.elements[0 + 3 * 4] = (left + right) / (left - right);
		this.elements[1 + 3 * 4] = (bottom + top) / (bottom - top);
		this.elements[2 + 3 * 4] = (far + near) / (far - near);

	}

	protected void perspective(float fov, float aspect, float near, float far) {
		this.Identity();

		fov = (float) Math.toRadians(fov);

		float f = (float) (1.0f / Math.tan(fov));

		float frustrum_length = near - far;

		this.elements[0 + 0 * 4] = f / aspect;

		this.elements[1 + 1 * 4] = f;

		this.elements[2 + 2 * 4] = ((far + near) / frustrum_length);

		this.elements[2 + 3 * 4] = -1;

		this.elements[3 + 2 * 4] = ((2 * near * far) / frustrum_length);

		this.elements[3 + 3 * 4] = 0;
	}

	protected void translate(Vector3f vector) {
		this.elements[3 + 0 * 4] = vector.x;
		this.elements[3 + 1 * 4] = vector.y;
		this.elements[3 + 2 * 4] = vector.z;
	}

	protected Matrix4f scale(Vector3f vector) {
		Matrix4f result = new Matrix4f();
		result.Identity();
		result.elements[0 + 0 * 4] = vector.x;
		result.elements[1 + 1 * 4] = vector.y;
		result.elements[2 + 2 * 4] = vector.z;
		return result;
	}

	protected Matrix4f rotate(float angle, float x, float y, float z) {
		Matrix4f result = new Matrix4f();
		result.Identity();
		float r = (float) Math.toRadians(angle);
		float cos = (float) Math.cos(r);
		float sin = (float) Math.sin(r);
		float omc = 1.0f - cos;

		result.elements[0 + 0 * 4] = x * omc + cos;
		result.elements[1 + 0 * 4] = x * y * omc - z * sin;
		result.elements[2 + 0 * 4] = x * z * omc + y * sin;

		result.elements[0 + 1 * 4] = y * x * omc + z * sin;
		result.elements[1 + 1 * 4] = y * omc + cos;
		result.elements[2 + 1 * 4] = y * z * omc - x * sin;

		result.elements[0 + 2 * 4] = x * z * omc - y * sin;
		result.elements[1 + 2 * 4] = y * z * omc + x * sin;
		result.elements[2 + 2 * 4] = z * omc + cos;
		
		return result;
	}

	protected void multiply(Matrix4f matrix) {
		Matrix4f result = new Matrix4f();
		result.Identity();
		for (int y = 0; y < 4; y++) {
			for (int x = 0; x < 4; x++) {
				float sum = 0.0f;
				for (int e = 0; e < 4; e++) {
					sum += this.elements[x + e * 4] * matrix.elements[e + y * 4];
				}
				result.elements[x + y * 4] = sum;
			}
		}
		this.elements = result.elements;
	}

	@Override
	public String toString() {
		String str = "";
		str += "|" + this.elements[0 + 0 * 4] + "\t" + elements[1 + 0 * 4] + "\t" + elements[2 + 0 * 4] + "\t" + elements[3 + 0 * 4] + "|\n";
		str += "|" + this.elements[0 + 1 * 4] + "\t" + elements[1 + 1 * 4] + "\t" + elements[2 + 1 * 4] + "\t" + elements[3 + 1 * 4] + "|\n";
		str += "|" + this.elements[0 + 2 * 4] + "\t" + elements[1 + 2 * 4] + "\t" + elements[2 + 2 * 4] + "\t" + elements[3 + 2 * 4] + "|\n";
		str += "|" + this.elements[0 + 3 * 4] + "\t" + elements[1 + 3 * 4] + "\t" + elements[2 + 3 * 4] + "\t" + elements[3 + 3 * 4] + "|\n";
		return str;
	}

	public FloatBuffer toFloatBuffer() {
		return createFloatBuffer(elements);
	}

	private static FloatBuffer createFloatBuffer(float[] array) {
		FloatBuffer res = ByteBuffer.allocateDirect(array.length << 2).order(ByteOrder.nativeOrder()).asFloatBuffer();
		res.put(array).flip();
		return res;
	}

}
