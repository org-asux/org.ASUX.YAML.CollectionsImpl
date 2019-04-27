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

import java.util.regex.*;

import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.Set;

/**
 *  <p>This concrete class is part of a set of 4 concrete sub-classes (representing YAML-COMMANDS to read/query, list, delete and replace ).</p>
 *  <p>This class contains implementation batch-processing of multiple YAML commands (combinations of read, list, delete, replace, macro commands)</p>
 *  <p>This org.ASUX.yaml GitHub.com project and the <a href="https://github.com/org-asux/org.ASUX.cmdline">org.ASUX.cmdline</a> GitHub.com projects, would
 *  simply NOT be possible without the genius Java library <a href="https://github.com/EsotericSoftware/yamlbeans">"com.esotericsoftware.yamlbeans"</a>.</p>
 *  <p>See full details of how to use this, in {@link org.ASUX.yaml.Cmd} as well as the <a href="https://github.com/org-asux/org-ASUX.github.io/wiki">org.ASUX.cmdline</a> GitHub.com projects.</p>
 * @see org.ASUX.yaml.Cmd
 */
public class BatchYamlProcessor {

    public static final String CLASSNAME = "org.ASUX.yaml.BatchYamlProcessor";

    // enum FileType { PROPERTIESFILE, BATCHFILE };

    /** <p>Whether you want deluge of debug-output onto System.out.</p><p>Set this via the constructor.</p>
     *  <p>It's read-only (final data-attribute).</p>
     */
    private final boolean verbose;

    /** <p>The only constructor - public/private/protected</p>
     *  @param _verbose Whether you want deluge of debug-output onto System.out.
     */
    public BatchYamlProcessor(boolean _verbose) {
        this.verbose = _verbose;
    }

    private BatchYamlProcessor() { this.verbose = false;}

    private class BatchFileException extends Exception {
        private static final long serialVersionUID = 1L;
        public BatchFileException(String _s) { super(_s); }
    }

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

    // I prefer a LinkedHashMap over a plain HashMap.. as it can help with future enhancements like Properties#1, #2, ..
    // That is being aware of Sequence in which Property-files are loaded.   Can't do that with HashMap
    private LinkedHashMap<String,Properties> AllProps = new LinkedHashMap<String,Properties>();

    private LinkedHashMap<String, LinkedHashMap<String, Object> > savedOutputMaps = new LinkedHashMap<>();

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    /** As com.esotericsoftware.yamlBeans has some magic where Keys are NOT strings! ..
     *  In order for me to add new entries to the _map created by that library, I need to go thru hoops.
     *  @param _batchFileName batchfile full path (ry to avoid relative paths)
     *  @param _inputMap This contains the java.utils.LinkedHashMap&lt;String, Object&gt; (created by com.esotericsoftware.yamlbeans library) containing the entire Tree representing the YAML file.
     *  @param _returnedMap Send in a BLANK/EMPTY/NON-NULL java.utils.LinkedHashMap&lt;String, Object&gt; object and you'll get the final Map output representing all processing done by the batch file
     *  @return true = all processed WITHOUT ANY errors.  False = Some error somewhere!
     */
    public boolean go( final String _batchFileName, final LinkedHashMap<String, Object> _inputMap,
                    final LinkedHashMap<String, Object> _returnedMap ) {

        if ( _batchFileName == null ) return true;  // null is treated as  batchfile with ZERO commands.
        // LinkedHashMap<String,Object> inputMap = _inputMap;
        String line = null;

        final ConfigFileProcessor batchCmds = new ConfigFileProcessor(false);

        try {
            if ( batchCmds.openFile( _batchFileName, true ) ) {

                if ( this.verbose ) System.out.println( CLASSNAME + ": openBatchFile(): successfully opened _batchFileName [" + _batchFileName +"]" );

                final LinkedHashMap<String, Object>  retMap = this.processBatch( false, batchCmds, _inputMap );

                _returnedMap.putAll( retMap );
                return true; // all ok

            } else { // if-else openFile()
                return false;
            }

        } catch (com.esotericsoftware.yamlbeans.YamlException e) { // Warning: This must PRECEDE IOException, else compiler error.
            e.printStackTrace(System.err);
            System.err.println( CLASSNAME + ": openBatchFile():\n\nERROR In Batchfile ["+ _batchFileName +"] @ lime# "+ batchCmds.getLineNum() +".   SERIOUS Internal error: unable to process YAML.  See details above.");
            // System.exit(9);
        } catch (BatchFileException bfe) {
            System.err.println(CLASSNAME + ": openBatchFile():\n\n" + bfe.getMessage() );
        } catch(FileNotFoundException fe) {
            fe.printStackTrace(System.err);
            System.err.println(CLASSNAME + ": openBatchFile():\n\nERROR In Batchfile ["+ _batchFileName +"] @ lime# "+ batchCmds.getLineNum() +".   See details on error above. ");
        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.err.println( CLASSNAME + ": openBatchFile(): Unknown Serious Internal error.\n\n ERROR while processing Batchfile ["+ _batchFileName +"] @ lime# "+ batchCmds.getLineNum() +".   See details above");
            // System.exit(103);
        }

        return false;
    }

