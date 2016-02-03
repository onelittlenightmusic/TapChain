package org.tapchain.core;

import org.tapchain.editor.ITap;

/**
 * Created by hiro on 2015/05/28.
 */
public interface IConnectHandler<ACTORVIEW extends ITap, PATHVIEW extends ITap> {
    void onConnect(ACTORVIEW actorview, PATHVIEW pathview, ACTORVIEW actorview2, LinkType linkType);
}
