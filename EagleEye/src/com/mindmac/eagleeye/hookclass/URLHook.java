package com.mindmac.eagleeye.hookclass;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.mindmac.eagleeye.MethodParser;
import com.mindmac.eagleeye.Util;


import android.os.Binder;
import android.util.Log;



import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

public class URLHook extends MethodHook {
	private static final String mClassName = "java.net.URL";

	private URLHook(Methods method) {
		super(mClassName, method.name());
	}

	// public URLConnection openConnection (Proxy proxy)
	// public URLConnection openConnection ()
	// libcore/luni/src/main/java/java/net/URL.java
	// http://developer.android.com/reference/java/net/URL.html

	private enum Methods {
		openConnection
	};

	public static List<MethodHook> getMethodHookList() {
		List<MethodHook> methodHookList = new ArrayList<MethodHook>();
		methodHookList.add(new URLHook(Methods.openConnection));
		
		return methodHookList;
	}

	@Override
	public void before(MethodHookParam param) throws Throwable {
		int uid = Binder.getCallingUid();
		logSpecial(uid, param);
	}
	
	private void logSpecial(int uid, MethodHookParam param){
		if(!this.isNeedLog(uid))
			return;
		
		URL url = (URL) param.thisObject;
		String urlStr = "";
		if(url != null){
			urlStr = url.toString();
		}
		String returnValue = MethodParser.parseReturnValue(param);
		String logMsg = String.format("{\"Basic\":[\"%d\", \"%d\",\"false\"], \"InvokeApi\":{\"%s->%s\":{\"url\":\"%s\"},\"return\":[%s]}}", 
				uid, Util.FRAMEWORK_HOOK_SYSTEM_API, this.getClassName(), this.getMethodName(), urlStr, returnValue);
		Log.i(Util.LOG_TAG, logMsg);
	}
	
}
