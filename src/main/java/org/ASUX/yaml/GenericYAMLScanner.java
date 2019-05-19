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
import org.ASUX.common.Debug;


import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.InputStream;

// import java.io.FileNotFoundException;
// import java.io.IOException;

import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;

import static org.junit.Assert.*;

// https://yaml.org/spec/1.2/spec.html#id2762107
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.composer.Composer;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.NodeId;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.Mark; // https://bitbucket.org/asomov/snakeyaml/src/default/src/main/java/org/yaml/snakeyaml/error/Mark.java
import org.yaml.snakeyaml.DumperOptions;

// import org.yaml.snakeyaml.constructor.SafeConstructor;

//##################################################################################
public class GenericYAMLScanner {

    public static final String CLASSNAME = GenericYAMLScanner.class.getName();
    private boolean verbose;

    private YAML_Libraries sYAMLLibrary = YAML_Libraries.SNAKEYAML_Library;

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     * The only constructor
     * @param _verbose  Whether you want deluge of debug-output onto System.out.
     */
    public GenericYAMLScanner( final boolean _verbose ) {
        this.verbose = _verbose;
    }

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    /**
     * Tells you what internal implementation of the YAML read/parsing is, and by implication what the internal implementation for YAML-output generation is.
     * @return a reference to the YAML Library in use. See {@link GenericYAMLScanner.YAML_Libraries} for legal values.
     */
    public YAML_Libraries getYamlLibrary() {
        return this.sYAMLLibrary;
    }

