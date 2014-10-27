package be.kuleuven.mech.rsg.jni;

import be.kuleuven.mech.rsg.IOutputPort;
import be.kuleuven.mech.rsg.Logger;
import android.util.Log;

public class RsgJNI {

	
	public static native boolean initialize();	
	public static native boolean cleanup();
	public static native boolean resend();
	
	
	/* High level World Model API methods */
	
//	C++ pendenats:	
//  void getSceneObjects(vector<rsg::Attribute> attributes, vector<SceneObject>& results);
//  void getCurrentTransform(rsg::Id id, IHomogeneousMatrix44::IHomogeneousMatrix44Ptr transform);
//  void insertTransform(rsg::Id id, IHomogeneousMatrix44::IHomogeneousMatrix44Ptr transform);
//  void addSceneObject(SceneObject newObject, rsg::Id& assignedId);
	
	public static native long[] getSceneObjects(long attributesPtr);
	public static native void insertTransform(long idPtr, long transformPtr);
	public static native long getCurrentTransform(long idPtr); // not yet supported
	public static native long addSceneObject(long sceneObjectPtr);
	
	/* Scene graph facade methods */
//    C++ pendant:	
//    Id getRootId();
//
//    /* Implemented query interfaces */
//    bool getNodes(vector<Attribute> attributes, vector<Id>& ids); //subgraph?
//    bool getNodeAttributes(Id id, vector<Attribute>& attributes);
//    bool getNodeParents(Id id, vector<Id>& parentIds);
//    bool getGroupChildren(Id id, vector<Id>& childIds);
//    bool getTransform(Id id, TimeStamp timeStamp, IHomogeneousMatrix44::IHomogeneousMatrix44Ptr& transform);
//    bool getUncertainTransform(Id id, TimeStamp timeStamp, IHomogeneousMatrix44::IHomogeneousMatrix44Ptr& transform, ITransformUncertainty::ITransformUncertaintyPtr &uncertainty);
//    bool getGeometry(Id id, Shape::ShapePtr& shape, TimeStamp& timeStamp);
//
//    bool getTransformForNode (Id id, Id idReferenceNode, TimeStamp timeStamp, IHomogeneousMatrix44::IHomogeneousMatrix44Ptr& transform);
//
//    /* Implemented update interfaces */
//    bool addNode(Id parentId, Id& assignedId, vector<Attribute> attributes, bool forcedId = false);
//    bool addGroup(Id parentId, Id& assignedId, vector<Attribute> attributes, bool forcedId = false);
//    bool addTransformNode(Id parentId, Id& assignedId, vector<Attribute> attributes, IHomogeneousMatrix44::IHomogeneousMatrix44Ptr transform, TimeStamp timeStamp, bool forcedId = false);
//    bool addUncertainTransformNode(Id parentId, Id& assignedId, vector<Attribute> attributes, IHomogeneousMatrix44::IHomogeneousMatrix44Ptr transform, ITransformUncertainty::ITransformUncertaintyPtr uncertainty, TimeStamp timeStamp, bool forcedId = false);
//    bool addGeometricNode(Id parentId, Id& assignedId, vector<Attribute> attributes, Shape::ShapePtr shape, TimeStamp timeStamp, bool forcedId = false);
//    bool setNodeAttributes(Id id, vector<Attribute> newAttributes);
//    bool setTransform(Id id, IHomogeneousMatrix44::IHomogeneousMatrix44Ptr transform, TimeStamp timeStamp);
//    bool setUncertainTransform(Id id, IHomogeneousMatrix44::IHomogeneousMatrix44Ptr transform, ITransformUncertainty::ITransformUncertaintyPtr uncertainty, TimeStamp timeStamp);
//	  bool deleteNode(Id id);
//    bool addParent(Id id, Id parentId);
//    bool removeParent(Id id, Id parentId);	
	
	/**
	 * @return Pointer to root Id object.
	 */
	public static native long getRootId();
	
	/**
	 * 
	 * @param attributesPtrs Pointer to native attributes list. @see createAttributeList
	 * @return Array of pointers to ID objects. 
	 * @note The return value (boolean) of the native method is omitted. 
	 */
	public static native long[] getNodes(long attributesPtrs);
	
	/**
	 * @param idPtr Pointer to an ID object. 
	 *         The node with this ID will be queried. 
	 * @return Pointer to an attributes list. 
	 * @note The return value (boolean) of the native method is omitted. 
	 */
	public static native long getNodeAttributes(long idPtr);
	
