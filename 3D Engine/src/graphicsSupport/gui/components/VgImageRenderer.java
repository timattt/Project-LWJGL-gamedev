package graphicsSupport.gui.components;

import static org.lwjgl.nanovg.NanoVG.nvgBeginPath;
import static org.lwjgl.nanovg.NanoVG.nvgFill;
import static org.lwjgl.nanovg.NanoVG.nvgFillPaint;
import static org.lwjgl.nanovg.NanoVG.nvgImagePattern;
import static org.lwjgl.nanovg.NanoVG.nvgRect;
import static org.lwjgl.nanovg.NanoVG.nvgRoundedRect;

import org.joml.Vector2f;
import org.lwjgl.nanovg.NVGPaint;

import engine.Window;
import graphicsSupport.gui.VgGuiHandler;
import graphicsSupport.texture.Texture;

public class VgImageRenderer {

	// Paint
	private static NVGPaint paint = NVGPaint.calloc();

	public static void renderImage(VgGuiHandler vg_bundle, Texture name, float x, float y, float w, float h) {
		nvgBeginPath(vg_bundle.getVg());
		paint.clear();
		nvgRect(vg_bundle.getVg(), x * Window.instance.getWIDTH(), y * Window.instance.getHEIGHT(), w * Window.instance.getWIDTH(),
				h * Window.instance.getHEIGHT());
		nvgImagePattern(vg_bundle.getVg(), x * Window.instance.getWIDTH(), y * Window.instance.getHEIGHT(),
				w * Window.instance.getWIDTH(), h * Window.instance.getHEIGHT(), 0, name.getVGId(), 1f, paint);
		nvgFillPaint(vg_bundle.getVg(), paint);
		nvgFill(vg_bundle.getVg());
	}

	public static void renderImage(VgGuiHandler vg_bundle, Texture name, float x, float y, float w, float h,
			float alpha) {
		nvgBeginPath(vg_bundle.getVg());
		paint.clear();
		nvgRect(vg_bundle.getVg(), x * Window.instance.getWIDTH(), y * Window.instance.getHEIGHT(), w * Window.instance.getWIDTH(),
				h * Window.instance.getHEIGHT());
		nvgImagePattern(vg_bundle.getVg(), x * Window.instance.getWIDTH(), y * Window.instance.getHEIGHT(),
				w * Window.instance.getWIDTH(), h * Window.instance.getHEIGHT(), 0, name.getVGId(), alpha, paint);
		nvgFillPaint(vg_bundle.getVg(), paint);
		nvgFill(vg_bundle.getVg());
	}

	public static void renderImage(VgGuiHandler vg_bundle, Texture name, Vector2f p0, Vector2f p1) {
		nvgBeginPath(vg_bundle.getVg());
		paint.clear();
		nvgRoundedRect(vg_bundle.getVg(), p0.x * Window.instance.getWIDTH(), p0.y * Window.instance.getHEIGHT(),
				(p1.x - p0.x) * Window.instance.getWIDTH(), (p1.y - p0.y) * Window.instance.getHEIGHT(), 25f);
		nvgImagePattern(vg_bundle.getVg(), p0.x * Window.instance.getWIDTH(), p0.y * Window.instance.getHEIGHT(),
				(p1.x - p0.x) * Window.instance.getWIDTH(), (p1.y - p0.y) * Window.instance.getHEIGHT(), 0, name.getVGId(), 1f,
				paint);
		nvgFillPaint(vg_bundle.getVg(), paint);
		nvgFill(vg_bundle.getVg());
	}

	public static void renderImage(VgGuiHandler vg_bundle, Texture name, float alpha, Vector2f p0, Vector2f p1) {
		nvgBeginPath(vg_bundle.getVg());
		paint.clear();
		nvgRect(vg_bundle.getVg(), p0.x * Window.instance.getWIDTH(), p0.y * Window.instance.getHEIGHT(),
				(p1.x - p0.x) * Window.instance.getWIDTH(), (p1.y - p0.y) * Window.instance.getHEIGHT());
		nvgImagePattern(vg_bundle.getVg(), p0.x * Window.instance.getWIDTH(), p0.y * Window.instance.getHEIGHT(),
				(p1.x - p0.x) * Window.instance.getWIDTH(), (p1.y - p0.y) * Window.instance.getHEIGHT(), 0, name.getVGId(), alpha,
				paint);
		nvgFillPaint(vg_bundle.getVg(), paint);
		nvgFill(vg_bundle.getVg());
	}
}
