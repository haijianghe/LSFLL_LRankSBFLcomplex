/**
 * 
 */
package common;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * @author Administrator
 * 从RawDatasetConfig.xml读每个对象的配置信息。
 */
public class XMLConfigFile {
	 //几个数据子集聚合成一个数据集，计算其性能,便于论文减少篇幅。
	private static Map<String,List<String>> aggregations = new HashMap<String,List<String>>();
	private static Map<String,BenchmarkAttribute> benchamrkes = new HashMap<String,BenchmarkAttribute>();
	
	public static Map<String,List<String>> getAggregations()
	{
		return aggregations;
	}
	
	/**  依据对象名获取其类型：单个源代码solo，多个源代码multi
	 * @param objectName  对象名，比如gzip,closure,cli,totinfo,...
	 * @return true,单个源代码solo;  false,多个源代码multi
	 */
	public static boolean isSoloObject(String objectName)
	{
		boolean isSoloProject = false;
		boolean found = false;
		//增强型for循环
		for(Map.Entry<String, BenchmarkAttribute>  entry  :  benchamrkes.entrySet()){
			BenchmarkAttribute bma = entry.getValue();
			List<String> objectLst = bma.getObjects();
			for( String project : objectLst )
			{
				if( project.contentEquals(objectName) )
				{
					String comment = bma.getComment();
					if( comment.contentEquals("solo") )
						isSoloProject = true;
					else if( comment.contentEquals("multi") )
						isSoloProject = false;
					else
						System.out.println("XMLConfigFile::isSoloObject error.");
					found = true;
					break;
				}
			}
			if( found )
				break;
		}
		return isSoloProject;
	}
	
	/**  依据对象名获取其类型：单个源代码solo，多个源代码multi
	 * @param objectName  对象名，比如gzip,closure,cli,totinfo,...
	 * @return
	 */
	public static String getCommentOfObject(String objectName)
	{
		String comment = "";
		boolean found = false;
		//增强型for循环
		for(Map.Entry<String, BenchmarkAttribute>  entry  :  benchamrkes.entrySet()){
			BenchmarkAttribute bma = entry.getValue();
			List<String> objectLst = bma.getObjects();
			for( String project : objectLst )
			{
				if( project.contentEquals(objectName) )
				{
					comment = bma.getComment();
					found = true;
					break;
				}
			}
			if( found )
				break;
		}
		return comment;
	}
	
	/**  依据对象名获取其数据的根目录。
	 * @param objectName  对象名，比如gzip,closure,cli,totinfo,...
	 * @return
	 */
	public static String getDirectoryOfObject(String objectName)
	{
		String directory = "";
		boolean found = false;
		//增强型for循环
		for(Map.Entry<String, BenchmarkAttribute>  entry  :  benchamrkes.entrySet()){
			BenchmarkAttribute bma = entry.getValue();
			List<String> objectLst = bma.getObjects();
			for( String project : objectLst )
			{
				if( project.contentEquals(objectName) )
				{
					directory = bma.getDirectory();
					found = true;
					break;
				}
			}
			if( found )
				break;
		}
		return directory;
	}

	/**  依据对象名获取其源代码文件名：单个源代码solo的才有，多个源代码multi的为空
	 * @param objectName  对象名，比如gzip,closure,cli,totinfo,...
	 * @return
	 */
	public static String getSourceCodeFileOfObject(String objectName)
	{
		String sourceCode = ""; //若comment =multi，此值为空
		boolean found = false;
		//增强型for循环
		for(Map.Entry<String, BenchmarkAttribute>  entry  :  benchamrkes.entrySet()){
			BenchmarkAttribute bma = entry.getValue();
			List<String> objectLst = bma.getObjects();
			for( String project : objectLst )
			{
				if( !project.contentEquals(objectName) )
					continue; //不是要找的对象名。
				//对象名匹配。
				String comment = bma.getComment();
				if( comment.contentEquals("solo") )
				{ 
					sourceCode = objectName+".c";
					String versionFlag = bma.getVersionFlag();
					if( versionFlag.contentEquals("Vxx") )
					{
						int pos = objectName.lastIndexOf('V');
						sourceCode = objectName.substring(0,pos);
						sourceCode = sourceCode+".c";
					}
				}//end of if...	
				//multi类型的sourceCode值为空
				found = true;
				break;
			}//end of for( String project : objectLst )
			if( found )
				break;
		}//end of for...
		return sourceCode;
	}

