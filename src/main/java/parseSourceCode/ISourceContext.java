/**
 * 
 */
package parseSourceCode;

/**
 * @author Administrator
 *
 */
public interface ISourceContext {
	int getStartLine();
	void setStartLine(int startLine);
	int getEndLine();
	void setEndLine(int endLine);
	void showMe(); //��ʾ������Ϣ��for debug.
}
