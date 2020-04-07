grammar TestSuite;

suite
    :     'Suite' IDENTIFIER '{'
            execDirectory?
            (suite | step)*
        '}'
    ;

step
    : IDENTIFIER IDENTIFIER '{'
            (attribute | assertion)*
      '}'
    ;

execDirectory
	: 'exec-dir' ':' (FILE_PATH | IDENTIFIER)
	;

assertion
    : 'assert' IDENTIFIER VERB value
    ;

attribute
    : IDENTIFIER ':' value
    ;

value
    : NUMBER | MULTILINE_STRING | STRING
    ;

VERB
    : 'Contains'
    | 'Equals'
    ;

IDENTIFIER
    : [A-Za-z][._\-A-Za-z0-9]*
    ;

FILE_PATH
	: WINDOWS_FILE_PATH | UNIX_FILE_PATH
	;

WINDOWS_FILE_PATH
	: ([A-Z] ':\\' (FILENAME '\\')*)? (FILENAME '\\')+ FILENAME?
	;

UNIX_FILE_PATH
	: ('/' (FILENAME '/')*)? (FILENAME '/')+ FILENAME?
	;

STRING
    : DQUOTE (ESC | ~ ["\\])* DQUOTE
    ;

MULTILINE_STRING
    : DQUOTE DQUOTE DQUOTE (ESC | '"' | ~["\\])* DQUOTE DQUOTE DQUOTE
    ;

fragment FILENAME
	: [._\-A-Za-z0-9]+
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
