/* Todo:
 *  - Comments justifying and explaining every rule.
 */

lexer grammar CreoleTokens;

/* ***** Headings ***** */

H1   : LINE '=' ;
H2   : LINE '==' ;
H3   : LINE '===' ;
H4   : LINE '====' ;
H5   : LINE '=====' ;
H6   : LINE '======' ;
HEnd : ' '* '='+? ;

/* ***** Lists ***** */

U1 : LINE '@';
U2 : LINE '@@' ;
U3 : LINE '@@@' ;
U4 : LINE '@@@@' ;
U5 : LINE '@@@@@' ;

O1 : LINE '#' ;
O2 : LINE '##' ;
O3 : LINE '###' ;
O4 : LINE '####' ;
O5 : LINE '#####' ;

/* ***** Horizontal Rules ***** */

Rule : LINE '---' '-'+?;

/* ***** Tables ***** */

CellSep : '|' ;
TdStart : '|' ;
ThStart : '|=' ;

/* ***** Inline Formatting ***** */

Bold         : '**' ;
Italic       : '//' ;
Sthrough     : '--' ;
NoWikiInline : '{{{' -> pushMode(PREFORMATTED_INLINE) ;

/* ***** Links ***** */

LiSt  : '[[' -> pushMode(LINK);
ImSt  : '{{' -> pushMode(LINK);

/* ***** Breaks ***** */

InlineBrk : '\\\\' ;

ParBreak  : LineBreak LineBreak+ ;

LineBreak : '\r'? '\n'+? ;

/* ***** Links ***** */

RawUrl    : ALNUM+ ':' ~('['|']'|'"'|'\''|'('|')')+ ~(' '|'['|']'|'"'|'\''|'('|')'|','|'.')+?;

WikiWords : (ALNUM+ ':')? (UPPER ((ALNUM|'.')* ALNUM)*) (UPPER ((ALNUM|'.')* ALNUM)*)+;

/* ***** Plain Text ***** */

NoWiki    : '{{{' -> pushMode(PREFORMATTED);

/* ***** Miscellaneous ***** */

Any : . ;
WS  : (' '|'\t'|'\r'|'\n')+ -> skip ;

fragment LINE  : ({getCharPositionInLine()==0}? WS? | LineBreak WS?);
fragment ALNUM : (ALPHA | DIGIT) ;
fragment ALPHA : (UPPER | LOWER) ;
fragment UPPER : ('A'..'Z') ;
fragment LOWER : ('a'..'z') ;
fragment DIGIT : ('0'..'9') ;

/* ***** Contextual stuff ***** */


mode LINK;

LiEnd : ']]' -> popMode ;
ImEnd : '}}' -> popMode ;

Sep : '|' ;

InLink : ~(']'|'}'|'|')+ ;

mode PREFORMATTED_INLINE;

AnyInlineText : (~('\r'|'\n'|'}')* '}'? ~('\r'|'\n'|'}'))*;

EndNoWikiInline : '}}}' -> popMode ;

mode PREFORMATTED;

AnyText   : ' }}}'
          | .
          ; 

EndNoWiki : '}}}' -> popMode ;