package com.mindmac.eagleeye.hookclass;

import java.util.ArrayList;
import java.util.List;

import android.os.Binder;

import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

public class LocationClientHook extends MethodHook {
	private static final String mClassName = "com.google.android.gms.location.LocationClient";

	private LocationClientHook(Methods method) {
		super(mClassName, method.name());
	}



	// @formatter:off
	// Location getLastLocation()
	// https://developer.android.com/reference/com/google/android/gms/location/LocationClient.html

	// @formatter:on

	private enum Methods {
		getLastLocation
	};

	public static List<MethodHook> getMethodHookList() {
		List<MethodHook> methodHookList = new ArrayList<MethodHook>();
				
		methodHookList.add(new LocationClientHook(Methods.getLastLocation));
		return methodHookList;
	}
	
	@Override
	public void after(MethodHookParam param) throws Throwable {
		int uid = Binder.getCallingUid();
		String argNames = null;
	
		log(uid, param, argNames);
	}
}
