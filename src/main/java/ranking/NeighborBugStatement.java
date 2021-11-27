/**
 *  ��ȡbug�����ھ����
 */
package ranking;

import java.util.ArrayList;
import java.util.List;

import affiliated.SpectrumStruct;

/**
 * @author Administrator
 * ĳ���ļ��ڵ�bug��䣬�ҳ������ھ����
 */
public class NeighborBugStatement {
	private int numberOfNeighbor; //������������ھ����������
	private List<Integer> existLineno; //��ǰ�Ѿ����ڵ��кţ��Ժ�Ҫ�ظ����롣
	private List<Integer> stmtNeighbor; //��¼��Ҫ������ھ��к�
	private List<SpectrumStruct> spectrumList;//��ǰ�ļ��ĳ�����
	private int bugDivision; //����Ϊ�磬����С���к��ҳ�һ��(numberOfNeighbor/2)����������к��ҳ���һ�롣
	
	//���캯��
	public NeighborBugStatement(int numberOfNeighbor, List<Integer> existLineno, List<SpectrumStruct> spectrumList,
			int divison ) 
	{
		this.numberOfNeighbor = numberOfNeighbor;
		this.existLineno = existLineno;
		this.spectrumList = spectrumList;
		bugDivision = divison;
		stmtNeighbor = new ArrayList<>();
	}
	
	//��¼��Ҫ������ھ��к�
	public List<Integer> getStmtNeighbor() {
		return stmtNeighbor;
	}


	/** ��ȡ��ǰ�ļ��ģ�bug�����ھ������к��б�
	 * @return
	 */
	public List<Integer> getNeighborLineNo()
	{
		//existLineno�������bug,�����ظ�������ǵ��кš�
		List<Integer> smallLst = new ArrayList<>();
		List<Integer> largeLst = new ArrayList<>();
		getOrdinalLineno(smallLst,largeLst); //smallNos:��bugDivisionС���к�
		//��¼�������׵��кš�ע��ȥ��existLineno
		int[] smallNos = smallLst.stream().mapToInt(Integer::intValue).toArray();
		int[] largeNos = largeLst.stream().mapToInt(Integer::intValue).toArray();
		//��С��������
		sortLineno(smallNos);
		sortLineno(largeNos);
		//ǰ���ھӷֱ�ȡnumberOfNeighbor/2��
		int nfetch = numberOfNeighbor/2;
		int nsmall = smallNos.length;
		int nlarge = largeNos.length;
		if( nsmall<=nfetch )
		{ //ǰ�ھӸ���������ոչ���
			//��smallNos���ȫ��ȡ����
			for( int k=0;k<nsmall;k++ )
				stmtNeighbor.add(smallNos[k]);
			int yetwant = numberOfNeighbor-nsmall;
			//���ھӾ���ȡ��
			for( int t=0;(t<nlarge)&&(t<yetwant);t++ )
				stmtNeighbor.add(largeNos[t]);
		}
		else
		{ //ǰ�ھӸ����㹻numberOfNeighbor��һ��
			if( nlarge<=nfetch )
			{ //���ھӸ���������ոչ���
				//��largeNos���ȫ��ȡ����
				for( int k=0;k<nlarge;k++ )
					stmtNeighbor.add(largeNos[k]);
				int yetwant = numberOfNeighbor-nlarge;
				//ǰ�ھӾ���ȡ��
				for( int t=nsmall-1;(t>=0)&&(t>=nsmall-yetwant);t-- )
					stmtNeighbor.add(smallNos[t]);
			}
			else
			{//���ھӸ���Ҳ�㹻һ�룬��ȡһ�롣
				for( int k=0;k<nfetch;k++ )
					stmtNeighbor.add(largeNos[k]);
				for( int t=nsmall-1;t>=nsmall-nfetch;t-- )
					stmtNeighbor.add(smallNos[t]);
			}
		}
		return stmtNeighbor;
	}
	
	//��spectrumList���к�ȡ����ע��ȥ��existLineno
	//��bugDivisionС�ķŵ�smallLst����smallLines��ķŵ�largeLst
	private void getOrdinalLineno(List<Integer> smallLst,List<Integer> largeLst)
	{
		for(SpectrumStruct ss : spectrumList)
		{
			int lineno = ss.getLineNo();
			if( !existLineno.contains(lineno) )
			{ 
				if( lineno<bugDivision )
					smallLst.add(lineno);
				else //ע�⣺bugDivision�϶���existLineno
					largeLst.add(lineno);
			}
		}//end of for...
	}
	
	//��С��������
	private void sortLineno(int[] stLines)
	{
		int totalLine = stLines.length;
		for( int i=0;i<totalLine;i++ )
		{
			for( int j=i+1;j<totalLine;j++ )
			{
				if( stLines[j]<stLines[i] )
				{
					int tmp = stLines[i];
					stLines[i] = stLines[j];
					stLines[j] = tmp;
				}
			}//end of for...j
		}//end of for...i
	}
}
