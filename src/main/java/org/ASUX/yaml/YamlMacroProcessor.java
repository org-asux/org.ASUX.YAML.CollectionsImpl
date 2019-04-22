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
import java.util.Properties;

import java.util.regex.*;

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
public class YamlMacroProcessor {

    public static final String CLASSNAME = "org.ASUX.YamlMacroProcessor";

    /** <p>Whether you want deluge of debug-output onto System.out.</p><p>Set this via the constructor.</p>
     *  <p>It's read-only (final data-attribute).</p>
     */
    public final boolean verbose;

    /** The only Constructor.
     *  @param _verbose Whether you want deluge of debug-output onto System.out
     */
    public YamlMacroProcessor(boolean _verbose) {
        this.verbose = _verbose;
    }
    protected YamlMacroProcessor(){
        this.verbose = false;
    }

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

    /** <p>This is a RECURSIVE-FUNCTION.  Make sure to pass in the right parameters.</p>
     *  <p>Note: this function expects you to pass in a 'new java.utils.Map()' as the 2nd parameter.  It will be 'filled' when function returns.</p>
     *  <p>This function returns true, if ANY occurance of ${ASUX::__} was detected and evaluated. If false, _inpMap and _outMap will be identical when function returns</p>
     *  @param _inpMap A java.utils.Map (created by com.esotericsoftware.yamlbeans library) containing the entire Tree representing the YAML file.
     *  @param _outpMap Pass in a new 'empty' java.util.Map().  THis is what this function *RETURNS* after Macros are evalated within _inpMap
     *  @param _props can be null, otherwise an instance of {@link java.util.Properties}
     *  @return true = whether at least one match of ${ASUX::} happened.
     *  @throws com.esotericsoftware.yamlbeans.YamlException - this is thrown by the library com.esotericsoftware.yamlbeans
     */
    public boolean recursiveSearch(
            final Map _inpMap,
			final LinkedHashMap _outpMap,
			final Properties _props
    ) throws com.esotericsoftware.yamlbeans.YamlException {

        if ( (_inpMap == null) || (_outpMap==null) ) return false;

        boolean bChangesMade = false;

        //--------------------------
        for (Object keyAsIs : _map.keySet()) {

            final String key = checkForMacros( keyAsIs, _props );
            assert( key != null );

            // Note: the lookup within _map .. uses keyAsIs.  Not key.
            final Object rhs = _map.get(keyAsIs);  // otherwise we'll inefficiently be doing map.get multiple times below.
            final String rhsStr = rhs.toString(); // to make verbose logging code simplified

            if ( this.verbose ) System.out.println ( "\n"+ CLASSNAME +": recursing @ YAML-file-location: "+ key +"/"+ keyAsIs +" = "+ rhsStr.substring(0,rhsStr.length()>181?180:rhsStr.length()) );

			//--------------------------------------------------------
			// So.. we need to keep recursing (specifically for Map & ArrayList YAML elements)
			if ( rhs instanceof Map ) {

				final newMap1 = new LinkedHashMap();
				bChangesMade = this.recursiveSearch( (Map)rhs, newMap1, _props ); // recursion call
				_outpMap.add( key, newMap1 );

			} else if ( rhs instanceof java.util.ArrayList ) {

				final ArrayList arr = (ArrayList) rhs;
				final ArrayList newarr = new ArrayList();

				// Loop thru the 'arr' Array
				for ( Object o: arr ) {
					// iterate over each element
					if ( o instanceof Map ) {
						final newMap2 = new Map();
						bChangesMade = this.recursiveSearch( (Map)rhs, newMap2, _props ); // recursion call
						newarr.add( newMap2 );
					} else if ( o instanceof java.lang.String ) {
						// by o.toString(), I'm cloning the String object.. .. so both _inpMap and _outpMap do NOT share the same String object
						newarr.add ( checkForMacros( o.toString(), _props ) );
					} else {
						System.err.println(CLASSNAME +": incomplete code #1: failure w Array-type '"+ o.getClass().getName() +"'");
						System.exit(92); // This is a serious failure. Shouldn't be happening.
					} // if-Else   o instanceof Map - (WITHIN FOR-LOOP)
				} // for Object o: arr

				_outpMap.add( key, newarr );

			} else if ( rhs instanceof java.lang.String ) {
				// by rhs.toString(), I'm cloning the String object.. .. so both _inpMap and _outpMap do NOT share the same String object
				_outpMap.add( key, checkForMacros( rhs.toString(), _props)   );

            } else {
				System.err.println(CLASSNAME +": incomplete code #2: failure w Type '"+ rhs.getClass().getName() +"'");
				System.exit(93); // This is a serious failure. Shouldn't be happening.
            }// if-else yamlPElemPatt.matcher()

        } // for loop   key: _map.keySet()

        // Now that we looped thru all keys at current recursion level..
        // .. for now nothing to do here.

        return bChangesMade;
    }

	public static final String pattStr = "[$]\\{ASUX::([^}]+)\\}";

	public static String checkForMacros( final String _s, final Properties _props) {
		if ( (_s==null) || (_props==null) ) return null;

		try {

			Pattern pattern = Pattern.compile( pattStr );
			Matcher matcher = pattern.matcher( _s );

			boolean found = false;
			String retstr = "";
			int prevIndex = 0;

			while (matcher.find()) {
				found = true;
				// System.out.println("I found the text "+matcher.group()+" starting at index "+  matcher.start()+" and ending at index "+matcher.end());    
				retstr += _s.substring( prevIndex, matcher.start() );
				final String v = _props.getProperty( matcher.group(1) ); // lookup value for ${ASUX::__} and add it to retStr
				retstr += v;
				prevIndex = matcher.end();
				// System.out.println( CLASSNAME + ": Matched: "+matcher.group(1)+" to ["+ v +"]@ index "+ matcher.start()+" and ending @ "+matcher.end());
			}

			if(found){
				if ( prevIndex < _s.length() )
					retstr += _s.substring( prevIndex );
				return retstr;
			} else {
				System.out.println("No match found.");
				return _s;
			}

		} catch (PatternSyntaxException e) {
			System.err.println(CLASSNAME + ": Unexpected Internal ERROR, while checking if '" + _s + "' matches pattern " + pattStr);
			e.printStackTrace(System.err);
			System.exit(91); // This is a serious failure. Shouldn't be happening.
		}

		return _s; // program control should never get here.
	} // function

}
