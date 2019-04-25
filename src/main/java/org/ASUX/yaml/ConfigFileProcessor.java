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

import java.util.regex.*;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *  <p>This is part of org.ASUX.yaml GitHub.com project and the <a href="https://github.com/org-asux/org-ASUX.github.io/wiki">org.ASUX.cmdline</a> GitHub.com projects.</p>
 *  <p>This class is a bunch of tools to help make it easy to work with the Configuration and Propertyfiles - while making it very human-friendly w.r.t .comments etc...</p>
 *
 * @see org.ASUX.yaml.Cmd
 * @see org.ASUX.yaml.BatchYamlProcessor
 */
public class ConfigFileProcessor {

    public static final String CLASSNAME = "org.ASUX.yaml.ConfigFileProcessor";

    // enum FileType { PROPERTIESFILE, BATCHFILE };

    /** <p>Whether you want deluge of debug-output onto System.out.</p><p>Set this via the constructor.</p>
     *  <p>It's read-only (final data-attribute).</p>
     */
    private final boolean verbose;

    /** <p>The only constructor - public/private/protected</p>
     *  @param _verbose Whether you want deluge of debug-output onto System.out.
     */
    public ConfigFileProcessor(boolean _verbose) {
        this.verbose = _verbose;
    }

    private ConfigFileProcessor() { this.verbose = false;}

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    private ArrayList<String> lines = new ArrayList<>();
    private Iterator<String> iterator = null;
    private String currentLine = null;

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

