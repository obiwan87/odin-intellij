// Generated by JFlex 1.9.1 http://jflex.de/  (tweaked for IntelliJ platform)
// source: Odin.flex

package com.lasagnerd.odin.lang;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static com.lasagnerd.odin.lang.psi.OdinTypes.*;


public class OdinLexer implements FlexLexer {

  /** This character denotes the end of file */
  public static final int YYEOF = -1;

  /** initial size of the lookahead buffer */
  private static final int ZZ_BUFFERSIZE = 16384;

  /** lexical states */
  public static final int YYINITIAL = 0;
  public static final int STRING_STATE = 2;

  /**
   * ZZ_LEXSTATE[l] is the state in the DFA for the lexical state l
   * ZZ_LEXSTATE[l+1] is the state in the DFA for the lexical state l
   *                  at the beginning of a line
   * l is of the form l = 2*k, k a non negative integer
   */
  private static final int ZZ_LEXSTATE[] = {
     0,  0,  1, 1
  };

  /**
   * Top-level table for translating characters to character classes
   */
  private static final int [] ZZ_CMAP_TOP = zzUnpackcmap_top();

  private static final String ZZ_CMAP_TOP_PACKED_0 =
    "\1\0\u10ff\u0100";

  private static int [] zzUnpackcmap_top() {
    int [] result = new int[4352];
    int offset = 0;
    offset = zzUnpackcmap_top(ZZ_CMAP_TOP_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackcmap_top(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }


  /**
   * Second-level tables for translating characters to character classes
   */
  private static final int [] ZZ_CMAP_BLOCKS = zzUnpackcmap_blocks();

  private static final String ZZ_CMAP_BLOCKS_PACKED_0 =
    "\11\0\1\1\1\2\1\0\1\1\1\3\22\0\1\1"+
    "\1\0\1\4\5\0\1\5\1\6\1\7\1\0\1\10"+
    "\1\11\1\12\1\13\1\14\7\15\2\16\1\17\2\0"+
    "\1\20\1\21\2\0\6\22\16\23\1\24\5\23\1\0"+
    "\1\25\2\0\1\26\1\0\1\27\1\30\1\31\1\32"+
    "\1\33\1\34\1\35\1\23\1\36\1\23\1\37\1\23"+
    "\1\40\1\41\1\42\1\43\1\23\1\44\1\23\1\45"+
    "\1\46\1\47\1\23\1\50\2\23\1\51\1\0\1\52"+
    "\u0182\0";

  private static int [] zzUnpackcmap_blocks() {
    int [] result = new int[512];
    int offset = 0;
    offset = zzUnpackcmap_blocks(ZZ_CMAP_BLOCKS_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackcmap_blocks(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }

  /**
   * Translates DFA states to action switch labels.
   */
  private static final int [] ZZ_ACTION = zzUnpackAction();

  private static final String ZZ_ACTION_PACKED_0 =
    "\2\0\1\1\2\2\1\3\1\4\1\5\1\6\1\1"+
    "\1\7\1\1\2\10\1\11\1\12\5\13\1\14\1\15"+
    "\1\16\1\17\1\16\2\2\1\20\1\0\1\21\2\0"+
    "\1\22\1\23\5\13\1\16\5\0\1\24\1\25\5\13"+
    "\3\0\1\26\3\13\1\27\1\13\1\0\1\30\3\13"+
    "\1\0\1\31\1\13\1\32\1\33";

  private static int [] zzUnpackAction() {
    int [] result = new int[72];
    int offset = 0;
    offset = zzUnpackAction(ZZ_ACTION_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAction(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }


  /**
   * Translates a state to a row index in the transition table
   */
  private static final int [] ZZ_ROWMAP = zzUnpackRowMap();

  private static final String ZZ_ROWMAP_PACKED_0 =
    "\0\0\0\53\0\126\0\201\0\254\0\126\0\126\0\126"+
    "\0\126\0\327\0\126\0\u0102\0\u012d\0\u0158\0\u0183\0\126"+
    "\0\u01ae\0\u01d9\0\u0204\0\u022f\0\u025a\0\126\0\126\0\u0285"+
    "\0\126\0\u02b0\0\126\0\u02db\0\126\0\u0306\0\u0331\0\u035c"+
    "\0\u0387\0\126\0\126\0\u03b2\0\u03dd\0\u0408\0\u0433\0\u045e"+
    "\0\126\0\u0489\0\u04b4\0\u04df\0\u050a\0\u0535\0\u0560\0\u058b"+
    "\0\u05b6\0\u05e1\0\u060c\0\u0637\0\u0662\0\u068d\0\u06b8\0\u06e3"+
    "\0\126\0\u070e\0\u0739\0\u0764\0\u01ae\0\u078f\0\u07ba\0\u01ae"+
    "\0\u07e5\0\u0810\0\u083b\0\u0866\0\u01ae\0\u0891\0\u01ae\0\u01ae";

  private static int [] zzUnpackRowMap() {
    int [] result = new int[72];
    int offset = 0;
    offset = zzUnpackRowMap(ZZ_ROWMAP_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackRowMap(String packed, int offset, int [] result) {
    int i = 0;  /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length() - 1;
    while (i < l) {
      int high = packed.charAt(i++) << 16;
      result[j++] = high | packed.charAt(i++);
    }
    return j;
  }

  /**
   * The transition table of the DFA
   */
  private static final int [] ZZ_TRANS = zzUnpacktrans();

  private static final String ZZ_TRANS_PACKED_0 =
    "\1\3\1\4\2\5\1\6\1\7\1\10\1\3\1\11"+
    "\1\12\1\13\1\14\1\15\2\16\1\17\1\20\1\3"+
    "\3\21\1\3\4\21\1\22\3\21\1\23\4\21\1\24"+
    "\1\25\4\21\1\26\1\27\2\30\2\3\1\31\20\30"+
    "\1\32\25\30\54\0\1\4\1\33\1\34\51\0\2\5"+
    "\70\0\1\35\40\0\1\36\3\0\1\37\53\0\3\16"+
    "\7\0\1\16\13\0\1\40\5\0\1\41\16\0\3\16"+
    "\7\0\1\16\43\0\1\42\1\43\46\0\3\21\3\0"+
    "\3\21\1\0\23\21\16\0\3\21\3\0\3\21\1\0"+
    "\5\21\1\44\15\21\16\0\3\21\3\0\3\21\1\0"+
    "\12\21\1\45\10\21\16\0\3\21\3\0\3\21\1\0"+
    "\1\21\1\46\14\21\1\47\4\21\16\0\3\21\3\0"+
    "\3\21\1\0\5\21\1\50\15\21\2\0\2\30\3\0"+
    "\20\30\1\0\25\30\4\0\1\51\7\0\2\52\6\0"+
    "\1\53\2\0\2\51\2\0\2\51\4\0\1\51\2\0"+
    "\2\51\1\54\1\51\1\55\4\0\1\33\50\0\7\36"+
    "\1\56\43\36\2\37\2\0\47\37\14\0\2\57\51\0"+
    "\3\60\3\0\1\60\4\0\6\60\32\0\3\21\3\0"+
    "\3\21\1\0\6\21\1\61\14\21\16\0\3\21\3\0"+
    "\3\21\1\0\15\21\1\62\5\21\16\0\3\21\3\0"+
    "\3\21\1\0\3\21\1\63\17\21\16\0\3\21\3\0"+
    "\3\21\1\0\14\21\1\64\6\21\16\0\3\21\3\0"+
    "\3\21\1\0\17\21\1\65\3\21\16\0\2\51\51\0"+
    "\3\66\3\0\1\66\4\0\6\66\32\0\3\67\3\0"+
    "\1\67\4\0\6\67\32\0\3\70\3\0\1\70\4\0"+
    "\6\70\16\0\13\36\1\71\37\36\14\0\2\57\10\0"+
    "\1\57\40\0\3\60\3\0\1\60\3\0\7\60\32\0"+
    "\3\21\3\0\3\21\1\0\5\21\1\72\15\21\16\0"+
    "\3\21\3\0\3\21\1\0\14\21\1\73\6\21\16\0"+
    "\3\21\3\0\3\21\1\0\11\21\1\74\11\21\16\0"+
    "\3\21\3\0\3\21\1\0\3\21\1\75\17\21\16\0"+
    "\3\21\3\0\3\21\1\0\20\21\1\76\2\21\16\0"+
    "\3\77\3\0\1\77\4\0\6\77\32\0\3\55\3\0"+
    "\1\55\4\0\6\55\32\0\3\51\3\0\1\51\4\0"+
    "\6\51\32\0\3\21\3\0\3\21\1\0\16\21\1\100"+
    "\4\21\16\0\3\21\3\0\3\21\1\0\16\21\1\101"+
    "\4\21\16\0\3\21\3\0\3\21\1\0\1\21\1\102"+
    "\21\21\16\0\3\21\3\0\3\21\1\0\16\21\1\103"+
    "\4\21\16\0\3\104\3\0\1\104\4\0\6\104\32\0"+
    "\3\21\3\0\3\21\1\0\17\21\1\105\3\21\16\0"+
    "\3\21\3\0\3\21\1\0\7\21\1\106\13\21\16\0"+
    "\3\21\3\0\3\21\1\0\13\21\1\107\7\21\16\0"+
    "\3\54\3\0\1\54\4\0\6\54\32\0\3\21\3\0"+
    "\3\21\1\0\5\21\1\110\15\21\2\0";

  private static int [] zzUnpacktrans() {
    int [] result = new int[2236];
    int offset = 0;
    offset = zzUnpacktrans(ZZ_TRANS_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpacktrans(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      value--;
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }


  /* error codes */
  private static final int ZZ_UNKNOWN_ERROR = 0;
  private static final int ZZ_NO_MATCH = 1;
  private static final int ZZ_PUSHBACK_2BIG = 2;

  /* error messages for the codes above */
  private static final String[] ZZ_ERROR_MSG = {
    "Unknown internal scanner error",
    "Error: could not match input",
    "Error: pushback value was too large"
  };

  /**
   * ZZ_ATTRIBUTE[aState] contains the attributes of state {@code aState}
   */
  private static final int [] ZZ_ATTRIBUTE = zzUnpackAttribute();

  private static final String ZZ_ATTRIBUTE_PACKED_0 =
    "\2\0\1\11\2\1\4\11\1\1\1\11\4\1\1\11"+
    "\5\1\2\11\1\1\1\11\1\1\1\11\1\1\1\11"+
    "\1\0\1\1\2\0\2\11\5\1\1\11\5\0\7\1"+
    "\3\0\1\11\5\1\1\0\4\1\1\0\4\1";

  private static int [] zzUnpackAttribute() {
    int [] result = new int[72];
    int offset = 0;
    offset = zzUnpackAttribute(ZZ_ATTRIBUTE_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAttribute(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }

  /** the input device */
  private java.io.Reader zzReader;

  /** the current state of the DFA */
  private int zzState;

  /** the current lexical state */
  private int zzLexicalState = YYINITIAL;

  /** this buffer contains the current text to be matched and is
      the source of the yytext() string */
  private CharSequence zzBuffer = "";

  /** the textposition at the last accepting state */
  private int zzMarkedPos;

  /** the current text position in the buffer */
  private int zzCurrentPos;

  /** startRead marks the beginning of the yytext() string in the buffer */
  private int zzStartRead;

  /** endRead marks the last character in the buffer, that has been read
      from input */
  private int zzEndRead;

  /** zzAtEOF == true <=> the scanner is at the EOF */
  private boolean zzAtEOF;

  /** Number of newlines encountered up to the start of the matched text. */
  @SuppressWarnings("unused")
  private int yyline;

  /** Number of characters from the last newline up to the start of the matched text. */
  @SuppressWarnings("unused")
  protected int yycolumn;

  /** Number of characters up to the start of the matched text. */
  @SuppressWarnings("unused")
  private long yychar;

  /** Whether the scanner is currently at the beginning of a line. */
  @SuppressWarnings("unused")
  private boolean zzAtBOL = true;

  /** Whether the user-EOF-code has already been executed. */
  @SuppressWarnings("unused")
  private boolean zzEOFDone;

  /* user code: */
 StringBuffer string = new StringBuffer();

  public OdinLexer() {
    this((java.io.Reader)null);
  }


  /**
   * Creates a new scanner
   *
   * @param   in  the java.io.Reader to read input from.
   */
  public OdinLexer(java.io.Reader in) {
    this.zzReader = in;
  }


  /** Returns the maximum size of the scanner buffer, which limits the size of tokens. */
  private int zzMaxBufferLen() {
    return Integer.MAX_VALUE;
  }

  /**  Whether the scanner buffer can grow to accommodate a larger token. */
  private boolean zzCanGrow() {
    return true;
  }

  /**
   * Translates raw input code points to DFA table row
   */
  private static int zzCMap(int input) {
    int offset = input & 255;
    return offset == input ? ZZ_CMAP_BLOCKS[offset] : ZZ_CMAP_BLOCKS[ZZ_CMAP_TOP[input >> 8] | offset];
  }

  public final int getTokenStart() {
    return zzStartRead;
  }

  public final int getTokenEnd() {
    return getTokenStart() + yylength();
  }

  public void reset(CharSequence buffer, int start, int end, int initialState) {
    zzBuffer = buffer;
    zzCurrentPos = zzMarkedPos = zzStartRead = start;
    zzAtEOF  = false;
    zzAtBOL = true;
    zzEndRead = end;
    yybegin(initialState);
  }

  /**
   * Refills the input buffer.
   *
   * @return      {@code false}, iff there was new input.
   *
   * @exception   java.io.IOException  if any I/O-Error occurs
   */
  private boolean zzRefill() throws java.io.IOException {
    return true;
  }


  /**
   * Returns the current lexical state.
   */
  public final int yystate() {
    return zzLexicalState;
  }


  /**
   * Enters a new lexical state
   *
   * @param newState the new lexical state
   */
  public final void yybegin(int newState) {
    zzLexicalState = newState;
  }


  /**
   * Returns the text matched by the current regular expression.
   */
  public final CharSequence yytext() {
    return zzBuffer.subSequence(zzStartRead, zzMarkedPos);
  }


  /**
   * Returns the character at position {@code pos} from the
   * matched text.
   *
   * It is equivalent to yytext().charAt(pos), but faster
   *
   * @param pos the position of the character to fetch.
   *            A value from 0 to yylength()-1.
   *
   * @return the character at position pos
   */
  public final char yycharat(int pos) {
    return zzBuffer.charAt(zzStartRead+pos);
  }


  /**
   * Returns the length of the matched text region.
   */
  public final int yylength() {
    return zzMarkedPos-zzStartRead;
  }


  /**
   * Reports an error that occurred while scanning.
   *
   * In a wellformed scanner (no or only correct usage of
   * yypushback(int) and a match-all fallback rule) this method
   * will only be called with things that "Can't Possibly Happen".
   * If this method is called, something is seriously wrong
   * (e.g. a JFlex bug producing a faulty scanner etc.).
   *
   * Usual syntax/scanner level error handling should be done
   * in error fallback rules.
   *
   * @param   errorCode  the code of the errormessage to display
   */
  private void zzScanError(int errorCode) {
    String message;
    try {
      message = ZZ_ERROR_MSG[errorCode];
    }
    catch (ArrayIndexOutOfBoundsException e) {
      message = ZZ_ERROR_MSG[ZZ_UNKNOWN_ERROR];
    }

    throw new Error(message);
  }


  /**
   * Pushes the specified amount of characters back into the input stream.
   *
   * They will be read again by then next call of the scanning method
   *
   * @param number  the number of characters to be read again.
   *                This number must not be greater than yylength()!
   */
  public void yypushback(int number)  {
    if ( number > yylength() )
      zzScanError(ZZ_PUSHBACK_2BIG);

    zzMarkedPos -= number;
  }


  /**
   * Resumes scanning until the next regular expression is matched,
   * the end of input is encountered or an I/O-Error occurs.
   *
   * @return      the next token
   * @exception   java.io.IOException  if any I/O-Error occurs
   */
  public IElementType advance() throws java.io.IOException
  {
    int zzInput;
    int zzAction;

    // cached fields:
    int zzCurrentPosL;
    int zzMarkedPosL;
    int zzEndReadL = zzEndRead;
    CharSequence zzBufferL = zzBuffer;

    int [] zzTransL = ZZ_TRANS;
    int [] zzRowMapL = ZZ_ROWMAP;
    int [] zzAttrL = ZZ_ATTRIBUTE;

    while (true) {
      zzMarkedPosL = zzMarkedPos;

      zzAction = -1;

      zzCurrentPosL = zzCurrentPos = zzStartRead = zzMarkedPosL;

      zzState = ZZ_LEXSTATE[zzLexicalState];

      // set up zzAction for empty match case:
      int zzAttributes = zzAttrL[zzState];
      if ( (zzAttributes & 1) == 1 ) {
        zzAction = zzState;
      }


      zzForAction: {
        while (true) {

          if (zzCurrentPosL < zzEndReadL) {
            zzInput = Character.codePointAt(zzBufferL, zzCurrentPosL);
            zzCurrentPosL += Character.charCount(zzInput);
          }
          else if (zzAtEOF) {
            zzInput = YYEOF;
            break zzForAction;
          }
          else {
            // store back cached positions
            zzCurrentPos  = zzCurrentPosL;
            zzMarkedPos   = zzMarkedPosL;
            boolean eof = zzRefill();
            // get translated positions and possibly new buffer
            zzCurrentPosL  = zzCurrentPos;
            zzMarkedPosL   = zzMarkedPos;
            zzBufferL      = zzBuffer;
            zzEndReadL     = zzEndRead;
            if (eof) {
              zzInput = YYEOF;
              break zzForAction;
            }
            else {
              zzInput = Character.codePointAt(zzBufferL, zzCurrentPosL);
              zzCurrentPosL += Character.charCount(zzInput);
            }
          }
          int zzNext = zzTransL[ zzRowMapL[zzState] + zzCMap(zzInput) ];
          if (zzNext == -1) break zzForAction;
          zzState = zzNext;

          zzAttributes = zzAttrL[zzState];
          if ( (zzAttributes & 1) == 1 ) {
            zzAction = zzState;
            zzMarkedPosL = zzCurrentPosL;
            if ( (zzAttributes & 8) == 8 ) break zzForAction;
          }

        }
      }

      // store back cached position
      zzMarkedPos = zzMarkedPosL;

      if (zzInput == YYEOF && zzStartRead == zzCurrentPos) {
        zzAtEOF = true;
        return null;
      }
      else {
        switch (zzAction < 0 ? zzAction : ZZ_ACTION[zzAction]) {
          case 1:
            { return BAD_CHARACTER;
            }
          // fall through
          case 28: break;
          case 2:
            { return WHITE_SPACE;
            }
          // fall through
          case 29: break;
          case 3:
            { yybegin(STRING_STATE); string.setLength(0);
            }
          // fall through
          case 30: break;
          case 4:
            { return LPAREN;
            }
          // fall through
          case 31: break;
          case 5:
            { return RPAREN;
            }
          // fall through
          case 32: break;
          case 6:
            { return COMMA;
            }
          // fall through
          case 33: break;
          case 7:
            { return DOT;
            }
          // fall through
          case 34: break;
          case 8:
            { return INTEGER_DEC_LITERAL;
            }
          // fall through
          case 35: break;
          case 9:
            { return COLON;
            }
          // fall through
          case 36: break;
          case 10:
            { return EQ;
            }
          // fall through
          case 37: break;
          case 11:
            { return IDENTIFIER;
            }
          // fall through
          case 38: break;
          case 12:
            { return LBRACE;
            }
          // fall through
          case 39: break;
          case 13:
            { return RBRACE;
            }
          // fall through
          case 40: break;
          case 14:
            { 
            }
          // fall through
          case 41: break;
          case 15:
            { yybegin(YYINITIAL); return STRING_LITERAL;
            }
          // fall through
          case 42: break;
          case 16:
            { return ARROW;
            }
          // fall through
          case 43: break;
          case 17:
            { return LINE_COMMENT;
            }
          // fall through
          case 44: break;
          case 18:
            { return DOUBLE_COLON;
            }
          // fall through
          case 45: break;
          case 19:
            { return ASSIGN;
            }
          // fall through
          case 46: break;
          case 20:
            { return INTEGER_OCT_LITERAL;
            }
          // fall through
          case 47: break;
          case 21:
            { return INTEGER_HEX_LITERAL;
            }
          // fall through
          case 48: break;
          case 22:
            { return BLOCK_COMMENT;
            }
          // fall through
          case 49: break;
          case 23:
            { return PROC;
            }
          // fall through
          case 50: break;
          case 24:
            { return DEFER;
            }
          // fall through
          case 51: break;
          case 25:
            { return IMPORT;
            }
          // fall through
          case 52: break;
          case 26:
            { return RETURN;
            }
          // fall through
          case 53: break;
          case 27:
            { return PACKAGE;
            }
          // fall through
          case 54: break;
          default:
            zzScanError(ZZ_NO_MATCH);
          }
      }
    }
  }


}
