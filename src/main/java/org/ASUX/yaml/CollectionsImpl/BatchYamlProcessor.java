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

import org.ASUX.common.Tuple;
import org.ASUX.common.Output;
import org.ASUX.common.Debug;
import org.ASUX.common.Macros;

import org.ASUX.yaml.MemoryAndContext;
import org.ASUX.yaml.BatchFileGrammer;
import org.ASUX.yaml.CmdLineArgs;
import org.ASUX.yaml.CmdLineArgsBasic;
import org.ASUX.yaml.CmdLineArgsBatchCmd;

import java.util.regex.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.LinkedHashMap;

import java.util.Properties;
import java.util.Set;

import static org.junit.Assert.*;

/**
 *  <p>This concrete class is part of a set of 4 concrete sub-classes (representing YAML-COMMANDS to read/query, list, delete and replace ).</p>
 *  <p>This class contains implementation batch-processing of multiple YAML commands (combinations of read, list, delete, replace, macro commands)</p>
 *  <p>This org.ASUX.yaml GitHub.com project and the <a href="https://github.com/org-asux/org.ASUX.cmdline">org.ASUX.cmdline</a> GitHub.com projects.</p>
 *  <p>See full details of how to use this, in {@link CmdInvoker} as well as the <a href="https://github.com/org-asux/org-ASUX.github.io/wiki">org.ASUX.cmdline</a> GitHub.com projects.</p>
 * @see CmdInvoker
 */
public class BatchYamlProcessor extends org.ASUX.yaml.BatchCmdProcessor< LinkedHashMap<String,Object> > {

    public static final String CLASSNAME = BatchYamlProcessor.class.getName();

    // public static final String FOREACH_INDEX = "foreach.index"; // which iteration # (Int) are we in within the loop.
    // public static final String FOREACH_ITER_KEY = "foreach.iteration.key"; // if 'foreach' ends up iterating over an array of strings, then you can get each string's value this way.
    // public static final String FOREACH_ITER_VALUE = "foreach.iteration.value"; // if 'foreach' ends up iterating over an array of strings, then you can get each string's value this way.

    // I prefer a LinkedHashMap over a plain HashMap.. as it can help with future enhancements like Properties#1, #2, ..
    // That is being aware of Sequence in which Property-files are loaded.   Can't do that with HashMap
    // private LinkedHashMap<String,Properties> allProps = new LinkedHashMap<String,Properties>();


    // /** <p>Whether you want deluge of debug-output onto System.out.</p><p>Set this via the constructor.</p>
    //  *  <p>It's read-only (final data-attribute).</p>
    //  */
    // private boolean verbose;

    // /** <p>Whether you want a final SHORT SUMMARY onto System.out.</p><p>a summary of how many matches happened, or how many entries were affected or even a short listing of those affected entries.</p>
    //  */
    // public final boolean showStats;

    // private int runcount = 0;
    // private java.util.Date startTime = null;
    // private java.util.Date endTime = null;

    // private MemoryAndContext memoryAndContext = null;

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================
    /** <p>The only constructor - public/private/protected</p>
     *  @param _verbose Whether you want deluge of debug-output onto System.out.
     *  @param _showStats Whether you want a final summary onto console / System.out
     *  @param _offline true if we pretent no internet-access is available, and we use 'cached' AWS-SDK responses - if available.
     */
    public BatchYamlProcessor( final boolean _verbose, final boolean _showStats, final boolean _offline ) {
        super( _verbose, _showStats, _offline, null );
    }

    // private BatchYamlProcessor() { this.verbose = false;    this.showStats = true;  } // Do Not use this.

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /**
     * Because super.class is a Generic&lt;T&gt;, compiler (for good reason) will Not allow me to type 'o instanceof T'.  Hence I am delegating this simple condition-check to the sub-classes.
     * @return true if 'o instanceof T' else false.
     */
    @Override
    protected boolean instanceof_YAMLImplClass( Object o ) {
        return o instanceof LinkedHashMap;
    }

    /**
     *  For SnakeYAML Library based subclass of this, simply return 'NodeTools.Node2YAMLString(tempOutput)'.. or .. for EsotericSoftware.com-based LinkedHashMap-based library, simply return 'tools.Map2YAMLString(tempOutputMap)'
     *  @param _o either the SnakeYaml library's org.yaml.snakeyaml.nodes.Node ( as generated by SnakeYAML library).. or.. EsotericSoftware Library's preference for LinkedHashMap&lt;String,Object&gt;, -- in either case, this object contains the entire Tree representing the YAML file.
     *  @return a string Not-Null
     */
    @Override
    protected String toStringDebug( Object _o ) throws Exception
    {   assertTrue ( _o == null || _o instanceof LinkedHashMap );
        @SuppressWarnings("unchecked")
        final LinkedHashMap<String,Object> map = (LinkedHashMap<String,Object>) _o;
        return  new Tools(this.verbose).Map2YAMLString( map );
    }

