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
	/*���ļ��thVer�������������Ȼ˳��,1,2,3,4,....
	 *   ���ļ��汾�thVer���൱��bugid,������Ȼ˳�򣬿�����1,4,6,7,....ʽ����
	 */
	private int thVer;  //�ڼ����汾��ע�ⲻ�ǰ汾����verNo��
	private List<SpectrumStruct> spectrumList;//������
	String sourceCodeFile;//Դ�����ļ�������Ŀ¼��ƥ��FileSpectrum���classFilename��
	
	//ĳһ���汾�ĳ����ס�
	public SoloProfileFile(String object,int verTh,String profileFilename,String codeFilename)
	{
		super(object,profileFilename);
		thVer = verTh;
		spectrumList = new ArrayList<>();
		sourceCodeFile = codeFilename;
	}
	
	//ע��verTh=1,...verNo,���ǰ汾����verNo��
	public SoloProfileFile(String object,int verTh,int passed,int failed,int total,
			      List<SpectrumStruct> ssList)
	{
		super(passed,failed,total,object);
		thVer = verTh;
		spectrumList = ssList;
		sourceCodeFile = "";
	}
	
	//�ڼ����汾��ע�ⲻ�ǰ汾����verNo��
	public int getVerTh()
	{
		return thVer;
	}
	
	
	//���ļ��ĳ�����
	public List<SpectrumStruct> getSpectrumStructList()
	{
		return spectrumList;
	}
	
	//���ļ��ĳ�����
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
	
	//��.profile�ļ����������
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
		        //����������������Ϣ��
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
	
	
	/**  ��������д��.profile�ļ�
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
	        //������������Ϣд�롣
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
	
	/** ĳ����������ļ����Ƿ���ڡ�
	 * @param faultLine �������
	 */
	public boolean isExistFault(int faultLine)
	{
		boolean isExist = false;
		for( SpectrumStruct ss : spectrumList )
		{
			if( ss.getLineNo()==faultLine )
			{ //�к�Ҳ�С�
				isExist = true;
				break;
			}
		}
		return isExist; 
	}
	
	/** ���Ҷ�Ӧfilename��lineno�ĳ�����
	 * @param filename
	 * @param lineno
	 * @return ���س����ף�û���ҵ��Ļ������ؽ�����к�-1.
	 */
	public SpectrumStruct getSpectrumFileLineno(String filename,int lineno)
	{
		SpectrumStruct speS = new SpectrumStruct(-1,0,0);
		if( sourceCodeFile.contentEquals(filename) )
		{//�ļ���ƥ��,
			//��SoloProfile��˵��һ���ļ���ֻ��һ��FileSpectrum����MultiProfile��ͬ
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
	
	//��ȡ�ļ���filename���д���ĳ�����
	public List<SpectrumStruct> getFileSpectrum(String filename)
	{
		if( sourceCodeFile.contentEquals(filename) )
			return spectrumList;
		else
			return null;
	}
	
	/**
	 * ���Դ���
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
