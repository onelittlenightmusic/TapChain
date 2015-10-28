package org.tapchain.core;

import org.tapchain.IFocusable;
import org.tapchain.core.Chain.ChainException;
import org.tapchain.editor.IActorEditor;
import org.tapchain.editor.IEditor;
import org.tapchain.editor.IFocusControl;
import org.tapchain.editor.ITap;
import org.tapchain.editor.IActorTap;
import org.tapchain.editor.TapChainEditor.InteractionType;

import android.content.res.Resources;

public interface IActorSharedHandler extends IGenericSharedHandler<IActorEditor, Actor, IActorTap> {
}