    /**
     *  For SnakeYAML Library based subclass of this, simply return 'NodeTools.getEmptyYAML( this.dumperoptions )' .. or .. for EsotericSoftware.com-based LinkedHashMap-based library, simply return 'new LinkedHashMap&lt;&gt;()'
     *  @return either the SnakeYaml library's org.yaml.snakeyaml.nodes.Node ( as generated by SnakeYAML library).. or.. EsotericSoftware Library's preference for LinkedHashMap&lt;String,Object&gt;, -- in either case, this object contains the entire Tree representing the YAML file.
     */
    @Override
    protected LinkedHashMap<String,Object> getEmptyYAML() {
        return new LinkedHashMap<String,Object>();
    }

    /**
     *  For SnakeYAML Library based subclass of this, simply return 'NodeTools.getNewSingleMap( newRootElem, "", this.dumperoptions )' .. or .. for EsotericSoftware.com-based LinkedHashMap-based library, simply return 'new LinkedHashMap&lt;&gt;.put( newRootElem, "" )'
     *  @param _newRootElemStr the string representing 'lhs' in "lhs: rhs" single YAML entry
     *  @param _valElemStr the string representing 'rhs' in "lhs: rhs" single YAML entry
     *  @return either the SnakeYaml library's org.yaml.snakeyaml.nodes.Node ( as generated by SnakeYAML library).. or.. EsotericSoftware Library's preference for LinkedHashMap&lt;String,Object&gt;, -- in either case, this object contains the entire Tree representing the YAML file.
     */
    @Override
    protected LinkedHashMap<String,Object> getNewSingleYAMLEntry( final String _newRootElemStr, final String _valElemStr ) {
        final LinkedHashMap<String,Object> map = new LinkedHashMap<String,Object>();
        map.put( _newRootElemStr, _valElemStr );
        return map;
    }

    /**
     * For SnakeYAML-based subclass of this, simply return 'NodeTools.deepClone( _node )' .. or .. for EsotericSoftware.com-based LinkedHashMap-based library, return ''
     * @param _map A Not-Null instance of either the SnakeYaml library's org.yaml.snakeyaml.nodes.Node ( as generated by SnakeYAML library).. or.. EsotericSoftware Library's preference for LinkedHashMap&lt;String,Object&gt;, -- in either case, this object contains the entire Tree representing the YAML file.
     * @return full deep-clone (Not-Null)
     */
    @Override
    protected LinkedHashMap<String,Object> deepClone( LinkedHashMap<String,Object> _map ) throws Exception {
        return org.ASUX.common.Utils.deepClone( _map );
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /**
     *  This function is meant for recursion.  Recursion happens when 'foreach' or 'batch' commands are detected in a batch file.
     *  After this function completes processing SUCCESSFULLY.. it returns a java.utils.LinkedHashMap&lt;String, Object&gt; object.
     *  If there is any failure whatsoever then the batch-file processing stops immediately.
     *  If there is any failure whatsoever either return value is NULL or an Exception is thrown.
     *  @param _bInRecursion true or false, whether this invocation is a recursive call or not.  If true, when the 'end' or &lt;EOF&gt; is detected.. this function returns
     *  @param _batchCmds an object of type BatchFileGrammer created by reading a batch-file, or .. .. the contents between 'foreach' and 'end' commands
     *  @param _input input YAML as java.utils.LinkedHashMap&lt;String, Object&gt; object.  In case of recursion, this object can be java.lang.String
     *  @return After this function completes processing SUCCESSFULLY.. it returns a java.utils.LinkedHashMap&lt;String, Object&gt; object.  If there is any failure, either return value is NULL or an Exception is thrown.
     *  @throws BatchFileException if any failure trying to execute any entry in the batch file.  Batch file processing will Not proceed once a problem occurs.
     *  @throws java.io.FileNotFoundException if the batch file to be loaded does Not exist
     *  @throws Exception when any of the commands within a batch-file are processed
     */
    @Override
    protected LinkedHashMap<String, Object> processBatch( final boolean _bInRecursion, final BatchFileGrammer _batchCmds, final java.util.LinkedHashMap<String,Object> _input )
                        throws BatchFileException, java.io.FileNotFoundException, Exception
    {
        assertTrue( _batchCmds != null );
        assertTrue( _input != null );
        final String HDR = CLASSNAME +": processBatch(recursion="+ _bInRecursion +","+ _batchCmds.getCmdType().toString() +"): ";
        LinkedHashMap<String,Object> inputMap = null;
        // LinkedHashMap<String, Object> tempOutputMap = null; // it's immediately re-initialized within WHILE-Loop below.
        final Tools tools = new Tools( this.verbose );

        if ( this.verbose ) System.out.println( HDR +" @ BEGINNING recursion="+ _bInRecursion +" & _input="+ ((_input!=null)?_input.toString():"null") +"]" );
        final Properties forLoopProps = this.allProps.get( FOREACH_PROPERTIES );
        final Properties globalVariables = this.allProps.get( BatchFileGrammer.GLOBALVARIABLES );

        if ( _input == null ) { // if the user specified /dev/null as --inputfile via command line, then _input===null
            inputMap = new LinkedHashMap<String, Object>();
            forLoopProps.setProperty( FOREACH_ITER_VALUE, "undefined" );
        } else if ( _input instanceof LinkedHashMap ) {
            @SuppressWarnings("unchecked")
            final LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>) _input;
            inputMap = map;
            forLoopProps.setProperty( FOREACH_ITER_VALUE, tools.Map2YAMLString(inputMap) );
        // } else if ( _input instanceof String ) {
        //     // WARNING: THIS IS NOT NEGOTIABLE .. I do NOT (can NOT) have an Input-Map (non-Scalar) as parameter !!!!!!!!!!!!!!!!!!!!!
        //     // so, we start off this function with an EMPTY 'inputMap'
        //     inputMap = new LinkedHashMap<String, Object>();
        //     forLoopProps.setProperty( FOREACH_ITER_VALUE, tools.Map2YAMLString(inputMap) );
        } else {
            throw new BatchFileException( HDR + " INTERNAL ERROR: _input is Neither Map nor String:  while processing "+ _batchCmds.getState() +" .. unknown object of type ["+ _input.getClass().getName() +"]" );
        }

        return super.processBatch( _bInRecursion, _batchCmds, inputMap );
    }

