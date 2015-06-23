package com.mindmac.eagleeye.hookclass;

import java.util.ArrayList;
import java.util.List;

import android.os.Binder;

import de.robv.android.xposed.XC_MethodHook.MethodHookParam;


public class MediaRecorderHook extends MethodHook {
	private Methods mMethod = null;
	
	private static final String mClassName = "android.media.MediaRecorder";

	private MediaRecorderHook(Methods method) {
		super(mClassName, method.name());
		mMethod = method;
	}


	// public void setOutputFile(FileDescriptor fd)
	// public void setOutputFile(String path)
	// frameworks/base/media/java/android/media/MediaRecorder.java
	// http://developer.android.com/reference/android/media/MediaRecorder.html

	private enum Methods {
		setOutputFile
	};

	public static List<MethodHook> getMethodHookList() {
		List<MethodHook> methodHookList = new ArrayList<MethodHook>();
		
		// Required permissions
		methodHookList.add(new MediaRecorderHook(Methods.setOutputFile));
		return methodHookList;
	}
	
	@Override
	public void after(MethodHookParam param) throws Throwable {
		int uid = Binder.getCallingUid();
		String argNames = null;
		
		if(mMethod == Methods.setOutputFile){
			if(param.args[0] instanceof String)
				argNames = "path";
			else
				argNames = "fd";
		}
		
		log(uid, param, argNames);
	}
}
