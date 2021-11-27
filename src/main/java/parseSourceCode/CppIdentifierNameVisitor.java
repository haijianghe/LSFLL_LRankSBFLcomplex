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
	 * 这里有重复内容没有关系，在外面，表达式部分去重。
	 */
	String lastName; //记录该表达式的最后一个标识符。
	List<String> qualifyNames; //记录该表达式除最后一个外的其它标识符。
	
	public CppIdentifierNameVisitor()
	{
		super(true);//public ASTVisitor(boolean visitNodes)
		lastName = "";
		qualifyNames = new ArrayList<>();
	}	
	
	//记录该语句的最后一个标识符。
	public String getLastName() {
		return lastName;
	}
	
	//记录该表达式除最后一个外的其它标识符。
	public List<String> getQualifyNames() {
		return qualifyNames;
	}

	/**
	 * 语句中C++名称，包括变量名、类名等。
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
				//CPPASTTemplateId类型的也会成为CPPASTQualifiedName的一部分，而CPPASTTemplateId无法转换为CPPASTName;
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
		return PROCESS_SKIP; //不再搜索子节点。
	}
}
