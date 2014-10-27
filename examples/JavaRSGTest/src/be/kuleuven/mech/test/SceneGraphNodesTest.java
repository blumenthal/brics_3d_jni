package be.kuleuven.mech.test;

import static org.junit.Assert.*;

import java.sql.Time;
import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import be.kuleuven.mech.rsg.*;
import be.kuleuven.mech.rsg.jni.*; 


public class SceneGraphNodesTest {

	@Before
	public void setUp() throws Exception {
		Logger.setUseAndroidLogger(false);
		Rsg.initializeWorldModel();
	}

	@After
	public void tearDown() throws Exception {
		Rsg.cleanupWorldModel();
	}

	@Test
	public void sceneObjetsTest() {
		if (true) {
			return;
		}

		String logTag = "sceneObjetsTest";
		SceneObject virtualFence = null;
		SceneObject obstacle = null;
		Box fenceBox = null;
		HomogeneousMatrix44 obstaclePose = null;
		Sphere obstacleShape = null; 
		

		virtualFence = new SceneObject();
		obstacle = new SceneObject(); 

		fenceBox = new Box(5, 6.1, 0);//[m] 
		assertEquals(5, fenceBox.getSizeX(), 0.001);
		assertEquals(6.1, fenceBox.getSizeY(), 0.001);
		assertEquals(0, fenceBox.getSizeZ(), 0.001);
		virtualFence.addBox(fenceBox);	
		Logger.info(logTag, "Box = " + fenceBox.getSizeX() + ", " + fenceBox.getSizeY() + ", " + fenceBox.getSizeZ());			
		ArrayList<Attribute> attributes = new ArrayList<Attribute>();
		attributes.add(new Attribute("name", "virtual_fence"));
		virtualFence.addAttributes(attributes);

		
		obstacleShape = new Sphere(0.35); //[m]
		assertEquals(0.35, obstacleShape.getRadius(), 0.001);
		obstacle.addSphere(obstacleShape);	
		obstaclePose = new HomogeneousMatrix44(
				1, 0, 0, // rotation  
				0, 1, 0, 
				0, 0, 1,
				2.1, 1.5, 0); // translation
		assertEquals(2.1, obstaclePose.getX(), 0.001);
		assertEquals(1.5, obstaclePose.getY(), 0.001);
		assertEquals(0, obstaclePose.getZ(), 0.001);
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
		assertEquals(2,  foundAllSceneOjects.size());
	

		ArrayList<SceneObject> foundSceneOjects = Rsg.getSceneObjects(attributes);
		Logger.info(logTag, "Result = found " + foundSceneOjects.size() + " Scene object(s)");
		assertEquals(1,  foundSceneOjects.size());

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
		assertEquals(2,  foundAllSceneOjects.size());
		
	}
	
	@Test
	public void testReplaceBox() {
		
		String logTag = "testReplaceBox";
		
		HomogeneousMatrix44 pose = null;
		Box box = null;
		TimeStamp stamp = null;
		
		pose = new HomogeneousMatrix44(
				1, 0, 0,	// rotation  
				0, 1, 0, 
				0, 0, 1,
				2, 3, 4.5); // translation
		
		stamp = new TimeStamp(10.0); //NOTE: do not pass long integers here!	
		assertEquals(10.0, stamp.getSeconds(), 0.001);
		
		Id rootId = Rsg.getRootId();
		Logger.info(logTag, "rootId =  " + rootId.toString());
		
		/* Add a "fence" scene object  */
		SceneObject virtualFence = new SceneObject();
		Box fenceBox = new Box(5, 6.1, 0);//[m] 
		virtualFence.addBox(fenceBox);	
		Logger.info(logTag, "Box = " + fenceBox.getSizeX() + ", " + fenceBox.getSizeY() + ", " + fenceBox.getSizeZ());			
		ArrayList<Attribute> attributes = new ArrayList<Attribute>();
		attributes.add(new Attribute("name", "virtual_fence"));
		virtualFence.addAttributes(attributes);
		Rsg.addSceneObject(virtualFence);
		
		/* update fence geometry by creation of a new geometry */
		ArrayList<Attribute> queryAttributes = new ArrayList<Attribute>();
		ArrayList<Id> resultIds = new ArrayList<Id>();
		queryAttributes.clear();
		resultIds.clear();
		queryAttributes.add(new Attribute("name", "virtual_fence"));
		resultIds = Rsg.getNodes(queryAttributes);
		assertEquals(1, resultIds.size());
		
		/* Browse all box like parents */
		for (Id id : resultIds) {
			ArrayList<Id> childs = new ArrayList<Id>();
			childs = Rsg.getGroupChildren(id);
			assertEquals(1, childs.size()); // there should be only one: The box
			for (Id child: childs) {
				Logger.info(logTag, "Child ID = " + child.toString());			
				Shape geometry = Rsg.getGeometry(child);
				
				if (geometry != null) {
					Logger.info(logTag, "	found a shape: " + child.toString());	
					assertTrue(geometry.getBox() != null);
					assertTrue(geometry.getSphere() == null);
					Box oldBox = geometry.getBox();
					assertEquals(5, oldBox.getSizeX(), 0.001);
					assertEquals(6.1, oldBox.getSizeY(), 0.001);
					assertEquals(0, oldBox.getSizeZ(), 0.001);
				}
			}
		}
		
		
//		wmHandle->scene.getNodes(queryAttributes, resultIds);
//		for(vector<Id>::iterator it = resultIds.begin(); it!=resultIds.end(); ++it) { // delete all node  with "name", "virtual_fence"
//			wmHandle->scene.deleteNode(*it);
//		}
		attributes.clear();
		attributes.add(new Attribute("shape", "Box"));
		attributes.add(new Attribute("name", "virtual_fence"));
		Box newBox = new Box(1.5,2.5,0);
		Id newBoxId;
		//wmHandle->scene.addGeometricNode(boxTfId, newBoxId, attributes, newBox, wmHandle->now());

		
	}

}
