package com.geoscope.GeoLog.DEVICE.VideoModule.Codecs.H264;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLExt;
import android.opengl.EGLSurface;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Bundle;
import android.view.Surface;

import com.geoscope.Classes.MultiThreading.TCancelableThread;
import com.geoscope.GeoLog.DEVICE.VideoModule.TVideoModule;

@SuppressLint({ "NewApi" })
public class TH264EncoderServer {

	public static final boolean IsSupported() {
		return (android.os.Build.VERSION.SDK_INT >= 18); 
	}
	
	private static final String CodecTypeName = "video/avc";
	@SuppressWarnings("unused")
	private static final String CodecName = "OMX.SEC.avc.enc"; //. Samsung Galaxy S3 specific
	private static final int	CodecLatency = 100000; //. microseconds
	@SuppressWarnings("unused")
	private static final int 	CodecWaitInterval = 10000; //. microseconds
	//.
	private static final int 	Encoding_IFRAMEInterval = 5; //. seconds
	//. Fragment shader that swaps color channels around.
    @SuppressWarnings("unused")
	private static final String SWAPPED_FRAGMENT_SHADER =
            "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;\n" +
            "varying vec2 vTextureCoord;\n" +
            "uniform samplerExternalOES sTexture;\n" +
            "void main() {\n" +
            "  gl_FragColor = texture2D(sTexture, vTextureCoord).gbra;\n" +
            "}\n";

	public static class TClient extends TH264EncoderAbstract {

		public boolean flApplyParameters;
		
		public TClient(boolean pflApplyParameters) {
			flApplyParameters = pflApplyParameters;
		}
		
		@Override
		public void DoOnConfiguration(byte[] Buffer, int BufferSize) throws Exception {
			DoOnOutputBuffer(Buffer,BufferSize, 0,false);
		}
		
		@Override
		public void DoOnOutputBuffer(byte[] Buffer, int BufferSize, long Timestamp, boolean flSyncFrame) throws Exception {
		}
	}
	
	/**
     * Holds state associated with a Surface used for MediaCodec encoder input.
     * <p>
     * The constructor takes a Surface obtained from MediaCodec.createInputSurface(), and uses
     * that to create an EGL window surface.  Calls to eglSwapBuffers() cause a frame of data to
     * be sent to the video encoder.
     * <p>
     * This object owns the Surface -- releasing this will release the Surface too.
     */
    protected static class TInputSurface {
    	
        private static final int EGL_RECORDABLE_ANDROID = 0x3142;

        private Surface mSurface;
        //.
        private int Width;
        private int Height;
        //.
        protected EGLDisplay 	mEGLDisplay = EGL14.EGL_NO_DISPLAY;
        protected EGLConfig[] 	mEGLConfigs = new EGLConfig[1];
        protected EGLContext 	mEGLContext = EGL14.EGL_NO_CONTEXT;
        protected EGLSurface 	mEGLSurface = EGL14.EGL_NO_SURFACE;

        /**
         * Creates a CodecInputSurface from a Surface.
         */
        public TInputSurface(Surface surface, int pWidth, int pHeight) {
            if (surface == null) {
                throw new NullPointerException();
            }
            mSurface = surface;
            //.
            Width = pWidth;
            Height = pHeight;

            eglSetup();
        }

