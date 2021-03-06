package be.kuleuven.mech.androidrsgdemozmq;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import be.kuleuven.mech.rsg.*;
import be.kuleuven.mech.rsg.jni.RsgJNI;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;











//import org.jeromq.ZMQ; depends on used version of JeroMQ
import org.zeromq.ZMQ;



 

public class MainActivity extends Activity implements OnSeekBarChangeListener {

	WorldModelUpdatesBroadcaster outputPort = null;
	RobotMotionCoordinator coordinator = null;
	Thread listenerThread = null;
	
	String logTag = "YouBotWorldModel";
	SceneObject virtualFence = null;
	SceneObject obstacle = null;
	SceneObject goal = null;
	Box fenceBox = null;
	HomogeneousMatrix44 obstaclePose = null;
	HomogeneousMatrix44 goalPose = null;
	Sphere obstacleShape = null;  
	double fenceXDimension = 1.0;
	double fenceYDimension = 2.0;
	
	/* GUI elements */
	
	private SeekBar xSeekBar;
	private SeekBar ySeekBar;
	private TextView xValueText;
	private TextView yValueText;
	private TextView numberOfObjectsText;
	private EditText xValueFenceText;
	private EditText yValueFenceText;
	private ToggleButton motionToggle = null;
	private Spinner sceneObjectPicker = null;
	private ArrayAdapter<String> sceneObjectPickerAdapter;
	List<String> sceneObjectPickerList = null;
	String pickedScenObjectName  = "obstacle";
	
	boolean isFirstUpdate = true; 
	
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		xSeekBar = (SeekBar)findViewById(R.id.xSeekBar); 
		xSeekBar.setOnSeekBarChangeListener(this); 
		ySeekBar = (SeekBar)findViewById(R.id.ySeekBar); 
		ySeekBar.setOnSeekBarChangeListener(this); 
		
		xValueText = (TextView) findViewById(R.id.textViewXValue);
		xValueText.setText("no_value");
		yValueText = (TextView) findViewById(R.id.TextViewYValue);
		yValueText.setText("no_value");
		numberOfObjectsText = (TextView) findViewById(R.id.textViewNumberOfObjects);
		numberOfObjectsText.setText("no_value");
		
		xValueFenceText = (EditText) findViewById(R.id.editText1);
		xValueFenceText.addTextChangedListener(new TextWatcher() {

			public void afterTextChanged(Editable s) {
				double fenceXDimensionTmp = fenceXDimension;

				Log.e("xValueFenceText", s.toString());
				try {
					fenceXDimension = Double.valueOf(s.toString());
					updateFenceBoundaries("virtual_fence", fenceXDimension, fenceYDimension);
				} catch (Exception e) {
					fenceXDimension = fenceXDimensionTmp; // recover
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
		});
		yValueFenceText = (EditText) findViewById(R.id.editText2);
		yValueFenceText.addTextChangedListener(new TextWatcher() {

			public void afterTextChanged(Editable s) {
				double fenceYDimensionTmp = fenceXDimension;

				Log.e("yValueFenceText", s.toString());
				try { 
					fenceYDimension = Double.valueOf(s.toString());
					updateFenceBoundaries("virtual_fence", fenceXDimension, fenceYDimension);
				} catch (Exception e) {
					fenceYDimension = fenceYDimensionTmp; // recover 
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
		});

		
		sceneObjectPickerList = new ArrayList<String>();
//		list.add("Goal"); 
//		list.add("Obj1"); 
		sceneObjectPicker = (Spinner)findViewById(R.id.spinner);
		sceneObjectPickerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, sceneObjectPickerList);
		sceneObjectPickerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		sceneObjectPicker.setAdapter(sceneObjectPickerAdapter);
		sceneObjectPicker.setOnItemSelectedListener( new OnItemSelectedListener() {

		    public void onItemSelected(AdapterView<?> arg0, View v, int position, long id) {
		    	Log.i("onSpinnerClick", "onSpinnerClick");
				//switch (v.getId()) {
				//case R.id.spinner:
					if(position != AdapterView.INVALID_POSITION){
						pickedScenObjectName = sceneObjectPickerList.get(position);
						Log.i("onSpinnerClick", "Setting pickedScenObjectName to: " + pickedScenObjectName);
						displayObstacleCoordinates();
					}
				//	break;
				//default:
				//	break;
				//}
		    }

		    public void onNothingSelected(AdapterView<?> v) {

		    }
		});
		
		motionToggle = (ToggleButton) findViewById(R.id.motionRoggleButton);
		motionToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
		    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		        if (isChecked) {
		            // The toggle is enabled
		        	if (coordinator != null) {
		        		coordinator.startMotion();
		        	}
		        } else {
		            // The toggle is disabled
		        	if (coordinator != null) {
		        		coordinator.stopMotion();
		        	}
		        }
		    }
		});
		

