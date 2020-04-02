grammar TestSuite;

testSuite 
	: 	'TestSuite' IDENTIFIER '{' '}' 
	;

WS
    :   [ \t\r\n]+ -> skip
    ;

IDENTIFIER
    :   [A-Za-z0-9]+
    ;