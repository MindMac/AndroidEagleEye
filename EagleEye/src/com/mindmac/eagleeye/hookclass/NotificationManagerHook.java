package com.mindmac.eagleeye.hookclass;

import java.util.ArrayList;
import java.util.List;

import android.os.Binder;


import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

public class NotificationManagerHook extends MethodHook {
	private Methods mMethod = null;
	
	private NotificationManagerHook(String className, Methods method) {
		super(className, method.name());
		mMethod = method;
	}

	// @formatter:off

	// public void notify (int id, Notification notification)
	// public void notify (String tag, int id, Notification notification)
	// frameworks/base/telephony/java/android/telephony/SmsManager.java
	// http://developer.android.com/reference/android/telephony/SmsManager.html

	// @formatter:on

	private enum Methods {
		notify
	};

	public static List<MethodHook> getMethodHookList(Object instance) {
		String className = instance.getClass().getName();
		
		List<MethodHook> methodHookList = new ArrayList<MethodHook>();
		methodHookList.add(new NotificationManagerHook(className, Methods.notify));
		return methodHookList;
	}
	
	@Override
	public void after(MethodHookParam param) throws Throwable {
		int uid = Binder.getCallingUid();
		String argNames = null;
		
		if(mMethod == Methods.notify){
			if(param.args.length == 2)
				argNames = "id|notification";
			else if(param.args.length == 3)
				argNames = "tag|id|notification";
		}

		log(uid, param, argNames);
	}
}
