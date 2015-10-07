package com.mindmac.eagleeye.hookclass;

import com.mindmac.eagleeye.MethodParser;
import com.mindmac.eagleeye.Util;


import android.util.Log;

import de.robv.android.xposed.XC_MethodHook.MethodHookParam;


/***
 * 
 * @author Wenjun Hu
 * 
 * Abstract base class of hooked method
 *
 ***/


public abstract class MethodHook {
	private String mClassName;
	private String mMethodName;
	
	protected MethodHook(String className, String methodName){
		mClassName = className;
		mMethodName = methodName;
	}
	
	
	public String getClassName(){
		return mClassName;
	}
	
	public String getMethodName(){
		return mMethodName;
	}

	
	public void before(MethodHookParam param) throws Throwable {
		// Do nothing
	}

	public void after(MethodHookParam param) throws Throwable {
		// Do nothing
	}
	
	public boolean isNeedLog(int uid){
		return Util.isAppNeedFrLog(uid);
	}
		
	protected void log(int uid, MethodHookParam param, String argNames){
		// Check if need log
		if(!this.isNeedLog(uid))
			return;
		
		String[] argNamesArray = null;
		if(argNames != null)
			argNamesArray = argNames.split("\\|");
		String formattedArgs = MethodParser.parseMethodArgs(param, argNamesArray);
		if(formattedArgs == null)
			formattedArgs = "";
		String returnValue = MethodParser.parseReturnValue(param);
		String logMsg = String.format("{\"Basic\":[\"%d\",\"%s\",\"false\"], " +
				"\"InvokeApi\":{\"%s->%s\":{%s}, \"return\":{%s}}}", uid, Util.FRAMEWORK_HOOK_SYSTEM_API, 
				mClassName, mMethodName, formattedArgs, returnValue);
		Log.i(Util.LOG_TAG, logMsg);
	}
	

}
