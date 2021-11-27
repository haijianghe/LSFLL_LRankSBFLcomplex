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
 *ֻ���浥��.profile���ݣ��ж��Դ����ĳ����ף������Ǳ������а汾�ġ�
 */
public class MultiProfileFile extends AbstractProfileFile {
	//private int thVer;  //�ڼ����汾��ע�ⲻ�ǰ汾����verNo����1��ʼ����Ȼ�����С�
	private int bugID;  //defects4j��bugid�ĸ��.profile��洢����bugid������thVer;
	/* �ر�ע�⣺
	 *    ��MultiProfileFile�У�FileSpectrum���ļ����Ʋ���һһ��Ӧ��һ���ļ��������ҵ����FileSpectrum��
	 *    �ڲ��ࡢǶ��������������ˡ�
	 * */
	private List<FileSpectrum> fileSpecta; //������
	
	//���캯�������ݴ��ļ����롣for : readProfileFile
	public MultiProfileFile(String object,int bugid,String profileFilename)
	{
		super(object,profileFilename);
		bugID = bugid;
		fileSpecta =  new ArrayList<FileSpectrum> ();
	}
	
	//ĳһ��bugid�汾�ĳ����ס�
	public MultiProfileFile(String object,int bugid,int passed,int failed)
	{
		super(passed,failed,0,object);
		bugID = bugid;
		fileSpecta = new ArrayList<FileSpectrum> ();
	}
	
	//ע��bugid�ǰ汾�š�
	//���Ǵ�1��ʼ����Ȼ�����С�
	public MultiProfileFile(String object,int bugid,int passed,int failed,int total,
			      List<FileSpectrum> ssList)
	{
		super(passed,failed,total,object);
		bugID = bugid;
		fileSpecta = ssList;
	}
	
	//defects4j��bugid�ĸ��.profile��洢����bugid��ע����thVer�Ĳ��
	public int getBugId()
	{
		return bugID;
	}
	
	//��ȡ�ܵĿ�ִ�������Ŀ
	@Override
	public int getTotalExec()
	{
		calTotalExec();
		return execTotal;
	}

	//�����ܵĿ�ִ�������Ŀ
	private void calTotalExec()
	{
		int total = 0;
		for( FileSpectrum fsp : fileSpecta )
			total += fsp.getTotalExec();
		execTotal = total;
	}

	//���ļ��ĳ�����
	@Override
	public List<FileSpectrum> getSpectrumList()
	{
		return fileSpecta;
	}
	
	/** ĳ����������ļ����Ƿ���ڡ�
	 * @param faultFile �����ļ���
	 * @param faultLine �������
	 */
	public boolean isExistFault(String faultFile,int faultLine)
	{
		boolean isExist = false;
		for( FileSpectrum fspect : fileSpecta)
		{
			if( !fspect.getClassFilename().equals(faultFile) )
				continue;
			//�ļ���ƥ��
			List<SpectrumStruct> ssList = fspect.getLineCodes();
			for( SpectrumStruct ss : ssList )
				if( ss.getLineNo()==faultLine )
				{ //�ļ�����ͬ���к�Ҳ�С�
					isExist = true;
					break;
				}
			if( isExist )
				break; //ע�⣺FileSpectrum���ļ����Ʋ���һһ��Ӧ��һ���ļ��������ҵ����FileSpectrum��
		}
		return isExist; 
	}
	
	//��.profile�ļ����������
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
	 	        //�����ַ���
	 	        int len = dis.readInt();
	            byte []buf = new byte[len];
	            dis.read(buf);
	            objectName = new String(buf);
	            
	        	//thVer = dis.readInt(); //Ubuntu+VisualBox�¶�ȡdefects4j���ݵ���Ŀ���õ���thVer
	            int bugid = dis.readInt();  //��bugid
	            if( bugid!= bugID )
	            	throw new Exception("The bugid is error when read .profile file.");
	        	tcPassed = dis.readInt();
	        	tcFailed = dis.readInt();
	        	execTotal = dis.readInt();
	        	int numberOfFiles = dis.readInt();//�ܵ��ļ�������
		        //��������ļ�������Ϣ��
	        	for(int k=0;k<numberOfFiles;k++ )
		        {
	        		FileSpectrum item = new FileSpectrum();
	        		item.readFile(dis);
	        		//һ��Ҫ������������򷽷���
	        		item.sortLineNo();//######ע�⣬defects4j������õģ�������ô�����������ݼ��أ�
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
	        //д���ַ���
			dos.writeInt(objectName.length());
			dos.writeBytes(objectName);
	        //dos.writeInt(thVer);//Ubuntu+VisualBox�¶�ȡdefects4j���ݵ���Ŀ���õ���thVer
            dos.writeInt(bugID);  //дbugid
	        dos.writeInt(tcPassed);
	        dos.writeInt(tcFailed);
	        calTotalExec();
	        dos.writeInt(execTotal);
	        dos.writeInt(fileSpecta.size());//�ܵ��ļ�������
	        //������������Ϣд�롣
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
	
	/** ���Ҷ�Ӧfilename��lineno�ĳ�����
	 *  ע�⣺FileSpectrum���ļ����Ʋ���һһ��Ӧ��һ���ļ��������ҵ����FileSpectrum��
	 * @param filename
	 * @param lineno
	 * @return ���س����ף�û���ҵ��Ļ������ؽ�����к�-1.
	 */
	public SpectrumStruct getSpectrumFileLineno(String filename,int lineno)
	{
		SpectrumStruct speS = new SpectrumStruct(-1,0,0);
		for( FileSpectrum fs: fileSpecta )
		{
			String classFilename = fs.getClassFilename(); 
			if( classFilename.contentEquals(filename) )
			{//�ļ���ƥ��
				speS = fs.getSpectrum(lineno);
				if( speS.getLineNo()>0 )
					break;
				//����ڴ�FileSpectrumû���ҵ�������ζfilename����û�С�
			}
		}
		return speS;
	}

	/*            ��ȡ�ļ���filename���д���ĳ�����
	 * �ر�ע�⣺
	 *    ��MultiProfileFile�У�FileSpectrum���ļ����Ʋ���һһ��Ӧ��һ���ļ��������ҵ����FileSpectrum��
	 *    �ڲ��ࡢǶ��������������ˡ�
	 * */
	public List<SpectrumStruct> getFileSpectrum(String filename)
	{
		List<SpectrumStruct> rtnSpecta = new ArrayList<>();
		for( FileSpectrum fs: fileSpecta )
		{
			String classFilename = fs.getClassFilename(); 
			if( classFilename.contentEquals(filename) )
			{//�ļ���ƥ��
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
