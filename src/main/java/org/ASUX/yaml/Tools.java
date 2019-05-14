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

import org.ASUX.common.Tuple;
import org.ASUX.common.Output;

import java.util.LinkedList;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Properties;


import static org.junit.Assert.*;

import com.esotericsoftware.yamlbeans.YamlConfig;
import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import com.esotericsoftware.yamlbeans.YamlWriter;

/**
 *  <p>This class is a bunch of tools to help make it easy to work with the java.util.Map objects that the YAML library creates.</p>
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

    public static void defaultConfigurationForYamlReader( YamlReader reader, final Cmd cmd )
    {
        // reader.getConfig().readConfig.setClassLoader( Equals.class.getClassLoader() );
        // reader.getConfig().setClassTag("Equals", Equals.class);
        // reader.getConfig().setClassTag("Or", Or.class);
        // reader.getConfig().setClassTag("Ref", Ref.class);
    }

    public static void defaultConfigurationForYamlWriter( YamlWriter writer )
                                            // YamlConfig.Quote qtyp
    {
            // writer.getConfig().writeConfig.setWriteRootTags(false); // Does NOTHING :-
            writer.getConfig().writeConfig.setWriteClassname(YamlConfig.WriteClassName.NEVER); // I hate !<pkg.className> within YAML files. So does AWS I believe.
            // writer.getConfig().writeConfig.setQuoteChar( qtyp );
            // writer.getConfig().writeConfig.setQuoteChar( YamlConfig.Quote.NONE );
            // writer.getConfig().writeConfig.setQuoteChar( YamlConfig.Quote.SINGLE );
            // writer.getConfig().writeConfig.setQuoteChar( YamlConfig.Quote.DOUBLE );

            // writer.getConfig().setClassTag("Equals", Equals.class);
            // writer.getConfig().setClassTag("Or", Or.class);
            // writer.getConfig().setClassTag("Ref", Ref.class);
    }


    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /** This function ensures the String form of JSON will work will all the YAML commands: read, list, replace, macro, ..
     *  I need such a function, as I learned the hard way that libraries do NOT work 100% well.  Only file-formats are the workaorund/ way out.
     *  I definitely "fgool-proof" method to ensure 'valid' YAML, for error-free processing by the entire org.ASUX.yaml library to work without any issues
     *  Note: Currently this function is identical to JSON2YAML()!
     *  @param _jsonStr java.lang.String object
     *  @return a java.util.LinkedHashMap&lt;String, Object&gt; object that's definitely "kosher" for the entire org.ASUX.yaml library to work without any issues
     * @throws com.fasterxml.jackson.core.JsonProcessingException if any error using Jackson library
     * @throws YamlException if unable to convert into LinkedHashMap per YAMLReader library classes
     * @throws java.io.IOException if any error using java.io.StringReader and java.io.StringWriter
     * @throws Exception any other run-time exception, while parsing large Strings, nullpointers, etc.. ..
     */
    public LinkedHashMap<String, Object>  JSONlintRemover( final String  _jsonStr )
                    throws YamlException, com.fasterxml.jackson.core.JsonProcessingException,
                            java.io.IOException, Exception
    {
        return JSONString2YAML( _jsonStr );
    }

    //-----------------------------------------------------------------------------------------
    /** String output variant of JSONlintRemover(String).
     *  @param _jsonString java.lang.String object
     *  @return a String object that's definitely "kosher" for the entire org.ASUX.yaml library to work without any issues
     * @throws com.fasterxml.jackson.core.JsonProcessingException if any error using Jackson library
     * @throws YamlException if unable to convert into LinkedHashMap per YAMLReader library classes
     * @throws java.io.IOException if any error using java.io.StringReader and java.io.StringWriter
     * @throws Exception any other run-time exception, while parsing large Strings, nullpointers, etc.. ..
     */
    public String  JSONlintRemoverString( final String  _jsonString )
                    throws YamlException, com.fasterxml.jackson.core.JsonProcessingException,
                            java.io.IOException, Exception
    {
        final LinkedHashMap<String, Object> map = JSONString2YAML( _jsonString );
        return this.Map2JSONString(map);
    }

    //-----------------------------------------------------------------------------------------
    /** Convert an in-memory YAML into a JSON-compatible string.
     *  @param _map a java.util.LinkedHashMap&lt;String, Object&gt; object, as generated by Jackson http://tutorials.jenkov.com/java-json/jackson-objectmapper.html#read-map-from-json-string 
     *  @return a String object that's definitely "kosher" for the entire org.ASUX.yaml library to work without any issues
     * @throws com.fasterxml.jackson.core.JsonProcessingException if any error using Jackson library
     * @throws YamlException if unable to convert into LinkedHashMap per YAMLReader library classes
     * @throws java.io.IOException if any error using java.io.StringReader and java.io.StringWriter
     * @throws Exception any other run-time exception, while parsing large Strings, nullpointers, etc.. ..
     */
    public String Map2JSONString( final LinkedHashMap<String, Object>  _map )
                    throws YamlException, com.fasterxml.jackson.core.JsonProcessingException,
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
    /** Takes any YAML input - as a LinkedHashmap - and exports it as YAML-String (to java.util.String)
     *  @param _yaml a java.util.LinkedHashMap&lt;String, Object&gt; object, as generated by Jackson http://tutorials.jenkov.com/java-json/jackson-objectmapper.html#read-map-from-json-string 
     *  @return a java.util.LinkedHashMap&lt;String, Object&gt; object that's definitely "kosher" for the entire org.ASUX.yaml library to work without any issues
     *  @throws Exception Any issue whatsoever when dealing with convering YAML/JSON content into Strings and back (as part of lintremoval)
     */
    public String Map2YAMLString( final LinkedHashMap<String, Object> _yaml ) throws Exception
    {
        final java.io.StringWriter strwrtr3 = new java.io.StringWriter();
        try {
            YamlWriter writer3 = new YamlWriter( strwrtr3 );
            defaultConfigurationForYamlWriter( writer3 ); // , YamlConfig.Quote.NONE
            writer3.write( _yaml );
            writer3.close();
            if ( this.verbose ) System.out.println( CLASSNAME + ": Map2YAMLString(): created new YAML-String\n" + strwrtr3.toString() +"\n" );

            return strwrtr3.toString();

        } catch (YamlException e) { // Warning: This must PRECEDE IOException, else compiler error.
            e.printStackTrace(System.err);
            System.err.println( CLASSNAME + ": Map2YAMLString(): Internal error: unable to enhance existing YAML object with ["+ strwrtr3.toString() +"]");
            throw e;
        } catch (java.io.IOException e) {
            if ( this.verbose ) e.printStackTrace(System.err);
            if ( this.verbose ) System.err.println( CLASSNAME + ": Map2YAMLString(): Failure to read/write the contents of the String '" + strwrtr3.toString() +"'.");
            throw e;
        } catch (Exception e) {
            if ( this.verbose ) e.printStackTrace(System.err);
            if ( this.verbose ) System.err.println( CLASSNAME + ": Map2YAMLString(): Unknown Internal error:.");
            throw e;
        }
        // return null;
    } // function

    //-----------------------------------------------------------------------------------------
    /** Takes any JSON input - as a LinkedHashmap obtained from any Library - and exports it as YAML (to java.util.String), and then reads it back as YAML.
     *  I need such a function, as I learned the hard way that libraries do NOT work 100% well.  Only file-formats are the workaorund/ way out.
     *  I definitely "fgool-proof" method to ensure 'valid' YAML, for error-free processing by the entire org.ASUX.yaml library to work without any issues
     *  @param _map a java.util.LinkedHashMap&lt;String, Object&gt; object, as generated by Jackson http://tutorials.jenkov.com/java-json/jackson-objectmapper.html#read-map-from-json-string 
     *  @return a java.util.LinkedHashMap&lt;String, Object&gt; object that's definitely "kosher" for the entire org.ASUX.yaml library to work without any issues
     *  @throws Exception Any issue whatsoever when dealing with convering YAML/JSON content into Strings and back (as part of lintremoval)
     */
    public LinkedHashMap<String, Object> lintRemover( final LinkedHashMap<String, Object> _map ) throws Exception 
    {
        // First write it to java.lang.String object... then, read it back into YAML, using the YamlReder class

        // final java.io.StringWriter strwrtr7 = new java.io.StringWriter();
        try {
            // YamlWriter writer3 = new YamlWriter( strwrtr7 );
            // defaultConfigurationForYamlWriter( writer3, YamlConfig.Quote.NONE );
            // writer3.write( _json );
            // writer3.close();
            // if ( this.verbose ) System.out.println( CLASSNAME + ": JSON2YAML(): created new YAML-String\n" + strwrtr7.toString() +"\n" );

            final String s = Map2YAMLString( _map );
            return YAMLString2YAML( s, false ); // 2nd parameter is 'bWrapScalar' === false;.  's' cannot be a scalar at this point.  If it is, I want things to fail with null-pointer.

        } catch (YamlException e) { // Warning: This must PRECEDE IOException, else compiler error.
            if ( this.verbose ) e.printStackTrace(System.err);
            if ( this.verbose ) System.err.println( CLASSNAME + ": JSON2YAML(): Internal error: unable to enhance existing YAML object with ["+ _map.toString() +"]");
            throw e;
        } catch (java.io.IOException e) {
            if ( this.verbose ) e.printStackTrace(System.err);
            if ( this.verbose ) System.err.println( CLASSNAME + ": JSON2YAML(): Failure to read/write the contents of the String '" + _map.toString() +"'.");
            throw e;
        } catch (Exception e) {
            if ( this.verbose ) e.printStackTrace(System.err);
            if ( this.verbose ) System.err.println( CLASSNAME + ": JSON2YAML(): Unknown Internal error:.");
            throw e;
        }
        // return null;
    } // function


    /**
     *  Takes any STRING-form JSON as input - it better be valid JSON - and reads it back as YAML/LinkedHashMap.
     *  I need such a function, as I learned the hard way that libraries do NOT work 100% well.  Only file-formats are the workaorund/ way out.
     *  I definitely "fgool-proof" method to ensure 'valid' YAML, for error-free processing by the entire org.ASUX.yaml library to work without any issues
     *  @param _jsonString a java.lang.String object
     *  @return a java.util.LinkedHashMap&lt;String, Object&gt; object that's definitely "kosher" for the entire org.ASUX.yaml library to work without any issues
     * @throws YamlException if unable to convert into LinkedHashMap per YAMLReader library classes
     * @throws java.io.IOException if any error using java.io.StringReader and java.io.StringWriter
     * @throws Exception any other run-time exception, while parsing large Strings, nullpointers, etc.. ..
     */
    public LinkedHashMap<String, Object>  JSONString2YAML( final String  _jsonString )
                    throws YamlException, java.io.IOException, Exception
    {
        String wellFormedJSONString = _jsonString;
        final Tools tools = new Tools(this.verbose);

        if ( _jsonString.contains("=") && ! _jsonString.contains(":") ) {
            // WEll! it means the entire string in Key=Value format.   Not in proper Key:Value JSON format.
            wellFormedJSONString = _jsonString.replaceAll("=", ": "); // fingers crossed. I hope this works.
        } else {
            wellFormedJSONString = _jsonString.replaceAll(":", ": "); // Many libraries do NOT like  'key:value'.  They want a blank after colon like 'key: value'
        }
        if ( this.verbose ) System.out.println(">>>>>>>>>>>>>>>>>>>> "+ CLASSNAME+": JSONString2YAML(): "+ wellFormedJSONString);

        try {
            final java.io.Reader reader3 = new java.io.StringReader( wellFormedJSONString );
            // http://tutorials.jenkov.com/java-json/jackson-objectmapper.html#read-map-from-json-string 
            // https://fasterxml.github.io/jackson-databind/javadoc/2.7/com/fasterxml/jackson/databind/ObjectMapper.html#readValue(java.io.Reader,%20java.lang.Class)
            com.fasterxml.jackson.databind.ObjectMapper objMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            objMapper.configure( com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true );
            objMapper.configure( com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
            com.fasterxml.jackson.databind.type.MapType type = objMapper.getTypeFactory().constructMapType( LinkedHashMap.class, String.class, Object.class );
            LinkedHashMap<String, Object> retMap2 = objMapper.readValue( reader3, new com.fasterxml.jackson.core.type.TypeReference< LinkedHashMap<String,Object> >(){}  );
            if ( this.verbose ) System.out.println( CLASSNAME +" JSONString2YAML("+ _jsonString +"): jsonMap loaded BY OBJECTMAPPER into a LinkedHashMao =" + retMap2 );
            retMap2 = tools.lintRemover( retMap2 ); // this will 'clean/lint-remove'
            return retMap2;

        } catch (com.fasterxml.jackson.core.JsonParseException e) {
            if (this.verbose) e.printStackTrace(System.err);
            if (this.verbose) System.err.println( CLASSNAME+": JSONString2YAML(): Failed to parse ["+ _jsonString +"] after converting to ["+ wellFormedJSONString +"]" );
            throw e;
        } catch (com.fasterxml.jackson.databind.JsonMappingException e) {
            if (this.verbose) e.printStackTrace(System.err);
            if (this.verbose) System.err.println( CLASSNAME+": JSONString2YAML(): Failed to parse ["+ _jsonString +"] after converting to ["+ wellFormedJSONString +"]" );
            throw e;
        } catch (java.io.IOException e) {
            if (this.verbose) e.printStackTrace(System.err);
            if (this.verbose) System.err.println( CLASSNAME+": JSONString2YAML(): Failed to parse ["+ _jsonString +"] after converting to ["+ wellFormedJSONString +"]" );
            throw e;
        }
    } // function

    /**
     *  Takes any STRING-form JSON as input - it better be valid JSON - and reads it back as YAML/LinkedHashMap.
     *  I need such a function, as I learned the hard way that libraries do NOT work 100% well.  Only file-formats are the workaorund/ way out.
     *  I definitely "fgool-proof" method to ensure 'valid' YAML, for error-free processing by the entire org.ASUX.yaml library to work without any issues
     *  @param _yamlString a java.lang.String object
     *  @param _bWrapScalar true or false.  If the returne value is going to be a SCALAR, do you want it wrapped into a LinkedHashMap or throw instead?
     *  @return a java.util.LinkedHashMap&lt;String, Object&gt; object that's definitely "kosher" for the entire org.ASUX.yaml library to work without any issues
     * @throws YamlException if unable to convert into LinkedHashMap per YAMLReader library classes
     * @throws java.io.IOException if any error using java.io.StringReader and java.io.StringWriter
     * @throws Exception any other run-time exception, while parsing large Strings, nullpointers, etc.. ..
     */
    public LinkedHashMap<String, Object>  YAMLString2YAML( final String  _yamlString, final boolean _bWrapScalar )
                    throws YamlException, java.io.IOException, Exception
    {
        if ( this.verbose ) System.out.println(">>>>>>>>>>>>>>>>>>>> "+ CLASSNAME+": YAMLString2YAML(): "+ _yamlString);

        try {
            final java.io.Reader reader3 = new java.io.StringReader( _yamlString );
            @SuppressWarnings("unchecked")
            final LinkedHashMap<String, Object> tempMap = new YamlReader(reader3).read( LinkedHashMap.class );
            reader3.close();
            if ( this.verbose ) System.out.println( CLASSNAME + ": YAMLString2YAML(): created new Map = " + tempMap.toString() +" " );

            return tempMap;

        } catch (YamlReader.YamlReaderException e) {
            // YamlReader$YamlReaderException: Line 0, column 10: Expected data for a java.util.LinkedHashMap field but found: scalar
            if (this.verbose) System.out.println( CLASSNAME+": YAMLString2YAML(): Hmmm.. Just a string?? passed as parameter??");
            // if ( e.getMessage().contains("Expected data for a java.util.LinkedHashMap field but found: scalar"))
            if ( e.getMessage().contains("but found: scalar") && _bWrapScalar )
                return new Output(this.verbose).wrapAnObject_intoLinkedHashMap( _yamlString );
            else {
                if ( this.verbose ) e.printStackTrace(System.err);
                if ( this.verbose ) System.err.println( CLASSNAME+": YAMLString2YAML(): Input String ["+ _yamlString +"] does Not seem to be YAML - nor - a simple SCALAR string" );
                throw e;
            }
        }
    } // function

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    // /** 
    //  *  In order for me to add new entries to the _map created by that library, I need to go thru hoops.
    //  *  @param _map a java.util.LinkedHashMap&lt;String, Object&gt; object
    //  *  @param _key the key in key-value pair
    //  *  @param _rhs the value(java.lang.Object) in the key-value pair
    //  */
    // public void addStringEntry( LinkedHashMap<String, Object> _map, final String _key, final String _rhs) {

    //     final String quotedStr = _rhs.contains(":") ? "'"+_rhs+"'" : _rhs;
    //     // final String s = "{ "+ _key +": "+ quotedStr +" }";
    //     final String s = _key +": "+ quotedStr +"\n";
    //     try {
    //         final java.io.Reader reader2 = new java.io.StringReader( s );
    //         @SuppressWarnings("unchecked")
    //         final LinkedHashMap<String, Object> tempMap = new YamlReader(reader2).read(LinkedHashMap.class);
    //         if ( this.verbose ) System.out.println( CLASSNAME + ": created new Map [" + tempMap.toString() +"]" );

    //         _map.putAll( tempMap );

    //     } catch (YamlException e) { // Warning: This must PRECEDE IOException, else compiler error.
    //         e.printStackTrace(System.err);
    //         System.err.println( CLASSNAME + ": Internal error: unable to enhance existing YAML object with ["+ s +"]");
    //         throw e;
    //     } catch (java.io.IOException e) {
    //         e.printStackTrace(System.err);
    //         System.err.println( CLASSNAME + ": Failure to convert the String '" + _key +"' & "+ _rhs +"' into YAML.");
    //         throw e;
    //     } catch (Exception e) {
    //         e.printStackTrace(System.err);
    //         System.err.println( CLASSNAME + ": Unknown Internal error:.");
    //         throw e;
    //     }
        
    // } // function

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    // /** 
    //  *  In order for me to add new entries to the _map created by that library, I need to go thru hoops.
    //  *  @param _map a java.util.LinkedHashMap&lt;String, Object&gt; object
    //  *  @param _key the key in key-value pair
    //  *  @param _rhs the value(java.lang.Object) in the key-value pair
    //  */
    // public void addMapEntry( LinkedHashMap<String, Object> _map, final String _key, final Object _rhs) {

    //     // tokenizer.ScalarToken keyToken =
    //     //     new tokenizer.ScalarToken( _key, false, '\'' );
    //     // _map.put( keyToken, _rhs );

    //     String s = null;
    //     try {
    //         final java.io.StringWriter strwrtr = new java.io.StringWriter();
    //         final YamlWriter writer = 
    //             new YamlWriter( strwrtr );
    //         writer.getConfig().writeConfig.setWriteClassname( YamlConfig.WriteClassName.NEVER ); // I hate !org.pkg.class within YAML files.  So does AWS I believe.
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
    //         final LinkedHashMap<String, Object> tempMap = new YamlReader(reader2).read(LinkedHashMap.class);
    //         if ( this.verbose ) System.out.println( CLASSNAME + ": created new Map [" + tempMap.toString() +"]" );

    //         _map.putAll( tempMap );

    //     } catch (YamlException e) { // Warning: This must PRECEDE IOException, else compiler error.
    //         e.printStackTrace(System.err);
    //         System.err.println( CLASSNAME + ": Internal error: unable to enhance existing YAML object with key='" + _key +"' & rhs='" + _rhs.toString() +"' & s = ["+ s +"]" );
    //         throw e;
    //     } catch (java.io.IOException e) {
    //         e.printStackTrace(System.err);
    //         System.err.println( CLASSNAME + ": Failure to convert to YAML - with  key='" + _key +"' & rhs='" + _rhs.toString() +"' & s = ["+ s +"]" );
    //         throw e;
    //     } catch (Exception e) {
    //         e.printStackTrace(System.err);
    //         System.err.println( CLASSNAME + ": Unknown Internal error:.");
    //         throw e;
    //     }
    // }

    public static void main( String[] args ) {
        try {
            LinkedHashMap<String, Object> map = new Tools(true).JSONString2YAML( args[0] );
            System.out.println("Normal completion of program");
        } catch (YamlReader.YamlReaderException e) {
            System.out.println("Hmmm.. Just a string?");
        } catch (YamlException e) { // Warning: This must PRECEDE IOException, else compiler error.
            e.printStackTrace(System.err);
            System.exit(101);
        } catch (java.io.IOException e) {
            e.printStackTrace(System.err);
            System.exit(102);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.exit(103);
        }
    }
}
