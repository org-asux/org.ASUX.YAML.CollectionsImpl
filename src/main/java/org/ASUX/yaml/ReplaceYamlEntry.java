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

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;

/** <p>This concrete class is minimalistic because I am re-using code to query/traverse a YAML file.   See it's parent-class {@link org.ASUX.yaml.AbstractYamlEntryProcessor}.</p>
 *  <p>This concrete class is part of a set of 4 concrete sub-classes (representing YAML-COMMANDS to read/query, list, delete and replace ).</p>
 *  <p>This class contains implementation for 4 "callbacks" - </p><ol><li> whenever there is partial match - on the way to a complete(a.k.a. end2end match) </li><li> whenever a full match is found </li><li> a match failed (which implies, invariably, to keep searching till end of YAML file - but.. is a useful callback if you are using a "negative" pattern to search for YAML elements) </li><li> done processing entire YAML file</li></ol>
 *  <p>This org.ASUX.yaml GitHub.com project and the <a href="https://github.com/org-asux/org.ASUX.cmdline">org.ASUX.cmdline</a> GitHub.com projects, would
 *  simply NOT be possible without the genius Java library <a href="https://github.com/EsotericSoftware/yamlbeans">"com.esotericsoftware.yamlbeans"</a>.</p>
 *  <p>See full details of how to use this, in {@link org.ASUX.yaml.Cmd} as well as the <a href="https://github.com/org-asux/org.ASUX.cmdline">org.ASUX.cmdline</a> GitHub.com project.</p>
 * @see org.ASUX.yaml.AbstractYamlEntryProcessor
 */
public class ReplaceYamlEntry extends AbstractYamlEntryProcessor {

    public static final String CLASSNAME = "org.ASUX.yaml.ReplaceYamlEntry";

    // Note: We need to remove the "old" - exactly as DeleteYamlEntry.java does.  Then we insert new value.
    protected final LinkedList<Tuple<Object,Map> > keys2bRemoved = new LinkedList<>();
    protected Object replacementData = "";
    //    protected final LinkedHashMap<Object,Map> replDataMap; // = new LinkedHashMap<>();

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    public class Tuple<X, Y> {
        public final X key;
        public final Y map;
        public Tuple(X _k, Y _m) {
            this.key = _k;
            this.map = _m;
        }
    }
    
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    /** The only Constructor.
     *  @param _verbose Whether you want deluge of debug-output onto System.out
     *  @param _r this can be either a java.lang.String or a java.util.Map (created by com.esotericsoftware.yamlbeans)
     *  @throws java.lang.Exception - if the _r parameter is not as per above Spec
     */
    public ReplaceYamlEntry(boolean _verbose, Object _r ) throws Exception {
        super(_verbose);
        if ( _r == null )
            throw new Exception( CLASSNAME + ": _r parameter is Null");
        if ( ! (_r instanceof java.lang.String) && ! (_r instanceof java.util.Map) )
            throw new Exception( CLASSNAME + ": Invalid _r parameter of type:" + _r.getClass().getName() + "'");

        this.replacementData = _r;
    }

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    
    /** This function will be called when a partial match of a YAML path-expression happens.
     * See details and warnings in {@link org.ASUX.yaml.AbstractYamlEntryProcessor#onPartialMatch}
     */
    protected boolean onPartialMatch(final Map _map, final YAMLPath _yamlPath, final Object _key, final Map _parentMap, final LinkedList<String> _end2EndPaths) {

        // Do Nothing for "Replace YAML-entry command"
        return true;
    }

    //-------------------------------------
    /** This function will be called when a full/end2end match of a YAML path-expression happens.
     * See details and warnings in {@link org.ASUX.yaml.AbstractYamlEntryProcessor#onEnd2EndMatch}
     */
    protected boolean onEnd2EndMatch(final Map _map, final YAMLPath _yamlPath, final Object _key, final Map _parentMap, final LinkedList<String> _end2EndPaths) {

//      if ( this.verbose ) {
//          System.out.print("onEnd2EndMatch: _end2EndPaths =");
            _end2EndPaths.forEach( s -> System.out.print(s+", ") );
            System.out.println("");
//      }
        this.keys2bRemoved.add( new Tuple<>(_key, _map) );
//      if ( this.verbose ) System.out.println("onE2EMatch: count="+this.keys2bRemoved.size());
        return true;
    }

    //-------------------------------------
    /** This function will be called whenever the YAML path-expression fails to match.
     * See details and warnings in {@link org.ASUX.yaml.AbstractYamlEntryProcessor#onMatchFail}
     */
    protected void onMatchFail(final Map _map, final YAMLPath _yamlPath, final Object _key, final Map _parentMap, final LinkedList<String> _end2EndPaths) {

        // Do Nothing for "Replace YAML-entry command"
    }

    //-------------------------------------
    /** This function will be called when processing has ended.
     * After this function returns, the AbstractYamlEntryProcessor class is done!
     * See details and warnings in {@link AbstractYamlEntryProcessor#atEndOfInput}
     *
     * You can fuck with the contents of any of the parameters passed, to your heart's content.
     */
    protected void atEndOfInput(final Map _map, final YAMLPath _yamlPath) {

        System.out.println("count=" + this.keys2bRemoved.size() );
        for (Tuple<Object,Map> tpl: this.keys2bRemoved ) {
            final String rhsStr = tpl.map.toString();
            if ( this.verbose ) System.out.println("atEndOfInput: "+ tpl.key +": "+ rhsStr.substring(0,rhsStr.length()>121?120:rhsStr.length()));
            tpl.map.remove(tpl.key);

            // Now put in a new entry - with the replacement data!
            tpl.map.put( tpl.key, ReplaceYamlEntry.deepClone(this.replacementData) );
            // If there are multiple matches.. then without deepclone, the EsotericSoftware
            // library, will use "&1" to define your 1st copy (in output) and put "*1" in
            // all other locations this replacement text WAS SUPPOSED have been :-(
        }
        // java's forEach never works if you are altering anything within the Lambda body
        // this.keys2bRemoved.forEach( tpl -> {tpl.map.remove(tpl.key); });
    }

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

    /** This deepClone function is unnecessary, if you can invoke org.apache.commons.lang3.SerializationUtils.clone(this)
     *  @param _orig what you want to deep-clone
     *  @return a deep-cloned copy, created by serializing into a ByteArrayOutputStream and reading it back (leveraging ObjectOutputStream)
     */
    public static Object deepClone(Object _orig) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(_orig);
            
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            return ois.readObject();
        } catch (IOException e) {
            e.printStackTrace(System.err);
            return null;
        } catch (ClassNotFoundException e) {
            e.printStackTrace(System.err);
            return null;
        }
    }

}
