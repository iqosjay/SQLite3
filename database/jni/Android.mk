LOCAL_PATH              :=  $(call my-dir)

#sqlite3
include $(CLEAR_VARS)
LOCAL_MODULE            :=  sqlite
LOCAL_C_INCLUDES        :=  $(LOCAL_PATH)/libs
LOCAL_CFLAGS            :=  -w -std=c11 -Os -DNULL=0 -DSOCKLEN_T=socklen_t -DLOCALE_NOT_USED -D_LARGEFILE_SOURCE=1
LOCAL_CFLAGS            +=  -DANDROID_NDK -DDISABLE_IMPORTGL -fno-strict-aliasing -fprefetch-loop-arrays -DAVOID_TABLES -DANDROID_TILE_BASED_DECODE -DANDROID_ARMV6_IDCT -DHAVE_STRCHRNUL=0
LOCAL_SRC_FILES         :=  $(LOCAL_PATH)/libs/sqlite/sqlite3.c

include    $(BUILD_STATIC_LIBRARY)

#sqlite3
include    $(CLEAR_VARS)
LOCAL_MODULE            :=  sqlite3
LOCAL_LDLIBS            +=  -llog
LOCAL_STATIC_LIBRARIES  +=  sqlite
LOCAL_C_INCLUDES        :=  $(LOCAL_PATH)
LOCAL_C_INCLUDES        +=  $(LOCAL_PATH)/libs
LOCAL_SRC_FILES         :=  jni_sqlite.cpp

include $(BUILD_SHARED_LIBRARY)