package com.mindmac.eagleeye.hookclass;

import java.util.ArrayList;
import java.util.List;


import android.os.Binder;

import de.robv.android.xposed.XC_MethodHook.MethodHookParam;


public class RuntimeHook extends MethodHook {
	private Methods mMethod = null;
	private static final String mClassName = "java.lang.Runtime";

	private RuntimeHook(Methods method) {
		super(mClassName, method.name());
		mMethod = method;
	}


	// public Process exec(String[] progArray, String[] envp, File directory)
	// void load(String filename, ClassLoader loader)
	// void loadLibrary(String libraryName, ClassLoader loader)
	// libcore/luni/src/main/java/java/lang/Runtime.java
	// http://developer.android.com/reference/java/lang/Runtime.html

	private enum Methods {
		exec, load, loadLibrary
	};

	public static List<MethodHook> getMethodHookList() {
		List<MethodHook> methodHookList = new ArrayList<MethodHook>();
		for(Methods method : Methods.values())
			methodHookList.add(new RuntimeHook(method));
		
		return methodHookList;
	}
	
	@Override
	public void after(MethodHookParam param) throws Throwable {
		int uid = Binder.getCallingUid();
		String argNames = null;
		
		if(mMethod == Methods.load){
			argNames = "filename|loader";
			log(uid, param, argNames);
		}else if(mMethod == Methods.loadLibrary){
			argNames = "library|loader";
			log(uid, param, argNames);
		}else if(mMethod == Methods.exec){
			if(param.args[0] instanceof String[] && param.args.length == 3){
				log(uid, param, argNames);
			}
		}	
	}
}
