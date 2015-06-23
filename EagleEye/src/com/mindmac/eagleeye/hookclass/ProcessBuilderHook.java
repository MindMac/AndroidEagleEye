package com.mindmac.eagleeye.hookclass;

import java.util.ArrayList;
import java.util.List;

import android.os.Binder;

import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

public class ProcessBuilderHook extends MethodHook {
	private Methods mMethod = null;
	private static final String mClassName = "java.lang.ProcessBuilder";
	
	private ProcessBuilderHook(Methods method) {
		super(mClassName, method.name());
		mMethod = method;
	}

	// @formatter:off
	// ProcessBuilder command(List<String>	command)
	// ProcessBuilder command(String...	command)
	// public Process	start()
	// libcore/luni/src/main/java/java/lang/ProcessBuilder.java	
	// http://developer.android.com/reference/java/lang/ProcessBuilder.html
	// @formatter:on

	private enum Methods {
		command, start
	};

	public static List<MethodHook> getMethodHookList() {
		List<MethodHook> methodHookList = new ArrayList<MethodHook>();
		for(Methods method : Methods.values())
			methodHookList.add(new ProcessBuilderHook(method));
		
		return methodHookList;
	}
	
	@Override
	public void after(MethodHookParam param) throws Throwable {
		int uid = Binder.getCallingUid();
		String argNames = null;
		
		if(mMethod == Methods.command){
			argNames = "command";
		}

		log(uid, param, argNames);
	}
}
