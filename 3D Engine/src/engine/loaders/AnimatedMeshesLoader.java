package engine.loaders;

import static org.lwjgl.assimp.Assimp.aiImportFile;
import static org.lwjgl.assimp.Assimp.aiProcess_Triangulate;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.AIMaterial;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AIScene;

import graphicsSupport.effects.Material;
import graphicsSupport.mesh.Mesh;
import graphicsSupport.mesh.animation.Animation;
import graphicsSupport.mesh.animation.ChunkedAnimation;

public class AnimatedMeshesLoader extends StaticMeshesLoader {

	public static Mesh[] loadAnimation(File[] frames, long[] times, String name, int chunk) {
		RawMesh[][] rawFrames = new RawMesh[frames.length][];
		for (int i = 0; i < frames.length; i++) {
			try {
				rawFrames[i] = load(frames[i]);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		int id = Mesh.createKey();

		Mesh[] anims = new Mesh[rawFrames[0].length];
		for (int meshIndex = 0; meshIndex < anims.length; meshIndex++) {

			float[][] verts = new float[frames.length][];
			float[][] normals = new float[frames.length][];
			float[] texturePos = rawFrames[0][meshIndex].texturePositions;
			int[] indices = rawFrames[0][meshIndex].indices;
			Material mat = rawFrames[0][meshIndex].material;

			for (int frameIndex = 0; frameIndex < frames.length; frameIndex++) {
				verts[frameIndex] = rawFrames[frameIndex][meshIndex].vertices;
				normals[frameIndex] = rawFrames[frameIndex][meshIndex].normals;
			}

			if ((mat.hasDiffuseTexture() || texturePos.length > 0) && chunk == 0) {
				anims[meshIndex] = new Animation(verts, normals, indices, name, mat, id, times);
			}
			if ((!mat.hasDiffuseTexture() && texturePos.length == 0) && chunk == 0) {
				anims[meshIndex] = new Animation(verts, normals, texturePos, indices, name, mat, id, times);
			}
			if ((mat.hasDiffuseTexture() || texturePos.length > 0) && chunk != 0) {
				anims[meshIndex] = new ChunkedAnimation(verts, normals, indices, name, mat, id, times, chunk);
			}
			if ((!mat.hasDiffuseTexture() && texturePos.length == 0) && chunk != 0) {
				anims[meshIndex] = new ChunkedAnimation(verts, normals, texturePos, indices, name, mat, id, times, chunk);
			}
		}

		Mesh.ALL_LOADED_MESHES.put(id, anims);
		
		return anims;
	}

	private static RawMesh[] load(File file) throws Exception {
		AIScene aiScene = aiImportFile(file.getPath(), aiProcess_Triangulate);
		if (aiScene == null) {
			throw new Exception("Error loading model");
		}

		int numMaterials = aiScene.mNumMaterials();
		PointerBuffer aiMaterials = aiScene.mMaterials();
		List<Material> materials = new ArrayList<>();
		for (int i = 0; i < numMaterials; i++) {
			AIMaterial aiMaterial = AIMaterial.create(aiMaterials.get(i));
			processMaterial(aiMaterial, materials, file.getParent());
		}

		int numMeshes = aiScene.mNumMeshes();
		PointerBuffer aiMeshes = aiScene.mMeshes();
		RawMesh[] meshes = new RawMesh[numMeshes];
		for (int i = 0; i < numMeshes; i++) {
			AIMesh aiMesh = AIMesh.create(aiMeshes.get(i));
			RawMesh mesh = processMesh(aiMesh, materials);
			meshes[i] = mesh;
		}

		return meshes;
	}

	private static RawMesh processMesh(AIMesh aiMesh, List<Material> materials) {
		List<Float> vertices = new ArrayList<>();
		List<Float> textures = new ArrayList<>();
		List<Float> normals = new ArrayList<>();
		List<Integer> indices = new ArrayList<>();

		processVertices(aiMesh, vertices);
		processNormals(aiMesh, normals);
		processTextCoords(aiMesh, textures);
		processIndices(aiMesh, indices);

		Material material;
		int materialIdx = aiMesh.mMaterialIndex();
		if (materialIdx >= 0 && materialIdx < materials.size()) {
			material = materials.get(materialIdx);
		} else {
			material = new Material("");
		}

		return new RawMesh(listToArray(vertices), listToArray(normals), listToArray(textures), listIntToArray(indices),
				material);
	}

	public static class RawMesh {

		public float[] vertices;
		public float[] normals;
		public float[] texturePositions;
		public int[] indices;
		public Material material;

		public RawMesh(float[] vertices, float[] normals, float[] texturePositions, int[] indices, Material material) {
			this.vertices = vertices;
			this.normals = normals;
			this.texturePositions = texturePositions;
			this.indices = indices;
			this.material = material;
		}

	}

}