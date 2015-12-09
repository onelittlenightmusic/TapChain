package org.tapchain;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.tapchain.core.ClassEnvelope;
import org.tapchain.core.IPoint;
import org.tapchain.core.IValue;
import org.tapchain.game.CarEngineer;
import org.tapchain.realworld.R;

import java.util.Calendar;
import java.util.concurrent.ConcurrentHashMap;

public class BitmapMaker {
    static ConcurrentHashMap<String, Bitmap> bitmaps = new ConcurrentHashMap<String, Bitmap>();
    static Activity act;

    //	bitmaps;
    static {
    }

    public BitmapMaker() {
    }

    public static void setActivity(Activity activity) {
        act = activity;
        if (act == null)
            return;
        makeOrReuse("Integer", R.drawable.num);
        makeOrReuse("Float", R.drawable.f123);
        makeOrReuse("IPoint", R.drawable.up);
        makeOrReuse("String", R.drawable.a);
        makeOrReuse("Calendar", R.drawable.clock);
        makeOrReuse("RotationAcceleration", R.drawable.rotation2);
        makeOrReuse("Angle", R.drawable.pedal);
    }

    public static Bitmap makeOrReuse(String str, int resource, int x, int y) {
        if (bitmaps.containsKey(str))
            return bitmaps.get(str);
        Bitmap bitmap = Bitmap.createScaledBitmap(BitmapFactory
                        .decodeResource(act.getResources(), resource), x,
                y, true);
        bitmaps.put(str, bitmap);
        return bitmap;
    }

    public static Bitmap makeOrReuse(String str, int resource) {
        if (bitmaps.containsKey(str))
            return bitmaps.get(str);
        Bitmap bitmap = BitmapFactory.decodeResource(
                act.getResources(), resource);
        bitmaps.put(str, bitmap);
        return bitmap;
    }

    public static Bitmap getClassImage(ClassEnvelope cls, int sizex, int sizey) {
        // Log.w("test_getClassImage", cls.getSimpleName());
        Class<?> _cls = cls.getRawClass();
        if (cls.getRawClass() == IValue.class)
            _cls = cls.getSubclass(0);
        return getClassImage(_cls, sizex, sizey);
    }

    public static Bitmap getClassImage(Class<?> cls, int sizex, int sizey) {
        return makeOrReuse(String.format("%s_%d_%d", cls.getSimpleName(), sizex, sizey), getImageFromClass(cls), sizex, sizey);
    }

    static int getImageFromClass(Class<?> cls) {
        switch (cls.getSimpleName()) {
            case "Integer":
                return R.drawable.num;
            case "Float":
                return R.drawable.f123;
            case "IPoint":
                return R.drawable.up;
            case "String":
                return R.drawable.a;
            case "Calendar":
                return R.drawable.clock;
            case "RotationAcceleration":
                return R.drawable.rotation2;
            case "Angle":
                return R.drawable.pedal;
        }
        return R.drawable.question;
    }
}