/* Todo:
 *  - Comments justifying and explaining every rule.
 */

lexer grammar CreoleTokens;

options { superClass=ContextSensitiveLexer; }

@members {
  Formatting bold;
  Formatting italic;
  Formatting strike;

  public void setupFormatting() {
    bold   = new Formatting("**");
    italic = new Formatting("//");
    strike = new Formatting("--");

    inlineFormatting.add(bold);
    inlineFormatting.add(italic);
    inlineFormatting.add(strike);
  }

  public boolean inHeader = false;
  public boolean start = false;
  public int listLevel = 0;
  boolean nowiki = false;
  boolean cpp = false;
  boolean html = false;
  boolean java = false;
  boolean xhtml = false;
  boolean xml = false;
  boolean intr = false;

  public void doHdr() {
    String prefix = getText().trim();
    boolean seekback = false;

    if(!prefix.substring(prefix.length() - 1).equals("=")) {
      prefix = prefix.substring(0, prefix.length() - 1);
      seekback = true;
    }

    if(prefix.length() <= 6) {
      if(seekback) {
        seek(-1);
      }

      setText(prefix);
      inHeader = true;
    } else {
      setType(Any);
    }
  }

  public void setStart() {
    String next1 = next();
    String next2 = get(1);
    start = (next1.equals("*") && !next2.equals("*")) || (next1.equals("#") && !next2.equals("#"));
  }

  public void doList(int level) {
    listLevel = level;

    seek(-1);
    setStart();
    resetFormatting();
  }

  public void doUrl() {
    String url = getText();
    String last = url.substring(url.length()-1);
    String next = next();

    if((last + next).equals("//") || last.equals(".") || last.equals(",")) {
      seek(-1);
      setText(url.substring(0, url.length() - 1));
    }
  }

  public void breakOut() {
    resetFormatting();
    listLevel = 0;
    inHeader = false;
    intr = false;
    nowiki = false;
    cpp = false;
    html = false;
    java = false;
    xhtml = false;
    xml = false;
  }

  public String[] thisKillsTheFormatting() {
    String[] ends = new String[4];

    if(inHeader || intr || listLevel > 0) {
      ends[0] = "\n";
    } else {
      ends[0] = null;
    }

    if(intr) {
      ends[1] = "|";
    } else {
      ends[1] = null;
    }

    ends[2] = "\n\n";
    ends[3] = "\r\n\r\n";

    return ends;
  }
}

/* ***** Headings ***** */

HSt  : LINE '='+ ~'=' WS? {doHdr();} ;
HEnd : ' '* '='* (LineBreak | ParBreak) {inHeader}? {breakOut();} ;

/* ***** Lists ***** */

U1 : START '*' ~'*'                       {doList(1);} ;
U2 : START '**' ~'*'    {listLevel >= 1}? {doList(2);} ;
U3 : START '***' ~'*'   {listLevel >= 2}? {doList(3);} ;
U4 : START '****' ~'*'  {listLevel >= 3}? {doList(4);} ;
U5 : START '*****' ~'*' {listLevel >= 4}? {doList(5);} ;

O1 : START '#' ~'#'                       {doList(1);} ;
O2 : START '##' ~'#'    {listLevel >= 1}? {doList(2);} ;
O3 : START '###' ~'#'   {listLevel >= 2}? {doList(3);} ;
O4 : START '####' ~'#'  {listLevel >= 3}? {doList(4);} ;
O5 : START '#####' ~'#' {listLevel >= 4}? {doList(5);} ;

/* ***** Horizontal Rules ***** */

Rule : LINE '---' '-'+? {breakOut();} ;

/* ***** Tables ***** */

TdStartLn : LINE '|'  {intr=true; setType(TdStart);} ;
ThStartLn : LINE '|=' {intr=true; setType(ThStart);} ;

RowEnd  : '|' LineBreak {intr}? {breakOut();} ;
TdStart : '|'  {intr}? {breakOut(); intr=true;} ;
ThStart : '|=' {intr}? {breakOut(); intr=true;} ;

/* ***** Inline Formatting ***** */

BSt : '**' {!bold.active}?   {setFormatting(bold,   Any);} ;
ISt : '//' {!italic.active}? {setFormatting(italic, Any);} ;
SSt : '--' {!strike.active}? {setFormatting(strike, Any);} ;

BEnd : '**' {bold.active}?   {unsetFormatting(bold);} ;
IEnd : '//' {italic.active}? {unsetFormatting(italic);} ;
SEnd : '--' {strike.active}? {unsetFormatting(strike);} ;

