/*
 * 
 */
package affiliated;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Administrator
 * �����.profile�ж���ļ������Ρ�ÿ���汾�Ĳ�������ͨ������ʧ�ܸ������������в��
 */
public class MultiFaultFile implements IFaultFile{ 
	private String projectName; 
	int verNo; //version  amount �汾����Ŀ
	List<Integer> tcNos;  //test case number;ÿ���汾�Ĳ�������������ͬ��
	List<Integer> tcPasseds;//ÿ���汾�ĳɹ���������������ͬ��
	List<Integer> tcFaileds;//ÿ���汾��δͨ����������������ͬ��
	//versionList���Ǵ洢�汾�ŵ���Ȼ�����У�����benchmark��bugid���С�
	List<Integer> versionList; //�汾�ŵ��б�ע�⣺������26���汾���汾�Ų��Ǵ�1��26�������м���ȱʧ��defects4j����ˡ�
	/*
	 * һ��Ҫע�⣺faultFileLines��һ��Ԫ���ǵ�һ���汾V1�Ĺ�������кż��������ļ��ļ��ϣ��ڶ�������ˣ�
	 * �������ơ�  ��Ȼ��(object)_fault.csv�ļ������ϢҲ���밴��˳���š�
	 */
	List<FaultOfVersion> faultFileLines;////���а汾�Ĺ�����估�������ļ��ļ��ϡ�
	String faultFilename; //��Ŀ¼��(object)_fault.csv
	String tcFilename; //��Ŀ¼��(object).testcase
	List<String> sourceDirectory;//Դ�������ڵ�Ŀ¼��ÿ���汾��Դ���벻ͬ����ʱû���õ���ͳһΪbuggy.
	/*
	 * objectName:�������֡�
	 */
	public MultiFaultFile(String objectName,String faultFilename,String tcFilename)
	{
		projectName = objectName;
		verNo = 0;
		this.faultFilename = faultFilename;
		faultFileLines = new ArrayList<FaultOfVersion>();
		versionList  = new ArrayList<Integer>();
		tcNos  = new ArrayList<Integer>();
		tcPasseds = new ArrayList<Integer>();
		tcFaileds = new ArrayList<Integer>();
		sourceDirectory = new ArrayList<String>();
		this.tcFilename = tcFilename;
	}
	
	/**
	 * @return true: read file ok.
	 */
	public boolean readFaultFile()
	{
		boolean result = true;
		try {
			File file = new File(faultFilename);
			if (file.isFile() && file.exists()) {
				InputStreamReader read = new InputStreamReader(new FileInputStream(file));
				BufferedReader br = new BufferedReader(read);
				//[0:version no,bugid]	[1:test cases]	[2:passed]  [3:failed]
				//[4:code line of fault]	[5:fault type]	[6:source code dir]
				String lineTXT = br.readLine(); //the prompt
				int vers = 0; //�汾����
				//����������ݡ�
				while((lineTXT = br.readLine())!= null){
					vers++;
					//System.out.println("vers=: "+vers); //for test
					String[] strAry = lineTXT.split(","); //csv�����ŷָ�
					String strVersion = strAry[0].substring(1);//��һ���ַ���v or V
					versionList.add(Integer.parseInt(strVersion));//��һ����version no(bugid).
					tcNos.add(Integer.parseInt(strAry[1]));//�ڶ����ǲ���������Ŀ
					tcPasseds.add(Integer.parseInt(strAry[2]));//passed test cases
					tcFaileds.add(Integer.parseInt(strAry[3]));//failed test cases
					if( strAry[4].length()<=8 ) //����8���ַ�������Ϊ���С�������X.java_X
					{
						FaultOfVersion fov = new FaultOfVersion();
						faultFileLines.add(fov);
						sourceDirectory.add(strAry[6]);//��7����Դ���������ļ��С�
						continue;  //�����ǿ��У�����δ���ü����룬Ҳ���ܸð汾�������̫�࣬�������ˡ�
					}
					//code line of fault ÿ���汾�����д����ȱ�����Ķ�������ļ����Էֺŷָ���һ��ֵ���޷ֺš�
					String[] strLine = strAry[4].split(";");//��5����ȱ������к�
					int totalLine = strLine.length;
					List<Integer> linenoLst = new ArrayList<Integer>();
					List<String> fileLst = new ArrayList<String>();
					for( int k=0;k<totalLine;k++ )
					{
						int _label= strLine[k].lastIndexOf("_"); //�»���ǰ���ļ������������кš�
						String fname = strLine[k].substring(0, _label);//0,_label - 1.length is endIndex-beginIndex. 
						//fileLst[k] = fname;
						//linenoLst[k] = Integer.parseInt(strLine[k].substring(_label+1));
						String lineStr = strLine[k].substring(_label+1);
						String[] lineAry = lineStr.split("#");
						int number = lineAry.length;
						for( int t=0;t<number;t++)
						{
							fileLst.add(fname);
							linenoLst.add(Integer.parseInt(lineAry[t]));
						}
					}
					FaultOfVersion fov = new FaultOfVersion();
					fov.assign(fileLst, linenoLst);
					faultFileLines.add(fov);
					
					sourceDirectory.add(strAry[6]);//��7����Դ���������ļ��С�
				}
				verNo = vers;
				br.close();
				read.close();
			}
			else
			{
				System.out.println("File: "+faultFilename+" is not found.");
				result = false;
			}
		}//end of try. 
		catch (Exception e) {
			e.printStackTrace();
			result = false;
		}
		return result;
	}
	
