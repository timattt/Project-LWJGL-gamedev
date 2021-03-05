package game_logic.gui;

import java.util.LinkedList;

import org.joml.Vector2f;

import game_logic.gui.events.Delegate;
import game_logic.gui.events.Events;
import game_logic.gui.events.EventsCreator;
import graphicsSupport.gui.VgGui;
import graphicsSupport.gui.components.VgButton;

public class NextTurnButton implements VgGui, EventsCreator {

	// Size
	private final float aspectRatio = 2f;
	private final float width_scalar = 0.3f;

	// Next turn button
	private VgButton next_turn_button;

	// Event system
	private LinkedList<Delegate> delegates;

	public NextTurnButton() {
		Vector2f p0 = new Vector2f((1 - width_scalar), 1);
		Vector2f p1 = new Vector2f(1, 1 - width_scalar / aspectRatio);
		next_turn_button = new VgButton(p0, p1, 0.051f, "Next turn!") {
			@Override
			protected void clicked_left() {
				createEvent(delegates, Events.EVENT_HUMAN_PLAYER_TURN_ENDED);
			}
		};

	}

	@Override
	public void prepare(LinkedList<Delegate> list) {
		delegates = list;
	}

	@Override
	public void update() {
		next_turn_button.update();
	}

	@Override
	public boolean contains(Vector2f vec) {
		return next_turn_button.isInComponent(vec);
	}

	@Override
	public void render() {
		next_turn_button.setIndentation(0.07f);
		next_turn_button.render();
	}

}
