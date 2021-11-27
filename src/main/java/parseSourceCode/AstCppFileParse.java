/**
 * 
 */
package parseSourceCode;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.ExpansionOverlapsBoundaryException;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.parser.IScannerExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.c.ANSICParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.c.GCCParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.c.GCCScannerExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.c.ICParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.ANSICPPParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.GPPParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.GPPScannerExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.ICPPParserExtensionConfiguration;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.parser.ExtendedScannerInfo;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.IncludeFileContentProvider;
import org.eclipse.cdt.core.parser.NullLogService;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.internal.core.dom.parser.AbstractGNUSourceCodeParser;
import org.eclipse.cdt.internal.core.dom.parser.c.GNUCSourceParser;
import org.eclipse.cdt.internal.core.dom.parser.cpp.GNUCPPSourceParser;
import org.eclipse.cdt.internal.core.parser.IMacroDictionary;
import org.eclipse.cdt.internal.core.parser.scanner.CPreprocessor;
import org.eclipse.cdt.internal.core.parser.scanner.InternalFileContent;
import org.eclipse.cdt.internal.core.parser.scanner.InternalFileContent.InclusionKind;
import org.eclipse.cdt.internal.core.parser.scanner.InternalFileContentProvider;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

/** C++ �Ľ�����
 * @author Administrator
 *
 */
public class AstCppFileParse {
	private IASTTranslationUnit unitCompile;
    private final static IParserLogService NULL_LOG = new NullLogService();
    private IncludeFileResolve resolveIncludeFile;  //��ȡ�������ļ���ͷ�ļ���
    
    public AstCppFileParse()
    {
    	unitCompile = null;
    	resolveIncludeFile = null;
    }
    
    /**
	 * @param cppFilePath  ��Ŀ¼���ļ�����
	 * @param clazzList ������Ľ������˶���
	 * @return
	 */
	public List<ClassContext> parseFile(String cppFilePath)
	{
		resolveIncludeFile = new IncludeFileResolve(cppFilePath);
		resolveIncludeFile.resolve();
		//��������Ĵ����ȡ�������ļ���ͷ�ļ����ټ��뵽�������̡�
		
		boolean result=true;
		List<ClassContext> clazzList = null;
		if( createCompilationUnit(cppFilePath)  )
		{
			int pos = cppFilePath.lastIndexOf("\\");
			String parsingFilename = cppFilePath.substring(pos+1);
			CppCodeVisitor cdtVisitor  = new CppCodeVisitor(parsingFilename);
			unitCompile.accept(cdtVisitor);
			clazzList = cdtVisitor.getClazzList();
		    //��ǰ���ڼ����������Ӱ�����ĸ��Ӷ�ʱ����δ�����ⲿ�����Ե�Ӱ�졣�ڴ˴�һ�����㡣
			//�������adjustNestedNestedClass(clazzList)֮ǰ����Ϊ��ʱ��������ϵ��������
		    adjustNestedComplexMetricWithAttribute(clazzList);
			//����ֻ����һ��Ƕ�ס������ڲ�����ĸ���Ƕ���ࡣ
			adjustNestedNestedClass(clazzList);
		    //��ǰ���ڼ��㸴�Ӷ�ʱ����δ����ȫ�ֱ�����Ӱ�졣�ڴ˴�һ�����㡣
			adjustComplexMetricWithGlobalVariable(clazzList);
			// ����궨������ĸ��Ӷȡ�
			ProcessMacroDefineition(clazzList); 
		}
		else
			result = false;
		if( result )
			return clazzList;
		else
			return null;
	}
	
