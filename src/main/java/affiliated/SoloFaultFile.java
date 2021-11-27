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
 *  针对其.profile只有单个文件的情形。所有版本的测试用例通过数，失败个数，总数都相同。
 */
public class SoloFaultFile implements IFaultFile{
	String projectName;   //即objectName

	int verNo; //版本总数，version  numbers, bugNo 
	int tcNo;  //测试用例总数，test case number;
	/*
	 * 一定要注意：testCases第一个元素是第一个版本的passed,failed,依次类推 。
	 * 当然，(object).testcase文件里的信息也必须按照顺序存放。
	 */
	List<int[]> testCases = new ArrayList<int[]>();  //所有版本的passed,failed。
	/*
	 * 一定要注意：faultLines第一个元素是第一个版本V1的故障语句行号集合，第二个亦如此，依次类推 。
	 * 当然，(object).fault文件里的信息也必须按照顺序存放。
	 */
	List<int[]> faultLines = new ArrayList<int[]>();  //所有版本的故障语句行号集合。
	String faultFilename; //带目录的(object).fault
	String tcFilename; //带目录的(object).testcase
	String sourceCodeFile;//源代码文件，不带目录，匹配MultiFaultFile里的List<String[]> faultFiles。
	/*
	 * objectName:对象名字。
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
	/* spilt(" ");的处理结果。
	 * 1，字符串首部的每个空格都转成了一个空串。
	 * 2，中间的多个空格中，多余的每个空格都转成了一个空串。
	 * 3，末尾的多个空格全都去掉了。
	 * 
	 * spilt("\\s+");的处理结果。
	 * 1，字符串首部的所有空格转成了一个空串。
	 * 2，中间的多个空格，全都去掉了。
	 * 3，末尾的多个空格全都去掉了。
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
				lineTXT = lineTXT.trim(); //去掉首尾空格
				String[]  strAry = lineTXT.split("\\s+"); //允许多个空格分割字符串
				verNo = Integer.valueOf(strAry[0]); //版本数
				
				lineTXT = br.readLine();  //test case total
				lineTXT = lineTXT.trim(); //去掉首尾空格
				strAry = lineTXT.split("\\s+");
				tcNo = Integer.valueOf(strAry[0]); //测试用例数目
				
				br.readLine(); //fault line info prompt
				for( int i=0;i<verNo;i++ )
				{
					lineTXT = br.readLine();  //fault line info
					lineTXT = lineTXT.trim(); //去掉首尾空格
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
				lineTXT = lineTXT.trim(); //去掉首尾空格
				String[]  strAry = lineTXT.split("\\s+"); //允许多个空格分割字符串
				verNo = Integer.valueOf(strAry[1]); //版本数
				
				lineTXT = br.readLine();  //test case total
				lineTXT = lineTXT.trim(); //去掉首尾空格
				strAry = lineTXT.split("\\s+");
				tcNo = Integer.valueOf(strAry[1]); //测试用例数目
				
				br.readLine(); //testcase line info prompt
				for( int i=0;i<verNo;i++ )
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

	//版本总数
	@Override
	public int getVerNo()
	{
		return this.verNo;
	}
	
	//获取第几个版本(自然顺序)的版本号,index从1到verNo,注意index并非对应bugid.
	//可能出现这种情况，共三个版本，版本号分别为:2,5,7
	@Override
	public int getBugID(int index)
	{
		return index;
	}
	
	//测试用例总数，所有版本的测试用例总数都相同。
	public int getTesecaseNo()
	{
		return this.tcNo;
	}
	
	 //所有版本的故障语句行号集合。
	public List<int[]> getFaultLines()
	{
		return this.faultLines;
	}
	

	//获取某个版本的故障语句数组。
	//获取第几个版本(自然顺序)的故障语句数组,index从1到verNo,注意index并非对应bugid.
	@Override
	public int[] getFaultLinesVer(int index)
	{
		return faultLines.get(index-1);
	}
	
	//获取某个版本的故障语句所在文件的集合。特别注意：faultFiles和faultLines的二维顺序保持一致。
	//index从1到verNo,注意index并非对应bugid.
	@Override
	public String[] getFaultFilesVer(int index)
	{
		int numberOfFault = faultLines.get(index-1).length;
		String[] codeFiles = new String[numberOfFault];
		for( int i=0;i<numberOfFault;i++)
			codeFiles[i] = sourceCodeFile;
		return codeFiles;
	}
	
	//filename和lineno指定的语句是故障语句吗？
	//index从1到verNo,注意index并非对应bugid.
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
	 * 测试代码
	 */
	@Override
	public void testMe()
	{
		int verno = getVerNo();
		System.out.println("version amount := "+verno);
		int tcno = getTesecaseNo();
		System.out.println("test case amount := "+tcno);
		//故障语句显示。
		System.out.println("Fault lines : ");
		List<int[]> fls = getFaultLines();
		for( int[] items : fls )
		{
			int num = items.length;
			for( int t=0;t<num;t++ )
				System.out.print(items[t]+"  ");
			System.out.println(" ");
		}
		//测试用例显示
		System.out.println("testcases(verTh,passed,failed) : ");
		for( int i=0;i<verNo;i++ )
		{
			System.out.println( "v"+String.valueOf(i+1)+
					"     "+String.valueOf(testCases.get(i)[0])+
					"     "+String.valueOf(testCases.get(i)[1]) );
		}

	} //end of testMe.

