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

import android.content.Context;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.opengl.EGL14;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.util.Xml;
import android.view.Display;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

class SensorInfo {
	public String mName;
	public float mMaxRange;
	public float mResolution;
}

class EGLConfigInfo {
	public int id;
	public int redSize; 
	public int greenSize; 
	public int blueSize;
	public int alphaSize;
	public int depthSize;
	public int stencilSize;
	public String renderableType;
	
	public void getFromEGLConfig (EGL10 EGL, EGLDisplay display, EGLConfig config) {
		int[] value = new int[1];

		EGL.eglGetConfigAttrib(display, config, EGL10.EGL_CONFIG_ID, value);
		id = value[0];		
		
		EGL.eglGetConfigAttrib(display, config, EGL10.EGL_RED_SIZE, value);
		redSize = value[0];

		EGL.eglGetConfigAttrib(display, config, EGL10.EGL_GREEN_SIZE, value);
		greenSize = value[0];

		EGL.eglGetConfigAttrib(display, config, EGL10.EGL_BLUE_SIZE, value);
		blueSize = value[0];
		
		EGL.eglGetConfigAttrib(display, config, EGL10.EGL_ALPHA_SIZE, value);
		alphaSize = value[0];
		
		EGL.eglGetConfigAttrib(display, config, EGL10.EGL_DEPTH_SIZE, value);
		depthSize = value[0];				

		EGL.eglGetConfigAttrib(display, config, EGL10.EGL_STENCIL_SIZE, value);
		stencilSize = value[0];				
		
		EGL.eglGetConfigAttrib(display, config, EGL10.EGL_RENDERABLE_TYPE, value);
		
		renderableType = "";
		
		if ((value[0] & EGL14.EGL_OPENGL_ES_BIT) == EGL14.EGL_OPENGL_ES_BIT)   {
			renderableType = "GLES";
		}
				
		if ((value[0] & EGL14.EGL_OPENGL_ES2_BIT) == EGL14.EGL_OPENGL_ES2_BIT)   {
			renderableType += " GLES2";
		}
		
		if ((value[0] & EGL14.EGL_OPENVG_BIT) == EGL14.EGL_OPENVG_BIT)   {
			renderableType += " OpenVG";
		}				
		renderableType = renderableType.trim().replace(" ", ", ");		
	}
	
}

class GLESInfo {
	public String mRenderer = "none";
	public String mVersion = "none";
	public String mVendor = "none";
	public String mExtensions = "none";
	public int mMajorVersion = 0;
	public int mMinorVersion = 0;
	public int mShadingLanguageMajorVersion = 0;
	public int mShadingLanguageMinorVersion = 0;
	public List<String> mGLES20CapsDisplayName;
	public List<String> mGLES20CapsName;
	public List<String> mGLES20CapsValue;
	public List<String> mGLES30CapsDisplayName;
	public List<String> mGLES30CapsName;
	public List<String> mGLES30CapsValue;
	public List<String> mGLCompressedFormats;
	public List<String> mGLShaderBinaryFormats;
	public List<String> mGLProgramBinaryFormats;
	public String mShadingLanguageVersion = "empty";

    public boolean mEGLAvailable = false;
    public String mEGLVendor = "";
    public String mEGLVersion = "";
    public String mEGLExtensions = "";
    public String mEGLClientAPIs = "";
    public EGLConfigInfo[] mEGLConfigs;

    public String mDeviceName = "";
    public String mDeviceOS = "";
    public int mDeviceCPUCores = 0;
    public float mDeviceCPUSpeed = 0;
    public int mDeviceTotalRAM = 0;
    public String mDeviceCPUArch = "";
    public int mScreenWidth = 0;
    public int mScreenHeight = 0;
    public List<String> mDeviceFeatures;
    public List<SensorInfo> mDeviceSensors;
       
    public GLESInfo() {
    	super();
    	
		mGLES20CapsValue = new ArrayList<String>();
		mGLES20CapsDisplayName = new ArrayList<String>();
		mGLES20CapsName = new ArrayList<String>();
		
		mGLES30CapsValue = new ArrayList<String>();
		mGLES30CapsDisplayName = new ArrayList<String>();    			
		mGLES30CapsName = new ArrayList<String>();
    	
		mDeviceFeatures = new ArrayList<String>();
		mDeviceSensors = new ArrayList<SensorInfo>();
		
		mGLCompressedFormats = new ArrayList<String>();
		mGLShaderBinaryFormats = new ArrayList<String>();
		mGLProgramBinaryFormats = new ArrayList<String>();
    }
    
    // Extract major and minor OpenGL ES version from version string (which may contain additional version numbers and info)
    public void extractOpenGLESVersion(String versionString) {
        if (versionString != null) {
         Scanner scanner = new Scanner(versionString);
            scanner.useDelimiter("[^\\w']+");

            while (scanner.hasNext()) {
                if (scanner.hasNextInt()) {
                	mMajorVersion = scanner.nextInt();
                	mMinorVersion = scanner.nextInt();
                	break;
                }
                if (scanner.hasNext()) {
                    scanner.next();
                }
            }
        }
    }
    
    // Extract major and minor OpenGL ES shading language version from version string (which may contain additional version numbers and info)
    public void extractOpenGLESShadingLanguageVersion(String versionString) {
        if (versionString != null) {
         Scanner scanner = new Scanner(versionString);
            scanner.useDelimiter("[^\\w']+");

            while (scanner.hasNext()) {
                if (scanner.hasNextInt()) {
                	mShadingLanguageMajorVersion = scanner.nextInt();
                	mShadingLanguageMinorVersion = scanner.nextInt();
                	break;
                }
                if (scanner.hasNext()) {
                    scanner.next();
                }
            }
        }
    }
    

