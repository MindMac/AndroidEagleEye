package com.mindmac.eagleeye.hookclass;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Binder;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

public class AccountManagerHook extends MethodHook {
	private Methods mMethod = null;
	
	private AccountManagerHook(String className, Methods method) {
		super(className, method.name());
		mMethod = method;
	}

	// @formatter:off


	// public Account[] getAccounts()
	// public Account[] getAccountsByType(String type)
	// public Account[] getAccountsByTypeAsUser(String type, UserHandle userHandle)
	// public Account[] getAccountsByTypeForPackage(String type, String packageName)
	// public AccountManagerFuture<Account[]> getAccountsByTypeAndFeatures(final String type, final String[] features, AccountManagerCallback<Account[]> callback, Handler handler)
	// public AccountManagerFuture<Bundle> getAuthToken(final Account account, final String authTokenType, final Bundle options, final Activity activity, AccountManagerCallback<Bundle> callback, Handler handler)
	// public AccountManagerFuture<Bundle> getAuthToken(final Account account, final String authTokenType, final boolean notifyAuthFailure, AccountManagerCallback<Bundle> callback, Handler handler)
	// public AccountManagerFuture<Bundle> getAuthToken(final Account account, final String authTokenType, final Bundle options, final boolean notifyAuthFailure, AccountManagerCallback<Bundle> callback, Handler handler)
	// public AccountManagerFuture<Bundle> getAuthTokenByFeatures(final String accountType, final String authTokenType, final String[] features, final Activity activity, final Bundle addAccountOptions, final Bundle getAuthTokenOptions, final AccountManagerCallback<Bundle> callback, final Handler handler)
	// frameworks/base/core/java/android/accounts/AccountManager.java
	// http://developer.android.com/reference/android/accounts/AccountManager.html

	// @formatter:on

	private enum Methods {
		getAccounts, getAccountsByType, getAccountsByTypeForPackage, 
		getAccountsByTypeAndFeatures, getAuthToken, getAuthTokenByFeatures
	};

	public static List<MethodHook> getMethodHookList(Object instance) {
		String className = instance.getClass().getName();
		List<MethodHook> methodHookList = new ArrayList<MethodHook>();
		for(Methods method : Methods.values())
			methodHookList.add(new AccountManagerHook(className, method));
		
		return methodHookList;
	}
	
	public void after(MethodHookParam param) throws Throwable {
		int uid = Binder.getCallingUid();
		String argNames = null;
		
		if(mMethod == Methods.getAccountsByType){
			if(param.args.length == 1)
				argNames = "type";
			else if(param.args.length == 2)
				argNames = "type|userHandle";
		}else if(mMethod == Methods.getAccountsByTypeForPackage)
			argNames = "type|packageName";
		else if(mMethod == Methods.getAccountsByTypeAndFeatures)
			argNames = "type|features|callback|handler";
		else if(mMethod == Methods.getAuthToken){
			if(param.args.length == 5)
				argNames = "account|authTokenType|notifyAuthFailure|callback|handler";
			if(param.args.length == 6){
				if(param.args[3] instanceof Activity || param.args[3] == null)
					argNames = "account|authTokenType|options|activity|callback|handler";
				else
					argNames = "account|authTokenType|options|notifyAuthFailure|callback|handler";	
			}
		}
		else if(mMethod == Methods.getAuthTokenByFeatures)
			argNames = "accountType|authTokenType|features|activity|addAccountOptions|getAuthTokenOptions|callback|handler";
		
		log(uid, param, argNames);
	}
}
