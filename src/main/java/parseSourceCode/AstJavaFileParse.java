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
	 * @param javaFilePath  ��Ŀ¼���ļ�����
	 * @param clazzList ������Ľ������˶���
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
		    //��ǰ���ڼ����������Ӱ�����ĸ��Ӷ�ʱ����δ�����ⲿ�����Ե�Ӱ�졣�ڴ˴�һ�����㡣
			//�������adjustNestedNestedClass(clazzList)֮ǰ����Ϊ��ʱ��������ϵ��������
		    adjustNestedComplexMetricWithAttribute(clazzList);
			//����ֻ����һ��Ƕ�ס������ڲ�����ĸ���Ƕ���ࡣ
			adjustNestedNestedClass(clazzList);
		}
		else
			result = false;
		if( result )
			return clazzList;
		else
			return null;
	}
	
	/**�����ڲ�����ĸ���Ƕ���ࣨ��ʧһ���ԣ�����Ϊinninn����
	 * �����㷨����inninn��ķ������ƶ���������ࣻ����������ֵ���䣬��type��Ϊ5.
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
			//���ڲ��ࡣ
			ClassContext parentNode = getClassNode(clazzLst,cnode.getParentName());
			//������ⲿ��Ҳ��Ƕ���ࣨ�ڲ��࣬�ֲ��࣬�����ࣩ,��ɾ����������ݣ����������ƶ����ζ����ࡣ
			if( parentNode.isNesting() )
			{
				ClassContext subTopParent = subTopParentClass(clazzLst,parentNode);
				//��Ȼ�÷�����ɾ��������������ݵñ�����
				subTopParent.mergeInner2Class(cnode);
				cnode.setWillRemove(true);//����ɾ����ǡ�
				//iterator.remove();//ʹ�õ�������ɾ������ɾ��
			}
		}//end of while.
        
        //ɾ�����������ϵ�Ƕ���ࡣ
        iterator = clazzLst.iterator();
        while (iterator.hasNext()) 
		{
        	ClassContext cnode = (ClassContext)(iterator.next());
        	if( cnode.isWillRemove() )
        		iterator.remove();//ʹ�õ�������ɾ������ɾ��
		}//end of while
	}
	
	/** ����������ֻ�ȡ��ڵ㡣
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
	
	/**  ��ȡccNode�ζ�����ⲿ��ڵ㣬�����һ���ġ�
	 * @param ccNode  �϶����ڲ��࣬��isNesting=true������ccNode���ڲ��ࡣ
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
	
    /*��ǰ���ڼ����������Ӱ�����ĸ��Ӷ�ʱ����δ�����ⲿ�����Ե�Ӱ�졣�ڴ˴�һ�����㡣
	�������adjustNestedNestedClass(clazzList)֮ǰ����Ϊ��ʱ��������ϵ��������
	*/
    private void adjustNestedComplexMetricWithAttribute(List<ClassContext> clazzLst)
    {
        Iterator<ClassContext> iterator = clazzLst.iterator();
        while (iterator.hasNext()) 
		{
        	ClassContext cnode = iterator.next();
			if( !cnode.isNesting() )
				continue; //�����࣬���������
			//cnode���ڲ��࣬���ҳ��������ⲿ�ࡣ
			ClassContext parentNode = getClassNode(clazzLst,cnode.getParentName());
			do
			{
				List<String> parentAttributes = parentNode.getAttributes();//������������б�
				cnode.addOutterAttribute(parentAttributes); //�����ϴ�������Լӵ���������Ա�
				parentNode = getClassNode(clazzLst,parentNode.getParentName());
			}while (parentNode!=null); 
		}
    }

	//javaFilePath:��Ŀ¼���ļ�����
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
		// ���ñ������յĺϹ����Ϊjdk 1.8
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
