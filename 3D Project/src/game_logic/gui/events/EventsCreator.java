package game_logic.gui.events;

import java.util.LinkedList;

public interface EventsCreator {
	public void prepare(LinkedList<Delegate> list);
	public default void createEvent(LinkedList<Delegate> list, int index) {
		for(Delegate del : list) {
			del.newEvent(index);
		}
	}
	public default void sendPremise(LinkedList<Delegate> list, Object premise, int index) {
		for(Delegate del : list) {
			del.premise(premise, index);
		}
	}
}
