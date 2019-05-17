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

package org.ASUX.yaml;

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
 * @see org.ASUX.yaml.ReadYamlEntry
 * @see org.ASUX.yaml.ListYamlEntry
 * @see org.ASUX.yaml.DeleteYamlEntry
 * @see org.ASUX.yaml.ReplaceYamlEntry
 */
public class CmdInvoker {

    public static final String CLASSNAME = CmdInvoker.class.getName();

    // private static final String TMP FILE = System.getProperty("java.io.tmpdir") +"/org.ASUX.yaml.STDOUT.txt";

    /**
     * <p>Whether you want deluge of debug-output onto System.out.</p>
     * <p>Set this via the constructor.</p>
     * <p>It's read-only (final data-attribute).</p>
     */
    public final boolean verbose;

    /**
     * This is a private LinkedHashMap&lt;String, LinkedHashMap&lt;String, Object&gt; &gt; memoryAndContext = new LinkedHashMap&lt;&gt;(); .. cannot be null.  Most useful for @see org.ASUX.yaml.BatchYamlProcessor - which allows this this class to lookup !propertyvariable.
     * In case you need access to it - be nice and use it in a read-only manner - use the getter()
     */
    private final MemoryAndContext memoryAndContext;

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
        this.verbose = _verbose;
        if ( _memoryAndContext == null )
            this.memoryAndContext = new MemoryAndContext( _verbose, _showStats, this );
        else
            this.memoryAndContext = _memoryAndContext;
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================
    /**
     * This allows this class (CmdInvoker) to interact better with BatchYamlProcessor.java, which is the authoritative source of all "saveAs" outputs.
     * This class (CmdInvoker) will use this object (this.memoryAndContext) primarily for passing the replacement-Content and insert-Content (which is NOT the same as --input/-i cmdline option)
     * @return this.memoryAndContext
     */
    public MemoryAndContext getMemoryAndContext() {
        return this.memoryAndContext;
    }


    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================
    /**
     *  This function is meant to be used by Cmd.main() and by BatchProcessor.java.  Read the code *FIRST*, to see if you can use this function too.
     *  @param _cmdLineArgs yes, everything passed as commandline arguments to the Java program / org.ASUX.yaml.Cmd
     *  @param _inputData _the YAML inputData that is the input to pretty much all commands (a java.utils.LinkedHashMap&lt;String, Object&gt; object).
     *  @return either a String, java.utils.LinkedHashMap&lt;String, Object&gt;
     *  @throws YAMLPath.YAMLPathException if Pattern for YAML-Path provided is either semantically empty or is NOT java.util.Pattern compatible.
     *  @throws FileNotFoundException if the filenames within _cmdLineArgs do NOT exist
     *  @throws IOException if the filenames within _cmdLineArgs give any sort of read/write troubles
     *  @throws Exception by ReplaceYamlCmd method and this nethod (in case of unknown command)
     */
    public Object processCommand ( CmdLineArgs _cmdLineArgs, final LinkedHashMap<String, Object> _inputData )
                throws FileNotFoundException, IOException, Exception,
                YAMLPath.YAMLPathException
    {
        // final Output output = new Output(this.verbose);
        // final Tools tools = new Tools( this.verbose, this.memoryAndContext );

        // OPEN QUESTION: do we need to do these 2 lines for every line in the batch-file?  I think no.
        // This set..() invocations are taken care of - within Cmd.java and within BatchYamlProcess.java
        // this.memoryAndContext.getYamlLoader().setYamlLibrary( _cmdLineArgs.YAMLLibrary );
        // this.memoryAndContext.getYamlWriter().setYamlLibrary( _cmdLineArgs.YAMLLibrary );

        if ( _cmdLineArgs.isReadCmd ) {
            ReadYamlEntry readcmd = new ReadYamlEntry( _cmdLineArgs.verbose, _cmdLineArgs.showStats );
            readcmd.searchYamlForPattern( _inputData, _cmdLineArgs.yamlRegExpStr, _cmdLineArgs.yamlPatternDelimiter );
            final LinkedList<Object> outputStr = readcmd.getOutput();
            return outputStr;

        } else if ( _cmdLineArgs.isListCmd ) {
            ListYamlEntry listcmd = new ListYamlEntry( _cmdLineArgs.verbose, _cmdLineArgs.showStats, YAMLPath.DEFAULTPRINTDELIMITER );
            listcmd.searchYamlForPattern( _inputData, _cmdLineArgs.yamlRegExpStr, _cmdLineArgs.yamlPatternDelimiter );
            final ArrayList<String> outputStr = listcmd.getOutput();
            return outputStr;

        } else if ( _cmdLineArgs.isTableCmd ) {
            if (_cmdLineArgs.verbose) System.out.println(CLASSNAME + ": processCommand(isTableCmd):  _cmdLineArgs.yamlRegExpStr="+ _cmdLineArgs.yamlRegExpStr +" & tableColumns=[" + _cmdLineArgs.tableColumns +"]" );
            TableYamlQuery tblcmd = new TableYamlQuery( _cmdLineArgs.verbose, _cmdLineArgs.showStats, _cmdLineArgs.tableColumns, _cmdLineArgs.yamlPatternDelimiter );
            tblcmd.searchYamlForPattern( _inputData, _cmdLineArgs.yamlRegExpStr, _cmdLineArgs.yamlPatternDelimiter );
            final LinkedList< ArrayList<String> > output = tblcmd.getOutput();
            return output;

        } else if ( _cmdLineArgs.isDelCmd) {
            if ( _cmdLineArgs.verbose ) System.out.println(CLASSNAME + ": processCommand(isDelCmd): about to start DELETE command");
            DeleteYamlEntry delcmd = new DeleteYamlEntry( _cmdLineArgs.verbose, _cmdLineArgs.showStats );
            delcmd.searchYamlForPattern( _inputData, _cmdLineArgs.yamlRegExpStr, _cmdLineArgs.yamlPatternDelimiter );
            return _inputData;

        } else if ( _cmdLineArgs.isInsertCmd ) {
            if (_cmdLineArgs.verbose) System.out.println(CLASSNAME + ": processCommand(isInsertCmd):  _cmdLineArgs.yamlRegExpStr="+ _cmdLineArgs.yamlRegExpStr +" & loading @Insert-file: " + _cmdLineArgs.insertFilePath);
            final Object newContent = this.getDataFromReference( _cmdLineArgs.insertFilePath );
            if (_cmdLineArgs.verbose) System.out.println( CLASSNAME + ": processCommand(isInsertCmd): about to start INSERT command using: [" + newContent.toString() + "]");
            // Within a Batch-YAML context, the output of the previous line does NOT have to be a LinkedHashMap.
            // In such a case, an ArrayList or LinkedList object is converted into one -- by Tools.wrapAnObject_intoLinkedHashMap().
            // So, we will use the inverse-function Tools.getTheActualObject() to undo that.
            InsertYamlEntry inscmd = new InsertYamlEntry( _cmdLineArgs.verbose, _cmdLineArgs.showStats, new Output(this.verbose).getTheActualObject( newContent ) );
            inscmd.searchYamlForPattern( _inputData, _cmdLineArgs.yamlRegExpStr, _cmdLineArgs.yamlPatternDelimiter );
            return _inputData;

        } else if ( _cmdLineArgs.isReplaceCmd ) {
            if (_cmdLineArgs.verbose) System.out.println(CLASSNAME + ": processCommand(isReplaceCmd): loading @Replace-file: " + _cmdLineArgs.replaceFilePath);
            final Object replContent = this.getDataFromReference( _cmdLineArgs.replaceFilePath );
            if (_cmdLineArgs.verbose) System.out.println( CLASSNAME + ": processCommand(isReplaceCmd): about to start CHANGE/REPLACE command using: [" + replContent.toString() + "]");
            ReplaceYamlEntry replcmd = new ReplaceYamlEntry( _cmdLineArgs.verbose, _cmdLineArgs.showStats, replContent );
            replcmd.searchYamlForPattern( _inputData, _cmdLineArgs.yamlRegExpStr, _cmdLineArgs.yamlPatternDelimiter );
            return _inputData;

        } else if ( _cmdLineArgs.isMacroCmd ) {
            if (_cmdLineArgs.verbose) System.out.println( CLASSNAME + ": processCommand(isMacroCmd): loading Props file [" + _cmdLineArgs.propertiesFilePath + "]");
            final Properties properties = new Properties();
            assert( _cmdLineArgs.propertiesFilePath != null );
            if ( _cmdLineArgs.propertiesFilePath.startsWith("@") ) {
                final java.io.InputStream input = new java.io.FileInputStream( _cmdLineArgs.propertiesFilePath.substring(1) );
                properties.load( input );
            } else {
                final java.io.StringReader sr = new java.io.StringReader( _cmdLineArgs.propertiesFilePath );
                properties.load( sr );
            }
            if (_cmdLineArgs.verbose) System.out.println( CLASSNAME + ": processCommand(isMacroCmd): about to start MACRO command using: [Props file [" + _cmdLineArgs.propertiesFilePath + "]");
            MacroYamlProcessor macro = new MacroYamlProcessor( _cmdLineArgs.verbose, _cmdLineArgs.showStats );
            final LinkedHashMap<String, Object> outpMap = new LinkedHashMap<>();
            macro.recursiveSearch( _inputData, outpMap, properties );
            // writer.write(outpMap); // The contents of java.util.LinkedHashMap<String, Object> has been updated with replacement strings. so, dump it.
            return outpMap;

        } else if ( _cmdLineArgs.isBatchCmd ) {
            if (_cmdLineArgs.verbose) System.out.println( CLASSNAME + ": processCommand(isBatchCmd): about to start BATCH command using: BATCH file [" + _cmdLineArgs.batchFilePath + "]");
            BatchYamlProcessor batcher = new BatchYamlProcessor( _cmdLineArgs.verbose, _cmdLineArgs.showStats );
            batcher.setMemoryAndContext( this.memoryAndContext );
            LinkedHashMap<String, Object> outpMap = new LinkedHashMap<String, Object>();
            batcher.go( _cmdLineArgs.batchFilePath, _inputData, outpMap );
            if ( this.verbose ) System.out.println( CLASSNAME +" processCommand(isBatchCmd):  outpMap =" + outpMap +"\n\n");
            // writer.write(outpMap); // The contents of java.util.LinkedHashMap<String, Object> has been updated with replacement strings. so, dump it.
            return outpMap;

        } else {
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
     * This functon takes a single parameter that is a javalang.String value - and, either detects it to be inline YAML/JSON, or a filename (must be prefixed with '@'), or a reference to something saved in {@link MemoryAndContext} within a Batch-file execution (must be prefixed with a '!')
     * @param _src a javalang.String value - either inline YAML/JSON, or a filename (must be prefixed with '@'), or a reference to a property within a Batch-file execution (must be prefixed with a '!')
     * @return an object (either LinkedHashMap, ArrayList or LinkedList)
     * @throws FileNotFoundException if the filenames within _cmdLineArgs do NOT exist
     * @throws IOException if the filenames within _cmdLineArgs give any sort of read/write troubles
     * @throws Exception by ReplaceYamlCmd method and this nethod (in case of unknown command)
     */
    public Object getDataFromReference( final String _src  )
                throws FileNotFoundException, IOException, Exception
    {
        if ( _src == null || _src.trim().length() <= 0 )
            return null;
        final Tools tools = new Tools( this.verbose, this.memoryAndContext.getYamlLoader(), this.memoryAndContext.getYamlWriter() );

        if ( _src.startsWith("@") ) {
            final String srcFile = _src.substring(1);
            final InputStream fs = new FileInputStream( srcFile );
            if ( srcFile.endsWith(".json") ) {
                if ( this.verbose ) System.out.println( CLASSNAME +" getDataFromReference("+ _src +"): detected a JSON-file provided via '@'." );
                //     // https://github.com/google/gson/blob/master/gson/src/main/java/com/google/gson/Gson.java
                // final LinkedHashMap<String, Object> retMap2 = ..
                // tempOutputMap = new com.google.gson.Gson().fromJson(  reader1,
                //                        new com.google.gson.reflect.TypeToken< LinkedHashMap<String, Object> >() {}.getType()   );
                // http://tutorials.jenkov.com/java-json/jackson-objectmapper.html#read-map-from-json-string 
                com.fasterxml.jackson.databind.ObjectMapper objMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                objMapper.configure( com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true );
                objMapper.configure( com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
                    com.fasterxml.jackson.databind.type.MapType type = objMapper.getTypeFactory().constructMapType( LinkedHashMap.class, String.class, Object.class );
                LinkedHashMap<String, Object> retMap2 = null;
                retMap2 = objMapper.readValue( fs, new com.fasterxml.jackson.core.type.TypeReference< LinkedHashMap<String,Object> >(){}  );
                if ( this.verbose ) System.out.println( CLASSNAME +" getDataFromReference("+ _src +"): jsonMap loaded BY OBJECTMAPPER into tempOutputMap =" + retMap2 );
                retMap2 = tools.lintRemover( retMap2 );
                fs.close();
                return retMap2;
            } else if ( srcFile.endsWith(".yaml") ) {
                if ( this.verbose ) System.out.println( CLASSNAME +" getDataFromReference("+ _src +"): detected a YAML-file provided via '@'." );
                final java.io.Reader reader1 = new java.io.InputStreamReader( fs  );
                final org.ASUX.common.Output.Object<?> output = this.memoryAndContext.getYamlLoader().load( reader1 );
                final LinkedHashMap<String, Object> mapObj = output.getMap(); // new YamlReader( reader1 ).read( LinkedHashMap.class );
                reader1.close(); // automatically includes fs.close();
                // final LinkedHashMap<String, Object> mapObj = new YamlReader( reader1 ).read( LinkedHashMap.class );
                // @SuppressWarnings("unchecked")
                // final LinkedHashMap<String, Object> retMap3 = (LinkedHashMap<String, Object>) mapObj;
                if ( this.verbose ) System.out.println( CLASSNAME +" getDataFromReference("+ _src +"): YAML loaded into tempOutputMap =" + mapObj );
                return mapObj;
            } else {
                if ( this.verbose ) System.out.println( CLASSNAME +" getDataFromReference("+ _src +"): detecting NEITHER a JSON NOR A YAML file provided via '@'." );
                return null;
            }

        } else if ( _src.startsWith("!") ) {
            if ( this.verbose ) System.out.println( CLASSNAME +" getDataFromReference("+ _src +"): detecting Recall-from-memory via '!'." );
            final String savedMapName = _src.startsWith("!") ?  _src.substring(1) : _src;
            // This can happen only within a BatchYaml-file context.  It only makes any sense (and will only work) within a BatchYaml-file context.
            final Object recalledContent = (this.memoryAndContext != null) ?  this.memoryAndContext.getDataFromMemory( savedMapName ) : null;
            if (this.verbose) System.out.println( CLASSNAME +": getDataFromReference("+ _src +"): Memory returned =" + ((recalledContent==null)?"null":recalledContent.toString()) );
            return recalledContent;

        } else {
            if ( this.verbose ) System.out.println( CLASSNAME +" getDataFromReference("+ _src +"): Must be an inline String.  Let me see if it's inline-JSON or inline-YAML." );
            try{
                // more than likely, we're likely to see a JSON as a string - inline - within the command (or in a batch-file line)
                // and less likely to see a YAML string inline
                return tools.JSONString2YAML(_src);
            } catch( Exception e ) {
                if (this.verbose) System.out.println( CLASSNAME +": getDataFromReference("+ _src +"): FAILED-attempted to PARSE as JSON for [" + _src +"]" );
                try {
                    // more than likely, we're likely to see a JSON as a string - inline - within the command (or in a batch-file line)
                    // and less likely to see a YAML string inline
                    return tools.YAMLString2YAML( _src, false );
                } catch(Exception e2) {
                    if (this.verbose) System.out.println( CLASSNAME +": getDataFromReference("+ _src +"): FAILED-attempted to PARSE as YAML for [" + _src +"] also!  So.. treating it as a SCALAR string." );
                    return _src; // The user provided a !!!SCALAR!!! java.lang.String directly - to be used AS-IS
                }
            } // outer-try-catch
        } // if-else startsWith("@")("!")
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /**
     * This function saved _inputMap to a reference to a file (_dest parameter must be prefixed with an '@').. or, to a string prefixed with '!' (in which it's saved into Working RAM, Not to disk/file)
     * @param _dest a javalang.String value - either a filename (must be prefixed with '@'), or a reference to a (new) property-variable within a Batch-file execution (must be prefixed with a '!')
     * @param _inputMap the object to be saved using the reference provided in _dest paramater
     * @throws FileNotFoundException if the filenames within _cmdLineArgs do NOT exist
     * @throws IOException if the filenames within _cmdLineArgs give any sort of read/write troubles
     * @throws Exception by ReplaceYamlCmd method and this nethod (in case of unknown command)
     */
    public void saveDataIntoReference( final String _dest, final LinkedHashMap<String, Object> _inputMap )
                throws FileNotFoundException, IOException, Exception
    {
        final Tools tools = new Tools( this.verbose, this.memoryAndContext );
        if ( _dest != null ) {
            if ( _dest.startsWith("@") ) {
                if ( this.verbose ) System.out.println( CLASSNAME +" saveDataIntoReference("+ _dest +"): detected a JSON-file provided via '@'." );
                final String destFile = _dest.substring(1);  // remove '@' as the 1st character in the file-name provided
                if ( destFile.endsWith(".json") ) {
                    //     // https://github.com/google/gson/blob/master/gson/src/main/java/com/google/gson/Gson.java
                    // final LinkedHashMap<String, Object> retMap2 = ..
                    // tempOutputMap = new com.google.gson.Gson().fromJson(  reader1,
                    //                        new com.google.gson.reflect.TypeToken< LinkedHashMap<String, Object> >() {}.getType()   );
                    // http://tutorials.jenkov.com/java-json/jackson-objectmapper.html#read-map-from-json-string 
                    final com.fasterxml.jackson.databind.ObjectMapper objMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    // final InputStream fs = new FileInputStream( destFile );
                    final java.io.FileWriter filewr = new java.io.FileWriter( destFile );
                    objMapper.writeValue( filewr, _inputMap );
                    filewr.close();
                    // fs.close();
                    if ( this.verbose ) System.out.println( CLASSNAME +" saveDataIntoReference("+ _dest +"): JSON written was =" + tools.Map2JSONString(_inputMap) );
                    return;
                } else if ( destFile.endsWith(".yaml") ) {
                    if ( this.verbose ) System.out.println( CLASSNAME +" saveDataIntoReference("+ _dest +"): detected a YAML-file provided via '@'." );
                    final GenericYAMLWriter yamlwriter = this.memoryAndContext.getYamlWriter();
                    final java.io.FileWriter filewr = new java.io.FileWriter( destFile );
                    yamlwriter.prepare( filewr );
                    yamlwriter.write( _inputMap );
                    yamlwriter.close();
                    // final YamlWriter yamlwriter = new YamlWriter( new java.io.FileWriter( destFile ) );
                    // yamlwriter.write( _inputMap );
                    // yamlwriter.close();
                    filewr.close();
                    if ( this.verbose ) System.out.println( CLASSNAME +" saveDataIntoReference("+ _dest +"): YAML written was =" + tools.Map2YAMLString(_inputMap) );
                    return;
                } else {
                    if ( this.verbose ) System.out.println( CLASSNAME +" saveDataIntoReference("+ _dest +"): detecting NEITHER a JSON NOR A YAML file provided via '@'." );
                    throw new Exception("The saveTo @____ is NEITHER a YAML nor JSON file-name-extension.  Based on file-name-extension, the output is saved appropriately. ");
                }
            } else {
                // Unlike load/read (as done in getDataFromReference()..) whether or not the user uses a !-prefix.. same action taken.
                if ( this.verbose ) System.out.println( CLASSNAME +" getDataFromReference("+ _dest +"): detecting Save-To-memory via '!' (if '!' is not specified, it's implied)." );
                final String saveToMapName = _dest.startsWith("!") ?  _dest.substring(1) : _dest;
                if ( this.memoryAndContext != null ) {
                    // This can happen only within a BatchYaml-file context.  It only makes any sense (and will only work) within a BatchYaml-file context.
                    this.memoryAndContext.saveDataIntoMemory( saveToMapName, _inputMap );  // remove '!' as the 1st character in the destination-reference provided
                    if (this.verbose) System.out.println( CLASSNAME +": saveDataIntoReference("+ _dest +"): saved into 'memoryAndContext'=" + tools.Map2YAMLString(_inputMap) );
                }
            }
        } else {
            return; // do Nothing.
        }
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

}
