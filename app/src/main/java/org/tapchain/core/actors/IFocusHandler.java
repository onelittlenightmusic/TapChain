package org.tapchain.core.actors;

import android.content.res.Resources;

import org.tapchain.editor.IFocusable;
import org.tapchain.core.ClassEnvelope;
import org.tapchain.core.LinkType;
import org.tapchain.editor.IFocusControl;
import org.tapchain.editor.ITapView;

/**
 * Created by hiro on 2015/11/24.
 */
public interface IFocusHandler<VIEW extends ITapView> {
    IFocusControl getFocusControl();
    void resetSpot();
    Resources getResources();
    void changeFocus(LinkType al, IFocusable spot, ClassEnvelope clazz);

    void addFocus(VIEW t);

}
