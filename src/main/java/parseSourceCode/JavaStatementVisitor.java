/**
 * 
 */
package parseSourceCode;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

/** java语句的解析
 *      为简化问题，也符合以行为单位计算复杂度的情形，不考虑表达式语句和变量声明内的嵌套语句。
 * @author Administrator
 *Direct Known Subclasses:
 *AssertStatement, Block, BreakStatement, ConstructorInvocation, ContinueStatement, DoStatement, 
 *EmptyStatement, EnhancedForStatement, ExpressionStatement, ForStatement, IfStatement, 
 *LabeledStatement, ReturnStatement, SuperConstructorInvocation, SwitchCase, SwitchStatement, 
 *SynchronizedStatement, ThrowStatement, TryStatement, TypeDeclarationStatement, VariableDeclarationStatement,
 * WhileStatement
 */
public class JavaStatementVisitor extends ASTVisitor{
	private List<StatementContext> statementList; //某个方法的所有语句。注意：尽量以行为单位定义语句，不同于AST的语句概念。
	private CompilationUnit unitCompile; //为获取行号。
	//private JavaFragmentVisitor fragmentVisitor; //读取代码片段，计算复杂度。
	
	public JavaStatementVisitor(CompilationUnit unit)
	{
		statementList = new ArrayList<>();
		unitCompile = unit;
		//fragmentVisitor = new JavaFragmentVisitor(); //这样行不通。
	}
	
	//某个方法的所有语句。
	public List<StatementContext> getStatementList() {
		return statementList;
	}
	
	/**   当前节点作为一条语句，整体地评估其认知复杂度。
	 * @param node  包括定义语句，表达式语句，if,for...
	 * @param style 语句类型。
	 * @return
	 */
	private StatementContext evaluateComplexMetric(ASTNode node,int style)
	{
		StatementContext statement = new StatementContext();
		statement.setStatementStyle(style); //语句类型。当前不能嵌套。
		int startLine = unitCompile.getLineNumber(node.getStartPosition());
		int endLine = unitCompile.getLineNumber(node.getStartPosition()+node.getLength()-1);
		statement.setStartLine(startLine);
		statement.setEndLine(endLine);
		JavaFragmentVisitor fragmentVisitor= new JavaFragmentVisitor();
		node.accept(fragmentVisitor);
		//先清洗标识符，再将其赋值给语句。
		fragmentVisitor.excludeInvoMethodFromIdentiferName();
		statement.setSimpleNames(fragmentVisitor.getIdentiferNames());
		statement.setFuncCalls(fragmentVisitor.getInvoMethods());
		statement.setLogicOperaters(fragmentVisitor.getLogicOperators());
		statement.setOtherOperaters(fragmentVisitor.getStrayOperators());
		return statement;
	}
	
	/**	 * VariableDeclarationStatement 变量声明语句。
	 * 有两种变量声明语句，例如： int  diselA;   int diselA=5;
	 *                   前者不可执行，后者为可执行语句
	 *   但是，此visit都会捕获，懒得去识别了。实际上，并不会影响研究工作。因为，不可执行语句的数据会丢弃。
	 * @param vdStatement
	 * @return
	 */
	@Override
	public boolean visit(VariableDeclarationStatement vdStatement) {
		StatementContext statement = evaluateComplexMetric(vdStatement,StatementContext.StatementRest);
		statementList.add(statement);
		return false;//无须检索子节点。
	}
	
	/** ExpressionStatement 表达式语句。
	 * @param expStatement  由单个表达式组成的语句
	 * @return
	 */
	@Override
	public boolean visit(ExpressionStatement expStatement) {
		StatementContext statement = evaluateComplexMetric(expStatement,StatementContext.StatementRest);
		statementList.add(statement);
		return false;//无须检索子节点。
	}
	
	
	/** TypeDeclarationStatement  语句。 通常定义类、接口、enum；由多条语句组成，不再捕获它。
	 * 格式：TypeDeclaration
	 *       EnumDeclaration
	 * @param tdeStatement 
	 * @return
	 */
/*	@Override
	public boolean visit(TypeDeclarationStatement tdeStatement) {
		StatementContext statement = evaluateComplexMetric(tdeStatement);
		statementList.add(statement);
		return true;
	}*/

	/** AssertStatement assert 语句。
	 * 格式：assert Expression [ : Expression ] ;
	 * @param assertStatement 
	 * @return
	 */
	@Override
	public boolean visit(AssertStatement assertStatement) {
		StatementContext statement = evaluateComplexMetric(assertStatement,StatementContext.IfStyle);
		statementList.add(statement);
		return false;//视作一条完整语句，不检索子节点。
	}
	