        /**
         * Prepares EGL.  We want a GLES 2.0 context and a surface that supports recording.
         */
        private void eglSetup() {
            mEGLDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
            if (mEGLDisplay == EGL14.EGL_NO_DISPLAY) {
                throw new RuntimeException("unable to get EGL14 display");
            }
            int[] version = new int[2];
            if (!EGL14.eglInitialize(mEGLDisplay, version, 0, version, 1)) {
                throw new RuntimeException("unable to initialize EGL14");
            }

            // Configure EGL for recording and OpenGL ES 2.0.
            int[] attribList = {
                    EGL14.EGL_RED_SIZE, 8,
                    EGL14.EGL_GREEN_SIZE, 8,
                    EGL14.EGL_BLUE_SIZE, 8,
                    EGL14.EGL_ALPHA_SIZE, 8,
                    EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                    EGL_RECORDABLE_ANDROID, 1,
                    EGL14.EGL_NONE
            };
            int[] numConfigs = new int[1];
            EGL14.eglChooseConfig(mEGLDisplay, attribList, 0, mEGLConfigs, 0, mEGLConfigs.length,
                    numConfigs, 0);
            checkEglError("eglCreateContext RGB888+recordable ES2");

            // Configure context for OpenGL ES 2.0.
            int[] attrib_list = {
                    EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                    EGL14.EGL_NONE
            };
            mEGLContext = EGL14.eglCreateContext(mEGLDisplay, mEGLConfigs[0], EGL14.EGL_NO_CONTEXT,
                    attrib_list, 0);
            checkEglError("eglCreateContext");

            // Create a window surface, and attach it to the Surface we received.
            int[] surfaceAttribs = {
                    EGL14.EGL_NONE
            };
            mEGLSurface = EGL14.eglCreateWindowSurface(mEGLDisplay, mEGLConfigs[0], mSurface,
                    surfaceAttribs, 0);
            checkEglError("eglCreateWindowSurface");
        }

        /**
         * Discards all resources held by this class, notably the EGL context.  Also releases the
         * Surface that was passed to our constructor.
         */
        public void release() {
            if (mEGLDisplay != EGL14.EGL_NO_DISPLAY) {
                EGL14.eglMakeCurrent(mEGLDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE,
                        EGL14.EGL_NO_CONTEXT);
                EGL14.eglDestroySurface(mEGLDisplay, mEGLSurface);
                EGL14.eglDestroyContext(mEGLDisplay, mEGLContext);
                EGL14.eglReleaseThread();
                EGL14.eglTerminate(mEGLDisplay);
            }
            mSurface.release();

            mEGLDisplay = EGL14.EGL_NO_DISPLAY;
            mEGLContext = EGL14.EGL_NO_CONTEXT;
            mEGLSurface = EGL14.EGL_NO_SURFACE;

            mSurface = null;
        }

        /**
         * Makes our EGL context and surface current.
         */
        public void makeCurrent() {
            EGL14.eglMakeCurrent(mEGLDisplay, mEGLSurface, mEGLSurface, mEGLContext);
        	GLES20.glViewport(0,0, Width,Height);
            ///DEBUG: checkEglError("eglMakeCurrent");
        }

        /**
         * Calls eglSwapBuffers.  Use this to "publish" the current frame.
         */
        public boolean swapBuffers() {
            boolean result = EGL14.eglSwapBuffers(mEGLDisplay, mEGLSurface);
            ///DEBUG: checkEglError("eglSwapBuffers");
            return result;
        }

        /**
         * Sends the presentation time stamp to EGL.  Time is expressed in nanoseconds.
         */
        public void setPresentationTime(long nsecs) {
            EGLExt.eglPresentationTimeANDROID(mEGLDisplay, mEGLSurface, nsecs);
            ///DEBUG: checkEglError("eglPresentationTimeANDROID");
        }

        /**
         * Checks for EGL errors.  Throws an exception if one is found.
         */
        private void checkEglError(String msg) {
            int error;
            if ((error = EGL14.eglGetError()) != EGL14.EGL_SUCCESS) {
                throw new RuntimeException(msg + ": EGL error: 0x" + Integer.toHexString(error));
            }
        }
    }

    protected static class TPreviewSurface {
    	
        private Surface mSurface;
        //.
        private int Width;
        private int Height;
        //.
        private EGLDisplay 	mEGLDisplay;
        private EGLConfig[] mEGLConfigs;
        private EGLContext 	mEGLContext;
        //.
        private EGLSurface mEGLSurface = EGL14.EGL_NO_SURFACE;

