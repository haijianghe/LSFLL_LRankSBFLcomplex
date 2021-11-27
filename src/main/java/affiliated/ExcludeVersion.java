/**
 * 
 */
package affiliated;

/**
 * @author Administrator
 *
 */
public class ExcludeVersion {
	//�������д�������ڲ��ԡ�
	//private static String[] objectDts={"schedule2","schedule","print_tokens"};
	//private static int[][] verDts = {{9},{1,3},{2,5,6}};
	private static String[] objectDts={
//=======================         SIR dataset C ====================================================			
			"print_tokens","print_tokens2",
            "schedule",   "schedule2",
            "replace","tcas","tot_info",
            "space",
            "flexV4","flexV2",
            "grepV3","grepV2","grepV4",
            "gzipV1","gzipV2",
            "sedV6","sedV7",
//=======================         Defects4j dataset Java ====================================================            
			"Chart",	"Cli", "Codec","Compress",
			//Time   CSV  JxPath ���а汾����Ҫ��
			"Lang", "JacksonXml","Collections","JacksonCore",
			"Math","Gson","JSoup",
			"JacksonDatabind","Closure",
//=======================         PairikaOpenCV dataset C++ =================================================										
			"PairikaOpenCV331","PairikaOpenCV340",
//=================         Bears: https://github.com/bears-bugs/bears-benchmark Java =====================										
			"FasterXML"
     };
	//ע�⣺��Щ���ֶ���bugid��������Ȼ��vXX
	private static int[][] verDts = { 
//--------------------       SIR dataset C    			
			 {4,6},{10},
             {1,5,6,9},{8,9,2,3,5,7,10},
             {27,32},{38,5,27,34},{21},
             {1,2,25,30,32,34,35,36,38},
              {1,2,3,4,9,10,11}, {1,4,8,12,13,17,19,20,3}, //  "flexV4","flexV2",
              {4,5,6,7,9,11,13,14,15,17},{3,4,5,8},{1,3,4,5,6,7,8,9,10,11},//"grepV3","grepV2","grepV4",
              {1,3,4,6,7,8,9,10,11,12},{2,4,5,7}, //"gzipV1","gzipV2",
              {2},{2},
 //--------------------   Defects4j            
			{10,23}, {3,7,19}, {12,13,14,17},  {22},
								{23,25,56,57},  {6}, {26,27,28},{13},
								{10,12,48},{9,14,16,18},{9,15,69,71,78,80,87},
								{4,22,36,43,20,21,23,26,40,72,84,86,89,92},
			{2,4,6,7,10,11,17,18,19,21,22,26,27,31,32,33,37,38,39,41,42,43,44,47,48,51,52,54,56,57,59,60,61,62,63,64,65,66,68,69,
				70,71,73,75,77,79,80,81,82,84,86,90,92,93,94,96,104,109,110,112,113,115,116,117,118,120,122,123,125,128,129,131,
				133,135,137,140,141,142,144,145,146,149,152,153,154,155,157,158,160,162,28,46,148,163,105,119},
						/*PairikaOpenCV*/
								{6,10},{3,8},
						/*Bears: https://github.com/bears-bugs/bears-benchmark */
								{6}
								};
	
