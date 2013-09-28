package me.sheimi.sgit.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sheimi on 8/23/13.
 */
public class CodeUtils {

    private final static String[][] FILENAME_EXTENSION_ARRAY = {
            {"Python", "python", "py"},
            {"Ruby", "ruby", "rb"},
            {"Perl", "perl", "pl"},
            {"PHP", "php", "php"},
            {"Scala", "scala", "scala"},
            {"Go", "go", "go"},
            {"XML", "xml", "xml", "html", "htm"},
            {"Markdown", "markdown", "md", "markdown"},
            {"CSS", "css", "css"},
            {"JSON", "json", "json"},
            {"JavaScript", "javascript", "js"},
            {"CoffeeScript", "coffeescript", "coffee"},
            {"Lua", "lua", "lua"},
            {"AppleScript", "applescript", "scpt", "AppleScript", "applescript"},
            {"Delphi", "delphi", "p", "pp", "pas"},
            {"Java", "java", "java"},
            {"C++/C", "cpp", "h", "hpp", "cpp", "c"},
            {"Objective-C", "objectivec", "m"},
            {"Vala", "vala", "vala", "vapi"},
            {"C#", "cs", "cs"},
            {"D", "d", "d"},
            {"SQL", "sql", "sql"},
            {"Smalltalk", "smalltalk", "st"},
            {"Lisp", "lisp", "lisp", "lsp", "el", "cl", "jl", "L", "emacs", "sawfishrc"},
            {"Bash", "bash", "bash", "sh"},
            {"Ini", "ini", "ini"},
            {"Diff", "diff", "diff", "patch", "rej"},
            {"Haskell", "haskell", "hs", "hs-boot"},
            {"Tex", "tex", "cls", "latex", "tex", "sty", "dtx", "ltx", "bbl"},
            {"Clojure", "clojure", "clj", "cljs"},
            {"Dos Batch", "dos", "bat", "sys"},
            {"Erlang", "erlang", "erl", "hrl", "yaws"},
            {"R", "r", "r"},
            {"ActionScript", "actionscript", "as"},
            {"VBScript", "vbscript", "vbs", "vbe", "wsc"},
            {"Django Templates", "django"},
            {"Http", "http"},
            {"Apache", "apache"},
            {"Nginx", "nginx"},
            {"CMake", "cmake"},
            {"Axapta", "axapta"},
            {"1C", "1c"},
            {"AVR Assembler", "avrasm"},
            {"VHDL", "vhdl"},
            {"Parser 3", "parser3"},
            {"BrainFuck", "brainfuck"},
            {"Rust", "rust"},
            {"Matlab", "matlab"},
            {"GLSL", "glsl"},
            {"MEL", "mel"},
            {"RSL", "rsl"},
            {"RIB", "rib"},
    };

    private static Map<String, String> mFilenameExtensionMap =
            new HashMap<String, String>();
    private static List<String> mSupportLanguageList =
            new ArrayList<String>();

    private static Map<String, String> mDisplayTagMap =
            new HashMap<String, String>();

    static {
        for (int i = 0; i < FILENAME_EXTENSION_ARRAY.length; i++) {
            String[] extensions = FILENAME_EXTENSION_ARRAY[i];
            String display = extensions[0];
            String tag = extensions[1];
            mDisplayTagMap.put(display, tag);
            for (int j = 2; j < extensions.length; j++) {
                mFilenameExtensionMap.put(extensions[j], tag);
            }
        }
        mSupportLanguageList.addAll(mDisplayTagMap.keySet());
        Collections.sort(mSupportLanguageList);
    }

    public static String guessCodeType(String filename) {
        String[] filesplit = filename.split("\\.");
        if (filesplit.length <= 1)
            return null;
        String extension = filesplit[filesplit.length - 1];
        return mFilenameExtensionMap.get(extension);
    }

    public static List<String> getLanguageList() {
        return mSupportLanguageList;
    }

    public static String getLanguageTag(String language) {
        return mDisplayTagMap.get(language);
    }

    public static String wrapUrlScript(String script) {
        return String.format(URL_SCRIPT_WRAPPER, script);
    }

    public final static String URL_SCRIPT_WRAPPER = "javascript:(function(){%s;})()";


}