    // Get name from compressed texture format
	public String getCompressedFormatName(int Enum) {

		String mFormatName = "unknown";
		
		switch(Enum) {
		
		//GL_AMD_compressed_3DC_texture
		case 0x87F9:
			mFormatName = "3DC_X_AMD";
			break;
		case 0x87FA:
			mFormatName = "3DC_XY_AMD";
			break;
	
		// GL_AMD_compressed_ATC_texture
		case 0x8C92:
			mFormatName = "ATC_RGB_AMD";
			break;
		case 0x8C93:
			mFormatName = "ATC_RGBA_EXPLICIT_ALPHA_AMD";
			break;
		case 0x87EE:
			mFormatName = "ATC_RGBA_INTERPOLATED_ALPHA_AMD";
			break;
			
		// GL_OES_compressed_ETC1_RGB8_texture
		case 0x8D64:
			mFormatName = "ETC1_RGB8_OES";
			break;
			
		// GL_OES_compressed_paletted_texture        
		case 0x8B90: 
			mFormatName = "PALETTE4_RGB8_OES";
			break;         
        case 0x8B91: 
        	mFormatName = "PALETTE4_RGBA8_OES";
			break;                 
        case 0x8B92: 
        	mFormatName = "PALETTE4_R5_G6_B5_OES";
			break;              
        case 0x8B93: 
        	mFormatName = "PALETTE4_RGBA4_OES";
			break;                 
        case 0x8B94: 
        	mFormatName = "PALETTE4_RGB5_A1_OES";
			break;               
        case 0x8B95: 
        	mFormatName = "PALETTE8_RGB8_OES";
			break;                  
        case 0x8B96: 
        	mFormatName = "PALETTE8_RGBA8_OES";
			break;                 
        case 0x8B97: 
        	mFormatName = "PALETTE8_R5_G6_B5_OES";
			break;              
        case 0x8B98: 
        	mFormatName = "PALETTE8_RGBA4_OES";
			break;                 
        case 0x8B99: 
        	mFormatName = "PALETTE8_RGB5_A1_OES";
			break;    
			
		// GL_EXT_texture_compression_dxt1 
		// GL_ANGLE_texture_compression_dxt1..5 
		// GL_NV_texture_compression_s3tc
		// GL_EXT_texture_compression_s3tc
        case 0x83F0: 
        	mFormatName = "COMPRESSED_RGB_S3TC_DXT1";
			break;    
        case 0x83F1: 
        	mFormatName = "COMPRESSED_RGBA_S3TC_DXT1";
			break;    
        case 0x83F2: 
        	mFormatName = "OMPRESSED_RGBA_S3TC_DXT3";
			break;    
        case 0x83F3: 
        	mFormatName = "COMPRESSED_RGBA_S3TC_DXT5";
			break;    
			
		// GL_IMG_texture_compression_pvrtc
        case 0x8C00: 
        	mFormatName = "COMPRESSED_RGB_PVRTC_4BPPV1_IMG";
			break;    						
        case 0x8C01: 
        	mFormatName = "COMPRESSED_RGB_PVRTC_2BPPV1_IMG";
			break;    						
        case 0x8C02: 
        	mFormatName = "COMPRESSED_RGBA_PVRTC_4BPPV1_IMG";
			break;    						
        case 0x8C03: 
        	mFormatName = "COMPRESSED_RGBA_PVRTC_2BPPV1_IMG";
			break;    	
			
		// GL_KHR_texture_compression_astc_hdr
	    // GL_KHR_texture_compression_astc_ldr
			
        case 0x93B0:
        	mFormatName = "COMPRESSED_RGBA_ASTC_4x4_KHR";
			break;            
        case 0x93B1:
        	mFormatName = "COMPRESSED_RGBA_ASTC_5x4_KHR";
			break;            
        case 0x93B2:
        	mFormatName = "COMPRESSED_RGBA_ASTC_5x5_KHR";
			break;           
        case 0x93B3:
        	mFormatName = "COMPRESSED_RGBA_ASTC_6x5_KHR";
			break;           
        case 0x93B4:
        	mFormatName = "COMPRESSED_RGBA_ASTC_6x6_KHR";
			break;       
        case 0x93B5:
        	mFormatName = "COMPRESSED_RGBA_ASTC_8x5_KHR";
			break;            
        case 0x93B6:	
        	mFormatName = "COMPRESSED_RGBA_ASTC_8x6_KHR";
			break;            
        case 0x93B7:	
        	mFormatName = "COMPRESSED_RGBA_ASTC_8x8_KHR";
			break;            
        case 0x93B8:	
        	mFormatName = "COMPRESSED_RGBA_ASTC_10x5_KHR";
			break;           
        case 0x93B9:	
        	mFormatName = "COMPRESSED_RGBA_ASTC_10x6_KHR";
			break;           
        case 0x93BA:	
        	mFormatName = "COMPRESSED_RGBA_ASTC_10x8_KHR";
			break;           
        case 0x93BB:	
        	mFormatName = "COMPRESSED_RGBA_ASTC_10x10_KHR";
			break;          
        case 0x93BC:	
        	mFormatName = "COMPRESSED_RGBA_ASTC_12x10_KHR";
			break;          
        case 0x93BD:	
        	mFormatName = "COMPRESSED_RGBA_ASTC_12x12_KHR";
			break;          
        case 0x93D0:
        	mFormatName = "COMPRESSED_SRGB8_ALPHA8_ASTC_4x4_KHR";
			break;   
        case 0x93D1:	
        	mFormatName = "COMPRESSED_SRGB8_ALPHA8_ASTC_5x4_KHR";
			break;    
        case 0x93D2:	
        	mFormatName = "COMPRESSED_SRGB8_ALPHA8_ASTC_5x5_KHR";
			break;   
        case 0x93D3:	
        	mFormatName = "COMPRESSED_SRGB8_ALPHA8_ASTC_6x5_KHR";
			break;    
        case 0x93D4:	
        	mFormatName = "COMPRESSED_SRGB8_ALPHA8_ASTC_6x6_KHR";
			break;    
        case 0x93D5:	
        	mFormatName = "COMPRESSED_SRGB8_ALPHA8_ASTC_8x5_KHR";
			break;    
        case 0x93D6:	
        	mFormatName = "COMPRESSED_SRGB8_ALPHA8_ASTC_8x6_KHR";
			break;    
        case 0x93D7:	
        	mFormatName = "COMPRESSED_SRGB8_ALPHA8_ASTC_8x8_KHR";
			break;    
        case 0x93D8:	
        	mFormatName = "COMPRESSED_SRGB8_ALPHA8_ASTC_10x5_KHR";
			break;   
        case 0x93D9:	
        	mFormatName = "COMPRESSED_SRGB8_ALPHA8_ASTC_10x6_KHR";
			break;   
        case 0x93DA:	
        	mFormatName = "COMPRESSED_SRGB8_ALPHA8_ASTC_10x8_KHR";
			break;   
		case 0x93DB:	
			mFormatName = "COMPRESSED_SRGB8_ALPHA8_ASTC_10x10_KHR";
			break;  
		case 0x93DC:
			mFormatName = "COMPRESSED_SRGB8_ALPHA8_ASTC_12x10_KHR";
			break;  
		case 0x93DD:	
			mFormatName = "COMPRESSED_SRGB8_ALPHA8_ASTC_12x12_KHR";
			break;
			
		// GL_NV_texture_compression_latc
		case 0x8C70:
			mFormatName = "COMPRESSED_LUMINANCE_LATC1_NV";
			break;                   
		case 0x8C71:
			mFormatName = "COMPRESSED_SIGNED_LUMINANCE_LATC1_NV";
			break;            
		case 0x8C72:
			mFormatName = "COMPRESSED_LUMINANCE_ALPHA_LATC2_NV";
			break;
		case 0x8C73:
			mFormatName = "COMPRESSED_SIGNED_LUMINANCE_ALPHA_LATC2_NV";
			break;
			
		// GL_IMG_texture_compression_pvrtc2	
		case 0x9137:
			mFormatName = "COMPRESSED_RGBA_PVRTC_2BPPV2_IMG";
			break;
		case 0x9138:
			mFormatName = "COMPRESSED_RGBA_PVRTC_4BPPV2_IMG";
			break;
									
			
		default:
			mFormatName = "0x" + Integer.toHexString(Enum);
		}
		
		return mFormatName;		
	}
	
