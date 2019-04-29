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
import java.io.InputStream;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.LinkedHashMap;

import java.util.Properties;
import java.util.Set;

// import com.fasterxml.jackson.databind.ObjectMapper; // This cannot read ARRAYS within JSON-STRINGS into Map

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

    // I prefer a LinkedHashMap over a plain HashMap.. as it can help with future enhancements like Properties#1, #2, ..
    // That is being aware of Sequence in which Property-files are loaded.   Can't do that with HashMap
    private LinkedHashMap<String,Properties> AllProps = new LinkedHashMap<String,Properties>();

    private LinkedHashMap<String, LinkedHashMap<String, Object> > savedOutputMaps = new LinkedHashMap<>();

    /** <p>Whether you want deluge of debug-output onto System.out.</p><p>Set this via the constructor.</p>
     *  <p>It's read-only (final data-attribute).</p>
     */
    private final boolean verbose;

    /** <p>The only constructor - public/private/protected</p>
     *  @param _verbose Whether you want deluge of debug-output onto System.out.
     */
    public BatchYamlProcessor(boolean _verbose) {
        this.verbose = _verbose;
        this.AllProps.put( BatchFileGrammer.FOREACH, new Properties() );
    }

    private BatchYamlProcessor() { this.verbose = false;} // Do Not use this.

    //------------------------------------------------------------------------------
    private static class BatchFileException extends Exception {
        private static final long serialVersionUID = 1L;
        public BatchFileException(String _s) { super(_s); }
    }

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

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

        final BatchFileGrammer batchCmds = new BatchFileGrammer(false);

        try {
            if ( batchCmds.openFile( _batchFileName, true ) ) {

                if ( this.verbose ) System.out.println( CLASSNAME + ": go(): successfully opened _batchFileName [" + _batchFileName +"]" );

                final LinkedHashMap<String, Object>  retMap1 = this.processBatch( false, batchCmds, _inputMap );
                if ( this.verbose ) System.out.println( CLASSNAME +" go():  retMap1 =" + retMap1 +"\n\n");

                _returnedMap.putAll( retMap1 );
                // System.out.println( CLASSNAME +" go():  _returnedMap =" + _returnedMap +"\n\n");
                return true; // all ok

            } else { // if-else openFile()
                return false;
            }

        } catch (com.esotericsoftware.yamlbeans.YamlException e) { // Warning: This must PRECEDE IOException, else compiler error.
            e.printStackTrace(System.err);
            System.err.println( CLASSNAME + ": go():\n\nERROR In Batchfile ["+ _batchFileName +"] @ lime# "+ batchCmds.getLineNum() +".   SERIOUS Internal error: unable to process YAML.  See details above.");
            // System.exit(9);
        } catch (BatchFileException bfe) {
            System.err.println(CLASSNAME + ": go():\n\n" + bfe.getMessage() );
        } catch(FileNotFoundException fe) {
            fe.printStackTrace(System.err);
            System.err.println(CLASSNAME + ": go():\n\nERROR In Batchfile ["+ _batchFileName +"] @ lime# "+ batchCmds.getLineNum() +".   See details on error above. ");
        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.err.println( CLASSNAME + ": go(): Unknown Serious Internal error.\n\n ERROR while processing Batchfile ["+ _batchFileName +"] @ lime# "+ batchCmds.getLineNum() +".   See details above");
            // System.exit(103);
        }

        return false;
    }

    //=======================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=======================================================================

    private void printAllProps( final String _printPrefix ) {
        System.out.print( _printPrefix + " ... @@@@@@@@@@@@@@@@@@@@@@@@@ >> "); // this.AllProps.forEach( (k, v) -> System.out.println(k + " = " + v.toString() ) );
        for ( String key : this.AllProps.keySet() ) {
            System.err.print(key +" = ");
            final Properties p = (Properties)this.AllProps.get(key);
            p.list(System.out);
        }
        System.out.println();
    }

    /**
     * This function is meant for recursion.  Recursion happens when 'foreach' or 'batch' commands are detected in a batch file.
     * After this function completes processing SUCCESSFULLY.. it returns a java.utils.LinkedHashMap&lt;String, Object&gt; object.
     * If there is any failure whatsoever then the batch-file processing stops immediately.
     * If there is any failure whatsoever either return value is NULL or an Exception is thrown.
     * @param _bInRecursion true or false, whether this invocation is a recursive call or not.  If true, when the 'end' or <EOF> is detected.. this function returns
     * @param _batchCmds an object of type BatchFileGrammer created by reading a batch-file, or .. .. the contents between 'foreach' and 'end' commands
     * @param _inputMap input YAML as an object of type 
     * @return After this function completes processing SUCCESSFULLY.. it returns a java.utils.LinkedHashMap&lt;String, Object&gt; object.  If there is any failure, either return value is NULL or an Exception is thrown.
     * @throws com.esotericsoftware.yamlbeans.YamlException
     * @throws BatchFileException if any failure trying to execute any entry in the batch file.  Batch file processing will Not proceed once a problem occurs.
     * @throws FileNotFoundException if the batch file to be loaded does Not exist
     * @throws Exception
     */
    private LinkedHashMap<String, Object> processBatch( final boolean _bInRecursion, final BatchFileGrammer _batchCmds,
                        final LinkedHashMap<String, Object> _inputMap )
        throws com.esotericsoftware.yamlbeans.YamlException, BatchFileException, FileNotFoundException, Exception
    {

        // Set<String> keySet = null; // This will get set once we hit the 'foreach' keyword within the batchfile.
        LinkedHashMap<String,Object> inputMap = _inputMap;
        LinkedHashMap<String, Object> tempOutputMap = null;

        if ( this.verbose ) System.out.println( CLASSNAME +" processBatch(@BeginningOfFunction): recursion="+ _bInRecursion +" & "+ _batchCmds.hasNextLine() +" @ line# "+ _batchCmds.getLineNum() );
        while ( _batchCmds.hasNextLine() ) {

            final String line = _batchCmds.nextLine();
            // start each loop, with an 'empty' placeholder Map, to collect output of current batch command
            tempOutputMap = new LinkedHashMap<String, Object>();

            // if ( this.verbose ) 
            if ( this.verbose ) System.out.println( CLASSNAME +" processBatch(while-loop,recursion="+ _bInRecursion +"): INSIDE-WHILE-LOOP @ Batch-line# "+ _batchCmds.getLineNum() +" = ["+ _batchCmds.currentLine() +"]" );

            //------------------------------------------------
            final BatchFileGrammer.KVPair kv = _batchCmds.isPropertyLine(); // could be null, implying NOT a kvpair
            if ( kv != null) {
                final Properties props = new Properties();
                final String fn = MacroYamlProcessor.evaluateMacros( kv.value, this.AllProps );
                props.load( new FileInputStream( fn ) );
                this.AllProps.put( kv.key, props ); // This line is the action taken by this 'PropertyFile' line of the batchfile
                tempOutputMap = inputMap; // as nothing changes re: Input and Output Maps.
            }

            //------------------------------------------------
            final String printLine = _batchCmds.isPrintLine();
            if ( this.verbose ) System.out.println( CLASSNAME +" processBatch(isPrintLine): \t print command=[" + printLine + "]");
            if ( printLine != null ) {
                final String outputStr = MacroYamlProcessor.evaluateMacros( printLine, this.AllProps );
                System.out.println( outputStr );
                tempOutputMap = inputMap; // as nothing changes re: Input and Output Maps.
            }

            //------------------------------------------------
            // Might sound crazy - at first.  inpMap for this 'saveAs' command is the output of prior command.
            final String saveTo_AsIs = _batchCmds.isSaveToLine();
            if ( saveTo_AsIs != null ) {
                processSaveToLine( _batchCmds, inputMap );
                tempOutputMap = inputMap; // as nothing changes re: Input and Output Maps.
            }

            //------------------------------------------------
            final String inputFrom_AsIs = _batchCmds.isUseAsInputLine();
            if ( inputFrom_AsIs != null ) {
                tempOutputMap = processUseAsInputLine( _batchCmds );
            }

            //------------------------------------------------
            final boolean bForEach = _batchCmds.isForEachLine();
            // if ( this.verbose ) 
            if ( bForEach ) {
                if ( this.verbose ) System.out.println( CLASSNAME +" processBatch(foreach): \t 'foreach'_cmd detected'=[" + bForEach + "]");

                // in case we have an earlier forloop within which we have a new forloop starting below..
                // .. let's save some values, to RESTORE then at end of for() starting below.
                final Properties forLoopProps = this.AllProps.get( BatchFileGrammer.FOREACH );
                final String outerForLoopIndex = forLoopProps.getProperty( BatchFileGrammer.FOREACH_INDEX );
                // We'll allow NESTED FOREACH, so do NOT..    throw new BatchFileException("ERROR In Batchfile ["+ _batchCmds.getFileName() +"] @ lime# "+ _batchCmds.getLineNum() +".  Duplicate 'foreach' keyword detected.. before a 'end' keyword occured.");

                int forEach_ItemNum = -1;

                if ( this.verbose ) System.out.println( CLASSNAME +": processBatch(foreach): InputMap = "+ inputMap.toString() );

                for ( String key : inputMap.keySet() ) {

                    forEach_ItemNum ++;
                    if ( this.verbose ) System.out.println(CLASSNAME +": processBatch(foreach): foreach #"+ forEach_ItemNum +" where key=[" + key + "] .. ");

                    forLoopProps.setProperty( BatchFileGrammer.FOREACH_INDEX, ""+forEach_ItemNum ); // to be used by all commands INSIDE the 'foreach' block-inside-batchfile

                    final Object o = inputMap.get(key);
                    if ( o instanceof String ) {
                        this.processBatch_StringsOnly( BatchFileGrammer.deepClone(_batchCmds), o.toString() );
                        // this special function processBatch_StringsOnly() is rarely about producing new YAML
                        // So.. let's leave tempOutputMap remain unchanged (as it was before the foreach command started)

                    } else if ( o instanceof LinkedHashMap ) {
                        if ( this.verbose ) System.out.println( CLASSNAME +" processBatch(foreach): @@@@@@@@@@@@@@@@@ foreach/LinkedHashMap-Object = "+ o.toString() );
                        @SuppressWarnings("unchecked")
                        final LinkedHashMap<String, Object> rhsMap = (LinkedHashMap<String, Object>) o;
                        final LinkedHashMap<String, Object> outpMap1 = this.processBatch( true, BatchFileGrammer.deepClone(_batchCmds), rhsMap );
                        tempOutputMap.putAll( outpMap1 );

                    } else if ( o instanceof ArrayList ) {
                        // 99.9% chance that the ENCOMPASSING/ABOVE FOR-Loop has only 1 iteration
                        // So, we iterate over the elements of the ArrayList
                        @SuppressWarnings("unchecked")
                        final ArrayList arr = (ArrayList) o;

                        for ( int ix=0;  ix < arr.size(); ix ++ ) {
                            if ( this.verbose ) System.out.println( CLASSNAME +" processBatch(foreach): @@@@@@@@@@@@@@@@@ foreach/Array-index #"+ ix +" : Object = "+ arr.get(ix).toString() );
                            if ( arr.get(ix) instanceof String ) {
                                // System.out.println( CLASSNAME +" processBatch(foreach/STRINGS): @@@@@@@@@@@@@@@@@ foreach/Array-index #"+ ix +" : about to do RECURSION!!" );
                                this.processBatch_StringsOnly( BatchFileGrammer.deepClone(_batchCmds), arr.get(ix).toString() );
                                // this special function processBatch_StringsOnly() is rarely about producing new YAML
                                // So.. let's leave tempOutputMap remain unchanged (as it was before the foreach command started)
                            } else if ( arr.get(ix) instanceof LinkedHashMap ) {
                                // System.out.println( CLASSNAME +" processBatch(foreach/MAPObjects): @@@@@@@@@@@@@@@@@ foreach/Array-index #"+ ix +" : about to do RECURSION!!" );
                                @SuppressWarnings("unchecked")
                                final LinkedHashMap<String, Object> arrMap = (LinkedHashMap<String, Object>) arr.get(ix);
                                final LinkedHashMap<String, Object> outpMap2 = this.processBatch( true, BatchFileGrammer.deepClone(_batchCmds), arrMap );
                                if ( outpMap2 != null )
                                    tempOutputMap.putAll( outpMap2 );
                            } else {
                                throw new BatchFileException("ERROR In Batchfile ["+ _batchCmds.getFileName() +"] @ lime# "+ _batchCmds.getLineNum() +" @ ArrayYAML Item # "+ ix +".  But.. item is of type ["+ arr.get(ix).getClass().getName() +"]");
                            }
                        } // for arr.size()

                    } else {
                        throw new BatchFileException("ERROR In Batchfile ["+ _batchCmds.getFileName() +"] @ lime# "+ _batchCmds.getLineNum() +". 'foreach' Item is of type ["+ o.getClass().getName() +"]");
                    }

                } // for inputMap.keySet()

                // since we processed the lines !!INSIDE!! the 'foreach' --> 'end' block .. via recursion.. we need to skip all those lines here.
                boolean bEverythingIsDoneProperly = false;
                int bkmark = _batchCmds.getLineNum();
                while ( _batchCmds.hasNextLine() ) {
                    /* final String line22 = */ _batchCmds.nextLine();
                    final boolean bForEach22 = _batchCmds.isEndLine();
                    // System.out.println( CLASSNAME +" processBatch(while-loop,recursion="+ _bInRecursion +"): SKIPPING .. .. .. .. Batch-line# "+ _batchCmds.getLineNum() +" = ["+ _batchCmds.currentLine() +"]" );
                    if ( bForEach22 ) {
                        bEverythingIsDoneProperly = true;
                        break; // we're done completely processing the 'foreach' --> 'end' block
                    }
                }
                if (  !  bEverythingIsDoneProperly ) // sanity check.  These exceptions will get thrown if logic in 100 lines above isn't water-tight
                    throw new BatchFileException("ERROR In Batchfile ["+ _batchCmds.getFileName() +"] !!STARTING!!@line# "+ bkmark +".. do NOT see a MATCHING 'end' keyword following the  'foreach'.");


                if ( outerForLoopIndex != null ) // if there was an outer FOREACH within the batch file, restore it's index.
                    forLoopProps.setProperty( BatchFileGrammer.FOREACH_INDEX, outerForLoopIndex );

            } // if bForEach

            //------------------------------------------------
            final boolean bEndLine = _batchCmds.isEndLine();
            if ( this.verbose ) System.out.println( CLASSNAME +" processBatch(END-cmd,recursion="+ _bInRecursion +"): found matching 'end' keyword for 'foreach': [" + bEndLine + "] !!!!!!! \n\n");
            if ( bEndLine ) {
                if ( _bInRecursion ) {
                    return tempOutputMap;
                    // !!!!!!!!!!!! ATTENTION : Function exits here SUCCESSFULLY / NORMALLY. !!!!!!!!!!!!!!!!
                } else {
                    // continue with the REST of the BATCH file.
                    tempOutputMap = inputMap; // as nothing changes re: Input and Output Maps.
                    // INVALID:- throw new BatchFileException("ERROR In Batchfile ["+ _batchCmds.getFileName() +"] @ lime# "+ _batchCmds.getLineNum() +".  UNEXPECTED 'end' keyword detected.. No matching 'foreach' keyword detected prior.");
                }
            }

            //------------------------------------------------
            final String bSubBatch_AsIs = _batchCmds.isSubBatchLine();
            if ( bSubBatch_AsIs != null ) {
                final String bSubBatch = MacroYamlProcessor.evaluateMacros( bSubBatch_AsIs, this.AllProps );
                // LinkedHashMap<String,Object> temp222OutputMap = new LinkedHashMap<String,Object> ();
                this.go( bSubBatch, inputMap, tempOutputMap );
                // technically, this.go() method is NOT meant to used recursively.  Semantically, this is NOT recursion :-(
            }

            //------------------------------------------------
            final String cmd_AsIs = _batchCmds.getCommand();
            if ( cmd_AsIs != null ) {
                tempOutputMap = processYAMLCommand( _batchCmds, inputMap );
            } // if BatchCmd

            inputMap = tempOutputMap; // because we might be doing ANOTHER iteraton of the While() loop.

        } // while loop

        if ( this.verbose ) System.out.println( CLASSNAME +" processBatch(--@END-- recursion="+ _bInRecursion +"):  tempOutputMap =" + tempOutputMap +"\n\n");
        // reached end of file.
        return tempOutputMap;
    }

    //-----------------------------------------------------------------------------
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //-----------------------------------------------------------------------------

    /**
     * This function is a simpler variant of processBatch(), in that it takes NO Map as input, and is unlikely (but can) return a Map back.
     * This function is best used to help the user load batchfiles based on the string _s, or load property-files based on the string _s
     * @param _batchCmds pass-on the object BatchFileGrammer - as defined within processBatch()
     * @param _s the string value to process further n this function
     * @return After this function completes processing SUCCESSFULLY.. it returns a java.utils.LinkedHashMap&lt;String, Object&gt; object.  If there is any failure, either return value is NULL or an Exception is thrown.
     * @throws org.ASUX.yaml.BatchYamlProcessor.BatchFileException if any failure trying to execute any entry in the batch file.  Batch file processing will Not proceed once a problem occurs.
     * @throws org.ASUX.yaml.MacroYamlProcessor.MacroException - thrown if any attempt to evaluate MACROs fails within evaluateMacros() functions
     * @throws FileNotFoundException if the batch file to be loaded does Not exist
     * @throws java.io.IOException any problems reading or writing Sub-batch file or a property file or a file specified by saveAs/useAsInput
     */
    public LinkedHashMap<String, Object> processBatch_StringsOnly( final BatchFileGrammer _batchCmds, final String _s )
                        throws BatchFileException, MacroYamlProcessor.MacroException, java.io.FileNotFoundException, java.io.IOException
    {

        // WARNING: THIS IS NOT NEGOTIABLE .. I do NOT (can NOT) have an Input Map as parameter !!!!!!!!!!!!!!!!!!!!!

        LinkedHashMap<String, Object> inputMap = null;
        LinkedHashMap<String, Object> tempOutputMap = null;
        final Properties forLoopProps = this.AllProps.get( BatchFileGrammer.FOREACH );
        forLoopProps.setProperty( BatchFileGrammer.FOREACH_ITER_VALUE, _s );

        while ( _batchCmds.hasNextLine() ) {

            if ( this.verbose ) System.out.println( CLASSNAME +" processBatch_StringsOnly(while-loop): "+ _batchCmds.nextLine());

            final String line = _batchCmds.nextLine();
            // start each loop, with an 'empty' placeholder Map, to collect output of current batch command
            tempOutputMap = new LinkedHashMap<String, Object>();

            if ( this.verbose ) System.out.println( CLASSNAME +" processBatch_StringsOnly(while-loop): @ Batch-line# "+" @ line# "+ _batchCmds.getLineNum() +" = ["+ _batchCmds.currentLine() +"]" );

            //------------------------------------------------
            final BatchFileGrammer.KVPair kv = _batchCmds.isPropertyLine(); // could be null, implying NOT a kvpair
            if ( kv != null) {
                final Properties props = new Properties();
                final String fn = MacroYamlProcessor.evaluateMacros( kv.value, this.AllProps );
                props.load( new FileInputStream( fn ) );
                this.AllProps.put( kv.key, props ); // This line is the action taken by this 'PropertyFile' line of the batchfile
                tempOutputMap = inputMap; // as nothing changes re: Input and Output Maps.
            }

            final String printLine = _batchCmds.isPrintLine();
            if ( printLine != null ) {
                final String outputStr = MacroYamlProcessor.evaluateMacros( printLine, this.AllProps );
                System.out.println( outputStr );
                tempOutputMap = inputMap; // as nothing changes re: Input and Output Maps.
            }

            final String saveTo_AsIs = _batchCmds.isSaveToLine();
            if ( saveTo_AsIs != null ) {
                processSaveToLine( _batchCmds, inputMap );
                tempOutputMap = inputMap; // as nothing changes re: Input and Output Maps.
            }

            final String inputFrom_AsIs = _batchCmds.isUseAsInputLine();
            if ( inputFrom_AsIs != null ) {
                tempOutputMap = processUseAsInputLine( _batchCmds );
            }

            final String bSubBatch_AsIs = _batchCmds.isSubBatchLine();
            if ( bSubBatch_AsIs != null ) {
                final String bSubBatch = MacroYamlProcessor.evaluateMacros( bSubBatch_AsIs, this.AllProps );
                this.go( bSubBatch, inputMap, tempOutputMap );
            }

            final String cmd_AsIs = _batchCmds.getCommand();
            if ( cmd_AsIs != null ) {
                tempOutputMap = processYAMLCommand( _batchCmds, inputMap );
            } // if BatchCmd

            inputMap = tempOutputMap; // because we might be doing ANOTHER iteraton of the While() loop.

        } // while loop

        // reached end of file.
        return tempOutputMap;
    }

    //-----------------------------------------------------------------------------
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //-----------------------------------------------------------------------------

    private void processSaveToLine( final BatchFileGrammer _batchCmds, final LinkedHashMap<String, Object> _inputMap )
                                    throws MacroYamlProcessor.MacroException,  java.io.IOException,
                                    com.esotericsoftware.yamlbeans.YamlException
    {
        final String saveTo_AsIs = _batchCmds.isSaveToLine();
        if ( saveTo_AsIs != null ) {
            final String saveTo = MacroYamlProcessor.evaluateMacros( saveTo_AsIs, this.AllProps );
            if ( saveTo.startsWith("@") ) {
                final String saveToFile = saveTo.substring(1);
                final com.esotericsoftware.yamlbeans.YamlWriter yamlwriter
                        = new com.esotericsoftware.yamlbeans.YamlWriter( new java.io.FileWriter( saveToFile ) );
                yamlwriter.write( _inputMap );
                yamlwriter.close();
            } else {
                savedOutputMaps.put( saveTo, _inputMap );
            }
        }
    }

    private LinkedHashMap<String, Object> processUseAsInputLine( final BatchFileGrammer _batchCmds )
                                throws MacroYamlProcessor.MacroException, java.io.FileNotFoundException, java.io.IOException,
                                com.esotericsoftware.yamlbeans.YamlException
    {
        final String inputFrom_AsIs = _batchCmds.isUseAsInputLine();
        final String inputFrom = MacroYamlProcessor.evaluateMacros( inputFrom_AsIs, this.AllProps );
        if ( inputFrom != null ) {
            // final LinkedHashMap<String, Object> savedMap = savedOutputMaps.get( inputFrom );
            // we might be 'double-loading' inputMap - once from memory and 2nd-time from the file (heck, why not.)
            if ( inputFrom.startsWith("@") ) {
                final String inputFromFile = inputFrom.substring(1);
                final InputStream fs = new FileInputStream( inputFromFile );
                if ( inputFromFile.endsWith(".json") ) {
                    //     // https://github.com/google/gson/blob/master/gson/src/main/java/com/google/gson/Gson.java
                    // final LinkedHashMap<String, Object> retMap2 = ..
                    // tempOutputMap = new com.google.gson.Gson().fromJson(  reader1,
                    //                        new com.google.gson.reflect.TypeToken< LinkedHashMap<String, Object> >() {}.getType()   );
                    // http://tutorials.jenkov.com/java-json/jackson-objectmapper.html#read-map-from-json-string 
                    com.fasterxml.jackson.databind.ObjectMapper objMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    com.fasterxml.jackson.databind.type.MapType type = objMapper.getTypeFactory().constructMapType( LinkedHashMap.class, String.class, Object.class );
                    LinkedHashMap<String, Object> retMap2 = null;
                    retMap2 = objMapper.readValue( fs, new com.fasterxml.jackson.core.type.TypeReference<LinkedHashMap<String,Object>>(){}  );
                    if ( this.verbose ) System.out.println( CLASSNAME +" processBatch(inputAsIs): jsonMap loaded BY OBJECTMAPPER into tempOutputMap =" + retMap2 );
                    retMap2 = new Tools(this.verbose).JSON2YAML( retMap2 );
                    fs.close();
                    return retMap2;
                }
                if ( inputFromFile.endsWith(".yaml") ) {
                    final java.io.Reader reader1 = new InputStreamReader( fs  );
                    final LinkedHashMap mapObj = new com.esotericsoftware.yamlbeans.YamlReader( reader1 ).read( LinkedHashMap.class );
                    @SuppressWarnings("unchecked")
                    final LinkedHashMap<String, Object> retMap3 = (LinkedHashMap<String, Object>) mapObj;
                    reader1.close(); // automatically includes fs.close();
                    if ( this.verbose ) System.out.println( CLASSNAME +" processBatch(inputAsIs): YAML loaded into tempOutputMap =" + retMap3 );
                    return retMap3;
                }
                return null; // compiler is complaining about missing return statement.
            } else {
                final LinkedHashMap<String, Object> savedMap = savedOutputMaps.get( inputFrom );
                return savedMap;
            }
        } else {
            return null;
        }
    }

    //-----------------------------------------------------------------------------
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //-----------------------------------------------------------------------------

    private LinkedHashMap<String, Object> processYAMLCommand( final BatchFileGrammer _batchCmds, final LinkedHashMap<String, Object> _inputMap )
                                            throws BatchFileException, MacroYamlProcessor.MacroException, java.io.IOException
    {

        final String cmd_AsIs = _batchCmds.getCommand();
        final String cmdStr2 = MacroYamlProcessor.evaluateMacros( cmd_AsIs, this.AllProps ).strip();
        if ( cmdStr2 == null )
            return null;

        // https://mvnrepository.com/artifact/com.opencsv/opencsv
        String cmdStrCompacted = _batchCmds.currentLine().replaceAll("\\s\\s*", " "); // replace multiple spaces with a single space.
        // cmdStrCompacted = cmdStrCompacted.strip(); // no need.  The _batchCmds already took care of it.
        cmdStrCompacted += " -i - -o -";
        if ( this.verbose ) System.out.println( cmdStrCompacted );

        final java.io.StringReader reader = new java.io.StringReader( cmdStrCompacted );
        final com.opencsv.CSVParser parser = new com.opencsv.CSVParserBuilder().withSeparator(' ').withIgnoreQuotations(false).build();
        final com.opencsv.CSVReader cmdLineParser = new com.opencsv.CSVReaderBuilder( reader ).withSkipLines(0).withCSVParser( parser ).build();
        final String [] cmdLineArgs = cmdLineParser.readNext();

        if ( this.verbose ) for( String s: cmdLineArgs ) System.out.println( "\t"+ s );
        try {
            CmdLineArgs cmdLineArgsObj = new CmdLineArgs( cmdLineArgs );
            final Cmd cmd = new Cmd(cmdLineArgsObj.verbose);
    
            final Object output = cmd.processCommand( cmdLineArgsObj, _inputMap );
            if ( this.verbose ) System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>");
            if ( this.verbose ) System.out.println( _inputMap.toString());
            if ( this.verbose ) System.out.println( output.getClass().getName());
            if ( this.verbose ) System.out.println( output.toString());
            if ( this.verbose ) System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<");

            if ( output instanceof LinkedList ) {
                @SuppressWarnings("unchecked")
                final LinkedList<Object> list = ( LinkedList<Object> ) output;
                // list.forEach( s -> System.out.println( s.toString() ) );
                final LinkedHashMap<String, Object> retMap = new LinkedHashMap<String, Object>();
                retMap.put( "output", list );
                retMap = new Tools(this,verbose).lintRemover(retMap);
                return retMap;

            } else if ( output instanceof ArrayList ) {
                @SuppressWarnings("unchecked")
                final ArrayList<String> arr = ( ArrayList<String> ) output;
                final LinkedHashMap<String, Object> retMap = new LinkedHashMap<String, Object>();
                retMap.put( "output", arr );
                retMap = new Tools(this,verbose).lintRemover(retMap);
                return retMap;

            } else if ( output instanceof LinkedHashMap) {
                @SuppressWarnings("unchecked")
                final LinkedHashMap<String, Object> retMap = (LinkedHashMap<String, Object>) output;
                // I do Not need to run Tools().lintRemover() as the output of org.ASUX.yaml library stays 100% conformant withe com.esotericsoftware library usage.
                return retMap;

            } else {
                throw new BatchFileException( CLASSNAME +" processBatch(org.ASUX.yaml.Cmd.processCommand()): output is Not of type LinkedHashMap.  It's ["+ output.getClass().getName() +"]");
            }

        } catch (Exception e) {
            e.printStackTrace(System.err);
            throw new BatchFileException("ERROR In Batchfile ["+ _batchCmds.getFileName() +"] @line# "+ _batchCmds.getLineNum() +".. Failed to run the command ["+ _batchCmds.currentLine() +"]");
        }
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
