package game_logic.map;

import java.util.LinkedList;

import org.joml.Intersectionf;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import engine.Engine;
import game_logic.GameOptions;
import game_logic.tile_object.Combatable;
import game_logic.tile_object.Movable;
import game_logic.tile_object.TileObject;

public class Tile {

	public enum TileVisibility {
		HIDDEN, INVISIBLE, VISIBLE
	}

	// Holding points
	public final Vector3f p1;// Z - (p4 - p1);
	public final Vector3f p2;// X - (p2 - p1);
	public final Vector3f p3;
	public final Vector3f p4;

	public final Vector3f center;

	private final Vector4f[][][] planes;
	private final Quaternionf[][][] quaternions;

	public final Vector3f globalNormal;
	public final Quaternionf globalRotation;

	private final float[] vertices;
	private final float[] normals;
	private final float[] textureCoordinates;

	// Objects on tile
	private LinkedList<TileObject> objects = new LinkedList<TileObject>();

	public Tile(float[][] mapZ, int globx, int globz) {

		int n = TileSizeHandler.instance.getTileCuts();
		int x = globx / (n + 1);
		int z = globz / (n + 1);
		float TILE_SIZE = TileSizeHandler.instance.getTileSize();
		this.p1 = new Vector3f(x * TILE_SIZE, mapZ[globx][globz], z * TILE_SIZE);
		this.p2 = new Vector3f(x * TILE_SIZE + TILE_SIZE, mapZ[globx + n + 1][globz], z * TILE_SIZE);
		this.p3 = new Vector3f(x * TILE_SIZE + TILE_SIZE, mapZ[globx + n + 1][globz + n + 1],
				z * TILE_SIZE + TILE_SIZE);
		this.p4 = new Vector3f(x * TILE_SIZE, mapZ[globx][globz + n + 1], z * TILE_SIZE + TILE_SIZE);

		// Calculating center
		center = new Vector3f();
		center.add(p1);
		center.add(p2);
		center.add(p3);
		center.add(p4);
		center.mul(0.25f);

		vertices = new float[TileSizeHandler.instance.getTileVerticesComponentsQuantity()];
		normals = new float[TileSizeHandler.instance.getTileVerticesComponentsQuantity()];
		textureCoordinates = new float[TileSizeHandler.instance.getTileTextureComponentsQuantity()];
		planes = new Vector4f[n + 1][n + 1][2];
		quaternions = new Quaternionf[n + 1][n + 1][2];

		calcTriangles(mapZ, globx, globz);

		globalNormal = new Vector3f();
		for (int i = 0; i < normals.length; i += 3) {
			globalNormal.x += normals[i];
			globalNormal.y += normals[i + 1];
			globalNormal.z += normals[i + 2];
		}
		globalNormal.mul(3f / (float) normals.length);

		globalRotation = new Quaternionf();
		globalRotation.rotateTo(new Vector3f(0f, 1f, 0f), globalNormal);
	}