    //-----------------------------------------------------------------------------
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //-----------------------------------------------------------------------------

    /**
     *  Based on command type, process the inputMap and produce an output - for that specific command
     *  @param _batchCmds Non-Null instance of {@link BatchFileGrammer}
     *  @param _o input YAML as java.utils.LinkedHashMap&lt;String, Object&gt; object.  In case of recursion, this object can be java.lang.String
     *  @return After this function completes processing SUCCESSFULLY.. it returns a java.utils.LinkedHashMap&lt;String, Object&gt; object.  If there is any failure, either return value is NULL or an Exception is thrown.
     *  @throws BatchYamlProcessor.BatchFileException if there is any issue with the command in the batchfile
     *  @throws Macros.MacroException if there is any issues with evaluating Macros.  This is extremely rare, and indicates a software bug.
     *  @throws java.io.FileNotFoundException specifically thrown by the SnakeYAML-library subclass of this
     *  @throws java.io.IOException Any issues reading or writing to PropertyFiles or to JSON/YAML files
     *  @throws Exception Any other unexpected error
     */
    @Override
    protected LinkedHashMap<String, Object>  processFOREACHCmd_Step1( final BatchFileGrammer _batchCmds, java.util.LinkedHashMap<String,Object> _o )
                throws BatchYamlProcessor.BatchFileException, Macros.MacroException, java.io.FileNotFoundException, java.io.IOException, Exception
    {
        final String HDR = CLASSNAME +": processFOREACHCmd_Step1(): ";
        assertTrue( _batchCmds != null );
        assertTrue( _o != null );

        // if ( _o == null )
        //     return new LinkedHashMap<String, Object>();
        final Output output = new Output( this.verbose );
        final Output.OutputType typ = output.getWrappedObjectType( _o );

        if ( this.verbose ) System.out.println( CLASSNAME +" processFOREACHCmdForObject(): BEFORE STARTING SWITCH-stmt.. re: "+ _batchCmds.getState() +" object of type ["+ _o.getClass().getName() +"] = "+ typ.toString() );

        switch(typ) {
            case Type_ArrayList:
                final ArrayList<String> arr = output.getArrayList( _o );
                return processFOREACH_Step2( _batchCmds, arr );
                // break;
            case Type_LinkedList:
                final LinkedList<String> lst = output.getLinkedList( _o );
                return processFOREACH_Step2( _batchCmds, lst );
                // break;
            case Type_KVPairs:  // PLURAL;  Note the 's' character @ end.  This is Not KVPair (singular)
                ArrayList< Tuple< String,String > > kvpairs = output.getKVPairs( _o );
                // final LinkedHashMap<String, Object> outpMap1 = this.process Batch( true, BatchFileGrammer.deepClone(_batchCmds), map );
                // Note: KVPairs means a LinkedHashMap - with NO elements at Depth 2 or more!  It really must be a VERY MOST SHALLOW LinkedHashMap containing just "String":"String" elements in it.
                // Then, yes it kind of makes sense to iterate over KVPairs.
                return processFOREACH_Step2( _batchCmds, kvpairs );
                // break;

            case Type_KVPair:  // singular;  No 's' character @ end.  This is Not KVPairs
            case Type_String:
                throw new BatchFileException( CLASSNAME +": processFOREACHCmdForObject(): ERROR while processing "+ _batchCmds.getState() +" .. executing a FOREACH-command after getting a SINGLE STRING scalar value as output does Not make AMY sense!" );
                // return this.processBatch( BatchFileGrammer.deepClone(_batchCmds), output.getTheActualObject( _o ).toString() );
                // break;
            case Type_LinkedHashMap:
                    throw new BatchFileException( CLASSNAME +": processFOREACHCmdForObject(): ERROR while processing "+ _batchCmds.getState() +" .. executing a FOREACH-command over a LinkedHashMap's contents, which contains arbitrary Nested Map-structure does Not make AMY sense!" );
                // @SuppressWarnings("unchecked")
                // final LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>) _o;
                // final LinkedHashMap<String, Object> outpMap1 = this.process Batch( true, BatchFileGrammer.deepClone(_batchCmds), map );
                // return outpMap1;
                // break;
            case Type_Unknown:
            default:
                throw new BatchFileException( CLASSNAME +": processFOREACHCmdForObject(): ERROR while processing "+ _batchCmds.getState() +" .. unknown object of type ["+ _o.getClass().getName() +"]");
        } // switch
    }

