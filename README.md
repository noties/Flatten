# Flatten

Flatten json response with this simple library (for those who uses Gson for json deserialization).

## Use case

Given a json:
```json
{
  "first": {
    "second": {
      "third": {
        "forth": {
          fifth: {
            "hello_here_i_am": true
          }
        }
      }
    }
  }
}
```

### The *old* way
Mostly one would end up with a bunch of inner classes:
```java
public static class Response {
    First first;
}

private static class First {
    Second second;
}

private static class Second {
    Third third;
}

private static class Third {
    Forth forth;
}

private static class Forth {
    Fifth fifth;
}

private static class Fifth {
    @SerializedName("hello_here_i_am")
    boolean value;
}
```

and end up with something like this (mileage may vary) to retrive this value:
```java
public boolean extractValue(Response response) {
    if (response != null
            && response.first != null
            && response.first.second != null
            && response.first.second.third != null
            && response.first.second.third.forth != null
            && response.first.second.third.forth.fifth != null) {
        return response.first.second.third.forth.fifth.value;
    }
    return false;
}
```

### The *Flatten* way
Class definition:
```java
private static class Response {
    @Flatten("second::third::forth::fifth::hello_here_i_am")
    @SerializedName("first")
    Flattened<Boolean> value;
}
```

Value retrieval:
```java
private boolean extractValue(Response response) {
    if (response != null
            && response.value != null) {
        return response.value.get();
    }
    return false;
}
```
Off cause it doesn't eliminate *all* the null checks (and in this case (of boxed boolean) it would be wise to additionally call `response.value.hasValue()`), but it's a definite progress.

## Features
* Eliminates the need in classes that are used only to get to the desired value.
* Eliminates a lot of NULL checks
* Eases the pain of migration to other response model
* Supports custom deserialization (if a type wrapped in Flattened<> has registered TypeAdapter it will be deserialized with it)
* Utilizes `Null Object Pattern` for the cases when parser meets a *dead-end* in a parsing way (for example, when `third` is null, then `value` won't be null, but `value.hasValue()` will return `false`)

## Setup
Register type adapter for a `Flattened` type with `FlattenJsonDeserializer` type adapter (pass to it root classes that contain `Flatten` annotations, for example in case of former response it would be `new FlattenJsonDeserializer(Response.class)` )
```java
final Gson gson = new GsonBuilder()
        .registerTypeAdapter(Flattened.class, new FlattenJsonDeserializer(
                MyFirstFlatten.class,
                MySecondFlatten.class,
                MyThirdFlatten.class
        ))
        .create();
```

Use this `gson` to deserialze your objects.

## License

```
  Copyright 2015 Dimitry Ivanov (mail@dimitryivanov.ru)

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
```

