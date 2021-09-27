//
// Created by roy on 2021/9/27.
//

#ifndef DATABASE_LOGGER_H_
#define DATABASE_LOGGER_H_


#define LOG_ENABLED 1

#if (defined(LOG_ENABLED) && (LOG_ENABLED))
#  ifdef __ANDROID__
#    include <android/log.h>
#    define LOG_TAG    "sqlite3"
#    define LOGD(...)  (__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__))
#    define LOGE(...)  (__android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__))
#  else
#    include <stdio.h>
#    define LOGD(...)  printf(__VA_ARGS__)
#    define LOGE(...)  printf(__VA_ARGS__)
#  endif
#else
#    define LOGD(...)
#    define LOGE(...)
#endif//LOG_ENABLED

#endif//DATABASE_LOGGER_H_