    /**
     * Allows you to set the YAML-parsing/emitting library of choice.  Ideally used within a Batch-Yaml script.
     * @param _l the YAML-library to use going forward. See {@link GenericYAMLScanner.YAML_Libraries} for legal values to this parameter
     */
    public void setYamlLibrary( final YAML_Libraries _l ) {
        this.sYAMLLibrary = _l;
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     * This method will use the YAML-Library specified via {@link #setYamlLibrary} and load the YAML content (pointed to by the _inreader paramater).
     * @param _inreader either a StringReader or a FileReader
     * @return instance of {@link org.ASUX.common.Output.Object}
     * @throws Exception if the YAML libraries have any issues with ERRORs inthe YAML or other issues.
     */
    public org.ASUX.common.Output.Object<?> load( final java.io.Reader _inreader ) throws Exception
    {
        org.ASUX.common.Output.Object<?> outputObj = null;

        // -----------------------
        // Leverage the appropriate YAMLReader library to load file-contents into a java.util.LinkedHashMap<String, Object>
        switch ( this.getYamlLibrary() ) {
            case ESOTERICSOFTWARE_Library:
                final com.esotericsoftware.yamlbeans.YamlReader reader = new com.esotericsoftware.yamlbeans.YamlReader( _inreader );
                this.defaultConfigurationForYamlReader( reader );
                if ( this.verbose ) System.out.println( CLASSNAME +" load(): about to read YAML using "+ this.getYamlLibrary() );

                final LinkedHashMap inputDataObj = reader.read( LinkedHashMap.class ); // LinkedHashMap.class );
                if ( this.verbose ) System.out.println( CLASSNAME +" load(): read YAML ="+ inputDataObj );

                @SuppressWarnings("unchecked")
                final LinkedHashMap<String, Object> lhm11 = (inputDataObj != null) ?
                                    ((LinkedHashMap<String, Object>) inputDataObj) : new LinkedHashMap<String, Object>();
                _inreader.close();
                outputObj = new org.ASUX.common.Output.Object<String>();
                outputObj.setMap( lhm11 );
                return outputObj;
                // break;

            case SNAKEYAML_Library:
                // https://yaml.org/spec/1.2/spec.html#id2762107
                // per https://bitbucket.org/asomov/snakeyaml/src/tip/src/test/java/examples/CustomMapExampleTest.java
                // See also https://bitbucket.org/asomov/snakeyaml/wiki/Documentation#markdown-header-collections
                // class MyCustomConstructor extends org.yaml.snakeyaml.constructor.Constructor {
                //     @Override
                //     protected Map<Object, Object> createDefaultMap(int initSize) {
                //         final Map<Object, Object> retval = (Map<Object, Object>) new LinkedHashMap<Object, Object>();
                //         return retval;
                //     }
                //     // @Override
                //     // protected Class<?> getClassForNode(Node node) {
                //     //     Class<? extends Object> classForTag = typeTags.get(node.getTag());
                //     //     if (classForTag == null) {
                //     //         Class<?> cl;
                //     //         try {
                //     //             String name = node.getTag().getClassName();
                //     //             cl = getClassForName(name);
                //     //         } catch (ClassNotFoundException e) {
                //     //             // This is where we override the PARENT class's definition of this function/method.
                //     //             // throw new YAMLException("Class not found: " + name);
                //     //             cl = String.class;
                //     //         } catch (org.yaml.snakeyaml.error.YAMLException e) {
                //     //             cl = String.class;
                //     //         }
                //     //         typeTags.put(node.getTag(), cl);
                //     //         return cl;
                //     //     } else {
                //     //         return classForTag;
                //     //     }
                //     // }
                // }; // inline class definition
                // Yaml yaml = new Yaml( new MyCustomConstructor() ); // see class-definition for MyCustomConstructor in above few lines
                // // Yaml.load() accepts a String or an InputStream object
                // // List<Object> list = (List<Object>) yaml.load( is1 );
                // @SuppressWarnings("unchecked")
                // final LinkedHashMap<String, Object> lhm22 = (LinkedHashMap<String, Object>) yaml.load( is1 );
                final org.yaml.snakeyaml.reader.StreamReader snkrdr = new org.yaml.snakeyaml.reader.StreamReader(_inreader);
                final Composer composer = new Composer( new org.yaml.snakeyaml.parser.ParserImpl(snkrdr), new org.yaml.snakeyaml.resolver.Resolver() );
                // final Node rootNode = composer.getSingleNode();
{
    System.out.println( ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>" );
    final Composer composer2 = new Composer( new org.yaml.snakeyaml.parser.ParserImpl(snkrdr), new org.yaml.snakeyaml.resolver.Resolver() );
    composer2.checkNode();
    final java.io.StringWriter stdoutSurrogate = new java.io.StringWriter();
    final GenericYAMLWriter snakewr = new GenericYAMLWriter( this.verbose );
    snakewr.test( stdoutSurrogate, composer2.getNode() );
    snakewr.close();
    stdoutSurrogate.close();
    final String outputStr = stdoutSurrogate.toString();
    System.out.println(outputStr);
    System.out.println( ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>" );
}

                int numOfYamlDocuments = 0;
                final ArrayList<Object> docuArray = new ArrayList<Object>();

                while ( composer.checkNode() ) { // Check if further documents are available.
                    // getNode(): Reads and composes the next document.
                    final Node n = composer.getNode(); // https://bitbucket.org/asomov/snakeyaml/src/default/src/main/java/org/yaml/snakeyaml/nodes/Node.java
                    if ( this.verbose ) System.out.println( CLASSNAME +" load(): document # "+ numOfYamlDocuments + " is of type "+ n.getNodeId() +" " );

                    outputObj = recursiveConversion( n );

                    numOfYamlDocuments ++;
                    docuArray.add( outputObj );
// ??????????????? Is this while loop about loading MULTIPLE documents???
                } // while

                if ( numOfYamlDocuments == 1 ) {
                    return outputObj;
                    // inputData.values().iterator().next(); // if this throws exception.. I've no idea what's going on.
                } else {
                    final org.ASUX.common.Output.Object<java.lang.Object> o2 = new org.ASUX.common.Output.Object<java.lang.Object>();
                    o2.setArray( docuArray );
                    return o2;
                }
                // break;

            case ASUXYAML_Library:
                final String es = CLASSNAME + ": main(): Unimplemented YAML-Library: " + this.getYamlLibrary();
                System.err.println( es );
                throw new Exception( es );
                // break;
        } // switch
        return null;
    } //function


    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    public org.ASUX.common.Output.Object<?> recursiveConversion( final Node _node ) throws Exception
    {
        org.ASUX.common.Output.Object<?> outputObj= null;

        // public enum org.yaml.snakeyaml.nodes.NodeId = scalar, sequence, mapping, anchor
        final NodeId nid = _node.getNodeId(); // https://bitbucket.org/asomov/snakeyaml/src/default/src/main/java/org/yaml/snakeyaml/nodes/NodeId.java
        if ( this.verbose ) System.out.println( CLASSNAME +" recursiveConversion(): @top, node-id = ["+ nid + "]" );

        // DumperOptions.ScalarStyle = DOUBLE_QUOTED('"'), SINGLE_QUOTED('\''), LITERAL('|'), FOLDED('>'), PLAIN(null);
        // https://bitbucket.org/asomov/snakeyaml/src/default/src/main/java/org/yaml/snakeyaml/DumperOptions.java

        if ( _node instanceof MappingNode ) {
            // https://yaml.org/spec/1.2/spec.html#id2762107
            // https://bitbucket.org/asomov/snakeyaml/src/default/src/main/java/org/yaml/snakeyaml/nodes/MappingNode.java   
            final MappingNode map = (MappingNode) _node;
            // MappingNode(Tag ignore, boolean resolved, List<NodeTuple> value, Mark startMark, Mark endMark, DumperOptions.FlowStyle flowStyle)
            final java.util.List<NodeTuple> tuples = map.getValue();
            // https://bitbucket.org/asomov/snakeyaml/src/default/src/main/java/org/yaml/snakeyaml/nodes/NodeTuple.java
            if ( this.verbose ) System.out.println( CLASSNAME +" recursiveConversion(): Mapping-node has value/tuples= ["+ tuples + "]" );

            final LinkedHashMap<String, Object> lhm = new LinkedHashMap<String, Object>();
            for( NodeTuple kv: tuples ) {
                final Node key = kv.getKeyNode();
                assert ( key.getNodeId() == NodeId.scalar ); // if assert fails, what scenario does that represent?
                final ScalarNode scalarKey = (ScalarNode) key;
                String kstr = (scalarKey.getTag().startsWith("!")) ? (scalarKey.getTag()+" ") : "";
                kstr += scalarKey.getValue();

                final Node val = kv.getValueNode();
                if ( this.verbose ) System.out.println( CLASSNAME +" recursiveConversion(): found LHS: RHS = ["+ key + "] : ["+ val + "]" );

                if ( val.getNodeId() == NodeId.scalar) {
                    final ScalarNode scalarVal = (ScalarNode) val;
                    String v = (scalarVal.getTag().startsWith("!")) ? (scalarVal.getTag()+" ") : "";
                    v += scalarVal.getValue();
                    lhm.put( kstr, v );
                    if ( this.verbose ) System.out.println( CLASSNAME +" recursiveConversion(): >>>>>>>>>>> ADDED SCALAR KV-pair= ["+ kstr + "] = ["+ v + "]" );

                } else {
                    if ( this.verbose ) System.out.println( CLASSNAME +" recursiveConversion(): recursing.. ..= ["+ val.getNodeId() + "]" );
                    final org.ASUX.common.Output.Object<?> asuxobj = recursiveConversion( val );
                    lhm.put( kstr, asuxobj.getJavaObject() );
                }
            } // for
            if ( this.verbose ) System.out.println( CLASSNAME +" recursiveConversion(): function-returning a LinkedHashMap = ["+ lhm + "]" );
            outputObj = new org.ASUX.common.Output.Object<String>();
            outputObj.setMap( lhm );
            return outputObj;

        } else if ( _node instanceof SequenceNode ) {
            // https://bitbucket.org/asomov/snakeyaml/src/default/src/main/java/org/yaml/snakeyaml/nodes/SequenceNode.java
            final SequenceNode seq = (SequenceNode) _node;
            // SequenceNode(Tag ignore, boolean resolved, List<Node> value, Mark startMark, Mark endMark, DumperOptions.FlowStyle flowStyle)
            if ( this.verbose ) System.out.println( CLASSNAME +" recursiveConversion(): SEQUENCE-node-id = ["+ seq.getNodeId() + "]" );

            final java.util.List<Node> lst = seq.getValue();
            final ArrayList<Object> arrObj = new ArrayList<Object>();
            final ArrayList<String> arrStr = new ArrayList<String>();
            boolean bNonScalarsDetected = false;
            for( Node val: lst ) {
                if ( val.getNodeId() == NodeId.scalar) {
                    final ScalarNode scalarVal = (ScalarNode) val;
                    String v = (scalarVal.getTag().startsWith("!")) ? (scalarVal.getTag()+" ") : "";
                    v += scalarVal.getValue();
                    arrObj.add( v );
                    arrStr.add( v );
                    if ( this.verbose ) System.out.println( CLASSNAME +" recursiveConversion(): >>>>>>>>>>> ADDED SCALAR into Array = ["+ scalarVal.getValue() + "]" );
                } else {
                    if ( this.verbose ) System.out.println( CLASSNAME +" recursiveConversion(): recursing.. ..= ["+ val.getNodeId() + "]" );
                    final org.ASUX.common.Output.Object<?> asuxobj = recursiveConversion( val );
                    arrObj.add( asuxobj.getJavaObject() );
                    bNonScalarsDetected = true;
                }
            } // for
            if ( bNonScalarsDetected ) {
                final org.ASUX.common.Output.Object<java.lang.Object> o2 = new org.ASUX.common.Output.Object<java.lang.Object>();
                o2.setArray( arrObj );
                outputObj = o2;
            } else {
                final org.ASUX.common.Output.Object<String> o3 = new org.ASUX.common.Output.Object<String>();
                o3.setArray( arrStr );
                outputObj = o3;
            }
            if ( this.verbose ) System.out.println( CLASSNAME +" recursiveConversion(): function-returning something = ["+ outputObj + "]" );
            return outputObj;

        } else if ( _node instanceof ScalarNode ) {
            // https://bitbucket.org/asomov/snakeyaml/src/default/src/main/java/org/yaml/snakeyaml/nodes/ScalarNode.java
            final ScalarNode scalarVal = (ScalarNode) _node;
            // ScalarNode(Tag ignore, String value, Mark startMark, Mark endMark, DumperOptions.ScalarStyle style)
            String v = (scalarVal.getTag().startsWith("!")) ? (scalarVal.getTag()+" ") : "";
            v += scalarVal.getValue();
            // boolean scalarVal.isPlain()
            // lhm.put( ??What-is-the-Key??? , val );
            if ( this.verbose ) System.out.println( CLASSNAME +" recursiveConversion(): >>>>>>>>>>> returning a SCALAR !! = ["+ v + "]" );
            final org.ASUX.common.Output.Object<String> o4 = new org.ASUX.common.Output.Object<String>();
            o4.setString( v );
            return o4;

        } else {
            final String es = CLASSNAME + ": main(): Unimplemented SnakeYaml Node-type: " + nid +" = ["+ _node.toString() +"]";
            System.err.println( es );
            throw new Exception( es );
        } // if-else-if-else
    } // function


    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    public void defaultConfigurationForYamlReader( com.esotericsoftware.yamlbeans.YamlReader reader )
    {
        // reader.getConfig().readConfig.setClassLoader( Equals.class.getClassLoader() );
        // reader.getConfig().setClassTag("Equals", Equals.class);
        // reader.getConfig().setClassTag("Or", Or.class);
        // reader.getConfig().setClassTag("Ref", Ref.class);
    }

}
