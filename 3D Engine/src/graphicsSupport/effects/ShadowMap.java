/**
 * 
 */
package graphicsSupport.effects;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.glFramebufferTexture2D;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import frameBuffer.FrameBuffer;
import graphicsSupport.texture.Texture;

/**
 * @author timat
 *
 */
public class ShadowMap extends FrameBuffer {

	/**
	 * @param width
	 * @param height
	 * @throws Exception
	 */
	public ShadowMap(int width, int height) throws Exception {
		super(width, height, GL30.GL_DEPTH_ATTACHMENT, GL11.GL_DEPTH_COMPONENT);
	}

	@Override
	protected void postInit() {
		GL11.glDrawBuffer(GL11.GL_NONE);
		GL11.glReadBuffer(GL11.GL_NONE);
	}

	public final void setTexture(Texture texture) {
		glFramebufferTexture2D(GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, texture.getGLId(), 0);
	}

}
