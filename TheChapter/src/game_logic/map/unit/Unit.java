package game_logic.map.unit;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedList;

import org.joml.Vector2f;

import game_logic.gui.controllers.TileObjectController;
import game_logic.map.Map;
import game_logic.map.Team;
import game_logic.map.Tile;
import game_logic.map.TileSizeHandler;
import game_logic.map.badges.Badge;
import game_logic.map.decoration.Decoration;
import game_logic.map.unit.actions.Damaging;
import game_logic.map.unit.actions.Healing;
import game_logic.map.unit.actions.Moving;
import game_logic.map.unit.actions.Standing;
import game_logic.map.unit.actions.UnitAction;
import game_logic.map.unit.actions.Waiting;
import game_logic.map.unit.actions.permanent.PermanentAction;
import game_logic.map.unit.actions.permanent.PermanentHealing;
import game_logic.tile_object.Controllable;
import game_logic.tile_object.Damageable;
import game_logic.tile_object.Movable;
import game_logic.tile_object.Observer;
import game_logic.tile_object.Teamable;
import game_logic.tile_object.TileObject;
import game_logic.tile_object.moving.LeftBypassRouteCreator;
import game_logic.tile_object.moving.LinearRouteCreator;
import game_logic.tile_object.moving.RightBypassRouteCreator;
import game_logic.tile_object.moving.RouteCreator;
import graphicsSupport.mesh.Mesh;
import graphicsSupport.texture.Texture;
import soundSupport.SoundBuffer;

public abstract class Unit implements Controllable, TileObject, Movable, Damageable, Teamable, Observer {

	/**
	 * This class is in loadAll method and it converts meshes and scalar
	 * positions to decorations.
	 */
	protected class Loader {

		// Action and decorations
		private HashMap<Class<? extends UnitAction>, LinkedList<Mesh[][]>> actions_decorations = new HashMap<Class<? extends UnitAction>, LinkedList<Mesh[][]>>();

		// Men quantity
		private Vector2f[] scalar_positions;

		private Loader() {
		}

		/**
		 * This is the final conversion method that converts meshes and scalar
		 * position into decorations.
		 * 
		 * @return : HashMap of Decorations and unit action types.
		 */
		private HashMap<Class<? extends UnitAction>, Decoration[][]>[] convert() {
			@SuppressWarnings("unchecked")
			HashMap<Class<? extends UnitAction>, Decoration[][]>[] result = new HashMap[scalar_positions.length];

			for (int scal_i = 0; scal_i < scalar_positions.length; scal_i++) {
				result[scal_i] = new HashMap<Class<? extends UnitAction>, Decoration[][]>();
				for (Class<? extends UnitAction> key : actions_decorations.keySet()) {
					result[scal_i].put(key,
							prepareDecorations(this.actions_decorations.get(key), this.scalar_positions[scal_i]));
				}
			}

			return result;
		}

		/**
		 * This method bound given meshes to given action.
		 * 
		 * @param action
		 *            : Action that will have these meshes.
		 * @param meshes
		 *            : Meshes for action.
		 */
		public void load(Class<? extends UnitAction> action, Mesh[][] meshes) {
			if (!actions_decorations.containsKey(action)) {
				actions_decorations.put(action, new LinkedList<Mesh[][]>());
			}
			actions_decorations.get(action).add(meshes);
		}
		
		@SafeVarargs
		public final void load(Mesh[][] meshes, Class<? extends UnitAction>... actions) {
			for (int i = 0; i < actions.length; i++) {
				load(actions[i], meshes);
			}
		}
		
		@SafeVarargs
		public final void load(Mesh[] meshes, Class<? extends UnitAction>... actions) {
			for (int i = 0; i < actions.length; i++) {
				load(actions[i], meshes);
			}
		}

		/**
		 * This method bound given meshes to given action.
		 * 
		 * @param action
		 *            : Action that will have these meshes.
		 * @param meshes
		 *            : Meshes for action.
		 */
		public void load(Class<? extends UnitAction> action, Mesh[] meshes) {
			if (!actions_decorations.containsKey(action)) {
				actions_decorations.put(action, new LinkedList<Mesh[][]>());
			}
			actions_decorations.get(action).add(new Mesh[][] { meshes });
		}

