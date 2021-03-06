#include "be_kuleuven_mech_rsg_jni_RsgJNI.h"

/* Generic includes */
#include <assert.h>
#include <iomanip> // setprecision

/* Android related includes */
#ifdef ANDROID
#include <android/log.h>
#include "AndroidLoggerListener.h"
#endif

/* BRICS_3D includes for the world model */
#include <brics_3d/core/Version.h>
#include <brics_3d/core/HomogeneousMatrix44.h>
#include <brics_3d/worldModel/WorldModel.h>
#include <brics_3d/worldModel/sceneGraph/RemoteRootNodeAutoMounter.h>
#include <brics_3d/worldModel/sceneGraph/DotGraphGenerator.h>
#include <brics_3d/worldModel/sceneGraph/DotVisualizer.h>
#include <brics_3d/worldModel/sceneGraph/HDF5UpdateSerializer.h>
#include <brics_3d/worldModel/sceneGraph/HDF5UpdateDeserializer.h>
#include <brics_3d/worldModel/sceneGraph/SceneGraphToUpdatesTraverser.h>

using namespace brics_3d;
using namespace brics_3d::rsg;
using brics_3d::Logger;

/* Macros for Android loggers */
#ifdef ANDROID
#define LOG_INFO(...) ((void)__android_log_print(ANDROID_LOG_INFO, "rsg-jni", __VA_ARGS__))
#define LOG_DEBUG(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, "rsg-jni", __VA_ARGS__))
#define LOG_WARNING(...) ((void)__android_log_print(ANDROID_LOG_WARNING, "rsg-jni", __VA_ARGS__))
#define LOG_ERROR(...) ((void)__android_log_print(ANDROID_LOG_ERROR, "rsg-jni", __VA_ARGS__))
#else
#define LOG_INFO(...) (LOG(INFO) << __VA_ARGS__))
#endif

/* Global variables (for callbacks to virtual machine)*/
static JavaVM *javaVMHandle; 											// Handle for the virtual machine
static jobject interfaceObjectHandle; 									// Handle for a class instance of the interace jni interface class
const char *interfaceClassPath = "be/kuleuven/mech/rsg/jni/RsgJNI";	// "Path" to the interface class. Needs to follow exact jni syntax

/* Global world model variables */
brics_3d::WorldModel* wm = 0;
Logger::Listener*  androidLogger;
brics_3d::rsg::DotGraphGenerator* wmPrinter = 0;
brics_3d::rsg::DotVisualizer* wmDotGraphSaver = 0;
HDF5UpdateDeserializer* wmUpdatesToHdf5deserializer = 0;
HDF5UpdateSerializer* wmUpdatesToHdf5Serializer = 0;
SceneGraphToUpdatesTraverser* wmResender = 0;
RemoteRootNodeAutoMounter* wmAutoMounter = 0;

bool saveDotFiles = false;
std::string dotFileName = ""; // path + filename

/*
 * JNI helper functions
 */
void initializeClassHelper(JNIEnv *env, const char *path, jobject *objptr) {
    jclass cls = env->FindClass(path);
    if(!cls) {
        LOG(ERROR)<< "initializeClassHelper: failed to get %s class reference" << path; //NOTE: LoggerListerner might not be attached already
        return;
    }
    jmethodID constructor = env->GetMethodID(cls, "<init>", "()V");
    if(!constructor) {
    	LOG(ERROR) << "initializeClassHelper: failed to get %s constructor" << path;
        return;
    }
    jobject obj = env->NewObject(cls, constructor);
    if(!obj) {
    	LOG(ERROR) << "initializeClassHelper: failed to create a %s object" << path;
        return;
    }
    (*objptr) = env->NewGlobalRef(obj); //make object available as global reference

}

jint JNI_OnLoad(JavaVM* vm, void* reserved) {		//Will be executed when library is loaded
	LOG(INFO) << "Loading rsg-jni shared library (build: " << __DATE__ << " " << __TIME__ << ").";

	javaVMHandle = vm; 								// Make virtual machine reference available for all threads/functions
    JNIEnv *env = 0;

    if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
        LOG(ERROR) << "Failed to get the environment using GetEnv()";
        return -1;
    }
    initializeClassHelper(env, interfaceClassPath, &interfaceObjectHandle);
	return JNI_VERSION_1_4; 						//1.4 Provides will be moslt likely sufficiant, but could be even lower(?)
}

