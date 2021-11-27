/**
 * 
 */
package affiliated;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Administrator
 *只保存单个.profile数据（有多个源代码的程序谱），并非保存所有版本的。
 */
public class MultiProfileFile extends AbstractProfileFile {
	//private int thVer;  //第几个版本，注意不是版本个数verNo。从1开始的自然数序列。
	private int bugID;  //defects4j有bugid的概念，.profile里存储的是bugid，而非thVer;
	/* 特别注意：
	 *    在MultiProfileFile中，FileSpectrum和文件名称并非一一对应，一个文件名可能找到多个FileSpectrum，
	 *    内部类、嵌套类等情况就是如此。
	 * */
	private List<FileSpectrum> fileSpecta; //程序谱
	
	//构造函数，数据从文件读入。for : readProfileFile
	public MultiProfileFile(String object,int bugid,String profileFilename)
	{
		super(object,profileFilename);
		bugID = bugid;
		fileSpecta =  new ArrayList<FileSpectrum> ();
	}
	
	//某一个bugid版本的程序谱。
	public MultiProfileFile(String object,int bugid,int passed,int failed)
	{
		super(passed,failed,0,object);
		bugID = bugid;
		fileSpecta = new ArrayList<FileSpectrum> ();
	}
	
	//注意bugid是版本号。
	//并非从1开始的自然数序列。
	public MultiProfileFile(String object,int bugid,int passed,int failed,int total,
			      List<FileSpectrum> ssList)
	{
		super(passed,failed,total,object);
		bugID = bugid;
		fileSpecta = ssList;
	}
	
	//defects4j有bugid的概念，.profile里存储的是bugid，注意与thVer的差别。
	public int getBugId()
	{
		return bugID;
	}
	
	//获取总的可执行语句数目
	@Override
	public int getTotalExec()
	{
		calTotalExec();
		return execTotal;
	}

	//计算总的可执行语句数目
	private void calTotalExec()
	{
		int total = 0;
		for( FileSpectrum fsp : fileSpecta )
			total += fsp.getTotalExec();
		execTotal = total;
	}

	//多文件的程序谱
	@Override
	public List<FileSpectrum> getSpectrumList()
	{
		return fileSpecta;
	}
	
	/** 某故障语句在文件中是否存在。
	 * @param faultFile 故障文件名
	 * @param faultLine 故障语句
	 */
	public boolean isExistFault(String faultFile,int faultLine)
	{
		boolean isExist = false;
		for( FileSpectrum fspect : fileSpecta)
		{
			if( !fspect.getClassFilename().equals(faultFile) )
				continue;
			//文件名匹配
			List<SpectrumStruct> ssList = fspect.getLineCodes();
			for( SpectrumStruct ss : ssList )
				if( ss.getLineNo()==faultLine )
				{ //文件名相同，行号也有。
					isExist = true;
					break;
				}
			if( isExist )
				break; //注意：FileSpectrum和文件名称并非一一对应，一个文件名可能找到多个FileSpectrum。
		}
		return isExist; 
	}
	
	//从.profile文件读入程序谱
	@Override
	public boolean readProfileFile()
	{
		boolean result = true;
		
		fileSpecta = new ArrayList<FileSpectrum> ();
		FileInputStream fis = null;
	    DataInputStream dis = null;
	    File file;
	    try {
	        file = new File(profileFilename);
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
	        	tcPassed = dis.readInt();
	        	tcFailed = dis.readInt();
	        	execTotal = dis.readInt();
	        	int numberOfFiles = dis.readInt();//总的文件个数。
		        //逐个读入文件的谱信息。
	        	for(int k=0;k<numberOfFiles;k++ )
		        {
	        		FileSpectrum item = new FileSpectrum();
	        		item.readFile(dis);
	        		//一定要测试下面的排序方法。
	        		item.sortLineNo();//######注意，defects4j是排序好的，不用这么做；其它数据集呢？
	        		fileSpecta.add(item);
		        }
	        }//end of if
	        else
	        	result = false;
	    }
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
	
	
	/**  将程序谱写入.profile文件
	 * @return
	 */
	@Override
	public boolean writeProfileFile()
	{
		boolean result = true;
		FileOutputStream fos = null;
	    DataOutputStream dos = null;
	    File file;
	    try {
	        file = new File(profileFilename);
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
	        dos.writeInt(tcPassed);
	        dos.writeInt(tcFailed);
	        calTotalExec();
	        dos.writeInt(execTotal);
	        dos.writeInt(fileSpecta.size());//总的文件个数。
	        //逐条语句的谱信息写入。
	        for( FileSpectrum fspect : fileSpecta )
	        {
        		fspect.writeFile(dos);
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
	
	/** 查找对应filename和lineno的程序谱
	 *  注意：FileSpectrum和文件名称并非一一对应，一个文件名可能找到多个FileSpectrum。
	 * @param filename
	 * @param lineno
	 * @return 返回程序谱，没有找到的话，返回结果的行号-1.
	 */
	public SpectrumStruct getSpectrumFileLineno(String filename,int lineno)
	{
		SpectrumStruct speS = new SpectrumStruct(-1,0,0);
		for( FileSpectrum fs: fileSpecta )
		{
			String classFilename = fs.getClassFilename(); 
			if( classFilename.contentEquals(filename) )
			{//文件名匹配
				speS = fs.getSpectrum(lineno);
				if( speS.getLineNo()>0 )
					break;
				//如果在此FileSpectrum没有找到，不意味filename里面没有。
			}
		}
		return speS;
	}

	/*            获取文件名filename所有代码的程序谱
	 * 特别注意：
	 *    在MultiProfileFile中，FileSpectrum和文件名称并非一一对应，一个文件名可能找到多个FileSpectrum，
	 *    内部类、嵌套类等情况就是如此。
	 * */
	public List<SpectrumStruct> getFileSpectrum(String filename)
	{
		List<SpectrumStruct> rtnSpecta = new ArrayList<>();
		for( FileSpectrum fs: fileSpecta )
		{
			String classFilename = fs.getClassFilename(); 
			if( classFilename.contentEquals(filename) )
			{//文件名匹配
				List<SpectrumStruct>  nowss = fs.getLineCodes();
				rtnSpecta.addAll(nowss);
			}
		}
		return rtnSpecta;
	}

	@Override
	public void testMe() {
		int verth = getBugId();
		System.out.println(objectName+" ver th := "+verth);
		int pass = getPassed();
		System.out.println("Passed test case  := "+pass);
		int fail = getFailed();
		System.out.println("Failed test case  := "+fail);
		int total = getTotalExec();
		System.out.println("Total Exec statement: "+total);
		for( FileSpectrum fsp : fileSpecta )
		{
			System.out.println(fsp.getClassName());
			List<SpectrumStruct> ssl = fsp.getLineCodes();
			for( SpectrumStruct item : ssl )
			{
				System.out.print(item.getLineNo()+","+item.getAep()+","+item.getAef());
				System.out.println(" ");
			}
		}//end of for ...
	}//end of testMe

}
