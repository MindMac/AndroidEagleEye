package com.mindmac.eagleeye.hookclass;

import java.util.ArrayList;
import java.util.List;

import android.os.Binder;


import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

public class BluetoothAdapterHook extends MethodHook {
	
	private static final String mClassName = "android.bluetooth.BluetoothAdapter";
	
	private BluetoothAdapterHook(Methods method) {
		super(mClassName, method.name());
	}

	// @formatter:off

	// public boolean enable()
	// public String getAddress()
	// frameworks/base/core/java/android/bluetooth/BluetoothAdapter.java
	// http://developer.android.com/reference/android/bluetooth/BluetoothAdapter.html

	// @formatter:on

	private enum Methods {
		enable, getAddress
	};

	public static List<MethodHook> getMethodHookList() {
		List<MethodHook> methodHookList = new ArrayList<MethodHook>();
		for(Methods method : Methods.values())
			methodHookList.add(new BluetoothAdapterHook(method));
		
		return methodHookList;
	}
	
	public void after(MethodHookParam param) throws Throwable {
		int uid = Binder.getCallingUid();
		String argNames = null;		
		log(uid, param, argNames);
	}
}
