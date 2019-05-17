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

import com.esotericsoftware.yamlbeans.YamlConfig;
import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import com.esotericsoftware.yamlbeans.YamlWriter;

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
public class GenericYAMLWriter {

    public static final String CLASSNAME = GenericYAMLWriter.class.getName();

    private boolean verbose;

    protected YamlWriter writer = null;

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

    private YAML_Libraries sYAMLLibrary = YAML_Libraries.SNAKEYAML_Library;

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     * The only constructor
     * @param _verbose  Whether you want deluge of debug-output onto System.out.
     */
    public GenericYAMLWriter( final boolean _verbose ) {
        this.verbose = _verbose;
        init();
    }

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    /**
     * Tells you what internal implementation of the YAML read/parsing is, and by implication what the internal implementation for YAML-output generation is.
     * @return a reference to the YAML Library in use. See {@link YAML_Libraries} for legal values.
     */
    public YAML_Libraries getYamlLibrary() {
        return this.sYAMLLibrary;
    }

    /**
     * Allows you to set the YAML-parsing/emitting library of choice.  Ideally used within a Batch-Yaml script.
     * @param _l the YAML-library to use going forward. See {@link YAML_Libraries} for legal values to this parameter
     */
    public void setYamlLibrary( final YAML_Libraries _l ) {
        this.sYAMLLibrary = _l;
    }

    /**
     * Invoke this method to re-initialize this class, after completing a sequence of {@link #prepare} {@link #write} {@link close}
     */
    public void init() {
    }
    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     *  This method takes the writer (whether StringWriter or FileWriter) and prepares the YAML library to write to it.
     *  WARNING!!! The EsotericSoftware's YamlWriter implementation takes over stdout, and it will STOP working for all System.out.println();
     *  @param _writer StringWriter or FileWriter (cannot be null)
     *  @throws Exception if the YAML libraries have any issues with ERRORs inthe YAML or other issues.
     */
    public void prepare( final java.io.Writer _writer ) throws Exception
    {
        // Leverage the appropriate YAMLReader library to load file-contents into a java.util.LinkedHashMap<String, Object>
        switch ( this.getYamlLibrary() ) {
            case SNAKEYAML_Library:
                // per https://bitbucket.org/asomov/snakeyaml/src/tip/src/test/java/examples/CustomMapExampleTest.java
                // See also https://bitbucket.org/asomov/snakeyaml/wiki/Documentation#markdown-header-collections
//                break;
// for now fall-through

            case ESOTERICSOFTWARE_Library:
                // prepare for output: whether it goes to System.out -or- to an actual output-file.
                this.writer = new YamlWriter( _writer );
                // WARNING!!! YamlWriter takes over stdout, and it will STOP working for all System.out.println();

                this.defaultConfigurationForYamlWriter( this.writer ); // , cmdLineArgs.quoteType
                break;

            case ASUXYAML_Library:
                final String es = CLASSNAME + ": prepare(): Unimplemented YAML-Library: " + this.getYamlLibrary();
                System.err.println( es );
                throw new Exception( es );
                // break;
        } // switch
    } //function

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     * Write the YAML content (_output parameter) using the YAML-Library specified via {@link #setYamlLibrary} and to the java.io.Writer reference provided via {@link #prepare(java.io.Writer)}.
     * @param _output the content you want written out as a YAML file.
     * @throws Exception if the YAML libraries have any issues with ERRORs inthe YAML or other issues.
     */
    public void write( final Object _output ) throws Exception
    {
        if (this.writer != null) {
            // Leverage the appropriate YAMLReader library to load file-contents into a java.util.LinkedHashMap<String, Object>
            switch ( this.getYamlLibrary() ) {
                case SNAKEYAML_Library:
                    // per https://bitbucket.org/asomov/snakeyaml/src/tip/src/test/java/examples/CustomMapExampleTest.java
                    // See also https://bitbucket.org/asomov/snakeyaml/wiki/Documentation#markdown-header-collections
//                    break;
// for now fall-through

                case ESOTERICSOFTWARE_Library:
                    if ( _output instanceof LinkedHashMap || _output instanceof ArrayList || _output instanceof LinkedList || _output instanceof String ) {
                        if (this.verbose) System.out.println( CLASSNAME + ": write(): writing output " + _output + "]" );
                        if (this.verbose) System.out.println( CLASSNAME + ": write(): final output is of type " + _output.getClass().getName() + "]" );
                        this.writer.write( _output );
                        // @SuppressWarnings("unchecked")
                        // final LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>) _output;
                        // this.writer.write(map);
                    } else {
                        throw new Exception( "output is Not of type LinkedHashMap.  It's ["+ ((_output==null)?"null":_output.getClass().getName()) +"]");
                    }
                    break;

                case ASUXYAML_Library:
                    final String es = CLASSNAME + ": prepare(): Unimplemented YAML-Library: " + this.getYamlLibrary();
                    System.err.println( es );
                    throw new Exception( es );
                    // break;
            } // switch
        } // if writer != null
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     * Call this in exactly the way you'd close a file after writing to it.  This method should be called ONLY after {@link #write(Object)} will no longer be invoked.
     * @throws Exception if the YAML libraries have any issues with ERRORs inthe YAML or other issues.
     */
    public void close() throws Exception {
        if (this.writer != null)
            writer.close();
        this.writer = null;
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    public void defaultConfigurationForYamlWriter( YamlWriter writer )
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

}
