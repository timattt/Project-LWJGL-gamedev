package soundSupport;

import static org.lwjgl.openal.AL10.alDistanceModel;
import static org.lwjgl.openal.ALC10.alcCloseDevice;
import static org.lwjgl.openal.ALC10.alcCreateContext;
import static org.lwjgl.openal.ALC10.alcDestroyContext;
import static org.lwjgl.openal.ALC10.alcMakeContextCurrent;
import static org.lwjgl.openal.ALC10.alcOpenDevice;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;

import engine.monoDemeanor.MonoDemeanor;
import engine.monoDemeanor.MonoDemeanorCleanup;
import engine.monoDemeanor.MonoDemeanorInit;
import engine.monoDemeanor.MonoDemeanorInstance;
import engine.monoDemeanor.MonoDemeanorUpdate;
import graphicsSupport.camera.Camera;
import utilities.Console;

@MonoDemeanor
public class SoundManager {
	
	@MonoDemeanorInstance
	public static final SoundManager instance = new SoundManager();
	
	// System
	private long device;
	private long context;

	// Listener
	private SoundListener listener;

	// Sources bundles
	private static final int max_sound_sources_bundles = 5;
	private static final int max_sources_per_bundle = 10;

	private final SoundSourcesBundle[] bundles = new SoundSourcesBundle[max_sound_sources_bundles];

	// Camera
	private final Matrix4f cameraMatrix;

	private SoundManager() {
		cameraMatrix = new Matrix4f();
	}

	@MonoDemeanorInit
	public void init() throws Exception {
		this.device = alcOpenDevice((ByteBuffer) null);
		if (device == NULL) {
			throw new IllegalStateException("Failed to open the default OpenAL device.");
		}
		ALCCapabilities deviceCaps = ALC.createCapabilities(device);
		this.context = alcCreateContext(device, (IntBuffer) null);
		if (context == NULL) {
			throw new IllegalStateException("Failed to create OpenAL context.");
		}
		alcMakeContextCurrent(context);
		AL.createCapabilities(deviceCaps);

		for (int i = 0; i < max_sound_sources_bundles; i++) {
			bundles[i] = new SoundSourcesBundle(max_sources_per_bundle, true, false);
		}

		// setAttenuationModel(AL11.AL_EXPONENT_DISTANCE);
		listener = new SoundListener();
	}

	public SoundSource addSoundToPlay(Vector3f pos, long start, long time,
			SoundBuffer sound) {
		// Finding bundle for this sound
		for (int i = 0; i < max_sound_sources_bundles; i++) {
			SoundSourcesBundle b = bundles[i];

			if (b.getSound() != sound) {
				continue;
			}

			SoundSource free = b.getFree();

			if (free == null) {
				continue;
			}

			free.setBuffer(sound.getBufferId());
			free.setPosition(pos);
			free.setStartTime(start);
			free.setTotalTime(time);

			free.play();

			return free;
		}

		// If there is no such bundle
		for (int i = 0; i < max_sound_sources_bundles; i++) {
			SoundSourcesBundle b = bundles[i];
			if (b.getSound() == sound) {
				return null;
			}
			if (b.getSound() == null) {
				b.setSound(sound);
				return addSoundToPlay(pos, start, time, sound);
			}
		}

		return null;
	}

	public SoundSource addSoundToPlay(Vector3f pos, float pitch, SoundAttenuationConfiguration config, long start,
			long time, SoundBuffer sound) {
		SoundSource s = addSoundToPlay(pos, start, time, sound);
		if (s == null) {
			return null;
		}
		s.setPitch(pitch);
		config.config(s);
		return s;
	}

	@MonoDemeanorUpdate
	public void update() {
		for (int bundle_i = 0; bundle_i < max_sound_sources_bundles; bundle_i++) {
			bundles[bundle_i].update();
		}
		
		updateListenerPosition(Camera.CURRENT_CAMERA);
	}

	public void stopSound(SoundSource s) {
		for (int bundle_i = 0; bundle_i < max_sound_sources_bundles; bundle_i++) {
			bundles[bundle_i].stop(s);
		}
	}

	public SoundListener getListener() {
		return this.listener;
	}

	public void setListener(SoundListener listener) {
		this.listener = listener;
	}

	public void updateListenerPosition(Camera camera) {
		// Update camera matrix with camera data
		cameraMatrix.set(camera.getViewMatrix());

		listener.setPosition(camera.getPosition());
		Vector3f at = new Vector3f();
		cameraMatrix.positiveZ(at).negate();
		Vector3f up = new Vector3f();
		cameraMatrix.positiveY(up);
		listener.setOrientation(at, up);
	}

	public void setAttenuationModel(int model) {
		alDistanceModel(model);
	}

	@MonoDemeanorCleanup
	public void cleanup() {
		for (SoundSourcesBundle b : bundles) {
			b.cleanup();
		}

		if (context != NULL) {
			alcDestroyContext(context);
		}
		if (device != NULL) {
			alcCloseDevice(device);
		}
	}

	public final void logSoundCongestion() {
		String line = "Sound cogestion log [";
		for (int bundle_i = 0; bundle_i < max_sound_sources_bundles; bundle_i++) {
			line += bundles[bundle_i].getCharged() + "/" + max_sources_per_bundle + " ";
		}
		line += "]";
		Console.println(line);
	}
}
