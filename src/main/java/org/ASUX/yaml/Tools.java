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
import java.util.Properties;

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;

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

    private Tools() {
        this.verbose = false;
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    // see if you can have ths implement the interface BiConsumer<T,U>
    // https://docs.oracle.com/javase/8/docs/api/java/util/function/BiConsumer.html

    public static class Tuple<X, Y> implements java.io.Serializable {
        public final X key;
        public final Y val;
        public Tuple(X _k, Y _m) {
            this.key = _k;
            this.val = _m;
        }
        public String toString() { return this.key.toString() +"="+ this.val.toString(); }
    }

    public ArrayList< Tools.Tuple< String,String > >      getKVPairs( final Object _o ) {
        ArrayList< Tools.Tuple< String,String > > kvpairs = new ArrayList<>();
        final OutputObjectTypes typ = this.getOutputObjectType( _o );
        if ( typ != OutputObjectTypes.Type_KVPairs )
            return kvpairs; // as an empty ArrayList.

        @SuppressWarnings("unchecked")
        final LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>) _o;
        for ( String k: map.keySet() ) {
            kvpairs.add( new Tools.Tuple< String,String>(k, map.get(k).toString() ) );
        }
        return kvpairs; //unless o is an empty Map, this will have something in it.
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /** This function ensures the in-memory LinkedHashMap will work will all the YAML commands: read, list, replace, macro, ..
     *  I need such a function, as I learned the hard way that libraries do NOT work 100% well.  Only file-formats are the workaorund/ way out.
     *  I definitely "fgool-proof" method to ensure 'valid' YAML, for error-free processing by the entire org.ASUX.yaml library to work without any issues
     *  Note: Currently this function is identical to JSON2YAML()!
     *  @param _json a java.util.LinkedHashMap&lt;String, Object&gt; object, as generated by Jackson http://tutorials.jenkov.com/java-json/jackson-objectmapper.html#read-map-from-json-string 
     *  @return a java.util.LinkedHashMap&lt;String, Object&gt; object that's definitely "kosher" for the entire org.ASUX.yaml library to work without any issues
     * @throws com.fasterxml.jackson.core.JsonProcessingException if any error using Jackson library
     * @throws com.esotericsoftware.yamlbeans.YamlException if unable to convert into LinkedHashMap per com.esotericsoftware.yamlbeans library
     * @throws java.io.IOException if any error using java.io.StringReader and java.io.StringWriter
     * @throws Exception any other run-time exception, while parsing large Strings, nullpointers, etc.. ..
     */
    public LinkedHashMap<String, Object>  lintRemover( final LinkedHashMap<String, Object> _json )
                    throws com.esotericsoftware.yamlbeans.YamlException, com.fasterxml.jackson.core.JsonProcessingException,
                            java.io.IOException, Exception
    {
        return JSON2YAML( _json );
    }

    /** This function ensures the String form of JSON will work will all the YAML commands: read, list, replace, macro, ..
     *  I need such a function, as I learned the hard way that libraries do NOT work 100% well.  Only file-formats are the workaorund/ way out.
     *  I definitely "fgool-proof" method to ensure 'valid' YAML, for error-free processing by the entire org.ASUX.yaml library to work without any issues
     *  Note: Currently this function is identical to JSON2YAML()!
     *  @param _jsonStr java.lang.String object
     *  @return a java.util.LinkedHashMap&lt;String, Object&gt; object that's definitely "kosher" for the entire org.ASUX.yaml library to work without any issues
     * @throws com.fasterxml.jackson.core.JsonProcessingException if any error using Jackson library
     * @throws com.esotericsoftware.yamlbeans.YamlException if unable to convert into LinkedHashMap per com.esotericsoftware.yamlbeans library
     * @throws java.io.IOException if any error using java.io.StringReader and java.io.StringWriter
     * @throws Exception any other run-time exception, while parsing large Strings, nullpointers, etc.. ..
     */
    public LinkedHashMap<String, Object>  lintRemover( final String  _jsonStr )
                    throws com.esotericsoftware.yamlbeans.YamlException, com.fasterxml.jackson.core.JsonProcessingException,
                            java.io.IOException, Exception
    {
        return JSONString2YAML( _jsonStr );
    }

    //-----------------------------------------------------------------------------------------
    /** String output variant of lintRemover(String).
     *  @param _jsonString java.lang.String object
     *  @return a String object that's definitely "kosher" for the entire org.ASUX.yaml library to work without any issues
     * @throws com.fasterxml.jackson.core.JsonProcessingException if any error using Jackson library
     * @throws com.esotericsoftware.yamlbeans.YamlException if unable to convert into LinkedHashMap per com.esotericsoftware.yamlbeans library
     * @throws java.io.IOException if any error using java.io.StringReader and java.io.StringWriter
     * @throws Exception any other run-time exception, while parsing large Strings, nullpointers, etc.. ..
     */
    public String  lintRemoverString( final String  _jsonString )
                    throws com.esotericsoftware.yamlbeans.YamlException, com.fasterxml.jackson.core.JsonProcessingException,
                            java.io.IOException, Exception
    {
        final LinkedHashMap<String, Object> map = JSONString2YAML( _jsonString );
        return this.YAML2JSONString(map);
    }

    //-----------------------------------------------------------------------------------------
    /** Convert an in-memory YAML into a JSON-compatible string.
     *  @param _map a java.util.LinkedHashMap&lt;String, Object&gt; object, as generated by Jackson http://tutorials.jenkov.com/java-json/jackson-objectmapper.html#read-map-from-json-string 
     *  @return a String object that's definitely "kosher" for the entire org.ASUX.yaml library to work without any issues
     * @throws com.fasterxml.jackson.core.JsonProcessingException if any error using Jackson library
     * @throws com.esotericsoftware.yamlbeans.YamlException if unable to convert into LinkedHashMap per com.esotericsoftware.yamlbeans library
     * @throws java.io.IOException if any error using java.io.StringReader and java.io.StringWriter
     * @throws Exception any other run-time exception, while parsing large Strings, nullpointers, etc.. ..
     */
    public String YAML2JSONString( final LinkedHashMap<String, Object>  _map )
                    throws com.esotericsoftware.yamlbeans.YamlException, com.fasterxml.jackson.core.JsonProcessingException,
                            java.io.IOException, Exception
    {
        com.fasterxml.jackson.databind.ObjectMapper objMapper = new com.fasterxml.jackson.databind.ObjectMapper();
        // final java.io.StringWriter strwrtr4 = new java.io.StringWriter();
        // mapper.writeValue( strwrtr4, wellFormedJSONString );
        // strwrtr4.close();
        String s = objMapper.writeValueAsString(_map);
        s = s.replaceAll(":", ": ");
        return s;
    }

    //-----------------------------------------------------------------------------------------
    /** Takes any JSON input - as a LinkedHashmap - and exports it as YAML (to java.util.String), and then reads it back as YAML.
     *  I need such a function, as I learned the hard way that libraries do NOT work 100% well.  Only file-formats are the workaorund/ way out.
     *  I definitely "fgool-proof" method to ensure 'valid' YAML, for error-free processing by the entire org.ASUX.yaml library to work without any issues
     *  @param _json a java.util.LinkedHashMap&lt;String, Object&gt; object, as generated by Jackson http://tutorials.jenkov.com/java-json/jackson-objectmapper.html#read-map-from-json-string 
     *  @return a java.util.LinkedHashMap&lt;String, Object&gt; object that's definitely "kosher" for the entire org.ASUX.yaml library to work without any issues
     */
    public String JSON2String( final LinkedHashMap<String, Object> _json )
    {
        // First write it to java.lang.String object... then, read it back into YAML, using the com.esotericsoftware.yamlbeans.YamlReder class

        final java.io.StringWriter strwrtr3 = new java.io.StringWriter();
        try {
            com.esotericsoftware.yamlbeans.YamlWriter writer3 = new com.esotericsoftware.yamlbeans.YamlWriter( strwrtr3 );
            defaultConfigurationForYamlWriter( writer3 );
            writer3.write( _json );
            writer3.close();
            if ( this.verbose ) System.out.println( CLASSNAME + ": JSON2YAML(): created new YAML-String\n" + strwrtr3.toString() +"\n" );

            return strwrtr3.toString();

        } catch (com.esotericsoftware.yamlbeans.YamlException e) { // Warning: This must PRECEDE IOException, else compiler error.
            e.printStackTrace(System.err);
            System.err.println( CLASSNAME + ": JSON2YAML(): Internal error: unable to enhance existing YAML object with ["+ strwrtr3.toString() +"]");
            System.exit(101);
        } catch (java.io.IOException e) {
            e.printStackTrace(System.err);
            System.err.println( CLASSNAME + ": JSON2YAML(): Failure to read/write the contents of the String '" + strwrtr3.toString() +"'.");
            System.exit(102);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.err.println( CLASSNAME + ": JSON2YAML(): Unknown Internal error:.");
            System.exit(103);
        }
        return null;
    } // function

    //-----------------------------------------------------------------------------------------
    /** Takes any JSON input - as a LinkedHashmap - and exports it as YAML (to java.util.String), and then reads it back as YAML.
     *  I need such a function, as I learned the hard way that libraries do NOT work 100% well.  Only file-formats are the workaorund/ way out.
     *  I definitely "fgool-proof" method to ensure 'valid' YAML, for error-free processing by the entire org.ASUX.yaml library to work without any issues
     *  @param _json a java.util.LinkedHashMap&lt;String, Object&gt; object, as generated by Jackson http://tutorials.jenkov.com/java-json/jackson-objectmapper.html#read-map-from-json-string 
     *  @return a java.util.LinkedHashMap&lt;String, Object&gt; object that's definitely "kosher" for the entire org.ASUX.yaml library to work without any issues
     */
    public LinkedHashMap<String, Object> JSON2YAML( final LinkedHashMap<String, Object> _json )
    {
        // First write it to java.lang.String object... then, read it back into YAML, using the com.esotericsoftware.yamlbeans.YamlReder class

        final java.io.StringWriter strwrtr3 = new java.io.StringWriter();
        try {
            // com.esotericsoftware.yamlbeans.YamlWriter writer3 = new com.esotericsoftware.yamlbeans.YamlWriter( strwrtr3 );
            // defaultConfigurationForYamlWriter( writer3 );
            // writer3.write( _json );
            // writer3.close();
            // if ( this.verbose ) System.out.println( CLASSNAME + ": JSON2YAML(): created new YAML-String\n" + strwrtr3.toString() +"\n" );

            return JSONString2YAML( JSON2String(_json) );

        } catch (com.esotericsoftware.yamlbeans.YamlException e) { // Warning: This must PRECEDE IOException, else compiler error.
            e.printStackTrace(System.err);
            System.err.println( CLASSNAME + ": JSON2YAML(): Internal error: unable to enhance existing YAML object with ["+ strwrtr3.toString() +"]");
            System.exit(101);
        } catch (java.io.IOException e) {
            e.printStackTrace(System.err);
            System.err.println( CLASSNAME + ": JSON2YAML(): Failure to read/write the contents of the String '" + strwrtr3.toString() +"'.");
            System.exit(102);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.err.println( CLASSNAME + ": JSON2YAML(): Unknown Internal error:.");
            System.exit(103);
        }
        return null;
    } // function


    /**
     *  Takes any STRING-form JSON as input - it better be valid JSON - and reads it back as YAML/LinkedHashMap.
     *  I need such a function, as I learned the hard way that libraries do NOT work 100% well.  Only file-formats are the workaorund/ way out.
     *  I definitely "fgool-proof" method to ensure 'valid' YAML, for error-free processing by the entire org.ASUX.yaml library to work without any issues
     *  @param _jsonString a java.lang.String object
     *  @return a java.util.LinkedHashMap&lt;String, Object&gt; object that's definitely "kosher" for the entire org.ASUX.yaml library to work without any issues
     * @throws com.esotericsoftware.yamlbeans.YamlException if unable to convert into LinkedHashMap per com.esotericsoftware.yamlbeans library
     * @throws java.io.IOException if any error using java.io.StringReader and java.io.StringWriter
     * @throws Exception any other run-time exception, while parsing large Strings, nullpointers, etc.. ..
     */
    public LinkedHashMap<String, Object>  JSONString2YAML( final String  _jsonString )
                    throws com.esotericsoftware.yamlbeans.YamlException, java.io.IOException, Exception
    {
        String wellFormedJSONString = _jsonString;
        if ( _jsonString.contains("=") && ! _jsonString.contains(":") ) {
            // WEll! it means the entire string in Key=Value format.   Not in proper Key:Value JSON format.
            wellFormedJSONString = _jsonString.replaceAll("=", ": "); // fingers crossed. I hope this works.
        } else {
            wellFormedJSONString = _jsonString.replaceAll(":", ": "); // Many libraries do NOT like  'key:value'.  They want a blank after colon like 'key: value'
        }
        if ( this.verbose ) System.out.println(">>>>>>>>>>>>>>>>>>>> "+CLASSNAME+": JSONString2YAML(): "+ wellFormedJSONString);

        final java.io.StringWriter strwrtr3 = new java.io.StringWriter();
        final java.io.Reader reader3 = new java.io.StringReader( wellFormedJSONString );
        @SuppressWarnings("unchecked")
        final LinkedHashMap<String, Object> tempMap = new com.esotericsoftware.yamlbeans.YamlReader(reader3).read(LinkedHashMap.class);
        reader3.close();
        if ( this.verbose ) System.out.println( CLASSNAME + ": JSONString2YAML(): created new Map [" + tempMap.toString() +"]" );

        return tempMap;
    } // function

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    public static final String ASUXKEYWORDFORWRAPPER = "ASUX.output.";
    public static final String ARRAYWRAPPED = ASUXKEYWORDFORWRAPPER+"array";
    public static final String LISTWRAPPED = ASUXKEYWORDFORWRAPPER+"list";
    public static final String SINGLESTRINGWRAPPED = ASUXKEYWORDFORWRAPPER+"singleString";

    public LinkedHashMap<String, Object> wrapAnObject_intoLinkedHashMap( final Object _output ) throws Exception
    {
        if ( _output instanceof String ) {
            @SuppressWarnings("unchecked")
            final String s = ( String ) _output;
            LinkedHashMap<String, Object> retMap = new LinkedHashMap<String, Object>();
            retMap.put( LISTWRAPPED, s );
            retMap = new Tools(this.verbose).lintRemover(retMap);
            return retMap;
        } else if ( _output instanceof LinkedList ) {
            @SuppressWarnings("unchecked")
            final LinkedList<Object> list = ( LinkedList<Object> ) _output;
            // list.forEach( s -> System.out.println( s.toString() ) );
            LinkedHashMap<String, Object> retMap = new LinkedHashMap<String, Object>();
            retMap.put( ARRAYWRAPPED, list );
            retMap = new Tools(this.verbose).lintRemover(retMap);
            return retMap;

        } else if ( _output instanceof ArrayList ) {
            @SuppressWarnings("unchecked")
            final ArrayList<String> arr = ( ArrayList<String> ) _output;
            LinkedHashMap<String, Object> retMap = new LinkedHashMap<String, Object>();
            retMap.put( LISTWRAPPED, arr );
            retMap = new Tools(this.verbose).lintRemover(retMap);
            return retMap;

        } else if ( _output instanceof LinkedHashMap) {
            @SuppressWarnings("unchecked")
            final LinkedHashMap<String, Object> retMap = (LinkedHashMap<String, Object>) _output;
            // I do Not need to run Tools().lintRemover() as the _output of org.ASUX.yaml library stays 100% conformant withe com.esotericsoftware library usage.
            return retMap;

        } else {
            throw new Exception ( CLASSNAME +": processAnyCommand(): _output is Not of type LinkedHashMap.  It's ["+ ((_output==null)?"null":_output.getClass().getName()) +"]");
        }
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================


    public static enum OutputObjectTypes { Type_Unknown, Type_String, Type_LinkedHashMap, Type_ArrayList, Type_KVPair, Type_KVPairs, Type_LinkedList };
                                                            // 'KVPair' has No 's' character @ end.  While 'KVPairs' does.



    public OutputObjectTypes getOutputObjectType( Object o )
    {
        if ( o == null ) return OutputObjectTypes.Type_Unknown;
        if ( o instanceof String ) return  OutputObjectTypes.Type_String;
        if ( o instanceof Tuple ) return  OutputObjectTypes.Type_KVPair; // singular;  No 's' character @ end.  This is Not KVPairs
        if ( o instanceof ArrayList ) return  OutputObjectTypes.Type_ArrayList;
        if ( o instanceof LinkedList ) return  OutputObjectTypes.Type_LinkedList;
        if ( o instanceof LinkedHashMap ) {

            @SuppressWarnings("unchecked")
            final LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>) o;
            if (map.keySet().size() <= 0) return OutputObjectTypes.Type_LinkedHashMap; // This is the only unclear scenario

            if ( map.keySet().size() > 1 ) {
                if ( this.verbose ) System.out.println( CLASSNAME +": getOutputObjectType(): checking whether a Map is KVPairs/Plural.. for "+ map.toString() );
                // check to see if the LinkedHashMap is just 1-level deep with NOTHING but String:String Key-value pairs.
                // That is, the LinkedHashMap has NO NESTING.
                boolean bOnlyKVPairs = true; // guilty until proven innocent.
                for ( String k: map.keySet() ) {
                    if ( map.get(k) instanceof String ) {
                        continue;
                    } else {
                        bOnlyKVPairs = false;
                        break;
                    }
                }
                if ( this.verbose ) System.out.println( CLASSNAME +": getOutputObjectType(): .. .. .. .. it turns out that .. bOnlyKVPairs= "+ bOnlyKVPairs );
                if ( bOnlyKVPairs )
                    return OutputObjectTypes.Type_KVPairs; // PLURAL;  Note the 's' character @ end.  This is Not KVPair (singular)
                else
                    return OutputObjectTypes.Type_LinkedHashMap; // Its a big goop of data at at least 1 level of NESTED Maps inside it.

            } else {
                // assert:   map.keySet().size() == 1 exactly
                final String k = map.keySet().iterator().next();
                final Object o1 = map.get(k);
                if ( o1 instanceof String ) return  OutputObjectTypes.Type_String;
                if ( o1 instanceof ArrayList ) return  OutputObjectTypes.Type_ArrayList;
                if ( o1 instanceof LinkedList ) return  OutputObjectTypes.Type_LinkedList;
                if ( o1 instanceof LinkedHashMap ) return  OutputObjectTypes.Type_LinkedHashMap;
                return OutputObjectTypes.Type_Unknown;
            }
        }
        return OutputObjectTypes.Type_Unknown;
    }

    //--------------------------------
    /**
     * THis function exists.. as much of ths org.ASUX.yaml library requires a LinkedHashMap object.
     * So, any "sources" that bring in a "ArrayList", "LinkedList" or even a Scalar "String" object.. are put into a simple LinkedHashMap "wrapper"!
     * So.. this function exists to automatically "unwrap" that wrapper (if that's the case), or to return the object as-is (if that is Not the case)
     * @param _o the object we'd like to get the "real" value of
     * @return "unwrap" that simple LinkedHashMap wrapper (if that's the case), or to return the object as-is (if that is Not the case)
     * @throws Exception if unknown type, other than "LinkedHashMap", "ArrayList", "LinkedList" or even a Scalar "java.lang.String"
     */
    public Object getTheActualObject( final Object _o ) throws Exception
    {
        if ( _o == null ) return null;
        // final Tools.OutputObjectTypes typ = this.getOutputObjectType( _o );
        if ( _o instanceof String ) {
            return _o;
        } else if ( _o instanceof LinkedList ) {
            return _o;
        } else if ( _o instanceof ArrayList ) {
            return _o;
        } else if ( _o instanceof LinkedHashMap) {
// SHOULD I run Tools().lintRemover() to ensure this object stays 100% conformant withe com.esotericsoftware library usage.???
            // let's check if this LinkedHashMap is a 'wrapper' for an object that is Not a LinkedHashMap.
            @SuppressWarnings("unchecked")
            final LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>) _o;
// System.out.println(">>>>>>>>>>>> Tools.getTheActualObject(_o): "+_o.toString()+" <<<<<<<");
            if ( map.keySet().size() <= 0 ) {
                return _o; // This is the only unclear scenario.  Perhaps an empty result from a previous command in YAML-Batch?
            } else if ( map.keySet().size() > 1 ) {
                return _o; // return the originally passed object AS-IS.  It's a full-blown LinkedHashMap!
            } else {
                // assert:   map.keySet().size() == 1 exactly
                // Implies this LinkedHashMap is very likely a SIMPLE Wrapper.
                final String k = map.keySet().iterator().next();
                if ( k.startsWith( ASUXKEYWORDFORWRAPPER ) ) {
                    final Object o1 = map.get(k);
                    return o1;
                } else {
                    return _o; // well.. .. we almost mistook it for a "wrapper"
                }
            }
        } else {
            throw new Exception( CLASSNAME +": getTheActualObject(): ERROR: unknown object of type ["+ _o.getClass().getName() +"]");
        }
    }
    //------------------------------------------------------------
    public ArrayList<String> getArrayList( final Object _o ) throws Exception {
        if ( _o == null ) return null;
        final Tools.OutputObjectTypes typ = this.getOutputObjectType( _o );
        final Object o1 = this.getTheActualObject( _o );
// System.out.println(">>>>>>>>>>>>>>>>>> Tools.getArrayList(_o): "+_o.toString()+" <<<<<<<");
// System.out.println(">>>>>>>>>>>>>>>>>> Tools.getArrayList(o1): of type ["+o1.getClass().getName()+"] = "+o1.toString()+" <<<<<<<");
        switch(typ) {
            case Type_KVPairs:
            case Type_ArrayList:
                @SuppressWarnings("unchecked")
                final ArrayList<String> arr = (ArrayList<String>) o1;
                return arr;
            case Type_LinkedHashMap:
                    if ( o1 instanceof ArrayList ) {
                        @SuppressWarnings("unchecked")
                        final ArrayList<String> arr2 = (ArrayList<String>) o1;
                        return arr2;
                    } else {
                        throw new Exception ( CLASSNAME +": getArrayList(): LinkedHashMap with just 1 entry has object NOT of ArrayList type: "+ o1.getClass().getName() );
                    }
                    // break;
            case Type_String:
            case Type_KVPair:
            case Type_LinkedList:
            case Type_Unknown:
            default:
                throw new Exception( CLASSNAME +": getArrayList(): ERROR: unknown object of type ["+ _o.getClass().getName() +"]" );
        } // switch
    }

    //------------------------------------------------------------
    public LinkedList<String> getLinkedList( final Object _o ) throws Exception {
        if ( _o == null ) return null;
        final Tools.OutputObjectTypes typ = this.getOutputObjectType( _o );
        switch(typ) {
            case Type_LinkedList:
                @SuppressWarnings("unchecked")
                final LinkedList<String> list = (LinkedList<String>) this.getTheActualObject( _o );
                return list;
            case Type_LinkedHashMap:
                    final Object o1 = this.getTheActualObject( _o );
                    if ( o1 instanceof LinkedList ) {
                        @SuppressWarnings("unchecked")
                        final LinkedList<String> list2 = (LinkedList<String>) o1;
                        return list2;
                    } else {
                        throw new Exception ( CLASSNAME +": getLinkedList(): LinkedHashMap with just 1 entry has object NOT of LinkedList type: "+ o1.getClass().getName() );
                    }
                    // break;
            case Type_String:
            case Type_KVPair:
            case Type_KVPairs:
            case Type_ArrayList:
            case Type_Unknown:
            default:
                throw new Exception( CLASSNAME +": getLinkedList(): ERROR: unknown object of type ["+ _o.getClass().getName() +"]" );
        } // switch
    }

    //------------------------------------------------------------
    public String getString( Object _o ) throws Exception {
        if ( _o == null ) return null;
        final Tools.OutputObjectTypes typ = this.getOutputObjectType( _o );
        switch(typ) {
            case Type_String:
                @SuppressWarnings("unchecked")
                final String s = (String) this.getTheActualObject( _o );
                return s;
            case Type_LinkedHashMap:
                    final Object o1 = this.getTheActualObject( _o );
                    if ( o1 instanceof String ) {
                        @SuppressWarnings("unchecked")
                        final String s2 = (String) o1;
                        return s2;
                    } else {
                        throw new Exception ( CLASSNAME +": getLinkedList(): LinkedHashMap with just 1 entry has object NOT of String type: "+ o1.getClass().getName() );
                    }
                    // break;
            case Type_KVPair:
            case Type_KVPairs:
            case Type_LinkedList:
            case Type_ArrayList:
            case Type_Unknown:
            default:
                throw new Exception( CLASSNAME +": getLinkedList(): ERROR: unknown object of type ["+ _o.getClass().getName() +"]" );
        } // switch
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    public void printAllProps( final String _printPrefix, final LinkedHashMap<String,Properties> _AllProps ) {
        System.out.print( _printPrefix + " ... @@@@@@@@@@@@@@@@@@@@@@@@@ >> "); // _AllProps.forEach( (k, v) -> System.out.println(k + " = " + v.toString() ) );
        for ( String key : _AllProps.keySet() ) {
            System.out.print(key +" = ");
            final Properties p = (Properties)_AllProps.get(key);
            p.list(System.out);
        }
        System.out.println();
    }

    public static void defaultConfigurationForYamlWriter( com.esotericsoftware.yamlbeans.YamlWriter writer ) {
            // writer.getConfig().writeConfig.setWriteRootTags(false); // Does NOTHING :-
            writer.getConfig().writeConfig.setWriteClassname(com.esotericsoftware.yamlbeans.YamlConfig.WriteClassName.NEVER); // I hate !<pkg.className> within YAML files. So does AWS I believe.
            // writer.getConfig().writeConfig.setQuoteChar( cmdLineArgs.quoteType );
            // writer.getConfig().writeConfig.setQuoteChar( com.esotericsoftware.yamlbeans.YamlConfig.QuoteCharEnum.NONE );
            // writer.getConfig().writeConfig.setQuoteChar( com.esotericsoftware.yamlbeans.YamlConfig.QuoteCharEnum.SINGLEQUOTE );
            // writer.getConfig().writeConfig.setQuoteChar( com.esotericsoftware.yamlbeans.YamlConfig.QuoteCharEnum.DOUBLEQUOTE );
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

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
        } catch (java.io.IOException e) {
            e.printStackTrace(System.err);
            return null;
        } catch (ClassNotFoundException e) {
            e.printStackTrace(System.err);
            return null;
        }
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /** As com.esotericsoftware.yamlBeans has some magic where Keys are NOT strings! ..
     *  In order for me to add new entries to the _map created by that library, I need to go thru hoops.
     *  @param _map a java.util.LinkedHashMap&lt;String, Object&gt; object
     *  @param _key the key in key-value pair
     *  @param _rhs the value(java.lang.Object) in the key-value pair
     */
    // public void addStringEntry( LinkedHashMap<String, Object> _map, final String _key, final String _rhs) {

    //     final String quotedStr = _rhs.contains(":") ? "'"+_rhs+"'" : _rhs;
    //     // final String s = "{ "+ _key +": "+ quotedStr +" }";
    //     final String s = _key +": "+ quotedStr +"\n";
    //     try {
    //         final java.io.Reader reader2 = new java.io.StringReader( s );
    //         @SuppressWarnings("unchecked")
    //         final LinkedHashMap<String, Object> tempMap = new com.esotericsoftware.yamlbeans.YamlReader(reader2).read(LinkedHashMap.class);
    //         if ( this.verbose ) System.out.println( CLASSNAME + ": created new Map [" + tempMap.toString() +"]" );

    //         _map.putAll( tempMap );

    //     } catch (com.esotericsoftware.yamlbeans.YamlException e) { // Warning: This must PRECEDE IOException, else compiler error.
    //         e.printStackTrace(System.err);
    //         System.err.println( CLASSNAME + ": Internal error: unable to enhance existing YAML object with ["+ s +"]");
    //         System.exit(101);
    //     } catch (java.io.IOException e) {
    //         e.printStackTrace(System.err);
    //         System.err.println( CLASSNAME + ": Failure to convert the String '" + _key +"' & "+ _rhs +"' into YAML.");
    //         System.exit(102);
    //     } catch (Exception e) {
    //         e.printStackTrace(System.err);
    //         System.err.println( CLASSNAME + ": Unknown Internal error:.");
    //         System.exit(103);
    //     }
        
    // } // function

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /** As com.esotericsoftware.yamlBeans has some magic where Keys are NOT strings! ..
     *  In order for me to add new entries to the _map created by that library, I need to go thru hoops.
     *  @param _map a java.util.LinkedHashMap&lt;String, Object&gt; object
     *  @param _key the key in key-value pair
     *  @param _rhs the value(java.lang.Object) in the key-value pair
     */
    // public void addMapEntry( LinkedHashMap<String, Object> _map, final String _key, final Object _rhs) {

    //     // if ( _rhs instanceof String ) {
    //     //     System.err.println( CLASSNAME + ": Internal error: addMapEntry called with a String parameter ["+ _rhs.toString() +"]");
    //     //     System.exit(105);
    //     // }
    //     // com.esotericsoftware.yamlbeans.tokenizer.ScalarToken keyToken =
    //     //     new com.esotericsoftware.yamlbeans.tokenizer.ScalarToken( _key, false, '\'' );
    //     // _map.put( keyToken, _rhs );

    //     String s = null;
    //     try {
    //         final java.io.StringWriter strwrtr = new java.io.StringWriter();
    //         final com.esotericsoftware.yamlbeans.YamlWriter writer = 
    //             new com.esotericsoftware.yamlbeans.YamlWriter( strwrtr );
    //         writer.getConfig().writeConfig.setWriteClassname( com.esotericsoftware.yamlbeans.YamlConfig.WriteClassName.NEVER ); // I hate !org.pkg.class within YAML files.  So does AWS I believe.
    //         writer.write( _rhs );
    //         writer.close();
    //         strwrtr.flush();

    //         final String rhsstr = strwrtr.toString();
    //         // now insert 2 blanks/SPACE characters at begining of EACH line in rhsstr.
    //         // That way we can ensure thr YAML created in variable 's' below is RIGHTLY INDENTED
    //         String rhsstrIndented = "";
    //         // String[] lines = rhsstr.split(System.getProperty("line.separator"));
    //         final java.util.Scanner scanner = new java.util.Scanner( rhsstr );
    //         while (scanner.hasNextLine()) {
    //             final String line = scanner.nextLine();
    //             rhsstrIndented += "  "+ line + "\n"; // insert 2 blanks/SPACE characters in each line
    //         }
    //         scanner.close();

    //         s = _key +":\n"+ rhsstrIndented +"\n";
    //         // System.out.println( CLASSNAME + ": s = " + s +"\n\n" );

    //         final java.io.Reader reader2 = new java.io.StringReader( s );
    //         @SuppressWarnings("unchecked")
    //         final LinkedHashMap<String, Object> tempMap = new com.esotericsoftware.yamlbeans.YamlReader(reader2).read(LinkedHashMap.class);
    //         if ( this.verbose ) System.out.println( CLASSNAME + ": created new Map [" + tempMap.toString() +"]" );

    //         _map.putAll( tempMap );

    //     } catch (com.esotericsoftware.yamlbeans.YamlException e) { // Warning: This must PRECEDE IOException, else compiler error.
    //         e.printStackTrace(System.err);
    //         System.err.println( CLASSNAME + ": Internal error: unable to enhance existing YAML object with key='" + _key +"' & rhs='" + _rhs.toString() +"' & s = ["+ s +"]" );
    //         System.exit(101);
    //     } catch (java.io.IOException e) {
    //         e.printStackTrace(System.err);
    //         System.err.println( CLASSNAME + ": Failure to convert to YAML - with  key='" + _key +"' & rhs='" + _rhs.toString() +"' & s = ["+ s +"]" );
    //         System.exit(102);
    //     } catch (Exception e) {
    //         e.printStackTrace(System.err);
    //         System.err.println( CLASSNAME + ": Unknown Internal error:.");
    //         System.exit(103);
    //     }
    // }

}