void triggerJNIDataCallback(jbyteArray dataBuffer, jint dataLength) {
	std::string methodName = "onWriteUpdateToOutputPort";

    int jniStatus = 0;		// For return values of jni functions
    JNIEnv *env = 0;		// NOTE: Each thread has its own "environment" => cannot be a global variable

    /* Before we detach we have to check if it is danderour to do so (can cause: ERROR: detaching thread with interp frames)
     * cf. https://groups.google.com/forum/#!topic/android-ndk/2H8z5grNqjo
     */
    bool attached = false;

    switch (javaVMHandle->GetEnv((void**)&env, JNI_VERSION_1_6)) {
    case JNI_OK:
    	break;
    case JNI_EDETACHED:
    	if (javaVMHandle->AttachCurrentThread(&env, NULL)!=0)
    	{
    		LOG(ERROR) << "triggerJNIDataCallback: failed to attach, current thread";
    	}
    	attached = true;
    	return;
    case JNI_EVERSION:
    	LOG(ERROR) << "triggerJNIDataCallback: Invalid java version-";
    }

	jclass cls = env->GetObjectClass(interfaceObjectHandle);
    if(!cls) {
        LOG(ERROR) << "triggerJNIDataCallback: failed to get class reference";
        javaVMHandle->DetachCurrentThread(); //Nerver forget to detach, otherwise the callback can get stuck!
        return;
    }

	jmethodID method = env->GetStaticMethodID(cls, methodName.c_str(), "([BI)I"); // ([BI)I
    if(!method) {
        LOG(ERROR) << "triggerJNIDataCallback: failed to get method ID";
        javaVMHandle->DetachCurrentThread(); //Nerver forget to detach, otherwise the callback can get stuck!
        return;
    }

    /* finally call the function... */
    env->CallIntMethod(interfaceObjectHandle, method, dataBuffer, dataLength);

    if (attached){
    	javaVMHandle->DetachCurrentThread(); //Nerver forget to detach, otherwise the callback can get stuck!
    }

}

/**
 * Implementation of OUT data transmission.
 *
 * A serialized UDF "messages" is send via via its corresbonding JNI callback.
 * The Java application will then decide what to do next.
 */
class HSDF5JNIOutputBridge : public brics_3d::rsg::IOutputPort {
public:
	HSDF5JNIOutputBridge() : debugTag("HSDF5JNIOutputBridge") {
		LOG(DEBUG) << debugTag << " created.";
	};

	virtual ~HSDF5JNIOutputBridge(){};

	int write(const char *dataBuffer, int dataLength, int &transferredBytes) {
		LOG(DEBUG) << debugTag << " writing to port.";

		JNIEnv *env = 0;
	    javaVMHandle->GetEnv((void**)&env, JNI_VERSION_1_6);

		jbyteArray res = NULL;
		jbyteArray dataArray = env->NewByteArray(dataLength);
		if(dataArray == NULL || env->ExceptionCheck() == JNI_TRUE) {
			LOG(ERROR) << debugTag << " Can not allocate a new jbyteArray";
		} else {
			env->SetByteArrayRegion(dataArray, 0, dataLength, (const signed char*) dataBuffer);
			if(env->ExceptionCheck() == JNI_TRUE) {
				LOG(ERROR) << debugTag << " Can not fill  jbyteArray with size " << dataLength;
			} else {
				res = dataArray;
			}
		}

		if(res == 0 && dataArray != 0) {
			env->DeleteLocalRef(dataArray);
			return -1;
		}

		triggerJNIDataCallback(dataArray, dataLength);
		return 0; //TODO
	};

private:
	std::string debugTag;

};

JNIEXPORT jboolean JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_initialize
  (JNIEnv *, jclass) {

//	LOG_INFO("Initialize RSG world model.");
	wm = new WorldModel();

	/*
	 * Policy for error propagation (= call of observers):
	 * Here we don't propagete them to prevent "ping pong" updates.
	 */
	wm->scene.setCallObserversEvenIfErrorsOccurred(false);

	/* Setup logger */
	Logger::setMinLoglevel(Logger::LOGDEBUG);
#ifdef ANDROID
	androidLogger = new AndroidLoggerListener();
	Logger::setListener(androidLogger);
#endif

    /* Use auto mount policy */
	wmAutoMounter = new RemoteRootNodeAutoMounter(&wm->scene, wm->getRootNodeId()); //mount everything relative to root node
	wm->scene.attachUpdateObserver(wmAutoMounter);

	/* Setup graph printer helper tool */
	wmPrinter = new brics_3d::rsg::DotGraphGenerator();
	VisualizationConfiguration config;
	config.abbreviateIds = false; // We want to see the complete IDs for debugging
	wmPrinter->setConfig(config);

	/* See if the logger works right at the beginning. */
	Version version;
	LOG(INFO) << "A new world model instance has been created. (version: " << version.getVersionAsString() <<
			", build: " << __DATE__ << " " << __TIME__ << ")";

	/* Init ports */
#ifdef	BRICS_HDF5_ENABLE
	HSDF5JNIOutputBridge* jniOutBridge = new HSDF5JNIOutputBridge();
	brics_3d::rsg::HDF5UpdateSerializer* wmUpdatesToHdf5Serializer = new brics_3d::rsg::HDF5UpdateSerializer(jniOutBridge);
	wm->scene.attachUpdateObserver(wmUpdatesToHdf5Serializer);

	wmUpdatesToHdf5deserializer = new HDF5UpdateDeserializer(wm);

	/* Init resender */
	wmResender = new brics_3d::rsg::SceneGraphToUpdatesTraverser(wmUpdatesToHdf5Serializer);
	LOG(INFO) << "HDF5 support is enabled.";
#endif



	return true;
}

