#brics_3d-jni: The C++ part

What is it?
-----------
 
The shared C++ libray called  brics_3d-jni (brics_3d-jni.so) that provides the C++ part of the wrapper. 
It provides functionality for using the Robot Scene Graph (RSG) that is a subset of the BRICS_3D library.

Installation
------------

A precompiled binary can he found [here](http://www.best-of-robotics.org/brics_3d/downloads/libbrics_3d-jni.so). 
To retrive the latest version follow the steps below: 

Dependencies:
 * [BRICS_3D](http://www.best-of-robotics.org/brics_3d/installation.html) including the HDF5 support (CMake flag is -DUSE_HDF5=true).
 * [Android NDK](https://developer.android.com/tools/sdk/ndk/index.html) (standalone toolchain)
 * [android-cmake toolchain](https://code.google.com/p/android-cmake/) (Please read the documentation).

Compilation:

 1. Cross compile the BRICS_3D library and its dependencies with the `android-cmake` toolchain.
 
 As a preperation all dependencies of the BRICS_3D library need to be satisfied. I.e. all those libraries have to be cross-compiled.
 The BRICS_3D library has the following dependencies:
 
 * Eigen
 * Boost
 * HDF5

Cross compilation of Eigen and Boost are already coverd by the android-cmake. cf. http://android-cmake.googlecode.com/hg/documentation.html 
It is briefly summarized as follows:  
 
 ```
	cd <path_to_android-cmake>/common-libs/eigen
	sh ./get_eigen.sh
	cd eigen-android
	mkdir androidbuild
	cd androidbuild/
	cmake -DCMAKE_TOOLCHAIN_FILE=<path_to_android-cmake_file>/android.toolchain.cmake -DARM_TARGET=armeabi .. 
	make
	make install

	cd <path_to_android-cmake>/common-libs/boost
	sh ./get_boost.sh
	mkdir androidbuild
	cd androidbuild/
	cmake -DCMAKE_TOOLCHAIN_FILE=<path_to_android-cmake_file>/android.toolchain.cmake -DARM_TARGET=armeabi .. 
	make
	make install
 ``` 

HDF5 library:

 Get a vervion >= 1.8.9 and < 1.8.12. Version [1.8.9](www.hdfgroup.org/ftp/HDF5/prev-releases/hdf5-1.8.9/src/hdf5-1.8.9.tar.gz) is recommended.

 ```
 	#uncomment in H5pubconf.h: the line: /* #undef H5_HAVE_GETPWUID */
 		
	mkdir androidbuild
	cd androidbuild/
	cmake -DCMAKE_TOOLCHAIN_FILE=<path_to_android-cmake_file>/android.toolchain.cmake -DHDF5_BUILD_CPP_LIB=true -DHDF5_BUILD_HL_LIB=true -DARM_TARGET=armeabi .. 
	make
	
	# Here you have to interrupt and execute the following binaries on the target platform to correctly generate the settings for the library.
	./H5detect > H5Tinit.c
	./H5make_libsettings > H5lib_settings.c
	
	# Then comment out the respective Makefile sections for H5detect and H5make_libsettings such that the generated files are not overiden
	# with empty files.
	
	make
	make install
 ```
 
Then cross compile BRICS_3D: 
 
 ```
  	export HDF5_ROOT=<path_to_android_ndk>/standalone-toolchain-api5/user/armeabi
 	mkdir androidbuild
	cd androidbuild
	cmake -DCMAKE_TOOLCHAIN_FILE=<path_to_android-cmake_file>/android.toolchain.cmake -DARM_TARGET=armeabi -DUSE_HDF5=true ..
	make
	make install
 ```
 
 2. Cross compile *this* library as well with the `android-cmake` toolchain:
 
 ```
	export BRICS_3D_DIR=<path_to_android_ndk>/standalone-toolchain-api5/user/armeabi
	mkdir androidbuild
	cd androidbuild
	cmake -DCMAKE_TOOLCHAIN_FILE=<path_to_android-cmake_file>/android.toolchain.cmake -DARM_TARGET=armeabi ..
	make
 ```
 
 3. Copy the  libs/armeabi/libbrics_3d-jni.so to your Android project/app.
 
 