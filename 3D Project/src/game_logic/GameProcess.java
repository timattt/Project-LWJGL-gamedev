package game_logic;

import java.util.LinkedList;

import engine.Engine;
import engine.monoDemeanor.MonoDemeanor;
import engine.monoDemeanor.MonoDemeanorCleanup;
import engine.monoDemeanor.MonoDemeanorInit;
import engine.monoDemeanor.MonoDemeanorInstance;
import engine.monoDemeanor.MonoDemeanorPriority;
import engine.monoDemeanor.MonoDemeanorUpdate;
import game_logic.gui.InGameMenu;
import game_logic.gui.NextTurnButton;
import game_logic.gui.TileInformationPanel;
import game_logic.gui.cardsManager.CardsManager;
import game_logic.gui.controllers.Controller;
import game_logic.gui.events.Delegate;
import game_logic.gui.events.Events;
import game_logic.gui.events.EventsBundle;
import game_logic.gui.events.EventsCreator;
import game_logic.gui.events.EventsListener;
import game_logic.gui.events.EventsSystem;
import game_logic.map.Map;
import game_logic.map.Team;
import game_logic.map.player.Human;
import graphicsSupport.gui.VgGuiHandler;
import utilities.Console;

@MonoDemeanor
public class GameProcess implements Runnable, EventsBundle, EventsCreator {

	@MonoDemeanorInstance
	public static final GameProcess instance = new GameProcess();

	// Visibility updater
	private Thread mapVisibilityUpdater = new Thread("Visibility updater") {
		@Override
		public void run() {
			while (Engine.isRunning()) {
				synchronized (this) {
					if (map != null) {
						try {
							map.updateAllVisibility();
						} catch (Exception e) {
						}
					}
				}
			}
		}
	};

	// Map manager
	private LinkedList<Delegate> delegates;

	// Map
	private Map map;

	// Thread
	private Thread gameProcess;

	// GUIs
	protected NextTurnButton next_turn_button;
	protected Controller controller;
	protected TileInformationPanel tile_info_panel;
	protected InGameMenu menu;
	protected CardsManager cardsManager = new CardsManager();

	private GameProcess() {
	}

	@MonoDemeanorInit(priority = MonoDemeanorPriority.LOW)
	public void init() {

		gameProcess = new Thread(this, "Game process");
		EventsSystem.instance.addBundle(this);

		// Initialize HUDs
		next_turn_button = new NextTurnButton();
		controller = new Controller();
		tile_info_panel = new TileInformationPanel();
		menu = new InGameMenu();

		gameProcess.start();
		mapVisibilityUpdater.start();

	}

	@Override
	public void run() {
		// Turns
		try {
			while (Engine.isRunning()) {
				if (map == null) {
					Thread.sleep(1000);
					continue;
				}

				try {
					map.turn_started();

					// Turn
					for (Team team : map.getTeams()) {
						if (!Engine.isRunning()) {
							return;
						}
						team.turnStarted();
						team.doTurn();
						team.turnFinished();
					}

					if (map == null) {
						continue;
					}
					
					map.endTurn();
					map.turnNumber++;
					Console.println("Turn: " + map.turnNumber);
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@MonoDemeanorUpdate
	public void updateMap() {
		if (map != null) {
			map.updateGraphics();
		}
	}

	public final void setMap(Map map, int playerTeamNumber) {
		sendPremise(delegates, map, Events.PREMISE_NEW_MAP);
		this.map = map;

		map.getTeams().get(playerTeamNumber).getPlayers().clear();
		map.boundToTeam(map.getTeams().get(playerTeamNumber));

		Human.instance.registerToTeam(map.getTeams().get(playerTeamNumber));

		VgGuiHandler.instance.addHUD(controller, next_turn_button, tile_info_panel, menu, cardsManager);
	}

	@MonoDemeanorCleanup
	public final void deleteMap() {
		try {
			if (map != null) {
				map.cleanup_remove();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		VgGuiHandler.instance.removeHUD(controller, next_turn_button, tile_info_panel, menu, cardsManager);
		map = null;
	}

	@Override
	public EventsCreator[] getEventsCreators() {
		return new EventsCreator[] { this, this.next_turn_button };
	}

	@Override
	public EventsListener[] getEventsListeners() {
		return new EventsListener[] { this.controller, this.tile_info_panel };
	}

	@Override
	public void prepare(LinkedList<Delegate> list) {
		delegates = list;
	}

	public final boolean hasMap() {
		return map != null;
	}

}
