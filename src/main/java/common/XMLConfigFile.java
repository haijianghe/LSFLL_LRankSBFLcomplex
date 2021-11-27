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
 * ��RawDatasetConfig.xml��ÿ�������������Ϣ��
 */
public class XMLConfigFile {
	 //���������Ӽ��ۺϳ�һ�����ݼ�������������,�������ļ���ƪ����
	private static Map<String,List<String>> aggregations = new HashMap<String,List<String>>();
	private static Map<String,BenchmarkAttribute> benchamrkes = new HashMap<String,BenchmarkAttribute>();
	
	public static Map<String,List<String>> getAggregations()
	{
		return aggregations;
	}
	
	/**  ���ݶ�������ȡ�����ͣ�����Դ����solo�����Դ����multi
	 * @param objectName  ������������gzip,closure,cli,totinfo,...
	 * @return true,����Դ����solo;  false,���Դ����multi
	 */
	public static boolean isSoloObject(String objectName)
	{
		boolean isSoloProject = false;
		boolean found = false;
		//��ǿ��forѭ��
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
	
	/**  ���ݶ�������ȡ�����ͣ�����Դ����solo�����Դ����multi
	 * @param objectName  ������������gzip,closure,cli,totinfo,...
	 * @return
	 */
	public static String getCommentOfObject(String objectName)
	{
		String comment = "";
		boolean found = false;
		//��ǿ��forѭ��
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
	
	/**  ���ݶ�������ȡ�����ݵĸ�Ŀ¼��
	 * @param objectName  ������������gzip,closure,cli,totinfo,...
	 * @return
	 */
	public static String getDirectoryOfObject(String objectName)
	{
		String directory = "";
		boolean found = false;
		//��ǿ��forѭ��
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

	/**  ���ݶ�������ȡ��Դ�����ļ���������Դ����solo�Ĳ��У����Դ����multi��Ϊ��
	 * @param objectName  ������������gzip,closure,cli,totinfo,...
	 * @return
	 */
	public static String getSourceCodeFileOfObject(String objectName)
	{
		String sourceCode = ""; //��comment =multi����ֵΪ��
		boolean found = false;
		//��ǿ��forѭ��
		for(Map.Entry<String, BenchmarkAttribute>  entry  :  benchamrkes.entrySet()){
			BenchmarkAttribute bma = entry.getValue();
			List<String> objectLst = bma.getObjects();
			for( String project : objectLst )
			{
				if( !project.contentEquals(objectName) )
					continue; //����Ҫ�ҵĶ�������
				//������ƥ�䡣
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
				//multi���͵�sourceCodeֵΪ��
				found = true;
				break;
			}//end of for( String project : objectLst )
			if( found )
				break;
		}//end of for...
		return sourceCode;
	}

	/**
	 * @return ��ȡ�������ݼ����ж�������ּ��ϡ�
	 */
	public static List<String> getAllObjectNames()
	{
		List<String> allObjectNames = new ArrayList<>();
		//��ǿ��forѭ��
		for(Map.Entry<String, BenchmarkAttribute>  entry  :  benchamrkes.entrySet()){
			BenchmarkAttribute bma = entry.getValue();
			List<String> objectLst = bma.getObjects();
			allObjectNames.addAll(objectLst);
			}
		return allObjectNames;
	}
	
	/**  ����ĳĿ¼�������ļ���
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
	 * @return ��ȡ���ݼ�ĳ�汾������Դ�����ļ������ϡ�
	 * SIR C : ֻ��һ��.c�ļ�
	 * PairikaOpenCV C++ �� Ŀ¼�������ļ���
	 * Defects4j��BearsRepair java: Ŀ¼�������ļ���
	 * ����ֵ��1��Դ�����ļ������ϣ�2��language �������� c/c++/java 3,faultSeed,�Ƿ�ı� FaultSeed.h��ֵ
	 */
	public static List<String> getSourceCodeFilenames(String objectName,int bugID,String[] language,boolean[] faultSeed)
	{
		List<String> allFilenames = new ArrayList<>();
		boolean found = false;
		
		faultSeed[0] = false; //��������󶼲�#include FaultSeed.h
		
		//��ǿ��forѭ��
		for(Map.Entry<String, BenchmarkAttribute>  entry  :  benchamrkes.entrySet()){
			BenchmarkAttribute bma = entry.getValue();
			List<String> objectLst = bma.getObjects();
			language[0] = bma.getLanguage(); //������ԡ�
			String parentPath = ""; // Ѱ��Դ�����ļ���Ŀ¼��
			parentPath = ProjectConfiguration.RawDatasetDirectory+"\\"+bma.getDirectory();
			for( String project : objectLst )
			{
				if( !project.contentEquals(objectName) )
					continue;//����Ҫ�ҵĶ�������
				//������ƥ�䡣
				String comment = bma.getComment();
				if( comment.contentEquals("solo") )
				{ 
					String sourceCode = ""; //Դ�����ļ���
					sourceCode = objectName+".c";
					String versionFlag = bma.getVersionFlag();
					if( versionFlag.contentEquals("Vxx") )
					{
						int pos = objectName.lastIndexOf('V');
						sourceCode = objectName.substring(0,pos);
						sourceCode = sourceCode+".c";
						faultSeed[0] = true; //gzip,grep,flex����#include FaultSeed.h
						parentPath = parentPath+"\\"+objectName+"\\versions.alt";
					}
					else
						parentPath = parentPath+"\\"+objectName+"/versions.alt/versions.orig/v"+String.valueOf(bugID);
					allFilenames.add(parentPath+"\\"+sourceCode); //ƴ���ļ�����
				}	
				else 
				{//multi��������ļ���
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
	
	/** �������Ľ������aggregations, benchamrkes
	 * @param configXMLFilename:RawDatasetConfig.xml 
	 * @return
	 */
	public static boolean parseFile()
	{
		String configXMLFilename = ProjectConfiguration.RawDatasetDirectory+"\\"+ProjectConfiguration.RawDatasetXMLConfigFile;
		boolean result = true;
		try {
			//1.����SAXReader����
			SAXReader saxReader=new SAXReader();
			//2.����read�ķ���
			Document xmlDocument;
			xmlDocument = saxReader.read(new File(configXMLFilename));
			//3.��ȡ��Ԫ��
			Element rootCoverage=xmlDocument.getRootElement();
			//4.ʹ�õ�������������ֱ���ӽڵ�
			for(Iterator<Element> iterRoot=rootCoverage.elementIterator();iterRoot.hasNext();) {
				Element profileElement=iterRoot.next();//profile��ֱ���ӽڵ�
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
			Element aggregationElement=aggsIter.next();//aggregations��ֱ���ӽڵ�
			//�ȶ����ԡ�
			String aggsName = "";
			for(Iterator<Attribute> iteAtt=aggregationElement.attributeIterator();iteAtt.hasNext();) {
				Attribute attr=iteAtt.next();
				if( attr.getName().equals("name") ) //aggregation name
					aggsName = attr.getValue();
				else
					continue;
			}
			//�ٶ��ڵ㡣
			List<String> dsItems = new ArrayList<String>();
			//Element name = "item"
			for(Iterator<Element> itemIter=aggregationElement.elementIterator("item");itemIter.hasNext();) {
				Element itemElement = itemIter.next();//aggregation��ֱ���ӽڵ�
				//Element name = "item"
				String itemName = itemElement.getText();
				dsItems.add(itemName);
			}//end of for...itemIter
			aggregations.put(aggsName, dsItems);
		}//end of for...aggsIter
		return result;
	}


	/** parse XML node "benchmark"  �÷�������ͨ���� 
	 * @param benchmarkElement  ����benchmark�ڵ�
	 * @return
	 */
	private static boolean parseBenchmark(Element benchmarkElement)
	{
		boolean result = true;
		//�ȶ����ԡ�
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
		//�ٶ��ڵ㡣
		List<String> objectes = new ArrayList<String>(); //object list
		String benchmarkName = "";
		String directory = "";
		String language = "";
		String versionFlag = "";
		//Element name = "item"
		for(Iterator<Element> iterNode=benchmarkElement.elementIterator();iterNode.hasNext();) {
			Element nodeElement = iterNode.next();//aggregation��ֱ���ӽڵ�
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
