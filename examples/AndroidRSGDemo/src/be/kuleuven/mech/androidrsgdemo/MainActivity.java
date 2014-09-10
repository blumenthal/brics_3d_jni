package be.kuleuven.mech.androidrsgdemo;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;

import be.kuleuven.mech.rsg.*;
import be.kuleuven.mech.rsg.jni.RsgJNI;

import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Menu;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

/* _  _ */


public class MainActivity extends Activity implements OnSeekBarChangeListener {

	Thread listenerThread = null;
	
	String logTag = "YouBotWorldModel";
	SceneObject virtualFence = null;
	SceneObject obstacle = null;
	Box fenceBox = null;
	HomogeneousMatrix44 obstaclePose = null;
	Sphere obstacleShape = null; 
	
	/* GUI elements */
	
	private SeekBar xSeekBar;
	private SeekBar ySeekBar;
	private TextView xValueText;
	private TextView yValueText;
	private TextView numberOfObjectsText;
	
	
	
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
		
		initializeWorldModel();

	}	
	
	public void initializeWorldModel() {
		
	
		Rsg.initializeWorldModel(); // always start with that one.

		WorldModelUpdatesBroadcaster outputPort = new WorldModelUpdatesBroadcaster("224.0.0.1");
		Rsg.setOutPort(outputPort);
		
		virtualFence = new SceneObject();
		obstacle = new SceneObject(); 

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


		Id fenceId = Rsg.addSceneObject(virtualFence);
		Log.i(logTag, "Added virtualFence with ID = " + fenceId.toString());  
		Id obstacleId = Rsg.addSceneObject(obstacle);
		Log.i(logTag, "Added obstaceId with ID = " + obstacleId.toString());  
				
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
		Rsg.insertTransform(obstacleId, obstaclePoseUpdate);  
		
		displayObstacleCoordinates();
		
		
		/* Move obstacle a bit */
		HomogeneousMatrix44 obstaclePoseUpdate2 = new HomogeneousMatrix44(
				1, 0, 0, // rotation  
				0, 1, 0, 
				0, 0, 1,
				3.4, 4.5, 0); // translation
		Rsg.insertTransform(obstacleId, obstaclePoseUpdate2);   
		
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
		
		listenerThread = new Thread(new WorldModelUpdatesListener());
		listenerThread.start();
		
		displayObstacleCoordinates();
		
		Log.i(logTag, "Done.");
	}


	public void printSceneObjects(ArrayList<SceneObject> sceneOjects) {
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
			}
			
			Log.i(logTag, "	------------");
		}
	}
	
	public void displayObstacleCoordinates() {
		double x = -1.0;
		double y = -1.0;
		ArrayList<Attribute> queryAttributes = new ArrayList<Attribute>();
		queryAttributes.add(new Attribute("name", "obstacle"));
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
		queryAttributes.add(new Attribute("name", "obstacle"));
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
	 * Simple UDP based communication mechanism for receiving world model update messages.
	 */
	public class WorldModelUpdatesListener implements Runnable {
		
		private String text;
	    private int server_port = 0;
	    private byte[] message = null;
	    private DatagramPacket p = null;
	    private DatagramSocket s = null;
	    private MulticastLock multicastLock = null;
		
		public WorldModelUpdatesListener() {
			/*
			 * NOTE: Multicast UDP messages cannot be received by the used Android tablet.
			 * This seems to be a vendor specific problem.	
			 */
			
		    server_port = 11411;
		    int BUFLEN = 20000; // For HDF5 messages longer than this we need some other transport mechanism.
			 					// In this demo all upatates are around 10k. Thus, this buffer is sufficient.
		    message = new byte[BUFLEN];
		    p = new DatagramPacket(message, message.length);
		    try {
				s = new DatagramSocket(server_port);
				s.setReuseAddress(true); // no more already in use (multicast)
			} catch (SocketException e) {
				e.printStackTrace();
			}
	    
			Log.i(logTag, "Starting WorldModelUpdatesListener.");
		}
		
		@Override
		public void run() {			
			while(true) {
				Log.i(logTag, "WorldModelUpdatesListener: Waiting for incomming message.");
				try {
					
					/* Receive data */
					s.receive(p);
					text = new String(message, 0, p.getLength());
					Log.d(logTag, "message: with length" +  p.getLength() + " = " + text);
					
					/* Process data */
					int processedBytes = RsgJNI.writeUpdateToInputPort(message, p.getLength());
					 
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
			s.close();
		}
	}
	
	public class WorldModelUpdatesBroadcaster /*extends AsyncTask*/ implements IOutputPort {
		public WorldModelUpdatesBroadcaster() {
			this("192.168.1.105");
		}
		
		public WorldModelUpdatesBroadcaster(String hostIP) {
			this.hostIP = hostIP;
			/* workaround for android.os.NetworkOnMainThreadException; better refactor towards AsyncTask */
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy); //Min API = 9 (was 8)
		}
		
		@Override
		public int write(byte[] dataBuffer, int dataLength) {

			try {


				//String messageStr="Hello Android!";
				int server_port = 11411;
				DatagramSocket s = new DatagramSocket();
				InetAddress local = InetAddress.getByName(hostIP);

				//int msg_length=messageStr.length();
				//byte[] message = messageStr.getBytes();		    
				//		    DatagramPacket p = new DatagramPacket(message, msg_length,local,server_port);

				DatagramPacket p = new DatagramPacket(dataBuffer, dataLength, local, server_port);

				s.send(p);
			} catch (Exception e) {
				// TODO: handle exception
				Log.w("WorldModelUpdatesBroadcaster", e);
			}
			return 0;
		}
		
		private String hostIP;

//		@Override
//		protected Object doInBackground(Object... arg0) {
//			// TODO Auto-generated method stub
//			return null;
//		}
		
	}
}

