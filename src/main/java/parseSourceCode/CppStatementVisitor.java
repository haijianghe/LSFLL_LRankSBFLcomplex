/**
 * 
 */
package parseSourceCode;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTMacroExpansionLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroExpansion;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTBreakStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTCaseStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTCatchHandler;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTContinueStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTDeclarationStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTDefaultStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTDoStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTExpressionStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTForStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTGotoStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTIfStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTReturnStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSwitchStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTWhileStatement;


/**
 * @author Administrator
 *
 */
public class CppStatementVisitor  extends ASTVisitor {
	private List<StatementContext> statementList; //某个方法的所有语句。注意：尽量以行为单位定义语句，不同于AST的语句概念。
	
	public CppStatementVisitor()
	{
		super(true);//public ASTVisitor(boolean visitNodes)
		statementList = new ArrayList<>();
	}
	//某个方法的所有语句。
	public List<StatementContext> getStatementList() {
		return statementList;
	}
	
	/** 
	 *IASTStatement : 接收各种语句。
	 * assert 语句作为 函数调用。
	 * For语句里的初始化部分IASTForStatement.INITIALIZER特殊，会以CPPASTDeclarationStatement和
	 * 		CPPASTExpressionStatement的形式重复visit。而其它，象while,do while,if等语句则不会；甚至for
	 *     语句的update部分也不会重复visit。
	 */
	@Override
	public int visit(IASTStatement  statement) {
		try {
			if( statement instanceof CPPASTBreakStatement ) //break语句无复杂度，不用添加到语句列表。
				return PROCESS_SKIP;
			else if( statement instanceof  CPPASTCatchHandler ) //catch...
				return 	ProcessCatchHandler((CPPASTCatchHandler)statement);
			else if( statement instanceof  CPPASTContinueStatement )  //continue语句无复杂度，不用添加到语句列表。
				return 	PROCESS_SKIP;
			else if( statement instanceof  CPPASTDeclarationStatement )  //变量声明
			{
				ASTNodeProperty nodeProperty = statement.getPropertyInParent();
				//System.out.println(" ^^^^  "+nodeProperty.getName());
				//IASTWhileStatement.CONDITIONEXPRESSION   IASTIfStatement.CONDITION
				if( nodeProperty==IASTForStatement.INITIALIZER  ) //排除循环里的变量声明
					return 	PROCESS_SKIP;
				return 	ProcessDeclarationStatement((CPPASTDeclarationStatement)statement);
			}
			else if( statement instanceof  CPPASTCaseStatement ) //case...
				return 	ProcessCaseStatement((CPPASTCaseStatement)statement);
			else if( statement instanceof  CPPASTDefaultStatement )  //Default语句无复杂度，不用添加到语句列表。
				return 	PROCESS_SKIP;
			else if( statement instanceof  CPPASTDoStatement )  //do while
				return 	ProcessDoStatement((CPPASTDoStatement)statement);
			else if( statement instanceof  CPPASTWhileStatement )  //while
				return 	ProcessWhileStatement((CPPASTWhileStatement)statement);
			else if( statement instanceof  CPPASTForStatement )  //for...
				return 	ProcessForStatement((CPPASTForStatement)statement);
			else if( statement instanceof  CPPASTIfStatement )  //if...
				return 	ProcessIfStatement((CPPASTIfStatement)statement);
			else if( statement instanceof  CPPASTReturnStatement )  //return...
				return 	ProcessReturnStatement((CPPASTReturnStatement)statement);
			else if( statement instanceof  CPPASTSwitchStatement )  //switch...
				return 	ProcessSwitchStatement((CPPASTSwitchStatement)statement);
			else if( statement instanceof  CPPASTExpressionStatement ) //普通表达式语句
			{
				ASTNodeProperty nodeProperty = statement.getPropertyInParent();
				if( nodeProperty==IASTForStatement.INITIALIZER  ) //排除循环里的表达式语句
					return 	PROCESS_SKIP;
				return 	ProcessExpressionStatement((CPPASTExpressionStatement)statement);
			}
			else if( statement instanceof  CPPASTGotoStatement )  //goto ...
				return 	ProcessGotoStatement((CPPASTGotoStatement)statement);
			else //其它未知语句
				return PROCESS_CONTINUE; 
		}//end of try
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("Parse of statement is error.");
			System.out.println(statement.getRawSignature());
			return PROCESS_ABORT;
		}
	}
	
	/**  计算case 语句的复杂度，并添加到List<StatementContext> statementList。
	 * @param caseStmt
	 * @return case 语句,作为一个整体，不再检索子节点。
	 */
	private int ProcessCaseStatement(CPPASTCaseStatement caseStmt )
	{
		StatementContext stmtCtx = evaluateComplexMetric(caseStmt,StatementContext.IfStyle);
		statementList.add(stmtCtx);
		return PROCESS_SKIP;
	}
	
	/**  计算普通表达式语句的复杂度，并添加到List<StatementContext> statementList。
	 * @param expressStmt 
	 * @return 该语句,作为一个整体，不再检索子节点。
	 */
	private int ProcessExpressionStatement(CPPASTExpressionStatement expressStmt )
	{
		StatementContext stmtCtx = evaluateComplexMetric(expressStmt,StatementContext.StatementRest);
		statementList.add(stmtCtx);
		return PROCESS_SKIP;
	}
	
	/**  计算变量声明语句的复杂度，并添加到List<StatementContext> statementList。
	 * 没有等于号的变量声明语句不是可执行语句，但这里没有排除，对后期的研究工作并无影响。
	 * @param declarStmt 
	 * @return 该语句,作为一个整体，不再检索子节点。
	 */
	private int ProcessDeclarationStatement(CPPASTDeclarationStatement declarStmt )
	{
		StatementContext stmtCtx = evaluateComplexMetric(declarStmt,StatementContext.StatementRest);
		statementList.add(stmtCtx);
		return PROCESS_SKIP;
	}
	
	/**  catch handler语句，包括catch和处理部分，处理部分用其它语句计算复杂度。
	 *      此部分只计算catch的复杂度
	 * @param catchHandler 
	 * @return 检索子节点的表达式。
	 */
	private int ProcessCatchHandler(CPPASTCatchHandler catchHandler )
	{
		StatementContext stmtContext = new StatementContext();
		stmtContext.setStatementStyle(StatementContext.IfStyle);

		IASTFileLocation  fileLocation = catchHandler.getFileLocation();
		//获得起始结束行号
		int startLine = fileLocation.getStartingLineNumber();
		int endLine = fileLocation.getEndingLineNumber();
		/*
		 * 结束行号，不能将handler部分包括进去。
		 * 1，针对catch(...)，无法判断)的位置，将{的起始行归于catch的结束行，对最终结果影响不大。
		 * 2, 针对carch(other),将other的结束行，归于catch的结束行，对最终结果影响不大。
		 */
		if( catchHandler.isCatchAll() )
		{
			IASTStatement astBody = catchHandler.getCatchBody();
			fileLocation = astBody.getFileLocation();
			int bodyStartLine = fileLocation.getStartingLineNumber();
			if( endLine>bodyStartLine )
				endLine = bodyStartLine;
		}
		else
		{
			IASTDeclaration astDeclar = catchHandler.getDeclaration();
			fileLocation = astDeclar.getFileLocation();
			endLine = fileLocation.getEndingLineNumber();
			/*
			 * 简化问题，不处理catch中的表达式，一般来说，这些表达式并无XX复杂度。
			 */
		}
		stmtContext.setStartLine(startLine);
		stmtContext.setEndLine(endLine);
		
		statementList.add(stmtContext);
		return PROCESS_CONTINUE; 
	}
	
	/**  for 语句，。
	 * @param forStatement 
	 * @return 允许嵌套，检索子节点。
	 */
	private int ProcessForStatement(CPPASTForStatement forStatement )
	{
		boolean isSpecialFor = true;  //for(;;)这种for语句没有ForInit，Expression和ForUpdate
		StatementContext forContext = new StatementContext();
		//for语句的初始化部分。
		IASTStatement initialStmt = forStatement.getInitializerStatement();
		if(  initialStmt!=null )
		{
			isSpecialFor = false; //有ForInit
			StatementContext statement = evaluateComplexMetric(initialStmt,StatementContext.StatementRest);
			forContext.enlargeToComplexStatement(statement);
		}
		//forContext
		IASTExpression 	forExpress = forStatement.getConditionExpression();
		if( forExpress!=null )
		{
			isSpecialFor = false; //有Expression
			StatementContext statement = evaluateComplexMetric(forExpress,StatementContext.StatementRest);
			forContext.enlargeToComplexStatement(statement);
		}
		//for语句的update部分。
		IASTExpression 	forUpdates = forStatement.getIterationExpression();
		if( forUpdates!=null )
		{
			isSpecialFor = false; //有ForUpdate
			StatementContext statement = evaluateComplexMetric(forUpdates,StatementContext.StatementRest);
			forContext.enlargeToComplexStatement(statement);
		}		
		//for语句的三部分组成一个StatementContext，加入到语句列表。
		//担心for语句里 for 单独一行，那么该行号将不会作为整个for语句的起始行号。
		IASTFileLocation  fileLocation = forStatement.getFileLocation();
		int startLine = fileLocation.getStartingLineNumber();
		if( isSpecialFor )
		{ //for(;;)这种for语句没有ForInit，Expression和ForUpdate
			forContext.setStartLine(startLine);
			forContext.setEndLine(startLine); //不好取结束行。用起始行不准确，但不会影响SBFL。
		}
		else
			forContext.enlargeStartLineno(startLine);
		forContext.setStatementStyle(StatementContext.LoopStyle);
		statementList.add(forContext);
		return PROCESS_CONTINUE; //允许嵌套，搜索子节点
	}
	
	/**  do while语句，do不是可执行语句，loop不用处理，只计算while这句的复杂度。
	 * @param doStatement 
	 * @return 允许嵌套，检索子节点。
	 */
	private int ProcessDoStatement(CPPASTDoStatement doStatement )
	{
		IASTExpression 	astExpression = doStatement.getCondition();
		StatementContext stmtContext = evaluateComplexMetric(astExpression,StatementContext.LoopStyle);
		
		//body的结束行号作为它的起始行号，虽然不准确，但不会出错。
		IASTStatement astBody = doStatement.getBody();
		IASTFileLocation fileLocation = astBody.getFileLocation();
		int bodyEndLine = fileLocation.getEndingLineNumber();
		stmtContext.enlargeStartLineno(bodyEndLine);
		statementList.add(stmtContext);
		return PROCESS_CONTINUE; //允许嵌套，搜索子节点
	}
	
	/**  if 语句，then不是可执行语句，只计算if这句的复杂度。
	 * else 后可能跟if
	 * @param ifStatement 
	 * @return 允许嵌套，检索子节点。
	 */
	private int ProcessIfStatement(CPPASTIfStatement ifStatement )
	{
		IASTExpression 	astExpression = ifStatement.getConditionExpression();
		StatementContext stmtCtx;
		if( astExpression==null )
		{  //当形如	if( int ppp= Overview) 的语句出现时，if条件里并非表达式，而是声明语句。
			IASTDeclaration astDeclar = ifStatement.getConditionDeclaration();
			stmtCtx= evaluateComplexMetric(astDeclar,StatementContext.IfStyle);
		}
		else
			stmtCtx= evaluateComplexMetric(astExpression,StatementContext.IfStyle);
		//担心if语句里 if 单独一行，那么该行号将不会作为整个if()语句的起始行号。
		IASTFileLocation fileLocation = ifStatement.getFileLocation();
		int startLine = fileLocation.getStartingLineNumber();
		stmtCtx.enlargeStartLineno(startLine);
		statementList.add(stmtCtx);
		return PROCESS_CONTINUE; //允许嵌套，搜索子节点
	}
	
	/**  Return语句。
	 * @param returnStmt 
	 * @return 该语句,作为一个整体，不再检索子节点。
	 */
	private int ProcessReturnStatement(CPPASTReturnStatement returnStmt )
	{
		StatementContext stmtCtx = evaluateComplexMetric(returnStmt,StatementContext.ReturnStyle);
		statementList.add(stmtCtx);
		return PROCESS_SKIP;//视作一条完整语句，不检索子节点。
	}
	
	/**  switch语句。
	 * @param switchStmt 
	 * @return 
	 */
	private int ProcessSwitchStatement(CPPASTSwitchStatement switchStmt )
	{
		IASTExpression 	astExpression = switchStmt.getControllerExpression();
		StatementContext stmtCtx = evaluateComplexMetric(astExpression,StatementContext.IfStyle);
		//担心switch语句里 switch 单独一行，那么该行号将不会作为整个switch()语句的起始行号。
		IASTFileLocation fileLocation = switchStmt.getFileLocation();
		int startLine = fileLocation.getStartingLineNumber();
		stmtCtx.enlargeStartLineno(startLine);
		
		statementList.add(stmtCtx);
		return PROCESS_CONTINUE;//允许嵌套，搜索子节点
	}
	
	/**  while语句。
	 * @param whileStmt 
	 * @return 
	 */
	private int ProcessWhileStatement(CPPASTWhileStatement whileStmt )
	{
		IASTExpression 	astExpression = whileStmt.getCondition();
		StatementContext stmtCtx;
		if( astExpression==null )
		{  //当形如	while( int k=len++ ) 的语句出现时，while条件里并非表达式，而是声明语句。
			IASTDeclaration astDeclar = whileStmt.getConditionDeclaration();
			stmtCtx= evaluateComplexMetric(astDeclar,StatementContext.LoopStyle);
		}
		else
			stmtCtx= evaluateComplexMetric(astExpression,StatementContext.LoopStyle);
		//担心while语句里 while 单独一行，那么该行号将不会作为整个while()语句的起始行号。
		IASTFileLocation fileLocation = whileStmt.getFileLocation();
		int startLine = fileLocation.getStartingLineNumber();
		stmtCtx.enlargeStartLineno(startLine);
		
		statementList.add(stmtCtx);
		return PROCESS_CONTINUE;//允许嵌套，搜索子节点
	}
	
	/**  goto 语句。
	 * @param gotoStmt 
	 * @return 该语句,作为一个整体，不再检索子节点。
	 */
	private int ProcessGotoStatement(CPPASTGotoStatement gotoStmt )
	{
		StatementContext stmtCtx = evaluateComplexMetric(gotoStmt,StatementContext.StatementRest);
		statementList.add(stmtCtx);
		return PROCESS_SKIP;//视作一条完整语句，不检索子节点。
	}
	
	/**   当前节点作为一条语句，整体地评估其认知复杂度。
	 * @param statement  包括定义语句，表达式语句，if,for...
	 * @return
	 */
	private StatementContext evaluateComplexMetric(IASTStatement  statement,int style)
	{
		return evaluateComplexMetric((IASTNode)statement,style);
	}
	
	/**   当前节点作为一个表达式，整体地评估其认知复杂度。
	 * @param expression  各种表达式
	 * @return
	 */
	private StatementContext evaluateComplexMetric(IASTExpression  expression,int style)
	{
		return evaluateComplexMetric((IASTNode)expression,style);
	}
	
	/**   当前节点作为一个表达式，整体地评估其认知复杂度。
	 * @param declaration  变量声明
	 * @return
	 */
	private StatementContext evaluateComplexMetric(IASTDeclaration  declaration,int style)
	{
		return evaluateComplexMetric((IASTNode)declaration,style);
	}
	
	/**   为不同的对象来调用，产生带复杂度的语句计算。
	 * @param astNode  : 表达式，普通语句，变量声明，...
	 * @return
	 */
	private StatementContext evaluateComplexMetric(IASTNode  astNode,int style)
	{
		StatementContext stmtContext = new StatementContext();
		stmtContext.setStatementStyle(style); //语句类型。当前不能嵌套。

		IASTFileLocation  fileLocation = astNode.getFileLocation();
		//获得起始结束行号
		int startLine = fileLocation.getStartingLineNumber();
		int endLine = fileLocation.getEndingLineNumber();
		stmtContext.setStartLine(startLine);
		stmtContext.setEndLine(endLine);
		CppFragmentVisitor fragmentVisitor= new CppFragmentVisitor();
		astNode.accept(fragmentVisitor);
		//先清洗标识符，再将其赋值给语句。
		fragmentVisitor.excludeInvoMethodFromIdentiferName();
		stmtContext.setSimpleNames(fragmentVisitor.getIdentiferNames());
		stmtContext.setFuncCalls(fragmentVisitor.getInvoMethods());
		stmtContext.setLogicOperaters(fragmentVisitor.getLogicOperators());
		stmtContext.setOtherOperaters(fragmentVisitor.getStrayOperators());

		//检查该语句，或者表达式里是否有宏定义，有的话，加入语句的simpleNames；将来添加复杂度。
		IASTNodeLocation[] location = astNode.getNodeLocations();
		for (IASTNodeLocation loc : location) {
		    if (loc instanceof IASTMacroExpansionLocation) {
		    	IASTPreprocessorMacroExpansion iapmExpansion = ((IASTMacroExpansionLocation)loc).getExpansion();
		    	// IASTPreprocessorMacroDefinition imd= iapme.getMacroDefinition(); //<< returns the macro that generated "node"
		        IASTName macroName = iapmExpansion.getMacroReference();
		        stmtContext.addMacroDefinitionName(macroName.getLastName().toString());
		    }
		}
		return stmtContext;
	}
}
