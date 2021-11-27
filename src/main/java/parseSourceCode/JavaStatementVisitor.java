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

/** java���Ľ���
 *      Ϊ�����⣬Ҳ��������Ϊ��λ���㸴�Ӷȵ����Σ������Ǳ��ʽ���ͱ��������ڵ�Ƕ����䡣
 * @author Administrator
 *Direct Known Subclasses:
 *AssertStatement, Block, BreakStatement, ConstructorInvocation, ContinueStatement, DoStatement, 
 *EmptyStatement, EnhancedForStatement, ExpressionStatement, ForStatement, IfStatement, 
 *LabeledStatement, ReturnStatement, SuperConstructorInvocation, SwitchCase, SwitchStatement, 
 *SynchronizedStatement, ThrowStatement, TryStatement, TypeDeclarationStatement, VariableDeclarationStatement,
 * WhileStatement
 */
public class JavaStatementVisitor extends ASTVisitor{
	private List<StatementContext> statementList; //ĳ��������������䡣ע�⣺��������Ϊ��λ������䣬��ͬ��AST�������
	private CompilationUnit unitCompile; //Ϊ��ȡ�кš�
	//private JavaFragmentVisitor fragmentVisitor; //��ȡ����Ƭ�Σ����㸴�Ӷȡ�
	
	public JavaStatementVisitor(CompilationUnit unit)
	{
		statementList = new ArrayList<>();
		unitCompile = unit;
		//fragmentVisitor = new JavaFragmentVisitor(); //�����в�ͨ��
	}
	
	//ĳ��������������䡣
	public List<StatementContext> getStatementList() {
		return statementList;
	}
	
	/**   ��ǰ�ڵ���Ϊһ����䣬�������������֪���Ӷȡ�
	 * @param node  ����������䣬���ʽ��䣬if,for...
	 * @param style ������͡�
	 * @return
	 */
	private StatementContext evaluateComplexMetric(ASTNode node,int style)
	{
		StatementContext statement = new StatementContext();
		statement.setStatementStyle(style); //������͡���ǰ����Ƕ�ס�
		int startLine = unitCompile.getLineNumber(node.getStartPosition());
		int endLine = unitCompile.getLineNumber(node.getStartPosition()+node.getLength()-1);
		statement.setStartLine(startLine);
		statement.setEndLine(endLine);
		JavaFragmentVisitor fragmentVisitor= new JavaFragmentVisitor();
		node.accept(fragmentVisitor);
		//����ϴ��ʶ�����ٽ��丳ֵ����䡣
		fragmentVisitor.excludeInvoMethodFromIdentiferName();
		statement.setSimpleNames(fragmentVisitor.getIdentiferNames());
		statement.setFuncCalls(fragmentVisitor.getInvoMethods());
		statement.setLogicOperaters(fragmentVisitor.getLogicOperators());
		statement.setOtherOperaters(fragmentVisitor.getStrayOperators());
		return statement;
	}
	
	/**	 * VariableDeclarationStatement ����������䡣
	 * �����ֱ���������䣬���磺 int  diselA;   int diselA=5;
	 *                   ǰ�߲���ִ�У�����Ϊ��ִ�����
	 *   ���ǣ���visit���Ჶ������ȥʶ���ˡ�ʵ���ϣ�������Ӱ���о���������Ϊ������ִ���������ݻᶪ����
	 * @param vdStatement
	 * @return
	 */
	@Override
	public boolean visit(VariableDeclarationStatement vdStatement) {
		StatementContext statement = evaluateComplexMetric(vdStatement,StatementContext.StatementRest);
		statementList.add(statement);
		return false;//��������ӽڵ㡣
	}
	
	/** ExpressionStatement ���ʽ��䡣
	 * @param expStatement  �ɵ������ʽ��ɵ����
	 * @return
	 */
	@Override
	public boolean visit(ExpressionStatement expStatement) {
		StatementContext statement = evaluateComplexMetric(expStatement,StatementContext.StatementRest);
		statementList.add(statement);
		return false;//��������ӽڵ㡣
	}
	
	
	/** TypeDeclarationStatement  ��䡣 ͨ�������ࡢ�ӿڡ�enum���ɶ��������ɣ����ٲ�������
	 * ��ʽ��TypeDeclaration
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

	/** AssertStatement assert ��䡣
	 * ��ʽ��assert Expression [ : Expression ] ;
	 * @param assertStatement 
	 * @return
	 */
	@Override
	public boolean visit(AssertStatement assertStatement) {
		StatementContext statement = evaluateComplexMetric(assertStatement,StatementContext.IfStyle);
		statementList.add(statement);
		return false;//����һ��������䣬�������ӽڵ㡣
	}
	
