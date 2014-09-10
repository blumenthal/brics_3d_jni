package be.kuleuven.mech.rsg;

import be.kuleuven.mech.rsg.jni.RsgJNI;

public class Sphere {

	private long spherePtr = 0;

	public Sphere(double radius) {
		spherePtr = RsgJNI.createSphere(radius);
    	assert(spherePtr != 0);
	}

	public Sphere(long spherePtr) {
		this.spherePtr = spherePtr;
		assert(spherePtr != 0);
	}
	
	public double getRadius() {
        return RsgJNI.getSphereRadius(spherePtr);
    }

	public long getBoxPtr() {
		return spherePtr;
	}
	
}