        /**
         * Creates a CodecInputSurface from a Surface.
         */
        public TPreviewSurface(Surface surface, int pWidth, int pHeight, EGLDisplay pEGLDisplay, EGLConfig[] pEGLConfigs, EGLContext pEGLContext) {
            if (surface == null) {
                throw new NullPointerException();
            }
            mSurface = surface;
            //.
            Width = pWidth;
            Height = pHeight;
            //.
            mEGLDisplay = pEGLDisplay;
            mEGLConfigs = pEGLConfigs;
            mEGLContext = pEGLContext;
            //. Create a window surface, and attach it to the Surface we received.
            int[] surfaceAttribs = {
                    EGL14.EGL_NONE
            };
            mEGLSurface = EGL14.eglCreateWindowSurface(mEGLDisplay, mEGLConfigs[0], mSurface, surfaceAttribs, 0);
            checkEglError("eglCreateWindowSurface");
        }

        public void release() {
        	EGL14.eglDestroySurface(mEGLDisplay, mEGLSurface);
            //.
            mEGLSurface = EGL14.EGL_NO_SURFACE;
            //.
            mSurface = null;
        }

        /**
         * Makes our EGL context and surface current.
         */
        public void makeCurrent() {
            EGL14.eglMakeCurrent(mEGLDisplay, mEGLSurface, mEGLSurface, mEGLContext);
        	GLES20.glViewport(0,0, Width,Height);
            ///DEBUG: checkEglError("eglMakeCurrent");
        }

        /**
         * Calls eglSwapBuffers.  Use this to "publish" the current frame.
         */
        public boolean swapBuffers() {
            boolean result = EGL14.eglSwapBuffers(mEGLDisplay, mEGLSurface);
            ///DEBUG: checkEglError("eglSwapBuffers");
            return result;
        }

        /**
         * Sends the presentation time stamp to EGL.  Time is expressed in nanoseconds.
         */
        public void setPresentationTime(long nsecs) {
            EGLExt.eglPresentationTimeANDROID(mEGLDisplay, mEGLSurface, nsecs);
            ///DEBUG: checkEglError("eglPresentationTimeANDROID");
        }

        /**
         * Checks for EGL errors.  Throws an exception if one is found.
         */
        private void checkEglError(String msg) {
            int error;
            if ((error = EGL14.eglGetError()) != EGL14.EGL_SUCCESS) {
                throw new RuntimeException(msg + ": EGL error: 0x" + Integer.toHexString(error));
            }
        }
    }

    /**
     * Manages a SurfaceTexture.  Creates SurfaceTexture and TextureRender objects, and provides
     * functions that wait for frames and render them to the current EGL surface.
     * <p>
     * The SurfaceTexture can be passed to Camera.setPreviewTexture() to receive camera output.
     */
    protected static class TSurfaceTextureManager implements SurfaceTexture.OnFrameAvailableListener {
    	
    	private SurfaceTexture mSurfaceTexture;
        private TSTextureRender mTextureRender;

        private Object mFrameSyncObject = new Object();     // guards mFrameAvailable
        private boolean mFrameAvailable;

        /**
         * Creates instances of TextureRender and SurfaceTexture.
         */
        public TSurfaceTextureManager() {
            mTextureRender = new TSTextureRender();
            mTextureRender.surfaceCreated();

            mSurfaceTexture = new SurfaceTexture(mTextureRender.getTextureId());

            // This doesn't work if this object is created on the thread that CTS started for
            // these test cases.
            //
            // The CTS-created thread has a Looper, and the SurfaceTexture constructor will
            // create a Handler that uses it.  The "frame available" message is delivered
            // there, but since we're not a Looper-based thread we'll never see it.  For
            // this to do anything useful, OutputSurface must be created on a thread without
            // a Looper, so that SurfaceTexture uses the main application Looper instead.
            //
            // Java language note: passing "this" out of a constructor is generally unwise,
            // but we should be able to get away with it here.
            mSurfaceTexture.setOnFrameAvailableListener(this);
        }

