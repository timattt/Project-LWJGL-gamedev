package soundSupport;

public class SoundSourcesBundle {

	// Sound
	private SoundBuffer sound;

	// Sound sources
	private SoundSource[] sources;

	public SoundSourcesBundle(int size, boolean loop, boolean relative) {
		sources = new SoundSource[size];
		for (int i = 0; i < size; i++) {
			sources[i] = new SoundSource(loop, relative);
		}
	}

	public final SoundBuffer getSound() {
		return sound;
	}

	public final void setSound(SoundBuffer sound) {
		this.sound = sound;
	}

	public final SoundSource[] getSources() {
		return sources;
	}

	public final void setSources(SoundSource[] sources) {
		this.sources = sources;
	}

	public final SoundSource getFree() {
		for (int i = 0; i < sources.length; i++) {
			if (sources[i].isPlaying() || sources[i].getStartTime() != 0l) {
				continue;
			}
			return sources[i];
		}
		return null;
	}

	public final void update() {
		boolean playOne = false;
		for (int i = 0; i < sources.length; i++) {
			SoundSource source = sources[i];

			if (source.isPlaying()) {
				playOne = true;
			}
			if (source.isPlaying() && source.getStartTime() + source.getTotalTime() < System.currentTimeMillis()) {
				source.stop();
			}
		}

		if (!playOne) {
			sound = null;
		}
	}
	
	public final void stop(SoundSource s) {
		for (int i = 0; i < sources.length; i++) {
			if (s == sources[i]) {
				s.stop();
			}
		}
	}
	
	public final void cleanup() {
		for (int i = 0; i < sources.length; i++) {
			sources[i].cleanup();
		}
	}
	
	public final int getCharged() {
		int k = 0;
		for (int i = 0; i < sources.length; i++) {
			if (sources[i].isPlaying() || sources[i].getStartTime() != 0l) {
				k++;
			}
		}
		return k;
	}

}
