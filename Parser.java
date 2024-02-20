/*  MicroJava Parser (HM 06-12-28)
    ================
*/
package MJ;

import java.text.Normalizer.Form;
import java.util.*;
//import MJ.SymTab.*;
//import MJ.CodeGen.*;

public class Parser {
	private static final int  // token codes
		none      = 0,
		ident     = 1,
		number    = 2,
		charCon   = 3,
		plus      = 4,
		minus     = 5,
		times     = 6,
		slash     = 7,
		rem       = 8,
		eql       = 9,
		neq       = 10,
		lss       = 11,
		leq       = 12,
		gtr       = 13,
		geq       = 14,
		assign    = 15,
		semicolon = 16,
		comma     = 17,
		period    = 18,
		lpar      = 19,
		rpar      = 20,
		lbrack    = 21,
		rbrack    = 22,
		lbrace    = 23,
		rbrace    = 24,
		class_    = 25,
		else_     = 26,
		final_    = 27,
		if_       = 28,
		new_      = 29,
		print_    = 30,
		program_  = 31,
		read_     = 32,
		return_   = 33,
		void_     = 34,
		while_    = 35,
		eof       = 36;
	private static final String[] name = { // token names for error messages
		"none", "identifier", "number", "char constant", "+", "-", "*", "/", "%",
		"==", "!=", "<", "<=", ">", ">=", "=", ";", ",", ".", "(", ")",
		"[", "]", "{", "}", "class", "else", "final", "if", "new", "print",
		"program", "read", "return", "void", "while", "eof"
		};

	private static Token t;			// current token (recently recognized)
	private static Token la;		// lookahead token
	private static int sym;			// always contains la.kind
	public  static int errors;  // error counter
	private static int errDist;	// no. of correctly recognized tokens since last error

	private static BitSet exprStart, statStart, statSeqFollow, declStart, declFollow;

	//------------------- auxiliary methods ----------------------
	private static void scan() {
		t = la;
		la = Scanner.next();
		sym = la.kind;
		errDist++;
		/*
		System.out.print("line " + la.line + ", col " + la.col + ": " + name[sym]);
		if (sym == ident) System.out.print(" (" + la.string + ")");
		if (sym == number || sym == charCon) System.out.print(" (" + la.val + ")");
		System.out.println();*/
	}

	private static void check(int expected) {
		if (sym == expected) scan();
		else error(name[expected] + " expected");
	}

	public static void error(String msg) { // syntactic error at token la
		if (errDist >= 3) {
			System.out.println("-- line " + la.line + " col " + la.col + ": " + msg);
			errors++;
		}
		errDist = 0;
	}

	//-------------- parsing methods (in alphabetical order) -----------------

	// Program = "program" ident {ConstDecl | ClassDecl | VarDecl} '{' {MethodDecl} '}'.
	private static void Program() {
		check(program_);
		check(ident);
		while (true){
			if (sym == final_){ ConstDecl(); 
			} else if (sym == class_) { ClassDecl(); 
			} else if (sym == ident) { VarDecl();
			} else break;
		}

		check(lbrace);
		while (sym == void_ || sym == ident) {
			MethodDecl();	
		}
		check(rbrace);
		
	}

	//ConstDecl = "final" Type ident "=" (number | charConst) ";"
	static private void ConstDecl(){
		
		check(final_);
		Type();
		check(ident);
		check(assign);
		if (sym == number){
			scan();
		} else if (sym == charCon) {
			scan();			
		} else error("number or char constant expected");
		check(semicolon);
	}

	//Type = ident ["[" "]"].
	static private void Type(){
		check(ident);
		if (sym == lbrack){
			scan();
			check(rbrack);
		}
	}

	//FormPars = Type ident {"," Type ident}.
	static private void FormPars() {
		Type();
		check(ident);
		while (true) {
			if (sym == comma){
				scan();
				Type();
				check(ident);
			} else break;
		}
	}
	
	//ActPars = "(" [ Expr {"," Expr} ] ")".
	static void ActPars(){
		check(lpar);
		if (sym == minus | sym == ident){
			Expr();
			while (true){
				if (sym == comma){
					scan();
					Expr();
				} else break;
			}
		}
		check(rpar);
	}
	//Factor = Designator [ActPars] | number | charConst | "new" ident ["[" Expr "]"] | "(" Expr ")".
	private static void Factor(){
		if (sym == ident){	Designator(); ActPars(); 
		} else if (sym == number | sym == charCon) { scan();
		} else if (sym == new_) { 
			check(ident);
			if (sym == lbrack){
				scan();
				Expr();
				check(rbrack);
			}
		} else if (sym == lpar){
			scan();
			Expr();
			check(rpar);
		} else error("Designator, number, charcon, new, or lpar expected");

	}

	//Mulop = "*" | "/" | "%".
	static private void Mulop(){
		if (sym == times | sym == slash | sym == rem){
			scan();
		} else error("Times, slash or rem expected");
	}

	//Term = Factor {Mulop Factor}.
	private static void Term(){
		Factor();
		while (sym == times | sym == slash | sym == rem) { Mulop(); Factor(); }
	}