	/** ConstructorInvocation:  构造器的第一条语句，this();
	 * 格式： [ < Type { , Type } > ] this ( [ Expression { , Expression } ] ) ;。
	 * @param ciStatement
	 * @return
	 */
	@Override
	public boolean visit(ConstructorInvocation ciStatement) {
		StatementContext statement = evaluateComplexMetric(ciStatement,StatementContext.StatementRest);
		statementList.add(statement);
		return false;//视作一条完整语句，不检索子节点。
	}
	
	/** SuperConstructorInvocation:
	 * 格式： [ Expression . ]        
	 *            [ < Type { , Type } > ]
	 *             super ( [ Expression { , Expression } ] ) ;
	 * @param superStatement
	 * @return
	 */
	@Override
	public boolean visit(SuperConstructorInvocation superStatement) {
		StatementContext statement = evaluateComplexMetric(superStatement,StatementContext.StatementRest);
		statementList.add(statement);
		return false;//视作一条完整语句，不检索子节点。
	}
	
	/** break 语句:
	 * 格式： break [ Identifier ] ;
	 * @param breakStatement
	 * @return
	 */
	@Override
	public boolean visit(BreakStatement breakStatement) {
		StatementContext statement = evaluateComplexMetric(breakStatement,StatementContext.StatementRest);
		statementList.add(statement);
		return false;//视作一条完整语句，不检索子节点。
	}
	
	/** ContinueStatement 语句:
	 *  格式：continue  [ Identifier ] ;
	 * @param continueStatement
	 * @return
	 */
	@Override
	public boolean visit(ContinueStatement continueStatement) {
		StatementContext statement = evaluateComplexMetric(continueStatement,StatementContext.StatementRest);
		statementList.add(statement);
		return false;//视作一条完整语句，不检索子节点。
	}
	
	/** LabeledStatement 语句:
	 *  格式：Identifier : Statement
	 * @param labelStatement
	 * @return
	 */
	@Override
	public boolean visit(LabeledStatement labelStatement) {
		/*
		 * label: 其后的语句才是重点考察对象，不考虑label这个符号。
		 */
		return true;
	}
	
	/** ReturnStatement 语句:
	 *  格式：return [ Expression ] ;
	 * @param returnStatement
	 * @return
	 */
	@Override
	public boolean visit(ReturnStatement returnStatement) {
		StatementContext statement = evaluateComplexMetric(returnStatement,StatementContext.ReturnStyle);
		statementList.add(statement);
		return false;//视作一条完整语句，不检索子节点。
	}
	/** ForStatement For语句，作为一个整体，初始化、中间条件表达式、更新三部分复杂度累加。
	 * 格式： for (  [ ForInit ];     [ Expression ] ;        [ ForUpdate ] )
                        Statement
	 * @param forStatement   复杂度+5
	 * @return
	 */
	@Override
	public boolean visit(ForStatement forStatement) {
		boolean isSpecialFor = true;  //for(;;)这种for语句没有ForInit，Expression和ForUpdate
		StatementContext forContext = new StatementContext();
		//for语句的初始化部分。
		List<?> forInits = forStatement.initializers();
		for(  Object obj : forInits )
		{
			isSpecialFor = false; //有ForInit
			Expression express = (Expression)obj;
			StatementContext statement = evaluateComplexMetric(express,StatementContext.StatementRest);
			forContext.enlargeToComplexStatement(statement);
		}
		//forContext
		Expression	forExpress = forStatement.getExpression();
		if( forExpress!=null )
		{
			isSpecialFor = false; //有Expression
			StatementContext statement = evaluateComplexMetric(forExpress,StatementContext.StatementRest);
			forContext.enlargeToComplexStatement(statement);
		}
		//for语句的update部分。
		List<?> forUpdates = forStatement.updaters();
		for(  Object obj : forUpdates )
		{
			isSpecialFor = false; //有ForUpdate
			Expression express = (Expression)obj;
			StatementContext statement = evaluateComplexMetric(express,StatementContext.StatementRest);
			forContext.enlargeToComplexStatement(statement);
		}		
		//for语句的三部分组成一个StatementContext，加入到语句列表。
		//担心for语句里 for 单独一行，那么该行号将不会作为整个for语句的起始行号。
		int startLine = unitCompile.getLineNumber(forStatement.getStartPosition());
		if( isSpecialFor )
		{ //for(;;)这种for语句没有ForInit，Expression和ForUpdate
			forContext.setStartLine(startLine);
			forContext.setEndLine(startLine); //不好取结束行。用起始行不准确，但不会影响SBFL。
		}
		else
			forContext.enlargeStartLineno(startLine);
		//放在前面会出错，放在最后不会出错。
		forContext.setStatementStyle(StatementContext.LoopStyle);
		statementList.add(forContext);
		return true; //必须true,搜索其子节点。
	}
	