	/** 在XX_.fault和XX.testcase，两个文件的通过测试用例数目和失败测试用例数目之和是否与总数相同。
	 */
	@Override
	public boolean checkTestcasePassedFailed() {
		//比较XX_fault.cvs和XX.testcase的结果
		boolean result = true;
		int verAmount = getVerNo();
		for( int k=0;k<verAmount;k++ )
		{
			int[] tcs = testCases.get(k);//第一个值为通过的测试用例数目，第二个为fail数目。
			if( tcNo!=(tcs[0]+tcs[1]) )
			{
				System.out.println("tcamount!=(passed+failed) of the order="+(k+1)+", bugid="
										+k+1);  //单文件.profile所有bugid都不缺失。
				result = false;
			}
		}
		return result;
	}

	//检测手工输入的.fault文件与.profile文件，对比故障文件是否存在，故障语句是否存在
	//对比两个文件的通过测试用例数目和失败测试用例数目是否相同。
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

			SoloProfileFile pf = new SoloProfileFile(projectName,ver,profileFilename,sourceCode);//只能逐个版本测试。
			if( false==pf.readProfileFile() )
			{
				result = false;
				System.out.println("Read file "+projectName+"_"+String.valueOf(ver)+".profile is error.");
				continue; //原来是break，但可能多个.profile文件出错，所以改为continue.
			}
			//对比两个文件的通过测试用例数目和失败测试用例数目是否相同。
			int[] tcs = testCases.get(ver-1);//第一个值为通过的测试用例数目，第二个为fail数目。

			int passed = tcs[0];
			int failed = tcs[1];
			if( pf.getPassed()!=passed || pf.getFailed()!=failed )
			{
				System.out.println("pf.getPassed()!=passed ("+pf.getPassed()+","+passed+") || pf.getFailed()!=failed of the order="
							+ver+", bugid="	+ver);//考虑bugid
				result = false;
			}
			//对比故障文件是否存在，故障语句是否存在
			int[] faultStats = faultLines.get(ver-1); //该版本的故障语句号数组
			int faultSize = faultStats.length;
			for( int k=0;k<faultSize;k++ )
			{
				if( !pf.isExistFault(faultStats[k]) )
				{
					System.out.println("Fault Line "+faultStats[k]+" of the order= "+ver+
							"  or V"+ver+" of bugid  is not found.");//多文件.profile版本ver不是bugid
					result = false;
				}
			}
		}
		return result;

	}
}//end of class