JNIEXPORT jboolean JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_cleanup
  (JNIEnv *, jclass) {

	LOG(INFO) << "Cleaning up world model.";
	if(wm) {
		delete wm;
		wm = 0;
		LOG(DEBUG) << "World model deleted.";
	}

	if(wmPrinter) {
		delete wmPrinter;
		wmPrinter = 0;
	}

	if (wmAutoMounter) {
		delete wmAutoMounter;
		wmAutoMounter = 0;
	}

#ifdef	BRICS_HDF5_ENABLE

	if(wmUpdatesToHdf5deserializer) {
		delete wmUpdatesToHdf5deserializer;
		wmUpdatesToHdf5deserializer = 0;
	}

	if(wmUpdatesToHdf5deserializer) {
		delete wmUpdatesToHdf5deserializer;
		wmUpdatesToHdf5deserializer = 0;
	}

	if (wmUpdatesToHdf5Serializer) {
		delete wmUpdatesToHdf5Serializer;
		wmUpdatesToHdf5Serializer = 0;
	}

	if (wmResender) {
		delete wmResender;
		wmResender = 0;
	}

#endif

	if (androidLogger) {
		delete androidLogger;
		androidLogger = 0;
		LOG(DEBUG) << "Logger deleted.";
		Logger::setListener(0);
		LOG(DEBUG) << "Listener unset.";
	}

	return true;
}

JNIEXPORT jboolean JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_resend
  (JNIEnv *, jclass) {

#ifdef	BRICS_HDF5_ENABLE
	LOG(INFO) << "Resending complete world model.";
	wmResender->reset();
	wm->scene.executeGraphTraverser(wmResender, wm->getRootNodeId());
	return true;
#endif

	return false;
}

JNIEXPORT jlong JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_getRootId
  (JNIEnv* env, jclass) {

	Id* rootId = new Id();
	*rootId = wm->getRootNodeId();
	return reinterpret_cast<jlong>(rootId);
}

JNIEXPORT jlong JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_addGeometricNode
  (JNIEnv* env, jclass, jlong parentIdPtr, jlong attributesPtr, jlong shapePtr, jlong timeStampPtr, jboolean forcedId) {

	Id* parentId = reinterpret_cast<Id*>(parentIdPtr);
	assert (parentId != 0);

	vector<Attribute>* attributes = reinterpret_cast<vector<Attribute>* >(attributesPtr);
	assert (attributes != 0);

	Shape* shape = reinterpret_cast<Shape*>(shapePtr);
	assert(shape != 0);
	Shape::ShapePtr shapeSmartPtr(shape);

	TimeStamp* timeStamp = reinterpret_cast<TimeStamp*>(timeStampPtr);
	assert(timeStamp != 0);

	LOG(DEBUG) << "addGeometricNode invoked. ";
	if (wm != 0) {
		Id* assignedId = new Id;
		if(wm->scene.addGeometricNode(*parentId, *assignedId, *attributes, shapeSmartPtr, *timeStamp, forcedId)) {
			jlong javaHandle = reinterpret_cast<jlong>(assignedId);
			return javaHandle;
		}
	} else {
		LOG(ERROR) << "World mode is not initialized.";
	}

	return 0;

}

JNIEXPORT void JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_insertTransform
  (JNIEnv *, jclass, jlong idPtr, jlong transformPtr) {

	Id* id = reinterpret_cast<Id*>(idPtr);
	assert (id != 0);
	HomogeneousMatrix44* transform = reinterpret_cast<HomogeneousMatrix44*>(transformPtr);
	assert(transform != 0);
	IHomogeneousMatrix44::IHomogeneousMatrix44Ptr transformSmartPtr(transform);

	LOG(DEBUG) << "insertTransform invoked. ";
	if (wm != 0) {

		LOG(DEBUG) << "Inserting new transform: " << std::cout << *transformSmartPtr;
		wm->insertTransform(*id, transformSmartPtr);

		/* Just print what the world model has to offer. */
		wmPrinter->reset();
		wm->scene.executeGraphTraverser(wmPrinter, wm->getRootNodeId());
		LOG(DEBUG) << "After insertTransform: Current state of the world model = " << std::endl << wmPrinter->getDotGraph();

	} else {
		LOG(ERROR) << "World mode is not initialized.";
	}

}

