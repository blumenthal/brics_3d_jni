package be.kuleuven.mech.rsg;

import java.util.ArrayList;

import android.util.Log;
import be.kuleuven.mech.rsg.jni.RsgJNI;

public class Rsg {

//  void getSceneObjects(vector<rsg::Attribute> attributes, vector<SceneObject>& results);
//
//  void getCurrentTransform(rsg::Id id, IHomogeneousMatrix44::IHomogeneousMatrix44Ptr transform);
//
//  void insertTransform(rsg::Id id, IHomogeneousMatrix44::IHomogeneousMatrix44Ptr transform);
//
//  void addSceneObject(SceneObject newObject, rsg::Id& assignedId);
	
	/**
	 * @brief Initialize an empty world model.
	 * This _has_ to be done once before working with the world model.
	 * @return True on success.
	 */
	public static boolean initializeWorldModel() {
		//Log.d("Rsg-API", "Loading RSG C++ library.");
		Logger.debug("Rsg-API", "Loading RSG C++ library.");
		System.loadLibrary("brics_3d-jni"); // RSG is a part of brics_3d
		return RsgJNI.initialize();
	}
	

	/**
	 * @brief Clean up all stored data of the world model. 
	 * Invoke initializeWorldModel() to continue working with the world model.
	 * @return  True on success.
	 */
	public static boolean cleanupWorldModel() {
		return RsgJNI.cleanup();
	}
	
	/**
	 * @brief Resend the complete world model to all the listeners. 
	 * @return  True on success.
	 */
	public static boolean resendWorldModel() {
		return RsgJNI.resend();
	}
	
	/**
	 * @brief Retrieve "SceneObejcts" from the world model.
	 * @param attributes [IN] Query attributes.
	 * @return Object reference to resulting scene objects.
	 */
	public static ArrayList<SceneObject> getSceneObjects(ArrayList<Attribute> attributes) {
		ArrayList<SceneObject> resultObjects = new ArrayList<SceneObject>();
		
		/* Prepare attributes as native list */
		long attributesPtr = RsgJNI.createAttributeList();
		assert (attributesPtr != 0);
		
		for(Attribute a: attributes) {
			Logger.debug("getSceneObjects", "Adding attribute to query : " + a.toString());
			RsgJNI.addAttributeToAttributeList(a.key, a.value, attributesPtr);
		}
		
		/* Perform query */
		long[] results = RsgJNI.getSceneObjects(attributesPtr); 

		/* Wrap up results */
		for (long sceneObjectPtr : results) {
			resultObjects.add(new SceneObject(sceneObjectPtr));
		}
		
		return resultObjects;
	}
	
	public static Id addSceneObject(SceneObject newObject) {
		long assigendIdPtr = RsgJNI.addSceneObject(newObject.getSceneObjectPtr());
		Id assignedId = new Id(assigendIdPtr);
		return assignedId;
	}
	
	public static void insertTransform(Id id, HomogeneousMatrix44 transform) {
		RsgJNI.insertTransform(id.getIdPtr(), transform.getHomogeneousMatrix44Ptr());
	}
	
	public static void setOutPort(IOutputPort outPort) {
		RsgJNI.setOutPort(outPort);
	}
}
