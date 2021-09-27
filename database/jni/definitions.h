//
// Created by roy on 2021/9/25.
//

#ifndef DATABASE_DEFINITIONS_H_
#define DATABASE_DEFINITIONS_H_

#ifdef _MSC_VER
#  define FORCEINLINE  __forceinline                                //MSVC
#elif defined __GNUC__
#  define FORCEINLINE  __inline__ __attribute__((always_inline))    //Linux/AppOS X
#else
#  define FORCEINLINE  inline                                       //no match
#endif

#define SAFE_DELETE_PTR(PTR)    do { if (PTR) { delete  (PTR);  (PTR) = nullptr; } } while(0)
#define SAFE_DELETE_BUF(BUF)    do { if (BUF) { delete[](BUF);  (BUF) = nullptr; } } while(0)

#endif//DATABASE_DEFINITIONS_H_