JNIEXPORT jlongArray JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_getNodes
  (JNIEnv* env, jclass, jlong attributesPtr) {

	LOG(INFO) << "getNodes invoked.";

	if (wm != 0) {

		/* Set up query */
		vector<Attribute>* attributes = reinterpret_cast<vector<Attribute>* >(attributesPtr);
		assert (attributes != 0);

		vector<Attribute> queryArributes = *attributes;
		vector<Id> resultIds;

		wm->scene.getNodes(queryArributes, resultIds);
		jlong tmpResultIdsPtrs[resultIds.size()];

		/* Browse the results */
		for(unsigned int i = 0; i < resultIds.size() ; ++i) { // just loop over all objects

			// We have to duplicate it on the heap
			Id* tmpId = new Id(resultIds[i]); // trigger copy constructor
			jlong javaHandle = reinterpret_cast<jlong>(tmpId);
			tmpResultIdsPtrs[i] = javaHandle;
			LOG(DEBUG)  << "javaHandle for id = " << javaHandle;

		}

		/* Wrap up results */
		jlongArray javaResultObjects = env->NewLongArray(resultIds.size());
		env->SetLongArrayRegion(javaResultObjects, 0, resultIds.size(), tmpResultIdsPtrs );

		return javaResultObjects;
	}
	LOG(ERROR) << "World model is not initialized.";
	return 0;

}

JNIEXPORT jlongArray JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_getGroupChildren
  (JNIEnv* env, jclass, jlong idPtr) {
	LOG(INFO) << "getGroupChildren invoked.";

	Id* id = reinterpret_cast<Id*>(idPtr);
	assert (id != 0);

	if (wm != 0) {

		vector<Id> childIds;
		childIds.clear();

		wm->scene.getGroupChildren(*id, childIds);
		jlong tmpResultIdsPtrs[childIds.size()];

		/* Browse the results */
		for(unsigned int i = 0; i < childIds.size() ; ++i) { // just loop over all objects

			// We have to duplicate it on the heap
			Id* tmpId = new Id(childIds[i]); // trigger copy constructor
			jlong javaHandle = reinterpret_cast<jlong>(tmpId);
			tmpResultIdsPtrs[i] = javaHandle;
			LOG(DEBUG)  << "javaHandle for id = " << javaHandle;

		}

		/* Wrap up results */
		jlongArray javaResultObjects = env->NewLongArray(childIds.size());
		env->SetLongArrayRegion(javaResultObjects, 0, childIds.size(), tmpResultIdsPtrs );

		return javaResultObjects;
	}
	LOG(ERROR) << "World model is not initialized.";
	return 0;
}

JNIEXPORT jlong JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_getGeometry
  (JNIEnv* env, jclass, jlong idPtr) {
	Id* id = reinterpret_cast<Id*>(idPtr);
	assert (id != 0);

	LOG(DEBUG) << "getGeometry invoked. ";
	if (wm != 0) {
		TimeStamp dummy;
		Shape::ShapePtr shape;
		if(wm->scene.getGeometry(*id, shape, dummy)) {
			jlong javaHandle = reinterpret_cast<jlong>(shape.get());
			return javaHandle;
		}
		return 0;
	}
	LOG(ERROR) << "World mode is not initialized.";
	return 0;
}

JNIEXPORT jboolean JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_deleteNode
  (JNIEnv* env, jclass, jlong idPtr) {

	Id* id = reinterpret_cast<Id*>(idPtr);
	assert (id != 0);

	LOG(DEBUG) << "deleteNode invoked. ";
	if (wm != 0) {
		return wm->scene.deleteNode(*id);
	}
	LOG(ERROR) << "World mode is not initialized.";
	return false;

}

JNIEXPORT jlong JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_getCurrentTransform
  (JNIEnv *, jclass, jlong idPtr) {

	Id* id = reinterpret_cast<Id*>(idPtr);
	assert (id != 0);

	LOG(DEBUG) << "getCurrentTransform invoked. ";
	if (wm != 0) {

		//wm->getCurrentTransform(*id, ); // FIXME not yet implemented

		LOG(ERROR) << "Not yet implemented.";
		return 0;
	}
	LOG(ERROR) << "World mode is not initialized.";
	return 0;
}

