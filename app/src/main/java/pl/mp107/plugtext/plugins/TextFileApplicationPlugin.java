package pl.mp107.plugtext.plugins;


import java.util.HashMap;
import java.util.regex.Pattern;

public class TextFileApplicationPlugin extends BaseApplicationPlugin {

    public TextFileApplicationPlugin(String[] authors, String description, String name, int version, Pattern patternBuiltins, Pattern patternComments, Pattern patternFileExtensions, Pattern patternKeywords, Pattern patternLines, Pattern patternNumbers, Pattern patternPreprocessors) {
        super(authors, description, name, version, patternBuiltins, patternComments, patternFileExtensions, patternKeywords, patternLines, patternNumbers, patternPreprocessors);
    }
}
