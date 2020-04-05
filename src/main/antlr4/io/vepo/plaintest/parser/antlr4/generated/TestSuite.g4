grammar TestSuite;

suite
    :     'Suite' IDENTIFIER '{'
            suite*
            step*
            suite*
        '}'
    ;

step
    : IDENTIFIER IDENTIFIER '{'
            attribute*
            assertion*
            attribute*
      '}'
    ;

assertion
    : 'assert' IDENTIFIER ':' value
    ;

attribute
    : IDENTIFIER ':' value
    ;

value
    : NUMBER | MULTILINE_STRING | STRING 
    ;

IDENTIFIER
    : [A-Za-z][A-Za-z0-9]*
    ;

STRING
    : DQUOTE (ESC | ~ ["\\])* DQUOTE
    ;

MULTILINE_STRING
    : DQUOTE DQUOTE DQUOTE (ESC | '"' | ~["\\])* DQUOTE DQUOTE DQUOTE
    ;

fragment DQUOTE
    : '"'
    ;

fragment ESC
    : '\\' (["\\/bfnrt] | UNICODE)
    ;

fragment UNICODE
    : 'u' HEX HEX HEX HEX
    ;

fragment HEX
    : [0-9a-fA-F]
    ;

NUMBER
    : '-'? INT '.' [0-9] + EXP? | '-'? INT EXP | '-'? INT
    ;

fragment INT
    : '0' | [1-9] [0-9]*
    ;
    // no leading zeros

fragment EXP
    : [Ee] [+\-]? INT
    ;
    // \- since - means "range" inside [...]

WS
    :   [ \t\r\n]+ -> skip
    ;
