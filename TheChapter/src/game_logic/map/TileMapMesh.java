package game_logic.map;

import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBindBuffer;

import java.nio.FloatBuffer;
import java.util.LinkedList;

import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import engine.Engine;
import game_logic.GameOptions;
import game_logic.map.Tile.TileVisibility;
import game_logic.storage.Textures;
import graphicsSupport.ExternalRenderer;
import graphicsSupport.Item;
import graphicsSupport.camera.Camera;
import graphicsSupport.effects.DirectionalLight;
import graphicsSupport.effects.DirectionalLightMatricesHandler;
import graphicsSupport.effects.Material;
import graphicsSupport.mesh.Mesh;
import graphicsSupport.shaders.UniformedShaderProgram;
import graphicsSupport.texture.Texture;

public final class TileMapMesh extends Mesh implements ExternalRenderer {

	// Shader program
	public static final UniformedShaderProgram shader_program = UniformedShaderProgram
			.createExternalUniformedShaderProgram(Engine.getResourceAsFile("/shaders/tileMap/VertexTileMapShader.vert"),
					Engine.getResourceAsFile("/shaders/tileMap/FragmentTileMapShader.frag"), new String[] {
							// Matrices
							"projectionMatrix", "viewMatrix",

							// Lights
							"ambientLight", "pointLights", "directionalLight", "spotLights",

							// Material
							"material", "specularPower",

							// Effects
							"fog",

							// Samplers
							"texture0", "texture1", "texture2", "texture3", "max_textures",

							// Parameters
							"map_grid",

							// Frustum
							"frustum", "normalMap"

					});
	static {
		int q = Engine.getEngineOptions().getDirectionalLightPerspectiveSplitsQuantity();

		try {
			shader_program.createUniform("lightViewMatrix");
			shader_program.createUniform("orthoProjectionMatrix");
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		shader_program.createListUniform("splits", q);
		for (int i = 0; i < q; i++) {
			try {
				shader_program.createUniform("shadowMap" + i);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// Textures
	private final int[][][] texture_locations;
	private final int[][][] angles;
	private boolean buffer_changed;
	private final LinkedList<Texture> textures = new LinkedList<Texture>();

	private static final int max_texture_per_tile = 3;
	private final int[] texture_indexes_vbo = new int[modulesQuantity];
	private final int[] angles_vbo = new int[modulesQuantity];

	private final int visibilityMapVbo;

	private static final int modulesQuantity = 2;

	// Color map
	private static final Vector3f[] colors = new Vector3f[] { new Vector3f(0, 0.2f, 0), new Vector3f(1, 1, 1) };
	private static final float[] heights = new float[] { 0f, 10f };

	// Map
	private final Map map;

	// Refreshing visibility map
	private long lastRefresh;
	private static final long refreshPeriod = 10l;

	private final int pointsPerTileQuantity;

	public TileMapMesh(Map map) {
		super(convertVertices(map.getTiles()), convertNormals(map.getTiles()), convertTexturePositions(map.getTiles()),
				convertIndices(map.getTiles()), "TileMapMesh", convertMaterial(), -1);
		this.map = map;
		pointsPerTileQuantity = TileSizeHandler.instance.getTileVerticesComponentsQuantity() / 3;

		texture_locations = new int[map.getTiles().length][map.getTiles()[0].length][max_texture_per_tile
				* modulesQuantity];
		angles = new int[map.getTiles().length][map.getTiles()[0].length][max_texture_per_tile * modulesQuantity];

		// Enabling VAO
		GL30.glBindVertexArray(vaoId);

		FloatBuffer buf;
		float[] text_indexes = new float[texture_locations.length * texture_locations[0].length
				* pointsPerTileQuantity];
		int id;

		// Indexes
		for (int i = 0; i < modulesQuantity; i++) {

			buf = MemoryUtil.memAllocFloat(text_indexes.length);
			buf.put(text_indexes);
			buf.flip();

			id = GL15.glGenBuffers();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, id);

			// Loading to VBO
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buf, GL15.GL_STATIC_DRAW);
			GL20.glVertexAttribPointer(6 + i * 2, 1, GL11.GL_FLOAT, false, 0, 0);

			// disabling VBO
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

			MemoryUtil.memFree(buf);
			texture_indexes_vbo[i] = id;
			vbos.add(id);
		}

		for (int i = 0; i < modulesQuantity; i++) {
			buf = MemoryUtil.memAllocFloat(text_indexes.length);
			buf.put(text_indexes);
			buf.flip();

			id = GL15.glGenBuffers();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, id);

			// Loading to VBO
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buf, GL15.GL_STATIC_DRAW);
			GL20.glVertexAttribPointer(7 + i * 2, 1, GL11.GL_FLOAT, false, 0, 0);

			// disabling VBO
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

			MemoryUtil.memFree(buf);
			angles_vbo[i] = id;
			vbos.add(id);
		}

		// Visibility map
		buf = MemoryUtil.memAllocFloat(text_indexes.length);
		buf.put(text_indexes);
		buf.flip();

		id = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, id);

