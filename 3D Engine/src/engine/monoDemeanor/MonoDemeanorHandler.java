/**
 * 
 */
package engine.monoDemeanor;

import static engine.monoDemeanor.MonoDemeanorPriority.HIGH;
import static engine.monoDemeanor.MonoDemeanorPriority.LOW;
import static engine.monoDemeanor.MonoDemeanorPriority.MEDIUM;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import utilities.Console;

/**
 * @author timat
 *
 */
public final class MonoDemeanorHandler {

	// Mono demeanors
	private final LinkedList<MonoDemeanorPack> loadedMonoDemeanors = new LinkedList<MonoDemeanorPack>();

	private String lib_path;
	private String other_path;

	public MonoDemeanorHandler(String lib, String other) {
		this.lib_path = new String(lib);
		this.other_path = new String(other);

		if (!lib_path.endsWith("/")) {
			lib_path = lib_path + "/";
		}
		if (!other_path.endsWith("/")) {
			other_path = other_path + "/";
		}
	}

	public void preInitMonoDemeanors()
			throws ClassNotFoundException, IllegalArgumentException, IllegalAccessException, IOException {
		loadMonoDemeanors(loadedMonoDemeanors, lib_path);
		loadMonoDemeanors(loadedMonoDemeanors, other_path);
	}

	private void loadMonoDemeanors(LinkedList<MonoDemeanorPack> md, String dir)
			throws ClassNotFoundException, IOException, IllegalArgumentException, IllegalAccessException {

		LinkedList<Class<?>> clazzes = new LinkedList<Class<?>>();
		try {
			recursDirectory("", clazzes, new File(dir));
		} catch (Exception e) {
			loadClasses(clazzes, dir);
		}

		Iterator<Class<?>> iter = clazzes.iterator();

		while (iter.hasNext()) {
			Class<?> clazz = iter.next();
			boolean hasAnn = false;
			boolean allConstrPrivate = true;
			for (Annotation a : clazz.getAnnotations()) {
				if (a.annotationType() == MonoDemeanor.class) {
					hasAnn = true;
					break;
				}
			}

			for (Constructor<?> con : clazz.getConstructors()) {
				if (!Modifier.isPrivate(con.getModifiers()) && !Modifier.isProtected(con.getModifiers())) {
					allConstrPrivate = false;
					break;
				}
			}
			if (!hasAnn || !allConstrPrivate) {
				continue;
			}
			md.add(new MonoDemeanorPack(clazz));
		}

	}

	public void init() throws Exception {

		// High priority
		for (MonoDemeanorPack p : loadedMonoDemeanors) {
			if (p.priority_init == HIGH && p.init != null && !p.initComplete) {
				p.init.invoke(p.instance);
				p.switchInitMode();
			}
		}

		// Medium priority
		for (MonoDemeanorPack p : loadedMonoDemeanors) {
			if (p.priority_init == MEDIUM && p.init != null && !p.initComplete) {
				p.init.invoke(p.instance);
				p.switchInitMode();
			}
		}

		// Low priority
		for (MonoDemeanorPack p : loadedMonoDemeanors) {
			if (p.priority_init == LOW && p.init != null && !p.initComplete) {
				p.init.invoke(p.instance);
				p.switchInitMode();
			}
		}
	}

	public void update() throws Exception {

		// High priority
		for (MonoDemeanorPack p : loadedMonoDemeanors) {
			if (p.priority_update == HIGH && p.update != null) {
				p.update.invoke(p.instance);
			}
		}

		// Medium priority
		for (MonoDemeanorPack p : loadedMonoDemeanors) {
			if (p.priority_update == MEDIUM && p.update != null) {
				p.update.invoke(p.instance);
			}
		}

		// Low priority
		for (MonoDemeanorPack p : loadedMonoDemeanors) {
			if (p.priority_update == LOW && p.update != null) {
				p.update.invoke(p.instance);
			}
		}

	}

	public void cleanup() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		// High priority
		for (MonoDemeanorPack p : loadedMonoDemeanors) {
			if (p.priority_cleanup == HIGH && p.cleanup != null && !p.cleanupComplete) {
				p.cleanup.invoke(p.instance);
				p.switchInitMode();
			}
		}

		// Medium priority
		for (MonoDemeanorPack p : loadedMonoDemeanors) {
			if (p.priority_cleanup == MEDIUM && p.cleanup != null && !p.cleanupComplete) {
				p.cleanup.invoke(p.instance);
				p.switchInitMode();
			}
		}

