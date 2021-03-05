package game_logic.tile_object;

public interface Damageable {
	public float getCurrentHealth();

	public void damage(float val);

	public void heal(float val);
}
