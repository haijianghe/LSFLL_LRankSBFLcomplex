/**
 * 
 */
package common;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import affiliated.AffiliatedFactory;
import affiliated.ExcludeVersion;
import affiliated.IFaultFile;
import affiliated.IProfileFile;
import libLinear.LtoRUsingLibLinearRankSVM;
import libLinear.RandomProjectEvaluateLibLinear;
import ranking.BoostRankUsingRankLib;
import ranking.RankLibFeatrure;
import sbflMetrics.PbugverScore;
import sbflMetrics.SBFLTradTechnique;
import sbflMetrics.SBFLperformanceAssess;
import sbflMetrics.WorstBestMean;
import softComplexMetric.LineComplexFeatureFile;


/**
 * @author Administrator
 *
 */
public class MainProcessLRankSBFL {
	/*SIRObjectC:
	 * flexV4,flexV2,    grepV3,grepV2,grepV4,   gzipV1,gzipV2,  sedV3，sedV5,sedV6,sedV7,  space
	 * print_tokens  print_tokens2  schedule schedule2 replace tcas tot_info
	*/ 
	/*Defects4j: 
	 * Chart Cli Closure Codec Collections Compress Csv Gson
	 *JacksonCore  JacksonDatabind  JacksonXml  Jsoup  JxPath  
	 *Lang  Math  Time
	 */
	/*
	 * PairikaOpenCV331  PairikaOpenCV340
	 */
	/*
	 * FasterXML traccar  AutomatedCar
	 */
	private static String objectName = "JacksonXml";//sedV7 Chart

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		if( !XMLConfigFile.parseFile() )
		{
			System.out.println("Read XML config file is error.");
			return;
		}
		// TODO Auto-generated method stub
		System.out.println("Learning to Rank for Spectrum based Fault Localization with Software Complex Metrics. ");
		System.out.println("1, evaluate (one algorithm,one object ) SBFL performance of profile dataset. ");
		System.out.println("2, evaluate (one algorithm,all object ) profile dataset.");
		System.out.println("3, aggregative evaluate (one algorithm,all object ) profile dataset.");
		System.out.println("5, calculate statement complex metric feature and store to file.");
		System.out.println("7, make learing to rank feature file at RankLIB.");
		System.out.println("9, Evaluate Exam using RankLIB.");
		System.out.println("@@@   10, Evaluate Exam using LibLinear-RankSVM.");
		
		System.out.println("###             20, Random evaluate using LibLinear-RankSVM. ");
		System.out.println("65, check  .profile file of v(xx). ");
		System.out.println("66, check .fault,.testcase,_falut.csv, .profile file. ");
		