JNIEXPORT jlong JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_addSceneObject
  (JNIEnv *, jclass, jlong sceneObjectPtr) {
	LOG(INFO) << "addSceneObject invoked. " << wm;
	if (wm != 0) {

		SceneObject* sceneObject = reinterpret_cast<SceneObject*>(sceneObjectPtr);
		assert(sceneObject != 0);

		rsg::Id* assignedId = new Id(); // TODO return as
		sceneObject->attributes.push_back(Attribute("taskType","sceneObject")); // attach optional attribute
		wm->addSceneObject(*sceneObject, *assignedId);
		LOG(DEBUG) << "Added a new scene object with Id = " << *assignedId << std::endl << " at t = "
				<< std::setprecision(15) << wm->now().getSeconds() << "[s].";
		return reinterpret_cast<jlong>(assignedId);
	}
	LOG(ERROR) << "World mode is not initialized.";
	return 0;
}

JNIEXPORT jlong JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_createSceneObject
  (JNIEnv *, jclass) {

	brics_3d::SceneObject* handle = new brics_3d::SceneObject();

	Shape::ShapePtr targetShape(new Cylinder(0.5,0)); // radius and height in [m]
	IHomogeneousMatrix44::IHomogeneousMatrix44Ptr initPose(new HomogeneousMatrix44()); 	// here we use the identity matrix as it is the default constructor
	handle->shape = targetShape;
	handle->transform = initPose;
	handle->parentId =  wm->getRootNodeId(); // hook in after root node

	LOG(DEBUG) <<"Instantiation of a new SceneObject.";
	jlong javaHandle = reinterpret_cast<jlong>(handle);
	return javaHandle;
}

JNIEXPORT jlong JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_getIdFromSceneObject
  (JNIEnv *, jclass, jlong sceneObjectPtr) {

	SceneObject* sceneObject = reinterpret_cast<SceneObject*>(sceneObjectPtr);
	assert (sceneObject != 0);

	return reinterpret_cast<jlong>(&(sceneObject->id));
}


JNIEXPORT jlong JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_getParentIdFromSceneObject
  (JNIEnv *, jclass, jlong sceneObjectPtr) {

	SceneObject* sceneObject = reinterpret_cast<SceneObject*>(sceneObjectPtr);
	assert (sceneObject != 0);

	return reinterpret_cast<jlong>(&(sceneObject->parentId));
}

JNIEXPORT jboolean JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_addTransformToSceneObject
  (JNIEnv *, jclass, jlong transformPtr, jlong sceneObjectPtr) {

	SceneObject* sceneObject = reinterpret_cast<SceneObject*>(sceneObjectPtr);
	assert (sceneObject != 0);
	HomogeneousMatrix44* transform = reinterpret_cast<HomogeneousMatrix44*>(transformPtr);
	assert(transform != 0);
	IHomogeneousMatrix44::IHomogeneousMatrix44Ptr transformSmartPtr(transform);

	sceneObject->transform = transformSmartPtr;
	LOG(DEBUG) <<"Added transform to sceneObject : " << std::endl << *transform;

	return true;
}


JNIEXPORT jlong JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_getTransformFromSceneObject
  (JNIEnv *, jclass, jlong sceneObjectPtr) {

	SceneObject* sceneObject = reinterpret_cast<SceneObject*>(sceneObjectPtr);
	assert (sceneObject != 0);

	return reinterpret_cast<jlong>(sceneObject->transform.get());
}

JNIEXPORT jboolean JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_addShapeToSceneObject
  (JNIEnv *, jclass, jlong shapePtr, jlong sceneObjectPtr) {

	SceneObject* sceneObject = reinterpret_cast<SceneObject*>(sceneObjectPtr);
	assert (sceneObject != 0);
	Shape* shape = reinterpret_cast<Shape*>(shapePtr);
	assert(shape != 0);
	Shape::ShapePtr shapeSmartPtr(shape);

	sceneObject->shape = shapeSmartPtr;
	LOG(DEBUG) <<"Added shape to sceneObject.";

	return true;
}

JNIEXPORT jlong JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_getShapeFromSceneObject
  (JNIEnv *, jclass, jlong sceneObjectPtr) {

	SceneObject* sceneObject = reinterpret_cast<SceneObject*>(sceneObjectPtr);
	assert (sceneObject != 0);

	return reinterpret_cast<jlong>(sceneObject->shape.get());
}


