package pl.mp107.plugtext.constants;

import java.util.regex.Pattern;

public abstract class ApplicationPluginDefaultValues {

    /* General plugin attributes */
    public static final String PLUGIN_VERSION_NUMBER = "0";

    /* Syntax describing plugin attributes */
    public static final Pattern PLUGIN_FILE_BUILTINS_REGEX = Pattern.compile(
            "\\b(radians|degrees|sin|cos|tan|asin|acos|atan|pow|" +
                    "exp|log|exp2|log2|sqrt|inversesqrt|abs|sign|floor|ceil|fract|mod|" +
                    "min|max|clamp|mix|step|smoothstep|length|distance|dot|cross|" +
                    "normalize|faceforward|reflect|refract|matrixCompMult|lessThan|" +
                    "lessThanEqual|greaterThan|greaterThanEqual|equal|notEqual|any|all|" +
                    "not|dFdx|dFdy|fwidth|texture2D|texture2DProj|texture2DLod|" +
                    "texture2DProjLod|textureCube|textureCubeLod)\\b");
    public static final Pattern PLUGIN_FILE_COMMENTS_REGEX = Pattern.compile(
            "/\\*(?:.|[\\n\\r])*?\\*/|//.*");
    public static final Pattern PLUGIN_FILE_EXTENSIONS_REGEX = null;
    public static final Pattern PLUGIN_FILE_KEYWORDS_REGEX = Pattern.compile(
            "\\b(attribute|const|uniform|varying|break|continue|" +
                    "do|for|while|if|else|in|out|inout|float|int|void|bool|true|false|" +
                    "lowp|mediump|highp|precision|invariant|discard|return|mat2|mat3|" +
                    "mat4|vec2|vec3|vec4|ivec2|ivec3|ivec4|bvec2|bvec3|bvec4|sampler2D|" +
                    "samplerCube|struct|gl_Vertex|gl_FragCoord|gl_FragColor)\\b");
    public static final Pattern PLUGIN_FILE_LINES_REGEX = Pattern.compile(
            ".*\\n");
    public static final Pattern PLUGIN_FILE_NUMBERS_REGEX = Pattern.compile(
            "\\b(\\d*[.]?\\d+)\\b");
    public static final Pattern PLUGIN_FILE_PREPROCESSORS_REGEX = Pattern.compile(
            "^[\t ]*(#define|#undef|#if|#ifdef|#ifndef|#else|#elif|#endif|" +
                    "#error|#pragma|#extension|#version|#line)\\b",
            Pattern.MULTILINE);
}