    // *  @param _filetype whether its a Properties file or a Batch file (for use by yaml BATCH command).  Of ENUM TYPE 'FileType'
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    /** As com.esotericsoftware.yamlBeans has some magic where Keys are NOT strings! ..
     *  In order for me to add new entries to the _map created by that library, I need to go thru hoops.
     *  @param _propsFilename the full path to the file (don't assume relative paths will work ALL the time)
     *  @param _ok2TrimWhiteSpace true or false, whether to REMOVE any leading and trailing whitespace.  Example: For YAML processing, trimming is devastating.
     */
    public void openFile( final String _propsFilename, final boolean _ok2TrimWhiteSpace ) {

        this.reset(); // just in case.

        String line = null;
        try {
            final java.io.InputStream istrm = new java.io.FileInputStream( _propsFilename );
            // final java.io.Reader reader2 = new java.io.InputStreamReader(is2);
            if ( this.verbose ) System.out.println( CLASSNAME + ": openBatchFile(): successfully opened file [" + _propsFilename +"]" );

			Pattern emptyPattern        = Pattern.compile( "^\\s*$" ); // empty line
			Pattern hashlinePattern     = Pattern.compile( "^#.*" ); // from start of line ONLY
			Pattern hashPattern         = Pattern.compile(  "\\s*#.*" );
			Pattern slashlinePattern    = Pattern.compile( "^//.*" ); // from start of line ONLY
			Pattern slashPattern        = Pattern.compile(  "\\s*//.*" );
			Pattern dashlinepattern = Pattern.compile( "^--.*" );

            final java.util.Scanner scanner = new java.util.Scanner( istrm );
            while (scanner.hasNextLine()) {
                line = scanner.nextLine();
                if ( this.verbose ) System.out.println( CLASSNAME + ": openBatchFile(): AS-IS line=[" + line +"]" );

                Matcher emptyMatcher = emptyPattern.matcher( line );
                if ( emptyMatcher.matches() ) continue;
                Matcher hashlineMatcher = hashlinePattern.matcher( line );
                if ( hashlineMatcher.matches() ) continue;
                Matcher slashlineMatcher = slashlinePattern.matcher( line );
                if ( slashlineMatcher.matches() ) continue;
                Matcher dashlineMatcher = dashlinepattern.matcher( line );
                if ( dashlineMatcher.matches() ) continue;

                // if we are here, then the line does ___NOT___ start with # or.. // or --
                Matcher hashMatcher     = hashPattern.matcher( line );
                if (hashMatcher.find()) {
                    if ( this.verbose ) System.out.println( CLASSNAME +": openBatchFile(): I found the text "+ hashMatcher.group() +" starting at index "+  hashMatcher.start()+ " and ending at index "+ hashMatcher.end() );    
                    line = line.substring( 0, hashMatcher.start() );
                    if ( _ok2TrimWhiteSpace ) line = line.strip(); // trim both leading and trailing whitespace
                }
                Matcher slashMatcher    = slashPattern.matcher( line );
                if (slashMatcher.find()) {
                    if ( this.verbose ) System.out.println( CLASSNAME +": openBatchFile(): I found the text "+ slashMatcher.group() +" starting at index "+  slashMatcher.start() +" and ending at index "+ slashMatcher.end() );    
                    line = line.substring( 0, slashMatcher.start() );
                    if ( _ok2TrimWhiteSpace ) line = line.strip(); // trim both leading and trailing whitespace
                }

                // after all the comment pre-processing above.. check if the line has become equivalent to empty-line.. 
                emptyMatcher = emptyPattern.matcher( line ); // after all the above trimming, is the line pretty much whitespace?
                if ( emptyMatcher.matches() ) continue;

                if ( _ok2TrimWhiteSpace ) line = line.strip(); // trim both leading and trailing whitespace
                System.out.println( CLASSNAME + ": openBatchFile(): TRIMMED line=[" + line +"]" );
                this.lines.add( line );
            }
            scanner.close();
            istrm.close();

        // scanner.hasNext() only throws a RUNTIMEEXCEPTION: IllegalStateException - if this scanner is closed
        // scanner.next() only throws a RUNTIMEEXCEPTION: NoSuchElementException - if no more tokens are available

		} catch (PatternSyntaxException e) {
			System.err.println(CLASSNAME + ": openBatchFile(): Unexpected Internal ERROR, while checking for patterns for line= [" + line +"]" );
			e.printStackTrace(System.err);
			System.exit(91); // This is a serious failure. Shouldn't be happening.
        } catch (java.io.IOException e) {
            e.printStackTrace(System.err);
            System.err.println( CLASSNAME + ": openBatchFile(): Failure to read/write IO for file ["+ _propsFilename +"]" );
            System.exit(102);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.err.println( CLASSNAME + ": openBatchFile(): Unknown Internal error:.");
            System.exit(103);
        }
    }

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    /** This class aims to mimic java.util.Scanner's hasNextLine() and nextLine()
     *  @return true or false
     */
    public boolean hasNextLine() {
        if ( this.iterator == null ) {
            this.iterator = this.lines.iterator();
        }
        return this.iterator.hasNext();
    }

    //===========================================================================
    /** This class aims to mimic java.util.Scanner's hasNextLine() and nextLine()
     *  @return either null (graceful failure) or the next string in the list of lines
     */
    public String nextLine() {
        if ( this.iterator.hasNext() ) {
            this.currentLine = this.iterator.next();
        } else {
            this.currentLine = null; // dont create chaos in code in other CLASSES that invokes on hasNextLine() and nextLine()
        }
        return this.currentLine;
    }

    //===========================================================================
    /** This class aims to mimic java.util.Scanner's hasNextLine() and nextLine()
     *  @return either null (graceful failure) or the next string in the list of lines
     */
    public String currentLine() {
        if ( this.currentLine == null ) {
            return this.iterator.next();
        } else {
            return this.currentLine;
        }
    }

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    /**
     * For reading Properties, it is useful to return a KVPair.. from functions like isPropertyLine()
     */
    public class KVPair {
        public String key = null;
        public String value = null;
    }

