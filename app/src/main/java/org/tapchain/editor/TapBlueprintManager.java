package org.tapchain.editor;

import org.tapchain.core.Actor;
import org.tapchain.core.ActorBlueprint;
import org.tapchain.core.ActorBlueprintManager;
import org.tapchain.core.Blueprint;
import org.tapchain.core.Chain;
import org.tapchain.core.Factory;
import org.tapchain.core.TapBlueprint;

/**
 * Created by hiro on 2016/05/23.
 */
public class TapBlueprintManager extends ActorBlueprintManager {
    public TapBlueprintManager(Chain root, Factory<Actor> factory) {
        super(root, factory);
    }

    @Override
    public Blueprint __create(Class<? extends Actor> _cls) {
        return new TapBlueprint(getChain(), _cls);
    }

}
