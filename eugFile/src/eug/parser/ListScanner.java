// DO NOT EDIT
// Generated by JFlex 1.8.2 http://jflex.de/
// source: C:/Users/Michael/Documents/GitHub/eug/eugFile/src/eug/parser/listScanner.flex

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * ListScanner.java                                                          *
 * This file is generated from listScanner.flex. Please edit that file if    *
 * you wish to make changes.                                                 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package eug.parser;

import java.util.List;
import java.util.ArrayList;

/**
 * This scanner parses list items from something like:
 * <code>
 *  "New Zealand"#Important
 *              Ontario
 *              "Quebec" "British Columbia"
 * </code>
 * <p>
 * This is an internal class, meant for use only by {@link EUGScanner}.
 * @author Michael Myers
 * @since EUGFile 1.01.00
 */

// See https://github.com/jflex-de/jflex/issues/222
@SuppressWarnings("FallThrough")
final class ListScanner {

  /** This character denotes the end of file. */
  private static final int YYEOF = -1;

  /** Initial size of the lookahead buffer. */
  private static final int ZZ_BUFFERSIZE = 16384;

  // Lexical states.
  private static final int YYINITIAL = 0;

  /**
   * ZZ_LEXSTATE[l] is the state in the DFA for the lexical state l
   * ZZ_LEXSTATE[l+1] is the state in the DFA for the lexical state l
   *                  at the beginning of a line
   * l is of the form l = 2*k, k a non negative integer
   */
  private static final int ZZ_LEXSTATE[] = {
     0, 0
  };

  /**
   * Top-level table for translating characters to character classes
   */
  private static final int [] ZZ_CMAP_TOP = zzUnpackcmap_top();

