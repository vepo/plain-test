grammar TestSuite;

suiteBody
    : 'Suite' IDENTIFIER '{'
            (attribute | suiteBody | stepBody | typedBody )*
      '}'
    ;

stepBody
    : IDENTIFIER IDENTIFIER '{'
            (attribute | assertion)*
      '}'
    ;

typedBody
	: TYPE '{'
            (suiteBody | stepBody | typedBody | attribute)*
      '}'
	;

assertion
    : 'assert' IDENTIFIER VERB value
    ;

attribute
    : IDENTIFIER ':' (identifierList | value | propertyReference)
    ;

identifierList
	: '[' IDENTIFIER (',' IDENTIFIER)* ']'
	;

propertyReference
    : '${' IDENTIFIER '}'
    ;

value
    : NUMBER | BOOLEAN | FILE_PATH | IDENTIFIER | MULTILINE_STRING | STRING | NULL
    ;

TYPE
	:
		'Parallel' | 'PropertiesSource' | 'Properties'
	;

VERB
    : 'Contains'
    | 'Equals'
    ;

NULL
    : 'null'
    ;
    
BOOLEAN
	: [Tt][Rr][Uu][Ee] | [Ff][Aa][Ll][Ss][Ee];

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
