package pl.mp107.plugtext.utils;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;

import pl.mp107.plugtext.exceptions.RegexCreatorException;

public abstract class RegexCreatorUtil {

    private static final String TAG = "RegexCreatorUtil";
    public static String createStringRegexFromString(@NonNull String string)
            throws RegexCreatorException {
        return createStringRegexFromString(string, ",", "\\b", "\\b");
    }
    public static String createStringSearchRegexFromString(@NonNull String string)
            throws RegexCreatorException {
        return createStringRegexFromString(string, ",", null, null);
    }

    public static String createStringRegexFromString(
            @NonNull String string, @NonNull String separator, String beforeString, String afterString)
            throws RegexCreatorException {
        ArrayList<String> list = new ArrayList<String>(Arrays.asList(string.split(separator)));
        StringBuilder result = new StringBuilder("");
        if (beforeString != null)
            result.append(beforeString);
        result.append("(");
        for (int i = 0; i < list.size(); i++) {
            result.append(list.get(i));
            result.append("|");
        }
        // Cut out last "|" sign
        if (result.length() > 0) {
            result.setLength(result.length() - 1);
        }
        result.append(")");
        if (afterString != null)
            result.append(afterString);
        //Log.d(TAG, "REGEXCreatorUtil - Input List: " + string);
        //Log.d(TAG, "REGEXCreatorUtil - Created REGEX: " + result.toString());
        return result.toString();
    }
}
