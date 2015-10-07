package com.mindmac.eagleeye;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;

public class Util {
	public static final String SELF_PACKAGE_NAME = "com.mindmac.eagleeye";
	public static final String LOG_TAG = "EagleEye";
	
	public static final String NATIVE_LIB = "eagleeyenative";
	public static final String NATIVE_LIB_PATH = String.format("/data/data/%s/lib/lib%s.so", 
		Util.SELF_PACKAGE_NAME, Util.NATIVE_LIB);
	
	// Native Hook
	public static final String NATIVE_UIDS_PROP_KEY = "rw.eagleeye.nt.uids";
	public static final String CUSTOM_NATIVE_LIB_NAMES_CONFIG = "native_lib.config";
	public static final HashMap<Integer, Boolean> NATIVE_UIDS_MAP = new 
			HashMap<Integer, Boolean>();
	public static final HashMap<String, Boolean> CUSTOM_NATIVE_LIB_NAMES_MAP = new
			HashMap<String, Boolean>();
	
	// Java framework Hook
	public static final String FRAMEWORK_UIDS_PROP_KEY = "rw.eagleeye.fr.uids";
	public static HashMap<Integer, Boolean> FRAMEWORK_UIDS_MAP = new 
			HashMap<Integer, Boolean>();
	
	public static final String FRAMEWORK_SYSTEM_API_NUM_PROP_KEY = "rw.eagleeye.fr.sys_api_num";
	public static int FRAMEWORK_SYSTEM_API_NUM = 500;
	
	public static final String FRAMEWORK_SYSTEM_API_HOOK_CONFIG = "fr_sys_apis.config";
	public static ArrayList<String> FRAMEWORK_SYSTEM_UN_HOOKED_APIS = new ArrayList<String>();

	
	public static final String FRAMEWORK_APP_API_NUM_PROP_KEY = "rw.eagleeye.fr.app_api_num";
	public static int FRAMEWORK_APP_API_NUM = 500;

	public static final String FRAMEWORK_APP_API_HOOK_CONFIG = "fr_app_apis.config";
	public static ArrayList<String> FRAMEWORK_APP_UN_HOOKED_APIS = new ArrayList<String>();
	
	public static final int ANDROID_UID = android.os.Process.SYSTEM_UID;
	
	public static final int FRAMEWORK_HOOK_SYSTEM_API = 0x00;
	public static final int FRAMEWORK_HOOK_APP_API = 0x01;
			
	public static int DATA_BYTES_TO_LOG = 768;
		
	public static ArrayList<String> copyArrayList(ArrayList<String> srcArrayList){
		ArrayList<String> dstArrayList = new ArrayList<String>();
		for(String ele : srcArrayList)
			dstArrayList.add(ele);
		return dstArrayList;
	}
	
