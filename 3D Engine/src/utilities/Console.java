package utilities;

import static java.util.Calendar.HOUR_OF_DAY;
import static java.util.Calendar.MILLISECOND;
import static java.util.Calendar.MINUTE;
import static java.util.Calendar.SECOND;

import java.util.Calendar;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector2dc;
import org.joml.Vector2fc;
import org.joml.Vector2i;
import org.joml.Vector3fc;

public class Console {

	public static void println(String args) {
		Calendar calendar = Calendar.getInstance();
		System.out.println("[" + calendar.get(HOUR_OF_DAY) + ":" + calendar.get(MINUTE) + ":" + calendar.get(SECOND)
				+ ":" + calendar.get(MILLISECOND) + "] " + args);
	}

	public static void print(String args) {
		System.out.print(args);
	}

	public static void skipLine() {
		System.out.println();
	}

	public static String generateDateString() {
		Calendar calendar = Calendar.getInstance();
		return "[" + calendar.get(HOUR_OF_DAY) + ":" + calendar.get(MINUTE) + ":" + calendar.get(SECOND) + ":"
				+ calendar.get(MILLISECOND) + "]";
	}

	public static void println_err(String args) {
		System.err.println(generateDateString() + " " + args);
	}

	public static void printVector(Vector3fc vec) {
		System.out.println(generateDateString() + " x: " + (double) vec.x() + " y: " + (double) vec.y() + " z: "
				+ (double) vec.z());
	}

	public static void printQuaternion(Quaternionf vec) {
		Calendar calendar = Calendar.getInstance();
		System.out.println("[" + calendar.get(HOUR_OF_DAY) + ":" + calendar.get(MINUTE) + ":" + calendar.get(SECOND)
				+ ":" + calendar.get(MILLISECOND) + "] x: " + (double) vec.x() + " y: " + (double) vec.y() + " z: "
				+ (double) vec.z() + " w: " + (double) vec.w);
	}

	public static void printVector(Vector2i vec) {
		Calendar calendar = Calendar.getInstance();
		System.out.println("[" + calendar.get(HOUR_OF_DAY) + ":" + calendar.get(MINUTE) + ":" + calendar.get(SECOND)
				+ ":" + calendar.get(MILLISECOND) + "] x: " + (double) vec.x() + " y: " + (double) vec.y());
	}

	public static void printVector(Vector2fc vec) {
		Calendar calendar = Calendar.getInstance();
		System.out.println("[" + calendar.get(HOUR_OF_DAY) + ":" + calendar.get(MINUTE) + ":" + calendar.get(SECOND)
				+ ":" + calendar.get(MILLISECOND) + "] x: " + (double) vec.x() + " y: " + (double) vec.y());
	}

	public static void printVector(Vector2dc vec) {
		Calendar calendar = Calendar.getInstance();
		System.out.println("[" + calendar.get(HOUR_OF_DAY) + ":" + calendar.get(MINUTE) + ":" + calendar.get(SECOND)
				+ ":" + calendar.get(MILLISECOND) + "] x: " + (double) vec.x() + " y: " + (double) vec.y());
	}

	public static void printMatrix(Matrix4f mat) {
		Calendar calendar = Calendar.getInstance();
		System.out.println("[" + calendar.get(HOUR_OF_DAY) + ":" + calendar.get(MINUTE) + ":" + calendar.get(SECOND)
				+ ":" + calendar.get(MILLISECOND) + "]   [" + mat.m00() + " " + mat.m10() + " " + mat.m20() + " "
				+ mat.m30() + " ]");
		System.out.println("[" + calendar.get(HOUR_OF_DAY) + ":" + calendar.get(MINUTE) + ":" + calendar.get(SECOND)
				+ ":" + calendar.get(MILLISECOND) + "]   [" + mat.m01() + " " + mat.m11() + " " + mat.m21() + " "
				+ mat.m31() + " ]");
		System.out.println("[" + calendar.get(HOUR_OF_DAY) + ":" + calendar.get(MINUTE) + ":" + calendar.get(SECOND)
				+ ":" + calendar.get(MILLISECOND) + "]   [" + mat.m02() + " " + mat.m12() + " " + mat.m22() + " "
				+ mat.m32() + " ]");
		System.out.println("[" + calendar.get(HOUR_OF_DAY) + ":" + calendar.get(MINUTE) + ":" + calendar.get(SECOND)
				+ ":" + calendar.get(MILLISECOND) + "]   [" + mat.m03() + " " + mat.m13() + " " + mat.m23() + " "
				+ mat.m33() + " ]");
	}

}
