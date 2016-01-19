package org.tapchain.editor;

import org.tapchain.core.Actor;
import org.tapchain.core.IPoint;

/**
 * Created by hiro on 2015/05/21.
 */
public interface IActorEditor extends IEditor<Actor, IActorTap> {
    /**
     * Create an actor instance from its blueprint.
     *
     * @param key
     *           the key for the factory in which actor blueprint is registered
     * @param num
     *            the number of the actor blueprint
     * @param pos
     *            position where the actor instance sho0uld be added
     * @return EditorReturn including the created actor instance and the its tap instance
     */
    EditorReturn addActorFromBlueprint(TapChainEditor.FACTORY_KEY key, int num,
                                              IPoint pos);

    /**
     * Create an actor instance from its blueprint.
     * @param key
     *           the key for the factory in which actor blueprint is registered
     * @param tag
     *           the tag of the actor blueprint
     * @param pos
     *           position where the actor instance sho0uld be added
     * @return EditorReturn including the created actor instance and the its tap instance
     */
    EditorReturn addActorFromBlueprint(TapChainEditor.FACTORY_KEY key, String tag,
                                       IPoint pos);
}
