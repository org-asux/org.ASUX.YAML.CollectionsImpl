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

import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.LinkedList;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Properties;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * <p>
 * This org.ASUX.yaml GitHub.com project and the
 * <a href="https://github.com/org-asux/org.ASUX.cmdline">org.ASUX.cmdline</a>
 * GitHub.com projects, would simply NOT be possible without the genius Java
 * library <a href=
 * "https://github.com/EsotericSoftware/yamlbeans">"com.esotericsoftware.yamlbeans"</a>.
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
public class Cmd {

    public static final String CLASSNAME = "org.ASUX.yaml.Cmd";

    private static final String TMPFILE = System.getProperty("java.io.tmpdir") +"/org.ASUX.yaml.STDOUT.txt";

    /**
     * <p>Whether you want deluge of debug-output onto System.out.</p>
     * <p>Set this via the constructor.</p>
     * <p>It's read-only (final data-attribute).</p>
     */
    public final boolean verbose;

    /**
     * <p>The only constructor - public/private/protected</p>
     * @param _verbose Whether you want deluge of debug-output onto System.out.
     */
    public Cmd(boolean _verbose) {
        this.verbose = _verbose;
    }

    private LinkedHashMap<String, LinkedHashMap<String, Object> > savedOutputMaps = null;

    /**
     * This allows Cmd.java to interact better with BatchYamlProcessor.java, which is the authoritative source of all "saveAs" outputs.
     * Cmd.java will use this object (this.savedOutputMaps) primarily for passing the replacement-Content and insert-Content (which is NOT the same as --input/-i cmdline option)
     * @param _savedOutputMaps
     */
    public void setSavedOutputMaps( final LinkedHashMap<String, LinkedHashMap<String, Object> > _savedOutputMaps ) {
        this.savedOutputMaps = _savedOutputMaps;
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================
    /**
     * This is NOT testing code. It's actual means by which user's command line arguments are read and processed
     * @param args user's commandline arguments
     */
    public static void main( String[] args ) {

        CmdLineArgs cmdLineArgs = null;

        try {
            cmdLineArgs = new CmdLineArgs( args );
            Cmd cmd = new Cmd(cmdLineArgs.verbose);

            //======================================================================
            // read input, whether it's System.in -or- an actual input-file
            if (cmdLineArgs.verbose) System.out.println(CLASSNAME + ": about to load file: " + cmdLineArgs.inputFilePath);
            final java.io.InputStream is1 = ( cmdLineArgs.inputFilePath.equals("-") ) ? System.in
                    : new java.io.FileInputStream(cmdLineArgs.inputFilePath);
            final java.io.Reader reader1 = new java.io.InputStreamReader(is1);

            // -----------------------
            com.esotericsoftware.yamlbeans.YamlWriter writer = null;

            // -----------------------
            // post completion of YAML processing
            // if ( cmdLineArgs.isReadCmd ) {
            // } else if ( cmdLineArgs.isListCmd ) {
            // } else if ( cmdLineArgs.isDelCmd ) {
            // } else if ( cmdLineArgs.isInsertCmd ) {
            // } else if ( cmdLineArgs.isReplaceCmd ) {
            // } else if ( cmdLineArgs.isMacroCmd ) {
            // } else if ( cmdLineArgs.isBatchCmd ) {
            // } else {
            // }

            // common to 3 of the above empty if-else
            // if ( cmdLineArgs.isDelCmd || cmdLineArgs.isInsertCmd || cmdLineArgs.isReplaceCmd || cmdLineArgs.isMacroCmd ) { }

            // prepare for output: whether it goes to System.out -or- to an actual output-file.
            writer = ( cmdLineArgs.outputFilePath.equals("-") )
                    ? new com.esotericsoftware.yamlbeans.YamlWriter( new java.io.FileWriter(TMPFILE) )
                    : new com.esotericsoftware.yamlbeans.YamlWriter( new java.io.FileWriter(cmdLineArgs.outputFilePath) );
            // WARNING!!! com.esotericsoftware.yamlbeans.YamlWriter takes over stdout, and it will STOP working for all System.out.println();

            Tools.defaultConfigurationForYamlWriter( writer );

            // -----------------------
            // Leverage the wonderful com.esotericsoftware.yamlbeans library to load file-contents into a java.util.LinkedHashMap<String, Object>
            final LinkedHashMap dataObj = new com.esotericsoftware.yamlbeans.YamlReader( reader1 ).read( LinkedHashMap.class );
            @SuppressWarnings("unchecked")
            final LinkedHashMap<String, Object> data = (LinkedHashMap<String, Object>) dataObj;
            reader1.close();

            //======================================================================
            // run the command requested by user
            final Object output = cmd.processCommand( cmdLineArgs, data );

            //======================================================================
            // post completion of YAML processing
            if ( cmdLineArgs.isReadCmd ) {
                @SuppressWarnings("unchecked")
                final LinkedList<Object> list = ( LinkedList<Object> ) output;
                // list.forEach( s -> System.out.println( s.toString() ) );
                writer.write( list );
            } else if ( cmdLineArgs.isListCmd ) {
                @SuppressWarnings("unchecked")
                final ArrayList<String> arr = ( ArrayList<String> ) output;
                // arr.forEach( s -> System.out.println( s ) );
                writer.write( arr );
            } else if ( cmdLineArgs.isDelCmd ) {
            } else if ( cmdLineArgs.isInsertCmd )   { // see common code in next block-of-code below
            } else if ( cmdLineArgs.isReplaceCmd )  { // see common code in next block-of-code below
            } else if ( cmdLineArgs.isMacroCmd )    {   // see common code in next block-of-code below
            } else if ( cmdLineArgs.isBatchCmd )    {   // see common code in next block-of-code below
            } else {
            }

            if (cmdLineArgs.isDelCmd || cmdLineArgs.isInsertCmd || cmdLineArgs.isReplaceCmd || cmdLineArgs.isMacroCmd || cmdLineArgs.isBatchCmd ) {
                if (writer != null) {
                    if ( output instanceof LinkedHashMap) {
                        @SuppressWarnings("unchecked")
                        final LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>) output;
                        writer.write(map);
                    } else {
                        throw new Exception( "output is Not of type LinkedHashMap.  It's ["+ output.getClass().getName() +"]");
                    }
                } // if writer != null
            } // if

            //======================================================================
            // cleanup & close-out things.    This will actually do work for DELETE, INSERT, REPLACE and MACRO commands
            if ( cmdLineArgs.outputFilePath.equals("-") ) {
                // if we're writing to STDOUT/System.out ..
                if (writer != null) writer.close(); // Yes! Even for stdout/System.out .. we need to call close(). See https://github.com/EsotericSoftware/yamlbeans/issues/111
            } else {
                if (writer != null) writer.close(); // close the actual file.
            }

            // Now since we have a surrogate for STDOUT for use by , let's dump its output onto STDOUT!
            if ( cmdLineArgs.outputFilePath.equals("-") ) {
                try {
                    final java.io.InputStream istrm = new java.io.FileInputStream( TMPFILE );
                    final java.util.Scanner scanner = new java.util.Scanner( istrm );
                    while (scanner.hasNextLine()) {
                        System.out.println( scanner.nextLine() );
                    }
                } catch (java.io.IOException e) {
                    e.printStackTrace(System.err);
                    System.err.println( CLASSNAME + ": openBatchFile(): Failure to dump contents of file ["+ TMPFILE +"]" );
                    System.exit(102);
                } catch (Exception e) {
                    e.printStackTrace(System.err);
                    System.err.println( CLASSNAME + ": openBatchFile(): Unknown Internal error: re: ."+ TMPFILE );
                    System.exit(103);
                }
            }

        } catch (com.esotericsoftware.yamlbeans.YamlException e) { // Warning: This must PRECEDE IOException, else compiler error.
            e.printStackTrace(System.err);
            System.err.println( "Internal error: unable to process YAML");
            System.exit(9);
        } catch (java.io.FileNotFoundException e) {
            e.printStackTrace(System.err);
            System.err.println( "INPUT-File Not found: '" + cmdLineArgs.inputFilePath + "'.");
            System.exit(8);
        } catch (java.io.IOException e) {
            e.printStackTrace(System.err);
            System.err.println( "OUTPUT-File Not found: '" + cmdLineArgs.outputFilePath + "'.");
            System.exit(7);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.err.println( "Internal error: '" + cmdLineArgs.outputFilePath + "'.");
            System.exit(6);
        }

    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================
    /**
     * This function is meant to be used by Cmd.main() and by BatchProcessor.java.  Read the code *FIRST*, to see if you can use this function too.
     * @param _cmdLineArgs yes, everything passed as commandline arguments to this Java program / org.ASUX.yaml.Cmd
     * @param _data _the YAML data that is the input to pretty much all commands (a java.utils.LinkedHashMap&lt;String, Object&gt; object).
     * @return either a String, java.utils.LinkedHashMap&lt;String, Object&gt;
     * @throws com.esotericsoftware.yamlbeans.YamlException you'll need to refer to the library at <a href="https://github.com/EsotericSoftware/yamlbeans">"com.esotericsoftware.yamlbeans"</a>.
     * @throws FileNotFoundException if the filenames within _cmdLineArgs do NOT exist
     * @throws IOException if the filenames within _cmdLineArgs give any sort of read/write troubles
     * @throws Exception by ReplaceYamlCmd method and this nethod (in case of unknown command)
     */
    public Object processCommand ( CmdLineArgs _cmdLineArgs, final LinkedHashMap<String, Object> _data)
                throws FileNotFoundException, IOException, Exception, com.esotericsoftware.yamlbeans.YamlException
    {
        if ( _cmdLineArgs.isReadCmd ) {
            ReadYamlEntry readcmd = new ReadYamlEntry( _cmdLineArgs.verbose, _cmdLineArgs.showStats );
            readcmd.searchYamlForPattern( _data, _cmdLineArgs.yamlRegExpStr, _cmdLineArgs.yamlPatternDelimiter );
            final LinkedList<Object> outputStr = readcmd.getOutput();
            return outputStr;

        } else if ( _cmdLineArgs.isListCmd ) {
            ListYamlEntry listcmd = new ListYamlEntry( _cmdLineArgs.verbose, _cmdLineArgs.showStats, YAMLPath.DEFAULTPRINTDELIMITER );
            listcmd.searchYamlForPattern( _data, _cmdLineArgs.yamlRegExpStr, _cmdLineArgs.yamlPatternDelimiter );
            final ArrayList<String> outputStr = listcmd.getOutput();
            return outputStr;

        } else if ( _cmdLineArgs.isDelCmd) {
            if ( _cmdLineArgs.verbose ) System.out.println(CLASSNAME + ": processCommand(isDelCmd): about to start DELETE command");
            DeleteYamlEntry delcmd = new DeleteYamlEntry( _cmdLineArgs.verbose, _cmdLineArgs.showStats );
            delcmd.searchYamlForPattern( _data, _cmdLineArgs.yamlRegExpStr, _cmdLineArgs.yamlPatternDelimiter );
            // writer.write( _data ); // The contents of java.util.LinkedHashMap<String, Object> has some YAML rows removed. so, dump it.
            return _data;

        } else if ( _cmdLineArgs.isInsertCmd ) {
            if (_cmdLineArgs.verbose) System.out.println(CLASSNAME + ": processCommand(isInsertCmd):  _cmdLineArgs.yamlRegExpStr="+ _cmdLineArgs.yamlRegExpStr +" & loading @Insert-file: " + _cmdLineArgs.insertFilePath);
            final Object newContent = Cmd.getDataFromReference( this.verbose, _cmdLineArgs.insertFilePath, this.savedOutputMaps);
            if (_cmdLineArgs.verbose) System.out.println( CLASSNAME + ": processCommand(isInsertCmd): about to start INSERT command using: [" + newContent.toString() + "]");
            InsertYamlEntry inscmd = new InsertYamlEntry( _cmdLineArgs.verbose, _cmdLineArgs.showStats, newContent );
            inscmd.searchYamlForPattern( _data, _cmdLineArgs.yamlRegExpStr, _cmdLineArgs.yamlPatternDelimiter );
            // writer.write(_data); // The contents of java.util.LinkedHashMap<String, Object> has been updated with new/insert strings. so, dump it.
            return _data;

        } else if ( _cmdLineArgs.isReplaceCmd ) {
            if (_cmdLineArgs.verbose) System.out.println(CLASSNAME + ": processCommand(isReplaceCmd): loading @Replace-file: " + _cmdLineArgs.replaceFilePath);
            final Object replContent = Cmd.getDataFromReference( this.verbose, _cmdLineArgs.replaceFilePath, this.savedOutputMaps);
            if (_cmdLineArgs.verbose) System.out.println( CLASSNAME + ": processCommand(isReplaceCmd): about to start CHANGE/REPLACE command using: [" + replContent.toString() + "]");
            ReplaceYamlEntry replcmd = new ReplaceYamlEntry( _cmdLineArgs.verbose, _cmdLineArgs.showStats, replContent );
            replcmd.searchYamlForPattern( _data, _cmdLineArgs.yamlRegExpStr, _cmdLineArgs.yamlPatternDelimiter );
            // writer.write(_data); // The contents of java.util.LinkedHashMap<String, Object> has been updated with replacement strings. so, dump it.
            return _data;

        } else if ( _cmdLineArgs.isMacroCmd ) {
            if (_cmdLineArgs.verbose) System.out.println( CLASSNAME + ": processCommand(isMacroCmd): loading Props file [" + _cmdLineArgs.propertiesFilePath + "]");
            final Properties properties = new Properties();
            if (_cmdLineArgs.propertiesFilePath != null) {
                java.io.InputStream input = new java.io.FileInputStream( _cmdLineArgs.propertiesFilePath );
                properties.load( input );
            }
            if (_cmdLineArgs.verbose) System.out.println( CLASSNAME + ": processCommand(isMacroCmd): about to start MACRO command using: [Props file [" + _cmdLineArgs.propertiesFilePath + "]");
            MacroYamlProcessor macro = new MacroYamlProcessor( _cmdLineArgs.verbose, _cmdLineArgs.showStats );
            final LinkedHashMap<String, Object> outpMap = new LinkedHashMap<>();
            macro.recursiveSearch( _data, outpMap, properties );
            // writer.write(outpMap); // The contents of java.util.LinkedHashMap<String, Object> has been updated with replacement strings. so, dump it.
            return outpMap;

        } else if ( _cmdLineArgs.isBatchCmd ) {
            if (_cmdLineArgs.verbose) System.out.println( CLASSNAME + ": processCommand(isBatchCmd): about to start BATCH command using: BATCH file [" + _cmdLineArgs.batchFilePath + "]");
            BatchYamlProcessor batcher = new BatchYamlProcessor( _cmdLineArgs.verbose, _cmdLineArgs.showStats );
            LinkedHashMap<String, Object> outpMap = new LinkedHashMap<String, Object>();
            batcher.go( _cmdLineArgs.batchFilePath, _data, outpMap );
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
     * 
     * @param _verbose
     * @param _src
     * @param _savedOutputMaps
     * @return
     * @throws com.esotericsoftware.yamlbeans.YamlException you'll need to refer to the library at <a href="https://github.com/EsotericSoftware/yamlbeans">"com.esotericsoftware.yamlbeans"</a>.
     * @throws FileNotFoundException if the filenames within _cmdLineArgs do NOT exist
     * @throws IOException if the filenames within _cmdLineArgs give any sort of read/write troubles
     * @throws Exception by ReplaceYamlCmd method and this nethod (in case of unknown command)
     */
    public static Object getDataFromReference( final boolean _verbose, final String _src,
                                LinkedHashMap<String, LinkedHashMap<String, Object> > _savedOutputMaps )
                throws FileNotFoundException, IOException, Exception, com.esotericsoftware.yamlbeans.YamlException
    {
        final Tools tools = new Tools(_verbose);
        if ( _src != null ) {
            if ( _src.startsWith("@") ) {
                final String srcFile = _src.substring(1);
                final InputStream fs = new FileInputStream( srcFile );
                if ( srcFile.endsWith(".json") ) {
                    //     // https://github.com/google/gson/blob/master/gson/src/main/java/com/google/gson/Gson.java
                    // final LinkedHashMap<String, Object> retMap2 = ..
                    // tempOutputMap = new com.google.gson.Gson().fromJson(  reader1,
                    //                        new com.google.gson.reflect.TypeToken< LinkedHashMap<String, Object> >() {}.getType()   );
                    // http://tutorials.jenkov.com/java-json/jackson-objectmapper.html#read-map-from-json-string 
                    com.fasterxml.jackson.databind.ObjectMapper objMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    com.fasterxml.jackson.databind.type.MapType type = objMapper.getTypeFactory().constructMapType( LinkedHashMap.class, String.class, Object.class );
                    LinkedHashMap<String, Object> retMap2 = null;
                    retMap2 = objMapper.readValue( fs, new com.fasterxml.jackson.core.type.TypeReference<LinkedHashMap<String,Object>>(){}  );
                    if ( _verbose ) System.out.println( CLASSNAME +" getDataFromReference("+ _src +"): jsonMap loaded BY OBJECTMAPPER into tempOutputMap =" + retMap2 );
                    retMap2 = tools.JSON2YAML( retMap2 );
                    fs.close();
                    return retMap2;
                }
                if ( srcFile.endsWith(".yaml") ) {
                    final java.io.Reader reader1 = new java.io.InputStreamReader( fs  );
                    final LinkedHashMap mapObj = new com.esotericsoftware.yamlbeans.YamlReader( reader1 ).read( LinkedHashMap.class );
                    @SuppressWarnings("unchecked")
                    final LinkedHashMap<String, Object> retMap3 = (LinkedHashMap<String, Object>) mapObj;
                    reader1.close(); // automatically includes fs.close();
                    if ( _verbose ) System.out.println( CLASSNAME +" getDataFromReference("+ _src +"): YAML loaded into tempOutputMap =" + retMap3 );
                    return retMap3;
                }
                return null; // compiler is complaining about missing return statement.
            } else if ( _src.startsWith("!") ) {
                final String savedMapName = _src.startsWith("!") ?  _src.substring(1) : _src;
                // This can happen only within a BatchYaml-file context.  It only makes any sense (and will only work) within a BatchYaml-file context.
                final Object newContent /* LinkedHashMap<String, Object> */ = _savedOutputMaps.get( savedMapName );
                if (_verbose) System.out.println( CLASSNAME +": getDataFromReference("+ _src +"): newContent=" + newContent.toString() );
                return newContent;
            } else {
                return _src; // The user provided a java.lang.String directly - to be used AS-IS
            }
        } else {
            return null;
        }
    }

    //======================================================================

    /**
     * 
     * @param _verbose
     * @param _dest
     * @param _inputMap
     * @param _savedOutputMaps
     * @throws com.esotericsoftware.yamlbeans.YamlException you'll need to refer to the library at <a href="https://github.com/EsotericSoftware/yamlbeans">"com.esotericsoftware.yamlbeans"</a>.
     * @throws FileNotFoundException if the filenames within _cmdLineArgs do NOT exist
     * @throws IOException if the filenames within _cmdLineArgs give any sort of read/write troubles
     * @throws Exception by ReplaceYamlCmd method and this nethod (in case of unknown command)
     */
    public static void saveDataIntoReference( final boolean _verbose, final String _dest,
                                final LinkedHashMap<String, Object> _inputMap,
                                final LinkedHashMap<String, LinkedHashMap<String, Object> > _savedOutputMaps )
                throws FileNotFoundException, IOException, Exception, com.esotericsoftware.yamlbeans.YamlException
    {
        final Tools tools = new Tools(_verbose);
        if ( _dest != null ) {
            if ( _dest.startsWith("@") ) {
                final String destFile = _dest.substring(1);  // remove '@' as the 1st character in the file-name provided
                final InputStream fs = new FileInputStream( destFile );
                if ( destFile.endsWith(".json") ) {
                    //     // https://github.com/google/gson/blob/master/gson/src/main/java/com/google/gson/Gson.java
                    // final LinkedHashMap<String, Object> retMap2 = ..
                    // tempOutputMap = new com.google.gson.Gson().fromJson(  reader1,
                    //                        new com.google.gson.reflect.TypeToken< LinkedHashMap<String, Object> >() {}.getType()   );
                    // http://tutorials.jenkov.com/java-json/jackson-objectmapper.html#read-map-from-json-string 
                    final com.fasterxml.jackson.databind.ObjectMapper objMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    final java.io.FileWriter filewr = new java.io.FileWriter( destFile );
                    objMapper.writeValue( filewr, _inputMap );
                    filewr.close();
                    fs.close();
                    if ( _verbose ) System.out.println( CLASSNAME +" saveDataIntoReference("+ _dest +"): JSON written was =" + tools.YAML2JSONString(_inputMap) );
                    return;
                } else if ( destFile.endsWith(".yaml") ) {
                    final com.esotericsoftware.yamlbeans.YamlWriter yamlwriter
                            = new com.esotericsoftware.yamlbeans.YamlWriter( new java.io.FileWriter( destFile ) );
                    yamlwriter.write( _inputMap );
                    yamlwriter.close();
                    fs.close();
                    if ( _verbose ) System.out.println( CLASSNAME +" saveDataIntoReference("+ _dest +"): YAML written was =" + tools.YAML2JSONString(_inputMap) );
                    return;
                }
                return;
            } else {
                // Unlike load/read (as done in getDataFromReference()..) whether or not the user uses a !-prefix.. same action taken.
                final String saveToMapName = _dest.startsWith("!") ?  _dest.substring(1) : _dest;
                if ( _savedOutputMaps != null ) {
                    // This can happen only within a BatchYaml-file context.  It only makes any sense (and will only work) within a BatchYaml-file context.
                    _savedOutputMaps.put( saveToMapName, _inputMap );  // remove '!' as the 1st character in the destination-reference provided
                    if (_verbose) System.out.println( CLASSNAME +": saveDataIntoReference("+ _dest +"): saved into 'savedOutputMaps'=" + tools.YAML2JSONString(_inputMap) );
                }
            }
        } else {
            return; // do Nothing.
        }
    }

}
