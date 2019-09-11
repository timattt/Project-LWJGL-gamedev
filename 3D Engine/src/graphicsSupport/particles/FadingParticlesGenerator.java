package graphicsSupport.particles;

import java.util.LinkedList;

import org.joml.Vector3f;

import graphicsSupport.Item;
import graphicsSupport.mesh.Mesh;
import graphicsSupport.texture.Texture;

public class FadingParticlesGenerator implements ParticlesGenerator {

	// Particles
	private final LinkedList<Item> particles = new LinkedList<Item>();
	/**
	 * [0] - start [1] - total
	 */
	private final LinkedList<Long[]> time = new LinkedList<Long[]>();
	private final LinkedList<Float[]> scalars = new LinkedList<Float[]>();

	private final Mesh[] mesh;
	private final Texture texture;

	public FadingParticlesGenerator(Mesh[] mesh, Texture texture) {
		this.mesh = mesh;
		this.texture = texture;
	}

	@Override
	public LinkedList<Item> getParticles() {
		return particles;
	}

	@Override
	public void update() {
		for (int i = 0; i < particles.size(); i++) {
			float scalar;
			if (time.get(i)[0] > System.currentTimeMillis()) {
				particles.get(i).setScale(0);
			} else {
				scalar = (float) (System.currentTimeMillis() - time.get(i)[0]) / (float) time.get(i)[1];
				particles.get(i).setAlpha(1f - scalar);
				particles.get(i)
						.setScale(this.scalars.get(i)[0] + (this.scalars.get(i)[1] - this.scalars.get(i)[0]) * scalar);
			}
			if (time.get(i)[0] + time.get(i)[1] < System.currentTimeMillis()) {
				particles.remove(particles.get(i));
				time.remove(time.get(i));
			}
		}

	}

	public void addParticle(Vector3f loc, long startTime, long total, float startScal, float endScal) {
		synchronized (particles) {
			Item part = new Item(mesh).setPosition(loc);
			part.setBillboard(true);
			particles.add(part);
			this.time.add(new Long[] { (Long) (startTime), (Long) total });
			scalars.add(new Float[] { startScal, endScal });
		}
	}

	@Override
	public Texture getTexture() {
		return texture;
	}

}
