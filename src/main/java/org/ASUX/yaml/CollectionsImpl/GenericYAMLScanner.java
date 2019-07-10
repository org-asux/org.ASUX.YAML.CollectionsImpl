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

package org.ASUX.yaml.CollectionsImpl;

import org.ASUX.common.Tuple;
import org.ASUX.common.Output;
import org.ASUX.common.Debug;

import org.ASUX.yaml.YAML_Libraries;

import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.InputStream;

import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;

import static org.junit.Assert.*;

//##################################################################################
public class GenericYAMLScanner {

    public static final String CLASSNAME = GenericYAMLScanner.class.getName();
    private boolean verbose;

    private YAML_Libraries sYAMLLibrary = YAML_Libraries.CollectionsImpl_Library;

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
     * @return a reference to the YAML Library in use. See {@link org.ASUX.yaml.YAML_Libraries} for legal values.
     */
    public YAML_Libraries getYamlLibrary() {
        return this.sYAMLLibrary;
    }

    /**
     * Allows you to set the YAML-parsing/emitting library of choice.  Ideally used within a Batch-Yaml script.
     * @param _l the YAML-library to use going forward. See {@link org.ASUX.yaml.YAML_Libraries} for legal values to this parameter
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
        if (this.verbose) System.out.println( CLASSNAME + ": load(java.io.Reader): this.getYamlLibrary()="+ this.getYamlLibrary() );

        // -----------------------
        // Leverage the appropriate YAMLReader library to load file-contents into a java.util.LinkedHashMap<String, Object>
        switch ( this.getYamlLibrary() ) {
            case CollectionsImpl_Library:
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

            case NodeImpl_Library:
            case SNAKEYAML_Library:
            case ASUXYAML_Library:
            default:
                final String es = CLASSNAME + ": main(): Unimplemented YAML-Library: " + this.getYamlLibrary();
                System.err.println( es );
                throw new Exception( es );
                // break;
        } // switch
        // return null;
    } //function

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