    //-------------------------------------------------------------------------
    private LinkedHashMap<String, Object>  processFOREACH_Step2( final BatchFileGrammer _batchCmds, final java.util.List<?> coll )
                throws BatchYamlProcessor.BatchFileException, Macros.MacroException, java.io.FileNotFoundException, java.io.IOException, Exception
    {
        final String HDR = CLASSNAME +": processFOREACH_Step2(): ";
        assertTrue( _batchCmds != null );
        assertTrue( coll != null );

        LinkedHashMap<String, Object> tempOutputMap = new LinkedHashMap<String, Object>();

        final Output output = new Output( this.verbose );
        final Properties forLoopProps = this.allProps.get( FOREACH_PROPERTIES );
        final String prevForLoopIndex = forLoopProps.getProperty( FOREACH_INDEX );

        Iterator itr = coll.iterator();
        for ( int ix=0;  itr.hasNext(); ix ++ ) {
            final Object o = itr.next();
            final Output.OutputType typ = output.getWrappedObjectType( o );

            forLoopProps.setProperty( FOREACH_INDEX, ""+ix ); // to be used by all commands INSIDE the 'foreach' block-inside-batchfile
            if ( this.verbose ) System.out.println( CLASSNAME +" processFORECHForArray(): @@@@@@@@@@@@@@@@@ foreach/Array-index #"+ ix +" : Object's type ="+ o.getClass().getName() +" and it's toString()=["+ o +"]" );
            if ( this.verbose ) System.out.println( CLASSNAME +" processFORECHForArray(): SWITCH's Type="+ typ.toString() );

            switch(typ) {
                case Type_String:
                    final LinkedHashMap<String, Object> retMap6 = this.processBatch( true, BatchFileGrammer.deepClone(_batchCmds), output.wrapAnObject_intoLinkedHashMap( o ) );
                    tempOutputMap.putAll( retMap6 );
                    break;
                case Type_KVPair:  // singular;  No 's' character @ end.  This is Not KVPairs
                    @SuppressWarnings("unchecked")
                    final Tuple< String,String > kvpair = ( Tuple< String,String > ) o;
                    forLoopProps.setProperty( FOREACH_ITER_KEY, kvpair.key ); // to be used by all commands INSIDE the 'foreach' block-inside-batchfile
                    final LinkedHashMap<String, Object> retMap7 = this.processBatch( true, BatchFileGrammer.deepClone(_batchCmds), output.wrapAnObject_intoLinkedHashMap( kvpair.val ) );
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
                    throw new BatchFileException( CLASSNAME +": processFORECHForArray(): ERROR: Un-implemented logic.  Not sure what this means: Array of Arrays! In "+ _batchCmds.getState() +" .. trying to iterate over object ["+ o +"]");
            } // end switch
    
        } // for arr.size()

        if ( prevForLoopIndex != null ) // if there was an outer FOREACH within the batch file, restore it's index.
            forLoopProps.setProperty( FOREACH_INDEX, prevForLoopIndex );

        return tempOutputMap;
    }

