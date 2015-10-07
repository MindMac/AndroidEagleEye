#include "util.h"
#include <unistd.h>
#include <stdio.h>
#include <errno.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <sys/uio.h>

int MAX_DATA_LEN = 300;

/**
 *  IO : open, read, write - kernel functions
 *  fopen, fread, fwrite - lib functions, can be ignored
 */
int eagle_open(char *filename, int access, int permission);
HOOK_INFO system_hook_info_open = {{}, "libc", "open", eagle_open, eagle_open};
int eagle_open(char *filename, int access, int permission)
{
	// Declare the original method
	int (*orig_open)(char *filename, int access, int permission);
	// Set variable eph to the HOO_INFO struct
	struct hook_t eph = system_hook_info_open.eph;
	// Set the original method address
	orig_open = (void*)eph.orig;
	// Invoke hook_precall
	hook_precall(&eph);
	// Invoke original method
	int status = orig_open(filename, access, permission);
	// Invoke hook_postcall
	hook_postcall(&eph);

	// Log the information as you need
	int uid = getuid();
	if(filename != NULL)
		LOGI("{\"Basic\":[\"%d\",\"%d\",\"false\"],\"InvokeApi\":{\"%s\":{\"filename\":\"%s\",\"access\":\"%d\",\"permission\":\"%d\"},"
			"\"return\":{\"int\":\"%d\"}}}",uid, NATIVE_SYSTEM_API, "open", filename, access, permission, status);
	return status;
}


int eagle_read(int handle, void *buffer, int nbyte);
HOOK_INFO system_hook_info_read = {{}, "libc", "read", eagle_read, eagle_read};
int eagle_read(int handle, void *buffer, int nbyte){
	int (*orig_read)(int handle, void *buffer, int nbyte);
	struct hook_t eph = system_hook_info_read.eph;
	orig_read = (void*)eph.orig;
	hook_precall(&eph);
	int read_count = orig_read(handle, buffer, nbyte);
	hook_postcall(&eph);
	int uid = getuid();

	int id = get_id();
	int pid = getpid();
	char file_path[(2*120)+1] = "";
	if(find_file_path_from_fd(uid, pid, handle, file_path)){
		LOGD("Success finding path for uid:%d, pid:%d, fd:%d", uid, pid, handle);
	}

	char *tmp_buf = buffer;
	int total_count = read_count;
	// Log content in hex format, each character is encoded with 2 bytes
	// Malloc memory and free it at the end of function
	char *read_content = (char*) malloc(MAX_DATA_LEN * 2 + 1);
	while(total_count > 0 && tmp_buf != NULL && read_content != NULL){
		int copy_count = (total_count >= MAX_DATA_LEN ? MAX_DATA_LEN : total_count);
		to_hex(tmp_buf, read_content, copy_count);
		LOGI("{\"Basic\":[\"%d\",\"%d\",\"false\"],\"InvokeApi\":{\"%s\":{\"handle\":\"%d\",\"buffer\":\"%s\",\"nbyte\":\"%d\",\"id\":\"%d\",\"path\":\"%s\"},"
			 "\"return\":{\"int\":\"%d\"}}}", uid, NATIVE_SYSTEM_API, "read", handle, read_content, nbyte, id, file_path, read_count);
		total_count -= copy_count;
		tmp_buf += copy_count;
	}
	free(read_content);
	return read_count;
}


