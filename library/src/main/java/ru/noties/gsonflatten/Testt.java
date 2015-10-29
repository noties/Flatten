package ru.noties.gsonflatten;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Dimitry Ivanov on 29.10.2015.
 */
public class Testt {

//    {
//        "first": {
//        "second": {
//            "third": {
//                "forth": {
//                    fifth: {
//                        "hello_here_i_am": true
//                    }
//                }
//            }
//        }
//    }
//    }

private static class Response {
    @Flatten("second::third::forth::fifth::hello_here_i_am")
    @SerializedName("first")
    Flattened<Boolean> value;
}

private boolean extractValue(Response response) {
    if (response != null
            && response.value != null) {
        return response.value.get();
    }
    return false;
}
//
//public static class Response {
//    First first;
//}
//
//private static class First {
//    Second second;
//}
//
//private static class Second {
//    Third third;
//}
//
//private static class Third {
//    Forth forth;
//}
//
//private static class Forth {
//    Fifth fifth;
//}
//
//private static class Fifth {
//    @SerializedName("hello_here_i_am")
//    boolean value;
//}
//
//public boolean extractValue(Response response) {
//    if (response != null
//            && response.first != null
//            && response.first.second != null
//            && response.first.second.third != null
//            && response.first.second.third.forth != null
//            && response.first.second.third.forth.fifth != null) {
//        return response.first.second.third.forth.fifth.value;
//    }
//    return false;
//}
}