	//��ȡ�ڼ����汾(��Ȼ˳��)�İ汾��,index��1��verNo,ע��index���Ƕ�Ӧbugid.
	//���ܳ�������������������汾���汾�ŷֱ�Ϊ:2,5,7
	@Override
	public int getBugID(int index)
	{
		return versionList.get(index-1);
	}
	
	//��ȡ�汾�ܵ���Ŀ��
	@Override
	public int getVerNo()
	{
		return this.verNo;
	}
	
	//��ȡ�����汾�Ĳ���������Ŀ�б�
	public List<Integer> getTesecaseNos()
	{
		return this.tcNos;
	}
	
	//��ȡĳ���汾�Ĺ���������顣
	//��ȡ�ڼ����汾(��Ȼ˳��)�Ĺ����������,index��1��verNo,ע��index���Ƕ�Ӧbugid.
	@Override
	public int[] getFaultLinesVer(int index)
	{
		FaultOfVersion fov = faultFileLines.get(index-1);
		return fov.getFaultLines();
	}
	
	//��ȡĳ���汾�Ĺ�����������ļ��ļ��ϡ��ر�ע�⣺faultFiles��faultLines�Ķ�ά˳�򱣳�һ�¡�
	//index��1��verNo,ע��index���Ƕ�Ӧbugid.
	@Override
	public String[] getFaultFilesVer(int index)
	{
		FaultOfVersion fov = faultFileLines.get(index-1);
		return fov.getFaultFiles();
	}
	
	//filename��linenoָ��������ǹ��������
	//index��1��verNo,ע��index���Ƕ�Ӧbugid.
	@Override
	public boolean isFaultStatement(int index,String filename,int lineno)
	{
		FaultOfVersion fov = faultFileLines.get(index-1);
		return fov.isFaultStatement(filename, lineno);
	}
	
	/**
	 * ���Դ���
	 */
	@Override
	public void testMe()
	{
		int verAmount = getVerNo();
		System.out.println("version numbers := "+verAmount);
		for( int k=0;k<verAmount;k++ )
		{
			System.out.println("V"+versionList.get(k)+"(bugid):  ");
			System.out.print("      tcs="+tcNos.get(k)+", ");
			System.out.print("passed= "+tcPasseds.get(k)+", "+"failed= "+tcFaileds.get(k)+", ");
			System.out.print("fault= ");
			FaultOfVersion fov = faultFileLines.get(k);
			String[] faultFiles = fov.getFaultFiles();
			int[] faultLines = fov.getFaultLines();
			int number = fov.getNumberOfFault();
			for( int p=0;p<number;p++)
				System.out.print(faultFiles[p]+"_"+faultLines[p]+",");
			System.out.print("   dir= "+sourceDirectory.get(k));
			System.out.println(".");
		}
	} //end of testMe.
	
	/**
	 * @return true: read .testcase file ok.
	 */
	private boolean readTestcaseFile(List<int[]> tcPFs)
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
				int vs = Integer.valueOf(strAry[1]); //�汾��
				
