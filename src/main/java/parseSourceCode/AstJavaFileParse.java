/**
 * 
 */
package parseSourceCode;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

/**
 * @author Administrator
 *
 */
public class AstJavaFileParse {
	private CompilationUnit unitCompile;
	
	public AstJavaFileParse()
	{
		unitCompile = null;
	}
	
	/**
	 * @param javaFilePath  带目录的文件名。
	 * @param clazzList 解析后的结果存入此队列
	 * @return
	 */
	public List<ClassContext> parseFile(String javaFilePath)
	{
		boolean result=true;
		List<ClassContext> clazzList = null;
		if( createCompilationUnit(javaFilePath)  )
		{
			int pos = javaFilePath.lastIndexOf("\\");
			String parsingFilename = javaFilePath.substring(pos+1);
			JavaCodeVisitor jcVisitor = new JavaCodeVisitor(unitCompile,parsingFilename);
			unitCompile.accept(jcVisitor);
			clazzList = jcVisitor.getClazzList();
		    //此前，在计算类的属性影响语句的复杂度时，并未考虑外部类属性的影响。在此处一并计算。
			//必须放在adjustNestedNestedClass(clazzList)之前，因为此时的链条关系还清晰。
		    adjustNestedComplexMetricWithAttribute(clazzList);
			//子类只允许一次嵌套。调整内部类里的各种嵌套类。
			adjustNestedNestedClass(clazzList);
		}
		else
			result = false;
		if( result )
			return clazzList;
		else
			return null;
	}
	
	/**调整内部类里的各种嵌套类（不失一般性，假设为inninn）。
	 * 调整算法，将inninn里的方法，移动到其最顶层类；方法的其它值不变，将type改为5.
	 * @param clazzLst
	 */
	private void adjustNestedNestedClass(List<ClassContext> clazzLst)
	{
        Iterator<ClassContext> iterator = clazzLst.iterator();
        while (iterator.hasNext()) 
		{
        	ClassContext cnode = iterator.next();
			if( !cnode.isNesting() )
				continue;
			//是内部类。
			ClassContext parentNode = getClassNode(clazzLst,cnode.getParentName());
			//如果其外部类也是嵌套类（内部类，局部类，匿名类）,则删除此类的数据，将其数据移动到次顶层类。
			if( parentNode.isNesting() )
			{
				ClassContext subTopParent = subTopParentClass(clazzLst,parentNode);
				//虽然该方法被删除，但其语句数据得保留。
				subTopParent.mergeInner2Class(cnode);
				cnode.setWillRemove(true);//设置删除标记。
				//iterator.remove();//使用迭代器的删除方法删除
			}
		}//end of while.
        
        //删除二级及以上的嵌套类。
        iterator = clazzLst.iterator();
        while (iterator.hasNext()) 
		{
        	ClassContext cnode = (ClassContext)(iterator.next());
        	if( cnode.isWillRemove() )
        		iterator.remove();//使用迭代器的删除方法删除
		}//end of while
	}
	
	/** 依据类的名字获取类节点。
	 * @param name
	 * @return
	 */
	private ClassContext getClassNode(List<ClassContext> clazzLst,String name)
	{
		ClassContext rtnNode = null;
		for ( ClassContext cnode : clazzLst )
		{
			if( cnode.getName().contentEquals(name) )
			{
				rtnNode = cnode;
				break;
			}
		}
		return rtnNode;
	}
	
	/**  获取ccNode次顶层的外部类节点，最顶层下一级的。
	 * @param ccNode  肯定是内部类，其isNesting=true，而且ccNode有内部类。
	 * @return
	 */
	private ClassContext subTopParentClass(List<ClassContext> clazzLst,ClassContext ccNode)
	{
		ClassContext topParentNode = getClassNode(clazzLst,ccNode.getParentName());
		ClassContext subTopParent = ccNode;
		while( topParentNode.isNesting() )
		{
			subTopParent = topParentNode;
			topParentNode = getClassNode(clazzLst,topParentNode.getParentName());
		}
		return subTopParent;
	}
	
    /*此前，在计算类的属性影响语句的复杂度时，并未考虑外部类属性的影响。在此处一并计算。
	必须放在adjustNestedNestedClass(clazzList)之前，因为此时的链条关系还清晰。
	*/
    private void adjustNestedComplexMetricWithAttribute(List<ClassContext> clazzLst)
    {
        Iterator<ClassContext> iterator = clazzLst.iterator();
        while (iterator.hasNext()) 
		{
        	ClassContext cnode = iterator.next();
			if( !cnode.isNesting() )
				continue; //顶层类，无须调整。
			//cnode是内部类，逐级找出其所有外部类。
			ClassContext parentNode = getClassNode(clazzLst,cnode.getParentName());
			do
			{
				List<String> parentAttributes = parentNode.getAttributes();//该类的属性名列表。
				cnode.addOutterAttribute(parentAttributes); //所有上代类的属性加到此类的属性表。
				parentNode = getClassNode(clazzLst,parentNode.getParentName());
			}while (parentNode!=null); 
		}
    }

	//javaFilePath:带目录的文件名。
	private boolean createCompilationUnit(String javaFilePath)
	{
		boolean result=true;
        byte[] input = null;
		try {
		    BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(javaFilePath));
		    input = new byte[bufferedInputStream.available()];
	            bufferedInputStream.read(input);
	            bufferedInputStream.close();
		} 
		catch (FileNotFoundException e) 
		{
			result = false;
			e.printStackTrace();
		} 
		catch (IOException e) {
			result = false;
			e.printStackTrace();
		}
		if( !result )
			return false;
		
		ASTParser astParser = ASTParser.newParser(AST.JLS8);
		Map<String, String> compilerOptions = JavaCore.getOptions();
		// 设置编译依照的合规参数为jdk 1.8
		compilerOptions.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
		compilerOptions.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
		compilerOptions.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
		compilerOptions.put(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, JavaCore.ENABLED);
		astParser.setCompilerOptions(compilerOptions);
		char[] codeStream = new String(input).toCharArray();
        astParser.setSource(codeStream);
        astParser.setKind(ASTParser.K_COMPILATION_UNIT);
        /*Since evaluating bindings is costly, the binding service has to be explicitly requested at parse time. 
         * This is done by passing true the method ASTParser.setResolveBindings() before the source is being parsed.        
         */
        astParser.setResolveBindings(false);
        unitCompile = (CompilationUnit) (astParser.createAST(null));
        
        return result;
    }

}