	/**
	 * @return 获取所有数据集所有对象的名字集合。
	 */
	public static List<String> getAllObjectNames()
	{
		List<String> allObjectNames = new ArrayList<>();
		//增强型for循环
		for(Map.Entry<String, BenchmarkAttribute>  entry  :  benchamrkes.entrySet()){
			BenchmarkAttribute bma = entry.getValue();
			List<String> objectLst = bma.getObjects();
			allObjectNames.addAll(objectLst);
			}
		return allObjectNames;
	}
	
	/**  搜索某目录下所有文件。
	 * @param parentPath
	 * @return
	 */
	private static List<String> getAllFilesFromDirectory(String parentPath)
	{
		List<String> allFilenames = new ArrayList<>();
		File directory = new File(parentPath);
		File[] files = directory.listFiles();
		for (File file : files) {
			if (file.isFile()) {
				/*if (file.toString().endsWith(type)) {
							fileList.add(file);
						}*/
				allFilenames.add(file.getAbsolutePath());
			} //end of if
		}

		return allFilenames;
	}
	
	/**
	 * @return 获取数据集某版本包含的源代码文件名集合。
	 * SIR C : 只有一个.c文件
	 * PairikaOpenCV C++ ： 目录下所有文件。
	 * Defects4j，BearsRepair java: 目录下所有文件。
	 * 返回值：1，源代码文件名集合，2，language 语言类型 c/c++/java 3,faultSeed,是否改变 FaultSeed.h的值
	 */
	public static List<String> getSourceCodeFilenames(String objectName,int bugID,String[] language,boolean[] faultSeed)
	{
		List<String> allFilenames = new ArrayList<>();
		boolean found = false;
		
		faultSeed[0] = false; //大多数对象都不#include FaultSeed.h
		
		//增强型for循环
		for(Map.Entry<String, BenchmarkAttribute>  entry  :  benchamrkes.entrySet()){
			BenchmarkAttribute bma = entry.getValue();
			List<String> objectLst = bma.getObjects();
			language[0] = bma.getLanguage(); //编程语言。
			String parentPath = ""; // 寻找源代码文件的目录。
			parentPath = ProjectConfiguration.RawDatasetDirectory+"\\"+bma.getDirectory();
			for( String project : objectLst )
			{
				if( !project.contentEquals(objectName) )
					continue;//不是要找的对象名。
				//对象名匹配。
				String comment = bma.getComment();
				if( comment.contentEquals("solo") )
				{ 
					String sourceCode = ""; //源代码文件名
					sourceCode = objectName+".c";
					String versionFlag = bma.getVersionFlag();
					if( versionFlag.contentEquals("Vxx") )
					{
						int pos = objectName.lastIndexOf('V');
						sourceCode = objectName.substring(0,pos);
						sourceCode = sourceCode+".c";
						faultSeed[0] = true; //gzip,grep,flex等有#include FaultSeed.h
						parentPath = parentPath+"\\"+objectName+"\\versions.alt";
					}
					else
						parentPath = parentPath+"\\"+objectName+"/versions.alt/versions.orig/v"+String.valueOf(bugID);
					allFilenames.add(parentPath+"\\"+sourceCode); //拼接文件名。
				}	
				else 
				{//multi，有许多文件。
					parentPath = parentPath+"\\"+objectName+"\\"+bma.getSourceCode()+"\\v"+String.valueOf(bugID)+"\\buggy";
					allFilenames = getAllFilesFromDirectory(parentPath);	
				}
				found = true;
				break;
			}//end of for( String project : objectLst )
			if( found )
				break;
		}//end of for...
		return allFilenames;
	}
	
