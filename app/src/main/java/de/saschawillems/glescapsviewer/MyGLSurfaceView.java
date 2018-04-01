/*
*
* OpenGL ES hardware capability viewer and database
*
* Copyright (C) 2011-2018 by Sascha Willems (www.saschawillems.de)
*
* This code is free software, you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License version 3 as published by the Free Software Foundation.
*
* Please review the following information to ensure the GNU Lesser
* General Public License version 3 requirements will be met:
* http://opensource.org/licenses/lgpl-3.0.html
*
* The code is distributed WITHOUT ANY WARRANTY; without even the
* implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
* PURPOSE.  See the GNU LGPL 3.0 for more details.
*
*/

package de.saschawillems.glescapsviewer;

import android.content.Context;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.MotionEvent;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.opengles.GL10;

// Simple OpenGL ES 2.0 renderer
class GLES20Renderer implements GLSurfaceView.Renderer {

	private List<PropertyChangeListener> listener = new ArrayList<PropertyChangeListener>();	
	
	public TableLayout mTableLayout;
	public Display mDisplay;

	public GLESInfo mGLESInfo;
	public Context mContext;
	
    private float[] mProjMatrix = new float[16];
		
    public GLES20Renderer(Context context, GLESInfo glesinfo) {
    	mGLESInfo = glesinfo;
    	mContext = context;
    }

	@Override
	public void onDrawFrame(GL10 arg0) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glClear( GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
    }

