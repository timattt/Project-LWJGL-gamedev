package graphicsSupport;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import engine.monoDemeanor.MonoDemeanor;
import engine.monoDemeanor.MonoDemeanorInstance;

@MonoDemeanor
public class FrustumFilter {

	@MonoDemeanorInstance
	public static final FrustumFilter instance = new FrustumFilter();

	// Planes
	private final Vector4f[] planes = new Vector4f[6];

	// Corners
	private final Vector3f[] corners = new Vector3f[8];

	private FrustumFilter() {
		for (int i = 0; i < 6; i++) {
			planes[i] = new Vector4f();
		}
		for (int i = 0; i < 8; i++) {
			corners[i] = new Vector3f();
		}
	}

	public void recalculatePlanes(Matrix4f view, Matrix4f proj) {
		Matrix4f mat = new Matrix4f();
		proj.mul(view, mat);

		for (int i = 0; i < 6; i++) {
			planes[i] = mat.frustumPlane(i, planes[i]);
		}

		for (int i = 0; i < 8; i++) {
			corners[i] = mat.frustumCorner(i, corners[i]);
		}
	}

	public final Vector4f[] getPlanes() {
		return planes;
	}

	public final Vector3f[] getCorners() {
		return corners;
	}

}