	private void promptTips()
	{
		/*
		 * print_tokens ��V4��V6,.C�ļ���ͬ��������.h�ļ���
		 * �ų���
		 */
		/*
		 * print_tokens2 ��V10ִ�в��ֲ�������ʱ������Segmentation Fault,GCOV�޷�����.gcno�ļ���û�и������ݡ�
		 * ���⣬�Ƚ��ļ��ķ�ʽ��SIR matrix_fault�Ľ��Ҳ��ͬ�������ų���
		 */
		/*
		 *  schedule��V1,V5,V6 ����Segmentation Fault,GCOV�޷�����.gcno�ļ���û�и������ݡ�
		 *  ������Щ�汾���в��������Ƚ��ļ��ķ�ʽ��SIR matrix_fault�Ľ����ͬ��
		 *  V9����Segmentation Fault,GCOV�޷�����.gcno�ļ���û�и������ݡ�
		 *   �ų���Щ�汾��
		 */
		/*
		 * schedule2��V8����Segmentation Fault,GCOV�޷�����.gcno�ļ���û�и������ݡ�
		 *  ���Ҹð汾�в��������Ƚ��ļ��ķ�ʽ��SIR matrix_fault�Ľ����ͬ��
		 * schedule2��V9û��ʧ�ܵĲ���������
		 *  �ų���Щ�汾��
		 */
		/* replace�޸��˴��룺
		 *    #define NULL0   ===========>  #define TNULL 0
		 *    getline         ===========>  get_line  
		 * replace��V27����Segmentation Fault,GCOV�޷�����.gcno�ļ���û�и������ݡ�
		 * schedule2��V32û��ʧ�ܵĲ���������
		 *  �ų���Щ�汾��
		 *  V8��V13,V14,V26���ĸ��汾���Ƚ��ļ��ķ�ʽ��SIR matrix_fault�Ľ����ͬ��
		 *  ��Щ�汾������ʹ�����Լ��ıȽϽ������matrix_fault���ɵĸ���matrix_fault.sir
		 */
		/*
		 * tcas��V38�汾�в��������Ƚ��ļ��ķ�ʽ��SIR matrix_fault�Ľ����ͬ��
		 * ԭ����matrix_fault��ð汾��0��FAIL����������56��FAIL
		 *  �ų��ð汾��
		 *  ���⣬V4,V23,V28,V29,V30,V33,V35,V36,V37,V40,V41��Щ�汾���Ƚ��ļ��ķ�ʽ��SIR matrix_fault�Ľ����ͬ��
		 *  �����ǲ�ͬ�Ĳ���������������١�
		 *  ��Щ�汾������ʹ�����Լ��ıȽϽ������matrix_fault���ɵĸ���matrix_fault.sir
		 */
		/*
		 * tot_info��V21�汾���������һ���궨�壬ǣ�浽�������������ƶ���������λ�á�
		 *  �ų��ð汾��
		 *  ���⣬V11,V15,��Щ�汾���Ƚ��ļ��ķ�ʽ��SIR matrix_fault�Ľ����ͬ��
		 *  �����ǲ�ͬ�Ĳ���������������١�
		 *  ��Щ�汾������ʹ�����Լ��ıȽϽ������matrix_fault���ɵĸ���matrix_fault.sir
		 */
		/*
		 * space��V1,2,32��������ȫ��ͨ�����ų���Щ�汾��
		 *  ���⣬V34,�Ƚ��ļ��ķ�ʽ��SIR matrix_fault�Ľ��̫�಻ͬ���ų�
		 *  V25,30,35,36,38,��Щ�汾���ܿ���ָ�����⣬���������޷���¼gcov���ų���Щ�汾��
		 *  V26������Ҳ��check file (noExecutedTimes)�Ĵ������ұ�����
		 *  v7(2),v9(50),v10(19),v11(21),v16(2),v17(2),v19(19),v20(1),v21(1),v23(1),v28(70),
		 *  ���ǲ�ͬ�Ĳ��������������Խ��٣������ڲ�ͬ����Ĳ�����������
		 *  ��Щ�汾������ʹ�����Լ��ıȽϽ������matrix_fault���ɵĸ���matrix_fault.sir
		 */
		/*
		 * flex(4)��V2�汾��������V1��ͬһ�������飬�������֡�
		 *  �ų��ð汾��
		 *  V3,V4,V9,V10,V11��û��δͨ���Ĳ����������ų���Щ�汾��
		 *  ʹ���ҵıȽϽ������ԭ����fault-matrix������Ҳֻ�еڶ�����������������汾�����ͬ��
		 */
		/*
		 * flex(2)��  V1,4,8,12,13,17,19,20��û��δͨ���Ĳ����������ų���Щ�汾��
		 *  ʹ���ҵıȽϽ������ԭ����fault-matrix������Ҳֻ�е�6����������������汾�����ͬ��
		 */
		/*
		 * grep(3)��  V4,5,6,7,9,11,13,14,15,17��û��δͨ���Ĳ����������ų���Щ�汾��
		 *  ʹ���ҵıȽϽ������ԭ����fault-matrix�����߲��
		 *      V3һ������������ͬ�� V13���������������ͬ��
		 */
		/*
		 * grep(2)��  V3,4,5,8��û��δͨ���Ĳ����������ų���Щ�汾��
		 *  ʹ���ҵıȽϽ������ԭ����fault-matrix��������ȫ��ͬ��
		 */
		/*
		 * grep(4)��  V1,3~11��û��δͨ���Ĳ����������ų���Щ�汾��
		 *  ʹ���ҵıȽϽ������ԭ����fault-matrix��������ȫ��ͬ��
		 */
		/*
		 * gzip(1)��  V1,3,6~12��û��δͨ���Ĳ����������ų���Щ�汾��
		 *  v4���ҵıȽϽ����ԭ����fault-matrix��̫�಻ͬ��Ҳ�ų�v4.
		 *  ʹ���ҵıȽϽ������ԭ����fault-matrix����v5,v15����ȫ��ͬ��
		 *    v5��tc52, v15��tc38,tc52��ͬ��
		 */
		/*
		 * gzip(2)��  V2,4,5,7��û��δͨ���Ĳ����������ų���Щ�汾��
		 *  ʹ���ҵıȽϽ������ԭ����fault-matrix����
		 *    v1��tc38,49,52, v3��tc52��ͬ,v6��tc38,49,52��ͬ��
		 */
		/*
		 * sed(6)��  V2��֪��ʲôԭ������ֵ����100%������û�����뵽��ִ����䣩��
		 */
		/*
		 * sed(7)��  V2��ֻ��һ��ʧ�ܲ�������������tc13,tc17����û�п�ִ����䡣
		 */

		//!!!!!!!!!!!!!!ע��:V10,�˴���10���ǵ�10���汾,�����ļ����µ�V10(bugid=10)
		/*
		 * Chart ��V10,û��ͨ���Ĳ���������V23������䲻�ö�λ��
		 * �ų���
		 */
		/*
		 * Cli ��V3 V7,û��ͨ���Ĳ���������
		 * V19û��ʵ���ϵ��޸Ĵ��룬�Ķ��ĵط�����Ӱ��������С�
		 * �ų���
		 */
		/*
		 * Codec ��V13 V14,û��.profile�ļ���Defects4jԭʼ������ܳ���
		 * V17 û��ʵ���ϵ��޸Ĵ��룬�Ķ��ĵط�����Ӱ��������С�
		 * V12 ���ö�λ����
		 * �ų���
		 */
		/*
		 * Compress ��22������䲻�ö�λ��
		 * �ų���
		 */
		/*
		 * Lang ��V57û��ͨ���Ĳ���������
		 * V23 V25,V56������䲻�ö�λ��
		 * �ų���
		 */
		/*
		 * JacksonXml ��V6������䲻�ö�λ��
		 * �ų���
		 */
		/*
		 * Collections ��V26,27,28������䲻�ö�λ��
		 * �ų���
		 */
		/*
		 * JacksonCore ��V13������䲻�ö�λ��
		 * �ų���
		 */
		/*
		 * Math ��V10,12������䲻�ö�λ��
		 *        V48��break���ǰɾ���˴��롣
		 * �ų���
		 */
		/*
		 * Gson ��V14,16,V18,û��.profile�ļ���Defects4jԭʼ������ܳ���
		 *          V9������䲻�ö�λ��
		 * �ų���
		 */
		/*
		 * JSoup ��V71û��.profile�ļ���Defects4jԭʼ������ܳ���
		 *          V9,V15,V69,V78,V80,V87������䲻�ö�λ��
		 * �ų���
		 */
		/*
		 * JacksonDatabind ��V{20,21,23,26,40,72,84,86}������䲻�ö�λ��
		 * V4,22,36,43,������䲻�ö�λ
		 * V89,V92��̬��ʼ�����⣬������䲻�ö�λ
		 * �ų���
		 */
		/*
		 * Closure ��V{}������䲻�ö�λ��
		 * ���bugid��ʧ�ܲ�������̫�ִ࣬��ʱ��̫����û�м�¼���ǵ��ס�
		 * 28,46,148,163,105,119 ������䲻�ö�λ��
		 * �ų���
		 */
		
		/*
		 * PairikaOpenCV331 ��V6������䲻�ö�λ��
		 *   V10��Tumi4��ʵ��������ͬ��
		 * �ų���
		 */
		/*
		 * PairikaOpenCV340 ��V3��������ڸ����������Ҳ�����
		 *   V8���궨����󣬲��ö�λ��
		 * �ų���
		 */
		/*Bears: https://github.com/bears-bugs/bears-benchmark */
		/*
		 * FasterXML ��V6ʧ�ܲ������������ߵĽ�����9����
		 *   
		 * �ų���
		 */
	}
	
	/**
	 * @param objectName dataset object name
	 * @param bugid : bug id
	 * @return true: this version will exclude.
	 */
	public static boolean isExcludeVer(String objectName,int bugid)
	{
		boolean result = false;
		int index = 0;
		for( String strObject : objectDts )
		{
			index++;
			if( false==strObject.equalsIgnoreCase(objectName))
				continue;
			for( int vItem : verDts[index-1] )
			{
				if( vItem==bugid )
				{
					result = true;
					break;
				}
			}
		}//end of for(String
		return result;
	}
	
	//����ö�����Ҫ�ų��İ汾��Ŀ��
	public static int getNumberOfExcludeVer(String objectName)
	{
		boolean result = false;
		int index = 0;
		for( String strObject : objectDts )
		{
			if( true==strObject.equalsIgnoreCase(objectName))
			{
				result = true;
				break;
			}
			index++;
		}//end of for(String
		if( true==result )
			return verDts[index].length;
		else
			return 0;
	}
}