	@Override
	public void onSurfaceChanged(GL10 arg0, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        float ratio = (float) width / height;
        Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, 2, 128);	
	}
	

	@Override
	public void onSurfaceCreated(GL10 arg0, EGLConfig arg1) {

		mGLESInfo.getOpenGLImplementationInfo();
		mGLESInfo.getEGLImplementationInfo();
		mGLESInfo.getDeviceInfo(mDisplay, mContext);

        notifyListeners(this, "surfaceCreated", "", "");
	}
	
	public void addTableContent(TableLayout tableLayout, String caption, String content, int CaptionColor) {
		if (!caption.equals("")) {
			TableRow rowCaption = new TableRow(mContext);
	    	TextView newCaption = new TextView(mContext);
	    	newCaption.setTextColor(CaptionColor);
	    	newCaption.setText(caption);
		    newCaption.setPadding(10, 0, 0, 0);
	    	rowCaption.addView(newCaption);
	    	tableLayout.addView(rowCaption);
		}

		if (!content.equals("")) {
			TableRow rowContent = new TableRow(mContext);
			TextView newContent = new TextView(mContext);
	    	newContent.setTextColor(Color.GRAY);
		    newContent.setText(content);
		    newContent.setPadding(25, 0, 0, 0);
		    rowContent.addView(newContent);	
	    	tableLayout.addView(rowContent);
		}
	}
	
	public void addNewTableRow(TableLayout tableLayout, String caption, String content, Boolean Header) {

    	TableRow newTableRow = new TableRow(mContext);
    	
    	TextView newCaption = new TextView(mContext);
    	newCaption.setTextColor(Color.WHITE);
    	newCaption.setText(caption);
    	newTableRow.addView(newCaption);

    	if (!content.equals("")) {
	    	TextView newContent = new TextView(mContext);
	    	newContent.setText(content);
	    	newTableRow.addView(newContent);
    	}

    	tableLayout.addView(newTableRow);
    	
    	if (Header) {
    		
        	TableRow emptyTableRow = new TableRow(mContext);
            TableRow emptyTableRowSpacer = new TableRow(mContext);

        	emptyTableRow.setMinimumHeight(2);
        	emptyTableRow.setBackgroundColor(Color.rgb(45, 159, 201));

            emptyTableRowSpacer.setMinimumHeight(6);
            emptyTableRowSpacer.setBackgroundColor(Color.BLACK);

        	newCaption.setTextColor(Color.rgb(45,159,201));
        	newCaption.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        	
        	tableLayout.addView(emptyTableRow);
            tableLayout.addView(emptyTableRowSpacer);
    	}
    		
	}		

    public void fillTableLayout() {    	
        // Device
        addNewTableRow(mTableLayout, "Device", "", true);
    	addTableContent(mTableLayout, "Name", mGLESInfo.mDeviceName, Color.WHITE);
    	addTableContent(mTableLayout, "OS", mGLESInfo.mDeviceOS, Color.WHITE);
    	addTableContent(mTableLayout, "Screensize", String.valueOf(mGLESInfo.mScreenWidth) + " x " + String.valueOf(mGLESInfo.mScreenHeight), Color.WHITE);
    	addTableContent(mTableLayout, "CPU", String.valueOf(mGLESInfo.mDeviceCPUCores) + " x " + String.valueOf(mGLESInfo.mDeviceCPUSpeed) + "MHz (" + mGLESInfo.mDeviceCPUArch + ")", Color.WHITE);
    	//addTableContent(mTableLayout, "User space memory", String.valueOf(mGLESInfo.mDeviceTotalRAM) + " MBytes", Color.WHITE);

        // Implementation
        addNewTableRow(mTableLayout, "", "", false);
        addNewTableRow(mTableLayout, "OpenGL ES", "", true);
        addTableContent(mTableLayout, "Vendor", mGLESInfo.mVendor, Color.WHITE);
        addTableContent(mTableLayout, "Renderer", mGLESInfo.mRenderer, Color.WHITE);
        addTableContent(mTableLayout, "Version", String.valueOf(mGLESInfo.mMajorVersion) + "." + String.valueOf(mGLESInfo.mMinorVersion) + " (" + mGLESInfo.mVersion + ")", Color.WHITE);
        addTableContent(mTableLayout, "Shading language version", String.valueOf(mGLESInfo.mShadingLanguageMajorVersion) + "." + String.valueOf(mGLESInfo.mShadingLanguageMinorVersion) + " (" + mGLESInfo.mShadingLanguageVersion + ")", Color.WHITE);

        // Extensions
        String[] extensions = mGLESInfo.mExtensions.split(" ");
        addTableContent(mTableLayout, "Extensions (" + String.valueOf(extensions.length) + ")", "", Color.WHITE);
        for (String extension : extensions) {
            addTableContent(mTableLayout, "", extension, Color.GRAY);
        }

        // Compressed formats
        addTableContent(mTableLayout, "Compressed formats", "", Color.WHITE);
        if (mGLESInfo.mGLCompressedFormats.size() > 0) {
	        String[] compressedFormats = mGLESInfo.mGLCompressedFormats.toArray(new String[mGLESInfo.mGLCompressedFormats.size()]);
            for (String compressedFormat : compressedFormats)
                addTableContent(mTableLayout, "", compressedFormat, Color.GRAY);
        } else {
            addTableContent(mTableLayout, "", "None", Color.GRAY);       	        	        	
        }
                
        // Shader binary formats
        addTableContent(mTableLayout, "Shader binary formats", "", Color.WHITE);
        if (mGLESInfo.mGLShaderBinaryFormats.size() > 0) {
            String[] shaderBinaryFormats = mGLESInfo.mGLShaderBinaryFormats.toArray(new String[mGLESInfo.mGLShaderBinaryFormats.size()]);
            for (String shaderBinaryFormat : shaderBinaryFormats)
                addTableContent(mTableLayout, "", shaderBinaryFormat, Color.GRAY);
        } else {
            addTableContent(mTableLayout, "", "None", Color.GRAY);       	        	
        }
        
        // Program binary formats (ES 3.0)
        addTableContent(mTableLayout, "Program binary formats", "", Color.WHITE);
        if (mGLESInfo.mGLProgramBinaryFormats.size() > 0) {
            String[] programBinaryFormats = mGLESInfo.mGLProgramBinaryFormats.toArray(new String[mGLESInfo.mGLProgramBinaryFormats.size()]);
            for (String programBinaryFormat : programBinaryFormats)
                addTableContent(mTableLayout, "", programBinaryFormat, Color.GRAY);
        } else {
            addTableContent(mTableLayout, "", "None", Color.GRAY);       	       	
        }

        // Caps
        // GL ES 2.0
        addNewTableRow(mTableLayout, "", "", false);
        addNewTableRow(mTableLayout, "OpenGL ES 2.0 Caps", "", true);
        if (mGLESInfo.mGLES20CapsDisplayName.size() > 0) {
	        for (int i=0; i < mGLESInfo.mGLES20CapsDisplayName.size(); ++i) {
	            addTableContent(mTableLayout, mGLESInfo.mGLES20CapsDisplayName.get(i), mGLESInfo.mGLES20CapsValue.get(i), Color.WHITE);
	        }
        } else {
            addTableContent(mTableLayout, "OpenGL ES 2.0 not supported", "", Color.GRAY);       	        	        	
        }
        // GL ES 3.0
        addNewTableRow(mTableLayout, "", "", false);
        addNewTableRow(mTableLayout, "OpenGL ES 3.0 Caps", "", true);
        if (mGLESInfo.mGLES30CapsDisplayName.size() > 0) {
            for (int i=0; i < mGLESInfo.mGLES30CapsDisplayName.size(); ++i) {
                addTableContent(mTableLayout, mGLESInfo.mGLES30CapsDisplayName.get(i), mGLESInfo.mGLES30CapsValue.get(i), Color.WHITE);
            }
        } else {
            addTableContent(mTableLayout, "OpenGL ES 3.0 not supported", "", Color.GRAY);       	        	
        }
		// GL ES 3.1
		addNewTableRow(mTableLayout, "", "", false);
		addNewTableRow(mTableLayout, "OpenGL ES 3.1 Caps", "", true);
		if (mGLESInfo.mGLES31Caps.values.size() > 0) {
			for (int i=0; i < mGLESInfo.mGLES31Caps.values.size(); ++i) {
				addTableContent(mTableLayout, mGLESInfo.mGLES31Caps.displayNames.get(i), mGLESInfo.mGLES31Caps.values.get(i), Color.WHITE);
			}
		} else {
			addTableContent(mTableLayout, "OpenGL ES 3.1 not supported", "", Color.GRAY);
		}
		// GL ES 3.2
		addNewTableRow(mTableLayout, "", "", false);
		addNewTableRow(mTableLayout, "OpenGL ES 3.2 Caps", "", true);
		if (mGLESInfo.mGLES31Caps.values.size() > 0) {
			for (int i=0; i < mGLESInfo.mGLES32Caps.values.size(); ++i) {
				addTableContent(mTableLayout, mGLESInfo.mGLES32Caps.displayNames.get(i), mGLESInfo.mGLES32Caps.values.get(i), Color.WHITE);
			}
		} else {
			addTableContent(mTableLayout, "OpenGL ES 3.2 not supported", "", Color.GRAY);
		}

        // EGL implementation
        if (mGLESInfo.mEGLAvailable) {
            // EGL implementation
            addNewTableRow(mTableLayout, "", "", false);
            addNewTableRow(mTableLayout, "EGL", "", true);
            addTableContent(mTableLayout, "Vendor", mGLESInfo.mEGLVendor, Color.WHITE);
            addTableContent(mTableLayout, "Version", mGLESInfo.mEGLVersion, Color.WHITE);

            // EGL Client APIs
            String[] EGLClientAPIs = mGLESInfo.mEGLClientAPIs.split(" ");
            addTableContent(mTableLayout, "Client APIs (" + String.valueOf(EGLClientAPIs.length) +  ")", "", Color.WHITE);
            for (String EGLClientAPI : EGLClientAPIs) {
                addTableContent(mTableLayout, "", EGLClientAPI, Color.GRAY);
            }            
            
            // EGL Extensions
            String[] EGLextensions = mGLESInfo.mEGLExtensions.split(" ");
            addTableContent(mTableLayout, "Extensions (" + String.valueOf(EGLextensions.length) +  ")", "", Color.WHITE);
            for (String EGLextension : EGLextensions)
                addTableContent(mTableLayout, "", EGLextension, Color.GRAY);
            
            // EGL configs
            addTableContent(mTableLayout, "Configurations (" + String.valueOf(mGLESInfo.mEGLConfigs.length) +  ")", "", Color.WHITE);
            for (int i=0; i < mGLESInfo.mEGLConfigs.length; i++) {
            	EGLConfigInfo configInfo = mGLESInfo.mEGLConfigs[i]; 
            	String configText = " R" + String.valueOf(configInfo.redSize) + 
            						" G" + String.valueOf(configInfo.greenSize) + 
            						" B" + String.valueOf(configInfo.blueSize) + 
            						" A" + String.valueOf(configInfo.alphaSize) + " " +
            						configInfo.renderableType;
            	addTableContent(mTableLayout, "Config no. " + String.valueOf(configInfo.id), configText, Color.GRAY);            	
            }


        } else {
        	// Should never happen, no embedded device without embedded GL
            addNewTableRow(mTableLayout, "", "", false);
            addNewTableRow(mTableLayout, "EGL is not available on this device", "", true);
        }

        // Additional device details
        addNewTableRow(mTableLayout, "", "", false);
        addNewTableRow(mTableLayout, "Device details", "", true);

        // Device sensors
        SensorInfo[] sensors = mGLESInfo.mDeviceSensors.toArray(new SensorInfo[mGLESInfo.mDeviceSensors.size()]);
        addTableContent(mTableLayout, "Sensors (" + String.valueOf(sensors.length) +  ")", "", Color.WHITE);
        for (SensorInfo sensor : sensors) {
            addTableContent(mTableLayout, "", sensor.mName, Color.GRAY);
            addTableContent(mTableLayout, "", "    Max. Range = " + String.valueOf(sensor.mMaxRange), Color.GRAY);
            addTableContent(mTableLayout, "", "    Resolution = " + String.valueOf(sensor.mResolution), Color.GRAY);
        } 
                
        // Features       
        mGLESInfo.mDeviceFeatures.removeAll(Collections.singleton(null));
        Collections.sort(mGLESInfo.mDeviceFeatures);
        String[] deviceFeatures = mGLESInfo.mDeviceFeatures.toArray(new String[mGLESInfo.mDeviceFeatures.size()]);        
        addTableContent(mTableLayout, "Features (" + String.valueOf(deviceFeatures.length) +  ")", "", Color.WHITE);
        for (String deviceFeature : deviceFeatures)
            addTableContent(mTableLayout, "", deviceFeature, Color.WHITE);
    }

	private void notifyListeners(Object object, String property, String oldValue, String newValue) {
	    for (PropertyChangeListener name : listener)
            name.propertyChange(new PropertyChangeEvent(this, property, oldValue, newValue));
	}
	
	public void addChangeListener(PropertyChangeListener newListener) {
		listener.add(newListener);
	}
	
}    


