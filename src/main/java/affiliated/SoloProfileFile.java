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
 *
 */
public class SoloProfileFile extends AbstractProfileFile{
	/*单文件里，thVer这个必须依照自然顺序,1,2,3,4,....
	 *   多文件版本里，thVer会相当于bugid,并非自然顺序，可能如1,4,6,7,....式样。
	 */
	private int thVer;  //第几个版本，注意不是版本个数verNo。
	private List<SpectrumStruct> spectrumList;//程序谱
	String sourceCodeFile;//源代码文件，不带目录，匹配FileSpectrum里的classFilename。
	
	//某一个版本的程序谱。
	public SoloProfileFile(String object,int verTh,String profileFilename,String codeFilename)
	{
		super(object,profileFilename);
		thVer = verTh;
		spectrumList = new ArrayList<>();
		sourceCodeFile = codeFilename;
	}
	
	//注意verTh=1,...verNo,不是版本个数verNo。
	public SoloProfileFile(String object,int verTh,int passed,int failed,int total,
			      List<SpectrumStruct> ssList)
	{
		super(passed,failed,total,object);
		thVer = verTh;
		spectrumList = ssList;
		sourceCodeFile = "";
	}
	
	//第几个版本，注意不是版本个数verNo。
	public int getVerTh()
	{
		return thVer;
	}
	
	
	//单文件的程序谱
	public List<SpectrumStruct> getSpectrumStructList()
	{
		return spectrumList;
	}
	
	//多文件的程序谱
	public List<FileSpectrum> getSpectrumList()
	{
		List<FileSpectrum> filSpectrumList = new ArrayList<FileSpectrum>();
		FileSpectrum fspra = new FileSpectrum();
		fspra.setClassFilename(sourceCodeFile);
		int pos = sourceCodeFile.lastIndexOf('.');
		String clazz = sourceCodeFile.substring(0,pos);
		fspra.setClassName(clazz);
		fspra.setLineCodes(spectrumList);
		filSpectrumList.add(fspra);
		return filSpectrumList;
	}
	
	//从.profile文件读入程序谱
	@Override
	public boolean readProfileFile()
	{
		boolean result = true;
		
		spectrumList = new ArrayList<SpectrumStruct> ();
		FileInputStream fis = null;
	    DataInputStream dis = null;
	    File file;
	    try {
	        file = new File(profileFilename);
	        if( file.isFile()&& file.exists() )
	        {
	        	fis = new FileInputStream(file);
	        	dis = new DataInputStream(fis);
	        	thVer = dis.readInt();
	        	tcPassed = dis.readInt();
	        	tcFailed = dis.readInt();
	        	execTotal = dis.readInt();
		        //逐条读入语句的谱信息。
	        	for(int k=0;k<execTotal;k++ )
		        {
	        		int lineno = dis.readInt();
	        		int aep = dis.readInt();
	        		int aef = dis.readInt();
	        		SpectrumStruct item  = new SpectrumStruct(lineno,aep,aef);
	        		spectrumList.add(item);
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
	        dos.writeInt(thVer);
	        dos.writeInt(tcPassed);
	        dos.writeInt(tcFailed);
	        dos.writeInt(execTotal);
	        //逐条语句的谱信息写入。
	        for( SpectrumStruct item : spectrumList )
	        {
        		dos.writeInt(item.getLineNo());
        		dos.writeInt(item.getAep());
        		dos.writeInt(item.getAef());
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
	
	/** 某故障语句在文件中是否存在。
	 * @param faultLine 故障语句
	 */
	public boolean isExistFault(int faultLine)
	{
		boolean isExist = false;
		for( SpectrumStruct ss : spectrumList )
		{
			if( ss.getLineNo()==faultLine )
			{ //行号也有。
				isExist = true;
				break;
			}
		}
		return isExist; 
	}
	
	/** 查找对应filename和lineno的程序谱
	 * @param filename
	 * @param lineno
	 * @return 返回程序谱，没有找到的话，返回结果的行号-1.
	 */
	public SpectrumStruct getSpectrumFileLineno(String filename,int lineno)
	{
		SpectrumStruct speS = new SpectrumStruct(-1,0,0);
		if( sourceCodeFile.contentEquals(filename) )
		{//文件名匹配,
			//对SoloProfile来说，一个文件里只有一个FileSpectrum；与MultiProfile不同
			for( SpectrumStruct ss : spectrumList )
			{
				if( ss.getLineNo()==lineno )
				{
					speS = ss;
					break;
				}
			}//end of for...
		}
		return speS;
	}
	
	//获取文件名filename所有代码的程序谱
	public List<SpectrumStruct> getFileSpectrum(String filename)
	{
		if( sourceCodeFile.contentEquals(filename) )
			return spectrumList;
		else
			return null;
	}
	
	/**
	 * 测试代码
	 */
	@Override
	public void testMe()
	{
		int verth = getVerTh();
		System.out.println(objectName+" ver th := "+verth);
		int pass = getPassed();
		System.out.println("Passed test case  := "+pass);
		int fail = getFailed();
		System.out.println("Failed test case  := "+fail);
		int total = getTotalExec();
		System.out.println("Total Exec statement: "+total);
		List<SpectrumStruct> ssl = getSpectrumStructList();
		for( SpectrumStruct item : ssl )
		{
			System.out.print(item.getLineNo()+","+item.getAep()+","+item.getAef());
			System.out.println(" ");
		}
	} //end of testMe.

}
