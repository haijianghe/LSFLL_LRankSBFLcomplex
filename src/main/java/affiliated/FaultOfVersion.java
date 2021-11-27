/**
 * 
 */
package affiliated;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Administrator
 * ��¼�����汾�Ĺ��ϣ� �����ļ���������кš�
 * һ���汾�������ж���ļ��й��ϣ���ÿ���ļ��ֿ����ж��������ϡ�
 */
public class FaultOfVersion {
	String[] faultFiles;   //�ð汾�Ĺ�����������ļ��ļ��ϡ�faultFiles��faultLines�Ķ�ά˳�򱣳�һ�¡�
	int[] faultLines;  //�ð汾�Ĺ�������кż��ϡ�
	int numberOfFault; //�������������
	
	public FaultOfVersion()
	{
		faultFiles = null;
		faultLines = null;
		numberOfFault = 0;
	}
	
	//����һ���汾��
	public void assign(List<String> fileLst,List<Integer> linenoLst)
	{
		numberOfFault = linenoLst.size();
		faultLines = new int[numberOfFault]; //the pointer of the number of statement
		Integer[] Istat =  linenoLst.toArray(new Integer[numberOfFault]);
		for(int k=0;k<numberOfFault;k++)
			faultLines[k] = Istat[k].intValue();

		faultFiles = (String[])fileLst.toArray(new String[numberOfFault]);
	}

	//�������������
	public int getNumberOfFault() {
		return numberOfFault;
	}

	//�������������
	public void setNumberOfFault(int numberOfFault) {
		this.numberOfFault = numberOfFault;
	}

	//�ð汾�Ĺ�����������ļ��ļ��ϡ�
	public String[] getFaultFiles() {
		return faultFiles;
	}

	//�ð汾�Ĺ�������кż��ϡ�
	public int[] getFaultLines() {
		return faultLines;
	}
	
	//filename��linenoָ��������ǹ��������
	public boolean isFaultStatement(String filename,int lineno)
	{
		for(int k=0;k<numberOfFault;k++)
		{
			if( faultFiles[k].contentEquals(filename)
					&& faultLines[k]==lineno )
				return true;
		}
		return false;
	}
		
}
