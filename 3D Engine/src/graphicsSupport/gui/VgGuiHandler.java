package graphicsSupport.gui;

import static org.lwjgl.nanovg.NanoVG.nvgBeginFrame;
import static org.lwjgl.nanovg.NanoVG.nvgCreateFontMem;
import static org.lwjgl.nanovg.NanoVG.nvgEndFrame;
import static org.lwjgl.nanovg.NanoVGGL3.NVG_ANTIALIAS;
import static org.lwjgl.nanovg.NanoVGGL3.NVG_STENCIL_STROKES;
import static org.lwjgl.nanovg.NanoVGGL3.nvgCreate;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.nio.ByteBuffer;
import java.util.LinkedList;

import org.joml.Vector2f;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.opengl.GL11;

import debug.LightConfigurationPanel;
import engine.Engine;
import engine.Window;
import engine.monoDemeanor.MonoDemeanor;
import engine.monoDemeanor.MonoDemeanorInit;
import engine.monoDemeanor.MonoDemeanorInstance;
import engine.monoDemeanor.MonoDemeanorPriority;
import engine.monoDemeanor.MonoDemeanorUpdate;
import graphicsSupport.gui.VgGui.InterruptInput;
import utilities.Utilities;

@MonoDemeanor
public class VgGuiHandler {

	@MonoDemeanorInstance
	public static final VgGuiHandler instance = new VgGuiHandler();

	// HUDs
	private LinkedList<VgGui> huds = new LinkedList<VgGui>();

	// Nano vg
	protected long vg;

	protected NVGColor colour;

	private ByteBuffer fontBuffer;

	// Font
	public final String FONT_NAME = "BOLD";

	private VgGuiHandler() {
		if (Engine.getEngineOptions().isEnabledLightDebug()) {
			huds.add(new LightConfigurationPanel());
		}
	}

	@MonoDemeanorInit(priority = MonoDemeanorPriority.HIGH)
	public void init() throws Exception {
		vg = Engine.getEngineOptions().antialiasing() ? nvgCreate(NVG_ANTIALIAS | NVG_STENCIL_STROKES)
				: nvgCreate(NVG_STENCIL_STROKES);
		if (vg == NULL) {
			throw new Exception("Could not init nanovg");
		}
		fontBuffer = Utilities
				.ioResourceToByteBuffer(Engine.getLibraryResourceAsFile("/engine/fonts/OpenSans-Bold.ttf"), 150 * 1024);
		int font = nvgCreateFontMem(vg, FONT_NAME, fontBuffer, 0);
		if (font == -1) {
			throw new Exception("Could not add font");
		}
		colour = NVGColor.create();
	}

	public void render() {
		nvgBeginFrame(getVg(), (int) Window.instance.getWIDTH(), (int) Window.instance.getHEIGHT(), 1);
		for (VgGui hud : huds) {
			hud.render();
		}
		nvgEndFrame(getVg());

		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_STENCIL_TEST);
		GL11.glDepthMask(true);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		if (Engine.getEngineOptions().cull_face()) {
			GL11.glEnable(GL11.GL_CULL_FACE);
			GL11.glCullFace(GL11.GL_BACK);
		}

	}

	public boolean contains(Vector2f vec) {
		for (VgGui hud : huds) {
			if (hud.contains(vec)) {
				return true;
			}
		}
		return false;
	}

	@MonoDemeanorUpdate
	public void update() {
		for (int i = 0; i < huds.size(); i++) {
			try {
				huds.get(i).update();
			} catch (InterruptInput e) {
				return;
			}
		}
	}

	public void addHUD(VgGui... hud) {
		for (int i = 0; i < hud.length; i++) {
			huds.add(hud[i]);
			hud[i].added();
		}
	}

	public void removeHUD(VgGui... hud) {
		for (int i = 0; i < hud.length; i++) {
			huds.remove(hud[i]);
			hud[i].removed();
		}
	}

	public final LinkedList<VgGui> getHuds() {
		return huds;
	}

	public final String getFontName() {
		return FONT_NAME;
	}

	public final long getVg() {
		return vg;
	}

	public final NVGColor getColour() {
		return colour;
	}

	public final ByteBuffer getFontBuffer() {
		return fontBuffer;
	}

	public final NVGColor createColor(int r, int g, int b, int a) {
		colour.r(r / 255.0f);
		colour.g(g / 255.0f);
		colour.b(b / 255.0f);
		colour.a(a / 255.0f);

		return colour;
	}

}
