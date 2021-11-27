/**
 * 
 */
package ranking;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import affiliated.AbstractProfileFile;
import affiliated.AffiliatedFactory;
import affiliated.ExcludeVersion;
import affiliated.FileSpectrum;
import affiliated.IFaultFile;
import affiliated.IProfileFile;
import affiliated.SpectrumStruct;
import common.ProjectConfiguration;
import common.XMLConfigFile;
import sbflMetrics.ProfileStatement;
import sbflMetrics.SBFLTradNormalizeTechnique;
import softComplexMetric.LineComplexFeatureFile;

/**
 * @author Administrator
 *
 */
public class RankLibFeatrure {
	/*��Ŀ�ֳ�ʮ�飬ÿһ�����������Ŀ��ѵ�����Ͳ��Լ����� 7:3
	 * ��һ�� G1,G2,...G7 ѵ��   G8,G9,G10 ����
	 * �ڶ��� G2,...G7 G8 ѵ��   G9,G10,G1 ����
	 * ....
	 * ��ʮ�� G10,G1,...G6 ѵ��   G7,G8,G9 ����
	 */
	/*�ڶ���������ѵ��������֤���Ͳ��Լ����� 4:3:3
	 * ��һ�� 1,2,3,4 ѵ��........5,6,7 ��֤........8,9,10 ����
	 * �ڶ��� 2,3,4,5 ѵ��........6,7,8 ��֤........9,10,1 ����
	 * ������ 3,4,5,6 ѵ��........7,8,9 ��֤........10,1,2 ����
	 * ������ 4,5,6,7 ѵ��........8,9,10 ��֤........1,2,3 ����
	 * ������ 5,6,7,8 ѵ��........9,10,1��֤ ........2,3,4 ����
	 * ������ 6,7,8,9 ѵ��........10,1,2��֤ ........3,4,5 ����
	 * ������ 7,8,9,10 ѵ��........1,2,3��֤ ........4,5,6 ����
	 * �ڰ��� 8,9,10,1 ѵ��........2,3,4��֤ ........5,6,7 ����
	 * �ھ��� 9,10,1,2 ѵ��........3,4,5��֤ ........6,7,8 ����
	 * ��ʮ�� 10,1,2,3 ѵ��........4,5,6��֤ ........7,8,9 ����
	 */
	private static String[][] projectGroup = {
			{"flexV4","flexV2","PairikaOpenCV331","traccar","Chart"},  //1: 95
			{"grepV3","grepV2","grepV4","print_tokens2","Closure","JacksonXml"}, //93
			{"gzipV1","gzipV2","sedV3","sedV5","sedV6","sedV7","Lang"}, //87
			{"replace","Csv","Time"}, //72
			{"space","PairikaOpenCV340","Compress"}, //5: 85
			{"tcas","JxPath","Codec"}, //73
			{"print_tokens","JacksonDatabind","Gson"}, //117
			{"schedule","Math","Collections","AutomatedCar"}, //111
			{"schedule2","Jsoup","FasterXML"}, //109
			{"tot_info","Cli","JacksonCore"}  //10��83
		};
	private static int[][] subgroupInFold = {
			{2,3,4},  //���ڵ�һ�����Ŀ��Ҳ����ǰ���G1, �õڶ�����������ѵ����ģ�ͼ���������   
			{3,4,5},
			{4,5,6},
			{5,6,7},
			{6,7,8},//���ڵ�5�����Ŀ��Ҳ����ǰ���G5:, �õ�6��7��8��ѵ����ģ�ͼ���������"space","PairikaOpenCV340","Compress"
			{7,8,9},
			{8,9,10},
			{9,10,1},
			{10,1,2},
			{1,2,3} //��10�飬G10,"tot_info","Cli","JacksonCore", �õ�1��2��3��ѵ����ģ�ͼ���������
		};
	/*
	 * ʹ��Learing to Rankģʽ����ĳ��Ŀ��ĳ���汾��˵��
	 *   ����������Ϊ1�����������Ϊ0��  
	 *   Ϊ����ģ�͹���ϣ�ͬһ���汾������ֻ�ɼ�NumberOfQueryStatement����䡣
	 */
	private final static int NumberOfQueryStatement = 100;  
	
	
	/**projectGroup ����ʮ�飬ȡ��index����Ŀ����
	 * @param index    0.1.2...9
	 * @return
	 */
	public static String[] getProjectSubgroup(int index)
	{
		return projectGroup[index];
	}
	