NoWiki     : '{{{'      {nowiki=true;} -> mode(CODE_INLINE) ;
StartCpp   : '[<c++>]'   {cpp=true;}   -> mode(CODE_INLINE) ;
StartHtml  : '[<html>]'  {html=true;}  -> mode(CODE_INLINE) ;
StartJava  : '[<java>]'  {java=true;}  -> mode(CODE_INLINE) ;
StartXhtml : '[<xhtml>]' {xhtml=true;} -> mode(CODE_INLINE) ;
StartXml   : '[<xml>]'   {xml=true;}   -> mode(CODE_INLINE) ;

/* ***** Links ***** */

LiSt  : '[[' -> mode(LINK) ;
ImSt  : '{{' -> mode(LINK) ;

IBLSt : 'ibug:' -> mode(BUG_LINK) ;
EBLSt : 'bug:'  -> mode(BUG_LINK) ;

/* ***** Breaks ***** */

InlineBrk : '\\\\' ;

ParBreak  : LineBreak LineBreak+ {breakOut();} ;

LineBreak : '\r'? '\n' ;

/* ***** Links ***** */

RawUrl    : (('http' 's'? | 'ftp') '://' | 'mailto:') (~(' '|'\t'|'\r'|'\n'|'/'|'|'|'['|']')+ '/'?)+ {doUrl();};

WikiWords : (ALNUM+ ':')? (UPPER ((ALNUM|'.')* ALNUM)+) ((UPPER | DIGIT) ((ALNUM|'.')* ALNUM)*)+;

/* ***** Macros ***** */

MacroSt : '<<' -> mode(MACRO) ;

/* ***** Miscellaneous ***** */

Any : . ;
WS  : (' '|'\t')+ ;

fragment START : {start}? | LINE ;
fragment LINE  : {getCharPositionInLine()==0}? (' '|'\t')*;
fragment ALNUM : (ALPHA | DIGIT) ;
fragment ALPHA : (UPPER | LOWER) ;
fragment UPPER : ('A'..'Z') ;
fragment LOWER : ('a'..'z') ;
fragment DIGIT : ('0'..'9') ;

/* ***** Contextual stuff ***** */

mode LINK;

LiEnd : ']]' -> mode(DEFAULT_MODE) ;
ImEnd : '}}' -> mode(DEFAULT_MODE) ;

Sep : '|' ;

InLink : ~(']'|'}'|'|')+ ;

mode BUG_LINK;

BugNum : DIGIT+ -> mode(DEFAULT_MODE) ;

mode MACRO;

MacroName : ~(':'|'>')+ ;

MacroSep  : ':' -> mode(MACRO_ARGS) ;

mode MACRO_ARGS;

MacroArgs : . -> more ;

MacroEnd  : '>>' -> mode(DEFAULT_MODE) ;

mode CODE_INLINE;

AnyInline : ~('\r'|'\n') -> more;

OopsItsABlock : ('\r'|'\n') -> mode(CODE_BLOCK), more ;

EndNoWikiInline : '}}}' (~'}' {seek(-1);} | EOF) {nowiki}? -> mode(DEFAULT_MODE) ;
EndCppInline   : '[</c++>]'   {cpp}?   {cpp=false;}   -> mode(DEFAULT_MODE) ;
EndHtmlInline  : '[</html>]'  {html}?  {html=false;}  -> mode(DEFAULT_MODE) ;
EndJavaInline  : '[</java>]'  {java}?  {java=false;}  -> mode(DEFAULT_MODE) ;
EndXhtmlInline : '[</xhtml>]' {xhtml}? {xhtml=false;} -> mode(DEFAULT_MODE) ;
EndXmlInline   : '[</xml>]'   {xml}?   {xml=false;}   -> mode(DEFAULT_MODE) ;

mode CODE_BLOCK;

AnyText   : . -> more ;

EndNoWikiBlock : ~' ' '}}}'      {nowiki}? {nowiki=false;} -> mode(DEFAULT_MODE) ;
EndCppBlock    : ~' ' '[</cpp>]'   {cpp}?   {cpp=false;}   -> mode(DEFAULT_MODE) ;
EndHtmlBlock   : ~' ' '[</html>]'  {html}?  {html=false;}  -> mode(DEFAULT_MODE) ;
EndJavaBlock   : ~' ' '[</java>]'  {java}?  {java=false;}  -> mode(DEFAULT_MODE) ;
EndXhtmlBlock  : ~' ' '[</xhtml>]' {xhtml}? {xhtml=false;} -> mode(DEFAULT_MODE) ;
EndXmlBlock    : ~' ' '[</xml>]'   {xml}?   {xml=false;}   -> mode(DEFAULT_MODE) ;