    //==================================
    /** This function helps detect if the current line pointed to by this.currentLine() contains a property entry (a.k.a. a KVPair entry of the form key=value)
     * @return either null.. or, the Key + Value (an instance of the ConfigFileProcessor.KVPair class) detected in the current line of batch file
     */
    public KVPair isPropertyLine() {
        final String line = this.currentLine(); // remember the line is most likely already trimmed
        if ( line == null )
            return null;

        KVPair kv = null;
        try {
            Pattern propsPattern = Pattern.compile( "^\\s*([a-zA-Z][a-zA-Z0-9]*)=(\\S\\S*)\\s*$" ); // empty line
            Matcher propsMatcher    = propsPattern.matcher( line );
            if (propsMatcher.find()) {
                // System.out.println( CLASSNAME +": I found the text "+ propsMatcher.group() +" starting at index "+  propsMatcher.start() +" and ending at index "+ propsMatcher.end() );    
                kv = new KVPair();
                kv.key = propsMatcher.group(1); // line.substring( propsMatcher.start(), propsMatcher.end() );
                kv.value = propsMatcher.group(2);
                System.out.println( "\t KVPair=[" + kv.key +","+ kv.value +"]" );
            }
		} catch (PatternSyntaxException e) {
			System.err.println(CLASSNAME + ": isPropertyLine(): Unexpected Internal ERROR, while checking for patterns for line= [" + line +"]" );
			e.printStackTrace(System.err);
			System.exit(91); // This is a serious failure. Shouldn't be happening.
        }

        return kv;
    }

    //==================================
    /** This function helps detect if the current line pointed to by this.currentLine() contains a 'saveAs ___' entry
     *  @return String the argument provided to the saveAs command (if no argument provided, this returns null - quiet &amp; graceful degradation)
     */
    public String isSaveAsLine() {
        final String line = this.currentLine(); // remember the line is most likely already trimmed
        if ( line == null ) return null;
        try {
            Pattern saveAsPattern = Pattern.compile( "^\\s*saveAs\\s\\s*(\\S.*\\S)\\s*$" ); // empty line
            Matcher saveAsMatcher    = saveAsPattern.matcher( line );
            if (saveAsMatcher.find()) {
                // System.out.println( CLASSNAME +": I found the text "+ saveAsMatcher.group() +" starting at index "+  saveAsMatcher.start() +" and ending at index "+ saveAsMatcher.end() );    
                final String sa = saveAsMatcher.group(1); // line.substring( saveAsMatcher.start(), saveAsMatcher.end() );
                System.out.println( "\t SaveAs=[" + sa +"]" );
                return sa;
            }
		} catch (PatternSyntaxException e) {
			System.err.println(CLASSNAME + ": isSaveAsLine(): Unexpected Internal ERROR, while checking for patterns for line= [" + line +"]" );
			e.printStackTrace(System.err);
			System.exit(91); // This is a serious failure. Shouldn't be happening.
        }
        return null;
    }

    //==================================
    /** This function helps detect if the current line pointed to by this.currentLine() contains a 'useAsInput ___' entry
     *  @return String the argument provided to the useAsInput command (if no argument provided, this returns null - quiet &amp; graceful degradation)
     */
    public String isUseAsInputLine() {
        final String line = this.currentLine(); // remember the line is most likely already trimmed
        if ( line == null ) return null;
        try {
            Pattern useAsInputPattern = Pattern.compile( "^\\s*useAsInput\\s\\s*(\\S.*\\S)\\s*$" ); // empty line
            Matcher useAsInputMatcher    = useAsInputPattern.matcher( line );
            if (useAsInputMatcher.find()) {
                // System.out.println( CLASSNAME +": I found the text "+ useAsInputMatcher.group() +" starting at index "+  useAsInputMatcher.start() +" and ending at index "+ useAsInputMatcher.end() );    
                final String sa = useAsInputMatcher.group(1); // line.substring( useAsInputMatcher.start(), useAsInputMatcher.end() );
                System.out.println( "\t useAsInput=[" + sa +"]" );
                return sa;
            }
		} catch (PatternSyntaxException e) {
			System.err.println(CLASSNAME + ": isUseAsInputLine(): Unexpected Internal ERROR, while checking for patterns for line= [" + line +"]" );
			e.printStackTrace(System.err);
			System.exit(91); // This is a serious failure. Shouldn't be happening.
        }
        return null;
    }