    //=======================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=======================================================================

    /**
     * This function is meant for recursion.  Recursion happens when 'foreach' or 'batch' commands are detected in a batch file.
     * After this function completes processing SUCCESSFULLY.. it returns a java.utils.LinkedHashMap&lt;String, Object&gt; object.
     * If there is any failure, either return value is NULL or an Exception is thrown.
     * @param _bInRecursion true or false, whether this invocation is a recursive call or not.  If true, when the 'end' or <EOF> is detected.. this function returns
     * @param _batchCmds an object of type ConfigFileProcessor created by reading a batch-file, or .. .. the contents between 'foreach' and 'end' commands
     * @param _inputMap input YAML as an object of type 
     * @return After this function completes processing SUCCESSFULLY.. it returns a java.utils.LinkedHashMap&lt;String, Object&gt; object.  If there is any failure, either return value is NULL or an Exception is thrown.
     * @throws com.esotericsoftware.yamlbeans.YamlException
     * @throws BatchFileException
     * @throws FileNotFoundException
     * @throws Exception
     */
    private LinkedHashMap<String, Object> processBatch( final boolean _bInRecursion, final ConfigFileProcessor _batchCmds,
                        final LinkedHashMap<String, Object> _inputMap )
        throws com.esotericsoftware.yamlbeans.YamlException, BatchFileException, FileNotFoundException, Exception
    {

        // Set<String> keySet = null; // This will get set once we hit the 'foreach' keyword within the batchfile.
        LinkedHashMap<String,Object> inputMap = _inputMap;
        LinkedHashMap<String, Object> tempOutputMap = null;

        while ( _batchCmds.hasNextLine() ) {

            // start each loop, with an 'empty' placeholder Map, to collect output of current batch command
            tempOutputMap = new LinkedHashMap<String, Object>();

            // if ( this.verbose ) 
            System.out.println(_batchCmds.nextLine());

            //------------------------------------------------
            final ConfigFileProcessor.KVPair kv = _batchCmds.isPropertyLine(); // could be null, implying NOT a kvpair
            if ( kv != null) {
                final Properties props = new Properties();
                final String fn = MacroYamlProcessor.evaluateMacros( kv.value, this.AllProps );
                props.load( new FileInputStream( fn ) );
                this.AllProps.put( kv.key, props ); // This line is the action taken by this 'PropertyFile' line of the batchfile
            }

            //------------------------------------------------
            final String saveTo_AsIs = _batchCmds.isSaveToLine();
            final String saveTo = MacroYamlProcessor.evaluateMacros( saveTo_AsIs, this.AllProps );
            if ( saveTo != null ) {
                savedOutputMaps.put( saveTo, tempOutputMap );
                if ( saveTo.startsWith("@") ) {
                    final com.esotericsoftware.yamlbeans.YamlWriter yamlwriter
                            = new com.esotericsoftware.yamlbeans.YamlWriter( new java.io.FileWriter( saveTo ) );
                    yamlwriter.write( tempOutputMap );
                    yamlwriter.close();
                }
            }

            //------------------------------------------------
            final String inputFrom_AsIs = _batchCmds.isUseAsInputLine();
            final String inputFrom = MacroYamlProcessor.evaluateMacros( inputFrom_AsIs, this.AllProps );
            if ( inputFrom != null ) {
                inputMap = savedOutputMaps.get( inputFrom );
                // we might be 'double-loading' inputMap - once from memory and 2nd-time from the file (heck, why not.)
                if ( inputFrom.startsWith("@") ) {
                    final java.io.Reader reader1 = new InputStreamReader( new FileInputStream(( inputFrom ))  );
                    final LinkedHashMap mapObj = new com.esotericsoftware.yamlbeans.YamlReader( reader1 ).read( LinkedHashMap.class );
                    @SuppressWarnings("unchecked")
                    final LinkedHashMap<String, Object> inpMp = (LinkedHashMap<String, Object>) mapObj;
                    reader1.close();
                }
            }

            //------------------------------------------------
            final boolean bForEach = _batchCmds.isForEachLine();
            if ( this.verbose ) System.out.println("\t Loop begins=[" + bForEach + "]");
            if ( bForEach ) {

                // if ( keySet != null ) {
                //     throw new BatchFileException("ERROR In Batchfile ["+ _batchCmds.getFileName() +"] @ lime# "+ _batchCmds.getLineNum() +".  Duplicate 'foreach' keyword detected.. before a 'end' keyword occured.");
                // }
                int forEach_ItemNum = -1;
                for ( String key : inputMap.keySet() ) {

                    forEach_ItemNum ++;
                    System.out.println("\t foreach #"+ forEach_ItemNum +"=[" + key + "] .. ");
                    // recurse .. 
                    final Object o = inputMap.get(key);
                    if ( o instanceof String ) {
                        this.processBatch_StringsOnly( _batchCmds, o.toString() );
                        // this special function processBatch_StringsOnly() is rarely about producing new YAML
                        // So.. let's leave tempOutputMap remain unchanged (as it was before the foreach command started)
                    } else if ( o instanceof LinkedHashMap ) {
                        @SuppressWarnings("unchecked")
                        final LinkedHashMap<String, Object> rhsMap = (LinkedHashMap<String, Object>) o;
                        final LinkedHashMap<String, Object> outpMap1 = this.processBatch( true, _batchCmds, rhsMap );
                        tempOutputMap.putAll( outpMap1 );
                    } else if ( o instanceof ArrayList ) {
                        // 99.9% chance that the ENCOMPASSING/ABOVE FOR-Loop has only 1 iteration
                        // So, we iterate over the elements of the ArrayList
                        @SuppressWarnings("unchecked")
                        final ArrayList arr = (ArrayList) o;
                        for ( int ix=0;  ix < arr.size(); ix ++ ) {
                            if ( arr.get(ix) instanceof String ) {
                                this.processBatch_StringsOnly( _batchCmds, arr.get(ix).toString() );
                                // this special function processBatch_StringsOnly() is rarely about producing new YAML
                                // So.. let's leave tempOutputMap remain unchanged (as it was before the foreach command started)
                            } else if ( arr.get(ix) instanceof LinkedHashMap ) {
                                @SuppressWarnings("unchecked")
                                final LinkedHashMap<String, Object> arrMap = (LinkedHashMap<String, Object>) arr.get(ix);
                                    final LinkedHashMap<String, Object> outpMap2 = this.processBatch( true, _batchCmds, arrMap );
                                tempOutputMap.putAll( outpMap2 );
                            } else {
                                throw new BatchFileException("ERROR In Batchfile ["+ _batchCmds.getFileName() +"] @ lime# "+ _batchCmds.getLineNum() +" @ ArrayYAML Item # "+ ix +".  Duplicate 'foreach' keyword detected.. before a 'end' keyword occured.");
                            }
                        } // for arr.size()

                    } else {
                        throw new BatchFileException("ERROR In Batchfile ["+ _batchCmds.getFileName() +"] @ lime# "+ _batchCmds.getLineNum() +".  Duplicate 'foreach' keyword detected.. before a 'end' keyword occured.");
                    }

                } // for inputMap.keySet()

                // foreach is rarely about producing new YAML
                // So.. let's leave tempOutputMap remain unchanged (as it was before the foreach command started)

            } // if bForEach

            //------------------------------------------------
            final boolean bEndLine = _batchCmds.isEndLine();
            if ( this.verbose ) System.out.println("\t Loop ENDS=[" + bEndLine + "]");
            if ( bEndLine ) {
                if ( _bInRecursion ) {
                    return tempOutputMap;
                    // !!!!!!!!!!!! ATTENTION : Function exits here SUCCESSFULLY / NORMALLY. !!!!!!!!!!!!!!!!
                } else {
                    throw new BatchFileException("ERROR In Batchfile ["+ _batchCmds.getFileName() +"] @ lime# "+ _batchCmds.getLineNum() +".  UNEXPECTED 'end'' keyword detected.. No matching 'foreach' keyword detected prior.");
                }
            }

            //------------------------------------------------
            final String bSubBatch_AsIs = _batchCmds.isSubBatchLine();
            final String bSubBatch = MacroYamlProcessor.evaluateMacros( bSubBatch_AsIs, this.AllProps );
            if ( bSubBatch != null ) {
                // LinkedHashMap<String,Object> temp222OutputMap = new LinkedHashMap<String,Object> ();
                this.go( bSubBatch, inputMap, tempOutputMap );
                // technically, this.go() method is NOT meant to used recursively.  Semantically, this is NOT recursion :-(
            }

            //------------------------------------------------
            final String cmd_AsIs = _batchCmds.getCommand();
            final String cmd = MacroYamlProcessor.evaluateMacros( cmd_AsIs, this.AllProps );
            if ( cmd != null ) {

            }

            inputMap = tempOutputMap; // because we might be doing ANOTHER iteraton of the While() loop.

        } // while loop

        // reached end of file.
        return tempOutputMap;
    }

