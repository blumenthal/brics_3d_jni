package be.kuleuven.mech.rsg;

import be.kuleuven.mech.rsg.jni.RsgJNI;

public class Shape {

	protected long shapePtr = 0;

	// Note: For creation please use Box or Sphere geometries.
	
	public Shape(long shapePtr) {
		this.shapePtr = shapePtr;
		assert(shapePtr != 0);
	}	
	
	public Box getBox() {
		Box box = null;
		
		if(RsgJNI.isBox(shapePtr)) {
			box = new Box(shapePtr);
		}
		
		return box;
	}
		
	public Sphere getSphere() {
		Sphere sphere = null;
		
		if(RsgJNI.isSphere(shapePtr)) {
			sphere = new Sphere(shapePtr);
		}
		
		return sphere;
	}

	public long getShapePtr() {
		return shapePtr;
	}

	
}
