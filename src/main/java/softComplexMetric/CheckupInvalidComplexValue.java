/**
 * 
 */
package softComplexMetric;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** �ӽ�������в���ĳ�еĸ��Ӷ�ֵʱ��
 *     �в�Ϊ0������Чֵ�����ȫ��Ϊ0������Чֵ���ֹ������룬�����Ǻ�����������Чֵ��
 *  �����ԣ�������
 *  1��{ }�ȴ��룬 2���ҵĽ����������⣬  3���ҵ��㷨���⣬...
 *  ���������ų���һ�������
 * @author Administrator
 *
 */
public class CheckupInvalidComplexValue {
	private List<Integer> lineInvalidValue; //��¼��Чֵ���кš�
	private String pathname; //��Ӧ�ļ��ľ���·��
	private String filename; //��Ӧ�ļ�������
	
	public CheckupInvalidComplexValue(String pathname, String filename) {
		super();
		this.pathname = pathname;
		this.filename = filename;
		lineInvalidValue = new ArrayList<Integer>();
	}
	
	/**
	 * ������һ����Чֵ��
	 */
	public void addLineno(int lineno)
	{
		lineInvalidValue.add(lineno);
	}
	
	/** ����к�Ϊlineno��������Ϊinfo�������Ƿ�ִ�������
	 * @param lineno 
	 * @param info
	 */
	private void checkNonExecuteStatement(int lineno,String info)
	{
		Iterator<Integer> iterator = lineInvalidValue.iterator();
        while (iterator.hasNext()) 
		{
        	int stmt = iterator.next();
        	if( stmt!=lineno )
        		continue;
        	//�����кţ�����������ݣ������{ ����} break else������Ϊ�Ƿ�ִ����䣬ɾ��������Ϣ�������鹤������
        	info = info.trim();
        	if( info.contentEquals("{") || info.contentEquals("}") ||  
        			info.contentEquals("break") || info.contentEquals("else") )
        	{
        		iterator.remove();
        		break; //���ټ���
        	}
		}//end of while...
	}
	
	/**
	 *  �Ƴ� { }�����ķ�ִ����䡣
	 */
	private void removeSpecialLine()
	{
		String pathFilename = pathname+"\\"+filename;
		try {
			File file = new File(pathFilename);
			if (file.isFile() && file.exists()) {
				InputStreamReader read = new InputStreamReader(new FileInputStream(file));
				BufferedReader br = new BufferedReader(read);
				String lineTXT = br.readLine(); //the prompt
				int lindex = 2; //�к�����
				while((lineTXT = br.readLine())!= null){
					checkNonExecuteStatement(lindex,lineTXT);
					lindex++;
				}
				br.close();
				read.close();
			}
		}//end of try. 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 *  ��ʾ��Чֵ���кţ��ڴ�֮ǰ�����Ƴ���ִ�������кš�
	 */
	public void showInvalidLineno()
	{
		removeSpecialLine();
		Iterator<Integer> iterator = lineInvalidValue.iterator();
        while (iterator.hasNext()) 
        {
        	int stmt = iterator.next();
        	System.out.println(filename+","+stmt+"    complex value is error");
        }
	}
}