int eagle_write(int handle, void *buffer, int nbyte);
HOOK_INFO system_hook_info_write = {{}, "libc", "write", eagle_write, eagle_write};
int eagle_write(int handle, void *buffer, int nbyte){
	int (*orig_write)(int handle, void *buffer, int nbyte);
	struct hook_t eph = system_hook_info_write.eph;
	orig_write = (void*)eph.orig;
	hook_precall(&eph);
	int write_count = orig_write(handle, buffer, nbyte);
	hook_postcall(&eph);
	int uid = getuid();

	int id = get_id();
	int pid = getpid();
	char file_path[(2*120)+1] = "";
	if(find_file_path_from_fd(uid, pid, handle, file_path)){
		LOGD("Success finding path for uid:%d, pid:%d, fd:%d ", uid, pid, handle);
	}

	char *tmp_buf = buffer;
	int total_count = write_count;
	char *write_content = (char*) malloc(MAX_DATA_LEN * 2 + 1);
	while(total_count > 0 && tmp_buf != NULL && write_content != NULL){
		int copy_count = (total_count >= MAX_DATA_LEN ? MAX_DATA_LEN : total_count);
		to_hex(tmp_buf, write_content, copy_count);
		LOGI("{\"Basic\":[\"%d\",\"%d\",\"false\"],\"InvokeApi\":{\"%s\":{\"handle\":\"%d\",\"buffer\":\"%s\",\"nbyte\":\"%d\",\"id\":\"%d\",\"path\":\"%s\"},"
				"\"return\":{\"int\":\"%d\"}}}",uid, NATIVE_SYSTEM_API, "write", handle, write_content, nbyte, id, file_path, write_count);
		total_count -= copy_count;
		tmp_buf += copy_count;
	}
	free(write_content);
	return write_count;
}

/*
FILE* eagle_fopen(const char* path, const char* mode);
HOOK_INFO system_hook_info_fopen = {{}, "libc", "fopen", eagle_fopen, eagle_fopen};
FILE* eagle_fopen(const char* path, const char* mode)
{
	FILE* (*orig_fopen)(const char* path, const char* mode);
	struct hook_t eph = system_hook_info_fopen.eph;
	orig_fopen = (void*)eph.orig;
	hook_precall(&eph);
	FILE* file = orig_fopen(path, mode);
	hook_postcall(&eph);
	int uid = getuid();
	LOGI("{\"Basic\":[\"%d\",\"%d\",\"false\"],\"InvokeApi\":{\"%s\":{\"path\":\"%s\",\"mode\":\"%s\"},\"return\":{\"FILE\":\"%p\"}}}",
		uid, NATIVE_SYSTEM_API, "fopen", path, mode, file);
	return file;
}

size_t eagle_fread(void *buf, size_t size, size_t count, FILE *fp);
HOOK_INFO system_hook_info_fread = {{}, "libc", "fread", eagle_fread, eagle_fread};
size_t eagle_fread(void *buf, size_t size, size_t count, FILE *fp){
	int (*orig_fread)(void *buf, size_t size, size_t count, FILE *fp);
	struct hook_t eph = system_hook_info_fread.eph;
	orig_fread = (void*)eph.orig;
	hook_precall(&eph);
	size_t read_count = orig_fread(buf, size, count, fp);
	hook_postcall(&eph);
	int uid = getuid();
	if(read_count == 0)
		return read_count;

	int id = get_id();
	int fd = fileno(fp);
	int pid = getpid();
	char file_path[(2*120)+1] = "";
	if(find_file_path_from_fd(uid, pid, fd, file_path)){
		LOGD("Success finding path for uid:%d, pid:%d, fd:%d", uid, pid, fd);
	}

	int total_count = size * read_count;
	char *tmp_buf = buf;

	while(total_count > 0){
		int copy_count = (total_count >= MAX_DATA_LEN ? MAX_DATA_LEN : total_count);
		char read_content[copy_count * 2];
		to_hex(tmp_buf, read_content, copy_count);
		LOGI("{\"Basic\":[\"%d\",\"%d\",\"false\"],\"InvokeApi\":{\"%s\":{\"buf\":\"%s\",\"size\":\"%d\",\"count\":\"%d\",\"id\":\"%d\",\"path\":\"%s\"},"
				"\"return\":{\"size_t\":\"%d\"}}}",
			uid, NATIVE_SYSTEM_API, "fread", read_content, size, count, id, file_path, read_count);
		total_count -= copy_count;
		tmp_buf += copy_count;
	}
	return read_count;
}


size_t eagle_fwrite(const void *buf, size_t size, size_t count, FILE *fp);
HOOK_INFO system_hook_info_fwrite = {{}, "libc", "fwrite", eagle_fwrite, eagle_fwrite};
size_t eagle_fwrite(const void *buf, size_t size, size_t count, FILE *fp){
	size_t (*orig_fwrite)(const void *buf, size_t size, size_t count, FILE *fp);
	struct hook_t eph = system_hook_info_fwrite.eph;
	orig_fwrite = (void*)eph.orig;
	hook_precall(&eph);
	size_t write_count = orig_fwrite(buf, size, count, fp);
	hook_postcall(&eph);
	int uid = getuid();
	if(write_count == 0)
		return write_count;

	int id = get_id();
	int fd = fileno(fp);
	int pid = getpid();
	char file_path[(2*120)+1] = "";
	if(find_file_path_from_fd(uid, pid, fd, file_path)){
		LOGD("Success finding path for uid:%d, pid:%d, fd:%d ", uid, pid, fd);
	}

	int total_count = size * write_count;
	char *tmp_buf = buf;

	while(total_count > 0){
		int copy_count = (total_count >= MAX_DATA_LEN ? MAX_DATA_LEN : total_count);
		char write_content[copy_count * 2];
		to_hex(tmp_buf, write_content, copy_count);
		LOGI("{\"Basic\":[\"%d\",\"%d\",\"false\"],\"InvokeApi\":{\"%s\":{\"buf\":\"%s\",\"size\":\"%d\",\"count\":\"%d\",\"id\":\"%d\",\"path\":\"%s\"},"
				"\"return\":{\"size_t\":\"%d\"}}}",
			uid, NATIVE_SYSTEM_API, "fwrite", write_content, size, count, id, file_path, write_count);
		total_count -= copy_count;
		tmp_buf += copy_count;
	}
	return write_count;
}*/

