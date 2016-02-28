package org.tapchain.core;

public class ActorBlueprintManager extends BlueprintManager<Actor> {
	public ActorBlueprintManager(Factory<Actor> factory) {
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
	public Blueprint create(Class<? extends Actor> _cls) {
		return new ActorBlueprint(_cls);
	}
	
	@Override
	public BlueprintManager newSession() {
		return new ActorBlueprintManager(factory).setOuterInstanceForInner(outer);
	}

    @Override
    public ActorBlueprintManager setLogLevel() {
        if (getRoot() instanceof ActorBlueprint) {
            ((ActorBlueprint) getRoot()).setLog();
        }
        return this;
    }

    @Override
    public <VALUE, INPUT, OUTPUT> ActorBlueprintManager add(final IFunc<VALUE, INPUT, OUTPUT> func, final VALUE init) {
        add(Filter.FilterSkelton.class, func, init);
        return this;
    }
    @Override
    public <VALUE, OUTPUT> ActorBlueprintManager add(final IGenerator<VALUE, OUTPUT> generator, final VALUE init) {
        add(Generator.GeneratorSkelton.class, generator, init);
        return this;
    }
    @Override
    public <VALUE, INPUT> ActorBlueprintManager add(final IConsumer<VALUE, INPUT> consumer, final VALUE init) {
        add(Consumer.ConsumerSkelton.class, consumer, init);
        return this;
    }

    @Override
    public <PARENT, EFFECT> ActorBlueprintManager add(final IEffector<PARENT, EFFECT> effector, final EFFECT init, int duration) {
        add(Effector.EffectorSkelton.class, effector, init, duration);
        return this;
    }
        @Override
    public ActorBlueprintManager save() {
        super.save();
        getBlueprint().register();
        return this;
    }

    @Override
    public ActorBlueprint getBlueprint() {
        return (ActorBlueprint)super.getBlueprint();
    }
}
