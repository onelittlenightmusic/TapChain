package org.tapchain.editor.view;

import org.tapchain.ColorLib;
import org.tapchain.core.Actor;
import org.tapchain.core.IPiece;
import org.tapchain.core.IPoint;
import org.tapchain.core.IValue;
import org.tapchain.editor.IActorEditor;
import org.tapchain.editor.ITap;
import org.tapchain.editor.TapChainEditor;
import org.tapchain.editor.controller.IActorTap;

/**
 * Created by hiro on 2015/09/03.
 */
public interface IActorTapView extends ITap<IActorEditor, Actor, IActorTap>, IPiece<Actor> {
}
