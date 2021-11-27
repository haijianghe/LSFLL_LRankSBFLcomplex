/**
 * 
 */
package common;

import java.util.ArrayList;
import java.util.List;

import parseSourceCode.AstCppFileParse;
import parseSourceCode.AstJavaFileParse;
import parseSourceCode.AstProcedureFileParse;
import parseSourceCode.ClassContext;
import parseSourceCode.ProjectContext;

/**
 * @author Administrator
 *
 */
public class TestMyCode {
	//Java程序
	public static void CheckJavaParseFile()
	{
		//ObjectMapper  Paser2.java DeserializationFeature TypedScopeCreator
		String strFileName = "f:\\MultiFileSBFLProfile\\JDTParseLearn\\parseFileTesting\\Paser2.java";
		//String strFileName = "f:\\MultiFileSBFLProfile\\JDTParseLearn\\parseFileTesting\\ObjectMapper.java";

		AstJavaFileParse atfParse = new AstJavaFileParse();
		List<ClassContext> parsedClazzes = atfParse.parseFile(strFileName);
		if( parsedClazzes==null )
			System.out.println("Parse file is error.");
		else
		{
			ProjectContext pContext = new ProjectContext(3);//3=java
			pContext.addClazzList(strFileName, parsedClazzes);
			String filename = ProjectContext.getFilenameFromPathFile(strFileName);
			parsedClazzes = pContext.getClassNodeFromFile(filename);
			for( ClassContext cnode : parsedClazzes)
				cnode.showMe();
			System.out.println("......");
		}
	}
	
	//C++程序
	public static void CheckCppParseFile()
	{
		//convolution_layer  dnn nbayes
		//String sourceFile = "f:\\MultiFileSBFLProfile\\JDTParseLearn\\parseFileTesting\\rho.cpp";
		//leaf.hpp  flowermain.cpp
		String sourceFile = "f:\\MultiFileSBFLProfile\\JDTParseLearn\\parseFileTesting\\devcpp\\flowermain.cpp";
	
	    AstCppFileParse astTranslationUnitCore = new AstCppFileParse();
		List<ClassContext> parsedClazzes = astTranslationUnitCore.parseFile(sourceFile);
		if( parsedClazzes==null )
			System.out.println("Parse file is error.");
		else
		{
			ProjectContext pContext = new ProjectContext(2);//2=C++
			pContext.addClazzList(sourceFile, parsedClazzes);
			String filename = ProjectContext.getFilenameFromPathFile(sourceFile);
			parsedClazzes = pContext.getClassNodeFromFile(filename);
			for( ClassContext cnode : parsedClazzes)
				cnode.showMe();
			//依据文件名和行号，找出复杂度。可以检测那些未解析到的语句情况。
			/*int vComplex1 = pContext.getCognitiveComplexByFileLineno("flowermain.cpp",149);
			int vComplex2 = pContext.getCognitiveComplexByFileLineno("flowermain.cpp",175);
			int vComplex3 = pContext.getCognitiveComplexByFileLineno("flowermain.cpp",226);
			System.out.println("cognitive value :"+vComplex1+","+vComplex2+","+vComplex3);*/
		}
	}

	//C程序
	public static void CheckMainParseFile()
	{
		//tot_info.c  treeleaf schedule2 gzip
		String sourceFile = "f:\\MultiFileSBFLProfile\\JDTParseLearn\\parseFileTesting\\cMain\\treeleaf.c";
	
	    AstProcedureFileParse astTranslationUnitCore = new AstProcedureFileParse();
		ClassContext classCtx = astTranslationUnitCore.parseFile(sourceFile);
		if( classCtx==null )
			System.out.println("Parse file is error.");
		else
		{
			List<ClassContext> parsedClazzes = new ArrayList<>();
			parsedClazzes.add(classCtx);
			ProjectContext pContext = new ProjectContext(1);//1=C
			pContext.addClazzList(sourceFile, parsedClazzes);
			String filename = ProjectContext.getFilenameFromPathFile(sourceFile);
			parsedClazzes = pContext.getClassNodeFromFile(filename);
			for( ClassContext cnode : parsedClazzes)
				cnode.showMe();
		}
	}

}
