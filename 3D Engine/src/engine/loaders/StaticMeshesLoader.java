package engine.loaders;

import static org.lwjgl.assimp.Assimp.*;

import java.io.File;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.AIColor4D;
import org.lwjgl.assimp.AIFace;
import org.lwjgl.assimp.AIMaterial;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AIScene;
import org.lwjgl.assimp.AIString;
import org.lwjgl.assimp.AIVector3D;
import org.lwjgl.assimp.Assimp;

import graphicsSupport.effects.Material;
import graphicsSupport.mesh.ChunkedMesh;
import graphicsSupport.mesh.Mesh;
import graphicsSupport.texture.Texture;

public class StaticMeshesLoader {

	private static final Vector3f default_color = new Vector3f(0, 0, 0);

	public static Mesh[] load(File file, String name, String author, int chunk) throws Exception {
		AIScene aiScene = aiImportFile(file.getPath(), aiProcess_Triangulate | aiProcess_GenSmoothNormals);
		if (aiScene == null) {
			throw new Exception("Error loading model from " + file.getPath());
		}
	
		int numMaterials = aiScene.mNumMaterials();
		PointerBuffer aiMaterials = aiScene.mMaterials();
		List<Material> materials = new ArrayList<>();
		for (int i = 0; i < numMaterials; i++) {
			AIMaterial aiMaterial = AIMaterial.create(aiMaterials.get(i));
			processMaterial(aiMaterial, materials, file.getParent());
		}

		int id = Mesh.createKey();
		int numMeshes = aiScene.mNumMeshes();
		PointerBuffer aiMeshes = aiScene.mMeshes();
		Mesh[] meshes = new Mesh[numMeshes];
		for (int i = 0; i < numMeshes; i++) {
			AIMesh aiMesh = AIMesh.create(aiMeshes.get(i));
			Mesh mesh = processMesh(aiMesh, materials, id, name, chunk, author);
			meshes[i] = mesh;
		}

		Mesh.ALL_LOADED_MESHES.put(id, meshes);

		return meshes;
	}

	protected static void processIndices(AIMesh aiMesh, List<Integer> indices) {
		int numFaces = aiMesh.mNumFaces();
		AIFace.Buffer aiFaces = aiMesh.mFaces();
		for (int i = 0; i < numFaces; i++) {
			AIFace aiFace = aiFaces.get(i);
			IntBuffer buffer = aiFace.mIndices();
			while (buffer.remaining() > 0) {
				indices.add(buffer.get());
			}
		}
	}

	protected static void processMaterial(AIMaterial aiMaterial, List<Material> materials, String texturesDir)
			throws Exception {
		AIColor4D colour = AIColor4D.create();

		AIString path = AIString.calloc();
		Assimp.aiGetMaterialTexture(aiMaterial, aiTextureType_DIFFUSE, 0, path, (IntBuffer) null, null, null, null,
				null, null);
		String textPath = path.dataString();
		Texture texture = null;

		if (textPath != null && textPath.length() > 0) {
			String textureFile = texturesDir + "/" + textPath;
			textureFile = textureFile.replace("//", "/");
			texture = new Texture(new File(textureFile));
		}

		@SuppressWarnings("unused")
		Vector3f ambient = default_color;
		int result = aiGetMaterialColor(aiMaterial, AI_MATKEY_COLOR_AMBIENT, aiTextureType_NONE, 0, colour);
		if (result == 0) {
			ambient = new Vector3f(colour.r(), colour.g(), colour.b());
		}

		Vector3f diffuse = default_color;
		result = aiGetMaterialColor(aiMaterial, AI_MATKEY_COLOR_DIFFUSE, aiTextureType_NONE, 0, colour);
		if (result == 0) {
			diffuse = new Vector3f(colour.r(), colour.g(), colour.b());
		}

		Vector3f specular = default_color;
		result = aiGetMaterialColor(aiMaterial, AI_MATKEY_COLOR_SPECULAR, aiTextureType_NONE, 0, colour);
		if (result == 0) {
			specular = new Vector3f(colour.r(), colour.g(), colour.b());
		}

		Material material = new Material(textPath);
		material.setAmbientColor(diffuse);
		material.setDiffuseColor(diffuse);
		material.setSpecularColor(specular);
		material.setReflectance(1.0f);
		material.setTexture(texture);
		materials.add(material);
	}

	private static Mesh processMesh(AIMesh aiMesh, List<Material> materials, int id, String name, int chunk,
			String author) {
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
			material = new Material(name);
		}

		Mesh mesh = null;
		if ((material.hasDiffuseTexture() || textures.size() > 0) && chunk == 0) {
			mesh = new Mesh(listToArray(vertices), listToArray(normals), listToArray(textures), listIntToArray(indices),
					name, material, id);
		}
		if ((!material.hasDiffuseTexture() && textures.size() == 0) && chunk == 0) {
			mesh = new Mesh(listToArray(vertices), listToArray(normals), listIntToArray(indices), name, material, id);
		}
		if ((!material.hasDiffuseTexture() && textures.size() == 0) && chunk != 0) {
			mesh = new ChunkedMesh(listToArray(vertices), listToArray(normals), listIntToArray(indices), name, chunk,
					material, id);
		}
		if ((material.hasDiffuseTexture() || textures.size() > 0) && chunk != 0) {
			mesh = new ChunkedMesh(listToArray(vertices), listToArray(normals), listToArray(textures),
					listIntToArray(indices), name, chunk, material, id);
		}

		mesh.setAuthor(author);

		return mesh;
	}

	protected static void processNormals(AIMesh aiMesh, List<Float> normals) {
		AIVector3D.Buffer aiNormals = aiMesh.mNormals();
		while (aiNormals != null && aiNormals.remaining() > 0) {
			AIVector3D aiNormal = aiNormals.get();
			normals.add(aiNormal.x());
			normals.add(aiNormal.y());
			normals.add(aiNormal.z());
		}
	}

	protected static void processTextCoords(AIMesh aiMesh, List<Float> textures) {
		AIVector3D.Buffer textCoords = aiMesh.mTextureCoords(0);
		int numTextCoords = textCoords != null ? textCoords.remaining() : 0;
		for (int i = 0; i < numTextCoords; i++) {
			AIVector3D textCoord = textCoords.get();
			textures.add(textCoord.x());
			textures.add(1 - textCoord.y());
		}
	}

	protected static void processVertices(AIMesh aiMesh, List<Float> vertices) {
		AIVector3D.Buffer aiVertices = aiMesh.mVertices();
		while (aiVertices.remaining() > 0) {
			AIVector3D aiVertex = aiVertices.get();
			vertices.add(aiVertex.x());
			vertices.add(aiVertex.y());
			vertices.add(aiVertex.z());
		}
	}

	protected static int[] listIntToArray(List<Integer> list) {
		int[] result = list.stream().mapToInt((Integer v) -> v).toArray();
		return result;
	}

	protected static float[] listToArray(List<Float> list) {
		int size = list != null ? list.size() : 0;
		float[] floatArr = new float[size];
		for (int i = 0; i < size; i++) {
			floatArr[i] = list.get(i);
		}
		return floatArr;
	}

}
