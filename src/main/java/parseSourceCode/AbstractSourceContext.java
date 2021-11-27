/**
 * 
 */
package parseSourceCode;

/**
 * @author Administrator
 *
 */
public class AbstractSourceContext implements ISourceContext{
	private int startLine;  //该段代码开始的行号；
	private int endLine;    //该段代码结束的行号；
	
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
	
	//显示所有信息
	@Override
	public void showMe()
	{
		System.out.println("        My line from "+startLine+" to "+endLine);
	}
}
	