		System.out.println("76, print information of all projects. ");
		System.out.println("\r\n Others, exit............ .");
		System.out.println("OperatorInterface:      Please key your choice.");
		Scanner sc=new Scanner(System.in);
		int choice = sc.nextInt();
		sc.close();
		switch( choice )
		{
			case 1:
				evaluateOneAlgorithmOneObjectSBFL();
				break;
			case 2:
				evaluateOneAlgorithmForAllObject();
				break;
			case 3:
				 aggregativeEvaluateOneAlgorithmForAllObject();
				break;	
			case 5:
				generateStatementFeatureWithSoftwareComplex();
				break;	
			case 7:
				generateRankLibFeatureFileForLtoRank();
				break;
			case 9:
				evaluateExamUsingRankLib();
				break;
			case 10:
				evaluateExamUsingLibLinearRankSVM();
				break;
			case 20:
				randomEvaluateUsingLibLinear();
				break;
			case 65:
				checkProfileFileOfBugid(7);
				break;
			case 66:
	    		//读入XX_fault.cvs或者.fault and .testcase文件，读入.profile文件，检查读入是否正确。
				checkFaultTestcaseProfileFile();
	    		break;	
			case 76:
				//TestMyCode.CheckJavaParseFile();
				//TestMyCode.CheckCppParseFile();
				//TestMyCode.CheckMainParseFile();
				printInformationOfAllProjects();
				break;
			default:
				break;
		}//end of switch
		System.out.println("The task about (####"+objectName+"####)is over.");
	}
	
	//测试，一次只计算一个SBFL技术，一个对象，两种metric method。
	private static void evaluateOneAlgorithmOneObjectSBFL()
	{
		//String algorihtmAry = "Jaccard";//Jaccard,Tarantula,Ochiai,Opass
		boolean bStoreFile = true; //是否将实验结果保存到文件。
		String[] algorihtmAry = SBFLTradTechnique.getAlgorithmNames();
		for( String strAlgorihtm : algorihtmAry )
			EvaluatePerformanceSBFL.evaluateShowOne(objectName,strAlgorihtm,bStoreFile);
	}//end of evaluateOneMetricSBFL

	
	//一次只计算一个SBFL技术，所有对象，两种metric method。
	private static void evaluateOneAlgorithmForAllObject()
	{
		//"Optimal","Opass","Kulczynski2","Wong3","Tarantula","Jaccard","Ochiai","RusselRao"
		//String algorihtmAry = "Ochiai";//Jaccard,Tarantula,Ochiai,Opass
		EvaluatePerformanceSBFL.DonotShowMe();
		List<String> allObjectNames = XMLConfigFile.getAllObjectNames();
		//String[] algorihtmAry = SBFLTradTechnique.getAlgorithmNames();
		String[] algorihtmAry = {"Optimal"};
		for( String strAlgorihtm : algorihtmAry )
		{
			for( String project : allObjectNames )
				EvaluatePerformanceSBFL.evaluateShowOne(project,strAlgorihtm,false);
			EvaluatePerformanceSBFL.storeExpensePscoreToFile(false,strAlgorihtm); //false=不聚合
		}
	}//end of evaluateOneMetricSBFL

	/*一次只计算一个SBFL技术，所有对象，两种metric method。
	 * 与evaluateOneAlgorithmForAllObject不同，此处使用聚合功能，Siemens,gzip,grep,sed等。
	 */
	private static void aggregativeEvaluateOneAlgorithmForAllObject()
	{
		//"Optimal","Opass","Kulczynski2","Wong3","Tarantula","Jaccard","Ochiai","RusselRao","Ample","GP2","GP13"
		//String strAlgorihtm = "Zoltar";//Jaccard,Tarantula,Ochiai,Opass,GP13
		EvaluatePerformanceSBFL.DonotShowMe();
		List<String> allObjectNames = XMLConfigFile.getAllObjectNames();
		String[] algorihtmAry = SBFLTradTechnique.getAlgorithmNames();
		for( String strAlgorihtm : algorihtmAry )
		{
			for( String project : allObjectNames )
				EvaluatePerformanceSBFL.evaluateShowOne(project,strAlgorihtm,false);
			EvaluatePerformanceSBFL.aggregateSIRProjectSiemensUnix();
			EvaluatePerformanceSBFL.storeExpensePscoreToFile(true,strAlgorihtm);//true=聚合
		}
	}

	/**
	 *  产生以行为单位的特征文件，将来用作排序学习。
	 *  1，获取需要解析的文件          2，解析所有文件，计算语句的各项特征。
	 *  3，将各条语句的特征保存到文件。
	 */
	private static void generateStatementFeatureWithSoftwareComplex()
	{
		StatementFeatureGeneration.makeFeatureFile();
	}
	
	/**
	 *  产生RankLIB格式的特征文件。
	 *  <line> .=. <target> qid:<qid> <feature>:<value> <feature>:<value> ... <feature>:<value> # <info>
	 */
	private static void  generateRankLibFeatureFileForLtoRank()
	{
		//训练集和测试集
		//RankLibFeatrure.makeRankingFeatureFile(); 
		//训练集、验证集和测试集
		RankLibFeatrure.makeRankingFeatureFile2();
		//为所有项目的每个版本产生一个单独的.train特征文件，每个版本大约只有100个特征向量。
		RankLibFeatrure.makeAllProjectTrainDataset();
	}
	
	
	/**
	 * https://mvnrepository.com/artifact/de.julielab/julielab-ranklib-mallet/1.0.0
	 * 调用这个库，来评估我的软件错误定位性能，特征： 31个SBFL+语句级的复杂度 
	 */
	private static void  evaluateExamUsingRankLib()
	{
		BoostRankUsingRankLib arurl = new BoostRankUsingRankLib();
		arurl.evaluateExperiment();
	}
	
	/**
	 * Large-scale rankSVM:  https://www.csie.ntu.edu.tw/~cjlin/libsvmtools/#large_scale_ranksvm
	 * 调用这个库，来评估我的软件错误定位性能，特征： 31个SBFL+语句级的复杂度 
	 */
	private static void  evaluateExamUsingLibLinearRankSVM()
	{
		LtoRUsingLibLinearRankSVM ltrullrs = new LtoRUsingLibLinearRankSVM();
		ltrullrs.evaluateExperiment();
	}

	/** 检查某版本的.profile文件。
	 * @param bugid
	 */
	private static void checkProfileFileOfBugid(int bugid)
	{
		IProfileFile pfiAgent = AffiliatedFactory.createProfileFileObject(objectName, bugid);
		if( !pfiAgent.readProfileFile() )
			return;
		pfiAgent.testMe();
	}
	
	//读入XX_fault.cvs或者.fault and .testcase文件，读入.profile文件，检查读入是否正确。
	private static void checkFaultTestcaseProfileFile()
	{
		IFaultFile ffiAgent = AffiliatedFactory.createFaultFileObject(objectName);
		if( !ffiAgent.readFaultFile() )
			return;
		System.out.println("readFaultFile is ok.");
		ffiAgent.testMe();
		if( !ffiAgent.checkTestcasePassedFailed() )
			return;
		System.out.println("checkTestcasePassedFailed is ok.");
		if( ffiAgent.checkProfileAndFaultFile() )
			System.out.println("checkProfileAndFaultFile is ok.");
	}

	/**
	 * 随机选取训练集、验证集和测试集。
	 */
	private static void randomEvaluateUsingLibLinear()
	{
		//每个项目，产生一个独立的训练集数据，备用。
		//这部分训练集只为类RandomProjectEvaluateLibLinear所用，但为了减小代码量，放在这个类中。
		//RankLibFeatrure.makeAllProjectTrainDataset();
		RandomProjectEvaluateLibLinear rpell = new RandomProjectEvaluateLibLinear();
		rpell.evaluateExperiment();
	}
	
	
	/**
	 * 打印各项目的信息，写文章要用到。
	 */
	private static void printInformationOfAllProjects()
	{
		List<String> projectNames = XMLConfigFile.getAllObjectNames();
		for( String project : projectNames )
		{
			IFaultFile ffiAgent = AffiliatedFactory.createFaultFileObject(project);
			if( false==ffiAgent.readFaultFile() )
				continue;
			int nver = ffiAgent.getVerNo();
			nver = nver-ExcludeVersion.getNumberOfExcludeVer(project);//扣除后的版本总数。
			int minExecute = 99999999,maxExecute=0;//最大最小可执行语句数目。
			int minTcs = 99999999,maxTcs = 0;//最大最小测试用例数目
			int minFail = 99999999,maxFail =0;//最大最小失败测试用例数目
			for( int ver=1;ver<=nver; ver++)
			{
				int bugId = ffiAgent.getBugID(ver);
				if( true==ExcludeVersion.isExcludeVer(project,bugId) )
					continue; //该版本不参加计算。
				IProfileFile profileAgent = AffiliatedFactory.createProfileFileObject(project, bugId);//只能逐个版本测试。
				if( false==profileAgent.readProfileFile() )
					break;
				int nexec = profileAgent.getTotalExec();
				//记录所有版本最小行
				if( nexec<minExecute )
					minExecute = nexec;
				//记录所有版本最大行
				if( nexec>maxExecute )
					maxExecute = nexec;
				int ntcs = profileAgent.getPassed()+profileAgent.getFailed();
				//记录所有版本最小测试用例数目
				if( ntcs<minTcs )
					minTcs = ntcs;
				//记录所有版本最大测试用例数目
				if( ntcs>maxTcs )
					maxTcs = ntcs;
				int nFail = profileAgent.getFailed();
				//记录所有版本最小失败测试用例数目
				if( nFail<minFail )
					minFail = nFail;
				//记录所有版本最大失败测试用例数目
				if( nFail>maxFail )
					maxFail = nFail;
			}
			System.out.println(project+":   "+"vers="+nver+"  execute lines : "+minExecute+"~"+maxExecute+
					"  testcase : "+minTcs+"~"+maxTcs+"  fails : "+minFail+"~"+maxFail);
		}
	}
}