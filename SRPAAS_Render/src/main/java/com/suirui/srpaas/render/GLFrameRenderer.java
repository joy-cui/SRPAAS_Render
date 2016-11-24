package com.suirui.srpaas.render;

import android.annotation.SuppressLint;
import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import java.nio.ByteBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

@SuppressLint("NewApi")
public class GLFrameRenderer implements Renderer {
    private final static String TAG ="org.suirui.srpass.render.GLFrameRenderer";
    private GLSurfaceView mTargetSurface;
    private GLProgram prog = null;
    private int mScreenWidth, mScreenHeight;
    private Context context;
    public static float[] maxVers;
    int isScale = 0;
    float scaleVer = 1.0f;
    float initVer = 1.0f;
    float[] ScaleVers;
    float scalef = 1.0f;
    float[] transVers;

    private ByteBuffer y = null;
    private ByteBuffer u = null;
    private ByteBuffer v = null;
    private int mVideoWidth = 0, mVideoHeight = 0;

    private String appId = "1";

    public GLFrameRenderer(GLSurfaceView surface, Context context,
                           DisplayMetrics dm) {

        prog = new GLProgram(0, 0);
        mTargetSurface = surface;
        mScreenWidth = dm.widthPixels;
        mScreenHeight = dm.heightPixels;
        this.context = context;

    }

