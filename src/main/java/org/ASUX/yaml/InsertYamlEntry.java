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

import java.util.LinkedList;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/** <p>This concrete class is minimalistic because I am re-using code to query/traverse a YAML file.   See it's parent-class {@link org.ASUX.yaml.AbstractYamlEntryProcessor}.</p>
 *  <p>This concrete class is part of a set of 4 concrete sub-classes (representing YAML-COMMANDS to read/query, list, delete and replace ).</p>
 *  <p>This class contains implementation for 4 "callbacks" - </p><ol><li> whenever there is partial match - on the way to a complete(a.k.a. end2end match) </li><li> whenever a full match is found </li><li> a match failed (which implies, invariably, to keep searching till end of YAML file - but.. is a useful callback if you are using a "negative" pattern to search for YAML elements) </li><li> done processing entire YAML file</li></ol>
 *  <p>This org.ASUX.yaml GitHub.com project and the <a href="https://github.com/org-asux/org.ASUX.cmdline">org.ASUX.cmdline</a> GitHub.com projects, would
 *  simply NOT be possible without the genius Java library <a href="https://github.com/EsotericSoftware/yamlbeans">"com.esotericsoftware.yamlbeans"</a>.</p>
 *  <p>See full details of how to use this, in {@link org.ASUX.yaml.Cmd} as well as the <a href="https://github.com/org-asux/org-ASUX.github.io/wiki">org.ASUX Wiki</a> of the GitHub.com projects.</p>
 * @see org.ASUX.yaml.AbstractYamlEntryProcessor
 */
public class InsertYamlEntry extends AbstractYamlEntryProcessor {

    public static final String CLASSNAME = "org.ASUX.yaml.InsertYamlEntry";

    // Note: We need to remove the "old" - exactly as DeleteYamlEntry.java does.  Then we insert new value.
    protected final ArrayList< Tools.Tuple< String, LinkedHashMap<String, Object> > >
                    existingPathsForInsertion = new ArrayList<>();
    protected final ArrayList< Tools.Tuple< YAMLPath, LinkedHashMap<String, Object> > >
                    newPaths2bCreated = new ArrayList<>();
    protected Object newData2bInserted = "";

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    /** The only Constructor.
     *  @param _verbose Whether you want deluge of debug-output onto System.out
     *  @param _r this can be either a java.lang.String or a java.util.LinkedHashMap&lt;String, Object&gt; (created by com.esotericsoftware.yamlbeans)
     *  @param _showStats Whether you want a final summary onto console / System.out
     *  @throws java.lang.Exception - if the _r parameter is not as per above Spec
     */
    public InsertYamlEntry( final boolean _verbose, final boolean _showStats, Object _r ) throws Exception {
        super( _verbose, _showStats );
        if ( _r == null )
            throw new Exception( CLASSNAME + ": constructor(): _r parameter is Null");

        if (_r instanceof java.lang.String) {
            // Convert Strings into YAML/JSON compatible LinkedHashMap .. incl. converting Key=Value  --> Key: Value
            _r = new Tools(this.verbose).JSONString2YAML( _r.toString() );
        } else if (_r instanceof java.util.LinkedHashMap ) {
            // Do Nothing
        } else
            throw new Exception( CLASSNAME + ": constructor(): Invalid _r parameter of type:" + _r.getClass().getName() + "'");

        this.newData2bInserted = _r;
    }

    private InsertYamlEntry() {
        super( false, true );
    }

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    
    /** This function will be called when a partial match of a YAML path-expression happens.
     * See details and warnings in {@link org.ASUX.yaml.AbstractYamlEntryProcessor#onPartialMatch}
     */
    protected boolean onPartialMatch( final LinkedHashMap<String, Object> _map, final YAMLPath _yamlPath, final String _key, final LinkedHashMap<String, Object> _parentMap, final LinkedList<String> _end2EndPaths ) {

        // Do Nothing for "Insert YAML-entry command"
        return true;
    }

