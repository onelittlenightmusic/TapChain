package org.tapchain.core;

import org.tapchain.editor.ITapView;

/**
 * Created by hiro on 2015/05/28.
 */
public interface IConnectHandler<ACTORVIEW extends ITapView, PATHVIEW extends ITapView> {
    void onConnect(ACTORVIEW actorview, PATHVIEW pathview, ACTORVIEW actorview2, LinkType linkType);
}
