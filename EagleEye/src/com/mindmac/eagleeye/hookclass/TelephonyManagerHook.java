package com.mindmac.eagleeye.hookclass;

import java.util.ArrayList;
import java.util.List;

import com.mindmac.eagleeye.Util;

import android.os.Binder;

import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

public class TelephonyManagerHook extends MethodHook {
	private Methods mMethod = null;
	
	private TelephonyManagerHook(String className, Methods method) {
		super(className, method.name());
		mMethod = method;
	}



	// public CellLocation getCellLocation()
	// public List<CellInfo> getAllCellInfo ()
	// public String getDeviceId()
	// public String getGroupIdLevel1()
	// public String getLine1Number()
	// public String getNetworkCountryIso()
	// public String getNetworkOperator()
	// public String getNetworkOperatorName()
	// public int getNetworkType()
	// public int getPhoneType()
	// public String getSimCountryIso()
	// public String getSimOperator()
	// public String getSimOperatorName()
	// public String getSimSerialNumber()
	// public String getSubscriberId()
	// public String getVoiceMailAlphaTag()
	// public String getVoiceMailNumber()
	// public List<NeighboringCellInfo> getNeighboringCellInfo ()
	// frameworks/base/telephony/java/android/telephony/TelephonyManager.java
	// http://developer.android.com/reference/android/telephony/TelephonyManager.html

	// @formatter:off
	private enum Methods {
		getCellLocation, getAllCellInfo,
		getDeviceId, getGroupIdLevel1, getLine1Number, 
		getNetworkCountryIso, getNetworkOperator, 
		getNetworkOperatorName, getNetworkType, getPhoneType,
		getSimCountryIso, getSimOperator, getSimOperatorName, 
		getSimSerialNumber, getSubscriberId, getVoiceMailAlphaTag, 
		getVoiceMailNumber, getNeighboringCellInfo
	};
	// @formatter:on

	public static List<MethodHook> getMethodHookList(Object instance) {
		String className = instance.getClass().getName();

		List<MethodHook> methodHookList = new ArrayList<MethodHook>();
		
		for(Methods method : Methods.values())
			methodHookList.add(new TelephonyManagerHook(className, method));

		return methodHookList;
	}
	
	@Override
	public void after(MethodHookParam param) throws Throwable {
		int uid = Binder.getCallingUid();
		String argNames = null;

		log(uid, param, argNames);
		
		// Anti anti emulator
		if(this.isNeedLog(uid))
			this.antiAntiEmu(param);
		
	}
	
	private void antiAntiEmu(MethodHookParam param){
		if(mMethod == Methods.getLine1Number || mMethod == Methods.getVoiceMailNumber)
			param.setResult(Util.generateRandomNums(11));
		else if(mMethod == Methods.getDeviceId || mMethod == Methods.getSubscriberId)
			param.setResult(Util.generateRandomNums(15));
		else if(mMethod == Methods.getSimSerialNumber)
			param.setResult(Util.generateRandomNums(20));
		else if(mMethod == Methods.getNetworkOperatorName)
			param.setResult(Util.generateRandomStrs(4));
	}

}
