package soundSupport;

import static org.lwjgl.openal.AL10.AL_BUFFER;
import static org.lwjgl.openal.AL10.AL_GAIN;
import static org.lwjgl.openal.AL10.AL_LOOPING;
import static org.lwjgl.openal.AL10.AL_PITCH;
import static org.lwjgl.openal.AL10.AL_PLAYING;
import static org.lwjgl.openal.AL10.AL_POSITION;
import static org.lwjgl.openal.AL10.AL_SOURCE_RELATIVE;
import static org.lwjgl.openal.AL10.AL_SOURCE_STATE;
import static org.lwjgl.openal.AL10.AL_TRUE;
import static org.lwjgl.openal.AL10.AL_VELOCITY;
import static org.lwjgl.openal.AL10.alDeleteSources;
import static org.lwjgl.openal.AL10.alGenSources;
import static org.lwjgl.openal.AL10.alGetSourcei;
import static org.lwjgl.openal.AL10.alSource3f;
import static org.lwjgl.openal.AL10.alSourcePause;
import static org.lwjgl.openal.AL10.alSourcePlay;
import static org.lwjgl.openal.AL10.alSourceStop;
import static org.lwjgl.openal.AL10.alSourcef;
import static org.lwjgl.openal.AL10.alSourcei;

import org.joml.Vector3f;
import org.lwjgl.openal.AL10;

public class SoundSource {

	private final int sourceId;

	private long startTime = 0l;
	private long totalTime = 0l;
	
	public SoundSource(boolean loop, boolean relative) {
		this.sourceId = alGenSources();

		if (loop) {
			alSourcei(sourceId, AL_LOOPING, AL_TRUE);
		}
		if (relative) {
			alSourcei(sourceId, AL_SOURCE_RELATIVE, AL_TRUE);
		}

	}

	public final long getStartTime() {
		return startTime;
	}

	public final void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public final long getTotalTime() {
		return totalTime;
	}

	public final void setTotalTime(long totalTime) {
		this.totalTime = totalTime;
	}

	public void setBuffer(int bufferId) {
		stop();
		alSourcei(sourceId, AL_BUFFER, bufferId);
	}

	public void setPosition(Vector3f position) {
		alSource3f(sourceId, AL_POSITION, position.x, position.y, position.z);
	}

	public void setSpeed(Vector3f speed) {
		alSource3f(sourceId, AL_VELOCITY, speed.x, speed.y, speed.z);
	}

	public void setGain(float gain) {
		alSourcef(sourceId, AL_GAIN, gain);
	}

	public void setPitch(float gain) {
		alSourcef(sourceId, AL_PITCH, gain);
	}

	public void setProperty(int param, float value) {
		alSourcef(sourceId, param, value);
	}

	public void setReferenceDistance(float v) {
		alSourcef(sourceId, AL10.AL_REFERENCE_DISTANCE, v);
	}
	
	public void setRollOfFactor(float f) {
		alSourcef(sourceId, AL10.AL_ROLLOFF_FACTOR, f);
	}
	
	public void setMaxDistance(float v) {
		alSourcef(sourceId, AL10.AL_MAX_DISTANCE, v);
	}
	
	public void play() {
		alSourcePlay(sourceId);
	}

	public boolean isPlaying() {
		return alGetSourcei(sourceId, AL_SOURCE_STATE) == AL_PLAYING;
	}

	public void pause() {
		alSourcePause(sourceId);
	}

	public void stop() {
		alSourceStop(sourceId);
		startTime = 0l;
		totalTime = 0l;
	}

	public void cleanup() {
		stop();
		alDeleteSources(sourceId);
	}

}