package be.kuleuven.mech.rsg;

public class Attribute {

    public Attribute (String key, String value) {
    	this.key = key;
    	this.value = value;
    }
	
    public String key;

    public String value;
    
    @Override
    public String toString() {
    	return "(" + this.key + " = " + this.value + ")";
    }
}
