#include <jni.h>
#include <android/log.h>
#include <stdio.h>
#include <memory.h>
#include <unistd.h>
#include <stdlib.h>


void my_read();
void my_write();
void my_execmd();

#define LOG_TAG "EagleEyeTest"
#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

jint
Java_com_mindmac_eagleeyetest_Native_nativeEntry( JNIEnv* env, jobject thiz )
{
	test(12345, "hello world");
	my_write();
	my_read();
	my_execmd();
	return 0;
}

int test(int a, char *string){
	LOGD("a: %d, string: %s", a, string);
	return 1234;
}

void my_read(){

   FILE *fp;
   char buffer[200];

   /* Open file for both reading and writing */
   fp = fopen("/sdcard/hello.txt", "r");

   /* Read and display data */
   fread(buffer, 1, 150, fp);
   fclose(fp);
}

void my_write(){
   FILE *fp;
   char str[] = "This is tutorialspoint.com";

   fp = fopen( "/sdcard/hello.txt" , "w" );
   fwrite(str , 1 , sizeof(str) , fp );

   fclose(fp);
}

void my_execmd(){
	 LOGD("Execute process: getprop \n");
	 if(fork() == 0){
	 	int retval = execlp("getprop", "getprop", NULL);
	 	LOGD("Execute return: %d", retval);
	 }

}
