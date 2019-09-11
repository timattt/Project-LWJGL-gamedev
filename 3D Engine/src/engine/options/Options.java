package engine.options;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import utilities.Console;

public abstract class Options {

	// File
	protected File dir;

	protected Options() {}

	private void setDir(File dir) {
		this.dir = dir;
	}
	
	public abstract void createNewOptionsFile() throws IOException;

	protected abstract void loadOptions() throws NumberFormatException, IOException;

	protected static String cutComments(String arg) {
		return arg.substring(arg.indexOf(':') + 1);
	}

	public static Options loadOptions(Class<? extends Options> cl, File path) throws InstantiationException, IllegalAccessException, NumberFormatException, IOException {
		Options opts = null;
		try {
			opts = cl.getDeclaredConstructor().newInstance();
		} catch (IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		opts.setDir(path);
		
		opts.loadOptions();
		Console.println("Loaded options: " + opts.toString());
		
		return opts;
	}
	
	public static Options createNewOptions(Class<? extends Options> cl, File path) throws IOException, InstantiationException, IllegalAccessException {
		Options opts = null;
		try {
			opts = cl.getDeclaredConstructor().newInstance();
		} catch (IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		opts.setDir(path);
		
		opts.createNewOptionsFile();
		Console.println("Created options: " + opts.toString());
		
		return opts;
	}
}
