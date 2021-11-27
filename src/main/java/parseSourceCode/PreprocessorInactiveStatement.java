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

/** 预处理中的inactive语句。
 * @author Administrator
 *
 */
public class PreprocessorInactiveStatement {
	/*
	 * 属性和方法都定义为静态的，避免传递参数。
	 */
	private static IASTTranslationUnit curUnitCompile = null; //当前的解析单元。

	/**
	 * @param unitCompile
	 * @param parsingPathname
	 */
	public static void parseInactiveStatement(IASTTranslationUnit unitCompile,String parsingPathname)
	{
		curUnitCompile = unitCompile;//赋值，供后续方法使用。
		
		IASTPreprocessorStatement[] prepStatms = unitCompile.getAllPreprocessorStatements();
		for( IASTPreprocessorStatement statement : prepStatms )
		{
			String inFilename = statement.getContainingFilename();
			if( !inFilename.contentEquals(parsingPathname) )
				continue; //不在解析文件里的预处理，不解析。
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
