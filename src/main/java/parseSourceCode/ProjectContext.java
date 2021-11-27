/**
 * 
 */
package parseSourceCode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import affiliated.SpectrumStruct;
import softComplexMetric.StatementFeatureStruct;

/** ������Ŀ�Ľ������ݡ�
 * @author Administrator
 *
 */
/**
 * @author Administrator
 * ע�⣺ �����ļ�������ΪMap�����key.
 */
public class ProjectContext {
	private int language; //1=C,2=C++,3=Java
	/*��¼�������н������ݡ�һ����˵��һ���ļ�ֻ��һ���࣬Ҳ����˵List<ClassNode>ֻ�е�������
	 *���C���ԣ�������ԼΪ�ļ�������������ԼΪ�ļ��ڵ�ȫ�ֱ����� 
	 *C���Ժ�C++��ͷ�ļ������ļ����ֿܷ���String���ļ�����ָ���ļ�,��ClassContext��parsingFilename���ǽ���ʱ�����ĵ�һ���ļ�����
	 *��Java��˵���򲻴��ڴ����⡣String���ļ�����ClassContext��parsingFilename��ͬһ����������ʱ���ࡣ
	 */
	Map<String,List<ClassContext>>  codeContexts;  
	
	//���캯����
	public ProjectContext(int type)
	{
		this.language = type;
		codeContexts = new HashMap<String,List<ClassContext>>();
	}
	
	public ProjectContext(String codeType)
	{
		if( codeType.contentEquals("C") )
			this.language = 1;
		else if( codeType.contentEquals("C++") )
			this.language = 2;
		else if( codeType.contentEquals("Java") )
			this.language = 3;
		else
			this.language = 0;
		codeContexts = new HashMap<String,List<ClassContext>>();
	}
	
	/** ���һ���ļ��������
	 * @param pathName  ��������Դ�����ļ�,��Ŀ¼��Ϣ��
	 * @param clazzLst  ���ļ��Ľ��������
	 */
	public void addClazzList(String pathName,List<ClassContext> clazzLst)
	{
		codeContexts.put(getFilenameFromPathFile(pathName), clazzLst);
	}
	
	
	//���ļ�����ȡ����������
	public List<ClassContext> getClassNodeFromFile(String filename)
	{
		return codeContexts.get(filename);
	}
	
	
	/** �Ӵ�·�����ļ�����ȡ������ļ�����
	 * @param pathName ��·�����ļ���
	 * @return
	 */
	public static String getFilenameFromPathFile(String pathName)
	{
		int pos = pathName.lastIndexOf('\\');
		String filename = pathName.substring(pos+1);
		return filename;
	}
	
	/** ��ctxLst���������������������׼���Ĳ��֣��ȼ���ã���䵽lstStatementFeature�С�
	 * @param ctxLst
	 */
	private void fillEasyFeature(List<ClassContext> ctxLst)
	{
		for( ClassContext cContext : ctxLst )
		{
			cContext.fillEasyFeature();
		}
	}
	
	/**��ctxLst������������������VFL���֣��ȼ���ã���䵽StatementFeatureStruct�С�
	 *   VFL: variable-based fault localization
	 * @param ctxLst
	 * @param lineCodes  ÿ�����ĳ����ס�
	 * @param passed
	 * @param failed
	 */
	private void fillVFLFearture(List<ClassContext> ctxLst,List<SpectrumStruct> lineCodes,
			int passed,int failed)
	{
		for( ClassContext cContext : ctxLst )
		{
			cContext.fillVFLFearture(lineCodes,passed,failed);
		}
		
	}
	
	/** ���ļ����������Ĵ��뾲̬���Ӷ���������ã������lstStatementFeature�У������ٲ��ҡ�
	 * @param classFilename ������Ӧ���ļ������ɴ˲��ҵ�List<ClassContext>
	 * @param lineCodes ÿ�����ĳ����ס�
	 * @param passed ĳ�ļ���Ӧ�ĸð汾�ĳɹ�������������
	 * @param failed �ð汾��δͨ��������������
	 */
	public void computerSourceCodeStaticFeature(String classFilename,List<SpectrumStruct> lineCodes,
							int passed,int failed)
	{
		List<ClassContext> ctxLst = null;
		for(Map.Entry<String, List<ClassContext>>  entry  :  codeContexts.entrySet()){
			String parsingFilename = entry.getKey();
			if( parsingFilename.contentEquals(classFilename) )
			{
				//�ļ���ƥ��
				ctxLst = entry.getValue();
				break;
			}
		}
		if( ctxLst==null )
			return;  //δ�ҵ��ļ���Ӧ�Ľ������ݡ�
		/*��������������׼���Ĳ��֣��ȼ���ã���䵽StatementFeatureStruct�С�
		 * ���׼���Ĳ�����ָ��
		 *    �ֲ����������������������������Ժ�ȫ�ֱ�����������������͡��߼������������������߼���Ĳ���������������������������
		 */
		fillEasyFeature(ctxLst);
		//����VFL���� VFL: variable-based fault localization
		fillVFLFearture(ctxLst,lineCodes,passed,failed);
	}
	
	/** ���ļ��������кţ��ҳ���Ӧ��������뾲̬���Ӷ�
	 * @param filename  �ļ���
	 * @param lineno    �к�
	 * @return ���Ӷ�ֵ��
	 *           ���ļ��У��Ѿ��ҵ����к���linenoֵ>0��
	 *         �϶����ļ��У�δ�ҵ����򷵻�StatementFeatureStruct������к�����Ϊ0. ���������ټ�����
	 *          �϶������ļ��У�  �򷵻�StatementFeatureStruct������к�����Ϊ-1.
	 */
	public StatementFeatureStruct getCodeStaticComplexMetricByFileLineno(String filename,int lineno) 
	{
		StatementFeatureStruct fsStatement = new StatementFeatureStruct();
		for(Map.Entry<String, List<ClassContext>>  entry  :  codeContexts.entrySet()){
			String parsingFilename = entry.getKey();
			if( !parsingFilename.contentEquals(filename) )
				continue;
			//�ļ���ƥ��  �ļ���Ȼ�ҵ��������Ҳ����кš�
			List<ClassContext> ctxLst = entry.getValue();
			for( ClassContext cContext : ctxLst )
			{
				StatementFeatureStruct sfs = cContext.getCodeStaticComplexMetricByFileLineno(lineno);
				int locLineno = sfs.getLineno();//-1 ��ʾ������cContext�У�0��ʾ����cContext�У�δ�ҵ���>0�ҵ���
				if( locLineno>0 ) 
				{//�ҵ����кŵ�������뾲̬���Ӷ�
					fsStatement = sfs;
					break; 
				}
				else if ( locLineno==0 ) 
				{ //��ʾ����cContext�У�δ�ҵ����϶��ڴ��ļ��У������Ҳ���������䡣���������{, }��
					fsStatement.setLineno(0);
					break;
				}
				else //������cContext�У������ڵ�ǰ�ļ����ҡ�
					continue;
			}//end of for...
			break;//�ļ��ҵ����к��Ҳ���;�����Ѿ��������Ӷȡ�����������������˳�ѭ��
		}
		return fsStatement;
	}
}
