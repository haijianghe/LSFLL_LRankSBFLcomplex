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

/**   C语言 程序
 * @author Administrator
 *
 */
public class AstProcedureFileParse {
	private IASTTranslationUnit unitCompile;
    private final static IParserLogService NULL_LOG = new NullLogService();
    private IncludeFileResolve resolveIncludeFile;  //获取待解析文件的头文件。

    public AstProcedureFileParse()
    {
    	unitCompile = null;
    	resolveIncludeFile = null;
    }
    
    /**
	 * @param cFilePath  带目录的文件名。
	 * @return 解析后的结果，一个文件对应一个类。
	 */
	public ClassContext parseFile(String cFilePath)
	{
		resolveIncludeFile = new IncludeFileResolve(cFilePath);
		resolveIncludeFile.resolve();
		//先由上面的代码获取待解析文件的头文件。再加入到解析过程。

		boolean result=true;
		ClassContext clazzCtx = null;
		if( createCompilationUnit(cFilePath)  )
		{
			//处理预编译
			//PreprocessorInactiveStatement.parseInactiveStatement(unitCompile,cFilePath);
			
			int pos = cFilePath.lastIndexOf("\\");
			String parsingFilename = cFilePath.substring(pos+1);
			ProcedureCodeVisitor cdtVisitor  = new ProcedureCodeVisitor(parsingFilename);
			unitCompile.accept(cdtVisitor);
			clazzCtx = cdtVisitor.getClazzContext();
		    //此前，在计算复杂度时，并未考虑全局变量的影响。在此处一并计算。
			adjustComplexMetricWithGlobalVariable(clazzCtx);
			// 处理宏定义带来的复杂度。
			ProcessMacroDefineition(clazzCtx); 
		}
		else
			result = false;
		if( result )
			return clazzCtx;
		else
			return null;
	}
	
	 /*此前，在计算复杂度时，并未考虑全局变量的影响。在此处一并计算。
		以后还要考虑.h文件的全局变量。
		1，将虚拟类的attributes当做此C文件的全局变量。
		2，对所有方法的所有语句增加全局变量（）带来的复杂度，全局变量定义语句也不例外。
	*/
    private void adjustComplexMetricWithGlobalVariable(ClassContext clazzCtx)
    {
    	//clazzCtx.cAdjustComplexMetricWithGlobalVariable();
    }
	    
	
	//cppFilePath:带目录的文件名。
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
        // garcia.wul 最后一个参数：IncludeFileContentProvider传null，不然会报workspace is closed错误
        //IncludeFileContentProvider ifcp = IncludeFileContentProvider.getEmptyFilesProvider();
        IncludeFileContentProvider ifcp = createInternalFileContentProvider(true);
        return new CPreprocessor(
            fileContent, scannerInfo, parserLanguage, NULL_LOG, configuration, ifcp
        );
    }

    /**  #include  头文件解析。
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
     * 处理宏定义带来的复杂度。
     * 对象形式的宏定义, IASTPreprocessorObjectStyleMacroDefinition
     * 和函数形式的宏定义 IASTPreprocessorFunctionStyleMacroDefinition，它们的复杂度赋予相同值。
     */
    private void ProcessMacroDefineition(ClassContext clazzCtx)
    {
    	IASTPreprocessorMacroDefinition[] 	iapMacros = unitCompile.getMacroDefinitions();
    	List<String> macroDefines = new ArrayList<>();
		for( IASTPreprocessorMacroDefinition item: iapMacros )
			macroDefines.add(item.getName().toString());
		if( macroDefines.size()>0 )//有宏定义
			clazzCtx.adjustComplexMetricWithMacroDefinition(macroDefines);
    }
}

