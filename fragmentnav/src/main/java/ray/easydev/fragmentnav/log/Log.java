package ray.easydev.fragmentnav.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class Log {
	/**
	 * 全局日志开关
	 */
	private static boolean GLOBAL_SWITCH = true;
	private static int LOG_LEVEL = LogLevel.OFF;
	private static String DIR_ROOT_DUMP;
	private static SimpleDateFormat sDateFormat = new SimpleDateFormat("[MM-dd HH:mm:ss]:");
	private static List<Object> sCares = new ArrayList();
	private static List<Object> sIgnores = new ArrayList();
	
	public static void care(Object... tags){
		if(tags != null){
			for(Object tag : tags){
                if(tag != null){
                    sCares.add(tag);
                    if(tag instanceof Class){
                        sCares.add(((Class) tag).getSimpleName());
                    }
                }
			}
		}
	}
	
	public static void ignore(Object... tags){
		if(tags != null){
			for(Object tag : tags){
                if(tag != null){
                    sIgnores.add(tag);
                    if(tag instanceof Class){
                        sIgnores.add(((Class) tag).getSimpleName());
                    }
                }
			}
		}
	}
	
	public static void setLogLevel(boolean global, int level){
        GLOBAL_SWITCH = global;
		LOG_LEVEL = level;
	}
	
	public static void w(String tag, Object msg, Object... args){
		if(isLogoutEnabled(LogLevel.W, tag))
			android.util.Log.w(tag, objToString(msg,args));
	}
	
	public static void w(Class<?> cls, Object msg, Object... args){
		if(isLogoutEnabled(LogLevel.W, cls))
			android.util.Log.w(tagFromClass(cls), objToString(msg, args));
	}
	
	public static void i(String tag, Object msg, Object... args){
		if(isLogoutEnabled(LogLevel.I, tag))
			android.util.Log.i(tag, objToString(msg,args));
	}
	
	public static void i(Class<?> cls, Object msg, Object... args){
		if(isLogoutEnabled(LogLevel.I, cls))
			android.util.Log.i(tagFromClass(cls), objToString(msg, args));
	}
	
	public static void e(String tag, Object msg, Object... args){
		if(isLogoutEnabled(LogLevel.E, tag))
			android.util.Log.e(tag, objToString(msg, args));
	}
	
	public static void p(String tag, Object msg, Object... args){
		if(isLogoutEnabled(LogLevel.I, tag))
			System.out.println("[" + tag + "]:" + objToString(msg, args));
	}
	
	public static void p(Class<?> cls, Object msg, Object... args){
		if(isLogoutEnabled(LogLevel.I, cls)){
			p(tagFromClass(cls), msg, args);
		}
	}
	
	public static void e(Class<?> cls, Object msg, Object... args){
		if(isLogoutEnabled(LogLevel.E, cls)){
			e(tagFromClass(cls), msg, args);
		}
	}

	static String tagFromClass(Class<?> cls){
        while(cls != null && cls.isAnonymousClass()){
            cls = cls.getEnclosingClass();
        }
		return cls == null ? "" : cls.getSimpleName();
	}

	public static void pStack(Class<?> cls, Throwable ex){
		p(cls, strackTrace(ex));
	}

	public static void eStack(String tag, Throwable ex){
		e(tag, strackTrace(ex));
	}

    public static void pStack(String tag, Throwable ex){
        p(tag, strackTrace(ex));
    }

    public static void eStack(Class<?> cls, Throwable ex){
        e(cls, strackTrace(ex));
    }

	public static String strackTrace(Throwable ex){
        if(ex == null){
            return "";
        }
		StringWriter sw = new StringWriter(1024);
		ex.printStackTrace(new PrintWriter(sw));
		sw.append("\n");
		return sw.toString();
	}

	private static String objToString(Object obj, Object... args){
        try{
            return obj == null ? "NULL" : ((args == null || args.length == 0) ? obj.toString() : String.format(obj.toString(), args));
        } catch (Exception e){
            return e + "";
        }
	}
	
	public static boolean isLogoutEnabled(int level, Object tag){
		if(!GLOBAL_SWITCH){
			return false;
		}
		
		if(LOG_LEVEL < level){
			return false;
		}
		
		if(tag != null){
			if(sCares.size() > 0 && !sCares.contains(tag)){
				return false;
			}
			
			if(sIgnores.size() > 0 && sIgnores.contains(tag)){
				return false;
			}
		}

        LogLevel logOut = null;
        if(tag instanceof Class){
            Class cls = (Class) tag;
            logOut = (LogLevel) cls.getAnnotation(LogLevel.class);
        }

		if(logOut != null && logOut.value() >= level){
			return true;
		} else if (logOut == null){
			return LOG_LEVEL >= level;
		}

		return false;
	}
}
