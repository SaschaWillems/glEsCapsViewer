package de.saschawillems.glescapsviewer.util;

import android.content.Context;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLES31;
import android.opengl.GLES32;
import android.view.Display;
import android.view.WindowManager;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

public class GLESInfoCollector {
    private static GLESInfoCollector instance;
    private Context context;

    // Device Info
    private String deviceName = "N/A";
    private String deviceOS = "N/A";
    private int deviceCPUCores = 0;
    private float deviceCPUSpeed = 0.0f;
    private String deviceCPUArch = "N/A";
    private int screenWidth = 0;
    private int screenHeight = 0;

    // OpenGL ES Info
    private String renderer = "N/A";
    private String version = "N/A";
    private String vendor = "N/A";
    private String extensions = "";
    private String shadingLanguageVersion = "N/A";
    private int majorVersion = 0;
    private int minorVersion = 0;

    // Capabilities
    private List<String> gles20CapsNames = new ArrayList<>();
    private List<String> gles20CapsValues = new ArrayList<>();
    private List<String> gles30CapsNames = new ArrayList<>();
    private List<String> gles30CapsValues = new ArrayList<>();
    private List<String> gles31CapsNames = new ArrayList<>();
    private List<String> gles31CapsValues = new ArrayList<>();
    private List<String> gles32CapsNames = new ArrayList<>();
    private List<String> gles32CapsValues = new ArrayList<>();

    // Formats
    private List<String> compressedFormats = new ArrayList<>();
    private List<String> shaderBinaryFormats = new ArrayList<>();
    private List<String> programBinaryFormats = new ArrayList<>();

    // EGL Info
    private String eglVendor = "N/A";
    private String eglVersion = "N/A";
    private String eglExtensions = "";
    private String eglClientAPIs = "";

    // Device Features & Sensors
    private List<String> deviceFeatures = new ArrayList<>();
    private List<String> deviceSensors = new ArrayList<>();

    private GLESInfoCollector(Context context) {
        this.context = context.getApplicationContext();
    }

    public static synchronized GLESInfoCollector getInstance(Context context) {
        if (instance == null) {
            instance = new GLESInfoCollector(context);
        }
        return instance;
    }

    public void collectAllInfo() {
        collectDeviceInfo();
        collectGLESInfo();
        collectEGLInfo();
    }

    private void collectDeviceInfo() {
        deviceName = android.os.Build.MODEL;
        deviceOS = android.os.Build.VERSION.RELEASE;
        deviceCPUCores = Runtime.getRuntime().availableProcessors();
        deviceCPUSpeed = getMaxCPUFreqMHz();
        deviceCPUArch = System.getProperty("os.arch");

        // Screen info
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        screenWidth = display.getWidth();
        screenHeight = display.getHeight();

        // Device features
        PackageManager pm = context.getPackageManager();
        FeatureInfo[] features = pm.getSystemAvailableFeatures();
        for (FeatureInfo f : features) {
            if (f.name != null) {
                deviceFeatures.add(f.name);
            }
        }

        // Sensors
        SensorManager sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensors = sm.getSensorList(Sensor.TYPE_ALL);
        for (Sensor s : sensors) {
            deviceSensors.add(s.getName() + " (" + s.getType() + ")");
        }
    }

    private void collectGLESInfo() {
        renderer = GLES20.glGetString(GLES20.GL_RENDERER);
        version = GLES20.glGetString(GLES20.GL_VERSION);
        vendor = GLES20.glGetString(GLES20.GL_VENDOR);
        extensions = GLES20.glGetString(GLES20.GL_EXTENSIONS);
        shadingLanguageVersion = GLES20.glGetString(GLES20.GL_SHADING_LANGUAGE_VERSION);

        if (shadingLanguageVersion == null) {
            shadingLanguageVersion = "N/A";
        }

        extractOpenGLESVersion(version);
        collectGLES20Caps();

        if (majorVersion >= 3) {
            collectGLES30Caps();
            if (minorVersion >= 1) {
                collectGLES31Caps();
            }
            if (minorVersion >= 2) {
                collectGLES32Caps();
            }
        }

        collectCompressedFormats();
        collectShaderBinaryFormats();
        collectProgramBinaryFormats();
    }

