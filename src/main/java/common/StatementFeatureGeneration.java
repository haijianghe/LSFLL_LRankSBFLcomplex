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

/** �������ĸ��Ӷȣ��洢����Ӧ�ļ���
 * @author Administrator
 *
 */
public class StatementFeatureGeneration {

	//generateSourceCodeComplexMetrics
	//calculate and store the complex metric of program source code of  dataset.
	/**
	 * ���㣬�������ݼ��ģ�����Ϊ��λ��������
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
	
	/**��Ȩ����������ΪCSDN���������������ڡ���ԭ�����£���ѭCC 4.0 BY-SA��ȨЭ�飬ת���븽��ԭ�ĳ������Ӽ���������
	ԭ�����ӣ�https://blog.csdn.net/m0_38059938/article/details/80658409
	 * ����String��ʱ�䣬��ȡlong��ʱ�䣬��λ����
	 * @param inVal ʱ���ַ���
	 * @return long��ʱ��
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
	
	/** ����ض��������ơ�
	 * ���㣬�������������ݼ��ģ�����Ϊ��λ�ĳ�����伶���븴�Ӷȡ�
	 */
	private static boolean calculateStatementComplexMetricFeature(String objectName)
	{
		boolean processok = true;
		//�ȶ�ȡ�ö���İ汾������
		IFaultFile ffiAgent = AffiliatedFactory.createFaultFileObject(objectName);
		if( !ffiAgent.readFaultFile() )
			return false;
		int timeCounts = 0;  //����ƽ��֮��Ĵ�����
		long timeCalFeature = 0; //��������ƽ���ļ���ʱ�䡣
		int vers = ffiAgent.getVerNo();
		for( int v=1;v<=vers; v++)
		{
			//if( v!=5 )
			//	continue;  //for test
			int bugId = ffiAgent.getBugID(v);
			if( true==ExcludeVersion.isExcludeVer(objectName,bugId) )
				continue; //�ð汾���μӼ��㡣
			//��ȡ.profile�ļ���
			//int[] faultStats = ffiAgent.getFaultLinesVer(v); //�ð汾�Ĺ�����������
			//String[] faultFilenames = ffiAgent.getFaultFilesVer(v);//�ð汾�Ĺ����ļ������顣
			IProfileFile profileAgent = AffiliatedFactory.createProfileFileObject(objectName, bugId);
			if( false==profileAgent.readProfileFile() )
			{
				processok = false;
				System.out.println("Read file "+objectName+"_v"+String.valueOf(bugId)+".profile is error.");
				break;
			}//end of if...
			timeCounts++;
			//��ȡ��ǰʱ��Ϊ��ֹʱ�䣬ת��Ϊlong��
			long startTime =fromDateStringToLong(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss:SSS").format(new Date()));
			//ע�⣺ �Ժ������ù���ģʽ���������Ӷȵļ��㣬�����ࡣ
			SoftwareLineComplexMetricFeature sMetric = new SoftwareLineComplexMetricFeature(objectName,bugId);
			boolean calResult = sMetric.calComplexMetricValue();
			if( !calResult  )
			{
				processok = false;
				break;
			}
			//��ȡ��ǰʱ��Ϊ��ʼʱ�䣬ת��Ϊlong��
			long stopTime = fromDateStringToLong(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss:SSS").format(new Date()));
			//����ʱ���,��λ����
			timeCalFeature += stopTime - startTime;
			//������ȷ������������.complex�ļ�
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
