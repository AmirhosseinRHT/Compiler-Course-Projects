grammar utl;

start: prog? EOF;

simpleDec: variableTypes Identifier
    | variableTypes Identifier Assign expression
    | variableTypes listDeclaration Identifier
    ;

dec: Static? simpleDec;

prog: Shared? dec Semi prog
    | function prog
    | mainFunc
    ;

scheduleStmt :Schedule  subSchedule ((Preorder | Parallel) subSchedule)* Semi;

subSchedule : Identifier | LeftParen Identifier (Parallel | Preorder) Identifier RightParen |
    Identifier (Parallel | Preorder) Identifier ;


mainFuncBody : body mainFuncBody? | scheduleStmt mainFuncBody?;

mainFunc: Void Main LeftParen RightParen LeftBrace mainFuncBody RightBrace;

function: funcReturnType funcIdentifiers LeftParen funcDeclarationArgs RightParen (Throw Exception)? LeftBrace body RightBrace;

funcDeclarationArgs: variableTypes Identifier (Comma funcDeclarationArgs)? ;

tryCatch: Try LeftBrace body RightBrace Catch Exception Identifier LeftBrace body RightBrace;

funcIdentifiers: Connect | Observe | RefreshRate | OnStart | OnInit | Terminate | Print | Order | Exception |Identifier;

body: dec Semi body? | conditionStmt body? | forLoop body? | whileLoop body? | funcCall Semi body? | assignStmt Semi body?
    | switchStatement body? | tryCatch body? | returnStmt body? | Throw elements Semi body? | doubleOperatorAssigns Semi body?;

returnStmt: Return (expression | condition) Semi ;

conditionStmt: If LeftParen condition RightParen LeftBrace conditionBody RightBrace (Else LeftBrace conditionBody RightBrace)? ;

conditionBody : body conditionBody? | Continue Semi conditionBody? | Break Semi conditionBody? ;

forLoop: For LeftParen assignStmt Semi condition Semi (assignStmt| doubleOperatorAssigns) RightParen LeftBrace conditionBody RightBrace;

switchStatement: Switch LeftParen elements RightParen LeftBrace (Case elements Colon body (Break Semi)?)* RightBrace;

whileLoop: While LeftParen condition RightParen LeftBrace body RightBrace;

expression : expression nonAssigningOperators expression | LeftParen expression RightParen |elements;

assignStmt : ((Identifier | arrayElement | classAttr)  assigningOperators expression ) | dec | classAttr;

condition : expression comparingOperators expression;

inc_Dec_operator_left : (PlusPlus | MinusMinus ) (Identifier | classAttr | arrayElement );

bitwiseNot :Tilde elements;

logicalNot :Not elements;

inc_Dec_operator_right : (Identifier | classAttr | arrayElement) (PlusPlus | MinusMinus );

doubleOperatorAssigns: inc_Dec_operator_left | inc_Dec_operator_right ;

value : Number | String | FloatVal | BooleanVal;

funcCall : funcIdentifiers LeftParen (elements (Comma elements)*)* RightParen;

arrayElement: Identifier LeftBracket expression RightBracket;

classAttr : (Identifier | arrayElement | funcCall) (Dot (Identifier | arrayElement | funcCall))+ ;

classMethodCall : Identifier (Dot Identifier)* Dot funcCall;

elements : value | arrayElement | classAttr | funcCall |
    logicalNot| bitwiseNot | inc_Dec_operator_left | inc_Dec_operator_right | Identifier | classMethodCall;

nonAssigningOperators : Plus | Minus | Multi | Div | Mod | AndAnd | OrOr | LeftShift | RightShift |
    And | Or | Caret ;

assigningOperators : Assign | PlusAssign | MinusAssign | StarAssign | DivAssign | ModAssign ;

comparingOperators : Equal | NotEqual | GreaterThan | LessThan ;

types: Int| Float| Double| Str| Bool| Trade| Order| Candle| Exception ;

listDeclaration: LeftBracket Number RightBracket ;

variableTypes: types listDeclaration? ;

funcReturnType: types | Void ;

Str: 'string';

Candle : 'Candle' ;

Trade: 'Trade';

Order: 'Order';

Connect: 'Connect';

Observe: 'Observe';

RefreshRate: 'RefreshRate';

OnStart: 'OnStart';

Try: 'try';

Catch : 'catch';

OnInit: 'OnInit';

Terminate: 'terminate';

Main : 'Main' ;

Print : 'Print';

BooleanVal : 'true' | 'false';

Static: 'static';

Parallel : 'parallel';

Preorder : 'preorder';

Schedule: '@schedule';

Return : 'return';

Shared: 'shared';

Void: 'void';

Throw: 'throw';

Exception: 'Exception';

For: 'for';

While: 'while';

Switch: 'switch';

Case: 'case';

If: 'if';

Else: 'else';

Continue: 'continue';

Break: 'break';

Int: 'int';

Float: 'float';

Double: 'double';

Bool: 'bool';

Colon: ':';

Semi: ';';

Dot: '.';

Comma: ',';

LeftParen: '(';

RightParen: ')';

LeftBracket: '[';

RightBracket: ']';

LeftBrace: '{';

RightBrace: '}';

Plus: '+';

Minus: '-';

Multi: '*';

Div: '/';

Mod: '%';

Caret: '^';

And: '&';

Or: '|';

Tilde: '~';

Not: '!';

PlusPlus: '++';

MinusMinus: '--';

Assign: '=';

LeftShift: '<<';

RightShift: '>>';

PlusAssign: '+=';

MinusAssign: '-=';

StarAssign: '*=';

DivAssign: '/=';

ModAssign: '%=';

Equal: '==';

NotEqual: '!=';

LessThan: '<';

GreaterThan: '>';

AndAnd: '&&';

OrOr: '||';

String: '"' ~('"')* '"';

FloatVal : Number Dot Number;

Number: [0-9]+;

Identifier:
	NONDIGIT (NONDIGIT | Number)*;

NONDIGIT: [a-zA-Z_];

Whitespace: [ \t]+ -> skip;

Newline: ('\r' '\n'? | '\n') -> skip;

BlockComment: '/*' .*? '*/' -> skip;

LineComment: '//' ~ [\r\n]* -> skip;