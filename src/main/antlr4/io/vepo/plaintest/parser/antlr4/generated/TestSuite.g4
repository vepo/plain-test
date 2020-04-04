grammar TestSuite;

testSuite 
	: 	'TestSuite' IDENTIFIER '{'
			testSuite*
			testStep*
	    '}' 
	;

testStep 
	: IDENTIFIER IDENTIFIER '{'
			propertyDefinition*	
	  '}'
	;

propertyDefinition
    : IDENTIFIER ':' propertyValue
    ;

propertyValue
	: NUMBER | STRING
	;
	
IDENTIFIER
    : [A-Za-z][A-Za-z0-9]*
    ;

STRING
	: '"' (ESC | ~ ["\\])* '"'
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
