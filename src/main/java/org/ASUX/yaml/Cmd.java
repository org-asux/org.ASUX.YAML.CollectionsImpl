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
public class Cmd {

    public static final String CLASSNAME = Cmd.class.getName();

    // private static final String TMP FILE = System.getProperty("java.io.tmpdir") +"/org.ASUX.yaml.STDOUT.txt";

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     * This is NOT testing code. It's actual means by which user's command line arguments are read and processed
     * @param args user's commandline arguments
     */
    public static void main( String[] args )
    {
        CmdLineArgs cmdLineArgs = null;
        final java.io.StringWriter stdoutSurrogate = new java.io.StringWriter();

        try {
            cmdLineArgs = new CmdLineArgs( args );
            CmdInvoker cmdinvoker = new CmdInvoker( cmdLineArgs.verbose, cmdLineArgs.showStats );
            if (cmdLineArgs.verbose) System.out.println( CLASSNAME + ": main(String[]): getting started with cmdline args = " + cmdLineArgs + " " );

            cmdinvoker.getMemoryAndContext().getYamlLoader().setYamlLibrary( cmdLineArgs.YAMLLibrary );
            cmdinvoker.getMemoryAndContext().getYamlWriter().setYamlLibrary( cmdLineArgs.YAMLLibrary );
            if (cmdLineArgs.verbose) System.out.println( CLASSNAME + ": main(String[]): set YAML-Library to [" + cmdLineArgs.YAMLLibrary + "]" );

            //======================================================================
            // read input, whether it's System.in -or- an actual input-file
            if (cmdLineArgs.verbose) System.out.println(CLASSNAME + ": about to load file: " + cmdLineArgs.inputFilePath );
            final java.io.InputStream is1 = ( cmdLineArgs.inputFilePath.equals("-") ) ? System.in
                    : new java.io.FileInputStream(cmdLineArgs.inputFilePath);
            final java.io.Reader filereader = new java.io.InputStreamReader(is1);

            final org.ASUX.common.Output.Object<?> inputObj = cmdinvoker.getMemoryAndContext().getYamlLoader().load( filereader );
            if ( inputObj.getType() != OutputType.Type_LinkedHashMap && inputObj.getType() != OutputType.Type_KVPairs )
                throw new Exception("The input provided by '"+ cmdLineArgs.inputFilePath +"' did Not return a proper YAML.  Got = "+ inputObj );

            if (cmdLineArgs.verbose) System.out.println( CLASSNAME + ": main(String[]): loaded data of type [" + (inputObj==null?"null":inputObj.getType()) + "]" );
            if (cmdLineArgs.verbose) System.out.println( CLASSNAME + ": main(String[]): loaded data of type [" + inputObj + "]" );
            final LinkedHashMap<String, Object> inputData = inputObj.getMap();

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

            //======================================================================
            // run the command requested by user
            final Object output = cmdinvoker.processCommand( cmdLineArgs, inputData );
            if (cmdLineArgs.verbose) System.out.println( CLASSNAME + ": main(String[]): processing of entire command returned [" + (output==null?"null":output.getClass().getName()) + "]" );

            //======================================================================
            final java.io.Writer javawriter = ( cmdLineArgs.outputFilePath.equals("-") )
                ? stdoutSurrogate // new java.io.FileWriter(TMP FILE)
                : new java.io.FileWriter(cmdLineArgs.outputFilePath);

            final GenericYAMLWriter writer = cmdinvoker.getMemoryAndContext().getYamlWriter();
            // writer.prepare( stdoutSurrogate, cmdLineArgs.outputFilePath );
            writer.prepare( javawriter );

            //======================================================================
            // post completion of YAML processing
            if ( cmdLineArgs.isReadCmd ) {
                @SuppressWarnings("unchecked")
                final LinkedList<Object> list = ( LinkedList<Object> ) output;
                writer.write( list );
            } else if ( cmdLineArgs.isTableCmd ) {
                @SuppressWarnings("unchecked")
                final LinkedList< ArrayList<String> > list = ( LinkedList< ArrayList<String> > ) output;
                writer.write( list );
            } else if ( cmdLineArgs.isListCmd ) {
                @SuppressWarnings("unchecked")
                final ArrayList<String> arr = ( ArrayList<String> ) output;
                writer.write( arr );
            } else if ( cmdLineArgs.isDelCmd ) {
            } else if ( cmdLineArgs.isInsertCmd )   { // see common code in next block-of-code below
            } else if ( cmdLineArgs.isReplaceCmd )  { // see common code in next block-of-code below
            } else if ( cmdLineArgs.isMacroCmd )    {   // see common code in next block-of-code below
            } else if ( cmdLineArgs.isBatchCmd )    {   // see common code in next block-of-code below
            } else {
            }

            if (cmdLineArgs.isDelCmd || cmdLineArgs.isInsertCmd || cmdLineArgs.isReplaceCmd || cmdLineArgs.isMacroCmd || cmdLineArgs.isBatchCmd ) {
                if (cmdLineArgs.verbose) System.out.println( CLASSNAME + ": main(String[]): saving the final output " + output + "]" );
                if (cmdLineArgs.verbose) System.out.println( CLASSNAME + ": main(String[]): final output is of type " + output.getClass().getName() + "]" );
                writer.write( output );
            } // if

            //======================================================================
            // cleanup & close-out things.    This will actually do work for DELETE, INSERT, REPLACE and MACRO commands
            if ( cmdLineArgs.outputFilePath.equals("-") ) {
                // if we're writing to STDOUT/System.out ..
                if (writer != null) writer.close(); // Yes! Even for stdout/System.out .. we need to call close(). This is driven by one the YAML libraries (eso teric soft ware)
            } else {
                if (writer != null) writer.close(); // close the actual file.
            }
            stdoutSurrogate.flush();

            // Now since we have a surrogate for STDOUT for use by , let's dump its output onto STDOUT!
            if ( cmdLineArgs.outputFilePath.equals("-") ) {
                if (cmdLineArgs.verbose) System.out.println( CLASSNAME + ": main(String[]): dumpingh the final output to STDOUT" );
                final String outputStr = stdoutSurrogate.toString();
                try {
                    // final java.io.InputStream istrm = new java.io.FileInputStream( TMP FILE );
                    final java.io.Reader reader6 = new java.io.StringReader( outputStr );
                    final java.util.Scanner scanner = new java.util.Scanner( reader6 );
                    while (scanner.hasNextLine()) {
                        System.out.println( scanner.nextLine() );
                    }
                // } catch (java.io.IOException e) {
                //     e.printStackTrace(System.err);
                //     System.err.println( CLASSNAME + ": openBatchFile(): Failure to read Command-output contents ["+ outputStr +"]" );
                //     System.exit(102);
                } catch (Exception e) {
                    e.printStackTrace(System.err);
                    System.err.println( CLASSNAME + ": openBatchFile(): Unknown Internal error: re: Command-output contents ["+ outputStr +"]" );
                    System.exit(103);
                }
            }

        } catch (YAMLPath.YAMLPathException e) {
            e.printStackTrace(System.err);
            System.err.println( "YAML-Path pattern is invalid: '" + cmdLineArgs.yamlRegExpStr + "'.");
            System.exit(8);
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

}