    //-------------------------------------
    /** This function will be called when a full/end2end match of a YAML path-expression happens.
     * See details and warnings in {@link org.ASUX.yaml.AbstractYamlEntryProcessor#onEnd2EndMatch}
     */
    protected boolean onEnd2EndMatch( final LinkedHashMap<String, Object> _map, final YAMLPath _yamlPath, final String _key, final LinkedHashMap<String, Object> _parentMap, final LinkedList<String> _end2EndPaths ) {

        if ( this.verbose ) {
            System.out.print("onEnd2EndMatch: _end2EndPaths =");
            _end2EndPaths.forEach( s -> System.out.print(s+", ") );
            System.out.println("");
        }
        this.existingPathsForInsertion.add( new Tools.Tuple< String, LinkedHashMap<String, Object> >(_key, _map) );
        if ( this.verbose ) System.out.println("onE2EMatch: count="+this.existingPathsForInsertion.size());
        return true;
    }

    //-------------------------------------
    /** This function will be called whenever the YAML path-expression fails to match.
     * See details and warnings in {@link org.ASUX.yaml.AbstractYamlEntryProcessor#onMatchFail}
     */
    protected void onMatchFail( final LinkedHashMap<String, Object> _map, final YAMLPath _yamlPath, final String _key, final LinkedHashMap<String, Object> _parentMap, final LinkedList<String> _end2EndPaths )
    {
        // we are going to have TONS and TONS of entries within this.newPaths2bCreated !!!
        // Especially for large YAML files - let's say - 1000 lines, then.. you could see a couple of hundred entries
        // Also, we'll have SO MANY DUPLICATES!
        if ( _yamlPath == null || _map == null )
            return;
        final Tools.Tuple< YAMLPath, LinkedHashMap<String, Object> > tuple = new Tools.Tuple<>( YAMLPath.deepClone(_yamlPath), _map );
        if ( this.verbose ) System.out.println( CLASSNAME +": onMatchFail():>>>>>>>>>>>> _yamlPath="+ _yamlPath.toString() );
        if ( this.verbose ) System.out.println( CLASSNAME +": onMatchFail():>>>>>>>>>>>> tuple="+ tuple.key );
        this.newPaths2bCreated.add( tuple );
    }

