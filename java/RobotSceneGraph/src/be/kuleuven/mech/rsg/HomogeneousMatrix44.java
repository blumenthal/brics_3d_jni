package be.kuleuven.mech.rsg;

import be.kuleuven.mech.rsg.jni.RsgJNI;

public class HomogeneousMatrix44 {

	/*
	 * column-row layout:
	 * 0 4 8  12
	 * 1 5 9  13
	 * 2 6 10 14
	 * 3 7 11 15
	 */
	
	/// Amount of elements in 4x4 matrix
	private static final int matrixElements = 16; // 4x4

	private static final int x = 12;
	private static final int y = 13;
	private static final int z = 14;
	
	/// Array that holds data in column-row (column-major) order
	//private double[] matrixData = new double[matrixElements];
		
	private long homogeneousMatrix44Ptr = 0;
	
	public HomogeneousMatrix44(double r0, double r1, double r2, double r3, double r4, double r5 , double r6, double r7, double r8, double t0, double t1, double t2) {
		homogeneousMatrix44Ptr = RsgJNI.createTransform(r0, r1, r2, r3, r4, r5, r6, r7, r8, t0, t1, t2);
		assert (homogeneousMatrix44Ptr != 0);
	}
	
	public HomogeneousMatrix44(long homogeneousMatrix44Ptr) {
		this.homogeneousMatrix44Ptr = homogeneousMatrix44Ptr;
		assert (this.homogeneousMatrix44Ptr != 0);
	}
	
	public long getHomogeneousMatrix44Ptr() {
		return homogeneousMatrix44Ptr;
	}

	
	public double getMatrixElement(int index) {
		return RsgJNI.getTransformElement(index, homogeneousMatrix44Ptr);
	}

	/* Some convenience methods */
	public double getX () {
		return getMatrixElement(x);
	}
	
	public double getY () {
		return getMatrixElement(y);
	}
	
	public double getZ () {
		return getMatrixElement(z);
	}
}
