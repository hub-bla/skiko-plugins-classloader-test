package org.jetbrains.skiko.tests

// Same declarations as in the Skiko branch (skiko-platform/src/awtTest/.../tests/CoreInstanceProbe.kt): the JNI names
// are derived from the file and function names, so the file must be called CoreInstanceProbe.kt.

// Implemented in the test helpers of the core library (src/jvmTest/cpp), identifies its copy.
external fun _nCoreInstance(): Long

// Implemented in the test helpers of skiko-platform (src/jvmTest/cpp), asks the core library it is linked to.
external fun _nCoreInstanceSeenByPlatform(): Long
