/**
 * 
 */
package softComplexMetric;

import java.util.ArrayList;
import java.util.List;

import affiliated.FileSpectrum;
import affiliated.IProfileFile;
import affiliated.SpectrumStruct;
import common.XMLConfigFile;
import parseSourceCode.AstCppFileParse;
import parseSourceCode.AstJavaFileParse;
import parseSourceCode.AstProcedureFileParse;
import parseSourceCode.ClassContext;
import parseSourceCode.ProjectContext;

/** 以行为单位的认知复杂度。
 * @author Administrator
 *
 */
public class SoftwareLineComplexMetricFeature {
	private String objectName; //对象名。
	private int bugID;  //defects4j有bugid的概念，.profile里存储的是bugid，而非thVer;

	private ProjectContext pContext;//某一个项目版本的解析结果，各条语句的特征，有语句所在方法、类的信息。
	private String pathForCheckup; //记录被解析文件的绝对路径，将来用于检测无效的软件复杂度值。

	/**  	空的构造函数
	 */
	public SoftwareLineComplexMetricFeature(String objectName, int bugID) {
		this.objectName = objectName;
		this.bugID = bugID;
		pContext = null;
		pathForCheckup = "";
	}
	
	/**
	 * 计算软件复杂度的特征值。
	 */
	public boolean calComplexMetricValue() {
		String[] language = new String[1]; //编程语言
		boolean[] faultSeed = new boolean[1];//是否带#include "FaultSeed.h"
		List<String> allFilenames = XMLConfigFile.getSourceCodeFilenames(objectName, bugID,language, faultSeed);
		/*allFilenames任意取一个文件，找出其绝对路径，备用,用于检查无效的软件复杂度值。*/
		String parsedFilename =   allFilenames.get(0);
		int pos = parsedFilename.lastIndexOf('\\');
		pathForCheckup = parsedFilename.substring(0,pos);

		pContext = new ProjectContext(language[0]);
		//解析所有文件。
		boolean parsingError = false;
		for( String filename :allFilenames )
		{
			//if( !filename.contains("TarArchiveEntry.java") )  //for test.
			//	continue;
			//System.out.println(filename+" 's paring is start.");//for test.
			List<ClassContext> parsedClazzes = parseFile(filename,language[0],faultSeed[0]);
			if( parsedClazzes==null )
				parsingError = true;
			else
				pContext.addClazzList(filename, parsedClazzes);
			//System.out.println(filename);
		}
		if( !parsingError )
		{
			System.out.print("V"+bugID+",");
			return true;
		}
		else
		{
			System.out.println(objectName+"_V"+bugID+" parsing is fail.");
			return false;
		}
	}
	