	/** ConstructorInvocation:  �������ĵ�һ����䣬this();
	 * ��ʽ�� [ < Type { , Type } > ] this ( [ Expression { , Expression } ] ) ;��
	 * @param ciStatement
	 * @return
	 */
	@Override
	public boolean visit(ConstructorInvocation ciStatement) {
		StatementContext statement = evaluateComplexMetric(ciStatement,StatementContext.StatementRest);
		statementList.add(statement);
		return false;//����һ��������䣬�������ӽڵ㡣
	}
	
	/** SuperConstructorInvocation:
	 * ��ʽ�� [ Expression . ]        
	 *            [ < Type { , Type } > ]
	 *             super ( [ Expression { , Expression } ] ) ;
	 * @param superStatement
	 * @return
	 */
	@Override
	public boolean visit(SuperConstructorInvocation superStatement) {
		StatementContext statement = evaluateComplexMetric(superStatement,StatementContext.StatementRest);
		statementList.add(statement);
		return false;//����һ��������䣬�������ӽڵ㡣
	}
	
	/** break ���:
	 * ��ʽ�� break [ Identifier ] ;
	 * @param breakStatement
	 * @return
	 */
	@Override
	public boolean visit(BreakStatement breakStatement) {
		StatementContext statement = evaluateComplexMetric(breakStatement,StatementContext.StatementRest);
		statementList.add(statement);
		return false;//����һ��������䣬�������ӽڵ㡣
	}
	
	/** ContinueStatement ���:
	 *  ��ʽ��continue  [ Identifier ] ;
	 * @param continueStatement
	 * @return
	 */
	@Override
	public boolean visit(ContinueStatement continueStatement) {
		StatementContext statement = evaluateComplexMetric(continueStatement,StatementContext.StatementRest);
		statementList.add(statement);
		return false;//����һ��������䣬�������ӽڵ㡣
	}
	
	/** LabeledStatement ���:
	 *  ��ʽ��Identifier : Statement
	 * @param labelStatement
	 * @return
	 */
	@Override
	public boolean visit(LabeledStatement labelStatement) {
		/*
		 * label: �����������ص㿼����󣬲�����label������š�
		 */
		return true;
	}
	
	/** ReturnStatement ���:
	 *  ��ʽ��return [ Expression ] ;
	 * @param returnStatement
	 * @return
	 */
	@Override
	public boolean visit(ReturnStatement returnStatement) {
		StatementContext statement = evaluateComplexMetric(returnStatement,StatementContext.ReturnStyle);
		statementList.add(statement);
		return false;//����һ��������䣬�������ӽڵ㡣
	}
	/** ForStatement For��䣬��Ϊһ�����壬��ʼ�����м��������ʽ�����������ָ��Ӷ��ۼӡ�
	 * ��ʽ�� for (  [ ForInit ];     [ Expression ] ;        [ ForUpdate ] )
                        Statement
	 * @param forStatement   ���Ӷ�+5
	 * @return
	 */
	@Override
	public boolean visit(ForStatement forStatement) {
		boolean isSpecialFor = true;  //for(;;)����for���û��ForInit��Expression��ForUpdate
		StatementContext forContext = new StatementContext();
		//for���ĳ�ʼ�����֡�
		List<?> forInits = forStatement.initializers();
		for(  Object obj : forInits )
		{
			isSpecialFor = false; //��ForInit
			Expression express = (Expression)obj;
			StatementContext statement = evaluateComplexMetric(express,StatementContext.StatementRest);
			forContext.enlargeToComplexStatement(statement);
		}
		//forContext
		Expression	forExpress = forStatement.getExpression();
		if( forExpress!=null )
		{
			isSpecialFor = false; //��Expression
			StatementContext statement = evaluateComplexMetric(forExpress,StatementContext.StatementRest);
			forContext.enlargeToComplexStatement(statement);
		}
		//for����update���֡�
		List<?> forUpdates = forStatement.updaters();
		for(  Object obj : forUpdates )
		{
			isSpecialFor = false; //��ForUpdate
			Expression express = (Expression)obj;
			StatementContext statement = evaluateComplexMetric(express,StatementContext.StatementRest);
			forContext.enlargeToComplexStatement(statement);
		}		
		//for�������������һ��StatementContext�����뵽����б�
		//����for����� for ����һ�У���ô���кŽ�������Ϊ����for������ʼ�кš�
		int startLine = unitCompile.getLineNumber(forStatement.getStartPosition());
		if( isSpecialFor )
		{ //for(;;)����for���û��ForInit��Expression��ForUpdate
			forContext.setStartLine(startLine);
			forContext.setEndLine(startLine); //����ȡ�����С�����ʼ�в�׼ȷ��������Ӱ��SBFL��
		}
		else
			forContext.enlargeStartLineno(startLine);
		//����ǰ������������󲻻����
		forContext.setStatementStyle(StatementContext.LoopStyle);
		statementList.add(forContext);
		return true; //����true,�������ӽڵ㡣
	}
	
