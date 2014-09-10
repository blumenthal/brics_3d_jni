package be.kuleuven.mech.rsg;

import be.kuleuven.mech.rsg.jni.RsgJNI;

public class Id {

	private long idPtr = 0;

	public Id() {
		idPtr = RsgJNI.createId();
    	assert(idPtr != 0);
	}

	public Id(long idPtr) {
		this.idPtr = idPtr;
    	assert(this.idPtr != 0);
	}
	
	public String toString() {
        return RsgJNI.getIdAsString(idPtr);
    }

	public long getIdPtr() {
		return idPtr;
	}
	
}
