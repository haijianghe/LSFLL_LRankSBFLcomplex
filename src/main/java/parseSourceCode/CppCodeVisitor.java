/**
 * 
 */
package parseSourceCode;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionTryBlockDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.gnu.c.ICASTKnRFunctionDeclarator;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguousDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTSimpleDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDefinition;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTNamespaceDefinition;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTemplateDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTranslationUnit;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;

/**
 * @author Administrator
 *
 */
public class CppCodeVisitor extends ASTVisitor {
	private List<ClassContext> clazzList; //���ļ��ڰ��������Լ�Ƕ���ࡣ
	String parsingFilename; //��������������ĸ��ļ�������Ŀ¼��Ϣ��
	private ClassContext topDeclartion; //��C++�ļ��ڰ����Ķ��������ȫ�ֱ�������Ϊ�˱�̷��㣬����һ����"TopDeclartionCpp"��
	
	public CppCodeVisitor(String filename) 
	{
		super.includeInactiveNodes = true;  //Per default inactive nodes are not visited.
        super.shouldVisitAmbiguousNodes = true; //Normally neither ambiguous nodes nor their children are visited.
        super.shouldVisitArrayModifiers = true; //Set this flag to visit array modifiers.
        super.shouldVisitBaseSpecifiers = true; //Set this flag to visit base specifiers off composite types.
        super.shouldVisitDeclarations = true;   //Set this flag to visit declarations.
        super.shouldVisitDeclarators = true;    //Set this flag to visit declarators.
        super.shouldVisitDeclSpecifiers = true; //Set this flag to visit declaration specifiers.
        super.shouldVisitDesignators = true;    // Set this flag to visit designators of initializers.
        super.shouldVisitEnumerators = true;    //Set this flag to visit enumerators.
        super.shouldVisitExpressions = true;    //Set this flag to visit expressions.
        //Sometimes more than one implicit name is created for a binding, set this flag to true to visit more than one name for an implicit binding.
        super.shouldVisitImplicitNameAlternates = true;
        //Implicit names are created to allow implicit bindings to be resolved, normally they are not visited, set this flag to true to visit them.
        super.shouldVisitImplicitNames = true;
        super.shouldVisitInitializers = true;  //Set this flag to visit initializers.
        super.shouldVisitNames = true;         // Set this flag to visit names.
        super.shouldVisitNamespaces = true;    // Set this flag to visit to visit namespaces.
        super.shouldVisitParameterDeclarations = true;  //Set this flag to visit parameter declarations.
        super.shouldVisitPointerOperators = true;       //Set this flag to visit pointer operators of declarators.
        super.shouldVisitProblems = true;               //Set this flag to visit problem nodes.
        super.shouldVisitStatements = true;             //Set this flag to visit statements.
        super.shouldVisitTemplateParameters = true;     //Set this flag to visit template parameters.
        super.shouldVisitTranslationUnit = true;        //Set this flag to visit translation units.
        super.shouldVisitTypeIds = true;                //Set this flag to visit typeids.

        clazzList = new ArrayList<>();
		parsingFilename = filename;
		topDeclartion = new ClassContext();
		topDeclartion.setParsingFilename(filename); //��¼������������ĸ��ļ���
		topDeclartion.setTopDeclartionNameCategory();//c++���Գ����TopDeclartionCpp������������
	}
	