	/** �� ��Ŀ���Ʋ��ң���Ӧ���������� Fold ѵ��ģ�Ͷ�����������ֵ��
	 * @param objectName ��Ŀ����
	 * @return
	 */
	public static int[] getFoldthObject(String objectName)
	{
		int index = -1; 
		for( int i=0;i<10;i++ )
		{
			String[] projects = projectGroup[i];
			boolean found = false;
			for( String project : projects )
			{
				if( project.contentEquals(objectName) )
				{
					found = true;
					break;
				}
			}
			if( found )
			{
				index = i; //��������Ҳ������������˵���������
				break;
			}
		}
		return subgroupInFold[index];
	}
	
	
	/**����RankLib��ʽ�������ļ���ֻ��ѵ�����Ͳ��Լ���
	 * 
	 */
	public static void makeRankingFeatureFile()
	{
		for( int i=0;i<10;i++ )
		{
			//if( i!=0 )
			//	continue;  //for testing
			String trainFile = ProjectConfiguration.PathLineLtoRankTrainFearture+"\\Fold"+String.valueOf(i+1)+".train";
			List<String> objectLst = new ArrayList<>();
			for( int j=0;j<7;j++ )
			{
				String[] projects = projectGroup[(i+j)%10];
				for( String project: projects )
					objectLst.add(project);
			}
			writeRankingFeatureFile(trainFile,objectLst); //����ѵ���ļ���
		/*	String testingFile = "Fold"+String.valueOf(i+1)+".testing";
			objectLst.clear();
			for( int k=7;k<10;k++ )
			{
				String[] projects = projectGroup[(i+k)%10];
				for( String project: projects )
					objectLst.add(project);
			}
			writeRankingFeatureFile(testingFile,objectLst);*/
		}
		//д�����ļ���ÿ��project�����Ϊ���Լ���
		int testIdQuery = 1000; 
		for( int k=0;k<10;k++ )
		{
			String[] projects = projectGroup[k];
			for( String project: projects )
			{
				writeTestingRankFeatureFile(testIdQuery,project);
				testIdQuery += 1000;
			}
		}//end of for( int k=0;k<10;k++ )
	}
	
