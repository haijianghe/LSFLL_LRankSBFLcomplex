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

/** ��ȡ�ͱ��� .cognitive�ļ�
 * ����Ϊ��λ����֪���Ӷȡ�
 * @author Administrator
 *
 */
/**
 * @author Administrator
 *
 */
public class LineComplexFeatureFile {
	private int bugID;  //defects4j��bugid�ĸ��.cognitive��洢����bugid������thVer;
	private List<ClassFileComplexValue> fileComplexList; //�ð汾���ļ������к���֯�Ĵ��뾲̬���Ӷ�ֵ��
	private String objectName; //��������
	private String complexFilename;//��Ŀ¼��.complex�ļ���
	
	//���캯����
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

	//bugid������thVer;
	public int getBugID() {
		return bugID;
	}
	
	//�ð汾���ļ������к���֯�Ĵ��뾲̬���Ӷ�ֵ��
	public List<ClassFileComplexValue> getFileComplexList() {
		return fileComplexList;
	}
	
	//�����ð汾�Ĵ��뾲̬���Ӷ�ֵ����ֵ��
	public void setFileComplexList(List<ClassFileComplexValue> fileComplexList) {
		this.fileComplexList = fileComplexList;
	}

	//��������
	public String getObjectName() {
		return objectName;
	}
	
	//��Ŀ¼��.complex�ļ���
	public String getComplexFilename() {
		return complexFilename;
	}

	//��¼�Ĵ����Ӷ������Ŀ����.profile�ļ���Ŀ�ִ�������Ŀ��ͬ��
	private int getTotalStatement()
	{
		int total = 0;
		for( ClassFileComplexValue fcv : fileComplexList )
			total += fcv.getTotalStatement();
		return total;
	}	
	
	/**  ������Ϊ��λ�������̬���Ӷ�д��.complex�ļ�
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
	        //д���ַ���
			dos.writeInt(objectName.length());
			dos.writeBytes(objectName);
	        //dos.writeInt(thVer);//Ubuntu+VisualBox�¶�ȡdefects4j���ݵ���Ŀ���õ���thVer
            dos.writeInt(bugID);  //дbugid
	        dos.writeInt(fileComplexList.size());//�ܵ��ļ�������
	        //������������Ϣд�롣
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
	
	//��.complex�ļ���������Ϊ��λ�������̬���Ӷ�
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
	 	        //�����ַ���
	 	        int len = dis.readInt();
	            byte []buf = new byte[len];
	            dis.read(buf);
	            objectName = new String(buf);
	            
	        	//thVer = dis.readInt(); //Ubuntu+VisualBox�¶�ȡdefects4j���ݵ���Ŀ���õ���thVer
	            int bugid = dis.readInt();  //��bugid
	            if( bugid!= bugID )
	            	throw new Exception("The bugid is error when read .profile file.");
	        	int numberOfFiles = dis.readInt();//�ܵ��ļ�������
		        //��������ļ�������Ϊ��λ�������̬���Ӷȡ�
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
	

	/**�Ȼ�ȡ�ļ������кŶ�Ӧ��StatementFeatureStruct�� ת����RankLibҪ���������ʽ
	 * @param filename
	 * @param lineno
	 * @return 
	 */
	public String getRankLibFeatureString(String filename,int lineno)
	{
		//ע�⣺liblinear-ranksvmֻ����һ���ո�
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
	 *  ���ԡ�
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
