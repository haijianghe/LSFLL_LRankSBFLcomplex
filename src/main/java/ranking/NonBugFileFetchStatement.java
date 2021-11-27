package ranking;

import java.util.List;

import affiliated.FileSpectrum;
import affiliated.SpectrumStruct;

/**
 * 带有bug的文件里，已经找出bug语句及其邻居的特征；
 * 语句个数达不到NumberOfQueryStatement的话，从无bug的文件里面采集语句，凑齐NumberOfQueryStatement个。
 * @author Administrator
 *
 */
public class NonBugFileFetchStatement {
	private List<FileSpectrum> fileSpecta; //某个项目的某个版本，其所有语句的程序谱
	/* 还要从非bug文件里找出numStatm条语句，形成Learing to Rank特征。
	 * 这些语句由filenames和linenos指定*/
	private String[] filenames; //记录某条语句所在的文件名
	private int[] linenos;      //记录某条语句所在的行号
	private int numStatm;       //还需要的语句条数。
	private int fetchStatm;     //实际采集到的语句条数。
	private List<String> bugFilenames;//这是带bug的文件名，这些文件里的语句已经采集。
	
	public NonBugFileFetchStatement(List<FileSpectrum> fileSpectrums,int num,List<String> bugFiles)
	{
		fileSpecta = fileSpectrums;
		numStatm = num;
		bugFilenames = bugFiles;
		fetchStatm = 0;
		filenames = new String[numStatm];
		linenos = new int[numStatm];
	}
	
	//实际采集到的语句条数。
	public int getFetchStatm() {
		return fetchStatm;
	}

	//由index找出文件名，index = 0,1,2...fetchStatm-1
	public String getIndexFilename(int index)
	{
		return filenames[index];
	}

	//由index找出行号，index = 0,1,2...fetchStatm-1	
	public int getIndexLineno(int index)
	{
		return linenos[index];
	}
	
	/** 每个文件最多采集这么多条语句。
	 * @return
	 */
	private int maximunFromOneFile()
	{
		int nonBugFiles = 0;
		for( FileSpectrum fs : fileSpecta )
		{
			String nonBugFilename = fs.getClassFilename();
			if( !bugFilenames.contains(nonBugFilename) ) //带bug的文件不能采集语句。
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
		int numOneFile = maximunFromOneFile();//每个文件采集这么多条语句。
		if( numOneFile<=0 )
			return; //没有可采集的文件。
		for( FileSpectrum fs : fileSpecta )
		{
			String nonBugFilename = fs.getClassFilename();
			if( bugFilenames.contains(nonBugFilename) ) //带bug的文件不能在此处采集语句。
				continue;
			int index = 0; //记录该文件内语句的序号，尽量不连续采集语句，每三条采集一条。
			int fetchThisOne = 0;//从此条文件内采集的语句条数。
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
					break;//此文件不再采集
				if( fetchStatm>=numStatm )
					break; //采集语句条数达到限制。
			}//end of for...
			if( fetchStatm>=numStatm )
				break; //采集语句条数达到限制。
		}//end of for( FileSpectrum fs : fileSpecta )
	}
}
