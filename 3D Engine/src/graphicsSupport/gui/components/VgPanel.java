package graphicsSupport.gui.components;

import static org.lwjgl.nanovg.NanoVG.NVG_ALIGN_LEFT;
import static org.lwjgl.nanovg.NanoVG.NVG_ALIGN_TOP;
import static org.lwjgl.nanovg.NanoVG.nvgBeginPath;
import static org.lwjgl.nanovg.NanoVG.nvgCircle;
import static org.lwjgl.nanovg.NanoVG.nvgFill;
import static org.lwjgl.nanovg.NanoVG.nvgFillColor;
import static org.lwjgl.nanovg.NanoVG.nvgFontFace;
import static org.lwjgl.nanovg.NanoVG.nvgFontSize;
import static org.lwjgl.nanovg.NanoVG.nvgRect;
import static org.lwjgl.nanovg.NanoVG.nvgRoundedRect;
import static org.lwjgl.nanovg.NanoVG.nvgText;
import static org.lwjgl.nanovg.NanoVG.nvgTextAlign;

import org.joml.Vector2f;

import controlSupport.MouseHandler;
import engine.Window;
import graphicsSupport.gui.VgGuiHandler;
import graphicsSupport.texture.Texture;

public class VgPanel implements VgComponent {

	// Points
	protected Vector2f p0;
	protected Vector2f p1;
	protected final float width;
	protected final float height;

	// Text
	protected String text = null;
	protected float indentation;
	protected float indentationY = 0.03f;

	// Image
	protected Texture image = null;

	// Color
	protected int r = 50;
	protected int g = 50;
	protected int b = 50;
	protected int a = 255;

	protected int text_r = 10;
	protected int text_g = 10;
	protected int text_b = 10;
	protected int text_a = 255;

	protected float rad = 0.01f;

	protected float textHeight = 0.06f;

	public VgPanel(Vector2f p0, Vector2f p1) {
		this.p0 = p0;
		this.p1 = p1;
		width = Math.abs(p0.x - p1.x);
		height = Math.abs(p0.y - p1.y);
	}

	public VgPanel(Vector2f p0, Vector2f p1, String text, float indentation) {
		this.p0 = p0;
		this.p1 = p1;
		this.text = text;
		this.indentation = indentation;
		width = Math.abs(p0.x - p1.x);
		height = Math.abs(p0.y - p1.y);
	}

	@Override
	public void render() {
		VgGuiHandler vg = VgGuiHandler.instance;
		if (image != null) {
			VgImageRenderer.renderImage(vg, image, p0, p1);
		} else {
			nvgBeginPath(vg.getVg());
			nvgRect(vg.getVg(), p0.x * Window.instance.getWIDTH(), p0.y * Window.instance.getHEIGHT(),
					(p1.x - p0.x) * Window.instance.getWIDTH(), (p1.y - p0.y) * Window.instance.getHEIGHT());
			nvgFillColor(vg.getVg(), vg.createColor(r, g, b, a));
			nvgFill(vg.getVg());
		}
		if (text != null) {
			renderText(vg, text, new Vector2f((p0.x + p1.x) / 2 - indentation, (p0.y + p1.y) / 2 - indentationY));
		}
	}

	/*
	 * protected void render(Vector2f move) {
	 * 
	 * Vector2f p0 = this.p0; Vector2f p1 = this.p1;
	 * 
	 * p0.add(move); p1.add(move);
	 * 
	 * VgGuiHandler vg = VgGuiHandler.instance; if (image != null) {
	 * VgImageRenderer.renderImage(vg, image, p0, p1); } else {
	 * nvgBeginPath(vg.getVg()); nvgRoundedRect(vg.getVg(), p0.x *
	 * Window.instance.getWIDTH(), p0.y * Window.instance.getHEIGHT(), (p1.x -
	 * p0.x) * Window.instance.getWIDTH(), (p1.y - p0.y) *
	 * Window.instance.getHEIGHT(), rad * Window.instance.getWIDTH());
	 * nvgFillColor(vg.getVg(), vg.createColor(r, g, b, a));
	 * nvgFill(vg.getVg()); } if (text != null) { renderText(vg, text, new
	 * Vector2f((p0.x + p1.x) / 2 - indentation, (p0.y + p1.y) / 2 -
	 * indentationY)); }
	 * 
	 * }
	 */
	protected void drawRect(float x, float y, float w, float h, int r, int g, int b, int a, VgGuiHandler vg) {
		if (w == 0 || h == 0) {
			return;
		}
		nvgBeginPath(vg.getVg());
		nvgRect(vg.getVg(), x * Window.instance.getWIDTH(), y * Window.instance.getHEIGHT(),
				w * Window.instance.getWIDTH(), h * Window.instance.getHEIGHT());
		nvgFillColor(vg.getVg(), vg.createColor(r, g, b, a));
		nvgFill(vg.getVg());
	}