        public void release() {
            // this causes a bunch of warnings that appear harmless but might confuse someone:
            //  W BufferQueue: [unnamed-3997-2] cancelBuffer: BufferQueue has been abandoned!
            mSurfaceTexture.release();

            mTextureRender = null;
            mSurfaceTexture = null;
        }

        /**
         * Returns the SurfaceTexture.
         */
        public SurfaceTexture getSurfaceTexture() {
            return mSurfaceTexture;
        }

        /**
         * Replaces the fragment shader.
         */
        public void changeFragmentShader(String fragmentShader) {
            mTextureRender.changeFragmentShader(fragmentShader);
        }

        /**
         * Latches the next buffer into the texture.  Must be called from the thread that created
         * the OutputSurface object.
         */
        public void awaitNewImage() throws InterruptedException {
            final int TIMEOUT_MS = 1000*600; //. seconds

            synchronized (mFrameSyncObject) {
                while (!mFrameAvailable) {
                    // Wait for onFrameAvailable() to signal us.  Use a timeout to avoid
                    // stalling the test if it doesn't arrive.
                    mFrameSyncObject.wait(TIMEOUT_MS);
                    if (!mFrameAvailable) {
                        //. TO-DO: if "spurious wakeup", continue while loop
                        throw new RuntimeException("Camera frame wait timed out");
                    }
                }
                mFrameAvailable = false;
            }

            // Latch the data.
            mTextureRender.checkGlError("before updateTexImage");
            mSurfaceTexture.updateTexImage();
        }

        /**
         * Draws the data from SurfaceTexture onto the current EGL surface.
         */
        public void drawImage() {
            mTextureRender.drawFrame(mSurfaceTexture);
        }

        @Override
        public void onFrameAvailable(SurfaceTexture st) {
            synchronized (mFrameSyncObject) {
                if (mFrameAvailable) {
                    throw new RuntimeException("mFrameAvailable already set, frame could be dropped");
                }
                mFrameAvailable = true;
                mFrameSyncObject.notifyAll();
            }
        }
    }

    /**
     * Code for rendering a texture onto a surface using OpenGL ES 2.0.
     */
    protected static class TSTextureRender {
    	
        private static final int FLOAT_SIZE_BYTES = 4;
        private static final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES;
        private static final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;
        private static final int TRIANGLE_VERTICES_DATA_UV_OFFSET = 3;
        private final float[] mTriangleVerticesData = {
                // X, Y, Z, U, V
                -1.0f, -1.0f, 0, 0.f, 0.f,
                 1.0f, -1.0f, 0, 1.f, 0.f,
                -1.0f,  1.0f, 0, 0.f, 1.f,
                 1.0f,  1.0f, 0, 1.f, 1.f,
        };

        private FloatBuffer mTriangleVertices;

        private static final String VERTEX_SHADER =
                "uniform mat4 uMVPMatrix;\n" +
                "uniform mat4 uSTMatrix;\n" +
                "attribute vec4 aPosition;\n" +
                "attribute vec4 aTextureCoord;\n" +
                "varying vec2 vTextureCoord;\n" +
                "void main() {\n" +
                "    gl_Position = uMVPMatrix * aPosition;\n" +
                "    vTextureCoord = (uSTMatrix * aTextureCoord).xy;\n" +
                "}\n";

        private static final String FRAGMENT_SHADER =
                "#extension GL_OES_EGL_image_external : require\n" +
                "precision mediump float;\n" +      // highp here doesn't seem to matter
                "varying vec2 vTextureCoord;\n" +
                "uniform samplerExternalOES sTexture;\n" +
                "void main() {\n" +
                "    gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
                "}\n";

        private float[] mMVPMatrix = new float[16];
        private float[] mSTMatrix = new float[16];

