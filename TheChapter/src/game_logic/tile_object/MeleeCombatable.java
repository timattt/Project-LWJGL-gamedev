package game_logic.tile_object;

public interface MeleeCombatable extends Combatable {

	@Override
	public default void doCombat(Combatable opponent) {
		float def_damage = calculateDefenderDamage((TileObject & Combatable) opponent)[4];
		float att_damage = calculateAttackerDamage((TileObject & Combatable) opponent)[4];

		Marker marker = new Marker() {

			private boolean run = false;

			@Override
			public boolean isReady(Combatable comb) {
				return super.isReady(comb) && (run || comb == MeleeCombatable.this);
			}

			@Override
			public void ready() {
				if (index == 2) {
					run = true;
				} else {
					super.ready();
				}
			}

		};

		opponent.waitBeforeCombat((TileObject & Combatable) this, marker);
		waitBeforeCombat((TileObject & Combatable) opponent, marker);

		runToOpponent((TileObject & Combatable) opponent, marker);

		opponent.defend((TileObject & Combatable) this);
		this.attack((TileObject & Combatable) opponent);

		if (opponent instanceof Damageable) {
			((Damageable) opponent).damage(def_damage);
		}
		if (this instanceof Damageable) {
			((Damageable) this).damage(att_damage);
		}

		if (this instanceof Movable) {

			/*
			 * && opponent instanceof Damageable && ((Damageable)
			 * opponent).getCurrentHealth() == 0) { RouteCreationStatus status =
			 * new RouteCreationStatus(); Map map = ((TileObject)
			 * this).getHomeMap(); map.moveObject((TileObject & Movable &
			 * Teamable) this, map.getCoordinates(((TileObject)
			 * opponent).getHomeTile()), status);
			 * 
			 */
			((Movable) this).stop();
		}

	}

	public <T extends TileObject & Combatable> void runToOpponent(T opponent, Marker mar);

	@Override
	public default int getRange() {
		return 1;
	}

	public long getRunTime();
}
