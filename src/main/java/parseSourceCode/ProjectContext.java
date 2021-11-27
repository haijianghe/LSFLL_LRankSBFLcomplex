/**
 * 
 */
package parseSourceCode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import affiliated.SpectrumStruct;
import softComplexMetric.StatementFeatureStruct;

/** 整个项目的解析数据。
 * @author Administrator
 *
 */
/**
 * @author Administrator
 * 注意： 解析文件名会作为Map对象的key.
 */
public class ProjectContext {
	private int language; //1=C,2=C++,3=Java
	/*记录工程所有解析数据。一般来说，一个文件只有一个类，也就是说List<ClassNode>只有单个对象。
	 *针对C语言，类名规约为文件名；属性名规约为文件内的全局变量。 
	 *C语言和C++，头文件和主文件可能分开，String的文件名是指主文件,而ClassContext的parsingFilename则是解析时遇到的第一个文件名；
	 *对Java来说，则不存在此问题。String的文件名和ClassContext的parsingFilename是同一个东西，此时冗余。
	 */
	Map<String,List<ClassContext>>  codeContexts;  
	
	//构造函数。
	public ProjectContext(int type)
	{
		this.language = type;
		codeContexts = new HashMap<String,List<ClassContext>>();
	}
	
	public ProjectContext(String codeType)
	{
		if( codeType.contentEquals("C") )
			this.language = 1;
		else if( codeType.contentEquals("C++") )
			this.language = 2;
		else if( codeType.contentEquals("Java") )
			this.language = 3;
		else
			this.language = 0;
		codeContexts = new HashMap<String,List<ClassContext>>();
	}
	
	/** 添加一个文件解析结果
	 * @param pathName  被解析的源程序文件,带目录信息。
	 * @param clazzLst  该文件的解析结果。
	 */
	public void addClazzList(String pathName,List<ClassContext> clazzLst)
	{
		codeContexts.put(getFilenameFromPathFile(pathName), clazzLst);
	}
	
	
	//由文件名获取其解析结果。
	public List<ClassContext> getClassNodeFromFile(String filename)
	{
		return codeContexts.get(filename);
	}
	
	
	/** 从带路径的文件名中取纯粹的文件名。
	 * @param pathName 带路径的文件名
	 * @return
	 */
	public static String getFilenameFromPathFile(String pathName)
	{
		int pos = pathName.lastIndexOf('\\');
		String filename = pathName.substring(pos+1);
		return filename;
	}
	
	/** 把ctxLst内所有类的语句特征，容易计算的部分，先计算好，填充到lstStatementFeature中。
	 * @param ctxLst
	 */
	private void fillEasyFeature(List<ClassContext> ctxLst)
	{
		for( ClassContext cContext : ctxLst )
		{
			cContext.fillEasyFeature();
		}
	}
	
	/**把ctxLst内所有类的语句特征，VFL部分，先计算好，填充到StatementFeatureStruct中。
	 *   VFL: variable-based fault localization
	 * @param ctxLst
	 * @param lineCodes  每条语句的程序谱。
	 * @param passed
	 * @param failed
	 */
	private void fillVFLFearture(List<ClassContext> ctxLst,List<SpectrumStruct> lineCodes,
			int passed,int failed)
	{
		for( ClassContext cContext : ctxLst )
		{
			cContext.fillVFLFearture(lineCodes,passed,failed);
		}
		
	}
	
	/** 将文件内所有语句的代码静态复杂度特征计算好，存放在lstStatementFeature中，后面再查找。
	 * @param classFilename 类名对应的文件名，由此查找到List<ClassContext>
	 * @param lineCodes 每条语句的程序谱。
	 * @param passed 某文件对应的该版本的成功测试用例个数
	 * @param failed 该版本的未通过测试用例个数
	 */
	public void computerSourceCodeStaticFeature(String classFilename,List<SpectrumStruct> lineCodes,
							int passed,int failed)
	{
		List<ClassContext> ctxLst = null;
		for(Map.Entry<String, List<ClassContext>>  entry  :  codeContexts.entrySet()){
			String parsingFilename = entry.getKey();
			if( parsingFilename.contentEquals(classFilename) )
			{
				//文件名匹配
				ctxLst = entry.getValue();
				break;
			}
		}
		if( ctxLst==null )
			return;  //未找到文件对应的解析数据。
		/*把语句特征，容易计算的部分，先计算好，填充到StatementFeatureStruct中。
		 * 容易计算的部分是指：
		 *    局部变量种类数、参数种类数、属性和全局变量种类数、语句类型、逻辑操作符种类数、除逻辑外的操作符种类数、函数调用种类数
		 */
		fillEasyFeature(ctxLst);
		//计算VFL特征 VFL: variable-based fault localization
		fillVFLFearture(ctxLst,lineCodes,passed,failed);
	}
	
	/** 由文件名和其行号，找出对应的软件代码静态复杂度
	 * @param filename  文件名
	 * @param lineno    行号
	 * @return 复杂度值，
	 *           在文件中，已经找到，行号是lineno值>0。
	 *         肯定在文件中，未找到，则返回StatementFeatureStruct对象的行号设置为0. 后续不用再继续找
	 *          肯定不在文件中，  则返回StatementFeatureStruct对象的行号设置为-1.
	 */
	public StatementFeatureStruct getCodeStaticComplexMetricByFileLineno(String filename,int lineno) 
	{
		StatementFeatureStruct fsStatement = new StatementFeatureStruct();
		for(Map.Entry<String, List<ClassContext>>  entry  :  codeContexts.entrySet()){
			String parsingFilename = entry.getKey();
			if( !parsingFilename.contentEquals(filename) )
				continue;
			//文件名匹配  文件虽然找到，可能找不到行号。
			List<ClassContext> ctxLst = entry.getValue();
			for( ClassContext cContext : ctxLst )
			{
				StatementFeatureStruct sfs = cContext.getCodeStaticComplexMetricByFileLineno(lineno);
				int locLineno = sfs.getLineno();//-1 表示不在类cContext中；0表示在类cContext中，未找到；>0找到。
				if( locLineno>0 ) 
				{//找到该行号的软件代码静态复杂度
					fsStatement = sfs;
					break; 
				}
				else if ( locLineno==0 ) 
				{ //表示在类cContext中，未找到。肯定在此文件中，但是找不到具体语句。可能语句是{, }等
					fsStatement.setLineno(0);
					break;
				}
				else //不在类cContext中，继续在当前文件查找。
					continue;
			}//end of for...
			break;//文件找到，行号找不到;或者已经读到复杂度。无论哪种情况，都退出循环
		}
		return fsStatement;
	}
}
