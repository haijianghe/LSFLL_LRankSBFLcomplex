/**
 * 
 */
package vfl;

/** ����ṹ����������VFL������
 * @author Administrator
 *
 */
public class VFLSpectrumStruct {
	private String variable; //������
	private float covfail; //Aef/F
	private float covpass; //Aep/P
	private float ncovok;  //Anp/(Anf+Anp)
	private float ncovbug; //Anf/(Anf+Anp)
	
	public VFLSpectrumStruct(String variable, float covfail, float covpass, float ncovok, float ncovbug) {
		//super();
		this.variable = variable;
		this.covfail = covfail;
		this.covpass = covpass;
		this.ncovok = ncovok;
		this.ncovbug = ncovbug;
	}
	
	/** ��ǰ������VFL�����ױȣ�covfail��covpass��ncovok��ncovbug�����ڵ�VFL�����ף��и���Ŀ��ɶ�
	 * @param covfail
	 * @param covpass
	 * @param ncovok
	 * @param ncovbug
	 * @return true����ǰ���Ŀ��ɶȴ󣬲��õ�ǰ������䣻
	 *         false ��covfail��covpass��ncovok��ncovbug����ɵĿ��ɶȴ󣬲��ã���
	 */
	public boolean largeSuspicious(float covfail, float covpass, float ncovok, float ncovbug)
	{
		//�ȱȽ�Aef/F ��aefԽ������������Ŀ�����Խ��
		if( this.covfail>covfail )
			return true;
		else if( this.covfail<covfail )
			return false;
		else {}
		//Aef/F��ȣ��ٱȽ�Aep/P ; aepԽ������������Ŀ�����ԽС
		if( this.covpass<covpass )
			return true;
		else if( this.covpass>covpass )
			return false;
		else {}
		//Aef/F �� Aep/P��ȣ��ٱȽ�Anp/(Anf+Anp) ��anpԽ������������Ŀ�����Խ��
		if( this.ncovok>ncovok )
			return true;
		else if( this.ncovok<ncovok )
			return false;
		else {}
		//Aef/F �� Aep/P �� Anp/(Anf+Anp)  ����ȣ��ٱȽ� Anf/(Anf+Anp) ��
		//anfԽ������������Ŀ�����ԽС��
		if( this.ncovbug<ncovbug )
			return true;
		else if( this.ncovbug>ncovbug )
			return false;
		else //��ȣ����ñȽϣ����õ�ǰ��䡣
			return true;
	}
	
	//������
	public String getVariable() {
		return variable;
	}
	public void setVariable(String variable) {
		this.variable = variable;
	}
	
	//Aef/F 
	public float getCovfail() {
		return covfail;
	}
	public void setCovfail(float covfail) {
		this.covfail = covfail;
	}
	
	//Aep/P
	public float getCovpass() {
		return covpass;
	}
	public void setCovpass(float covpass) {
		this.covpass = covpass;
	}
	
	//Anp/(Anf+Anp)
	public float getNcovok() {
		return ncovok;
	}
	public void setNcovok(float ncovok) {
		this.ncovok = ncovok;
	}
	
	//Anf/(Anf+Anp)
	public float getNcovbug() {
		return ncovbug;
	}
	public void setNcovbug(float ncovbug) {
		this.ncovbug = ncovbug;
	}
	
}
