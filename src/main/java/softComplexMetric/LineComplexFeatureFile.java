/**
 * 
 */
package softComplexMetric;

import java.io.File;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;


import common.ProjectConfiguration;

/** 读取和保存 .cognitive文件
 * 以行为单位的认知复杂度。
 * @author Administrator
 *
 */
/**
 * @author Administrator
 *
 */
public class LineComplexFeatureFile {
	private int bugID;  //defects4j有bugid的概念，.cognitive里存储的是bugid，而非thVer;
	private List<ClassFileComplexValue> fileComplexList; //该版本以文件名和行号组织的代码静态复杂度值。
	private String objectName; //对象名。
	private String complexFilename;//带目录的.complex文件名
	
	//构造函数。
	public LineComplexFeatureFile(String objectName,int bugID) 
	{
		this.bugID = bugID;
		fileComplexList = new ArrayList<>();
		this.objectName = objectName;
		complexFilename = ProjectConfiguration.PathLineComplexFearture+"\\"+objectName+"_v"+bugID+".complex";
	}
	
	public LineComplexFeatureFile(int bugID, List<ClassFileComplexValue> fileComplexList, String objectName,
			String complexFilename) {
		super();
		this.bugID = bugID;
		this.fileComplexList = fileComplexList;
		this.objectName = objectName;
		this.complexFilename = complexFilename;
	}

	//bugid，而非thVer;
	public int getBugID() {
		return bugID;
	}
	
	//该版本以文件名和行号组织的代码静态复杂度值。
	public List<ClassFileComplexValue> getFileComplexList() {
		return fileComplexList;
	}
	
	//给定该版本的代码静态复杂度值序列值。
	public void setFileComplexList(List<ClassFileComplexValue> fileComplexList) {
		this.fileComplexList = fileComplexList;
	}

	//对象名。
	public String getObjectName() {
		return objectName;
	}
	
	//带目录的.complex文件名
	public String getComplexFilename() {
		return complexFilename;
	}

	//记录的带复杂度语句数目，与.profile文件里的可执行语句数目相同。
	private int getTotalStatement()
	{
		int total = 0;
		for( ClassFileComplexValue fcv : fileComplexList )
			total += fcv.getTotalStatement();
		return total;
	}	
	
	/**  将以行为单位的软件静态复杂度写入.complex文件
	 * @return
	 */
	public boolean writeComplexFeartureFile()
	{
		boolean result = true;
		FileOutputStream fos = null;
	    DataOutputStream dos = null;
	    File file;
	    try {
	        file = new File(complexFilename);
	        if( file.isFile()&& file.exists() )
	        	file.delete();
	        file.createNewFile();
	        fos = new FileOutputStream(file);
	        dos = new DataOutputStream(fos);
	        //写入字符串
			dos.writeInt(objectName.length());
			dos.writeBytes(objectName);
	        //dos.writeInt(thVer);//Ubuntu+VisualBox下读取defects4j数据的项目，用的是thVer
            dos.writeInt(bugID);  //写bugid
	        dos.writeInt(fileComplexList.size());//总的文件个数。
	        //逐条语句的谱信息写入。
	        for( ClassFileComplexValue fcv : fileComplexList )
	        {
	        	fcv.writeFile(dos);
	        }
	    }
	    catch (Exception e) {
	    	result = false;
	        e.printStackTrace();
	    } 
	    finally {
	        try {
	            if (fos != null) {
	            	dos.close();
	            	fos.close();
	            }
	        } 
	        catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
		return result;
	}
	
	//从.complex文件读入以行为单位的软件静态复杂度
	public boolean readComplexFeartureFile()
	{
		boolean result = true;
		
		fileComplexList = new ArrayList<> ();
		FileInputStream fis = null;
	    DataInputStream dis = null;
	    File file;
	    try {
	        file = new File(complexFilename);
	        if( file.isFile()&& file.exists() )
	        {
	        	fis = new FileInputStream(file);
	        	dis = new DataInputStream(fis);
	 	        //读入字符串
	 	        int len = dis.readInt();
	            byte []buf = new byte[len];
	            dis.read(buf);
	            objectName = new String(buf);
	            
	        	//thVer = dis.readInt(); //Ubuntu+VisualBox下读取defects4j数据的项目，用的是thVer
	            int bugid = dis.readInt();  //读bugid
	            if( bugid!= bugID )
	            	throw new Exception("The bugid is error when read .profile file.");
	        	int numberOfFiles = dis.readInt();//总的文件个数。
		        //逐个读入文件的以行为单位的软件静态复杂度。
	        	for(int k=0;k<numberOfFiles;k++ )
		        {
	        		ClassFileComplexValue item = new ClassFileComplexValue();
	        		item.readFile(dis);
	        		fileComplexList.add(item);
		        }
	        }//end of if
		    else
		    {
		    	System.out.println(complexFilename+"  is not exist.");
		        result = false;
		    }
		}//end of try...
		catch (Exception e) {
		    	result = false;
		        e.printStackTrace();
		} 
	    finally {
	        try {
	            if (fis != null) {
	            	dis.close();
	            	fis.close();
	            }
	        } 
	        catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
		return result;
	}
	

	/**先获取文件名、行号对应的StatementFeatureStruct， 转换成RankLib要求的特征格式
	 * @param filename
	 * @param lineno
	 * @return 
	 */
	public String getRankLibFeatureString(String filename,int lineno)
	{
		//注意：liblinear-ranksvm只允许一个空格
		String sinfo = "32:0 33:0 34:0 35:0 36:0 37:0 38:1 39:0 40:0 41:0 42:0 43:0 44:0 45:0";
		for( ClassFileComplexValue cfcv : fileComplexList )
		{
			if( cfcv.getFilename().contentEquals(filename) )
			{
				sinfo = cfcv.getRankLibFeatureString(lineno);
				break;
			}
		}
		return sinfo;
	}
	
	/**
	 *  测试。
	 */
	public void testMe() 
	{
		int verth = getBugID();
		System.out.println(objectName+" ver th := "+verth);
		int total = getTotalStatement();
		System.out.println("Total Exec statement: "+total);
		for( ClassFileComplexValue fcv : fileComplexList )
		{
			System.out.println(fcv.getFilename());
			List<StatementFeatureStruct> ssl = fcv.getLineComplexs();
			for( StatementFeatureStruct item : ssl )
			{
				System.out.print(item.toString());
				System.out.println(" ");
			}
		}//end of for ...
	}//end of testMe

}