	/** EnhancedForStatement ��ǿ��For��䣬��Ϊһ�����壬FormalParameter��Expression�����ָ��Ӷ��ۼӡ�
	 * ��ʽ�� for ( FormalParameter : Expression )
	 *                   Statement
	 * @param enforStatement   ���Ӷ�+5
	 * @return
	 */
	@Override
	public boolean visit(EnhancedForStatement enforStatement) {
		StatementContext enforContext = new StatementContext();
		//��ǿ��for����FormalParameter���֡�
		SingleVariableDeclaration svd = enforStatement.getParameter();
		StatementContext statement = evaluateComplexMetric(svd,StatementContext.StatementRest);
		enforContext.enlargeToComplexStatement(statement);
		//Expression���֡�
		Expression	express = enforStatement.getExpression();
		statement = evaluateComplexMetric(express,StatementContext.StatementRest);
		enforContext.enlargeToComplexStatement(statement);
		//��ǿ��for�������������һ��StatementContext�����뵽����б�
		//����foreach����� foreach ����һ�У���ô���кŽ�������Ϊ����foreach������ʼ�кš�
		int startLine = unitCompile.getLineNumber(enforStatement.getStartPosition());
		enforContext.enlargeStartLineno(startLine);
		statementList.add(enforContext);
		//����ǰ������������󲻻����
		enforContext.setStatementStyle(StatementContext.LoopStyle);
		return true; //����true,�������ӽڵ㡣
	}
	
	/** DoStatement do��䣬��Ϊһ�����塣
	 * ��ʽ��  do Statement while ( Expression ) ;
	 * @param doStatement   ���Ӷ�+5
	 * @return
	 */
	@Override
	public boolean visit(DoStatement doStatement) {
		/*�˲����е����⣬���while�ͱ��ʽ����һ�У����StatementContext���кŻ�����⡣
		 *���˼·����ͨ��visitor�ҳ�while��� SimpleName����ͨ��unitCompile��ȡ���кš�
		 *�Ժ����޸ġ� 		
		 */
		StatementContext statement = evaluateComplexMetric(doStatement.getExpression(),StatementContext.LoopStyle);
		statementList.add(statement);
		return true; //����true,�������ӽڵ㡣
	}	
	
	/** WhileStatement while��䣬��Ϊһ�����塣
	 * ��ʽ��  while ( Expression ) Statement
	 * @param whileStatement   ���Ӷ�+6
	 * @return
	 */
	@Override
	public boolean visit(WhileStatement whileStatement) {
		StatementContext statement = evaluateComplexMetric(whileStatement.getExpression(),StatementContext.LoopStyle);
		//����WhileStatement����� while ����һ�У���ô���кŽ�������Ϊ����while������ʼ�кš�
		int startLine = unitCompile.getLineNumber(whileStatement.getStartPosition());
		statement.enlargeStartLineno(startLine);		
		statementList.add(statement);
		return true; //����true,�������ӽڵ㡣
	}	
	
	/** IfStatement if��䣬��Ϊһ�����塣
	 * ��ʽ��  if ( Expression ) Statement [ else Statement]
	 * @param ifStatement   ���Ӷ�+3
	 * @return
	 */
	@Override
	public boolean visit(IfStatement ifStatement) {
		StatementContext statement = evaluateComplexMetric(ifStatement.getExpression(),StatementContext.IfStyle);
		//����if����� if ����һ�У���ô���кŽ�������Ϊ����if()������ʼ�кš�
		int startLine = unitCompile.getLineNumber(ifStatement.getStartPosition());
		statement.enlargeStartLineno(startLine);
		statementList.add(statement);
		return true; //����true,�������ӽڵ㡣
	}	
	
	/** SwitchStatement switch case��䣬��Ϊһ�����塣
	 * ��ʽ��  switch ( Expression )
	 *               { { SwitchCase | Statement } }
	 * @param switchStatement   ���Ӷ�+1
	 * @return
	 */
	@Override
	public boolean visit(SwitchStatement switchStatement) {
		StatementContext statement = evaluateComplexMetric(switchStatement.getExpression(),StatementContext.IfStyle);
		//����switch����� switch ����һ�У���ô���кŽ�������Ϊ����switch()������ʼ�кš�
		int startLine = unitCompile.getLineNumber(switchStatement.getStartPosition());
		statement.enlargeStartLineno(startLine);
		statementList.add(statement);
		return true; //����true,�������ӽڵ㡣
	}	
	
