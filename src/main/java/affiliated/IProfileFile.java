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
	boolean readProfileFile(); //读.profile文件。
	boolean writeProfileFile();//保存.profile文件。
	void testMe(); //测试文件内容读入是否正确。
	int getPassed();//测试用例通过数
	int getFailed();
	int getTotalExec();//获取总的可执行语句数目
	List<FileSpectrum> getSpectrumList();//多文件的程序谱
	List<SpectrumStruct> getFileSpectrum(String filename); //获取文件名filename所有代码的程序谱
	SpectrumStruct getSpectrumFileLineno(String filename,int lineno);//对应文件、行号的程序谱。
}