	/** 将解析的结果存入aggregations, benchamrkes
	 * @param configXMLFilename:RawDatasetConfig.xml 
	 * @return
	 */
	public static boolean parseFile()
	{
		String configXMLFilename = ProjectConfiguration.RawDatasetDirectory+"\\"+ProjectConfiguration.RawDatasetXMLConfigFile;
		boolean result = true;
		try {
			//1.创建SAXReader对象
			SAXReader saxReader=new SAXReader();
			//2.调用read的方法
			Document xmlDocument;
			xmlDocument = saxReader.read(new File(configXMLFilename));
			//3.获取根元素
			Element rootCoverage=xmlDocument.getRootElement();
			//4.使用迭代器遍历集合直接子节点
			for(Iterator<Element> iterRoot=rootCoverage.elementIterator();iterRoot.hasNext();) {
				Element profileElement=iterRoot.next();//profile的直接子节点
				if( profileElement.getName().equals("aggregations") )
					parseAggregations(profileElement);
				if( profileElement.getName().equals("benchmark") ) 
				{
					parseBenchmark(profileElement);
				}//end of if...
				if( false==result )
					break;
			}
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			result = false;
			e.printStackTrace();
		}
		return result;
	}
	

	/** parse XML node "aggregations"  
	 * @param packElement 
	 * @return
	 */
	private static boolean parseAggregations(Element aggsElement)
	{
		boolean result = true;
		for(Iterator<Element> aggsIter=aggsElement.elementIterator("aggregation");aggsIter.hasNext();) {
			Element aggregationElement=aggsIter.next();//aggregations的直接子节点
			//先读属性。
			String aggsName = "";
			for(Iterator<Attribute> iteAtt=aggregationElement.attributeIterator();iteAtt.hasNext();) {
				Attribute attr=iteAtt.next();
				if( attr.getName().equals("name") ) //aggregation name
					aggsName = attr.getValue();
				else
					continue;
			}
			//再读节点。
			List<String> dsItems = new ArrayList<String>();
			//Element name = "item"
			for(Iterator<Element> itemIter=aggregationElement.elementIterator("item");itemIter.hasNext();) {
				Element itemElement = itemIter.next();//aggregation的直接子节点
				//Element name = "item"
				String itemName = itemElement.getText();
				dsItems.add(itemName);
			}//end of for...itemIter
			aggregations.put(aggsName, dsItems);
		}//end of for...aggsIter
		return result;
	}


	/** parse XML node "benchmark"  该方法调试通过。 
	 * @param benchmarkElement  单个benchmark节点
	 * @return
	 */
	private static boolean parseBenchmark(Element benchmarkElement)
	{
		boolean result = true;
		//先读属性。
		String comment = "",sourceCode = "";
		for(Iterator<Attribute> iteAtt=benchmarkElement.attributeIterator();iteAtt.hasNext();) {
			Attribute attr=iteAtt.next();
			if( attr.getName().equals("comment") ) //comment
				comment = attr.getValue();
			else if( attr.getName().equals("sourceCode") ) //comment
				sourceCode = attr.getValue();
			else
				continue;
		}
		//再读节点。
		List<String> objectes = new ArrayList<String>(); //object list
		String benchmarkName = "";
		String directory = "";
		String language = "";
		String versionFlag = "";
		//Element name = "item"
		for(Iterator<Element> iterNode=benchmarkElement.elementIterator();iterNode.hasNext();) {
			Element nodeElement = iterNode.next();//aggregation的直接子节点
			//Element name = "....."
			if( nodeElement.getName().equals("benchmarkName") )
				benchmarkName = nodeElement.getText();
			else if( nodeElement.getName().equals("directory") )
				directory = nodeElement.getText();
			else if( nodeElement.getName().equals("language") )
				language = nodeElement.getText();
			else if( nodeElement.getName().equals("object") )
				objectes.add(nodeElement.getText());
			else if( nodeElement.getName().equals("versionFlag") )
				versionFlag = nodeElement.getText();
			else
				continue;
		}//end of for...itemIter
		BenchmarkAttribute bma = new BenchmarkAttribute(comment, sourceCode, benchmarkName, directory,
										language, objectes,versionFlag);
		benchamrkes.put(benchmarkName, bma);
		return result;
	}
	
}
