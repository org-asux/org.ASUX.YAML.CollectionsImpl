/*
 BSD 3-Clause License
 
 Copyright (c) 2019, Udaybhaskar Sarma Seetamraju
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
 
 * Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer.
 
 * Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.
 
 * Neither the name of the copyright holder nor the names of its
 contributors may be used to endorse or promote products derived from
 this software without specific prior written permission.
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.ASUX.yaml.CollectionsImpl;

import org.ASUX.yaml.YAMLPath;
import org.ASUX.yaml.YAML_Libraries;
import org.ASUX.yaml.MemoryAndContext;
import org.ASUX.yaml.CmdLineArgs;
import org.ASUX.yaml.CmdLineArgsBatchCmd;
import org.ASUX.yaml.CmdLineArgsInsertCmd;
import org.ASUX.yaml.CmdLineArgsMacroCmd;
import org.ASUX.yaml.CmdLineArgsReplaceCmd;
import org.ASUX.yaml.CmdLineArgsTableCmd;
import org.ASUX.yaml.JSONTools;
import org.ASUX.common.Tuple;
import org.ASUX.common.Output.OutputType;
import org.ASUX.common.Output;
import org.ASUX.common.Debug;

import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.InputStream;

import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Properties;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * <p>
 * This org.ASUX.yaml GitHub.com project and the
 * <a href="https://github.com/org-asux/org.ASUX.cmdline">org.ASUX.cmdline</a>
 * GitHub.com projects.
 * </p>
 * <p>
 * This class is the "wrapper-processor" for the various "YAML-commands" (which
 * traverse a YAML file to do what you want).
 * </p>
 * <p>
 * The 4 YAML-COMMANDS are: <b>read/query, list, delete</b> and <b>replace</b>.
 * </p>
 * <p>
 * See full details of how to use these commands - in this GitHub project's wiki
 * - or - in
 * <a href="https://github.com/org-asux/org.ASUX.cmdline">org.ASUX.cmdline</a>
 * GitHub.com project and its wiki.
 * </p>
 *
 * <p>
 * Example:
 * <code>java org.ASUX.yaml.Cmd --delete --yamlpath "paths.*.*.responses.200" -i $cwd/src/test/my-petstore-micro.yaml -o /tmp/output2.yaml  --double-quote</code><br>
 * Example: <b><code>java org.ASUX.yaml.Cmd</code></b> will show all command
 * line options supported.
 * </p>
 * 
 * @see org.ASUX.yaml.YAMLPath
 * @see org.ASUX.yaml.CmdLineArgs
 *
 * @see ReadYamlEntry
 * @see ListYamlEntry
 * @see DeleteYamlEntry
 * @see ReplaceYamlEntry
 */
public class CmdInvoker extends org.ASUX.yaml.CmdInvoker {

    private static final long serialVersionUID = 212L;

    public static final String CLASSNAME = CmdInvoker.class.getName();

    // private static final String TMP FILE = System.getProperty("java.io.tmpdir") +"/STDOUT.txt";

    private final Tools tools;
    private transient GenericYAMLScanner YAMLScanner;
    private transient GenericYAMLWriter YAMLWriter;

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================
    /**
     *  The constructor exclusively for use by  main() classes anywhere.
     *  @param _verbose Whether you want deluge of debug-output onto System.out.
     *  @param _showStats Whether you want a final summary onto console / System.out
     */
    public CmdInvoker( final boolean _verbose, final boolean _showStats ) {
        this( _verbose, _showStats, null );
    }

