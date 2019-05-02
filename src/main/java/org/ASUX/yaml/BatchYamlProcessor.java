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

    public static final String FOREACH_INDEX = "foreach.index"; // which iteration # (Int) are we in within the loop.
    public static final String FOREACH_ITER_KEY = "foreach.iteration.key"; // if 'foreach' ends up iterating over an array of strings, then you can get each string's value this way.
    public static final String FOREACH_ITER_VALUE = "foreach.iteration.value"; // if 'foreach' ends up iterating over an array of strings, then you can get each string's value this way.

    // I prefer a LinkedHashMap over a plain HashMap.. as it can help with future enhancements like Properties#1, #2, ..
    // That is being aware of Sequence in which Property-files are loaded.   Can't do that with HashMap
    private LinkedHashMap<String,Properties> AllProps = new LinkedHashMap<String,Properties>();

    private LinkedHashMap<String, LinkedHashMap<String, Object> > savedOutputMaps = new LinkedHashMap<>();

    /** <p>Whether you want deluge of debug-output onto System.out.</p><p>Set this via the constructor.</p>
     *  <p>It's read-only (final data-attribute).</p>
     */
    private final boolean verbose;

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    /** <p>The only constructor - public/private/protected</p>
     *  @param _verbose Whether you want deluge of debug-output onto System.out.
     */
    public BatchYamlProcessor(boolean _verbose) {
        this.verbose = _verbose;
        this.AllProps.put( BatchFileGrammer.FOREACHCMD, new Properties() );
    }

    private BatchYamlProcessor() { this.verbose = false;} // Do Not use this.

    //------------------------------------------------------------------------------
    private static class BatchFileException extends Exception {
        private static final long serialVersionUID = 1L;
        public BatchFileException(String _s) { super(_s); }
    }

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    /** This is the entry point for this class, with the appropriate TRY-CATCH taken care of, hiding the innumerable exception types.
     *  @param _batchFileName batchfile full path (ry to avoid relative paths)
     *  @param _inputMap This contains the java.utils.LinkedHashMap&lt;String, Object&gt; (created by com.esotericsoftware.yamlbeans library) containing the entire Tree representing the YAML file.
     *  @param _returnThisMap Send in a BLANK/EMPTY/NON-NULL java.utils.LinkedHashMap&lt;String, Object&gt; object and you'll get the final Map output representing all processing done by the batch file
     *  @return true = all processed WITHOUT ANY errors.  False = Some error somewhere!
     */
    public boolean go( final String _batchFileName, final LinkedHashMap<String, Object> _inputMap,
                    final LinkedHashMap<String, Object> _returnThisMap ) {

        if ( _batchFileName == null ) return true;  // null is treated as  batchfile with ZERO commands.
        // LinkedHashMap<String,Object> inputMap = _inputMap;
        String line = null;

        final BatchFileGrammer batchCmds = new BatchFileGrammer(false);

        try {
            if ( batchCmds.openFile( _batchFileName, true ) ) {
                if ( this.verbose ) System.out.println( CLASSNAME + ": go(): successfully opened _batchFileName [" + _batchFileName +"]" );

                final LinkedHashMap<String, Object>  retMap1 = this.processBatch( false, batchCmds, _inputMap );
                if ( this.verbose ) System.out.println( CLASSNAME +" go():  retMap1 =" + retMap1 +"\n\n");

                _returnThisMap.putAll( retMap1 );
                // System.out.println( CLASSNAME +" go():  _returnThisMap =" + _returnThisMap +"\n\n");
                return true; // all ok
            } else { // if-else openFile()
                return false;
            }

        } catch (com.esotericsoftware.yamlbeans.YamlException e) { // Warning: This must PRECEDE IOException, else compiler error.
            e.printStackTrace(System.err);
            System.err.println( CLASSNAME + ": go():\n\nERROR In Batchfile ["+ _batchFileName +"] @ lime# "+ batchCmds.getLineNum() +".   SERIOUS Internal error: unable to process YAML.  See details above.");
            // System.exit(9);
        } catch (BatchFileException bfe) {
            bfe.printStackTrace(System.err);
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
            if ( this.verbose ) System.out.println( CLASSNAME +" processBatch(while-loop,cmd_type="+ _batchCmds.getCmdType().toString() +"): INSIDE-WHILE-LOOP @ Batch-line# "+ _batchCmds.getLineNum() +" = ["+ _batchCmds.currentLine() +"]" );

            /* final String line = */ _batchCmds.nextLine();
            // start each loop, with an 'empty' placeholder Map, to collect output of current batch command
            tempOutputMap = new LinkedHashMap<String, Object>();

            switch( _batchCmds.getCmdType() ) {
                case Cmd_Properties:
                    tempOutputMap = this.onPropertyLineCmd( _batchCmds, inputMap, tempOutputMap );
                    break;
                case Cmd_Print:
                    tempOutputMap = this.onPrintCmd( _batchCmds, inputMap, tempOutputMap );
                    break;
                case Cmd_SaveTo:
                    // Might sound crazy - at first.  inpMap for this 'saveAs' command is the output of prior command.
                    // final String saveTo_AsIs = _batchCmds.getSaveTo();
                    processSaveToLine( _batchCmds, inputMap );
                    tempOutputMap = inputMap; // as nothing changes re: Input and Output Maps.
                    break;
                case Cmd_UseAsInput:
                    tempOutputMap = processUseAsInputLine( _batchCmds );
                    break;
                case Cmd_Foreach:
                    if ( this.verbose ) System.out.println( CLASSNAME +" processBatch(foreach): \t'foreach'_cmd detected'");
                    if ( this.verbose ) System.out.println( CLASSNAME +": processBatch(foreach): InputMap = "+ inputMap.toString() );
                    final LinkedHashMap<String, Object> retMap4 = processFOREACHCmdForObject( _batchCmds, inputMap  );
                    tempOutputMap.putAll( retMap4 );
                    // since we processed the lines !!INSIDE!! the 'foreach' --> 'end' block .. via recursion.. we need to skip all those lines here.
                    skipInnerForeachLoops( _batchCmds, "processForeach_StringsOnly(foreach)" );
                    break;
                case Cmd_End:
                    if ( this.verbose )
                        System.out.println( CLASSNAME +" processBatch(END-cmd): found matching 'end' keyword for 'foreach' !!!!!!! \n\n");
                    return tempOutputMap;
                    // !!!!!!!!!!!! ATTENTION : Function exits here SUCCESSFULLY / NORMALLY. !!!!!!!!!!!!!!!!
                    // break;
                case Cmd_Batch:
                    final String bSubBatch = MacroYamlProcessor.evaluateMacros( _batchCmds.getSubBatchFile(), this.AllProps );
                    this.go( bSubBatch, inputMap, tempOutputMap );
                    // technically, this.go() method is NOT meant to used recursively.  Semantically, this is NOT recursion :-(
                    break;
                case Cmd_Any:
                    //This MUST ALWAYS be the 2nd last 'case' in this SWITCH statement
                    tempOutputMap = this.onAnyCmd( _batchCmds, inputMap, tempOutputMap );
                    break;
            } // switchg

            // this line below must be the very last line in the loop
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
     *  As com.esotericsoftware.yamlBeans has some magic where Keys are NOT strings! ..
     *  + the fact that I'd like this entire org.ASUX,yaml library to work with Maps created elsewhere/other libraries..
     *  .. in order for me to SUCCESSFULLY read/manipulate/iterator through ANY _map passed in, I need to go thru a few hoops and multiple levels of functions.
     * @param _batchCmds
     * @param _o
     * @return
     * @throws BatchYamlProcessor.BatchFileException
     * @throws MacroYamlProcessor.MacroException
     * @throws com.esotericsoftware.yamlbeans.YamlException
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     * @throws Exception
     */
    private LinkedHashMap<String, Object>  processFOREACHCmdForObject( final BatchFileGrammer _batchCmds, Object _o )
                throws BatchYamlProcessor.BatchFileException, MacroYamlProcessor.MacroException, com.esotericsoftware.yamlbeans.YamlException, java.io.FileNotFoundException, java.io.IOException, Exception
    {
        if ( _o == null ) return new LinkedHashMap<String, Object>();
        final Tools tools = new Tools( this.verbose );
        final Tools.OutputObjectTypes typ = tools.getOutputObjectType( _o );
        switch(typ) {
            case Type_ArrayList:
                final ArrayList<String> arr = tools.getArrayList( _o );
                return processFORECHForArray( _batchCmds, arr );
                // break;
            case Type_LinkedList:
                final LinkedList<String> lst = tools.getLinkedList( _o );
                return processFORECHForArray( _batchCmds, lst );
                // break;
            case Type_KVPairs:  // PLURAL;  Note the 's' character @ end.  This is Not KVPair (singular)
                ArrayList< Tools.Tuple< String,String > > kvpairs = tools.getKVPairs( _o );
                // final LinkedHashMap<String, Object> outpMap1 = this.process Batch( true, BatchFileGrammer.deepClone(_batchCmds), map );
                // Note: KVPairs means a LinkedHashMap - with NO elements at Depth 2 or more!  It really must be a VERY MOST SHALLOW LinkedHashMap containing just "String":"String" elements in it.
                // Then, yes it kind of makes sense to iterate over KVPairs.
                return processFORECHForArray( _batchCmds, kvpairs );
                // break;

            case Type_KVPair:  // singular;  No 's' character @ end.  This is Not KVPairs
            case Type_String:
                throw new BatchFileException( CLASSNAME +": processFOREACHCmdForObject(): ERROR while processing Batchfile ["+ _batchCmds.getFileName() +"] @ lime# "+ _batchCmds.getLineNum() +" .. executing a FOREACH-command after getting a SINGLE STRING scalar value as output does Not make AMY sense!" );
                // return this.processForeach_StringsOnly( BatchFileGrammer.deepClone(_batchCmds), tools.getTheActualObject( _o ).toString() );
                // ATTENTION !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                // Maybe .. .. this special function processForeach_StringsOnly() is rarely about producing new YAML
                // So.. SHOULD WE?? leave tempOutputMap remain unchanged (as it was before the foreach command started)
                // break;
            case Type_LinkedHashMap:
                    throw new BatchFileException( CLASSNAME +": processFOREACHCmdForObject(): ERROR while processing Batchfile ["+ _batchCmds.getFileName() +"] @ lime# "+ _batchCmds.getLineNum() +" .. executing a FOREACH-command over a LinkedHashMap's contents, which !!ONLY!! containing simple KV-pairs (sub-Maps) does Not make AMY sense!" );
                // @SuppressWarnings("unchecked")
                // final LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>) _o;
                // final LinkedHashMap<String, Object> outpMap1 = this.process Batch( true, BatchFileGrammer.deepClone(_batchCmds), map );
                // return outpMap1;
                // break;
            case Type_Unknown:
            default:
                throw new BatchFileException( CLASSNAME +": processFOREACHCmdForObject(): ERROR while processing Batchfile ["+ _batchCmds.getFileName() +"] @ lime# "+ _batchCmds.getLineNum() +" .. unknown object of type ["+ _o.getClass().getName() +"]");
        } // switch
    }

    //-------------------------------------------------------------------------
    private LinkedHashMap<String, Object>  processFORECHForArray( final BatchFileGrammer _batchCmds, final java.util.AbstractCollection coll )
                throws BatchYamlProcessor.BatchFileException, MacroYamlProcessor.MacroException, com.esotericsoftware.yamlbeans.YamlException, java.io.FileNotFoundException, java.io.IOException, Exception
    {
        LinkedHashMap<String, Object> tempOutputMap = new LinkedHashMap<String, Object>();

        final Tools tools = new Tools( this.verbose );
        final Properties forLoopProps = this.AllProps.get( BatchFileGrammer.FOREACHCMD );
        final String prevForLoopIndex = forLoopProps.getProperty( FOREACH_INDEX );

        Iterator itr = coll.iterator();
        for ( int ix=0;  itr.hasNext(); ix ++ ) {
            final Object o = itr.next();
            final Tools.OutputObjectTypes typ = tools.getOutputObjectType( o );

            forLoopProps.setProperty( FOREACH_INDEX, ""+ix ); // to be used by all commands INSIDE the 'foreach' block-inside-batchfile
            if ( this.verbose ) System.out.println( CLASSNAME +" processFORECHForArray(): @@@@@@@@@@@@@@@@@ foreach/Array-index #"+ ix +" : Object's type ="+ o.getClass().getName() +" and it's toString()=["+ o.toString() +"]" );
            if ( this.verbose ) System.out.println( CLASSNAME +" processFORECHForArray(): SWITCH's Type="+ typ.toString() );

            switch(typ) {
                case Type_String:
                    final LinkedHashMap<String, Object> retMap6 = this.processForeach_StringsOnly( BatchFileGrammer.deepClone(_batchCmds), tools.getTheActualObject( o ).toString() );
                    tempOutputMap.putAll( retMap6 );
                    // ATTENTION !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                    // Maybe .. .. this special function processForeach_StringsOnly() can produce new YAML in LinkedHashMap form
                    // Example: The value of string can be used to load a specific file - by using that value as the 'useAsInput' command's parameter
                    break;
                case Type_KVPair:  // singular;  No 's' character @ end.  This is Not KVPairs
                    @SuppressWarnings("unchecked")
                    final Tools.Tuple< String,String > kvpair = ( Tools.Tuple< String,String > ) o;
                    forLoopProps.setProperty( FOREACH_ITER_KEY, kvpair.key ); // to be used by all commands INSIDE the 'foreach' block-inside-batchfile
                    final LinkedHashMap<String, Object> retMap7 = this.processForeach_StringsOnly( BatchFileGrammer.deepClone(_batchCmds), kvpair.val );
                    tempOutputMap.putAll( retMap7 );
                    break;
                case Type_LinkedHashMap:
                    @SuppressWarnings("unchecked")
                    final LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>) o;
                    final LinkedHashMap<String, Object> retMap8 = this.processBatch( true, BatchFileGrammer.deepClone(_batchCmds), map );
                    tempOutputMap.putAll( retMap8 );
                    break;
                case Type_KVPairs:  // PLURAL;  Note the 's' character @ end.  This is Not KVPair (singular)
                case Type_ArrayList: // array of arrays?  What am I going to do?  What does such a data structure mean? In what real-world use-case scenario?
                case Type_LinkedList:
                case Type_Unknown:
                default:
                    throw new BatchFileException( CLASSNAME +": processFORECHForArray(): ERROR: Un-implemented logic.  Not sure what this means: Array of Arrays! In file ["+ _batchCmds.getFileName() +"] @ lime# "+ _batchCmds.getLineNum() +" .. trying to iterate over object ["+ o.toString() +"]");
            } // end switch
    
        } // for arr.size()

        if ( prevForLoopIndex != null ) // if there was an outer FOREACH within the batch file, restore it's index.
            forLoopProps.setProperty( FOREACH_INDEX, prevForLoopIndex );

        return tempOutputMap;
    }

    //-----------------------------------------------------------------------------
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //-----------------------------------------------------------------------------

    /**
     * This function is an almost identical variant of processBatch(), in that it takes NO Map as input, and is unlikely (but can) return a Map back.
     * This function is best used to help the user load batchfiles based on the string _s, or load property-files based on the string _s
     * @param _batchCmds pass-on the object BatchFileGrammer - as defined within processBatch()
     * @param _s the string value to process further n this function
     * @return After this function completes processing SUCCESSFULLY.. it returns a java.utils.LinkedHashMap&lt;String, Object&gt; object.  If there is any failure, either return value is NULL or an Exception is thrown.
     * @throws org.ASUX.yaml.BatchYamlProcessor.BatchFileException if any failure trying to execute any entry in the batch file.  Batch file processing will Not proceed once a problem occurs.
     * @throws org.ASUX.yaml.MacroYamlProcessor.MacroException - thrown if any attempt to evaluate MACROs fails within evaluateMacros() functions
     * @throws FileNotFoundException if the batch file to be loaded does Not exist
     * @throws java.io.IOException any problems reading or writing Sub-batch file or a property file or a file specified by saveAs/useAsInput
     * @throws Exception This happens when the output of a previous Batch-command is NOT compatible !!enough!! (checked via Tools.java methods)
     */
    public LinkedHashMap<String, Object> processForeach_StringsOnly( final BatchFileGrammer _batchCmds, final String _s )
                        throws BatchFileException, MacroYamlProcessor.MacroException, java.io.FileNotFoundException, java.io.IOException, Exception
    {
        // WARNING: THIS IS NOT NEGOTIABLE .. I do NOT (can NOT) have an Input-Map (non-Scalar) as parameter !!!!!!!!!!!!!!!!!!!!!
        // so, we start off this function with an EMPTY 'inputMap'
        LinkedHashMap<String, Object> inputMap = new LinkedHashMap<String, Object>();
        LinkedHashMap<String, Object> tempOutputMap = null; // it's immediately re-initialized within WHILE-Loop below.

        final Properties forLoopProps = this.AllProps.get( BatchFileGrammer.FOREACHCMD );
        forLoopProps.setProperty( FOREACH_ITER_VALUE, _s );

final String _bInRecursion = "N/A";
        if ( this.verbose ) System.out.println( CLASSNAME +" processForeach_StringsOnly(@BeginningOfFunction): recursion="+ _bInRecursion +" & "+ _batchCmds.hasNextLine() +" @ line# "+ _batchCmds.getLineNum() );
        while ( _batchCmds.hasNextLine() ) {
            if ( this.verbose ) System.out.println( CLASSNAME +" processForeach_StringsOnly(while-loop,recursion="+ _bInRecursion +"): INSIDE-WHILE-LOOP @ Batch-line# "+ _batchCmds.getLineNum() +" = ["+ _batchCmds.currentLine() +"]" );

            /* final String line = */ _batchCmds.nextLine();
            // start each loop, with an 'empty' placeholder Map, to collect output of current batch command
            tempOutputMap = new LinkedHashMap<String, Object>();

            if ( this.verbose ) System.out.println( CLASSNAME +" processForeach_StringsOnly(while-loop,cmd_type="+ _batchCmds.getCmdType().toString() +"): @ Batch-line# "+" @ line# "+ _batchCmds.getLineNum() +" = ["+ _batchCmds.currentLine() +"]" );

            switch( _batchCmds.getCmdType() ) {
                case Cmd_Properties:
                    tempOutputMap = this.onPropertyLineCmd( _batchCmds, inputMap, tempOutputMap );
                    break;
                case Cmd_Print:
                    tempOutputMap = this.onPrintCmd( _batchCmds, inputMap, tempOutputMap );
                    break;
                case Cmd_SaveTo:
                    // Might sound crazy - at first.  inpMap for this 'saveAs' command is the output of prior command.
                    processSaveToLine( _batchCmds, inputMap );
                    tempOutputMap = inputMap; // as nothing changes re: Input and Output Maps.
                    break;
                case Cmd_UseAsInput:
                    tempOutputMap = processUseAsInputLine( _batchCmds );
                    break;
                case Cmd_Foreach:
                    if ( this.verbose ) System.out.println( CLASSNAME +": processForeach_StringsOnly(foreach): \t'foreach'_cmd detected'");
                    if ( this.verbose ) System.out.println( CLASSNAME +": processForeach_StringsOnly(foreach): InputMap = "+ inputMap.toString() );
                    final LinkedHashMap<String, Object> retMap4 = processFOREACHCmdForObject( _batchCmds, inputMap );
                    tempOutputMap.putAll( retMap4 );
                    // since we processed the lines !!INSIDE!! the 'foreach' --> 'end' block .. via recursion.. we need to skip all those lines here.
                    skipInnerForeachLoops( _batchCmds, "processForeach_StringsOnly(foreach)" );
                    break;
                case Cmd_End:
// ?? is this is the only place that this SWITCH statement here, differs from the almost-identical copy in processForEach() function?
                    if ( this.verbose )
                        System.out.println( CLASSNAME +" processForeach_StringsOnly(END-cmd): Found matching 'end' keyword for 'foreach' !!!!!!! \n\n");
                    return inputMap; // Not tempOutputMap, which has been reset to anb empty Map, at the beginning of the loop
                    // !!!!!!!!!!!! ATTENTION : Function exits here SUCCESSFULLY / NORMALLY. !!!!!!!!!!!!!!!!
                    // break;
                case Cmd_Batch:
                    final String bSubBatch = MacroYamlProcessor.evaluateMacros( _batchCmds.getSubBatchFile(), this.AllProps );
                    this.go( bSubBatch, inputMap, tempOutputMap );
                    // technically, this.go() method is NOT meant to used recursively.  Semantically, this is NOT recursion :-(
                    break;
                case Cmd_Any:
                    //This MUST ALWAYS be the 2nd last 'case' in this SWITCH statement
                    tempOutputMap = this.onAnyCmd( _batchCmds, inputMap, tempOutputMap );
                    break;
            } // switchg

            // this line below must be the very last line in the loop
            inputMap = tempOutputMap; // because we might be doing ANOTHER iteraton of the While() loop.

        } // while loop

        // reached end of file.
        return tempOutputMap;
    }

    //-----------------------------------------------------------------------------
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //-----------------------------------------------------------------------------


    /**
     * When this function returns, the "pointer" within _batchCmds (.currentLine & .getLineNum()) ..
     *   should be pointing to the command AFTER the 'end' command.
     * This function basically keeps track of any inner foreachs .. and that's how it knows when the matching 'end' was detected.
     * @param _batchCmds pass-by-reference, so we can alter it's state and move it to the line AFTER matching 'end' commamd
     * @param _sInvoker
     * @throws BatchFileException
     */
    private void skipInnerForeachLoops( final BatchFileGrammer _batchCmds, final String _sInvoker )
                                                throws BatchFileException
    {
// System.out.println();
        final int bookmark = _batchCmds.getLineNum();
        boolean bFoundMatchingENDCmd = false;
        int recursionLevel = 0;
        while ( _batchCmds.hasNextLine() ) {
            /* final String line22 = */ _batchCmds.nextLine(); // we do Not care what the line is about.
            final boolean bForEach22 = _batchCmds.isForEachLine();
            if ( bForEach22 ) recursionLevel ++;
            final boolean bEnd22 = _batchCmds.isEndLine();
// System.out.println( CLASSNAME +" skipInnerForeachLoops("+_sInvoker+") @level="+recursionLevel+" : SKIPPING .. .. .. .. Batch-line# "+ _batchCmds.getLineNum() +" = ["+ _batchCmds.currentLine() +"]" );
            if ( bEnd22 ) {
                recursionLevel --;
                if ( recursionLevel < 0 ) {
                    bFoundMatchingENDCmd = true;
                    break; // we're done completely SKIPPING all the lines between 'foreach' --> 'end'
                } else
                    continue; // while _batchCmds.hasNextLine()
            } // if bEnd22
        }
        if (  !  bFoundMatchingENDCmd ) // sanity check.  These exceptions will get thrown if logic in 100 lines above isn't water-tight
            throw new BatchFileException( CLASSNAME +": skipInnerForeachLoops("+_sInvoker+"): ERROR In Batchfile ["+ _batchCmds.getFileName() +"] !!STARTING!! from line# "+ bookmark +".. do NOT see a MATCHING 'end' keyword following the  'foreach'.");
    }

    //======================================================================
    private void processSaveToLine( final BatchFileGrammer _batchCmds, final LinkedHashMap<String, Object> _inputMap )
                                    throws MacroYamlProcessor.MacroException,  java.io.IOException,
                                    com.esotericsoftware.yamlbeans.YamlException
    {
        final String saveTo_AsIs = _batchCmds.getSaveTo();
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

    //======================================================================
    private LinkedHashMap<String, Object> processUseAsInputLine( final BatchFileGrammer _batchCmds )
                                throws MacroYamlProcessor.MacroException, java.io.FileNotFoundException, java.io.IOException,
                                com.esotericsoftware.yamlbeans.YamlException
    {
        final String inputFrom_AsIs = _batchCmds.getUseAsInput();
        final String inputFrom = MacroYamlProcessor.evaluateMacros( inputFrom_AsIs, this.AllProps );
        if ( inputFrom != null ) {
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
                    if ( this.verbose ) System.out.println( CLASSNAME +" processUseAsInputLine(): jsonMap loaded BY OBJECTMAPPER into tempOutputMap =" + retMap2 );
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
                    if ( this.verbose ) System.out.println( CLASSNAME +" processUseAsInputLine(): YAML loaded into tempOutputMap =" + retMap3 );
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

    private LinkedHashMap<String, Object> onPropertyLineCmd(
        final BatchFileGrammer _batchCmds, final LinkedHashMap<String, Object> inputMap, final LinkedHashMap<String, Object> tempOutputMap )
        throws MacroYamlProcessor.MacroException, java.io.FileNotFoundException, java.io.IOException
    {
        final Tools.Tuple<String,String> kv = _batchCmds.getPropertyKV(); // could be null, implying NOT a kvpair
        if ( kv != null) {
            final Properties props = new Properties();
            final String fn = MacroYamlProcessor.evaluateMacros( kv.val, this.AllProps );
            props.load( new FileInputStream( fn ) );
            this.AllProps.put( kv.key, props ); // This line is the action taken by this 'PropertyFile' line of the batchfile
        }
        return inputMap; // as nothing changes re: Input and Output Maps.
    }


    private LinkedHashMap<String, Object> onPrintCmd(
        final BatchFileGrammer _batchCmds, final LinkedHashMap<String, Object> inputMap, final LinkedHashMap<String, Object> tempOutputMap )
        throws MacroYamlProcessor.MacroException
    {
        final String printLine = _batchCmds.getPrintExpr();
        if ( this.verbose ) System.out.print( ">>>>>>>>>>>>> print line is ["+printLine +"]" );
        if ( printLine != null ) {
            String outputStr = MacroYamlProcessor.evaluateMacros( printLine, this.AllProps );
            if ( outputStr.endsWith("\\n") ) {
                outputStr = outputStr.substring(0, outputStr.length()-2); // chop out the 2 characters '\n'
                System.out.println( outputStr +" " );
            } else {
                System.out.print( outputStr +" " );
            }
            // ATTENTION!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            // DO NOT COMMENT THIS ABOVE OUT.  Do NOT ADD AN IF CONDITION to this.  This is by design.
            System.out.flush();
        }
        return inputMap; // as nothing changes re: Input and Output Maps.
    }

    private LinkedHashMap<String, Object> onAnyCmd(
        final BatchFileGrammer _batchCmds, final LinkedHashMap<String, Object> inputMap, final LinkedHashMap<String, Object> tempOutputMap )
        throws BatchFileException, MacroYamlProcessor.MacroException, java.io.FileNotFoundException, java.io.IOException, Exception
    {
        final String cmd_AsIs = _batchCmds.getCommand();
        if ( cmd_AsIs != null ) {
            if ( cmd_AsIs.equals("yaml") ) {
                return processAnyCommand( new YAMLCmdType(), _batchCmds, inputMap );
            } else if ( cmd_AsIs.equals("aws.sdk") ) {
                return processAnyCommand( new AWSCmdType(), _batchCmds, inputMap );
            } else {
                throw new BatchFileException( CLASSNAME +" onANYCmd(): Unknown Batchfile command ["+ cmd_AsIs +"]  in file: ["+ _batchCmds.getFileName() +"] @line# "+ _batchCmds.getLineNum() +" = ["+ _batchCmds.currentLine() +"]");
            }
        } // if BatchCmd
        return inputMap; // as nothing changes re: Input and Output Maps.
    }

    //-----------------------------------------------------------------------------
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //-----------------------------------------------------------------------------

    private static abstract class IWhichCMDType {
        protected String [] cmdLineArgs = null;
        public abstract Object go( final LinkedHashMap<String, Object> _inputMap ) throws java.io.FileNotFoundException, java.io.IOException, java.lang.Exception;

        public String[] convStr2Array( final String _cmdStr, final LinkedHashMap<String,Properties> _allProps ) throws MacroYamlProcessor.MacroException, java.io.IOException {
            String cmdStrCompacted = _cmdStr.replaceAll("\\s\\s*", " "); // replace multiple spaces with a single space.
            // cmdStrCompacted = cmdStrCompacted.trim(); // no need.  The _batchCmds already took care of it.
            final String cmdStrNoMacros = MacroYamlProcessor.evaluateMacros( cmdStrCompacted, _allProps ).trim();
            // if ( this.verbose ) System.out.println( cmdStrCompacted );

            // https://mvnrepository.com/artifact/com.opencsv/opencsv
            final java.io.StringReader reader = new java.io.StringReader( cmdStrNoMacros );
            final com.opencsv.CSVParser parser = new com.opencsv.CSVParserBuilder().withSeparator(' ').withIgnoreQuotations(false).build();
            final com.opencsv.CSVReader cmdLineParser = new com.opencsv.CSVReaderBuilder( reader ).withSkipLines(0).withCSVParser( parser ).build();
            this.cmdLineArgs = cmdLineParser.readNext(); // pretend we're reading the 1st line ONLY of a CSV file.
            return cmdLineArgs;
        }
    }
    //----------------------
    private static class YAMLCmdType extends IWhichCMDType {
         // technically override -.. but, actually extend!
        public String[] convStr2Array( final String _cmdStr, final LinkedHashMap<String,Properties> _allProps ) throws MacroYamlProcessor.MacroException, java.io.IOException {
            final String cmdStrWIO = _cmdStr + " -i - -o -";
            return super.convStr2Array(cmdStrWIO, _allProps );
        }
        public Object go( final LinkedHashMap<String, Object> _inputMap ) throws java.io.FileNotFoundException, java.io.IOException, java.lang.Exception {
            // final String [] cmdLineArgs = this.convStr2Array( cmdStrWIO );
            final CmdLineArgs cmdLineArgsObj = new CmdLineArgs( this.cmdLineArgs );
            final Cmd cmd = new Cmd( cmdLineArgsObj.verbose );
            final Object output = cmd.processCommand( cmdLineArgsObj, _inputMap );
            return output;
        }
    }
    //----------------------
    private static class AWSCmdType extends IWhichCMDType {
        private static AWSSDK awssdk = null;
        //----------------------
            public Object go( final LinkedHashMap<String, Object> _inputMap ) throws java.io.FileNotFoundException, java.io.IOException, java.lang.Exception {
            // final String [] cmdLineArgs = this.convStr2Array( _cmdStr );
            // for( String s: this.cmdLineArgs) System.out.print( "\t"+s );   System.out.println("\n\n");

            // aws.sdk ----list-regions us-east-2
            // aws.sdk ----list-AZs     us-east-2
            if ( this.cmdLineArgs.length < 2 )
                throw new BatchFileException( CLASSNAME +": processAnyCommand(AWSCmdType): AWS.SDK command is NOT of sufficient # of parameters ["+ this.cmdLineArgs +"]");

            if ( AWSCmdType.awssdk == null ) AWSCmdType.awssdk = AWSSDK.AWSCmdline();

            final String awscmdStr = this.cmdLineArgs[1];
            final String[] awscmdlineArgs = java.util.Arrays.copyOfRange( this.cmdLineArgs, 2, this.cmdLineArgs.length ); // last parameter: the final index of the range to be copied, exclusive
            if ( awscmdStr.equals("--list-regions")) {
                final ArrayList<String> regionsList = awssdk.getRegions( );
                return regionsList;
            }
            if ( awscmdStr.equals("--list-AZs")) {
                if ( this.cmdLineArgs.length < 3 )
                    throw new BatchFileException( CLASSNAME +": processAnyCommand(AWSCmdType): AWS.SDK --list-AZs command: INSUFFICIENT # of parameters ["+ this.cmdLineArgs +"]");
                final ArrayList<String> AZList = awssdk.getAZs( awscmdlineArgs[0] ); // ATTENTION: Pay attention to index# of awscmdlineArgs
                return AZList;
            }
            return null;
        }
    }

    //-----------------------------------------------------------------------------
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //-----------------------------------------------------------------------------

    private LinkedHashMap<String, Object> processAnyCommand(
        final IWhichCMDType _cmdType, final BatchFileGrammer _batchCmds, final LinkedHashMap<String, Object> _inputMap )
                                            throws BatchFileException, MacroYamlProcessor.MacroException, java.io.IOException
    {
        final String cmd_AsIs = _batchCmds.getCommand();
        final String cmdStr2 = MacroYamlProcessor.evaluateMacros( cmd_AsIs, this.AllProps ).trim();
        if ( cmdStr2 == null )
            return null;

        final String [] cmdLineArgs = _cmdType.convStr2Array( _batchCmds.currentLine(), this.AllProps );

        if ( this.verbose ) for( String s: cmdLineArgs ) System.out.println( "\t"+ s );
        try {
            final Object output = _cmdType.go( _inputMap );
            return new Tools(this.verbose).wrapAnObject_intoLinkedHashMap( output );
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
