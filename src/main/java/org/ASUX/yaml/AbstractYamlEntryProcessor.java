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

import java.util.Map;
import java.util.LinkedList;
import java.util.ArrayList;

import java.util.regex.*;

import static org.junit.Assert.*;

public abstract class AbstractYamlEntryProcessor {

    public static final String CLASSNAME = "com.esotericsoftware.yamlbeans.AbstractYamlEntryProcessor";

    public final boolean verbose;

    public AbstractYamlEntryProcessor(boolean _verbose) {
        this.verbose = _verbose;
    }
    protected AbstractYamlEntryProcessor(){
        this.verbose = false;
    }

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

    /* This function will be called when a partial match of a YAML path-expression happens.
     * Example: if the YAML-Path-regexp is paths.*.*.responses.200.description
     * This function will be called for: paths./pet   paths./pet.put   paths./pet.put.responses paths./pet.put.responses.200
     * Note: This function will NOT be invoked for a full/end2end match at paths./pet.put.responses.200.description
     * That full/end2end match will trigger the other function "onEnd2EndMatch()".
     *
     * Do NOT fuck with (a.k.a alter) the contents of any of the parameters passed.   Use the parameters ONLY in Read-only manner.  Got itchy fingers?  Then, Deepclone both the parameters.  YAMLPath class has a static member-function to make it easy to deepClone.
     */
    abstract boolean onPartialMatch(final Map _map, final YAMLPath _yamlPath, final Object _key, final Map _parentMap, final LinkedList<String> _end2EndPaths);

    //-------------------------------------
    /* This function will be called when a full/end2end match of a YAML path-expression happens.
     * Example: if the YAML-Path-regexp is paths.*.*.responses.200.description
     * This function will be called ONLY for     paths./pet.put.responses.200.description
     * That partial matches (of "parent yaml-elements" will trigger the other function "onPartialMatch()".
     * The words "onFullMatch" * "onCompleteMatch()" are confusing from user/regexp perspective.
     *  Hence the choice of onEnd2EndMath() as the function name.
     *
     * Do NOT fuck with (a.k.a alter) the contents of any of the parameters passed.   Use the parameters ONLY in Read-only manner.  Got itchy fingers?  Then, Deepclone both the parameters.  YAMLPath class has a static member-function to make it easy to deepClone.
     */
    abstract boolean onEnd2EndMatch(final Map _map, final YAMLPath _yamlPath, final Object _key, final Map _parentMap, final LinkedList<String> _end2EndPaths);

    //-------------------------------------
    /* This function will be called whenever the YAML path-expression fails to match.
     * This will be called way too often.  It's only interesting if you want a "negative" match scenario (as in show all rows that do Not match)
     *
     * Do NOT fuck with (a.k.a alter) the contents of any of the parameters passed.   Use the parameters ONLY in Read-only manner.  Got itchy fingers?  Then, Deepclone both the parameters.  YAMLPath class has a static member-function to make it easy to deepClone.
     */
    abstract boolean onMatchFail(final Map _map, final YAMLPath _yamlPath, final Object _key, final Map _parentMap, final LinkedList<String> _end2EndPaths);

    //-------------------------------------
    /* This function will be called when processing has ended.
     * After this function returns, the AbstractYamlEntryProcessor class is done!
     *
     * You can fuck with the contents of any of the parameters passed, to your heart's content.
     */
    abstract boolean atEndOfInput(final Map _map, final YAMLPath _yamlPath);

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

    /* This is NOT NOT NOT NOT NOT ........RECURSION - NOT - FUNCTION.
     * Takes a java.utils.Map created by com.esotericsoftware.yamlbeans library.
     * Takes a Regexp parameter _yamlPathStr (in string form)
     */
    public boolean searchYamlForPattern(Map _map, String _yamlPathStr) throws com.esotericsoftware.yamlbeans.YamlException {
        final LinkedList<String> end2EndPaths = new LinkedList<>();
        final YAMLPath yp = new YAMLPath( _yamlPathStr );
        final boolean retval = this.recursiveSearch( _map, yp, end2EndPaths );
        atEndOfInput( _map, yp );
        // What should be done if atEndOfInput returns false.. ?
        return retval;
   }

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