JNIEXPORT jboolean JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_addAttributeListToSceneObject
  (JNIEnv *, jclass, jlong attributesPtr, jlong sceneObjectPtr) {

	SceneObject* sceneObject = reinterpret_cast<SceneObject*>(sceneObjectPtr);
	assert (sceneObject != 0);
	vector<Attribute>* attributes = reinterpret_cast<vector<Attribute>* >(attributesPtr);
	assert (attributes != 0);

	sceneObject->attributes = *attributes;
	return true;
}

JNIEXPORT jlong JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_getAttributeListFromSceneObject
  (JNIEnv *, jclass, jlong sceneObjectPtr) {

	SceneObject* sceneObject = reinterpret_cast<SceneObject*>(sceneObjectPtr);
	assert (sceneObject != 0);

	return reinterpret_cast<jlong>(&(sceneObject->attributes));
}

JNIEXPORT jlongArray JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_getSceneObjects
	  (JNIEnv* env, jclass obj, jlong attributesPtr) {

	LOG(INFO) << "getSceneObjects invoked.";

	if (wm != 0) {

		/* Set up query */
		vector<Attribute>* attributes = reinterpret_cast<vector<Attribute>* >(attributesPtr);
		assert (attributes != 0);

		vector<Attribute> queryArributes = *attributes;
		vector<SceneObject> resultObjects;

//		jfieldID jLongArrayId = env->GetFieldID(obj, "longArray", "[J");
//		jlongArray javaResultObjects = (jlongArray) env->GetObjectField(obj, jLongArrayId);

//		jlongArray javaResultObjects;
//		jlongArray buf = env->NewLongArray(5);

		queryArributes.push_back(Attribute("taskType","sceneObject"));
		wm->getSceneObjects(queryArributes, resultObjects);
		jlong tmpResultObjectsPtrs[resultObjects.size()];

		/* Browse the results */
		LOG(INFO)  << resultObjects.size() << " Scene objects found ( T ~= " << std::setprecision(15) << wm->now().getSeconds() << "[s])";
		for(unsigned int i = 0; i < resultObjects.size() ; ++i) { // just loop over all objects
			LOG(DEBUG) << "ID: " << resultObjects[i].id;
			LOG(DEBUG)  << "TF: " << *resultObjects[i].transform;
			for(unsigned int j = 0; j < resultObjects[i].attributes.size() ; ++j) {
				LOG(DEBUG)  << "Attributes: " << resultObjects[i].attributes[j].key << " " << resultObjects[i].attributes[j].value;
			}

			 // We have to duplicate it on the heap
			SceneObject* tmpSceneObject = new SceneObject(resultObjects[i]); // trigger copy constructor
			jlong javaHandle = reinterpret_cast<jlong>(tmpSceneObject);
//			env->SetLongArrayElement(javaResultObjects, i, javaHandle); // Setter does not exist :-(
			tmpResultObjectsPtrs[i] = javaHandle;
			LOG(DEBUG)  << "javaHandle = " << javaHandle;

		}

		/* Wrap up results */
		jlongArray javaResultObjects = env->NewLongArray(resultObjects.size());
		env->SetLongArrayRegion(javaResultObjects, 0, resultObjects.size(), tmpResultObjectsPtrs );

		return javaResultObjects;
	}
	LOG(ERROR) << "World model is not initialized.";
	return 0;

}

JNIEXPORT jlong JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_createId
  (JNIEnv *, jclass) {

	Id* id = new Id();
	return reinterpret_cast<jlong>(id);
}

JNIEXPORT jstring JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_getIdAsString
  (JNIEnv* env, jclass, jlong idPtr) {

	Id* id = reinterpret_cast<Id*>(idPtr);
	assert (id != 0);
	return (env)->NewStringUTF(id->toString().c_str());
}

JNIEXPORT jlong JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_createTimeStamp
  (JNIEnv* env, jclass, jdouble timeStamp) {

	TimeStamp* stamp = new TimeStamp(timeStamp, Units::Second);
	return reinterpret_cast<jlong>(stamp);
}

JNIEXPORT jdouble JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_getTimeStampAsSeconds
  (JNIEnv* env, jclass, jlong timeStampPtr) {

	TimeStamp* timeStamp = reinterpret_cast<TimeStamp*>(timeStampPtr);
	assert (timeStamp != 0);
	return timeStamp->getSeconds();
}

JNIEXPORT jlong JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_getCurrentTimestamp
  (JNIEnv* env, jclass) {

	TimeStamp* timeStamp = new TimeStamp();
	*timeStamp = wm->now();
	return reinterpret_cast<jlong>(timeStamp);

}

JNIEXPORT jlong JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_createBox
  (JNIEnv *, jclass, jdouble x, jdouble y, jdouble z) {

	Box* newBox = new Box(x, y, z);

	LOG(DEBUG) <<"Instantiation of a new Box.";
	jlong javaHandle = reinterpret_cast<jlong>(newBox);
	return javaHandle;
}