		initializeWorldModel();
		xValueFenceText.setText("2.0");
		yValueFenceText.setText("3.0");
		coordinator = new RobotMotionCoordinator("tcp://*:22422");
		
		

	}	
	
	public void initializeWorldModel() {
		
		Rsg.initializeWorldModel(); 	 // Always start with that one.
		
		String dotFilePath = "/mnt/sdcard/rsg"; 
		Rsg.setDotFilePath(dotFilePath); // Sets path and enables storage of dot files. (optional)

//		WorldModelUpdatesBroadcaster outputPort = new WorldModelUpdatesBroadcaster("tcp://192.168.1.101:11411");
		outputPort = new WorldModelUpdatesBroadcaster("tcp://*:11411");
		Rsg.setOutPort(outputPort);
		
		
		virtualFence = new SceneObject();
		obstacle = new SceneObject();
		goal = new SceneObject();

		fenceBox = new Box(5, 6.1, 0);//[m] 
		virtualFence.addBox(fenceBox);								
		Log.i(logTag, "Box = " + fenceBox.getSizeX() + ", " + fenceBox.getSizeY() + ", " + fenceBox.getSizeZ());			
		ArrayList<Attribute> attributes = new ArrayList<Attribute>();
		attributes.add(new Attribute("name", "virtual_fence"));
		virtualFence.addAttributes(attributes);

		displayObstacleCoordinates();
		
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
		Log.i(logTag, "Spere = " + obstacleShape.getRadius());   

		goalPose = new HomogeneousMatrix44(
				1, 0, 0, // rotation  
				0, 1, 0, 
				0, 0, 1,
				0.1, 0.1, 0); // translation
		goal.addTransform(goalPose);
		//shape?
		attributes.clear();
		attributes.add(new Attribute("name", "goal"));
		goal.addAttributes(attributes);

//		if(!namedObjcetExistsAlready("virtual_fence")) {
			Id fenceId = Rsg.addSceneObject(virtualFence);
			Log.i(logTag, "Added virtualFence with ID = " + fenceId.toString()); 
//		}	
		
//		if(!namedObjcetExistsAlready("obstacle")) {
			Id obstacleId = Rsg.addSceneObject(obstacle);
			Log.i(logTag, "Added obstaceId with ID = " + obstacleId.toString());  
//		}	
		
//		if(!namedObjcetExistsAlready("goal")) {
			Id goalId = Rsg.addSceneObject(goal);
			Log.i(logTag, "Added obstaceId with ID = " + goalId.toString());  
//		}
		displayObstacleCoordinates();
		
		
		ArrayList<Attribute> emptyAttributes = new ArrayList<Attribute>();
		ArrayList<SceneObject> foundAllSceneOjects = Rsg.getSceneObjects(emptyAttributes);
		Log.i(logTag, "Result (all) = found " + foundAllSceneOjects.size() + " Scene object(s)");
		printSceneObjects(foundAllSceneOjects);
	

		ArrayList<SceneObject> foundSceneOjects = Rsg.getSceneObjects(attributes);
		Log.i(logTag, "Result = found " + foundSceneOjects.size() + " Scene object(s)");
		printSceneObjects(foundSceneOjects);

		/* Move obstacle a bit */
		HomogeneousMatrix44 obstaclePoseUpdate = new HomogeneousMatrix44(
				1, 0, 0, // rotation  
				0, 1, 0, 
				0, 0, 1,
				3.3, 4.4, 0); // translation
	//	Rsg.insertTransform(obstacleId, obstaclePoseUpdate);  
		
		displayObstacleCoordinates();
		
		
		/* Move obstacle a bit */
		HomogeneousMatrix44 obstaclePoseUpdate2 = new HomogeneousMatrix44(
				1, 0, 0, // rotation  
				0, 1, 0, 
				0, 0, 1,
				3.4, 4.5, 0); // translation
	//	Rsg.insertTransform(obstacleId, obstaclePoseUpdate2);   
		
		foundAllSceneOjects.clear();
		foundAllSceneOjects = Rsg.getSceneObjects(emptyAttributes);
		Log.i(logTag, "Result (all;again) = found " + foundAllSceneOjects.size() + " Scene object(s)");
		printSceneObjects(foundAllSceneOjects);
		
		byte[] testData = new byte[8];  
		testData[0] = (byte)0xDE;   
		testData[1] = (byte)0xAF;     
		testData[2] = (byte)0xBE;  
		testData[3] = (byte)0xAF;
		testData[4] = (byte)'Q';
		int processedBytes = RsgJNI.writeUpdateToInputPort(testData, testData.length);
		
//		listenerThread = new Thread(new WorldModelUpdatesListener());
		listenerThread = new Thread(new WorldModelUpdatesListener("tcp://192.168.1.100:11511"));  
//		listenerThread = new Thread(new WorldModelUpdatesListener("tcp://192.168.1.105:11511"));
//		listenerThread = new Thread(new WorldModelUpdatesListener("tcp://192.168.10.225:11511")); // @ robolab
		listenerThread.start();
		
		displayObstacleCoordinates(); 
		

		Rsg.resendWorldModel(); 

	
		
		Log.i(logTag, "Done.");
	}


	public void printSceneObjects(ArrayList<SceneObject> sceneOjects) {
		
		sceneObjectPickerList.clear();
//		sceneObjectPickerAdapter.notifyDataSetChanged();
		
		for (SceneObject sceneObject : sceneOjects) {

			/* Just print everything */
			Log.i(logTag, "	Scene Object has ID = " + sceneObject.id);
			Log.i(logTag, "	Scene Object has parentId = " + sceneObject.parentId);

			Log.i(logTag, "	Scene Object has position (x,y,z) = (" 
					+ sceneObject.getTransform().getX() + ", " 
					+ sceneObject.getTransform().getY() + ", " 
					+ sceneObject.getTransform().getZ() + ")");

			if(sceneObject.getBox() != null) {
				Log.i(logTag, "	Scene Object has a box shape (x,y,z) =  (" 
						+ sceneObject.getBox().getSizeX() + ", "
						+ sceneObject.getBox().getSizeY() + ", "
						+ sceneObject.getBox().getSizeZ() + ")");
			} else if (sceneObject.getSphere() != null) {
				Log.i(logTag, "	Scene Object has a shere shape (radius) =  (" 
						+ sceneObject.getSphere().getRadius() + ")");
			} else {
				Log.w(logTag, "	Scene Object has unkonwn shape.");
			}

			for (Attribute a : sceneObject.getAttributes()) {
				Log.i(logTag, "	Scene Object has a attribute: " + a.toString()); 
				if(a.key.compareTo("name") == 0) {
					Log.i(logTag, "	Found a named object: " + a.value);
					sceneObjectPickerList.add(a.value);
//					sceneObjectPickerAdapter.notifyDataSetChanged();
				}
			}
			
			Log.i(logTag, "	------------");
		}
	}
	
	public boolean namedObjcetExistsAlready(String name) {
		
		ArrayList<Attribute> queryAttributes = new ArrayList<Attribute>();
		ArrayList<Id> resultIds = new ArrayList<Id>();
		queryAttributes.clear();
		resultIds.clear();
		queryAttributes.add(new Attribute("name", name)); // e.g. name "virtual_fence"
		resultIds = Rsg.getNodes(queryAttributes);
		
		if(resultIds.size() > 0) {
			return true;
		}
		
		return false;
	}
	
	public void updateFenceBoundaries(String name, double x, double y) {
		
	
		/* update fence geometry by creation of a new geometry */
		ArrayList<Attribute> queryAttributes = new ArrayList<Attribute>();
		ArrayList<Id> resultIds = new ArrayList<Id>();
		queryAttributes.clear();
		resultIds.clear();
		queryAttributes.add(new Attribute("name", name)); // e.g. name "virtual_fence"
		resultIds = Rsg.getNodes(queryAttributes);
		
		if (resultIds.size() < 1) {
			Logger.error(logTag , "updateFenceBoundaries: object with name = " + name + " does not exist.");
			return;
		}
		
		if (resultIds.size() >1) {
			Logger.error(logTag , "updateFenceBoundaries: " + resultIds.size() + " objects found. ");  
		}
		
		/* Browse all box scene objects with that name tag */
		for (Id id : resultIds) {
			ArrayList<Id> childs = new ArrayList<Id>();
			childs = Rsg.getGroupChildren(id);

			/* We expect only one, but we will delete all discovered boxes */
			for (Id child: childs) { 
				Logger.debug(logTag, "Child ID = " + child.toString());			
				Shape geometry = Rsg.getGeometry(child);
				
				if (geometry != null) {
					Logger.debug(logTag, "	found a shape: " + child.toString());	
					if(geometry.getBox() != null) {
						Logger.debug(logTag, "	found a box, deleting it: " + child.toString());	
						//Box oldBox = geometry.getBox();
						Rsg.deleteNode(child);
					}
				}
			}
			
			/* Add new node */
			ArrayList<Attribute> attributes = new ArrayList<Attribute>();
			attributes.clear();
//			attributes.add(new Attribute("shape", "Box"));
//			attributes.add(new Attribute("name", "virtual_fence"));
			Box newBox = new Box(x,y,0);
			Id newBoxId;
			TimeStamp dummyStamp = new TimeStamp(10.0);
			Rsg.addGeometricNode(id, attributes, newBox, dummyStamp, false);
		}
	}
	public void displayObstacleCoordinates() {
		double x = -1.0;
		double y = -1.0;
		ArrayList<Attribute> queryAttributes = new ArrayList<Attribute>();
//		queryAttributes.add(new Attribute("name", "obstacle"));
		queryAttributes.add(new Attribute("name", pickedScenObjectName));		
		ArrayList<SceneObject> foundSceneOjects = Rsg.getSceneObjects(queryAttributes);
		Log.i("displayObstacleCoordinates", "Result (obsatcles) = found " + foundSceneOjects.size() + " Scene object(s)");
		
		if(foundSceneOjects.size() > 0) {
			SceneObject obstacle = foundSceneOjects.get(0);
			x = obstacle.getTransform().getX();
			y = obstacle.getTransform().getY();
			Log.i("displayObstacleCoordinates", " (x,y) = (" + x + ", " + y + ")");
		}
		
		xValueText.setText(String.format("%2.2f", x));
		yValueText.setText(String.format("%2.2f", y));
	
		queryAttributes.clear();
		ArrayList<SceneObject> allFoundSceneOjects = Rsg.getSceneObjects(queryAttributes);
		numberOfObjectsText.setText(String.format("%d", allFoundSceneOjects.size()));
	}
	
	/* Callback for changes of the world model */
	public void onWorldModelUpdate() {
		Log.i(logTag, "onWorldModelUpdate()");  

		/* Get all curretn scene objects */
		ArrayList<Attribute> emptyAttributes = new ArrayList<Attribute>();
		ArrayList<SceneObject> foundSceneOjects = Rsg.getSceneObjects(emptyAttributes);
		Log.i(logTag, "There are " + foundSceneOjects.size() + " Scene object(s).");
		printSceneObjects(foundSceneOjects);
		
		/* Display (some) values on GUI */
		//numberOfObjectsText.setText(String.format("%d", foundSceneOjects.size()));

		
	}
	
	/* App life cycle callbacks */
	
	@Override
	protected void onStop() {
		Log.i(logTag, "onStop()");
		super.onStop();
		cleanup();
	}
	
	@Override
	protected void onDestroy() {
		Log.i(logTag, "onDestroy()");
		super.onDestroy();
		cleanup();
	}
	
	@Override
	protected void onPause() {
		Log.i(logTag, "onPause()");
		super.onPause();
		//cleanup();
	}
	
	void cleanup() {
		if(listenerThread != null) {
			listenerThread.interrupt();
			listenerThread = null;
		}
		Rsg.cleanupWorldModel();
		outputPort.cleanup();
		coordinator.cleanup();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {    	
    	// change progress text label with current seekbar value
    	//ValueText.setText("The value is: "+progress + " " + seekBar.getId());
    	// change action text label to changing
    	//yValueText.setText("changing");
    	
		ArrayList<Attribute> queryAttributes = new ArrayList<Attribute>();
//		queryAttributes.add(new Attribute("name", "obstacle"));
		queryAttributes.add(new Attribute("name", pickedScenObjectName));	
		ArrayList<SceneObject> foundSceneOjects = Rsg.getSceneObjects(queryAttributes);
		Log.i("onProgressChanged", "Result (obsatcles) = found " + foundSceneOjects.size() + " Scene object(s)");
    	
		if(foundSceneOjects.size() > 0) {
			SceneObject obstacle = foundSceneOjects.get(0);
    	
	    	double x = obstacle.getTransform().getX();
	    	double y = obstacle.getTransform().getY();
	    	
	    	if(seekBar.getId() == xSeekBar.getId()) {
	    		//yValueText.setText("changing x");
	    		x = (progress-50.0) / 10.0;
	    	} else if (seekBar.getId() == ySeekBar.getId()) {
	    		//yValueText.setText("changing y");
	    		y = (progress-50.0) / 10.0;
			} else {
				return;
			}
	    	
			/* Move obstacle a bit */
			HomogeneousMatrix44 obstaclePoseUpdate = new HomogeneousMatrix44(
					1, 0, 0, // rotation  
					0, 1, 0, 
					0, 0, 1,
					x, y, 0); // translation
			Rsg.insertTransform(obstacle.id, obstaclePoseUpdate);  
	    	
			displayObstacleCoordinates();			
			onWorldModelUpdate();
			
			if (isFirstUpdate) {
				
				Rsg.resendWorldModel();
				isFirstUpdate = false;
				Log.w("onProgressChanged", "Rsg.resendWorldModel()");
			}
			
			sceneObjectPickerAdapter.notifyDataSetChanged();
    	}
    }
	
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    	seekBar.setSecondaryProgress(seekBar.getProgress());
    	//yValueText.setText("starting to track touch");   	
    }
    
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    	seekBar.setSecondaryProgress(seekBar.getProgress());
    	//yValueText.setText("ended tracking touch"); 
    		
    }
    

   
	/**
	 * ZMQ based communication mechanism for receiving world model update messages.
	 */
	public class WorldModelUpdatesListener implements Runnable {
		
//		private String text;
//	    private byte[] message = null;

		private ZMQ.Context context = null;
	    private ZMQ.Socket subscriber = null;				// mechanism
		private String zmqInputConnectionSpecification;		// policy
	    	   
		public WorldModelUpdatesListener() {
			this("tcp://localhost:11411");
		}
		
		public WorldModelUpdatesListener(String zmqInputConnectionSpecification) {
			this.zmqInputConnectionSpecification = zmqInputConnectionSpecification;
			
	        context = ZMQ.context(1);
	        subscriber = context.socket(ZMQ.SUB);
	        subscriber.connect(zmqInputConnectionSpecification);
	        
//	        subscriber->setsockopt(ZMQ_SUBSCRIBE, "1", 0);
//	        subscriber.subscribe(null); // ?
	        String filter = "";
	        subscriber.subscribe(filter.getBytes(ZMQ.CHARSET));
	        
		    int BUFLEN = 20000; // For HDF5 messages longer than this we need some other transport mechanism.
			 					// In this demo all upatates are around 10k. Thus, this buffer is sufficient.
//		    message = new byte[BUFLEN];	    
			Log.i(logTag, "Starting WorldModelUpdatesListener.");
		}
		
		@Override
		public void run() {			
			while(true) {
				Log.i(logTag, "WorldModelUpdatesListener: Waiting for incomming message.");
				try {
					
					/* Receive data */
					byte[] message = subscriber.recv(); // null Pointer
					String text = new String(message, 0, message.length);
					Log.d(logTag, "message: with length" +  message.length);// + " = " + text);
					
					/* Process data */
					int processedBytes = RsgJNI.writeUpdateToInputPort(message, message.length);
					 
					/* Inform GUI */
					onWorldModelUpdate();
					
				} catch (Exception e) {
					Log.i(logTag, "Shutting down WorldModelUpdatesListener interrupted.");
					e.printStackTrace();
					cleanup();	
					break;
				}
			}
		}

		protected void cleanup() {
			Log.i(logTag, "Shutting down WorldModelUpdatesListener.");
			if (subscriber != null) {
				subscriber.close();	
			}

		}
	}
	
	public class WorldModelUpdatesBroadcaster /*extends AsyncTask*/ implements IOutputPort { 
		
		private String zmqConnectionSpecification;
		private ZMQ.Socket publisher = null;
		
		public WorldModelUpdatesBroadcaster() {
			this("tcp://*:11411");
		}
		
		public WorldModelUpdatesBroadcaster(String zmqConnectionSpecification) {
			this.zmqConnectionSpecification = zmqConnectionSpecification;
			/* workaround for android.os.NetworkOnMainThreadException; better refactor towards AsyncTask */
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy); //Min API = 9 (was 8)
			
	        ZMQ.Context context = ZMQ.context(1); // create globally?!?
	        publisher = context.socket(ZMQ.PUB);
	        publisher.bind(this.zmqConnectionSpecification);
	        Log.i("WorldModelUpdatesBroadcaster", "Init done.");
		}
		
		@Override
		public int write(byte[] dataBuffer, int dataLength) {

			try {
				Log.i("WorldModelUpdatesBroadcaster", "Trying to publish " + dataLength + " bytes.");
				publisher.send(dataBuffer);				
			} catch (Exception e) {
				Log.w("WorldModelUpdatesBroadcaster", e);
			}
			return 0;
		}
		
		public void cleanup() {
			Log.i(logTag, "Shutting down WorldModelUpdatesBroadcaster.");
			if (publisher != null) {
				publisher.close();	
			}
		}
	}
	
	/**
	 * @brief Start and stop the robot via a dedicated channel
	 */
	public class RobotMotionCoordinator  {
		
		private String zmqConnectionSpecification;
		private ZMQ.Socket publisher = null;
		
		/* Protocol definition */
		private byte CMD_STOP = 0x00;
		private byte CMD_START = 0x01;
		
		public RobotMotionCoordinator() {
			this("tcp://*:22422");
		}
		
		public RobotMotionCoordinator(String zmqConnectionSpecification) {
			this.zmqConnectionSpecification = zmqConnectionSpecification;
			/* workaround for android.os.NetworkOnMainThreadException; better refactor towards AsyncTask */
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy); //Min API = 9 (was 8)
			
	        ZMQ.Context context = ZMQ.context(1); // create globally?!?
	        publisher = context.socket(ZMQ.PUB);
	        publisher.bind(this.zmqConnectionSpecification);
	        Log.i("RobotMotionCoordinator", "Init done.");
		}
		
	
		private void write(byte command)  {
			try {
				Log.i("RobotMotionCoordinator", "Trying to send command " + command);
				byte[] dataBuffer = new byte[1]; // exactly one byte!
				dataBuffer[0] = command;
				publisher.send(dataBuffer);				
			} catch (Exception e) {
				Log.w("RobotMotionCoordinator", e);
			}
		}
		
		public void startMotion() {
			write(CMD_START);
		}
		
		public void stopMotion() {
			write(CMD_STOP);
		}
		
		public void cleanup() {
			Log.i(logTag, "Shutting down RobotMotionCoordinator.");
			if (publisher != null) {
				publisher.close();	
			}
		}
	}
}































