	 /*��ǰ���ڼ��㸴�Ӷ�ʱ����δ����ȫ�ֱ�����Ӱ�졣�ڴ˴�һ�����㡣
	�Ժ�Ҫ����.h�ļ���ȫ�ֱ�����
	1,�ҳ�����Ϊ14����,C++����ı������������ڸ������ࣨTopDeclartionCpp�ࣩ�С�
	2���������attributes������C++�ļ���ȫ�ֱ�����
	3����������������з����������������������ȫ�ֱ������������ĸ��Ӷȣ�ȫ�ֱ����������Ҳ�����⡣
	*/
	private void adjustComplexMetricWithGlobalVariable(List<ClassContext> clazzLst)
	{
		ClassContext topDeclarationClass = null;
		for( ClassContext ctx :  clazzLst )
		{
			if( ctx.isTopDeclartionClass() ) //14��C++ȫ�ֱ������ļ��������������䣬��ɵ�TopDeclartionCpp�ࡣ
			{
				topDeclarationClass = ctx;
				break;
			}
		}
		if( topDeclarationClass==null )
			return;  //���ļ�û��ȫ�ֱ���
		//��������Լ��ļ���ȫ�ֱ�����
		List<String> globalVariables = topDeclarationClass.getAttributes();
		//û�кõ�������ȫ�ֱ�����ӵ������࣬��Ϊ�����������
		for( ClassContext ctx :  clazzLst )
			ctx.cppAdjustAttributeWithGlobalVariable(globalVariables);
	}

	 /*��ǰ���ڼ����������Ӱ�����ĸ��Ӷ�ʱ����δ�����ⲿ�����Ե�Ӱ�졣�ڴ˴�һ�����㡣
		�������adjustNestedNestedClass(clazzList)֮ǰ����Ϊ��ʱ��������ϵ��������
	*/
    private void adjustNestedComplexMetricWithAttribute(List<ClassContext> clazzLst)
    {
        Iterator<ClassContext> iterator = clazzLst.iterator();
        while (iterator.hasNext()) 
		{
        	ClassContext cnode = iterator.next();
			if( !cnode.isNesting() )
				continue; //�����࣬���������
			//cnode���ڲ��࣬���ҳ��������ⲿ�ࡣ
			ClassContext parentNode = getClassNode(clazzLst,cnode.getParentName());
			do
			{
				List<String> parentAttributes = parentNode.getAttributes();//������������б�
				cnode.addOutterAttribute(parentAttributes);
				parentNode = getClassNode(clazzLst,parentNode.getParentName());
			}while (parentNode!=null); 
		}
    }
	    
	/**�����ڲ�����ĸ���Ƕ���ࣨ��ʧһ���ԣ�����Ϊinninn����
	 * �����㷨����inninn��ķ������ƶ���������ࣻ����������ֵ���䣬��type��Ϊ5.
	 * @param clazzLst
	 */
	private void adjustNestedNestedClass(List<ClassContext> clazzLst)
	{
        Iterator<ClassContext> iterator = clazzLst.iterator();
        while (iterator.hasNext()) 
		{
        	ClassContext cnode = iterator.next();
			if( !cnode.isNesting() )
				continue;
			//���ڲ��ࡣ
			ClassContext parentNode = getClassNode(clazzLst,cnode.getParentName());
			//������ⲿ��Ҳ��Ƕ���ࣨ�ڲ��࣬�ֲ��࣬�����ࣩ,��ɾ����������ݣ����������ƶ����ζ����ࡣ
			if( parentNode.isNesting() )
			{
				ClassContext subTopParent = subTopParentClass(clazzLst,parentNode);
				//��Ȼ�÷�����ɾ��������������ݵñ�����
				subTopParent.mergeInner2Class(cnode);
				cnode.setWillRemove(true);//����ɾ����ǡ�
				//iterator.remove();//ʹ�õ�������ɾ������ɾ��
			}
		}//end of while.
        
        //ɾ�����������ϵ�Ƕ���ࡣ
        iterator = clazzLst.iterator();
        while (iterator.hasNext()) 
		{
        	ClassContext cnode = (ClassContext)(iterator.next());
        	if( cnode.isWillRemove() )
        		iterator.remove();//ʹ�õ�������ɾ������ɾ��
		}//end of while
	}

