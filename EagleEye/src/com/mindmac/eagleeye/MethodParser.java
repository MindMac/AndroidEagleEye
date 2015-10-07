package com.mindmac.eagleeye;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.util.EntityUtils;

import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

import android.text.TextUtils;

public class MethodParser {
	
	// Record method name and arguments
	public static String parseMethodArgs(MethodHookParam param, String[] argNames){
		List<String> formattedArgsList = new ArrayList<String>();
		Class<?>[] argTypes = null;
		if(param.method instanceof Constructor){
			Constructor<?> constructor = (Constructor<?>) param.method;
			argTypes = constructor.getParameterTypes();
		}else if(param.method instanceof Method){
			Method method = (Method) param.method;
			argTypes = method.getParameterTypes();
		}
		
		int argLength = argTypes.length;
		if(argNames != null && argNames.length != argLength)
			return null;
		
		for(int i=0; i<argLength; i++){
			if(param.args[i] != null){
				String argTypeName = argTypes[i].getName();
				String argValueStr = "";
				if(argTypeName.startsWith("[")){
					// Parse array
					argValueStr = parseArrayArg(param.args[i], argTypeName);
				}else{
					// Parse non-array
					argValueStr = parseArg(param.args[i], argTypeName);
				}
				formattedArgsList.add(String.format("\"%s\":\"%s\"", argNames[i], argValueStr));
			}else
				formattedArgsList.add(String.format("\"%s\":\"%s\"", argNames[i], "null"));
		}
		
		String formattedArgsStr = TextUtils.join(", ", formattedArgsList.toArray());
		return formattedArgsStr;
	}
	
	public static String parseParameters(MethodHookParam param){
		List<String> formattedArgsList = new ArrayList<String>();
		Class<?>[] argTypes = null;
		if(param.method instanceof Constructor){
			Constructor<?> constructor = (Constructor<?>) param.method;
			argTypes = constructor.getParameterTypes();
		}else if(param.method instanceof Method){
			Method method = (Method) param.method;
			argTypes = method.getParameterTypes();
		}

		int argLength = argTypes.length;
		
		for(int i=0; i<argLength; i++){
			String argTypeName = argTypes[i].getName();
			String argValueStr = "null";
			if(param.args[i] != null)
				argValueStr = parseObject(argTypeName, param.args[i]);
			formattedArgsList.add(String.format("[\"%s\":\"%s\"]", argTypeName, argValueStr));
		}
		
		String formattedArgsStr = TextUtils.join(", ", formattedArgsList.toArray());		
		return formattedArgsStr;
	}
	
	public static String parseReturnValue(MethodHookParam param){
		Object returnObject = param.getResult();
		String returnTypeName = "null";
		String returnValue = "null";
		if(returnObject != null){
			if(param.method instanceof Method){
				Method method = (Method) param.method;
				returnTypeName = method.getReturnType().getName();
			}

			returnValue = parseObject(returnTypeName, returnObject);
		}
		
		return String.format("\"%s\":\"%s\"", returnTypeName, returnValue);
	}

	private static String parseObject(String typeName, Object object){
		String valueStr = "";
		if(typeName.startsWith("[")){
			// Parse array
			valueStr = parseArrayArg(object, typeName);
		}else{
			// Parse non-array
			valueStr = parseArg(object, typeName);
		}
		return valueStr;
	}
	
	// Parse Array Argument
	private static String parseArrayArg(Object arg, String typeName){
		if(typeName.startsWith("[B"))
			return parseArrayArg((byte[]) arg);
		if(typeName.startsWith("[S"))
			return parseArrayArg((short[]) arg);
		if(typeName.startsWith("[I"))
			return parseArrayArg((int[]) arg);
		if(typeName.startsWith("[J"))
			return parseArrayArg((long[]) arg);
		if(typeName.startsWith("[F"))
			return parseArrayArg((float[]) arg);
		if(typeName.startsWith("[D"))
			return parseArrayArg((double[]) arg);
		if(typeName.startsWith("[C"))
			return parseArrayArg((char[]) arg);
		if(typeName.startsWith("[Z"))
			return parseArrayArg((boolean[]) arg);
		else
			return parseArrayArg((Object[]) arg);
		
	}
	
	
	private static String parseArrayArg(Object[] argArray){
		List<String> argValueList = new ArrayList<String>();
		
		Object tmpObject = null;
		int length = argArray.length;
		
		for(int i=0; i<length; i++){
			try{
				tmpObject = argArray[i];
				if(tmpObject == null)
					argValueList.add("null");
				else
					argValueList.add(tmpObject.toString());
			}catch(UnsupportedOperationException ex){
				argValueList.add("");
			}
		}
		return parseArrayArg(argValueList);

	}
	
