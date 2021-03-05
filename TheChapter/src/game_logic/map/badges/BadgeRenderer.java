package game_logic.map.badges;

import java.util.LinkedList;

import org.joml.Matrix4f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import engine.Engine;
import game_logic.map.Map;
import game_logic.map.TileSizeHandler;
import game_logic.map.Tile.TileVisibility;
import game_logic.map.player.Human;
import graphicsSupport.ExternalRenderer;
import graphicsSupport.camera.Camera;
import graphicsSupport.mesh.Mesh;
import graphicsSupport.shaders.UniformedShaderProgram;
import graphicsSupport.texture.Texture;

public class BadgeRenderer implements ExternalRenderer {

	// Shader
	public static final UniformedShaderProgram shader = UniformedShaderProgram.createExternalUniformedShaderProgram(
			Engine.getResourceAsFile("/shaders/badges/VertexBadgeShader.vert"),
			Engine.getResourceAsFile("/shaders/badges/FragmentBadgeShader.frag"),
			new String[] { "projectionMatrix", "modelMatrix", "viewMatrix",
					// Lights
					"ambientLight", "pointLights", "directionalLight", "spotLights", // Frustum
					"frustum", "frag_texture", "material", "specularPower", "fog", "color", "alpha" });

	// Storage system
	private LinkedList<BadgeColor> storage = new LinkedList<BadgeColor>();

	// Badge mesh
	private final Mesh mesh = Mesh.find("Badge")[0];
	private Matrix4f modelMatrix = new Matrix4f();

	public BadgeRenderer() {
	}

	@Override
	public void render(Camera cam) {
		shader.setUniform("material", mesh.material);
		
		// Enabling VAO
		GL30.glBindVertexArray(mesh.vaoId);

		// Enabling vertex VBO
		GL20.glEnableVertexAttribArray(0);

		// Enabling normal VBO
		GL20.glEnableVertexAttribArray(1);

		GL20.glEnableVertexAttribArray(3);

		GL13.glActiveTexture(GL13.GL_TEXTURE0);

		Map map = Human.instance.getTeam().getMap();

		for (BadgeColor color : storage) {
			shader.setUniform("color", color.color);

			for (BadgeTexture texture : color.badges) {

				GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.texture.getGLId());
				shader.setUniform("frag_texture", texture.texture);

				for (int i = 0; i < texture.badges.size(); i++) {

					Vector2i coords = map.getCoordinates(map.getTile(texture.badges.get(i).getLocation()));
					if (Human.instance.getTeam().getVisibilityMap()[coords.x][coords.y] != TileVisibility.VISIBLE) {
						continue;
					}

					this.modelMatrix.identity().translate(texture.badges.get(i).getLocation());
					cam.getViewMatrix().transpose3x3(this.modelMatrix);
					modelMatrix.scale(TileSizeHandler.instance.getTileSize() / 12f);

					shader.setUniform("alpha", (float) ((float) texture.badges.get(i).getAlpha() / 255f));
					shader.setUniform("modelMatrix", this.modelMatrix);

					GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, mesh.vertexQuantity);
				}
			}
		}

		GL20.glDisableVertexAttribArray(3);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

		// Disable normals VBO
		GL20.glDisableVertexAttribArray(1);

		// Disabling vertex VBO
		GL20.glDisableVertexAttribArray(0);

		// Disabling VAO
		GL30.glBindVertexArray(0);
	}

	@Override
	public UniformedShaderProgram getShader() {
		return shader;
	}

	private BadgeColor findColor(Vector3i col) {
		for (int i = 0; i < storage.size(); i++) {
			if (storage.get(i).r == col.x && storage.get(i).g == col.y && storage.get(i).b == col.z) {
				return storage.get(i);
			}
		}
		return null;
	}

	private class BadgeColor {
		// RGB
		private int r;
		private int g;
		private int b;

		// Color
		private Vector3f color;

		// Textures
		private LinkedList<BadgeTexture> badges = new LinkedList<BadgeTexture>();

		public BadgeColor(int r, int g, int b) {
			super();
			this.r = r;
			this.g = g;
			this.b = b;
			color = new Vector3f((float) ((float) r / 255f), (float) ((float) g / 255f), (float) ((float) b / 255f));
		}

		private BadgeTexture findTexture(Texture text) {
			for (int i = 0; i < badges.size(); i++) {
				if (badges.get(i).texture == text) {
					return badges.get(i);
				}
			}
			return null;
		}

	}

	private class BadgeTexture {
		// Texture
		private final Texture texture;

		// List
		private final LinkedList<Badge> badges = new LinkedList<Badge>();

		public BadgeTexture(Texture texture) {
			super();
			this.texture = texture;
		}

	}

	public void add(Badge badge) {
		BadgeColor color = findColor(badge.getColor());
		if (color == null) {
			storage.add((color = new BadgeColor(badge.getColor().x, badge.getColor().y, badge.getColor().z)));
		}

		BadgeTexture texture = color.findTexture(badge.getTexture());
		if (texture == null) {
			color.badges.add((texture = new BadgeTexture(badge.getTexture())));
		}

		texture.badges.add(badge);
	}

	public void remove(Badge badge) {
		BadgeColor color = findColor(badge.getColor());
		BadgeTexture texture = color.findTexture(badge.getTexture());

		texture.badges.remove(badge);

		if (texture.badges.size() == 0) {
			color.badges.remove(texture);
		}

		if (color.badges.size() == 0) {
			storage.remove(color);
		}
	}

	@Override
	public RenderPriority getRenderPriority() {
		return RenderPriority.hight;
	}

}
