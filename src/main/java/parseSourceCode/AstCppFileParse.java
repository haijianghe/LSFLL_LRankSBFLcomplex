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

/** C++ 的解析。
 * @author Administrator
 *
 */
public class AstCppFileParse {
	private IASTTranslationUnit unitCompile;
    private final static IParserLogService NULL_LOG = new NullLogService();
    private IncludeFileResolve resolveIncludeFile;  //获取待解析文件的头文件。
    
    public AstCppFileParse()
    {
    	unitCompile = null;
    	resolveIncludeFile = null;
    }
    
    /**
	 * @param cppFilePath  带目录的文件名。
	 * @param clazzList 解析后的结果存入此队列
	 * @return
	 */
	public List<ClassContext> parseFile(String cppFilePath)
	{
		resolveIncludeFile = new IncludeFileResolve(cppFilePath);
		resolveIncludeFile.resolve();
		//先由上面的代码获取待解析文件的头文件。再加入到解析过程。
		
		boolean result=true;
		List<ClassContext> clazzList = null;
		if( createCompilationUnit(cppFilePath)  )
		{
			int pos = cppFilePath.lastIndexOf("\\");
			String parsingFilename = cppFilePath.substring(pos+1);
			CppCodeVisitor cdtVisitor  = new CppCodeVisitor(parsingFilename);
			unitCompile.accept(cdtVisitor);
			clazzList = cdtVisitor.getClazzList();
		    //此前，在计算类的属性影响语句的复杂度时，并未考虑外部类属性的影响。在此处一并计算。
			//必须放在adjustNestedNestedClass(clazzList)之前，因为此时的链条关系还清晰。
		    adjustNestedComplexMetricWithAttribute(clazzList);
			//子类只允许一次嵌套。调整内部类里的各种嵌套类。
			adjustNestedNestedClass(clazzList);
		    //此前，在计算复杂度时，并未考虑全局变量的影响。在此处一并计算。
			adjustComplexMetricWithGlobalVariable(clazzList);
			// 处理宏定义带来的复杂度。
			ProcessMacroDefineition(clazzList); 
		}
		else
			result = false;
		if( result )
			return clazzList;
		else
			return null;
	}
	
	 /*此前，在计算复杂度时，并未考虑全局变量的影响。在此处一并计算。
	以后还要考虑.h文件的全局变量。
	1,找出类型为14的类,C++程序的变量声明语句放在该虚拟类（TopDeclartionCpp类）中。
	2，虚拟类的attributes当做此C++文件的全局变量。
	3，遍历所有类的所有方法，方法的所有语句增加全局变量（）带来的复杂度，全局变量定义语句也不例外。
	*/
	private void adjustComplexMetricWithGlobalVariable(List<ClassContext> clazzLst)
	{
		ClassContext topDeclarationClass = null;
		for( ClassContext ctx :  clazzLst )
		{
			if( ctx.isTopDeclartionClass() ) //14，C++全局变量，文件顶层变量声明语句，组成的TopDeclartionCpp类。
			{
				topDeclarationClass = ctx;
				break;
			}
		}
		if( topDeclarationClass==null )
			return;  //该文件没有全局变量
		//该类的属性即文件的全局变量。
		List<String> globalVariables = topDeclarationClass.getAttributes();
		//没有好的做法，全局变量添加到所有类，作为所有类的属性
		for( ClassContext ctx :  clazzLst )
			ctx.cppAdjustAttributeWithGlobalVariable(globalVariables);
	}

	 /*此前，在计算类的属性影响语句的复杂度时，并未考虑外部类属性的影响。在此处一并计算。
		必须放在adjustNestedNestedClass(clazzList)之前，因为此时的链条关系还清晰。
	*/
    private void adjustNestedComplexMetricWithAttribute(List<ClassContext> clazzLst)
    {
        Iterator<ClassContext> iterator = clazzLst.iterator();
        while (iterator.hasNext()) 
		{
        	ClassContext cnode = iterator.next();
			if( !cnode.isNesting() )
				continue; //顶层类，无须调整。
			//cnode是内部类，逐级找出其所有外部类。
			ClassContext parentNode = getClassNode(clazzLst,cnode.getParentName());
			do
			{
				List<String> parentAttributes = parentNode.getAttributes();//该类的属性名列表。
				cnode.addOutterAttribute(parentAttributes);
				parentNode = getClassNode(clazzLst,parentNode.getParentName());
			}while (parentNode!=null); 
		}
    }
	    
	/**调整内部类里的各种嵌套类（不失一般性，假设为inninn）。
	 * 调整算法，将inninn里的方法，移动到其最顶层类；方法的其它值不变，将type改为5.
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
			//是内部类。
			ClassContext parentNode = getClassNode(clazzLst,cnode.getParentName());
			//如果其外部类也是嵌套类（内部类，局部类，匿名类）,则删除此类的数据，将其数据移动到次顶层类。
			if( parentNode.isNesting() )
			{
				ClassContext subTopParent = subTopParentClass(clazzLst,parentNode);
				//虽然该方法被删除，但其语句数据得保留。
				subTopParent.mergeInner2Class(cnode);
				cnode.setWillRemove(true);//设置删除标记。
				//iterator.remove();//使用迭代器的删除方法删除
			}
		}//end of while.
        
        //删除二级及以上的嵌套类。
        iterator = clazzLst.iterator();
        while (iterator.hasNext()) 
		{
        	ClassContext cnode = (ClassContext)(iterator.next());
        	if( cnode.isWillRemove() )
        		iterator.remove();//使用迭代器的删除方法删除
		}//end of while
	}

	/*
     * 处理宏定义带来的复杂度。
     * 对象形式的宏定义, IASTPreprocessorObjectStyleMacroDefinition
     * 和函数形式的宏定义 IASTPreprocessorFunctionStyleMacroDefinition，它们的复杂度赋予相同值。
     */
    private void ProcessMacroDefineition(List<ClassContext> clazzLst)
    {
    	IASTPreprocessorMacroDefinition[] 	iapMacros = unitCompile.getMacroDefinitions();
    	List<String> macroDefines = new ArrayList<>();
		for( IASTPreprocessorMacroDefinition item: iapMacros )
			macroDefines.add(item.getName().toString());
		if( macroDefines.size()>0 )//有宏定义
		{
			for(ClassContext clazzCtx : clazzLst )
				clazzCtx.adjustComplexMetricWithMacroDefinition(macroDefines);
		}
    }
    
	/** 依据类的名字获取类节点。
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
	
	/**  获取ccNode次顶层的外部类节点，最顶层下一级的。
	 * @param ccNode  肯定是内部类，其isNesting=true，而且ccNode有内部类。
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
	
	//cppFilePath:带目录的文件名。
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
            //ParserMode注释：https://www.cct.lsu.edu/~rguidry/eclipse-doc36/src-html/org/eclipse/cdt/core/parser/ParserMode.html#line.31
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

