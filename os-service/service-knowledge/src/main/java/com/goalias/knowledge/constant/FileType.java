package com.goalias.knowledge.constant;

public interface FileType {
    String TXT = "txt";
    String CSV = "csv";
    String MD = "md";
    String DOC = "doc";
    String DOCX = "docx";
    String PDF = "pdf";
    String XLS = "xls";
    String XLSX = "xlsx";

    String LOG = "log";
    String XML = "xml";
    String JSON = "json";

    String JAVA = "java";
    String HTML = "html";
    String HTM = "htm";
    String CSS = "css";
    String JS = "js";
    String PY = "py";
    String CPP = "cpp";
    String SQL = "sql";
    String PHP = "php";
    String RUBY = "ruby";
    String C = "c";
    String H = "h";
    String HPP = "hpp";
    String SWIFT = "swift";
    String TS = "ts";
    String RUST = "rs";
    String PERL = "perl";
    String SHELL = "shell";
    String BAT = "bat";
    String CMD = "cmd";

    String PROPERTIES = "properties";
    String INI = "ini";
    String YAML = "yaml";
    String YML = "yml";

    static boolean isTextFile(String type){
        return type.equalsIgnoreCase(TXT) || type.equalsIgnoreCase(PROPERTIES)
                || type.equalsIgnoreCase(INI) || type.equalsIgnoreCase(YAML) || type.equalsIgnoreCase(YML)
                || type.equalsIgnoreCase(LOG) || type.equalsIgnoreCase(XML) || type.equalsIgnoreCase(JSON);
    }

    static boolean isCodeFile(String type){
        return type.equalsIgnoreCase(JAVA) || type.equalsIgnoreCase(HTML) || type.equalsIgnoreCase(HTM) || type.equalsIgnoreCase(JS) || type.equalsIgnoreCase(PY)
                || type.equalsIgnoreCase(CPP) || type.equalsIgnoreCase(SQL) || type.equalsIgnoreCase(PHP) || type.equalsIgnoreCase(RUBY)
                || type.equalsIgnoreCase(C) || type.equalsIgnoreCase(H) || type.equalsIgnoreCase(HPP) || type.equalsIgnoreCase(SWIFT)
                || type.equalsIgnoreCase(TS) || type.equalsIgnoreCase(RUST) || type.equalsIgnoreCase(PERL) || type.equalsIgnoreCase(SHELL)
                || type.equalsIgnoreCase(BAT) || type.equalsIgnoreCase(CMD) || type.equalsIgnoreCase(CSS);
    }

    static boolean isMdFile(String type){
        return type.equalsIgnoreCase(MD);
    }

    static boolean isWord(String type){
        return type.equalsIgnoreCase(DOC) || type.equalsIgnoreCase(DOCX);
    }

    static boolean isPdf(String type){
        return type.equalsIgnoreCase(PDF);
    }

    static boolean isExcel(String type){
        return type.equalsIgnoreCase(XLS) || type.equalsIgnoreCase(XLSX) || type.equalsIgnoreCase(CSV);
    }

}