		/**
		 * This method converts meshes double array and scalar position into
		 * Decoration list.
		 * 
		 * @param meshes
		 *            : Meshes to converts.
		 * @param scal
		 *            : Scalar position that all new decorations will have.
		 * @return : Decoration array.
		 */
		private Decoration[][] prepareDecorations(LinkedList<Mesh[][]> meshes, Vector2f scal) {
			Decoration[][] result = new Decoration[meshes.size()][];
			for (int i = 0; i < meshes.size(); i++) {
				result[i] = new Decoration[meshes.get(i).length];
				for (int a = 0; a < meshes.get(i).length; a++) {
					result[i][a] = new Decoration(meshes.get(i)[a], Unit.this, scal);
				}
			}
			return result;
		}

		/**
		 * This method loads all scalar positions list to use in unit.
		 * 
		 * @param scalar_positions
		 *            : Positions for decorations.
		 */
		public final void setScalar_positions(Vector2f[] scalar_positions) {
			this.scalar_positions = scalar_positions;
		}
	}

	// Badge position
	public static final float badge_height = TileSizeHandler.instance.getTileSize() / 2f;

	// Health
	protected float current_health = 1;

	// Home
	protected volatile Map homeMap;
	protected volatile Tile homeTile;

	// Decoration parameters
	protected Vector2f direction = new Vector2f(0, 1);

	// Subjects
	protected final Subject[] subjects;

	// Actions
	protected final HashMap<UnitAction, Long> current_actions = new HashMap<UnitAction, Long>();
	protected PermanentAction permanentAction = null;

	// Route creator
	private final RouteCreator[] routeCreators;

	// Distance
	private int base_move_points;
	private int current_move_points;

	// Player
	private Team master;

	// Badge
	private Badge badge;

	protected Unit() {
		Loader loader = new Loader();
		loadAll(loader);
		HashMap<Class<? extends UnitAction>, Decoration[][]>[] dec_act = loader.convert();
		subjects = new Subject[dec_act.length];
		for (int i = 0; i < dec_act.length; i++) {
			subjects[i] = new Subject(this, dec_act[i]);
		}
		routeCreators = new RouteCreator[] { new LinearRouteCreator(), new RightBypassRouteCreator(),
				new LeftBypassRouteCreator() };
		current_move_points = base_move_points;
		registerActionToAllSubjects(Standing.class, new Object[0]);
	}

	@Override
	public void damage(float val) {
		current_health = Math.max(0, this.current_health - val);

		int size = (int) (float) ((1 - current_health) * (float) subjects.length);
		LinkedList<Subject> subs_list = new LinkedList<Subject>();
		int a = 0;
		for (int i = 0; i < size; i++) {
			if (a == subjects.length) {
				break;
			}
			if (!subjects[a].isKilled()) {
				subs_list.add(subjects[a]);
			}
			a++;
		}

		if (subs_list.size() == 0) {
			return;
		}

		Subject[] subs = new Subject[subs_list.size()];
		for (int i = 0; i < subs.length; i++) {
			subs[i] = subs_list.get(i);
			subs[i].kill();
		}

		registerActionToSubjects(subs, Damaging.class, new Object[] { (current_health <= 0), getDamageTime() });
	}

	@Override
	public void deleteFromMap(Map map) {
		homeMap.removeBadge(badge);
		badge = null;
		map = null;
	}

	/**
	 * This method gives end time for given action.
	 * 
	 * @param ua
	 *            : Action to search time for.
	 * @return : Time when action will be destroyed;
	 */
	public final long getActionEndTime(UnitAction ua) {
		return current_actions.get(ua);
	}

	/**
	 * This method gives texture to use on badge.
	 * 
	 * @return : Texture, unique unit symbol.
	 */
	public abstract Texture getBadgeTexture();

	@Override
	public final float getCurrentHealth() {
		return current_health;
	}

	@Override
	public final int getCurrentMovePoints() {
		return current_move_points;
	}

	/**
	 * This parameter method gives unit damage animation time.
	 * 
	 * @return : Time of animation.
	 */
	public abstract long getDamageTime();

