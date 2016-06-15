package org.tapchain.core;

public class ActorBlueprintManager extends BlueprintManager<Actor> {
	public ActorBlueprintManager(Chain root, Factory<Actor> factory) {
		super(root, factory);
	}

	@Override
	public Blueprint __create(Class<? extends Actor> _cls) {
		return new ActorBlueprint(getChain(), _cls);
	}
	
	@Override
	public BlueprintManager newSession() {
		return new ActorBlueprintManager(getChain(), factory).setOuterInstanceForInner(outer);
	}

    @Override
    public ActorBlueprintManager setLogLevel() {
        if (getRoot() instanceof ActorBlueprint) {
            ((ActorBlueprint) getRoot()).setLog();
        }
        return this;
    }

    @Override
    public <VALUE, INPUT, OUTPUT> ActorBlueprintManager add(final IFilter<VALUE, INPUT, OUTPUT> func, final VALUE init) {
//        add(Filter.FilterSkelton.class, func, init);
        class LocalFilter extends Filter.FilterSkelton<VALUE, INPUT, OUTPUT> {
            public LocalFilter(final IFilter<VALUE, INPUT, OUTPUT> func, final VALUE init) {
                super(func, init);
            }
        }
        addWithOuterObject(LocalFilter.class, this).arg(func, init);
        return this;
    }
    @Override
    public <VALUE, OUTPUT> ActorBlueprintManager add(final IGenerator<VALUE, OUTPUT> generator, final VALUE init) {
//        add(Generator.GeneratorSkelton.class, generator, init);
//        IGenerator<VALUE, OUTPUT> g = val -> generator.generate(val);
        class LocalGenerator extends Generator.GeneratorSkelton<VALUE, OUTPUT> {
            public LocalGenerator(final IGenerator<VALUE, OUTPUT> generator, final VALUE init) {
                super(generator, init);
            }
        }
        addWithOuterObject(LocalGenerator.class, this).arg(generator, init);
//        addWithOuterObject(new Generator.GeneratorSkelton<VALUE, OUTPUT>(generator, init){}.getClass(), this).arg(generator, init);
        return this;
    }
    @Override
    public <VALUE, INPUT> ActorBlueprintManager add(final IConsumer<VALUE, INPUT> consumer, final VALUE init) {
//        add(Consumer.ConsumerSkelton.class, consumer, init);
//        addWithOuterObject(new Consumer.ConsumerSkelton<VALUE, INPUT>(consumer,init){}.getClass(), this);
        class LocalConsumer extends Consumer.ConsumerSkelton<VALUE, INPUT> {
            public LocalConsumer(final IConsumer<VALUE, INPUT> consumer, final VALUE init) {
                super(consumer, init);
            }
        }
        addWithOuterObject(LocalConsumer.class, this).arg(consumer, init);
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
