package com.inherentgames;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;

@SuppressLint("NewApi")
public class MapScreen extends Activity {

	Button stage1;
	ImageButton stage2;
	Button stage3;
	
	int width, height;
	int levelNum;
	
	@SuppressWarnings( "deprecation" )
	@SuppressLint( "NewApi" )
	@Override
	public void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		
		width = BB.getWidth();
		height = BB.getHeight();
		

		SharedPreferences settings = getSharedPreferences( MenuScreen.PREFERENCES, 0 );
		levelNum = settings.getInt( "nextLevel", 1 );
		switch( levelNum ) {
			case 1:
				Log.i( "MapScreen", "loading Level 1 map" );
				setContentView( R.layout.stage1 );
				break;
			case 2:
				Log.i( "MapScreen", "loading Level 2 map" );
				setContentView( R.layout.stage2 );
				break;
			case 3:
				Log.i( "MapScreen", "Loading Level 3 map" );
				setContentView( R.layout.stage3 );
				break;
		}
	}
	
	@SuppressLint("InlinedApi")
	public void onResume() {
		super.onResume();
		
		// Enable Immersive mode (hides status and nav bar)
		if ( android.os.Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT ) {
			BB.setImmersiveMode( findViewById( Window.ID_ANDROID_CONTENT ), getWindow().getDecorView() );
		}
	}
	
	public boolean onTouchEvent( MotionEvent me ) {
		float xpos = me.getX();
		float ypos = me.getY();
		if ( me.getAction() == MotionEvent.ACTION_DOWN ) {
			if ( xpos > width*.13 && xpos < width*.33 && ypos > height*0.162 && ypos < height*0.4 ) {
				getSharedPreferences( MenuScreen.PREFERENCES, 0 ).edit().putInt( "loadLevel", 1 ).commit();
                Intent i = new Intent( MapScreen.this, VideoScreen.class );
                i.putExtra( MenuScreen.EXTRA_MESSAGE, "comic1a" );
                startActivity( i );
                finish();
			}
			
			else if ( xpos > width*.4 && xpos < width*.6 && ypos > height*0.618 && ypos < height*0.88 ) {
				if ( levelNum > 1 ) {
					getSharedPreferences( MenuScreen.PREFERENCES, 0 ).edit().putInt( "loadLevel", 2 ).commit();
                    Intent i = new Intent( MapScreen.this, VideoScreen.class );
                    i.putExtra( MenuScreen.EXTRA_MESSAGE, "comic2a" );
                    startActivity( i );
                    finish();
				}
			}
			
			else if ( xpos > width*.675 && xpos < width*.88 && ypos > height*0.176 && ypos < height*0.44 ) {
				if ( levelNum > 2 ) {
					getSharedPreferences( MenuScreen.PREFERENCES, 0 ).edit().putInt( "loadLevel", 3 ).commit();
                    Intent i = new Intent( MapScreen.this, GameScreen.class );
                    startActivity( i );
                    finish();
				}
			}
			
			
		}
		return true;
	}
	
	
	@Override
	public void onBackPressed() {
	   Log.d( "MapScreen", "onBackPressed Called" );
	   Intent setIntent = new Intent( this, MenuScreen.class );
	   setIntent.setFlags( Intent.FLAG_ACTIVITY_REORDER_TO_FRONT );
	   startActivity( setIntent );
	}
	
}