    private void collectGLES20Caps() {
        int[] caps = {
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
                GLES20.GL_MAX_VIEWPORT_DIMS
        };

        String[] names = {
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
                "GL_MAX_VIEWPORT_DIMS"
        };

        IntBuffer buffer = IntBuffer.allocate(2);
        for (int i = 0; i < caps.length; i++) {
            GLES20.glGetIntegerv(caps[i], buffer);
            if (GLES20.glGetError() == GLES20.GL_NO_ERROR) {
                gles20CapsNames.add(names[i]);
                gles20CapsValues.add(String.valueOf(buffer.get(0)));
            }
            buffer.rewind();
        }
    }

    private void collectGLES30Caps() {
        int[] caps = {
                GLES30.GL_MAX_3D_TEXTURE_SIZE,
                GLES30.GL_MAX_COLOR_ATTACHMENTS,
                GLES30.GL_MAX_DRAW_BUFFERS,
                GLES30.GL_MAX_ELEMENT_INDEX,
                GLES30.GL_MAX_SAMPLES,
                GLES30.GL_MAX_TRANSFORM_FEEDBACK_SEPARATE_ATTRIBS
        };

        String[] names = {
                "GL_MAX_3D_TEXTURE_SIZE",
                "GL_MAX_COLOR_ATTACHMENTS",
                "GL_MAX_DRAW_BUFFERS",
                "GL_MAX_ELEMENT_INDEX",
                "GL_MAX_SAMPLES",
                "GL_MAX_TRANSFORM_FEEDBACK_SEPARATE_ATTRIBS"
        };

        IntBuffer buffer = IntBuffer.allocate(2);
        for (int i = 0; i < caps.length; i++) {
            GLES30.glGetIntegerv(caps[i], buffer);
            if (GLES30.glGetError() == GLES30.GL_NO_ERROR) {
                gles30CapsNames.add(names[i]);
                gles30CapsValues.add(String.valueOf(buffer.get(0)));
            }
            buffer.rewind();
        }
    }

