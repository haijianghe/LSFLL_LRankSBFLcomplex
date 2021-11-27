/**
 * 
 */
package vfl;

import java.util.ArrayList;
import java.util.List;

import affiliated.SpectrumStruct;

/** VFL程序谱操作都放在这个类中。
 * @author Administrator
 *
 */
public class VFLSpectraManagement {
	private List<VFLSpectrumStruct> vflSpecta; //VFL程序谱集合
	
	public VFLSpectraManagement()
	{
		vflSpecta = new ArrayList<>();
	}
	
	//VFL程序谱集合
	public List<VFLSpectrumStruct> getVflSpecta() {
		return vflSpecta;
	}


	/** 找到在startLineno和endLineno之间代码的程序谱
	 * @param lineCodes 程序谱集合,
	 * @param startLineno
	 * @param endLineno
	 * @return 返回程序谱，如果找不到，则返回null
	 */
	private SpectrumStruct getSpectrum( List<SpectrumStruct> lineCodes,int startLineno,int endLineno )
	{
		SpectrumStruct speS = null;
		for( SpectrumStruct ss : lineCodes )
		{
			int lineno = ss.getLineNo();
			if( lineno>=startLineno && lineno<=endLineno )
			{//按道理，一条语句只允许有一个程序谱。
				speS = ss;
				break;
			}
		}
		return speS;
	}
	
	/** List<VariableInStatement> 里逐一计算变量的VFL程序谱
	 * @param varIStmtLst
	 * @param lineCodes 语句的程序谱
	 * @param passed 
	 * @param failed
	 */
	public void computerSpectraVaiables(List<VariableInStatement> varIStmtLst,
					List<SpectrumStruct> lineCodes,int passed,int failed)
	{
		//先找出所有变量名
		List<String> varinames = new ArrayList<>();
		for( VariableInStatement vis : varIStmtLst )
		{
			String vname = vis.getVariable();
			if( !varinames.contains(vname) )
				varinames.add(vname); //没有出现过，才添加进来，确保不重复。
		}
		//逐一计算这些变量的平均VFL程序谱,可以有效率更高的算法，懒得修改了。
		for( String vname : varinames )
		{
			float totalCovfail = 0; //Aef/F
			float totalCovpass = 0; //Aep/P
			float totalNCovok = 0;  //Anp/(Anf+Anp)
			float totalNCovbug = 0; //Anf/(Anf+Anp)
			int totalFound = 0; //出现次数
			for( VariableInStatement vis : varIStmtLst )
			{
				if( !vname.contentEquals(vis.getVariable()) )
					continue;
				//找出对应变量的VFL程序谱
				SpectrumStruct specStru = getSpectrum(lineCodes,vis.getStartno(),vis.getEndno());
				if( specStru==null )
					continue; //照道理，肯定有，除非我的算法错误，或者代码覆盖程序有问题。
				float aef = (float)specStru.getAef();
				float aep = (float)specStru.getAep();
				totalCovfail += aef/failed; //Aef/F   F不可能等于0
				totalCovpass += aep/passed; //Aep/P   P不可能等于0
				//passed==aep，会有Anp=0；totalNCovok的值不变
				if( passed!=aep )
					totalNCovok += (passed-aep)/(failed-aef+passed-aep); //Anp/(Anf+Anp)
				//failed==aef，会有Anf=0；totalNCovbug的值不变
				if( failed!=aef )
						totalNCovbug += (failed-aef)/(failed-aef+passed-aep); //Anf/(Anf+Anp)
				totalFound ++;
			}
			//计算他们的平均值，并填入列表中。
			if( totalFound>0 )
			{
				totalCovfail = totalCovfail/totalFound;
				totalCovpass = totalCovpass/totalFound;
				totalNCovok = totalNCovok/totalFound;
				totalNCovbug = totalNCovbug/totalFound;
			}
			VFLSpectrumStruct vss = new VFLSpectrumStruct(vname,totalCovfail,totalCovpass,totalNCovok,totalNCovbug);
			vflSpecta.add(vss);
		}//end of for( String vname : varinames )
	}
}