    public GLFrameRenderer(GLSurfaceView surface, Context context) {
        prog = new GLProgram(0, 0);
        mTargetSurface = surface;
        DisplayMetrics dm = getRelDM(context);
        mScreenWidth = dm.widthPixels;
        mScreenHeight = dm.heightPixels;
        this.context = context;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public void setVideoData(int w, int h, ByteBuffer y, ByteBuffer u,
                             ByteBuffer v) {
        this.mVideoWidth = w;
        this.mVideoHeight = h;
        this.y = y;
        this.u = u;
        this.v = v;
    }

    @SuppressLint("NewApi")
    public DisplayMetrics getRelDM(Context context) {
        WindowManager windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Display d = windowManager.getDefaultDisplay();

        DisplayMetrics realDisplayMetrics = new DisplayMetrics();
        d.getMetrics(realDisplayMetrics);
        return realDisplayMetrics;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        if (prog != null && !prog.isProgramBuilt()) {
            prog.buildProgram();
        }
        GLES20.glClearColor(0f, 0f, 0f, 0f);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

    }

    private void draw(GL10 gl) {
        try {
            if (y != null) {
                y.position(0);
                u.position(0);
                v.position(0);
                GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
                GLES20.glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, 1);
                prog.buildTextures(y, u, v, mVideoWidth, mVideoHeight);
                prog.drawFrame();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void onDrawFrame(GL10 gl) {
        synchronized (appId) {
            if (isScale == 2)
                return;
            draw(gl);
        }
    }

    public void setTranScale(float dx, float dy) {
        if (isScale != 0) {
            transVers = getTranVers(dx, dy);
            if (transVers != null) {
                maxVers = transVers;
                if (prog != null)
                    prog.createBuffers(maxVers);
                if (mTargetSurface != null)
                    mTargetSurface.requestRender();
            }
        }

    }

    private float[] getTranVers(float dx, float dy) {
        float x = dx / (mScreenWidth / 2);
        float y = dy / (mScreenHeight / 2);
        float[] vers = null;
        float f1 = 1f * mScreenHeight / mScreenWidth;
        float f2 = 1f * mVideoHeight / mVideoWidth;
        if (f1 == f2) {
            vers = new float[]{(-1.0f + x) * 2 * scalef,
                    (-1.0f - y) * 2 * scalef, (1.0f + x) * 2 * scalef,
                    (-1.0f - y) * 2 * scalef, (-1.0f + x) * 2 * scalef,
                    (1.0f - y) * 2 * scalef, (1.0f + x) * 2 * scalef,
                    (1.0f - y) * 2 * scalef,};
        } else if (f1 < f2) {
            float widScale = f1 / f2;
            vers = new float[]{(-widScale + x) * 2 * scalef,
                    (-1.0f - y) * 2 * scalef, (widScale + x) * 2 * scalef,
                    (-1.0f - y) * 2 * scalef, (-widScale + x) * 2 * scalef,
                    (1.0f - y) * 2 * scalef, (widScale + x) * 2 * scalef,
                    (1.0f - y) * 2 * scalef,};
        } else {
            float heightScale = f2 / f1;
            vers = new float[]{(-1.0f + x) * 2 * scalef,
                    (-heightScale - y) * 2 * scalef, (1.0f + x) * 2 * scalef,
                    (-heightScale - y) * 2 * scalef, (-1.0f + x) * 2 * scalef,
                    (heightScale - y) * 2 * scalef, (1.0f + x) * 2 * scalef,
                    (heightScale - y) * 2 * scalef,};
        }
        return vers;
    }


    private float[] getScaleVers(float scale) {
        float[] vers = null;
        float f1 = 1f * mScreenHeight / mScreenWidth;
        float f2 = 1f * mVideoHeight / mVideoWidth;

        if (f1 == f2) {
            vers = new float[]{-1.0f * 2 * scale, -1.0f * 2 * scale,
                    1.0f * 2 * scale, -1.0f * 2 * scale, -1.0f * 2 * scale,
                    1.0f * 2 * scale, 1.0f * 2 * scale, 1.0f * 2 * scale,};
            scaleVer = 1.0f * 2 * scale;
        } else if (f1 < f2) {
            float widScale = f1 / f2;
            vers = new float[]{-widScale * 2 * scale, -1.0f * 2 * scale,
                    widScale * 2 * scale, -1.0f * 2 * scale,
                    -widScale * 2 * scale, 1.0f * 2 * scale,
                    widScale * 2 * scale, 1.0f * 2 * scale,};
            scaleVer = widScale * 2 * scale;

        } else {
            float heightScale = f2 / f1;
            vers = new float[]{-1.0f * 2 * scale, -heightScale * 2 * scale,
                    1.0f * 2 * scale, -heightScale * 2 * scale,
                    -1.0f * 2 * scale, heightScale * 2 * scale,
                    1.0f * 2 * scale, heightScale * 2 * scale,};
            scaleVer = heightScale * 2 * scale;

        }
        if (scaleVer <= initVer)
            return null;

        return vers;
    }

    public boolean scale(float scale) {
        synchronized (appId) {
            ScaleVers = getScaleVers(scale);
            if (ScaleVers != null) {
                maxVers = ScaleVers;
                if (prog != null)
                    prog.createBuffers(maxVers);
                scalef = scale;
                isScale = 1;
                return true;
            } else
                return false;
        }

    }

    public void refresh() {
        if (mTargetSurface != null)
            mTargetSurface.requestRender();
    }

    public void stopScale() {
        isScale = 2;
    }

    /**
     * this method will be called from native code, it happens when the video is
     * about to play or the video size changes.
     */

    public void update(int w, int h, boolean isSmall) {
        synchronized (appId) {
            if (isScale == 2)
                isScale = 1;
            try {
                if (w > 0 && h > 0) {
                    if (mScreenWidth > 0 && mScreenHeight > 0) {
                        float f1 = 1f * mScreenHeight / mScreenWidth;
                        float f2 = 1f * h / w;
                        if (isSmall) {
                            prog.createBuffers(GLProgram.squareVertices0);
                            initVer = 1.0f;
                        } else {
                            if (f1 == f2) {
                                if (isScale != 0 && maxVers != null) {
                                    prog.createBuffers(maxVers);
                                } else {
                                    prog.createBuffers(GLProgram.squareVertices0);
                                    initVer = 1.0f;
                                }
                            } else if (f1 < f2) {
                                float widScale = f1 / f2;
                                if (isScale != 0 && maxVers != null) {
                                    prog.createBuffers(maxVers);
                                } else {
                                    prog.createBuffers(new float[]{-widScale,
                                            -1.0f, widScale, -1.0f, -widScale,
                                            1.0f, widScale, 1.0f,});
                                    initVer = widScale;

                                }
                            } else {
                                float heightScale = f2 / f1;
                                if (isScale != 0 && maxVers != null) {
                                    prog.createBuffers(maxVers);
                                } else {
                                    prog.createBuffers(new float[]{-1.0f,
                                            -heightScale, 1.0f, -heightScale,
                                            -1.0f, heightScale, 1.0f,
                                            heightScale,});
                                    initVer = heightScale;
                                }

                            }
                        }
                    }

                    if (w != mVideoWidth && h != mVideoHeight) {
                        this.mVideoWidth = w;
                        this.mVideoHeight = h;
                        int yarraySize = w * h;
                        int uvarraySize = yarraySize / 4;
                        synchronized (this) {
                            y = ByteBuffer.allocate(yarraySize);
                            u = ByteBuffer.allocate(uvarraySize);
                            v = ByteBuffer.allocate(uvarraySize);
                        }
                    }

                }
                if (mTargetSurface != null)
                    mTargetSurface.requestRender();

            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     *
     *
     * @param ydata
     * @param udata
     * @param vdata
     */
    public void update(byte[] ydata, byte[] udata, byte[] vdata) {
        synchronized (appId) {
            if (isScale == 2)
                isScale = 1;
            if (y != null)
                y.clear();
            if (u != null)
                u.clear();
            if (v != null)
                v.clear();
            if (ydata != null && y != null)
                y.put(ydata, 0, ydata.length);
            if (udata != null && u != null)
                u.put(udata, 0, udata.length);
            if (vdata != null && v != null)
                v.put(vdata, 0, vdata.length);
        }
        if (mTargetSurface != null)
            mTargetSurface.requestRender();
    }

    public void freeRender() {
        synchronized (this) {
            mTargetSurface = null;
            context = null;
            prog = null;
            if (y != null)
                y.clear();
            if (u != null)
                u.clear();
            if (v != null)
                v.clear();
            y = null;
            u = null;
            v = null;
        }
    }
}