	private void calcTriangles(float[][] zs, int globx, int globz) {
		int n = TileSizeHandler.instance.getTileCuts();
		float delta = TileSizeHandler.instance.getTileSize() / (float) (n + 1f);
		float texture_delta = 1f / (n + 1);
		Vector3f pa = new Vector3f();
		Vector3f pb = new Vector3f();
		Vector3f pc = new Vector3f();
		Vector3f pd = new Vector3f();

		Vector3f norm = new Vector3f();
		Vector3f p = new Vector3f();
		Vector3f q = new Vector3f();

		int i = 0;
		int norm_i = 0;
		int text_i = 0;
		for (int x = 0; x < n + 1; x++) {
			pa.set(p1).add(x * delta, 0f, 0f);
			pb.set(p1).add(x * delta + delta, 0f, 0f);
			pc.set(p1).add(x * delta + delta, 0f, delta);
			pd.set(p1).add(x * delta, 0f, delta);
			for (int z = 0; z < n + 1; z++) {
				pa.y = zs[globx + x][globz + z];
				pb.y = zs[globx + x + 1][globz + z];
				pc.y = zs[globx + x + 1][globz + z + 1];
				pd.y = zs[globx + x][globz + z + 1];

				// Vertices
				i = pack(vertices, pa, i);
				i = pack(vertices, pb, i);
				i = pack(vertices, pc, i);

				i = pack(vertices, pa, i);
				i = pack(vertices, pc, i);
				i = pack(vertices, pd, i);

				// Normals
				pc.add(-pb.x, -pb.y, -pb.z, p);
				pa.add(-pb.x, -pb.y, -pb.z, q);

				q.cross(p, norm);
				if (norm.y < 0f) {
					// norm.negate();
				}
				norm.normalize();
				quaternions[x][z][0] = new Quaternionf().rotateTo(new Vector3f(0, 1, 0), norm);
				planes[x][z][0] = createPlane(pa, norm);

				norm_i = pack(normals, norm, norm_i);
				norm_i = pack(normals, norm, norm_i);
				norm_i = pack(normals, norm, norm_i);

				pd.add(-pa.x, -pa.y, -pa.z, p);
				pc.add(-pd.x, -pd.y, -pd.z, q);

				p.cross(q, norm);
				if (norm.y < 0f) {
					// norm.negate();
				}
				norm.normalize();
				quaternions[x][z][1] = new Quaternionf().rotateTo(new Vector3f(0, 1, 0), norm);
				planes[x][z][1] = createPlane(pa, norm);

				norm_i = pack(normals, norm, norm_i);
				norm_i = pack(normals, norm, norm_i);
				norm_i = pack(normals, norm, norm_i);

				// Texture coordinates
				text_i = pack(textureCoordinates, z * texture_delta, (n - x) * texture_delta + texture_delta, text_i);
				text_i = pack(textureCoordinates, z * texture_delta, (n - x) * texture_delta, text_i);
				text_i = pack(textureCoordinates, z * texture_delta + texture_delta, (n - x) * texture_delta, text_i);

				text_i = pack(textureCoordinates, z * texture_delta, (n - x) * texture_delta + texture_delta, text_i);
				text_i = pack(textureCoordinates, z * texture_delta + texture_delta, (n - x) * texture_delta, text_i);
				text_i = pack(textureCoordinates, z * texture_delta + texture_delta,
						(n - x) * texture_delta + texture_delta, text_i);

				// Moving
				pa.add(0f, 0f, delta);
				pb.add(0f, 0f, delta);
				pc.add(0f, 0f, delta);
				pd.add(0f, 0f, delta);
			}
		}
	}

	private Vector4f createPlane(Vector3f on, Vector3f norm) {
		Vector4f result = new Vector4f();

		result.x = norm.x;
		result.y = norm.y;
		result.z = norm.z;
		result.w = -(on.x * norm.x + on.y * norm.y + on.z * norm.z);

		return result;
	}

	private int pack(float[] arr, Vector3f v, int i) {
		arr[i] = v.x;
		arr[i + 1] = v.y;
		arr[i + 2] = v.z;
		return i + 3;
	}

	private int pack(float[] arr, float x, float y, int i) {
		arr[i] = x;
		arr[i + 1] = y;
		return i + 2;
	}

	public Quaternionf createQuaternion(Vector2f scalarPos) {
		if (((GameOptions) Engine.getExternalOptions()).isAreUnitsOrthogonalToTiles()) {
			int n = TileSizeHandler.instance.getTileCuts();
			float transfX = (scalarPos.x + 1f) / 2f;
			float transfY = (scalarPos.y + 1f) / 2f;
			int x = (int) (transfX * (float) (n + 1));
			int z = (int) (transfY * (float) (n + 1));
			int side = (transfX % (1f / (float) (1 + n)) > transfY % (1f / (float) (1 + n))) ? 0 : 1;
			Quaternionf val = quaternions[x][z][side];
			return val != null ? new Quaternionf(val) : new Quaternionf();
		} else {
			return new Quaternionf();
		}
	}

	Quaternionf createQuaternion(Vector3f scalarPos) {
		if (((GameOptions) Engine.getExternalOptions()).isAreUnitsOrthogonalToTiles()) {
			int n = TileSizeHandler.instance.getTileCuts();
			Vector3f a = new Vector3f(scalarPos);
			a.negate();
			a.add(p1);
			a.negate();
			int x = (int) (a.x / TileSizeHandler.instance.getTileSize() * (n + 1));
			int z = (int) (a.z / TileSizeHandler.instance.getTileSize() * (n + 1));
			int side = ((a.x % TileSizeHandler.instance.getTileSubquadSide()) > (a.z
					% TileSizeHandler.instance.getTileSubquadSide())) ? 0 : 1;
			try {
				Quaternionf val = quaternions[x][z][side];
				return (val == null ? new Quaternionf() : val);
			} catch (Exception e) {
				return new Quaternionf();
			}
		} else {
			return new Quaternionf();
		}
	}

	/**
	 * This method invokes updateGraphics method for all objects.
	 */
	void updateGraphics() {
		for (int i = 0; i < objects.size(); i++) {
			objects.get(i).updateGraphics();
		}
	}

	void turn_started() {
		for (int i = 0; i < objects.size(); i++) {
			objects.get(i).turn_started();
		}
	}

	void endTurn() {
		for (int i = 0; i < objects.size(); i++) {
			objects.get(i).turn_ended();
		}
	}

