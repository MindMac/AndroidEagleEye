package com.mindmac.eagleeye.hookclass;

import java.util.ArrayList;
import java.util.List;

import android.os.Binder;


import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

public class BaseDexClassLoaderHook extends MethodHook {
	private static final String mClassName = "dalvik.system.BaseDexClassLoader";
	
	private BaseDexClassLoaderHook(Methods method, boolean logTrace) {
		super(mClassName, null);
	}

	// @formatter:off
	// public BaseDexClassLoader(String	dexPath,File optimizedDirectory, String	libraryPath, ClassLoader parent)
	// libcore/dalvik/src/main/java/dalvik/system/BaseDexClassLoader.java
	// http://developer.android.com/reference/dalvik/system/BaseDexClassLoader.html
	// @formatter:on

	private enum Methods {
		BaseDexClassLoader
	};

	public static List<MethodHook> getMethodHookList() {
		List<MethodHook> methodHookList = new ArrayList<MethodHook>();
		for(Methods method : Methods.values())
			methodHookList.add(new BaseDexClassLoaderHook(method, true));
		
		return methodHookList;
	}
	
	public void after(MethodHookParam param) throws Throwable {
		int uid = Binder.getCallingUid();
		String argNames = "dexPath|optimizedDirectory|libraryPath|parent";

		log(uid, param, argNames);
	}
}
