package graphicsSupport.particles;

import java.util.LinkedList;

import graphicsSupport.Item;
import graphicsSupport.texture.Texture;

public interface ParticlesGenerator {

	public LinkedList<Item> getParticles();
	public void update();
	public Texture getTexture();
	
}
