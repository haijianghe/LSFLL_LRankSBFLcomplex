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
	private List<ClassContext> clazzList; //���ļ��ڰ��������Լ�Ƕ���ࡣ
	private CompilationUnit unitCompile; //Ϊ��ȡ�кš�
	String parsingFilename; //��������������ĸ��ļ�������Ŀ¼��Ϣ��
	
	public JavaCodeVisitor(CompilationUnit unit,String filename)
	{
		clazzList = new ArrayList<>();
		unitCompile = unit;
		parsingFilename = filename;
	}
	/**
	 *  ÿ�����ӿ����͵�����Ϊһ��TypeDeclaration �ڵ㣬����ע���ĵ���ͬһ���뵥Ԫ�����ж����������
	 *  enum ����û��ʵ�֣������Ժ�
	 *  �磺 FastXML��SerializationFeature.java
	 */
	@Override
	public boolean visit(TypeDeclaration nodeClass) {
		ClassContext clazz = new ClassContext();
		clazz.setParsingFilename(parsingFilename); //��¼������������ĸ��ļ���
		
		//�򵥵ش���Ƕ�׽ڵ�,inner node & outter node.
	    if (!nodeClass.isPackageMemberTypeDeclaration()) 
	    {
	    	ASTNode  parentNode = nodeClass.getParent();
/* ���������ڲ���,������(����������ķ�ʽ���ǻص�ģʽ��ʹ��)��
 * 	    1,�����ڶ�̬��ʼ����ľֲ��ڲ���   *      2,�����ڶ�̬��ʼ����������ڲ���
 *      3,�����ھ�̬��ʼ����ľֲ��ڲ���   *      4,�����ھ�̬��ʼ����������ڲ���
 *      5,�����ڹ��췽���ľֲ��ڲ���       *      6,�����ڹ��췽���������ڲ���
 *      7,�����ھ�̬��Ա�����ľֲ��ڲ���   *      8,�����ھ�̬�����������ڲ���
 *      9,�����ڳ�Ա�����ľֲ��ڲ���       *      10,�����ڳ�Ա�����������ڲ���          */
	    	if( parentNode instanceof TypeDeclaration )
	    	{
	    		SimpleName parentSimple = ((TypeDeclaration)parentNode).getName();
	    		String outterName = parentSimple.getIdentifier();
	    		clazz.setParentNode(outterName);
	    	}
	    	else  //�ڲ���,������Ӧ�ö�������ͨASTNode,����TypeDeclaration��
	    		return true;//��Щ�����佫�ŵ����ⷽ��_GENIUS��ȥ,ͨ��ö��Initializer��
	    }
		String nodeName = nodeClass.getName().getIdentifier();
		clazz.setName(nodeName);
		clazz.setInterface(nodeClass.isInterface());
		int startLine = unitCompile.getLineNumber(nodeClass.getStartPosition());
		int endLine = unitCompile.getLineNumber(nodeClass.getStartPosition()+nodeClass.getLength()-1);
		clazz.setStartLine(startLine);
		clazz.setEndLine(endLine);
	    //ö�ٸ��ࣨ��ӿڣ���������
	    enumFieldsOfClass(nodeClass,clazz);
	    //�ҳ���������з�����
	    JavaMethodVisitor jmVisitor = new JavaMethodVisitor(unitCompile,nodeName);
	    nodeClass.accept(jmVisitor);
	    //Ϊ�����ֵ������������佨�������ⷽ�����������������
	    jmVisitor.addMethodByFieldDeclaration();
	    clazz.setMethods(jmVisitor.getMethodList());
	    //������ӵ��б�
		clazzList.add(clazz);
		return true;  //false�Ļ��������ҳ�����Ƕ�׵��ࡣ
	}

    //ö�ٸ��ࣨ��ӿڣ���������
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
	
	//��������б���
	public List<ClassContext> getClazzList() {
		return clazzList;
	}
	
}
