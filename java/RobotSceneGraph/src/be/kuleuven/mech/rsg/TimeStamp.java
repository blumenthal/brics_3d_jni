package be.kuleuven.mech.rsg;

import be.kuleuven.mech.rsg.jni.RsgJNI;

public class TimeStamp {

	private long timeStampPtr = 0;

//	public TimeStamp() {
//		timeStampPtr = RsgJNI.createTimeStamp();
//    	assert(timeStampPtr != 0);
//	}
	
	public TimeStamp(double timeStamp) {
		timeStampPtr = RsgJNI.createTimeStamp(timeStamp);
    	assert(timeStampPtr != 0);
	}

	public TimeStamp(long idPtr) {
		this.timeStampPtr = idPtr;
    	assert(this.timeStampPtr != 0);
	}
	
	public double getSeconds() {
        return RsgJNI.getTimeStampAsSeconds(timeStampPtr);
    }

	public long getTimeStampPtr() {
		return timeStampPtr;
	}
	
}
