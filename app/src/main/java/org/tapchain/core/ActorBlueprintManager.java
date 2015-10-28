package org.tapchain.core;

public class ActorBlueprintManager<TYPE extends Actor> extends BlueprintManager<TYPE> {
	public ActorBlueprintManager(Factory<TYPE> factory) {
		super(factory);
	}

	@Override
	public Blueprint create() {
		return new ActorBlueprint();
	}

	@Override
	public Blueprint create(Blueprint bp, Actor... args) {
		return new ActorBlueprint(bp, args);
	}

	@Override
	public Blueprint create(Class<? extends IPiece> _cls) {
		return new ActorBlueprint(_cls);
	}
	
	@Override
	public BlueprintManager newSession() {
		return new ActorBlueprintManager(factory).setOuterInstanceForInner(outer);
	}

    @Override
    public ActorBlueprintManager<TYPE> setLogLevel() {
        if (getRoot() instanceof ActorBlueprint) {
            ((ActorBlueprint) getRoot()).setLog();
        }
        return this;
    }


}
