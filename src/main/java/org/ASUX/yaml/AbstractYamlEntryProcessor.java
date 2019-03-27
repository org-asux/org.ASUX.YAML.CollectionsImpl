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

/** <p>This abstract class was written to re-use code to query/traverse a YAML file.</p>
 *  <p>This org.ASUX.yaml GitHub.com project and the <a href="https://github.com/org-asux/org.ASUX.cmdline">org.ASUX.cmdline</a> GitHub.com projects, would
 *  simply NOT be possible without the genius Java library <a href="https://github.com/EsotericSoftware/yamlbeans">"com.esotericsoftware.yamlbeans"</a>.</p>
 *  <p>This abstract class has 4 concrete sub-classes (representing YAML-COMMANDS to read/query, list, delete and replace).</p>
 *  <p>See full details of how to use this, in {@link org.ASUX.yaml.Cmd} as well as the <a href="https://github.com/org-asux/org.ASUX.cmdline">org.ASUX.cmdline</a> GitHub.com project.</p>
 * @see org.ASUX.yaml.ReadYamlEntry
 * @see org.ASUX.yaml.ListYamlEntry
 * @see org.ASUX.yaml.DeleteYamlEntry
 * @see org.ASUX.yaml.ReplaceYamlEntry
*/
public abstract class AbstractYamlEntryProcessor {

    public static final String CLASSNAME = "com.esotericsoftware.yamlbeans.AbstractYamlEntryProcessor";

    /** <p>Whether you want deluge of debug-output onto System.out.</p><p>Set this via the constructor.</p>
     *  <p>It's read-only (final data-attribute).</p>
     */
    public final boolean verbose;

    /** The only Constructor.
     *  @param _verbose Whether you want deluge of debug-output onto System.out
     */
    public AbstractYamlEntryProcessor(boolean _verbose) {
        this.verbose = _verbose;
    }
    protected AbstractYamlEntryProcessor(){
        this.verbose = false;
    }

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

    /** <p>This function will be called when a partial match of a YAML path-expression happens.</p>
     * <p>Example: if the YAML-Path-regexp is <code>paths.*.*.responses.200.description</code></p>
     * <p>This function will be called for: <code>paths./pet   paths./pet.put   paths./pet.put.responses paths./pet.put.responses.200</code></p>
     * <p>Note: This function will NOT be invoked for a full/end2end match for <code>paths./pet.put.responses.200.description</code></p>
     * <p>That full/end2end match will trigger the other function "onEnd2EndMatch()".</p>
     *
     * <p>Do NOT fuck with (a.k.a alter) the contents of any of the parameters passed.   Use the parameters ONLY in Read-only manner.  Got itchy fingers?  Then, Deepclone both the parameters.  YAMLPath class has a static member-function to make it easy to deepClone.</p>
     *  @param _map This contains the java.utils.Map (created by com.esotericsoftware.yamlbeans library) containing the YAML SUB-tree (Note: Sub-tree) of the YAML file, as pointed to by "_yamlPath" and "_key".
     *  @param _yamlPath See the class YAMLPath @see org.ASUX.yaml.YAMLPath
     *  @param _key The value (typically a String) is what matched the _yamlPath.  Use it to get the "rhs" of the YAML element pointed to by _key
     *  @param _parentMap A Placeholder to be used in the future.  Right now it's = null
     *  @param _end2EndPaths for _yamlPathStr, this java.util.LinkedList shows the "stack of matches".   Example:  ["paths", "/pet", "get", "responses", "200"]
     *  @return The concrete sub-class can return false, to STOP any further progress on this partial match
     */
    protected abstract boolean onPartialMatch(final Map _map, final YAMLPath _yamlPath, final Object _key, final Map _parentMap, final LinkedList<String> _end2EndPaths);