	// Get name from binary shader format enum
	public String getBinaryShaderFormatName(int Enum) {

		String mFormatName = "";

		switch(Enum) {
		
		// GL_FJ_shader_binary_GCCSO
		case 0x9260:
			mFormatName = "GCCSO_SHADER_BINARY_FJ";
			break;
			
		// GL_IMG_shader_binary
		case 0x8C0A:
			mFormatName = "SGX_BINARY_IMG";
			break;

		// GL_ARM_mali_shader_binary
		case 0x8F60:
			mFormatName = "MALI_SHADER_BINARY_ARM";
			break;

		// GL_VIV_shader_binary
		case 0x8FC4:
			mFormatName = "SHADER_BINARY_VIV";
			break;

		// GL_DMP_shader_binary
		case 0x9250:
			mFormatName = "SHADER_BINARY_DMP";
			break;
										
		// GL_DMP_shader_binary
		case 0x890B:
			mFormatName = "NVIDIA_PLATFORM_BINARY_NV";
			break;			
		
		default:
			mFormatName = "0x" + Integer.toHexString(Enum);
		}
	
		return mFormatName;		
	}
	
	// Get name from binary program format enum
	public String getBinaryProgramFormatName(int Enum) {

		String mFormatName = "";
		
		switch(Enum) {
		
		// GL_AMD_program_binary_Z400
		case 0x8740:
			mFormatName = "Z400_BINARY_AMD";
			break;

		// GL_IMG_program_binary	
		case 0x9130:
			mFormatName = "SGX_PROGRAM_BINARY_IMG";
			break;

		// GL_ARM_mali_program_binary	
		case 0x8F61:
			mFormatName = "MALI_PROGRAM_BINARY_ARM";
			break;
			
		// GL_ANGLE_program_binary
		case 0x93A6:
			mFormatName = "PROGRAM_BINARY_ANGLE";
			break;			
				
		default:
			mFormatName = "0x" + Integer.toHexString(Enum);
		}
	
		return mFormatName;		
	}
	
