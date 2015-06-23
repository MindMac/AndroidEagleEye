package com.mindmac.eagleeye.hookclass;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.mindmac.eagleeye.Util;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;

import dalvik.system.DexClassLoader;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

public class ApplicationHookBak extends MethodHook {
	private static final String mClassName = "android.app.Application";
	
	private static boolean mReceiverInstalled = false;

	public static String mLogSetExtraKey = "log_set";
	public static String mEnableLog = "enable";
	public static String mDisableLog = "disable";

	public static String ACTION_SET_LOG = "com.mindmac.eagleeye.ACTION_SET_LOG";

	public ApplicationHookBak(Methods method) {
		super(mClassName, method.name());
	}

	// public void onCreate()
	// frameworks/base/core/java/android/app/Application.java
	// http://developer.android.com/reference/android/app/Application.html

	private enum Methods {
		onCreate
	};

	public static List<MethodHook> getMethodHookList() {
		List<MethodHook> methodHookList = new ArrayList<MethodHook>();
		methodHookList.add(new ApplicationHookBak(Methods.onCreate));
		return methodHookList;
	}
	
	public void after(MethodHookParam param) throws Throwable {
		// Register receiver
		registerReceiver(param);
		
		// Dynamically load the required native method
		int uid = Binder.getCallingUid();
		this.loadPathLogMethod(uid, param);
		
	}

	private void registerReceiver(MethodHookParam param){
		if (!mReceiverInstalled)
			try {
				Application app = (Application) param.thisObject;
				if (app != null) {
					mReceiverInstalled = true;
					app.registerReceiver(new Receiver(), new IntentFilter(ACTION_SET_LOG));
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			} 
	}
	
	private class Receiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				Bundle bundle = intent.getExtras();
				if(bundle == null)
					return;
				String setLogExtra = bundle.getString(mLogSetExtraKey);
				if (mEnableLog.equals(setLogExtra))
					Util.ENABLE_LOG = true;
				else if (mDisableLog.equals(setLogExtra))
					Util.ENABLE_LOG = false;
			} catch (Throwable ex) {
				ex.printStackTrace();
			}
		}
	}
	
	private void loadPathLogMethod(int uid, MethodHookParam param){
//		if(!Util.isAppNeedLog(uid))
//			return;
		
		if(uid <= Util.ANDROID_UID)
			return;
		
		if(Util.pathConvertorClass != null && Util.logFilePathMethod != null)
			return;
		
		Application application = (Application) param.thisObject;
		if(application == null)
			return;
		
		ApplicationInfo appInfo = application.getApplicationInfo();
		// Dynamically loading the fd2path.dex
		File dexFd2Path = new File(Environment.getExternalStorageDirectory().toString()
                		  + File.separator + Util.DEXFD2PATH);
        DexClassLoader dexClassLoader = new DexClassLoader(dexFd2Path.getAbsolutePath(), 
        		appInfo.dataDir, null, application.getClassLoader());
        try {
        	Util.pathConvertorClass = dexClassLoader.loadClass(Util.PATH_CONVERTOR_CLASS);
        	for(Method method : Util.pathConvertorClass.getDeclaredMethods()){
        		if(method.getName().equals(Util.LOG_FILE_PATH_METHOD_NAME)){
        			Util.logFilePathMethod = method;
        			break;
        		}
        	}
        	} catch (Exception exception) {
                exception.printStackTrace();
            }
	}
	
}