	/** ��objectLst��Ŀ�������ҳ�������ŵ��ļ�filename��
	 * @param featureFilename  ���浽���ļ�����
	 * @param objectLst ��Ŀ������
	 */
	private static boolean  writeRankingFeatureFile(String featureFilename,List<String> objectLst)
	{
		boolean result = true;
		try {
			File file = new File(featureFilename);
			if (file.isFile() && file.exists())
				file.delete();
			OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file));
			BufferedWriter bw = new BufferedWriter(writer);
			int idQuery = 1000;  //�������ĵ��ţ�����ѯ����Ŀ��RankLib�ļ�Ҫ��Ĳ�ѯID
			for( String objectName : objectLst )
			{
				//if( !objectName.contentEquals("traccar") )
				//	continue;  //for testing
				writeProjectRankingFeatureFile(idQuery,bw,objectName);
				idQuery += 1000; //��һ����Ŀ��103���汾���������Щ��
			}
			writer.close();
		}//end of try. 
		catch (Exception e) {
			e.printStackTrace();
			result = false;
		}
		return result;
	}
	
	/* �� ...//LtoRankTestingFeature�ļ��������ÿ����Ŀ��ÿ���汾����һ��.train�ļ�������ÿ���汾��Լֻ��100������������
	 * ���ļ����£�ÿ����Ŀ��ÿ���汾������һ��.testing�ļ�����.train�ļ���ͬ���������п�ִ����������������
	 * ÿ����Ŀ��ÿ���汾������һ��������ѵ�������ݣ����á�
	 * �ⲿ��ѵ����ֻΪ��RandomProjectEvaluateLibLinear���ã���Ϊ�˼�С������������������С�
	 */
	public static void makeAllProjectTrainDataset()
	{
		try {
			List<String> allObjectNames = XMLConfigFile.getAllObjectNames();
			int idQuery = 1000;  //�������ĵ��ţ�����ѯ����Ŀ��RankLib�ļ�Ҫ��Ĳ�ѯID
			for( String project : allObjectNames )
			{
				//�ȶ�ȡ�ö���İ汾������
				IFaultFile ffiAgent = AffiliatedFactory.createFaultFileObject(project);
				if( !ffiAgent.readFaultFile() )
					throw new Exception("read .fault is error"); //����
				int vers = ffiAgent.getVerNo();
				for( int v=1;v<=vers; v++)
				{
					int bugId = ffiAgent.getBugID(v);
					if( true==ExcludeVersion.isExcludeVer(project,bugId) )
						continue; //�ð汾���μӼ��㡣
					String[] faultFilenames = ffiAgent.getFaultFilesVer(v); 
					int[] faultStats = ffiAgent.getFaultLinesVer(v);
					String trainFeatureFilename =  ProjectConfiguration.PathLineLtoRankTestingFearture+"\\"+
							project+"\\LibLinear_"+project+"_v"+String.valueOf(bugId)+".train";
					File file = new File(trainFeatureFilename);
					if (file.isFile() && file.exists())
						file.delete();
					OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file));
					BufferedWriter bw = new BufferedWriter(writer);
					//��ȡ.profile�ļ���
					IProfileFile profileAgent = AffiliatedFactory.createProfileFileObject(project, bugId);
					if( false==profileAgent.readProfileFile() )
					{
						System.out.println("Read file "+project+"_v"+String.valueOf(bugId)+".profile is error.");
						bw.close();
						writer.close();
						throw new Exception("read .profile is error"); //����;
					}//end of if...
					//��ȡ.complex�ļ���
					LineComplexFeatureFile lsMetric = new LineComplexFeatureFile(project,bugId);
					boolean lsResult = lsMetric.readComplexFeartureFile();
					if( !lsResult  )
					{
						bw.close();
						writer.close();
						throw new Exception("read .complex is error"); //����;
					}
					//�����������ļ���������Ϻ󣬴���bw������ļ�
					int projectVerIdQuery = idQuery+bugId; //��ͬ�汾��Ӧ��ͬ�Ĳ�ѯID��
					writePartialStatementFeatureFile(projectVerIdQuery,bw,faultFilenames, faultStats,profileAgent,lsMetric );
					bw.close();
					writer.close();
				}//end of ffor( int v=1;v<=...
				idQuery += 1000; //��һ����Ŀ��103���汾���������Щ��
			}//end of for( String project ...
		}//end of try. 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/** ��ĳ����Ŀ������д���ļ���
	 * @param featureFilename
	 * @param objectLst
	 * @return
	 */
	private static boolean writeProjectRankingFeatureFile(int idQuery,BufferedWriter bw,String project) throws IOException
	{
		boolean processok = true;
		//�ȶ�ȡ�ö���İ汾������
		IFaultFile ffiAgent = AffiliatedFactory.createFaultFileObject(project);
		if( !ffiAgent.readFaultFile() )
			return false;
		int vers = ffiAgent.getVerNo();
		for( int v=1;v<=vers; v++)
		{
			int bugId = ffiAgent.getBugID(v);
			if( true==ExcludeVersion.isExcludeVer(project,bugId) )
				continue; //�ð汾���μӼ��㡣
			int projectVerIdQuery = idQuery+bugId; //��ͬ�汾��Ӧ��ͬ�Ĳ�ѯID��
			//�����������ļ���������Ϻ󣬴���bw������ļ�
			processok = mergeProfileComplexFileWholeStatementFeature(projectVerIdQuery,project,bugId,bw,ffiAgent,v);
		}//end of for...
		if( processok )
			System.out.println(project+" 's feature file is ok! ");
		return processok;
	}
	
	
	/** ��bugFilename�����д�������к���ӵ�haveAddLineno
	 * @param haveAddLineno 
	 * @param faultFilenames �ð汾�еĴ����ļ�
	 * @param faultStats          ��Ӧ���к�
	 * @param bugFilename ֻ��Ӵ��ļ����кš�
	 *       ���ظô���������ļ������һ�������кţ�����ж���Ļ���
	 */
	private static int addLinenoOfFault(List<Integer> haveAddLineno,String[] faultFilenames,  int[] faultStats,
			String bugFilename)
	{
		int bugStats = 0;
		int nFaults = faultStats.length; //�������������
		for( int t=0;t<nFaults;t++ )
		{
			if( faultFilenames[t].contentEquals(bugFilename) )
			{
				haveAddLineno.add(faultStats[t]);
				bugStats = faultStats[t];
			}
		}
		return bugStats;
	}
	
	//faultLabel = true: fault
	private static String getStatementRankingFeatureString(boolean faultLabel,int idQuery,
			String filename,  int lineno, SpectrumStruct spes, int passed,int failed,String complexFeature )
	{
		if( spes.getLineNo()<=0 )
		{
			System.out.println(idQuery+"!  "+filename+" : "+lineno+ " have not found. Big error! "+(faultLabel==true?"#########":" "));
			return "";
		}
		StringBuilder sb = new StringBuilder();
		if( faultLabel )
			sb.append("1 qid:"); //liblinear-ranksvmֻ����һ���ո�
		else
			sb.append("0 qid:");
		sb.append(idQuery);
		//ע�⣺liblinear-ranksvmֻ����һ���ո񣬹�һ��������ֵ��
		sb.append(" "+SBFLTradNormalizeTechnique.getSBFLTradFeature(spes, passed,failed,lineno));
		sb.append(" "+complexFeature);
		return sb.toString()+"\n";
	}
	
	/** �����������ļ���������Ϻ󣬴���bw������ļ�
	 * @param idQuery RankLibҪ��ġ�
	 * @param bw
	 * @param faultFilenames  ȱ�����ڵ��ļ�
	 * @param faultStats      ȱ������кţ�faultStats��faultFilenames����
	 * @param profileAgent     .profile���ݡ�
	 * @param lcffFeats  .complex���ݡ� ########������к���profileAgent����к��Ѿ���Ӧ��####
	 * @throws IOException
	 */
	private static void writePartialStatementFeatureFile(int idQuery,BufferedWriter bw,
			String[] faultFilenames,  int[] faultStats,
			IProfileFile profileAgent,
			LineComplexFeatureFile lcffFeats ) throws IOException
	{
		int passed = profileAgent.getPassed();
		int failed = profileAgent.getFailed();

		//�Ƚ����д�������������Ϣд��bw������ļ�	
		int nFaults = faultStats.length; //�������������
		for( int t=0;t<nFaults;t++ )
		{
			//�ҳ���t��bug(�ļ���faultFilenames[t], �к�faultStats[t])�ĳ�����
			SpectrumStruct speS = profileAgent.getSpectrumFileLineno(faultFilenames[t], faultStats[t]);
			String complexFeatureString = lcffFeats.getRankLibFeatureString(faultFilenames[t], faultStats[t]);
			bw.append( getStatementRankingFeatureString(true,idQuery,
					faultFilenames[t], faultStats[t], speS, passed,failed, complexFeatureString ));
		}
		//ͳ���ж��ٸ��ļ�������bug��ע��faultFilenames�����ظ����ļ�
		List<String> bugFiles = new ArrayList<>();
		for( int k=0;k<faultFilenames.length;k++ )
		{
			String filename = faultFilenames[k];
			if( !bugFiles.contains(filename) )
				bugFiles.add(filename);
		}
		//ÿ����bug���ļ���д����freeStats����ͨ��䡣
		int freeStats = NumberOfQueryStatement/bugFiles.size();
		int nTotalSamples  = 0;//������Ŀ��ÿ��idQuery��ԼΪNumberOfQueryStatement����
		for( String bugFilename : bugFiles )
		{
			//��ÿ���ļ���˵������һ�����������Ѿ����뵽RankLib�����ļ����кż�¼����������ظ���ӡ�
			List<Integer> haveAddLineno = new ArrayList<>();
			//���ļ������д�������Ѿ����,����ж�������¼���һ���кš�
			int lastLineno = addLinenoOfFault(haveAddLineno,faultFilenames,faultStats,bugFilename); 
			List<SpectrumStruct> ssList = profileAgent.getFileSpectrum(bugFilename);
			NeighborBugStatement nbsNeighbor = new NeighborBugStatement(freeStats,haveAddLineno,ssList,lastLineno);
			List<Integer> stmtNeighbor = nbsNeighbor.getNeighborLineNo();
			nTotalSamples += stmtNeighbor.size();
			for( Integer ln : stmtNeighbor )
			{
				//�ҳ�bugFilename���һ��bug��ǰ���ھӣ����������ǵ�������
				SpectrumStruct speS = profileAgent.getSpectrumFileLineno(bugFilename, ln);
				String complexFeatureString = lcffFeats.getRankLibFeatureString(bugFilename, ln);
				bw.append( getStatementRankingFeatureString(false,idQuery,
						bugFilename, ln, speS, passed,failed, complexFeatureString ));
			}
		}
		//���������Ļ����������ļ�������bug�ģ�ȥ�ҡ�
		if( nTotalSamples<NumberOfQueryStatement )
		{
			NonBugFileFetchStatement nbffs = new NonBugFileFetchStatement(profileAgent.getSpectrumList(),
					NumberOfQueryStatement-nTotalSamples,bugFiles);
			nbffs.fetchStatementForFearture();
			int numFetchStatm = nbffs.getFetchStatm();
			for( int q=0;q<numFetchStatm;q++ )
			{
				String stmtFilename = nbffs.getIndexFilename(q);
				int stmeLineno = nbffs.getIndexLineno(q);
				SpectrumStruct speS = profileAgent.getSpectrumFileLineno(stmtFilename, stmeLineno);
				String complexFeatureString = lcffFeats.getRankLibFeatureString(stmtFilename, stmeLineno);
				bw.append( getStatementRankingFeatureString(false,idQuery,
						stmtFilename, stmeLineno, speS, passed,failed, complexFeatureString ));
			}
		}
		bw.flush(); //д���ļ���
	}
	
	/** ����Ŀproject�������ҳ�������ŵ��ļ�.testing��
	 * @param project  ��Ŀ����
	 * @param testIdQuery �������ֲ�ͬ��Ŀ����ͬ�汾
	 */
	private static boolean  writeTestingRankFeatureFile(int testIdQuery,String project)
	{
		boolean result = true;
		try {
			String prjDir = ProjectConfiguration.PathLineLtoRankTestingFearture+"\\"+project;
			File directory = new File(prjDir);
			if( !directory.exists() )
				directory.mkdir(); //�ȴ���Ŀ¼,Ŀ¼�½���Ÿ��汾�������ļ������ڲ��ԡ�
			//�ȶ�ȡ�ö���İ汾������
			IFaultFile ffiAgent = AffiliatedFactory.createFaultFileObject(project);
			if( !ffiAgent.readFaultFile() )
				return false;
			int vers = ffiAgent.getVerNo();
			for( int v=1;v<=vers; v++)
			{
				int bugId = ffiAgent.getBugID(v);
				if( true==ExcludeVersion.isExcludeVer(project,bugId) )
					continue; //�ð汾���μӼ��㡣
				String featureFilename = prjDir+"\\"+project+"_v"+String.valueOf(bugId)+".testing";
				File file = new File(featureFilename);
				if (file.isFile() && file.exists())
					file.delete();
				OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file));
				BufferedWriter bw = new BufferedWriter(writer);
				int projectVerIdQuery = testIdQuery+bugId; //��ͬ�汾��Ӧ��ͬ�Ĳ�ѯID��
				result = mergeProfileComplexFileWholeStatementFeature(projectVerIdQuery,project,bugId,bw,ffiAgent,v);
				writer.close();
			}//end of for...
			if( result )
				System.out.println(project+" 's feature file of testing is ok! ");
		}//end of try. 
		catch (Exception e) {
			e.printStackTrace();
			result = false;
		}
		return result;
	}
	
	//��.profile�ļ���.complex�ļ�����������󣬺ϲ����ļ�bw
	private static boolean mergeProfileComplexFileWholeStatementFeature(int pvqQid,String project,int bugId,
			BufferedWriter bw,IFaultFile ffiAgent,  int verTh)throws IOException
	{
		//��ȡ.profile�ļ���
		IProfileFile profileAgent = AffiliatedFactory.createProfileFileObject(project, bugId);
		if( false==profileAgent.readProfileFile() )
		{
			System.out.println("Read file "+project+"_v"+String.valueOf(bugId)+".profile is error.");
			return false;
		}//end of if...
		//��ȡ.complex�ļ���
		LineComplexFeatureFile lsMetric = new LineComplexFeatureFile(project,bugId);
		boolean lsResult = lsMetric.readComplexFeartureFile();
		if( !lsResult  )
			return false;
		//������ �����������ļ���������Ϻ󣬴���bw������ļ�
		writeWholeStatementFeatureFile(pvqQid,bw,ffiAgent,verTh/*����bugId*/,profileAgent,lsMetric);
		return true;
	}

	/** �����������ļ���������Ϻ󣬴���bw������ļ�
	 * @param idQuery RankLibҪ��ġ�
	 * @param bw
	 * @param faultFilenames  ȱ�����ڵ��ļ�
	 * @param faultStats      ȱ������кţ�faultStats��faultFilenames����
	 * @param profileAgent     .profile���ݡ�
	 * @param lcffFeats  .complex���ݡ� ########������к���profileAgent����к��Ѿ���Ӧ��####
	 * @throws IOException
	 */
	private static void writeWholeStatementFeatureFile(int idQuery,BufferedWriter bw,
			IFaultFile ffiAgent,  int verTh,
			IProfileFile profileAgent,
			LineComplexFeatureFile lcffFeats ) throws IOException
	{
		int passed = profileAgent.getPassed();
		int failed = profileAgent.getFailed();

		//��������������д��
		List<FileSpectrum> fileSpectra = profileAgent.getSpectrumList();//������
		for( FileSpectrum fs : fileSpectra )
		{
			String filename = fs.getClassFilename();
			List<SpectrumStruct> lineCodes = fs.getLineCodes();
			for( SpectrumStruct ss :lineCodes )
			{
				int lineno = ss.getLineNo();
				SpectrumStruct speS = profileAgent.getSpectrumFileLineno(filename, lineno);
				String complexFeatureString = lcffFeats.getRankLibFeatureString(filename, lineno);
				boolean faultLabel = ffiAgent.isFaultStatement(verTh, filename, lineno);
				bw.append( getStatementRankingFeatureString(faultLabel,idQuery,
						filename, lineno, speS, passed,failed, complexFeatureString ));
			} 
		}//end of for( fs
		bw.flush(); //д���ļ���
	}
	
	//����RankLib��ʽ�������ļ�����Ϊѵ��������֤���Ͳ��Լ���
	public static void makeRankingFeatureFile2()
	{
	/*	for( int i=0;i<10;i++ )
		{
			String trainFile = ProjectConfiguration.PathLineLtoRankTrainFearture+"\\Fold"+String.valueOf(i+1)+".train2";
			List<String> objectLst = new ArrayList<>();
			for( int j=0;j<=3;j++ )
			{
				String[] projects = projectGroup[(i+j)%10];
				for( String project: projects )
					objectLst.add(project);
			}
			writeRankingFeatureFile(trainFile,objectLst); //����ѵ���ļ���
//			String validateFile = ProjectConfiguration.PathLineLtoRankTrainFearture+"\\Fold"+String.valueOf(i+1)+".validate2";
//			List<String> vobjLst = new ArrayList<>();
//			for( int t=4;t<=6;t++ )
//			{
//				String[] projects = projectGroup[(i+t)%10];
//				for( String project: projects )
//					vobjLst.add(project);
//			}
//			writeRankingFeatureValidateFile(validateFile,vobjLst); //������֤���ļ���
		}*/
		//д�����ļ���ÿ��project�����Ϊ���Լ���
		int testIdQuery = 1000; 
		for( int k=0;k<10;k++ )
		{
			String[] projects = projectGroup[k];
			for( String project: projects )
			{
				writeTestingRankFeatureFile(testIdQuery,project);
				testIdQuery += 1000;
			}
		}//end of for( int k=0;k<10;k++ )
	}
	
	/**  ��Ϊѵ��������֤���Ͳ��Լ��� 
	 * ��objectLst��Ŀ�������ҳ�������ŵ��ļ�filename��
	 * @param featureFilename  ���浽���ļ�����
	 * @param objectLst ��Ŀ������
	 */
	private static boolean  writeRankingFeatureValidateFile(String featureFilename,List<String> objectLst)
	{
		boolean result = true;
		try {
			File file = new File(featureFilename);
			if (file.isFile() && file.exists())
				file.delete();
			OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file));
			BufferedWriter bw = new BufferedWriter(writer);
			int idQuery = 30000;  //�������ĵ��ţ�����ѯ����Ŀ��RankLib�ļ�Ҫ��Ĳ�ѯID
			for( String objectName : objectLst )
			{
				//��ѵ����һ����ֻ�ɼ�100�������Ϊ��֤�������ַ�����RankLib��Ϊ����
				writeProjectRankingFeatureFile(idQuery,bw,objectName);
				//����Լ�һ�����ɼ����������Ϊ��֤����ʹ��LibLinear-ranksvm
				//writeProjectRankingFeatureValidateFile(idQuery,bw,objectName);
				idQuery += 1000; //��һ����Ŀ��103���汾���������Щ��
			}
			writer.close();
		}//end of try. 
		catch (Exception e) {
			e.printStackTrace();
			result = false;
		}
		return result;
	}

}