        private int mProgram;
        private int mTextureID = -12345;
        private int muMVPMatrixHandle;
        private int muSTMatrixHandle;
        private int maPositionHandle;
        private int maTextureHandle;

        public TSTextureRender() {
            mTriangleVertices = ByteBuffer.allocateDirect(
                    mTriangleVerticesData.length * FLOAT_SIZE_BYTES)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer();
            mTriangleVertices.put(mTriangleVerticesData).position(0);

            Matrix.setIdentityM(mSTMatrix, 0);
        }

        public int getTextureId() {
            return mTextureID;
        }

        public void drawFrame(SurfaceTexture st) {
            ///DEBUG: checkGlError("onDrawFrame start");
            st.getTransformMatrix(mSTMatrix);

            // (optional) clear to green so we can see if we're failing to set pixels
            GLES20.glClearColor(0.0f, 1.0f, 0.0f, 1.0f);
            GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

            GLES20.glUseProgram(mProgram);
            ///DEBUG: checkGlError("glUseProgram");

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureID);

            mTriangleVertices.position(TRIANGLE_VERTICES_DATA_POS_OFFSET);
            GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false,
                    TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);
            ///DEBUG: checkGlError("glVertexAttribPointer maPosition");
            GLES20.glEnableVertexAttribArray(maPositionHandle);
            ///DEBUG: checkGlError("glEnableVertexAttribArray maPositionHandle");

            mTriangleVertices.position(TRIANGLE_VERTICES_DATA_UV_OFFSET);
            GLES20.glVertexAttribPointer(maTextureHandle, 2, GLES20.GL_FLOAT, false,
                    TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);
            ///DEBUG: checkGlError("glVertexAttribPointer maTextureHandle");
            GLES20.glEnableVertexAttribArray(maTextureHandle);
            ///DEBUG: checkGlError("glEnableVertexAttribArray maTextureHandle");

