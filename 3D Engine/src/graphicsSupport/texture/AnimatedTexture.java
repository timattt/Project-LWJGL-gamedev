package graphicsSupport.texture;

import java.io.File;

import org.joml.Vector2f;
import org.joml.Vector2i;

public class AnimatedTexture extends Texture {

	// Time
	private long frameTime;
	private long startTime;
	private long totalTime;

	public AnimatedTexture(File in, int numR, int numC, long frameTime) {
		super(in);
		this.num_cols = numC;
		this.num_rows = numR;
		this.frameTime = frameTime;
		totalTime = numC * numR * frameTime;
		startAnimation();
	}

	public void startAnimation() {
		startTime = System.currentTimeMillis();
	}

	private Vector2i getCurrentRowAndCol() {
		long cur_frame_time = (System.currentTimeMillis() - startTime) % totalTime;

		int pos = (int) (float) (((float) cur_frame_time / (float) totalTime) * (float) num_cols * (float) num_rows);
		int x = pos % num_rows;
		int y = pos / num_rows;
		return new Vector2i(x, y);
	}

	@Override
	public Vector2f getFrameOfsets() {
		Vector2i res = getCurrentRowAndCol();
		return new Vector2f((float) ((float) res.x / (float) num_cols), (float) ((float) res.y / (float) num_rows));
	}

	public final long getFrameTime() {
		return frameTime;
	}

	public final long getStartTime() {
		return startTime;
	}

	public final long getTotalTime() {
		return totalTime;
	}

}
