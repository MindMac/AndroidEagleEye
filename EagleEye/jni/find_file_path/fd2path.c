#include "../hooks/util.h"
#include <jni.h>
#include <string.h>
#include <stdbool.h>
#include <errno.h>

jboolean Java_com_mindmac_eagleeye_NativeEntry_logFilePathFromFd(
	 JNIEnv* env, jobject thiz, jint uid, jint pid, jint fd, jint id)
{

	char file_path[(2*120)+1] = "";
	if(find_file_path_from_fd(uid, pid, fd, file_path)){
		LOGD("Success finding path for uid:%d, pid:%d, fd:%d", uid, pid, fd);

		LOGI("{\"Basic\":[\"%d\",\"%d\",\"false\"], \"FdAccess\": {\"path\": \"%s\", \"id\": \"%d\" }}",
				uid, FRAMEWORK_SYSTEM_API, file_path, id);
		return true;
	}

	return false;

}
