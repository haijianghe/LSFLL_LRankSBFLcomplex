/**
 * 
 */
package sbflMetrics;

import java.util.ArrayList;
import java.util.List;

import affiliated.FileSpectrum;
import affiliated.SpectrumStruct;
import softComplexMetric.StatementFeatureStruct;
import softComplexMetric.ClassFileComplexValue;

/**
 * @author HeJiahui
 *两种情况， 评估： 源代码只有单个文件
 *           评估： 源代码有多个文件
 * 
 * 要考虑具有相同可疑度值的故障语句个数对性能的影响。
 * 许多版本都具有多个故障语句，它们都很可能具有相同的可疑度值，特别是数据集Defects4j，Bears等!!!!!!!!!(2021,2,3完成)
 */
public class SBFLperformanceAssess {
	//private int thVer;  //第几个版本，注意不是版本个数verNo。
	private int tcPassed; //测试用例通过数
	private int tcFailed; //测试用例未通过数
	private int execTotal; //该版本代码，可执行语句条数
	private List<FileSpectrum> filSpectrumList; //该版本的多个文件的程序谱
	//注意，faultStatms的值从1开始，并非从0开始。
	private int[] faultStatms;  //该版本的故障语句行号集合。注意：实际上是索引，并非真正的行号。
	/**
	 * 避免产生空的构造函数，导致assembleIndexFromFileStatementList未被调用。
	 */
	private SBFLperformanceAssess()
	{
		tcPassed = 0;
		tcFailed = 0;
		execTotal = 0;
		filSpectrumList = null;
	}
	
	//fls里元素个数就是版本总个数。
	public SBFLperformanceAssess(int pass,int fail,int total,List<FileSpectrum> ssl,
									String[] ffiles,int[] fls)
	{
		tcPassed = pass;
		tcFailed = fail;
		execTotal = total;
		filSpectrumList = ssl;
		//故障语句在整个对象（某版本）可执行语句集合中的索引顺序。
		assembleIndexFromFileStatementList(ffiles,fls);
	}
	
	//该版本的故障文件名集合。特别注意：faultFiles和faultLines的二维顺序保持一致。
	private void assembleIndexFromFileStatementList(String[] ffiles,int[] fls)
	{
		int faults = ffiles.length;
		faultStatms = new int[faults];
		int index = 0; //表示故障语句在整个队列（所有文件的所有行）的顺序
		for( int i=0;i<faults;i++ )
		{ //该版本总共有faults条故障语句
			boolean found = false;
			int statementIndex = 0; //记录当前考察到第几条语句。
			for( FileSpectrum sps : filSpectrumList )
			{
				if( !sps.getClassFilename().equals(ffiles[i]) )
				{
					statementIndex += sps.getTotalExec();
					continue;
				}
				//文件名对得上号，才执行后续操作。
				List<SpectrumStruct> lineCodes = sps.getLineCodes();
				for( SpectrumStruct spectrum : lineCodes )
				{
					statementIndex ++; //从1开始
					if( spectrum.getLineNo()==fls[i] )
					{
						found = true;
						faultStatms[index++] = statementIndex;
						break;
					}
				}//end of for
				if( found )
					break;
			}//end of for...所有文件
		}//end of for( int i=0....
	}
	
	/**
	 * @param strAlgorithm   SBFL 传统算法
	 * @param strMetric  expense,exam,T-score,P-score,...
	 * @return   返回结果。Worst-Best-Mean
	 */
	public WorstBestMean calPerformance(String strAlgorithm,String strMetric)
	{
		//List<Integer> execLineAry = assembleExecLineList();//一维的行号队列。
		//每个文件，包含多个可执行语句，当由可执行语句查找文件名时，需要记住它的文件序号。
		//List<Integer> filenoAry = assembleFilenoList();//一维的文件序号队列。
		int totalExec = execTotal;//可执行语句总数。
		//将来要对suspiciousnessAry里面的值排序，所以，需要记住每个值的初始索引位置。
		List<Integer> indexAry = assembleIndexList(totalExec);//从1 到total的队列，1,2,3,4，...,total。
		List<Double> suspiciousnessAry = new ArrayList<Double>();
		for( FileSpectrum fsItem : filSpectrumList )
		{
			List<SpectrumStruct> lineCodes = fsItem.getLineCodes();
			for( SpectrumStruct spectra : lineCodes )
			{
				int aep = spectra.getAep();
				int aef = spectra.getAef();
				double suspi = calculateSuspicious(strAlgorithm,spectra.getLineNo(),aep,aef);
				suspiciousnessAry.add(suspi);
			}//end of for...spectra
		}//end of for ... ssItem

		/*此处是否排序，不会影响结果。但是注意：多个文件的行号如何制定顺序。
		 * ExpenseScore的方法scoreCalculate计算各种策略的性能时，并未考虑suspiciousnessAry是否有顺序，
		 * 所有这里是否排序不影响最终结果。
		 * @@@@@@@@排序是为了SOS策略，以后的研究不再考虑SOS策略，所以无需排序。@@@@@@
		*/
		//suspiciousnessAry按照从大到小排序，相应地，更改indexAry的顺序。
		sortSuspiciousness(suspiciousnessAry,indexAry);

		Double[] Dsups = suspiciousnessAry.toArray(new Double[totalExec]);
		double[] pSuspicious = new double[totalExec];//suspiciousness of each statement
		for(int i=0;i<totalExec;i++)
			pSuspicious[i] = Dsups[i].doubleValue();
			
		//特别注意： 此时的pStatement并非存储行号，而是某文件某行代码的序号。
		//懒得修改代码，与以前兼容，变量名等都未作修改。
		int[] pStatement = new int[totalExec]; //the pointer of the number of statement
		Integer[] Istat =  indexAry.toArray(new Integer[totalExec]);
		for(int k=0;k<totalExec;k++)
			pStatement[k] = Istat[k].intValue();
			
		//find the most suspiciousness,return Max(Suspicious) and his code line no.
		double[] maxFaultSuspi = {0.0}; //max suspiciousness in fault code line.
		int noMiniLine = 0; //the mini number of Fault statement which suspiciousness is max.
		noMiniLine = getMaxSuspiFaultLine(pSuspicious,pStatement,maxFaultSuspi);
		//注意，faultStatms的值从1开始，并非从0开始。
		IMetricMethod esMetric = MethodStrategyComplexFactory.createMetricMethodObject(strMetric, faultStatms);
		esMetric.calculateWorstBestMean(pSuspicious,pStatement,maxFaultSuspi[0]);
		return esMetric.getWorstBestMeanResult();
	}
	
