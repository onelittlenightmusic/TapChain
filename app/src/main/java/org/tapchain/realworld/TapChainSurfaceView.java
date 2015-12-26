package org.tapchain.realworld;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PointF;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import org.tapchain.PaletteSort;
import org.tapchain.core.IPoint;
import org.tapchain.core.WorldPoint;
import org.tapchain.editor.IWindow;
import org.tapchain.editor.TapChainEditor;
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
    String log = "";
    TextPaint mTextPaint = new TextPaint();
    StaticLayout mTextLayout;
    static final int NONE = 0;
    static final int ZOOM = 1;
    static final int CAPTURED = 2;
    static final String TAG = "ACTION";
    int mode = NONE;
    float oldDist = 0f;
    Matrix savedMatrix = new Matrix();
    PointF mid = new PointF();
    Point size = new Point();

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
        mTextLayout = new StaticLayout("", mTextPaint, 500, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
        setFocusable(true);
        requestFocus();
    }

    public void onDraw() {
        ((Activity) getContext()).runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Canvas canvas = null;
                try {
                    canvas = getHolder().lockCanvas();
                    if (canvas == null) {
                        return;
                    }
                    paintBackground(canvas);
                    myDraw(canvas);
//						canvas.drawText("Log = "+log, 20, 120, paint_text);
                    canvas.save();
                    canvas.translate(20, 120);
                    mTextLayout.draw(canvas);
                    canvas.restore();
                } finally {
                    if (canvas != null)
                        getHolder().unlockCanvasAndPost(canvas);
                }

            }

        });
    }

    public abstract void myDraw(Canvas canvas);

    StringBuilder buf = new StringBuilder();

    public void log(String... strings) {
        for (String s : strings) {
            buf.append(s);
        }
        buf.append("\n");
        if (buf.length() > 300)
            buf.delete(0, buf.length() - 300);
        log = buf.toString();
        mTextLayout = new StaticLayout(log, mTextPaint, 500, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
    }

    public void paintBackground(Canvas canvas) {
        return;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        window_size.x = getWidth();
        window_size.y = getHeight();
        getEditor().kickTapDraw(null);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        window_size.x = getWidth();
        window_size.y = getHeight();
        getEditor().kickTapDraw(null);
    }

    public abstract TapChainEditor getEditor();


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

    @Override
    public void move(float vx, float vy) {
        IPoint v = getScreenVector(-vx, -vy);
        matrix.postTranslate(v.x(), v.y());
        matrix.invert(inverse);
    }

    @Override
    public boolean isInWindow(float x, float y) {
        IPoint d = getScreenPosition(x, y);
        if (size.x < d.x() || 0 > d.x() || size.y < d.y() || 0 > d.y()) {
            return false;
        }
        return true;
    }

    @Override
    public IPoint getMiddlePoint() {
        return getPosition(size.x / 2f, size.y / 2f);
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