	private static String parseArrayArg(byte[] argArray){
		List<Byte> argValueList = new ArrayList<Byte>();
		int length = argArray.length;
		
		for(int i=0; i<length; i++)
			argValueList.add(argArray[i]);
		
		return parseArrayArg(argValueList);
	
	}
	
	private static String parseArrayArg(short[] argArray){
		List<Short> argValueList = new ArrayList<Short>();
		int length = argArray.length;
		
		for(int i=0; i<length; i++)
			argValueList.add(argArray[i]);
		return parseArrayArg(argValueList);
	}
	
	private static String parseArrayArg(int[] argArray){
		List<Integer> argValueList = new ArrayList<Integer>();
		int length = argArray.length;
		
		for(int i=0; i<length; i++)
			argValueList.add(argArray[i]);
		return parseArrayArg(argValueList);
	}
	
	private static String parseArrayArg(long[] argArray){
		List<Long> argValueList = new ArrayList<Long>();
		int length = argArray.length;
		
		for(int i=0; i<length; i++)
			argValueList.add(argArray[i]);
		return parseArrayArg(argValueList);
	}
	
	private static String parseArrayArg(float[] argArray){
		List<Float> argValueList = new ArrayList<Float>();
		int length = argArray.length;
		
		for(int i=0; i<length; i++)
			argValueList.add(argArray[i]);
		return parseArrayArg(argValueList);
	}
	
	private static String parseArrayArg(double[] argArray){
		List<Double> argValueList = new ArrayList<Double>();
		int length = argArray.length;
		
		for(int i=0; i<length; i++)
			argValueList.add(argArray[i]);
		return parseArrayArg(argValueList);
	}
	
	private static String parseArrayArg(char[] argArray){
		List<Character> argValueList = new ArrayList<Character>();
		int length = argArray.length;
		
		for(int i=0; i<length; i++)
			argValueList.add(argArray[i]);
		return parseArrayArg(argValueList);
	}
	
	private static String parseArrayArg(boolean[] argArray){
		List<Boolean> argValueList = new ArrayList<Boolean>();
		int length = argArray.length;
		
		for(int i=0; i<length; i++)
			argValueList.add(argArray[i]);
		return parseArrayArg(argValueList);
	}
	
	private static String parseArrayArg(List<?> argValueList){
		String argValueStr = "";
		argValueStr = TextUtils.join(", ", argValueList);
		argValueStr = String.format("{ %s }", argValueStr);
		
		return argValueStr;
	}
	
	private static String parseArg(Object arg, String typeName){
		return parseArg(arg);
	}

	private static String parseArg(Object arg){
		String argValue = "null";
		try{
			if(arg instanceof HttpPost){
				URI uri = ((HttpPost)arg).getURI();
				byte[] postBytes = EntityUtils.toByteArray(((HttpPost)arg).getEntity());
				String postStrHex =  Util.toHex(postBytes);
				argValue = String.format("uri=%s && post=%s", uri.toString(), postStrHex);
			}else if(arg instanceof HttpGet){
				URI uri = ((HttpGet)arg).getURI();
				argValue = uri.toString();
			}else if(arg instanceof HttpUriRequest){
				URI uri = ((HttpUriRequest)arg).getURI();
				argValue = uri.toString();
			}else
				argValue = arg.toString();
		}catch(Exception ex){
			// Do noting
		}
		
		return argValue;
	}
}