	protected void drawRoundedRect(float x, float y, float w, float h, float rad, int r, int g, int b, int a,
			VgGuiHandler vg) {
		if (w == 0 || h == 0) {
			return;
		}
		nvgBeginPath(vg.getVg());
		nvgRoundedRect(vg.getVg(), x * Window.instance.getWIDTH(), y * Window.instance.getHEIGHT(),
				w * Window.instance.getWIDTH(), h * Window.instance.getHEIGHT(), rad * Window.instance.getWIDTH());
		nvgFillColor(vg.getVg(), vg.createColor(r, g, b, a));
		nvgFill(vg.getVg());
	}

	protected void drawCircle(float x, float y, float rad, int r, int g, int b, int a, VgGuiHandler vg) {
		if (rad == 0) {
			return;
		}
		nvgBeginPath(vg.getVg());
		nvgCircle(vg.getVg(), x * Window.instance.getWIDTH(), y * Window.instance.getHEIGHT(),
				rad * Window.instance.getWIDTH());
		nvgFillColor(vg.getVg(), vg.createColor(r, g, b, a));
		nvgFill(vg.getVg());
	}

	protected void renderText(VgGuiHandler vg, String text, Vector2f p0) {
		nvgFontSize(vg.getVg(), textHeight * Window.instance.getHEIGHT());
		nvgFontFace(vg.getVg(), vg.getFontName());
		nvgTextAlign(vg.getVg(), NVG_ALIGN_LEFT | NVG_ALIGN_TOP);
		nvgFillColor(vg.getVg(), vg.createColor(text_r, text_g, text_b, text_a));
		nvgText(vg.getVg(), p0.x * Window.instance.getWIDTH(), p0.y * Window.instance.getHEIGHT(), text);
	}

	public void renderText(VgGuiHandler vg, String text, Vector2f p0, float font_height) {
		nvgFontSize(vg.getVg(), font_height * Window.instance.getHEIGHT());
		nvgFontFace(vg.getVg(), vg.getFontName());
		nvgTextAlign(vg.getVg(), NVG_ALIGN_LEFT | NVG_ALIGN_TOP);
		nvgFillColor(vg.getVg(), vg.createColor(text_r, text_g, text_b, text_a));
		nvgText(vg.getVg(), p0.x * Window.instance.getWIDTH(), p0.y * Window.instance.getHEIGHT(), text);
	}

	public void renderText(VgGuiHandler vg, String text, Vector2f p0, float font_height, int r, int g, int b, int a) {
		nvgFontSize(vg.getVg(), font_height * Window.instance.getHEIGHT());
		nvgFontFace(vg.getVg(), vg.getFontName());
		nvgTextAlign(vg.getVg(), NVG_ALIGN_LEFT | NVG_ALIGN_TOP);
		nvgFillColor(vg.getVg(), vg.createColor(r, g, b, a));
		nvgText(vg.getVg(), p0.x * Window.instance.getWIDTH(), p0.y * Window.instance.getHEIGHT(), text);
	}

	@Override
	public boolean isInComponent(Vector2f vec) {
		return (vec.x < Math.max(p0.x, p1.x)) && (vec.x > Math.min(p0.x, p1.x)) && (vec.y < Math.max(p0.y, p1.y))
				&& (vec.y > Math.min(p0.y, p1.y));
	}

	@Override
	public boolean update() {
		MouseHandler hand = MouseHandler.instance;
		return (hand.leftMouseButtonPressed() || hand.rightMouseButtonPressed())
				&& isInComponent(hand.getMousePosition());
	}

	public final Texture getImage() {
		return image;
	}

	public final void setImage(Texture image) {
		this.image = image;
	}

	public final void setR(int r) {
		this.r = r;
	}

	public final void setG(int g) {
		this.g = g;
	}

	public final void setB(int b) {
		this.b = b;
	}

	public final void setA(int a) {
		this.a = a;
	}

	public final void setText_r(int text_r) {
		this.text_r = text_r;
	}

	public final void setText_g(int text_g) {
		this.text_g = text_g;
	}

	public final void setText_b(int text_b) {
		this.text_b = text_b;
	}

	public final void setText_a(int text_a) {
		this.text_a = text_a;
	}

	public final void setTextHeight(float textHeight) {
		this.textHeight = textHeight;
	}

	public final void setIndentation(float indentation) {
		this.indentation = indentation;
	}

	public final void setRad(float rad) {
		this.rad = rad;
	}

	public final void setIndentationY(float indentationY) {
		this.indentationY = indentationY;
	}

	public void setP0(float x, float y) {
		p0.set(x, y);
	}

	public void setP1(float x, float y) {
		p1.set(x, y);
	}

	public void move(float x, float y) {
		p0.add(x, y);
		p1.add(x, y);
	}

}
