package scenario.stuf;

import org.joml.Vector2f;
import org.joml.Vector3f;

import game_logic.map.unit.RangeMilitaryUnit;
import game_logic.map.unit.actions.UnitAction;
import graphicsSupport.mesh.Mesh;
import graphicsSupport.texture.Texture;
import scenario.storage.Meshes;
import scenario.storage.Sounds;
import scenario.storage.Textures;
import soundSupport.SoundBuffer;

public class LineInfantry extends RangeMilitaryUnit {

	public LineInfantry() {
		setMovePoints(3);
	}

	@Override
	public int getVisibleRange() {
		return 3;
	}

	@Override
	public float getShellScale() {
		return 1.5f;
	}

	@Override
	public void loadAll(Loader loader) {

		Vector2f[] scals = new Vector2f[5];

		scals[0] = new Vector2f(0.66f, 0f);
		scals[1] = new Vector2f(0.33f, 0f);
		scals[2] = new Vector2f(0f, 0f);
		scals[3] = new Vector2f(-0.33f, 0f);
		scals[4] = new Vector2f(-0.66f, 0f);
		/*
		 * scals[5] = new Vector2f(-0.66f, 0); scals[6] = new Vector2f(-0.33f,
		 * 0); scals[7] = new Vector2f(0f, 0); scals[8] = new Vector2f(0.33f,
		 * 0); scals[9] = new Vector2f(0.66f, 0);
		 * 
		 * scals[10] = new Vector2f(-0.66f, 0.5f); scals[11] = new
		 * Vector2f(-0.33f, 0.5f); scals[12] = new Vector2f(0f, 0.5f); scals[13]
		 * = new Vector2f(0.33f, 0.5f); scals[14] = new Vector2f(0.66f, 0.5f);
		 */

		loader.setScalar_positions(scals);

		loader.load(UnitAction.HEALING, new Mesh[][] { Meshes.LINE_INFANTRY_STANDING1 });
		loader.load(UnitAction.STAND, new Mesh[][] { Meshes.LINE_INFANTRY_STANDING1 });
		loader.load(UnitAction.STAND, new Mesh[][] { Meshes.LINE_INFANTRY_STANDING2 });
		loader.load(UnitAction.MOVE, new Mesh[][] { Meshes.LINE_INFANTRY_WALKING });
		loader.load(UnitAction.RANGE_ATTACK, new Mesh[][] { Meshes.LINE_INFANTRY_SHOOTING });
		loader.load(UnitAction.DAMAGE, new Mesh[][] { Meshes.LINE_INFANTRY_DEATH1 });
		loader.load(UnitAction.DAMAGE, new Mesh[][] { Meshes.LINE_INFANTRY_DEATH2 });
		loader.load(UnitAction.DAMAGE, new Mesh[][] { Meshes.LINE_INFANTRY_DEATH3 });
		loader.load(UnitAction.WAITING_FOR_COMBAT, new Mesh[][] { Meshes.LINE_INFANTRY_SHOOTING_READY });
		loader.load(UnitAction.DEFEND, new Mesh[][] { Meshes.LINE_INFANTRY_SHOOTING_READY });
		loader.load(UnitAction.WAITING, new Mesh[][] { Meshes.LINE_INFANTRY_STANDING1 });
		loader.load(UnitAction.WAITING, new Mesh[][] { Meshes.LINE_INFANTRY_STANDING2 });
	}

	@Override
	public String getName() {
		return "Line infantry";
	}

	@Override
	public float getStrength() {
		return 15;
	}

	@Override
	public float getCourage() {
		return 0.4f;
	}

	@Override
	public Mesh[] loadShells() {
		return Meshes.LINE_INFANTRY_BULLET;
	}

	@Override
	public long getShellFlightTime() {
		return 1200;
	}

	@Override
	public Vector3f getShellStartLocation() {
		return new Vector3f(-0.148f, 1.1f, 1.1f);
	}

	@Override
	public int getRange() {
		return 2;
	}

	@Override
	public long getAttackTime() {
		return getShellFlightTime();
	}

	@Override
	public long getDamageTime() {
		return 1100 + 1450;
	}

	@Override
	public Texture getBadgeTexture() {
		return Textures.LINE_INFANTRY_BADGE;
	}

	@Override
	public long getTileMoveTime() {
		return 1050 * 2;
	}

	@Override
	public float getEndurance() {
		return 0.4f;
	}

	@Override
	public long getHealingTime() {
		return 0;
	}

	@Override
	public int getAttackParticlesGeneratorIndex() {
		return 0;
	}

	@Override
	public int getBulletFrapperParticleGeneratorIndex() {
		return 2;
	}

	@Override
	public SoundBuffer getShootSound() {
		return Sounds.LINE_INFANTRY_SOUND_SHOOT;
	}

	@Override
	public SoundBuffer getMoveSound() {
		return Sounds.MARCHING_SOUND;
	}

	@Override
	public SoundBuffer getTargetHitSound() {
		return Sounds.EXPLOSION_SOUND;
	}

	@Override
	public float getDecorationHeight() {
		return 40f;
	}

}
