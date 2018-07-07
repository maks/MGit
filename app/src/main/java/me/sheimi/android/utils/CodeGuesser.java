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
            { "APL", "text/apl", "apl" },
            { "Asterisk dialplan", "text/x-asterisk", "conf" },
            { "C", "text/x-csrc", "c", "m" },
            { "C++", "text/x-c++src", "cpp", "cc", "hpp", "hh", "h" },
            { "C#", "text/x-csharp", "cs" },
            { "C-Shell", "application/x-csh", "csh" },
            { "Java", "text/x-java", "java" },
            { "CLIPS", "application/x-msclip", "clp" },
            { "Clojure", "text/x-clojure.", "clj", "cljs" },
            { "COBOL", "text/x-cobol", "cbl" },
            { "CoffeeScript", "text/x-coffeescript", "coffee" },
            { "Lisp", "text/x-common-lisp", "lisp", "lsp", "el", "cl", "jl",
                    "L", "emacs", "sawfishrc" },
            { "CSS", "text/css", "css" },
            { "Scss", "text/x-scss", "scss" },
            { "Sass", "text/x-sass", "sass" },
            { "Less", "text/x-x-less", "less" },
            { "D", "text/x-d", "d" },
            { "Diff", "text/x-diff", "diff", "patch", "rej" },
            { "DTD", "application/xml-dtd" },
            { "ECL", "text/x-ecl", "ecl" },
            { "Eiffel", "text/x-eiffel", "e" },
            { "Erlang", "text/x-erlang", "erl", "hrl", "yaws" },
            { "Fortran", "text/x-Fortran", "f", "for", "f90", "f95" },
            { "Gas", "text/x-gas", "as", "gas" },
            { "Go", "text/x-go", "go" },
            { "Groovy", "text/x-groovy", "groovy", "gvy", "gy", "gsh" },
            { "HAML", "text/x-haml", "haml" },
            { "Haskell", "text/x-haskell", "hs" },
            { "ASP.net", "text/x-aspx", "asp", "aspx" },
            { "JSP", "text/x-jsp", "jsp" },
            { "HTML", "text/html", "html", "htm", "xhtml" },
            { "Jade", "text/x-jade", "jade" },
            { "JavaScript", "text/javascript", "js", "javascript" },
            { "Jinja2", "jinja2" },
            { "LiveScript", "text/x-livescript", "ls" },
            { "Lua", "text/x-lua", "lua" },
            { "Markdown", "text/x-markdown", "md", "markdown" },
            { "Markdown (Github)", "gfm", "md", "markdown" },
            { "Nginx", "text/nginx", "conf" },
            { "OCaml", "text/x-ocaml", "ocaml", "ml", "mli" },
            { "Matlab", "text/x-octave", "fig", "m", "mat" },
            { "Pascal", "text/x-pascal", "p", "pp", "pas" },
            { "PHP", "application/x-httpd-php", "php" },
            { "Pig Latin", "text/x-pig", "pig" },
            { "Perl", "text/x-perl", "pl" },
            { "Ini", "text/x-ini", "ini" },
            { "Properties", "text/x-properties", "properties" },
            { "Python", "text/x-python", "py" },
            //{ "Qt", "text/plain", "pro" },
            { "R", "text/x-rsrc", "r" },
            { "Ruby", "text/x-ruby", "rb" },
            { "Rust", "text/x-rustsrc", "rs" },
            { "Scala", "text/x-scala", "scala" },
            { "Scheme", "text/x-scheme", "scm", "ss" },
            { "Shell", "text/x-sh", "sh", "bash" },
            { "Smalltalk", "text/x-stsrc", "st" },
            { "SQL", "text/x-sql", "sql" },
            { "SVG", "image/svg+xml", "svg" },
            { "TeX", "text/x-stex", "cls", "latex", "tex", "sty", "dtx", "ltx",
                    "bbl" },
            { "VBScript", "text/vbscript", "vbs", "vbe", "wsc" },
            { "XML", "application/xml", "xml" },
            { "Kotlin", "text/x-kotlin", "kt", "kts" },
            { "YAML", "text/x-yaml", "yml", "yaml" }, };

    private static Map<String, String> mFilenameExtensionMap = new HashMap<String, String>();
    private static List<String> mSupportLanguageList = new ArrayList<String>();

    private static Map<String, String> mDisplayTagMap = new HashMap<String, String>();

    static {
        for (int i = 0; i < FILENAME_EXTENSION_ARRAY.length; ++i) {
            String[] extensions = FILENAME_EXTENSION_ARRAY[i];
            String display = extensions[0];
            String tag = extensions[1];
            mDisplayTagMap.put(display, tag);
            for (int j = 2; j < extensions.length; ++j) {
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
