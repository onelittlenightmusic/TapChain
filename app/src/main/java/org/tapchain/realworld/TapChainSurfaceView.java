package org.tapchain.realworld;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.text.DynamicLayout;
import android.text.Layout;
import android.text.TextPaint;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import org.tapchain.editor.PaletteSort;
import org.tapchain.core.IPoint;
import org.tapchain.core.WorldPoint;
import org.tapchain.editor.IWindow;
import org.tapchain.editor.TapChain;
import org.tapchain.game.ISensorView;

import static java.lang.Math.sqrt;

/**
 * Created by hiro on 2015/12/26.
 */
public abstract class TapChainSurfaceView
        // extends TextureView implements
        extends SurfaceView implements SurfaceHolder.Callback,
        IWindow, ISensorView {
    protected GestureDetector gdetect;
    Matrix matrix = new Matrix();
    Matrix inverse = new Matrix();
    Paint paint = new Paint(), paint_text = new Paint();
    WorldPoint window_size = new WorldPoint();
    TextPaint mTextPaint = new TextPaint();
    final DynamicLayout mTextLayout;
    static final int NONE = 0;
    static final int ZOOM = 1;
    static final int CAPTURED = 2;
    static final String TAG = "ACTION";
    int mode = NONE;
    float oldDist = 0f;
    PointF mid = new PointF();
    StringBuilder buf = new StringBuilder();

    public TapChainSurfaceView(Context context) {
        super(context);
//			gdetect = new GestureDetector(context, this);
        SurfaceHolder holder = getHolder();
        holder.setFormat(PixelFormat.TRANSPARENT);
        holder.addCallback(this);
        paint_text.setColor(0xff000000);
        paint_text.setTextSize(20);
        paint.setColor(0xff444444);
        mTextPaint.setTextSize(20);
        mTextLayout = new DynamicLayout(buf, mTextPaint, 500, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
        setFocusable(true);
        requestFocus();
    }

    public void onDraw() {
        ((Activity) getContext()).runOnUiThread(() -> {
            Canvas canvas = null;
            try {
                canvas = getHolder().lockCanvas();
                if (canvas == null) {
                    return;
                }
                paintBackground(canvas);
                myDraw(canvas);
                canvas.save();
                canvas.translate(20, 120);
                synchronized (mTextLayout) {
                    mTextLayout.draw(canvas);
                }
                canvas.restore();
            } finally {
                if (canvas != null)
                    getHolder().unlockCanvasAndPost(canvas);
            }

        });
    }

    public abstract void myDraw(Canvas canvas);

    StringBuffer strBuf = new StringBuffer();
    public void log(String... strings) {
        for(String s: strings)
            strBuf.append(s);
        Log.i("test", strBuf.toString());
        strBuf.setLength(0);
        synchronized(mTextLayout) {
            for (String s : strings) {
                buf.append(s);
            }
            buf.append("\n");
            if (buf.length() > 300)
                buf.delete(0, buf.length() - 300);
        }
    }

    public void paintBackground(Canvas canvas) {
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        window_size.x = getWidth();
        window_size.y = getHeight();
        getTapChain().invalidate();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        window_size.x = getWidth();
        window_size.y = getHeight();
        getTapChain().invalidate();
    }

    public abstract TapChain getTapChain();


    float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) sqrt(x * x + y * y);
    }

    void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }


    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    public void move(float vx, float vy) {
        IPoint v = getScreenVector(-vx, -vy);
        matrix.postTranslate(v.x(), v.y());
        matrix.invert(inverse);
    }

    public boolean isInWindow(float x, float y) {
        IPoint d = getScreenPosition(x, y);
        return !(window_size.x() < d.x() || 0 > d.x() || window_size.y() < d.y() || 0 > d.y());
    }


    @Override
    public IPoint getMiddlePoint() {
        return getPosition(window_size.x / 2f, window_size.y / 2f);
    }

    protected void setMode(int _mode) {
        mode = _mode;
    }

    public IPoint getPosition(float x, float y) {
        return getPosition(x, y, inverse);
    }

    public IPoint getVector(float x, float y) {
        return getVector(x, y, inverse);
    }

    public IPoint getScreenVector(float x, float y) {
        return getVector(x, y, matrix);
    }

    public IPoint getScreenPosition(float x, float y) {
        return getPosition(x, y, matrix);
    }

    public boolean onSecondTouch(final IPoint iPoint) {
        return false;
    }

    @Override
    public void shake(int interval) {
    }

    @Override
    public void showPalette(final PaletteSort sort) {
    }

    @Override
    public void run(final Runnable runnable) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                runnable.run();
                return null;
            }
        }.execute();
    }

    public static IPoint getPosition(float x, float y, Matrix matrix) {
        float[] pos = new float[]{x, y};
        matrix.mapPoints(pos);
        return new WorldPoint(pos[0], pos[1]);

    }

    public static IPoint getVector(float x, float y, Matrix matrix) {
        float[] pos = new float[]{x, y};
        matrix.mapVectors(pos);
        return new WorldPoint(pos[0], pos[1]).setDif();
    }
}
