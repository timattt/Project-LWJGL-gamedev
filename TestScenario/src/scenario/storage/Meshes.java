package scenario.storage;

import graphicsSupport.mesh.Mesh;

public class Meshes {

	// Terrains
	public static final Mesh[] TREE = Mesh.find("Tree_oak");
	
	// Units
	// Line infantry
	public static final Mesh[] LINE_INFANTRY_STANDING1 = Mesh.find("LineInfantryStand");
	public static final Mesh[] LINE_INFANTRY_STANDING2 = Mesh.find("LineInfantryStand2");
	public static final Mesh[] LINE_INFANTRY_WALKING = Mesh.find("LineInfantryMove");
	public static final Mesh[] LINE_INFANTRY_SHOOTING_READY = Mesh.find("LineInfantryShootReady");
	public static final Mesh[] LINE_INFANTRY_SHOOTING = Mesh.find("LineInfantryShoot");
	public static final Mesh[] LINE_INFANTRY_DEATH1 = Mesh.find("LineInfantryKilled");
	public static final Mesh[] LINE_INFANTRY_DEATH2 = Mesh.find("LineInfantryDeath2");
	public static final Mesh[] LINE_INFANTRY_DEATH3 = Mesh.find("LineInfantryDeath3");
	public static final Mesh[] LINE_INFANTRY_BULLET = Mesh.find("LineInfantryBullet");

	// City
	public static final Mesh[] HOUSE = Mesh.find("Building1");
	public static final Mesh[] CITY_HALL = Mesh.find("CityHall");
	public static final Mesh[] BUILDING_MARK = Mesh.find("BuildProcess");

	// Swordman
	public static final Mesh[] SWORDMAN_STAND1 = Mesh.find("SwordmanStand");
	public static final Mesh[] SWORDMAN_WALK = Mesh.find("SwordmanRun");
	public static final Mesh[] SWORDMAN_ATTACK = Mesh.find("SwordmanAttack");
	public static final Mesh[] SWORDMAN_DEATH = Mesh.find("SwordmanDeath");
	public static final Mesh[] SWORDMAN_RUN = Mesh.find("SwordmanRun");
	public static final Mesh[] SWORDMAN_STAND2 = Mesh.find("SwordmanStand2");
	
	// Zeppelin
	public static final Mesh[] ZEPPELIN = Mesh.find("Zeppelin");
	public static final Mesh[] ZEPPELIN_BULLET = Mesh.find("ZeppelinBullet");
}