	/** SwitchCase switch case��䣬��Ϊһ�����塣
	 * ��ʽ�� case Expression  :
	 *        default :
	 * @param caseStatement   ���Ӷ�+1
	 * @return
	 */
	@Override
	public boolean visit(SwitchCase caseStatement) {
		Expression express = caseStatement.getExpression();
		if( express==null ) 
		{//default��䣬�������临�Ӷȣ������ͨ��Ҳ���ǿ�ִ����䡣
			return true;
		}
		StatementContext statement = evaluateComplexMetric(express,StatementContext.IfStyle);
		//����case����� case ����һ�У���ô���кŽ�������Ϊ����case������ʼ�кš�
		int startLine = unitCompile.getLineNumber(caseStatement.getStartPosition());
		statement.enlargeStartLineno(startLine);
		statementList.add(statement);
		return true; //����true,�������ӽڵ㡣
	}	
	
	/** SynchronizedStatement Synchronized��䣬��Ϊһ�����塣
	 * û�п������µ��÷���
	 *           public synchronized void accessVal(int newVal);  
	 * ��ʽ��  synchronized ( Expression ) Block
	 * @param synchStatement   ���Ӷ�+4
	 * @return
	 */
	@Override
	public boolean visit(SynchronizedStatement synchStatement) {
		StatementContext statement = evaluateComplexMetric(synchStatement.getExpression(),StatementContext.StatementRest);
		//����SynchronizedStatement����� Synchronized ����һ�У���ô���кŽ�������Ϊ����Synchronized������ʼ�кš�
		int startLine = unitCompile.getLineNumber(synchStatement.getStartPosition());
		statement.enlargeStartLineno(startLine);
		statementList.add(statement);
		return true; //����true,�������ӽڵ㡣
	}	
	
	/** ThrowStatement throw��䣬��Ϊһ�����塣
	 * ��ʽ��  throw Expression ;
	 * @param throwStatement   ���Ӷ�+1
	 * @return
	 */
	@Override
	public boolean visit(ThrowStatement throwStatement) {
		StatementContext statement = evaluateComplexMetric(throwStatement,StatementContext.StatementRest);
		statementList.add(statement);
/*����������⣬����thorw new Exception()��������䣬��û�м��Ϸ������õĸ��Ӷȡ�
 * 		ʵ�ʳ��򣬿��ܸ����ӣ���Ҫ�����������������ɵĸ��Ӷȡ�
 * Ҫ�������Ļ����ֲ��޸��������룬�ڴ˴��������´��룺
 *       .accept(  new ASTVisitor() { 
			public boolean visit(...) { 
 */
		return false;//����һ��������䣬�������ӽڵ㡣
	}	
	/** TryStatement try��䣬��ɢ�ɶ�����֡�
	 * ��ʽ��   try [ ( Resources ) ]
	 *          	Block
	 *          [ { CatchClause } ]
	 *          [ finally Block ] 
	 * @param tryStatement   try�Լ�ÿ��CatchClause���Ӷ�+2  finally���ǿ�ִ����䡣
	 * @return
	 */
	@Override
	public boolean visit(TryStatement tryStatement) {
		List<?> tryResources = tryStatement.resources();
		if( tryResources.size()>0 )
		{ // try [ ( Resources ) ]��Ϊһ�����塣
			StatementContext resourceContext = new StatementContext();
			for( Object obj : tryResources )
			{
				Expression express = (Expression)obj;
				StatementContext statement = evaluateComplexMetric(express,StatementContext.StatementRest);
				resourceContext.enlargeToComplexStatement(statement);
			}
			//����try����� try ����һ�У���ô���кŽ�������Ϊ����try������ʼ�кš�
			int startLine = unitCompile.getLineNumber(tryStatement.getStartPosition());
			resourceContext.enlargeStartLineno(startLine);
			//����ǰ������������󲻻����
			resourceContext.setStatementStyle(StatementContext.IfStyle);
			statementList.add(resourceContext);
		}
		/*ÿ��catch�־䶼��һ�������ִ����䣬
		CatchClause��ʽ:
		    catch ( FormalParameter ) Block
		    The FormalParameter is represented by a SingleVariableDeclaration.
		*/
		List<?> catchCauchLst = tryStatement.catchClauses();
		for( Object obj: catchCauchLst)
		{
			CatchClause cClause = (CatchClause)obj;
			//CatchClause����FormalParameter���֡�
			SingleVariableDeclaration svdCatch = cClause.getException();
			StatementContext statement = evaluateComplexMetric(svdCatch,StatementContext.IfStyle);
			//����catch����� catch ����һ�У���ô���кŽ�������Ϊ����catch������ʼ�кš�
			int startLine = unitCompile.getLineNumber(cClause.getStartPosition());
			statement.enlargeStartLineno(startLine);
			statementList.add(statement); //ÿ��CatchClause����һ�������Ŀ�ִ����䣬���Ӷ�+2
		}
		return true; //����true,�������ӽڵ㡣
	}		
}