    //-------------------------------------
    /** This function will be called when processing has ended.
     * After this function returns, the AbstractYamlEntryProcessor class is done!
     * See details and warnings in {@link AbstractYamlEntryProcessor#atEndOfInput}
     *
     * You can fuck with the contents of any of the parameters passed, to your heart's content.
     */
    protected void atEndOfInput( final LinkedHashMap<String, Object> _map, final YAMLPath _yamlPath )
    {
        // first loop goes over Paths that already exist, in the sense the leaf-element exists, and we'll add a new Child element to that.
        for ( Tools.Tuple< String, LinkedHashMap<String, Object> > tpl: this.existingPathsForInsertion ) {
            final String rhsStr = tpl.val.toString();
            if ( this.verbose ) System.out.println( CLASSNAME +": atEndOfInput(): "+ tpl.key +": "+ rhsStr.substring(0,rhsStr.length()>121?120:rhsStr.length()));
            tpl.val.remove(tpl.key);

            // Now put in a new entry - with the replacement data!
            tpl.val.put( tpl.key, Tools.deepClone( this.newData2bInserted ) );
            // If there are multiple matches.. then without deepclone, the EsotericSoftware
            // library, will use "&1" to define your 1st copy (in output) and put "*1" in
            // all other locations this replacement text WAS SUPPOSED have been :-(
        }

        //------------------------------------------------
        // IMPORTANT: See the comments inside onMatchFail
        // We need to 'cull' the entries within this.newPaths2bCreated
        int longestDepth = -1;
        for ( Tools.Tuple< YAMLPath, LinkedHashMap<String, Object> > tpl : this.newPaths2bCreated ) {
            final YAMLPath yp = tpl.key;
            if ( yp.index()> longestDepth ) longestDepth = yp.index();
        }

        if ( this.verbose ) System.out.println( CLASSNAME +": atEndOfInput(): longestDepth ="+ longestDepth +"]" );
        final ArrayList< Tools.Tuple< YAMLPath, LinkedHashMap<String, Object> > >
                    deepestNewPaths2bCreated = new ArrayList<>();

        outerloop:
        for ( Tools.Tuple< YAMLPath, LinkedHashMap<String, Object> > tpl : this.newPaths2bCreated ) {
            final YAMLPath ypNew = tpl.key;
            if ( this.verbose ) System.out.println( CLASSNAME +": atEndOfInput(): ypNew ="+ ypNew +"]" );
            if ( ypNew.index() >= longestDepth ) {
                // let's check .. have we added it already?
                for ( Tools.Tuple< YAMLPath, LinkedHashMap<String, Object> > tpl2 : deepestNewPaths2bCreated ) {
                    final YAMLPath ypE = tpl2.key;
                    if ( YAMLPath.areEquivalent( ypNew, ypE ) )
                        continue outerloop;
                } // inner for-loop
                deepestNewPaths2bCreated.add ( tpl );
                if ( this.verbose ) System.out.println( CLASSNAME +": atEndOfInput(): added new entry "+tpl +" making deepestNewPaths2bCreated's size ="+ deepestNewPaths2bCreated.size() +"]" );
            }
        } // outer for-loop
        // going forward.. ignore this.newPaths2bCreated
        // instead use deepestNewPaths2bCreated (local variable).  Both are exactly the same class-type.

        //------------------------------------------------
        // This 2nd loop is going to deal with WITH MISSING 'paths' to the missing leaf-element.
        // similar to how 'mkdir -p' works on Linux.
        if ( this.verbose ) System.out.println( CLASSNAME +": atEndOfInput(): deepestNewPaths2bCreated.size()="+ deepestNewPaths2bCreated.size() +"]" );
        for ( Tools.Tuple< YAMLPath, LinkedHashMap<String, Object> > tpl : deepestNewPaths2bCreated ) {
            final YAMLPath yp = tpl.key;
            final LinkedHashMap<String, Object> lowestmap = tpl.val;
            final String prefix = yp.getPrefix();
            final String suffix = yp.getSuffix();
            if ( this.verbose ) System.out.println( CLASSNAME +": atEndOfInput(): about to add the NEW path ["+ suffix +"]" );
            Object prevchildelem = this.newData2bInserted;
            for( int ix=yp.yamlElemArr.length - 1;   ix > yp.index() ; ix-- ) {
                // ATTENTION !!!!!!!!!!!!!!!!!!!!!!!!!!!! This iterator / for-loop counts DOWN.
                final LinkedHashMap<String, Object> newelem = new LinkedHashMap<>();
                newelem.put ( yp.yamlElemArr[ix], prevchildelem );
                prevchildelem = newelem;
                if ( this.verbose ) System.out.println( CLASSNAME +": atEndOfInput(): @ "+ ix +" yp.yamlElemArr[ix]="+ yp.yamlElemArr[ix] +"  newelem= ["+ newelem.toString() +"]" );
            }
            if ( this.verbose ) System.out.println( CLASSNAME +": atEndOfInput(): Adding the final MISSING Path-elem @ ["+ yp.index() +"]" );
            if ( this.verbose ) System.out.println( CLASSNAME +": atEndOfInput(): parent Map = ["+ lowestmap.toString() +"]" );
            lowestmap.put(  yp.yamlElemArr[ yp.index() ],  prevchildelem );
        }

        // java's forEach never works if you are altering anything within the Lambda body
        // this.existingPathsForInsertion.forEach( tpl -> {tpl.val.remove(tpl.key); });
        if ( this.showStats ) System.out.println( "count="+ (this.existingPathsForInsertion.size() + deepestNewPaths2bCreated.size()) );
        if ( this.showStats ) this.existingPathsForInsertion.forEach( tpl -> { System.out.println(tpl.key); } );
    }

}
