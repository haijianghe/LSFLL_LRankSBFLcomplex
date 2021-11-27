/**
 * 
 */
package vfl;

import java.util.ArrayList;
import java.util.List;

import affiliated.SpectrumStruct;

/** VFL�����ײ���������������С�
 * @author Administrator
 *
 */
public class VFLSpectraManagement {
	private List<VFLSpectrumStruct> vflSpecta; //VFL�����׼���
	
	public VFLSpectraManagement()
	{
		vflSpecta = new ArrayList<>();
	}
	
	//VFL�����׼���
	public List<VFLSpectrumStruct> getVflSpecta() {
		return vflSpecta;
	}


	/** �ҵ���startLineno��endLineno֮�����ĳ�����
	 * @param lineCodes �����׼���,
	 * @param startLineno
	 * @param endLineno
	 * @return ���س����ף�����Ҳ������򷵻�null
	 */
	private SpectrumStruct getSpectrum( List<SpectrumStruct> lineCodes,int startLineno,int endLineno )
	{
		SpectrumStruct speS = null;
		for( SpectrumStruct ss : lineCodes )
		{
			int lineno = ss.getLineNo();
			if( lineno>=startLineno && lineno<=endLineno )
			{//������һ�����ֻ������һ�������ס�
				speS = ss;
				break;
			}
		}
		return speS;
	}
	
	/** List<VariableInStatement> ����һ���������VFL������
	 * @param varIStmtLst
	 * @param lineCodes ���ĳ�����
	 * @param passed 
	 * @param failed
	 */
	public void computerSpectraVaiables(List<VariableInStatement> varIStmtLst,
					List<SpectrumStruct> lineCodes,int passed,int failed)
	{
		//���ҳ����б�����
		List<String> varinames = new ArrayList<>();
		for( VariableInStatement vis : varIStmtLst )
		{
			String vname = vis.getVariable();
			if( !varinames.contains(vname) )
				varinames.add(vname); //û�г��ֹ�������ӽ�����ȷ�����ظ���
		}
		//��һ������Щ������ƽ��VFL������,������Ч�ʸ��ߵ��㷨�������޸��ˡ�
		for( String vname : varinames )
		{
			float totalCovfail = 0; //Aef/F
			float totalCovpass = 0; //Aep/P
			float totalNCovok = 0;  //Anp/(Anf+Anp)
			float totalNCovbug = 0; //Anf/(Anf+Anp)
			int totalFound = 0; //���ִ���
			for( VariableInStatement vis : varIStmtLst )
			{
				if( !vname.contentEquals(vis.getVariable()) )
					continue;
				//�ҳ���Ӧ������VFL������
				SpectrumStruct specStru = getSpectrum(lineCodes,vis.getStartno(),vis.getEndno());
				if( specStru==null )
					continue; //�յ����϶��У������ҵ��㷨���󣬻��ߴ��븲�ǳ��������⡣
				float aef = (float)specStru.getAef();
				float aep = (float)specStru.getAep();
				totalCovfail += aef/failed; //Aef/F   F�����ܵ���0
				totalCovpass += aep/passed; //Aep/P   P�����ܵ���0
				//passed==aep������Anp=0��totalNCovok��ֵ����
				if( passed!=aep )
					totalNCovok += (passed-aep)/(failed-aef+passed-aep); //Anp/(Anf+Anp)
				//failed==aef������Anf=0��totalNCovbug��ֵ����
				if( failed!=aef )
						totalNCovbug += (failed-aef)/(failed-aef+passed-aep); //Anf/(Anf+Anp)
				totalFound ++;
			}
			//�������ǵ�ƽ��ֵ���������б��С�
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
