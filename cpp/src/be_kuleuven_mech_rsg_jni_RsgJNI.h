/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class be_kuleuven_mech_rsg_jni_RsgJNI */

#ifndef _Included_be_kuleuven_mech_rsg_jni_RsgJNI
#define _Included_be_kuleuven_mech_rsg_jni_RsgJNI
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     be_kuleuven_mech_rsg_jni_RsgJNI
 * Method:    initialize
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_initialize
  (JNIEnv *, jclass);

/*
 * Class:     be_kuleuven_mech_rsg_jni_RsgJNI
 * Method:    cleanup
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_cleanup
  (JNIEnv *, jclass);

/*
 * Class:     be_kuleuven_mech_rsg_jni_RsgJNI
 * Method:    resend
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_resend
  (JNIEnv *, jclass);

JNIEXPORT jlong JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_getRootId
  (JNIEnv *, jclass);

JNIEXPORT jlong JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_addGeometricNode
  (JNIEnv *, jclass, jlong, jlong, jlong, jlong, jboolean);

JNIEXPORT jlongArray JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_getNodes
  (JNIEnv *, jclass, jlong);

JNIEXPORT jlongArray JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_getGroupChildren
  (JNIEnv *, jclass, jlong);

JNIEXPORT jlong JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_getGeometry
  (JNIEnv *, jclass, jlong);

JNIEXPORT jboolean JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_deleteNode
  (JNIEnv *, jclass, jlong);


/*
 * Class:     be_kuleuven_mech_rsg_jni_RsgJNI
 * Method:    getSceneObjects
 * Signature: (J)[J
 */
JNIEXPORT jlongArray JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_getSceneObjects
  (JNIEnv *, jclass, jlong);

/*
 * Class:     be_kuleuven_mech_rsg_jni_RsgJNI
 * Method:    insertTransform
 * Signature: (JJ)V
 */
JNIEXPORT void JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_insertTransform
  (JNIEnv *, jclass, jlong, jlong);

/*
 * Class:     be_kuleuven_mech_rsg_jni_RsgJNI
 * Method:    getCurrentTransform
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_getCurrentTransform
  (JNIEnv *, jclass, jlong);

/*
 * Class:     be_kuleuven_mech_rsg_jni_RsgJNI
 * Method:    addSceneObject
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_addSceneObject
  (JNIEnv *, jclass, jlong);

/*
 * Class:     be_kuleuven_mech_rsg_jni_RsgJNI
 * Method:    createSceneObject
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_createSceneObject
  (JNIEnv *, jclass);

/*
 * Class:     be_kuleuven_mech_rsg_jni_RsgJNI
 * Method:    getIdFromSceneObject
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_getIdFromSceneObject
  (JNIEnv *, jclass, jlong);

/*
 * Class:     be_kuleuven_mech_rsg_jni_RsgJNI
 * Method:    getParentIdFromSceneObject
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_getParentIdFromSceneObject
  (JNIEnv *, jclass, jlong);

/*
 * Class:     be_kuleuven_mech_rsg_jni_RsgJNI
 * Method:    addTransformToSceneObject
 * Signature: (JJ)Z
 */
JNIEXPORT jboolean JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_addTransformToSceneObject
  (JNIEnv *, jclass, jlong, jlong);

/*
 * Class:     be_kuleuven_mech_rsg_jni_RsgJNI
 * Method:    getTransformFromSceneObject
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_getTransformFromSceneObject
  (JNIEnv *, jclass, jlong);

/*
 * Class:     be_kuleuven_mech_rsg_jni_RsgJNI
 * Method:    addShapeToSceneObject
 * Signature: (JJ)Z
 */
JNIEXPORT jboolean JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_addShapeToSceneObject
  (JNIEnv *, jclass, jlong, jlong);

/*
 * Class:     be_kuleuven_mech_rsg_jni_RsgJNI
 * Method:    getShapeFromSceneObject
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_getShapeFromSceneObject
  (JNIEnv *, jclass, jlong);

/*
 * Class:     be_kuleuven_mech_rsg_jni_RsgJNI
 * Method:    addAttributeListToSceneObject
 * Signature: (JJ)Z
 */
JNIEXPORT jboolean JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_addAttributeListToSceneObject
  (JNIEnv *, jclass, jlong, jlong);

/*
 * Class:     be_kuleuven_mech_rsg_jni_RsgJNI
 * Method:    getAttributeListFromSceneObject
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_getAttributeListFromSceneObject
  (JNIEnv *, jclass, jlong);

/*
 * Class:     be_kuleuven_mech_rsg_jni_RsgJNI
 * Method:    createId
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_createId
  (JNIEnv *, jclass);

/*
 * Class:     be_kuleuven_mech_rsg_jni_RsgJNI
 * Method:    getIdAsString
 * Signature: (J)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_getIdAsString
  (JNIEnv *, jclass, jlong);

/*
 * (D)J
 */
