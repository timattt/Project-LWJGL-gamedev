/**
 * 
 */
package debug;

import org.joml.Vector2f;

import graphicsSupport.Universe;
import graphicsSupport.effects.DirectionalLight;
import graphicsSupport.gui.VgGui;
import graphicsSupport.gui.components.VgButton;
import graphicsSupport.gui.components.VgPanel;
import graphicsSupport.gui.components.VgScrollPanel;
import utilities.Console;

/**
 * @author timat
 *
 */
public class LightConfigurationPanel implements VgGui {

	// Panels
	private VgPanel background = new VgPanel(new Vector2f(0, 0), new Vector2f(0.3f, 0.5f));
	private VgScrollPanel ambientLightPanel = new VgScrollPanel(new Vector2f(0, 0), new Vector2f(0.3f, 0.1f),
			"Ambient light color", 0f);
	private VgScrollPanel specularPowerPanel = new VgScrollPanel(new Vector2f(0f, 0.1f), new Vector2f(0.3f, 0.2f),
			"Specular power", 0f);
	private VgScrollPanel dirLightColorPowerPanel = new VgScrollPanel(new Vector2f(0f, 0.2f), new Vector2f(0.3f, 0.3f),
			"Dir light color", 0f);
	private VgScrollPanel dirLightIntPanel = new VgScrollPanel(new Vector2f(0f, 0.3f), new Vector2f(0.3f, 0.4f),
			"Dir light intensity", 0f);
	private VgButton print = new VgButton(new Vector2f(0.1f, 0.42f), new Vector2f(0.2f, 0.48f), 0f, "Print") {

		@Override
		protected void clicked_left() {
			Universe u = Universe.instance;
			Console.println("Ambient light: " + u.getAmbientLight().x + ", " + u.getAmbientLight().y + ", "
					+ u.getAmbientLight().z + ";");
			Console.println("Specular power: " + u.getSpecularPower() + ";");
			Console.println("Directional light color: " + DirectionalLight.instance.getColor().x + ", "
					+ DirectionalLight.instance.getColor().y + ", " + DirectionalLight.instance.getColor().z + ";");
			Console.println("Directional light intensity: " + DirectionalLight.instance.getIntensity() + ";");
		}

	};

	public LightConfigurationPanel() {
		ambientLightPanel.setTextHeight(0.03f);
		specularPowerPanel.setTextHeight(0.03f);
		dirLightColorPowerPanel.setTextHeight(0.03f);
		dirLightIntPanel.setTextHeight(0.03f);
		print.setTextHeight(0.03f);
	}

	@Override
	public void update() throws InterruptInput {
		ambientLightPanel.setScrollScalar(Universe.instance.getAmbientLight().x);
		specularPowerPanel.setScrollScalar(Universe.instance.getSpecularPower() / 15f);
		dirLightColorPowerPanel.setScrollScalar(DirectionalLight.instance.getColor().x);
		dirLightIntPanel.setScrollScalar(DirectionalLight.instance.getIntensity() / 10f);
		
		ambientLightPanel.update();
		background.update();
		specularPowerPanel.update();
		dirLightColorPowerPanel.update();
		dirLightIntPanel.update();
		print.update();

		float scalar = this.ambientLightPanel.getScrollScalar();
		Universe.instance.getAmbientLight().set(scalar, scalar, scalar);
		
		scalar = specularPowerPanel.getScrollScalar();
		Universe.instance.setSpecularPower(15f * scalar);

		scalar = dirLightColorPowerPanel.getScrollScalar();
		DirectionalLight.instance.getColor().set(scalar, scalar, scalar);

		scalar = dirLightIntPanel.getScrollScalar();
		DirectionalLight.instance.setIntensity(10f * scalar);
	}

	@Override
	public boolean contains(Vector2f vec) {
		return background.isInComponent(vec) || ambientLightPanel.isInComponent(vec);
	}

	@Override
	public void render() {
		background.render();
		ambientLightPanel.render();
		specularPowerPanel.render();
		dirLightColorPowerPanel.render();
		dirLightIntPanel.render();
		print.render();
	}

}