	//Calculate Suspicious 
	private double calculateSuspicious(String strAlgorithm,int lineno,int aep,int aef)
	{
		ProfileStatement stProfile = new ProfileStatement();
		stProfile.no = lineno; //多余。
		stProfile.Aep = aep;
		stProfile.Aef = aef;
		stProfile.Anp = tcPassed-stProfile.Aep;
		stProfile.Anf = tcFailed-stProfile.Aef;
		//归一化的
		return SBFLTradNormalizeTechnique.zAlgorithmSuspicious(strAlgorithm, stProfile);
	}
	
	/** Check nStatement is or not fault code line.
	 * @param nStatement 待检查的语句，其行号
	 * @return return TRUE: is fault; else is not fault.
	 */
	public boolean isFaultCode(int nStatement)
	{
		boolean bFault = false;
		//judge this is a fault line.
		for( int item : faultStatms )
		{
			if( nStatement==item )
			{
				bFault = true;
				break;
			}
		}
		return bFault;
	}
	
	/** 故障语句可能有多条，找出他们之中可疑度最大值。
	 * find the most suspiciousness,return Max(Suspicious) and his code line no.
	 * if two fault have same Suspicious,then return the mini line no.
	 * @param pSuspicious 各语句依照顺序，可疑度
	 * @param pStatement  语句号，与pSuspicious顺序相同。
	 * @param maxFaultSuspi 最大可疑度值
	 * @return 故障语句里具有最大可疑度值，行号最小的那个。
	 */
	private int getMaxSuspiFaultLine(double[] pSuspicious,int[] pStatement,double[] maxFaultSuspi)
	{
		//get the most suspiciousness
		double maxSuspi = -9999;
		int lineno = 0;
		for ( int i=0;i<pSuspicious.length;i++ )
		{
			if( false==isFaultCode(pStatement[i]) )//this line'code  is not a fault
				continue;
			if ( pSuspicious[i]>maxSuspi )
			{
				maxSuspi = pSuspicious[i];
				lineno = pStatement[i]; //相同可疑度值，pStatement按照从小到大的顺序排列，所以最先的一定行号最小
			}
		}

		maxFaultSuspi[0] = maxSuspi;
		return lineno;
	}
	
	/**  将suspiciousnessAry里的值从大到小排列，并记录下新序列每个值的初始位置，便于显示它属于哪个文件的哪条语句。
	 * @param suspiciousnessAry 可疑度值序列
	 * @param indexAry  suspiciousnessAry的初始索引顺序
	 */
	private void sortSuspiciousness(List<Double> suspiciousnessAry,List<Integer> indexAry)
	{
		int execs = suspiciousnessAry.size();
		for( int i=0;i<execs;i++ )
		{
			double maxv = suspiciousnessAry.get(i);//假设为最大值
			int index = i; //记录最大值的索引位置
			for( int j=i+1;j<execs;j++ )
			{
				double thisv = suspiciousnessAry.get(j);
				if( thisv>maxv )
				{
					maxv = thisv;
					index = j;
				}
			}//end of for ...j
			if( i!=index )  //假设的最大值被其它超过。
			{
				double tmpv = suspiciousnessAry.get(i);
				//交换可疑度值
				suspiciousnessAry.set(i, maxv);
				suspiciousnessAry.set(index, tmpv);
				//交换索引位置。
				int tmpi = indexAry.get(i);
				indexAry.set(i, indexAry.get(index));
				indexAry.set(index, tmpi);
			}
		}//end of for ...i
	}
	
	/**
	 * @param total 总的个数
	 * @return 从1 到total的队列，1,2,3,4，...,total。 存储错误定位可疑度数组的索引
	 */
	private List<Integer> assembleIndexList(int total)
	{
		List<Integer> indexAry = new ArrayList<Integer>();
		for( int i=1;i<=total;i++ )
			indexAry.add(i);
		return indexAry;
	}
	
}