	/*
     * ����궨������ĸ��Ӷȡ�
     * ������ʽ�ĺ궨��, IASTPreprocessorObjectStyleMacroDefinition
     * �ͺ�����ʽ�ĺ궨�� IASTPreprocessorFunctionStyleMacroDefinition�����ǵĸ��Ӷȸ�����ֵͬ��
     */
    private void ProcessMacroDefineition(List<ClassContext> clazzLst)
    {
    	IASTPreprocessorMacroDefinition[] 	iapMacros = unitCompile.getMacroDefinitions();
    	List<String> macroDefines = new ArrayList<>();
		for( IASTPreprocessorMacroDefinition item: iapMacros )
			macroDefines.add(item.getName().toString());
		if( macroDefines.size()>0 )//�к궨��
		{
			for(ClassContext clazzCtx : clazzLst )
				clazzCtx.adjustComplexMetricWithMacroDefinition(macroDefines);
		}
    }
    
	/** ����������ֻ�ȡ��ڵ㡣
	 * @param name
	 * @return
	 */
	private ClassContext getClassNode(List<ClassContext> clazzLst,String name)
	{
		ClassContext rtnNode = null;
		for ( ClassContext cnode : clazzLst )
		{
			if( cnode.getName().contentEquals(name) )
			{
				rtnNode = cnode;
				break;
			}
		}
		return rtnNode;
	}
	
	/**  ��ȡccNode�ζ�����ⲿ��ڵ㣬�����һ���ġ�
	 * @param ccNode  �϶����ڲ��࣬��isNesting=true������ccNode���ڲ��ࡣ
	 * @return
	 */
	private ClassContext subTopParentClass(List<ClassContext> clazzLst,ClassContext ccNode)
	{
		ClassContext topParentNode = getClassNode(clazzLst,ccNode.getParentName());
		ClassContext subTopParent = ccNode;
		while( topParentNode.isNesting() )
		{
			subTopParent = topParentNode;
			topParentNode = getClassNode(clazzLst,topParentNode.getParentName());
		}
		return subTopParent;
	}
	
	//cppFilePath:��Ŀ¼���ļ�����
	private boolean createCompilationUnit(String cppFilePath)
	{
		unitCompile = parse(	cppFilePath, ParserLanguage.CPP, true, false  ); //useGNUExtensions=true
		if( unitCompile==null )
			return false;
		else
			return true;
    }

	/*
	 * alibaba
	 */
    private IASTTranslationUnit parse(
        String cppFilePath,
        ParserLanguage parserLanguage,
        boolean useGNUExtensions,
        boolean skipTrivialInitializers    ) 
    {
        IScanner scanner = null;
		boolean result=true;
        byte[] input = null;
		try {
		    BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(cppFilePath));
		    input = new byte[bufferedInputStream.available()];
	        bufferedInputStream.read(input);
            bufferedInputStream.close();
		} 
		catch (FileNotFoundException e) 
		{
			result = false;
			e.printStackTrace();
		} 
		catch (IOException e) {
			result = false;
			e.printStackTrace();
		}
		if( !result )
			return null;

		char[] codeStream = new String(input).toCharArray();

