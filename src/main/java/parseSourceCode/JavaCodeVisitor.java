/**
 * 
 */
package parseSourceCode;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

/**
 * @author Administrator
 *
 */
public class JavaCodeVisitor extends ASTVisitor {
	private List<ClassContext> clazzList; //该文件内包含的类以及嵌套类。
	private CompilationUnit unitCompile; //为获取行号。
	String parsingFilename; //解析结果来自于哪个文件，不带目录信息。
	
	public JavaCodeVisitor(CompilationUnit unit,String filename)
	{
		clazzList = new ArrayList<>();
		unitCompile = unit;
		parsingFilename = filename;
	}
	/**
	 *  每个类或接口类型的声明为一个TypeDeclaration 节点，包括注释文档。同一编译单元可以有多个类声明。
	 *  enum 类型没有实现，考虑以后。
	 *  如： FastXML的SerializationFeature.java
	 */
	@Override
	public boolean visit(TypeDeclaration nodeClass) {
		ClassContext clazz = new ClassContext();
		clazz.setParsingFilename(parsingFilename); //记录解析结果来自哪个文件。
		
		//简单地处理嵌套节点,inner node & outter node.
	    if (!nodeClass.isPackageMemberTypeDeclaration()) 
	    {
	    	ASTNode  parentNode = nodeClass.getParent();
/* 忽略以下内部类,匿名类(匿名类最常见的方式就是回调模式的使用)。
 * 	    1,定义在动态初始化块的局部内部类   *      2,定义在动态初始化块的匿名内部类
 *      3,定义在静态初始化块的局部内部类   *      4,定义在静态初始化块的匿名内部类
 *      5,定义在构造方法的局部内部类       *      6,定义在构造方法的匿名内部类
 *      7,定义在静态成员方法的局部内部类   *      8,定义在静态方法的匿名内部类
 *      9,定义在成员方法的局部内部类       *      10,定义在成员方法的匿名内部类          */
	    	if( parentNode instanceof TypeDeclaration )
	    	{
	    		SimpleName parentSimple = ((TypeDeclaration)parentNode).getName();
	    		String outterName = parentSimple.getIdentifier();
	    		clazz.setParentNode(outterName);
	    	}
	    	else  //内部类,匿名类应该都属于普通ASTNode,而非TypeDeclaration；
	    		return true;//这些类的语句将放到特殊方法_GENIUS中去,通过枚举Initializer。
	    }
		String nodeName = nodeClass.getName().getIdentifier();
		clazz.setName(nodeName);
		clazz.setInterface(nodeClass.isInterface());
		int startLine = unitCompile.getLineNumber(nodeClass.getStartPosition());
		int endLine = unitCompile.getLineNumber(nodeClass.getStartPosition()+nodeClass.getLength()-1);
		clazz.setStartLine(startLine);
		clazz.setEndLine(endLine);
	    //枚举该类（或接口）所有属性
	    enumFieldsOfClass(nodeClass,clazz);
	    //找出该类的所有方法。
	    JavaMethodVisitor jmVisitor = new JavaMethodVisitor(unitCompile,nodeName);
	    nodeClass.accept(jmVisitor);
	    //为类带赋值的属性声明语句建立了特殊方法，将它加入进来。
	    jmVisitor.addMethodByFieldDeclaration();
	    clazz.setMethods(jmVisitor.getMethodList());
	    //将类添加到列表
		clazzList.add(clazz);
		return true;  //false的话，不会找出里面嵌套的类。
	}

    //枚举该类（或接口）所有属性
	private void enumFieldsOfClass(TypeDeclaration nodeClass, ClassContext clazz)
	{
		FieldDeclaration[] fieldAry = nodeClass.getFields();
		for( FieldDeclaration field : fieldAry )
		{
			List<?> fragments = field.fragments();
			for( Object object: fragments )
			{
				VariableDeclarationFragment vdf = (VariableDeclarationFragment)object;
				clazz.addAttribueName(vdf.getName().getIdentifier());
			}
		}
	}
	
	//返回类的列表结果
	public List<ClassContext> getClazzList() {
		return clazzList;
	}
	
}
