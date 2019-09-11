package engine.options;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import org.joml.Vector2i;

public class EngineOptions extends Options {

	// Rendering options
	private float fov = (float) Math.toRadians(90);
	private float render_range = 100;
	private boolean v_sync = false;
	private boolean mipmapping = true;
	private int targetFps = 80;
	private boolean printFPS = false;
	private boolean printSoundLog = false;
	private boolean cull_face = false;
	private boolean antialiasing = true;
	private boolean full_screen = false;
	private boolean enabled_shadows = true;
	private boolean compatible_profile = true;
	private boolean enabledLightDebug = false;
	private int contextVersionMinor = 3;
	private int contextVersionMajor = 3;
	private float[] directional_light_splits = new float[] { 10f, 30f, 50f, 100f };
	private Vector2i[] directional_light_splits_resolution = new Vector2i[] { new Vector2i(2048, 2048),
			new Vector2i(1024, 1024), new Vector2i(1024, 1024), new Vector2i(1024, 1024) };

	protected EngineOptions() {
	}

	@Override
	public void createNewOptionsFile() throws IOException {
		if (dir.exists()) {
			dir.delete();
		}
		dir.createNewFile();

		BufferedWriter writer = new BufferedWriter(new FileWriter(dir));

		writer.write("FOV:" + Float.toString(fov));
		writer.newLine();

		writer.write("Render range:" + Float.toString(render_range));
		writer.newLine();

		writer.write("vsync enabled:" + Boolean.toString(v_sync));
		writer.newLine();

		writer.write("mipmapping enabled:" + Boolean.toString(mipmapping));
		writer.newLine();

		writer.write("target FPS:" + Integer.toString(targetFps));
		writer.newLine();

		writer.write("print FPS enabled:" + Boolean.toString(printFPS));
		writer.newLine();

		writer.write("face culling enabled:" + Boolean.toString(cull_face));
		writer.newLine();

		writer.write("antialiasing enabled:" + Boolean.toString(antialiasing));
		writer.newLine();

		writer.write("full screen enabled:" + Boolean.toString(full_screen));
		writer.newLine();

		writer.write("pring sound log:" + Boolean.toString(printSoundLog));
		writer.newLine();

		writer.write("enabled shadows:" + Boolean.toString(enabled_shadows));
		writer.newLine();

		writer.write("compatible profile:" + Boolean.toString(compatible_profile));
		writer.newLine();

		writer.write("light debug enabled:" + Boolean.toString(enabledLightDebug));
		writer.newLine();

		writer.write("context version minor:" + Integer.toString(contextVersionMinor));
		writer.newLine();

		writer.write("context version major:" + Integer.toString(contextVersionMajor));
		writer.newLine();

		writer.write("directional light total splits:" + Integer.toString(directional_light_splits.length - 1));
		writer.newLine();

		for (int i = 0; i < directional_light_splits.length - 1; i++) {
			writer.write("split " + i + ":" + directional_light_splits[i]);
			writer.newLine();

			writer.write("split resolution:" + directional_light_splits_resolution[i].x + ":"
					+ directional_light_splits_resolution[i].y);
			writer.newLine();
		}

		writer.flush();
		writer.close();
	}

	@Override
	protected void loadOptions() throws NumberFormatException, IOException {
		if (!dir.exists()) {
			createNewOptionsFile();
		} else {
			BufferedReader reader = new BufferedReader(new FileReader(dir));

			fov = Float.parseFloat(cutComments(reader.readLine()));
			render_range = Float.parseFloat(cutComments(reader.readLine()));
			v_sync = Boolean.parseBoolean(cutComments(reader.readLine()));
			mipmapping = Boolean.parseBoolean(cutComments(reader.readLine()));
			targetFps = Integer.parseInt(cutComments(reader.readLine()));
			printFPS = Boolean.parseBoolean(cutComments(reader.readLine()));
			cull_face = Boolean.parseBoolean(cutComments(reader.readLine()));
			antialiasing = Boolean.parseBoolean(cutComments(reader.readLine()));
			full_screen = Boolean.parseBoolean(cutComments(reader.readLine()));
			printSoundLog = Boolean.parseBoolean(cutComments(reader.readLine()));
			enabled_shadows = Boolean.parseBoolean(cutComments(reader.readLine()));
			compatible_profile = Boolean.parseBoolean(cutComments(reader.readLine()));
			enabledLightDebug = Boolean.parseBoolean(cutComments(reader.readLine()));
			contextVersionMinor = Integer.parseInt(cutComments(reader.readLine()));
			contextVersionMajor = Integer.parseInt(cutComments(reader.readLine()));
			directional_light_splits = new float[1 + Integer.parseInt(cutComments(reader.readLine()))];
			for (int i = 0; i < directional_light_splits.length - 1; i++) {
				directional_light_splits[i] = Float.parseFloat(cutComments(reader.readLine()));
				String[] sp = reader.readLine().split(":");
				directional_light_splits_resolution[i] = new Vector2i(Integer.parseInt(sp[1]), Integer.parseInt(sp[2]));
			}
			directional_light_splits[directional_light_splits.length - 1] = render_range;
			
			
			reader.close();
		}
	}

	public final float getFov() {
		return fov;
	}

	public final float getRender_range() {
		return render_range;
	}

	public final boolean v_sync() {
		return v_sync;
	}

	public final boolean mipmapping() {
		return mipmapping;
	}

	public final float getTargetFps() {
		return targetFps;
	}

	public final boolean printFPS() {
		return printFPS;
	}

	public final boolean cull_face() {
		return cull_face;
	}

	public final boolean antialiasing() {
		return antialiasing;
	}

	public final boolean full_screen() {
		return full_screen;
	}

	public final boolean printSoundLog() {
		return printSoundLog;
	}

	public final boolean isEnabled_shadows() {
		return enabled_shadows;
	}

	public final boolean isCompatible_profile() {
		return compatible_profile;
	}

	public final boolean isEnabledLightDebug() {
		return enabledLightDebug;
	}

	public final int getContextVersionMinor() {
		return contextVersionMinor;
	}

	public final int getContextVersionMajor() {
		return contextVersionMajor;
	}

	public final int getDirectionalLightPerspectiveSplitsQuantity() {
		return directional_light_splits.length;
	}

	public final float[] getDirectional_light_splits() {
		return directional_light_splits;
	}

	public final Vector2i[] getDirectional_light_splits_resolution() {
		return directional_light_splits_resolution;
	}

	@Override
	public String toString() {
		return "EngineOptions [fov=" + fov + ", render_range=" + render_range + ", v_sync=" + v_sync + ", mipmapping="
				+ mipmapping + ", targetFps=" + targetFps + ", printFPS=" + printFPS + ", printSoundLog="
				+ printSoundLog + ", cull_face=" + cull_face + ", antialiasing=" + antialiasing + ", full_screen="
				+ full_screen + ", enabled_shadows=" + enabled_shadows + ", compatible_profile=" + compatible_profile
				+ ", enabledLightDebug=" + enabledLightDebug + ", contextVersionMinor=" + contextVersionMinor
				+ ", contextVersionMajor=" + contextVersionMajor + ", directional_light_splits="
				+ Arrays.toString(directional_light_splits) + ", directional_light_splits_resolution="
				+ Arrays.toString(directional_light_splits_resolution) + "]";
	}

}
