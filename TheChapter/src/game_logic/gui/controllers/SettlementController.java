package game_logic.gui.controllers;

import java.util.LinkedList;

import org.joml.Vector2f;
import org.joml.Vector2i;

import game_logic.graphics.StrategyCamera;
import game_logic.gui.controllers.selectors.TileSelector;
import game_logic.map.Tile;
import game_logic.map.settlement.Building;
import game_logic.map.settlement.District;
import game_logic.map.settlement.Settlement;
import game_logic.tile_object.BuildableByCity;
import game_logic.tile_object.Controllable;
import game_logic.tile_object.Resourceable.ResourceHeap;
import game_logic.tile_object.TileObject;
import graphicsSupport.gui.VgGuiHandler;
import graphicsSupport.gui.components.VgButton;
import graphicsSupport.gui.components.VgImageRenderer;
import graphicsSupport.gui.components.VgPanel;

public class SettlementController implements TileObjectController {

	// Settlement
	private Settlement settlement;

	// Panels
	private VgPanel basePanel = new BaseInfoPanel();
	private VgPanel productionButton = new BuildProjectButton();
	private BuildingProjectChooser projectChooser = new BuildingProjectChooser();
	private VgButton addDistrict = new VgButton(new Vector2f(0f, 0.81f), new Vector2f(0.2f, 0.73f), 0, "Add district") {

		@Override
		protected void clicked_left() {
			if (selectTile) {
				selectTile = false;
				StrategyCamera.instance.unconcentrate();
				return;
			} else {
				selectTile = true;
				mode = false;
				StrategyCamera.instance.concentrateOnTile(settlement.getHomeTile());
			}
		}

		@Override
		public void render() {
			if (selectTile && !mode) {
				setG(200);
			} else {
				setG(100);
			}
			super.render();
		}

	};

	// District selector
	private boolean selectTile = false;
	private BuildableByCity project;
	private boolean mode;
	private TileSelector tileSelector = new TileSelector() {

		@Override
		public void rightMouseButtonClickedTile(Tile tile) {
			if (tile == null) {
				return;
			}

			Vector2i coords = settlement.getHomeMap().getCoordinates(tile);

			if (mode) {
				if (settlement.canAddBuildable(project, coords.x, coords.y)) {
					settlement.addProject(project.getClass(), coords.x, coords.y);
					selectTile = false;
					StrategyCamera.instance.unconcentrate();
					projectChooser.visible = false;
				}
			} else {
				if (settlement.canAddDistrict(tile)) {
					settlement.addDistrict(coords.x, coords.y);
					selectTile = false;
					StrategyCamera.instance.unconcentrate();
				}
			}
		}

	};

	public SettlementController() {
		addDistrict.setB(150);
		addDistrict.setTextHeight(0.03f);
		addDistrict.setIndentation(0.044f);
		addDistrict.setIndentationY(0.018f);
	}

	@Override
	public void update() throws InterruptInput {

		productionButton.update();
		projectChooser.update();
		addDistrict.update();
		if (selectTile) {
			tileSelector.selectTile(this.settlement.getHomeMap());
		}
	}

	@Override
	public boolean contains(Vector2f vec) {
		return addDistrict.isInComponent(vec) || basePanel.isInComponent(vec) || productionButton.isInComponent(vec)
				|| projectChooser.isInComponent(vec);
	}

	@Override
	public void render() {
		basePanel.render();
		productionButton.render();
		projectChooser.render();
		addDistrict.render();
	}

	@Override
	public void newTeamTurn() {
	}

	@Override
	public void set(Controllable cont, Controller controller) {
		settlement = (Settlement) cont;
		settlement.refresh();
		projectChooser.refresh();
	}

	private class BaseInfoPanel extends VgPanel {

		public BaseInfoPanel() {
			super(new Vector2f(0f, 1f), new Vector2f(0.2f, 0f));

		}

		@Override
		public void render() {
			super.render();
			VgGuiHandler vg = VgGuiHandler.instance;

			// Resources
			float y = p1.y + 0.005f;
			float x = 0.04f;
			renderText(vg, settlement.getSettlementName(), new Vector2f(x, y), 0.05f);
			y += 0.06f;

			x = 0.01f;

			renderText(vg, "Population: " + settlement.getPopulation(), new Vector2f(x, y), 0.03f);

			renderText(vg, "Resources: ", new Vector2f(x, y += 0.04f), 0.03f);

			x = 0.03f;
			drawResources(settlement.getTotalResources(), vg, x, y += 0.04f, 0.15f);

			y += 0.08f;

			// Districts
			LinkedList<District> ds = settlement.getDistricts();

			x = 0.01f;
			renderText(vg, "Districts: ", new Vector2f(x, y), 0.03f);

			y += 0.04f;
			x = 0.03f;
			int i = 1;
			for (District d : ds) {
				LinkedList<Building> bs = d.getAllBuildingsUnderDistrict();
				renderText(vg, "District " + i + " :", new Vector2f(x, y), 0.03f);
				i++;
				y += 0.04f;
				renderText(vg, "Buildings:", new Vector2f(x + 0.04f, y), 0.03f);

				y += 0.04f;
				for (Building b : bs) {
					renderText(vg, "- " + b.getName(), new Vector2f(x + 0.06f, y), 0.015f);
					y += 0.02f;
				}
				renderText(vg, "Health: " + d.getCurrentHealth(), new Vector2f(x + 0.04f, y), 0.03f);
				y += 0.04f;
			}

			// Income
			x = 0.01f;
			y += 0.02f;
			renderText(vg, "Income: ", new Vector2f(x, y), 0.03f);

			x = 0.03f;
			y += 0.04f;
			renderText(vg, "Production: " + settlement.getProductionIncome(), new Vector2f(x, y), 0.03f);

			y += 0.04f;
			renderText(vg, "Livelihood: " + settlement.getLivelihoodIncome(), new Vector2f(x, y), 0.03f);

			// Build projects
			x = 0.01f;
			y = 0.82f;

			renderText(vg, "Build project: ", new Vector2f(x, y), 0.03f);

		}