/**
 * Socket: socket, connect, bind, listen, accept, sendto, recvfrom
 */
int eagle_socket(int domain, int type, int protocol);
HOOK_INFO system_hook_info_socket = {{}, "libc", "socket", eagle_socket, eagle_socket};
int eagle_socket(int domain, int type, int protocol){
	int (*orig_socket)(int domain, int type, int protocol);
	struct hook_t eph = system_hook_info_socket.eph;
	orig_socket = (void*)eph.orig;
	hook_precall(&eph);
	int status = orig_socket(domain, type, protocol);
	hook_postcall(&eph);

	int uid = getuid();

	LOGI("{\"Basic\":[\"%d\",\"%d\",\"false\"],\"InvokeApi\":{\"%s\":{\"domain\":\"%d\",\"type\":\"%d\",\"protocol\":\"%d\"},"
			"\"return\":{\"int\":\"%d\"}}}",uid, NATIVE_SYSTEM_API, "socket", domain, type, protocol, status);

	return status;
}

int eagle_connect(int socket, const struct sockaddr *address, socklen_t address_len);
HOOK_INFO system_hook_info_connect = {{}, "libc", "connect", eagle_connect, eagle_connect};
int eagle_connect(int socket, const struct sockaddr *address, socklen_t address_len){
	int (*orig_connect)(int socket, const struct sockaddr *address, socklen_t address_len);
	struct hook_t eph = system_hook_info_connect.eph;
	orig_connect = (void*)eph.orig;
	hook_precall(&eph);
	int status = orig_connect(socket, address, address_len);
	hook_postcall(&eph);

	int uid = getuid();

	LOGI("{\"Basic\":[\"%d\",\"%d\",\"false\"],\"InvokeApi\":{\"%s\":{\"socket\":\"%d\",\"address->sa_family\":\"%d\",\"address->sa_data\":\"%s\",\"address_len\":\"%d\"},"
			"\"return\":{\"int\":\"%d\"}}}",uid, NATIVE_SYSTEM_API, "connect", socket, address->sa_family, address->sa_data, address_len, status);

	return status;

}