  private static final String ZZ_CMAP_TOP_PACKED_0 =
    "\1\0\1\u0100\1\u0200\1\u0300\1\u0400\1\u0500\1\u0600\1\u0700"+
    "\1\u0800\1\u0900\1\u0a00\1\u0b00\1\u0c00\1\u0d00\1\u0e00\1\u0f00"+
    "\1\u1000\1\u0100\1\u1100\1\u1200\1\u1300\1\u0100\1\u1400\1\u1500"+
    "\1\u1600\1\u1700\1\u1800\1\u1900\1\u1a00\1\u1b00\1\u0100\1\u1c00"+
    "\1\u1d00\1\u1e00\12\u1f00\1\u2000\1\u2100\1\u2200\1\u1f00\1\u2300"+
    "\1\u2400\2\u1f00\31\u0100\1\u2500\121\u0100\1\u2600\4\u0100\1\u2700"+
    "\1\u0100\1\u2800\1\u2900\1\u2a00\1\u2b00\1\u2c00\1\u2d00\53\u0100"+
    "\1\u2e00\10\u2f00\31\u1f00\1\u0100\1\u3000\1\u3100\1\u0100\1\u3200"+
    "\1\u3300\1\u3400\1\u3500\1\u1f00\1\u3600\1\u3700\1\u3800\1\u3900"+
    "\1\u0100\1\u3a00\1\u3b00\1\u3c00\1\u3d00\1\u3e00\1\u3f00\1\u4000"+
    "\1\u1f00\1\u4100\1\u4200\1\u4300\1\u4400\1\u4500\1\u4600\1\u4700"+
    "\1\u4800\1\u4900\1\u4a00\1\u4b00\1\u4c00\1\u1f00\1\u4d00\1\u4e00"+
    "\1\u4f00\1\u1f00\3\u0100\1\u5000\1\u5100\1\u5200\12\u1f00\4\u0100"+
    "\1\u5300\17\u1f00\2\u0100\1\u5400\41\u1f00\2\u0100\1\u5500\1\u5600"+
    "\2\u1f00\1\u5700\1\u5800\27\u0100\1\u5900\2\u0100\1\u5a00\45\u1f00"+
    "\1\u0100\1\u5b00\1\u5c00\11\u1f00\1\u5d00\27\u1f00\1\u5e00\1\u5f00"+
    "\1\u6000\1\u6100\11\u1f00\1\u6200\1\u6300\5\u1f00\1\u6400\1\u6500"+
    "\4\u1f00\1\u6600\21\u1f00\246\u0100\1\u6700\20\u0100\1\u6800\1\u6900"+
    "\25\u0100\1\u6a00\34\u0100\1\u6b00\14\u1f00\2\u0100\1\u6c00\u0e05\u1f00";

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
    "\10\0\2\1\1\2\2\3\1\2\22\0\1\1\1\4"+
    "\1\5\1\4\3\0\1\6\4\0\17\6\1\4\4\0"+
    "\34\6\1\0\1\6\1\0\1\6\1\0\32\6\12\0"+
    "\1\3\32\0\1\1\7\0\1\6\1\0\1\6\11\0"+
    "\2\6\4\0\1\6\5\0\27\6\1\0\37\6\1\0"+
    "\u01ca\6\4\0\14\6\16\0\5\6\7\0\1\6\1\0"+
    "\1\6\201\0\5\6\1\0\2\6\2\0\4\6\1\0"+
    "\1\6\6\0\1\6\1\0\3\6\1\0\1\6\1\0"+
    "\24\6\1\0\123\6\1\0\213\6\10\0\246\6\1\0"+
    "\46\6\2\0\1\6\6\0\51\6\107\0\33\6\4\0"+
    "\4\6\55\0\53\6\43\0\2\6\1\0\143\6\1\0"+
    "\1\6\17\0\2\6\7\0\2\6\12\0\3\6\2\0"+
    "\1\6\20\0\1\6\1\0\36\6\35\0\131\6\13\0"+
    "\1\6\30\0\41\6\11\0\2\6\4\0\1\6\5\0"+
    "\26\6\4\0\1\6\11\0\1\6\3\0\1\6\27\0"+
    "\31\6\7\0\13\6\65\0\25\6\1\0\10\6\106\0"+
    "\66\6\3\0\1\6\22\0\1\6\7\0\12\6\17\0"+
    "\20\6\4\0\10\6\2\0\2\6\2\0\26\6\1\0"+
    "\7\6\1\0\1\6\3\0\4\6\3\0\1\6\20\0"+
    "\1\6\15\0\2\6\1\0\3\6\16\0\2\6\12\0"+
    "\1\6\10\0\6\6\4\0\2\6\2\0\26\6\1\0"+
    "\7\6\1\0\2\6\1\0\2\6\1\0\2\6\37\0"+
    "\4\6\1\0\1\6\23\0\3\6\20\0\11\6\1\0"+
    "\3\6\1\0\26\6\1\0\7\6\1\0\2\6\1\0"+
    "\5\6\3\0\1\6\22\0\1\6\17\0\2\6\27\0"+
    "\1\6\13\0\10\6\2\0\2\6\2\0\26\6\1\0"+
    "\7\6\1\0\2\6\1\0\5\6\3\0\1\6\36\0"+
    "\2\6\1\0\3\6\17\0\1\6\21\0\1\6\1\0"+
    "\6\6\3\0\3\6\1\0\4\6\3\0\2\6\1\0"+
    "\1\6\1\0\2\6\3\0\2\6\3\0\3\6\3\0"+
    "\14\6\26\0\1\6\64\0\10\6\1\0\3\6\1\0"+
    "\27\6\1\0\20\6\3\0\1\6\32\0\3\6\5\0"+
    "\2\6\36\0\1\6\4\0\10\6\1\0\3\6\1\0"+
    "\27\6\1\0\12\6\1\0\5\6\3\0\1\6\40\0"+
    "\1\6\1\0\2\6\17\0\2\6\22\0\10\6\1\0"+
    "\3\6\1\0\51\6\2\0\1\6\20\0\1\6\5\0"+
    "\3\6\10\0\3\6\30\0\6\6\5\0\22\6\3\0"+
    "\30\6\1\0\11\6\1\0\1\6\2\0\7\6\72\0"+
    "\60\6\1\0\2\6\14\0\7\6\72\0\2\6\1\0"+
    "\1\6\1\0\5\6\1\0\30\6\1\0\1\6\1\0"+
    "\12\6\1\0\2\6\11\0\1\6\2\0\5\6\1\0"+
    "\1\6\25\0\4\6\40\0\1\6\77\0\10\6\1\0"+
    "\44\6\33\0\5\6\163\0\53\6\24\0\1\6\20\0"+
    "\6\6\4\0\4\6\3\0\1\6\3\0\2\6\7\0"+
    "\3\6\4\0\15\6\14\0\1\6\21\0\46\6\1\0"+
    "\1\6\5\0\1\6\2\0\53\6\1\0\115\6\1\0"+
    "\4\6\2\0\7\6\1\0\1\6\1\0\4\6\2\0"+
    "\51\6\1\0\4\6\2\0\41\6\1\0\4\6\2\0"+
    "\7\6\1\0\1\6\1\0\4\6\2\0\17\6\1\0"+
    "\71\6\1\0\4\6\2\0\103\6\45\0\20\6\20\0"+
    "\126\6\2\0\6\6\3\0\u016c\6\2\0\21\6\1\0"+
    "\32\6\5\0\113\6\6\0\10\6\7\0\15\6\1\0"+
    "\4\6\16\0\22\6\16\0\22\6\16\0\15\6\1\0"+
    "\3\6\17\0\64\6\43\0\1\6\4\0\1\6\103\0"+
    "\131\6\7\0\5\6\2\0\42\6\1\0\1\6\5\0"+
    "\106\6\12\0\37\6\61\0\36\6\2\0\5\6\13\0"+
    "\54\6\4\0\32\6\66\0\27\6\11\0\65\6\122\0"+
    "\1\6\135\0\57\6\21\0\7\6\67\0\36\6\15\0"+
    "\2\6\12\0\54\6\32\0\44\6\51\0\3\6\12\0"+
    "\44\6\2\0\11\6\7\0\53\6\2\0\3\6\51\0"+
    "\4\6\1\0\6\6\1\0\2\6\3\0\1\6\5\0"+
    "\300\6\100\0\26\6\2\0\6\6\2\0\46\6\2\0"+
    "\6\6\2\0\10\6\1\0\1\6\1\0\1\6\1\0"+
    "\1\6\1\0\37\6\2\0\65\6\1\0\7\6\1\0"+
    "\1\6\3\0\3\6\1\0\7\6\3\0\4\6\2\0"+
    "\6\6\4\0\15\6\5\0\3\6\1\0\7\6\23\0"+
    "\12\6\16\0\2\3\107\0\1\6\15\0\1\6\20\0"+
    "\15\6\145\0\1\6\4\0\1\6\2\0\12\6\1\0"+
    "\1\6\3\0\5\6\6\0\1\6\1\0\1\6\1\0"+
    "\1\6\1\0\4\6\1\0\13\6\2\0\4\6\5\0"+
    "\5\6\4\0\1\6\64\0\2\6\u017b\0\57\6\1\0"+
    "\57\6\1\0\205\6\6\0\4\6\3\0\2\6\14\0"+
    "\46\6\1\0\1\6\5\0\1\6\2\0\70\6\7\0"+
    "\1\6\20\0\27\6\11\0\7\6\1\0\7\6\1\0"+
    "\7\6\1\0\7\6\1\0\7\6\1\0\7\6\1\0"+
    "\7\6\1\0\7\6\120\0\1\6\325\0\2\6\52\0"+
    "\5\6\5\0\2\6\4\0\126\6\6\0\3\6\1\0"+
    "\132\6\1\0\4\6\5\0\53\6\1\0\136\6\21\0"+
    "\33\6\65\0\306\6\112\0\360\6\20\0\215\6\103\0"+
    "\56\6\2\0\15\6\3\0\20\6\12\0\2\6\24\0"+
    "\57\6\20\0\37\6\2\0\106\6\61\0\11\6\2\0"+
    "\147\6\2\0\65\6\2\0\5\6\60\0\13\6\1\0"+
    "\3\6\1\0\4\6\1\0\27\6\35\0\64\6\16\0"+
    "\62\6\76\0\6\6\3\0\1\6\1\0\2\6\13\0"+
    "\34\6\12\0\27\6\31\0\35\6\7\0\57\6\34\0"+
    "\1\6\20\0\5\6\1\0\12\6\12\0\5\6\1\0"+
    "\51\6\27\0\3\6\1\0\10\6\24\0\27\6\3\0"+
    "\1\6\3\0\62\6\1\0\1\6\3\0\2\6\2\0"+
    "\5\6\2\0\1\6\1\0\1\6\30\0\3\6\2\0"+
    "\13\6\7\0\3\6\14\0\6\6\2\0\6\6\2\0"+
    "\6\6\11\0\7\6\1\0\7\6\1\0\53\6\1\0"+
    "\14\6\10\0\163\6\35\0\244\6\14\0\27\6\4\0"+
    "\61\6\4\0\u0100\3\156\6\2\0\152\6\46\0\7\6"+
    "\14\0\5\6\5\0\1\6\1\0\12\6\1\0\15\6"+
    "\1\0\5\6\1\0\1\6\1\0\2\6\1\0\2\6"+
    "\1\0\154\6\41\0\153\6\22\0\100\6\2\0\66\6"+
    "\50\0\14\6\164\0\5\6\1\0\207\6\44\0\32\6"+
    "\6\0\32\6\13\0\131\6\3\0\6\6\2\0\6\6"+
    "\2\0\6\6\2\0\3\6\43\0\14\6\1\0\32\6"+
    "\1\0\23\6\1\0\2\6\1\0\17\6\2\0\16\6"+
    "\42\0\173\6\205\0\35\6\3\0\61\6\57\0\40\6"+
    "\15\0\24\6\1\0\10\6\6\0\46\6\12\0\36\6"+
    "\2\0\44\6\4\0\10\6\60\0\236\6\22\0\44\6"+
    "\4\0\44\6\4\0\50\6\10\0\64\6\234\0\67\6"+
    "\11\0\26\6\12\0\10\6\230\0\6\6\2\0\1\6"+
    "\1\0\54\6\1\0\2\6\3\0\1\6\2\0\27\6"+
    "\12\0\27\6\11\0\37\6\101\0\23\6\1\0\2\6"+
    "\12\0\26\6\12\0\32\6\106\0\70\6\6\0\2\6"+
    "\100\0\1\6\17\0\4\6\1\0\3\6\1\0\35\6"+
    "\52\0\35\6\3\0\35\6\43\0\10\6\1\0\34\6"+
    "\33\0\66\6\12\0\26\6\12\0\23\6\15\0\22\6"+
    "\156\0\111\6\67\0\63\6\15\0\63\6\15\0\44\6"+
    "\334\0\35\6\12\0\1\6\10\0\26\6\232\0\27\6"+
    "\14\0\65\6\113\0\55\6\40\0\31\6\32\0\44\6"+
    "\35\0\1\6\13\0\43\6\3\0\1\6\14\0\60\6"+
    "\16\0\4\6\25\0\1\6\1\0\1\6\43\0\22\6"+
    "\1\0\31\6\124\0\7\6\1\0\1\6\1\0\4\6"+
    "\1\0\17\6\1\0\12\6\7\0\57\6\46\0\10\6"+
    "\2\0\2\6\2\0\26\6\1\0\7\6\1\0\2\6"+
    "\1\0\5\6\3\0\1\6\22\0\1\6\14\0\5\6"+
    "\236\0\65\6\22\0\4\6\24\0\1\6\40\0\60\6"+
    "\24\0\2\6\1\0\1\6\270\0\57\6\51\0\4\6"+
    "\44\0\60\6\24\0\1\6\73\0\53\6\15\0\1\6"+
    "\107\0\33\6\345\0\54\6\164\0\100\6\37\0\1\6"+
    "\240\0\10\6\2\0\47\6\20\0\1\6\1\0\1\6"+
    "\34\0\1\6\12\0\50\6\7\0\1\6\25\0\1\6"+
    "\13\0\56\6\23\0\1\6\42\0\71\6\7\0\11\6"+
    "\1\0\45\6\21\0\1\6\61\0\36\6\160\0\7\6"+
    "\1\0\2\6\1\0\46\6\25\0\1\6\31\0\6\6"+
    "\1\0\2\6\1\0\40\6\16\0\1\6\u0147\0\23\6"+
    "\15\0\232\6\346\0\304\6\274\0\57\6\321\0\107\6"+
    "\271\0\71\6\7\0\37\6\161\0\36\6\22\0\60\6"+
    "\20\0\4\6\37\0\25\6\5\0\23\6\260\0\100\6"+
    "\200\0\113\6\5\0\1\6\102\0\15\6\100\0\2\6"+
    "\1\0\1\6\34\0\370\6\10\0\363\6\15\0\37\6"+
    "\61\0\3\6\21\0\4\6\10\0\u018c\6\4\0\153\6"+
    "\5\0\15\6\3\0\11\6\7\0\12\6\146\0\125\6"+
    "\1\0\107\6\1\0\2\6\2\0\1\6\2\0\2\6"+
    "\2\0\4\6\1\0\14\6\1\0\1\6\1\0\7\6"+
    "\1\0\101\6\1\0\4\6\2\0\10\6\1\0\7\6"+
    "\1\0\34\6\1\0\4\6\1\0\5\6\1\0\1\6"+
    "\3\0\7\6\1\0\u0154\6\2\0\31\6\1\0\31\6"+
    "\1\0\37\6\1\0\31\6\1\0\37\6\1\0\31\6"+
    "\1\0\37\6\1\0\31\6\1\0\37\6\1\0\31\6"+
    "\1\0\10\6\64\0\55\6\12\0\7\6\20\0\1\6"+
    "\u0171\0\54\6\24\0\305\6\73\0\104\6\7\0\1\6"+
    "\264\0\4\6\1\0\33\6\1\0\2\6\1\0\1\6"+
    "\2\0\1\6\1\0\12\6\1\0\4\6\1\0\1\6"+
    "\1\0\1\6\6\0\1\6\4\0\1\6\1\0\1\6"+
    "\1\0\1\6\1\0\3\6\1\0\2\6\1\0\1\6"+
    "\2\0\1\6\1\0\1\6\1\0\1\6\1\0\1\6"+
    "\1\0\1\6\1\0\2\6\1\0\1\6\2\0\4\6"+
    "\1\0\7\6\1\0\4\6\1\0\4\6\1\0\1\6"+
    "\1\0\12\6\1\0\21\6\5\0\3\6\1\0\5\6"+
    "\1\0\21\6\104\0\327\6\51\0\65\6\13\0\336\6"+
    "\2\0\u0182\6\16\0\u0131\6\37\0\36\6\342\0";

