package game_logic.map.unit;

import java.util.HashMap;
import java.util.LinkedList;

import org.joml.Vector2f;

import game_logic.map.decoration.Decoration;
import game_logic.map.unit.actions.Standing;
import game_logic.map.unit.actions.UnitAction;

public class Subject {

	// Actions
	private final LinkedList<UnitAction> action_sequence = new LinkedList<UnitAction>();
	
	// Decorations
	protected static final Decoration[] empty_decorations = new Decoration[0];
	
	protected final HashMap<Class<? extends UnitAction>, Decoration[][]> actions_decorations;
	protected Decoration[] current_decorations = empty_decorations;
	
	// Scalar position
	private final Vector2f scalar_position;

	// Unit
	private Unit unit;

	// Killed
	private boolean isKilled;
	private boolean isVisible = true;

	// Random
	protected final float random_coef = (float) Math.random();
	
	public Subject(Unit unit, HashMap<Class<? extends UnitAction>, Decoration[][]> ac_de) {
		this.unit = unit;
		this.actions_decorations = ac_de;
		scalar_position = ac_de.get(Standing.class)[0][0].scalar_location;
	}

	/**
	 * This method set variable isKilled to true.
	 */
	public final void kill() {
		isKilled = true;
	}

	/**
	 * This method removes action from this subject and always will leave
	 * STANDING action.
	 * 
	 * @param action
	 *            : Action to delete.
	 */
	public final void removeAction(UnitAction action) {
		action_sequence.remove(action);
		if (action_sequence.size() == 1) {
			unit.current_actions.replace(this.action_sequence.getFirst(), (long) -1);
		}
	}

	/**
	 * This method puts this action to action sequence.
	 * 
	 * @param action
	 *            : Action to register.
	 */
	public final void registerAction(UnitAction action) {
		if (action == null || !actions_decorations.containsKey(action.getClass())) {
			return;
		}
		
		if (action_sequence.size() > 0) {
			action_sequence.add(this.action_sequence.size() - 1, action);
		} else {
			action_sequence.add(action);
		}
	}

	/**
	 * This method checks if this subjects variable isKilled is true.
	 * 
	 * @return : True if killed.
	 */
	public final boolean isKilled() {
		return isKilled;
	}

	/**
	 * This method collects decoration from current action. If the subject is
	 * not visible then nothing will be given.
	 * 
	 * @return : Decorations array.
	 */
	public final Decoration[] collectDecorations() {
		return !isVisible ? Subject.empty_decorations
				: actions_decorations.get(this.action_sequence.getFirst().getClass())[(int) (random_coef * (actions_decorations.get(action_sequence.getFirst().getClass()).length))];
	}

	/**
	 * This method collects decoration from given actions. If the subject is not
	 * visible then nothing will be given.
	 * 
	 * @return : Decorations array.
	 */
	public final Decoration[] collectDecorations(Class<? extends UnitAction> ac) {
		return !isVisible ? Subject.empty_decorations : actions_decorations.get(ac)[(int) (random_coef * actions_decorations.get(ac).length)];
	}

	/**
	 * This method gives subjects master unit.
	 * 
	 * @return : Unit that contains this subject.
	 */
	public final Unit getUnit() {
		return unit;
	}

	/**
	 * This method gives this subjects unrotated scalar position.
	 * 
	 * @return : Scalar position.
	 */
	public final Vector2f getScalar_position() {
		return scalar_position;
	}

	/**
	 * This method sets subject visibility.
	 * 
	 * @param isVisible
	 *            : Visibility parameter.
	 */
	public final void setVisible(boolean isVisible) {
		this.isVisible = isVisible;
	}

	/**
	 * This method gives subjects visibility parameter.
	 * 
	 * @return : True if visible.
	 */
	public final boolean isVisible() {
		return isVisible;
	}

	/**
	 * This method sets subject to be killed.
	 * 
	 * @param isKilled
	 *            : Killed parameter.
	 */
	public final void setKilled(boolean isKilled) {
		this.isKilled = isKilled;
	}
	
	/**
	 * This method gives first action from sequence list.
	 * @return : Action
	 */
	public final UnitAction getCurrentAction() {
		return action_sequence.getFirst();
	}

	/**
	 * This method gives quantity of actions that will be invoked.
	 * @return : Action sequence list size.
	 */
	public final int getActionsQuantity() {
		return action_sequence.size();
	}
}
