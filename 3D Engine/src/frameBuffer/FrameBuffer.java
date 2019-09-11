package frameBuffer;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER_COMPLETE;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;
import static org.lwjgl.opengl.GL30.glCheckFramebufferStatus;
import static org.lwjgl.opengl.GL30.glDeleteFramebuffers;
import static org.lwjgl.opengl.GL30.glFramebufferTexture2D;
import static org.lwjgl.opengl.GL30.glGenFramebuffers;

import java.util.LinkedList;

import graphicsSupport.texture.Texture;

public class FrameBuffer {

	// All frame buffers
	private static final LinkedList<FrameBuffer> ALL_FRAME_BUFFERS = new LinkedList<FrameBuffer>();
	
	// Texture
	private final Texture texture;
	
	// Id
	private final int fbo;
	
	public FrameBuffer(int width, int height, int attachment, int texturePixelFormat) throws Exception {
		// Create a FBO to render the depth map
        fbo = glGenFramebuffers();
        
        texture = new Texture(width, height, texturePixelFormat);
        
        // Attach the the depth map texture to the FBO
        glBindFramebuffer(GL_FRAMEBUFFER, fbo);
        glFramebufferTexture2D(GL_FRAMEBUFFER, attachment, GL_TEXTURE_2D, texture.getGLId(), 0);
       	
        postInit();
        
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            throw new Exception("Could not create FrameBuffer");
        }

        // Unbind
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
	
        ALL_FRAME_BUFFERS.add(this);
	}
	
	protected void postInit() {
		
	}

	public final int getFbo() {
		return fbo;
	}

	public void cleanup() {
        glDeleteFramebuffers(fbo);
        ALL_FRAME_BUFFERS.remove(this);
    }

	public final Texture getTexture() {
		return texture;
	}
	
	public void bind() {
		glBindFramebuffer(GL_FRAMEBUFFER, fbo);
	}
	
	public void unbind() {
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
	}
	
	public static final void massCleanup() {
		while (!ALL_FRAME_BUFFERS.isEmpty()) {
			ALL_FRAME_BUFFERS.getFirst().cleanup();
		}
	}

	public final int getWidth() {
		return texture.getWidth();
	}

	public final int getHeight() {
		return texture.getHeight();
	}
	
}
