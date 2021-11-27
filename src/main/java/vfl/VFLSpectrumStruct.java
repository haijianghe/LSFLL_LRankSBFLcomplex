/**
 * 
 */
package vfl;

/** 定义结构，变量名带VFL程序谱
 * @author Administrator
 *
 */
public class VFLSpectrumStruct {
	private String variable; //变量名
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
	
	/** 当前变量的VFL程序谱比（covfail，covpass，ncovok，ncovbug）所在的VFL程序谱，有更大的可疑度
	 * @param covfail
	 * @param covpass
	 * @param ncovok
	 * @param ncovbug
	 * @return true，当前语句的可疑度大，采用当前语句的语句；
	 *         false （covfail，covpass，ncovok，ncovbug）组成的可疑度大，采用（）
	 */
	public boolean largeSuspicious(float covfail, float covpass, float ncovok, float ncovbug)
	{
		//先比较Aef/F ；aef越大，语句包含错误的可能性越大。
		if( this.covfail>covfail )
			return true;
		else if( this.covfail<covfail )
			return false;
		else {}
		//Aef/F相等，再比较Aep/P ; aep越大，语句包含错误的可能性越小
		if( this.covpass<covpass )
			return true;
		else if( this.covpass>covpass )
			return false;
		else {}
		//Aef/F 和 Aep/P相等，再比较Anp/(Anf+Anp) ：anp越大，语句包含错误的可能性越大
		if( this.ncovok>ncovok )
			return true;
		else if( this.ncovok<ncovok )
			return false;
		else {}
		//Aef/F 、 Aep/P 和 Anp/(Anf+Anp)  都相等，再比较 Anf/(Anf+Anp) ；
		//anf越大，语句包含错误的可能性越小。
		if( this.ncovbug<ncovbug )
			return true;
		else if( this.ncovbug>ncovbug )
			return false;
		else //相等，则不用比较，采用当前语句。
			return true;
	}
	
	//变量名
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
