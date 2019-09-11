package game_logic.storage;

import org.lwjgl.openal.AL11;

import game_logic.map.TileSizeHandler;
import soundSupport.SoundAttenuationConfiguration;
import soundSupport.SoundManager;
import soundSupport.SoundSource;

public class SoundConfigs {
	public static final SoundAttenuationConfiguration CONFIG1 = new SoundAttenuationConfiguration() {
		@Override
		public void config(SoundSource source) {
			source.setReferenceDistance(TileSizeHandler.instance.getTileSize() / 2f);
			source.setMaxDistance(TileSizeHandler.instance.getTileSize() * 10f);
			SoundManager.instance.setAttenuationModel(AL11.AL_LINEAR_DISTANCE_CLAMPED);
		}
	};
	public static final SoundAttenuationConfiguration CONFIG2 = new SoundAttenuationConfiguration() {
		@Override
		public void config(SoundSource source) {
			source.setReferenceDistance(TileSizeHandler.instance.getTileSize() / 2f);
			source.setMaxDistance(TileSizeHandler.instance.getTileSize() * 3f);
			SoundManager.instance.setAttenuationModel(AL11.AL_LINEAR_DISTANCE_CLAMPED);
		}
	};
}
