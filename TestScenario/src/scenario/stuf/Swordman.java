/**
 * 
 */
package scenario.stuf;

import org.joml.Vector2f;

import game_logic.map.TileSizeHandler;
import game_logic.map.unit.MeleeMilitaryUnit;
import game_logic.map.unit.actions.UnitAction;
import game_logic.tile_object.Combatable;
import game_logic.tile_object.TileObject;
import graphicsSupport.mesh.Mesh;
import graphicsSupport.texture.Texture;
import scenario.storage.Meshes;
import scenario.storage.Sounds;
import scenario.storage.Textures;
import soundSupport.SoundBuffer;

/**
 * @author timat
 *
 */
public class Swordman extends MeleeMilitaryUnit {

	/**
	 * 
	 */
	public Swordman() {
		setMovePoints(2);
	}

	@Override
	public long getRunTime() {
		return 2000l;
	}

	@Override
	public <T extends TileObject & Combatable> long getAttackTime() {
		return 1000l;
	}

	@Override
	public float getCourage() {
		return 0.3f;
	}

	@Override
	public float getEndurance() {
		return 0.3f;
	}

	@Override
	public float getStrength() {
		return 20f;
	}

	@Override
	public String getName() {
		return "Swordman";
	}

	@Override
	public float getDecorationHeight() {
		return 10f;
	}

	@Override
	public float getDistanceFromEnemy() {
		return TileSizeHandler.instance.getTileSize() / 6f;
	}

	@Override
	public int getAttackParticlesGeneratorIndex() {
		return 1;
	}

	@Override
	public Texture getBadgeTexture() {
		return Textures.LINE_INFANTRY_BADGE;
	}

	@Override
	public long getDamageTime() {
		return 1600l;
	}

	@Override
	public long getTileMoveTime() {
		return 1500l;
	}

	@Override
	public void loadAll(Loader loader) {
		Vector2f[] scals = new Vector2f[3];

		scals[0] = new Vector2f(-0.5f, 0f);
		scals[1] = new Vector2f(0f, 0f);
		scals[2] = new Vector2f(0.5f, 0f);

		loader.setScalar_positions(scals);

		loader.load(Meshes.SWORDMAN_STAND1, UnitAction.HEALING, UnitAction.STAND, UnitAction.WAITING_FOR_COMBAT,
				UnitAction.DEFEND, UnitAction.WAITING);
		loader.load(Meshes.SWORDMAN_STAND2, UnitAction.HEALING, UnitAction.STAND, UnitAction.WAITING_FOR_COMBAT,
				UnitAction.DEFEND, UnitAction.WAITING);

		loader.load(UnitAction.MOVE, new Mesh[][] { Meshes.SWORDMAN_RUN });

		loader.load(UnitAction.RUN_ATTACK, new Mesh[][] { Meshes.SWORDMAN_WALK });

		loader.load(UnitAction.MELEE_ATTACK, new Mesh[][] { Meshes.SWORDMAN_ATTACK });

		loader.load(UnitAction.DAMAGE, new Mesh[][] { Meshes.SWORDMAN_DEATH });

	}

	@Override
	public long getHealingTime() {
		return 0;
	}

	@Override
	public SoundBuffer getMoveSound() {
		return Sounds.MARCHING_SOUND;
	}

}
