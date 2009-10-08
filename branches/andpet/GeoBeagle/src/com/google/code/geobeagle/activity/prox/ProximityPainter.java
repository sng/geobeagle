package com.google.code.geobeagle.activity.prox;

import com.google.code.geobeagle.Geocache;
import com.google.code.geobeagle.activity.main.GeoUtils;
import com.google.code.geobeagle.database.CachesProvider;
import com.google.code.geobeagle.database.CachesProviderCount;
import com.google.code.geobeagle.database.ICachesProviderCenter;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.DiscretePathEffect;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.Paint.Style;
import android.graphics.Shader.TileMode;
import android.os.SystemClock;
import android.util.Log;

import java.util.ArrayList;

public class ProximityPainter {
    //private int mUserX;
    /** Where on the display to show the user's location */
    private int mUserY;
    
    /* X center of display */
    private int mCenterX;
    
    /** Higher value means that big distances are compressed relatively more */
    private double mLogScale = 1.25;

    private final int mUserScreenRadius = 25;
    
    private Paint mTextPaint;
    private Paint mDistancePaint;
    private Paint mCachePaint;
    private Paint mUserPaint;
    private Paint mSpeedPaint;
    private Paint mAccuracyPaint;
    private Paint mCompassNorthPaint;
    private Paint mCompassSouthPaint;
    private Paint mGlowPaint;
    private Shader mGlowShader;
    private final CachesProviderCount mCachesProvider;
    //private ArrayList<Geocache> mCaches = new ArrayList<Geocache>();
    
    /** This class contains all parameters needed to render the proximity view.
     */
    class ProximityParameters {
        private ArrayList<Geocache> mCaches;
        /** The last point in time when we got a GPS reading */
        private long mLastPositioningTime;
    }

    /** Which direction the device is pointed, in degrees */
    //TODO: Create new class DegreesParameter
    private Parameter mDeviceDirection = new AngularParameter(0.03, 0.3);

    /** Location reading accuracy in meters */
    private Parameter mGpsAccuracy = new ScalarParameter(4.0);

    private Parameter mScaleFactor = new ScalarParameter(12.0);

    /** The speed of the user, measured using GPS */
    private Parameter mUserSpeed = new ScalarParameter(0);
    /** Which way the user is moving */
    private Parameter mUserDirection = new AngularParameter(0.03, 0.3);
    
    private Parameter mLatitude = new ScalarParameter();
    private Parameter mLongitude = new ScalarParameter();
    
    private Parameter[] allParameters = { mDeviceDirection, mGpsAccuracy,
            mScaleFactor, mUserSpeed, mUserDirection };
    
    public ProximityPainter(CachesProviderCount cachesProvider) {
        mCachesProvider = cachesProvider;
        mUserY = 350;
        
        //mUseImerial = mSharedPreferences.getBoolean("imperial", false);

        mTextPaint = new Paint();
        mTextPaint.setColor(Color.GREEN);

        mDistancePaint = new Paint();
        mDistancePaint.setARGB(255, 0, 96, 0);
        mDistancePaint.setStyle(Style.STROKE);
        mDistancePaint.setStrokeWidth(2);
        mDistancePaint.setAntiAlias(true);
        
        mCachePaint = new Paint();
        mCachePaint.setARGB(255, 200, 200, 248);
        mCachePaint.setStyle(Style.STROKE);
        mCachePaint.setStrokeWidth(2);
        mCachePaint.setAntiAlias(true);

        int userColor = Color.argb(255, 216, 176, 128);
        //mGlowShader = new RadialGradient(0, 0, 
        //        (int)mGpsAccuracy.get(), 0x2000ff00, 0xc000ff00, TileMode.CLAMP);
        mAccuracyPaint = new Paint();
        //mAccuracyPaint.setShader(mGlowShader);
        mAccuracyPaint.setStrokeWidth(6);
        mAccuracyPaint.setStyle(Style.STROKE);
        mAccuracyPaint.setColor(userColor);
        float[] intervals = { 20, 10};
        mAccuracyPaint.setPathEffect(new DashPathEffect(intervals, 5));
        mAccuracyPaint.setAntiAlias(true);
        
        mUserPaint = new Paint();
        mUserPaint.setColor(userColor);
        mUserPaint.setStyle(Style.STROKE);
        mUserPaint.setStrokeWidth(6);
        mUserPaint.setAntiAlias(true);
        
        mSpeedPaint = new Paint();
        mSpeedPaint.setColor(userColor);
        mSpeedPaint.setStrokeWidth(6);
        mSpeedPaint.setStyle(Style.STROKE);
        mSpeedPaint.setAntiAlias(true);
        
        mCompassNorthPaint = new Paint();
        mCompassNorthPaint.setARGB(255, 255, 0, 0);
        mCompassNorthPaint.setStrokeWidth(3);
        mCompassNorthPaint.setAntiAlias(true);
        mCompassSouthPaint = new Paint();
        mCompassSouthPaint.setARGB(255, 230, 230, 230);
        mCompassSouthPaint.setStrokeWidth(3);
        mCompassSouthPaint.setAntiAlias(true);
        
        mGlowPaint = new Paint();
        mGlowPaint.setStyle(Style.FILL);
    }
    