    //-------------------------------------
    /** <p>This function will be called when a full/end2end match of a YAML path-expression happens.</p>
     * <p>Example: if the YAML-Path-regexp is <code>paths.*.*.responses.200.description</code></p>
     * <p>This function will be called ONLY for  <code>paths./pet.put.responses.200.description</code></p>
     * <p>Partial matches (of "parent yaml-elements") will trigger the other function "onPartialMatch()".</p>
     * <p>The words "onFullMatch" * "onCompleteMatch()" are confusing from user/regexp perspective.
     *  Hence the choice of onEnd2EndMath() as the function name.</p>
     *
     * <p>Do NOT fuck with (a.k.a alter) the contents of any of the parameters passed.   Use the parameters ONLY in Read-only manner.  Got itchy fingers?  Then, Deepclone both the parameters.  YAMLPath class has a static member-function to make it easy to deepClone.</p>
     *  @param _map This contains the java.utils.Map (created by com.esotericsoftware.yamlbeans library) containing the "bottom-most" YAML SUB-tree (Note: Sub-tree) of the YAML file, as pointed to by "_yamlPath" and "_key".  This map could be potentially represent a simple YAML element like "name: petid"
     *  @param _yamlPath See the class YAMLPath @see org.ASUX.yaml.YAMLPath
     *  @param _key The value (typically a String) is what matched the _yamlPath.  For "YAML Query", the "rhs" of the YAML element pointed to by _key is what you're looking for.  For "YAML Delete" or "YAML Replace", you do Not care about the "rhs".. just use the _key to remove the entry/replace the "rhs".
     *  @param _parentMap A Placeholder to be used in the future.  Right now it's = null
     *  @param _end2EndPaths for _yamlPathStr, this java.util.LinkedList shows the "stack of matches".   Example:  ["paths", "/pet", "get", "responses", "200"]
     *  @return The concrete sub-class can return false, to STOP any further progress on this partial match
     */
    protected abstract boolean onEnd2EndMatch(final Map _map, final YAMLPath _yamlPath, final Object _key, final Map _parentMap, final LinkedList<String> _end2EndPaths);

    //-------------------------------------
    /** <p>This function will be called whenever the YAML path-expression fails to match.</p>
     * <p>This will be called way too often.  It's only interesting if you want a "negative" match scenario (as in show all rows that do Not match)</p>
     *
     * <p>Do NOT fuck with (a.k.a alter) the contents of any of the parameters passed.   Use the parameters ONLY in Read-only manner.  Got itchy fingers?  Then, Deepclone both the parameters.  YAMLPath class has a static member-function to make it easy to deepClone.</p>
     * <p>Note: Unlike the other abstract methods of this Abstract class, this does NOT have a return-value.</p>
     *
     *  @param _map This contains the java.utils.Map (created by com.esotericsoftware.yamlbeans library) containing the YAML SUB-tree (Note: Sub-tree) of the YAML file (as pointed to by "_yamlPath" and "_key") - representing where PathPattern match failed.  This map could be potentially represent a simple YAML element like "name: petid"
     *  @param _yamlPath See the class YAMLPath @see org.ASUX.yaml.YAMLPath
     *  @param _key The value (typically a String) is what *FAILED* to match the _yamlPath.
     *  @param _parentMap A Placeholder to be used in the future.  Right now it's = null
     *  @param _end2EndPaths for _yamlPathStr, this java.util.LinkedList shows the "stack of matches".   Example:  ["paths", "/pet", "get", "responses", "200"]
     */
    protected abstract void onMatchFail(final Map _map, final YAMLPath _yamlPath, final Object _key, final Map _parentMap, final LinkedList<String> _end2EndPaths);

    //-------------------------------------
    /** <p>This function will be called when processing has ended.</p>
     *  <p>After this function returns, the AbstractYamlEntryProcessor class is done!</p>
     *
     *  <p>You can fuck with the contents of any of the parameters passed, to your heart's content.</p>
     *
     *  @param _map This contains the java.utils.Map (created by com.esotericsoftware.yamlbeans library) containing the entire Tree representing the YAML file.
     *  @param _yamlPath See the class YAMLPath @see org.ASUX.yaml.YAMLPath
     */
    protected abstract void atEndOfInput(final Map _map, final YAMLPath _yamlPath);

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