	// Gathers (and stores) all OpenGL ES 2.0 related information
	public void getOpenGLES20Caps() {
		final String[] capDisplayNames = {
			"Max. combined texture image units",
		    "Max. cubemap texture size",
		    "Max. fragment uniform buffers",
		    "Max. renderbuffer size",
		    "Max. texture image units",
		    "Max. texture size",
		    "Max. varying vectors",
		    "Max. vertex attributes",
		    "Max. vertex texture image units",
		    "Max. vertex uniform vectors",
		    "Max. viewport dimension",
		    "Max. compressed texture formats",
		    "Number of binary shader formats",
		    "Number of binary program formats" // Works on ES 2.0 too
		};		

		final int[] capValues = {
			GLES20.GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS,
		    GLES20.GL_MAX_CUBE_MAP_TEXTURE_SIZE,
		    GLES20.GL_MAX_FRAGMENT_UNIFORM_VECTORS,
		    GLES20.GL_MAX_RENDERBUFFER_SIZE,
		    GLES20.GL_MAX_TEXTURE_IMAGE_UNITS,
		    GLES20.GL_MAX_TEXTURE_SIZE,
		    GLES20.GL_MAX_VARYING_VECTORS,
		    GLES20.GL_MAX_VERTEX_ATTRIBS,
		    GLES20.GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS,
		    GLES20.GL_MAX_VERTEX_UNIFORM_VECTORS,
		    GLES20.GL_MAX_VIEWPORT_DIMS,			
		    GLES20.GL_NUM_COMPRESSED_TEXTURE_FORMATS,
		    GLES20.GL_NUM_SHADER_BINARY_FORMATS,
		    GLES30.GL_NUM_PROGRAM_BINARY_FORMATS // Works on ES 2.0 too
		};
		
		final String[] capNames = {
				"GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS",
			    "GL_MAX_CUBE_MAP_TEXTURE_SIZE",
			    "GL_MAX_FRAGMENT_UNIFORM_VECTORS",
			    "GL_MAX_RENDERBUFFER_SIZE",
			    "GL_MAX_TEXTURE_IMAGE_UNITS",
			    "GL_MAX_TEXTURE_SIZE",
			    "GL_MAX_VARYING_VECTORS",
			    "GL_MAX_VERTEX_ATTRIBS",
			    "GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS",
			    "GL_MAX_VERTEX_UNIFORM_VECTORS",
			    "GL_MAX_VIEWPORT_DIMS",			
			    "GL_NUM_COMPRESSED_TEXTURE_FORMATS",
			    "GL_NUM_SHADER_BINARY_FORMATS",
			    "GL_NUM_PROGRAM_BINARY_FORMATS" // Works on ES 2.0 too
			};		
		
		IntBuffer capsValue = IntBuffer.allocate(2);	

		for (int i=0; i < capValues.length; i++) {
			mGLES20CapsDisplayName.add(capDisplayNames[i]);
			mGLES20CapsName.add(capNames[i]);
			GLES20.glGetIntegerv(capValues[i], (IntBuffer)capsValue.rewind());		
			if (GLES20.glGetError() == GLES20.GL_NO_ERROR) { 
				mGLES20CapsValue.add(String.valueOf(capsValue.get(0)));
			} else {
				mGLES20CapsValue.add("unknown");				
			}
		}
	}
	
