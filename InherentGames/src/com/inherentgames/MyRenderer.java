package com.inherentgames;

import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.vecmath.Vector3f;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.util.Log;
import android.util.SparseIntArray;
import android.widget.Toast;

import com.bulletphysics.collision.broadphase.AxisSweep3;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.linearmath.Clock;
import com.threed.jpct.Camera;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.Light;
import com.threed.jpct.Object3D;
import com.threed.jpct.PolygonManager;
import com.threed.jpct.RGBColor;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.Texture;
import com.threed.jpct.TextureManager;
import com.threed.jpct.util.BitmapHelper;
import com.threed.jpct.util.MemoryHelper;

class MyRenderer implements GLSurfaceView.Renderer{
	private TextureManager tm = TextureManager.getInstance();
	public static final int SHORT_TOAST = 5;
	private FrameBuffer fb = null;
	private Room world = null;
	private RGBColor back = new RGBColor(50,50,100);
	
	private float touchTurn = 0;
	private float touchTurnUp = 0;
	
	private long lastRotateTime = 0;
	
	private SimpleVector V;
	
	private Camera cam;
	
	private int lightCycle = 0;
	private Light sun = null;
	Context context;
	
	private int roomNum = 0;
	
	private ArrayList<String> bubbleWords = new ArrayList<String>();
	
	private String fireButtonState = "fireButton";
	
	private Renderer2D renderer2D;
	
	private DiscreteDynamicsWorld dynamicWorld;
	private DefaultCollisionConfiguration collisionConfiguration;
	private CollisionDispatcher dispatcher;
	private Clock clock;

	private int width = 0;
	private int height = 0;
	
	private int fuelHeight = 0;
	private int timeHeight;
	
	private String bubbleTexture = "bubbleBlue";
	private String pauseButtonState = "pauseButton";
	
	private long lastShot = System.currentTimeMillis();
	private long endTime;
	private long timeLeft;
	
	private Handler handler = new Handler();
	
	private SoundPool soundPool;
	SparseIntArray soundPoolMap;
	int soundID = 1;
	
	private boolean isLocked;
	private boolean isPaused;
	