    /** <p>Internal Note: This is <b>NOT NOT NOT NOT NOT</b> ........ a RECURSIVE-FUNCTION.</p>
     *  <p>This is a simple way to invoke the real-recursive function {@link #recursiveSearch}.</p>
     *  @param _map This contains the java.utils.Map (created by com.esotericsoftware.yamlbeans library) containing the entire Tree representing the YAML file.
     *  @param _yamlPathStr Example: "<code>paths.*.*.responses.200</code>" - <b>ATTENTION: This is a human readable pattern, NOT a proper RegExp-pattern</b>
     *  @return true = whether at least one match happened.
     *  @throws com.esotericsoftware.yamlbeans.YamlException - this is thrown by the library com.esotericsoftware.yamlbeans
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

    /** <p>This is a RECURSIVE-FUNCTION.  Make sure to pass in the right parameters.</p>
     *  <p><b>Don't tell me I did NOT warn you!</b>  Use the {@link searchYamlForPattern} function instead.</p>
     *  <p>This function returns true, if the invocation (or it's recursion) did find a match (partial or end2end).<br>
     *  For now, I'm Not using the return value ANYWHERE.   Either I will - or - will refactor the return as Void.</p>
     *  @param _map This contains the java.utils.Map (created by com.esotericsoftware.yamlbeans library) containing the entire Tree representing the YAML file.
     *  @param _yamlPath This is the {@link YAMLPath} class consstructed using example strings like "<code>paths.*.*.responses.200</code>" - <b>ATTENTION: This string is a human readable pattern, NOT a proper RegExp-pattern</b>
     *  @param _end2EndPaths for _yamlPathStr, this java.util.LinkedList shows the "stack of matches".   Example:  ["paths", "/pet", "get", "responses", "200"]
     *  @return true = whether at least one match happened.
     *  @throws com.esotericsoftware.yamlbeans.YamlException - this is thrown by the library com.esotericsoftware.yamlbeans
     */
    public boolean recursiveSearch(Map _map, final YAMLPath _yamlPath, final LinkedList<String> _end2EndPaths )
    throws com.esotericsoftware.yamlbeans.YamlException {
        if ( _map == null || (! _yamlPath.isValid) ) return false;
        if ( ! _yamlPath.hasNext() ) return false; // YAML path has ended

        //--------------------------
        final String yamlPathElemStr = _yamlPath.get(); // current path-element (a substring of full yamlPath)

        boolean aMatchFound = false;

        //--------------------------
        for (Object key : _map.keySet()) {

            final Object rhs = _map.get(key);  // otherwise we'll inefficiently be doing map.get multiple times below.
            final String rhsStr = rhs.toString(); // to make verbose logging code simplified

            if ( this.verbose ) System.out.println ( "\n"+ CLASSNAME +": "+ key +": "+ rhsStr.substring(0,rhsStr.length()>181?180:rhsStr.length()) );

            //-----------------
            boolean hasThisYamlLineMatched = false;
            boolean hasThisYamlLineLiterallyMatched = ! yamlPathElemStr.equals("**");
            if ( yamlPathElemStr.equals("**") ) {
                hasThisYamlLineLiterallyMatched = false; // redundant
                hasThisYamlLineMatched = true;
            } else {
                final Pattern yamlPElemPatt = java.util.regex.Pattern.compile(yamlPathElemStr); // This should Not throw, per precautions in YAMLPath class
                hasThisYamlLineLiterallyMatched = yamlPElemPatt.matcher(key.toString()).matches();
                hasThisYamlLineMatched = hasThisYamlLineLiterallyMatched;
            }
            // One more check: If current YamlLine's key did NOT match, but is there a "**" for a "greedy-match"
            if ( ! hasThisYamlLineMatched && _yamlPath.hasWildcardPrefix() ) {
                hasThisYamlLineMatched = true;
                hasThisYamlLineLiterallyMatched = false;
            }

            if ( hasThisYamlLineMatched ) {
                if ( this.verbose ) System.out.println(CLASSNAME + ": @# " + _yamlPath.index() +"\t"+ _yamlPath.getPrefix() +"\t"+ _yamlPath.get() +"\t"+ _yamlPath.getSuffix() + "\t matched("+ hasThisYamlLineLiterallyMatched+ ") '"+ key +"':\t"+ rhsStr.substring(0,rhsStr.length()>121?120:rhsStr.length()) +"\t\t of type '"+rhs.getClass().getName() +"'");

                _end2EndPaths.add( key.toString() ); // _end2EndPaths keeps the breadcrumbs

                //------------------------------------------------------
                assert (_yamlPath.hasNext()); // why on earth would this assertion fail - see checks @ top of function.

                final YAMLPath cloneOfYAMLPath = YAMLPath.deepClone(_yamlPath); // to keep _yamlPath intact as we recurse in & out of sub-yaml-elements
                if (  hasThisYamlLineLiterallyMatched ||  !  _yamlPath.hasWildcardPrefix() )
                    cloneOfYAMLPath.next(); // _yamlPath.get() should continue to have "**" as previous element.

                @SuppressWarnings("unchecked")
                final LinkedList<String> cloneOfE2EPaths = (LinkedList<String>) _end2EndPaths.clone();

                if ( this.verbose ) System.out.println(CLASSNAME + ": @ whether to recurse: deepcloned YamlPath @# " + cloneOfYAMLPath.index() +"\t"+ cloneOfYAMLPath.getPrefix() +"\t"+ cloneOfYAMLPath.get() +"\t"+ cloneOfYAMLPath.getSuffix() +" -- cloneOfYP.hasNext()='"+ cloneOfYAMLPath.hasNext() +"'  _yamlPath.hasWildcardPrefix()='"+ _yamlPath.hasWildcardPrefix() +"'");

                if ( ! cloneOfYAMLPath.hasNext() ) {
                    // NO more recursion feasible!
                    // well! we've matched end2end .. to a "Map" element (instead of String elem)!

                    // let sub-classes determine what to do here
                    final boolean callbkRet3 = onEnd2EndMatch(_map, _yamlPath, key, null, cloneOfE2EPaths); // location #1 for end2end match
                    if ( ! callbkRet3 ) continue; // Pretend as if match failed.
                    _end2EndPaths.removeLast();

                    if ( this.verbose ) System.out.println(CLASSNAME +": End2End Match#1 in YAML-file: "+ _yamlPath.getPrefix() +" "+ key  +":\t"+  rhsStr.substring(0,rhsStr.length()>121?120:rhsStr.length()) +"\t\t type '"+rhs.getClass().getName() +"'");

                    continue; // outermost for-loop (Object key : _map.keySet())
                }
                //else .. continue below.

                //------------------------------------------------------
                // If we're here, it means INCOMPLETE match..

                // let sub-classes determine what to do here
                final boolean callbkRet2 = onPartialMatch(_map, _yamlPath, key, _map, _end2EndPaths );
                if ( ! callbkRet2 ) continue; // If so, STOP  any further matching DOWN/BENEATH that partial-match

                if ( this.verbose ) System.out.println(CLASSNAME + ": recursing with YAMLPath @# " + cloneOfYAMLPath.index() +"\t"+ cloneOfYAMLPath.getPrefix() +"\t"+ cloneOfYAMLPath.get() +"\t"+ cloneOfYAMLPath.getSuffix() +": ... @ YAML-file-location: '"+ key +"': "+ rhsStr.substring(0,rhsStr.length()>121?120:rhsStr.length()));

                //--------------------------------------------------------
                // if we are here, we've only a PARTIAL match.
                // So.. we need to keep recursing (specifically for Map & ArrayList YAML elements)
                if ( rhs instanceof Map ) {

                    aMatchFound = this.recursiveSearch( (Map)rhs, cloneOfYAMLPath, cloneOfE2EPaths); // recursion call

                } else if ( rhs instanceof java.util.ArrayList ) {

                    ArrayList arr = (ArrayList) rhs;
                    for ( Object o: arr ) {
                        // iterate over each element?  Need a new variant of this function - to match "index"
                        if ( o instanceof Map ) { // Shouldn't this always be true - per YAML spec?
                            aMatchFound = this.recursiveSearch( (Map)o, cloneOfYAMLPath, cloneOfE2EPaths); // recursion call
                        } else if ( o instanceof java.lang.String ) {
                            // can't be a match, as it's Not even in the format   "rhs: lhs"
                            onMatchFail(_map, _yamlPath, key, _map, _end2EndPaths); // location #1 for failure-2-match
                        } else {
                            System.err.println(CLASSNAME +": incomplete code: failure w Array-type '"+ o.getClass().getName() +"'");
                            onMatchFail(_map, _yamlPath, key, _map, _end2EndPaths); // location #2 for failure-2-match
                        } // if-Else   o instanceof Map - (WITHIN FOR-LOOP)
                    } // for Object o: arr

                } else if ( rhs instanceof java.lang.String ) {
                    if ( _yamlPath.hasNext() ) { // then it's Not an end2end match
                        _end2EndPaths.removeLast();
                        continue;
                    } else {
                        // yeah! We found a full end2end match!  Also, No more recursion is feasible.
                        // let sub-classes determine what to do here
                        final boolean callbkRet5 = onEnd2EndMatch(_map, _yamlPath, key, null, cloneOfE2EPaths); // location #2 for end2end match
                        if ( this.verbose ) System.out.println(CLASSNAME +": callbkRet5="+callbkRet5+" End2End Match#2 @ YAML-File: "+ key +": "+ rhsStr.substring(0,rhsStr.length()>121?120:rhsStr.length()));
                        if ( ! callbkRet5 ) continue; // Pretend as if match failed and continue to next peer YAML element.
                        _end2EndPaths.clear();
                        aMatchFound = true;
                    }

                } else {
                    System.err.println(CLASSNAME +": incomplete code: Unable to handle rhs of type '"+ rhs.getClass().getName() +"'");
                    onMatchFail(_map, _yamlPath, key, _map, _end2EndPaths); // location #3 for failure-2-match

                } // if-else   rhs instanceof   Map/Array/String/.. ..

                // As we've had AT-LEAST a PARTIAL-MATCH, in CURRENT-ITERATION (of FOR-LOOP).. ..
                // we need to "undo" that for next iteration (of FOR) for the next-peer YAML-element
                if ( _end2EndPaths.size() > 0 )  _end2EndPaths.removeLast();
                
            } else {
                // false == foundAMatch  -- -- i.e., FAILED to match YAML-Path pattern.
                onMatchFail(_map, _yamlPath, key, _map, _end2EndPaths); // location #4 for failure-2-match
                
            }// if-else yamlPElemPatt.matcher()

        } // for loop   key: _map.keySet()

        // Now that we looped thru all keys at current recursion level..
        // .. for now nothing to do here.

        return aMatchFound;
    } // function

}