int eagle_bind(int sockfd, const struct sockaddr *addr, socklen_t addrlen);
HOOK_INFO system_hook_info_bind = {{}, "libc", "bind", eagle_bind, eagle_bind};
int eagle_bind(int sockfd, const struct sockaddr *addr, socklen_t addrlen){
	int (*orig_bind)(int sockfd, const struct sockaddr *addr, socklen_t addrlen);
	struct hook_t eph = system_hook_info_bind.eph;
	orig_bind = (void*)eph.orig;
	hook_precall(&eph);
	int status = orig_bind(sockfd, addr, addrlen);
	hook_postcall(&eph);

	int uid = getuid();

	LOGI("{\"Basic\":[\"%d\",\"%d\",\"false\"],\"InvokeApi\":{\"%s\":{\"sockfd\":\"%d\",\"addr->sa_family\":\"%d\",\"addr->sa_data\":\"%s\",\"addrlen\":\"%d\"},"
			"\"return\":{\"int\":\"%d\"}}}", uid, NATIVE_SYSTEM_API, "bind", sockfd, addr->sa_family, addr->sa_data, addrlen, status);

	return status;
}

int eagle_listen(int sockfd, int backlog);
HOOK_INFO system_hook_info_listen = {{}, "libc", "listen", eagle_listen, eagle_listen};
int eagle_listen(int sockfd, int backlog){
	int (*orig_listen)(int sockfd, int backlog);
	struct hook_t eph = system_hook_info_listen.eph;
	orig_listen = (void*)eph.orig;
	hook_precall(&eph);
	int status = orig_listen(sockfd, backlog);
	hook_postcall(&eph);

	int uid = getuid();

	LOGI("{\"Basic\":[\"%d\",\"%d\",\"false\"],\"InvokeApi\":{\"%s\":{\"sockfd\":\"%d\",\"backlog\":\"%d\"},"
			"\"return\":{\"int\":\"%d\"}}}",uid, NATIVE_SYSTEM_API, "listen", sockfd, backlog, status);

	return status;
}

int eagle_accept(int sockfd, struct sockaddr *addr, socklen_t *addrlen);
HOOK_INFO system_hook_info_accept = {{}, "libc", "accept", eagle_accept, eagle_accept};
int eagle_accept(int sockfd, struct sockaddr *addr, socklen_t *addrlen){
	int (*orig_accept)(int sockfd, struct sockaddr *addr, socklen_t *addrlen);
	struct hook_t eph = system_hook_info_accept.eph;
	orig_accept = (void*)eph.orig;
	hook_precall(&eph);
	int status = orig_accept(sockfd, addr, addrlen);
	hook_postcall(&eph);

	int uid = getuid();
	int sa_family = -1;
	char *sa_data;
	if(addr == NULL){
		sa_family = -1;
		sa_data = "";
	}else{
		sa_family = addr->sa_family;
		sa_data = addr->sa_data;
	}
	LOGI("{\"Basic\":[\"%d\",\"%d\",\"false\"],\"InvokeApi\":{\"%s\":{\"sockfd\":\"%d\",\"addr->sa_family\":\"%d\",\"addr->sa_data\":\"%s\",\"addrlen\":\"%p\"},"
		"\"return\":{\"int\":\"%d\"}}}",uid, NATIVE_SYSTEM_API, "accept", sockfd, sa_family, sa_data, addrlen, status);

	return status;
}