	// Gathers (and stores) all OpenGL ES 3.0 related information (Note : Only call when ES 3.0 is available, else values may be more-or-less random)
	public void getOpenGLES30Caps() {

		final int[] capValues = {
			GLES30.GL_MAX_3D_TEXTURE_SIZE,
			GLES30.GL_MAX_ARRAY_TEXTURE_LAYERS,
			GLES30.GL_MAX_COLOR_ATTACHMENTS,
			GLES30.GL_MAX_COMBINED_FRAGMENT_UNIFORM_COMPONENTS,
			GLES30.GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS,
			GLES30.GL_MAX_COMBINED_UNIFORM_BLOCKS,
			GLES30.GL_MAX_COMBINED_VERTEX_UNIFORM_COMPONENTS,
			GLES30.GL_MAX_CUBE_MAP_TEXTURE_SIZE,
			GLES30.GL_MAX_DRAW_BUFFERS,
			GLES30.GL_MAX_ELEMENT_INDEX,
			GLES30.GL_MAX_ELEMENTS_INDICES,
			GLES30.GL_MAX_ELEMENTS_VERTICES,
			GLES30.GL_MAX_FRAGMENT_INPUT_COMPONENTS,
			GLES30.GL_MAX_FRAGMENT_UNIFORM_BLOCKS,
			GLES30.GL_MAX_FRAGMENT_UNIFORM_COMPONENTS,
			GLES30.GL_MAX_FRAGMENT_UNIFORM_VECTORS,
			GLES30.GL_MIN_PROGRAM_TEXEL_OFFSET,
			GLES30.GL_MAX_PROGRAM_TEXEL_OFFSET,
			GLES30.GL_MAX_RENDERBUFFER_SIZE,
			GLES30.GL_MAX_SAMPLES,
			GLES30.GL_MAX_SERVER_WAIT_TIMEOUT,
			GLES30.GL_MAX_TEXTURE_IMAGE_UNITS,
			GLES30.GL_MAX_TEXTURE_LOD_BIAS,
			GLES30.GL_MAX_TEXTURE_SIZE,
			GLES30.GL_MAX_TRANSFORM_FEEDBACK_INTERLEAVED_COMPONENTS,
			GLES30.GL_MAX_TRANSFORM_FEEDBACK_SEPARATE_ATTRIBS,
			GLES30.GL_MAX_TRANSFORM_FEEDBACK_SEPARATE_COMPONENTS,
			GLES30.GL_MAX_UNIFORM_BLOCK_SIZE,
			GLES30.GL_MAX_UNIFORM_BUFFER_BINDINGS,
			GLES30.GL_MAX_VARYING_COMPONENTS,
			GLES30.GL_MAX_VARYING_VECTORS,
			GLES30.GL_MAX_VERTEX_ATTRIBS,
			GLES30.GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS,
			GLES30.GL_MAX_VERTEX_OUTPUT_COMPONENTS,
			GLES30.GL_MAX_VERTEX_UNIFORM_BLOCKS,
			GLES30.GL_MAX_VERTEX_UNIFORM_COMPONENTS,
			GLES30.GL_MAX_VERTEX_UNIFORM_VECTORS,
			GLES30.GL_MAX_VIEWPORT_DIMS
		};		
		
		final String[] capDisplayNames = {
			"Max. 3D texture size",
			"Max. array texture layers",
			"Max. framebuffer color attachments",
			"Max. fragment shader uniform variables",
			"Max. combined texture image units",
			"Max. uniform blocks per program",
			"Max. words for vertex shader uniform variables",
			"Max. cubemap texture size",
			"Max. draw fragment shader outputs",
			"Max. support implementation index",
			"Recommended max vertex array indices",
			"Recommended max vertex array vertices",
			"Max. fragment shader inputs",
			"Max. fragment shader uniform blocks",
			"Max. fragment shader uniform components",
			"Max. fragment shader uniform vectors",
			"Max. texture lookup texel offset",
			"Max. renderbuffer size",
			"Max. number of multisample samples",
			"Max. glWaitSync timeout interval",
			"Max. texture image units",
			"Min. absolute level-of-detail bias",
			"Max. absolute level-of-detail bias",
			"Max. texture size",
			"Max. transform feedback buffer components (interleaved mode)",
			"Max. separate transform feedback mode attributes or outputs",
			"Max. separate transform feedback mode components perattribute or output",
			"Max. size of uniform blocks (in basic machine units)",
			"Max. uniform buffer binding points",
			"Max. varying components",
			"Max. varying vectors",
			"Max. vertex attributes",
			"Max. vertex texture image units",
			"Max. vertex shader output components",
			"Max. vertex shader uniform blocks",			
			"Max. vertex shader uniform components",
			"Max. vertex shader uniform vectors",
			"Max. viewport dimension"
		};
		
		final String[] capNames = {
				"GL_MAX_3D_TEXTURE_SIZE",
				"GL_MAX_ARRAY_TEXTURE_LAYERS",
				"GL_MAX_COLOR_ATTACHMENTS",
				"GL_MAX_COMBINED_FRAGMENT_UNIFORM_COMPONENTS",
				"GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS",
				"GL_MAX_COMBINED_UNIFORM_BLOCKS",
				"GL_MAX_COMBINED_VERTEX_UNIFORM_COMPONENTS",
				"GL_MAX_CUBE_MAP_TEXTURE_SIZE",
				"GL_MAX_DRAW_BUFFERS",
				"GL_MAX_ELEMENT_INDEX",
				"GL_MAX_ELEMENTS_INDICES",
				"GL_MAX_ELEMENTS_VERTICES",
				"GL_MAX_FRAGMENT_INPUT_COMPONENTS",
				"GL_MAX_FRAGMENT_UNIFORM_BLOCKS",
				"GL_MAX_FRAGMENT_UNIFORM_COMPONENTS",
				"GL_MAX_FRAGMENT_UNIFORM_VECTORS",
				"GL_MIN_PROGRAM_TEXEL_OFFSET",
				"GL_MAX_PROGRAM_TEXEL_OFFSET",
				"GL_MAX_RENDERBUFFER_SIZE",
				"GL_MAX_SAMPLES",
				"GL_MAX_SERVER_WAIT_TIMEOUT",
				"GL_MAX_TEXTURE_IMAGE_UNITS",
				"GL_MAX_TEXTURE_LOD_BIAS",
				"GL_MAX_TEXTURE_SIZE",
				"GL_MAX_TRANSFORM_FEEDBACK_INTERLEAVED_COMPONENTS",
				"GL_MAX_TRANSFORM_FEEDBACK_SEPARATE_ATTRIBS",
				"GL_MAX_TRANSFORM_FEEDBACK_SEPARATE_COMPONENTS",
				"GL_MAX_UNIFORM_BLOCK_SIZE",
				"GL_MAX_UNIFORM_BUFFER_BINDINGS",
				"GL_MAX_VARYING_COMPONENTS",
				"GL_MAX_VARYING_VECTORS",
				"GL_MAX_VERTEX_ATTRIBS",
				"GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS",
				"GL_MAX_VERTEX_OUTPUT_COMPONENTS",
				"GL_MAX_VERTEX_UNIFORM_BLOCKS",
				"GL_MAX_VERTEX_UNIFORM_COMPONENTS",
				"GL_MAX_VERTEX_UNIFORM_VECTORS",
				"GL_MAX_VIEWPORT_DIMS"
			};			
		
		IntBuffer capsValue = IntBuffer.allocate(2);	

		for (int i=0; i < capValues.length; i++) {
			mGLES30CapsDisplayName.add(capDisplayNames[i]);
			mGLES30CapsName.add(capNames[i]);
			GLES20.glGetIntegerv(capValues[i], (IntBuffer)capsValue.rewind());		
			if (GLES20.glGetError() == GLES20.GL_NO_ERROR) { 
				mGLES30CapsValue.add(String.valueOf(capsValue.get(0)));
			} else {
				mGLES30CapsValue.add("unknown");				
			}
		}	
				
	}
	
