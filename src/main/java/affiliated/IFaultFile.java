/**
 * 
 */
package affiliated;

/**
 * @author Administrator
 *
 */
public interface IFaultFile {
	boolean readFaultFile(); //��_fault.csv����.fault�ļ���
	int getVerNo();//��ȡ�汾�ܵ���Ŀ��
	int getBugID(int index);//��ȡ�汾�ţ��Ե����ļ���.profile��������Ȼ����ֵ������ļ���.profile�������ݼ���bugid.
	void testMe(); //�����ļ����ݶ����Ƿ���ȷ��
	boolean checkTestcasePassedFailed(); //���.testcase�ļ���.fault(_fault.csv)�ļ��Ƿ�һ��
	boolean checkProfileAndFaultFile();  //���.profile�ļ���.fault(_fault.csv)�ļ��Ƿ�һ��
	int[] getFaultLinesVer(int index);//��ȡĳ���汾�Ĺ���������顣index from 1,....VerNo
	String[] getFaultFilesVer(int index);//��ȡĳ���汾�Ĺ�����������ļ��ļ��ϡ�
	boolean isFaultStatement(int index,String filename,int lineno);//filename��linenoָ��������ǹ��������
}
