/**
 * 
 */
package common;

import java.util.List;

/**
 * @author Administrator
 * ��.profile�ļ�����Դ���붼Ҫ���ݴ��������ԡ�
 */
public class BenchmarkAttribute {
	private String comment; //.profile��¼����Ϊ�����ļ������Ƕ���ļ���"multi" or "solo"
	//ע�⣺��.fault�ļ���.testcase�ļ���.csv .bugid�ļ�������"multi" or "solo"�����֡�
	private String sourceCode; //Դ����ļ�����ʽ��
	/*SourceCode : Դ������ڴ�Ŀ¼�£�v(xx)�·�Ϊbuggy��fixed������Ŀ¼��
	 *         buggy�´�Ŷ���ļ�����Ҫ������㣻 fixed�½���ű������Ĵ��룬��������㡣
	 *versions.orig�� 
	 *      ������Դ��������source.alt/source.orig�£���������㣻
	 *      �����ϵ�Դ��������versions.alt/versions.orig��v(xx)�£��������. 
	 *FaultSeeds.h��
	 *      ���ϳ����������ĳ��򶼴����versions.altĿ¼�£���ͬ�汾��Դ���붼������ͬһ���ļ���������Ҫ���ļ�FaultSeeds.h
	 */
	private String benchmarkName; //�������ݼ����ƣ��磺PairikaOpenCV��BearsRepair,Defects4j�ȣ���Щ���ࣻ�������ɷ�����ԡ�
	private String directory; //�������ݼ��ĸ�Ŀ¼��
	private String language; //�������ݼ��ı�����ԡ�
	private List<String> objects; //�������ݼ������ж���
	/*
	 * PairikaOpenCV��BearsRepair,Defects4j�ȣ�versionFlag�ǿ��ã�˵����buggy������.java or .cpp...�ļ���Ҫ���㡣
	 * SIRProjectC1��ֻ����(object).c 
	 * SIRProjectC2�����������ΪgzipV2�������gzip.c��Ҫ�ų�V2����benchmark��versionFlag="Vxx"
	 */
	private String versionFlag; //ָ��������Դ�����ļ������ж������͡�
	
	//���캯����
	public BenchmarkAttribute(String comment, String sourceCode, String benchmarkName, String directory,
			String language, List<String> objects,String versionFlag) {
		super();
		this.comment = comment;
		this.sourceCode = sourceCode;
		this.benchmarkName = benchmarkName;
		this.directory = directory;
		this.language = language;
		this.objects = objects;
		this.versionFlag = versionFlag;
	}

	//.profile��¼����Ϊ�����ļ������Ƕ���ļ���"multi" or "solo"
	public String getComment() {
		return comment;
	}

	//Դ����ļ�����ʽ��SourceCode  versions.orig   FaultSeeds.h
	public String getSourceCode() {
		return sourceCode;
	}

	//�������ݼ�����
	public String getBenchmarkName() {
		return benchmarkName;
	}

	//�������ݼ��ĸ�Ŀ¼��
	public String getDirectory() {
		return directory;
	}

	//�������ݼ��ı�����ԡ�
	public String getLanguage() {
		return language;
	}

	//�������ݼ������ж���
	public List<String> getObjects() {
		return objects;
	}
	
	//������Դ�����ļ������ͣ��ж��֡�
	public String getVersionFlag() {
		return versionFlag;
	}
	
}
