/**
 * 
 */
package graphicsSupport.gui.components;

import org.joml.Vector2f;

import controlSupport.MouseHandler;
import graphicsSupport.gui.VgGuiHandler;

/**
 * @author timat
 *
 */
public class VgScrollPanel extends VgPanel {

	private float scrollScalar = 0f;

	/**
	 * @param p0
	 * @param p1
	 */
	public VgScrollPanel(Vector2f p0, Vector2f p1) {
		super(p0, p1);
	}

	/**
	 * @param p0
	 * @param p1
	 * @param text
	 * @param indentation
	 */
	public VgScrollPanel(Vector2f p0, Vector2f p1, String text, float indentation) {
		super(p0, p1, text, indentation);
	}

	@Override
	public void render() {
		super.render();

		drawRect(p0.x + width / 10f, p0.y + height / 10f, width / 10f * 8f, height / 10f * 8f, r, g, b, a,
				VgGuiHandler.instance);
		drawRect(p0.x + width / 10f + width / 10f * 8f * scrollScalar - width / 24f,
				p0.y + height / 10f,
				width / 12f,
				height / 10f * 8f,
				250, g, b, a, VgGuiHandler.instance);
	}

	public final float getScrollScalar() {
		return scrollScalar;
	}

	@Override
	public boolean update() {
		MouseHandler h = MouseHandler.instance;
		if (h.leftMouseButtonPressed() && isInScroll(h.getMousePosition())) {
			scrollScalar = (h.getMousePosition().x - p0.x - width / 10f) / (width / 10f * 8f);
			return true;
		}
		return false;
	}

	private boolean isInScroll(Vector2f vec) {
		return p0.x + width / 10f < vec.x && vec.x < p1.x - width / 10f && p0.y + height / 10f < vec.y
				&& vec.y < p1.y - height / 10f;
	}

	public final void setScrollScalar(float scrollScalar) {
		this.scrollScalar = scrollScalar;
	}

}
