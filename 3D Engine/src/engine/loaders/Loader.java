package engine.loaders;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import graphicsSupport.mesh.Mesh;
import graphicsSupport.texture.Texture;
import resources.ResourcesPack;
import soundSupport.SoundBuffer;
import utilities.Console;

public class Loader {

	// Constants
	private static final String informationFileName = "MODEL_INFO.MI";

	public static ResourcesPack loadModels(File dir) throws Exception {

		Console.println("Loading models from: " + dir.getPath());
		
		File[] files = dir.listFiles();
		
		ResourcesPack pack = new ResourcesPack();
		
		for (File file : files) {
			Mesh[] mesh = loadMesh(file);
			Mesh.ALL_LOADED_MESHES.put(mesh[0].ID, mesh);
			for (int i = 0; i < mesh.length; i++) {
				pack.put(mesh[i]);
			}
		}
		
		pack.seal();
		
		return pack;
	}

	public static ResourcesPack loadAnimations(File dir) throws Exception {
		Console.println("Loading animations from: " + dir.getPath());
		
		File[] files = dir.listFiles();
		
		ResourcesPack pack = new ResourcesPack();
		
		for (File file : files) {
			Mesh[] mesh = loadAnimation(file);
			Mesh.ALL_LOADED_MESHES.put(mesh[0].ID, mesh);
			for (int i = 0; i < mesh.length; i++) {
				pack.put(mesh[i]); 
			}
		}
		
		pack.seal();
		
		return pack;
	}

	private static Mesh[] loadMesh(File base) {
		try {
			File info = new File(base.getAbsolutePath() + "/" + informationFileName);
			BufferedReader reader = new BufferedReader(new FileReader(info));

			// Name
			String[] param = readINFOParameter(reader.readLine());
			String name = param[1];

			// Model
			param = readINFOParameter(reader.readLine());
			String modelPath = param[1];

			// Chunked
			param = readINFOParameter(reader.readLine());
			int chunked;
			if (!param[1].equals("")) {
				chunked = Integer.parseInt(param[1]);
			} else {
				chunked = 0;
			}

			// Author
			param = readINFOParameter(reader.readLine());
			String author = param[1];

			reader.close();

			File model = new File(base.getAbsolutePath() + modelPath);

			Mesh[] result = StaticMeshesLoader.load(model, name, author, chunked);

			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static String[] readINFOParameter(String in) {
		String[] out = new String[2];
		out[0] = in.substring(0, in.indexOf("{"));
		out[1] = in.substring(in.indexOf("{") + 1, in.indexOf("}"));
		return out;
	}

	private static Mesh[] loadAnimation(File base) {

		// Parameters
		String name;
		int chunk;
		int quantity;
		String author;
		File[] frames;
		long[] times;

		try {
			// Reading info file
			File info = new File(base.getAbsolutePath() + "/" + informationFileName);
			BufferedReader reader = new BufferedReader(new FileReader(info));

			// Name
			String[] param = readINFOParameter(reader.readLine());
			name = param[1];
			
			// Chunked
			param = readINFOParameter(reader.readLine());
			if (param[1].equals("")) {
				chunk = 0;
			} else {
				chunk = Integer.parseInt(param[1]);
			}

			// Animation quantity
			param = readINFOParameter(reader.readLine());
			quantity = Integer.parseInt(param[1]);

			frames = new File[quantity];
			times = new long[quantity];

			// Frames
			for (int i = 0; i < quantity; i++) {
				param = readINFOParameter(reader.readLine());
				frames[i] = new File(base.getPath() + param[1]);
				param = readINFOParameter(reader.readLine());
				times[i] = Long.parseLong(param[1]);
			}

			// Author
			param = readINFOParameter(reader.readLine());
			author = param[1];

			reader.close();

			Mesh[] meshes = AnimatedMeshesLoader.loadAnimation(frames, times, name, chunk);

			for (Mesh anim : meshes) {
				anim.setAuthor(author);
			}

			return meshes;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static ResourcesPack loadTextures(File dir) throws Exception {
		Console.println("loading textures from: " + dir.getPath());
		ArrayList<File> files = new ArrayList<File>();
		listf(dir.getPath(), files);

		ResourcesPack pack = new ResourcesPack();
		
		for (File file : files) {
			pack.put(new Texture(file));
		}
		
		pack.seal();

		return pack;
	}

	public static ResourcesPack loadSounds(File dir) throws Exception {
		Console.println("Loading sounds from: " + dir.getPath());
		ArrayList<File> files = new ArrayList<File>();
		listf(dir.getPath(), files);

		ResourcesPack pack = new ResourcesPack();
		
		for (File file : files) {
			pack.put(new SoundBuffer(file));
		}
		
		return pack;
	}


	public static void listf(String directoryName, ArrayList<File> files) {
		File directory = new File(directoryName);

		// get all the files from a directory
		File[] fList = directory.listFiles();
		for (File file : fList) {
			if (file.isFile()) {
				files.add(file);
			} else if (file.isDirectory()) {
				listf(file.getAbsolutePath(), files);
			}
		}
	}

}
