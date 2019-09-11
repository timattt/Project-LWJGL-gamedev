package game_logic.map.decoration;

import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

import game_logic.map.Tile;
import game_logic.map.TileSizeHandler;
import game_logic.tile_object.TileObject;
import graphicsSupport.Item;
import graphicsSupport.mesh.Mesh;
import graphicsSupport.mesh.animation.Animation;
import graphicsSupport.mesh.animation.ChunkedAnimation;

public class Decoration extends Item {

	// Final
	private volatile boolean transformable = true;

	// Location on tile
	public final Vector2f scalar_location;

	// Tile object
	private final TileObject object;

	public Decoration(Mesh shape, TileObject obj) {
		super(new Mesh[] { shape });
		this.object = obj;
		scalar_location = new Vector2f(0, 0);
		rescale();
	}

	public Decoration(Mesh[] mesh, TileObject obj) {
		super(mesh);
		this.object = obj;
		scalar_location = new Vector2f(0, 0);
		rescale();
	}

	public Decoration(Mesh[] mesh, TileObject obj, float scale) {
		super(mesh);
		this.object = obj;
		scalar_location = new Vector2f(0, 0);
		this.setScale(scale);
	}

	public Decoration(Mesh[] mesh, TileObject obj, Vector2f scalar_loc) {
		super(mesh);
		this.object = obj;
		this.scalar_location = scalar_loc;
		rescale();
	}

	private final void rescale() {
		setScale(TileSizeHandler.instance.getTileObjectStandartSize() / (object != null ? object.getDecorationHeight() : 1));
	}

	public void prepare(Tile tile) {
		Vector2f dir = object.getDirection();
		float angle = dir.angle(TileObject.BASE_DIRECTION);
		
		Vector3f rot_ = new Vector3f(scalar_location.x, 0f, scalar_location.y);
		Matrix3f rotY = new Matrix3f().rotateY(angle);
		rot_.mul(rotY);
		
		Vector2f scalar_pos_rotated = new Vector2f(rot_.x, rot_.z);

		Quaternionf rotation = tile.createQuaternion(scalar_pos_rotated);
		
		rotation.rotateY(angle);
		rotate(rotation);

		Vector3f pos = new Vector3f(tile.center);

		// Calculating scalar position
		Vector3f scalar_pos = new Vector3f(this.calculateScalarPosition());
		Quaternionf rot_y = new Quaternionf();
		rot_y.rotateY(angle);
		scalar_pos.rotate(rot_y);

		pos.add(scalar_pos);

		pos.y = object.getHomeMap().projectY(pos);
		
		setPosition(pos);
		rescale();
	}

	public final TileObject getObject() {
		return object;
	}

	public Vector3f calculateScalarPosition() {
		Vector3f x = new Vector3f(TileSizeHandler.instance.getTileSize() / 2, 0, 0), y = new Vector3f(0, 0, TileSizeHandler.instance.getTileSize() / 2);

		x.mul(scalar_location.x);
		y.mul(scalar_location.y);

		x.add(y);
		return x;
	}

	public static Vector3f calculateScalarPosition(Vector2f scalar_location) {
		Vector3f x = new Vector3f(TileSizeHandler.instance.getTileSize() / 2, 0, 0), y = new Vector3f(0, 0, TileSizeHandler.instance.getTileSize() / 2);

		x.mul(scalar_location.x);
		y.mul(scalar_location.y);

		x.add(y);
		return x;
	}

	public final boolean isTransformable() {
		return transformable;
	}

	public final void setTransformable(boolean transformable) {
		this.transformable = transformable;
	}

	@Override
	public Item setScale(float scale) {
		if (!transformable) {
			return this;
		}
		return super.setScale(scale);
	}

	@Override
	public Item setPosition(float x, float y, float z) {
		if (!transformable) {
			return this;
		}
		return super.setPosition(x, y, z);
	}

	@Override
	public Item rotate(Quaternionf quat) {
		if (!transformable) {
			return this;
		}
		return super.rotate(quat);
	}

	@Override
	public Item setRotation(float x, float y, float z) {
		if (!transformable) {
			return this;
		}
		return super.setRotation(x, y, z);
	}

	@Override
	public Item setPosition(Vector3f vec) {
		if (!transformable) {
			return this;
		}
		super.setPosition(vec);
		return this;
	}

	@Override
	public void movePosition(float x, float y, float z) {
		if (!transformable) {
			return;
		}
		super.movePosition(x, y, z);
	}

	@Override
	public void moveRotation(float x, float y, float z) {
		if (!transformable) {
			return;
		}
		super.moveRotation(x, y, z);
	}

	@Override
	public void movePosition(Vector3f vec) {
		if (!transformable) {
			return;
		}
		super.movePosition(vec);
	}

	public void refresh() {
		if (getBaseMesh() instanceof Animation || getBaseMesh() instanceof ChunkedAnimation) {
			for (Mesh mesh : meshes) {
				if (mesh instanceof Animation) {
					((Animation) mesh).startAnimation();
				}
				if (mesh instanceof ChunkedAnimation) {
					((ChunkedAnimation) mesh).startAnimation();
				}
			}
		}
	}
}
