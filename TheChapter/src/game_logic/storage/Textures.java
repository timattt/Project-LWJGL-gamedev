package game_logic.storage;

import engine.Engine;
import graphicsSupport.texture.AnimatedTexture;
import graphicsSupport.texture.Texture;

public class Textures {

	public static final Texture PLAINS_TERRAIN = Texture.find("Plains");

	public static final Texture PLAINS_TERRAIN_NM = Texture.find("PlainsNM");
	
	public static final Texture TILE_MAP_GRID = new Texture(Engine.getResourceAsFile("/textures/terrain/TileMapGrid.png"));
	
	public static final Texture GRAVEL_ROAD = Texture.find("GravelRoad");
	
	public static final Texture ATTACK_SIGN = new Texture(Engine.getResourceAsFile("/textures/Attack_sign.png"));

	public static final Texture PARTICLE_SMOKE = new AnimatedTexture(Engine.getResourceAsFile("/textures/Particles/Smoke.png"), 4, 4, 500);

	public static final Texture PARTICLE_SPARKLES = new Texture(Engine.getResourceAsFile("/textures/Particles/Sparkles.png"));

	public static final Texture PARTICLE_EXPLOSION = new Texture(Engine.getResourceAsFile("/textures/Particles/Explosion.png"));

	public static final Texture ATTACK_BORDER = new Texture(Engine.getResourceAsFile("/textures/AttackSelectionFrame.png"));
}