  private static int [] zzUnpackcmap_blocks() {
    int [] result = new int[27904];
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
    "\1\0\1\1\2\2\1\1\1\3\1\0\1\4";

  private static int [] zzUnpackAction() {
    int [] result = new int[8];
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
    "\0\0\0\7\0\16\0\25\0\34\0\43\0\34\0\7";

  private static int [] zzUnpackRowMap() {
    int [] result = new int[8];
    int offset = 0;
    offset = zzUnpackRowMap(ZZ_ROWMAP_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackRowMap(String packed, int offset, int [] result) {
    int i = 0;  /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int high = packed.charAt(i++) << 16;
      result[j++] = high | packed.charAt(i++);
    }
    return j;
  }

  /**
   * The transition table of the DFA
   */
  private static final int [] ZZ_TRANS = zzUnpackTrans();

  private static final String ZZ_TRANS_PACKED_0 =
    "\1\2\2\3\1\0\1\4\1\5\1\6\10\0\2\3"+
    "\4\0\2\4\1\0\4\4\5\7\1\10\1\7\6\0"+
    "\1\6";

  private static int [] zzUnpackTrans() {
    int [] result = new int[42];
    int offset = 0;
    offset = zzUnpackTrans(ZZ_TRANS_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackTrans(String packed, int offset, int [] result) {
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


  /** Error code for "Unknown internal scanner error". */
  private static final int ZZ_UNKNOWN_ERROR = 0;
  /** Error code for "could not match input". */
  private static final int ZZ_NO_MATCH = 1;
  /** Error code for "pushback value was too large". */
  private static final int ZZ_PUSHBACK_2BIG = 2;

  /**
   * Error messages for {@link #ZZ_UNKNOWN_ERROR}, {@link #ZZ_NO_MATCH}, and
   * {@link #ZZ_PUSHBACK_2BIG} respectively.
   */
  private static final String ZZ_ERROR_MSG[] = {
    "Unknown internal scanner error",
    "Error: could not match input",
    "Error: pushback value was too large"
  };

  /**
   * ZZ_ATTRIBUTE[aState] contains the attributes of state {@code aState}
   */
  private static final int [] ZZ_ATTRIBUTE = zzUnpackAttribute();

  private static final String ZZ_ATTRIBUTE_PACKED_0 =
    "\1\0\1\11\4\1\1\0\1\11";

  private static int [] zzUnpackAttribute() {
    int [] result = new int[8];
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

  /** Input device. */
  private java.io.Reader zzReader;

  /** Current state of the DFA. */
  private int zzState;

  /** Current lexical state. */
  private int zzLexicalState = YYINITIAL;

  /**
   * This buffer contains the current text to be matched and is the source of the {@link #yytext()}
   * string.
   */
  private char zzBuffer[] = new char[ZZ_BUFFERSIZE];

  /** Text position at the last accepting state. */
  private int zzMarkedPos;

  /** Current text position in the buffer. */
  private int zzCurrentPos;

  /** Marks the beginning of the {@link #yytext()} string in the buffer. */
  private int zzStartRead;

  /** Marks the last character in the buffer, that has been read from input. */
  private int zzEndRead;

  /**
   * Whether the scanner is at the end of file.
   * @see #yyatEOF
   */
  private boolean zzAtEOF;

  /**
   * The number of occupied positions in {@link #zzBuffer} beyond {@link #zzEndRead}.
   *
   * <p>When a lead/high surrogate has been read from the input stream into the final
   * {@link #zzBuffer} position, this will have a value of 1; otherwise, it will have a value of 0.
   */
  private int zzFinalHighSurrogate = 0;

  /** Number of newlines encountered up to the start of the matched text. */
  @SuppressWarnings("unused")
  private int yyline;

  /** Number of characters from the last newline up to the start of the matched text. */
  @SuppressWarnings("unused")
  private int yycolumn;

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
    private static volatile ListScanner scanner; // volatile so lazy initialization works

    /**
     * Returns the shared instance of <code>ListScanner</code>. If it has
     * previously been created, it is reset with the given reader.
     * <p>
     * This method is used to keep from creating a new <code>ListScanner</code>
     * every time a list is read. This greatly reduces overhead in creating
     * the character tables, etc.
     * @since EUGFile 1.03.00
     */
    private static ListScanner getScanner(java.io.Reader reader) {
        if (scanner == null) {
            scanner = new ListScanner(reader);
        } else {
            scanner.yyreset(reader);
        }
        return scanner;
    }
            
    /**
     * Parses an array of strings from the given string. This is like
     * <code>str.split("\\s+")</code> except that it allows for quoted strings
     * with whitespace enclosed.
     * @param list the whitespace-delimited list of strings. Note that quoted
     * and unquoted strings can be mixed freely.
     * @return the array of strings.
     * @see #parseList(String)
     */
    static String[] parseArray(String list) {
        try {
            return getScanner(new java.io.StringReader(list)).parseArray();
        } catch (Exception ex) {
            System.err.println("Error reading list!");
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Parses a list of strings from the given string. This is like
     * <code>str.split("\\s+")</code> except that it allows for quoted strings
     * with whitespace enclosed and it returns a list.
     * @param list the whitespace-delimited list of strings. Note that quoted
     * and unquoted strings can be mixed freely.
     * @return the list of strings.
     * @see #parseArray(String)
     */
    static List<String> parseList(String list) {
        try {
            return getScanner(new java.io.StringReader(list)).parseList();
        } catch (Exception ex) {
            System.err.println("Error reading list!");
            ex.printStackTrace();
            return null;
        }
    }

    private String[] parseArray() throws java.io.IOException {
        final List<String> list = new ArrayList<String>();
        String tmp;
        while ((tmp = yylex()) != null)
            list.add(tmp);

        return list.toArray(new String[list.size()]);
    }

    private List<String> parseList() throws java.io.IOException {
        final List<String> list = new ArrayList<String>();
        String tmp;
        while ((tmp = yylex()) != null)
            list.add(tmp);

        return list;
    }

    private void badChar(char c) {
        System.err.println("Unexpected character when parsing a list: \'" + c +
            "\' (#" + Integer.toHexString((int)c) + ")");
    }


  /**
   * Creates a new scanner
   *
   * @param   in  the java.io.Reader to read input from.
   */
  ListScanner(java.io.Reader in) {
    this.zzReader = in;
  }

  /**
   * Translates raw input code points to DFA table row
   */
  private static int zzCMap(int input) {
    int offset = input & 255;
    return offset == input ? ZZ_CMAP_BLOCKS[offset] : ZZ_CMAP_BLOCKS[ZZ_CMAP_TOP[input >> 8] | offset];
  }

  /**
   * Refills the input buffer.
   *
   * @return {@code false} iff there was new input.
   * @exception java.io.IOException  if any I/O-Error occurs
   */
  private boolean zzRefill() throws java.io.IOException {

    /* first: make room (if you can) */
    if (zzStartRead > 0) {
      zzEndRead += zzFinalHighSurrogate;
      zzFinalHighSurrogate = 0;
      System.arraycopy(zzBuffer, zzStartRead,
                       zzBuffer, 0,
                       zzEndRead - zzStartRead);

      /* translate stored positions */
      zzEndRead -= zzStartRead;
      zzCurrentPos -= zzStartRead;
      zzMarkedPos -= zzStartRead;
      zzStartRead = 0;
    }

    /* is the buffer big enough? */
    if (zzCurrentPos >= zzBuffer.length - zzFinalHighSurrogate) {
      /* if not: blow it up */
      char newBuffer[] = new char[zzBuffer.length * 2];
      System.arraycopy(zzBuffer, 0, newBuffer, 0, zzBuffer.length);
      zzBuffer = newBuffer;
      zzEndRead += zzFinalHighSurrogate;
      zzFinalHighSurrogate = 0;
    }

    /* fill the buffer with new input */
    int requested = zzBuffer.length - zzEndRead;
    int numRead = zzReader.read(zzBuffer, zzEndRead, requested);

    /* not supposed to occur according to specification of java.io.Reader */
    if (numRead == 0) {
      throw new java.io.IOException(
          "Reader returned 0 characters. See JFlex examples/zero-reader for a workaround.");
    }
    if (numRead > 0) {
      zzEndRead += numRead;
      if (Character.isHighSurrogate(zzBuffer[zzEndRead - 1])) {
        if (numRead == requested) { // We requested too few chars to encode a full Unicode character
          --zzEndRead;
          zzFinalHighSurrogate = 1;
        } else {                    // There is room in the buffer for at least one more char
          int c = zzReader.read();  // Expecting to read a paired low surrogate char
          if (c == -1) {
            return true;
          } else {
            zzBuffer[zzEndRead++] = (char)c;
          }
        }
      }
      /* potentially more input available */
      return false;
    }

    /* numRead < 0 ==> end of stream */
    return true;
  }


  /**
   * Closes the input reader.
   *
   * @throws java.io.IOException if the reader could not be closed.
   */
  private final void yyclose() throws java.io.IOException {
    zzAtEOF = true; // indicate end of file
    zzEndRead = zzStartRead; // invalidate buffer

    if (zzReader != null) {
      zzReader.close();
    }
  }


  /**
   * Resets the scanner to read from a new input stream.
   *
   * <p>Does not close the old reader.
   *
   * <p>All internal variables are reset, the old input stream <b>cannot</b> be reused (internal
   * buffer is discarded and lost). Lexical state is set to {@code ZZ_INITIAL}.
   *
   * <p>Internal scan buffer is resized down to its initial length, if it has grown.
   *
   * @param reader The new input stream.
   */
  private final void yyreset(java.io.Reader reader) {
    zzReader = reader;
    zzEOFDone = false;
    yyResetPosition();
    zzLexicalState = YYINITIAL;
    if (zzBuffer.length > ZZ_BUFFERSIZE) {
      zzBuffer = new char[ZZ_BUFFERSIZE];
    }
  }

  /**
   * Resets the input position.
   */
  private final void yyResetPosition() {
      zzAtBOL  = true;
      zzAtEOF  = false;
      zzCurrentPos = 0;
      zzMarkedPos = 0;
      zzStartRead = 0;
      zzEndRead = 0;
      zzFinalHighSurrogate = 0;
      yyline = 0;
      yycolumn = 0;
      yychar = 0L;
  }


  /**
   * Returns whether the scanner has reached the end of the reader it reads from.
   *
   * @return whether the scanner has reached EOF.
   */
  private final boolean yyatEOF() {
    return zzAtEOF;
  }


  /**
   * Returns the current lexical state.
   *
   * @return the current lexical state.
   */
  private final int yystate() {
    return zzLexicalState;
  }


  /**
   * Enters a new lexical state.
   *
   * @param newState the new lexical state
   */
  private final void yybegin(int newState) {
    zzLexicalState = newState;
  }


  /**
   * Returns the text matched by the current regular expression.
   *
   * @return the matched text.
   */
  private final String yytext() {
    return new String(zzBuffer, zzStartRead, zzMarkedPos-zzStartRead);
  }


  /**
   * Returns the character at the given position from the matched text.
   *
   * <p>It is equivalent to {@code yytext().charAt(pos)}, but faster.
   *
   * @param position the position of the character to fetch. A value from 0 to {@code yylength()-1}.
   *
   * @return the character at {@code position}.
   */
  private final char yycharat(int position) {
    return zzBuffer[zzStartRead + position];
  }


  /**
   * How many characters were matched.
   *
   * @return the length of the matched text region.
   */
  private final int yylength() {
    return zzMarkedPos-zzStartRead;
  }


  /**
   * Reports an error that occurred while scanning.
   *
   * <p>In a well-formed scanner (no or only correct usage of {@code yypushback(int)} and a
   * match-all fallback rule) this method will only be called with things that
   * "Can't Possibly Happen".
   *
   * <p>If this method is called, something is seriously wrong (e.g. a JFlex bug producing a faulty
   * scanner etc.).
   *
   * <p>Usual syntax/scanner level error handling should be done in error fallback rules.
   *
   * @param errorCode the code of the error message to display.
   */
  private static void zzScanError(int errorCode) {
    String message;
    try {
      message = ZZ_ERROR_MSG[errorCode];
    } catch (ArrayIndexOutOfBoundsException e) {
      message = ZZ_ERROR_MSG[ZZ_UNKNOWN_ERROR];
    }

    throw new Error(message);
  }


  /**
   * Pushes the specified amount of characters back into the input stream.
   *
   * <p>They will be read again by then next call of the scanning method.
   *
   * @param number the number of characters to be read again. This number must not be greater than
   *     {@link #yylength()}.
   */
  private void yypushback(int number)  {
    if ( number > yylength() )
      zzScanError(ZZ_PUSHBACK_2BIG);

    zzMarkedPos -= number;
  }




  /**
   * Resumes scanning until the next regular expression is matched, the end of input is encountered
   * or an I/O-Error occurs.
   *
   * @return the next token.
   * @exception java.io.IOException if any I/O-Error occurs.
   */
  private String yylex() throws java.io.IOException {
    int zzInput;
    int zzAction;

    // cached fields:
    int zzCurrentPosL;
    int zzMarkedPosL;
    int zzEndReadL = zzEndRead;
    char[] zzBufferL = zzBuffer;

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
            zzInput = Character.codePointAt(zzBufferL, zzCurrentPosL, zzEndReadL);
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
              zzInput = Character.codePointAt(zzBufferL, zzCurrentPosL, zzEndReadL);
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
          {     return null;
 }
      }
      else {
        switch (zzAction < 0 ? zzAction : ZZ_ACTION[zzAction]) {
          case 1:
            { badChar(yycharat(0)); return yylex();
            }
            // fall through
          case 5: break;
          case 2:
            { break;
            }
            // fall through
          case 6: break;
          case 3:
            { return yytext();
            }
            // fall through
          case 7: break;
          case 4:
            { return yytext().substring(1, yylength() - 1);
            }
            // fall through
          case 8: break;
          default:
            zzScanError(ZZ_NO_MATCH);
        }
      }
    }
  }


}