JNIEXPORT jdouble JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_getBoxSizeX
  (JNIEnv *, jclass, jlong boxPtr) {

	Box* box = reinterpret_cast<Box*>(boxPtr);
	assert(box != 0);
	return box->getSizeX();
}


JNIEXPORT jdouble JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_getBoxSizeY
  (JNIEnv *, jclass, jlong boxPtr) {

	Box* box = reinterpret_cast<Box*>(boxPtr);
	assert(box != 0);
	return box->getSizeY();
}

JNIEXPORT jdouble JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_getBoxSizeZ
  (JNIEnv *, jclass, jlong boxPtr) {

	Box* box = reinterpret_cast<Box*>(boxPtr);
	assert(box != 0);
	return box->getSizeZ();
}

JNIEXPORT jboolean JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_isBox
  (JNIEnv *, jclass, jlong shapePtr) {

	if(shapePtr == 0) {
		LOG(WARNING) << "isBox? ptr = " << shapePtr;
		return false;
	}

	Shape* shape = reinterpret_cast<Shape*>(shapePtr);
	Box* box = dynamic_cast<Box*>(shape);
	return (box != 0);
}

JNIEXPORT jlong JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_createSphere
  (JNIEnv *, jclass, jdouble radius) {

	Sphere* newSphere = new Sphere(radius);
	LOG(DEBUG) <<"Instantiation of a new Spehre.";
	return reinterpret_cast<jlong>(newSphere);
}


JNIEXPORT jdouble JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_getSphereRadius
  (JNIEnv *, jclass, jlong spherePtr) {

	Sphere* sphere = reinterpret_cast<Sphere*>(spherePtr);
	assert(sphere != 0);
	return sphere->getRadius();
}

JNIEXPORT jboolean JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_isSphere
  (JNIEnv *, jclass, jlong shapePtr) {

	if(shapePtr == 0) {
		LOG(WARNING) << "isSphere? ptr = " << shapePtr;
		return false;
	}

	Shape* shape = reinterpret_cast<Shape*>(shapePtr);
	Sphere* sphere = dynamic_cast<Sphere*>(shape);
	return (sphere != 0);
}

JNIEXPORT jlong JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_createTransform
  (JNIEnv *, jclass, jdouble r0, jdouble r1, jdouble r2, jdouble r3, jdouble r4, jdouble r5, jdouble r6, jdouble r7, jdouble r8, jdouble t0, jdouble t1, jdouble t2) {

	HomogeneousMatrix44* transform = new HomogeneousMatrix44(r0, r1, r2, r3, r4, r5, r6, r7, r8, t0, t1, t2);
	return reinterpret_cast<jlong>(transform);
}

JNIEXPORT jdouble JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_getTransformElement
  (JNIEnv *, jclass, jint index, jlong transformPtr) {

	HomogeneousMatrix44* transform = reinterpret_cast<HomogeneousMatrix44*>(transformPtr);
	assert(transform != 0);
	const double* matrixData = transform->getRawData();
	if (index < 0 || index >= 16) {
		LOG(ERROR) << "Index for HomogeneousMatrix44 is out of bounds: " << index;
		return -1;
	} else {
		return matrixData[index];
	}
}

JNIEXPORT jlong JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_createAttributeList
  (JNIEnv *, jclass) {

	vector<Attribute>* attributes = new vector<Attribute>();
	return reinterpret_cast<jlong>(attributes);
}


JNIEXPORT jint JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_getAttributeListSize
  (JNIEnv *, jclass, jlong attributesPtr) {

	vector<Attribute>* attributes = reinterpret_cast<vector<Attribute>* >(attributesPtr);
	assert (attributes != 0);
	return static_cast<jint>(attributes->size());
}


JNIEXPORT jint JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_addAttributeToAttributeList
  (JNIEnv* env, jclass, jstring key, jstring value, jlong attributesPtr) {

	vector<Attribute>* attributes = reinterpret_cast<vector<Attribute>* >(attributesPtr);
	assert (attributes != 0);

	jboolean isCopy;
    const char* keyAsChar =  env->GetStringUTFChars(key, &isCopy);
    string tmpKey(keyAsChar);
    const char* valueAsChar =  env->GetStringUTFChars(value, &isCopy);
    string tmpValue(valueAsChar);

	attributes->push_back(Attribute(tmpKey, tmpValue));
	LOG(DEBUG) << "Added new attribute " << attributes->back();

	env->ReleaseStringUTFChars(key, keyAsChar);
	env->ReleaseStringUTFChars(value, valueAsChar);

	return static_cast<jint>(attributes->size()-1);
}


