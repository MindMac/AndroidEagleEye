package com.mindmac.eagleeye.hookclass;

import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

import android.annotation.SuppressLint;
import android.os.Binder;

public class ActivityThreadHook extends MethodHook {
	private Methods mMethod = null;
	private static final String mClassName = "android.app.ActivityThread";

	private ActivityThreadHook(Methods method) {
		super(mClassName, method.name());
		mMethod = method;
	}

	private enum Methods {
		handleReceiver
	};
	

	// @formatter:off

	// private void handleReceiver(ReceiverData data)
	// frameworks/base/core/java/android/app/ActivityThread.java

	// @formatter:on

	@SuppressLint("InlinedApi")
	public static List<MethodHook> getMethodHookList() {
		List<MethodHook> methodHookList = new ArrayList<MethodHook>();

		methodHookList.add(new ActivityThreadHook(Methods.handleReceiver));
				
		return methodHookList;
	}
	
	public void after(MethodHookParam param) throws Throwable {
		int uid = Binder.getCallingUid();
		String argNames = null;
		
		if(mMethod == Methods.handleReceiver)
			argNames = "data";
		
		log(uid, param, argNames);
	}

}
