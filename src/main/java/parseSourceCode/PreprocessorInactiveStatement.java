/**
 * 
 */
package parseSourceCode;

import org.eclipse.cdt.core.dom.ast.IASTPreprocessorElifStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorElseStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorEndifStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfdefStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfndefStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

/** Ԥ�����е�inactive��䡣
 * @author Administrator
 *
 */
public class PreprocessorInactiveStatement {
	/*
	 * ���Ժͷ���������Ϊ��̬�ģ����⴫�ݲ�����
	 */
	private static IASTTranslationUnit curUnitCompile = null; //��ǰ�Ľ�����Ԫ��

	/**
	 * @param unitCompile
	 * @param parsingPathname
	 */
	public static void parseInactiveStatement(IASTTranslationUnit unitCompile,String parsingPathname)
	{
		curUnitCompile = unitCompile;//��ֵ������������ʹ�á�
		
		IASTPreprocessorStatement[] prepStatms = unitCompile.getAllPreprocessorStatements();
		for( IASTPreprocessorStatement statement : prepStatms )
		{
			String inFilename = statement.getContainingFilename();
			if( !inFilename.contentEquals(parsingPathname) )
				continue; //���ڽ����ļ����Ԥ������������
			 if( statement instanceof   IASTPreprocessorElifStatement  )
			 {
				IASTPreprocessorElifStatement iapes = (IASTPreprocessorElifStatement)statement;
				System.out.println("^^^^  "+iapes.getRawSignature()+","+iapes.taken());
			 }
			 else if (	 statement instanceof   IASTPreprocessorElseStatement   )
			 {
				IASTPreprocessorElseStatement iapes = (IASTPreprocessorElseStatement)statement;
				System.out.println("^^^^  "+iapes.getRawSignature()+","+iapes.taken());
				
				 
			 }
			else if ( statement instanceof   IASTPreprocessorIfndefStatement )
			{
				
			}
			else if(  statement instanceof   IASTPreprocessorIfdefStatement  )
			{
				
			}
			else if ( statement instanceof    IASTPreprocessorEndifStatement  )
			{
				
			}
			else if (statement instanceof   IASTPreprocessorIfStatement  	)  
			 {
			System.out.println("^^^^  "+statement.getRawSignature());
			//return PROCESS_CONTINUE;
			 }
		}//end of for ... statement
	}
}
