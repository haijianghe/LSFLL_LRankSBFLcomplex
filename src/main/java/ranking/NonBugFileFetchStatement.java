package ranking;

import java.util.List;

import affiliated.FileSpectrum;
import affiliated.SpectrumStruct;

/**
 * ����bug���ļ���Ѿ��ҳ�bug��估���ھӵ�������
 * �������ﲻ��NumberOfQueryStatement�Ļ�������bug���ļ�����ɼ���䣬����NumberOfQueryStatement����
 * @author Administrator
 *
 */
public class NonBugFileFetchStatement {
	private List<FileSpectrum> fileSpecta; //ĳ����Ŀ��ĳ���汾�����������ĳ�����
	/* ��Ҫ�ӷ�bug�ļ����ҳ�numStatm����䣬�γ�Learing to Rank������
	 * ��Щ�����filenames��linenosָ��*/
	private String[] filenames; //��¼ĳ��������ڵ��ļ���
	private int[] linenos;      //��¼ĳ��������ڵ��к�
	private int numStatm;       //����Ҫ�����������
	private int fetchStatm;     //ʵ�ʲɼ��������������
	private List<String> bugFilenames;//���Ǵ�bug���ļ�������Щ�ļ��������Ѿ��ɼ���
	
	public NonBugFileFetchStatement(List<FileSpectrum> fileSpectrums,int num,List<String> bugFiles)
	{
		fileSpecta = fileSpectrums;
		numStatm = num;
		bugFilenames = bugFiles;
		fetchStatm = 0;
		filenames = new String[numStatm];
		linenos = new int[numStatm];
	}
	
	//ʵ�ʲɼ��������������
	public int getFetchStatm() {
		return fetchStatm;
	}

	//��index�ҳ��ļ�����index = 0,1,2...fetchStatm-1
	public String getIndexFilename(int index)
	{
		return filenames[index];
	}

	//��index�ҳ��кţ�index = 0,1,2...fetchStatm-1	
	public int getIndexLineno(int index)
	{
		return linenos[index];
	}
	
	/** ÿ���ļ����ɼ���ô������䡣
	 * @return
	 */
	private int maximunFromOneFile()
	{
		int nonBugFiles = 0;
		for( FileSpectrum fs : fileSpecta )
		{
			String nonBugFilename = fs.getClassFilename();
			if( !bugFilenames.contains(nonBugFilename) ) //��bug���ļ����ܲɼ���䡣
				nonBugFiles++;
		}
		if( nonBugFiles<=0 )
			return 0;
		else if ( nonBugFiles<=5 )
			return numStatm/nonBugFiles+1;
		else
			return numStatm/5+1;
	}
	
	/**
	 * 
	 */
	public void fetchStatementForFearture()
	{
		int numOneFile = maximunFromOneFile();//ÿ���ļ��ɼ���ô������䡣
		if( numOneFile<=0 )
			return; //û�пɲɼ����ļ���
		for( FileSpectrum fs : fileSpecta )
		{
			String nonBugFilename = fs.getClassFilename();
			if( bugFilenames.contains(nonBugFilename) ) //��bug���ļ������ڴ˴��ɼ���䡣
				continue;
			int index = 0; //��¼���ļ���������ţ������������ɼ���䣬ÿ�����ɼ�һ����
			int fetchThisOne = 0;//�Ӵ����ļ��ڲɼ������������
			List<SpectrumStruct> lineCodes = fs.getLineCodes();
			for( SpectrumStruct ss : lineCodes )
			{
				index ++;
				if( (index%3) !=0 )
					continue;
				filenames[fetchStatm] = nonBugFilename;
				linenos[fetchStatm] = ss.getLineNo();
				fetchStatm++;
				if( fetchThisOne++>numOneFile )
					break;//���ļ����ٲɼ�
				if( fetchStatm>=numStatm )
					break; //�ɼ���������ﵽ���ơ�
			}//end of for...
			if( fetchStatm>=numStatm )
				break; //�ɼ���������ﵽ���ơ�
		}//end of for( FileSpectrum fs : fileSpecta )
	}
}
