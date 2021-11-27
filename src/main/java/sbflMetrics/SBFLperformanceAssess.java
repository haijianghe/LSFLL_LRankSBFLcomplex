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
 *��������� ������ Դ����ֻ�е����ļ�
 *           ������ Դ�����ж���ļ�
 * 
 * Ҫ���Ǿ�����ͬ���ɶ�ֵ�Ĺ��������������ܵ�Ӱ�졣
 * ���汾�����ж��������䣬���Ƕ��ܿ��ܾ�����ͬ�Ŀ��ɶ�ֵ���ر������ݼ�Defects4j��Bears��!!!!!!!!!(2021,2,3���)
 */
public class SBFLperformanceAssess {
	//private int thVer;  //�ڼ����汾��ע�ⲻ�ǰ汾����verNo��
	private int tcPassed; //��������ͨ����
	private int tcFailed; //��������δͨ����
	private int execTotal; //�ð汾���룬��ִ���������
	private List<FileSpectrum> filSpectrumList; //�ð汾�Ķ���ļ��ĳ�����
	//ע�⣬faultStatms��ֵ��1��ʼ�����Ǵ�0��ʼ��
	private int[] faultStatms;  //�ð汾�Ĺ�������кż��ϡ�ע�⣺ʵ�����������������������кš�
	/**
	 * ��������յĹ��캯��������assembleIndexFromFileStatementListδ�����á�
	 */
	private SBFLperformanceAssess()
	{
		tcPassed = 0;
		tcFailed = 0;
		execTotal = 0;
		filSpectrumList = null;
	}
	
	//fls��Ԫ�ظ������ǰ汾�ܸ�����
	public SBFLperformanceAssess(int pass,int fail,int total,List<FileSpectrum> ssl,
									String[] ffiles,int[] fls)
	{
		tcPassed = pass;
		tcFailed = fail;
		execTotal = total;
		filSpectrumList = ssl;
		//�����������������ĳ�汾����ִ����伯���е�����˳��
		assembleIndexFromFileStatementList(ffiles,fls);
	}
	
	//�ð汾�Ĺ����ļ������ϡ��ر�ע�⣺faultFiles��faultLines�Ķ�ά˳�򱣳�һ�¡�
	private void assembleIndexFromFileStatementList(String[] ffiles,int[] fls)
	{
		int faults = ffiles.length;
		faultStatms = new int[faults];
		int index = 0; //��ʾ����������������У������ļ��������У���˳��
		for( int i=0;i<faults;i++ )
		{ //�ð汾�ܹ���faults���������
			boolean found = false;
			int statementIndex = 0; //��¼��ǰ���쵽�ڼ�����䡣
			for( FileSpectrum sps : filSpectrumList )
			{
				if( !sps.getClassFilename().equals(ffiles[i]) )
				{
					statementIndex += sps.getTotalExec();
					continue;
				}
				//�ļ����Ե��Ϻţ���ִ�к���������
				List<SpectrumStruct> lineCodes = sps.getLineCodes();
				for( SpectrumStruct spectrum : lineCodes )
				{
					statementIndex ++; //��1��ʼ
					if( spectrum.getLineNo()==fls[i] )
					{
						found = true;
						faultStatms[index++] = statementIndex;
						break;
					}
				}//end of for
				if( found )
					break;
			}//end of for...�����ļ�
		}//end of for( int i=0....
	}
	
