package org.tapchain.core.actors;

import org.tapchain.core.Actor;
import org.tapchain.core.ActorChain;
import org.tapchain.core.Chain;
import org.tapchain.core.IActor;
import org.tapchain.core.IPoint;
import org.tapchain.core.IValue;
import org.tapchain.core.Self;
import org.tapchain.core.WorldPoint;

/**
 * Created by hiro on 2015/10/27.
 */
public class ViewActor extends
        Actor.Controllable<Self, Integer, Void, Void> implements IActor,
        ActorChain.IView/* , IPoint */, IValue<IPoint>, ActorChain.IViewCallback {
    protected IPoint _wp = new WorldPoint();
    IPoint back_wp = new WorldPoint();
    IPoint _size = new WorldPoint(30f, 30f);
    IPoint _percent = new WorldPoint(100f, 100f);
    int _Alpha = 255;
    private float _Angle = 0.0f;
    int color = 0;
    ActorChain.IView myView;
    ViewActorSize sizeValue = new ViewActorSize();
    Integer multi = 1;

    // 1.Initialization
    public ViewActor() {
        super();
        setView(this);
        setAutoStart();
    }

    protected void __setPointObject(IPoint obj) {
        _wp = obj;
    }

    // 2.Getters and setters
    public void ctrlStop() {
        removeViewFromAnimation();
        view_end();
    }

    @Override
    public void ctrlStart() throws Chain.ChainException {
        view_init();
        addViewToAnimation();
    }

    public void setView(ActorChain.IView view) {
        myView = view;
    }

    @Override
    public boolean view_user(Object canvas, IPoint iPoint, IPoint size,
                             int alpha, float angle) {
        return true;
    }

    public void view_init() throws Chain.ChainException {
    }

    public void move_user(IPoint iPoint) {
    }

    public ViewActor setSize(IPoint size) {
        _size.set(size);
        return this;
    }

    public ViewActor setPercent(IPoint effectp) {
        _percent = effectp;
        return this;
    }

    @Override
    public ViewActor setCenter(IPoint pos) {
        back_wp = _wp.copy();
        _wp.set(pos);// v= pos;
        move_user(_wp.subNew(back_wp).setDif());
        invalidate();
        return this;
    }


    public final ViewActor addSize(WorldPoint size) {
        setSize(getRawSize().plusNew(size));
        return this;
    }

    public final ViewActor addPercent(WorldPoint effectp) {
        setPercent(getPercent().plusNew(effectp));
        return this;
    }

	public ViewActor setAlpha(int a) {
        _Alpha = a;
        return this;
    }

    public ViewActor setColor(int _color) {
        color = _color;
        return this;
    }

    public IPoint getRawSize() {
        return _size;
    }

    public IValue<IPoint> getSize() {
        return sizeValue;
    }

    public IPoint getPercent() {
        return _percent;
    }

    public IPoint getCenter() {
        return _wp;
    }

    public int getAlpha() {
        return _Alpha;
    }

    public int getColor() {
        return color;
    }

    public void setAngle(float _Angle) {
        this._Angle = _Angle;
    }

    public float getAngle() {
        return _Angle;
    }

    // 3.Changing state
    protected void addViewToAnimation() {
        ((ActorChain) getParentChain()).add(this);
    }

    protected void removeViewFromAnimation() {
        ((ActorChain) getParentChain()).remove(this);
    }

    @Override
    public final boolean view_impl(Object canvas) {
        boolean rtn = true;
        for (int i = 0; i < multi; i++) {
            rtn &= myView.view_user(
                    canvas,
                    getCenter().plusNew(
                            new WorldPoint(50f, 50f).multiplyNew(i)),
                    getRawSize(), getAlpha(), getAngle());
        }
        return rtn;
    }

    @Override
    public boolean _valueSet(IPoint value) {
        setCenter(value);
        return true;
    }

    @Override
    public IPoint _valueGet() {
        return getCenter();
    }

    ;

    public class ViewActorSize extends WorldPoint implements IValue<IPoint> {
        public ViewActorSize() {
            super();
            setOffset(this, false);
        }

        @Override
        public boolean _valueSet(IPoint value) {
            ViewActor.this.setSize(value);
            return true;
        }

        @Override
        public IPoint _valueGet() {
            return ViewActor.this.getRawSize();
        }

    }

    @Override
    public void view_end() {
    }

}
