/** Programming Languajes
	By: Carmen Carvajal
	Parser Class: this class verifies the source file syntax according to the grammar
**/
import java.util.*;
import java.lang.*;
import java.io.*;

public class Parser{
	//token: (Token) used to process each token in the source code
	private Token token;
	//lexer: (Lexer) used to obtain each token in the source code
	private Lexer lexer;
	
	/**
		Constructor: 
			Creates the lexer to obtain each token in the source file
			Points the iterator to the first token
			Starts parsing at the start symbol according the the grammar
	**/
	public Parser(String fileName){
		try{
			//Creates the lexer
			lexer = new Lexer(fileName);
			//Points the iterator to the first token
			token = lexer.nextToken();
			//Starts the parser
			program();
		} catch (FileNotFoundException ex){
			System.out.println("File not found!");
			System.exit(1);
		}
	}
	/**
		Function recognize: verifies if the current token corresponds to the expected token 
		according with the grammar and move the tokenIterator to the next one.
		@param expected: (int) expected token code
	**/
	private void recognize(int expected){
		if (token.code == expected)
			//the token is the expected one, move the tokenIterator
			token = lexer.nextToken();
		else {
			//The current token is not expected, syntax error!
			System.out.println("Syntax error in line " + token.line);
			System.out.println("Expected: "  + expected + " found " + token.code);
			//STOP!
			System.exit(2);
		}
	}
	/**
		Function program: verifies the production 
			<program>::= program <funDefinitionList> endProgram
	**/
	public void program(){
		//checks for "program"
		recognize(Lexer.PROGRAM);
		//checks for <funDefinitionList>
		funDefinitionList();
		//checks for "endProgram"
		recognize(Lexer.ENDPROGRAM);
		//if EOf is found, no error is found!
		if (token.code == Lexer.EOF){
			System.out.println("No errors found!");
		}
	}
	
	/**
		Function funDefinitionList: verifies the production 
		<funDefinitionList>::=<funDefinition> { <funDefinition> }
	**/ 
	public void funDefinitionList()
	{
		//checks for <funDefinition>
		funDefinition();
		//verifies if there are more <funDefinition>
		while(lexer.getCurrentToken().code == Lexer.DEF){
			funDefinition();
		}
	}
	
	/**
		Function funDefinition: verifies the production 
		<funDefinition>::= def <variable> <lparen> [<varDefList>] <rparen>
								[<varDefList>]
								[<statementList>]
							enddef
	**/
	public void funDefinition(){
		//checks for "def"
		recognize(Lexer.DEF);
		//checks for <variable>
		recognizeVariable();
		//checks for "("
		recognize(Lexer.LPAREN);
		//verifies if there are parameters
		if (lexer.getCurrentToken().code != Lexer.RPAREN) // def main ( )           def main ( int a int b)
			varDefList();
		//checks for ")"
		recognize(Lexer.RPAREN);
		//ver is there are variables definition
		if (lexer.getCurrentToken().code == Lexer.INT)
			varDefList();
		//checks for <statementList>
		statementList();
		//checks for "enddef"
		recognize(Lexer.ENDDEF);
	}
	
	/**
		Function varDefList: verifies the production <varDefList>::=<varDef> {<varDef>}
	**/
	public void varDefList(){
		//checks for <varDef>
		varDef();
		//Verifies if there are more <varDef>
		while (lexer.getCurrentToken().code == Lexer.INT){
			varDef();
		}	
	}
	
	/**
		Function varDef: verifies the production <varDef>::=int variable
	**/
	public void varDef(){
		recognize(Lexer.INT);
		recognize(Lexer.VARIABLE);
	}
	
	/**
		Function asignacion: verifies the production <asignacion> ::=<variable><assign>(<constant>|<variable>|<expr>) 
	**/
	public void asignacion(){
		recognize(Lexer.VARIABLE);
		recognize(Lexer.ASSIGN);
		//checks for read <constant>
		if (lexer.getCurrentToken().code == Lexer.CONSTANT){
			recognize(Lexer.CONSTANT);
		} //checks for print <variable>
		else if (lexer.getCurrentToken().code == Lexer.VARIABLE){
			recognize(Lexer.VARIABLE);
		} //checks for print <expr>
		else {
			expr();
		}
	}
	
	/**
		Function expr: verifies the production <expr> ::= <term> {+ <term>} 
	**/
	public void expr(){
		//checks for <term>
		term();
		recognize(Lexer.SUMA);
		term();
	}
	
	/**
		Function term: verifies the production <term> ::= <factor> {* <factor>} 
	**/
	public void term(){
		//checks for <factor>
		factor();
		recognize(Lexer.MULTIPLICACION);
		factor();
	}
	
	/**
		Function factor: verifies the production <factor> ::= (<expr>) | variable | constant 
	**/
	public void factor(){
		if (lexer.getCurrentToken().code == Lexer.LPAREN){
			recognize(Lexer.LPAREN);
			expr();
			recognize(Lexer.RPAREN);
		} //checks for print <variable>
		else if (lexer.getCurrentToken().code == Lexer.VARIABLE){
			recognizeVariable();
		} //checks for call <variable> <lparen> [argumentList] <rparen>
		else if (lexer.getCurrentToken().code == Lexer.CONSTANT){
			//checks for "constant"
			recognize(Lexer.CONSTANT);	
		}
	}
	
	/**
		Function statementList: verifies the production <statementList>::=<statement> {<statement>}
	**/
	public void statementList()
	{
		//checks for <statement>
		statement();
		//verifies if there are more <statement>
		while ((lexer.getCurrentToken().code != Lexer.ENDDEF) && (statement())){
			statement();
		}
	}

