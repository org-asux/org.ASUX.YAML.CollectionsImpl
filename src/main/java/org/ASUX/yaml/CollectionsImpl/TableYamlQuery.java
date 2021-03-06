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

import java.util.regex.*;

import java.util.LinkedList;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/** <p>This concrete class is minimalistic because I am re-using code to query/traverse a YAML file.   See it's parent-class {@link AbstractYamlEntryProcessor}.</p>
 *  <p>This concrete class is part of a set of 4 concrete sub-classes (representing YAML-COMMANDS to read/query, list, delete and replace ).</p>
 *  <p>This class contains implementation for 4 "callbacks" - </p><ol><li> whenever there is partial match - on the way to a complete(a.k.a. end2end match) </li><li> whenever a full match is found </li><li> a match failed (which implies, invariably, to keep searching till end of YAML file - but.. is a useful callback if you are using a "negative" pattern to search for YAML elements) </li><li> done processing entire YAML file</li></ol>
 *  <p>This org.ASUX.yaml GitHub.com project and the <a href="https://github.com/org-asux/org.ASUX.cmdline">org.ASUX.cmdline</a> GitHub.com projects.</p>
 *  <p>See full details of how to use this, in {@link org.ASUX.yaml.Cmd} as well as the <a href="https://github.com/org-asux/org-ASUX.github.io/wiki">org.ASUX Wiki</a> of the GitHub.com projects.</p>
 * @see AbstractYamlEntryProcessor
 */
public class TableYamlQuery extends AbstractYamlEntryProcessor {

    public static final String CLASSNAME = "org.ASUX.yaml.TableYamlQuery";

    private String[] tableColumns = new String[]{"UNinitialized", "TableColumns"};
    private String delimiter = "UNINITIALIZED DELIMITER";
    private int count;
    private LinkedList< ArrayList<String> > output;

    //------------------------------------------------------------------------------
    public static class TableCmdException extends Exception {
        private static final long serialVersionUID = 12L;
        public TableCmdException(String _s) { super(_s); }
    }

    //=======================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=======================================================================

    /** The only Constructor.
     *  @param _verbose Whether you want deluge of debug-output onto System.out
     *  @param _showStats Whether you want a final summary onto console / System.out
     *  @param _tableColumns a delimiter-separated list of "columns" (think SQL table columns).  The output of this table-yaml command is true 2-D table (2-D Array to be precise)
     *  @param _delim This delimiter should be used to separate the 'column-names' within the _tableColumns parameter.  pass in a value like '.'  '\t'   ','   .. pass in such a character as a string-parameter (being flexible in case delimiters can be more than a single character)
     *  @throws Exception if Pattern provided for YAML-Path is either semantically empty, Not a viable variable-name (see BatchFileGrammer.java's REGEXP_NAME constant) or is NOT java.util.Pattern compatible.
     */
    public TableYamlQuery( final boolean _verbose, final boolean _showStats, String _tableColumns, final String _delim )
                        throws Exception
    {
        super( _verbose, _showStats );
        this.delimiter = _delim;
        this.count = 0;
        this.output = new LinkedList<>();

        // Sanity check of "_delim"
        try {
            Pattern p = Pattern.compile(_delim);
        }catch(PatternSyntaxException e){
            e.printStackTrace(System.err);
            System.err.println( CLASSNAME +" Constructor: Invalid delimiter-pattern '"+ _delim +"' provided to constructor " );
            return; // invalid YAML Path.  Let "this.isValid" stay as false
        }

        _tableColumns = _tableColumns.trim(); // strip leading and trailing whitesapce (Java11 user strip(), Java<11, use trim()
        if ( _tableColumns.length() <= 0 ) {
            throw new Exception( CLASSNAME +" Constructor: semantically EMPTY list of Table-columns provided to Table-query Command." );
        }

        if (this.verbose) System.out.println( CLASSNAME + ": about to split '"+ _tableColumns +"' with delimiter '"+ _delim +"'");
        this.tableColumns = _tableColumns.split( _delim );
        for(int ix=0; ix < this.tableColumns.length; ix++ ) {
            final String elem = this.tableColumns[ix];
            final String errMsg = CLASSNAME +" Constructor: Invalid column # "+ ix +" '"+ elem +"' provided to Table-query Command.";
            try {
                Pattern p = Pattern.compile( org.ASUX.yaml.BatchFileGrammer.REGEXP_NAME );
                if ( p.matcher( elem ).matches() )
                    continue;
                else
                    throw new Exception( errMsg );
            }catch(PatternSyntaxException e){
                e.printStackTrace(System.err);
                throw new Exception( errMsg );
            }
        }
    }