    /**
     *  Variation of constructor that allows you to pass-in memory from another previously existing instance of this class.  Useful within {@link BatchYamlProcessor} which creates new instances of this class, whenever it encounters a YAML or AWS command within the Batch-file.
     *  @param _verbose Whether you want deluge of debug-output onto System.out.
     *  @param _showStats Whether you want a final summary onto console / System.out
     *  @param _memoryAndContext pass in memory from another previously existing instance of this class.  Useful within {@link BatchYamlProcessor} which creates new instances of this class, whenever it encounters a YAML or AWS command within the Batch-file.
     */
    public CmdInvoker( final boolean _verbose, final boolean _showStats, final MemoryAndContext _memoryAndContext ) {
        super(_verbose, _showStats, _memoryAndContext );
        init();
        tools = new Tools( _verbose );
    }

    private void init() {
        this.YAMLScanner = new GenericYAMLScanner( this.verbose );
        this.YAMLWriter = new GenericYAMLWriter( this.verbose );
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     * Reference to the implementation of the YAML read/parsing ONLY
     * @return a reference to the YAML Library in use.
     */
    public GenericYAMLScanner getYamlScanner() {
        return this.YAMLScanner;
    }

    /**
     * Reference to the implementation of the YAML read/parsing ONLY
     * @return a reference to the YAML Library in use.
     */
    public GenericYAMLWriter getYamlWriter() {
        return this.YAMLWriter;
    }

    /**
     * know which YAML-parsing/emitting library was chosen by user.  Ideally used within a Batch-Yaml script / BatchYamlProcessor.java
     * @return the YAML-library in use. See {@link YAML_Libraries} for legal values to this parameter
     */
    public YAML_Libraries getYamlLibrary() {
        // why make this check below with assert()?
        // Why shouln't users use one library to read YAML and another to write YAML?
        final YAML_Libraries sclib = this.YAMLScanner.getYamlLibrary();
        // String s = sclib.toString();
        // s = (s==null) ? "null" : s;
        // assert( s.equals( this.YAMLWriter.getYamlLibrary() ) );
        assert( sclib == this.YAMLWriter.getYamlLibrary() );
        return sclib;
    }

    /**
     * Allows you to set the YAML-parsing/emitting library of choice.  Ideally used within a Batch-Yaml script.
     * @param _l the YAML-library to use going forward. See {@link YAML_Libraries} for legal values to this parameter
     */
    public void setYamlLibrary( final YAML_Libraries _l ) {
        if ( this.YAMLScanner == null || this.YAMLWriter == null )
            this.init();
        this.YAMLScanner.setYamlLibrary(_l);
        this.YAMLWriter.setYamlLibrary(_l);
    }

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

    /**
     *  <p>Example: For SnakeYAML-library based subclass of this, this should return DumperOptions.class</p>
     *  <p>This is to be used primarily within BatchCmdProcessor.onAnyCmd().</p>
     *  @return name of class of the object that subclasses of {@link CmdInvoker} use, to configure YAML-Output (example: SnakeYAML uses DumperOptions)
     */
    @Override
    public Class<?> getLibraryOptionsClass() {
        return String.class;
    }

    /**
     *  <p>Example: For SnakeYAML-library based subclass of this, this should return the reference to the instance of the class DumperOption</p>
     *  <p>This is to be used primarily within BatchCmdProcessor.onAnyCmd().</p>
     * @return instance/object that subclasses of {@link CmdInvoker} use, to configure YAML-Output (example: SnakeYAML uses DumperOptions objects)
     */
    @Override
    public Object getLibraryOptionsObject() {
        return null;
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     *  This function is meant to be used by Cmd.main() and by BatchProcessor.java.  Read the code *FIRST*, to see if you can use this function too.
     *  @param _cmdLineArgs yes, everything passed as commandline arguments to the Java program / org.ASUX.yaml.Cmd
     *  @param _inputData _the YAML inputData that is the input to pretty much all commands
     *  @return either a String, java.utils.LinkedHashMap&lt;String, Object&gt;
     *  @throws YAMLPath.YAMLPathException if Pattern for YAML-Path provided is either semantically empty or is NOT java.util.Pattern compatible.
     *  @throws FileNotFoundException if the filenames within _cmdLineArgs do NOT exist
     *  @throws IOException if the filenames within _cmdLineArgs give any sort of read/write troubles
     *  @throws Exception by ReplaceYamlCmd method and this nethod (in case of unknown command)
     */
    public Object processCommand ( org.ASUX.yaml.CmdLineArgsCommon _cmdLineArgs, final Object _inputData )
                throws FileNotFoundException, IOException, Exception,
                YAMLPath.YAMLPathException
    {
        assertTrue( _cmdLineArgs instanceof org.ASUX.yaml.CmdLineArgs );
        final org.ASUX.yaml.CmdLineArgs cmdLineArgs = (org.ASUX.yaml.CmdLineArgs) _cmdLineArgs;
        final String HDR = CLASSNAME + ": processCommand("+ cmdLineArgs.cmdType +"): ";

        assert( _inputData instanceof LinkedHashMap );
        @SuppressWarnings("unchecked")
        final LinkedHashMap<String, Object> _inputMap = (LinkedHashMap<String, Object>) _inputData;

        // This entire CollectionsImpl library clearly is chained to the EsotericSoftware Yamlbeans library.
        // So, let's mae that explicit
        this.getYamlScanner().setYamlLibrary( YAML_Libraries.ESOTERICSOFTWARE_Library );
        this.getYamlWriter().setYamlLibrary( YAML_Libraries.ESOTERICSOFTWARE_Library );

        switch ( cmdLineArgs.cmdType ) {
        case READ:
            ReadYamlEntry readcmd = new ReadYamlEntry( cmdLineArgs.verbose, cmdLineArgs.showStats );
            readcmd.searchYamlForPattern( _inputMap, cmdLineArgs.yamlRegExpStr, cmdLineArgs.yamlPatternDelimiter );
            final LinkedList<Object> outputStr = readcmd.getOutput();
            return outputStr;

        case LIST:
            ListYamlEntry listcmd = new ListYamlEntry( cmdLineArgs.verbose, cmdLineArgs.showStats, YAMLPath.DEFAULTPRINTDELIMITER );
            listcmd.searchYamlForPattern( _inputMap, cmdLineArgs.yamlRegExpStr, cmdLineArgs.yamlPatternDelimiter );
            final ArrayList<String> outputStr2 = listcmd.getOutput();
            return outputStr2;

        case DELETE:
            if ( cmdLineArgs.verbose ) System.out.println(CLASSNAME + ": processCommand(isDelCmd): about to start DELETE command");
            DeleteYamlEntry delcmd = new DeleteYamlEntry( cmdLineArgs.verbose, cmdLineArgs.showStats );
            delcmd.searchYamlForPattern( _inputMap, cmdLineArgs.yamlRegExpStr, cmdLineArgs.yamlPatternDelimiter );
            return _inputMap;

        case TABLE:
            final CmdLineArgsTableCmd claTbl = (CmdLineArgsTableCmd) cmdLineArgs;
            if (claTbl.verbose) System.out.println(CLASSNAME + ": processCommand(isTableCmd):  claTbl.yamlRegExpStr="+ claTbl.yamlRegExpStr +" & tableColumns=[" + claTbl.tableColumns +"]" );
            TableYamlQuery tblcmd = new TableYamlQuery( claTbl.verbose, claTbl.showStats, claTbl.tableColumns, claTbl.yamlPatternDelimiter );
            tblcmd.searchYamlForPattern( _inputMap, claTbl.yamlRegExpStr, claTbl.yamlPatternDelimiter );
            final LinkedList< ArrayList<String> > output = tblcmd.getOutput();
            return output;

        case INSERT:
            final CmdLineArgsInsertCmd claIns = (CmdLineArgsInsertCmd) cmdLineArgs;
            if (claIns.verbose) System.out.println(CLASSNAME + ": processCommand(isInsertCmd):  claIns.yamlRegExpStr="+ claIns.yamlRegExpStr +" & loading @Insert-file: " + claIns.insertFilePath);
            final Object newContent = this.getDataFromReference( claIns.insertFilePath );
            if (claIns.verbose) System.out.println( CLASSNAME + ": processCommand(isInsertCmd): about to start INSERT command using: [" + newContent.toString() + "]");
            // Within a Batch-YAML context, the output of the previous line does NOT have to be a LinkedHashMap.
            // In such a case, an ArrayList or LinkedList object is converted into one -- by Tools.wrapAnObject_intoLinkedHashMap().
            // So, we will use the inverse-function Tools.getTheActualObject() to undo that.
            InsertYamlEntry inscmd = new InsertYamlEntry( claIns.verbose, claIns.showStats, new Output(this.verbose).getTheActualObject( newContent ) );
            inscmd.searchYamlForPattern( _inputMap, claIns.yamlRegExpStr, claIns.yamlPatternDelimiter );
            return _inputMap;

        case REPLACE:
            final CmdLineArgsReplaceCmd claRepl = (CmdLineArgsReplaceCmd) cmdLineArgs;
            if (claRepl.verbose) System.out.println(CLASSNAME + ": processCommand(isReplaceCmd): loading @Replace-file: " + claRepl.replaceFilePath);
            final Object replContent = this.getDataFromReference( claRepl.replaceFilePath );
            if (claRepl.verbose) System.out.println( CLASSNAME + ": processCommand(isReplaceCmd): about to start CHANGE/REPLACE command using: [" + replContent.toString() + "]");
            ReplaceYamlEntry replcmd = new ReplaceYamlEntry( claRepl.verbose, claRepl.showStats, replContent );
            replcmd.searchYamlForPattern( _inputMap, claRepl.yamlRegExpStr, claRepl.yamlPatternDelimiter );
            return _inputMap;

        case MACROYAML:
        case MACRO:
            final CmdLineArgsMacroCmd claMacro = (CmdLineArgsMacroCmd) cmdLineArgs;
            if (claMacro.verbose) System.out.println( HDR +" loading Props file [" + claMacro.propertiesFilePath + "]");
            assertTrue( claMacro.propertiesFilePath != null );

            MacroYamlProcessor macroYamlPr = null;
            // MacroStringProcessor macroStrPr = null;

            switch ( cmdLineArgs.cmdType ) {
                case MACRO:     assertTrue( false ); // we can't get here with '_input' ..  _WITHOUT_ it being a _VALID_ YAML content.   So, so might as well as use 'MacroYamlProcessor'
                                // macroStrPr = new MacroStringProcessor( claMacro.verbose, claMacro.showStats ); // does NOT use 'dumperopt'
                                break;
                case MACROYAML: macroYamlPr = new MacroYamlProcessor( claMacro.verbose, claMacro.showStats ); // does NOT use 'dumperopt'
                                break;
                default: assertTrue( false ); // should not be here.
            }

            Properties properties = null;
            if ( "!AllProperties".equals( claMacro.propertiesFilePath ) ) {
                // do Nothing.   properties will remain set to 'null'
            } else {
                final Object content = this.getDataFromReference( claMacro.propertiesFilePath );
                if (content instanceof Properties) {
                    properties = (Properties) content;
                }else {
                    throw new Exception( claMacro.propertiesFilePath +" is Not a java properties file, with the extension '.properties' .. or, it's contents (of type'"+ content.getClass().getName() +"')are Not compatible with java.util.Properties" );
                }
            }

            if (claMacro.verbose) System.out.println( HDR +" about to start MACRO command using: [Props file [" + claMacro.propertiesFilePath + "]");
            final LinkedHashMap<String, Object> outpMap = new LinkedHashMap<>();
            switch ( cmdLineArgs.cmdType ) {
                case MACRO:     assertTrue( false ); // we can't get here with '_input' ..  _WITHOUT_ it being a _VALID_ YAML content.   So, so might as well as use 'MacroYamlProcessor'
                                // outpData = macroStrPr.searchNReplace( raw-java.lang.String-from-where??, properties, this.memoryAndContext.getAllPropsRef() );
                                break;
                case MACROYAML: macroYamlPr.recursiveSearch( _inputMap, outpMap, properties, this.memoryAndContext.getAllPropsRef() );
                                break;
                default: assertTrue( false ); // should not be here.
            }

            // writer.write(outpMap); // The contents of java.util.LinkedHashMap<String, Object> has been updated with replacement strings. so, dump it.
            return outpMap;

        case BATCH:
            final CmdLineArgsBatchCmd claBatch = (CmdLineArgsBatchCmd) cmdLineArgs;
            if (claBatch.verbose) System.out.println( HDR +" about to start BATCH command using: BATCH file [" + claBatch.batchFilePath + "]");
            final BatchYamlProcessor batcher = new BatchYamlProcessor( claBatch.verbose, claBatch.showStats );
            batcher.setMemoryAndContext( this.memoryAndContext );
            // final LinkedHashMap<String, Object> outpMap2 = new LinkedHashMap<String, Object>();
            final LinkedHashMap<String, Object> outpMap2 = batcher.go( claBatch.batchFilePath, _inputMap );
            if ( this.verbose ) System.out.println( HDR +" outpMap2 =" + outpMap2 +"\n\n");
            // writer.write(outpMap2); // The contents of java.util.LinkedHashMap<String, Object> has been updated with replacement strings. so, dump it.
            return outpMap2;

        default:
            final String es = CLASSNAME + ": processCommand(): Unimplemented command: " + _cmdLineArgs.toString();
            System.err.println( es );
            throw new Exception( es );
        }
        // return null; // should Not reach here!
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================
    /**
     * This is a simpler facade/interface to {@link InputsOutputs#getDataFromReference}, for use by {@link BatchYamlProcessor}
     * @param _src a javalang.String value - either inline YAML/JSON, or a filename (must be prefixed with '@'), or a reference to a property within a Batch-file execution (must be prefixed with a '!')
     * @return an object (either any of Node, SequenceNode, MapNode, ScalarNode ..)
     * @throws FileNotFoundException if the filenames within _cmdLineArgs do NOT exist
     * @throws IOException if the filenames within _cmdLineArgs give any sort of read/write troubles
     * @throws Exception by ReplaceYamlCmd method and this nethod (in case of unknown command)
     */
    public Object getDataFromReference( final String _src )
                                throws FileNotFoundException, IOException, Exception
    {   return InputsOutputs.getDataFromReference( _src, this.memoryAndContext, this.getYamlScanner(), this.verbose );
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /**
     * This is a simpler facade/interface to {@link InputsOutputs#saveDataIntoReference}, for use by {@link BatchYamlProcessor}
     * @param _dest a javalang.String value - either a filename (must be prefixed with '@'), or a reference to a (new) property-variable within a Batch-file execution (must be prefixed with a '!')
     * @param _input the object to be saved using the reference provided in _dest paramater
     * @throws FileNotFoundException if the filenames within _cmdLineArgs do NOT exist
     * @throws IOException if the filenames within _cmdLineArgs give any sort of read/write troubles
     * @throws Exception by ReplaceYamlCmd method and this nethod (in case of unknown command)
     */
    public void saveDataIntoReference( final String _dest, final Object _input )
                            throws FileNotFoundException, IOException, Exception
    {   InputsOutputs.saveDataIntoReference( _dest, _input, this.memoryAndContext, this.getYamlWriter(), this.verbose );
    }
                        
    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /**
     *  <p>This method needs to supplement org.ASUX.YAML.CmdInvoker.deepClone() as this subclass (org.ASUX.YAML.NodeImpl.CmdInvoker) has it's own transient instance-fields/variables.</p>
     *  <p>Such Transients are made Transients for only ONE-SINGLE REASON - they are NOT serializable).</p>
     *  <p>!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! ATTENTION !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!</p>
     *  <p>So, after a deepClone() of CmdInvoker.java .. you'll need to call: </p>
     *  <p> <code> clone.dumperopt = origObj.dumperopt; </code> <br>
     *  @param origObj the non-null original to clone
     *  @return a properly cloned and re-initiated clone of the original (that works around instance-variables that are NOT serializable)
     *  @throws Exception when org.ASUX.common.Utils.deepClone clones the core of this class-instance 
     */
    public static CmdInvoker deepClone( final CmdInvoker origObj ) throws Exception {
        final org.ASUX.yaml.CmdInvoker newCmdInvk = org.ASUX.yaml.CmdInvoker.deepClone( origObj );
        final CmdInvoker newCmdinvoker = (CmdInvoker) newCmdInvk;
        return newCmdinvoker;
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    // /**
    //  * This functon takes a single parameter that is a javalang.String value - and, either detects it to be inline YAML/JSON, or a filename (must be prefixed with '@'), or a reference to something saved in {@link MemoryAndContext} within a Batch-file execution (must be prefixed with a '!')
    //  * @param _src a javalang.String value - either inline YAML/JSON, or a filename (must be prefixed with '@'), or a reference to a property within a Batch-file execution (must be prefixed with a '!')
    //  * @return an object (either LinkedHashMap, ArrayList or LinkedList)
    //  * @throws FileNotFoundException if the filenames within _cmdLineArgs do NOT exist
    //  * @throws IOException if the filenames within _cmdLineArgs give any sort of read/write troubles
    //  * @throws Exception by ReplaceYamlCmd method and this nethod (in case of unknown command)
    //  */
    // public Object getDataFromReference( final String _src  )
    //             throws FileNotFoundException, IOException, Exception
    // {
    //     if ( _src == null || _src.trim().length() <= 0 )
    //         return null;
    //     final Tools tools = new Tools( this.verbose );

    //     if ( _src.startsWith("@") ) {
    //         final String srcFile = _src.substring(1);
    //         final InputStream fs = new FileInputStream( srcFile );
    //         if ( srcFile.endsWith(".json") ) {
    //             if ( this.verbose ) System.out.println( CLASSNAME +" getDataFromReference("+ _src +"): detected a JSON-file provided via '@'." );
    //             //     // https://github.com/google/gson/blob/master/gson/src/main/java/com/google/gson/Gson.java
    //             // final LinkedHashMap<String, Object> retMap2 = ..
    //             // tempOutputMap = new com.google.gson.Gson().fromJson(  reader1,
    //             //                        new com.google.gson.reflect.TypeToken< LinkedHashMap<String, Object> >() {}.getType()   );
    //             // http://tutorials.jenkov.com/java-json/jackson-objectmapper.html#read-map-from-json-string 
    //             com.fasterxml.jackson.databind.ObjectMapper objMapper = new com.fasterxml.jackson.databind.ObjectMapper();
    //             objMapper.configure( com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true );
    //             objMapper.configure( com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
    //                 com.fasterxml.jackson.databind.type.MapType type = objMapper.getTypeFactory().constructMapType( LinkedHashMap.class, String.class, Object.class );
    //             LinkedHashMap<String, Object> retMap2 = null;
    //             retMap2 = objMapper.readValue( fs, new com.fasterxml.jackson.core.type.TypeReference< LinkedHashMap<String,Object> >(){}  );
    //             if ( this.verbose ) System.out.println( CLASSNAME +" getDataFromReference("+ _src +"): jsonMap loaded BY OBJECTMAPPER into tempOutputMap =" + retMap2 );
    //             retMap2 = tools.lintRemoverMap( retMap2 );
    //             fs.close();
    //             return retMap2;
    //         } else if ( srcFile.endsWith(".yaml") ) {
    //             if ( this.verbose ) System.out.println( CLASSNAME +" getDataFromReference("+ _src +"): detected a YAML-file provided via '@'." );
    //             final java.io.Reader reader1 = new java.io.InputStreamReader( fs  );
    //             final org.ASUX.common.Output.Object<?> output = this.getYamlScanner().load( reader1 );
    //             final LinkedHashMap<String, Object> mapObj = output.getMap(); // new YamlReader( reader1 ).read( LinkedHashMap.class );
    //             reader1.close(); // automatically includes fs.close();
    //             // final LinkedHashMap<String, Object> mapObj = new YamlReader( reader1 ).read( LinkedHashMap.class );
    //             // @SuppressWarnings("unchecked")
    //             // final LinkedHashMap<String, Object> retMap3 = (LinkedHashMap<String, Object>) mapObj;
    //             if ( this.verbose ) System.out.println( CLASSNAME +" getDataFromReference("+ _src +"): YAML loaded into tempOutputMap =" + mapObj );
    //             return mapObj;
    //         } else {
    //             if ( this.verbose ) System.out.println( CLASSNAME +" getDataFromReference("+ _src +"): detecting NEITHER a JSON NOR A YAML file provided via '@'." );
    //             return null;
    //         }

    //     } else if ( _src.startsWith("!") ) {
    //         if ( this.verbose ) System.out.println( CLASSNAME +" getDataFromReference("+ _src +"): detecting Recall-from-memory via '!'." );
    //         final String savedMapName = _src.startsWith("!") ?  _src.substring(1) : _src;
    //         // This can happen only within a BatchYaml-file context.  It only makes any sense (and will only work) within a BatchYaml-file context.
    //         final Object recalledContent = (this.memoryAndContext != null) ?  this.memoryAndContext.getDataFromMemory( savedMapName ) : null;
    //         if (this.verbose) System.out.println( CLASSNAME +": getDataFromReference("+ _src +"): Memory returned =" + ((recalledContent==null)?"null":recalledContent.toString()) );
    //         return recalledContent;

    //     } else {
    //         if ( this.verbose ) System.out.println( CLASSNAME +" getDataFromReference("+ _src +"): Must be an inline String.  Let me see if it's inline-JSON or inline-YAML." );
    //         try{
    //             // more than likely, we're likely to see a JSON as a string - inline - within the command (or in a batch-file line)
    //             // and less likely to see a YAML string inline
    //             return JSONTools.JSONString2Map(  this.verbose, _src );
    //         } catch( Exception e ) {
    //             if (this.verbose) System.out.println( CLASSNAME +": getDataFromReference("+ _src +"): FAILED-attempted to PARSE as JSON for [" + _src +"]" );
    //             try {
    //                 // more than likely, we're likely to see a JSON as a string - inline - within the command (or in a batch-file line)
    //                 // and less likely to see a YAML string inline
    //                 return tools.YAMLString2Map( _src, false );
    //             } catch(Exception e2) {
    //                 if (this.verbose) System.out.println( CLASSNAME +": getDataFromReference("+ _src +"): FAILED-attempted to PARSE as YAML for [" + _src +"] also!  So.. treating it as a SCALAR string." );
    //                 return _src; // The user provided a !!!SCALAR!!! java.lang.String directly - to be used AS-IS
    //             }
    //         } // outer-try-catch
    //     } // if-else startsWith("@")("!")
    // }

    // //==============================================================================
    // //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    // //==============================================================================

    // /**
    //  * This function saved _input to a reference to a file (_dest parameter must be prefixed with an '@').. or, to a string prefixed with '!' (in which it's saved into Working RAM, Not to disk/file)
    //  * @param _dest a javalang.String value - either a filename (must be prefixed with '@'), or a reference to a (new) property-variable within a Batch-file execution (must be prefixed with a '!')
    //  * @param _input the object to be saved using the reference provided in _dest paramater
    //  * @throws FileNotFoundException if the filenames within _cmdLineArgs do NOT exist
    //  * @throws IOException if the filenames within _cmdLineArgs give any sort of read/write troubles
    //  * @throws Exception by ReplaceYamlCmd method and this nethod (in case of unknown command)
    //  */
    // public void saveDataIntoReference( final String _dest, final Object _input )
    //             throws FileNotFoundException, IOException, Exception
    // {
    //     if ( _dest != null ) {
    //         if ( _dest.startsWith("@") ) {
    //             if ( this.verbose ) System.out.println( CLASSNAME +" saveDataIntoReference("+ _dest +"): detected a JSON-file provided via '@'." );
    //             final String destFile = _dest.substring(1);  // remove '@' as the 1st character in the file-name provided
    //             if ( destFile.endsWith(".json") ) {
    //                 //     // https://github.com/google/gson/blob/master/gson/src/main/java/com/google/gson/Gson.java
    //                 // final LinkedHashMap<String, Object> retMap2 = ..
    //                 // tempOutputMap = new com.google.gson.Gson().fromJson(  reader1,
    //                 //                        new com.google.gson.reflect.TypeToken< LinkedHashMap<String, Object> >() {}.getType()   );
    //                 // http://tutorials.jenkov.com/java-json/jackson-objectmapper.html#read-map-from-json-string 
    //                 final com.fasterxml.jackson.databind.ObjectMapper objMapper = new com.fasterxml.jackson.databind.ObjectMapper();
    //                 // final InputStream fs = new FileInputStream( destFile );
    //                 final java.io.FileWriter filewr = new java.io.FileWriter( destFile );
    //                 objMapper.writeValue( filewr, _input );
    //                 filewr.close();
    //                 if ( this.verbose ) System.out.println( CLASSNAME +" saveDataIntoReference("+ _dest +"): JSON written was =" + _input );
    //                 return;
    //             } else if ( destFile.endsWith(".yaml") ) {
    //                 if ( this.verbose ) System.out.println( CLASSNAME +" saveDataIntoReference("+ _dest +"): detected a YAML-file provided via '@'." );
    //                 final GenericYAMLWriter yamlwriter = this.getYamlWriter();
    //                 final java.io.FileWriter filewr = new java.io.FileWriter( destFile );
    //                 yamlwriter.prepare( filewr );
    //                 yamlwriter.write( _input );
    //                 yamlwriter.close();
    //                 filewr.close();
    //                 if ( this.verbose ) System.out.println( CLASSNAME +" saveDataIntoReference("+ _dest +"): YAML written was =" + _input );
    //                 return;
    //             } else {
    //                 if ( this.verbose ) System.out.println( CLASSNAME +" saveDataIntoReference("+ _dest +"): detecting NEITHER a JSON NOR A YAML file provided via '@'." );
    //                 throw new Exception("The saveTo @____ is NEITHER a YAML nor JSON file-name-extension.  Based on file-name-extension, the output is saved appropriately. ");
    //             }
    //         } else {
    //             // Unlike load/read (as done in getDataFromReference()..) whether or not the user uses a !-prefix.. same action taken.
    //             if ( this.verbose ) System.out.println( CLASSNAME +" getDataFromReference("+ _dest +"): detecting Save-To-memory via '!' (if '!' is not specified, it's implied)." );
    //             final String saveToMapName = _dest.startsWith("!") ?  _dest.substring(1) : _dest;
    //             if ( this.memoryAndContext != null ) {
    //                 // This can happen only within a BatchYaml-file context.  It only makes any sense (and will only work) within a BatchYaml-file context.
    //                 this.memoryAndContext.saveDataIntoMemory( saveToMapName, _input );  // remove '!' as the 1st character in the destination-reference provided
    //                 if (this.verbose) System.out.println( CLASSNAME +": saveDataIntoReference("+ _dest +"): saved into 'memoryAndContext'=" + _input );
    //             }
    //         }
    //     } else {
    //         return; // do Nothing.
    //     }
    // }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

}
