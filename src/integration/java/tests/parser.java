/*
 * Copyright (c) 2012, NTT Multimedia Communications Laboratories, Inc. and Koushik Sen
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package tests;

import catg.CATG;

/**
 * Author: Koushik Sen (ksen@cs.berkeley.edu)
 */
public class parser {
    int  ident = 1;
    int number = 2;
    int lparen = 3;
    int rparen = 4;
    int times = 5;
    int slash = 6;
    int plus = 7;
    int  minus = 8;
    int eql = 9;
    int neq = 10;
    int lss = 11;
    int leq = 12;
    int gtr = 13;
    int geq = 14;
    int callsym = 15;
    int beginsym = 16;
    int semicolon = 17;
    int endsym = 18;
    int ifsym = 19;
    int whilesym = 20;
    int becomes = 21;
    int thensym = 22;
    int dosym = 23;
    int comma = 25;
    int period = 28;
    int oddsym = 29;

    int sym;

    boolean isdigit(char x) {
        return x >='0'&& x <='9';
    }

    boolean isalpha(char x) {
        return (x >='A'&& x <='Z')||(x>='a'&&x<='z')||(x=='_');
    }

    boolean isalphanum(char x)  {
        return isalpha(x)||(x>='0'&& x <='9');
    }


    void error(String msg){
        System.out.println("Error:" + msg + "\n");
        CATG.skipPath();
        System.exit(0);
    }

    boolean matchStr(char[] s1, String s2) {
        CATG.BeginScope("test1");
        boolean ret;
        int len = s2.length();

        if (s1.length >= len) {
            ret = true;
            for (int i = 0; i < len; i++) {
                if (s2.charAt(i) != s1[i]) {
                    ret = false;
                }
            }
        } else {
            ret = false;
        }
        CATG.EndScope("test1");
        ret = CATG.abstractBool("test1", ret);
        return ret;
    }

    char[] token;
    final public static int  TOKEN_LEN = 6;

    void init_token(){
        token = new char[TOKEN_LEN];
    }

    int gettoken(char[] s){
        if(matchStr(s, "id") ) return ident;
        if(matchStr(s, "10") ) return number;
        if(matchStr(s, "(") ) return lparen;
        if(matchStr(s, ")") ) return rparen;
        if(matchStr(s, "*") ) return times;
        if(matchStr(s, "/") ) return slash;
        if(matchStr(s, "+") ) return plus;
        if(matchStr(s, "-") ) return minus;
        if(matchStr(s, "=") ) return eql;
        if(matchStr(s, "!=") ) return neq;
        if(matchStr(s, "<=") ) return leq;
        if(matchStr(s, "<") ) return lss;
        if(matchStr(s, ">=") ) return geq;
        if(matchStr(s, ">") ) return gtr;
        if(matchStr(s, "call") ) return callsym;
        if(matchStr(s, "begin") ) return beginsym;
        if(matchStr(s, ";") ) return semicolon;
        if(matchStr(s, "end") ) return endsym;
        if(matchStr(s, "if") ) return ifsym;
        if(matchStr(s, "while") ) return whilesym;
        if(matchStr(s, ":=") ) return becomes;
        if(matchStr(s, "then") ) return thensym;
        if(matchStr(s, "do") ) return dosym;
        if(matchStr(s, ".") ) return period;
        if(matchStr(s, "odd") ) return oddsym;
        return 0;
    }

    int count = 0;
    int MAX = 5;

    void getsym(){
        count++;
        if (count <= MAX) {
            int i;
            System.out.print(":");
            for (i = 0; i < TOKEN_LEN; i++) {
                token[i] = CATG.readChar('0');
                System.out.print(token[i]);
            }
            System.out.println(":");

            CATG.BeginScope("test1");
            sym = gettoken(token);
            CATG.EndScope("test1");
            System.out.print("<");
            System.out.println(sym);
            sym = CATG.abstractInt("test1", sym);
            System.out.print(sym);
            System.out.println(">");
        } else {
            sym = period;
            System.out.println("Token:.:");
        }
    }



    boolean accept(int s) {
        if (sym == s) {
            getsym();
            return true;
        }
        return false;
    }

    boolean expect(int s) {
        if (accept(s))
            return true;
        error("expect: unexpected symbol");
        return false;
    }

    void factor() {
        if (accept(ident)) {
            ;
        } else if (accept(number)) {
            ;
        } else if (accept(lparen)) {
            expression();
            expect(rparen);
        } else {
            error("factor: syntax error");
            getsym();
        }
    }

    void term() {
        factor();
        while (sym == times || sym == slash) {
            getsym();
            factor();
        }
    }

    void expression() {
        if (sym == plus || sym == minus)
            getsym();
        term();
        while (sym == plus || sym == minus) {
            getsym();
            term();
        }
    }

    void condition() {
        if (accept(oddsym)) {
            expression();
        } else {
            expression();
            if (sym == eql || sym == neq || sym == lss ||
                    sym == leq || sym == gtr || sym == geq) {
                getsym();
                expression();
            } else {
                error("condition: invalid operator");
                getsym();
            }
        }
    }

    void statement() {
        if (accept(ident)) {
            expect(becomes);
            expression();
        } else if (accept(callsym)) {
            expect(ident);
        } else if (accept(beginsym)) {
            do {
                statement();
            } while (accept(semicolon));
            expect(endsym);
        } else if (accept(ifsym)) {
            condition();
            expect(thensym);
            statement();
        } else if (accept(whilesym)) {
            condition();
            expect(dosym);
            statement();
        }
    }

    void f() {
        init_token();
        getsym();
        statement();
        expect(period);
    }

    public static void main(String[] args) {
        (new parser()).f();
    }
}
