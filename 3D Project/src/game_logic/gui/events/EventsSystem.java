package game_logic.gui.events;

import java.util.LinkedList;

import engine.monoDemeanor.MonoDemeanor;
import engine.monoDemeanor.MonoDemeanorInstance;
import engine.monoDemeanor.MonoDemeanorUpdate;

@MonoDemeanor
public class EventsSystem {

	@MonoDemeanorInstance
	public static final EventsSystem instance = new EventsSystem();
	
	private LinkedList<EventsBundle> bundles = new LinkedList<EventsBundle>();
	private LinkedList<Delegate> delegates = new LinkedList<Delegate>();
	
	private EventsSystem() {
	}
	
	@MonoDemeanorUpdate
	public void updateEventsSystem() {
		if (bundles.isEmpty()) {
			return;
		}
		
		// Event system finding {delegates}
		// Checking Bundles
		for (EventsBundle bundle : bundles) {
			if (bundle.getEventsListeners() == null) {
				continue;
			}
			for (EventsListener arg : bundle.getEventsListeners()) {
				delegates.add(arg.getDelegate());
			}
		}

		// Events system {prepare} initializer
		// Checking Bundles
		for (EventsBundle bundle : bundles) {
			if (bundle.getEventsCreators() == null) {
				continue;
			}
			for (EventsCreator arg : bundle.getEventsCreators()) {
				arg.prepare(delegates);
			}
		}
		
		bundles.clear();
	}

	public final void addBundle(EventsBundle bundle) {
		bundles.add(bundle);
	}
}