JNIEXPORT jstring JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_getKeyFromAttributeList
  (JNIEnv* env, jclass, jint index, jlong attributesPtr) {

	vector<Attribute>* attributes = reinterpret_cast<vector<Attribute>* >(attributesPtr);
	assert (attributes != 0);

	string key;
	if (index < static_cast<jint>(attributes->size())) {
		key = (*attributes)[index].key;
	} else {
		LOG(ERROR) << "Index " << index << " is out of bounds for attributes list.";
	}

	return (env)->NewStringUTF(key.c_str());
}


JNIEXPORT jstring JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_getValueFromAttributeList
  (JNIEnv* env, jclass, jint index, jlong attributesPtr) {
	vector<Attribute>* attributes = reinterpret_cast<vector<Attribute>* >(attributesPtr);
	assert (attributes != 0);

	string value;
	if (index < static_cast<jint>(attributes->size())) {
		value = (*attributes)[index].value;
	} else {
		LOG(ERROR) << "Index " << index << " is out of bounds for attributes list.";
	}

	return (env)->NewStringUTF(value.c_str());
}



JNIEXPORT void JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_dispose
  (JNIEnv *, jclass, jlong ptr) {
	LOG(DEBUG) << "Deleting object.";
	delete (void*)ptr;
}

JNIEXPORT jint JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_writeUpdateToInputPort
  (JNIEnv* env, jclass, jbyteArray dataBuffer, jint dataLength) {

	//Note: jbyteArray comes in as _unsigned_ char...

	int returnValue = -1;
	LOG(DEBUG) << "Recieved a new update massage with data length = " << dataLength;

	jboolean isCopy;
	jbyte* a = env->GetByteArrayElements(dataBuffer, &isCopy);

	char* buffer;
	if(a != 0) { // cf. http://developer.android.com/training/articles/perf-jni.html#arrays
		buffer = new char [dataLength];
		memcpy(buffer, a, dataLength);
		env->ReleaseByteArrayElements(dataBuffer, a, JNI_ABORT);
	} else {
		env->ReleaseByteArrayElements(dataBuffer, a, JNI_ABORT);
		LOG(ERROR) << "writeUpdateToInputPort failed as data array cannot be obtained from JNI enviroment.";
		return returnValue;
	}

	int transferredBytes;
	if (wmUpdatesToHdf5deserializer != 0) {
		LOG(DEBUG) << "Performing update to world model";
		returnValue = wmUpdatesToHdf5deserializer->write(buffer, dataLength, transferredBytes);
	}
	LOG(DEBUG) << "RsgJNI_writeUpdateToInputPort: \t" << transferredBytes << " bytes transferred.";

	/* Just print what the world model has to offer. */
	wmPrinter->reset();
	wm->scene.executeGraphTraverser(wmPrinter, wm->getRootNodeId());
	LOG(DEBUG) << "sgJNI_writeUpdateToInputPort: Current state of the world model = " << std::endl << wmPrinter->getDotGraph();

	delete buffer;
	return returnValue;
}

JNIEXPORT jboolean JNICALL Java_be_kuleuven_mech_rsg_jni_RsgJNI_setDotFilePath
  (JNIEnv* env, jclass, jstring pathName) {

	jboolean isCopy;
    const char* pathNameAsChar = env->GetStringUTFChars(pathName, &isCopy);
    string tmpPathName(pathNameAsChar);
    dotFileName = tmpPathName + "/rsg_jni"; // we adde a default file name to denote that data is generated as part of this JNI.

	LOG(DEBUG) << "Folder and file name for dot files has been set to: " << dotFileName;

	if(!saveDotFiles) {
		wmDotGraphSaver = new brics_3d::rsg::DotVisualizer(&wm->scene);
		VisualizationConfiguration dotConfig;
		dotConfig.abbreviateIds = true;
		wmDotGraphSaver->setConfig(dotConfig);
		wmDotGraphSaver->setKeepHistory(true);
		LOG(DEBUG) << "DotVisualizer is enabled with file name: " << dotFileName;
		wmDotGraphSaver->setFileName(dotFileName); // Assumes the folder has been created beforehands. Jave client is in charge of that.
		wm->scene.attachUpdateObserver(wmDotGraphSaver);
		saveDotFiles = true;
	} else {
		LOG(WARNING) << "DotVisualizer is reconfigured with with file name: " << dotFileName;
		assert(wmDotGraphSaver);
		wmDotGraphSaver->setFileName(dotFileName);
	}

	env->ReleaseStringUTFChars(pathName, pathNameAsChar);
	return true;
}

/* EOF */
