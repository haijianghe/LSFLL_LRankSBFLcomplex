/**
 * 
 */
package affiliated;

import java.util.List;

/**
 * @author Administrator
 *
 */
public interface IProfileFile {
	boolean readProfileFile(); //��.profile�ļ���
	boolean writeProfileFile();//����.profile�ļ���
	void testMe(); //�����ļ����ݶ����Ƿ���ȷ��
	int getPassed();//��������ͨ����
	int getFailed();
	int getTotalExec();//��ȡ�ܵĿ�ִ�������Ŀ
	List<FileSpectrum> getSpectrumList();//���ļ��ĳ�����
	List<SpectrumStruct> getFileSpectrum(String filename); //��ȡ�ļ���filename���д���ĳ�����
	SpectrumStruct getSpectrumFileLineno(String filename,int lineno);//��Ӧ�ļ����кŵĳ����ס�
}
