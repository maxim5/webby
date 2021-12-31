package io.webby.demo.model;

class SqlInjections {
    // See also https://www.netsparker.com/blog/web-security/sql-injection-cheat-sheet/
    public static final String[] MALICIOUS_STRING_INPUTS = new String[]{
        "--",
        ";",
        "+",
        "||",
        "admin'--",
        "foo OR 1=1",
        "' OR 1=1"
    };
}
