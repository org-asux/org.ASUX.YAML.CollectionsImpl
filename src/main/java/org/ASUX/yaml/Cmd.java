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

import java.io.FileNotFoundException;
import java.io.IOException;

//import java.util.Map;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Properties;

import javax.xml.transform.OutputKeys;

//import java.util.regex.*;

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
            // } else if ( cmdLineArgs.isReplaceCmd ) {
            // } else if ( cmdLineArgs.isMacroCmd ) {
            // } else if ( cmdLineArgs.isBatchCmd ) {
            // } else {
            // }

            // common to 3 of the above empty if-else
            // if ( cmdLineArgs.isDelCmd || cmdLineArgs.isReplaceCmd || cmdLineArgs.isMacroCmd ) { }

            // prepare for output: whether it goes to System.out -or- to an actual output-file.
            writer = ( cmdLineArgs.outputFilePath.equals("-") )
                    ? new com.esotericsoftware.yamlbeans.YamlWriter( new java.io.FileWriter(TMPFILE) )
                    : new com.esotericsoftware.yamlbeans.YamlWriter( new java.io.FileWriter(cmdLineArgs.outputFilePath) );
            // WARNING!!! com.esotericsoftware.yamlbeans.YamlWriter takes over stdout, and it will STOP working for all System.out.println();

            // writer.getConfig().writeConfig.setWriteRootTags(false); // Does NOTHING :-
            writer.getConfig().writeConfig.setWriteClassname(com.esotericsoftware.yamlbeans.YamlConfig.WriteClassName.NEVER); // I hate !<pkg.className> within YAML files. So does AWS I believe.
            // writer.getConfig().writeConfig.setQuoteChar( cmdLineArgs.quoteType );
            // writer.getConfig().writeConfig.setQuoteChar( com.esotericsoftware.yamlbeans.YamlConfig.QuoteCharEnum.NONE );
            // writer.getConfig().writeConfig.setQuoteChar( com.esotericsoftware.yamlbeans.YamlConfig.QuoteCharEnum.SINGLEQUOTE );
            // writer.getConfig().writeConfig.setQuoteChar( com.esotericsoftware.yamlbeans.YamlConfig.QuoteCharEnum.DOUBLEQUOTE );

            // -----------------------
            // Leverage the wonderful com.esotericsoftware.yamlbeans library to load file-contents into a java.util.LinkedHashMap<String, Object>
            final LinkedHashMap dataObj = new com.esotericsoftware.yamlbeans.YamlReader( reader1 ).read( LinkedHashMap.class );
            @SuppressWarnings("unchecked")
            final LinkedHashMap<String, Object> data = (LinkedHashMap<String, Object>) dataObj;

            //======================================================================
            // run the command requested by user
            final Object output = cmd.processCommand( cmdLineArgs, data );

            // if ( output instanceof ArrayList ) {
            //     // for now do nothing.
            // } else if ( output instanceof java.util.LinkedHashMap ) {
            //     // for now do nothing.
            // } else {
            //     System.err.println("Internal error: unable to process output from cmd.processCommand() of type ["+ output.getClass().getName() +"]");
            //     System.exit(14);
            // }

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
            } else if ( cmdLineArgs.isReplaceCmd ) { // see common code in next block-of-code below
            } else if ( cmdLineArgs.isMacroCmd ) {   // see common code in next block-of-code below
            } else if ( cmdLineArgs.isBatchCmd ) {   // see common code in next block-of-code below
            } else {
            }

            if (cmdLineArgs.isDelCmd || cmdLineArgs.isReplaceCmd || cmdLineArgs.isMacroCmd) {
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
            // cleanup & close-out things.    This will actually do work for DELETE, REPLACE and MACRO commands
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
            ReadYamlEntry readcmd = new ReadYamlEntry( _cmdLineArgs.verbose );
            readcmd.searchYamlForPattern( _data, _cmdLineArgs.yamlRegExpStr, _cmdLineArgs.yamlPatternDelimiter );
            final LinkedList<Object> outputStr = readcmd.getOutput();
            return outputStr;

        } else if ( _cmdLineArgs.isListCmd ) {
            ListYamlEntry listcmd = new ListYamlEntry( _cmdLineArgs.verbose, YAMLPath.DEFAULTPRINTDELIMITER );
            listcmd.searchYamlForPattern( _data, _cmdLineArgs.yamlRegExpStr, _cmdLineArgs.yamlPatternDelimiter );
            final ArrayList<String> outputStr = listcmd.getOutput();
            return outputStr;

        } else if ( _cmdLineArgs.isDelCmd) {
            if ( _cmdLineArgs.verbose ) System.out.println(CLASSNAME + ": processCommand(): about to start DELETE command");
            DeleteYamlEntry delcmd = new DeleteYamlEntry( _cmdLineArgs.verbose);
            delcmd.searchYamlForPattern( _data, _cmdLineArgs.yamlRegExpStr, _cmdLineArgs.yamlPatternDelimiter );
            // writer.write( _data ); // The contents of java.util.LinkedHashMap<String, Object> has some YAML rows removed. so, dump it.
            return _data;

        } else if ( _cmdLineArgs.isReplaceCmd ) {
            if (_cmdLineArgs.verbose) System.out.println(CLASSNAME + ": processCommand(): loading @Replace-file: " + _cmdLineArgs.replaceFilePath);
            Object replContent = null;
            if ( !_cmdLineArgs.replaceFilePath.startsWith("@") ) {
                // user provided a SIMPLE String as the RHS-replacement value
                replContent = _cmdLineArgs.replaceFilePath;
            } else {
                // user provided a FILE for replacement-content (instead of a simple-string)
                try {
                    final java.io.InputStream is2 = new java.io.FileInputStream( _cmdLineArgs.replaceFilePath.substring(1)); // remove '@' as the 1st character in the user's input
                    final java.io.Reader reader2 = new java.io.InputStreamReader(is2);
                    // Leverage the wonderful com.esotericsoftware.yamlbeans to load replace-file
                    // contents into a java.util.LinkedHashMap<String, Object>
                    replContent = new com.esotericsoftware.yamlbeans.YamlReader(reader2).read(LinkedHashMap.class);
                    if (_cmdLineArgs.verbose) System.out.println( CLASSNAME + ": processCommand(): Loaded REPLACEMENT-contents: [" + replContent.toString() + "]");
                } catch (java.io.IOException e) {
                    e.printStackTrace(System.err);
                    System.err.println( CLASSNAME + ": processCommand(): trouble with REPLACEMENT-File: '" + _cmdLineArgs.replaceFilePath.substring(1) + "'.");
                    System.exit(11);
                }
            } // if-else startsWith"@"
            if (_cmdLineArgs.verbose) System.out.println( CLASSNAME + ": processCommand(): about to start CHANGE/REPLACE command using: [" + replContent.toString() + "]");
            ReplaceYamlEntry replcmd = new ReplaceYamlEntry(_cmdLineArgs.verbose, replContent);
            replcmd.searchYamlForPattern(_data, _cmdLineArgs.yamlRegExpStr, _cmdLineArgs.yamlPatternDelimiter);
            // writer.write(_data); // The contents of java.util.LinkedHashMap<String, Object> has been updated with replacement strings. so, dump it.
            return _data;

        } else if ( _cmdLineArgs.isMacroCmd ) {
            if (_cmdLineArgs.verbose) System.out.println( CLASSNAME + ": processCommand(): loading Props file [" + _cmdLineArgs.propertiesFilePath + "]");
            final Properties properties = new Properties();
            if (_cmdLineArgs.propertiesFilePath != null) {
                java.io.InputStream input = new java.io.FileInputStream( _cmdLineArgs.propertiesFilePath );
                properties.load( input );
            }
            if (_cmdLineArgs.verbose) System.out.println( CLASSNAME + ": processCommand(): about to start MACRO command using: [Props file [" + _cmdLineArgs.propertiesFilePath + "]");
            MacroYamlProcessor macro = new MacroYamlProcessor(_cmdLineArgs.verbose);
            final LinkedHashMap<String, Object> outpMap = new LinkedHashMap<>();
            macro.recursiveSearch( _data, outpMap, properties );
            // writer.write(outpMap); // The contents of java.util.LinkedHashMap<String, Object> has been updated with replacement strings. so, dump it.
            return outpMap;

        } else if ( _cmdLineArgs.isBatchCmd ) {
            if (_cmdLineArgs.verbose) System.out.println( CLASSNAME + ": processCommand(): about to start MACRO command using: BATCH file [" + _cmdLineArgs.batchFilePath + "]");
            BatchYamlProcessor batcher = new BatchYamlProcessor( _cmdLineArgs.verbose );
            batcher.go( _data, _cmdLineArgs.batchFilePath );
            // writer.write(outpMap); // The contents of java.util.LinkedHashMap<String, Object> has been updated with replacement strings. so, dump it.
            return null;

        } else {
            final String es = CLASSNAME + ": processCommand(): Unimplemented command: " + _cmdLineArgs.toString();
            System.err.println( es );
            throw new Exception( es );
        }
        // return null; // should Not reach here!
    }

}
