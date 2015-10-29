package ru.noties.gsonflatten;

/**
 * Created by Dimitry Ivanov on 29.10.2015.
 */
class FlattenedImpl<T> implements Flattened<T> {

    static final FlattenedImpl<?> EMPTY = new FlattenedImpl<>(null);

    final T value;

    public FlattenedImpl(T value) {
        this.value = value;
    }

    @Override
    public T get() {
        return value;
    }

    @Override
    public boolean hasValue() {
        return value != null;
    }
}
