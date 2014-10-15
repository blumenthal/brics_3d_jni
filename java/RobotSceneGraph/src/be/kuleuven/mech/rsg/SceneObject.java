package be.kuleuven.mech.rsg;

import java.util.ArrayList;

import android.util.Log;
import be.kuleuven.mech.rsg.jni.RsgJNI;

public class SceneObject {

//    rsg::Id id;
//
//    rsg::Id parentId;
//
//    IHomogeneousMatrix44::IHomogeneousMatrix44Ptr transform;
//
//    rsg::Shape::ShapePtr shape;
//
//    vector<rsg::Attribute> attributes;
	
	private long sceneObjectPtr = 0;
	private String tag = "SceneObject";
	
	public Id id;
	public Id parentId;
	
	public long getSceneObjectPtr() {
		return sceneObjectPtr;
	}

	public SceneObject() {
		sceneObjectPtr = RsgJNI.createSceneObject(); //spwanSceneObject
		assert(sceneObjectPtr != 0);
	}
	
	public SceneObject(long sceneObjectPtr) {
		this.sceneObjectPtr = sceneObjectPtr;
		assert(sceneObjectPtr != 0);
		id = new Id(RsgJNI.getIdFromSceneObject(sceneObjectPtr));
		parentId = new Id(RsgJNI.getParentIdFromSceneObject(sceneObjectPtr));
	}
	
	private void addShape(long shape) {
		RsgJNI.addShapeToSceneObject(shape, sceneObjectPtr);
	}
	
	public void addTransform (HomogeneousMatrix44 transform) {
		RsgJNI.addTransformToSceneObject(transform.getHomogeneousMatrix44Ptr(), sceneObjectPtr);
	}
	
	public HomogeneousMatrix44 getTransform() {
		long transformPtr = RsgJNI.getTransformFromSceneObject(sceneObjectPtr);
		assert (transformPtr != 0);
		
		HomogeneousMatrix44 transform = new HomogeneousMatrix44(transformPtr);
		return transform;
	}
	
	/* Convenience (type safe) functions */
	public void addBox(Box box) {
		RsgJNI.addShapeToSceneObject(box.getBoxPtr(), sceneObjectPtr);
	}
	
	public Box getBox() {
		Box box = null;
		long boxPtr = RsgJNI.getShapeFromSceneObject(sceneObjectPtr);
		assert (boxPtr != 0);
		
		if(RsgJNI.isBox(boxPtr)) {
			box = new Box(boxPtr);
		}
		
		return box;
	}
	
	public void addSphere(Sphere sphere) {
		RsgJNI.addShapeToSceneObject(sphere.getBoxPtr(), sceneObjectPtr);
	}
	
	public Sphere getSphere() {
		Sphere sphere = null;
		long spherePtr = RsgJNI.getShapeFromSceneObject(sceneObjectPtr);
		assert (spherePtr != 0);
		
		if(RsgJNI.isSphere(spherePtr)) {
			sphere = new Sphere(spherePtr);
		}
		
		return sphere;
	}
	
	public void addAttributes(ArrayList<Attribute> attributes) {
		long attributesPtr = RsgJNI.createAttributeList();
		assert (attributesPtr != 0);
		
		for(Attribute a: attributes) {
			Logger.debug(tag, "Adding: " + a.toString());
			RsgJNI.addAttributeToAttributeList(a.key, a.value, attributesPtr);
		}
		
		RsgJNI.addAttributeListToSceneObject(attributesPtr, sceneObjectPtr);
	}
	
	public ArrayList<Attribute> getAttributes() {
		ArrayList<Attribute> attributes = new ArrayList<Attribute>();
		long attributesPtr = RsgJNI.getAttributeListFromSceneObject(sceneObjectPtr);
		assert(attributesPtr != 0);
		
		int listSize = RsgJNI.getAttributeListSize(attributesPtr);
		for (int i = 0; i < listSize; i++) {
			String tmpKey = RsgJNI.getKeyFromAttributeList(i, attributesPtr);
			String tmpValue = RsgJNI.getValueFromAttributeList(i, attributesPtr);
			Attribute tmpAttribute = new Attribute(tmpKey, tmpValue);
			Logger.debug(tag, "Retrieved: " + tmpAttribute.toString());
			attributes.add(tmpAttribute);			
		}
		
		return attributes;
	}
}