    private TableYamlQuery() {
        super(false, false);
        this.count = 0;
        this.output = new LinkedList<>();
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /** This function will be called when a partial match of a YAML path-expression happens.
     * See details and warnings in @see org.ASUX.yaml.AbstractYamlEntryProcessor#onPartialMatch()
     */
    protected boolean onPartialMatch(final LinkedHashMap<String, Object> _map, final YAMLPath _yamlPath, final String _key, final LinkedHashMap<String, Object> _parentMap, final LinkedList<String> _end2EndPaths) {

        // Do Nothing for "Table YAML-entry command"
        return true;
    }

    //-------------------------------------
    /** This function will be called when a full/end2end match of a YAML path-expression happens.
     * See details and warnings in @see org.ASUX.yaml.AbstractYamlEntryProcessor#onEnd2EndMatch()
     */
    protected boolean onEnd2EndMatch(final LinkedHashMap<String, Object> _map, final YAMLPath _yamlPath, final String _key, final LinkedHashMap<String, Object> _parentMap, final LinkedList<String> _end2EndPaths )
        throws Exception
    {
        this.count ++;
        if ( this.verbose ) {
            System.out.print( CLASSNAME +": onEnd2EndMatch(): _end2EndPaths =");
            _end2EndPaths.forEach( s -> System.out.print(s+_yamlPath.delimiter) );
            System.out.println("onEnd2EndMatch: _key = ["+ _key +"] _map.get(_key) = ["+ _map.get(_key) +"]");
        }

        String errmsg = CLASSNAME +" onEnd2EndMatch(): For the pattern for YAML-Path "+ _yamlPath.toString() +" we found [";
        for( String s: _end2EndPaths )
            errmsg += s+this.delimiter;

        //-------------------------------------
        // local Class - so I can create a pasueod "local function"
        class PullTableElemsFromMap {
            public void go(final Object _o, final String[] _tableColumns, final String _errmsg ) throws TableCmdException {
                @SuppressWarnings("unchecked")
                final LinkedHashMap<String, Object> map = ( LinkedHashMap<String, Object> ) _o;
                final ArrayList<String> tablerow = new ArrayList<>();
                for(int ix=0; ix < _tableColumns.length; ix++ ) {
                    final String elem = _tableColumns[ix];
                    if ( verbose) System.out.println( CLASSNAME +": onEnd2EndMatch().PullTableElemsFromMap.go(): Going thru ArrayList item # "+ ix +" = "+ _o.toString() +" for key="+ elem );
                    final Object o = map.get(elem);
                    if ( verbose) System.out.println( CLASSNAME +": onEnd2EndMatch().PullTableElemsFromMap.go(): for key="+ elem + " val=["+ o.toString() +"]" );
                    if ( (o != null) && (o instanceof String) ) {
                        tablerow.add( o.toString() );
                    } else {
                        throw new TableCmdException( _errmsg + "].  Can NOT find the columns "+ ix +" '"+ elem +"' provided to Table-query Command." );
                    }
                } // for
                output.add( tablerow ); // could be a string or a java.util.LinkedHashMap&lt;String, Object&gt;
            } // go()
        } // local class PullTableElemsFromMap
        //-------------------------------------
        final Object om = _map.get(_key);
        if ( om instanceof LinkedHashMap ) {
            new PullTableElemsFromMap().go( om, this.tableColumns, errmsg );
        } else if ( om instanceof ArrayList ) {
            @SuppressWarnings("unchecked")
            final ArrayList<Object> arr = ( ArrayList<Object> ) om;
            int ix = 0;
            for ( Object ao: arr ) {
                if ( this.verbose) System.out.println( CLASSNAME +": onEnd2EndMatch(): Going thru ArrayList item # "+ (ix++) +" = "+ ao.toString() +" of type "+ ao.getClass().getName() );
                if ( ao instanceof LinkedHashMap ) {
                    // final ArrayList<String> tablerow = new ArrayList<>();
                    new PullTableElemsFromMap().go( ao, this.tableColumns, errmsg );
                    // this.output.add( tablerow ); // could be a string or a java.util.LinkedHashMap&lt;String, Object&gt;
                } else
                    throw new TableCmdException( errmsg +"].  But it does NOT have subelements!  Instead it's of type ["+ ao.getClass().getName() );
            } // for
        } else {
            throw new TableCmdException( errmsg +"].  But it does NOT have subelements!  Instead it's of type ["+ om.getClass().getName() );
        }

        return true;
    }

    //-------------------------------------
    /** This function will be called whenever the YAML path-expression fails to match.
     * See details and warnings in @see org.ASUX.yaml.AbstractYamlEntryProcessor#onMatchFail()
     */
    protected void onMatchFail(final LinkedHashMap<String, Object> _map, final YAMLPath _yamlPath, final String _key, final LinkedHashMap<String, Object> _parentMap, final LinkedList<String> _end2EndPaths) {

            // Do Nothing for "Table YAML-entry command"
    }

    //-------------------------------------
    /** This function will be called when processing has ended.
     * After this function returns, the AbstractYamlEntryProcessor class is done!
     * See details in @see org.ASUX.yaml.AbstractYamlEntryProcessor#oatEndOfInput()
     *
     * You can fuck with the contents of any of the parameters passed, to your heart's content.
     */
    protected void atEndOfInput(final LinkedHashMap<String, Object> _map, final YAMLPath _yamlPath) {
        if ( this.showStats ) System.out.println("Total=" + this.count );

        if ( this.verbose ) {
            for( ArrayList<String> arr: this.output ) {
                arr.forEach( s -> System.out.print(s+"\t") );
                System.out.println();
            } // for
        } // if
    }

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

    /**
     * @return the count of how many matches happened.  This value is also = this.getOutput.size()
     */
    public int getCount() {
        return this.count;
    }

    /**
     * @return the output as an LinkedList of objects (either Strings or java.util.LinkedHashMap&lt;String, Object&gt; objects).  This is because the 'rhs' of an 
     */
    public LinkedList< ArrayList<String> > getOutput() {
        return this.output;
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

}
