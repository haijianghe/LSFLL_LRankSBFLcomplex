/**
 * 
 */
package softComplexMetric;

import java.util.ArrayList;
import java.util.List;

import affiliated.FileSpectrum;
import affiliated.IProfileFile;
import affiliated.SpectrumStruct;
import common.XMLConfigFile;
import parseSourceCode.AstCppFileParse;
import parseSourceCode.AstJavaFileParse;
import parseSourceCode.AstProcedureFileParse;
import parseSourceCode.ClassContext;
import parseSourceCode.ProjectContext;

/** ����Ϊ��λ����֪���Ӷȡ�
 * @author Administrator
 *
 */
public class SoftwareLineComplexMetricFeature {
	private String objectName; //��������
	private int bugID;  //defects4j��bugid�ĸ��.profile��洢����bugid������thVer;

	private ProjectContext pContext;//ĳһ����Ŀ�汾�Ľ������������������������������ڷ����������Ϣ��
	private String pathForCheckup; //��¼�������ļ��ľ���·�����������ڼ����Ч��������Ӷ�ֵ��

	/**  	�յĹ��캯��
	 */
	public SoftwareLineComplexMetricFeature(String objectName, int bugID) {
		this.objectName = objectName;
		this.bugID = bugID;
		pContext = null;
		pathForCheckup = "";
	}
	
	/**
	 * ����������Ӷȵ�����ֵ��
	 */
	public boolean calComplexMetricValue() {
		String[] language = new String[1]; //�������
		boolean[] faultSeed = new boolean[1];//�Ƿ��#include "FaultSeed.h"
		List<String> allFilenames = XMLConfigFile.getSourceCodeFilenames(objectName, bugID,language, faultSeed);
		/*allFilenames����ȡһ���ļ����ҳ������·��������,���ڼ����Ч��������Ӷ�ֵ��*/
		String parsedFilename =   allFilenames.get(0);
		int pos = parsedFilename.lastIndexOf('\\');
		pathForCheckup = parsedFilename.substring(0,pos);

		pContext = new ProjectContext(language[0]);
		//���������ļ���
		boolean parsingError = false;
		for( String filename :allFilenames )
		{
			//if( !filename.contains("TarArchiveEntry.java") )  //for test.
			//	continue;
			//System.out.println(filename+" 's paring is start.");//for test.
			List<ClassContext> parsedClazzes = parseFile(filename,language[0],faultSeed[0]);
			if( parsedClazzes==null )
				parsingError = true;
			else
				pContext.addClazzList(filename, parsedClazzes);
			//System.out.println(filename);
		}
		if( !parsingError )
		{
			System.out.print("V"+bugID+",");
			return true;
		}
		else
		{
			System.out.println(objectName+"_V"+bugID+" parsing is fail.");
			return false;
		}
	}
	
