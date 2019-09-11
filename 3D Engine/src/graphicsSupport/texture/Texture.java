package graphicsSupport.texture;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.GL_UNPACK_ALIGNMENT;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glDeleteTextures;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glPixelStorei;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.LinkedList;

import javax.imageio.ImageIO;

import org.joml.Vector2f;
import org.lwjgl.BufferUtils;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import engine.Engine;
import graphicsSupport.gui.VgGuiHandler;
import resources.Resource;

public class Texture implements Resource {

	public static final LinkedList<Texture> ALL_LOADED_TEXTURES = new LinkedList<Texture>();

	// Rows and columns
	protected int num_rows = 1;
	protected int num_cols = 1;

	// ID
	private final int gl_id;
	private final int vg_id;

	// Dimension
	private int width;
	private int height;

	public final String name;

	public Texture(File file) {
		name = file.getName().substring(0, file.getName().lastIndexOf("."));
		int gl_id = -1;
		try {
			BufferedImage image = ImageIO.read(file);
			width = image.getWidth();
			height = image.getHeight();

			width = image.getWidth();
			height = image.getHeight();

			int[] pixels = new int[image.getWidth() * image.getHeight()];
			image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());

			ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4); // 4
																					// for
																					// RGBA,
																					// 3
																					// for
																					// RGB

			for (int y = 0; y < image.getHeight(); y++) {
				for (int x = 0; x < image.getWidth(); x++) {
					int pixel = pixels[y * image.getWidth() + x];
					buffer.put((byte) ((pixel >> 16) & 0xFF)); // Red component
					buffer.put((byte) ((pixel >> 8) & 0xFF)); // Green component
					buffer.put((byte) (pixel & 0xFF)); // Blue component
					buffer.put((byte) ((pixel >> 24) & 0xFF)); // Alpha
																// component.
																// Only for RGBA
				}
			}
			buffer.flip();

			// Create a new OpenGL texture
			int textureId = GL11.glGenTextures();

			// Bind the texture
			glBindTexture(GL_TEXTURE_2D, textureId);

			// Tell OpenGL how to unpack the RGBA bytes. Each component is 1
			// byte size
			glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

			// Upload the texture data
			glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);

			// Generate Mip Map
			glGenerateMipmap(GL_TEXTURE_2D);

			gl_id = textureId;
		} catch (Exception e) {
			e.printStackTrace();
		}

		this.gl_id = gl_id;

		if (Engine.getEngineOptions().mipmapping()) {
			createMipmap();
		}

		vg_id = NanoVG.nvgCreateImage(VgGuiHandler.instance.getVg(), (file).getPath(),
				Engine.getEngineOptions().mipmapping() ? (NanoVG.NVG_IMAGE_GENERATE_MIPMAPS | NanoVG.NVG_IMAGE_NEAREST)
						: 0);

		ALL_LOADED_TEXTURES.add(this);
	}

	/**
	 * Creates an empty texture.
	 *
	 * @param width
	 *            Width of the texture
	 * @param height
	 *            Height of the texture
	 * @param pixelFormat
	 *            Specifies the format of the pixel data (GL_RGBA, etc.)
	 * @throws Exception
	 */
	public Texture(int width, int height, int pixelFormat) throws Exception {
		this.gl_id = glGenTextures();
		this.name = "Empty" + gl_id;
		this.width = width;
		this.height = height;
		glBindTexture(GL_TEXTURE_2D, this.gl_id);
		glTexImage2D(GL_TEXTURE_2D, 0, pixelFormat, this.width, this.height, 0, pixelFormat, GL_FLOAT,
				(ByteBuffer) null);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

		vg_id = -1;
	}

	private void createMipmap() {
		GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
	}

	public final int getWidth() {
		return width;
	}

	public final int getHeight() {
		return height;
	}

	public int getVGId() {
		return vg_id;
	}

	public int getGLId() {
		return gl_id;
	}

	@Override
	public void cleanup() {
		glDeleteTextures(gl_id);
		NanoVG.nvgDeleteImage(VgGuiHandler.instance.getVg(), vg_id);
		ALL_LOADED_TEXTURES.remove(this);
	}

	public final int getNum_rows() {
		return num_rows;
	}

	public final int getNum_cols() {
		return num_cols;
	}

	/**
	 * Vec4( x_ofset, y_ofset );
	 * 
	 * @return
	 */
	public Vector2f getFrameOfsets() {
		return new Vector2f(0, 0);
	}

	public static final void masscleanup() {
		while (!ALL_LOADED_TEXTURES.isEmpty()) {
			ALL_LOADED_TEXTURES.getFirst().cleanup();
		}
	}

	public static final Texture find(String name) {
		for (Texture t : ALL_LOADED_TEXTURES) {
			if (t.name.equals(name)) {
				return t;
			}
		}
		return null;
	}
}