/**
 * 
 */
package common;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import affiliated.AffiliatedFactory;
import affiliated.ExcludeVersion;
import affiliated.FileSpectrum;
import affiliated.IFaultFile;
import affiliated.IProfileFile;
import softComplexMetric.SoftwareLineComplexMetricFeature;

/** 计算代码的复杂度，存储到对应文件。
 * @author Administrator
 *
 */
public class StatementFeatureGeneration {

	//generateSourceCodeComplexMetrics
	//calculate and store the complex metric of program source code of  dataset.
	/**
	 * 计算，所有数据集的，以行为单位的特征。
	 */
	public static void makeFeatureFile()
	{
		List<String> allObjectNames = XMLConfigFile.getAllObjectNames();
		for( String project : allObjectNames )
		{
			//PairikaOpenCV331 Math FasterXML print_tokens
			//if( !project.contentEquals("flexV4") )  //for test.
			//	continue;
			System.out.println(project+" start. ");
			boolean processok = calculateStatementComplexMetricFeature(project);
			if( processok )
				System.out.println("\n    @@"+project+" 's feature file is ok.");
			else
				System.out.println("\n    @@"+project+" 's feature file is fail. Check it.");
		}
	}
	
	/**版权声明：本文为CSDN博主「加拉萨假期」的原创文章，遵循CC 4.0 BY-SA版权协议，转载请附上原文出处链接及本声明。
	原文链接：https://blog.csdn.net/m0_38059938/article/details/80658409
	 * 根据String型时间，获取long型时间，单位毫秒
	 * @param inVal 时间字符串
	 * @return long型时间
	 */
	public static long fromDateStringToLong(String inVal) {
	    Date date = null;
	    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss:SSS");
	    try {
	        date = inputFormat.parse(inVal);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return date.getTime();
	}
	
	/** 针对特定对象名称。
	 * 计算，并保存所有数据集的，以行为单位的程序语句级代码复杂度。
	 */
	private static boolean calculateStatementComplexMetricFeature(String objectName)
	{
		boolean processok = true;
		//先读取该对象的版本总数。
		IFaultFile ffiAgent = AffiliatedFactory.createFaultFileObject(objectName);
		if( !ffiAgent.readFaultFile() )
			return false;
		int timeCounts = 0;  //计算平均之间的次数。
		long timeCalFeature = 0; //特征向量平均的计算时间。
		int vers = ffiAgent.getVerNo();
		for( int v=1;v<=vers; v++)
		{
			//if( v!=5 )
			//	continue;  //for test
			int bugId = ffiAgent.getBugID(v);
			if( true==ExcludeVersion.isExcludeVer(objectName,bugId) )
				continue; //该版本不参加计算。
			//读取.profile文件。
			//int[] faultStats = ffiAgent.getFaultLinesVer(v); //该版本的故障语句号数组
			//String[] faultFilenames = ffiAgent.getFaultFilesVer(v);//该版本的故障文件名数组。
			IProfileFile profileAgent = AffiliatedFactory.createProfileFileObject(objectName, bugId);
			if( false==profileAgent.readProfileFile() )
			{
				processok = false;
				System.out.println("Read file "+objectName+"_v"+String.valueOf(bugId)+".profile is error.");
				break;
			}//end of if...
			timeCounts++;
			//获取当前时间为截止时间，转换为long型
			long startTime =fromDateStringToLong(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss:SSS").format(new Date()));
			//注意： 以后这里用工厂模式，其它复杂度的计算，代码差不多。
			SoftwareLineComplexMetricFeature sMetric = new SoftwareLineComplexMetricFeature(objectName,bugId);
			boolean calResult = sMetric.calComplexMetricValue();
			if( !calResult  )
			{
				processok = false;
				break;
			}
			//获取当前时间为开始时间，转换为long型
			long stopTime = fromDateStringToLong(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss:SSS").format(new Date()));
			//计算时间差,单位毫秒
			timeCalFeature += stopTime - startTime;
			//计算正确，将其结果存入.complex文件
			List<FileSpectrum> fileSpectra = profileAgent.getSpectrumList();
			boolean writeResult = sMetric.writeComplexMetricFeatureFile(fileSpectra,
										profileAgent.getPassed(),profileAgent.getFailed());
			if( !writeResult )
			{
				processok = false;
				break;
			}
		}//end of for...
		System.out.println("\n%$^#@*!"+objectName+" 's time of calculate software complex is "+timeCalFeature/timeCounts+"ms");
		return processok;
	}
}