    //-----------------------------------------------------------------------------
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //-----------------------------------------------------------------------------

//     /**
//      * When this function returns, the "pointer" within _batchCmds (.currentLine & .getLineNum()) ..
//      *   should be pointing to the command AFTER the 'end' command.
//      * This function basically keeps track of any inner foreachs .. and that's how it knows when the matching 'end' was detected.
//      * @param _batchCmds pass-by-reference, so we can alter it's state and move it to the line AFTER matching 'end' commamd
//      * @param _sInvoker for use in debugging output only (as there is tons of recursion-loops within these classes)
//      * @throws BatchFileException
//      * @throws Exception
//      */
//     private void skipInnerForeachLoops( final BatchFileGrammer _batchCmds, final String _sInvoker )
//                                                 throws BatchFileException, Exception
//     {
//         final int bookmark = _batchCmds.getLineNum();
//         boolean bFoundMatchingENDCmd = false;
//         int recursionLevel = 0;
//         while ( _batchCmds.hasNextLine() ) {
//             /* final String line22 = */ _batchCmds.nextLineOrNull(); // we do Not care what the line is about.
//             _batchCmds.determineCmdType(); // must be the 2nd thing we do - if there is another line to be read from batch-file
//             if ( this.verbose ) System.out.println( CLASSNAME +" skipInnerForeachLoops("+_sInvoker+"): skipping cmd "+ _batchCmds.getState() );

//             final boolean bForEach22 = _batchCmds.isForEachLine();
//             if ( bForEach22 ) recursionLevel ++;

//             final boolean bEnd22 = _batchCmds.isEndLine();
//             if ( bEnd22 ) {
//                 recursionLevel --;
//                 if ( recursionLevel < 0 ) {
//                     bFoundMatchingENDCmd = true;
//                     break; // we're done completely SKIPPING all the lines between 'foreach' --> 'end'
//                 } else
//                     continue; // while _batchCmds.hasNextLine()
//             } // if bEnd22
//         }
//         if (  !  bFoundMatchingENDCmd ) // sanity check.  These exceptions will get thrown if logic in 100 lines above isn't water-tight
//             throw new BatchFileException( CLASSNAME +": skipInnerForeachLoops("+_sInvoker+"): ERROR In "+ _batchCmds.getState() +"] !!STARTING!! from line# "+ bookmark +".. do NOT see a MATCHING 'end' keyword following the  'foreach'.");
//     }

//     //======================================================================
//     private LinkedHashMap<String, Object> processSaveToLine( final BatchFileGrammer _batchCmds, final LinkedHashMap<String, Object> _inputMap )
//                                     throws Macros.MacroException,  java.io.IOException, Exception
//     {
//         final String saveTo_AsIs = _batchCmds.getSaveTo();
//         if ( saveTo_AsIs != null ) {
//             final String saveTo = Macros.eval( this.verbose, saveTo_AsIs, this.allProps );
//             if ( this.memoryAndContext == null || this.memoryAndContext.getContext() == null )
//                 throw new BatchFileException( CLASSNAME +": processSaveToLine(): ERROR In "+ _batchCmds.getState() +".. This program currently has NO/Zero memory from one line of the batch file to the next.  And a SaveTo line was encountered for ["+ saveTo +"]" );
//             else {
//                 @SuppressWarnings("unchecked")
//                 final LinkedHashMap<String,Object> newmap = (LinkedHashMap<String,Object>) org.ASUX.common.Utils.deepClone( _inputMap );
//                 this.memoryAndContext.getContext().saveDataIntoReference( saveTo, newmap );
//                 return newmap;
//             }
//         } else 
//             throw new BatchFileException( CLASSNAME +": processSaveToLine(): ERROR In "+ _batchCmds.getState() +".. Missing or empty label for SaveTo line was encountered = ["+ saveTo_AsIs +"]" );
//     }

//     //======================================================================
//     private LinkedHashMap<String, Object> processUseAsInputLine( final BatchFileGrammer _batchCmds )
//                                 throws java.io.FileNotFoundException, java.io.IOException, Exception,
//                                 Macros.MacroException, BatchFileException
//     {
//         final String inputFrom_AsIs = _batchCmds.getUseAsInput();
//         String inputFrom = Macros.eval( this.verbose, inputFrom_AsIs, this.allProps );
//         inputFrom = new org.ASUX.common.StringUtils(this.verbose).removeBeginEndQuotes( inputFrom );
//         if ( this.memoryAndContext == null || this.memoryAndContext.getContext() == null ) {
//             throw new BatchFileException( CLASSNAME +": processUseAsInputLine(): ERROR In "+ _batchCmds.getState() +".. This program currently has NO/Zero memory from one line of the batch file to the next.  And a useAsInput line was encountered for ["+ inputFrom +"]" );
//         } else {
//             final Object o = this.memoryAndContext.getContext().getDataFromReference( inputFrom );
//             if ( o instanceof LinkedHashMap ) {
//                 @SuppressWarnings("unchecked")
//                 final LinkedHashMap<String, Object> retMap3 = (LinkedHashMap<String, Object>) o;
//                 return retMap3;
//             } else {
//                 final String es = (o==null) ? "Nothing in memory under that label." : ("We have type="+ o.getClass().getName()  +" = ["+ o.toString() +"]");
//                 throw new BatchFileException( "ERROR In "+ _batchCmds.getState() +".. Failed to read YAML/JSON from ["+ inputFrom_AsIs +"].  "+ es );
//             }
//         }
//     }

//     //-----------------------------------------------------------------------------
//     //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
//     //-----------------------------------------------------------------------------

//     private LinkedHashMap<String, Object> onPropertyLineCmd(
//         final BatchFileGrammer _batchCmds, final LinkedHashMap<String, Object> inputMap, final LinkedHashMap<String, Object> tempOutputMap )
//         throws Macros.MacroException, java.io.FileNotFoundException, java.io.IOException
//     {
//         final Tuple<String,String> kv = _batchCmds.getPropertyKV(); // could be null, implying NOT a kvpair
//         if ( kv != null) {
//             final String kwom = Macros.eval( this.verbose, kv.key, this.allProps );
//             final String fnwom = Macros.eval( this.verbose, kv.val, this.allProps );
//             final Properties props = new Properties();
//             props.load( new java.io.FileInputStream( fnwom ) );
//             this.allProps.put( kwom, props ); // This line is the action taken by this 'PropertyFile' line of the batchfile
//         }
//         return inputMap; // as nothing changes re: Input and Output Maps.
//     }


//     private LinkedHashMap<String, Object> onPrintCmd(
//         final BatchFileGrammer _batchCmds, final LinkedHashMap<String, Object> inputMap, final LinkedHashMap<String, Object> tempOutputMap )
//         throws Macros.MacroException, Exception
//     {
//         final Tools tools = new Tools( this.verbose );
//         final String printExpression = _batchCmds.getPrintExpr();
//         if ( this.verbose ) System.out.print( ">>>>>>>>>>>>> print line is ["+printExpression +"]" );
//         if ( (printExpression != null) && (  !  printExpression.equals("-")) )  {
//             String outputStr = Macros.eval( this.verbose, printExpression, this.allProps );
//             if ( outputStr.trim().endsWith("\\n") ) {
//                 outputStr = outputStr.substring(0, outputStr.length()-2); // chop out the 2-characters '\n'
//                 if ( outputStr.trim().length() > 0 ) {
//                     // the print command has text other than the \n character
//                     final Object o = this.memoryAndContext.getDataFromMemory( outputStr.trim() );
//                     if ( o != null )
//                         System.out.println( o ); // println (end-of-line character outputted)
//                     else
//                         System.out.println( outputStr ); // println (end-of-line character outputted)
//                 } else { // if length() <= 0 .. which prints all we have is a simple 'print \n'
//                     System.out.println(); // OK. just print a new line, as the print command is a simple 'print \n'
//                 }
//             } else {
//                 final Object o = this.memoryAndContext.getDataFromMemory( outputStr.trim() );
//                 if ( o != null )
//                     System.out.println( o ); // println (end-of-line character outputted)
//                 else
//                     System.out.print( outputStr +" " ); // print only.  NO EOL character outputted.
//             }
//             // ATTENTION!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
//             // DO NOT COMMENT THIS ABOVE.  Do NOT ADD AN IF CONDITION to this.  This is by design.
//             System.out.flush();
//         } else {
//             // if the command/line is just the word 'print' .. print the inputMap
//             System.out.println( tools.Map2YAMLString(inputMap) );
//         }
//         return inputMap; // as nothing changes re: Input and Output Maps.
//     }

//     private LinkedHashMap<String, Object> onAnyCmd(
//         final BatchFileGrammer _batchCmds, final LinkedHashMap<String, Object> inputMap, final LinkedHashMap<String, Object> tempOutputMap )
//         throws BatchFileException, Macros.MacroException, java.io.FileNotFoundException, java.io.IOException, Exception
//     {
//         final String cmd_AsIs = _batchCmds.getCommand();
//         if ( cmd_AsIs != null ) {
//             if ( cmd_AsIs.equals("yaml") ) {
//                 return processAnyCommand( new YAMLCmdType(), _batchCmds, inputMap );
//             } else if ( cmd_AsIs.equals("aws.sdk") ) {
//                 return processAnyCommand( new AWSCmdType(), _batchCmds, inputMap );
//             } else {
//                 throw new BatchFileException( CLASSNAME +" onANYCmd(): Unknown Batchfile command ["+ cmd_AsIs +"]  in "+ _batchCmds.getState() );
//             }
//         } // if BatchCmd
//         return inputMap; // as nothing changes re: Input and Output Maps.
//     }

//     //-----------------------------------------------------------------------------
//     //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
//     //-----------------------------------------------------------------------------

//     private static abstract class IWhichCMDType {
//         protected String [] cmdLineArgsStrArr = null;
//         public abstract Object go( final boolean _verbose, final LinkedHashMap<String, Object> _inputMap, final MemoryAndContext _memoryAndContext )
//                         throws java.io.FileNotFoundException, java.io.IOException, java.lang.Exception;

//         public void convStr2Array( final boolean _verbose, final String _cmdStr, final LinkedHashMap<String,Properties> _allProps )
//                                 throws Macros.MacroException, java.io.IOException
//         {
//             if (_verbose) System.out.println( CLASSNAME +".IWhichCMDType.convStr2Array(): _cmdStr="+ _cmdStr );
//             String cmdStrCompacted = _cmdStr.replaceAll("\\s\\s*", " "); // replace multiple spaces with a single space.
//             // cmdStrCompacted = cmdStrCompacted.trim(); // no need.  The _batchCmds already took care of it.
//             final String cmdStrNoMacros = Macros.eval( _verbose, cmdStrCompacted, _allProps ).trim();
//             if (_verbose) System.out.println( CLASSNAME +".IWhichCMDType.convStr2Array(): cmdStrCompacted = "+ cmdStrCompacted );

//             // https://mvnrepository.com/artifact/com.opencsv/opencsv
//             final java.io.StringReader reader = new java.io.StringReader( cmdStrNoMacros );
//             final com.opencsv.CSVParser parser = new com.opencsv.CSVParserBuilder().withSeparator(' ').withQuoteChar('\'').withIgnoreQuotations(false).build();
//             final com.opencsv.CSVReader cmdLineParser = new com.opencsv.CSVReaderBuilder( reader ).withSkipLines(0).withCSVParser( parser ).build();
//             this.cmdLineArgsStrArr = cmdLineParser.readNext(); // pretend we're reading the 1st line ONLY of a CSV file.
//             if (_verbose) { System.out.print( CLASSNAME +".IWhichCMDType.convStr2Array(): cmdLineArgsStrArr = ");  for( String s: cmdLineArgsStrArr) System.out.println(s+"\t"); System.out.println(); }
//             // some of the strings in this.cmdLineArgsStrArr may still have a starting and ending single/double-quote
//             this.cmdLineArgsStrArr = new org.ASUX.common.StringUtils(_verbose).removeBeginEndQuotes( this.cmdLineArgsStrArr );
//             if (_verbose) { System.out.print( CLASSNAME +".IWhichCMDType.convStr2Array(): cmdLineArgsStrArr(NOQUOTES) = ");  for( String s: cmdLineArgsStrArr) System.out.println(s+"\t"); System.out.println(); }
//         }
//     }
//     //----------------------
//     private static class YAMLCmdType extends IWhichCMDType {
//          // technically override -.. but, actually extend!
//         public void convStr2Array( final boolean _verbose, final String _cmdStr, final LinkedHashMap<String,Properties> _allProps ) throws Macros.MacroException, java.io.IOException {
//             final String cmdStrWIO = _cmdStr + " -i - -o -";
//             super.convStr2Array( _verbose, cmdStrWIO, _allProps ); // this will set  this.cmdLineArgsStrArr
//             this.cmdLineArgsStrArr = Arrays.copyOfRange( this.cmdLineArgsStrArr, 1, this.cmdLineArgsStrArr.length ); // get rid of the 'yaml' word at the beginning
//         }
//         public Object go( final boolean _verbose, final LinkedHashMap<String, Object> _inputMap, final MemoryAndContext _memoryAndContext )
//                 throws java.io.FileNotFoundException, java.io.IOException, java.lang.Exception
//         {
//             final CmdLineArgsBasic cmdlineArgsBasic = new CmdLineArgsBasic( this.cmdLineArgsStrArr );
//             if (_verbose) System.out.println( CLASSNAME +".YAMLCmdType.go(): cmdlineArgsBasic = "+ cmdlineArgsBasic.toString() );
//             final CmdLineArgs cmdLineArgs = cmdlineArgsBasic.getSpecificCmd();
//             cmdLineArgs.verbose = _verbose; // pass on whatever this user specified on cmdline re: --verbose or not.
//             if (_verbose) System.out.println( CLASSNAME +".YAMLCmdType.go(): cmdLineArgs="+ cmdLineArgs.toString() );
//             final org.ASUX.yaml.CmdInvoker origObj = _memoryAndContext.getContext();
//             final org.ASUX.yaml.CmdInvoker newCmdinvoker = org.ASUX.common.Utils.deepClone( origObj );
//             newCmdinvoker.setYamlLibrary( origObj.getYamlLibrary() );
//             // final org.ASUX.yaml.CmdInvoker cmdinvoker = new org.ASUX.yaml.CmdInvoker( cmdLineArgs.verbose, cmdLineArgs.showStats, _memoryAndContext ); // the 3rd parameter passed is an instance variable of the outer BatchYamlProcessor.java class
//             final Object output = newCmdinvoker.processCommand( cmdLineArgs, _inputMap );
//             return output;
//         }
//     }
//     //----------------------
//     private static class AWSCmdType extends IWhichCMDType {
//         private static AWSSDK awssdk = null;
//         //----------------------
//         public Object go( final boolean _verbose, final LinkedHashMap<String, Object> _inputMap, final MemoryAndContext _memoryAndContext )
//                 throws java.io.FileNotFoundException, java.io.IOException, java.lang.Exception
//         {
//             // for( String s: this.cmdLineArgsStrArr) System.out.print( "\t"+s );   System.out.println("\n\n");
//             // aws.sdk ----list-regions us-east-2
//             // aws.sdk ----list-AZs     us-east-2
//             if ( this.cmdLineArgsStrArr.length < 2 )
//                 throw new BatchFileException( CLASSNAME +": AWSCmdType.go(AWSCmdType): AWS.SDK command is NOT of sufficient # of parameters ["+ this.cmdLineArgsStrArr +"]");

//             if ( AWSCmdType.awssdk == null ) AWSCmdType.awssdk = AWSSDK.AWSCmdline( _verbose );

//             // skip the 1st word (in this.cmdLineArgsStrArr)  which is fixed at 'AWS.SDK'
//             final String awscmdStr = this.cmdLineArgsStrArr[1];
//             final String[] awscmdlineArgs = java.util.Arrays.copyOfRange( this.cmdLineArgsStrArr, 2, this.cmdLineArgsStrArr.length ); // last parameter: the final index of the range to be copied, exclusive
//             if ( awscmdStr.equals("--list-regions")) {
//                 final ArrayList<String> regionsList = awssdk.getRegions( );
//                 return regionsList;
//             }
//             if ( awscmdStr.equals("--list-AZs")) {
//                 if ( this.cmdLineArgsStrArr.length < 3 )
//                     throw new BatchFileException( CLASSNAME +": AWSCmdType.go(AWSCmdType): AWS.SDK --list-AZs command: INSUFFICIENT # of parameters ["+ this.cmdLineArgsStrArr +"]");
//                 final ArrayList<String> AZList = awssdk.getAZs( awscmdlineArgs[0] ); // ATTENTION: Pay attention to index# of awscmdlineArgs
//                 return AZList;
//             }
//             if ( awscmdStr.equals("--describe-AZs")) {
//                 if ( this.cmdLineArgsStrArr.length < 3 )
//                     throw new BatchFileException( CLASSNAME +": AWSCmdType.go(AWSCmdType): AWS.SDK --list-AZs command: INSUFFICIENT # of parameters ["+ this.cmdLineArgsStrArr +"]");
//                 final ArrayList< LinkedHashMap<String,Object> > AZList = awssdk.describeAZs( awscmdlineArgs[0] ); // ATTENTION: Pay attention to index# of awscmdlineArgs
//                 return AZList;
//             }
//             return null;
//         } // go() function
//     } // end of AWSCmdType class

//     //-----------------------------------------------------------------------------
//     //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
//     //-----------------------------------------------------------------------------

//     private LinkedHashMap<String, Object> processAnyCommand(
//         final IWhichCMDType _cmdType, final BatchFileGrammer _batchCmds, final LinkedHashMap<String, Object> _inputMap )
//                                             throws BatchFileException, Macros.MacroException, java.io.IOException, Exception
//     {
//         final String cmd_AsIs = _batchCmds.getCommand();
//         final String cmdStr2 = Macros.eval( this.verbose, cmd_AsIs, this.allProps ).trim();
//         if ( cmdStr2 == null )
//             return null;

//         _cmdType.convStr2Array( this.verbose, _batchCmds.currentLine(), this.allProps );
//         // if ( this.verbose ) for( String s: cmdLineArgsStrArr ) System.out.println( "\t"+ s );

//         try {
//             final Object outp = _cmdType.go( this.verbose, _inputMap, this.memoryAndContext );
//             return new Output(this.verbose).wrapAnObject_intoLinkedHashMap( outp );
//         } catch (Exception e) {
//             e.printStackTrace(System.err);
//             final String estr = "ERROR In "+ _batchCmds.getState() +".. Failed to run the command in current line.";
//             System.err.println( CLASSNAME + estr );
//             throw new BatchFileException( estr );
//         }
// }

    //=======================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=======================================================================


}
