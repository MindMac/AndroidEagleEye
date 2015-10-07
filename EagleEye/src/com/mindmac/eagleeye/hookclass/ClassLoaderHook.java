package com.mindmac.eagleeye.hookclass;

import java.util.ArrayList;
import java.util.List;

import com.mindmac.eagleeye.Util;
import com.mindmac.eagleeye.service.Launcher;

import android.os.Binder;
import android.util.Log;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

public class ClassLoaderHook extends MethodHook {
	private static final String mClassName = "java.lang.ClassLoader";
	
	private ClassLoaderHook(Methods method) {
		super(mClassName, method.name());
	}

	// @formatter:off
    // public Class<?> loadClass(String className) throws ClassNotFoundException 
	// libcore/libart/src/main/java/java/lang/ClassLoader.java
	// @formatter:on

	private enum Methods {
		loadClass
	};

	public static List<MethodHook> getMethodHookList() {
		List<MethodHook> methodHookList = new ArrayList<MethodHook>();
		for(Methods method : Methods.values())
			methodHookList.add(new ClassLoaderHook(method));
		
		return methodHookList;
	}
	
	public void after(MethodHookParam param) throws Throwable {
		int uid = Binder.getCallingUid();
		if(uid <= 1000)
			return;
		
		if(!Util.isAppNeedFrLog(uid))
			return;
		
		if(param.args.length != 1)
			return;
		
		if(Util.FRAMEWORK_APP_UN_HOOKED_APIS.size() > 0){
			ArrayList<String> tmpUnHookedApis = Util.copyArrayList(Util.FRAMEWORK_APP_UN_HOOKED_APIS);
			Class<?> loadedClass = (Class<?>) param.getResult();
			for(String methodInfo : tmpUnHookedApis){
				if(Launcher.hookCustomizeWithKnownClass(methodInfo, loadedClass, Util.FRAMEWORK_HOOK_APP_API))
					Util.FRAMEWORK_APP_UN_HOOKED_APIS.remove(methodInfo);
			}
		}

	}
}
