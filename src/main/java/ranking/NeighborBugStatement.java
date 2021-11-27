/**
 *  获取bug语句的邻居语句
 */
package ranking;

import java.util.ArrayList;
import java.util.List;

import affiliated.SpectrumStruct;

/**
 * @author Administrator
 * 某个文件内的bug语句，找出它的邻居语句
 */
public class NeighborBugStatement {
	private int numberOfNeighbor; //允许最多加入的邻居语句条数。
	private List<Integer> existLineno; //当前已经存在的行号，以后不要重复加入。
	private List<Integer> stmtNeighbor; //记录需要加入的邻居行号
	private List<SpectrumStruct> spectrumList;//当前文件的程序谱
	private int bugDivision; //以它为界，比它小的行号找出一半(numberOfNeighbor/2)，比它大的行号找出另一半。
	
	//构造函数
	public NeighborBugStatement(int numberOfNeighbor, List<Integer> existLineno, List<SpectrumStruct> spectrumList,
			int divison ) 
	{
		this.numberOfNeighbor = numberOfNeighbor;
		this.existLineno = existLineno;
		this.spectrumList = spectrumList;
		bugDivision = divison;
		stmtNeighbor = new ArrayList<>();
	}
	
	//记录需要加入的邻居行号
	public List<Integer> getStmtNeighbor() {
		return stmtNeighbor;
	}


	/** 获取当前文件的，bug语句的邻居语句的行号列表。
	 * @return
	 */
	public List<Integer> getNeighborLineNo()
	{
		//existLineno语句属于bug,不能重复添加它们的行号。
		List<Integer> smallLst = new ArrayList<>();
		List<Integer> largeLst = new ArrayList<>();
		getOrdinalLineno(smallLst,largeLst); //smallNos:比bugDivision小的行号
		//记录带程序谱的行号。注意去除existLineno
		int[] smallNos = smallLst.stream().mapToInt(Integer::intValue).toArray();
		int[] largeNos = largeLst.stream().mapToInt(Integer::intValue).toArray();
		//从小到大排序
		sortLineno(smallNos);
		sortLineno(largeNos);
		//前后邻居分别取numberOfNeighbor/2个
		int nfetch = numberOfNeighbor/2;
		int nsmall = smallNos.length;
		int nlarge = largeNos.length;
		if( nsmall<=nfetch )
		{ //前邻居个数不够或刚刚够。
			//将smallNos里的全部取到。
			for( int k=0;k<nsmall;k++ )
				stmtNeighbor.add(smallNos[k]);
			int yetwant = numberOfNeighbor-nsmall;
			//后邻居尽量取满
			for( int t=0;(t<nlarge)&&(t<yetwant);t++ )
				stmtNeighbor.add(largeNos[t]);
		}
		else
		{ //前邻居个数足够numberOfNeighbor的一半
			if( nlarge<=nfetch )
			{ //后邻居个数不够或刚刚够。
				//将largeNos里的全部取到。
				for( int k=0;k<nlarge;k++ )
					stmtNeighbor.add(largeNos[k]);
				int yetwant = numberOfNeighbor-nlarge;
				//前邻居尽量取满
				for( int t=nsmall-1;(t>=0)&&(t>=nsmall-yetwant);t-- )
					stmtNeighbor.add(smallNos[t]);
			}
			else
			{//后邻居个数也足够一半，则都取一半。
				for( int k=0;k<nfetch;k++ )
					stmtNeighbor.add(largeNos[k]);
				for( int t=nsmall-1;t>=nsmall-nfetch;t-- )
					stmtNeighbor.add(smallNos[t]);
			}
		}
		return stmtNeighbor;
	}
	
	//将spectrumList的行号取出，注意去除existLineno
	//比bugDivision小的放到smallLst，比smallLines大的放到largeLst
	private void getOrdinalLineno(List<Integer> smallLst,List<Integer> largeLst)
	{
		for(SpectrumStruct ss : spectrumList)
		{
			int lineno = ss.getLineNo();
			if( !existLineno.contains(lineno) )
			{ 
				if( lineno<bugDivision )
					smallLst.add(lineno);
				else //注意：bugDivision肯定在existLineno
					largeLst.add(lineno);
			}
		}//end of for...
	}
	
	//从小到大排序
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
