/*
*
* OpenGL ES hardware capability viewer and database
*
* Copyright (C) 2011-2015 by Sascha Willems (www.saschawillems.de)
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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.UiModeManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TextView;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class GLActivity extends Activity implements PropertyChangeListener {

	public boolean mCapsVisible = false;
	
    public static GLESInfo mGLESInfo;
    private GLES20Renderer mRenderer;

    public static String baseURL = "https://opengles.gpuinfo.org";
    
    private Context mContext;
     
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);			
		mGLESInfo = new GLESInfo();
		setContentView(R.layout.activity_gl);
		        
	    mContext = this;

        MyGLSurfaceView mGLSurfaceView = (MyGLSurfaceView) findViewById(R.id.glsurfaceview);
		mRenderer = new GLES20Renderer(this, mGLESInfo);
	    mRenderer.addChangeListener(this);       			
		mRenderer.mTableLayout = (TableLayout) findViewById(R.id.tableLayout);
		mRenderer.mDisplay = getWindowManager().getDefaultDisplay();
	
		mGLSurfaceView.setRenderer(mRenderer);
	    mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        UiModeManager uiModeManager = (UiModeManager) getSystemService(UI_MODE_SERVICE);
        if (uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION)
            findViewById(R.id.buttoncontainer).setVisibility(View.VISIBLE);

        findViewById(R.id.uploadButton).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                uploadReport();
            }
        });
        findViewById(R.id.aboutButton).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showAbout();
            }
        });
    }
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	//	getMenuInflater().inflate(R.menu.gl, menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getPropertyName().equals("surfaceCreated")) {

            Thread t = new Thread() {
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mRenderer.fillTableLayout();
                        }
                    });
                }
            };
            t.start();
		}
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_upload:
                uploadReport();
                break;
            case R.id.action_about:
                showAbout();
                break;
            case R.id.action_showdatabase:
            	showDatabase();
            	break;
            default:
                break;
        }

        return true;
    }

    private void showUploadResult(int responseCode, String responseString) {
        String uploadMessage;
        String uploadCaption;

        if (responseString.contains("res_duplicate")) {
            uploadCaption = "Duplicate report";
            uploadMessage = "A report for your device and OpenGL ES version is already present in the database!";
        } else {
            if (responseString.contains("res_uploaded")) {
                uploadCaption = "Upload finished";
                uploadMessage = "Your report has been uploaded to the database! Thanks for your contribution!";
            } else {
                uploadCaption = "Error!";
                uploadMessage = responseString;
            }
        }

        // TODO : Custom dialog with link to uploaded report

        new AlertDialog.Builder(mContext)
            .setTitle(uploadCaption)
            .setMessage(uploadMessage)
            .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                }
            })
            .show();
    }

    private String checkReportPresent() {
        try {
        	
            StrictMode.ThreadPolicy policy = new StrictMode.
                    ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            
            String paramString = URLEncoder.encode(mGLESInfo.deviceDescription(), "utf-8");          
            
			URL url = new URL(baseURL + "/gles_checkreport.php?description=" + paramString);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            int serverResponseCode = conn.getResponseCode();
            String serverResponseMessage = conn.getResponseMessage();

            String[] responsePart = serverResponseMessage.split(" ");
            if (responsePart[0].equals("report_present")) {
            	return baseURL + "/gles_generatereport.php?reportID=" + responsePart[1];
            } else {
            	return "";
            }
                        
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }    	
    }
    
    private void uploadReport() {
    	
    	final String reportPresent = checkReportPresent() ;
    	if (!reportPresent.equals("")) {
            new AlertDialog.Builder(this)
            .setTitle("Device already present")
            .setMessage("A hardware report for this device is already present in the database." + "\n" + "\n" + "Display it in the browser?")
            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                }
            })
            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                	Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(reportPresent)); 
                	startActivity(intent);
            }
            })
            .show();
    	} else {    	
	        LayoutInflater inflater = this.getLayoutInflater();
	        final EditText input = new EditText(this);
	        input.setHint("Your nickname (optional)");
	        new AlertDialog.Builder(mContext)
	            .setTitle("Upload")
	//            .setView(inflater.inflate(R.layout.upload, null))
	            .setMessage("Upload current report to database?")
	        	.setView(input)           
	
	            .setPositiveButton("Upload", new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int which) {
	
	                    try {
	                        StrictMode.ThreadPolicy policy = new StrictMode.
	                                ThreadPolicy.Builder().permitAll().build();
	                        StrictMode.setThreadPolicy(policy);
	
	
	                        URL url = new URL(baseURL + "/gles_uploadreport.php");
	
	                        HttpURLConnection conn = null;
	                        DataOutputStream dos = null;
	                        String lineEnd = "\r\n";
	                        String twoHyphens = "--";
	                        String boundary = "*****";
	                        String filename = "glescapsviewerreport.xml";
	                        int bytesRead, bytesAvailable, bufferSize;
	                        byte[] buffer;
	                        int maxBufferSize = 1 * 1024 * 1024;
	
	                        // Open a HTTP  connection to  the URL
	                        conn = (HttpURLConnection) url.openConnection();
	                        conn.setDoInput(true); // Allow Inputs
	                        conn.setDoOutput(true); // Allow Outputs
	                        conn.setUseCaches(false); // Don't use a Cached Copy
	                        conn.setRequestMethod("POST");
	                        conn.setRequestProperty("Connection", "Keep-Alive");
	                        conn.setRequestProperty("ENCTYPE", "multipart/form-data");
	                        conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
	                        conn.setRequestProperty("name", filename);
	
	                        dos = new DataOutputStream(conn.getOutputStream());
	
	                        dos.writeBytes(twoHyphens + boundary + lineEnd);
	                        dos.writeBytes("Content-Disposition: form-data; name=\"data\";filename=\""
	                                + filename + "\"" + lineEnd);
	
	                        dos.writeBytes(lineEnd);
	
	                        String xmlstring = mGLESInfo.saveToXML(input.getText().toString());
	
	                        dos.writeBytes(xmlstring);
	
	                        // send multipart form data necesssary after file data...
	                        dos.writeBytes(lineEnd);
	                        dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
	
	                        // Responses from the server (code and message)
	                        int serverResponseCode = conn.getResponseCode();
	                        String serverResponseMessage = conn.getResponseMessage();
	
	                        showUploadResult(serverResponseCode, serverResponseMessage);
	
	                    } catch (Exception e) {
	                        e.printStackTrace();
	                    }
	
	                }
	            })
	
	            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int which) {
	                }
	            })
	
	            .show();
    	}
    }

    private void showAbout() {
        // Inflate the about message contents
        View messageView = getLayoutInflater().inflate(R.layout.about, null, false);


        // When linking text, force to always use default color. This works
        // around a pressed color state bug.
        TextView textView = (TextView) messageView.findViewById(R.id.about_credits);
        int defaultColor = textView.getTextColors().getDefaultColor();
        textView.setTextColor(defaultColor);

        new AlertDialog.Builder(this)
            .setTitle(R.string.app_name)
            .setView(messageView)

            .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                }
            })

            .show();
    }
    
    private void showDatabase() {
    	Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(baseURL));
    	startActivity(intent);
    }
        
    class HideMeListener implements OnClickListener {
        final View mTarget;

        HideMeListener(View target) {
          mTarget = target;
        }

        public void onClick(View v) {
          mTarget.setVisibility(View.INVISIBLE);
        }

      }

}
