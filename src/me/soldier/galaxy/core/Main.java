package me.soldier.galaxy.core;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

import java.nio.*;

import me.soldier.galaxy.elements.*;
import me.soldier.galaxy.renderer.*;

import org.lwjgl.glfw.*;

public class Main implements Runnable {

	//BASE
	public static int width = 1600;
	public static int height = 900;

	private Thread thread;
	private boolean running = false;

	private long window;
	// FIX Callback ClosureError;
	private GLFWKeyCallback keyCallback;
	
	//SPE
	private Galaxy galaxy = null;
	private int timeStepSize = 500000;
	private Renderer glRenderer;
	
	//BASE
	public void start() {
		running = true;
		thread = new Thread(this, "Game");
		thread.start();
	}

	private void init() {
		if (glfwInit() != GL_TRUE) {
			System.err.println("Could not initialize GLFW!");
			return;
		}

		glfwWindowHint(GLFW_RESIZABLE, GL_TRUE);
		glfwSwapInterval(0);
		window = glfwCreateWindow(width, height, "GalaxIbanez", NULL, NULL);
		if (window == NULL) {
			System.err.println("Could not create GLFW window!");
			return;
		}

		ByteBuffer vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
		glfwSetWindowPos(window, (GLFWvidmode.width(vidmode) - width) / 2, (GLFWvidmode.height(vidmode) - height) / 2);

		glfwSetKeyCallback(window, keyCallback = new Input());

		glfwMakeContextCurrent(window);
		glfwShowWindow(window);
		galaxy = new Galaxy();
		galaxy.SingleTimeStep(1);
		glRenderer = new Renderer(galaxy);
		System.out.println("OpenGL: " + glGetString(GL_VERSION));
	}

	public void run() {
		init();

		long lastTime = System.nanoTime();
		double delta = 0.0;
		double ns = 1000000000.0 / 60.0;
		long timer = System.currentTimeMillis();
		int updates = 0;
		int frames = 0;
		while (running) {
			long now = System.nanoTime();
			delta += (now - lastTime) / ns;
			lastTime = now;
			if (delta >= 1.0) {
				update();
				updates++;
				delta--;
			}
			render();
			frames++;
			if (System.currentTimeMillis() - timer > 1000) {
				timer += 1000;
				System.out.println(updates + " ups, " + frames + " fps");
				updates = 0;
				frames = 0;
			}
			if (glfwWindowShouldClose(window) == GL_TRUE)
				running = false;
		}
		keyCallback.release();
		glfwDestroyWindow(window);
		glfwTerminate();
	}
	
	private void render() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		int error = glGetError();
		if (error != GL_NO_ERROR)
			System.out.println("Error " + error);
		glRenderer.RenderScene();
		glfwSwapBuffers(window);
	}
	private void update() {
		glfwPollEvents();
		galaxy.SingleTimeStep(timeStepSize); 
	}

	

	public static void main(String[] args) {
		new Main().start();
	}
}
