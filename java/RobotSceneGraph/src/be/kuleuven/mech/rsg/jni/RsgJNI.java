package be.kuleuven.mech.rsg.jni;

import be.kuleuven.mech.rsg.IOutputPort;
import be.kuleuven.mech.rsg.Logger;
import android.util.Log;

public class RsgJNI {

	
	public static native boolean initialize();	
	public static native boolean cleanup();
	public static native boolean resend();
	
//	C++ pendenats:	
//  void getSceneObjects(vector<rsg::Attribute> attributes, vector<SceneObject>& results);
//  void getCurrentTransform(rsg::Id id, IHomogeneousMatrix44::IHomogeneousMatrix44Ptr transform);
//  void insertTransform(rsg::Id id, IHomogeneousMatrix44::IHomogeneousMatrix44Ptr transform);
//  void addSceneObject(SceneObject newObject, rsg::Id& assignedId);
	
	public static native long[] getSceneObjects(long attributesPtr);
	public static native void insertTransform(long idPtr, long transformPtr);
	public static native long getCurrentTransform(long idPtr); // not yet supported
	public static native long addSceneObject(long sceneObjectPtr);
	
	
	/* Native methods to spawn and query C++ objects of the Robot Scene Graph . */
	
	// @return Pointer to Id of freshly created scene Object
	public static native long createSceneObject();  
	public static native long getIdFromSceneObject(long sceneObjectPtr);
	public static native long getParentIdFromSceneObject(long sceneObjectPtr);
	public static native boolean addTransformToSceneObject(long transformPtr, long sceneObjectPtr);
	public static native long getTransformFromSceneObject(long sceneObjectPtr);
	public static native boolean addShapeToSceneObject(long shapePtr, long sceneObjectPtr);
	public static native long getShapeFromSceneObject(long sceneObjectPtr);
	public static native boolean addAttributeListToSceneObject(long attributesPtr, long sceneObjectPtr);
	public static native long getAttributeListFromSceneObject(long sceneObjectPtr);

	
	
	/* Id */
	public static native long createId();
	public static native String getIdAsString(long idPtr);
	
	/* Box */
	public static native long createBox(double sizeX, double sizeY, double sizeZ);
	public static native double getBoxSizeX(long boxPtr);
	public static native double getBoxSizeY(long boxPtr);
	public static native double getBoxSizeZ(long boxPtr);
	public static native boolean isBox(long shapePtr);
	
	/* Sphere */
	public static native long createSphere(double radius);
	public static native double getSphereRadius(long boxPtr);
	public static native boolean isSphere(long shapePtr);
	
	/* Transform */
	public static native long createTransform(double r0, double r1, double r2, double r3, double r4, double r5, double r6, double r7, double r8, double t0, double t1, double t2);
	public static native double getTransformElement(int index, long transformPtr);
	
	/* AttributeList */
	public static native long createAttributeList();
	public static native int getAttributeListSize(long attributesPtr);
	public static native int addAttributeToAttributeList(String key, String value, long attributesPtr);
	public static native String getKeyFromAttributeList(int index, long attributesPtr);
	public static native String getValueFromAttributeList(int index, long attributesPtr);
	
	/* Generic feed forward of delete operator */
	public static native void dispose(long ptr);
	
	/*
	 * Update ports:
	 */
	
	/* Input port */
	public static native int writeUpdateToInputPort(byte dataBuffer[],  int dataLength); // triggers update of WM
	
	/* Output port */
	private static IOutputPort outPort = null;
	
	public static void setOutPort(IOutputPort outPort) {
		RsgJNI.outPort = outPort;
	}
	
	private static int onWriteUpdateToOutputPort(byte dataBuffer[],  int dataLength) {
		Logger.info("onWriteUpdateToOutputPort", dataLength + " bytes to be send.");
		 if(outPort != null) {
			 return outPort.write(dataBuffer, dataLength); 
		 }
		 Logger.warning("onWriteUpdateToOutputPort", "The outPort is not configured. Skipping message");
		return -1;
	}
	
}
