package pl.mp107.plugtext.utils;

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

import pl.mp107.plugtext.exceptions.RegexCreatorException;

public abstract class RegexCreatorUtil {
    public static String createStringRegexFromString(@NonNull String string)
            throws RegexCreatorException {
        return createStringRegexFromString(string, ",");
    }

    public static String createStringRegexFromString(@NonNull String string, @NonNull String separator)
            throws RegexCreatorException {
        ArrayList<String> list = new ArrayList<String>(Arrays.asList(string.split(separator)));
        StringBuilder result = new StringBuilder("\\b(");
        for (int i = 0; i < list.size(); i++) {
            result.append(list.get(i));
            result.append("|");
        }
        // Cut out last "|" sign
        if (result.length() > 0) {
            result.setLength(result.length() - 1);
        }
        result.append(")\\b");
        Log.d("REGEXCreatorUtil", "Input List: " + string);
        Log.d("REGEXCreatorUtil", "Created REGEX: " + result.toString());
        return result.toString();
    }
}