        scanner = createScanner(
            FileContent.create(cppFilePath, codeStream),
            parserLanguage,
            //ParserModeע�ͣ�https://www.cct.lsu.edu/~rguidry/eclipse-doc36/src-html/org/eclipse/cdt/core/parser/ParserMode.html#line.31
            ParserMode.COMPLETE_PARSE, //Follow inclusions, parse function/method bodies.
            createScannerInfo(useGNUExtensions)
        );
        /*Toggles generation of tokens for inactive code branches. 
         * When turned on, each inactive code branch is preceded by a token of kind IToken.tINACTIVE_CODE_START and succeeded 
         * by one of kind IToken.tINACTIVE_CODE_END.
         */
        scanner.setProcessInactiveCode(true);
        AbstractGNUSourceCodeParser gnuSourceCodeParser = null;
        if (parserLanguage == ParserLanguage.CPP) {
            ICPPParserExtensionConfiguration configuration = useGNUExtensions ?
                new GPPParserExtensionConfiguration() :
                new ANSICPPParserExtensionConfiguration();
            gnuSourceCodeParser = new GNUCPPSourceParser(
                scanner, ParserMode.COMPLETE_PARSE, NULL_LOG, configuration, null
            );
        }
        else {
            ICParserExtensionConfiguration configuration = useGNUExtensions ?
                new GCCParserExtensionConfiguration():
                new ANSICParserExtensionConfiguration();
            gnuSourceCodeParser = new GNUCSourceParser(
                scanner, ParserMode.COMPLETE_PARSE, NULL_LOG, configuration, null
            );
        }
        if (skipTrivialInitializers) {
        	gnuSourceCodeParser.setMaximumTrivialExpressionsInAggregateInitializers(1);
            //gnuSourceCodeParser.setSkipTrivialExpressionsInAggregateInitializers(true);
        }
        return gnuSourceCodeParser.parse();
    }

    /*
     * GNUScannerExtensionConfiguration : Base class for all gnu scanner configurations. Provides gnu-specific macros and keywords. 
     */
    private IScanner createScanner(
        FileContent fileContent,
        ParserLanguage parserLanguage,
        ParserMode parserMode,
        IScannerInfo scannerInfo ) 
    {
        IScannerExtensionConfiguration configuration =
            parserLanguage == ParserLanguage.C ?
                GCCScannerExtensionConfiguration.getInstance(scannerInfo) : //Configures the preprocessor for parsing c-sources as accepted by gcc.
                GPPScannerExtensionConfiguration.getInstance(scannerInfo);  //Configures the preprocessor for c++-sources as accepted by g++.
        // garcia.wul ���һ��������IncludeFileContentProvider��null����Ȼ�ᱨworkspace is closed����
        //IncludeFileContentProvider ifcp = IncludeFileContentProvider.getEmptyFilesProvider();
        IncludeFileContentProvider ifcp = createInternalFileContentProvider(true);
        return new CPreprocessor(
            fileContent, scannerInfo, parserLanguage, NULL_LOG, configuration, ifcp
        );
    }

    /**  #include  ͷ�ļ�������
     * @return
     */
    private InternalFileContentProvider createInternalFileContentProvider(boolean shouldScanInclusionFiles)
    {
	    InternalFileContentProvider ifcp = new InternalFileContentProvider() {
			@Override
			public InternalFileContent getContentForInclusion(String filePath, IMacroDictionary macroDictionary) {
				InternalFileContent ifc = null;
				if (!shouldScanInclusionFiles) {
					ifc =  new InternalFileContent(filePath, InclusionKind.SKIP_FILE); 
				}else {
					ifc = (InternalFileContent) FileContent.createForExternalFileLocation(filePath);
				}
	
				return ifc;
			}
	
			@Override
			public InternalFileContent getContentForInclusion(IIndexFileLocation ifl, String astPath) {
				InternalFileContent c =  (InternalFileContent) FileContent.create(ifl);
				return c;
			}
	    };
		return ifcp; 
	}
	
    /**
     * @param useGNUExtensions GNU ANSI ...
     * @return
     */
    public ScannerInfo createScannerInfo(boolean useGNUExtensions) {
    	final String[] EMPTY_ARRAY_STRING = new String[0];
    	Map<String, String> macroMap = useGNUExtensions ? getGnuMap() : getStdMap();
    	//Returns an array of paths that are searched when processing an include directive.
    	String[] includePaths = resolveIncludeFile.getIncludePaths();
    	String[] includeFiles = resolveIncludeFile.getIncludeFiles();
    	String[] macroFiles = EMPTY_ARRAY_STRING;
    	return new ExtendedScannerInfo(macroMap,includePaths,macroFiles,includeFiles);
    }

    private Map<String, String> getGnuMap() {
        Map<String, String> map= new HashMap<>();
        map.put("__GNUC__", "4");
        map.put("__GNUC_MINOR__", "7");
        map.put("__SIZEOF_SHORT__", "2");
        map.put("__SIZEOF_INT__", "4");
        map.put("__SIZEOF_LONG__", "8");
        map.put("__SIZEOF_POINTER__", "8");
        return map;
    }

    private Map<String, String> getStdMap() {
        Map<String, String> map= new HashMap<>();
        map.put("__SIZEOF_SHORT__", "2");
        map.put("__SIZEOF_INT__", "4");
        map.put("__SIZEOF_LONG__", "8");
        map.put("__SIZEOF_POINTER__", "8");
        return map;
    }
}

