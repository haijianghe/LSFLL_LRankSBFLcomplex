/**
 * 
 */
package common;

/**
 * @author Administrator
 *
 */
public class ProjectConfiguration {
	public static final String RawDatasetDirectory = "F:\\MySBFLRawDataset";   //����ԭʼ.profile��Դ������ļ��С�
	public static final String RawDatasetXMLConfigFile = "RawDatasetConfig.xml"; //XML�����ļ������ݴ˶���ÿ�������������Ϣ��
	public static final String SourceCodeSubDir = "version.alt";   //�ݴ˲���Դ����Ŀ¼��
	//PathLineCognitiveComplex������ж����������Ӷ��ļ�������Ϊ��λ�ĸ��Ӷȡ�
	public static final String PathLineComplexFearture = RawDatasetDirectory+"\\SoftwareComplexMetric\\LtoRankWithStatic";
	//���Learning to Rank��Ҫ������Ϊ��λ������
	public static final String PathLineLtoRankTrainFearture = RawDatasetDirectory+"\\SoftwareComplexMetric\\LtoRankTrainFeature";
	public static final String PathLineRandomTrainFearture = RawDatasetDirectory+"\\SoftwareComplexMetric\\RandomTrainFeature";
	//ÿ����Ŀ��ŵ�һ�����ļ��У�����ѵ�������󣬼�����Լ��ϵ����ܡ�
	public static final String PathLineLtoRankTestingFearture = RawDatasetDirectory+"\\SoftwareComplexMetric\\LtoRankTestingFeature";
	//��ͬ���������
	public static final String PathFeartureScreen = RawDatasetDirectory+"\\SoftwareComplexMetric\\feature";
	//ʵ��������Ŀ¼
	public static final String PathSingleObjectExperiment = "F:\\MySBFLRawDataset\\ExperimentalResult\\LtoRankWithStaticSingle";
	public static final String PathAggregatedExperiment = "F:\\MySBFLRawDataset\\ExperimentalResult\\LtoRankWithStaticAggregated";
	//LibLinear-RankSVM�Ŀ�ִ�г�������Ŀ¼
	public static final String PathLibLinearRankSVM = "F:\\MySBFLRawDataset\\SoftwareComplexMetric\\liblinear-ranksvm-2.11\\windows";
}

/*
 *	����ֲ������������������γɵ�������Ӷ�
	private int nLocalVariable; //����ֲ�����������������ͬ�Ĳ��ظ�����
	private int nParameter;     //���������� ������ͬ�Ĳ��ظ�����
	private int nAttribute;     //���Ը����� ������ͬ�Ĳ��ظ�����

 	���������γɵ�������Ӷ�
	private int numLogicOperaters;  //�߼��������Ÿ����� && || !
	private int typeLogicOperaters; //�߼����������������� && || !
	private int numOtherOperaters;  //���߼�������ķ��Ÿ�����
	private int typeOtherOperaters; //���߼�������ķ�����������
	
	private int nFuncCall; //һ������ڵĺ���������������ͬһ�����������ظ�������

*/