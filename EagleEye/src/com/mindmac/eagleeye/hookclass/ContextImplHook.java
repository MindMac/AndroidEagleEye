package com.mindmac.eagleeye.hookclass;

import java.util.ArrayList;
import java.util.List;

import com.mindmac.eagleeye.service.Launcher;

import android.os.Binder;

import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

public class ContextImplHook extends MethodHook {
	private Methods mMethod;
	private static final String mClassName = "android.app.ContextImpl";
	
	private ContextImplHook(Methods method) {
		super(mClassName, method.name());
		mMethod = method;
	}


	// public PackageManager getPackageManager()
	// public Object getSystemService(String name)
	// public ComponentName startService(Intent service)
	// public void startActivity(Intent intent, Bundle options)
	// frameworks/base/core/java/android/app/ContextImpl.java

	private enum Methods {
		getPackageManager, getSystemService, startService, startActivity
	};

	public static List<MethodHook> getMethodHookList() {
		List<MethodHook> methodHookList = new ArrayList<MethodHook>();
		methodHookList.add(new ContextImplHook(Methods.getPackageManager));
		methodHookList.add(new ContextImplHook(Methods.getSystemService));
		methodHookList.add(new ContextImplHook(Methods.startService));
		methodHookList.add(new ContextImplHook(Methods.startActivity));
		return methodHookList;
	}

	@Override
	public void after(MethodHookParam param) throws Throwable {
		int uid = Binder.getCallingUid();
		String argNames = null;
		
		if (mMethod == Methods.getPackageManager) {
			Object instance = param.getResult();
			if (instance != null)
				Launcher.hookSystemServices(this, "PackageManager", instance);
			return;
		} else if (mMethod == Methods.getSystemService) {
			if (param.args.length > 0 && param.args[0] != null) {
				String name = (String) param.args[0];
				Object instance = param.getResult();
				if (name != null && instance != null)
					Launcher.hookSystemServices(this, name, instance);
			}
			return;
		}else if(mMethod == Methods.startService){
			argNames = "service";
		}else if(mMethod == Methods.startActivity){
			if(param.args.length == 2)
				argNames = "intent|options";
			else
				return;
		}
		
		log(uid, param, argNames);
	}
}
