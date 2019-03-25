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
import java.util.LinkedHashMap;

public class ListYamlEntry extends AbstractYamlEntryProcessor {

    public static final String CLASSNAME = "com.esotericsoftware.yamlbeans.ListYamlEntry";

    public final boolean verbose;
    public int count = 0;

    public ListYamlEntry(boolean _verbose) {
        this.verbose = _verbose;
        this.count = 0;
    }

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    
    /* This function will be called when a partial match of a YAML path-expression happens.
     * Example: if the YAML-Path-regexp is paths.*.*.responses.200.description
     * This function will be called for: paths./pet   paths./pet.put   paths./pet.put.responses paths./pet.put.responses.200
     * Note: This function will NOT be invoked for a full/end2end match at paths./pet.put.responses.200.description
     * That full/end2end match will trigger the other function "onEnd2EndMatch()".
     *
     * Do NOT fuck with (a.k.a alter) the contents of any of the parameters passed.   Use the parameters ONLY in Read-only manner.  Got itchy fingers?  Then, Deepclone both the parameters.  YAMLPath class has a static member-function to make it easy to deepClone.
     */
    boolean onPartialMatch(final Map _map, final YAMLPath _yamlPath, final Object _key, final Map _parentMap, final LinkedList<String> _end2EndPaths) {

        // Do Nothing for "delete YAML-entry command"
        return true;
    }

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
    boolean onEnd2EndMatch(final Map _map, final YAMLPath _yamlPath, final Object _key, final Map _parentMap, final LinkedList<String> _end2EndPaths) {

        this.count ++;
//        System.out.print("onEnd2EndMatch: _end2EndPaths =");
        _end2EndPaths.forEach( s -> System.out.print(s+"\t") );
        System.out.println("");

        return true;
    }

    //-------------------------------------
    /* This function will be called whenever the YAML path-expression fails to match.
     * This will be called way too often.  It's only interesting if you want a "negative" match scenario (as in show all rows that do Not match)
     *
     * Do NOT fuck with (a.k.a alter) the contents of any of the parameters passed.   Use the parameters ONLY in Read-only manner.  Got itchy fingers?  Then, Deepclone both the parameters.  YAMLPath class has a static member-function to make it easy to deepClone.
     */
    boolean onMatchFail(final Map _map, final YAMLPath _yamlPath, final Object _key, final Map _parentMap, final LinkedList<String> _end2EndPaths) {

        // Do Nothing for "delete YAML-entry command"
        return true;
    }

    //-------------------------------------
    /* This function will be called when processing has ended.
     * After this function returns, the AbstractYamlEntryProcessor class is done!
     *
     * You can fuck with the contents of any of the parameters passed, to your heart's content.
     */
    boolean atEndOfInput(final Map _map, final YAMLPath _yamlPath) {

        if ( this.verbose ) System.out.println("Total=" + this.count );
        return true;
    }

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

}
