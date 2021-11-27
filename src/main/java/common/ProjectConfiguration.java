/**
 * 
 */
package common;

/**
 * @author Administrator
 *
 */
public class ProjectConfiguration {
	public static final String RawDatasetDirectory = "F:\\MySBFLRawDataset";   //包含原始.profile和源代码的文件夹。
	public static final String RawDatasetXMLConfigFile = "RawDatasetConfig.xml"; //XML配置文件名，据此读入每个对象的配置信息。
	public static final String SourceCodeSubDir = "version.alt";   //据此查找源代码目录。
	//PathLineCognitiveComplex存放所有对象的软件复杂度文件，以行为单位的复杂度。
	public static final String PathLineComplexFearture = RawDatasetDirectory+"\\SoftwareComplexMetric\\LtoRankWithStatic";
	//存放Learning to Rank需要的以行为单位的特征
	public static final String PathLineLtoRankTrainFearture = RawDatasetDirectory+"\\SoftwareComplexMetric\\LtoRankTrainFeature";
	public static final String PathLineRandomTrainFearture = RawDatasetDirectory+"\\SoftwareComplexMetric\\RandomTrainFeature";
	//每个项目存放到一个子文件夹，用于训练结束后，计算测试集上的性能。
	public static final String PathLineLtoRankTestingFearture = RawDatasetDirectory+"\\SoftwareComplexMetric\\LtoRankTestingFeature";
	//不同的特征组合
	public static final String PathFeartureScreen = RawDatasetDirectory+"\\SoftwareComplexMetric\\feature";
	//实验结果保存目录
	public static final String PathSingleObjectExperiment = "F:\\MySBFLRawDataset\\ExperimentalResult\\LtoRankWithStaticSingle";
	public static final String PathAggregatedExperiment = "F:\\MySBFLRawDataset\\ExperimentalResult\\LtoRankWithStaticAggregated";
	//LibLinear-RankSVM的可执行程序所在目录
	public static final String PathLibLinearRankSVM = "F:\\MySBFLRawDataset\\SoftwareComplexMetric\\liblinear-ranksvm-2.11\\windows";
}

/*
 *	顶层局部变量、参数、属性形成的软件复杂度
	private int nLocalVariable; //顶层局部变量个数。名称相同的不重复计数
	private int nParameter;     //参数个数。 名称相同的不重复计数
	private int nAttribute;     //属性个数。 名称相同的不重复计数

 	操作符号形成的软件复杂度
	private int numLogicOperaters;  //逻辑操作符号个数。 && || !
	private int typeLogicOperaters; //逻辑操作符号种类数。 && || !
	private int numOtherOperaters;  //除逻辑操作外的符号个数。
	private int typeOtherOperaters; //除逻辑操作外的符号种类数。
	
	private int nFuncCall; //一条语句内的函数调用种类数，同一个函数名不重复计数。

*/