    //-----------------------------------------------------------------------------

    public LinkedHashMap<String, Object> processBatch_StringsOnly( final ConfigFileProcessor _batchCmds, final String _s ) {

        // WARNING: THERE is NO WAY .. I do NOT have an Input Map !!!!!!!!!!!!!!!!!!!!!
        final LinkedHashMap<String, Object> retMap = new LinkedHashMap<String, Object>();

        while ( _batchCmds.hasNextLine() ) {

            // if ( this.verbose ) 
            System.out.println(_batchCmds.nextLine());

            //------------------------------------------------
            final String bSubBatch_AsIs = _batchCmds.isSubBatchLine();
            final String bSubBatch = MacroYamlProcessor.evaluateMacros( bSubBatch_AsIs, this.AllProps );
            if ( bSubBatch != null ) {
                LinkedHashMap<String,Object> temp222OutputMap = new LinkedHashMap<String,Object> ();
                this.go( bSubBatch, new LinkedHashMap<String, Object>(), temp222OutputMap);  
                // technically, this.go() method is NOT meant to used recursively.  Semantically, this is NOT recursion :-(
                retMap.putAll( temp222OutputMap );
            }

            //------------------------------------------------
            final String cmd_AsIs = _batchCmds.getCommand();
            final String cmd = MacroYamlProcessor.evaluateMacros( cmd_AsIs, this.AllProps );
            if ( cmd != null ) {
            }

        } // while loop

        // reached end of file.
        return retMap;
    }

    //=======================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=======================================================================

    // For unit-testing purposes only
    public static void main(String[] args) {
        final BatchYamlProcessor o = new BatchYamlProcessor(false);
        LinkedHashMap<String, Object> inpMap = null;
        LinkedHashMap<String, Object> outpMap = null;
        o.go( args[0], inpMap, outpMap );
    }

}
