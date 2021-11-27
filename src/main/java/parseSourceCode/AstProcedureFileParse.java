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

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorElifStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorElseStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorEndifStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfdefStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfndefStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
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
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexFileSet;
import org.eclipse.cdt.core.parser.ExtendedScannerInfo;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IScannerInfo;
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
import org.eclipse.cdt.internal.core.parser.scanner.InternalFileContentProvider;
import org.eclipse.cdt.internal.core.parser.scanner.InternalFileContent.InclusionKind;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.pldt.openmp.analysis.PAST.PASTEndif;
import org.eclipse.ptp.pldt.openmp.analysis.PAST.PASTIfdef;

/**   C���� ����
 * @author Administrator
 *
 */
public class AstProcedureFileParse {
	private IASTTranslationUnit unitCompile;
    private final static IParserLogService NULL_LOG = new NullLogService();
    private IncludeFileResolve resolveIncludeFile;  //��ȡ�������ļ���ͷ�ļ���

    public AstProcedureFileParse()
    {
    	unitCompile = null;
    	resolveIncludeFile = null;
    }
    
    /**
	 * @param cFilePath  ��Ŀ¼���ļ�����
	 * @return ������Ľ����һ���ļ���Ӧһ���ࡣ
	 */
	public ClassContext parseFile(String cFilePath)
	{
		resolveIncludeFile = new IncludeFileResolve(cFilePath);
		resolveIncludeFile.resolve();
		//��������Ĵ����ȡ�������ļ���ͷ�ļ����ټ��뵽�������̡�

		boolean result=true;
		ClassContext clazzCtx = null;
		if( createCompilationUnit(cFilePath)  )
		{
			//����Ԥ����
			//PreprocessorInactiveStatement.parseInactiveStatement(unitCompile,cFilePath);
			
			int pos = cFilePath.lastIndexOf("\\");
			String parsingFilename = cFilePath.substring(pos+1);
			ProcedureCodeVisitor cdtVisitor  = new ProcedureCodeVisitor(parsingFilename);
			unitCompile.accept(cdtVisitor);
			clazzCtx = cdtVisitor.getClazzContext();
		    //��ǰ���ڼ��㸴�Ӷ�ʱ����δ����ȫ�ֱ�����Ӱ�졣�ڴ˴�һ�����㡣
			adjustComplexMetricWithGlobalVariable(clazzCtx);
			// ����궨������ĸ��Ӷȡ�
			ProcessMacroDefineition(clazzCtx); 
		}
		else
			result = false;
		if( result )
			return clazzCtx;
		else
			return null;
	}
	
	 /*��ǰ���ڼ��㸴�Ӷ�ʱ����δ����ȫ�ֱ�����Ӱ�졣�ڴ˴�һ�����㡣
		�Ժ�Ҫ����.h�ļ���ȫ�ֱ�����
		1�����������attributes������C�ļ���ȫ�ֱ�����
		2�������з����������������ȫ�ֱ������������ĸ��Ӷȣ�ȫ�ֱ����������Ҳ�����⡣
	*/
    private void adjustComplexMetricWithGlobalVariable(ClassContext clazzCtx)
    {
    	//clazzCtx.cAdjustComplexMetricWithGlobalVariable();
    }
	    
	
	//cppFilePath:��Ŀ¼���ļ�����
	private boolean createCompilationUnit(String cFilePath)
	{
		unitCompile = parse( cFilePath);
		if( unitCompile==null )
			return false;
		else
			return true;
    }

	/*
	 * alibaba
	 */
    private IASTTranslationUnit parse( String cFilePath )
    {
        IScanner scanner = null;
		boolean result=true;
        byte[] input = null;
		try {
		    BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(cFilePath));
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

        boolean useGNUExtensions = true;
        boolean skipTrivialInitializers  = false;

        scanner = createScanner(
            FileContent.create(cFilePath, codeStream),
            ParserLanguage.C,
            ParserMode.COMPLETE_PARSE, //Follow inclusions, parse function/method bodies.
            createScannerInfo(useGNUExtensions)
        );

        AbstractGNUSourceCodeParser gnuSourceCodeParser = null;
        ICParserExtensionConfiguration configuration = useGNUExtensions ?
            new GCCParserExtensionConfiguration():  //supportKnRC
            new ANSICParserExtensionConfiguration(); //disable KRC
        gnuSourceCodeParser = new GNUCSourceParser(
            scanner, ParserMode.COMPLETE_PARSE, NULL_LOG, configuration, null );
        if (skipTrivialInitializers) {
        	gnuSourceCodeParser.setMaximumTrivialExpressionsInAggregateInitializers(1);
            //gnuSourceCodeParser.setSkipTrivialExpressionsInAggregateInitializers(true);
        }
        return gnuSourceCodeParser.parse();
    }

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
    
    /*
     * ����궨������ĸ��Ӷȡ�
     * ������ʽ�ĺ궨��, IASTPreprocessorObjectStyleMacroDefinition
     * �ͺ�����ʽ�ĺ궨�� IASTPreprocessorFunctionStyleMacroDefinition�����ǵĸ��Ӷȸ�����ֵͬ��
     */
    private void ProcessMacroDefineition(ClassContext clazzCtx)
    {
    	IASTPreprocessorMacroDefinition[] 	iapMacros = unitCompile.getMacroDefinitions();
    	List<String> macroDefines = new ArrayList<>();
		for( IASTPreprocessorMacroDefinition item: iapMacros )
			macroDefines.add(item.getName().toString());
		if( macroDefines.size()>0 )//�к궨��
			clazzCtx.adjustComplexMetricWithMacroDefinition(macroDefines);
    }
}