	/**
	 * This method gives specific unit action type decorations from all
	 * subjects.
	 * 
	 * @param c
	 *            : Unit action type.
	 * @return : Array of decorations.
	 */
	public final Decoration[] getDecoration(Class<? extends UnitAction> c) {
		LinkedList<Decoration[]> raw_decs = new LinkedList<Decoration[]>();

		// Collecting from subjects
		for (Subject sub : subjects) {
			raw_decs.add(sub.collectDecorations(c));
		}

		// Calculating size
		int size = 0;
		for (Decoration[] decs : raw_decs) {
			size = size + decs.length;
		}

		// Putting to output array
		Decoration[] result = new Decoration[size];
		int i = 0;
		for (Decoration[] decs : raw_decs) {
			for (Decoration dec : decs) {
				result[i] = dec;
				i++;
			}
		}

		return result;
	}

	@Override
	public final Decoration[] getDecorations() {
		LinkedList<Decoration[]> raw_decs = new LinkedList<Decoration[]>();

		// Collecting from subjects
		for (Subject sub : subjects) {
			raw_decs.add(sub.collectDecorations());
		}

		// Calculating size
		int size = 0;
		for (Decoration[] decs : raw_decs) {
			size = size + decs.length;
		}

		// Putting to output array
		Decoration[] result = new Decoration[size];
		int i = 0;
		for (Decoration[] decs : raw_decs) {
			for (Decoration dec : decs) {
				result[i] = dec;
				i++;
			}
		}

		return result;
	}

	@Override
	public final Vector2f getDirection() {
		return direction;
	}

	@Override
	public final Map getHomeMap() {
		return homeMap;
	}

	@Override
	public final Tile getHomeTile() {
		return homeTile;
	}

	@Override
	public final TileObjectController getGui() {
		return TileObjectController.unit_controller;
	}

	@Override
	public final Team getMasterTeam() {
		return master;
	}

	@Override
	public final int getMovePoints() {
		return base_move_points;
	}

	@Override
	public final String getObjectInformation() {
		return "Unit: " + getName() + ", belongs to " + master.getName() + "." + (current_health > 0 ? "" : " killed.");
	}

	@Override
	public final RouteCreator[] getRouteCreatorAlgorithm() {
		return routeCreators;
	}

	/**
	 * This parameter method tells how long will this unit go on one tile.
	 * 
	 * @return : Time that takes one tile trip.
	 */
	public abstract long getTileMoveTime();

	/**
	 * This method gives unit badge.
	 * 
	 * @return : Unit badge.
	 */
	public final Badge getUnitBadge() {
		return badge;
	}

	@Override
	public void giveToTeam(Team pl) {
		this.master = pl;
	}

	@Override
	public final <T extends Movable & TileObject> boolean isWalkable(T obj) {
		return current_health <= 0f;
	}

	/**
	 * In this method all unit meshes should be loaded via loader object.
	 * 
	 * @param loader
	 *            : Loader object.
	 */
	public abstract void loadAll(Loader loader);

	@Override
	public void move(Tile[] path) {
		registerActionToAllSubjects(Moving.class, path[0], new Object[] { (Object[]) path, this.getTileMoveTime() });
		current_move_points = current_move_points - path.length + 1;
		badge.setAlpha((int) (float) ((float) this.current_move_points / (float) base_move_points * 255f));
	}

	@Override
	public void teamTurnFinished() {
	}

	@Override
	public void teamTurnStarted() {
		current_move_points = base_move_points;
		badge.setAlpha((int) (float) ((float) this.current_move_points / (float) base_move_points * 255f));

		if (permanentAction != null) {
			permanentAction.nextTeamTurn();
		}
	}

