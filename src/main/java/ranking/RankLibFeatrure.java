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
	/*项目分成十组，每一组包含若干项目。训练集和测试集比例 7:3
	 * 第一折 G1,G2,...G7 训练   G8,G9,G10 测试
	 * 第二折 G2,...G7 G8 训练   G9,G10,G1 测试
	 * ....
	 * 第十折 G10,G1,...G6 训练   G7,G8,G9 测试
	 */
	/*第二种做法，训练集、验证集和测试集比例 4:3:3
	 * 第一折 1,2,3,4 训练........5,6,7 验证........8,9,10 测试
	 * 第二折 2,3,4,5 训练........6,7,8 验证........9,10,1 测试
	 * 第三折 3,4,5,6 训练........7,8,9 验证........10,1,2 测试
	 * 第四折 4,5,6,7 训练........8,9,10 验证........1,2,3 测试
	 * 第五折 5,6,7,8 训练........9,10,1验证 ........2,3,4 测试
	 * 第六折 6,7,8,9 训练........10,1,2验证 ........3,4,5 测试
	 * 第七折 7,8,9,10 训练........1,2,3验证 ........4,5,6 测试
	 * 第八折 8,9,10,1 训练........2,3,4验证 ........5,6,7 测试
	 * 第九折 9,10,1,2 训练........3,4,5验证 ........6,7,8 测试
	 * 第十折 10,1,2,3 训练........4,5,6验证 ........7,8,9 测试
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
			{"tot_info","Cli","JacksonCore"}  //10：83
		};
	private static int[][] subgroupInFold = {
			{2,3,4},  //处于第一组的项目，也就是前面的G1, 用第二、三、四折训练的模型计算其结果。   
			{3,4,5},
			{4,5,6},
			{5,6,7},
			{6,7,8},//处于第5组的项目，也就是前面的G5:, 用第6、7、8折训练的模型计算其结果。"space","PairikaOpenCV340","Compress"
			{7,8,9},
			{8,9,10},
			{9,10,1},
			{10,1,2},
			{1,2,3} //第10组，G10,"tot_info","Cli","JacksonCore", 用第1、2、3折训练的模型计算其结果。
		};
	/*
	 * 使用Learing to Rank模式，对某项目的某个版本来说，
	 *   其错误语句标记为1，其它语句标记为0；  
	 *   为避免模型过拟合，同一个版本，大致只采集NumberOfQueryStatement条语句。
	 */
	private final static int NumberOfQueryStatement = 100;  
	
	
	/**projectGroup 共分十组，取其index的项目集。
	 * @param index    0.1.2...9
	 * @return
	 */
	public static String[] getProjectSubgroup(int index)
	{
		return projectGroup[index];
	}
	
	/** 由 项目名称查找，其应该由哪三类 Fold 训练模型而计算的排序分值。
	 * @param objectName 项目名称
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
				index = i; //如果出现找不到的情况，则说明程序出错。
				break;
			}
		}
		return subgroupInFold[index];
	}
	
	
	/**生成RankLib格式的特征文件。只有训练集和测试集。
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
			writeRankingFeatureFile(trainFile,objectLst); //保存训练文件。
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
		//写测试文件。每个project都会成为测试集。
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
	
	/** 将objectLst项目的特征找出来，存放到文件filename。
	 * @param featureFilename  保存到该文件名。
	 * @param objectLst 项目名集合
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
			int idQuery = 1000;  //类似于文档号，被查询的项目，RankLib文件要求的查询ID
			for( String objectName : objectLst )
			{
				//if( !objectName.contentEquals("traccar") )
				//	continue;  //for testing
				writeProjectRankingFeatureFile(idQuery,bw,objectName);
				idQuery += 1000; //有一个项目有103个版本，所以设大些。
			}
			writer.close();
		}//end of try. 
		catch (Exception e) {
			e.printStackTrace();
			result = false;
		}
		return result;
	}
	
	/* 在 ...//LtoRankTestingFeature文件夹下针对每个项目，每个版本生成一个.train文件，里面每个版本大约只有100个特征向量。
	 * 该文件夹下，每个项目，每个版本，还有一个.testing文件，与.train文件不同，包含所有可执行语句的特征向量。
	 * 每个项目，每个版本，产生一个独立的训练集数据，备用。
	 * 这部分训练集只为类RandomProjectEvaluateLibLinear所用，但为了减小代码量，放在这个类中。
	 */
	public static void makeAllProjectTrainDataset()
	{
		try {
			List<String> allObjectNames = XMLConfigFile.getAllObjectNames();
			int idQuery = 1000;  //类似于文档号，被查询的项目，RankLib文件要求的查询ID
			for( String project : allObjectNames )
			{
				//先读取该对象的版本总数。
				IFaultFile ffiAgent = AffiliatedFactory.createFaultFileObject(project);
				if( !ffiAgent.readFaultFile() )
					throw new Exception("read .fault is error"); //出错
				int vers = ffiAgent.getVerNo();
				for( int v=1;v<=vers; v++)
				{
					int bugId = ffiAgent.getBugID(v);
					if( true==ExcludeVersion.isExcludeVer(project,bugId) )
						continue; //该版本不参加计算。
					String[] faultFilenames = ffiAgent.getFaultFilesVer(v); 
					int[] faultStats = ffiAgent.getFaultLinesVer(v);
					String trainFeatureFilename =  ProjectConfiguration.PathLineLtoRankTestingFearture+"\\"+
							project+"\\LibLinear_"+project+"_v"+String.valueOf(bugId)+".train";
					File file = new File(trainFeatureFilename);
					if (file.isFile() && file.exists())
						file.delete();
					OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file));
					BufferedWriter bw = new BufferedWriter(writer);
					//读取.profile文件。
					IProfileFile profileAgent = AffiliatedFactory.createProfileFileObject(project, bugId);
					if( false==profileAgent.readProfileFile() )
					{
						System.out.println("Read file "+project+"_v"+String.valueOf(bugId)+".profile is error.");
						bw.close();
						writer.close();
						throw new Exception("read .profile is error"); //出错;
					}//end of if...
					//读取.complex文件。
					LineComplexFeatureFile lsMetric = new LineComplexFeatureFile(project,bugId);
					boolean lsResult = lsMetric.readComplexFeartureFile();
					if( !lsResult  )
					{
						bw.close();
						writer.close();
						throw new Exception("read .complex is error"); //出错;
					}
					//将两个特征文件的内容组合后，存入bw代表的文件
					int projectVerIdQuery = idQuery+bugId; //不同版本对应不同的查询ID。
					writePartialStatementFeatureFile(projectVerIdQuery,bw,faultFilenames, faultStats,profileAgent,lsMetric );
					bw.close();
					writer.close();
				}//end of ffor( int v=1;v<=...
				idQuery += 1000; //有一个项目有103个版本，所以设大些。
			}//end of for( String project ...
		}//end of try. 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/** 将某个项目的特征写入文件。
	 * @param featureFilename
	 * @param objectLst
	 * @return
	 */
	private static boolean writeProjectRankingFeatureFile(int idQuery,BufferedWriter bw,String project) throws IOException
	{
		boolean processok = true;
		//先读取该对象的版本总数。
		IFaultFile ffiAgent = AffiliatedFactory.createFaultFileObject(project);
		if( !ffiAgent.readFaultFile() )
			return false;
		int vers = ffiAgent.getVerNo();
		for( int v=1;v<=vers; v++)
		{
			int bugId = ffiAgent.getBugID(v);
			if( true==ExcludeVersion.isExcludeVer(project,bugId) )
				continue; //该版本不参加计算。
			int projectVerIdQuery = idQuery+bugId; //不同版本对应不同的查询ID。
			//将两个特征文件的内容组合后，存入bw代表的文件
			processok = mergeProfileComplexFileWholeStatementFeature(projectVerIdQuery,project,bugId,bw,ffiAgent,v);
		}//end of for...
		if( processok )
			System.out.println(project+" 's feature file is ok! ");
		return processok;
	}
	
	
	/** 将bugFilename里所有错误语句行号添加到haveAddLineno
	 * @param haveAddLineno 
	 * @param faultFilenames 该版本有的错误文件
	 * @param faultStats          对应的行号
	 * @param bugFilename 只添加此文件的行号。
	 *       返回该带错误语句文件的最后一个错误行号，如果有多个的话。
	 */
	private static int addLinenoOfFault(List<Integer> haveAddLineno,String[] faultFilenames,  int[] faultStats,
			String bugFilename)
	{
		int bugStats = 0;
		int nFaults = faultStats.length; //故障语句条数。
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
			sb.append("1 qid:"); //liblinear-ranksvm只允许一个空格
		else
			sb.append("0 qid:");
		sb.append(idQuery);
		//注意：liblinear-ranksvm只允许一个空格，归一化的特征值。
		sb.append(" "+SBFLTradNormalizeTechnique.getSBFLTradFeature(spes, passed,failed,lineno));
		sb.append(" "+complexFeature);
		return sb.toString()+"\n";
	}
	
	/** 将两个特征文件的内容组合后，存入bw代表的文件
	 * @param idQuery RankLib要求的。
	 * @param bw
	 * @param faultFilenames  缺陷所在的文件
	 * @param faultStats      缺陷语句行号，faultStats与faultFilenames对齐
	 * @param profileAgent     .profile内容。
	 * @param lcffFeats  .complex内容。 ########里面的行号与profileAgent里的行号已经对应。####
	 * @throws IOException
	 */
	private static void writePartialStatementFeatureFile(int idQuery,BufferedWriter bw,
			String[] faultFilenames,  int[] faultStats,
			IProfileFile profileAgent,
			LineComplexFeatureFile lcffFeats ) throws IOException
	{
		int passed = profileAgent.getPassed();
		int failed = profileAgent.getFailed();

		//先将所有错误语句的特征信息写入bw代表的文件	
		int nFaults = faultStats.length; //故障语句条数。
		for( int t=0;t<nFaults;t++ )
		{
			//找出第t个bug(文件名faultFilenames[t], 行号faultStats[t])的程序谱
			SpectrumStruct speS = profileAgent.getSpectrumFileLineno(faultFilenames[t], faultStats[t]);
			String complexFeatureString = lcffFeats.getRankLibFeatureString(faultFilenames[t], faultStats[t]);
			bw.append( getStatementRankingFeatureString(true,idQuery,
					faultFilenames[t], faultStats[t], speS, passed,failed, complexFeatureString ));
		}
		//统计有多少个文件里面有bug，注意faultFilenames里有重复的文件
		List<String> bugFiles = new ArrayList<>();
		for( int k=0;k<faultFilenames.length;k++ )
		{
			String filename = faultFilenames[k];
			if( !bugFiles.contains(filename) )
				bugFiles.add(filename);
		}
		//每个带bug的文件，写入其freeStats条普通语句。
		int freeStats = NumberOfQueryStatement/bugFiles.size();
		int nTotalSamples  = 0;//样本数目，每个idQuery大约为NumberOfQueryStatement个数
		for( String bugFilename : bugFiles )
		{
			//对每个文件来说，定义一个变量，将已经加入到RankLib特征文件的行号记录下来，免得重复添加。
			List<Integer> haveAddLineno = new ArrayList<>();
			//此文件的所有错误语句已经添加,如果有多条，记录最后一个行号。
			int lastLineno = addLinenoOfFault(haveAddLineno,faultFilenames,faultStats,bugFilename); 
			List<SpectrumStruct> ssList = profileAgent.getFileSpectrum(bugFilename);
			NeighborBugStatement nbsNeighbor = new NeighborBugStatement(freeStats,haveAddLineno,ssList,lastLineno);
			List<Integer> stmtNeighbor = nbsNeighbor.getNeighborLineNo();
			nTotalSamples += stmtNeighbor.size();
			for( Integer ln : stmtNeighbor )
			{
				//找出bugFilename最后一个bug的前后邻居，并计算它们的特征。
				SpectrumStruct speS = profileAgent.getSpectrumFileLineno(bugFilename, ln);
				String complexFeatureString = lcffFeats.getRankLibFeatureString(bugFilename, ln);
				bw.append( getStatementRankingFeatureString(false,idQuery,
						bugFilename, ln, speS, passed,failed, complexFeatureString ));
			}
		}
		//个数不够的话，从其它文件（不含bug的）去找。
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
		bw.flush(); //写入文件。
	}
	
	/** 将项目project的特征找出来，存放到文件.testing。
	 * @param project  项目名。
	 * @param testIdQuery 用来区分不同项目，不同版本
	 */
	private static boolean  writeTestingRankFeatureFile(int testIdQuery,String project)
	{
		boolean result = true;
		try {
			String prjDir = ProjectConfiguration.PathLineLtoRankTestingFearture+"\\"+project;
			File directory = new File(prjDir);
			if( !directory.exists() )
				directory.mkdir(); //先创建目录,目录下将存放各版本的特征文件，用于测试。
			//先读取该对象的版本总数。
			IFaultFile ffiAgent = AffiliatedFactory.createFaultFileObject(project);
			if( !ffiAgent.readFaultFile() )
				return false;
			int vers = ffiAgent.getVerNo();
			for( int v=1;v<=vers; v++)
			{
				int bugId = ffiAgent.getBugID(v);
				if( true==ExcludeVersion.isExcludeVer(project,bugId) )
					continue; //该版本不参加计算。
				String featureFilename = prjDir+"\\"+project+"_v"+String.valueOf(bugId)+".testing";
				File file = new File(featureFilename);
				if (file.isFile() && file.exists())
					file.delete();
				OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file));
				BufferedWriter bw = new BufferedWriter(writer);
				int projectVerIdQuery = testIdQuery+bugId; //不同版本对应不同的查询ID。
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
	
	//将.profile文件和.complex文件的特征计算后，合并到文件bw
	private static boolean mergeProfileComplexFileWholeStatementFeature(int pvqQid,String project,int bugId,
			BufferedWriter bw,IFaultFile ffiAgent,  int verTh)throws IOException
	{
		//读取.profile文件。
		IProfileFile profileAgent = AffiliatedFactory.createProfileFileObject(project, bugId);
		if( false==profileAgent.readProfileFile() )
		{
			System.out.println("Read file "+project+"_v"+String.valueOf(bugId)+".profile is error.");
			return false;
		}//end of if...
		//读取.complex文件。
		LineComplexFeatureFile lsMetric = new LineComplexFeatureFile(project,bugId);
		boolean lsResult = lsMetric.readComplexFeartureFile();
		if( !lsResult  )
			return false;
		//所有行 将两个特征文件的内容组合后，存入bw代表的文件
		writeWholeStatementFeatureFile(pvqQid,bw,ffiAgent,verTh/*不是bugId*/,profileAgent,lsMetric);
		return true;
	}

	/** 将两个特征文件的内容组合后，存入bw代表的文件
	 * @param idQuery RankLib要求的。
	 * @param bw
	 * @param faultFilenames  缺陷所在的文件
	 * @param faultStats      缺陷语句行号，faultStats与faultFilenames对齐
	 * @param profileAgent     .profile内容。
	 * @param lcffFeats  .complex内容。 ########里面的行号与profileAgent里的行号已经对应。####
	 * @throws IOException
	 */
	private static void writeWholeStatementFeatureFile(int idQuery,BufferedWriter bw,
			IFaultFile ffiAgent,  int verTh,
			IProfileFile profileAgent,
			LineComplexFeatureFile lcffFeats ) throws IOException
	{
		int passed = profileAgent.getPassed();
		int failed = profileAgent.getFailed();

		//所有语句的特征都写入
		List<FileSpectrum> fileSpectra = profileAgent.getSpectrumList();//程序谱
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
		bw.flush(); //写入文件。
	}
	
	//生成RankLib格式的特征文件。分为训练集、验证集和测试集。
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
			writeRankingFeatureFile(trainFile,objectLst); //保存训练文件。
//			String validateFile = ProjectConfiguration.PathLineLtoRankTrainFearture+"\\Fold"+String.valueOf(i+1)+".validate2";
//			List<String> vobjLst = new ArrayList<>();
//			for( int t=4;t<=6;t++ )
//			{
//				String[] projects = projectGroup[(i+t)%10];
//				for( String project: projects )
//					vobjLst.add(project);
//			}
//			writeRankingFeatureValidateFile(validateFile,vobjLst); //保存验证集文件。
		}*/
		//写测试文件。每个project都会成为测试集。
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
	
	/**  分为训练集、验证集和测试集。 
	 * 将objectLst项目的特征找出来，存放到文件filename。
	 * @param featureFilename  保存到该文件名。
	 * @param objectLst 项目名集合
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
			int idQuery = 30000;  //类似于文档号，被查询的项目，RankLib文件要求的查询ID
			for( String objectName : objectLst )
			{
				//与训练集一样，只采集100条语句作为验证集。此种方法对RankLib较为合理。
				writeProjectRankingFeatureFile(idQuery,bw,objectName);
				//与测试集一样，采集所有语句作为验证集。使用LibLinear-ranksvm
				//writeProjectRankingFeatureValidateFile(idQuery,bw,objectName);
				idQuery += 1000; //有一个项目有103个版本，所以设大些。
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