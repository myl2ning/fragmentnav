package ray.easydev.fragmentnav.utils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface LogLevel {
	int value();

	public final static int OFF = 0;
	public final static int V = 10;
	public final static int D = 5;
	public final static int I = 4;
	public final static int W = 3;
	public final static int E = 2;
	public final static int F = 1;
	
}