	public boolean containsTileObject(TileObject obj) {
		return objects.contains(obj);
	}

	public boolean containsType(Class<?> cl) {
		for (TileObject obj : objects) {
			if (cl.isInstance(obj)) {
				return true;
			}
		}

		return false;
	}

	void addTileObject(TileObject obj) {
		objects.add(obj);
		obj.registerToTile(this);
	}

	void removeTileObject(TileObject obj) {
		obj.removeFromTile(this);
		objects.remove(obj);
	}

	/**
	 * Tests if the given ray intersects with this tile
	 * 
	 * @param origin : Ray origin
	 * @param dir    : Ray direction
	 * @return : True if intersects
	 */
	public boolean intersectsRay(Vector3f origin, Vector3f dir) {
		return Intersectionf.testRayTriangle(origin, dir, p1, p3, p2, 0f)
				|| Intersectionf.testRayTriangle(origin, dir, p1, p4, p3, 0f);

	}

	/**
	 * Returns length between tile center and given vector.
	 * 
	 * @param origin : Given vector
	 * @return : Length
	 */
	public float getLength(Vector3f origin) {
		Vector3f dir = new Vector3f();
		center.add(-origin.x, -origin.y, -origin.z, dir);
		return dir.length();
	}

	@SuppressWarnings("unchecked")
	public <T> LinkedList<T> getAll(Class<T> cl) {
		LinkedList<T> result = new LinkedList<T>();

		for (TileObject obj : objects) {
			if (cl.isInstance(obj)) {
				result.add((T) obj);
			}
		}

		return result;
	}

	/**
	 * Finds enemy for given Combatable object.
	 * 
	 * @param comb : Object
	 * @return : Enemy if this tiles contains such or null.
	 */
	public Combatable getEnemy(Combatable comb) {
		for (TileObject obj : objects) {
			if (obj instanceof Combatable && comb.isEnemy((Combatable) obj)) {
				return (Combatable) obj;
			}
		}
		return null;
	}

	/**
	 * Checks if this tile contains some objects that do not allow to step on them.
	 * 
	 * @param o : Object to check
	 * @return : True if there is no such objects
	 */
	public <T extends TileObject & Movable> boolean isWalkable(T o) {
		for (TileObject obj : objects) {
			if (!obj.isWalkable(o)) {
				return false;
			}
		}
		return true;
	}

	float projectY(Vector3f vec) {
		int n = TileSizeHandler.instance.getTileCuts();
		Vector3f a = new Vector3f(vec);
		a.negate();
		a.add(p1);
		a.negate();
		float TILE_SIZE = TileSizeHandler.instance.getTileSize();
		if (a.x > TILE_SIZE || a.z > TILE_SIZE) {
			throw new NullPointerException("Vector is not in tile");
		}
		int x = (int) (a.x / TILE_SIZE * (n + 1));
		int z = (int) (a.z / TILE_SIZE * (n + 1));
		int side = (a.x % TileSizeHandler.instance.getTileSubquadSide() > a.z
				% TileSizeHandler.instance.getTileSubquadSide()) ? 0 : 1;
		Vector4f plane_equation = planes[x][z][side];
		return (plane_equation.x * vec.x + plane_equation.z * vec.z + plane_equation.w) / (-plane_equation.y);
	}

	/**
	 * This method invoke method getObjectInformation for all object on this tile
	 * and store it into new list.
	 * 
	 * @return : List of information.
	 */
	public LinkedList<String> collectObjectsInformation() {
		LinkedList<String> result = new LinkedList<String>();
		for (TileObject obj : objects) {
			String line = obj.getObjectInformation();
			if (line != null) {
				result.add(line);
			}
		}
		return result;
	}

	boolean isOnTile(Vector3f v, float epsilon) {
		int n = TileSizeHandler.instance.getTileCuts();
		Vector3f a = new Vector3f(v);
		a.negate();
		a.add(p1);
		a.negate();
		int x = (int) (a.x / TileSizeHandler.instance.getTileSize() * (n + 1));
		int z = (int) (a.z / TileSizeHandler.instance.getTileSize() * (n + 1));
		int side = (a.x % TileSizeHandler.instance.getTileSubquadSide() > a.z
				% TileSizeHandler.instance.getTileSubquadSide()) ? 0 : 1;
		Vector4f plane_equation = planes[x][z][side];
		return Math.abs(
				v.x * plane_equation.x + v.y * plane_equation.y + v.z * plane_equation.z + plane_equation.w) < epsilon;
	}

	public final LinkedList<TileObject> getObjects() {
		return objects;
	}

	public final float[] getVertices() {
		return vertices;
	}

	public final float[] getNormals() {
		return normals;
	}

	public final float[] getTextureCoordinates() {
		return textureCoordinates;
	}
}
