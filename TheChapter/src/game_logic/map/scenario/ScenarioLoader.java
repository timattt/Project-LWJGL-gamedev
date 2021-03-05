package game_logic.map.scenario;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class ScenarioLoader {

	// Jar
	private final File jarFile;

	// Classes
	private final LinkedList<Class<?>> classes = new LinkedList<Class<?>>();

	private Class<? extends Scenario> scenarioClass;

	public ScenarioLoader(File jarFile) {
		this.jarFile = jarFile;
	}

	public final void loadClasses() throws IOException, ClassNotFoundException {
		JarFile jarFile = new JarFile(this.jarFile.getPath());
		Enumeration<JarEntry> e = jarFile.entries();

		URL[] urls = { new URL("jar:file:" + this.jarFile.getPath() + "!/") };
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

			classes.add(c);
		}

		jarFile.close();
	}

	@SuppressWarnings("unchecked")
	public final void findScenarioClass() {
		for (Class<?> cl : classes) {
			if (cl.getSuperclass() == Scenario.class) {
				scenarioClass = (Class<? extends Scenario>) cl;
			}
		}
	}

	public final Scenario createScenario() throws InstantiationException, IllegalAccessException {	
		Scenario scenario = null;
		try {
			scenario = scenarioClass.getDeclaredConstructor().newInstance();
		} catch (IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		scenario.setLocalResourcesPath((this.jarFile.getParent() + "/Resources"));
		return scenario;
	}

	public final Scenario process() {
		try {
			loadClasses();
			findScenarioClass();
			return createScenario();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
