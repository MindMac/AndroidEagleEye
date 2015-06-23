package com.mindmac.eagleeye.hookclass;

import java.util.ArrayList;
import java.util.List;

import android.os.Binder;

import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

public class AudioRecordHook extends MethodHook {
	private Methods mMethod = null;
	private static final String mClassName = "android.media.AudioRecord";

	private AudioRecordHook(Methods method) {
		super(mClassName, method.name());
		mMethod = method;
	}


	// public void startRecording()
	// public void startRecording(MediaSyncEvent syncEvent)
	// frameworks/base/media/java/android/media/AudioRecord.java
	// http://developer.android.com/reference/android/media/AudioRecord.html

	private enum Methods {
		startRecording
	};

	public static List<MethodHook> getMethodHookList() {
		List<MethodHook> MethodHookList = new ArrayList<MethodHook>();
		MethodHookList.add(new AudioRecordHook(Methods.startRecording));
		return MethodHookList;
	}
	
	public void after(MethodHookParam param) throws Throwable {
		int uid = Binder.getCallingUid();
		String argNames = null;
		
		if(mMethod == Methods.startRecording){
			if(param.args.length == 1)
				argNames = "syncEnvent";
		}
			
		log(uid, param, argNames);
	}
}
