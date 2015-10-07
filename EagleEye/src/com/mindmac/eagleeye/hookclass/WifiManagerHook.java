package com.mindmac.eagleeye.hookclass;

import java.util.ArrayList;
import java.util.List;

import android.os.Binder;

import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

public class WifiManagerHook extends MethodHook {
	private Methods mMethod = null;
	
	private WifiManagerHook(String className, Methods method) {
		super(className, method.name());
		mMethod = method;
	}


	// @formatter:off
	// public	List<WifiConfiguration>	getConfiguredNetworks()	
	// public	WifiInfo	getConnectionInfo()
	// int	getWifiState()
	// boolean	isWifiEnabled()
	// public	WifiConfiguration	getWifiApConfiguration()
	// public	boolean	setWifiEnabled(boolean	enabled)
	// frameworks/base/wifi/java/android/net/wifi/WifiManager.java	
	// http://developer.android.com/reference/android/net/wifi/WifiManager.html

	// @formatter:on

	private enum Methods {
		getConfiguredNetworks, getConnectionInfo, getWifiState,
		isWifiEnabled, getWifiApConfiguration, setWifiEnabled
	};

	public static List<MethodHook> getMethodHookList(Object instance) {
		String className = instance.getClass().getName();
		List<MethodHook> methodHookList = new ArrayList<MethodHook>();
		for(Methods method : Methods.values())
			methodHookList.add(new WifiManagerHook(className, method));
		
		return methodHookList;
	}
	
	@Override
	public void after(MethodHookParam param) throws Throwable {
		int uid = Binder.getCallingUid();
		String argNames = null;
		
		if(mMethod == Methods.setWifiEnabled){
			argNames = "enabled";
		}

		log(uid, param, argNames);
	}
}