	// Get OpenGL information of current implementation
	public void getOpenGLImplementationInfo() {
		// Get supported OpenGL ES version
		extractOpenGLESVersion(GLES20.glGetString(GLES20.GL_VERSION));
		
		//IntBuffer GLintVal = IntBuffer.allocate(1);
		int glError;
		final int[] glRes = new int[1];
		IntBuffer GLintVal = IntBuffer.allocate(2);
		IntBuffer GLintArr = IntBuffer.allocate(2);
		
		// Gather information on the OpenGL ES implementation
		mRenderer = GLES20.glGetString(GLES20.GL_RENDERER);        
		mVersion = GLES20.glGetString(GLES20.GL_VERSION);
		mVendor = GLES20.glGetString(GLES20.GL_VENDOR);        
		mExtensions = GLES20.glGetString(GLES20.GL_EXTENSIONS);
		mShadingLanguageVersion = GLES20.glGetString(GLES20.GL_SHADING_LANGUAGE_VERSION);
		if (mShadingLanguageVersion == null) {
			// OpenGL ES 1.x device?
			mShadingLanguageVersion = "unknown";
		} else {
			extractOpenGLESShadingLanguageVersion(GLES20.glGetString(GLES20.GL_SHADING_LANGUAGE_VERSION));			
		}

		
		// OpenGL ES caps
		if (mMajorVersion >= 2) 
			getOpenGLES20Caps();
		
		if (mMajorVersion >= 3)
			getOpenGLES30Caps();

		// Available compressed texture formats
		GLES20.glGetIntegerv(GLES20.GL_NUM_COMPRESSED_TEXTURE_FORMATS, (IntBuffer)GLintVal.rewind());
		glError = GLES20.glGetError();		
		if (glError == GLES20.GL_NO_ERROR) {
			if (GLintVal.get(0) > 0) {
				GLintArr = IntBuffer.allocate(GLintVal.get(0));
				GLES20.glGetIntegerv(GLES20.GL_COMPRESSED_TEXTURE_FORMATS, (IntBuffer)GLintArr.rewind());						
				for (int i=0; i < GLintArr.capacity(); ++i) {					
					mGLCompressedFormats.add(getCompressedFormatName(GLintArr.get(i)));
				}
			}
		}		

		// Available binary shader formats		
		GLES20.glGetIntegerv(GLES20.GL_NUM_SHADER_BINARY_FORMATS, (IntBuffer)GLintVal.rewind());
		glError = GLES20.glGetError();
		if (glError == GLES20.GL_NO_ERROR) {
			if (GLintVal.get(0) > 0) {
				GLintArr = IntBuffer.allocate(GLintVal.get(0));
				GLES20.glGetIntegerv(GLES20.GL_SHADER_BINARY_FORMATS, (IntBuffer)GLintArr.rewind());						
				for (int i=0; i < GLintArr.capacity(); ++i) {					
					mGLShaderBinaryFormats.add(getBinaryShaderFormatName(GLintArr.get(i)));
				}
			}
		}
		
		// Available binary program formats (actually ES 3.0, but partially available on ES 2.0 devices)		
		GLES20.glGetIntegerv(GLES30.GL_NUM_PROGRAM_BINARY_FORMATS, (IntBuffer)GLintVal.rewind());
		glError = GLES20.glGetError();
		if (glError == GLES20.GL_NO_ERROR) {
			if (GLintVal.get(0) > 0) {
				GLintArr = IntBuffer.allocate(GLintVal.get(0));
				GLES20.glGetIntegerv(GLES30.GL_PROGRAM_BINARY_FORMATS, (IntBuffer)GLintArr.rewind());						
				for (int i=0; i < GLintArr.capacity(); ++i) {					
					mGLProgramBinaryFormats.add(getBinaryProgramFormatName(GLintArr.get(i)));
				}
			}
		}
	}

	// Get EGL information of current implementation
	public void getEGLImplementationInfo() {
        EGL10 EGL = (EGL10)EGLContext.getEGL();
        EGLDisplay eglDisplay = EGL.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        if (EGL.eglInitialize(eglDisplay, null)) {
            mEGLAvailable = true;
            mEGLVersion = EGL.eglQueryString(eglDisplay, EGL.EGL_VERSION);
            mEGLVendor = EGL.eglQueryString(eglDisplay, EGL.EGL_VENDOR);
            mEGLExtensions = EGL.eglQueryString(eglDisplay, EGL.EGL_EXTENSIONS);
            mEGLClientAPIs = EGL.eglQueryString(eglDisplay, EGL14.EGL_CLIENT_APIS);
            // Available configurations
            int[] numEGLConfigs = new int[1];
            EGL.eglGetConfigs(eglDisplay, null, 0, numEGLConfigs);
            int EGLConfigCount = numEGLConfigs[0]; 
            EGLConfig[] eglConfigs = new EGLConfig[numEGLConfigs[0]];
            EGL.eglGetConfigs(eglDisplay, eglConfigs, EGLConfigCount, numEGLConfigs);
            mEGLConfigs = new EGLConfigInfo[numEGLConfigs[0]];
            for (int i = 0; i < eglConfigs.length; i++) {
            	mEGLConfigs[i] = new EGLConfigInfo();
            	mEGLConfigs[i].getFromEGLConfig(EGL, eglDisplay, eglConfigs[i]);
            }
        } else {
            mEGLAvailable = false;
        }
	}

