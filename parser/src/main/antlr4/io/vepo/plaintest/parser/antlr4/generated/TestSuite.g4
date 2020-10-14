grammar TestSuite;

suite
    :     'Suite' IDENTIFIER '{'
            (execDirectory | timesAttribute | execDirectory timesAttribute | timesAttribute execDirectory)?
            ?
            (suite | step | propertiesSource | properties )*
        '}'
    ;

step
    : IDENTIFIER IDENTIFIER '{'
            (attribute | assertion)*
      '}'
    ;

propertiesSource
	: 	'PropertiesSource' '{' 
			(typeAttribute fileAttribute separatorAttribute headersAttribute |
			fileAttribute typeAttribute separatorAttribute headersAttribute |
			typeAttribute separatorAttribute fileAttribute headersAttribute |
			fileAttribute separatorAttribute typeAttribute headersAttribute |
			separatorAttribute typeAttribute fileAttribute headersAttribute |
			separatorAttribute fileAttribute typeAttribute headersAttribute |
			
			typeAttribute fileAttribute headersAttribute separatorAttribute |
			fileAttribute typeAttribute headersAttribute separatorAttribute |
			typeAttribute separatorAttribute headersAttribute fileAttribute |
			fileAttribute separatorAttribute headersAttribute typeAttribute |
			separatorAttribute typeAttribute headersAttribute fileAttribute |
			separatorAttribute fileAttribute headersAttribute typeAttribute |
			
			typeAttribute headersAttribute fileAttribute separatorAttribute |
			fileAttribute headersAttribute typeAttribute separatorAttribute |
			typeAttribute headersAttribute separatorAttribute fileAttribute |
			fileAttribute headersAttribute separatorAttribute typeAttribute |
			separatorAttribute headersAttribute typeAttribute fileAttribute |
			separatorAttribute headersAttribute fileAttribute typeAttribute |
			
			headersAttribute fileAttribute typeAttribute separatorAttribute |
			headersAttribute typeAttribute separatorAttribute fileAttribute |
			headersAttribute fileAttribute separatorAttribute typeAttribute |
			headersAttribute separatorAttribute typeAttribute fileAttribute |
			headersAttribute separatorAttribute fileAttribute typeAttribute) 
		'}'
	;

timesAttribute
	: 'times' ':' NUMBER
	;

properties
    :    'Properties' '{'
             attribute*
         '}'
    ;

execDirectory
	: 'exec-dir' ':' (FILE_PATH | IDENTIFIER)
	;

assertion
    : 'assert' IDENTIFIER VERB value
    ;

separatorAttribute
	: 'separator' ':' STRING
	;

typeAttribute
	: 'type' ':' IDENTIFIER
	;
	
fileAttribute
	: 'file' ':' FILE_PATH
	;
	
headersAttribute
	: 'headers' ':' IDENTIFIER (',' IDENTIFIER)*
	;

attribute
    : IDENTIFIER ':' (value | propertyReference)
    ;

propertyReference
    : '${' IDENTIFIER '}'
    ;

value
    : NUMBER | BOOLEAN | MULTILINE_STRING | STRING | NULL
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