	/** EnhancedForStatement 增强型For语句，作为一个整体，FormalParameter、Expression两部分复杂度累加。
	 * 格式： for ( FormalParameter : Expression )
	 *                   Statement
	 * @param enforStatement   复杂度+5
	 * @return
	 */
	@Override
	public boolean visit(EnhancedForStatement enforStatement) {
		StatementContext enforContext = new StatementContext();
		//增强型for语句的FormalParameter部分。
		SingleVariableDeclaration svd = enforStatement.getParameter();
		StatementContext statement = evaluateComplexMetric(svd,StatementContext.StatementRest);
		enforContext.enlargeToComplexStatement(statement);
		//Expression部分。
		Expression	express = enforStatement.getExpression();
		statement = evaluateComplexMetric(express,StatementContext.StatementRest);
		enforContext.enlargeToComplexStatement(statement);
		//增强型for语句的两部分组成一个StatementContext，加入到语句列表。
		//担心foreach语句里 foreach 单独一行，那么该行号将不会作为整个foreach语句的起始行号。
		int startLine = unitCompile.getLineNumber(enforStatement.getStartPosition());
		enforContext.enlargeStartLineno(startLine);
		statementList.add(enforContext);
		//放在前面会出错，放在最后不会出错。
		enforContext.setStatementStyle(StatementContext.LoopStyle);
		return true; //必须true,搜索其子节点。
	}
	
	/** DoStatement do语句，作为一个整体。
	 * 格式：  do Statement while ( Expression ) ;
	 * @param doStatement   复杂度+5
	 * @return
	 */
	@Override
	public boolean visit(DoStatement doStatement) {
		/*此部分有点问题，如果while和表达式不在一行，则该StatementContext的行号会出问题。
		 *解决思路，先通过visitor找出while这个 SimpleName，再通过unitCompile获取其行号。
		 *以后再修改。 		
		 */
		StatementContext statement = evaluateComplexMetric(doStatement.getExpression(),StatementContext.LoopStyle);
		statementList.add(statement);
		return true; //必须true,搜索其子节点。
	}	
	
	/** WhileStatement while语句，作为一个整体。
	 * 格式：  while ( Expression ) Statement
	 * @param whileStatement   复杂度+6
	 * @return
	 */
	@Override
	public boolean visit(WhileStatement whileStatement) {
		StatementContext statement = evaluateComplexMetric(whileStatement.getExpression(),StatementContext.LoopStyle);
		//担心WhileStatement语句里 while 单独一行，那么该行号将不会作为整个while语句的起始行号。
		int startLine = unitCompile.getLineNumber(whileStatement.getStartPosition());
		statement.enlargeStartLineno(startLine);		
		statementList.add(statement);
		return true; //必须true,搜索其子节点。
	}	
	
	/** IfStatement if语句，作为一个整体。
	 * 格式：  if ( Expression ) Statement [ else Statement]
	 * @param ifStatement   复杂度+3
	 * @return
	 */
	@Override
	public boolean visit(IfStatement ifStatement) {
		StatementContext statement = evaluateComplexMetric(ifStatement.getExpression(),StatementContext.IfStyle);
		//担心if语句里 if 单独一行，那么该行号将不会作为整个if()语句的起始行号。
		int startLine = unitCompile.getLineNumber(ifStatement.getStartPosition());
		statement.enlargeStartLineno(startLine);
		statementList.add(statement);
		return true; //必须true,搜索其子节点。
	}	
	
	/** SwitchStatement switch case语句，作为一个整体。
	 * 格式：  switch ( Expression )
	 *               { { SwitchCase | Statement } }
	 * @param switchStatement   复杂度+1
	 * @return
	 */
	@Override
	public boolean visit(SwitchStatement switchStatement) {
		StatementContext statement = evaluateComplexMetric(switchStatement.getExpression(),StatementContext.IfStyle);
		//担心switch语句里 switch 单独一行，那么该行号将不会作为整个switch()语句的起始行号。
		int startLine = unitCompile.getLineNumber(switchStatement.getStartPosition());
		statement.enlargeStartLineno(startLine);
		statementList.add(statement);
		return true; //必须true,搜索其子节点。
	}	
	