	public MyRenderer(Context c, int w, int h) {
		context = c.getApplicationContext();
		V = new SimpleVector(0, 0, 1);
		
		width = w;
		height = h;
		
		try{
		Texture text = new Texture(context.getResources().openRawResource(R.raw.font));
		text.setFiltering(false);
		tm.addTexture("gui_font", text);
		Texture bubble = new Texture(BitmapHelper.rescale(BitmapHelper.convert(context.getResources().getDrawable(R.drawable.bubblered)), 512, 512), true);
		tm.addTexture("bubbleRed", bubble);
		bubble = new Texture(BitmapHelper.rescale(BitmapHelper.convert(context.getResources().getDrawable(R.drawable.bubbleblue)), 512, 512), true);
		tm.addTexture("bubbleBlue", bubble);
		
		Texture screenImages = new Texture(BitmapHelper.rescale(BitmapHelper.convert(context.getResources().getDrawable(R.drawable.firebutton)), 128, 128), true);
		tm.addTexture("fireButton", screenImages);
		screenImages = new Texture(BitmapHelper.rescale(BitmapHelper.convert(context.getResources().getDrawable(R.drawable.firebuttonpressed)), 128, 128), true);
		tm.addTexture("fireButtonPressed", screenImages);
		screenImages = new Texture(BitmapHelper.rescale(BitmapHelper.convert(context.getResources().getDrawable(R.drawable.pause_button)), 128, 128), true);
		tm.addTexture("pauseButton", screenImages);
		screenImages = new Texture(BitmapHelper.rescale(BitmapHelper.convert(context.getResources().getDrawable(R.drawable.pause_button_pressed)), 128, 128), true);
		tm.addTexture("pauseButtonPressed", screenImages);
		screenImages = new Texture(BitmapHelper.rescale(BitmapHelper.convert(context.getResources().getDrawable(R.drawable.word_bar)), 32, 1024), true);
		tm.addTexture("FuelBar", screenImages);
		screenImages = new Texture(BitmapHelper.rescale(BitmapHelper.convert(context.getResources().getDrawable(R.drawable.time_bar)), 32, 1024), true);
		tm.addTexture("TimeBar", screenImages);
		screenImages = new Texture(BitmapHelper.rescale(BitmapHelper.convert(context.getResources().getDrawable(R.drawable.score_bars)), 256, 1024), true);
		tm.addTexture("ScoreBars", screenImages);
		screenImages = new Texture(BitmapHelper.rescale(BitmapHelper.convert(context.getResources().getDrawable(R.drawable.info_bar)), 256, 256), true);
		tm.addTexture("InfoBar", screenImages);
		screenImages = new Texture(BitmapHelper.rescale(BitmapHelper.convert(context.getResources().getDrawable(R.drawable.fuel_bar_arrow)), 64, 64), true);
		tm.addTexture("ScoreArrow", screenImages);
		
		Texture objectNames = new Texture(BitmapHelper.rescale(BitmapHelper.convert(context.getResources().getDrawable(R.drawable.pizarra)), 256, 256), true);
		tm.addTexture("Pizarra", objectNames);
		objectNames = new Texture(BitmapHelper.rescale(BitmapHelper.convert(context.getResources().getDrawable(R.drawable.escritorio)), 256, 256), true);
		tm.addTexture("Escritorio", objectNames);
		objectNames = new Texture(BitmapHelper.rescale(BitmapHelper.convert(context.getResources().getDrawable(R.drawable.silla)), 256, 256), true);
		tm.addTexture("Silla", objectNames);
		objectNames = new Texture(BitmapHelper.rescale(BitmapHelper.convert(context.getResources().getDrawable(R.drawable.mochila)), 256, 256), true);
		tm.addTexture("Mochila", objectNames);
		objectNames = new Texture(BitmapHelper.rescale(BitmapHelper.convert(context.getResources().getDrawable(R.drawable.reloj)), 256, 256), true);
		tm.addTexture("Reloj", objectNames);
		objectNames = new Texture(BitmapHelper.rescale(BitmapHelper.convert(context.getResources().getDrawable(R.drawable.calendario)), 256, 256), true);
		tm.addTexture("Calendario", objectNames);
		objectNames = new Texture(BitmapHelper.rescale(BitmapHelper.convert(context.getResources().getDrawable(R.drawable.puerta)), 256, 256), true);
		tm.addTexture("Puerta", objectNames);
		objectNames = new Texture(BitmapHelper.rescale(BitmapHelper.convert(context.getResources().getDrawable(R.drawable.libro)), 256, 256), true);
		tm.addTexture("Libro", objectNames);
		objectNames = new Texture(BitmapHelper.rescale(BitmapHelper.convert(context.getResources().getDrawable(R.drawable.papel)), 256, 256), true);
		tm.addTexture("Papel", objectNames);
		objectNames = new Texture(BitmapHelper.rescale(BitmapHelper.convert(context.getResources().getDrawable(R.drawable.ventana)), 256, 256), true);
		tm.addTexture("Ventana", objectNames);
		
		objectNames = new Texture(BitmapHelper.rescale(BitmapHelper.convert(context.getResources().getDrawable(R.drawable.defaulttexture)), 256, 256), true);
		tm.addTexture("Default", objectNames);
		
		Texture objects = new Texture(BitmapHelper.rescale(BitmapHelper.convert(context.getResources().getDrawable(R.drawable.chalkboard)), 256, 256), true);
		tm.addTexture("Chalkboard", objects);
		objects = new Texture(BitmapHelper.rescale(BitmapHelper.convert(context.getResources().getDrawable(R.drawable.calendar)), 256, 256), true);
		tm.addTexture("Calendar", objects);
		objects = new Texture(BitmapHelper.rescale(BitmapHelper.convert(context.getResources().getDrawable(R.drawable.clock)), 256, 256), true);
		tm.addTexture("Clock", objects);
		objects = new Texture(BitmapHelper.rescale(BitmapHelper.convert(context.getResources().getDrawable(R.drawable.backpack)), 256, 256), true);
		tm.addTexture("Backpack", objects);
		
		
		//set textures
		//Walls
		Texture wallTextures = new Texture(BitmapHelper.rescale(BitmapHelper.convert(context.getResources().getDrawable(R.drawable.room0wall0)), 1024, 512), true);
		tm.addTexture("Room0Wall0", wallTextures);
		wallTextures = new Texture(BitmapHelper.rescale(BitmapHelper.convert(context.getResources().getDrawable(R.drawable.room0wall1)), 1024, 512), true);
		tm.addTexture("Room0Wall1", wallTextures);
		wallTextures = new Texture(BitmapHelper.rescale(BitmapHelper.convert(context.getResources().getDrawable(R.drawable.room0wall2)), 1024, 512), true);
		tm.addTexture("Room0Wall2", wallTextures);
		wallTextures = new Texture(BitmapHelper.rescale(BitmapHelper.convert(context.getResources().getDrawable(R.drawable.room0wall3)), 1024, 512), true);
		tm.addTexture("Room0Wall3", wallTextures);
		//Floor
		wallTextures = new Texture(BitmapHelper.rescale(BitmapHelper.convert(context.getResources().getDrawable(R.drawable.room0floor)), 1024, 1024), true);
		tm.addTexture("Room0Floor", wallTextures);	
		//Ceiling
		wallTextures = new Texture(BitmapHelper.rescale(BitmapHelper.convert(context.getResources().getDrawable(R.drawable.room0ceiling)), 1024, 1024), true);
		tm.addTexture("Room0Ceiling", wallTextures);
		
		}catch(Exception e){
			
		}
		
		
	}

