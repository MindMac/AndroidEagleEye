package com.mindmac.eagleeye.hookclass;

import java.util.ArrayList;
import java.util.List;

public class ActivityManagerHook extends MethodHook {
	private Methods mMethod = null;
	
	private ActivityManagerHook(String className, Methods method) {
		super(className, method.name());
		mMethod = method;
	}

	// public List<RecentTaskInfo> getRecentTasks(int maxNum, int flags)
	// public List<RunningAppProcessInfo> getRunningAppProcesses()
	// public List<RunningServiceInfo> getRunningServices(int maxNum)
	// public List<RunningTaskInfo> getRunningTasks(int maxNum)
	// frameworks/base/core/java/android/app/ActivityManager.java
	// http://developer.android.com/reference/android/app/ActivityManager.html

	private enum Methods {
		getRecentTasks, getRunningAppProcesses, getRunningServices, getRunningTasks
	};

	public static List<MethodHook> getMethodHookList(Object instance) {
		String className = instance.getClass().getName();
		List<MethodHook> methodHookList = new ArrayList<MethodHook>();
		for (Methods method : Methods.values())
			methodHookList.add(new ActivityManagerHook(className, method));
		return methodHookList;
	}
	
//	public void after(MethodHookParam param) throws Throwable {
//		int uid = Binder.getCallingUid();
//		String argNames = null;
//		if(mMethod == Methods.getRecentTasks)
//			argNames = "maxNum|flags";
//		else if(mMethod == Methods.getRunningServices || mMethod == Methods.getRunningTasks)
//			argNames = "maxNum";
//		
//		log(uid, param, argNames);
//	}
}