	//Addop = "+" | "-".
	private static void Addop(){
		if (sym == plus | sym == minus){
			scan();
		} else error("plus or minus expected");
	}
	//Expr = ["-"] Term {Addop Term}.
	private static void Expr(){
		if (sym == minus){ scan(); } else error("Minus expected");
		Term();
		while (sym == plus | sym == minus) { Addop(); Term(); }
	}

	//Designator = ident {"." ident | "[" Expr "]"}.
	static private void Designator(){
		check(ident);
		while (true){
			if (sym == period){
				scan();
				check(ident);
			} else if (sym == lbrack){
				scan();
				Expr();
				check(rbrack);
			} else break;
		}

	}
	
	//Relop = "==" | "!=" | ">" | ">=" | "<" | "<=".
	static private void Relop(){
		if (sym == eql | sym == neq | sym == gtr | sym == geq | sym == lss | sym == leq) {
			scan();
		} else error("==, !=, >, >=, <, <= expected");
	}
	//Condition = Expr Relop Expr.
	static private void Condition(){
		Expr();
		Relop();
		Expr();
	}

	//Statement = Designator ("=" Expr | ActPars) ";" | "if" "(" Condition ")" Statement ["else" Statement]
	// | "while" "(" Condition ")" Statement | "return" [Expr] ";" | "read" "(" Designator ")" ";" 
	// | "print" "(" Expr ["," number] ")" ";" | Block | ";".
	static private void Statement(){
		switch (sym){
			case ident: 
				Designator();
				if (sym == assign) {
					scan();
					Expr();
				} else if (sym == lpar){ 
					ActPars();
				} else error("assign or lpar expected");
				check(semicolon);
				break;
			
			case if_:
				scan();
				check(lpar);
				Condition();
				check(rpar);
				Statement();
				if (sym == else_){
					scan();
					Statement();
				}
				break;
			
			case while_:
				scan();
				check(lpar);
				Condition();
				check(rpar);
				Statement();
				break;
			
			case return_:
				scan();
				if (sym == minus | sym == ident | sym == number | sym == charCon | sym == new_ | sym == lpar){
					Expr();
				}
				check(semicolon);
				break;
			
			case read_:
				scan();
				check(lpar);
				Designator();
				check(rpar);
				check(semicolon);
				break;

			case print_:
				scan();
				check(lpar);
				if (sym == minus | sym == ident | sym == number | sym == charCon | sym == new_ | sym == lpar){
					Expr();
					if (sym == comma | sym == number) {scan();
					} else error("comma or number expected");
				}
				check(rpar);
				check(semicolon);
				break;

			case lbrace:
				Block();
				break;
			
			case semicolon:
				scan();
				break;
			
			default:
				error("[long list here] 1 expected");
				break;
		}
	}
	
	//Block = "{" {Statement} "}".
	static private void Block(){
		check(lbrace);
		System.out.println("block");
		if (sym == ident | sym == if_ | sym == while_ | sym == return_ | sym == read_ |  
		sym == print_ | sym == lbrace | sym == lbrace | sym == semicolon) {
			Statement();
		} else error("[long list here] 2 expected");
		check(rbrace);
	}
	//ClassDecl = "class" ident "{" {VarDecl} "}".	
	static private void ClassDecl(){
		check(class_);
		check(ident);
		check(lbrace);
	
		VarDecl();

		check(rbrace);
		
	}
	//VarDecl = Type ident {"," ident } ";".
	static private void VarDecl(){
		check(ident);
		if (sym == lbrack) {
			scan();
			check(rbrack);
		} else { error("left bracket expected"); }
		check(ident);
		while (sym == comma) {
			check(comma);
			check(ident);
		}
		check(semicolon);
		scan();
	}
	//MethodDecl = (Type | "void") ident "(" [FormPars] ")" {VarDecl} Block.
	static private void MethodDecl(){
		if (sym == void_){
			scan();
		} else { Type(); }
		check(ident);
		check(lpar);
		if (sym == lpar){
			scan();
		} else FormPars();
		check(rpar);
		
		while (true){
			if (sym == ident) { VarDecl(); 
			} else break;
		}

		Block();

	}
	
	public static void parse() {
		// initialize symbol sets
		BitSet s;
		s = new BitSet(64); exprStart = s;
		s.set(ident); s.set(number); s.set(charCon); s.set(new_); s.set(lpar); s.set(minus);

		s = new BitSet(64); statStart = s;
		s.set(ident); s.set(if_); s.set(while_); s.set(read_);
		s.set(return_); s.set(print_); s.set(lbrace); s.set(semicolon);

		s = new BitSet(64); statSeqFollow = s;
		s.set(rbrace); s.set(eof);

		s = new BitSet(64); declStart = s;
		s.set(final_); s.set(ident); s.set(class_);

		s = new BitSet(64); declFollow = s;
		s.set(lbrace); s.set(void_); s.set(eof);

		// start parsing
		errors = 0; errDist = 3;
		scan();
		Program();
		if (sym != eof) error("end of file found before end of program");
	}

}