	public void onSurfaceChanged(GL10 gl, int w, int h) {
		if (fb != null) {
			fb.dispose();
		}
		fb = new FrameBuffer(gl, w, h);

		renderer2D = new Renderer2D(fb);
		clock = new Clock();
		
		world = new Room(roomNum, context, tm);
		world.setAmbientLight(20, 20, 20);
		
		
		sun = new Light(world);
		sun.setPosition(world.getLightLocation(0));
		sun.setIntensity(250, 250, 250);
		
		
		cam = world.getCamera();
		cam.setPosition(new SimpleVector(0,0,0));
		cam.setOrientation(new SimpleVector(0,0,1), new SimpleVector(0,-1,0));
		MemoryHelper.compact();
		
		collisionConfiguration = new DefaultCollisionConfiguration();
		dispatcher = new CollisionDispatcher(collisionConfiguration);
		Vector3f worldAabbMin = new Vector3f(-1000,-1000,-1000);
		Vector3f worldAabbMax = new Vector3f(1000,1000,1000);
		
		AxisSweep3 overlappingPairCache = new AxisSweep3(worldAabbMin, worldAabbMax, world.getNumWordObjects() + 50);
		SequentialImpulseConstraintSolver solver = new SequentialImpulseConstraintSolver();
		
		dynamicWorld = new DiscreteDynamicsWorld(dispatcher, overlappingPairCache, solver, collisionConfiguration);
		dynamicWorld.setGravity(new Vector3f(0,-10,0));
		dynamicWorld.getDispatchInfo().allowedCcdPenetration = 0f;
	
		for(int i = 0; i < world.getNumBodies(); i++){
			dynamicWorld.addCollisionObject(world.getBody(i));
		}
	
		dynamicWorld.clearForces();

		for(int i = 5; i < world.getNumBodies(); i++){
			dynamicWorld.addRigidBody(world.getBody(i));
		}
	
		timeHeight = (int)(height*0.76);
		endTime = System.currentTimeMillis() + 100000;
		fuelHeight = 0;
		
		soundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 100);
        //soundPoolMap = new HashMap<Integer, Integer>();
        soundPoolMap = new SparseIntArray();
        soundPoolMap.put(1, soundPool.load(context, R.raw.escritorio, 1));
        soundPoolMap.put(2, soundPool.load(context, R.raw.silla, 1));
        soundPoolMap.put(3, soundPool.load(context, R.raw.pizarra, 1));
        soundPoolMap.put(4, soundPool.load(context, R.raw.mochila, 1));
        soundPoolMap.put(5, soundPool.load(context, R.raw.calendario, 1));
        soundPoolMap.put(6, soundPool.load(context, R.raw.reloj, 1));
        soundPoolMap.put(7, soundPool.load(context, R.raw.puerta, 1));
        soundPoolMap.put(8, soundPool.load(context, R.raw.libro, 1));
        soundPoolMap.put(9, soundPool.load(context, R.raw.papel, 1));
        soundPoolMap.put(10, soundPool.load(context, R.raw.ventana, 1));
        isLocked = false;
        isPaused = false;
	}
	
	public void changeLevel(){
		if (fb != null) {
			fb.dispose();
		}
		roomNum++;
		renderer2D = new Renderer2D(fb);
		clock = new Clock();
		
		world = new Room(roomNum, context, tm);
		world.setAmbientLight(20, 20, 20);
		
		
		sun = new Light(world);
		sun.setPosition(world.getLightLocation(0));
		sun.setIntensity(250, 250, 250);
		
		
		cam = world.getCamera();
		cam.setPosition(new SimpleVector(0,0,0));
		cam.setOrientation(new SimpleVector(0,0,1), new SimpleVector(0,-1,0));
		MemoryHelper.compact();
		
		collisionConfiguration = new DefaultCollisionConfiguration();
		dispatcher = new CollisionDispatcher(collisionConfiguration);
		Vector3f worldAabbMin = new Vector3f(-1000,-1000,-1000);
		Vector3f worldAabbMax = new Vector3f(1000,1000,1000);
		
		AxisSweep3 overlappingPairCache = new AxisSweep3(worldAabbMin, worldAabbMax, world.getNumWordObjects() + 50);
		SequentialImpulseConstraintSolver solver = new SequentialImpulseConstraintSolver();
		
		dynamicWorld.destroy();
		dynamicWorld = new DiscreteDynamicsWorld(dispatcher, overlappingPairCache, solver, collisionConfiguration);
		dynamicWorld.setGravity(new Vector3f(0,-10,0));
		dynamicWorld.getDispatchInfo().allowedCcdPenetration = 0f;
	
		for(int i = 0; i < world.getNumBodies(); i++){
			dynamicWorld.addCollisionObject(world.getBody(i));
		}
	
		dynamicWorld.clearForces();

		for(int i = 5; i < world.getNumBodies(); i++){
			dynamicWorld.addRigidBody(world.getBody(i));
		}
	
		timeHeight = (int)(height*0.76);
		endTime = System.currentTimeMillis() + 100000;
		fuelHeight = 0;
		
		soundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 100);
        //soundPoolMap = new HashMap<Integer, Integer>();
        soundPoolMap = new SparseIntArray();
        soundPoolMap.put(1, soundPool.load(context, R.raw.escritorio, 1));
        soundPoolMap.put(2, soundPool.load(context, R.raw.silla, 1));
        soundPoolMap.put(3, soundPool.load(context, R.raw.pizarra, 1));
        soundPoolMap.put(4, soundPool.load(context, R.raw.mochila, 1));
        soundPoolMap.put(5, soundPool.load(context, R.raw.calendario, 1));
        soundPoolMap.put(6, soundPool.load(context, R.raw.reloj, 1));
        soundPoolMap.put(7, soundPool.load(context, R.raw.puerta, 1));
        soundPoolMap.put(8, soundPool.load(context, R.raw.libro, 1));
        soundPoolMap.put(9, soundPool.load(context, R.raw.papel, 1));
        soundPoolMap.put(10, soundPool.load(context, R.raw.ventana, 1));
        isLocked = false;
        isPaused = false;
	}
	
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
	}
	
	public void cycleLighting(){
		lightCycle++;
		if(lightCycle > 4)
			lightCycle = 0;
		switch(lightCycle){
		case 0:
			sun.enable();
			sun.setIntensity(250,250,250);
			break;
		case 1:
			sun.setIntensity(180,180,180);
			break;
		case 2:
			sun.setIntensity(100,100,100);
			break;
		case 3:
			sun.setIntensity(50,50,50);
			break;
		case 4:
			sun.disable();
			break;
		}
	}

	public void onDrawFrame(GL10 gl) {
		
		if ( touchTurn != 0 || touchTurnUp != 0 ) {
			V.set(cam.getDirection());
			V.rotateY(touchTurn);
			cam.lookAt(V);
			cam.rotateCameraX(-touchTurnUp/1.5f);
			touchTurn = 0;
			touchTurnUp = 0;
		}
		
		float ms = clock.getTimeMicroseconds();
		clock.reset();
		if(!isLocked){
			isLocked = true;
			dynamicWorld.stepSimulation(ms / 1000000f);
			isLocked = false;
		}
		fb.clear(back);
		
		world.renderScene(fb);
		world.draw(fb);
		renderer2D.blitCrosshair(fb, width, height);
		//Bubble image
		renderer2D.blitImage(fb, bubbleTexture, width/2, height, 512, 512, width/3, width/3, 5);
		//Bubble text
		renderer2D.blitText(world.getBubbleArticle(), width/2-width/25, height-width/10, width/25, height/10,RGBColor.WHITE);
		//Fire Button
		renderer2D.blitImage(fb, fireButtonState, width/8, height-(width/8), 128, 128, width/8, width/8, 10);
		//Pause Button
		renderer2D.blitImage(fb, pauseButtonState, width-width/30, width/35, 128, 128, width/15, width/15, 100);
		//Info Bar
		//Has extra 1 px hang if using real size? Decremented to 255x255
		renderer2D.blitImage(fb, "InfoBar", width/10, width/10, 255, 255, width/5, width/5, 100);
		//Dynamic fuel/time bars
		if(!isPaused){
			if(endTime - System.currentTimeMillis() > 0){
				timeHeight = (int)((float)(endTime - System.currentTimeMillis())/100000f*(height*0.76));
			}
			else{
				levelLose();
			}
		}
		renderer2D.blitImageBottomUp(fb, "FuelBar", (int)(width*0.909), height/2, 32, 1024, width/38, fuelHeight, (int)(height*0.76), 100);
		renderer2D.blitImageBottomUp(fb, "TimeBar", (int)(width*0.966), height/2, 32, 1024, width/38, timeHeight, (int)(height*0.76), 100);
		//Score bars
		renderer2D.blitImage(fb, "ScoreBars", width-(width/16), height/2, 256, 1024, width/8, (int)(height*0.9), 100);
		renderer2D.blitImage(fb, "ScoreArrow", (int)(width*0.9), (int)(height*0.881)- fuelHeight, 64, 64, width/38, width/38, 100);
		
		
		fb.display();
		if(!isLocked){
			isLocked = true;
			for(Bubble bubble : Reversed.reversed(world.getBubbleObjects())) {
				if(bubble.isHolding()){
					Object3D obj = world.getObject(bubble.getHeldObjectId());
					obj.setOrigin(bubble.getTranslation().calcSub(obj.getCenter()));
					if(lastRotateTime < (System.currentTimeMillis() - 15)){
						obj.rotateY(0.1f);
						world.getObject(bubble.getObjectId()).rotateY(0.1f);
					}
				}
				else{
					// Currently, the bubble pops but the next one shot breaks the physics engine.
					if(System.currentTimeMillis() > bubble.getTimeCreated() + 5000){
						Log.i("olsontl", "I'm deleting the bubble!");
						deleteBubble(bubble);
						continue;
					}
				}
			}
			isLocked = false;
		}
		if(lastRotateTime < (System.currentTimeMillis() - 15))
			lastRotateTime = System.currentTimeMillis();
		if(!isLocked){
			isLocked = true;
			checkBubble();
			isLocked = false;
		}
	}
	
	public void setTouchTurnUp(float value){
		touchTurnUp = value;
	}
	
	public Room getWorld(){
		return world;
	}
	
	public FrameBuffer getFrameBuffer(){
		return fb;
	}
	
	public void setTouchTurn(float value){
		touchTurn = value;
	}
	
	public RigidBody shoot(SimpleVector position){
		if(!isPaused){
			if(System.currentTimeMillis() > lastShot + 500){
				RigidBody body = world.addBubble(position);
				if(body != null){
					Log.i("olsontl", "Before adding bubble to physics world");
					dynamicWorld.addRigidBody(body);
					int size = dynamicWorld.getCollisionObjectArray().size();
					body = (RigidBody) dynamicWorld.getCollisionObjectArray().get(size-1);
					body.setGravity(new Vector3f(0,0,0));
					world.getLastBubble().setBodyIndex(size-1);
					lastShot = System.currentTimeMillis();
					Log.i("olsontl", "After adding bubble to physics world: " );
					return body;
				}
			}
		}
		return null;
	}
	
	public int checkBubble(){
		//Checks bubble collision and if a collision occurs, it shrinks the object down
		//and sets it in the state to stay inside the bubble object
		try {
			for(int i = 0; i < world.getNumBubbles(); i++) {
				Bubble bubble = world.getBubble(i);
				if(bubble.isHolding() == false && bubble.getBodyIndex() != -1 && bubble != null){
					RigidBody tempBody = (RigidBody) dynamicWorld.getCollisionObjectArray().get(bubble.getBodyIndex());
					Vector3f linearVelocity = new Vector3f(0,0,0);
					linearVelocity = tempBody.getLinearVelocity(linearVelocity);
					SimpleVector motion = new SimpleVector(linearVelocity.x,-linearVelocity.y,-linearVelocity.z);
					int id = world.getObject(bubble.getObjectId()).checkForCollision(motion, 5);
					WordObject collisionObject;
					if(id != -100) Log.i("olsontl", "Checking object with id: " + id);
					if(id >= 0){
						if((collisionObject = world.getWordObject(id)) != null){
							Log.i("olsontl", "Object is a WordObject!");
							if(collisionObject.getArticle() == bubble.getArticle()){
								bubbleWords.add(collisionObject.getName(Translator.ENGLISH));
								collisionObject.scale(5.0f);
								bubble.setHeldObjectId(id);
								//Object3D worldBubbleObject = world.getObject(bubble.getObjectId());
								bubble.setTexture(collisionObject.getName(Translator.SPANISH));
								bubble.calcTextureWrap();
								bubble.build();
								soundPool.play(Translator.getIndexByWord(collisionObject.getName(Translator.SPANISH)) + 1, 3, 3, 1, 0, 1f);
								hasWonGame();
								return 0;
							}
							else{
								deleteBubble(bubble);
								return 0;
							}
						}
						else if(world.isBubbleType(id)){
							Log.i("olsontl", "Object is a bubble!");
							Bubble bubbleCollisionObject = (Bubble) world.getObject(id);
							world.removeObject(bubbleCollisionObject.getHeldObjectId());
							deleteBubble(bubbleCollisionObject);
							deleteBubble(bubble);
							return 0;
						}
					}
				}
			}
		} catch (IndexOutOfBoundsException e) {
			Log.e("MyRenderer", "Index is out of bounds: " + e.getMessage());
		}
		return 0;
	}
	
	public Camera getCam(){
		return cam;
	}
	
	public void deleteBubble(Bubble bubble){
		dynamicWorld.removeRigidBody((RigidBody)dynamicWorld.getCollisionObjectArray().get(bubble.getBodyIndex()));
		world.removeBubble(bubble);
	}
	
	public void loadBubble(int state){
		//Put 2D bubble image on screen with 2D renderer
		world.setBubbleColor(state);
		if(state == WordObject.FEMININE)
			bubbleTexture = "bubbleRed";
		else
			bubbleTexture = "bubbleBlue";
	}
	
	public void setFireButtonState(boolean isPressed){
		if(isPressed){
			fireButtonState = "fireButtonPressed";
		}
		else{
			fireButtonState = "fireButton";
		}
	}
	
	public void setPauseButtonState(){
		if(pauseButtonState == "pauseButton"){
			isPaused = true;
			timeLeft = endTime - System.currentTimeMillis();
			pauseButtonState = "pauseButtonPressed";
		}
		else{
			endTime = System.currentTimeMillis() + timeLeft;
			isPaused = false;
			pauseButtonState = "pauseButton";
		}
	}
	
	public boolean hasWonGame(){
		ArrayList<String> tempWords = world.getRoomObjectWords();
		int listLength = tempWords.size();
		float numWordsCaptured = 0;
		for(int i = 0; i < tempWords.size(); i++){
			for(int j = 0; j < bubbleWords.size(); j++){
				if(bubbleWords.get(j) == tempWords.get(i)){
					numWordsCaptured++;
					break;
				}
			}
		}
		fuelHeight = (int)((float)(numWordsCaptured/listLength)*(height*0.75));
		if(numWordsCaptured != listLength){
			return false;
		}
		levelWin();
		return true;
	}
	
	public Vector3f getDimensions(Object3D obj){
		PolygonManager polyMan = obj.getPolygonManager();
		int polygons = polyMan.getMaxPolygonID();
		Vector3f minVerts = new Vector3f(1000,1000,1000);
		Vector3f maxVerts = new Vector3f(-1000,-1000,-1000);
		for(int i = 0; i < polygons; i++){
			for(int j = 0; j < 3; j++){
				if(minVerts.x > polyMan.getTransformedVertex(i, j).x)
					minVerts.x = polyMan.getTransformedVertex(i,j).x;
				if(maxVerts.x < polyMan.getTransformedVertex(i, j).x)
					maxVerts.x = polyMan.getTransformedVertex(i, j).x;
				if(minVerts.y > polyMan.getTransformedVertex(i, j).y)
					minVerts.y = polyMan.getTransformedVertex(i,j).y;
				if(maxVerts.y < polyMan.getTransformedVertex(i, j).y)
					maxVerts.y = polyMan.getTransformedVertex(i, j).y;
				if(minVerts.z > polyMan.getTransformedVertex(i, j).z)
					minVerts.z = polyMan.getTransformedVertex(i,j).z;
				if(maxVerts.z < polyMan.getTransformedVertex(i, j).z)
					maxVerts.z = polyMan.getTransformedVertex(i, j).z;
			}
		}
		return new Vector3f(maxVerts.x - minVerts.x, maxVerts.y - minVerts.y, maxVerts.z - minVerts.z);
	}
	
	public void levelWin(){
	        handler.post(new Runnable(){
	            public void run(){
	            	Toast toast = Toast.makeText(context, R.string.win_level_title, SHORT_TOAST);
	                toast.show();
	        		Intent intent = new Intent(context, GameScreen.class);
	        	    intent.setClass(context, VideoScreen.class);
	        	    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        	    intent.putExtra(MenuScreen.EXTRA_MESSAGE, "comic1a");
	        	    context.startActivity(intent);
	        		if(roomNum == 0)
	        			roomNum ++;
	        		world.dispose();
	            }
	        });
		

	}
	
	public void levelLose(){
		handler.post(new Runnable(){
            public void run(){
            	Toast toast = Toast.makeText(context, R.string.lose_level_title, SHORT_TOAST);
                toast.show();
                Intent intent = new Intent(context, GameScreen.class);
        	    intent.setClass(context, MenuScreen.class);
        	    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        	    context.startActivity(intent);
        	    world.dispose();
            }
        });
	}
	
	public Vector3f toVector3f(SimpleVector vector){
		return new Vector3f(vector.x,vector.y,vector.z);
	}
	
	public SimpleVector toSimpleVector(Vector3f vector){
		return new SimpleVector(vector.x,vector.y,vector.z);
	}
}

