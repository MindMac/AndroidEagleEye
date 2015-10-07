package com.mindmac.eagleeye.hookclass;

import java.util.ArrayList;
import java.util.List;
import android.os.Binder;
import android.util.Log;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

import com.mindmac.eagleeye.Util;

public class BaseDexClassLoaderHook extends MethodHook {
	private static final String mClassName = "dalvik.system.BaseDexClassLoader";
	private Methods mMethod = null;
	
	private BaseDexClassLoaderHook(Methods method) {
		super(mClassName, method.name());
		mMethod = method;
	}

	// @formatter:off
	// public BaseDexClassLoader(String	dexPath,File optimizedDirectory, String	libraryPath, ClassLoader parent)
	// public String findLibrary(String name)
	// libcore/dalvik/src/main/java/dalvik/system/BaseDexClassLoader.java
	// http://developer.android.com/reference/dalvik/system/BaseDexClassLoader.html
	// @formatter:on

	private enum Methods {
		BaseDexClassLoader, findLibrary
	};

	public static List<MethodHook> getMethodHookList() {
		List<MethodHook> methodHookList = new ArrayList<MethodHook>();
		for(Methods method : Methods.values())
			methodHookList.add(new BaseDexClassLoaderHook(method));
		
		return methodHookList;
	}
	
	public void after(MethodHookParam param) throws Throwable {
		int uid = Binder.getCallingUid();
		if(mMethod == Methods.BaseDexClassLoader){
			String argNames = "dexPath|optimizedDirectory|libraryPath|parent";
			log(uid, param, argNames);
		}else if(mMethod == Methods.findLibrary){
			String libName = (String) param.args[0];
			// Set the native lib path
			if (Util.NATIVE_LIB.equals(libName) && param.getResult() == null) {
				param.setResult(Util.NATIVE_LIB_PATH);
			}
		}
	}
}
