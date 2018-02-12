package pl.mp107.plugtext.utils;

import android.content.res.Resources;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

import pl.mp107.plugtext.constants.ApplicationPluginApiVersions;
import pl.mp107.plugtext.R;
import pl.mp107.plugtext.constants.ApplicationPluginDefaultValues;
import pl.mp107.plugtext.constants.TextFileApplicationPluginIdentifiers;
import pl.mp107.plugtext.exceptions.ApplicationPluginException;
import pl.mp107.plugtext.exceptions.RegexCreatorException;
import pl.mp107.plugtext.plugins.TextFileApplicationPlugin;

public abstract class TextFileApplicationPluginUtil {

    public static TextFileApplicationPlugin createTextFileApplicationPluginFromString(String content)
            throws ApplicationPluginException {
        Map<String, String> pluginConfig = parseTextIntoMap(content);

        int pluginApiVersionNumber = getPluginApiVersion(pluginConfig);
        if (pluginApiVersionNumber == ApplicationPluginApiVersions.PLUGIN_API_TEXTFILE_V1) {
            String[] authors = getAuthorsList(pluginConfig);
            String name = getName(pluginConfig);
            String description = getDescription(pluginConfig);
            int version = getPluginVersion(pluginConfig);
            try {
                TextFileApplicationPlugin plugin;
                Pattern builtins = getPattern(pluginConfig,
                        TextFileApplicationPluginIdentifiers.PLUGIN_FILE_BUILTINS_LIST,
                        TextFileApplicationPluginIdentifiers.PLUGIN_FILE_BUILTINS_REGEX,
                        ApplicationPluginDefaultValues.PLUGIN_FILE_BUILTINS_REGEX);
                Pattern comments = getPattern(pluginConfig,
                        TextFileApplicationPluginIdentifiers.PLUGIN_FILE_COMMENTS_LIST,
                        TextFileApplicationPluginIdentifiers.PLUGIN_FILE_COMMENTS_REGEX,
                        ApplicationPluginDefaultValues.PLUGIN_FILE_COMMENTS_REGEX);
                Pattern fileExtensions = getPattern(pluginConfig,
                        TextFileApplicationPluginIdentifiers.PLUGIN_FILE_EXTENSIONS_LIST,
                        TextFileApplicationPluginIdentifiers.PLUGIN_FILE_EXTENSIONS_REGEX,
                        ApplicationPluginDefaultValues.PLUGIN_FILE_EXTENSIONS_REGEX);
                Pattern keywords = getPattern(pluginConfig,
                        TextFileApplicationPluginIdentifiers.PLUGIN_FILE_KEYWORDS_LIST,
                        TextFileApplicationPluginIdentifiers.PLUGIN_FILE_KEYWORDS_REGEX,
                        ApplicationPluginDefaultValues.PLUGIN_FILE_KEYWORDS_REGEX);
                Pattern lines = getPattern(pluginConfig,
                        TextFileApplicationPluginIdentifiers.PLUGIN_FILE_LINES_LIST,
                        TextFileApplicationPluginIdentifiers.PLUGIN_FILE_LINES_REGEX,
                        ApplicationPluginDefaultValues.PLUGIN_FILE_LINES_REGEX);
                Pattern numbers = getPattern(pluginConfig,
                        TextFileApplicationPluginIdentifiers.PLUGIN_FILE_LINES_LIST,
                        TextFileApplicationPluginIdentifiers.PLUGIN_FILE_LINES_REGEX,
                        ApplicationPluginDefaultValues.PLUGIN_FILE_LINES_REGEX);
                Pattern preprocessors = getPattern(pluginConfig,
                        TextFileApplicationPluginIdentifiers.PLUGIN_FILE_PREPROCESSORS_LIST,
                        TextFileApplicationPluginIdentifiers.PLUGIN_FILE_PREPROCESSORS_REGEX,
                        ApplicationPluginDefaultValues.PLUGIN_FILE_PREPROCESSORS_REGEX);

                plugin
                        = new TextFileApplicationPlugin(
                        authors, description, name, version,
                        builtins, comments, fileExtensions, keywords,
                        lines, numbers, preprocessors);
                return plugin;
            } catch (RegexCreatorException e) {
                e.printStackTrace();
            }
        } else {
            // Plugin API version mismatch
            throw new ApplicationPluginException(
                    Resources.getSystem().getString(R.string.plugin_api_version_mismatch));
        }
        return null; //TODO - is it ok?
    }

    private static Map<String, String> parseTextIntoMap(String content)
            throws ApplicationPluginException {
        Map<String, String> pluginConfig = new HashMap<String, String>();
        String line, name, value;
        int separatorPosition;
        Scanner scanner = new Scanner(content);
        while (scanner.hasNextLine()) {
            line = scanner.nextLine();
            Log.d("TFAPUtil", "Scaning line: " + line);
            /* If line is not empty and does not start with # (comment) */
            if (!line.isEmpty() && !line.startsWith("#")) {
                separatorPosition = line.indexOf("=");
                if (separatorPosition != -1) {
                    name = line.substring(0, separatorPosition);
                    value = line.substring(separatorPosition + 1);
                    pluginConfig.put(name, value);
                } else {
                    Log.w("TFAPUtil", "");
                    throw new ApplicationPluginException("Error in plugin syntax"/*
                            Resources.getSystem().getString(R.string.plugin_file_parsing_exception)*/);

                }
            }
        }
        scanner.close();
        return pluginConfig;
    }

    private static int getPluginApiVersion(Map<String, String> pluginConfig) {
        return Integer.parseInt(pluginConfig.get(TextFileApplicationPluginIdentifiers.PLUGIN_API_VERSION));
    }

    private static String[] getAuthorsList(Map<String, String> pluginConfig) {
        try {
            String authorsValue = pluginConfig.get(TextFileApplicationPluginIdentifiers.PLUGIN_AUTHOR);
            if (authorsValue.contains(","))
                return authorsValue.split(",");
            return new String[]{authorsValue};
        } catch (NullPointerException e) {
            return new String[]{};
        }
    }

    private static String getName(Map<String, String> pluginConfig) {
        String value = pluginConfig.get(TextFileApplicationPluginIdentifiers.PLUGIN_NAME);
        if (value == null)
            return "";
        return value;
    }

    private static String getDescription(Map<String, String> pluginConfig) {
        String value = pluginConfig.get(TextFileApplicationPluginIdentifiers.PLUGIN_DESCRIPTION);
        if (value == null)
            return "";
        return value;
    }

    private static int getPluginVersion(Map<String, String> pluginConfig) {
        return Integer.parseInt(pluginConfig.get(TextFileApplicationPluginIdentifiers.PLUGIN_VERSION_NUMBER));
    }

    private static Pattern getPattern(Map<String, String> pluginConfig, String listIdentifier,
                                      String regexIdentifier, Pattern defaultPattern)
            throws RegexCreatorException {
        String listValue = pluginConfig.get(listIdentifier);
        String regexValue = pluginConfig.get(regexIdentifier);

        // If regex pattern is not specified in the file, then create it
        if (regexValue == null) {
            if (listValue != null)
                regexValue = RegexCreatorUtil.createStringRegexFromString(listValue);
            else
                return defaultPattern;
        }
        // Else include only it
        return Pattern.compile(regexValue);
    }
}