	/**
	 * This method creates and registers new action for all subjects. It puts
	 * new action to unit actions MAP. And it invokes action initialization
	 * methods.
	 * 
	 * @param act_class
	 *            : Unit action type to create.
	 * @param args
	 *            : Arguments for this action.
	 */
	public final void registerActionToAllSubjects(Class<? extends UnitAction> act_class, Object... args) {
		UnitAction action = null;
		try {
			action = act_class.getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		current_actions.put(action, -1l);
		action.init(subjects, homeTile);
		action.init(args);
		for (Subject sub : subjects) {
			sub.registerAction(action);
		}

	}

	/**
	 * This method creates and registers new action for all subjects but it is
	 * initializing action with the given tile. It puts new action to unit
	 * actions MAP. And it invokes action initialization methods.
	 * 
	 * @param act_class
	 *            : Unit action type to create.
	 * @param args
	 *            : Arguments for this action.
	 */
	public final void registerActionToAllSubjects(Class<? extends UnitAction> act_class, Tile tile, Object... args) {
		UnitAction action = null;
		try {
			action = act_class.getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		current_actions.put(action, -1l);
		action.init(subjects, tile);
		action.init(args);
		for (Subject sub : subjects) {
			sub.registerAction(action);
		}

	}

	/**
	 * This method creates and registers new action for given subjects. And for
	 * other subjects it creates and run WAIT action with the same end time as
	 * in the main action.
	 * 
	 * @param subs
	 *            : Subjects to create action for.
	 * @param act_class
	 *            : Unit action type to create.
	 * @param args
	 *            : Action arguments.
	 */
	public final void registerActionToSubjects(Subject[] subs, Class<? extends UnitAction> act_class, Object... args) {
		if (subs.length == 0) {
			return;
		}
		UnitAction action = null;
		try {
			action = act_class.getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		current_actions.put(action, (long) -1);
		action.init(subs, homeTile);
		action.init(args);
		LinkedList<Subject> subs_list = new LinkedList<Subject>();
		for (Subject sub : subs) {
			sub.registerAction(action);
			subs_list.add(sub);
		}

		// Registering wait action for other
		// Sub action
		LinkedList<Subject> other_subs_list = new LinkedList<Subject>();

		for (int i = 0; i < subjects.length; i++) {
			if (!subjects[i].isVisible()) {
				continue;
			}
			if (!subs_list.contains(subjects[i])) {
				other_subs_list.add(subjects[i]);
			}
		}

		if (other_subs_list.size() == 0) {
			return;
		}

		Subject[] other_subs = new Subject[other_subs_list.size()];
		for (int i = 0; i < other_subs_list.size(); i++) {
			other_subs[i] = other_subs_list.get(i);
		}

		UnitAction other_action = new Waiting();
		current_actions.put(other_action, (long) -1);
		other_action.init(other_subs, homeTile);
		other_action.init(new Object[] { (long) action.getTotal_action_time() });
		for (Subject sub : other_subs) {
			sub.registerAction(other_action);
		}
	}

	public final void registerPermanentAction(Class<? extends PermanentAction> cl, Object... pars) {
		try {
			permanentAction = cl.getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
		}
		permanentAction.init(this);
		permanentAction.init(pars);
		permanentAction.start();
	}

	@Override
	public void registerToMap(Map map) {
		homeMap = map;
	}

	@Override
	public void registerToTile(Tile homeTile) {
		this.homeTile = homeTile;

		if (badge == null) {
			badge = new Badge(this.getHomeTile(), this.getBadgeTexture(), master.getPlayer_color_i());
			homeMap.addBadge(badge);
		}
	}

	/**
	 * This method reloads decorations for given subjects.
	 * <p>
	 * It will no do this for subject if: 1. Current decorations equals to new
	 * ones. 2. Subjects first action is not started AND the action is not
	 * STANDING.
	 * 
	 * @param subs
	 *            : Subjects to reload decorations to.
	 */
	private final void reloadSubjects(Subject[] subs) {
		for (Subject sub : subs) {
			Decoration[] new_decs = sub.collectDecorations();

			if (!sub.isVisible() || sub.current_decorations != new_decs) {
				for (Decoration dec : sub.current_decorations) {
					homeMap.removeDecoration(dec);
				}
			}
			if (!sub.isVisible()) {
				continue;
			}

			for (Decoration dec : new_decs) {
				if (sub.current_decorations != new_decs) {
					if (!sub.getCurrentAction().abandonPreparingToTile()) {
						dec.prepare(sub.getCurrentAction().getAction_tile() == null ? homeTile
								: sub.getCurrentAction().getAction_tile());
					}
					homeMap.addDecoration(dec);
				}
			}
			sub.current_decorations = new_decs;
		}
	}

	@Override
	public void removeFromTeam() {
		this.master = null;
	}

	@Override
	public void removeFromTile(Tile homeTile) {
		this.homeTile = null;
	}

	/**
	 * This method sets end time for given action.
	 * 
	 * @param ua
	 *            : Action to set time for.
	 * @param time
	 *            : Time when this action must end.
	 */
	public final void setActionEndTime(UnitAction ua, long time) {
		current_actions.replace(ua, (long) time);
	}

	/**
	 * This method sets direction to unit.
	 * 
	 * @param dir
	 *            : New direction vector.
	 */
	public final void setDirection(Vector2f dir) {
		this.direction = dir;
	}

	/**
	 * This method set maximum move points constant.
	 * 
	 * @param base_move_points
	 *            : Move points.
	 */
	protected final void setMovePoints(int base_move_points) {
		this.base_move_points = base_move_points;
	}

	@Override
	public final void stop() {
		current_move_points = 0;
		badge.setAlpha((int) (float) ((float) this.current_move_points / (float) base_move_points * 255f));
	}

	@Override
	public void turn_ended() {
	}

	@Override
	public void turn_started() {
		LinkedList<UnitAction> to_upd = new LinkedList<UnitAction>();

		for (Subject sub : subjects) {
			UnitAction ac = sub.getCurrentAction();
			if (!to_upd.contains(ac)) {
				to_upd.add(ac);
			}
		}

		for (UnitAction ac : to_upd) {
			ac.turn_started();
		}

		if (permanentAction != null) {
			permanentAction.nextTurn();
		}
	}

	@Override
	public final boolean canControl(Team player) {
		return (player == master && this.current_health > 0);
	}

	public final void interruptPermanentAction() {
		permanentAction.interrupted();
		permanentAction = null;
	}

	private final boolean containsSubsidiary() {
		for (UnitAction key : current_actions.keySet()) {
			if (key.getClass() == Standing.class || permanentAction.isSubsidiary(key.getClass())) {
				continue;
			}
			return false;
		}
		return true;
	}

	@Override
	public void updateGraphics() {
		// Permanent actions
		if (permanentAction != null) {
			if (current_actions.size() > 1 && permanentAction.isStarted() && !containsSubsidiary()) {
				interruptPermanentAction();
			} else {
				permanentAction.updateGraphics();
			}
		}

		// Normal actions
		A: while (true) {
			LinkedList<UnitAction> to_upd = new LinkedList<UnitAction>();

			for (Subject sub : subjects) {
				// if (!sub.isVisible()) {
				// continue;
				// }
				UnitAction ac = sub.getCurrentAction();
				if (!to_upd.contains(ac)) {
					to_upd.add(ac);
				}
			}

			boolean started = false;
			boolean ended = false;

			for (UnitAction ac : to_upd) {
				if (current_actions.get(ac) == -1l && ac.canStart()) {
					long t = ac.getTotal_action_time();
					current_actions.replace(ac, (System.currentTimeMillis() + t));

					reloadSubjects(ac.getSubjects());

					ac.start();

					started = true;
				}

				if (ac.getClass() != Standing.class && current_actions.get(ac) != -1
						&& (current_actions.get(ac) - System.currentTimeMillis() <= 0)) {
					current_actions.remove(ac);
					ac.removeActionFromAllSubjects();
					ac.end();
					reloadSubjects(ac.getSubjects());
					ended = true;
				} else {
					if (current_actions.get(ac) != -1l) {
						ac.updateGraphics();
					}
				}

				if (started && ended) {
					continue A;
				}
			}

			break;
		}
	}

	/**
	 * This method will return true if at least one subject has action with
	 * Class that equals to given;
	 * 
	 * @param ac
	 *            : Given class.
	 * @return : True if contains.
	 */
	public final boolean actionIsFirst(Class<? extends UnitAction> ac) {
		for (Subject sub : subjects) {
			if (sub.getCurrentAction().getClass() == ac) {
				return true;
			}
		}
		return false;
	}

	@Override
	public final void heal(float val) {
		registerActionToAllSubjects(Healing.class, new Object[] { val });
	}

	public final void permanentlyHeal(float val) {
		registerPermanentAction(PermanentHealing.class, new Object[] { val });
	}

	/**
	 * This method tells how long will be healing animation played for this
	 * unit.
	 * 
	 * @return : Time to play animation.
	 */
	public abstract long getHealingTime();

	public abstract SoundBuffer getMoveSound();

	public final void setCurrent_health(float current_health) {
		this.current_health = current_health;
	}

	public final int getSubjectsQuantity() {
		return subjects.length;
	}

	public final PermanentAction getCurrentPermanentAction() {
		return permanentAction;
	}

	@Override
	public int getVisibleRange() {
		return 2;
	}

	@Override
	public final boolean staticVisibility() {
		return false;
	}

}