    /* This is RECURSION FUNCTION.
     * This function returns true, if the invocation (or it's recursion) did find a match (partial or end2end).
     * For now, I'm Not using the return value ANYWHERE.   Either I will - or - will refactor the return as Void.
     */
    public boolean recursiveSearch(Map _map, final YAMLPath _yamlPath, final LinkedList<String> _end2EndPaths )
    throws com.esotericsoftware.yamlbeans.YamlException {
        if ( _map == null || (! _yamlPath.isValid) ) return false;
        if ( ! _yamlPath.hasNext() ) return false; // YAML path has ended

        //--------------------------
        final String yamlPathElemStr = _yamlPath.get(); // current path-element (a substring of full yamlPath)
        final Pattern yamlPElemPatt = Pattern.compile(yamlPathElemStr); // This should Not throw, per precautions in YAMLPath class
        
        boolean matchFound = false;

        //--------------------------
        for (Object key : _map.keySet()) {

            final Object rhs = _map.get(key);  // otherwise we'll inefficiently be doing map.get multiple times below.
            final String rhsStr = rhs.toString(); // to make verbose logging code simplified

            if ( this.verbose ) System.out.println ( CLASSNAME +": "+ key +": "+ rhsStr.substring(0,rhsStr.length()>181?180:rhsStr.length()) );

            if ( yamlPElemPatt.matcher(key.toString()).matches() ) { // yaml-path-element matches the "key"
                if ( this.verbose ) System.out.println(CLASSNAME + ": @# " + _yamlPath.index() +"\t"+ _yamlPath.getPrefix() +"\t"+ _yamlPath.get() +"\t"+ _yamlPath.getSuffix() + "\t  matched '"+ key +"':\t"+ rhsStr.substring(0,rhsStr.length()>121?120:rhsStr.length()) +"\t\t of type '"+rhs.getClass().getName() +"'");

                _end2EndPaths.add( key.toString() ); // _end2EndPaths keeps the breadcrumbs

                //------------------------------------------------------
                assert (_yamlPath.hasNext()); // why on earth would this assertion fail - see checks @ top of function.
                final YAMLPath cloneOfYAMLPath = YAMLPath.deepClone(_yamlPath); // to keep _yamlPath intact as we recurse in & out of sub-yaml-elements
                cloneOfYAMLPath.next();

                if ( ! cloneOfYAMLPath.hasNext() ) {
                    // NO more recursion feasible!
                    // well! we've matched end2end .. to a "Map" element (instead of String elem)!

                    // let sub-classes determine what to do here
                    onEnd2EndMatch(_map, _yamlPath, key, _map, _end2EndPaths); // location #1 for end2end match
                    // What should be done if onEnd2EndMatch returns false.. ?
                    _end2EndPaths.removeLast();

                    if ( this.verbose ) System.out.println(CLASSNAME +": deleting entry in YAML-file: "+ _yamlPath.getPrefix() +" "+ key  +":\t"+  rhsStr.substring(0,rhsStr.length()>121?120:rhsStr.length()) +"\t\t type '"+rhs.getClass().getName() +"'");

                    continue; // outermost for-loop (Object key : _map.keySet())
                }
                //else .. continue below.

                //------------------------------------------------------
                // If we're here, it means INCOMPLETE match..

                // let sub-classes determine what to do here
                onPartialMatch(_map, _yamlPath, key, _map, _end2EndPaths );
                // What should be done if onPartialMatch returns false.. ?

                if ( this.verbose ) System.out.println(CLASSNAME + ": recursing with YAMLPath @# " + cloneOfYAMLPath.index() +"\t"+ cloneOfYAMLPath.getPrefix() +"\t"+ cloneOfYAMLPath.get() +"\t"+ cloneOfYAMLPath.getSuffix() +": ... @ YAML-file-location: '"+ key +"': "+ rhsStr.substring(0,rhsStr.length()>121?120:rhsStr.length()));

                @SuppressWarnings("unchecked") final LinkedList<String> cloneOfE2EPaths = (LinkedList<String>) _end2EndPaths.clone();

                //--------------------------------------------------------
                // if we are here, we've only a PARTIAL match.
                // So.. we need to keep recursing (specifically for Map & ArrayList YAML elements)
                if ( rhs instanceof Map ) {

                    matchFound = this.recursiveSearch( (Map)rhs, cloneOfYAMLPath, cloneOfE2EPaths); // recursion call

                } else if ( rhs instanceof java.util.ArrayList ) {
                    ArrayList arr = (ArrayList) rhs;
                    for ( Object o: arr ) {
                        // iterate over each element?  Need a new variant of this function - to match "index"
                        if ( o instanceof Map ) { // Shouldn't this always be true - per YAML spec?
                            matchFound = this.recursiveSearch( (Map)o, cloneOfYAMLPath, cloneOfE2EPaths); // recursion call
                        } else {
                            System.err.println(CLASSNAME +": incomplete code: failure w Array-type '"+ rhs.getClass().getName() +"'");
                            onMatchFail(_map, _yamlPath, key, _map, _end2EndPaths); // location #1 for failure-2-match
                            // What should be done if onMatchFail returns false.. ?
                        } // if-Else   o instanceof Map - (WITHIN FOR-LOOP)
                    } // for Object o: arr
                } else if ( rhs instanceof java.lang.String ) {
                    // yeah! We found a full end2end match!  No more recursion is feasible.
                    // let sub-classes determine what to do here
                    onEnd2EndMatch(_map, _yamlPath, key, _map, _end2EndPaths); // location #1 for end2end match
                    // What should be done if onMatchFail returns false.. ?
                    _end2EndPaths.clear();
                    matchFound = true;
                    if ( this.verbose ) System.out.println(CLASSNAME +": deleting entry: "+ key +": "+ rhsStr.substring(0,rhsStr.length()>121?120:rhsStr.length()));
                } else {
                    System.err.println(CLASSNAME +": incomplete code: Unable to handle rhs of type '"+ rhs.getClass().getName() +"'");
                    onMatchFail(_map, _yamlPath, key, _map, _end2EndPaths); // location #2 for failure-2-match
                    // What should be done if onMatchFail returns false.. ?
                } // if-else   rhs instanceof   Map/Array/String/.. ..

                // As we've had AT-LEAST a PARTIAL-MATCH, in CURRENT-ITERATION (of FOR-LOOP).. ..
                // we need to "undo" that for next iteration (of FOR) for the next-peer YAML-element
                if ( _end2EndPaths.size() > 0 )  _end2EndPaths.removeLast();
                
            } else {
                // Failure of yamlPElemPatt.matcher  -- -- i.e., to match YAML-Path pattern.
                onMatchFail(_map, _yamlPath, key, _map, _end2EndPaths); // location #3 for failure-2-match
                // What should be done if onMatchFail returns false.. ?
                
            }// if-else yamlPElemPatt.matcher()

        } // for loop   key: _map.keySet()

        // Now that we looped thru all keys at current recursion level..
        // .. for now nothing to do here.

        return matchFound;
    } // function

}
