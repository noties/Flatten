package ru.noties.flatten;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Dimitry Ivanov on 29.10.2015.
 */
public class FlattenJsonDeserializer implements JsonDeserializer<Flattened<?>> {

    private final Map<Type, List<FlattenCacheItem>> mCache;

    public FlattenJsonDeserializer(Class<?>... roots) throws IllegalStateException {
        if (roots == null
                || roots.length == 0) {
            throw new IllegalStateException("One must specify at least one class, that contains @Flatten annotation");
        }
        this.mCache = buildCache(roots);
    }

    private static Map<Type, List<FlattenCacheItem>> buildCache(Class<?>... roots) {

        final Map<Type, List<FlattenCacheItem>> cache = new HashMap<>();

        for (Class<?> root: roots) {

            final Field[] fields = root.getDeclaredFields();
            if (fields == null
                    || fields.length == 0) {
                throw new IllegalStateException("Internal error, cannot access any of class fields, class: " + root);
            }

            Flatten flatten;

            Type type;
            String path;

            FlattenCacheItem cacheItem;
            List<FlattenCacheItem> list;

            for (Field field : fields) {

                if (!field.isAnnotationPresent(Flatten.class)) {
                    continue;
                }

                flatten = field.getAnnotation(Flatten.class);
                path = flatten.value();
                type = getType(field.getGenericType());

                if (type == null) {
                    throw new IllegalStateException("Element `" + field.getName() + "` in class: `" + root + "` is not wrapped into `Flattened<>`");
                }

                cacheItem = new FlattenCacheItem(path.split("::"));

                if (cache.containsKey(type)) {
                    cache.get(type).add(cacheItem);
                } else {
                    list = new ArrayList<>();
                    list.add(cacheItem);
                    cache.put(type, list);
                }
            }

        }

        return Collections.unmodifiableMap(cache);
    }

    private static Type getType(Type type) {
        if (type instanceof ParameterizedType) {
            final ParameterizedType parameterizedType = (ParameterizedType) type;
            final Type[] params = parameterizedType.getActualTypeArguments();
            if (params != null
                    && params.length > 0) {
                return params[0];
            }
        }
        return null;
    }

    @Override
    public Flattened<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        final Type type = getType(typeOfT);

        // retrieve cache items fot type
        final List<FlattenCacheItem> items = mCache.get(type);
        if (items == null) {
            return null;
        }

        final JsonObject root = json != null && json.isJsonObject() ? json.getAsJsonObject() : null;
        if (root == null) {
            return FlattenedImpl.EMPTY;
        }

        JsonElement element;

        for (FlattenCacheItem item : items) {

            element = root;

            for (String pathElement : item.path) {

                if (element.isJsonObject()) {
                    element = ((JsonObject) element).get(pathElement);
                }

                if (element == null) {
                    break;
                }
            }

            if (element != null) {
                return new FlattenedImpl<>(context.deserialize(element, type));
            }
        }

        return FlattenedImpl.EMPTY;
    }

    private static class FlattenCacheItem {

        final String[] path;

        private FlattenCacheItem(String[] path) {
            this.path = path;
        }
    }
}
