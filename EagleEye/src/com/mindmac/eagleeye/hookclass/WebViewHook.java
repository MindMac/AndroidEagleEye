package com.mindmac.eagleeye.hookclass;

import java.util.ArrayList;
import java.util.List;

import android.os.Binder;

import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

public class WebViewHook extends MethodHook {
	private Methods mMethod = null;
	private static final String mClassName = "android.webkit.WebView";
	
	private WebViewHook(Methods method) {
		super(mClassName, method.name());
		mMethod = method;
	}


	// @formatter:off

	// public void loadUrl(String url)
	// public void loadUrl(String url, Map<String, String> additionalHttpHeaders)
	// frameworks/base/core/java/android/webkit/WebView.java
	// http://developer.android.com/reference/android/webkit/WebView.html

	// @formatter:on

	private enum Methods {
		loadUrl
	};

	public static List<MethodHook> getMethodHookList() {
		List<MethodHook> methodHookList = new ArrayList<MethodHook>();
		methodHookList.add(new WebViewHook(Methods.loadUrl));
		
		return methodHookList;
	}
	
	@Override
	public void after(MethodHookParam param) throws Throwable {
		int uid = Binder.getCallingUid();
		String argNames = null;
		
		if(mMethod == Methods.loadUrl){
			if(param.args.length == 1)
				argNames = "url";
			else if(param.args.length == 2)
				argNames = "url|additionalHttpHeaders";
		}

		log(uid, param, argNames);
	}
}
