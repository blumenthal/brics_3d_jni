#JNI Wrapper for BRICS_3D

What is it?
-----------
 
This project provides a thin JNI wrapper layer for the BRICS_3D library. The target system is Android.

Installation
------------

Dependencies:
 * [BRICS_3D](http://www.best-of-robotics.org/brics_3d/installation.html)
 * [Android NDK](https://developer.android.com/tools/sdk/ndk/index.html) (standalone toolchain)
 * [android-cmake toolchain](https://code.google.com/p/android-cmake/).

Compilation:
 1. Cross compile the BRICS_3D library with the `android-cmake` toolchain.
 2. Cross compile *this* library as well with the `android-cmake` toolchain:
 
 ```
 	mkdir androidbuild
	cd androidbuild
	cmake -DCMAKE_TOOLCHAIN_FILE=<path_to_android-cmake_file/android.toolchain.cmake> -DARM_TARGET=armeabi ..
	make
 ```
 
 3. Copy the  libs/armeabi/libbrics_3d-jni.so to your Android project/app.
 
Licensing
---------

This software is published under a dual-license: GNU Lesser General Public
License LGPL 2.1 and Modified BSD license. The dual-license implies that
users of this code may choose which terms they prefer. Please see the files
called LGPL-2.1 and BSDlicense.

Acknowledgements
----------------

This work was supported by the European FP7 project SHERPA (FP7-600958).


Impressum
---------

Written by Sebastian Blumenthal (blumenthal@locomotec.com)
Last update: 01.07.2014
 