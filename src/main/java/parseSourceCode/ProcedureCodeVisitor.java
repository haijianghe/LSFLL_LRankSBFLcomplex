/**
 * 
 */
package parseSourceCode;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorFunctionStyleMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

import org.eclipse.cdt.core.dom.ast.gnu.c.ICASTKnRFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguousDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTDeclarationStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTFunctionDefinition;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTKnRFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTName;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTSimpleDeclaration;
import org.eclipse.ptp.pldt.openmp.analysis.PAST.PASTElif;
import org.eclipse.ptp.pldt.openmp.analysis.PAST.PASTEndif;
import org.eclipse.ptp.pldt.openmp.analysis.PAST.PASTIfdef;
import  org.eclipse.ptp.pldt.openmp.analysis.PAST.PASTNode;

/**
 * @author Administrator
 *
 */
public class ProcedureCodeVisitor extends ASTVisitor {
	private ClassContext clazzContext; //���ļ��ڰ����Ľ������ݣ�C����û���࣬Ϊ�˱�̷��㣬����һ���ࡣ
	String parsingFilename; //��������������ĸ��ļ�������Ŀ¼��Ϣ��
	
	public ProcedureCodeVisitor(String filename) 
	{
		super(true); //��C++�Ľ������룬����ϸ��������˵����
		//this.includeInactiveNodes = true;
		clazzContext = new ClassContext();
		parsingFilename = filename;
		clazzContext.setParsingFilename(filename); //��¼������������ĸ��ļ���
		clazzContext.setVirtualNameCategory();//c���Գ��������������������
	}
	
