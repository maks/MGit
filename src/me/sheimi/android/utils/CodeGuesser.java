package me.sheimi.android.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sheimi on 8/23/13.
 */
public class CodeGuesser {

    private final static String[][] FILENAME_EXTENSION_ARRAY = {
            { "Bash", "sh", "bash" },
            { "C", "c", "m" },
            { "C++", "cpp", "cc", "hpp", "hh", "h" },
            { "C#", "cs" },
            { "Clojure", "clj", "cljs" },
            { "CoffeeScript", "coffee" },
            { "Lisp", "lisp", "lsp", "el", "cl", "jl", "L", "emacs", "sawfishrc" },
            { "CSS", "css" },
            { "SCSS", "scss", "sass" },
            { "Less", "less" },
            { "D", "d" },
            { "Diff", "diff", "patch", "rej" },
            { "Erlang", "erl", "hrl", "yaws" },
            { "Fortran", "f", "for", "f90", "f95" },
            { "Go", "go" },
            { "Groovy", "groovy", "gvy", "gy", "gsh" },
            { "HAML", "haml" },
            { "Haskell", "hs" },
            { "ASP.net", "asp", "aspx" },
            { "JSP", "jsp" },
            { "HTML", "text/html", "html", "htm", "xhtml" },
            { "Jade", "jade" },
            { "Java", "java" },
            { "JavaScript", "js" },
            { "LiveScript", "ls" },
            { "Lua", "lua" },
            { "Markdown", "md", "markdown", "gfm" },
            { "Nginx", "conf" },
            { "OCaml", "ocaml", "ml", "mli" },
            { "Matlab", "fig", "m", "mat" },
            { "PHP", "php" },
            { "Perl", "pl" },
            { "INI", "ini" },
            { "Python", "py" },
            { "R", "r" },
            { "Ruby", "rb" },
            { "Rust", "rs" },
            { "Scala", "scala" },
            { "Scheme", "scm", "ss" },
            { "Smalltalk", "st" },
            { "SQL", "sql" },
            { "SVG", "svg" },
            { "TeX", "cls", "latex", "tex", "sty", "dtx", "ltx", "bbl" },
            { "VBScript", "vbs", "vbe", "wsc" },
            { "XML", "xml" },
            // { "YAML", "yml", "yaml" }
    };

    private static Map<String, String> mFilenameExtensionMap = new HashMap<String, String>();
    private static List<String> mSupportLanguageList = new ArrayList<String>();

    static {
        for (int i = 0; i < FILENAME_EXTENSION_ARRAY.length; ++i) {
            String[] extensions = FILENAME_EXTENSION_ARRAY[i];
            String display = extensions[0];
            mSupportLanguageList.add(display);
            for (int j = 1; j < extensions.length; ++j) {
                mFilenameExtensionMap.put(extensions[j], display);
            }
        }
        Collections.sort(mSupportLanguageList);
    }

    public static String guessCodeType(String filename) {
        String[] filesplit = filename.split("\\.");
        if (filesplit.length <= 1)
            return null;
        String extension = filesplit[filesplit.length - 1];
        return normalizeLanguage(mFilenameExtensionMap.get(extension));
    }

    public static List<String> getLanguageList() {
        return mSupportLanguageList;
    }

    public static String normalizeLanguage(String language) {
        if (language != null) {
            language = language
                .toLowerCase()
                .replaceAll("^svg$", "xml")
                .replaceAll("^c$", "cpp")
                .replaceAll("^c\\+\\+$", "cpp")
                .replaceAll("^c#$", "cs");
        }
        return language;
    }

    public static String wrapUrlScript(String script) {
        return String.format(URL_SCRIPT_WRAPPER, script);
    }

    public final static String URL_SCRIPT_WRAPPER = "javascript:(function(){%s;})()";

}
