package me.sheimi.sgit.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by sheimi on 8/23/13.
 */
public class CodeUtils {

    private final static String [][] FILENAME_EXTENSION_ARRAY = {
            {"python", "py"},
            {"ruby", "rb"},
            {"perl", "pl"},
            {"php", "php"},
            {"scala", "scala"},
            {"go", "go"},
            {"xml", "xml", "html", "htm"},
            {"markdown", "md", "markdown"},
            {"css", "css"},
            {"json", "json"},
            {"javascript", "js"},
            {"cooffeescript", "coffee"},
            {"lua", "lua"},
            {"applescript", "scpt", "AppleScript", "applescript"},
            {"delphi", "p", "pp", "pas"},
            {"java", "java"},
            {"cpp", "h", "hpp", "cpp", "c"},
            {"objectivec", "m"},
            {"vala", "vala", "vapi"},
            {"cs", "cs"},
            {"d", "d"},
            {"sql", "sql"},
            {"smalltalk", "st"},
            {"lisp", "lisp", "lsp", "el", "cl", "jl", "L", "emacs", "sawfishrc"},
            {"bash", "bash", "sh"},
            {"ini", "ini"},
            {"diff", "diff", "patch", "rej"},
            {"haskell", "hs", "hs-boot"},
            {"tex", "cls", "latex", "tex", "sty", "dtx", "ltx", "bbl"},
            {"clojure", "clj", "cljs"},
            {"dos", "bat", "sys"},
            {"erlang", "erl", "hrl", "yaws"},
            {"r", "r"},
            {"actionscript", "as"},
            {"vbscript", "vbs", "vbe", "wsc"},

    };

    private static Map<String, String> mFilenameExtensionMap  =
            new HashMap<String, String>();

    static {
        for (int i = 0; i < FILENAME_EXTENSION_ARRAY.length; i++) {
            String[] extensions = FILENAME_EXTENSION_ARRAY[i];
            String tag = extensions[0];
            for (int j = 1; j < extensions.length; j++) {
                mFilenameExtensionMap.put(extensions[j], tag);
            }
        }
    }

    public static String guessCodeType(String filename) {
        String[] filesplit = filename.split("\\.");
        if (filesplit.length <= 1)
            return null;
        String extension = filesplit[filesplit.length - 1];
        return mFilenameExtensionMap.get(extension);
    }

}