int eagle_sendto(int s, const void *msg, size_t len, int flags, const struct sockaddr *to, socklen_t tolen);
HOOK_INFO system_hook_info_sendto = {{}, "libc", "sendto", eagle_sendto, eagle_sendto};
int eagle_sendto(int s, const void *msg, size_t len, int flags, const struct sockaddr *to, socklen_t tolen){
	int (*orig_sendto)(int s, const void *msg, size_t len, int flags, const struct sockaddr *to, socklen_t tolen);
	struct hook_t eph = system_hook_info_sendto.eph;
	orig_sendto = (void*)eph.orig;
	hook_precall(&eph);
	int send_count = orig_sendto(s, msg, len, flags, to, tolen);
	hook_postcall(&eph);

	int uid = getuid();
	int sa_family = -1;
	char *sa_data;
	if(to == NULL){
		sa_family = -1;
		sa_data = "";
	}else{
		sa_family = to->sa_family;
		sa_data = to->sa_data;
	}

	char *tmp_buf = msg;
	int total_count = send_count;
	char *send_content = (char*) malloc(MAX_DATA_LEN * 2 + 1);
	while(total_count > 0 && send_content != NULL && tmp_buf != NULL){
		int copy_count = (total_count >= MAX_DATA_LEN ? MAX_DATA_LEN : total_count);
		to_hex(tmp_buf, send_content, copy_count);
		LOGI("{\"Basic\":[\"%d\",\"%d\",\"false\"],\"InvokeApi\":{\"%s\":{\"s\":\"%d\",\"msg\":\"%s\",\"len\":\"%d\",\"flags\":\"%d\",\"to->sa_family\":\"%d\","
			 "\"to->sa_data\":\"%s\",\"tolen\":\"%d\"},\"return\":{\"int\":\"%d\"}}}",
			 uid, NATIVE_SYSTEM_API, "sendto", s, send_content, len, flags, sa_family, sa_data, tolen, send_count);
		total_count -= copy_count;
		tmp_buf += copy_count;
	}
	free(send_content);
	return send_count;
}

//int eagle_sendmsg(int s, const struct msghdr *msg, int flags);
//HOOK_INFO system_hook_info_sendmsg = {{}, "libc", "sendmsg", eagle_sendmsg, eagle_sendmsg};
//int eagle_sendmsg(int s, const struct msghdr *msg, int flags){
//	int (*orig_sendmsg)(int s, const struct msghdr *msg, int flags);
//	struct hook_t eph = system_hook_info_sendmsg.eph;
//	orig_sendmsg = (void*)eph.orig;
//	hook_precall(&eph);
//	int status = orig_sendmsg(s, msg, flags);
//	hook_postcall(&eph);
//
//	int uid = getuid();
//
//	char *msg_name = msg->msg_name;
//	if(msg_name == NULL)
//		msg_name = "";
//	struct iovec *msg_iov = msg->msg_iov;
//	if(msg_iov != NULL){
//		size_t iovlen = msg_iov->iov_len;
//		char msg_content[iovlen * 2 + 1];
//		to_hex(msg_iov->iov_base, msg_content, iovlen);
//		LOGI("{\"Basic\":[\"%d\",\"%d\",\"false\"],\"InvokeApi\":{\"%s\":{\"s\":\"%d\",\"msg->msg_name\":\"%s\",\"msg->msg_iov->iov_base\":\"%s\",\"flags\":\"%d\"},\"return\":{\"int\":\"%d\"}}}",
//				uid, NATIVE_SYSTEM_API, "sendmsg", s, msg_name, msg_content, flags, status);
//	}else
//		LOGI("{\"Basic\":[\"%d\",\"%d\",\"false\"],\"InvokeApi\":{\"%s\":{\"s\":\"%d\",\"msg->msg_name\":\"%s\",\"msg->msg_iov->iov_base\":\"%s\",\"flags\":\"%d\"},\"return\":{\"int\":\"%d\"}}}",
//						uid, NATIVE_SYSTEM_API, "sendmsg", s, msg_name, "", flags, status);
//
//	return status;
//}