    //==================================
    /** This function helps detect if the current line pointed to by this.currentLine() contains just the word 'foreach' (nothing else other than comments and whitespace)
     * This keyword 'foreach' indicates the beginning of a looping-construct within the batch file.
     * @return true of false, if 'foreach' was detected in the current line of batch file
     */
    public boolean isForEachLine() {
        final String line = this.currentLine(); // remember the line is most likely already trimmed
        if ( line == null ) return false;
        return line.equalsIgnoreCase("foreach");
    }

    //==================================
    /** This function helps detect if the current line pointed to by this.currentLine() contains just the word 'end' (nothing else other than comments and whitespace)
     * This keyword 'end' indicates the END of the looping-construct within the batch file
     * @return true of false, if 'end' was detected in the current line of batch file
     */
    public boolean isEndLine() {
        final String line = this.currentLine(); // remember the line is most likely already trimmed
        if ( line == null ) return false;
        return line.equalsIgnoreCase("end");
    }

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    /**
     * This function should be called *AFTER* all the various is___() functions/methods have been called.
     * This function should NOT be called BEFORE isSaveAs() and isUseAsInput(), as this function will get you confused.
     * @return String just for the command (whether 'yaml' 'aws' ..)
     */
    public String getCommand() {
        try {
            final java.util.Scanner scanner = new java.util.Scanner( this.currentLine() );
            scanner.useDelimiter("\\s\\s*");
            if (scanner.hasNext()) { // default whitespace delimiter used by a scanner
                final String cmd = scanner.next();
                System.out.println( "\t Command=[" + cmd +"]" );
                return cmd;
            } // if
            return null;
            // scanner.hasNext() only throws a RUNTIMEEXCEPTION: IllegalStateException - if this scanner is closed
            // scanner.next() only throws a RUNTIMEEXCEPTION: NoSuchElementException - if no more tokens are available
		} catch (Exception e) {
			System.err.println(CLASSNAME + ": getCommand(): Unexpected Internal ERROR, while checking for patterns for this.currentLine()= [" + this.currentLine() +"]" );
			e.printStackTrace(System.err);
			System.exit(91); // This is a serious failure. Shouldn't be happening.
            return null;
        }
    }

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    /** This class aims to mimic java.util.Scanner's hasNextLine() and nextLine().  Scanner has reset().  I prefer rewind().  reset() is still defined, but has draconian-implications - as if openBatchFile() was never called!
     */
    public void rewind() {
        this.iterator = null;
    }

    //---------------------------------
    /** This class aims to mimic java.util.Scanner's hasNextLine() and nextLine() and reset().<br>reset() has draconian-implications - as if openBatchFile() was never called!
     */
    public void reset() {
        this.lines = new ArrayList<>();
        this.iterator = null;
    }

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    // For unit-testing purposes only
    public static void main(String[] args) {
        final ConfigFileProcessor o = new ConfigFileProcessor(false);
        o.openFile(args[0], true);
        while (o.hasNextLine()) {
            System.out.println(o.nextLine());

            o.isPropertyLine();
            final KVPair kv = o.isPropertyLine(); // could be null, implying NOT a kvpair

            o.isForEachLine();
            o.isEndLine();
            o.isSaveAsLine();
            o.isUseAsInputLine();
            final boolean bForEach = o.isForEachLine();
            if ( bForEach ) System.out.println("\t Loop begins=[" + bForEach + "]");
            final boolean bEndLine = o.isEndLine();
            if ( bEndLine ) System.out.println("\t Loop ENDS=[" + bEndLine + "]");

            o.getCommand();
        }
    }

}
