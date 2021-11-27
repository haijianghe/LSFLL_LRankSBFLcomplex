/**
 * 
 */
package parseSourceCode;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDefinition;

/**
 * @author Administrator
 * CPPASTVisitor已经废弃
 */
public class CppMethodVisitor extends ASTVisitor {
	private List<MethodContext> methodList; //某个类的所有方法。注意：方法不允许嵌套。
	private String parentName; //parentNode的名字，只获取类的直接方法，不获取内部类的方法。
	private MethodContext fieldDeclareMethod; //为类带赋值的属性声明语句建立特殊方法。
	/*在java,为类建立一个特殊方法，命名为 class_FieldDeclaration,类型为6
	 * 属性带赋值的必须定义为可执行语句。C++大概没有吧。haveAssignment暂时用不上。
	 * haveAssignment用于此类情况。
	 */
	//private boolean haveAssignment = false;//有赋值语句吗？ Visitor不能使用方法内局部变量。
	//private List<String> localVariables; //方法内的直接局部变量集合。若有重名的，应该是我的程序有问题。

	/**
	 * @param unit  获取行号
	 * @param parentNodeName  检测是否为本类的直接方法或声明语句
	 */
	public CppMethodVisitor(String parentNodeName)
	{
		super(true);//public ASTVisitor(boolean visitNodes)
		
		methodList = new ArrayList<>();
		//localVariables = new ArrayList<>();
		//unitCompile = unit;
		parentName = parentNodeName;
		//为类带赋值的声明语句建立特殊方法。
		fieldDeclareMethod = new MethodContext();
		fieldDeclareMethod.setName("class_FieldDeclaration");
		fieldDeclareMethod.setType(6);
	}
	
	//该类的所有方法。注意：方法内的嵌套类、匿名类等inner class的方法将不会列入其中。
	public List<MethodContext> getMethodList() {
		return methodList;
	}
	
	/*
	 * 为类带赋值的属性声明语句建立了特殊方法。 将该方法加入到方法列表。
	 * 注意： 手工调用此方法，不好自动调用。
	 */
	public void addMethodByFieldDeclaration()
	{
		if( fieldDeclareMethod.getNumberOfStatements()>0 )
			methodList.add(fieldDeclareMethod);
	}
	
	@Override
	public int visit(IASTDeclaration funcDefin) {
		if ( !(funcDefin instanceof CPPASTFunctionDefinition)  )
			 return PROCESS_CONTINUE;
		CPPASTFunctionDefinition cppFunction = (CPPASTFunctionDefinition)funcDefin;
		if( !isTopMethod(cppFunction) ) //并非指定类的直接方法
		{
			/*
			 * 如果方法不是该类的直接方法，就是方法内的嵌套类、匿名类等inner class的方法；此处不用加入，在对应类会作为方法加入。
			 * 至于他们的语句信息，Initializer等Visitor或者后续工作会处理的。
			 */
			return PROCESS_SKIP; //自己都不是直接方法，无须搜索其子节点。
		}

		//以下读取类直接方法的数据。
		MethodContext method = new MethodContext();
		IASTFileLocation  fileLocation = cppFunction.getFileLocation();
		//获得起始结束行号
		int startLine = fileLocation.getStartingLineNumber();
		int endLine = fileLocation.getEndingLineNumber();
		method.setStartLine(startLine);
		method.setEndLine(endLine);
		
		CPPASTFunctionDeclarator funcDeclar = ( CPPASTFunctionDeclarator)cppFunction.getDeclarator();
		IASTName astName = funcDeclar.getName();
		IBinding binding = astName.resolveBinding();
		if ( !(binding instanceof ICPPMethod ) )
			return PROCESS_SKIP;//识别不了这种方法，不再搜索子节点。
		ICPPMethod cppMethod = (ICPPMethod)binding;
		
		//构造函数
		if (cppMethod.isImplicit())  
			method.setType(1);//构造函数或者assignment operator, etc.。
		else
			method.setType(3); //普通函数。
		//方法名
		String methodName = astName.toString();
		method.setName(methodName);
		//为了调试，加入下面的语句。
		//if( methodName.getIdentifier().contentEquals("useForType"))
			//System.out.println("This is for debuggin.");
		//参数列表。
		ICPPFunction icppf = (ICPPFunction)binding;
		ICPPParameter[] icppars = icppf.getParameters();
		for (ICPPParameter iccp : icppars) {
			String strPara = iccp.getName().toString();
			method.addParameter(strPara);
		}
		//获取该方法内所有局部变量
		CppCompositeMethodVisitor ccmVisitor = new CppCompositeMethodVisitor(cppFunction);
		cppFunction.accept(ccmVisitor);
		method.copyLocalVariables(ccmVisitor.getVariables());
		
		//获取方法内所有语句的数据。
		IASTStatement thisBlock = 	cppFunction.getBody();
		if( thisBlock!=null )
		{
			CppStatementVisitor csVisitor = new CppStatementVisitor();
			thisBlock.accept(csVisitor);
			//还要加上语句的数据。
			method.fillStatementsToMethod(csVisitor.getStatementList());
		}
		//将解析后的结果添加到列表。
		methodList.add(method);

		return PROCESS_SKIP; //不寻找函数内嵌套函数，不再搜索子节点。
	}
	
	/**  该方法是指定类的直接方法top-level吗？  希望排除内部类的方法。
	 * @param nodeMethod
	 * @return
	 */
	private boolean isTopMethod(CPPASTFunctionDefinition nodeMethod)
	{
		IASTNode  parentNode = nodeMethod.getParent();
    	if( parentNode instanceof CPPASTCompositeTypeSpecifier )
    	{
    		IASTName parentSimple = ((CPPASTCompositeTypeSpecifier)parentNode).getName();
    		String parentClassName = parentSimple.toString();
    		if( parentClassName.contentEquals(parentName) )
    			return true;
    		else
    			return false;
    	}
    	else
    		return false;
	}
	
	/** C++没有类似java Initializer这样的 东西。
	 * 不过 C++的静态成员在外部初始化，这个问题没有解决，以后再说。
	 * 注意：方法内的嵌套类、匿名类等inner class的方法将不会列入其中。
	 * 类或接口才能调用此visit，获取他们的initializer列表。
	 * 
	 * CDT可以使用：
	 *               
 	 */
	/*@Override
	public boolean visit(Initializer initializer) {
	}*/
	
	
	/**non-static data member initializers only available with -std=c++11 or -std=gnu++11
	 * C++98不允许为类的成员初始化值，暂时不考虑以下类似java的做法
	 * 为类建立一个特殊方法，命名为 class_FieldDeclaration,类型为6
	 * 类的属性声明语句， 若有赋值语句，可能会被记录为可执行语句，加入特殊方法；
	 *                     若无，则为非执行语句，不加入特殊方法。
	 */
/*	@Override
	public boolean visit(FieldDeclaration fieldStatement) {
	}*/
}