	/**
		Function statement: verifies the production <statement>::= read <variable> | print <variable> | <asignacion> | call<variable><lparen>[<argumentList>]<rparen> | <condicional> | <ciclo> | <varDef>
	**/
	public boolean statement(){
		boolean r = false;
		//checks for read <variable>
		if (lexer.getCurrentToken().code == Lexer.READ){
			recognize(Lexer.READ);
			recognize(Lexer.VARIABLE);
			System.out.println("Read ok!");
			r=true;
		} //checks for print <variable>
		else if (lexer.getCurrentToken().code == Lexer.PRINT){
			recognize(Lexer.PRINT);
			recognize(Lexer.VARIABLE);
			System.out.println("Print ok!");
			r=true;
		} //checks for <asignacion>
		else if ((lexer.getCurrentToken().code == Lexer.VARIABLE)){
			asignacion();
			System.out.println("Asignacion ok!");
			r=true;
		}//checks for call <variable> <lparen> [argumentList] <rparen>
		else if (lexer.getCurrentToken().code == Lexer.CALL){
			//checks for "call"
			recognize(Lexer.CALL);
			//checks for <variable>
			recognize(Lexer.VARIABLE);
			//checks for <lparen>
			recognize(Lexer.LPAREN);
			//verifies if there is an argument list
			if (lexer.getCurrentToken().code != Lexer.RPAREN)
				argumentList();
			//checks for <rparen>
			recognize(Lexer.RPAREN);
			System.out.println("call function ok!");
			r=true;			
		}//checks for <condicional>
		else if (lexer.getCurrentToken().code == Lexer.IF){
			condicional();
			System.out.println("If then Else ok!");
			r=true;
		}//checks for <ciclo>
		else if (lexer.getCurrentToken().code == Lexer.WHILE){
			ciclo();
			System.out.println("While ok!");
			r=true;
		}//checks for <varDef>
		else if (lexer.getCurrentToken().code == Lexer.INT){
			varDef();
			System.out.println("VarDef ok!");
			r=true;
		}
		return r;
	}
	
	/**
		Function condicional: verifies the production <condicional> ::= if <comparacion> {<statement>} [<elseStatement>] endif
	**/
	public void condicional()
	{
		//checks for if
		recognize(Lexer.IF);
		//checks for <comparacion>
		comparacion();
		//checks for <statement>
		statement();
		//verifies if there are more <statement>
		while ((lexer.getCurrentToken().code != Lexer.ENDIF)&&(lexer.getCurrentToken().code != Lexer.ELSE)&&(statement())){
			statement();
		}
		//verifies if <elseStatement>
		if (lexer.getCurrentToken().code == Lexer.ELSE){
			elseStatement();
		}
		//checks for endif
		recognize(Lexer.ENDIF);
	}
	
	/**
		Function comparacion: verifies the production <comparacion> ::= <lparent> <variable> (<equals>|<diferente>) (<variable>|<constant>) <rparent>
	**/
	public void comparacion()
	{
		//checks for <lparen>
		recognize(Lexer.LPAREN);
		//checks for <variable>
		recognize(Lexer.VARIABLE);
		//checks for <equals>
		if (lexer.getCurrentToken().code == Lexer.EQUALS){
			recognize(Lexer.EQUALS);
		}//checks for <diferente>
		else if (lexer.getCurrentToken().code == Lexer.DIFERENTE){
			recognize(Lexer.DIFERENTE);
		}
		//checks for <variable>
		if (lexer.getCurrentToken().code == Lexer.VARIABLE){
			recognize(Lexer.VARIABLE);
		}//checks for <constant>
		else if (lexer.getCurrentToken().code == Lexer.CONSTANT){
			recognize(Lexer.CONSTANT);
		} 
		//checks for <rparen>
		recognize(Lexer.RPAREN);
	}
	
	/**
		Function elseStatement: verifies the production <elseStatement> ::= else {<statement>}
	**/
	public void elseStatement()
	{
		//checks for else
		recognize(Lexer.ELSE);
		//checks for <statement>
		statement();
		//verifies if there are more <statement>
		while ((lexer.getCurrentToken().code != Lexer.ENDIF)&&(statement())){
			statement();
		}
	}
	
	/**
		Function ciclo: verifies the production <ciclo> ::= while <comparacion> {<statement>} <asignacion>
	**/
	public void ciclo()
	{
		//checks for while
		recognize(Lexer.WHILE);
		//checks for <comparacion>
		comparacion();
		//checks for <statement>
		statement();
		//verifies if there are more <statement>
		while ((lexer.getCurrentToken().code != Lexer.CONSTANT)&&(statement())){
			statement();
		}
		//checks for <asignacion>
		asignacion();
	}
	
		
	/**
		Function argumentList: verifies the production <argumentList> ::= <argumentDef> { <argumentDef> }
	**/
	public void argumentList(){
		//Checks for <argumentDef>
		argumentDef();
		//verifies if there are more <argumentDef>
		while (lexer.getCurrentToken().code != Lexer.RPAREN)
			argumentDef();
	}
	
	/**
		Function argumentDef: verifies the production <argumentDef> ::= <variable>
	**/
	public void argumentDef(){
		recognize(Lexer.VARIABLE);
	}
	
	/**
		Function recognizeVariable: verifies for variables
	**/
	public void recognizeVariable(){
		recognize(Lexer.VARIABLE);
	}
	
	
	public static void main(String args[])
	{
		try {
			Scanner scan = new Scanner(System.in);
			String archivo = scan.next();
			Parser parser = new Parser(archivo);
				} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
