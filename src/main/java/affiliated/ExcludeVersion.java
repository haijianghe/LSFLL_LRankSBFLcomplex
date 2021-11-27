/**
 * 
 */
package affiliated;

/**
 * @author Administrator
 *
 */
public class ExcludeVersion {
	//下面两行代码可用于测试。
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
			//Time   CSV  JxPath 所有版本符合要求。
			"Lang", "JacksonXml","Collections","JacksonCore",
			"Math","Gson","JSoup",
			"JacksonDatabind","Closure",
//=======================         PairikaOpenCV dataset C++ =================================================										
			"PairikaOpenCV331","PairikaOpenCV340",
//=================         Bears: https://github.com/bears-bugs/bears-benchmark Java =====================										
			"FasterXML"
     };
	//注意：这些数字都是bugid，并非自然数vXX
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
		 * print_tokens 的V4和V6,.C文件相同，错误在.h文件，
		 * 排除。
		 */
		/*
		 * print_tokens2 的V10执行部分测试用例时，发生Segmentation Fault,GCOV无法产生.gcno文件，没有覆盖数据。
		 * 另外，比较文件的方式和SIR matrix_fault的结果也不同。所以排除。
		 */
		/*
		 *  schedule的V1,V5,V6 发生Segmentation Fault,GCOV无法产生.gcno文件，没有覆盖数据。
		 *  并且这些版本都有测试用例比较文件的方式和SIR matrix_fault的结果不同。
		 *  V9发生Segmentation Fault,GCOV无法产生.gcno文件，没有覆盖数据。
		 *   排除这些版本。
		 */
		/*
		 * schedule2的V8发生Segmentation Fault,GCOV无法产生.gcno文件，没有覆盖数据。
		 *  并且该版本有测试用例比较文件的方式和SIR matrix_fault的结果不同。
		 * schedule2的V9没有失败的测试用例。
		 *  排除这些版本。
		 */
		/* replace修改了代码：
		 *    #define NULL0   ===========>  #define TNULL 0
		 *    getline         ===========>  get_line  
		 * replace的V27发生Segmentation Fault,GCOV无法产生.gcno文件，没有覆盖数据。
		 * schedule2的V32没有失败的测试用例。
		 *  排除这些版本。
		 *  V8，V13,V14,V26这四个版本，比较文件的方式和SIR matrix_fault的结果不同。
		 *  这些版本保留，使用我自己的比较结果填入matrix_fault，旧的更名matrix_fault.sir
		 */
		/*
		 * tcas的V38版本有测试用例比较文件的方式和SIR matrix_fault的结果不同。
		 * 原来的matrix_fault里，该版本有0个FAIL，现在我有56个FAIL
		 *  排除该版本。
		 *  另外，V4,V23,V28,V29,V30,V33,V35,V36,V37,V40,V41这些版本，比较文件的方式和SIR matrix_fault的结果不同。
		 *  但它们不同的测试用例结果数很少。
		 *  这些版本保留，使用我自己的比较结果填入matrix_fault，旧的更名matrix_fault.sir
		 */
		/*
		 * tot_info的V21版本故障语句是一个宏定义，牵涉到许多变量，不好制定故障语句的位置。
		 *  排除该版本。
		 *  另外，V11,V15,这些版本，比较文件的方式和SIR matrix_fault的结果不同。
		 *  但它们不同的测试用例结果数很少。
		 *  这些版本保留，使用我自己的比较结果填入matrix_fault，旧的更名matrix_fault.sir
		 */
		/*
		 * space的V1,2,32测试用例全部通过，排除这些版本。
		 *  另外，V34,比较文件的方式和SIR matrix_fault的结果太多不同。排除
		 *  V25,30,35,36,38,这些版本，很可能指针问题，程序奔溃，无法记录gcov。排除这些版本。
		 *  V26，尽管也有check file (noExecutedTimes)的错误，暂且保留。
		 *  v7(2),v9(50),v10(19),v11(21),v16(2),v17(2),v19(19),v20(1),v21(1),v23(1),v28(70),
		 *  它们不同的测试用例结果数相对较少，括号内不同结果的测试用例数。
		 *  这些版本保留，使用我自己的比较结果填入matrix_fault，旧的更名matrix_fault.sir
		 */
		/*
		 * flex(4)的V2版本故障语句和V1是同一个数组组，不能区分。
		 *  排除该版本。
		 *  V3,V4,V9,V10,V11都没有未通过的测试用例，排除这些版本。
		 *  使用我的比较结果覆盖原来的fault-matrix，两者也只有第二个测试用例有少许版本结果不同。
		 */
		/*
		 * flex(2)的  V1,4,8,12,13,17,19,20都没有未通过的测试用例，排除这些版本。
		 *  使用我的比较结果覆盖原来的fault-matrix，两者也只有第6个测试用例有少许版本结果不同。
		 */
		/*
		 * grep(3)的  V4,5,6,7,9,11,13,14,15,17都没有未通过的测试用例，排除这些版本。
		 *  使用我的比较结果覆盖原来的fault-matrix，两者差别：
		 *      V3一个测试用例不同； V13许多测试用例结果不同。
		 */
		/*
		 * grep(2)的  V3,4,5,8都没有未通过的测试用例，排除这些版本。
		 *  使用我的比较结果覆盖原来的fault-matrix，两者完全相同。
		 */
		/*
		 * grep(4)的  V1,3~11都没有未通过的测试用例，排除这些版本。
		 *  使用我的比较结果覆盖原来的fault-matrix，两者完全相同。
		 */
		/*
		 * gzip(1)的  V1,3,6~12都没有未通过的测试用例，排除这些版本。
		 *  v4，我的比较结果与原来的fault-matrix，太多不同，也排除v4.
		 *  使用我的比较结果覆盖原来的fault-matrix，除v5,v15外完全相同。
		 *    v5的tc52, v15的tc38,tc52不同。
		 */
		/*
		 * gzip(2)的  V2,4,5,7都没有未通过的测试用例，排除这些版本。
		 *  使用我的比较结果覆盖原来的fault-matrix，。
		 *    v1的tc38,49,52, v3的tc52不同,v6的tc38,49,52不同。
		 */
		/*
		 * sed(6)的  V2不知道什么原因，评估值超过100%（可能没有列入到可执行语句）。
		 */
		/*
		 * sed(7)的  V2，只有一个失败测试用例，并且tc13,tc17导致没有可执行语句。
		 */

		//!!!!!!!!!!!!!!注意:V10,此处的10并非第10个版本,而是文件夹下的V10(bugid=10)
		/*
		 * Chart 的V10,没有通过的测试用例；V23故障语句不好定位。
		 * 排除。
		 */
		/*
		 * Cli 的V3 V7,没有通过的测试用例；
		 * V19没有实质上地修改代码，改动的地方不会影响代码运行。
		 * 排除。
		 */
		/*
		 * Codec 的V13 V14,没有.profile文件，Defects4j原始程序可能出错；
		 * V17 没有实质上地修改代码，改动的地方不会影响代码运行。
		 * V12 不好定位错误。
		 * 排除。
		 */
		/*
		 * Compress 的22故障语句不好定位。
		 * 排除。
		 */
		/*
		 * Lang 的V57没有通过的测试用例；
		 * V23 V25,V56故障语句不好定位。
		 * 排除。
		 */
		/*
		 * JacksonXml 的V6故障语句不好定位。
		 * 排除。
		 */
		/*
		 * Collections 的V26,27,28故障语句不好定位。
		 * 排除。
		 */
		/*
		 * JacksonCore 的V13故障语句不好定位。
		 * 排除。
		 */
		/*
		 * Math 的V10,12故障语句不好定位。
		 *        V48是break语句前删除了代码。
		 * 排除。
		 */
		/*
		 * Gson 的V14,16,V18,没有.profile文件，Defects4j原始程序可能出错；
		 *          V9故障语句不好定位。
		 * 排除。
		 */
		/*
		 * JSoup 的V71没有.profile文件，Defects4j原始程序可能出错；
		 *          V9,V15,V69,V78,V80,V87故障语句不好定位。
		 * 排除。
		 */
		/*
		 * JacksonDatabind 的V{20,21,23,26,40,72,84,86}故障语句不好定位。
		 * V4,22,36,43,故障语句不好定位
		 * V89,V92静态初始化问题，故障语句不好定位
		 * 排除。
		 */
		/*
		 * Closure 的V{}故障语句不好定位。
		 * 许多bugid的失败测试用例太多，执行时间太长，没有记录它们的谱。
		 * 28,46,148,163,105,119 故障语句不好定位。
		 * 排除。
		 */
		
		/*
		 * PairikaOpenCV331 的V6故障语句不好定位。
		 *   V10和Tumi4的实验结果大不相同。
		 * 排除。
		 */
		/*
		 * PairikaOpenCV340 的V3故障语句在覆盖数据里找不到。
		 *   V8，宏定义错误，不好定位。
		 * 排除。
		 */
		/*Bears: https://github.com/bears-bugs/bears-benchmark */
		/*
		 * FasterXML 的V6失败测试用例与作者的结果相差9个。
		 *   
		 * 排除。
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
	
	//计算该对象需要排除的版本数目。
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
