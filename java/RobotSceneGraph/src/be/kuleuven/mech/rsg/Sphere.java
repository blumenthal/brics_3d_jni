package be.kuleuven.mech.rsg;

import be.kuleuven.mech.rsg.jni.RsgJNI;

public class Sphere extends Shape {

	public Sphere(double radius) {
		super();
		shapePtr = RsgJNI.createSphere(radius);
    	assert(shapePtr != 0);
	}

	public Sphere(long spherePtr) {
		this.shapePtr = spherePtr;
		assert(shapePtr != 0);
	}
	
	public double getRadius() {
        return RsgJNI.getSphereRadius(shapePtr);
    }

	public long getBoxPtr() { // we keep it for backwards compatibility
		return shapePtr;
	}
	
}
