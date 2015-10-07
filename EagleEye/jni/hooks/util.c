#include "util.h"

#include <stdbool.h>
#include <string.h>
#include <stdlib.h>
#include <time.h>
#include <errno.h>

//void parse_log_app_uids(const char* log_app_uids){
//	char *token, *string, *tofree;
//	tofree = string = strdup(log_app_uids);
//	int i = 0;
//	long app_uid;
//	char* end_ptr;
//	while ((token = strsep(&string, "|")) != NULL){
//		if( i >= MAX_APP_UID_NUM)
//			break;
//		app_uid = strtol(token, &end_ptr, 10);
//		log_app_uid_list[i] = app_uid;
//		i += 1;
//	}
//	free(tofree);
//}
//
//bool is_app_need_log(int target_app_uid){
//	int i = 0;
//	for(i=0; i < MAX_APP_UID_NUM; i++){
//		int app_uid = log_app_uid_list[i];
//		if(app_uid == target_app_uid)
//			return true;
//	}
//	return false;
//}


bool is_func_hooked(HOOKED_INFO_NODE* root, HOOK_INFO hook_info){
	if(root == NULL)
		return false;
	HOOKED_INFO_NODE* hooked_info_node = root->next;
	while(hooked_info_node != NULL){
		if(!strcmp(hooked_info_node->libname, hook_info.libname) &&
				!strcmp(hooked_info_node->funcname, hook_info.funcname)){
			return true;
		}
		hooked_info_node = hooked_info_node->next;
	}
	return false;
}

void add_hooked_info(HOOKED_INFO_NODE* root, HOOK_INFO hook_info){
	if(root == NULL)
		return;
	HOOKED_INFO_NODE *temp, *right;
	temp = (HOOKED_INFO_NODE*) malloc(sizeof(HOOKED_INFO_NODE));
	temp->libname = hook_info.libname;
	temp->funcname = hook_info.funcname;
	right = root;
	while(right->next != NULL)
		right = right->next;
	right->next = temp;
	right = temp;
	right->next = NULL;
}

bool find_file_path_from_fd(int uid, int pid, int fd, char *buffer){
	char ppath[20];
	char rpath[120];
	char *pbuffer = buffer;

	int err;

	int len, i;

	snprintf(ppath, 20, "/proc/%d/fd/%d", pid, fd);
	err = readlink(ppath, rpath, sizeof(rpath));

	if (err>=0)
	{
		int pos = (err < sizeof(rpath)) ? err : sizeof(rpath)-1;
		rpath[pos] = '\0';

		len = strlen(rpath);
		for (i = 0; i < len; i++) {
			sprintf(pbuffer, "%02x", rpath[i]);
			pbuffer += 2;
		}
		return true;
	}else{
		LOGD("Error finding path for uid:%d, pid:%d, fd:%d, errormsg:%s", uid, pid, fd, strerror(errno));
		return false;
	}
}

int get_id(){
	return (int)time(NULL);
}

void to_hex(char* input_string, char* output_string, int count){
	char *tmp_output_string = output_string;
	int i = 0;
	for(i = 0; i < count ;i++ )
	{
		sprintf(tmp_output_string, "%02x", input_string[i]);
		tmp_output_string += 2;
	}
}

// dest_string should be freeed by the user
void array_to_string(char *dest_string, char *const src_array[]){
	int count = 0;
	int str_len = 0;

	if(src_array == NULL)
		return;
	for(count=0; src_array[count]; count++){
		str_len += (strlen(src_array[count]) + 5);
	}
	dest_string = (char*) malloc(str_len);
	if(dest_string == NULL){
		LOGD("Malloc in array_to_string fail");
		return;
	}

	for(count=0; src_array[count]; count++){
		if(count > 0)
			strcat(dest_string, ",");
		strcat(dest_string, "\\\"");
		strcat(dest_string, src_array[count]);
		strcat(dest_string, "\\\"");
	}

}