    public void setUserLocation(double latitude, double longitude, float accuracy) {
        Log.d("GeoBeagle", "setUserLocation with accuracy = " + accuracy);
        mLatitude.set(latitude);
        mLongitude.set(longitude);
        mGpsAccuracy.set(accuracy);
        mCachesProvider.setCenter(latitude, longitude);
        //TODO: What unit is 'radius'??
        double radius = mCachesProvider.getRadius();
        //mScaleFactor = 
        mScaleFactor.set(mUserY * Math.log(mLogScale) / Math.log(radius*100000));
    }
    
    private long mLastUpdateMillis = 0;
    public void draw(Canvas canvas) {
        canvas.drawColor(Color.BLACK); //Clear screen

        mCenterX = canvas.getWidth() / 2;
        //The maximum pixel distance from user point that's visible on screen
        int maxScreenRadius = (int)Math.ceil(Math.sqrt(mCenterX*mCenterX + mUserY*mUserY));
        int accuracyScreenRadius = transformDistanceToScreen(mGpsAccuracy.get());
        double direction = mDeviceDirection.get();

        //Draw accuracy blur field
        if (accuracyScreenRadius > 0) {
            //TODO: Wasting objects!
            /*
            mGlowShader = new RadialGradient(mCenterX, mUserY,
                    accuracyScreenRadius, 0x80ffff00, 0x20ffff00, TileMode.CLAMP);
            mAccuracyPaint.setShader(mGlowShader);
            */
            canvas.drawCircle(mCenterX, mUserY, accuracyScreenRadius, mAccuracyPaint);
        }
        
        //North
        int x1 = xRelativeUser(maxScreenRadius, Math.toRadians(270-direction));
        int y1 = yRelativeUser(maxScreenRadius, Math.toRadians(270-direction));
        canvas.drawLine(mCenterX, mUserY, x1, y1, mCompassNorthPaint);
        //South
        int x2 = xRelativeUser(maxScreenRadius, Math.toRadians(90-direction));
        int y2 = yRelativeUser(maxScreenRadius, Math.toRadians(90-direction));
        canvas.drawLine(mCenterX, mUserY, x2, y2, mCompassSouthPaint);
        //West-east
        int x3 = xRelativeUser(maxScreenRadius, Math.toRadians(180-direction));
        int y3 = yRelativeUser(maxScreenRadius, Math.toRadians(180-direction));
        int x4 = xRelativeUser(maxScreenRadius, Math.toRadians(-direction));
        int y4 = yRelativeUser(maxScreenRadius, Math.toRadians(-direction));
        canvas.drawLine(x3, y3, x4, y4, mDistancePaint);

        //Draw user speed vector
        if (mUserSpeed.get() > 0) {
            int speedRadius = transformDistanceToScreen(mUserSpeed.get()*3.6);
            int x7 = xRelativeUser(speedRadius, Math.toRadians(mUserDirection.get()-direction-90));
            int y7 = yRelativeUser(speedRadius, Math.toRadians(mUserDirection.get()-direction-90));
            canvas.drawLine(mCenterX, mUserY, x7, y7, mSpeedPaint);
        }
        
        int[] distanceMarks = { 10, 20, 50, 100, 500, 1000, 5000, 10000, 50000 };
        String[] distanceTexts = { "10m", "20m", "50m", "100m", "500m", "1km", "5km", "10km", "50km" };
        for (int ix = 0; ix < distanceMarks.length; ix++) {
            int distance = distanceMarks[ix];
            String text = distanceTexts[ix];
            int radius = transformDistanceToScreen(distance);
            if (radius > maxScreenRadius)
                //Not visible anywhere on screen
                break;
            canvas.drawCircle(mCenterX, mUserY, radius, mDistancePaint);
            if (radius > mCenterX) {
                int height = (int)Math.sqrt(radius*radius - mCenterX*mCenterX);
                canvas.drawText(text, 5, mUserY-height-5, mTextPaint);
            } else {
                canvas.drawText(text, mCenterX-radius-10, mUserY, mTextPaint);
            }
        }
        
        //Draw all geocaches and lines to them
        for (Geocache geocache : mCachesProvider.getCaches()) {
            double angle = Math.toRadians(GeoUtils.bearing(mLatitude.get(), mLongitude.get(), 
                    geocache.getLatitude(), geocache.getLongitude()) - direction - 90);
            double distanceM = GeoUtils.distanceKm(mLatitude.get(), mLongitude.get(), 
                    geocache.getLatitude(), geocache.getLongitude()) * 1000;
            double screenDist = transformDistanceToScreen(distanceM);
            int cacheScreenRadius = (int)(2*scaleFactorAtDistance(distanceM*2));
            mCachePaint.setStrokeWidth((int)Math.ceil(0.5*scaleFactorAtDistance(distanceM)));
            mCachePaint.setAlpha(255);
            int x = mCenterX + (int)(screenDist * Math.cos(angle));
            int y = mUserY + (int)(screenDist * Math.sin(angle));
            drawGlow(canvas, x, y, cacheScreenRadius, (int)(mCachePaint.getStrokeWidth() * (1.0 + Math.abs(Math.sin(mTime)))));
            canvas.drawCircle(x, y, cacheScreenRadius, mCachePaint);
            //Lines to geocaches
            if (screenDist > mUserScreenRadius + cacheScreenRadius) {
                //Cache is outside accuracy circle
                int x5 = xRelativeUser(mUserScreenRadius, angle);
                int y5 = yRelativeUser(mUserScreenRadius, angle);
                int x6 = xRelativeUser(screenDist-cacheScreenRadius, angle);
                int y6 = yRelativeUser(screenDist-cacheScreenRadius, angle);
                mCachePaint.setStrokeWidth(Math.min(8, cacheScreenRadius));
                double closeness = 1 - (0.7*screenDist)/maxScreenRadius;
                mCachePaint.setAlpha((int)Math.min(255, 256 * 1.5 * closeness));
                canvas.drawLine(x5, y5, x6, y6, mCachePaint);
            }
        }
        
        canvas.drawCircle(mCenterX, mUserY, mUserScreenRadius, mUserPaint);
        
        //drawGlow(canvas, mCenterX, mUserY, mUserScreenRadius, 10);
    }

