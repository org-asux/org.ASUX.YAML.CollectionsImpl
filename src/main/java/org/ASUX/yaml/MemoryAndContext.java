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

import java.util.LinkedHashMap;

/** 
 * @see org.ASUX.yaml.Cmd
 */
public class MemoryAndContext {

    public static final String CLASSNAME = "org.ASUX.yaml.MemoryAndContext";

    /** <p>Whether you want deluge of debug-output onto System.out.</p><p>Set this via the constructor.</p>
     *  <p>It's read-only (final data-attribute).</p>
     */
    private final boolean verbose;

    /** <p>Whether you want a final SHORT SUMMARY onto System.out.</p><p>a summary of how many matches happened, or how many entries were affected or even a short listing of those affected entries.</p>
     */
	public final boolean showStats;

    /**
     * This is a private LinkedHashMap&lt;String, LinkedHashMap&lt;String, Object&gt; &gt; savedOutputMaps = new LinkedHashMap&lt;&gt;(); .. cannot be null.  Most useful for @see org.ASUX.yaml.BatchYamlProcessor - which allows this this class to lookup !propertyvariable.
     * In case you need access to it - be nice and use it in a read-only manner - use the getter()
     */
    private final LinkedHashMap<String, LinkedHashMap<String, Object> > savedOutputMaps = new LinkedHashMap<>();

    private final Cmd cmd;

    //======================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //======================================================================
    /** The only Constructor.
     *  @param _verbose Whether you want deluge of debug-output onto System.out
     *  @param _showStats Whether you want a final summary onto console / System.out
     *  @param _cmd the instance of {@link org.ASUX.yaml.Cmd} that is the entry point to this whole progam, and know all about user's inputs, options and parameters
     */
    public MemoryAndContext( final boolean _verbose, final boolean _showStats, final Cmd _cmd ) {
		this.verbose = _verbose;
        this.showStats = _showStats;
        this.cmd = _cmd;
    }

    private MemoryAndContext() {
        this(false, true, null);
    }

    //======================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //======================================================================

    /**
     * This allows Cmd.java to interact better with BatchYamlProcessor.java, which is the authoritative source of all "saveAs" outputs.
     * Cmd.java will use this object (this.savedOutputMaps) primarily for passing the replacement-Content and insert-Content (which is NOT the same as --input/-i cmdline option)
     * @return this.savedOutputMaps
     */
    public LinkedHashMap<String, LinkedHashMap<String, Object> > getSavedOutputMaps() {
        return this.savedOutputMaps;
    }

    //======================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //======================================================================

    /**
     * This functon takes a single parameter that is a javalang.String value - and, either detects it to be inline YAML/JSON, or a filename (must be prefixed with '@'), or a reference to something saved in {@link MemoryAndContext} within a Batch-file execution (must be prefixed with a '!')
     * @param _src a javalang.String value - either inline YAML/JSON, or a filename (must be prefixed with '@'), or a reference to a property within a Batch-file execution (must be prefixed with a '!')
     * @return an object (either LinkedHashMap, ArrayList or LinkedList)
     */
    public Object getDataFromMemory( final String _src  ) {
        if (this.verbose) System.out.println( CLASSNAME +": getDataFromReference("+ _src +"): about to lookup memory " );
        if ( _src == null ) return null;
        final String savedMapName = _src.startsWith("!") ?  _src.substring(1) : _src;
        final Object recalledContent = (this.savedOutputMaps != null) ?  this.savedOutputMaps.get( savedMapName ) : null;
        if (this.verbose) System.out.println( CLASSNAME +": getDataFromReference("+ _src +"): memory says=" + ((recalledContent==null)?"null":recalledContent.toString()) );
        return recalledContent;
    }

    /**
     * This function saved _inputMap to a reference to a file (_dest parameter must be prefixed with an '@').. or, to a string prefixed with '!' (in which it's saved into Working RAM, Not to disk/file)
     * @param _dest a javalang.String value - either a filename (must be prefixed with '@'), or a reference to a (new) property-variable within a Batch-file execution (must be prefixed with a '!')
     * @param _inputMap the object to be saved using the reference provided in _dest paramater
     */
    public void saveDataIntoMemory( final String _dest, final LinkedHashMap<String, Object> _inputMap ) {
        final Tools tools = new Tools(this.verbose);
        if (this.verbose) System.out.println( CLASSNAME +": saveDataIntoReference("+ _dest +"): 1: saving into 'memoryAndContext': " + _inputMap.toString() );
        if ( _dest == null || _inputMap == null ) return;
        final String saveToMapName = _dest.startsWith("!") ?  _dest.substring(1) : _dest;
        if (this.verbose) System.out.println( CLASSNAME +": saveDataIntoReference("+ saveToMapName +"): 2: saving into 'memoryAndContext': " + _inputMap.toString() );
        if ( (this.savedOutputMaps != null) && (saveToMapName != null) && (saveToMapName.length() > 0) ) {
            // This can happen only within a BatchYaml-file context.  It only makes any sense (and will only work) within a BatchYaml-file context.
            this.savedOutputMaps.put( saveToMapName, _inputMap );  // remove '!' as the 1st character in the destination-reference provided
            if (this.verbose) System.out.println( CLASSNAME +": saveDataIntoReference("+ _dest +"): saved into 'memoryAndContext' --> " + _inputMap.toString() );
        }
    }

    //======================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //======================================================================

    /**
     * @return the count of how many items in memory.
     */
    public Cmd getContext() {
        return this.cmd;
    }

    /**
     * @return the count of how many items in memory.
     */
    public int getCount() {
        return this.savedOutputMaps.size();
    }

    /**
     * @return the String format dump of memory.  Can be ugly to read.
     */
    public String dump() {
        return this.savedOutputMaps.toString();
    }

}