	// Read CPU speed from appropriate (Linux) file
	public static float getMaxCPUFreqMHz() {

	    float maxFreq = 0;
	    try {

	        RandomAccessFile reader = new RandomAccessFile( "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq", "r" );

	        boolean done = false;
	        while ( ! done ) {
	            String line = reader.readLine();
	            if ( null == line ) {
	                done = true;
	                break;
	            }
	            maxFreq = Float.parseFloat(line) / 1000.0f;
	        }
	        reader.close();

	    } catch ( IOException ex ) {
	        ex.printStackTrace();
	    }

	    return maxFreq;
	}	
	
	// Read memory information from appropriate (Linux) file
	public static synchronized int getTotalRAM() { 
		int tm=1000; 
		try { 
			RandomAccessFile reader = new RandomAccessFile("/proc/meminfo", "r"); 
			String load = reader.readLine(); 
			String[] totrm = load.split(" kB"); 
			String[] trm = totrm[0].split(" "); 
			tm = Integer.parseInt(trm[trm.length-1]); 
			tm = Math.round(tm/1024);
			reader.close();
		} catch (IOException ex) { 
			ex.printStackTrace(); 
		} 
		return tm; 
	}	

	// Get device information (non-GL related)
	public void getDeviceInfo(Display display, Context context) {
        // Device
        mDeviceName = android.os.Build.MODEL;
        mDeviceOS = android.os.Build.VERSION.RELEASE;
        mScreenWidth = display.getWidth();
        mScreenHeight = display.getHeight();
        mDeviceCPUCores = Runtime.getRuntime().availableProcessors();
        mDeviceCPUSpeed = getMaxCPUFreqMHz();
        mDeviceCPUArch = System.getProperty("os.arch");
        //mDeviceTotalRAM = getTotalRAM();
        
        // Available features
        PackageManager packageManager = context.getPackageManager();
        FeatureInfo[] featuresList = packageManager.getSystemAvailableFeatures();
        for (FeatureInfo f : featuresList) {
            mDeviceFeatures.add(f.name);
        }       
        
        // Available sensors
        SensorManager mSensorManager;
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> deviceSensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);

