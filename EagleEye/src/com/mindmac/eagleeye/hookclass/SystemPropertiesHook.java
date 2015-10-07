package com.mindmac.eagleeye.hookclass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import com.mindmac.eagleeye.Util;


import android.os.Binder;

import de.robv.android.xposed.XC_MethodHook.MethodHookParam;


public class SystemPropertiesHook extends MethodHook {
	private static final String mClassName = "android.os.SystemProperties";

	private HashMap<String, Boolean> propertyMap = new HashMap<String, Boolean>();
	
	private String[] propertyArray = {"init.svc.qemud", "init.svc.qemu-props", "qemu.hw.mainkeys",
            "qemu.sf.fake_camera", "qemu.sf.lcd_density","ro.bootloader", "ro.bootmode",
            "ro.hardware", "ro.kernel.android.qemud", "ro.kernel.qemu.gles", "ro.kernel.qemu",
            "ro.product.device", "ro.product.model","ro.product.name", "ro.serialno"};
	
	private SystemPropertiesHook(Methods method) {
		super(mClassName, method.name());
		
		for(String property : propertyArray)
			propertyMap.put(property, true);
	}


	// public static String get(String key)
	// public static String get(String key, String def)
	// /frameworks/base/core/java/android/os/SystemProperties.java

	private enum Methods {
		get
	};

	public static List<MethodHook> getMethodHookList() {
		List<MethodHook> methodHookList = new ArrayList<MethodHook>();
		for(Methods method : Methods.values())
			methodHookList.add(new SystemPropertiesHook(method));
		
		return methodHookList;
	}
	
	@Override
	public void after(MethodHookParam param) throws Throwable {
		int uid = Binder.getCallingUid();
		if(!isNeedLog(uid))
			return;
		if(param.args.length >= 1){
			String property = (String) param.args[0];
			antiAntiEmu(param, property);
		}
	}
	
	private void antiAntiEmu(MethodHookParam param, String property){
		Random randomGenerator = new Random();
		if(propertyMap.containsKey(property))
			param.setResult(Util.generateRandomStrs(randomGenerator.nextInt(5) + 5));
	}
}