	/**  ��������
	 * @param sourceFile  	���������ļ�����
	 * @param language      �������
	 * @param isFaultSeed   �Ƿ��FaultSeed.h ֻ��SIR C���Գ������
	 * @return �������
	 */
	private  List<ClassContext> parseFile(String sourceFile,String language,boolean isFaultSeed)
	{
		//boolean parseError = false; //���������д���
		List<ClassContext> parsedClazzes = null;
		if( language.contentEquals("C") )
		{
			//ע�⣺��Ҫ�ı�#include "FaultSeed.h"
			if( isFaultSeed )
			{
				//bugID���Ե����к�
				FaultSeedIncludeFile.awakenIncludeBug(pathForCheckup, bugID);
			}
			AstProcedureFileParse astTranslationUnitCore = new AstProcedureFileParse();
			ClassContext classCtx = astTranslationUnitCore.parseFile(sourceFile);
			if( classCtx==null )
				System.out.println("Parse file is error."+sourceFile);
			else
			{
				 parsedClazzes = new ArrayList<>();
				 parsedClazzes.add(classCtx);
			}
		}
		else if( language.contentEquals("C++") )
		{
			 AstCppFileParse astTranslationUnitCore = new AstCppFileParse();
			 parsedClazzes = astTranslationUnitCore.parseFile(sourceFile);
			 if( parsedClazzes==null )
					System.out.println("Parse file is error."+sourceFile);
		}
		else if( language.contentEquals("java") )
		{
			AstJavaFileParse atfParse = new AstJavaFileParse();
			parsedClazzes = atfParse.parseFile(sourceFile);
			if( parsedClazzes==null )
				System.out.println("Parse file is error."+sourceFile);
		}
			
		else
			{};
		return parsedClazzes;
	}
	
	
	/*����ĸ��Ӷ���ָ�� ����Ϊ��λ�������̬���Ӷ�
	 * ���浽.complex�ļ���
	 * passed�ð汾�ĳɹ��������������� failed�ð汾��δͨ��������������
	 * ��Щ�еĸ��Ӷ��Ҳ�����false;ȫ�����ҵ�,��true��
	 * �����ļ���ȷ����true��������̳�����false
	 */
	public boolean writeComplexMetricFeatureFile(List<FileSpectrum> fileSpectra, int passed,int failed) {
		boolean result = true;
		//�ҳ���ӦfileSpectra�������ļ�ÿһ�еĸ��Ӷȣ���װ��List<FileComplexValue>
		List<ClassFileComplexValue> fComplexs = new ArrayList<>(); 
		for( FileSpectrum fsp: fileSpectra )
		{
			String classFilename = fsp.getClassFilename(); //������Ӧ���ļ���
			//if( !classFilename.contains("TarArchiveEntry.java") )  //for test.
			//	continue;
			CheckupInvalidComplexValue cuicv = new CheckupInvalidComplexValue(pathForCheckup,classFilename);
			//����һ��.complex�ļ���Ҫ�Ľṹ��������к���.profile���кŶ�Ӧ��
			ClassFileComplexValue fcvItem = new ClassFileComplexValue();
			fcvItem.setFilename(classFilename);
			List<SpectrumStruct> lineCodes = fsp.getLineCodes();//���ļ��������и������ݡ�
			//�ȼ���ø��ļ���classFilename�����������Ĵ��뾲̬���Ӷȡ�
			pContext.computerSourceCodeStaticFeature(classFilename, lineCodes, passed, failed);
			for( SpectrumStruct ss: lineCodes )
			{
				int lineno = ss.getLineNo();
				//if( lineno==3955 )
				//	lineno = lineno+1-1; //Ϊ�˵��Գ�����ô����
				StatementFeatureStruct fsStatement = pContext.getCodeStaticComplexMetricByFileLineno(classFilename, lineno);
				/*�м������������fsStatement ���к�<0
				 * 1,Դ������} ����һЩ��ִ����䣬�������ʹ��������¼Ϊ��ִ����䡣
				 *    java��enum  C/C++�궨�����ֵ�program����
				 * 2���ҵĽ�����������©��δ��׽����Щ��䡣
				 * 3���ҵ�getCognitiveComplexByFileLineno������bug�����ֿ����Լ�С��
				 */
				if( fsStatement.getLineno()<=0 )
				{
					//��Чֵ��ǿ�Ƹ�ֵ0��ע���������ʽ��
					fsStatement.setNotFoundStatement(lineno);
					fcvItem.addOneComplexPriceStruct(fsStatement);
					//result = false;
					cuicv.addLineno(lineno);//���������
					//System.out.println(classFilename+","+lineno+"    cognitive complex value is error");
				}
				else
					fcvItem.addOneComplexPriceStruct(fsStatement);
			}//enf of for( SpectrumStruct ss: lineCodes )
			//�����ļ�����֪���Ӷ���ӵ�List<FileComplexValue>
			fComplexs.add(fcvItem);
			//��ʾ����Чֵ���кţ����ڼ����롣
			//cuicv.showInvalidLineno(); //check my .profile file.
		}//end of for( FileSpectrum fsp: fileSpectra )
		//���浽.complex�ļ�
		LineComplexFeatureFile lcvFile = new LineComplexFeatureFile(objectName, bugID);
		lcvFile.setFileComplexList(fComplexs);
		result = lcvFile.writeComplexFeartureFile();
		return result;
	}
	
	/**
	 * ���ԡ�
	 */
	public void testMe() {
		System.out.println("Coming soon...");
	}
}
