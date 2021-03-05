package game_logic.map.player;

import game_logic.map.Team;

public abstract class Player {
	
	// Name
	private String name;
	
	// Team 
	private Team team;
	
	public Player() {
	}

	public abstract void doTurn(Team team);

	public final String getName() {
		return name;
	}

	public final Player setName(String name) {
		this.name = name;
		return this;
	}

	public final void registerToTeam(Team team) {
		this.team = team;
		team.addPlayerInTeam(this);
	}

	public final Team getTeam() {
		return team;
	}
	
}
