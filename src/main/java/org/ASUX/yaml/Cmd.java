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

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

//import java.util.Map;
//import java.util.LinkedList;
//import java.util.ArrayList;
import java.util.LinkedHashMap;

//import java.util.regex.*;

import org.junit.Test;
import static org.junit.Assert.*;

/** <p>This class is the "wrapper-processor" for the various "YAML-commands" while traverse a YAML file.</p>
 *  <p>The 4 YAML-COMMANDS are: read/query, list, delete and replace.</p>
 *  <p>This org.ASUX.yaml GitHub.com project and the <a href="https://github.com/org-asux/org.ASUX.cmdline">org.ASUX.cmdline</a> GitHub.com projects, would
 *  simply NOT be possible without the genius Java library <a href="https://github.com/EsotericSoftware/yamlbeans">"com.esotericsoftware.yamlbeans"</a>.</p>
 *  <p>See full details of how to use this in this GitHub project's wiki - or - in <a href="https://github.com/org-asux/org.ASUX.cmdline">org.ASUX.cmdline</a> GitHub.com project and its wiki.</p>
 *
 *  <p>Example: <code>java org.ASUX.yaml.Cmd --delete --yamlpath "paths.*.*.responses.200" -i $cwd/src/test/my-petstore-micro.yaml -o /tmp/output2.yaml  --double-quote</code><br>
 *  Example: <b><code>java org.ASUX.yaml.Cmd</code></b> will show all command line options supported.</p>
 * @see org.ASUX.yaml.YAMLPath
 * @see org.ASUX.yaml.CmdLineArgs
 *
 * @see org.ASUX.yaml.ReadYamlEntry
 * @see org.ASUX.yaml.ListYamlEntry
 * @see org.ASUX.yaml.DeleteYamlEntry
 * @see org.ASUX.yaml.ReplaceYamlEntry
 */
public class Cmd {

    public static final String CLASSNAME = "com.esotericsoftware.yamlbeans.Cmd";

    public final boolean verbose;

    public Cmd(boolean _verbose) {
        this.verbose = _verbose;
    }

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    /** This is NOT testing code.  It's actual means by which user's command line arguments are read and processed
     *  @param args user's commandline arguments
     */
    public static void main(String[] args) {

        CmdLineArgs cmdLineArgs = null;

        try{
            cmdLineArgs = new CmdLineArgs(args);

            if ( cmdLineArgs.verbose ) System.out.println(CLASSNAME + ": about to load file: " + cmdLineArgs.inputFilePath );
            InputStream is = new FileInputStream(cmdLineArgs.inputFilePath);
            Reader reader = new InputStreamReader(is);

            //-----------------------
            LinkedHashMap data = new com.esotericsoftware.yamlbeans.YamlReader(reader).read(LinkedHashMap.class);
            
            //-----------------------
            if ( cmdLineArgs.isReadCmd ) {
                ReadYamlEntry readcmd = new ReadYamlEntry( cmdLineArgs.verbose );
                readcmd.searchYamlForPattern( data, cmdLineArgs.yamlPathStr );
            } else if ( cmdLineArgs.isListCmd ) {
                ListYamlEntry listcmd = new ListYamlEntry( cmdLineArgs.verbose );
                listcmd.searchYamlForPattern( data, cmdLineArgs.yamlPathStr );
            } else if ( cmdLineArgs.isDelCmd ) {
                if ( cmdLineArgs.verbose ) System.out.println(CLASSNAME + ": about to load file.");

                DeleteYamlEntry delcmd = new DeleteYamlEntry( cmdLineArgs.verbose );
                delcmd.searchYamlForPattern( data, cmdLineArgs.yamlPathStr );

                com.esotericsoftware.yamlbeans.YamlWriter writer = new com.esotericsoftware.yamlbeans.YamlWriter( new FileWriter(cmdLineArgs.outputFilePath) );
                // writer.getConfig().writeConfig.setWriteRootTags(false); // Does NOTHING :-
                writer.getConfig().writeConfig.setWriteClassname( com.esotericsoftware.yamlbeans.YamlConfig.WriteClassName.NEVER ); // I hate !org.pkg.class within YAML files.  So does AWS I believe.
//                writer.getConfig().writeConfig.setQuoteChar( cmdLineArgs.quoteType );
//                writer.getConfig().writeConfig.setQuoteChar( com.esotericsoftware.yamlbeans.YamlConfig.QuoteCharEnum.NONE );
//                writer.getConfig().writeConfig.setQuoteChar( com.esotericsoftware.yamlbeans.YamlConfig.QuoteCharEnum.SINGLEQUOTE );
//                writer.getConfig().writeConfig.setQuoteChar( com.esotericsoftware.yamlbeans.YamlConfig.QuoteCharEnum.DOUBLEQUOTE );
                writer.write(data);
                writer.close();
            } else if ( cmdLineArgs.isReplaceCmd ) {
                ReplaceYamlEntry replcmd = new ReplaceYamlEntry( cmdLineArgs.verbose );
                replcmd.searchYamlForPattern( data, cmdLineArgs.yamlPathStr );

                com.esotericsoftware.yamlbeans.YamlWriter writer = new com.esotericsoftware.yamlbeans.YamlWriter( new FileWriter(cmdLineArgs.outputFilePath) );
                writer.getConfig().writeConfig.setWriteClassname( com.esotericsoftware.yamlbeans.YamlConfig.WriteClassName.NEVER ); // I hate !org.pkg.class within YAML files.  So does AWS I believe.
//                writer.getConfig().writeConfig.setQuoteChar( cmdLineArgs.quoteType );
            } else {
                System.err.println("Unimplemented command: "+cmdLineArgs.toString() );
            }

        } catch (com.esotericsoftware.yamlbeans.YamlException e) { // Warning: This must PRECEDE IOException, else compiler error.
            System.err.println(e.getMessage());
            System.err.println("Internal error: unable to process YAML");
            System.exit(9);
        } catch (java.io.FileNotFoundException e) {
            System.err.println(e.getMessage());
            System.err.println("INPUT-File Not found: '" + cmdLineArgs.inputFilePath +"'.");
            System.exit(3);
        } catch (java.io.IOException e) {
            System.err.println(e.getMessage());
            System.err.println("OUTPUT-File Not found: '" + cmdLineArgs.outputFilePath +"'.");
            System.exit(3);
        }
        
    }

}
