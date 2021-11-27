/**
 * 
 */
package affiliated;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import common.XMLConfigFile;

/**
 * @author Administrator
 *  �����.profileֻ�е����ļ������Ρ����а汾�Ĳ�������ͨ������ʧ�ܸ�������������ͬ��
 */
public class SoloFaultFile implements IFaultFile{
	String projectName;   //��objectName

	int verNo; //�汾������version  numbers, bugNo 
	int tcNo;  //��������������test case number;
	/*
	 * һ��Ҫע�⣺testCases��һ��Ԫ���ǵ�һ���汾��passed,failed,�������� ��
	 * ��Ȼ��(object).testcase�ļ������ϢҲ���밴��˳���š�
	 */
	List<int[]> testCases = new ArrayList<int[]>();  //���а汾��passed,failed��
	/*
	 * һ��Ҫע�⣺faultLines��һ��Ԫ���ǵ�һ���汾V1�Ĺ�������кż��ϣ��ڶ�������ˣ��������� ��
	 * ��Ȼ��(object).fault�ļ������ϢҲ���밴��˳���š�
	 */
	List<int[]> faultLines = new ArrayList<int[]>();  //���а汾�Ĺ�������кż��ϡ�
	String faultFilename; //��Ŀ¼��(object).fault
	String tcFilename; //��Ŀ¼��(object).testcase
	String sourceCodeFile;//Դ�����ļ�������Ŀ¼��ƥ��MultiFaultFile���List<String[]> faultFiles��
	/*
	 * objectName:�������֡�
	 */
	public SoloFaultFile(String objectName,String faultFilename,String tcFilename,String codeFile)
	{
		projectName = objectName;
		this.faultFilename = faultFilename;
		this.tcFilename = tcFilename;
		sourceCodeFile = codeFile;
	}

	@Override
	public boolean readFaultFile()
	{
		if( !readDotFaultFile() )
			return false;
		if( !readTestcaseFile() )
			return false;
		return true;
	}
	/* spilt(" ");�Ĵ�������
	 * 1���ַ����ײ���ÿ���ո�ת����һ���մ���
	 * 2���м�Ķ���ո��У������ÿ���ո�ת����һ���մ���
	 * 3��ĩβ�Ķ���ո�ȫ��ȥ���ˡ�
	 * 
	 * spilt("\\s+");�Ĵ�������
	 * 1���ַ����ײ������пո�ת����һ���մ���
	 * 2���м�Ķ���ո�ȫ��ȥ���ˡ�
	 * 3��ĩβ�Ķ���ո�ȫ��ȥ���ˡ�
	 */
	/**
	 * @return true: read file ok.
	 */
	public boolean readDotFaultFile()
	{
		boolean result = true;
		try {
			File file = new File(faultFilename);
			if (file.isFile() && file.exists()) {
				InputStreamReader read = new InputStreamReader(new FileInputStream(file));
				BufferedReader br = new BufferedReader(read);
				String lineTXT = null;
				//while ((lineTXT = br.readLine()) != null) {}
				br.readLine(); //the prompt
			
				lineTXT = br.readLine(); //Version total
				lineTXT = lineTXT.trim(); //ȥ����β�ո�
				String[]  strAry = lineTXT.split("\\s+"); //�������ո�ָ��ַ���
				verNo = Integer.valueOf(strAry[0]); //�汾��
				
				lineTXT = br.readLine();  //test case total
				lineTXT = lineTXT.trim(); //ȥ����β�ո�
				strAry = lineTXT.split("\\s+");
				tcNo = Integer.valueOf(strAry[0]); //����������Ŀ
				
				br.readLine(); //fault line info prompt
				for( int i=0;i<verNo;i++ )
				{
					lineTXT = br.readLine();  //fault line info
					lineTXT = lineTXT.trim(); //ȥ����β�ո�
					strAry = lineTXT.split("\\s+");
					String[]  faultAry =  strAry[1].split(",");
					int[] faultLine = new int[faultAry.length];
					for( int k=0;k<faultAry.length;k++ )
					{
						int lineno = Integer.valueOf(faultAry[k]);
						faultLine[k] = lineno;
					}
					faultLines.add(faultLine);
				}
				read.close();
			}
			else
				result = false;
		}//end of try. 
		catch (Exception e) {
			e.printStackTrace();
			result = false;
		}
		return result;
	}
	
