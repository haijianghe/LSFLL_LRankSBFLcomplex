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
 * 针对其.profile有多个文件的情形。每个版本的测试用例通过数，失败个数，总数都有差别。
 */
public class MultiFaultFile implements IFaultFile{ 
	private String projectName; 
	int verNo; //version  amount 版本总数目
	List<Integer> tcNos;  //test case number;每个版本的测试用例个数不同。
	List<Integer> tcPasseds;//每个版本的成功测试用例个数不同。
	List<Integer> tcFaileds;//每个版本的未通过测试用例个数不同。
	//versionList并非存储版本号的自然数序列，而是benchmark的bugid序列。
	List<Integer> versionList; //版本号的列表，注意：假如有26个版本，版本号并非从1到26，可能中间有缺失。defects4j就如此。
	/*
	 * 一定要注意：faultFileLines第一个元素是第一个版本V1的故障语句行号及其所在文件的集合，第二个亦如此，
	 * 依次类推。  当然，(object)_fault.csv文件里的信息也必须按照顺序存放。
	 */
	List<FaultOfVersion> faultFileLines;////所有版本的故障语句及其所在文件的集合。
	String faultFilename; //带目录的(object)_fault.csv
	String tcFilename; //带目录的(object).testcase
	List<String> sourceDirectory;//源代码所在的目录。每个版本的源代码不同。暂时没有用到，统一为buggy.
	/*
	 * objectName:对象名字。
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
				int vers = 0; //版本数。
				//读后面的数据。
				while((lineTXT = br.readLine())!= null){
					vers++;
					//System.out.println("vers=: "+vers); //for test
					String[] strAry = lineTXT.split(","); //csv，逗号分隔
					String strVersion = strAry[0].substring(1);//第一个字符是v or V
					versionList.add(Integer.parseInt(strVersion));//第一列是version no(bugid).
					tcNos.add(Integer.parseInt(strAry[1]));//第二列是测试用例数目
					tcPasseds.add(Integer.parseInt(strAry[2]));//passed test cases
					tcFaileds.add(Integer.parseInt(strAry[3]));//failed test cases
					if( strAry[4].length()<=8 ) //少于8个字符，定义为空行。至少是X.java_X
					{
						FaultOfVersion fov = new FaultOfVersion();
						faultFileLines.add(fov);
						sourceDirectory.add(strAry[6]);//第7列是源代码所在文件夹。
						continue;  //该行是空行，可能未来得及输入，也可能该版本故障语句太多，舍弃它了。
					}
					//code line of fault 每个版本可能有带多个缺陷语句的多个故障文件。以分号分割，最后一个值后无分号。
					String[] strLine = strAry[4].split(";");//第5列是缺陷语句行号
					int totalLine = strLine.length;
					List<Integer> linenoLst = new ArrayList<Integer>();
					List<String> fileLst = new ArrayList<String>();
					for( int k=0;k<totalLine;k++ )
					{
						int _label= strLine[k].lastIndexOf("_"); //下划线前是文件名，后面是行号。
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
					
					sourceDirectory.add(strAry[6]);//第7列是源代码所在文件夹。
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
	
	//获取第几个版本(自然顺序)的版本号,index从1到verNo,注意index并非对应bugid.
	//可能出现这种情况，共三个版本，版本号分别为:2,5,7
	@Override
	public int getBugID(int index)
	{
		return versionList.get(index-1);
	}
	
	//获取版本总的数目。
	@Override
	public int getVerNo()
	{
		return this.verNo;
	}
	
	//获取各个版本的测试用例数目列表
	public List<Integer> getTesecaseNos()
	{
		return this.tcNos;
	}
	
	//获取某个版本的故障语句数组。
	//获取第几个版本(自然顺序)的故障语句数组,index从1到verNo,注意index并非对应bugid.
	@Override
	public int[] getFaultLinesVer(int index)
	{
		FaultOfVersion fov = faultFileLines.get(index-1);
		return fov.getFaultLines();
	}
	
	//获取某个版本的故障语句所在文件的集合。特别注意：faultFiles和faultLines的二维顺序保持一致。
	//index从1到verNo,注意index并非对应bugid.
	@Override
	public String[] getFaultFilesVer(int index)
	{
		FaultOfVersion fov = faultFileLines.get(index-1);
		return fov.getFaultFiles();
	}
	
	//filename和lineno指定的语句是故障语句吗？
	//index从1到verNo,注意index并非对应bugid.
	@Override
	public boolean isFaultStatement(int index,String filename,int lineno)
	{
		FaultOfVersion fov = faultFileLines.get(index-1);
		return fov.isFaultStatement(filename, lineno);
	}
	
	/**
	 * 测试代码
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
				lineTXT = lineTXT.trim(); //去掉首尾空格
				String[]  strAry = lineTXT.split("\\s+"); //允许多个空格分割字符串
				int vs = Integer.valueOf(strAry[1]); //版本数
				
				lineTXT = br.readLine();  //test case total，该值没有作用，已经废弃。
				lineTXT = lineTXT.trim(); //去掉首尾空格
				strAry = lineTXT.split("\\s+");
				//int  ts = Integer.valueOf(strAry[1]); //测试用例数目
				
				br.readLine(); //testcase line info prompt
				for( int i=0;i<vs;i++ )
				{
					lineTXT = br.readLine();  //testcase line info
					lineTXT = lineTXT.trim(); //去掉首尾空格
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
	
	/** 在XX_fault.cvs和XX.testcase，两个文件的通过测试用例数目和失败测试用例数目是否相同。
	 * tcPFs,每个数组都只有二个元素：通过测试用例数目和失败测试用例数目
	 */
	@Override
	public boolean checkTestcasePassedFailed()
	{
		List<int[]> tcPFs = new ArrayList<int[]>();
		//先读.testcase文件。
		if( !readTestcaseFile(tcPFs) )
			return false;
		//再比较XX_fault.cvs和XX.testcase的结果
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
										+getBugID(k+1));  //k不是bugid
				result = false;
			}
			int[] tcAry = tcPFs.get(k);
			if( tcAry[0]!=passed || tcAry[1]!=failed )
			{
				System.out.println("tcAry[0]!=passed ("+tcAry[0]+","+passed+") || tcAry[1]!=failed of the order="+(k+1)+", bugid="
						+getBugID(k+1)+"("+tcAry[1]+","+failed+")");//k不是bugid
				result = false;
			}
		}
		return result;
	}
	
	//检测手工输入的_fault.csv文件与.profile文件，对比故障文件是否存在，故障语句是否存在
	//对比两个文件的通过测试用例数目和失败测试用例数目是否相同。
	@Override
	public boolean checkProfileAndFaultFile()
	{
		boolean result = true;
		int verAmount = getVerNo();
		for( int ver=1;ver<=verAmount; ver++)
		{
			int bugid = getBugID(ver);  //bugid，并非自然顺序。
			int pos = faultFilename.lastIndexOf('\\');
			String path = faultFilename.substring(0,pos);
			String profileFilename = path +"\\profile\\"+	projectName+"_v"+String.valueOf(bugid)+".profile";

			MultiProfileFile pf = new MultiProfileFile(projectName,bugid,profileFilename);//只能逐个版本测试。
			if( false==pf.readProfileFile() )
			{
				result = false;
				System.out.println("Read file "+profileFilename+"  is error.");
				continue; //原来是break，但可能多个.profile文件出错，所以改为continue.
			}
			//对比两个文件的通过测试用例数目和失败测试用例数目是否相同。
			int passed = tcPasseds.get(ver-1);
			int failed = tcFaileds.get(ver-1);
			if( pf.getPassed()!=passed || pf.getFailed()!=failed )
			{
				System.out.println("pf.getPassed()!=passed ("+pf.getPassed()+","+passed+") || pf.getFailed()!=failed of the order="
							+ver+", bugid="	+bugid);//考虑bugid
				result = false;
			}
			//对比故障文件是否存在，故障语句是否存在
			int[] faultStats = getFaultLinesVer(ver); //该版本的故障语句号数组
			String[] fileNames = getFaultFilesVer(ver);//该版本的故障文件名数组。
			int faultSize = faultStats.length;
			for( int k=0;k<faultSize;k++ )
			{
				if( !pf.isExistFault(fileNames[k], faultStats[k]) )
				{
					System.out.println("Fault "+fileNames[k]+","+faultStats[k]+" of the order= "+ver+
							"  or V"+bugid+" of bugid  is not found.");//ver不是bugid
					result = false;
				}
			}
		}
		return result;
	}

}
