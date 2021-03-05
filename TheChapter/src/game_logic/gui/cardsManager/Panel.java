/**
 * 
 */
package game_logic.gui.cardsManager;

import org.joml.Vector2f;
import org.joml.Vector2i;

import controlSupport.MouseHandler;
import game_logic.gui.controllers.selectors.TileSelector;
import game_logic.map.Map;
import game_logic.map.Team;
import game_logic.map.Tile;
import game_logic.map.cards.Card;
import game_logic.map.player.Human;
import graphicsSupport.gui.VgGuiHandler;
import graphicsSupport.gui.components.VgImageRenderer;
import graphicsSupport.gui.components.VgPanel;
import graphicsSupport.texture.Texture;

/**
 * @author timat
 *
 */
class Panel extends VgPanel {

	// Mode
	private Card cardToPlace;
	private boolean selectCard = false;

	// TileSelector
	private TileSelector tileSelector = new TileSelector() {

		@Override
		public void rightMouseButtonClickedTile(Tile tile) {
			if (!selectCard || !cardToPlace.canBePutted(tile)) {
				selectCard = false;
				cardToPlace = null;
				return;
			}

			Team team = Human.instance.getTeam();
			Map map = team.getMap();

			Vector2i coords = map.getCoordinates(tile);
			team.placeCard(cardToPlace, coords.x, coords.y);

			cardToPlace = null;
			selectCard = false;

		}

		@Override
		public void leftMouseButtonClickedTile(Tile tile) {
			if (!selectCard || !cardToPlace.canBePutted(tile)) {
				selectCard = false;
				cardToPlace = null;
			}
		}

	};

	public Panel() {
		super(new Vector2f(0.8f, 0f), new Vector2f(1f, 0.71f));
	}

	@Override
	public void render() {
		super.render();

		float y = 0f;
		for (Card card : Human.instance.getTeam().getCards()) {
			drawRoundedRect(0.8f, y, 0.2f, 0.05f, 0.01f, 100, cardToPlace == card ? 200 : 100, 200, 200,
					VgGuiHandler.instance);

			renderText(VgGuiHandler.instance, card.getCardName(), new Vector2f(0.81f, y + 0.015f), 0.02f);
			VgImageRenderer.renderImage(VgGuiHandler.instance, Texture.find(card.getTexture().name), 0.95f, y + 0.005f, 0.03f,
					0.04f);
			y += 0.06f;
		}

	}

	@Override
	public boolean update() {
		tileSelector.selectTile(Human.instance.getTeam().getMap());
		Vector2f mouse = MouseHandler.instance.getMousePosition();

		if (mouse.x < 0.8f || !MouseHandler.instance.leftMouseButtonPressed()) {
			return false;
		}

		int index = (int) (mouse.y / 0.06f);

		if (index >= Human.instance.getTeam().getCards().size()) {
			cardToPlace = null;
			selectCard = false;
			return false;
		}

		Card selectedCard = Human.instance.getTeam().getCards().get(index);
		if (selectedCard != cardToPlace) {
			cardToPlace = selectedCard;
			selectCard = false;
			return true;
		} else {
			selectCard = true;
		}

		return true;
	}

}