				lineTXT = br.readLine();  //test case total����ֵû�����ã��Ѿ�������
				lineTXT = lineTXT.trim(); //ȥ����β�ո�
				strAry = lineTXT.split("\\s+");
				//int  ts = Integer.valueOf(strAry[1]); //����������Ŀ
				
				br.readLine(); //testcase line info prompt
				for( int i=0;i<vs;i++ )
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
					tcPFs.add(pfNums);
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
	
	/** ��XX_fault.cvs��XX.testcase�������ļ���ͨ������������Ŀ��ʧ�ܲ���������Ŀ�Ƿ���ͬ��
	 * tcPFs,ÿ�����鶼ֻ�ж���Ԫ�أ�ͨ������������Ŀ��ʧ�ܲ���������Ŀ
	 */
	@Override
	public boolean checkTestcasePassedFailed()
	{
		List<int[]> tcPFs = new ArrayList<int[]>();
		//�ȶ�.testcase�ļ���
		if( !readTestcaseFile(tcPFs) )
			return false;
		//�ٱȽ�XX_fault.cvs��XX.testcase�Ľ��
		boolean result = true;
		int verAmount = getVerNo();
		for( int k=0;k<verAmount;k++ )
		{
			int tcamount = tcNos.get(k);
			int passed = tcPasseds.get(k);
			int failed = tcFaileds.get(k);
			if( tcamount!=(passed+failed) )
			{
				System.out.println("tcamount!=(passed+failed) of the order="+(k+1)+", bugid="
										+getBugID(k+1));  //k����bugid
				result = false;
			}
			int[] tcAry = tcPFs.get(k);
			if( tcAry[0]!=passed || tcAry[1]!=failed )
			{
				System.out.println("tcAry[0]!=passed ("+tcAry[0]+","+passed+") || tcAry[1]!=failed of the order="+(k+1)+", bugid="
						+getBugID(k+1)+"("+tcAry[1]+","+failed+")");//k����bugid
				result = false;
			}
		}
		return result;
	}
	
	//����ֹ������_fault.csv�ļ���.profile�ļ����Աȹ����ļ��Ƿ���ڣ���������Ƿ����
	//�Ա������ļ���ͨ������������Ŀ��ʧ�ܲ���������Ŀ�Ƿ���ͬ��
	@Override
	public boolean checkProfileAndFaultFile()
	{
		boolean result = true;
		int verAmount = getVerNo();
		for( int ver=1;ver<=verAmount; ver++)
		{
			int bugid = getBugID(ver);  //bugid��������Ȼ˳��
			int pos = faultFilename.lastIndexOf('\\');
			String path = faultFilename.substring(0,pos);
			String profileFilename = path +"\\profile\\"+	projectName+"_v"+String.valueOf(bugid)+".profile";

			MultiProfileFile pf = new MultiProfileFile(projectName,bugid,profileFilename);//ֻ������汾���ԡ�
			if( false==pf.readProfileFile() )
			{
				result = false;
				System.out.println("Read file "+profileFilename+"  is error.");
				continue; //ԭ����break�������ܶ��.profile�ļ��������Ը�Ϊcontinue.
			}
			//�Ա������ļ���ͨ������������Ŀ��ʧ�ܲ���������Ŀ�Ƿ���ͬ��
			int passed = tcPasseds.get(ver-1);
			int failed = tcFaileds.get(ver-1);
			if( pf.getPassed()!=passed || pf.getFailed()!=failed )
			{
				System.out.println("pf.getPassed()!=passed ("+pf.getPassed()+","+passed+") || pf.getFailed()!=failed of the order="
							+ver+", bugid="	+bugid);//����bugid
				result = false;
			}
			//�Աȹ����ļ��Ƿ���ڣ���������Ƿ����
			int[] faultStats = getFaultLinesVer(ver); //�ð汾�Ĺ�����������
			String[] fileNames = getFaultFilesVer(ver);//�ð汾�Ĺ����ļ������顣
			int faultSize = faultStats.length;
			for( int k=0;k<faultSize;k++ )
			{
				if( !pf.isExistFault(fileNames[k], faultStats[k]) )
				{
					System.out.println("Fault "+fileNames[k]+","+faultStats[k]+" of the order= "+ver+
							"  or V"+bugid+" of bugid  is not found.");//ver����bugid
					result = false;
				}
			}
		}
		return result;
	}

}
