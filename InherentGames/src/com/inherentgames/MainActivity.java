package com.inherentgames;
import java.lang.reflect.Field;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.vecmath.Vector3f;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Window;

import com.bulletphysics.dynamics.RigidBody;
import com.threed.jpct.Camera;
import com.threed.jpct.Interact2D;
import com.threed.jpct.Logger;
import com.threed.jpct.SimpleVector;


public class MainActivity extends Activity {
	private static MainActivity master = null;
	
	private GLSurfaceView mGLView;
	private MyRenderer renderer = null;
	
	
	private float xpos = -1;
	private float ypos = -1;
	private float firstX;
	private float firstY;
	
	private boolean isShootMode = false;
	
	private int numFingers = 0;
	
	private int width;
	private int height;
	
	protected void onCreate(Bundle savedInstanceState) {
		Logger.log("onCreate");
		
		if (master != null){
			copy(master);
		}
		
		super.onCreate(savedInstanceState);
		
		Display display = getWindowManager().getDefaultDisplay(); 
		width = display.getWidth();  // deprecated
		height = display.getHeight();  // deprecated
		
		//Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		mGLView = new GLSurfaceView(getApplication());
		
		mGLView.setEGLConfigChooser(new GLSurfaceView.EGLConfigChooser() {
			
			@Override
			public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
				//Ensure that we get a 16bit framebuffer. Otherwise we'll fall
				//back to PixelFlinger on some device (read: Samsung I7500)
				int[] attributes = new int[] {EGL10.EGL_DEPTH_SIZE, 16, EGL10.EGL_NONE};
				EGLConfig[] configs = new EGLConfig[1];
				int[] result = new int[1];
				egl.eglChooseConfig(display, attributes, configs, 1, result);
				return configs[0];
			}
		});
		
		renderer = new MyRenderer(this);
		//mGLView.setRenderMode(RENDERMODE_WHEN_DIRTY);
		mGLView.setRenderer(renderer);
		
		setContentView(mGLView);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.menu, menu);
	    return true;
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mGLView.onPause();
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		mGLView.onResume();
	}

	@Override
	protected void onStop(){
		super.onStop();
	}
	
	private void copy(Object src){
		try{
			Logger.log("Copying data from master Activity!");
			Field[] fs = src.getClass().getDeclaredFields();
			for (Field f : fs){
				f.setAccessible(true);
				f.set(this,  f.get(src));
			}
			
		} catch (Exception e){
			throw new RuntimeException(e);
		}
	}
	
	public boolean onTouchEvent(MotionEvent me){
		if (me.getPointerCount() > 1)
			isShootMode = false;
		else
			isShootMode = true;
		
		if (me.getAction() == MotionEvent.ACTION_DOWN){
			xpos = me.getX();
			ypos = me.getY();
			firstX = xpos;
			firstY = ypos;
			return true;
		}
		
		if(me.getAction() == MotionEvent.ACTION_UP){
			xpos = -1;
			ypos = -1;
			renderer.setTouchTurn(0);
			renderer.setTouchTurnUp(0);
			if (isShootMode) {
				float xd = me.getX() - firstX;
				float yd = me.getY() - firstY;
				Log.i("XD and YD VALUES", " " + xd +" "+ yd);
				Log.i("WIDTH AND HEIGHT: ", "" + width + " " + height);
				if (yd < (-height/5)) {
					Camera cam = renderer.getCam();
					SimpleVector dir = Interact2D.reproject2D3DWS(cam, renderer.getFrameBuffer(), width/2, height/2);
					dir.scalarMul(-70);
					RigidBody body = renderer.shoot(cam.getPosition());
					if(body != null){
						Vector3f force = new Vector3f(-dir.x, dir.y, dir.z);
						body.activate(true);
						body.setLinearVelocity(force);
					}
				}
				else if(xd < -(width/10)){
					renderer.loadBubble(Bubble.FEMININE);
				}
				else if (xd > (width/10)){
					renderer.loadBubble(Bubble.MASCULINE);
				}
			}
			return true;
		}
		
		if(me.getAction() == MotionEvent.ACTION_MOVE){
			float xd = me.getX() - xpos;
			float yd = me.getY() - ypos;
			
			if(!isShootMode){
				renderer.setTouchTurn(xd / -100.0f);
				renderer.setTouchTurnUp(yd / -100.0f);
			}
			xpos = me.getX();
			ypos = me.getY();
			return true;
		}
		
		try {
			Thread.sleep(15);
		} catch (Exception e){
			//No need
		}
		return super.onTouchEvent(me);
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.lighting:
        	renderer.cycleLighting();
            return true;
        case R.id.kill:
        	renderer.deleteActiveBubble();
        	return true;
//
        }
        return super.onOptionsItemSelected(item);
    }
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent msg) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			int pid = android.os.Process.myPid();
			android.os.Process.killProcess(pid);
			return true;
		}
		return super.onKeyDown(keyCode, msg);
	}
	
	protected boolean isFullscreenOpaque() {
		return true;
	}
	
	
	
	
	
}
