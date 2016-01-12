package org.tapchain.realworld;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Vibrator;
import android.view.SurfaceHolder;

import org.tapchain.editor.PaletteSort;
import org.tapchain.core.Factory;
import org.tapchain.core.IBlueprintInitialization;
import org.tapchain.editor.IActorTap;
import org.tapchain.editor.TapChainEditor;

/**
 * Created by hiro on 2015/12/29.
 */
public class CanvasViewImpl extends TapChainWritingView {
    Rect r = new Rect();
    private TapChainEditor editor;

    public CanvasViewImpl(Context context) {
        super(context);
        paint.setColor(0xff303030);
        paint.setStyle(Paint.Style.FILL);
    }

    public void setEditor(TapChainEditor editor) {
        this.editor = editor;
    }

    @Override
    public void paintBackground(Canvas canvas) {
        canvas.drawRect(r, paint);
        return;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        int xmax = getWidth(), ymax = getHeight();
        r.set(0, 0, xmax, ymax);
        super.surfaceChanged(holder, format, width, height);
    }

    @Override
    public void myDraw(Canvas canvas) {
        canvas.setMatrix(matrix);
        getEditor().show(canvas);
        getEditor().userShow(canvas);
        canvas.drawText(
                "View = "
                        + Integer.toString(getEditor()
                        .editTap().getChain()
                        .getViewNum()), 20, 20,
                paint_text);
        canvas.drawText(
                "Effect = "
                        + Integer.toString(getEditor()
                        .editTap().getChain()
                        .getPieces().size()), 20,
                40, paint_text);
        canvas.drawText(
                "UserView = "
                        + Integer.toString(getEditor()
                        .edit()
                        .getChain().getViewNum()),
                20, 60, paint_text);
        canvas.drawText(
                "UserEffect = "
                        + Integer.toString(getEditor()
                        .edit()
                        .getChain().getPieces()
                        .size()), 20, 80,
                paint_text);

    }

    public TapChainEditor getEditor() {
        return editor;
    }

    @Override
    public void shake(int interval) {
        Vibrator vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(interval);
    }

    @Override
    public void showPalette(final PaletteSort sort) {
        final MainActivity act = (MainActivity) getContext();
        ((Activity) getContext()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (act.getGrid() != null)
                    act.getGrid().setCurrentFactory(sort.getNum());
            }
        });
    }

    boolean standby = false;

    @Override
    public boolean standbyRegistration(IActorTap selected, int x, int y) {
        final MainActivity act = (MainActivity) getContext();
        GridFragment f1 = act.getGrid();
        if (f1 != null
                && f1.contains(x, y)) {
            if (standby) {
                return true;
            }
            Factory f = act.getEditor().getFactory(f1.getCurrentFactory());
            IBlueprintInitialization i = getEditor().standbyRegistration(f, selected);
            if (i != null) {
                standby = true;
                return true;
            }
        }
        return false;
    }

    public void resetRegistration() {
        standby = false;
    }
}
