package game_logic.tile_object;

public interface RangeCombatable extends Combatable {

	@Override
	public default void doCombat(Combatable opponent) {
		float def_damage = calculateDefenderDamage((TileObject & Combatable) opponent)[4];
		
		Marker marker = new Marker();
		
		opponent.waitBeforeCombat((TileObject & Combatable) this, marker);
		waitBeforeCombat((TileObject & Combatable) opponent, marker);

		opponent.defend((TileObject & Combatable) this);
		this.attack((TileObject & Combatable) opponent);

		if (opponent instanceof Damageable) {
			((Damageable) opponent).damage(def_damage);
		}

		if (this instanceof Movable) {
			((Movable) this).stop();
		}

	}

	@Override
	public default <T extends TileObject & Combatable> float[] calculateAttackerDamage(T opponent) {
		float[] result = Combatable.super.calculateAttackerDamage(opponent);
		result[4] = 0;
		return result;
	}

}