		private void drawResources(ResourceHeap heap, VgGuiHandler vg, float x, float y, float max_x) {
			for (int a = 0; a < heap.getResources().length; a++) {
				for (int b = 0; b < heap.getQuantities()[a]; b++) {
					VgImageRenderer.renderImage(vg, heap.getResources()[a].texture, x + b * 0.011f, y + 0.031f * a,
							0.03f, 0.03f);
					renderText(vg, "- " + heap.getQuantities()[a], new Vector2f(max_x, y + 0.031f * a), 0.03f);
				}
			}
		}

	}

	private class BuildProjectButton extends VgButton {

		public BuildProjectButton() {
			super(new Vector2f(0f, 0.95f), new Vector2f(0.2f, 0.87f));
			setB(150);
		}

		@Override
		protected void clicked_left() {
			projectChooser.switchVisibility();
		}

		@Override
		public void render() {
			super.render();
			VgGuiHandler vg = VgGuiHandler.instance;
			;
			if (settlement.getProject() == null) {
				drawRoundedRect(p0.x + 0.01f, p1.y + 0.01f, 0.18f, 0.06f, 0.01f, 200, 0, 0, 200, vg);
			} else {
				renderText(vg, ((TileObject) settlement.getProject()).getName() + " "
						+ settlement.getProjectCompletionDuration(), new Vector2f(0.05f, p1.y + 0.02f), 0.04f);
				drawRoundedRect(p0.x + 0.01f, p1.y + 0.01f, 0.18f * settlement.getComplete(), 0.06f, 0.01f, 0, 100, 0,
						200, vg);
			}
		}

	}

	private class BuildingProjectChooser extends VgPanel {

		// Visible
		private boolean visible = false;

		// Projects
		private LinkedList<Button> available_proj_buttons = new LinkedList<Button>();

		public BuildingProjectChooser() {
			super(new Vector2f(0.2f, 1), new Vector2f(0.35f, 0.4f));
		}

		@Override
		public void render() {
			if (!visible) {
				return;
			}
			super.render();
			VgGuiHandler vg = VgGuiHandler.instance;
			;

			// Old
			float y = p1.y + 0.005f;
			float x = p0.x + 0.01f;
			renderText(vg, "Available projects:", new Vector2f(x, y), 0.03f);

			for (Button b : available_proj_buttons) {
				b.render();
			}

		}

		private void refresh() {
			available_proj_buttons.clear();

			int i = 0;
			for (BuildableByCity bu : settlement.getHomeMap().getScenario().getBuildableObjectsPatterns()) {
				if (settlement.getMasterTeam().projectIsPossible(bu)) {

					available_proj_buttons.add(new Button(i, bu) {
						@Override
						protected void clicked_left() {
							if (selectTile) {
								selectTile = false;
								project = null;
								StrategyCamera.instance.unconcentrate();
								return;
							}
							StrategyCamera.instance.concentrateOnTile(settlement.getHomeTile());
							mode = true;
							selectTile = true;
							project = bu;
						}
					});
					i++;
				}
			}
		}

		public void switchVisibility() {
			visible = !visible;
			project = null;
			selectTile = false;
			StrategyCamera.instance.unconcentrate();
		}

		@Override
		public boolean update() {
			return !visible ? false : (super.update() | updateButtons());
		}

		private boolean updateButtons() {
			for (Button b : available_proj_buttons) {
				if (b.update()) {
					return true;
				}
			}
			return false;
		}

		private class Button extends VgButton {

			// Project
			private final BuildableByCity proj;

			public Button(int i, BuildableByCity b) {
				super(new Vector2f(0.22f, 0.47f + i * 0.06f), new Vector2f(0.33f, 0.52f + i * 0.06f), 0.05f,
						((TileObject) b).getName());
				setTextHeight(0.02f);
				proj = b;
				setIndentationY(0.0115f);
			}

			@Override
			public void render() {
				if (proj == project && mode && selectTile) {
					setG(250);
				} else {
					setG(100);
				}
				super.render();
			}

		}

	}

}
