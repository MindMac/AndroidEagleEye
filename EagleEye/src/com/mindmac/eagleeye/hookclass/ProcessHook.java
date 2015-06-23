package com.mindmac.eagleeye.hookclass;

import java.util.ArrayList;
import java.util.List;

import android.os.Binder;

import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

public class ProcessHook extends MethodHook {
	private Methods mMethod = null;
	private static final String mClassName = "android.os.Process";

	private ProcessHook(Methods method) {
		super(mClassName, method.name());
		mMethod = method;
	}

	// public static final void killProcess (int pid)
	// frameworks/base/core/java/android/os/Process.java
	// http://developer.android.com/reference/android/os/Process.html

	private enum Methods {
		killProcess
	};

	public static List<MethodHook> getMethodHookList() {
		List<MethodHook> methodHookList = new ArrayList<MethodHook>();
		methodHookList.add(new ProcessHook(Methods.killProcess));
		return methodHookList;
	}
	
	@Override
	public void after(MethodHookParam param) throws Throwable {
		int uid = Binder.getCallingUid();
		String argNames = null;
		
		if(mMethod == Methods.killProcess){
			argNames = "pid";
		}

		log(uid, param, argNames);
	}
}
