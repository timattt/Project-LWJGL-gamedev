package graphicsSupport.gui;

import org.joml.Vector2f;

public interface VgGui {
	public void update() throws InterruptInput;

	public boolean contains(Vector2f vec);

	public void render();

	public default void removed() {
	}

	public default void added() {
	}

	public static void interruptInput() throws InterruptInput {
		throw new InterruptInput();
	}

	public class InterruptInput extends Exception {
		/**
		 * 
		 */
		private static final long serialVersionUID = 5652806958444260648L;

		public InterruptInput() {
			super("Interrupt input");
		}

	}
}
