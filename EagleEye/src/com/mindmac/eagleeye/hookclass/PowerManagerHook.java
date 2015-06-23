package com.mindmac.eagleeye.hookclass;

import java.util.ArrayList;
import java.util.List;

import android.os.Binder;

import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

public class PowerManagerHook extends MethodHook {
	private Methods mMethod = null;
	
	private PowerManagerHook(String className, Methods method) {
		super(className, method.name());
		mMethod = method;
	}

	// @formatter:off

	// public void reboot(String reason)	
	// frameworks/base/core/java/android/os/PowerManager.java
	// http://developer.android.com/reference/android/os/PowerManager.html

	// @formatter:on

	private enum Methods {
		reboot
	};

	public static List<MethodHook> getMethodHookList(Object instance) {
		String className = instance.getClass().getName();
		
		List<MethodHook> methodHookList = new ArrayList<MethodHook>();
		methodHookList.add(new PowerManagerHook(className, Methods.reboot));
		
		return methodHookList;
	}
	
	@Override
	public void after(MethodHookParam param) throws Throwable {
		int uid = Binder.getCallingUid();
		String argNames = null;
		
		if(mMethod == Methods.reboot){
			argNames = "reason";
		}

		log(uid, param, argNames);
	}
}