		// Loading to VBO
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buf, GL15.GL_STATIC_DRAW);
		GL20.glVertexAttribPointer(4, 1, GL11.GL_FLOAT, false, 0, 0);

		// disabling VBO
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

		MemoryUtil.memFree(buf);

		visibilityMapVbo = id;
		vbos.add(id);

		// Color map
		buf = MemoryUtil.memAllocFloat(text_indexes.length * 3);
		text_indexes = new float[text_indexes.length * 3];
		fillColors(text_indexes, map);
		buf.put(text_indexes);
		buf.flip();

		id = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, id);

		// Loading to VBO
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buf, GL15.GL_STATIC_DRAW);
		GL20.glVertexAttribPointer(5, 3, GL11.GL_FLOAT, false, 0, 0);

		// disabling VBO
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

		MemoryUtil.memFree(buf);

		vbos.add(id);

		// Disabling VAO
		GL30.glBindVertexArray(0);
	}

	private static void fillColors(float[] arr, Map map) {
		int i = 0;
		Tile[][] tiles = map.getTiles();
		for (int x = 0; x < tiles.length; x++) {
			for (int z = 0; z < tiles[x].length; z++) {
				float[] verts = tiles[x][z].getVertices();
				for (int a = 0; a < verts.length; a += 3) {
					calcColor(verts[a + 1], heights, map.getScenario().getMapGenerator().getMaxHeight(), colors, arr, i * 3);
					i++;
				}
			}
		}

	}

	private static void calcColor(float height, float[] heights, float maxHeight, Vector3f[] colors, float[] out,
			int index) {
		float scalar = height / maxHeight;
		int bottom = -1;
		int top = -1;
		for (int i = 0; i < heights.length; i++) {
			if (heights[i] <= scalar && scalar <= heights[i + 1]) {
				bottom = i;
				top = bottom + 1;
				break;
			}
		}

		float localScalar = (-heights[bottom] + scalar) / (heights[top] - heights[bottom]);
		
		out[index] = localScalar * (colors[top].x - colors[bottom].x) + colors[bottom].x;
		out[index + 1] = localScalar * (colors[top].y - colors[bottom].y) + colors[bottom].y;

		out[index + 2] = localScalar * (colors[top].z - colors[bottom].z) + colors[bottom].z;

	}

	private static Material convertMaterial() {
		Material res = new Material("TileMapMaterial");
		return res;
	}

	private static float[] convertTexturePositions(Tile[][] tiles) {
		float[] result = new float[tiles.length * tiles[0].length * tiles[0][0].getTextureCoordinates().length];
		int i = 0;
		for (int x = 0; x < tiles.length; x++) {
			for (int z = 0; z < tiles[x].length; z++) {
				float[] texts = tiles[x][z].getTextureCoordinates();
				for (int a = 0; a < texts.length; a++) {
					result[i] = texts[a];
					i++;
				}
			}
		}

		return result;
	}

	private static int[] convertIndices(Tile[][] tiles) {
		int[] result = new int[tiles.length * tiles[0].length
				* TileSizeHandler.instance.getTileVerticesComponentsQuantity() / 3];
		for (int i = 0; i < result.length; i++) {
			result[i] = i;
		}
		return result;
	}

	private static float[] convertNormals(Tile[][] tiles) {
		float[] result = new float[tiles.length * tiles[0].length * tiles[0][0].getNormals().length];
		int i = 0;
		for (int x = 0; x < tiles.length; x++) {
			for (int z = 0; z < tiles[x].length; z++) {
				float[] norms = tiles[x][z].getNormals();
				for (int a = 0; a < norms.length; a++) {
					result[i] = norms[a];
					i++;
				}
			}
		}

		return result;
	}

	private static float[] convertVertices(Tile[][] tiles) {
		float[] result = new float[tiles[0].length * tiles.length * tiles[0][0].getVertices().length];
		int i = 0;
		for (int x = 0; x < tiles.length; x++) {
			for (int z = 0; z < tiles[x].length; z++) {
				float[] verts = tiles[x][z].getVertices();
				for (int a = 0; a < verts.length; a++) {
					result[i] = verts[a];
					i++;
				}
			}
		}

		return result;
	}

	@Override
	public void renderObjectsList(LinkedList<Item> objs, Camera cam) {
	}

	@Override
	public UniformedShaderProgram getShader() {
		return shader_program;
	}

	@Override
	public void render(Camera cam) {
		if (System.currentTimeMillis() - lastRefresh > refreshPeriod) {
			refreshVisibility();
			lastRefresh = System.currentTimeMillis();
		}

		bufferTextureData();

		// Enabling VAO
		GL30.glBindVertexArray(vaoId);

		// Enabling vertex VBO
		GL20.glEnableVertexAttribArray(0);

		// Enabling normal VBO
		GL20.glEnableVertexAttribArray(1);

		if (((GameOptions) Engine.getExternalOptions()).isMap_grid()) {
			GL13.glActiveTexture(GL13.GL_TEXTURE0);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, Textures.TILE_MAP_GRID.getGLId());
		}
		if (textures.size() > 0) {
			GL13.glActiveTexture(GL13.GL_TEXTURE1);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.textures.get(0).getGLId());
		}
		if (textures.size() > 1) {
			GL13.glActiveTexture(GL13.GL_TEXTURE2);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.textures.get(1).getGLId());
		}
		if (textures.size() > 2) {
			GL13.glActiveTexture(GL13.GL_TEXTURE3);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.textures.get(2).getGLId());
		}
		GL13.glActiveTexture(GL13.GL_TEXTURE4);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, Textures.PLAINS_TERRAIN_NM.getGLId());

		GL20.glEnableVertexAttribArray(3);

		for (int i = 0; i < modulesQuantity * 2; i++) {
			GL20.glEnableVertexAttribArray(4 + i);
		}

		shader_program.setUniform("map_grid", ((GameOptions) Engine.getExternalOptions()).isMap_grid() ? 1f : 0f);

		shader_program.setUniform("material", material);
		shader_program.setUniform("texture0", 0);
		shader_program.setUniform("texture1", 1);
		shader_program.setUniform("texture2", 2);
		shader_program.setUniform("texture3", 3);
		shader_program.setUniform("normalMap", 4);

		shader_program.setUniform("max_textures", (float) max_texture_per_tile);

		GL20.glEnableVertexAttribArray(8);

		// GL11.glDepthMask(false);

		if (Engine.getEngineOptions().isEnabled_shadows()) {
			shader_program.setUniform("lightViewMatrix",
					DirectionalLightMatricesHandler.instance.getLightViewMatrices());
			shader_program.setUniform("orthoProjectionMatrix",
					DirectionalLightMatricesHandler.instance.getOrthoProjectionMatrices());
			shader_program.setUniform("splits", Engine.getEngineOptions().getDirectional_light_splits());

			for (int i = 0; i < Engine.getEngineOptions().getDirectionalLightPerspectiveSplitsQuantity(); i++) {
				GL13.glActiveTexture(GL13.GL_TEXTURE20 + i);
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, DirectionalLight.instance.getShadowMaps()[i].getGLId());
				shader_program.setUniform("shadowMap" + i, 20 + i);
			}
		}

		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, this.vertexQuantity);

		// GL11.glDepthMask(true);

		GL20.glDisableVertexAttribArray(8);

		glBindBuffer(GL_ARRAY_BUFFER, 0);

		for (int i = 0; i < modulesQuantity * 2; i++) {
			GL20.glDisableVertexAttribArray(4 + i);
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

	public void setTextureForTile(int x, int z, Texture text) {
		int index = 0;
		if (textures.contains(text)) {
			index = textures.indexOf(text);
		} else {
			index = textures.size();
			textures.add(text);
		}

		for (int i = 0; i < texture_locations[x][z].length; i++) {
			if (texture_locations[x][z][i] == 0) {
				texture_locations[x][z][i] = index + 1;
				break;
			}
		}

		buffer_changed = true;
	}

	public void setTextureForTile(int x, int z, Texture text, int angle) {
		int index = 0;
		if (textures.contains(text)) {
			index = textures.indexOf(text);
		} else {
			index = textures.size();
			textures.add(text);
		}

		for (int i = 0; i < texture_locations[x][z].length; i++) {
			if (texture_locations[x][z][i] == 0) {
				texture_locations[x][z][i] = index + 1;
				angles[x][z][i] = angle / 10;
				break;
			}
		}

		buffer_changed = true;
	}

	public void removeTextureFromTile(int x, int z, Texture text) {
		int index = 1 + textures.indexOf(text);
		for (int i = 0; i < max_texture_per_tile; i++) {
			if (texture_locations[x][z][i] == index) {
				texture_locations[x][z][i] = 0;
				angles[x][z][i] = 0;
			}
		}
		buffer_changed = true;
		for (int a = 0; a < texture_locations.length; a++) {
			for (int y = 0; y < texture_locations[x].length; y++) {
				for (int i = 0; i < max_texture_per_tile; i++) {
					if (textures.get(
							(this.texture_locations[a][y][i] > 0 ? this.texture_locations[a][y][i] : 1) - 1) == text) {
						return;
					}
				}
			}
		}

		textures.remove(text);

	}

	public void removeTextureFromAllTiles(Texture texture) {
		int index = 1 + textures.indexOf(texture);
		for (int a = 0; a < texture_locations.length; a++) {
			for (int y = 0; y < texture_locations[a].length; y++) {
				for (int i = 0; i < max_texture_per_tile; i++) {
					if (texture_locations[a][y][i] == index) {
						texture_locations[a][y][i] = 0;
						angles[a][y][i] = 0;
					}
				}
			}
		}

		buffer_changed = true;
	}

	private final void refreshVisibility() {
		TileVisibility[][] visMap = map.getBoundedTeam().getVisibilityMap();
		float[] out = new float[texture_locations.length * texture_locations[0].length * pointsPerTileQuantity];

		for (int x = 0; x < map.getWidth(); x++) {
			for (int y = 0; y < map.getHeight(); y++) {
				for (int i = 0; i < pointsPerTileQuantity; i++) {
					out[(x * texture_locations[0].length + y) * pointsPerTileQuantity + i] = visMap[x][y].ordinal();
				}
			}
		}

		glBindBuffer(GL_ARRAY_BUFFER, this.visibilityMapVbo);
		GL15.glBufferSubData(GL_ARRAY_BUFFER, 0, out);
		GL15.glBindBuffer(GL_ARRAY_BUFFER, 0);
	}

	private void bufferTextureData() {
		if (!buffer_changed) {
			return;
		}
		for (int index = 0; index < modulesQuantity; index++) {

			float[] out = new float[texture_locations.length * texture_locations[0].length * pointsPerTileQuantity];
			for (int x = 0; x < texture_locations.length; x++) {
				for (int y = 0; y < texture_locations[x].length; y++) {

					float code = 1;
					for (int i = 0; i < max_texture_per_tile; i++) {
						code = code * 100 + texture_locations[x][y][i + index * max_texture_per_tile];
					}

					for (int i = 0; i < pointsPerTileQuantity; i++) {
						out[(x * texture_locations[0].length + y) * pointsPerTileQuantity + i] = code;
					}

				}
			}

			glBindBuffer(GL_ARRAY_BUFFER, this.texture_indexes_vbo[index]);
			GL15.glBufferSubData(GL_ARRAY_BUFFER, 0, out);
			GL15.glBindBuffer(GL_ARRAY_BUFFER, 0);

			out = new float[texture_locations.length * texture_locations[0].length * pointsPerTileQuantity];
			for (int x = 0; x < texture_locations.length; x++) {
				for (int y = 0; y < texture_locations[x].length; y++) {

					float code = 1;
					for (int i = 0; i < max_texture_per_tile; i++) {
						code = code * 100f + (float) angles[x][y][i + index * max_texture_per_tile];
					}

					for (int i = 0; i < pointsPerTileQuantity; i++) {
						out[(x * texture_locations[0].length + y) * pointsPerTileQuantity + i] = code;
					}

				}
			}

			glBindBuffer(GL_ARRAY_BUFFER, this.angles_vbo[index]);
			GL15.glBufferSubData(GL_ARRAY_BUFFER, 0, out);
			GL15.glBindBuffer(GL_ARRAY_BUFFER, 0);

		}

		buffer_changed = false;
	}

	@Override
	public RenderPriority getRenderPriority() {
		return ExternalRenderer.RenderPriority.normal;
	}

}
