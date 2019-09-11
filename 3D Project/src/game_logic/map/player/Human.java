package game_logic.map.player;

import java.util.LinkedList;

import engine.Engine;
import engine.monoDemeanor.MonoDemeanor;
import engine.monoDemeanor.MonoDemeanorInstance;
import game_logic.GameProcess;
import game_logic.gui.events.Delegate;
import game_logic.gui.events.Events;
import game_logic.gui.events.EventsBundle;
import game_logic.gui.events.EventsCreator;
import game_logic.gui.events.EventsListener;
import game_logic.gui.events.EventsSystem;
import game_logic.map.Team;

@MonoDemeanor
public class Human extends Player implements EventsBundle, EventsCreator, EventsListener {

	@MonoDemeanorInstance
	public static final Human instance = new Human();
	
	// Turn
	private boolean nextTurn;

	// Events system
	private Delegate delegate = new Delegate() {
		@Override
		public void newEvent(int event) {
			if (event == Events.EVENT_HUMAN_PLAYER_TURN_ENDED) {
				nextTurn = true;
			}
		}

		@Override
		public void premise(Object premise, int index) {
		}
	};
	private LinkedList<Delegate> other_delegates;

	private Human() {
		EventsSystem.instance.addBundle(this);
	}

	@Override
	public void doTurn(Team team) {
		createEvent(other_delegates, Events.EVENT_NEW_HUMAN_PLAYER_TURN);

		while (!nextTurn && Engine.isRunning() && GameProcess.instance.hasMap()) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
			}
		}
		nextTurn = false;
	}

	@Override
	public Delegate getDelegate() {
		return delegate;
	}

	@Override
	public void prepare(LinkedList<Delegate> list) {
		other_delegates = list;
	}

	@Override
	public EventsCreator[] getEventsCreators() {
		return new EventsCreator[] { this };
	}

	@Override
	public EventsListener[] getEventsListeners() {
		return new EventsListener[] { this };
	}
}
