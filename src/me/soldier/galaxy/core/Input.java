package me.soldier.galaxy.core;

import org.lwjgl.glfw.*;

public class Input extends GLFWKeyCallback {

	public static boolean[] keys = new boolean[65536];

	public void invoke(long window, int key, int scancode, int action, int mods) {
		keys[key] = action != GLFW.GLFW_RELEASE; 
	}
	
	public static boolean isKeyDown(int keycode) {
		return keys[keycode];
	}
}