	/** C����ÿ���ļ�������һ���������࣬�������䷽����ȫ�ֱ����������ԡ�
	 *  IASTDeclaration��
	 */
	@Override
	public int visit(IASTDeclaration  nodeDecl) {
		try {
			if( !(nodeDecl.isPartOfTranslationUnitFile()) )
				return PROCESS_CONTINUE;
			ASTNodeProperty  nodeProperty = nodeDecl.getPropertyInParent();
			if( nodeProperty!=IASTTranslationUnit.OWNED_DECLARATION ) //�Ƿ񶥲㺯����������䡣
				return PROCESS_CONTINUE; 
			if ( nodeDecl instanceof CASTSimpleDeclaration  )
			{	//ȫ�ֱ����������������䡣
				//System.out.println("^^^^  "+nodeDecl.getRawSignature());
				CASTSimpleDeclaration simpDecl = (CASTSimpleDeclaration) nodeDecl;
				processTopDeclaration(simpDecl);
				return PROCESS_SKIP; //��Ѱ����Ƕ������䡣
			}
			else if ( nodeDecl instanceof CASTFunctionDefinition  )
			{ //��ͨ������
				//System.out.println("####  "+nodeDecl.getRawSignature());
				processTopFunction((CASTFunctionDefinition)nodeDecl); //��ͨ������
				return PROCESS_SKIP; //��Ѱ�Һ�����Ƕ�׺��������������ӽڵ㡣
			}
			else  //IASTAmbiguousSimpleDeclaration, IASTASMDeclaration, IASTProblemDeclaration 
				return PROCESS_CONTINUE;
		}//end of try
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("Parse of "+ parsingFilename+" is error.");
			return PROCESS_ABORT;
		}
	}

	/**
	 * @param funcDeclar
	 * @throws DOMException
	 */
	private void processTopFunction(CASTFunctionDefinition funcDefine) throws DOMException
	{
		//����Ǻ����Ļ���Ҫ���жϣ��Ƿ���ͷ�ļ��ڡ�
		String pathFilename = funcDefine.getContainingFilename();
		String curParsingFile = ProjectContext.getFilenameFromPathFile(pathFilename);
		if( !curParsingFile.contentEquals(parsingFilename) )
			return; //ͷ�ļ��ڵĿ�ִ����䲻�ܷŵ������ļ���ClassContext�С�
		
		//���캯���������ֲ���������������䡣
		MethodContext topLevelMethod = new MethodContext();
		IASTFileLocation  fileLocation = funcDefine.getFileLocation();
		//�����ʼ�����к�
		int startLine = fileLocation.getStartingLineNumber();
		int endLine = fileLocation.getEndingLineNumber();
		topLevelMethod.setStartLine(startLine);
		topLevelMethod.setEndLine(endLine);
		// CASTFunctionDeclarator or  CASTKnRFunctionDeclarator
		IASTFunctionDeclarator funcDeclar = funcDefine.getDeclarator();
		IASTName astName = funcDeclar.getName();
		String myMethodName = astName.toString(); //������
		topLevelMethod.setType(3);//C�ļ�����ͨ������
		topLevelMethod.setName(myMethodName);
		
		//������
		List<String> methodParameters = new ArrayList<>();
		if( funcDeclar instanceof CASTFunctionDeclarator )
		{
			CASTFunctionDeclarator cfFuncDeclar = (CASTFunctionDeclarator)funcDeclar;
			IASTParameterDeclaration[] paraDeclars = cfFuncDeclar.getParameters();
			for( IASTParameterDeclaration icpd : paraDeclars )
			{
				IASTDeclarator 	icdor = icpd.getDeclarator();
				methodParameters.add(icdor.getName().toString());
			}
		}
		else // CASTKnRFunctionDeclarator
		{
			CASTKnRFunctionDeclarator KnrFuncDeclar = (CASTKnRFunctionDeclarator)funcDeclar;
			IASTName[] paraNames = KnrFuncDeclar.getParameterNames();
			for( IASTName iname : paraNames )
				methodParameters.add(iname.toString());
		}
		topLevelMethod.setParameters(methodParameters);
		
		//���ֲ�����
		List<String> localVariables = new ArrayList<>();
		EnumInvocationIdentifer(funcDefine,localVariables);
		topLevelMethod.copyLocalVariables(localVariables);
		
	    //�ҳ��÷����ڵ�������䡣
	    ProcedureStatementVisitor csVisitor = new ProcedureStatementVisitor();
	    funcDefine.accept(csVisitor);
	    //��Ҫ�����������ݡ�
	    topLevelMethod.fillStatementsToMethod(csVisitor.getStatementList());
		
		//���ĳ�����ʹ���˷����Ĳ������߾ֲ���������Ӧ�����Ӹ��Ӷȡ�
	    //topLevelMethod.adjustComplexMetricWithParameterAndVaiable();
		
	    clazzContext.addMethod(topLevelMethod);
	}
	
	/**  �ҳ�������ľֲ����������뵽localVariables
	 * @param funcDefine ָ������
	 *@param localVariables
	 */
	private void EnumInvocationIdentifer(CASTFunctionDefinition funcDefine,List<String> localVariables)
	{
		funcDefine.accept(  new ASTVisitor() { 
			{
		        super.shouldVisitDeclarations = true;   //Set this flag to visit declarations.
		        super.shouldVisitStatements = true;             //Set this flag to visit statements.
			}
			
			@Override
			public int visit( IASTStatement statement ) {
				if( !(statement instanceof CASTDeclarationStatement) )
					return PROCESS_CONTINUE;
				//�ж���ָ��������ֱ����������𣿸��ڵ��Ƿ�����
				IASTNode parentNode = statement.getParent();
				ASTNodeProperty  ppnodeProperty = parentNode.getPropertyInParent(); 
				if( ppnodeProperty!=IASTFunctionDefinition.FUNCTION_BODY ) //�Ƿ����Ķ���������䡣
					return PROCESS_SKIP; //��Ѱ�������Ƕ��䣬���������ӽڵ㡣
				IASTDeclaration astDeclar = ((CASTDeclarationStatement)statement).getDeclaration();
				IASTDeclarator[] astDeclators = ((CASTSimpleDeclaration)astDeclar).getDeclarators();
				for( IASTDeclarator declator : astDeclators )
				{
					localVariables.add(declator.getName().toString());
				}
				return PROCESS_SKIP; //��Ѱ�������Ƕ��䣬���������ӽڵ㡣
			}

		}//end of ASTVisitor	
		); //end of accept.
	}
	
	/**  ����ȫ�ֱ����������������䡣
	 * ��������������Ϊ����������ԣ��������㸴�Ӷ�ʱ+3
	 * @param simpDecl
	 * @throws DOMException
	 */
	private void processTopDeclaration(CASTSimpleDeclaration simpDecl) throws DOMException
	{
		IASTDeclSpecifier iasdSpeci = simpDecl.getDeclSpecifier();
		if( iasdSpeci.getStorageClass()==IASTDeclSpecifier.sc_typedef )
			return;  //����typedef

		IASTDeclarator[] astDecltors = simpDecl.getDeclarators();
		//������ǿ�ִ������𣬴�=����Ϊ�ǡ� 
		boolean isExecute = false;
		List<String> declatorLst = new ArrayList<String>();//��¼ȫ�ֱ�����
		//ÿ����ʶ��������ȫ�ֱ���������������ԡ�
		for( IASTDeclarator declar :astDecltors )
		{
			if( 	declar instanceof  IASTFunctionDeclarator    ||
					declar instanceof ICASTKnRFunctionDeclarator ||
					declar instanceof IASTStandardFunctionDeclarator ||
					declar instanceof IASTAmbiguousDeclarator )
				continue;  //���⺯������Ϊȫ�ֱ�����
			// IASTArrayDeclarator, IASTFieldDeclarator
			
			String varname = declar.getName().toString(); //��ʶ������
			clazzContext.addAttribueName(varname); //�����������䣬��Ϊ����������ԡ�
			declatorLst.add(varname);
			IASTInitializer inital = declar.getInitializer();
			if( inital!=null ) //�г�ʼ���Ĳ���Ϊ�ǿ�ִ�����
				isExecute = true;
		}
		if( !isExecute )
			return;  //���ǿ�ִ����䣬�����û�б�Ҫִ�С�
		//����ǿ�ִ�����ı���������Ҫ���жϣ��Ƿ���ͷ�ļ��ڡ�
		String pathFilename = simpDecl.getContainingFilename();
		String curParsingFile = ProjectContext.getFilenameFromPathFile(pathFilename);
		if( !curParsingFile.contentEquals(parsingFilename) )
			return; //ͷ�ļ��ڵĿ�ִ����䲻�ܷŵ������ļ���ClassContext�С�
				
		//����ִ�������Ϣ���뵽�����С��ȴ�������䡣
		StatementContext stmtContext = new StatementContext();
		stmtContext.setStatementStyle(StatementContext.StatementRest);
		IASTFileLocation  fileLocation = simpDecl.getFileLocation();
		//�����ʼ�����к�
		int startLine = fileLocation.getStartingLineNumber();
		int endLine = fileLocation.getEndingLineNumber();
		stmtContext.setStartLine(startLine);
		stmtContext.setEndLine(endLine);
		ProcedureFragmentVisitor fragmentVisitor= new ProcedureFragmentVisitor();
		simpDecl.accept(fragmentVisitor);
		//ʹ��ProcedureFragmentVisitor�󣬲������ƺ����ķ��������ǣ���getDeclarators��õķ��ż�¼Ϊ��ʶ����
		//   �����������ݽ�����ɺ�...��Ҳ�ṩ��һ����֤��������
		stmtContext.setSimpleNames(declatorLst);
		stmtContext.setFuncCalls(fragmentVisitor.getInvoMethods());
		stmtContext.setLogicOperaters(fragmentVisitor.getLogicOperators());
		stmtContext.setOtherOperaters(fragmentVisitor.getStrayOperators());

		MethodContext methodCtx = createOrObtainSpecialMethod();
		methodCtx.addOneStatement(stmtContext);
		//createOrObtainSpecialMethod�����Ѿ���������ӵ�ClassContext���˴������ظ���ӡ�
	}
	
	/*
	 * �ӷ����б��в��ң��Ƿ����רΪ�ռ����������ִ��������õķ��������û�У��򴴽�һ��������֮���������뷽�����С�
	 * ����У���ֱ�ӷ��ظ÷�����
	 */
	private MethodContext createOrObtainSpecialMethod()
	{
		MethodContext methodContext = null;
		for( MethodContext cCtxt: clazzContext.getMethods() )
		{
			if( cCtxt.isGlobalVariableOfC() )
			{
				methodContext = cCtxt;
				break;
			}
		}
		if( methodContext==null )
		{
			methodContext = new MethodContext();
			methodContext.setGlobalVariableOfC();
		    //���÷�����ӵ���ķ����б�
			clazzContext.addMethod(methodContext);
		}	
		return methodContext;
	}
	
	//��������б���
  	public ClassContext getClazzContext() {
  		return clazzContext;
  	}
  	
}