	/**  解析代码
	 * @param sourceFile  	待解析的文件名。
	 * @param language      编程语言
	 * @param isFaultSeed   是否带FaultSeed.h 只有SIR C语言程序才有
	 * @return 解析结果
	 */
	private  List<ClassContext> parseFile(String sourceFile,String language,boolean isFaultSeed)
	{
		//boolean parseError = false; //解析过程有错误。
		List<ClassContext> parsedClazzes = null;
		if( language.contentEquals("C") )
		{
			//注意：先要改变#include "FaultSeed.h"
			if( isFaultSeed )
			{
				//bugID可以当做行号
				FaultSeedIncludeFile.awakenIncludeBug(pathForCheckup, bugID);
			}
			AstProcedureFileParse astTranslationUnitCore = new AstProcedureFileParse();
			ClassContext classCtx = astTranslationUnitCore.parseFile(sourceFile);
			if( classCtx==null )
				System.out.println("Parse file is error."+sourceFile);
			else
			{
				 parsedClazzes = new ArrayList<>();
				 parsedClazzes.add(classCtx);
			}
		}
		else if( language.contentEquals("C++") )
		{
			 AstCppFileParse astTranslationUnitCore = new AstCppFileParse();
			 parsedClazzes = astTranslationUnitCore.parseFile(sourceFile);
			 if( parsedClazzes==null )
					System.out.println("Parse file is error."+sourceFile);
		}
		else if( language.contentEquals("java") )
		{
			AstJavaFileParse atfParse = new AstJavaFileParse();
			parsedClazzes = atfParse.parseFile(sourceFile);
			if( parsedClazzes==null )
				System.out.println("Parse file is error."+sourceFile);
		}
			
		else
			{};
		return parsedClazzes;
	}
	
	
	/*这里的复杂度是指： 以行为单位的软件静态复杂度
	 * 保存到.complex文件。
	 * passed该版本的成功测试用例个数； failed该版本的未通过测试用例个数
	 * 有些行的复杂度找不到，false;全部都找到,则true。
	 * 保存文件正确，则true；保存过程出错，则false
	 */
	public boolean writeComplexMetricFeatureFile(List<FileSpectrum> fileSpectra, int passed,int failed) {
		boolean result = true;
		//找出对应fileSpectra里所有文件每一行的复杂度，组装成List<FileComplexValue>
		List<ClassFileComplexValue> fComplexs = new ArrayList<>(); 
		for( FileSpectrum fsp: fileSpectra )
		{
			String classFilename = fsp.getClassFilename(); //类名对应的文件名
			//if( !classFilename.contains("TarArchiveEntry.java") )  //for test.
			//	continue;
			CheckupInvalidComplexValue cuicv = new CheckupInvalidComplexValue(pathForCheckup,classFilename);
			//创建一个.complex文件需要的结构，里面的行号与.profile的行号对应。
			ClassFileComplexValue fcvItem = new ClassFileComplexValue();
			fcvItem.setFilename(classFilename);
			List<SpectrumStruct> lineCodes = fsp.getLineCodes();//该文件包含的行覆盖数据。
			//先计算好该文件（classFilename）里所有语句的代码静态复杂度。
			pContext.computerSourceCodeStaticFeature(classFilename, lineCodes, passed, failed);
			for( SpectrumStruct ss: lineCodes )
			{
				int lineno = ss.getLineNo();
				//if( lineno==3955 )
				//	lineno = lineno+1-1; //为了调试程序，这么做。
				StatementFeatureStruct fsStatement = pContext.getCodeStaticComplexMetricByFileLineno(classFilename, lineno);
				/*有几种情况，导致fsStatement 的行号<0
				 * 1,源代码是} 或者一些非执行语句，被覆盖率工具软件记录为可执行语句。
				 *    java的enum  C/C++宏定义后出现的program类型
				 * 2，我的解析代码有遗漏，未捕捉到这些语句。
				 * 3，我的getCognitiveComplexByFileLineno代码有bug，这种可能性极小。
				 */
				if( fsStatement.getLineno()<=0 )
				{
					//无效值，强制给值0。注意后续处理方式。
					fsStatement.setNotFoundStatement(lineno);
					fcvItem.addOneComplexPriceStruct(fsStatement);
					//result = false;
					cuicv.addLineno(lineno);//加入检查对象。
					//System.out.println(classFilename+","+lineno+"    cognitive complex value is error");
				}
				else
					fcvItem.addOneComplexPriceStruct(fsStatement);
			}//enf of for( SpectrumStruct ss: lineCodes )
			//将该文件的认知复杂度添加到List<FileComplexValue>
			fComplexs.add(fcvItem);
			//显示带无效值的行号，便于检查代码。
			//cuicv.showInvalidLineno(); //check my .profile file.
		}//end of for( FileSpectrum fsp: fileSpectra )
		//保存到.complex文件
		LineComplexFeatureFile lcvFile = new LineComplexFeatureFile(objectName, bugID);
		lcvFile.setFileComplexList(fComplexs);
		result = lcvFile.writeComplexFeartureFile();
		return result;
	}
	
	/**
	 * 测试。
	 */
	public void testMe() {
		System.out.println("Coming soon...");
	}
}
