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
	 * flexV4,flexV2,    grepV3,grepV2,grepV4,   gzipV1,gzipV2,  sedV3��sedV5,sedV6,sedV7,  space
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
	    		//����XX_fault.cvs����.fault and .testcase�ļ�������.profile�ļ����������Ƿ���ȷ��
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
	
	//���ԣ�һ��ֻ����һ��SBFL������һ����������metric method��
	private static void evaluateOneAlgorithmOneObjectSBFL()
	{
		//String algorihtmAry = "Jaccard";//Jaccard,Tarantula,Ochiai,Opass
		boolean bStoreFile = true; //�Ƿ�ʵ�������浽�ļ���
		String[] algorihtmAry = SBFLTradTechnique.getAlgorithmNames();
		for( String strAlgorihtm : algorihtmAry )
			EvaluatePerformanceSBFL.evaluateShowOne(objectName,strAlgorihtm,bStoreFile);
	}//end of evaluateOneMetricSBFL

	
	//һ��ֻ����һ��SBFL���������ж�������metric method��
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
			EvaluatePerformanceSBFL.storeExpensePscoreToFile(false,strAlgorihtm); //false=���ۺ�
		}
	}//end of evaluateOneMetricSBFL

	/*һ��ֻ����һ��SBFL���������ж�������metric method��
	 * ��evaluateOneAlgorithmForAllObject��ͬ���˴�ʹ�þۺϹ��ܣ�Siemens,gzip,grep,sed�ȡ�
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
			EvaluatePerformanceSBFL.storeExpensePscoreToFile(true,strAlgorihtm);//true=�ۺ�
		}
	}

	/**
	 *  ��������Ϊ��λ�������ļ���������������ѧϰ��
	 *  1����ȡ��Ҫ�������ļ�          2�����������ļ����������ĸ���������
	 *  3�������������������浽�ļ���
	 */
	private static void generateStatementFeatureWithSoftwareComplex()
	{
		StatementFeatureGeneration.makeFeatureFile();
	}
	
	/**
	 *  ����RankLIB��ʽ�������ļ���
	 *  <line> .=. <target> qid:<qid> <feature>:<value> <feature>:<value> ... <feature>:<value> # <info>
	 */
	private static void  generateRankLibFeatureFileForLtoRank()
	{
		//ѵ�����Ͳ��Լ�
		//RankLibFeatrure.makeRankingFeatureFile(); 
		//ѵ��������֤���Ͳ��Լ�
		RankLibFeatrure.makeRankingFeatureFile2();
		//Ϊ������Ŀ��ÿ���汾����һ��������.train�����ļ���ÿ���汾��Լֻ��100������������
		RankLibFeatrure.makeAllProjectTrainDataset();
	}
	
	
	/**
	 * https://mvnrepository.com/artifact/de.julielab/julielab-ranklib-mallet/1.0.0
	 * ��������⣬�������ҵ��������λ���ܣ������� 31��SBFL+��伶�ĸ��Ӷ� 
	 */
	private static void  evaluateExamUsingRankLib()
	{
		BoostRankUsingRankLib arurl = new BoostRankUsingRankLib();
		arurl.evaluateExperiment();
	}
	
	/**
	 * Large-scale rankSVM:  https://www.csie.ntu.edu.tw/~cjlin/libsvmtools/#large_scale_ranksvm
	 * ��������⣬�������ҵ��������λ���ܣ������� 31��SBFL+��伶�ĸ��Ӷ� 
	 */
	private static void  evaluateExamUsingLibLinearRankSVM()
	{
		LtoRUsingLibLinearRankSVM ltrullrs = new LtoRUsingLibLinearRankSVM();
		ltrullrs.evaluateExperiment();
	}

	/** ���ĳ�汾��.profile�ļ���
	 * @param bugid
	 */
	private static void checkProfileFileOfBugid(int bugid)
	{
		IProfileFile pfiAgent = AffiliatedFactory.createProfileFileObject(objectName, bugid);
		if( !pfiAgent.readProfileFile() )
			return;
		pfiAgent.testMe();
	}
	
	//����XX_fault.cvs����.fault and .testcase�ļ�������.profile�ļ����������Ƿ���ȷ��
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
	 * ���ѡȡѵ��������֤���Ͳ��Լ���
	 */
	private static void randomEvaluateUsingLibLinear()
	{
		//ÿ����Ŀ������һ��������ѵ�������ݣ����á�
		//�ⲿ��ѵ����ֻΪ��RandomProjectEvaluateLibLinear���ã���Ϊ�˼�С������������������С�
		//RankLibFeatrure.makeAllProjectTrainDataset();
		RandomProjectEvaluateLibLinear rpell = new RandomProjectEvaluateLibLinear();
		rpell.evaluateExperiment();
	}
	
	
	/**
	 * ��ӡ����Ŀ����Ϣ��д����Ҫ�õ���
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
			nver = nver-ExcludeVersion.getNumberOfExcludeVer(project);//�۳���İ汾������
			int minExecute = 99999999,maxExecute=0;//�����С��ִ�������Ŀ��
			int minTcs = 99999999,maxTcs = 0;//�����С����������Ŀ
			int minFail = 99999999,maxFail =0;//�����Сʧ�ܲ���������Ŀ
			for( int ver=1;ver<=nver; ver++)
			{
				int bugId = ffiAgent.getBugID(ver);
				if( true==ExcludeVersion.isExcludeVer(project,bugId) )
					continue; //�ð汾���μӼ��㡣
				IProfileFile profileAgent = AffiliatedFactory.createProfileFileObject(project, bugId);//ֻ������汾���ԡ�
				if( false==profileAgent.readProfileFile() )
					break;
				int nexec = profileAgent.getTotalExec();
				//��¼���а汾��С��
				if( nexec<minExecute )
					minExecute = nexec;
				//��¼���а汾�����
				if( nexec>maxExecute )
					maxExecute = nexec;
				int ntcs = profileAgent.getPassed()+profileAgent.getFailed();
				//��¼���а汾��С����������Ŀ
				if( ntcs<minTcs )
					minTcs = ntcs;
				//��¼���а汾������������Ŀ
				if( ntcs>maxTcs )
					maxTcs = ntcs;
				int nFail = profileAgent.getFailed();
				//��¼���а汾��Сʧ�ܲ���������Ŀ
				if( nFail<minFail )
					minFail = nFail;
				//��¼���а汾���ʧ�ܲ���������Ŀ
				if( nFail>maxFail )
					maxFail = nFail;
			}
			System.out.println(project+":   "+"vers="+nver+"  execute lines : "+minExecute+"~"+maxExecute+
					"  testcase : "+minTcs+"~"+maxTcs+"  fails : "+minFail+"~"+maxFail);
		}
	}
}