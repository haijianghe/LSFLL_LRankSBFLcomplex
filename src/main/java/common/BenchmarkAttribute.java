/**
 * 
 */
package common;

import java.util.List;

/**
 * @author Administrator
 * 读.profile文件，读源代码都要依据此配置属性。
 */
public class BenchmarkAttribute {
	private String comment; //.profile记录内容为单个文件，还是多个文件；"multi" or "solo"
	//注意：读.fault文件和.testcase文件和.csv .bugid文件都依据"multi" or "solo"来区分。
	private String sourceCode; //源代码的几种形式。
	/*SourceCode : 源代码放在此目录下，v(xx)下分为buggy和fixed两个子目录，
	 *         buggy下存放多个文件，都要参与计算； fixed下仅存放被修正的代码，不参与计算。
	 *versions.orig： 
	 *      修正的源代码存放在source.alt/source.orig下，不参与计算；
	 *      带故障的源代码存放在versions.alt/versions.orig的v(xx)下，参与计算. 
	 *FaultSeeds.h：
	 *      故障程序和修正后的程序都存放在versions.alt目录下，不同版本的源代码都来自于同一个文件，区分需要读文件FaultSeeds.h
	 */
	private String benchmarkName; //共享数据集名称，如：PairikaOpenCV，BearsRepair,Defects4j等，有些多余；不过，可方便调试。
	private String directory; //共享数据集的根目录。
	private String language; //共享数据集的编程语言。
	private List<String> objects; //共享数据集的所有对象。
	/*
	 * PairikaOpenCV，BearsRepair,Defects4j等，versionFlag是空置，说明，buggy下所有.java or .cpp...文件都要计算。
	 * SIRProjectC1，只计算(object).c 
	 * SIRProjectC2，比如对象名为gzipV2，则计算gzip.c；要排除V2。该benchmark的versionFlag="Vxx"
	 */
	private String versionFlag; //指出待检查的源代码文件名，有多种类型。
	
	//构造函数。
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

	//.profile记录内容为单个文件，还是多个文件；"multi" or "solo"
	public String getComment() {
		return comment;
	}

	//源代码的几种形式。SourceCode  versions.orig   FaultSeeds.h
	public String getSourceCode() {
		return sourceCode;
	}

	//共享数据集名称
	public String getBenchmarkName() {
		return benchmarkName;
	}

	//共享数据集的根目录。
	public String getDirectory() {
		return directory;
	}

	//共享数据集的编程语言。
	public String getLanguage() {
		return language;
	}

	//共享数据集的所有对象。
	public List<String> getObjects() {
		return objects;
	}
	
	//待检查的源代码文件名类型，有多种。
	public String getVersionFlag() {
		return versionFlag;
	}
	
}
