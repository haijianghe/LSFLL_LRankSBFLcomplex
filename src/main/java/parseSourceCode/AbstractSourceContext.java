/**
 * 
 */
package parseSourceCode;

/**
 * @author Administrator
 *
 */
public class AbstractSourceContext implements ISourceContext{
	private int startLine;  //�öδ��뿪ʼ���кţ�
	private int endLine;    //�öδ���������кţ�
	
	@Override
	public int getStartLine() {
		return startLine;
	}
	
	@Override
	public void setStartLine(int startLine) {
		this.startLine = startLine;
	}
	
	@Override
	public int getEndLine() {
		return endLine;
	}
	
	@Override
	public void setEndLine(int endLine) {
		this.endLine = endLine;
	}
	
	//��ʾ������Ϣ
	@Override
	public void showMe()
	{
		System.out.println("        My line from "+startLine+" to "+endLine);
	}
}
	