		// Low priority
		for (MonoDemeanorPack p : loadedMonoDemeanors) {
			if (p.priority_cleanup == LOW && p.cleanup != null && !p.cleanupComplete) {
				p.cleanup.invoke(p.instance);
				p.switchInitMode();
			}
		}

	}

	private static void recursDirectory(String packageName, LinkedList<Class<?>> result, File f)
			throws ClassNotFoundException, IOException {
		File[] files = f.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				recursDirectory(packageName + '.' + files[i].getName(), result, files[i]);
			}
			if (files[i].getName().endsWith(".class") && !files[i].getName().contains("$")) {
				result.add(ClassLoader.getSystemClassLoader()
						.loadClass(packageName.replaceFirst(".", "") + "." + files[i].getName().replace(".class", "")));
			}
		}

	}

	private static void loadClasses(LinkedList<Class<?>> clazzes, String path)
			throws IOException, ClassNotFoundException {
		JarFile jarFile = new JarFile(path);
		Enumeration<JarEntry> e = jarFile.entries();

		URL[] urls = { new URL("jar:file:" + path + "!/") };
		URLClassLoader cl = URLClassLoader.newInstance(urls);

		while (e.hasMoreElements()) {
			JarEntry je = e.nextElement();
			if (je.isDirectory() || !je.getName().endsWith(".class")) {
				continue;
			}
			// -6 because of .class
			String className = je.getName().substring(0, je.getName().length() - 6);
			className = className.replace('/', '.');
			Class<?> c = cl.loadClass(className);

			clazzes.add(c);
		}

		jarFile.close();
	}

	/*
	 * @SuppressWarnings({ "unchecked", "rawtypes" }) private static void
	 * getClasses(LinkedList<Class<?>> classes, String packageName) throws
	 * ClassNotFoundException, IOException { ClassLoader classLoader =
	 * Thread.currentThread().getContextClassLoader(); assert classLoader != null;
	 * String path = packageName.replace('.', '/'); Enumeration resources =
	 * classLoader.getResources(path); List<File> dirs = new ArrayList(); while
	 * (resources.hasMoreElements()) { URL resource = (URL) resources.nextElement();
	 * dirs.add(new File(resource.getFile())); }
	 * 
	 * for (File directory : dirs) { classes.addAll(findClasses(directory,
	 * packageName)); }
	 * 
	 * }
	 * 
	 * @SuppressWarnings({ "unchecked", "rawtypes" }) private static List
	 * findClasses(File directory, String packageName) throws ClassNotFoundException
	 * { List classes = new ArrayList(); if (!directory.exists()) { return classes;
	 * } File[] files = directory.listFiles(); for (File file : files) { if
	 * (file.isDirectory()) { assert !file.getName().contains(".");
	 * classes.addAll(findClasses(file, packageName + "." + file.getName())); } else
	 * if (file.getName().endsWith(".class")) { classes.add(
	 * Class.forName(packageName + '.' + file.getName().substring(0,
	 * file.getName().length() - 6))); } } return classes; }
	 */
	private class MonoDemeanorPack {
		private Object instance;
		private Method init;
		private Method update;
		private Method cleanup;
		private MonoDemeanorPriority priority_init;
		private MonoDemeanorPriority priority_update;
		private MonoDemeanorPriority priority_cleanup;

		private boolean initComplete = false;
		private boolean cleanupComplete = true;

		public MonoDemeanorPack(Class<?> clazz) throws IllegalArgumentException, IllegalAccessException {
			super();
			Console.print(Console.generateDateString() + " Initializing monodemeanor class: " + clazz.getName() + " {");

			// Instance
			for (Field f : clazz.getDeclaredFields()) {
				for (Annotation a : f.getAnnotations()) {
					if (a.annotationType() == MonoDemeanorInstance.class) {
						instance = f.get(null);
					}
				}
			}

			// Methods
			for (Method m : clazz.getMethods()) {
				for (Annotation a : m.getAnnotations()) {
					if (a.annotationType() == MonoDemeanorInit.class) {
						init = m;
					}
					if (a.annotationType() == MonoDemeanorUpdate.class) {
						update = m;
					}
					if (a.annotationType() == MonoDemeanorCleanup.class) {
						cleanup = m;
					}
				}
			}

			if (init != null) {
				priority_init = init.getAnnotation(MonoDemeanorInit.class).priority();
				Console.print("init priority: [" + priority_init.toString() + "] ");
			}
			if (update != null) {
				priority_update = update.getAnnotation(MonoDemeanorUpdate.class).priority();
				Console.print("update priority: [" + priority_update.toString() + "] ");
			}
			if (cleanup != null) {
				priority_cleanup = cleanup.getAnnotation(MonoDemeanorCleanup.class).priority();
				Console.print("cleanup priority: [" + priority_cleanup.toString() + "] ");
			}

			Console.print("}");
			Console.skipLine();
		}

		public void switchInitMode() {
			cleanupComplete = !cleanupComplete;
			initComplete = !initComplete;
		}
	}

}
