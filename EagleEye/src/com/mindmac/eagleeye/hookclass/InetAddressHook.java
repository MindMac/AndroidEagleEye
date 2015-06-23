package com.mindmac.eagleeye.hookclass;

import java.util.ArrayList;
import java.util.List;

import android.os.Binder;

import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

public class InetAddressHook extends MethodHook {
	private Methods mMethod = null;
	private static final String mClassName = "java.net.InetAddress";
	
	private InetAddressHook(Methods method) {
		super(mClassName, method.name());
		mMethod = method;
	}

	// @formatter:off
	// public static InetAddress[] getAllByName(String	host)
	
	// public static InetAddress getByAddress(byte[] ipAddress)	
	// public static InetAddress getByAddress(String hostName, byte[] ipAddress)
	// private static InetAddress getByAddress(String hostName, byte[] ipAddress, int scopeId)
	
	// public static InetAddress getByName(String host)
	// libcore/luni/src/main/java/java/net/InetAddress.java	
	// http://developer.android.com/reference/java/net/InetAddress.html
	// @formatter:on

	private enum Methods {
		getAllByName, getByAddress, getByName
	};

	public static List<MethodHook> getMethodHookList() {
		List<MethodHook> methodHookList = new ArrayList<MethodHook>();
		for(Methods method : Methods.values())
			methodHookList.add(new InetAddressHook(method));
		
		return methodHookList;
	}
	
	@Override
	public void after(MethodHookParam param) throws Throwable {
		int uid = Binder.getCallingUid();
		String argNames = null;
		
		if(mMethod == Methods.getAllByName){
			argNames = "host";
		}else if(mMethod == Methods.getByAddress){
			if(param.args.length == 1)
				argNames = "ipAddress";
			else if(param.args.length == 2)
				argNames = "hostName|ipAddress";
			else if(param.args.length == 3)
				argNames = "hostName|ipAddress|scopeId";
		}else if(mMethod == Methods.getByName){
			argNames = "host";
		}
		
		log(uid, param, argNames);
	}
}