        for (int i=0; i < deviceSensors.size(); i++) {
        	SensorInfo sensorInfo = new SensorInfo();
        	sensorInfo.mName = deviceSensors.get(i).getName();
        	sensorInfo.mMaxRange = deviceSensors.get(i).getMaximumRange();
        	sensorInfo.mResolution = deviceSensors.get(i).getResolution();       	
        	
            mDeviceSensors.add(sensorInfo);
        }
		
	}
	
	public String deviceDescription() {
		return mDeviceName + " " + mDeviceOS + " " + mVersion;
	}
	
    public String saveToXML(String submitter) {
        StringWriter writer = new StringWriter();
        XmlSerializer xmlSerializer = Xml.newSerializer();

        try {
            xmlSerializer.setOutput(writer);
            xmlSerializer.startDocument(null, Boolean.valueOf(true));
            xmlSerializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);

            xmlSerializer.startTag(null, "report");
            xmlSerializer.attribute(null, "submitter", submitter);
            xmlSerializer.attribute(null, "description", deviceDescription());

            // Device
            xmlSerializer.startTag(null, "device");

                // System
                xmlSerializer.startTag(null, "system");
                    xmlSerializer.startTag(null, "devicename").text(mDeviceName).endTag(null, "devicename");
                    xmlSerializer.startTag(null, "os").text(mDeviceOS).endTag(null, "os");
                    xmlSerializer.startTag(null, "screenwidth").text(String.valueOf(mScreenWidth)).endTag(null, "screenwidth");
                    xmlSerializer.startTag(null, "screenheight").text(String.valueOf(mScreenHeight)).endTag(null, "screenheight");
                    xmlSerializer.startTag(null, "cpuspeed").text(String.valueOf(mDeviceCPUSpeed)).endTag(null, "cpuspeed");
                    xmlSerializer.startTag(null, "cpucores").text(String.valueOf(mDeviceCPUCores)).endTag(null, "cpucores");
                    xmlSerializer.startTag(null, "cpuarch").text(String.valueOf(mDeviceCPUArch)).endTag(null, "cpuarch");
                xmlSerializer.endTag(null, "system");

                // Features
                xmlSerializer.startTag(null, "features");
                    for (int i=0; i < mDeviceFeatures.size(); ++i) {
                        xmlSerializer.startTag(null, "feature").text(mDeviceFeatures.get(i)).endTag(null, "feature");
                    }
                xmlSerializer.endTag(null, "features");
                
                // Sensors
                xmlSerializer.startTag(null, "sensors");
                for (int i=0; i < mDeviceSensors.size(); ++i) {
                    xmlSerializer.startTag(null, "sensor").
                    			  attribute(null, "maxrange", String.valueOf(mDeviceSensors.get(i).mMaxRange)).	
                    			  attribute(null, "resolution", String.valueOf(mDeviceSensors.get(i).mResolution)).	
                                  text(mDeviceSensors.get(i).mName).
                                  endTag(null, "sensor");
                }
                xmlSerializer.endTag(null, "sensors");

            xmlSerializer.endTag(null, "device");


            // GLES
            xmlSerializer.startTag(null, "opengles");

                // Implementation
                xmlSerializer.startTag(null, "implementation");
                	xmlSerializer.startTag(null, "vendor").text(mVendor).endTag(null, "vendor");
                	xmlSerializer.startTag(null, "renderer").text(mRenderer).endTag(null, "renderer");
                	xmlSerializer.startTag(null, "version").text(mVersion).endTag(null, "version");
                	xmlSerializer.startTag(null, "majorversion").text(String.valueOf(mMajorVersion)).endTag(null, "majorversion");
                	xmlSerializer.startTag(null, "minorversion").text(String.valueOf(mMinorVersion)).endTag(null, "minorversion");
                	xmlSerializer.startTag(null, "shadinglanguageversion").text(mShadingLanguageVersion).endTag(null, "shadinglanguageversion");
                	xmlSerializer.startTag(null, "shadinglanguagemajorversion").text(String.valueOf(mShadingLanguageMajorVersion)).endTag(null, "shadinglanguagemajorversion");
                	xmlSerializer.startTag(null, "shadinglanguageminorversion").text(String.valueOf(mShadingLanguageMinorVersion)).endTag(null, "shadinglanguageminorversion");
                xmlSerializer.endTag(null, "implementation");

                // Extensions
                xmlSerializer.startTag(null, "extensions");
                
                	String[] extensions = mExtensions.split(" ");
	                for (int i=0; i < extensions.length; ++i) {
	                    xmlSerializer.startTag(null, "extension").
	                    			  text(extensions[i]).
	                    			  endTag(null, "extension");
	                }
                
                xmlSerializer.endTag(null, "extensions");

                // Caps
                // GL ES 2.0
                xmlSerializer.startTag(null, "es20caps");
                
	                for (int i=0; i < mGLES20CapsValue.size(); ++i) {
	                    xmlSerializer.startTag(null, "cap").
	                    			  attribute(null, "name", mGLES20CapsName.get(i)).
	                    			  text(String.valueOf(mGLES20CapsValue.get(i))).
	                    			  endTag(null, "cap");               	
	                }
	                
                xmlSerializer.endTag(null, "es20caps");
                
                // GL ES 3.0
                xmlSerializer.startTag(null, "es30caps");
                
	                for (int i=0; i < mGLES30CapsValue.size(); ++i) {
	                    xmlSerializer.startTag(null, "cap").
					      			  attribute(null, "name", mGLES30CapsName.get(i)).
	                    			  text(String.valueOf(mGLES30CapsValue.get(i))).
	                    			  endTag(null, "cap");               	
	                }
	                
                xmlSerializer.endTag(null, "es30caps");
                

                // Compressed formats
                xmlSerializer.startTag(null, "compressedformats");

	                for (int i=0; i < mGLCompressedFormats.size(); ++i) {
	                    xmlSerializer.startTag(null, "compressedformat").
	                    			  text(mGLCompressedFormats.get(i)).
	                    			  endTag(null, "compressedformat");               	
	                }                
                
                xmlSerializer.endTag(null, "compressedformats");

                // Binary shader formats
                xmlSerializer.startTag(null, "binaryshaderformats");
                
	                for (int i=0; i < mGLShaderBinaryFormats.size(); ++i) {
	                    xmlSerializer.startTag(null, "binaryshaderformat").
	                    			  text(mGLShaderBinaryFormats.get(i)).
	                    			  endTag(null, "binaryshaderformat");               	
	                }                
                
                xmlSerializer.endTag(null, "binaryshaderformats");
                
                // Binary program formats
                xmlSerializer.startTag(null, "binaryprogramformats");
                
	                for (int i=0; i < mGLProgramBinaryFormats.size(); ++i) {
	                    xmlSerializer.startTag(null, "binaryprogramformat").
	                    			  text(mGLProgramBinaryFormats.get(i)).
	                    			  endTag(null, "binaryprogramformat");               	
	                }                
                
                xmlSerializer.endTag(null, "binaryprogramformats");
                

            xmlSerializer.endTag(null, "opengles");

                // EGL
                xmlSerializer.startTag(null, "egl");

                // Implementation
                xmlSerializer.startTag(null, "implementation");
	                xmlSerializer.startTag(null, "vendor").text(mEGLVendor).endTag(null, "vendor");
	                xmlSerializer.startTag(null, "version").text(mEGLVersion).endTag(null, "version");
                xmlSerializer.endTag(null, "implementation");

                // Extensions
                xmlSerializer.startTag(null, "extensions");
                
	            	String[] EGLextensions = mEGLExtensions.split(" ");
	                for (int i=0; i < EGLextensions.length; ++i) {
	                    xmlSerializer.startTag(null, "extension").
	                    			  text(EGLextensions[i]).
	                    			  endTag(null, "extension");               	
	                }        
	                
                xmlSerializer.endTag(null, "extensions");

                // Client APIs
                xmlSerializer.startTag(null, "clientapis");
                
	            	String[] EGLclientapis = mEGLClientAPIs.split(" ");
	                for (int i=0; i < EGLclientapis.length; ++i) {
	                    xmlSerializer.startTag(null, "clientapi").
	                    			  text(EGLclientapis[i]).
	                    			  endTag(null, "clientapi");               	
	                }        
                                
                xmlSerializer.endTag(null, "clientapis");

            xmlSerializer.endTag(null, "egl");


            xmlSerializer.endTag(null, "report");

            xmlSerializer.endDocument();
            xmlSerializer .flush();

            return writer.toString();
        } catch (IOException e) {
            return "";
        }
    }
	
}