package graphicsSupport;

import graphicsSupport.camera.Camera;
import graphicsSupport.shaders.UniformedShaderProgram;

public interface ExternalRenderer {
	public void render(Camera cam);

	public UniformedShaderProgram getShader();

	public default RenderPriority getRenderPriority() {
		return RenderPriority.normal;
	}

	public default boolean hasShader() {
		return true;
	}

	public default boolean needLightSetup() {
		return true;
	}

	public default void init() {
	}

	public default void end() {

	}
	
	public default boolean mustBeRendered() {
		return true;
	}

	public enum RenderPriority {
		hight, normal
	}
}