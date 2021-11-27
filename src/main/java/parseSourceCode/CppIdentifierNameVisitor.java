/**
 * 
 */
package parseSourceCode;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTQualifiedName;

/**
 * @author Administrator
 *
 */
public class CppIdentifierNameVisitor extends ASTVisitor {
	/*
	 * �������ظ�����û�й�ϵ�������棬���ʽ����ȥ�ء�
	 */
	String lastName; //��¼�ñ��ʽ�����һ����ʶ����
	List<String> qualifyNames; //��¼�ñ��ʽ�����һ�����������ʶ����
	
	public CppIdentifierNameVisitor()
	{
		super(true);//public ASTVisitor(boolean visitNodes)
		lastName = "";
		qualifyNames = new ArrayList<>();
	}	
	
	//��¼���������һ����ʶ����
	public String getLastName() {
		return lastName;
	}
	
	//��¼�ñ��ʽ�����һ�����������ʶ����
	public List<String> getQualifyNames() {
		return qualifyNames;
	}

	/**
	 * �����C++���ƣ������������������ȡ�
	 */
	@Override
	public int visit( IASTName astName ) {
		if( astName instanceof CPPASTName )
		{
			char[] sname = ((CPPASTName)astName).getSimpleID();
			lastName = new String(sname);
			//System.out.println("            "+new String(sname));
		}
		else if( astName instanceof CPPASTQualifiedName )
		{
			@SuppressWarnings("deprecation")
			IASTName[] 	subNames = ((CPPASTQualifiedName)astName).getNames();
			int number = subNames.length;
			//System.out.print("           ");
			for( int i=0;i<number;i++  )
			{
				IASTName icans = subNames[i];
				if( icans instanceof CPPASTName )
				{
					char[] sname = ((CPPASTName)icans).getSimpleID();
					if( i==(number-1) )
						lastName = new String(sname);
					else
						qualifyNames.add(new String(sname));
				}
				//CPPASTTemplateId���͵�Ҳ���ΪCPPASTQualifiedName��һ���֣���CPPASTTemplateId�޷�ת��ΪCPPASTName;
			}
			//System.out.println(" ");
		}

		else
		{
			/*
			 *  CPPASTConversionName, CPPASTImplicitName, CPPASTName, 
			 *  CPPASTNameBase, CPPASTOperatorName, CPPASTQualifiedName, CPPASTTemplateId
			 */
		}
		return PROCESS_SKIP; //���������ӽڵ㡣
	}
}
