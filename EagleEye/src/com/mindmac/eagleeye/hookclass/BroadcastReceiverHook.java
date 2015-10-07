package com.mindmac.eagleeye.hookclass;

import java.util.ArrayList;
import java.util.List;

import com.mindmac.eagleeye.MethodParser;
import com.mindmac.eagleeye.Util;

import android.content.BroadcastReceiver;
import android.os.Binder;
import android.util.Log;

import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

public class BroadcastReceiverHook extends MethodHook {
	private static final String mClassName = "android.content.BroadcastReceiver";
	
	private BroadcastReceiverHook(Methods method) {
		super(mClassName, method.name());
	}

	// @formatter:off


	// public final	void abortBroadcast()	
	// frameworks/base/core/java/android/content/BroadcastReceiver.java	
	// http://developer.android.com/reference/android/content/BroadcastReceiver.html

	// @formatter:on

	private enum Methods {
		abortBroadcast
	};

	public static List<MethodHook> getMethodHookList() {
		List<MethodHook> methodHookList = new ArrayList<MethodHook>();
		methodHookList.add(new BroadcastReceiverHook(Methods.abortBroadcast));
		
		return methodHookList;
	}
	
	@Override
	public void after(MethodHookParam param) throws Throwable {
		int uid = Binder.getCallingUid();
		logSpecial(uid, param);
	}
	
	private void logSpecial(int uid, MethodHookParam param){
		// Check if need log
		if(!this.isNeedLog(uid))
			return;
		BroadcastReceiver broadcastReceiver = (BroadcastReceiver) param.thisObject;
		String receiverStr = "";
		if(broadcastReceiver != null){
			receiverStr = broadcastReceiver.toString();
		}
		String returnValue = MethodParser.parseReturnValue(param);
		String logMsg = String.format("{\"Basic\":[\"%d\",\"%d\",\"false\"], " +
				"\"InvokeApi\":{\"%s->%s\":{\"broadcastReceiver\":\"%s\"},\"return\":[%s]}}", 
				uid, Util.FRAMEWORK_HOOK_SYSTEM_API, this.getClassName(), this.getMethodName(), receiverStr, returnValue);
		Log.i(Util.LOG_TAG, logMsg);
	}
}
