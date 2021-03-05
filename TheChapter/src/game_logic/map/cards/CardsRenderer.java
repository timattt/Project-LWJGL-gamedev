/**
 * 
 */
package game_logic.map.cards;

import java.util.HashMap;
import java.util.LinkedList;

import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import game_logic.map.badges.BadgeRenderer;
import game_logic.storage.Meshes;
import graphicsSupport.ExternalRenderer;
import graphicsSupport.camera.Camera;
import graphicsSupport.shaders.UniformedShaderProgram;
import graphicsSupport.texture.Texture;

/**
 * @author timat
 *
 */
public class CardsRenderer implements ExternalRenderer {

	// Cards
	private final HashMap<Texture, LinkedList<Card>> cards = new HashMap<Texture, LinkedList<Card>>();

	public CardsRenderer() {
	}

	@Override
	public void render(Camera cam) {
		UniformedShaderProgram shader = BadgeRenderer.shader;

		shader.setUniform("alpha", 1f);
		shader.setUniform("color", new Vector3f(0f, 0f, 0f));

		// Enabling VAO
		GL30.glBindVertexArray(Meshes.CARD[0].vaoId);

		// Enabling vertex VBO
		GL20.glEnableVertexAttribArray(0);

		// Enabling normal VBO
		GL20.glEnableVertexAttribArray(1);

		GL20.glEnableVertexAttribArray(3);

		GL13.glActiveTexture(GL13.GL_TEXTURE0);

		synchronized (this) {
			for (Texture text : cards.keySet()) {
				LinkedList<Card> cs = cards.get(text);
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, text.getGLId());
				shader.setUniform("frag_texture", text);

				for (Card card : cs) {
					
					shader.setUniform("modelMatrix", card.getModelMatrix());
					GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, Meshes.CARD[0].vertexQuantity);
				}
			}
		}

		GL20.glDisableVertexAttribArray(3);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

		// Disable normals VBO
		GL20.glDisableVertexAttribArray(1);

		// Disabling vertex VBO
		GL20.glDisableVertexAttribArray(0);

		// Disabling VAO
		GL30.glBindVertexArray(0);

	}

	@Override
	public UniformedShaderProgram getShader() {
		return BadgeRenderer.shader;
	}

	public void add(Card arg0) {
		if (!cards.containsKey(arg0.getTexture())) {
			cards.put(arg0.getTexture(), new LinkedList<Card>());
		}
		cards.get(arg0.getTexture()).add(arg0);
	}

	public void remove(Card arg0) {
		cards.get(arg0.getTexture()).remove(arg0);
	}

}
