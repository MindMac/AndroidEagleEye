package com.mindmac.eagleeye.hookclass;

import java.util.ArrayList;
import java.util.List;

import android.os.Binder;

import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

public class LocationManagerHook extends MethodHook {
	private Methods mMethod = null;
	
	private LocationManagerHook(String className, Methods method) {
		super(className, method.name());
		mMethod = method;
	}


	// @formatter:off
	// public Location getLastKnownLocation(String provider)
	// frameworks/base/location/java/android/location/LocationManager.java
	// http://developer.android.com/reference/android/location/LocationManager.html

	// @formatter:on

	private enum Methods {
		getLastKnownLocation
	};

	public static List<MethodHook> getMethodHookList(Object instance) {
		String className = instance.getClass().getName();
		List<MethodHook> methodHookList = new ArrayList<MethodHook>();
		
		methodHookList.add(new LocationManagerHook(className, Methods.getLastKnownLocation));
		
		return methodHookList;
	}
	
	@Override
	public void after(MethodHookParam param) throws Throwable {
		int uid = Binder.getCallingUid();
		String argNames = null;
		
		if(mMethod == Methods.getLastKnownLocation){
			argNames = "provider";

		}
		
		log(uid, param, argNames);
	}
}