            Matrix.setIdentityM(mMVPMatrix, 0);
            GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMVPMatrix, 0);
            GLES20.glUniformMatrix4fv(muSTMatrixHandle, 1, false, mSTMatrix, 0);

            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
            ///DEBUG: checkGlError("glDrawArrays");

            // IMPORTANT: on some devices, if you are sharing the external texture between two
            // contexts, one context may not see updates to the texture unless you un-bind and
            // re-bind it.  If you're not using shared EGL contexts, you don't need to bind
            // texture 0 here.
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        }

        /**
         * Initializes GL state.  Call this after the EGL surface has been created and made current.
         */
        public void surfaceCreated() {
            mProgram = createProgram(VERTEX_SHADER, FRAGMENT_SHADER);
            if (mProgram == 0) {
                throw new RuntimeException("failed creating program");
            }
            maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
            checkLocation(maPositionHandle, "aPosition");
            maTextureHandle = GLES20.glGetAttribLocation(mProgram, "aTextureCoord");
            checkLocation(maTextureHandle, "aTextureCoord");

            muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
            checkLocation(muMVPMatrixHandle, "uMVPMatrix");
            muSTMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uSTMatrix");
            checkLocation(muSTMatrixHandle, "uSTMatrix");

            int[] textures = new int[1];
            GLES20.glGenTextures(1, textures, 0);

            mTextureID = textures[0];
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureID);
            checkGlError("glBindTexture mTextureID");

            GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER,
                    GLES20.GL_NEAREST);
            GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER,
                    GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S,
                    GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T,
                    GLES20.GL_CLAMP_TO_EDGE);
            checkGlError("glTexParameter");
        }

        /**
         * Replaces the fragment shader.  Pass in null to reset to default.
         */
        public void changeFragmentShader(String fragmentShader) {
            if (fragmentShader == null) {
                fragmentShader = FRAGMENT_SHADER;
            }
            GLES20.glDeleteProgram(mProgram);
            mProgram = createProgram(VERTEX_SHADER, fragmentShader);
            if (mProgram == 0) {
                throw new RuntimeException("failed creating program");
            }
        }

        private int loadShader(int shaderType, String source) {
            int shader = GLES20.glCreateShader(shaderType);
            checkGlError("glCreateShader type=" + shaderType);
            GLES20.glShaderSource(shader, source);
            GLES20.glCompileShader(shader);
            int[] compiled = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] == 0) {
                GLES20.glDeleteShader(shader);
                shader = 0;
            }
            return shader;
        }
        
        private int createProgram(String vertexSource, String fragmentSource) {
            int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
            if (vertexShader == 0) {
                return 0;
            }
            int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
            if (pixelShader == 0) {
                return 0;
            }

            int program = GLES20.glCreateProgram();
            GLES20.glAttachShader(program, vertexShader);
            checkGlError("glAttachShader");
            GLES20.glAttachShader(program, pixelShader);
            checkGlError("glAttachShader");
            GLES20.glLinkProgram(program);
            int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] != GLES20.GL_TRUE) {
                GLES20.glDeleteProgram(program);
                program = 0;
            }
            return program;
        }

        public void checkGlError(String op) {
            int error;
            while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
                throw new RuntimeException(op + ": glError " + error);
            }
        }

        public static void checkLocation(int location, String label) {
            if (location < 0) {
                throw new RuntimeException("Unable to locate '" + label + "' in program");
            }
        }    
    }

    
	private class TInputProcessing extends TCancelableThread {
		
		public TInputProcessing() {
    		super();
    		//.
			_Thread = new Thread(this);
			_Thread.start();
		}
		
		public void Destroy() throws InterruptedException {
			CancelAndWait();
		}
		
		@Override
		public void run()  {
			try {
				//. _Thread.setPriority(Thread.MAX_PRIORITY);
				//.
				TInputSurface InputSurface = new TInputSurface(Codec.createInputSurface(), FrameWidth,FrameHeight);
				try {
	                InputSurface.makeCurrent();
					//.
	                TSurfaceTextureManager InputSurfaceTextureManager = new TSurfaceTextureManager();
					try {
						SurfaceTexture InputSurfaceTexture = InputSurfaceTextureManager.getSurfaceTexture();
						//.
			        	SourceCamera.setPreviewTexture(InputSurfaceTexture);
			        	//.
			        	TPreviewSurface _PreviewSurface = null;
						try {
							if (PreviewSurface != null)
								_PreviewSurface = new TPreviewSurface(PreviewSurface, PreviewSurfaceRect.width(),PreviewSurfaceRect.height(), InputSurface.mEGLDisplay, InputSurface.mEGLConfigs, InputSurface.mEGLContext);
							
							//.
							Codec.start();
				        	try {
					        	TOutputProcessing OutputProcessing = new TOutputProcessing();
					        	try {
					        		try {
					        			SignalOfInitialization(); //. fire initialization is done signal
					        			//.
							            while (!Canceller.flCancel) {
							                InputSurfaceTextureManager.awaitNewImage();
							                //.
							                InputSurfaceTextureManager.drawImage();
							                //. set a frame timestamp
							                InputSurface.setPresentationTime(System.nanoTime());
							                //. do encoding
							                InputSurface.swapBuffers();
							                //. do previewing
							                if (_PreviewSurface != null) {
							                	_PreviewSurface.makeCurrent();
								                InputSurfaceTextureManager.drawImage();
								                _PreviewSurface.swapBuffers();
							                	//. restore encoding surface
								                InputSurface.makeCurrent();
							                }
							            }
					        		}
					        		catch (InterruptedException IE) {
							            //. send end-of-stream to encoder
							            Codec.signalEndOfInputStream();
							            //.
							            return; //. ->
					        		}
						            //. send end-of-stream to encoder
						            Codec.signalEndOfInputStream();
					        	}
					        	finally {
					        		OutputProcessing.Destroy();
					        	}
				        	}
				        	finally {
				    			Codec.stop();
				        	}
						}
						finally {
							if (_PreviewSurface != null)
								_PreviewSurface.release();
						}
					}
					finally {
						InputSurfaceTextureManager.release();
					}
				}
				finally {
					InputSurface.release();
				}
			}
			catch (Throwable T) {
				//. fire initialization is done signal to unblock the waiting threads
    			try {
					SignalOfInitialization();
				} catch (InterruptedException IE) {
				} 
    			//.
				String S = T.getMessage();
				if (S == null)
					S = T.getClass().getName();
				VideoModule.Device.Log.WriteError("VideoRecorderModule.H264EncoderServer.InputProcessing",S);
			}
		}
	}
	
	private class TOutputProcessing extends TCancelableThread {
		
		public TOutputProcessing() {
    		super();
    		//.
			_Thread = new Thread(this);
			_Thread.start();
		}
		
		public void Destroy() throws InterruptedException {
			CancelAndWait();
		}
		
		@Override
		public void run()  {
			try {
				//. _Thread.setPriority(Thread.MAX_PRIORITY);
				//.
				ByteBuffer[] 	OutputBuffers = Codec.getOutputBuffers();
				byte[] 			OutData = new byte[0];
				int 			OutputBufferCount = 0;
				//.
				MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
				while (!Canceller.flCancel) {
					int outputBufferIndex = Codec.dequeueOutputBuffer(bufferInfo, CodecLatency);
					while (outputBufferIndex >= 0) {
						ByteBuffer outputBuffer = OutputBuffers[outputBufferIndex];
						if (OutData.length < bufferInfo.size)
							OutData = new byte[bufferInfo.size];
						outputBuffer.rewind(); //. reset position to 0
						outputBuffer.get(OutData, 0,bufferInfo.size);
						//. process output
						if (OutputBufferCount == 0) 
							synchronized (Clients) {
								Parameters = new byte[bufferInfo.size];
								System.arraycopy(OutData,0, Parameters,0, bufferInfo.size);
								//.
								Clients_DoOnParameters(Parameters,Parameters.length);
							}
						else
							Clients_DoOnOutputBuffer(OutData,bufferInfo.size, bufferInfo.presentationTimeUs,((bufferInfo.flags & MediaCodec.BUFFER_FLAG_SYNC_FRAME) > 0));
						OutputBufferCount++;
						//.
						Codec.releaseOutputBuffer(outputBufferIndex, false);
						//.
						outputBufferIndex = Codec.dequeueOutputBuffer(bufferInfo, 0);
					}
					if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) 
					     OutputBuffers = Codec.getOutputBuffers();
					else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
					     // Subsequent data will conform to new format.
					     ///? MediaFormat format = codec.getOutputFormat();
					}
					//. change the BitRate on the fly
					if (Codec_Bitrate != Codec_CurrentBitrate) {
						int Bitrate = Codec_Bitrate;
						//.
						Bundle params = new Bundle();
						params.putInt(MediaCodec.PARAMETER_KEY_VIDEO_BITRATE, Bitrate);
						Codec.setParameters(params);
						//.
						Codec_CurrentBitrate = Bitrate;
					}
				}
			}
			catch (Throwable T) {
				String S = T.getMessage();
				if (S == null)
					S = T.getClass().getName();
				VideoModule.Device.Log.WriteError("VideoRecorderModule.H264EncoderServer.OutputProcessing",S);
			}
		}
	}
	
	private TVideoModule VideoModule;
	//.
	private android.hardware.Camera SourceCamera; 
	//.
	private int FrameWidth;
	private int FrameHeight;
	private int BitRate;
	private int FrameRate;
	//.
	private ArrayList<TClient> Clients; 
	//.
	private Surface PreviewSurface;
	private Rect 	PreviewSurfaceRect;
	//.
	private MediaCodec 		Codec;
	private int 			Codec_CurrentBitrate;
	private volatile int	Codec_Bitrate;
	//.
	private TInputProcessing		InputProcessing = null;
	//.
	private Object InitializationSignal = new Object();
	//.
	public byte[] 	Parameters = null;
	
	public TH264EncoderServer(TVideoModule pVideoModule, android.hardware.Camera pSourceCamera, int pFrameWidth, int pFrameHeight, int pBitRate, int pFrameRate, ArrayList<TClient> pClients, Surface pPreviewSurface, Rect pPreviewSurfaceRect) {
		VideoModule = pVideoModule;
		SourceCamera = pSourceCamera;
		FrameWidth = pFrameWidth;
		FrameHeight = pFrameHeight;
		BitRate = pBitRate;
		FrameRate = pFrameRate;
		Clients = pClients;
		PreviewSurface = pPreviewSurface;
		PreviewSurfaceRect = pPreviewSurfaceRect;
		//.
		Codec = MediaCodec.createEncoderByType(CodecTypeName);
		Codec_CurrentBitrate = BitRate;
		Codec_Bitrate = Codec_CurrentBitrate;
		//.
		MediaFormat format = MediaFormat.createVideoFormat(CodecTypeName, FrameWidth,FrameHeight);
		format.setInteger(MediaFormat.KEY_FRAME_RATE, FrameRate);
		format.setInteger(MediaFormat.KEY_BIT_RATE, Codec_CurrentBitrate);
		format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, Encoding_IFRAMEInterval);
		format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
		//.
		Codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
		//.
		InputProcessing = new TInputProcessing();
	}
 
	public TH264EncoderServer(TVideoModule pVideoModule, android.hardware.Camera pSourceCamera, int pFrameWidth, int pFrameHeight, int pBitRate, int pFrameRate, ArrayList<TClient> pClients) {
		this(pVideoModule, pSourceCamera, pFrameWidth,pFrameHeight, pBitRate, pFrameRate, pClients, null,null);
	}
	
	public void Destroy() throws IOException, InterruptedException {
		if (InputProcessing != null) {
			InputProcessing.Destroy();
			InputProcessing = null;
		}
		//.
		if (Codec != null) {
			Codec.release();
			Codec = null;
		}
	}
 
	private void SignalOfInitialization() throws InterruptedException {
		synchronized (InitializationSignal) {
			InitializationSignal.notifyAll();
		}
	}
	
	public void WaitForInitialization() throws InterruptedException {
		synchronized (InitializationSignal) {
			InitializationSignal.wait();
		}
	}
	
	public boolean AreParametersTheSame(int pFrameWidth, int pFrameHeight, int pBitRate, int pFrameRate) {
		return ((FrameWidth == pFrameWidth) && (FrameHeight == pFrameHeight) && (BitRate == pBitRate) && (FrameRate == pFrameRate));
	}
	
	public void SetBitrate(int Value) {
		Codec_Bitrate = Value;
	}
	
	public int GetBitrate() {
		return Codec_CurrentBitrate;
	}
	
	private void Clients_DoOnParameters(byte[] Buffer, int BufferSize) throws Exception {
		synchronized (Clients) {
			int Cnt = Clients.size();
			for (int I = 0; I < Cnt; I++) {
				TClient Client = Clients.get(I);
				if (Client.flApplyParameters)
					Client.DoOnConfiguration(Buffer,BufferSize);
			}
		}
	}
	
	private void Clients_DoOnOutputBuffer(byte[] Buffer, int BufferSize, long Timestamp, boolean flSyncFrame) throws Exception {
		synchronized (Clients) {
			int Cnt = Clients.size();
			for (int I = 0; I < Cnt; I++)
				Clients.get(I).DoOnOutputBuffer(Buffer,BufferSize, Timestamp, flSyncFrame);
		}
	}
}