	/**
	 * @return true: read .testcase file ok.
	 */
	public boolean readTestcaseFile()
	{
		boolean result = true;
		try {
			File file = new File(tcFilename);
			if (file.isFile() && file.exists()) {
				InputStreamReader read = new InputStreamReader(new FileInputStream(file));
				BufferedReader br = new BufferedReader(read);
				String lineTXT = null;

				lineTXT = br.readLine(); //Version total
				lineTXT = lineTXT.trim(); //ȥ����β�ո�
				String[]  strAry = lineTXT.split("\\s+"); //�������ո�ָ��ַ���
				verNo = Integer.valueOf(strAry[1]); //�汾��
				
				lineTXT = br.readLine();  //test case total
				lineTXT = lineTXT.trim(); //ȥ����β�ո�
				strAry = lineTXT.split("\\s+");
				tcNo = Integer.valueOf(strAry[1]); //����������Ŀ
				
				br.readLine(); //testcase line info prompt
				for( int i=0;i<verNo;i++ )
				{
					lineTXT = br.readLine();  //testcase line info
					lineTXT = lineTXT.trim(); //ȥ����β�ո�
					strAry = lineTXT.split("\\s+");
					int[] pfNums = new int[2];
					for( int k=0;k<2;k++ )
					{
						int tcer = Integer.valueOf(strAry[k+1]);//0 is verTH
						pfNums[k] = tcer;
					}
					testCases.add(pfNums);
				}
				read.close();
			}
			else
				result = false;
		}//end of try. 
		catch (Exception e) {
			e.printStackTrace();
			result = false;
		}
		return result;
	}

	//�汾����
	@Override
	public int getVerNo()
	{
		return this.verNo;
	}
	
	//��ȡ�ڼ����汾(��Ȼ˳��)�İ汾��,index��1��verNo,ע��index���Ƕ�Ӧbugid.
	//���ܳ�������������������汾���汾�ŷֱ�Ϊ:2,5,7
	@Override
	public int getBugID(int index)
	{
		return index;
	}
	
	//�����������������а汾�Ĳ���������������ͬ��
	public int getTesecaseNo()
	{
		return this.tcNo;
	}
	
	 //���а汾�Ĺ�������кż��ϡ�
	public List<int[]> getFaultLines()
	{
		return this.faultLines;
	}
	

	//��ȡĳ���汾�Ĺ���������顣
	//��ȡ�ڼ����汾(��Ȼ˳��)�Ĺ����������,index��1��verNo,ע��index���Ƕ�Ӧbugid.
	@Override
	public int[] getFaultLinesVer(int index)
	{
		return faultLines.get(index-1);
	}
	
	//��ȡĳ���汾�Ĺ�����������ļ��ļ��ϡ��ر�ע�⣺faultFiles��faultLines�Ķ�ά˳�򱣳�һ�¡�
	//index��1��verNo,ע��index���Ƕ�Ӧbugid.
	@Override
	public String[] getFaultFilesVer(int index)
	{
		int numberOfFault = faultLines.get(index-1).length;
		String[] codeFiles = new String[numberOfFault];
		for( int i=0;i<numberOfFault;i++)
			codeFiles[i] = sourceCodeFile;
		return codeFiles;
	}
	
	//filename��linenoָ��������ǹ��������
	//index��1��verNo,ע��index���Ƕ�Ӧbugid.
	@Override
	public boolean isFaultStatement(int index,String filename,int lineno)
	{
		if( !sourceCodeFile.contentEquals(filename) )
			return false;
		int[] bugstms = getFaultLinesVer(index);
		for( int bst : bugstms )
		{
			if( lineno ==bst )
				return true;
		}
		return false;
	}
	