// OpenGL ES surface view
public class MyGLSurfaceView extends GLSurfaceView {
	
	public int mOpenGLESVersion;
	
	private class ContextFactory implements EGLContextFactory {

		private static final int EGL_CONTEXT_CLIENT_VERSION = 0x3098;

		public EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig eglConfig) {
			
		    int[] attrib_list_30 = {EGL_CONTEXT_CLIENT_VERSION, 3, EGL10.EGL_NONE};
		    int[] attrib_list_20 = {EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE};
		    int[] attrib_list_10 = {EGL_CONTEXT_CLIENT_VERSION, 1, EGL10.EGL_NONE};
			
			// Attempt to create OpenGL ES 3.0 context first
		    mOpenGLESVersion = 3;
		    EGLContext context = egl.eglCreateContext(display, eglConfig, EGL10.EGL_NO_CONTEXT, attrib_list_30);

		    if (context.equals(EGL10.EGL_NO_CONTEXT)) {
		    	Log.i("OpenGL ES CapsViewer", "No GL ES 3.0 context available...");
		    }
		    
		    // If the context is null, OpenGL ES 3.0 is not available, so settle for an OpenGL ES 2.0 context
		    if (context.equals(EGL10.EGL_NO_CONTEXT)) {
		    	Log.i("OpenGL ES CapsViewer", "No GL ES 3.0 context available, trying GL ES 2.0...");
		    	mOpenGLESVersion = 2; 
			    context = egl.eglCreateContext(display, eglConfig, EGL10.EGL_NO_CONTEXT, attrib_list_20);	   
		    }
		    
		    // If we still don't have a context, then...panic?
		    if (context.equals(EGL10.EGL_NO_CONTEXT)) {
		    	Log.i("OpenGL ES CapsViewer", "No GL ES 2.0 context available, creating GL ES 1.0 context...");
		    	mOpenGLESVersion = 1;
			    context = egl.eglCreateContext(display, eglConfig, EGL10.EGL_NO_CONTEXT, attrib_list_10);	    	
		    }
		    
		    return context;
		}

		@Override
		public void destroyContext(EGL10 arg0, EGLDisplay arg1, EGLContext arg2) {
			// TODO Auto-generated method stub
			
		}

		}
	
	private boolean isEmulator() {
	    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1
	            && (Build.FINGERPRINT.startsWith("generic")
	                    || Build.FINGERPRINT.startsWith("unknown")
	                    || Build.MODEL.contains("google_sdk")
	                    || Build.MODEL.contains("Emulator")
	                    || Build.MODEL.contains("Android SDK built for x86"));
	}	
	
    public MyGLSurfaceView(Context context, AttributeSet attrs) {
    	super(context, attrs);     
        //setEGLContextFactory(new ContextFactory());
        if (isEmulator()) {
        	// Automatic config chooser won't work on emulator, so request config hat hopefully works
        	setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        }
        setEGLContextClientVersion(2);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent e) {
		switch (e.getAction()) {
			case MotionEvent.ACTION_DOWN:
				
				break;    	
    }
		return true;    
    
    }
    
}
