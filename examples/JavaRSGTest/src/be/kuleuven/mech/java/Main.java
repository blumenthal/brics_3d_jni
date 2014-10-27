package be.kuleuven.mech.java;

import java.util.ArrayList;

import be.kuleuven.mech.rsg.*;
import be.kuleuven.mech.rsg.jni.RsgJNI;

public class Main {

	public static void main(String[] args) {
		System.out.println("Test for RSG-JIN on a x86 architecture.");
		
		/* IMPORTANT: Don't use the Android logger as it is not available. 
		 * Otherwise it will throw  java.lang.NoClassDefFoundError: android/util/Log 
		 */
		Logger.setUseAndroidLogger(false);
		Rsg.initializeWorldModel(); // always start with that one.
		
		String logTag = "'JavaRSGTest";
		SceneObject virtualFence = null;
		SceneObject obstacle = null;
		Box fenceBox = null;
		HomogeneousMatrix44 obstaclePose = null;
		Sphere obstacleShape = null; 
		

		virtualFence = new SceneObject();
		obstacle = new SceneObject(); 

		fenceBox = new Box(5, 6.1, 0);//[m] 
		virtualFence.addBox(fenceBox);	
		Logger.info(logTag, "Box = " + fenceBox.getSizeX() + ", " + fenceBox.getSizeY() + ", " + fenceBox.getSizeZ());			
		ArrayList<Attribute> attributes = new ArrayList<Attribute>();
		attributes.add(new Attribute("name", "virtual_fence"));
		virtualFence.addAttributes(attributes);

		
		obstacleShape = new Sphere(0.35); //[m] 
		obstacle.addSphere(obstacleShape);	
		obstaclePose = new HomogeneousMatrix44(
				1, 0, 0, // rotation  
				0, 1, 0, 
				0, 0, 1,
				2.1, 1.5, 0); // translation
		obstacle.addTransform(obstaclePose);
		attributes.clear();
		attributes.add(new Attribute("name", "obstacle"));
		obstacle.addAttributes(attributes);
		Logger.info(logTag, "Spere = " + obstacleShape.getRadius());   


		Id fenceId = Rsg.addSceneObject(virtualFence);
		Logger.info(logTag, "Added virtualFence with ID = " + fenceId.toString());  
		Id obstacleId = Rsg.addSceneObject(obstacle);
		Logger.info(logTag, "Added obstaceId with ID = " + obstacleId.toString());  
				
		
		
		ArrayList<Attribute> emptyAttributes = new ArrayList<Attribute>();
		ArrayList<SceneObject> foundAllSceneOjects = Rsg.getSceneObjects(emptyAttributes);
		Logger.info(logTag, "Result (all) = found " + foundAllSceneOjects.size() + " Scene object(s)");

	

		ArrayList<SceneObject> foundSceneOjects = Rsg.getSceneObjects(attributes);
		Logger.info(logTag, "Result = found " + foundSceneOjects.size() + " Scene object(s)");


		/* Move obstacle a bit */
		HomogeneousMatrix44 obstaclePoseUpdate = new HomogeneousMatrix44(
				1, 0, 0, // rotation  
				0, 1, 0, 
				0, 0, 1,
				3.3, 4.4, 0); // translation
		Rsg.insertTransform(obstacleId, obstaclePoseUpdate);  
		

		
		
		/* Move obstacle a bit */
		HomogeneousMatrix44 obstaclePoseUpdate2 = new HomogeneousMatrix44(
				1, 0, 0, // rotation  
				0, 1, 0, 
				0, 0, 1,
				3.4, 4.5, 0); // translation
		Rsg.insertTransform(obstacleId, obstaclePoseUpdate2);   
		
		foundAllSceneOjects.clear();
		foundAllSceneOjects = Rsg.getSceneObjects(emptyAttributes);
		Logger.info(logTag, "Result (all;again) = found " + foundAllSceneOjects.size() + " Scene object(s)");

		
	
		RsgJNI.addGeometricNode(0, 0, 0, 0, false);

		
		Logger.info(logTag, "Done.");

	}

}