	/**
	 * ���Դ���
	 */
	@Override
	public void testMe()
	{
		int verno = getVerNo();
		System.out.println("version amount := "+verno);
		int tcno = getTesecaseNo();
		System.out.println("test case amount := "+tcno);
		//���������ʾ��
		System.out.println("Fault lines : ");
		List<int[]> fls = getFaultLines();
		for( int[] items : fls )
		{
			int num = items.length;
			for( int t=0;t<num;t++ )
				System.out.print(items[t]+"  ");
			System.out.println(" ");
		}
		//����������ʾ
		System.out.println("testcases(verTh,passed,failed) : ");
		for( int i=0;i<verNo;i++ )
		{
			System.out.println( "v"+String.valueOf(i+1)+
					"     "+String.valueOf(testCases.get(i)[0])+
					"     "+String.valueOf(testCases.get(i)[1]) );
		}

	} //end of testMe.

	/** ��XX_.fault��XX.testcase�������ļ���ͨ������������Ŀ��ʧ�ܲ���������Ŀ֮���Ƿ���������ͬ��
	 */
	@Override
	public boolean checkTestcasePassedFailed() {
		//�Ƚ�XX_fault.cvs��XX.testcase�Ľ��
		boolean result = true;
		int verAmount = getVerNo();
		for( int k=0;k<verAmount;k++ )
		{
			int[] tcs = testCases.get(k);//��һ��ֵΪͨ���Ĳ���������Ŀ���ڶ���Ϊfail��Ŀ��
			if( tcNo!=(tcs[0]+tcs[1]) )
			{
				System.out.println("tcamount!=(passed+failed) of the order="+(k+1)+", bugid="
										+k+1);  //���ļ�.profile����bugid����ȱʧ��
				result = false;
			}
		}
		return result;
	}

	//����ֹ������.fault�ļ���.profile�ļ����Աȹ����ļ��Ƿ���ڣ���������Ƿ����
	//�Ա������ļ���ͨ������������Ŀ��ʧ�ܲ���������Ŀ�Ƿ���ͬ��
	@Override
	public boolean checkProfileAndFaultFile() {
		boolean result = true;
		int verAmount = getVerNo();
		String sourceCode = XMLConfigFile.getSourceCodeFileOfObject(projectName);
		for( int ver=1;ver<=verAmount; ver++)
		{
			int pos = faultFilename.lastIndexOf('\\');
			String path = faultFilename.substring(0,pos);
			String profileFilename = path +"\\profile\\"+	projectName+"_v"+String.valueOf(ver)+".profile";

			SoloProfileFile pf = new SoloProfileFile(projectName,ver,profileFilename,sourceCode);//ֻ������汾���ԡ�
			if( false==pf.readProfileFile() )
			{
				result = false;
				System.out.println("Read file "+projectName+"_"+String.valueOf(ver)+".profile is error.");
				continue; //ԭ����break�������ܶ��.profile�ļ��������Ը�Ϊcontinue.
			}
			//�Ա������ļ���ͨ������������Ŀ��ʧ�ܲ���������Ŀ�Ƿ���ͬ��
			int[] tcs = testCases.get(ver-1);//��һ��ֵΪͨ���Ĳ���������Ŀ���ڶ���Ϊfail��Ŀ��

			int passed = tcs[0];
			int failed = tcs[1];
			if( pf.getPassed()!=passed || pf.getFailed()!=failed )
			{
				System.out.println("pf.getPassed()!=passed ("+pf.getPassed()+","+passed+") || pf.getFailed()!=failed of the order="
							+ver+", bugid="	+ver);//����bugid
				result = false;
			}
			//�Աȹ����ļ��Ƿ���ڣ���������Ƿ����
			int[] faultStats = faultLines.get(ver-1); //�ð汾�Ĺ�����������
			int faultSize = faultStats.length;
			for( int k=0;k<faultSize;k++ )
			{
				if( !pf.isExistFault(faultStats[k]) )
				{
					System.out.println("Fault Line "+faultStats[k]+" of the order= "+ver+
							"  or V"+ver+" of bugid  is not found.");//���ļ�.profile�汾ver����bugid
					result = false;
				}
			}
		}
		return result;

	}
}//end of class

