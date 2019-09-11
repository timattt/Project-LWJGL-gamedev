package game_logic.gui;

import org.joml.Vector2f;

import game_logic.gui.controllers.selectors.TileSelector;
import game_logic.gui.events.Delegate;
import game_logic.gui.events.Events;
import game_logic.gui.events.EventsListener;
import game_logic.map.Map;
import game_logic.map.Tile;
import game_logic.tile_object.Resourceable;
import graphicsSupport.gui.VgGui;
import graphicsSupport.gui.VgGuiHandler;
import graphicsSupport.gui.components.VgImageRenderer;
import graphicsSupport.gui.components.VgPanel;

public class TileInformationPanel implements VgGui, EventsListener {

	// Selected tile
	private Tile tile;

	// Tile selector
	private TileSelector tile_selector = new TileSelector();

	// Delegate
	private Delegate delegate = new Delegate() {

		@Override
		public void newEvent(int index) {
		}

		@Override
		public void premise(Object premise, int index) {
			if (index == Events.PREMISE_NEW_MAP) {
				map = (Map) premise;
			}
		}

	};

	// Map
	private volatile Map map;

	// Panel
	private Panel panel = new Panel();
	private ResPanel resPanel = new ResPanel();

	public TileInformationPanel() {
	}

	@Override
	public void update() {
		if (map == null) {
			return;
		}
		tile = tile_selector.select(map);
	}

	@Override
	public boolean contains(Vector2f vec) {
		return false;
	}

	@Override
	public void render() {
		panel.render();
		resPanel.render();
	}

	@Override
	public Delegate getDelegate() {
		return delegate;
	}

	private class Panel extends VgPanel {

		public Panel() {
			super(new Vector2f(0.7f, 0.83f), new Vector2f(1f, 0.73f));
		}

		@Override
		public void render() {
			if (tile == null) {
				return;
			}
			VgGuiHandler vg = VgGuiHandler.instance;
			;

			super.render();

			float i = 0;
			for (String info : tile.collectObjectsInformation()) {
				renderText(vg, info, new Vector2f(p0.x + 0.02f, p1.y + 0.005f + i * 0.03f), 0.03f);
				i++;
			}
		}

	}

	private class ResPanel extends VgPanel {

		public ResPanel() {
			super(new Vector2f(0.9f, 0.73f), new Vector2f(1f, 0.47f));
		}

		@Override
		public void render() {
			VgGuiHandler vg = VgGuiHandler.instance;
			if (tile == null || !tile.containsType(Resourceable.class)) {
				return;
			}
			super.render();

			float yy = 0f;
			for (int i = 0; i < tile.getObjects().size(); i++) {
				if (!(tile.getObjects().get(i) instanceof Resourceable)) {
					continue;
				}
				Resourceable resObj = (Resourceable) tile.getObjects().get(i);
				for (int a = 0; a < resObj.getResources().getResources().length; a++) {
					for (int b = 0; b < resObj.getResources().getQuantities()[a]; b++) {
						VgImageRenderer.renderImage(vg, resObj.getResources().getResources()[a].texture,
								p0.x + b * 0.011f, p1.y + yy, 0.03f, 0.03f);
					}
					yy += 0.031f;
				}
			}
		}

	}

}
