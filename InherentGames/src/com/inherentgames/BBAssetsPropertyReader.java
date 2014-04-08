package com.inherentgames;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

public class BBAssetsPropertyReader {
       private Properties properties;

       public BBAssetsPropertyReader() {
              /**
               * Constructs a new Properties object.
               */
              properties = new Properties();
       }

       public Properties getProperties( String FileName ) {

              try {
                     /**
                      * getAssets() Return an AssetManager instance for your
                      * application's package. AssetManager Provides access to an
                      * application's raw asset files;
                      */
                     AssetManager assetManager = BB.context.getAssets();
                     /**
                      * Open an asset using ACCESS_STREAMING mode. This
                      */
                     InputStream inputStream = assetManager.open( FileName );
                     /**
                      * Loads properties from the specified InputStream,
                      */
                     properties.load( inputStream );

              } catch ( IOException e ) {
                     Log.e( "AssetsPropertyReader", e.toString() );
              }
              return properties;

       }

}
