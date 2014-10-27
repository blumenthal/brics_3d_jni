package be.kuleuven.mech.rsg;

import be.kuleuven.mech.rsg.jni.RsgJNI;

public class Box extends Shape {
	
	public Box(double sizeX, double sizeY, double sizeZ) {
		super();
    	shapePtr = RsgJNI.createBox(sizeX, sizeY, sizeZ);
    	assert(shapePtr != 0);  	
    }

	public Box(long boxPtr) {
		super(boxPtr);
	}	
	
	public double getSizeX() {
    	return RsgJNI.getBoxSizeX(shapePtr);
    }

	public double getSizeY() {
    	return RsgJNI.getBoxSizeY(shapePtr);
    }

	public double getSizeZ() {
    	return RsgJNI.getBoxSizeZ(shapePtr);
    }

	public long getBoxPtr() { // we keep it for backwards compatibility
		return shapePtr;
	}

	
}
