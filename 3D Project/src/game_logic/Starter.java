package game_logic;

import engine.Engine;

public class Starter {

	public static void main(String[] args) {
		Engine.initTEngine(Starter.class, "/Options.opt", GameOptions.class, "/GameOptions.opt");
	}
}
