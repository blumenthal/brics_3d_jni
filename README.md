#JNI Wrapper for the BRICS_3D library

What is it?
-----------
 
This project provides a thin JNI wrapper layer for the *Robot Scene Graph* [(RSG)]
(http://www.best-of-robotics.org/brics_3d/worldmodel.html) part of the BRICS_3D library. 
The target system is *Android*.

This package consists of three packages:

1. A shared C++ libray called  [brics_3d-jni](cpp/README.md) (`brics_3d-jni.so`) 
   that provides the C++ part of the wrapper.    
2. The corresponting [Java RobotSceneGraph](java/README.md) (`robotscenegraph.jar`) 
   that reprents the Java part of the wrapper.
3. [Example](examples/README.md) Android applications that make use of the above 
   libraries. Both above packages are a prerequisite
   that have to be included as references in the "libs" folder of an Android application. 

 
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
Last update: 10.09.2014
 