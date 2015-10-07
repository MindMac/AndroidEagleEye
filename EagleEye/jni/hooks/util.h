#include "../base/hook.h"
#include <android/log.h>
#include <stdbool.h>

#define LOG_TAG "EagleEye"
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

#ifndef FRAMEWORK_SYSTEM_API
#define FRAMEWORK_SYSTEM_API 0x00
#endif

#ifndef NATIVE_SYSTEM_API
#define NATIVE_SYSTEM_API 0x10
#endif

#ifndef NATIVE_APP_API
#define NATIVE_APP_API 0x11
#endif

typedef struct hook_info{
	struct hook_t eph;
	char* libname;
	char* funcname;
	void* hook_arm;
	void* hook_thumb;
} HOOK_INFO;

typedef struct hooked_info_node{
	char* libname;
	char* funcname;
	struct hooked_info_node* next;
} HOOKED_INFO_NODE;

bool find_file_path_from_fd(int uid, int pid, int fd, char *buffer);
bool is_func_hooked(HOOKED_INFO_NODE* root, HOOK_INFO hook_info);
void add_hooked_info(HOOKED_INFO_NODE* root, HOOK_INFO hook_info);
int get_id();
void to_hex(char* input_string, char* output_string, int count);
void array_to_string(char *dest_string, char *const src_array[]);
