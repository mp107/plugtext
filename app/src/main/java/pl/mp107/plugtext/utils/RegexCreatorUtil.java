package pl.mp107.plugtext.utils;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

import pl.mp107.plugtext.exceptions.RegexCreatorException;

public abstract class RegexCreatorUtil {
    public static String createStringRegexFromString(@NonNull String string)
            throws RegexCreatorException {
        return createStringRegexFromString(string, "|");
    }

    public static String createStringRegexFromString(@NonNull String string, @NonNull String separator)
            throws RegexCreatorException {
        ArrayList<String> list = new ArrayList<String>(Arrays.asList(string.split(separator)));
        StringBuilder result = new StringBuilder("\\b(");
        int lastElementIndex = list.size() - 1;
        for (int i = 0; i < list.size(); i++) {
            result.append(list.get(i));
            if (i != lastElementIndex)
                result.append("|");
        }
        result.append(")\\b");
        return result.toString();
    }
}