	/** SwitchCase switch case语句，作为一个整体。
	 * 格式： case Expression  :
	 *        default :
	 * @param caseStatement   复杂度+1
	 * @return
	 */
	@Override
	public boolean visit(SwitchCase caseStatement) {
		Expression express = caseStatement.getExpression();
		if( express==null ) 
		{//default语句，不计算其复杂度，该语句通常也不是可执行语句。
			return true;
		}
		StatementContext statement = evaluateComplexMetric(express,StatementContext.IfStyle);
		//担心case语句里 case 单独一行，那么该行号将不会作为整个case语句的起始行号。
		int startLine = unitCompile.getLineNumber(caseStatement.getStartPosition());
		statement.enlargeStartLineno(startLine);
		statementList.add(statement);
		return true; //必须true,搜索其子节点。
	}	
	
	/** SynchronizedStatement Synchronized语句，作为一个整体。
	 * 没有考虑如下的用法，
	 *           public synchronized void accessVal(int newVal);  
	 * 格式：  synchronized ( Expression ) Block
	 * @param synchStatement   复杂度+4
	 * @return
	 */
	@Override
	public boolean visit(SynchronizedStatement synchStatement) {
		StatementContext statement = evaluateComplexMetric(synchStatement.getExpression(),StatementContext.StatementRest);
		//担心SynchronizedStatement语句里 Synchronized 单独一行，那么该行号将不会作为整个Synchronized语句的起始行号。
		int startLine = unitCompile.getLineNumber(synchStatement.getStartPosition());
		statement.enlargeStartLineno(startLine);
		statementList.add(statement);
		return true; //必须true,搜索其子节点。
	}	
	
	/** ThrowStatement throw语句，作为一个整体。
	 * 格式：  throw Expression ;
	 * @param throwStatement   复杂度+1
	 * @return
	 */
	@Override
	public boolean visit(ThrowStatement throwStatement) {
		StatementContext statement = evaluateComplexMetric(throwStatement,StatementContext.StatementRest);
		statementList.add(statement);
/*这里简化了问题，类似thorw new Exception()这样的语句，并没有加上方法调用的复杂度。
 * 		实际程序，可能更复杂，还要加上其它可能情况造成的复杂度。
 * 要解决问题的话，又不修改其它代码，在此处加上如下代码：
 *       .accept(  new ASTVisitor() { 
			public boolean visit(...) { 
 */
		return false;//视作一条完整语句，不检索子节点。
	}	
	/** TryStatement try语句，分散成多个部分。
	 * 格式：   try [ ( Resources ) ]
	 *          	Block
	 *          [ { CatchClause } ]
	 *          [ finally Block ] 
	 * @param tryStatement   try以及每个CatchClause复杂度+2  finally并非可执行语句。
	 * @return
	 */
	@Override
	public boolean visit(TryStatement tryStatement) {
		List<?> tryResources = tryStatement.resources();
		if( tryResources.size()>0 )
		{ // try [ ( Resources ) ]作为一个整体。
			StatementContext resourceContext = new StatementContext();
			for( Object obj : tryResources )
			{
				Expression express = (Expression)obj;
				StatementContext statement = evaluateComplexMetric(express,StatementContext.StatementRest);
				resourceContext.enlargeToComplexStatement(statement);
			}
			//担心try语句里 try 单独一行，那么该行号将不会作为整个try语句的起始行号。
			int startLine = unitCompile.getLineNumber(tryStatement.getStartPosition());
			resourceContext.enlargeStartLineno(startLine);
			//放在前面会出错，放在最后不会出错。
			resourceContext.setStatementStyle(StatementContext.IfStyle);
			statementList.add(resourceContext);
		}
		/*每个catch字句都是一个整体可执行语句，
		CatchClause格式:
		    catch ( FormalParameter ) Block
		    The FormalParameter is represented by a SingleVariableDeclaration.
		*/
		List<?> catchCauchLst = tryStatement.catchClauses();
		for( Object obj: catchCauchLst)
		{
			CatchClause cClause = (CatchClause)obj;
			//CatchClause语句的FormalParameter部分。
			SingleVariableDeclaration svdCatch = cClause.getException();
			StatementContext statement = evaluateComplexMetric(svdCatch,StatementContext.IfStyle);
			//担心catch语句里 catch 单独一行，那么该行号将不会作为整个catch语句的起始行号。
			int startLine = unitCompile.getLineNumber(cClause.getStartPosition());
			statement.enlargeStartLineno(startLine);
			statementList.add(statement); //每个CatchClause都是一个完整的可执行语句，复杂度+2
		}
		return true; //必须true,搜索其子节点。
	}		
}