	public static boolean isExternalStorageWritable() {
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state)) {
	        return true;
	    }
	    return false;
	}
	
	public static String toHex(byte[] buf) {
		StringBuffer hexString = new StringBuffer();
		for (int i = 0; i < buf.length; i++) {
			String h = Integer.toHexString(0xFF & buf[i]);
			while (h.length() < 2)
				h = "0" + h;

			hexString.append(h);
		}

		return  hexString.toString();
	}	
	
	public static void storeNativeLogAppUids(){
		String targetUids = getSystemProperty(Util.NATIVE_UIDS_PROP_KEY);
		if(targetUids != null)
			try{
				String[] targetUidArray = targetUids.split("\\|");
				for(String targetUid : targetUidArray){
					targetUid = targetUid.trim();
					Util.NATIVE_UIDS_MAP.put(Integer.parseInt(targetUid), true);
				}
			}catch(Exception ex){
				ex.printStackTrace();
			}
	}
	
	public static void storeFrameworkLogAppUids(){
		String targetUids = getSystemProperty(Util.FRAMEWORK_UIDS_PROP_KEY);
		if(targetUids != null)
			try{
				String[] targetUidArray = targetUids.split("\\|");
				for(String targetUid : targetUidArray){
					targetUid = targetUid.trim();
					Util.FRAMEWORK_UIDS_MAP.put(Integer.parseInt(targetUid), true);
				}
			}catch(Exception ex){
				ex.printStackTrace();
			}
	}
	
	public static boolean isAppNeedNtLog(int uid){
		return Util.NATIVE_UIDS_MAP.containsKey(uid);
	}

	public static boolean isAppNeedFrLog(int uid){
		return Util.FRAMEWORK_UIDS_MAP.containsKey(uid);
	}
	

	
	public static String getSystemProperty(String propertyKey){
		Class<?> targetClass = null;
		Method  targetMethod = null;
		String propertyValue = null;
		try {
			if (targetClass == null) {
				targetClass = Class.forName("android.os.SystemProperties");
				targetMethod = targetClass.getDeclaredMethod("get", String.class);
				propertyValue = (String) targetMethod.invoke(targetClass, propertyKey);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(propertyValue != null && propertyValue.equals(""))
			propertyValue = null;
		return propertyValue;
	}
	
	public static int getFd(FileDescriptor fileDescriptor){
		int fdInt = -1;
		try{
			if(fileDescriptor != null){
				Field descriptor = fileDescriptor.getClass().getDeclaredField("descriptor");
				descriptor.setAccessible(true);
				fdInt = descriptor.getInt(fileDescriptor);
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return fdInt;
	}
	
	public static int getTimeId(){
		int x = (int) System.nanoTime();
        x ^= (x << 21);
        x ^= (x >>> 35);
        x ^= (x << 4);
        if (x < 0)
           	x = 0-x;
        return x;
	}
	
	public static void copyAsset(Context context, String assetFileName, String dstFile) {
		AssetManager assetManager = null;
		assetManager = context.getAssets();
		InputStream inputStream = null;
		FileOutputStream fileOutputStream = null;

		try {
			inputStream = assetManager.open(assetFileName);
			fileOutputStream = new FileOutputStream(dstFile);
			byte[] buffer = new byte[inputStream.available()];
			inputStream.read(buffer);
			fileOutputStream.write(buffer);
			fileOutputStream.flush();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				inputStream.close();
				fileOutputStream.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
//	public static void execSuCmd(String cmd){
//		Process process = null;
//        OutputStream out = null;
//        
//        try {
//            process = Runtime.getRuntime().exec("su");
//            out = process.getOutputStream();
//            out.write((cmd + "\n").getBytes());
//            out.write("exit\n".getBytes());
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        } catch (Exception ex) {
//           ex.printStackTrace();
//        } finally {
//            try {
//                out.flush();
//                out.close();
//            } catch (Exception ex) {
//               ex.printStackTrace();
//            }
//        }
//	}
	
	public static String parseParameterTypes(Method method) {
		String parameterTypes = "";
		for (Class<?> parameterClass : method.getParameterTypes())
			parameterTypes += parseClassType(parameterClass);
		return parameterTypes;
	}

	public static String parseParameterTypes(Constructor<?> constructor) {
		String parameterTypes = "";
		for (Class<?> parameterClass : constructor.getParameterTypes())
			parameterTypes += parseClassType(parameterClass);
		return parameterTypes;
	}

	
	public static String parseReturnType(Method method) {
		String returnType = "";
		Class<?> returnClass = method.getReturnType();
		returnType = parseClassType(returnClass);
		return returnType;
	}

	public static String parseClassType(Class<?> classInst) {
		String classType = "";
		String className = classInst.getName();
		// Primitive type
		if (className.equals("void"))
			classType = "V";
		else if (className.equals("byte"))
			classType = "B";
		else if (className.equals("short"))
			classType = "S";
		else if (className.equals("int"))
			classType = "I";
		else if (className.equals("long"))
			classType = "L";
		else if (className.equals("float"))
			classType = "F";
		else if (className.equals("double"))
			classType = "D";
		else if (className.equals("char"))
			classType = "C";
		else if (className.equals("boolean"))
			classType = "Z";
		// Class type
		else if (className.indexOf(".") != -1) {
			classType = className.replace(".", "/");
			if (className.indexOf(";") == -1)
				classType = classType + ";";
			if (className.indexOf("L") == -1)
				classType = "L" + classType;
		} else
			classType = className;

		return classType;
	}
	
	public static String generateRandomNums(int numCount){
		StringBuilder randomNums = new StringBuilder();
		Random randomGenerator = new Random();
	    for (int i = 1; i < numCount; i++){
	      int randomInt = randomGenerator.nextInt(10);
	      randomNums.append(randomInt);
	    }
	    return randomNums.toString();
	}
	
	public static String generateRandomStrs(int strCount){
		StringBuilder randomStrs = new StringBuilder();
		Random randomGenerator = new Random();
	    for (int i = 1; i < strCount; i++){
	      int randomInt = randomGenerator.nextInt(26);
	      randomStrs.append((char)(randomInt + 'a'));
	    }

		return randomStrs.toString();
	}
	
}


