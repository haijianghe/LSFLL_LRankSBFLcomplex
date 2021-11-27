/**
 * 
 */
package vfl;

/** 记录变量在语句出现的数据，便于计算VFL
 * @author Administrator
 *
 */
public class VariableInStatement {
	private String variable; //变量名
	private int startno;    //变量所在语句的起始行号
	private int endno;      //变量所在语句的结束行号
	
	public VariableInStatement(String variable, int startno, int endno) {
		//super();
		this.variable = variable;
		this.startno = startno;
		this.endno = endno;
	}

	//变量名
	public String getVariable() {
		return variable;
	}
	
	public void setVariable(String variable) {
		this.variable = variable;
	}
	
	//变量所在语句的起始行号
	public int getStartno() {
		return startno;
	}
	public void setStartno(int startno) {
		this.startno = startno;
	}
	
	//变量所在语句的结束行号
	public int getEndno() {
		return endno;
	}
	public void setEndno(int endno) {
		this.endno = endno;
	}
	
}
