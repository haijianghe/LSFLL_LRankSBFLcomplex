/**
 * 
 */
package vfl;

/** ��¼�����������ֵ����ݣ����ڼ���VFL
 * @author Administrator
 *
 */
public class VariableInStatement {
	private String variable; //������
	private int startno;    //��������������ʼ�к�
	private int endno;      //�����������Ľ����к�
	
	public VariableInStatement(String variable, int startno, int endno) {
		//super();
		this.variable = variable;
		this.startno = startno;
		this.endno = endno;
	}

	//������
	public String getVariable() {
		return variable;
	}
	
	public void setVariable(String variable) {
		this.variable = variable;
	}
	
	//��������������ʼ�к�
	public int getStartno() {
		return startno;
	}
	public void setStartno(int startno) {
		this.startno = startno;
	}
	
	//�����������Ľ����к�
	public int getEndno() {
		return endno;
	}
	public void setEndno(int endno) {
		this.endno = endno;
	}
	
}
