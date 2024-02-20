/* MicroJava Scanner (HM 06-12-28)
   =================
*/
package MJ;
import java.io.*;

public class Scanner {
	private static final char eofCh = '\u0080';
	private static final char eol = '\n';
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
	private static final String key[] = { // sorted list of keywords
		"class", "else", "final", "if", "new", "print",
		"program", "read", "return", "void", "while"
	};
	private static final int keyVal[] = {
		class_, else_, final_, if_, new_, print_,
		program_, read_, return_, void_, while_
	};

	private static char ch;			// lookahead character
	public  static int col;			// current column
	public  static int line;		// current line
	private static int pos;			// current position from start of source file
	private static Reader in;  	// source file reader
	private static char[] lex;	// current lexeme (token string)

	//----- ch = next input character
	private static void nextCh() {
		try {
			ch = (char)in.read(); col++; pos++;
			if (ch == eol) {line++; col = 0;}
			else if (ch == '\uffff') ch = eofCh;
		} catch (IOException e) {
			ch = eofCh;
		}
	}

	//--------- Initialize scanner
	public static void init(Reader r) {
		in = new BufferedReader(r);
		lex = new char[64];
		line = 1; col = 0;
		nextCh();
	}

	private static boolean isAlphanumerical(char c) {
		return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9');
	}
	
	private static void readName(Token t){
		t.val = "";
		t.val+=ch;

		while (isAlphanumerical(ch)){
				t.val+=ch;
				nextCh();
		}

		String keyword=""; 
		for (String s : key){
			if (t.val == s){
				keyword = s;
			}
		}
		switch (keyword){
			case "class": t.kind = class_; break;
			case "else": t.kind = else_; break;
			case "final": t.kind = final_; break;
			case "if": t.kind = if_; break;
			case "new": t.kind = new_; break;
			case "print": t.kind = print_; break;
			case "program": t.kind = program_;
			case "read": t.kind = read_;
			case "return": t.kind = return_;
			case "void": t.kind = void_;
			case "while": t.kind = while_;
			default: t.kind = ident; break;
		}
	}

	private static void readNumber(Token t){
		t.val = "";

		boolean flag = true;
		while (flag){
			if (ch >= 0 && ch <= 9){
				t.val += ch;
			} else {
				flag = false;
			}
		}
		try {
			t.numVal = Integer.parseInt(t.val);
		} catch (Exception e){
			ch = eofCh;
		}
		//t.numVal += (int)(ch);
	}
	//---------- Return next input token
	public static Token next() {
		 // add your code here
		 while (ch <=' ')nextCh(); //skips blanks, tabs, eol
		 Token t = new Token(); t.line=line; t.col=col;
		 switch (ch) {
			//names, keywords
			case 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l',
				 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 
				 'm', 'n', 'o', 'q', 'r', 's', 't', 'u', 'w', 'x', 'y', 'z',
				 'M', 'N', 'O', 'Q', 'R', 'S', 'T', 'U', 'W', 'X', 'Y', 'Z':
				readName(t); 
				break;
			//numbers
			case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9':
				readNumber(t);
				break;
			//simple tokens
			case ';': nextCh(); t.kind=semicolon; break;
			case '.': nextCh(); t.kind=period; break;
			case ',': nextCh(); t.kind=comma; break;

				//operators (divide in comment)
			case '+': nextCh(); t.kind=plus; break;
			case '-': nextCh(); t.kind=minus; break;
			case '*': nextCh(); t.kind=times; break;
			case '%': nextCh(); t.kind=rem; break;

				//brackets/braces/parentheses
			case '(': nextCh(); t.kind=lpar; break;
			case ')': nextCh(); t.kind=rpar; break;
			case '[': nextCh(); t.kind=lbrack; break;
			case ']': nextCh(); t.kind=rbrack; break;
			case '{': nextCh(); t.kind=lbrace; break;
			case '}': nextCh(); t.kind=rbrace; break;

			case eofCh: t.kind = eof; break; //no nextCh()
			//compound tokens
			case '=': 
				nextCh(); 
				if (ch =='='){nextCh(); t.kind=eql;} else t.kind = assign;
				break;
			case '!':
				nextCh();
				if (ch=='='){nextCh(); t.kind=neq;}
			case '<':
				nextCh();
				if (ch=='='){nextCh(); t.kind=leq;} else t.kind = lss;
			case '>':
				nextCh();
				if (ch=='='){nextCh(); t.kind=geq;} else t.kind = gtr;
				
			//comments && slash
			case '/':
				nextCh();
				if (ch == '/') {
					do nextCh(); while (ch != '\n' && ch != eofCh);
					t = next(); // call scanner recursively
					} else t.kind = slash;
				break;
			default:
				nextCh(); t.kind=none;
				break;
		 }
		 return t;
	}
}