    private void drawGlow(Canvas c, int x, int y, int radius, int thickness) {
        int[] colors = { Color.BLACK, Color.YELLOW, 0x00000000};
        //float[] positions = { radius-thickness, radius, radius+thickness };
        float ratio = thickness / (float)radius;
        float[] positions = { 1 - 2*ratio, 1 - ratio, 1 };
        //TODO: Wastes objects
        mGlowShader = new RadialGradient(x, y, radius+thickness, colors, positions, TileMode.CLAMP);
        mGlowPaint.setShader(mGlowShader);
        c.drawCircle(x, y, radius+2*thickness, mGlowPaint);
    }
    
    /** angle is in radians */
    private int xRelativeUser(double distance, double angle) {
        return mCenterX + (int)(distance * Math.cos(angle));
    }
    /** angle is in radians */
    private int yRelativeUser(double distance, double angle) {
        return mUserY + (int)(distance * Math.sin(angle));
    }
    
    /** Return the distance in pixels for a real-world distance in meters
     * Argument must be positive */
    private int transformDistanceToScreen(double meters) {
        int distance = (int)(mScaleFactor.get() * myLog(meters));
        return Math.max(20, distance - 40);
    }

    /** At distance 'meters', draw a meter as these many pixels */
    private double scaleFactorAtDistance(double meters) {
        if (meters < 6)
            meters = 1;
        else
            meters -= 5;
        return (int)(2000.0/(mScaleFactor.get() * myLog(meters)));
    }

    /** Returns logarithm of x in base mLogScale */
    private double myLog(double x) {
        return Math.log(x) / Math.log(mLogScale);
    }
    
    /** bearing 0 is north, bearing 90 is east */
    public void setUserDirection(double degrees) {
        mDeviceDirection.set(degrees);
    }

    /** bearing 0 is north, bearing 90 is east */
    public void setUserMovement(double bearing, double speed) {
        mUserSpeed.set(speed);
        mUserDirection.set(bearing);
    }

    /** Seconds */
    private double mTime;
    
    /** Update animations timeDelta seconds (preferably much less than a sec) */
    public void advanceTime(double timeDelta) {
        mTime += timeDelta;
        for (Parameter param : allParameters) {
            param.update(timeDelta);
        }
    }
}