JNIEXPORT jlong JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_createTimeStamp
  (JNIEnv *, jclass, jdouble);

JNIEXPORT jdouble JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_getTimeStampAsSeconds
  (JNIEnv *, jclass, jlong);

JNIEXPORT jlong JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_getCurrentTimestamp
  (JNIEnv *, jclass);



/*
 * Class:     be_kuleuven_mech_rsg_jni_RsgJNI
 * Method:    createBox
 * Signature: (DDD)J
 */
JNIEXPORT jlong JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_createBox
  (JNIEnv *, jclass, jdouble, jdouble, jdouble);

/*
 * Class:     be_kuleuven_mech_rsg_jni_RsgJNI
 * Method:    getBoxSizeX
 * Signature: (J)D
 */
JNIEXPORT jdouble JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_getBoxSizeX
  (JNIEnv *, jclass, jlong);

/*
 * Class:     be_kuleuven_mech_rsg_jni_RsgJNI
 * Method:    getBoxSizeY
 * Signature: (J)D
 */
JNIEXPORT jdouble JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_getBoxSizeY
  (JNIEnv *, jclass, jlong);

/*
 * Class:     be_kuleuven_mech_rsg_jni_RsgJNI
 * Method:    getBoxSizeZ
 * Signature: (J)D
 */
JNIEXPORT jdouble JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_getBoxSizeZ
  (JNIEnv *, jclass, jlong);

/*
 * Class:     be_kuleuven_mech_rsg_jni_RsgJNI
 * Method:    isBox
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_isBox
  (JNIEnv *, jclass, jlong);

/*
 * Class:     be_kuleuven_mech_rsg_jni_RsgJNI
 * Method:    createSphere
 * Signature: (D)J
 */
JNIEXPORT jlong JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_createSphere
  (JNIEnv *, jclass, jdouble);

/*
 * Class:     be_kuleuven_mech_rsg_jni_RsgJNI
 * Method:    getSphereRadius
 * Signature: (J)D
 */
JNIEXPORT jdouble JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_getSphereRadius
  (JNIEnv *, jclass, jlong);

/*
 * Class:     be_kuleuven_mech_rsg_jni_RsgJNI
 * Method:    isSphere
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_isSphere
  (JNIEnv *, jclass, jlong);

/*
 * Class:     be_kuleuven_mech_rsg_jni_RsgJNI
 * Method:    createTransform
 * Signature: (DDDDDDDDDDDD)J
 */
JNIEXPORT jlong JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_createTransform
  (JNIEnv *, jclass, jdouble, jdouble, jdouble, jdouble, jdouble, jdouble, jdouble, jdouble, jdouble, jdouble, jdouble, jdouble);

/*
 * Class:     be_kuleuven_mech_rsg_jni_RsgJNI
 * Method:    getTransformElement
 * Signature: (IJ)D
 */
JNIEXPORT jdouble JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_getTransformElement
  (JNIEnv *, jclass, jint, jlong);

/*
 * Class:     be_kuleuven_mech_rsg_jni_RsgJNI
 * Method:    createAttributeList
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_createAttributeList
  (JNIEnv *, jclass);

/*
 * Class:     be_kuleuven_mech_rsg_jni_RsgJNI
 * Method:    getAttributeListSize
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_getAttributeListSize
  (JNIEnv *, jclass, jlong);

/*
 * Class:     be_kuleuven_mech_rsg_jni_RsgJNI
 * Method:    addAttributeToAttributeList
 * Signature: (Ljava/lang/String;Ljava/lang/String;J)I
 */
JNIEXPORT jint JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_addAttributeToAttributeList
  (JNIEnv *, jclass, jstring, jstring, jlong);

/*
 * Class:     be_kuleuven_mech_rsg_jni_RsgJNI
 * Method:    getKeyFromAttributeList
 * Signature: (IJ)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_getKeyFromAttributeList
  (JNIEnv *, jclass, jint, jlong);

/*
 * Class:     be_kuleuven_mech_rsg_jni_RsgJNI
 * Method:    getValueFromAttributeList
 * Signature: (IJ)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_getValueFromAttributeList
  (JNIEnv *, jclass, jint, jlong);

/*
 * Class:     be_kuleuven_mech_rsg_jni_RsgJNI
 * Method:    dispose
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_dispose
  (JNIEnv *, jclass, jlong);

/*
 * Class:     be_kuleuven_mech_rsg_jni_RsgJNI
 * Method:    writeUpdateToInputPort
 * Signature: ([BI)I
 */
JNIEXPORT jint JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_writeUpdateToInputPort
  (JNIEnv *, jclass, jbyteArray, jint);

/*
 * Class:     be_kuleuven_mech_rsg_jni_RsgJNI
 * Method:    setDotFilePath
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_setDotFilePath
  (JNIEnv *, jclass, jstring);

#ifdef __cplusplus
}
#endif
#endif
