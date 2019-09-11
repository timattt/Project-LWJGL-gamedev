package game_logic.tile_object;

import game_logic.map.Team;

public interface Teamable {

	public Team getMasterTeam();

	public void teamTurnFinished();

	public void teamTurnStarted();

	/**
	 * This method is used only in map class.
	 * So DO NOT USE IT IN THE OTHER PLACE!!!
	 * @param pl
	 */
	public void giveToTeam(Team pl);
	
	/**
	 * This method is used only in map class.
	 * So DO NOT USE IT IN THE OTHER PLACE!!!
	 */
	public void removeFromTeam();
	
}
