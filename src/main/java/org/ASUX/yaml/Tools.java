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

// import java.io.StringBufferInputStream;
//import java.util.Map;
//import java.util.LinkedList;
//import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Properties;

//import java.util.regex.*;

import static org.junit.Assert.*;

/**
 *  <p>This org.ASUX.yaml GitHub.com project and the <a href="https://github.com/org-asux/org-ASUX.github.io/wiki">org.ASUX.cmdline</a> GitHub.com projects, would simply NOT be possible without the genius Java library <a href="https://github.com/EsotericSoftware/yamlbeans">"com.esotericsoftware.yamlbeans"</a>.</p>
 *  <p>This class is a bunch of tools to help make it easy to work with the java.util.Map objects that the EsotericSoftware library creates.</p>
 *  <p>One example is the work around required when replacing the 'Key' - within the MACRO command Processor.</p>
 *  <p>If the key is already inside single or double-quotes.. then the replacement ends up as <code>'"newkeystring"'</code></p>
 */
public class Tools {

    public static final String CLASSNAME = "org.ASUX.yaml.Tools";

    /** <p>Whether you want deluge of debug-output onto System.out.</p><p>Set this via the constructor.</p>
     *  <p>It's read-only (final data-attribute).</p>
     */
    public final boolean verbose;

    /** <p>The only constructor - public/private/protected</p>
     *  @param _verbose Whether you want deluge of debug-output onto System.out.
     */
    public Tools(boolean _verbose) {
        this.verbose = _verbose;
    }

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    /** As com.esotericsoftware.yamlBeans has some magic where Keys are NOT strings! ..
     *  In order for me to add new entries to the _map created by that library, I need to go thru hoops.
     *  @param _map a java.util.LinkedHashMap&lt;String, Object&gt; object
     *  @param _key the key in key-value pair
     *  @param _rhs the value(java.lang.Object) in the key-value pair
     */
    public void addStringEntry( LinkedHashMap<String, Object> _map, final String _key, final String _rhs) {

        final String quotedStr = _rhs.contains(":") ? "'"+_rhs+"'" : _rhs;
        // final String s = "{ "+ _key +": "+ quotedStr +" }";
        final String s = _key +": "+ quotedStr +"\n";
        try {
            @SuppressWarnings("deprecated")
            final java.io.InputStream is2 = null; // new java.io.StringBufferInputStream( s ); <<@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
            final java.io.Reader reader2 = new java.io.InputStreamReader(is2);
            @SuppressWarnings("unchecked")
            final LinkedHashMap<String, Object> tempMap = new com.esotericsoftware.yamlbeans.YamlReader(reader2).read(LinkedHashMap.class);
            if ( this.verbose ) System.out.println( CLASSNAME + ": created new Map [" + tempMap.toString() +"]" );

            _map.putAll( tempMap );

        } catch (com.esotericsoftware.yamlbeans.YamlException e) { // Warning: This must PRECEDE IOException, else compiler error.
            e.printStackTrace(System.err);
            System.err.println( CLASSNAME + ": Internal error: unable to enhance existing YAML object with ["+ s +"]");
            System.exit(101);
        } catch (java.io.IOException e) {
            e.printStackTrace(System.err);
            System.err.println( CLASSNAME + ": Failure to convert the String '" + _key +"' & "+ _rhs +"' into YAML.");
            System.exit(102);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.err.println( CLASSNAME + ": Unknown Internal error:.");
            System.exit(103);
        }
        
    } // function

    //----------------------------------------------------------
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    /** As com.esotericsoftware.yamlBeans has some magic where Keys are NOT strings! ..
     *  In order for me to add new entries to the _map created by that library, I need to go thru hoops.
     *  @param _map a java.util.LinkedHashMap&lt;String, Object&gt; object
     *  @param _key the key in key-value pair
     *  @param _rhs the value(java.lang.Object) in the key-value pair
     */
    public void addMapEntry( LinkedHashMap<String, Object> _map, final String _key, final Object _rhs) {

        // if ( _rhs instanceof String ) {
        //     System.err.println( CLASSNAME + ": Internal error: addMapEntry called with a String parameter ["+ _rhs.toString() +"]");
        //     System.exit(105);
        // }
        // com.esotericsoftware.yamlbeans.tokenizer.ScalarToken keyToken =
        //     new com.esotericsoftware.yamlbeans.tokenizer.ScalarToken( _key, false, '\'' );
        // _map.put( keyToken, _rhs );

        String s = null;
        try {
            final java.io.StringWriter strwrtr = new java.io.StringWriter();
            final com.esotericsoftware.yamlbeans.YamlWriter writer = 
                new com.esotericsoftware.yamlbeans.YamlWriter( strwrtr );
            writer.getConfig().writeConfig.setWriteClassname( com.esotericsoftware.yamlbeans.YamlConfig.WriteClassName.NEVER ); // I hate !org.pkg.class within YAML files.  So does AWS I believe.
            writer.write( _rhs );
            writer.close();
            strwrtr.flush();

            final String rhsstr = strwrtr.toString();
            // now insert 2 blanks/SPACE characters at begining of EACH line in rhsstr.
            // That way we can ensure thr YAML created in variable 's' below is RIGHTLY INDENTED
            String rhsstrIndented = "";
            // String[] lines = rhsstr.split(System.getProperty("line.separator"));
            final java.util.Scanner scanner = new java.util.Scanner( rhsstr );
            while (scanner.hasNextLine()) {
                final String line = scanner.nextLine();
                rhsstrIndented += "  "+ line + "\n"; // insert 2 blanks/SPACE characters in each line
            }
            scanner.close();

            s = _key +":\n"+ rhsstrIndented +"\n";
            // System.err.println( CLASSNAME + ": s = " + s +"\n\n" );

            final java.io.InputStream is2 = null; // new java.io.StringBufferInputStream( s ); <<@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
            final java.io.Reader reader2 = new java.io.InputStreamReader(is2);
            @SuppressWarnings("unchecked")
            final LinkedHashMap<String, Object> tempMap = new com.esotericsoftware.yamlbeans.YamlReader(reader2).read(LinkedHashMap.class);
            if ( this.verbose ) System.out.println( CLASSNAME + ": created new Map [" + tempMap.toString() +"]" );

            _map.putAll( tempMap );

        } catch (com.esotericsoftware.yamlbeans.YamlException e) { // Warning: This must PRECEDE IOException, else compiler error.
            e.printStackTrace(System.err);
            System.err.println( CLASSNAME + ": Internal error: unable to enhance existing YAML object with key='" + _key +"' & rhs='" + _rhs.toString() +"' & s = ["+ s +"]" );
            System.exit(101);
        } catch (java.io.IOException e) {
            e.printStackTrace(System.err);
            System.err.println( CLASSNAME + ": Failure to convert to YAML - with  key='" + _key +"' & rhs='" + _rhs.toString() +"' & s = ["+ s +"]" );
            System.exit(102);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.err.println( CLASSNAME + ": Unknown Internal error:.");
            System.exit(103);
        }
    }
}
