#include "hooks/util.h"
#include "base/util.h"

#include <jni.h>
#include <stdio.h>
#include <dlfcn.h>
#include <stdbool.h>
#include <stdlib.h>


char* lib_path = "/data/data/com.mindmac.eagleeye/lib/libeagleeyenative.so";

LIB_HOOK_INFO_NODE* custom_lib_hook_info_root = NULL;
HOOKED_INFO_NODE* hooked_info_root = NULL;

Java_com_mindmac_eagleeye_NativeEntry_initSystemNativeHook( JNIEnv* env, jobject this )
{
	LIB_HOOK_INFO_NODE* lib_hook_info_root = build_hook_info_list(lib_path, "system_hook_info");
	if(lib_hook_info_root == NULL){
		LOGD("Build system hook info list failed");
		return;
	}

	LIB_HOOK_INFO_NODE* lib_hook_info_node = lib_hook_info_root->next;
	void* handler = dlopen(lib_path, RTLD_LAZY);
	if(handler == NULL){
		LOGD("dlopen %s failed", lib_path);
		return;
	}

	while(lib_hook_info_node != NULL){
		char* hook_info_name = lib_hook_info_node->hook_info_name;
		void* tmp_hook_info = dlsym(handler, hook_info_name);
		HOOK_INFO* hook_info;
		hook_info = (HOOK_INFO*) tmp_hook_info;
		if(hook_info != NULL){
			LOGD("Try to hook %s in lib %s", hook_info->funcname, hook_info->libname);
			if(hook(&(hook_info->eph), getpid(), hook_info->libname, hook_info->funcname,
				hook_info->hook_arm, hook_info->hook_thumb))
				LOGD("Hooked %s in lib %s", hook_info->funcname, hook_info->libname);
		}
		lib_hook_info_node = lib_hook_info_node->next;
	}
}

Java_com_mindmac_eagleeye_NativeEntry_initCustomNativeHook(JNIEnv* env, jobject this, jstring lib_name){
	const char* lib_name_native = (*env)->GetStringUTFChars(env, lib_name, 0);
	if(custom_lib_hook_info_root == NULL)
		custom_lib_hook_info_root = build_hook_info_list(lib_path, "custom_hook_info");

	if(custom_lib_hook_info_root == NULL){
		LOGD("Build custom hook info list failed");
		return;
	}

	if(hooked_info_root == NULL){
		hooked_info_root = (HOOKED_INFO_NODE*) malloc(sizeof(HOOKED_INFO_NODE));
		if(hooked_info_root != NULL)
			hooked_info_root->next = NULL;
	}

	if(hooked_info_root == NULL){
		LOGD("Build custom hooked info list failed");
		return;
	}

	LIB_HOOK_INFO_NODE* lib_hook_info_node = custom_lib_hook_info_root->next;
	void* handler = dlopen(lib_path, RTLD_LAZY);
	if(handler == NULL){
		LOGD("dlopen %s failed ", lib_path);
		return;
	}

	while(lib_hook_info_node != NULL){
		char* hook_info_name = lib_hook_info_node->hook_info_name;
		void* tmp_hook_info = dlsym(handler, hook_info_name);
		HOOK_INFO* hook_info;
		hook_info = (HOOK_INFO*) tmp_hook_info;
		if(hook_info != NULL && !strncmp(hook_info->libname, lib_name_native, strlen(lib_name_native))){
			if(!is_func_hooked(hooked_info_root, *hook_info)){
				LOGD("Try to hook %s in lib %s", hook_info->funcname, hook_info->libname);
				if(hook(&(hook_info->eph), getpid(), hook_info->libname, hook_info->funcname,
						hook_info->hook_arm, hook_info->hook_thumb)){
					LOGD("Hooked %s in lib %s", hook_info->funcname, hook_info->libname);
					add_hooked_info(hooked_info_root, *hook_info);
				}
			}
		}
		lib_hook_info_node = lib_hook_info_node->next;
	}
}



