package com.mindmac.eagleeye.hookclass;

import java.util.ArrayList;
import java.util.List;

import com.mindmac.eagleeye.service.Launcher;
import android.annotation.SuppressLint;

import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

public class ActivityHook extends MethodHook {
	private Methods mMethod;

	private static final String mClassName = "android.app.Activity";

	private ActivityHook(Methods method) {
		super( mClassName, method.name());
		mMethod = method;
	}



	// @formatter:off

	// public Object getSystemService(String name)
	// frameworks/base/core/java/android/app/Activity.java

	// @formatter:on

	private enum Methods {
		getSystemService
	};

	@SuppressLint("InlinedApi")
	public static List<MethodHook> getMethodHookList() {
		List<MethodHook> methodHookList = new ArrayList<MethodHook>();
		methodHookList.add(new ActivityHook(Methods.getSystemService));

		return methodHookList;
	}

	@Override
	public void after(MethodHookParam param) throws Throwable {		
		if (mMethod == Methods.getSystemService) {
			if (param.args.length > 0 && param.args[0] != null) {
				String name = (String) param.args[0];
				Object instance = param.getResult();
				if (name != null && instance != null)
					Launcher.hookSystemServices(this, name, instance);
			}
		}
	}
}
