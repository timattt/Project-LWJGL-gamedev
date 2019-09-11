package graphicsSupport.gui.components;

import static org.lwjgl.nanovg.NanoVG.NVG_ALIGN_LEFT;
import static org.lwjgl.nanovg.NanoVG.NVG_ALIGN_TOP;
import static org.lwjgl.nanovg.NanoVG.nvgBeginPath;
import static org.lwjgl.nanovg.NanoVG.nvgFill;
import static org.lwjgl.nanovg.NanoVG.nvgFillColor;
import static org.lwjgl.nanovg.NanoVG.nvgFontFace;
import static org.lwjgl.nanovg.NanoVG.nvgFontSize;
import static org.lwjgl.nanovg.NanoVG.nvgText;
import static org.lwjgl.nanovg.NanoVG.nvgTextAlign;

import org.joml.Vector2f;
import org.lwjgl.nanovg.NanoVG;

import controlSupport.MouseHandler;
import engine.Window;
import graphicsSupport.gui.VgGuiHandler;
import graphicsSupport.texture.Texture;

public class VgButton extends VgPanel {

	// Texture
	private Texture button_pressed_texture = null;

	public VgButton(Vector2f p0, Vector2f p1) {
		super(p0, p1);
	}

	public VgButton(Vector2f p0, Vector2f p1, float indentation, String text) {
		super(p0, p1, text, indentation);
	}

	public VgButton(Vector2f p0, Vector2f p1, float indentation, String text, Texture texture) {
		super(p0, p1, text, indentation);
		button_pressed_texture = texture;
	}

	// Button
	protected boolean pressed_left;
	protected boolean pressed_right;

	@Override
	public boolean update() {
		MouseHandler hand = MouseHandler.instance;
		if (!this.isInComponent(hand.getMousePosition())) {
			pressed_left = false;
			pressed_right = false;
			return false;
		}

		// Pressed
		if (hand.leftMouseButtonPressed()) {
			pressed_left = true;
			leftButton_pressed();
		}
		if (hand.rightMouseButtonPressed()) {
			pressed_right = true;
			rightButton_pressed();
		}

		// Clicked
		if (!hand.leftMouseButtonPressed() && pressed_left) {
			clicked_left();
			pressed_left = false;
		}
		if (!hand.rightMouseButtonPressed() && pressed_right) {
			clicked_right();
			pressed_right = false;
		}

		return true;
	}

	@Override
	public void renderText(VgGuiHandler vg, String text, Vector2f p0) {
		if (pressed_left || pressed_right) {
			nvgFontSize(vg.getVg(), textHeight * Window.instance.getHEIGHT());
			nvgFontFace(vg.getVg(), vg.getFontName());
			nvgTextAlign(vg.getVg(), NVG_ALIGN_LEFT | NVG_ALIGN_TOP);
			nvgFillColor(vg.getVg(), vg.createColor(255, 200, 200, 255));
			nvgText(vg.getVg(), p0.x * Window.instance.getWIDTH(), p0.y * Window.instance.getHEIGHT(), text);
		} else {
			super.renderText(vg, text, p0);
		}
	}

	protected void leftButton_pressed() {
	}

	protected void rightButton_pressed() {
	}

	protected void clicked_left() {
	}

	protected void clicked_right() {
	}

	@Override
	public void render() {
		VgGuiHandler vg = VgGuiHandler.instance;
		if (pressed_left || pressed_right) {
			if (button_pressed_texture != null) {
				VgImageRenderer.renderImage(vg, this.button_pressed_texture, p0, p1);
			} else {
				nvgBeginPath(vg.getVg());
				NanoVG.nvgRect(vg.getVg(), p0.x * Window.instance.getWIDTH(), p0.y * Window.instance.getHEIGHT(),
						(p1.x - p0.x) * Window.instance.getWIDTH(), (p1.y - p0.y) * Window.instance.getHEIGHT());
				nvgFillColor(vg.getVg(),
						vg.createColor((this.pressed_left || this.pressed_right ? 50 : 0) + 100, 100, 100, 255));
				nvgFill(vg.getVg());
			}
			if (text != null) {
				renderText(vg, text, new Vector2f((p0.x + p1.x) / 2 - indentation, (p0.y + p1.y) / 2 - indentationY));
			}
		} else {
			super.render();
		}

	}

	public final void setImage_pressed(Texture button_pressed_texture) {
		this.button_pressed_texture = button_pressed_texture;
	}

}