	/** ÿ�����ӿ����͵�����Ϊһ��CPPASTSimpleDeclaration �ڵ㣬����ע���ĵ���ͬһ���뵥Ԫ�����ж����������
	 *  IASTDeclaration��
	 */
	@Override
	public int visit(IASTDeclaration  nodeDecl) {
		//System.out.println("####  "+nodeDecl.getClass().toString());
		try {
			if( !(nodeDecl.isPartOfTranslationUnitFile()) )
				return PROCESS_CONTINUE;
			if ( nodeDecl instanceof CPPASTSimpleDeclaration  )
			{	//��ͨ�࣬��Ƕ����Ҳ�ڴ������
				CPPASTSimpleDeclaration simpDecl = (CPPASTSimpleDeclaration) nodeDecl;
				IASTDeclSpecifier declSpecifier = simpDecl.getDeclSpecifier();
				if ( declSpecifier instanceof CPPASTCompositeTypeSpecifier )
				{
					processCppClassStruct(simpDecl);//C++�࣬�ṹ�����޶������ࣻ
					return PROCESS_CONTINUE;
				}
				else
				{ //������ı���������䡣
					processTopDeclaration(simpDecl);
					return PROCESS_SKIP;//��Ѱ����Ƕ������䡣
				}
			}
			else if ( nodeDecl instanceof CPPASTFunctionDefinition  )
			{ //��ͨ������
				IASTNode parentNode = nodeDecl.getParent();
				//System.out.println("parentNode "+parentNode.getClass().toString());
				if( parentNode instanceof CPPASTTranslationUnit || parentNode instanceof CPPASTNamespaceDefinition )
				{//�ҳ��ļ���top-level����
					CPPASTFunctionDefinition cppFunction = (CPPASTFunctionDefinition)nodeDecl;
					IASTFunctionDeclarator funcDeclar = ( IASTFunctionDeclarator)cppFunction.getDeclarator();
					if ( !(funcDeclar instanceof CPPASTFunctionDeclarator) )
						return PROCESS_SKIP;//���Ƕ��㺯�������������ӽڵ㡣
					processTopFunction(cppFunction); //��ͨ������
					return PROCESS_SKIP; //��Ѱ�Һ�����Ƕ�׺��������������ӽڵ㡣
				}
				else
					return PROCESS_SKIP; //���Ƕ��㺯�������������ӽڵ㡣
			}
			else if ( nodeDecl instanceof CPPASTTemplateDeclaration  )
			{ //ģ�庯����
				IASTNode parentNode = nodeDecl.getParent();
				if( parentNode instanceof CPPASTTranslationUnit || parentNode instanceof CPPASTNamespaceDefinition )
				{//�ҳ��ļ���top-levelģ�庯��
					CPPASTTemplateDeclaration cppTemplate = (CPPASTTemplateDeclaration)nodeDecl;
					IASTDeclaration tempDeclar = ( IASTDeclaration)cppTemplate.getDeclaration();
					if ( !(tempDeclar instanceof CPPASTFunctionDefinition) )
						return PROCESS_SKIP;//���Ƕ��㺯�������������ӽڵ㡣
					CPPASTFunctionDefinition funcDefin = (CPPASTFunctionDefinition)tempDeclar; 
					IASTFunctionDeclarator funcDeclar = ( IASTFunctionDeclarator)funcDefin.getDeclarator();
					if ( !(funcDeclar instanceof CPPASTFunctionDeclarator) )
						return PROCESS_SKIP;//���Ƕ��㺯�������������ӽڵ㡣
					processTopFunction(funcDefin); //ģ�庯����
					return PROCESS_SKIP; //��Ѱ�Һ�����Ƕ�׺��������������ӽڵ㡣
				}
				else
					return PROCESS_SKIP; //���Ƕ��㺯�������������ӽڵ㡣
			}
			/*
			 * �������κ���ĸ�ֵ���û�мӽ������Ժ��ǡ�
			 */
			else
				return PROCESS_CONTINUE;
		}//end of try
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("Parse of "+ parsingFilename+" is error.");
			return PROCESS_ABORT;
		}
	}

	/*
	 * ��clazzList�в��ң���ΪclassName���࣬���û�У��򴴽�һ��������֮������
	 * �ر�ע�⣺�˴�����ӵ��б����治Ҫ�ظ���ӡ�
	 */
	private ClassContext createOrObtainClass(String className)
	{
		ClassContext rtnCtx = null;
		for( ClassContext cCtxt: clazzList )
		{
			if( cCtxt.getName().contentEquals(className) )
			{
				rtnCtx = cCtxt;
				break;
			}
		}
		if( rtnCtx==null )
		{
			rtnCtx = new ClassContext();
			rtnCtx.setName(className);
		    //������ӵ��б�
			clazzList.add(rtnCtx);
		}	
		return rtnCtx;
	}
	
	
	/**
	 * @param funcDeclar
	 * @throws DOMException
	 */
	private void processTopFunction(CPPASTFunctionDefinition funcDefine) throws DOMException
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

		CPPASTFunctionDeclarator funcDeclar = ( CPPASTFunctionDeclarator)funcDefine.getDeclarator();
		IASTName astName = funcDeclar.getName();
		String myClassName;//������
		String myMethodName; //������
		//��topLevelFunction������ȱ��ͷ�ļ����ࣻ
		String declartor = astName.toString();
		String[] parsed = declartor.split("::");
		int len = parsed.length;
		int category;// ������12,C++�����ͨ����(�������κ���); 13,ȱ��ͷ�ļ����ࣻ
		if( len<=1 )
		{  //���޶�����ֻ�к�������
			myClassName = ClassContext.TopLevelFunction;
			myMethodName = declartor;
			category =12;
			topLevelMethod.setType(11);//C++�ļ��Ķ��㺯����
		}
		else
		{
			myClassName = parsed[len-2];//�����ڶ������š���������XX::yy::zz�Ķ���޶�����ȡ�����ڶ������ŵ�����Ҳ�ܺá�
			myMethodName = parsed[len-1];//���һ������
			category =13;
			topLevelMethod.setType(12);//ȱ��ͷ�ļ�����ķ�����������C++�ļ��Ķ��㺯��
		}
		topLevelMethod.setName(myMethodName);
		
		//������
		List<String> methodParameters = new ArrayList<>();
		ICPPASTParameterDeclaration[] paraDeclars = funcDeclar.getParameters();
		for( ICPPASTParameterDeclaration icpd : paraDeclars )
		{
			ICPPASTDeclarator 	icdor = icpd.getDeclarator();
			methodParameters.add(icdor.getName().toString());
		}
		topLevelMethod.setParameters(methodParameters);
		
		//���ֲ�����
		CppCompositeMethodVisitor ccmVisitor = new CppCompositeMethodVisitor(funcDefine);
		funcDefine.accept(ccmVisitor);
		topLevelMethod.copyLocalVariables(ccmVisitor.getVariables());
		
	    //�ҳ��÷����ڵ�������䡣
	    CppStatementVisitor csVisitor = new CppStatementVisitor();
	    funcDefine.accept(csVisitor);
	    //��Ҫ�����������ݡ�
	    topLevelMethod.fillStatementsToMethod(csVisitor.getStatementList());
		
		//���ĳ�����ʹ���˷����Ĳ������߾ֲ���������Ӧ�����Ӹ��Ӷȡ�
	    //topLevelMethod.adjustComplexMetricWithParameterAndVaiable();
		//��ʱ�����Ǵ����ͺ����ĵ����ԣ���Ȼ�������临�Ӷȡ�
    
		//�����࣬���������������Ժͷ����� 
		ClassContext clazz = createOrObtainClass(myClassName);//�����ࣨ���ޣ��򴴽�����������У�
		//���ͷ�ļ������ļ���ͬ�����⡣
		String filename = ProjectContext.getFilenameFromPathFile(funcDefine.getContainingFilename());
		clazz.setFuncParsingFilenameCategoy(filename,category); //����ĺ���ר�ã���¼������������ĸ��ļ�,��ָ�������Ⱥ��
		//clazz.setParsingFilename(parsingFilename); //��¼������������ĸ��ļ���
		clazz.addMethod(topLevelMethod);
	    //���ĳ�����ʹ����������ԣ���Ӧ�����Ӹ��Ӷȡ�ĳЩ����ʵ������ķ���������ͷ�ļ�������
	    //clazz.adjustComplexMetricWithAttribute();
		//createOrObtainClass���Ѿ�����˴��࣬��Ҫ�ظ���ӡ�
	}
	
	/**  ����C++���ṹ
	 * @param simpDecl
	 * @throws DOMException
	 */
	private void processCppClassStruct(CPPASTSimpleDeclaration simpDecl) throws DOMException
	{
		IASTDeclSpecifier declSpecifier = simpDecl.getDeclSpecifier();
		//���ﲻ���ж�declSpecifier�Ƿ�CPPASTCompositeTypeSpecifier���͵�ʵ��������ǰ�Ѿ�ȷ�ϡ�
		CPPASTCompositeTypeSpecifier compTypeSpecifier = (CPPASTCompositeTypeSpecifier)declSpecifier;
		//�����࣬���������������Ժͷ����� 
		ClassContext clazz = new ClassContext();
		//���ͷ�ļ������ļ���ͬ�����⡣
		String curParsingFile = ProjectContext.getFilenameFromPathFile(simpDecl.getContainingFilename());
		clazz.setParsingFilename(curParsingFile); //��¼������������ĸ��ļ���
		//clazz.setParsingFilename(parsingFilename); //��¼������������ĸ��ļ���
		if( curParsingFile.contentEquals(parsingFilename) )
		{
			//ͷ�ļ��Ļ����Ͳ����������Ĳ��裬����ʼ�кͽ�������ȻΪ0��
			IASTFileLocation  fileLocation = simpDecl.getFileLocation();
			//�����ʼ�����к�
			int startLine = fileLocation.getStartingLineNumber();
			int endLine = fileLocation.getEndingLineNumber();
			clazz.setStartLine(startLine);
			clazz.setEndLine(endLine);
		}
		
		IASTName astName = compTypeSpecifier.getName();
		String myClassName = astName.toString();//������
		/*C++������struct���������࣬����¼��Щ.
		 * ע�⣺��Щ�������ṹ�ᵼ��adjustNestedComplexMetricWithAttribute��ѭ����
		 * �������Ƕ���ṹ��ȡ��parent.*/
		if( myClassName.isEmpty() )
			return;
		clazz.setName(myClassName);
		
		//����Ƕ�ױ�־��
		IASTNode parentNode = simpDecl.getParent();
		String[] parentName = new String[1];
		boolean isNestClass = isCppClassStruct(parentNode,parentName);
		if( isNestClass )
			clazz.setParentNode(parentName[0]); //��������Ƕ�ױ�־

		//������ԡ�
		IBinding binding = astName.resolveBinding();
		if ( binding instanceof ICPPClassType )
		{
			ICPPClassType cppClass = (ICPPClassType)binding;
			clazz.setCategory(10);//C++��ͨ��
			/*  ICPPField[] getDeclaredFields() 
			 * Returns a list of ICPPField objects representing fields declared in this class. 
			 * It does not include fields inherited from base classes. 
			 */
			ICPPField[] cppFields = cppClass.getDeclaredFields();
			for( ICPPField field : cppFields)
				clazz.addAttribueName(field.getName());
		}
		else
		{//dnn.cpp ����������XX::yy���࣬����ΪProblemBinding����ICPPClassType�����⴦��
			clazz.setCategory(11);// ����XX::yy���࣬�������޶�����
			//�� visit�ķ�����ȡ�����ԡ�
			CppCompositeTypeVisitor ctv = new CppCompositeTypeVisitor();
			simpDecl.accept(ctv);
			List<String> attributes = ctv.getAttributes();
			for( String item : attributes )
				clazz.addAttribueName(item);
		}
		//�������Ŀ�ִ�����Ļ���Ҫ���жϣ��Ƿ���ͷ�ļ��ڡ�
		if( curParsingFile.contentEquals(parsingFilename) )
		{//����ͷ�ļ��ڵĿ�ִ����䣬���Էŵ������ļ���ClassContext�С�
		    //�ҳ���������з�����
		    CppMethodVisitor cmVisitor = new CppMethodVisitor(myClassName);
		    simpDecl.accept(cmVisitor);
		    //Ϊ�����ֵ������������佨�������ⷽ�����������������
		    cmVisitor.addMethodByFieldDeclaration();
		    clazz.setMethods(cmVisitor.getMethodList());
		    //���ĳ�����ʹ����������ԣ���Ӧ�����Ӹ��Ӷȡ�
		    //clazz.adjustComplexMetricWithAttribute();
		}
	    //������ӵ��б�
		clazzList.add(clazz);
	}
	
	
	/**  �ýڵ���C++���ṹ�������ڱ��ļ��ڶ��塣
	 * @param cppNode
	 * @return
	 */
	private boolean isCppClassStruct(IASTNode cppNode,String[] parentName)
	{
		if ( cppNode instanceof CPPASTCompositeTypeSpecifier 
				&& cppNode.isPartOfTranslationUnitFile() )
		{
			CPPASTCompositeTypeSpecifier compTypeSpecifier = (CPPASTCompositeTypeSpecifier)cppNode;
			IASTName astName = compTypeSpecifier.getName();
			IBinding binding = astName.resolveBinding();
			if ( binding instanceof ICPPClassType )
			{
				ICPPClassType cppClass = (ICPPClassType)binding;
				parentName[0] = cppClass.getName();
				return true;
			}//end of if binding 
		}//end of if cppNode....
		return false;
	}
	
  //��������б���
  	public List<ClassContext> getClazzList() {
  		return clazzList;
  	}
  	
  	/**  ����ȫ�ֱ������ļ�����ı���������䡣
	 * @param simpDecl
	 * @throws DOMException
	 */
	private void processTopDeclaration(CPPASTSimpleDeclaration simpDecl) throws DOMException
	{
		IASTDeclSpecifier iasdSpeci = simpDecl.getDeclSpecifier();
		if( iasdSpeci.getStorageClass()==IASTDeclSpecifier.sc_typedef )
			return;  //����typedef

		ASTNodeProperty  nodeProperty = simpDecl.getPropertyInParent();
		if( nodeProperty!=IASTTranslationUnit.OWNED_DECLARATION ) //�Ƿ�������䡣
			return; 
		IASTDeclarator[] astDecltors = simpDecl.getDeclarators();
		//������ǿ�ִ������𣬴�=����Ϊ�ǡ� 
		boolean isExecute = false;
		List<String> declatorLst = new ArrayList<String>();//��¼ȫ�ֱ�����
		//ÿ����ʶ��������ȫ�ֱ���������������ԡ�
		for( IASTDeclarator declar :astDecltors )
		{
			if( 	declar instanceof  ICPPASTFunctionDeclarator     ||
					declar instanceof IASTAmbiguousDeclarator )
				continue;  //���⺯������Ϊȫ�ֱ�����
			//  ICPPASTArrayDeclarator,  ICPPASTFieldDeclarator

			String varname = declar.getName().toString(); //��ʶ������
			topDeclartion.addAttribueName(varname); //�����������䣬��Ϊ����TopDeclartionCpp������ԡ�
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
		CppFragmentVisitor fragmentVisitor= new CppFragmentVisitor();
		simpDecl.accept(fragmentVisitor);
		//ʹ��CppFragmentVisitor�󣬲������ƺ����ķ��������ǣ���getDeclarators��õķ��ż�¼Ϊ��ʶ����
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
	 * ������TopDeclartionCpp��ķ����б��в��ң��Ƿ����רΪ�ռ����������ִ��������õķ�����
	 * ���û�У��򴴽�һ��������֮���������뷽�����С�
	 * ����У���ֱ�ӷ��ظ÷�����
	 */
	private MethodContext createOrObtainSpecialMethod()
	{
		MethodContext methodContext = null;
		for( MethodContext cCtxt: topDeclartion.getMethods() )
		{
			if( cCtxt.isGlobalVariableOfCpp() )
			{
				methodContext = cCtxt;
				break;
			}
		}
		if( methodContext==null )
		{
			methodContext = new MethodContext();
			methodContext.setGlobalVariableOfCpp();
		    //���÷�����ӵ���ķ����б�
			topDeclartion.addMethod(methodContext);
			//�������������б�
			clazzList.add(topDeclartion);
		}	
		return methodContext;
	}
}

   