	/**
	 * @param idPtr idPtr Pointer to an ID object. 
	 *         The node with this ID will be queried.
	 * @return List with pointers to ID objects.
	 * @note The return value (boolean) of the native method is omitted. 
	 */
	public static native long[] getNodeParents(long idPtr);
	
	/**
	 * @param idPtr idPtr Pointer to an ID object. 
	 *         The node with this ID will be queried.
	 * @return List with pointers to ID objects.
	 * @note The return value (boolean) of the native method is omitted. 
	 */
	public static native long[] getGroupChildren(long idPtr);

	/**
	 * 
	 * @param idPtr Pointer to an ID object. 
	 *         The node with this ID will be queried.
	 * @param timeStampPtr Pointer to time stamp object as used for the query.
	 * @return Pointer to a HomogeneousMatrix44
	 * @note The return value (boolean) of the native method is omitted. 
	 */
	public static native long getTransform(long idPtr, long  timeStampPtr);
	
	/**
	 * @param idPtr Pointer to an ID object. 
	 *         The node with this ID will be queried.
	 * @return Pointer to Shape object in case of a GeometricNode, null otherwise.
	 * @note The return value (boolean) and the time stamp of the native method are omitted.
	 *        In order to obtain the time stamp for a given geometric node, use the getGeometryTimestamp
	 *        method. 
	 */
	public static native long getGeometry(long idPtr);
	
	/**
	 * 
	 * @param idPtr Pointer to an ID object. 
	 *         The node with this ID will be queried.
	 * @return Pointer to time stamp object.
	 */
	public static native long getGeometryTimeStamp(long idPtr);
	
	/**
	 * Add a new "Node" to the Robot Scene Graph.
	 * 
	 * @param parentIdPtr ID of the parent node. To add more parents later, use addParent function.
	 * @param attributesPtr A set of attributes that will be set for the node.
	 * @param forcedId If set to true the ID defined in assignedId will be taken instead of an intanally generated one. This is
	 *                     in particular useful if distributed scene graphs propagate updates and ensures IDs are not re-created.
	 *                     Default = false.
	 * @return Pointer to the internally assigned ID. Null in case of an error.
	 *          In case the forcedId flag has been set to true it will used instead of the internal ID assignment.
	 *          Use this function with care. There are are some internal checks if the manually assigned is valid
	 *          but do not rely on this in a distributed setting.
	 * @note The return value (boolean) of the native method is omitted. 
	 */
	public static native long addNode(long parentIdPtr, long attributesPtr, boolean forcedId);
	
	
	/**
	 * Add a Group node to the Robot Scene Graph.
	 * @see addNode
	 */
	public static native long addGroup(long parentIdPtr, long attributesPtr, boolean forcedId);

	/**
	 * Add a Transform node to the Robot Scene Graph.
	 * @see addNode
	 * @param transformPtr Pointer to Transform object. @see createTransform
	 * @param timeStampPtr Pointer to TimeStamp object.
	 */
	public static native long addTransformNode(long parentIdPtr, long attributesPtr, long transformPtr, long timeStampPtr, boolean forcedId);

	/**
	 * Add a Geometric node to the Robot Scene Graph.
	 * @see addNode
	 * @param shapePtr Pointer to Shape object. @see createBox or createSphere
	 * @param timeStampPtr Pointer to TimeStamp object.
	 */
	public static native long addGeometricNode(long parentIdPtr, long attributesPtr, long shapePtr, long timeStampPtr, boolean forcedId);
	
//  bool setNodeAttributes(Id id, vector<Attribute> newAttributes);
//  bool setTransform(Id id, IHomogeneousMatrix44::IHomogeneousMatrix44Ptr transform, TimeStamp timeStamp);
	public static native boolean deleteNode(long idPtr);	
//  bool addParent(Id id, Id parentId);
//  bool removeParent(Id id, Id parentId);	
	
	/* 
	 * Native methods to spawn and query C++ objects of the Robot Scene Graph. 
	 */
	
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
	
	/* TimeStamp */
//	public static native long createTimeStamp();
	public static native long createTimeStamp(double timeStamp);
	public static native double getTimeStampAsSeconds(long timeStampPtr);
	public static native long getCurrentTimestamp();
	
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
