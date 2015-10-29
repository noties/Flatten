package ru.noties.flatten;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;

import junit.framework.TestCase;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by Dimitry Ivanov on 29.10.2015.
 */
public class FlattenTest extends TestCase {

    private Gson mGson;

    public void testNoClassesPassed() {
        try {
            buildGson();
            assertTrue(false);
        } catch (IllegalStateException e) {
            assertTrue(true);
        }
    }

    public void testClassNoFields() {
        try {
            buildGson(EmptyClass.class);
            assertTrue(false);
        } catch (IllegalStateException e) {
            assertTrue(true);
        }
    }

    public void testElementsNotWrapped() {
        try {
            buildGson(NotWrappedClass.class);
            assertTrue(false);
        } catch (IllegalStateException e) {
            assertTrue(true);
        }
    }

    public void testFlattenedPrimitive() {
        buildGson(FlattenedSinglePrimitive.class);

        final String json = "{ \"i\": { \"first_object\": { \"second_object\": 33 } } }";

        final Flattened<Integer> i = mGson.fromJson(json, FlattenedSinglePrimitive.class).i;

        assertTrue(i != null && i.get() == 33);
    }

    public void testFlattenPrimitiveNullAlongAWay() {

        buildGson(FlattenedSinglePrimitive.class);

        final String json = "{ \"i\": { \"first_object\": null } }";

        final Flattened<Integer> i = mGson.fromJson(json, FlattenedSinglePrimitive.class).i;

        assertFalse(i == null);
        assertTrue(!i.hasValue());
    }

    public void testWithCustomDeserializer() {

        final Gson gson = new GsonBuilder()
                .registerTypeAdapter(Flattened.class, new FlattenJsonDeserializer(FlattenedSinglePrimitive.class))
                .registerTypeAdapter(Integer.class, new JsonDeserializer<Integer>() {
                    @Override
                    public Integer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                        final boolean out = json.getAsBoolean();
                        return out ? 1 : 0;
                    }
                })
                .create();

        final String json = "{ \"i\": { \"first_object\": { \"second_object\": true } } }";
        final Flattened<Integer> i = gson.fromJson(json, FlattenedSinglePrimitive.class).i;

        assertFalse(i == null);
        assertTrue(i.hasValue());
        assertTrue(i.get() == 1);
    }

    public void testElementsOfOneTypeDifferentPaths() {
        buildGson(FlattenedMultiple.class);

        final String json = "{" +
                " \"first\": { \"second\": { \"third\": { \"forth\": { \"fifth\": { \"some_long\": 99, \"some_string\": \"first string ever\" } } } } }," +
                " \"second\": { \"fifth\": { \"forth\": { \"third\": { \"second\": { \"some_long\": 22, \"some_string\": \"second string ever\"  } } } } }" +
                "}";

        final FlattenedMultiple multiple = mGson.fromJson(json, FlattenedMultiple.class);

        final Flattened<SimpleType> first = multiple.first;
        final Flattened<SimpleType> second = multiple.second;

        assertTrue(first != null && second != null);
        assertTrue(first.hasValue() && second.hasValue());

        SimpleType simpleType = first.get();
        assertTrue(simpleType.some_long == 99);
        assertTrue(simpleType.some_string.equals("first string ever"));

        simpleType = second.get();
        assertTrue(simpleType.some_long == 22);
        assertTrue(simpleType.some_string.equals("second string ever"));
    }

    public void testSerializedName() {

        buildGson(FlattenedSerilizedName.class);

        final String json = "{ \"serialized_name\": { \"second\": true } }";

        final Flattened<Boolean> b = mGson.fromJson(json, FlattenedSerilizedName.class).bool;

        assertTrue(b != null && b.hasValue());
        assertTrue(b.get());
    }

    public void testBoxedBoolean() {

        buildGson(FlattenedBoolean.class);

        final String json = "{\"first\":{\"second\":{\"third\":{\"forth\":{fifth:{\"hello_here_i_am\":true}}}}}}";

        final FlattenedBoolean b = mGson.fromJson(json, FlattenedBoolean.class);

        assertTrue(b.value.get());
    }

    public void testList() {

        buildGson(FlattenedList.class);

        final String json = "{\"some\":{\"where\":{\"beyond\":{\"the\":{\"sea\":[0,1,2,3,4,5,6,7,8,9]}}}}}";

        final FlattenedList flattenedList = mGson.fromJson(json, FlattenedList.class);
        assertTrue(String.format("list: %s", flattenedList.list.get()), flattenedList.list != null && flattenedList.list.hasValue());
        assertTrue(String.format("list: %s", flattenedList.list.get()), flattenedList.list.get().size() == 10);
    }

    private void buildGson(Class<?>... classes) {
        mGson = new GsonBuilder()
                .registerTypeAdapter(Flattened.class, new FlattenJsonDeserializer(classes))
                .serializeNulls()
                .create();
    }


    private static class EmptyClass {}

    private static class NotWrappedClass {
        @Flatten("")
        Void v;
    }

    private static class FlattenedSinglePrimitive {

        @Flatten("first_object::second_object")
        Flattened<Integer> i;
    }

    private static class SimpleType {
        private long some_long;
        private String some_string;
    }

    private static class FlattenedMultiple {

        @Flatten("second::third::forth::fifth")
        Flattened<SimpleType> first;

        @Flatten("fifth::forth::third::second")
        Flattened<SimpleType> second;
    }

    private static class FlattenedSerilizedName {

        @Flatten("second")
        @SerializedName("serialized_name")
        Flattened<Boolean> bool;
    }

    private static class FlattenedBoolean {

        @Flatten("second::third::forth::fifth::hello_here_i_am")
        @SerializedName("first")
        Flattened<Boolean> value;
    }

    private static class FlattenedList {

        @Flatten("where::beyond::the::sea")
        @SerializedName("some")
        Flattened<List<Integer>> list;
    }
}