	/**
	 * @param strAlgorithm   SBFL ��ͳ�㷨
	 * @param strMetric  expense,exam,T-score,P-score,...
	 * @return   ���ؽ����Worst-Best-Mean
	 */
	public WorstBestMean calPerformance(String strAlgorithm,String strMetric)
	{
		//List<Integer> execLineAry = assembleExecLineList();//һά���кŶ��С�
		//ÿ���ļ������������ִ����䣬���ɿ�ִ���������ļ���ʱ����Ҫ��ס�����ļ���š�
		//List<Integer> filenoAry = assembleFilenoList();//һά���ļ���Ŷ��С�
		int totalExec = execTotal;//��ִ�����������
		//����Ҫ��suspiciousnessAry�����ֵ�������ԣ���Ҫ��סÿ��ֵ�ĳ�ʼ����λ�á�
		List<Integer> indexAry = assembleIndexList(totalExec);//��1 ��total�Ķ��У�1,2,3,4��...,total��
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

		/*�˴��Ƿ����򣬲���Ӱ����������ע�⣺����ļ����к�����ƶ�˳��
		 * ExpenseScore�ķ���scoreCalculate������ֲ��Ե�����ʱ����δ����suspiciousnessAry�Ƿ���˳��
		 * ���������Ƿ�����Ӱ�����ս����
		 * @@@@@@@@������Ϊ��SOS���ԣ��Ժ���о����ٿ���SOS���ԣ�������������@@@@@@
		*/
		//suspiciousnessAry���մӴ�С������Ӧ�أ�����indexAry��˳��
		sortSuspiciousness(suspiciousnessAry,indexAry);

		Double[] Dsups = suspiciousnessAry.toArray(new Double[totalExec]);
		double[] pSuspicious = new double[totalExec];//suspiciousness of each statement
		for(int i=0;i<totalExec;i++)
			pSuspicious[i] = Dsups[i].doubleValue();
			
		//�ر�ע�⣺ ��ʱ��pStatement���Ǵ洢�кţ�����ĳ�ļ�ĳ�д������š�
		//�����޸Ĵ��룬����ǰ���ݣ��������ȶ�δ���޸ġ�
		int[] pStatement = new int[totalExec]; //the pointer of the number of statement
		Integer[] Istat =  indexAry.toArray(new Integer[totalExec]);
		for(int k=0;k<totalExec;k++)
			pStatement[k] = Istat[k].intValue();
			
		//find the most suspiciousness,return Max(Suspicious) and his code line no.
		double[] maxFaultSuspi = {0.0}; //max suspiciousness in fault code line.
		int noMiniLine = 0; //the mini number of Fault statement which suspiciousness is max.
		noMiniLine = getMaxSuspiFaultLine(pSuspicious,pStatement,maxFaultSuspi);
		//ע�⣬faultStatms��ֵ��1��ʼ�����Ǵ�0��ʼ��
		IMetricMethod esMetric = MethodStrategyComplexFactory.createMetricMethodObject(strMetric, faultStatms);
		esMetric.calculateWorstBestMean(pSuspicious,pStatement,maxFaultSuspi[0]);
		return esMetric.getWorstBestMeanResult();
	}
	
	//Calculate Suspicious 
	private double calculateSuspicious(String strAlgorithm,int lineno,int aep,int aef)
	{
		ProfileStatement stProfile = new ProfileStatement();
		stProfile.no = lineno; //���ࡣ
		stProfile.Aep = aep;
		stProfile.Aef = aef;
		stProfile.Anp = tcPassed-stProfile.Aep;
		stProfile.Anf = tcFailed-stProfile.Aef;
		//��һ����
		return SBFLTradNormalizeTechnique.zAlgorithmSuspicious(strAlgorithm, stProfile);
	}
	
	/** Check nStatement is or not fault code line.
	 * @param nStatement ��������䣬���к�
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
	
	/** �����������ж������ҳ�����֮�п��ɶ����ֵ��
	 * find the most suspiciousness,return Max(Suspicious) and his code line no.
	 * if two fault have same Suspicious,then return the mini line no.
	 * @param pSuspicious ���������˳�򣬿��ɶ�
	 * @param pStatement  ���ţ���pSuspicious˳����ͬ��
	 * @param maxFaultSuspi �����ɶ�ֵ
	 * @return �����������������ɶ�ֵ���к���С���Ǹ���
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
				lineno = pStatement[i]; //��ͬ���ɶ�ֵ��pStatement���մ�С�����˳�����У��������ȵ�һ���к���С
			}
		}

		maxFaultSuspi[0] = maxSuspi;
		return lineno;
	}
	
	/**  ��suspiciousnessAry���ֵ�Ӵ�С���У�����¼��������ÿ��ֵ�ĳ�ʼλ�ã�������ʾ�������ĸ��ļ���������䡣
	 * @param suspiciousnessAry ���ɶ�ֵ����
	 * @param indexAry  suspiciousnessAry�ĳ�ʼ����˳��
	 */
	private void sortSuspiciousness(List<Double> suspiciousnessAry,List<Integer> indexAry)
	{
		int execs = suspiciousnessAry.size();
		for( int i=0;i<execs;i++ )
		{
			double maxv = suspiciousnessAry.get(i);//����Ϊ���ֵ
			int index = i; //��¼���ֵ������λ��
			for( int j=i+1;j<execs;j++ )
			{
				double thisv = suspiciousnessAry.get(j);
				if( thisv>maxv )
				{
					maxv = thisv;
					index = j;
				}
			}//end of for ...j
			if( i!=index )  //��������ֵ������������
			{
				double tmpv = suspiciousnessAry.get(i);
				//�������ɶ�ֵ
				suspiciousnessAry.set(i, maxv);
				suspiciousnessAry.set(index, tmpv);
				//��������λ�á�
				int tmpi = indexAry.get(i);
				indexAry.set(i, indexAry.get(index));
				indexAry.set(index, tmpi);
			}
		}//end of for ...i
	}
	
	/**
	 * @param total �ܵĸ���
	 * @return ��1 ��total�Ķ��У�1,2,3,4��...,total�� �洢����λ���ɶ����������
	 */
	private List<Integer> assembleIndexList(int total)
	{
		List<Integer> indexAry = new ArrayList<Integer>();
		for( int i=1;i<=total;i++ )
			indexAry.add(i);
		return indexAry;
	}
	
}