int eagle_recvfrom(int s, void *buf, size_t len, int flags, struct sockaddr *from, socklen_t *fromlen);
HOOK_INFO system_hook_info_recvfrom = {{}, "libc", "recvfrom", eagle_recvfrom, eagle_recvfrom};
int eagle_recvfrom(int s, void *buf, size_t len, int flags, struct sockaddr *from, socklen_t *fromlen){
	int (*orig_recvfrom)(int s, void *buf, size_t len, int flags, struct sockaddr *from, socklen_t *fromlen);
	struct hook_t eph = system_hook_info_recvfrom.eph;
	orig_recvfrom = (void*)eph.orig;
	hook_precall(&eph);
	int recv_count = orig_recvfrom(s, buf, len, flags, from, fromlen);
	hook_postcall(&eph);

	int uid = getuid();

	int sa_family = -1;
	char *sa_data;
	if(from == NULL){
		sa_family = -1;
		sa_data = "";
	}else{
		sa_family = from->sa_family;
		sa_data = from->sa_data;
	}

	char *tmp_buf = buf;
	int total_count = recv_count;
	char *recv_content = (char*) malloc(MAX_DATA_LEN * 2 + 1);
	while(total_count > 0 && recv_content != NULL && tmp_buf != NULL){
		int copy_count = (total_count >= MAX_DATA_LEN ? MAX_DATA_LEN : total_count);
		to_hex(tmp_buf, recv_content, copy_count);
		LOGI("{\"Basic\":[\"%d\",\"%d\",\"false\"],\"InvokeApi\":{\"%s\":{\"s\":\"%d\",\"buf\":\"%s\",\"len\":\"%d\",\"flags\":\"%d\",\"from->sa_family\":\"%d\","
			 "\"from->sa_data\":\"%s\",\"fromlen\":\"%p\"},\"return\":{\"int\":\"%d\"}}}",
			uid, NATIVE_SYSTEM_API, "recvfrom", s, recv_content, len, flags, sa_family, sa_data, fromlen, recv_count);
		total_count -= copy_count;
		tmp_buf += copy_count;
	}
	free(recv_content);
	return recv_count;
}

/**
 * Command execution: execve
 */
int eagle_execve(const char *filename, char *const argv[ ], char *const envp[ ]);
HOOK_INFO system_hook_info_execve = {{}, "libc", "execve", eagle_execve, eagle_execve};
int eagle_execve(const char *filename, char *const argv[ ], char *const envp[ ]){
	int (*orig_execve)(const char *filename, char *const argv[ ], char *const envp[ ]);
	struct hook_t eph = system_hook_info_execve.eph;
	orig_execve = (void*)eph.orig;
	hook_precall(&eph);
	int status = orig_execve(filename, argv, envp);
	hook_postcall(&eph);

	int uid = getuid();

	// Log message for argv
	char *argv_string = NULL;
	array_to_string(argv_string, argv);
	if(argv_string == NULL)
		argv_string = "";
	// Log message for envp
	char *envp_string = NULL;
	array_to_string(envp_string, envp);
	if(envp_string == NULL)
		envp_string = "";

	LOGI("{\"Basic\":[\"%d\",\"%d\",\"false\"],\"InvokeApi\":{\"%s\":{\"filename\":\"%s\",\"argv\":\"[%s]\",\"evnp\":\"[%s]\"},"
		"\"return\":{\"int\":\"%d\"}}}",uid, NATIVE_SYSTEM_API, "execve", filename, argv_string, envp_string, status);
	if(argv_string != NULL && strcmp(argv_string, ""))
		free(argv_string);
	if(envp_string != NULL && strcmp(envp_string, ""))
		free(envp_string);
	return status;
}

/**
 * Application scope native lib
 */

int eagle_test(int a, char* string);
HOOK_INFO custom_hook_info_test = {{}, "libeagleeyetest", "test", eagle_test, eagle_test};

int eagle_test(int a, char* string){
	int (*orig_test)(int a, char* string);
	struct hook_t eph = custom_hook_info_test.eph;
	orig_test = (void*)eph.orig;
	hook_precall(&eph);
	int res = orig_test(a, string);
	hook_postcall(&eph);
	int uid = getuid();
	LOGI("{\"Basic\":[\"%d\",\"%d\",\"false\"],\"InvokeApi\":{\"%s\":{\"a\":\"%d\",\"string\":\"%s\"},\"return\":{\"int\":\"%d\"}}}",
		uid, NATIVE_APP_API, "test", a, string, res);
	return res;
}