    private void collectGLES31Caps() {
        int[] caps = {
                GLES31.GL_MAX_COMPUTE_WORK_GROUP_SIZE,
                GLES31.GL_MAX_ATOMIC_COUNTER_BUFFER_BINDINGS
        };

        String[] names = {
                "GL_MAX_COMPUTE_WORK_GROUP_SIZE",
                "GL_MAX_ATOMIC_COUNTER_BUFFER_BINDINGS"
        };

        IntBuffer buffer = IntBuffer.allocate(2);
        for (int i = 0; i < caps.length; i++) {
            try {
                GLES31.glGetIntegerv(caps[i], buffer);
                if (GLES31.glGetError() == GLES31.GL_NO_ERROR) {
                    gles31CapsNames.add(names[i]);
                    gles31CapsValues.add(String.valueOf(buffer.get(0)));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            buffer.rewind();
        }
    }

    private void collectGLES32Caps() {
        try {
            IntBuffer buffer = IntBuffer.allocate(2);
            GLES32.glGetIntegerv(0x9100, buffer); // GL_TEXTURE_BUFFER_OFFSET_ALIGNMENT
            if (GLES32.glGetError() == GLES32.GL_NO_ERROR) {
                gles32CapsNames.add("GL_TEXTURE_BUFFER_OFFSET_ALIGNMENT");
                gles32CapsValues.add(String.valueOf(buffer.get(0)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void collectCompressedFormats() {
        IntBuffer buffer = IntBuffer.allocate(1);
        GLES20.glGetIntegerv(GLES20.GL_NUM_COMPRESSED_TEXTURE_FORMATS, buffer);
        int numFormats = buffer.get(0);

        if (numFormats > 0) {
            IntBuffer formats = IntBuffer.allocate(numFormats);
            GLES20.glGetIntegerv(GLES20.GL_COMPRESSED_TEXTURE_FORMATS, formats);
            for (int i = 0; i < numFormats; i++) {
                compressedFormats.add("0x" + Integer.toHexString(formats.get(i)));
            }
        }
    }

    private void collectShaderBinaryFormats() {
        IntBuffer buffer = IntBuffer.allocate(1);
        GLES20.glGetIntegerv(GLES20.GL_NUM_SHADER_BINARY_FORMATS, buffer);
        int numFormats = buffer.get(0);

        if (numFormats > 0) {
            IntBuffer formats = IntBuffer.allocate(numFormats);
            GLES20.glGetIntegerv(GLES20.GL_SHADER_BINARY_FORMATS, formats);
            for (int i = 0; i < numFormats; i++) {
                shaderBinaryFormats.add("0x" + Integer.toHexString(formats.get(i)));
            }
        }
    }

    private void collectProgramBinaryFormats() {
        try {
            IntBuffer buffer = IntBuffer.allocate(1);
            GLES30.glGetIntegerv(GLES30.GL_NUM_PROGRAM_BINARY_FORMATS, buffer);
            int numFormats = buffer.get(0);

            if (numFormats > 0) {
                IntBuffer formats = IntBuffer.allocate(numFormats);
                GLES30.glGetIntegerv(GLES30.GL_PROGRAM_BINARY_FORMATS, formats);
                for (int i = 0; i < numFormats; i++) {
                    programBinaryFormats.add("0x" + Integer.toHexString(formats.get(i)));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void collectEGLInfo() {
        try {
            EGL10 egl = (EGL10) EGLContext.getEGL();
            EGLDisplay display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);

            if (egl.eglInitialize(display, null)) {
                eglVendor = egl.eglQueryString(display, EGL10.EGL_VENDOR);
                eglVersion = egl.eglQueryString(display, EGL10.EGL_VERSION);
                eglExtensions = egl.eglQueryString(display, EGL10.EGL_EXTENSIONS);
                String clientAPIs = egl.eglQueryString(display, 0x308D); // EGL_CLIENT_APIS
                eglClientAPIs = clientAPIs != null ? clientAPIs : "N/A";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void extractOpenGLESVersion(String versionString) {
        if (versionString != null) {
            Scanner scanner = new Scanner(versionString);
            scanner.useDelimiter("[^\\w']+");

            while (scanner.hasNext()) {
                if (scanner.hasNextInt()) {
                    majorVersion = scanner.nextInt();
                    if (scanner.hasNextInt()) {
                        minorVersion = scanner.nextInt();
                    }
                    break;
                }
                if (scanner.hasNext()) {
                    scanner.next();
                }
            }
            scanner.close();
        }
    }

    private float getMaxCPUFreqMHz() {
        try {
            RandomAccessFile reader = new RandomAccessFile("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq", "r");
            String line = reader.readLine();
            reader.close();
            return Float.parseFloat(line) / 1000.0f;
        } catch (IOException e) {
            return 0.0f;
        }
    }

    // Getters
    public String getDeviceName() { return deviceName; }
    public String getDeviceOS() { return deviceOS; }
    public int getDeviceCPUCores() { return deviceCPUCores; }
    public float getDeviceCPUSpeed() { return deviceCPUSpeed; }
    public String getDeviceCPUArch() { return deviceCPUArch; }
    public int getScreenWidth() { return screenWidth; }
    public int getScreenHeight() { return screenHeight; }
    public String getRenderer() { return renderer; }
    public String getVersion() { return version; }
    public String getVendor() { return vendor; }
    public String getExtensions() { return extensions; }
    public String getShadingLanguageVersion() { return shadingLanguageVersion; }
    public int getMajorVersion() { return majorVersion; }
    public int getMinorVersion() { return minorVersion; }
    public List<String> getGLES20CapsNames() { return gles20CapsNames; }
    public List<String> getGLES20CapsValues() { return gles20CapsValues; }
    public List<String> getGLES30CapsNames() { return gles30CapsNames; }
    public List<String> getGLES30CapsValues() { return gles30CapsValues; }
    public List<String> getGLES31CapsNames() { return gles31CapsNames; }
    public List<String> getGLES31CapsValues() { return gles31CapsValues; }
    public List<String> getGLES32CapsNames() { return gles32CapsNames; }
    public List<String> getGLES32CapsValues() { return gles32CapsValues; }
    public List<String> getCompressedFormats() { return compressedFormats; }
    public List<String> getShaderBinaryFormats() { return shaderBinaryFormats; }
    public List<String> getProgramBinaryFormats() { return programBinaryFormats; }
    public String getEGLVendor() { return eglVendor; }
    public String getEGLVersion() { return eglVersion; }
    public String getEGLExtensions() { return eglExtensions; }
    public String getEGLClientAPIs() { return eglClientAPIs; }
}
