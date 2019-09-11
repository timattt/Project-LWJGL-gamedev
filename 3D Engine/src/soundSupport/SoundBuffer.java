package soundSupport;

import static org.lwjgl.openal.AL10.AL_FORMAT_MONO16;
import static org.lwjgl.openal.AL10.AL_FORMAT_STEREO16;
import static org.lwjgl.openal.AL10.alBufferData;
import static org.lwjgl.openal.AL10.alDeleteBuffers;
import static org.lwjgl.openal.AL10.alGenBuffers;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_close;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_get_info;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_get_samples_short_interleaved;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_open_memory;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_stream_length_in_samples;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.LinkedList;

import org.lwjgl.stb.STBVorbisInfo;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import resources.Resource;
import utilities.Utilities;

public class SoundBuffer implements Resource {

	public static final LinkedList<SoundBuffer> ALL_LOADED_SOUNDS = new LinkedList<SoundBuffer>();

	private final int bufferId;

	private ShortBuffer pcm = null;

	private ByteBuffer vorbis = null;

	public final String name;

	public SoundBuffer(File file) {
		name = file.getName().substring(0, file.getName().lastIndexOf("."));
		this.bufferId = alGenBuffers();
		try (STBVorbisInfo info = STBVorbisInfo.malloc()) {
			ShortBuffer pcm = null;
			try {
				pcm = readVorbis(file, 32 * 1024, info);
			} catch (Exception e) {
				e.printStackTrace();
			}

			// Copy to buffer
			alBufferData(bufferId, info.channels() == 1 ? AL_FORMAT_MONO16 : AL_FORMAT_STEREO16, pcm,
					info.sample_rate());
		}

		ALL_LOADED_SOUNDS.add(this);
	}

	public int getBufferId() {
		return this.bufferId;
	}

	private ShortBuffer readVorbis(File resource, int bufferSize, STBVorbisInfo info) throws Exception {
		try (MemoryStack stack = MemoryStack.stackPush()) {
			vorbis = Utilities.ioResourceToByteBuffer(resource, bufferSize);
			IntBuffer error = stack.mallocInt(1);
			long decoder = stb_vorbis_open_memory(vorbis, error, null);
			if (decoder == NULL) {
				throw new RuntimeException("Failed to open Ogg Vorbis file. Error: " + error.get(0));
			}

			stb_vorbis_get_info(decoder, info);

			int channels = info.channels();

			int lengthSamples = stb_vorbis_stream_length_in_samples(decoder);

			pcm = MemoryUtil.memAllocShort(lengthSamples);

			pcm.limit(stb_vorbis_get_samples_short_interleaved(decoder, channels, pcm) * channels);
			stb_vorbis_close(decoder);

			return pcm;
		}
	}

	public static void masscleanup() {
		while (!ALL_LOADED_SOUNDS.isEmpty()) {
			ALL_LOADED_SOUNDS.getFirst().cleanup();
		}
	}

	public static SoundBuffer find(String name) {
		for (SoundBuffer s : ALL_LOADED_SOUNDS) {
			if (s.name.equals(name)) {
				return s;
			}
		}
		return null;
	}

	@Override
	public void cleanup() {

		alDeleteBuffers(this.bufferId);
		if (pcm != null) {
			MemoryUtil.memFree(pcm);
		}

		ALL_LOADED_SOUNDS.remove(this);

	}
}