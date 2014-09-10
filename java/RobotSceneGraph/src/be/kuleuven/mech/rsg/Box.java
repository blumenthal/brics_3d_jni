package be.kuleuven.mech.rsg;

import be.kuleuven.mech.rsg.jni.RsgJNI;

public class Box {

	private long boxPtr = 0;
	
	public Box(double sizeX, double sizeY, double sizeZ) {
    	boxPtr = RsgJNI.createBox(sizeX, sizeY, sizeZ);
    	assert(boxPtr != 0);  	
    }

	public Box(long boxPtr) {
		this.boxPtr = boxPtr;
		assert(boxPtr != 0);
	}	
	
	public double getSizeX() {
    	return RsgJNI.getBoxSizeX(boxPtr);
    }

	public double getSizeY() {
    	return RsgJNI.getBoxSizeY(boxPtr);
    }

	public double getSizeZ() {
    	return RsgJNI.getBoxSizeZ(boxPtr);
    }

	public long getBoxPtr() {
		return boxPtr;
	}

	
}
