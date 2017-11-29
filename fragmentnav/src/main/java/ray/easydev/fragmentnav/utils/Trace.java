package ray.easydev.fragmentnav.utils;

import android.util.Log;

import java.io.File;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Trace {
	/**
	 * 全局日志开关
	 */
	private static boolean GLOBAL_SWITCH = true;//NEConfig.SDK_DEBUG;
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
	
	public static void setRootDumpPath(String dir){
		p(Trace.class, "日志路径:%s",  dir);
		DIR_ROOT_DUMP = dir;
	}
	
	public static void dateDump(String msg, String fileName){
		String date = sDateFormat.format(new Date(System.currentTimeMillis()));
		dump(date + msg, fileName);
	}
	
	public static synchronized boolean dump(String msg, String fileName){
		if(!fileName.contains(".")){
			fileName += ".log";
		}
		
		String prefix = new SimpleDateFormat("MM-dd").format(new Date(System.currentTimeMillis()));
		File file = new File(getLogPath() + prefix, fileName);
		
		try{
			File parent = file.getParentFile();
			if(!parent.exists() || !parent.isDirectory()){
				parent.mkdirs();
			}
			
			if(!file.isFile() || !file.exists()){
				file.createNewFile();
			}
			
			StringBuilder sb = new StringBuilder();
			sb.append(msg).append("\n");
			RandomAccessFile raf = new RandomAccessFile(file, "rwd");
			raf.seek(file.length());
			raf.write(sb.toString().getBytes("utf-8"));
			raf.close();
			
			return true;
		} catch (Exception e){
			
		}
		
		return false;
	}
	
	public static String getLogPath(){
		return DIR_ROOT_DUMP;
	}
	
	public static void w(String tag, Object msg, Object... args){
		if(isLogoutEnabled(LogLevel.W, tag))
			Log.w(tag, objToString(msg,args));
	}
	
	public static void w(Class<?> cls, Object msg, Object... args){
		if(isLogoutEnabled(LogLevel.W, cls))
			Log.w(tagFromClass(cls), objToString(msg, args));
	}
	
	public static void i(String tag, Object msg, Object... args){
		if(isLogoutEnabled(LogLevel.I, tag))
			Log.i(tag, objToString(msg,args));
	}
	
	public static void i(Class<?> cls, Object msg, Object... args){
		if(isLogoutEnabled(LogLevel.I, cls))
			Log.i(tagFromClass(cls), objToString(msg, args));
	}
	
	public static void e(String tag, Object msg, Object... args){
		if(isLogoutEnabled(LogLevel.E, tag))
			Log.e(tag, objToString(msg, args));
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

	/**
	 * 取stack信息中的第一和第二行，一般来说第一行为异常类型，第二行为抛异常的位置
	 * @param ex
	 * @return
     */
	public static String simpleStackTrace(Throwable ex){
		String info = strackTrace(ex);
		StringBuilder sb = new StringBuilder();
		//取第一和第二行
		String[] infoParts = info.split("\n");
		int i = 0;
		for(String str : infoParts){
			if(i > 1){
				break;
			}
			if(i == 0){
				str = str.replace(ex.getClass().getCanonicalName(), ex.getClass().getSimpleName());
			}

			sb.append(str).append("\n");
			i ++;
		}

		return sb.toString();
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
//		XLogout logOut = tag == null ? null : cls.getAnnotation(XLogout.class);
		if(logOut != null && logOut.value() >= level){
			return true;
		} else if (logOut == null){
			return LOG_LEVEL >= level;
		}

		return false;
	}
}
