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

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;

/**
 *  <p>This is part of org.ASUX.yaml GitHub.com project and the <a href="https://github.com/org-asux/org-ASUX.github.io/wiki">org.ASUX.cmdline</a> GitHub.com projects.</p>
 *  <p>This class is a bunch of tools to help make it easy to work with the Configuration and Propertyfiles - while making it very human-friendly w.r.t .comments etc...</p>
 *
 * @see org.ASUX.yaml.Cmd
 * @see org.ASUX.yaml.BatchYamlProcessor
 */
public class BatchFileGrammer implements java.io.Serializable {

    private static final long serialVersionUID = 5L;
    public static final String CLASSNAME = "org.ASUX.yaml.BatchFileGrammer";
    public static final String FOREACH = "foreach";
    public static final String FOREACH_INDEX = "foreach.index"; // which iteration # (Int) are we in within the loop.
    public static final String FOREACH_ITER_VALUE = "foreach.iteration.value"; // if 'foreach' ends up iterating over an array of strings, then you can get each string's value this way.
    // enum FileType { YAMLPROPERTIESFILE, BATCHFILE };

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

    /** <p>The only constructor - public/private/protected</p>
     *  @param _verbose Whether you want deluge of debug-output onto System.out.
     */
    public BatchFileGrammer(boolean _verbose) {
        this.verbose = _verbose;
    }

    private BatchFileGrammer() { this.verbose = false;}

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

    /**
     * For reading Properties, it is useful to return a KVPair.. from functions like isPropertyLine()
     */
    public class KVPair {
        public String key = null;
        public String value = null;
    }

    //==================================

    private final boolean verbose;

    private String fileName = null;
    private ArrayList<String> lines = new ArrayList<>();
    private transient Iterator<String> iterator = null;

    private int currentLineNum = -1;
    private String currentLine = null;

    private boolean bIsPropertyLine = false;
    private boolean bIsPrintLine = false;
    private boolean bIsSaveToLine = false;
    private boolean bIsUseAsInputLine = false;
    private boolean bIsForEachLine = false;
    private boolean bIsEndLine = false;
    private boolean bIsBatchLine = false;

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

    // *  @param _filetype whether its a Properties file or a Batch file (for use by yaml BATCH command).  Of ENUM TYPE 'FileType'
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    /** As com.esotericsoftware.yamlBeans has some magic where Keys are NOT strings! ..
     *  In order for me to add new entries to the _map created by that library, I need to go thru hoops.
     *  @param _filename the full path to the file (don't assume relative paths will work ALL the time)
     *  @param _ok2TrimWhiteSpace true or false, whether to REMOVE any leading and trailing whitespace.  Example: For YAML processing, trimming is devastating.
     */
    public boolean openFile( final String _filename, final boolean _ok2TrimWhiteSpace ) throws Exception {

        this.reset(); // just in case.
        this.fileName = _filename;

        String line = null;
        try {
            final java.io.InputStream istrm = new java.io.FileInputStream( this.fileName );
            // final java.io.Reader reader2 = new java.io.InputStreamReader(is2);
            if ( this.verbose ) System.out.println( CLASSNAME + ": openBatchFile(): successfully opened file [" + this.fileName +"]" );

            // different way to detect comments, and to remove them.
			Pattern emptyPattern        = Pattern.compile( "^\\s*$" ); // empty line
			Pattern hashlinePattern     = Pattern.compile( "^#.*" ); // from start of line ONLY
			Pattern hashPattern         = Pattern.compile(  "\\s*#.*" );
			Pattern slashlinePattern    = Pattern.compile( "^//.*" ); // from start of line ONLY
			Pattern slashPattern        = Pattern.compile(  "\\s*//.*" );
			Pattern dashlinepattern = Pattern.compile( "^--.*" );

            final java.util.Scanner scanner = new java.util.Scanner( istrm );
            while (scanner.hasNextLine()) {
                if ( this.currentLineNum < 0 ) this.currentLineNum = 1; else this.currentLineNum ++;
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
                if ( this.verbose ) System.out.println( CLASSNAME + ": openBatchFile(): TRIMMED line=[" + line +"]" );
                this.lines.add( line );
            }
            scanner.close();
            istrm.close();

            this.rewind(); // rewing the pointer to the 1st line in the batch file.
            return true;

        // scanner.hasNext() only throws a RUNTIMEEXCEPTION: IllegalStateException - if this scanner is closed
        // scanner.next() only throws a RUNTIMEEXCEPTION: NoSuchElementException - if no more tokens are available

		} catch (PatternSyntaxException e) {
			e.printStackTrace(System.err);
			System.err.println(CLASSNAME + ": openBatchFile(): Unexpected Internal ERROR, while checking for patterns for line= [" + line +"]" );
			System.exit(91); // This is a serious failure. Shouldn't be happening.
        } catch (java.io.IOException e) {
            e.printStackTrace(System.err);
            System.err.println( CLASSNAME + ": openBatchFile(): \n\nFailure to read/write IO for file ["+ this.fileName +"]" );
            // System.exit(102);  Let the commands do a better job of informing the end-user.
        // } catch (Exception e) {
        //     e.printStackTrace(System.err);
        //     System.err.println( CLASSNAME + ": openBatchFile(): Unknown Internal error:.");
        //     System.exit(103);
        }
        return false;
    }

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

    /** This class aims to mimic java.util.Scanner's hasNextLine() and nextLine() and reset().<br>reset() has draconian-implications - as if openBatchFile() was never called!
     */
    public void reset() {
        this.fileName = null;
        this.lines = new ArrayList<>();
        this.currentLine = null;
        this.currentLineNum = -1;
        this.iterator = null;

        // these are to be reset in ONLY 2 locations:  reset() function & rewind() function
        this.kv = null;  // one of th dirty-tricks class-variables that are hidden deep inside the code :-)
        this.sa = null;  // one of th dirty-tricks class-variables that are hidden deep inside the code :-)
        this.sbl = null; // one of th dirty-tricks class-variables that are hidden deep inside the code :-)
        this.uai = null; // one of th dirty-tricks class-variables that are hidden deep inside the code :-)
    }

    /** This function is exclusively for use within the go() - the primary function within this class - to make this very efficient when responding to the many isXXX() methods in this class.
     */
    private void resetFlagsForEachLine() {
        this.bIsPropertyLine = false;
        this.bIsPrintLine = false;
        this.bIsSaveToLine = false;
        this.bIsUseAsInputLine = false;
        this.bIsForEachLine = false;
        this.bIsEndLine = false;
        this.bIsBatchLine = false;


        // do NOT touch these variables in this fucntion.  They should ONLY be reset inside reset()!
        // this.kv = null;  // one of th dirty-tricks class-variables that are hidden deep inside the code :-)
        // this.sa = null;  // one of th dirty-tricks class-variables that are hidden deep inside the code :-)
        // this.sbl = null; // one of th dirty-tricks class-variables that are hidden deep inside the code :-)
        // this.uai = null; // one of th dirty-tricks class-variables that are hidden deep inside the code :-)
    }

    //---------------------------------
    /** This class aims to mimic java.util.Scanner's hasNextLine() and nextLine().  Scanner has reset().  I prefer rewind().  reset() is still defined, but has draconian-implications - as if openBatchFile() was never called!
     */
    public void rewind() {
        this.currentLine = null;
        this.currentLineNum = -1;
        this.iterator = null;

        this.hasNextLine(); // will initialize all of the above 3 variables.

        // these are to be reset in ONLY 2 locations:  reset() function & rewind() function
        this.kv = null;  // one of th dirty-tricks class-variables that are hidden deep inside the code :-)
        this.sa = null;  // one of th dirty-tricks class-variables that are hidden deep inside the code :-)
        this.sbl = null; // one of th dirty-tricks class-variables that are hidden deep inside the code :-)
        this.uai = null; // one of th dirty-tricks class-variables that are hidden deep inside the code :-)
    }

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    /** This class aims to mimic java.util.Scanner's hasNextLine() and nextLine()
     *  @return true or false
     */
    public boolean hasNextLine() {
        if ( this.iterator == null ) {
            this.iterator = this.lines.iterator();
            this.currentLineNum = 0; // Both -1 and 0 are invalid values.  1st line # === '1'.  At least it helps me debug.
            // Not sure re:  this.currentLine = ???? ... why on earth is code expecting a valid value for currentLine without invoking next()?
        }
        return this.iterator.hasNext();
    }

    //===========================================================================
    /** This class aims to mimic java.util.Scanner's hasNextLine() and nextLine()
     *  @return either null (graceful failure) or the next string in the list of lines
     */
    public String nextLine() {
        if ( this.hasNextLine() ) {
            this.currentLine = this.iterator.next();
            this.currentLineNum ++;
        } else {
            this.currentLine = null; // dont create chaos in code in other CLASSES that invokes on hasNextLine() and nextLine()
            this.currentLineNum = -1;
        }
        resetFlagsForEachLine(); // so that the isXXX() methods invoked of this class -- now that we're on NEW/NEXT line -- will NOT take a shortcut!
        return this.currentLine;
    }

    //===========================================================================
    /** This class aims to mimic java.util.Scanner's hasNextLine() and nextLine()
     *  @return either null (graceful failure) or the next string in the list of lines
     */
    public String currentLine() {

        if ( this.currentLine == null ) {
            if (   !   hasNextLine() )
                return null;
            else
                return this.nextLine(); // this will Not be null.. just because of the call to hasNextLine() above.
        } else {
            return this.currentLine;
        }
    }

    /**
     * Use this in conjunction with this.currentLine().    Line numbers start with 1, as all text-editors show.  If there is an error in the file being processed, the error-message will note the line#.
     * @return the line # within the process that is 'current'.  It will be -1, if anything failed in 'reading in' the fileName.
     */
    public int getLineNum()  {
        return this.currentLineNum;
    }

    /**
     * What was the file that class has ingested for processing
     * @return the fileName exactly as passed to go() method.
     */
    public String getFileName()  {
        return this.fileName;
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    // This is a dirty trick to improve performance by caching.  This'll will work as long as the hasNextLine() and getNextLine() are the only way to get data from this class.
    private KVPair kv = null;

    /** This function helps detect if the current line pointed to by this.currentLine() contains a property entry (a.k.a. a KVPair entry of the form key=value)
     * @return either null.. or, the Key + Value (an instance of the BatchFileGrammer.KVPair class) detected in the current line of batch file
     */
    public KVPair isPropertyLine() {
        if ( this.bIsPropertyLine ) return this.kv; // we've already executed the code below - SPECIFICALLY for the currentLine!

        final String line = this.currentLine(); // remember the line is most likely already trimmed
        if ( line == null )
            return null;

        this.kv = null;
        try {
            Pattern propsPattern = Pattern.compile( "^\\s*properties \\s*([a-zA-Z][a-zA-Z0-9]*)=(\\S\\S*)\\s*$" ); // empty line
            Matcher propsMatcher    = propsPattern.matcher( line );
            if (propsMatcher.find()) {
                if ( this.verbose ) System.out.println( CLASSNAME +": I found the text "+ propsMatcher.group() +" starting at index "+  propsMatcher.start() +" and ending at index "+ propsMatcher.end() );    
                kv = new KVPair();
                kv.key = propsMatcher.group(1); // line.substring( propsMatcher.start(), propsMatcher.end() );
                kv.value = propsMatcher.group(2);
                if ( this.verbose ) System.out.println( "\t KVPair=[" + kv.key +","+ kv.value +"]" );
                this.bIsPropertyLine = true;
            }

        } catch (PatternSyntaxException e) {
			e.printStackTrace(System.err);
			System.err.println(CLASSNAME + ": isPropertyLine(): Unexpected Internal ERROR, while checking for patterns for line= [" + line +"]" );
			System.exit(91); // This is a serious failure. Shouldn't be happening.
        }

        return kv;
    }

    //==================================

    /** This function helps detect if the current line pointed to by this.currentLine() contains a property entry (a.k.a. a KVPair entry of the form key=value)
     * @return either null.. or, the Key + Value (an instance of the BatchFileGrammer.KVPair class) detected in the current line of batch file
     */
    public String isPrintLine() {
        // if ( this.bIsPrintLine ) return _________; 
        final String line = this.currentLine(); // remember the line is most likely already trimmed
        if ( line == null ) return null;
        try {
            Pattern printPattern = Pattern.compile( "^\\s*print\\s\\s*(\\S.*\\S)\\s*$" ); // empty line
            Matcher printMatcher    = printPattern.matcher( line );
            if (printMatcher.find()) {
                if ( this.verbose ) System.out.println( CLASSNAME +": I found the text "+ printMatcher.group() +" starting at index "+  printMatcher.start() +" and ending at index "+ printMatcher.end() );    
                final String prexpr = printMatcher.group(1); // line.substring( printMatcher.start(), printMatcher.end() );
                if ( this.verbose ) System.out.println( "\t print=[" + prexpr +"]" );
                this.bIsPrintLine = true;
                return prexpr;
            }
		} catch (PatternSyntaxException e) {
			e.printStackTrace(System.err);
			System.err.println(CLASSNAME + ": isprintLine(): Unexpected Internal ERROR, while checking for patterns for line= [" + line +"]" );
			System.exit(91); // This is a serious failure. Shouldn't be happening.
        }
        return null;

    }

    //==================================
    // This is a dirty trick to improve performance by caching.  This'll will work as long as the hasNextLine() and getNextLine() are the only way to get data from this class.
    private String sa = null;

    /** This function helps detect if the current line pointed to by this.currentLine() contains a 'saveTo ___' entry
     *  @return String the argument provided to the saveTo command (if no argument provided, this returns null - quiet &amp; graceful degradation)
     */
    public String isSaveToLine() {
        if ( this.bIsSaveToLine ) return this.sa;

        final String line = this.currentLine(); // remember the line is most likely already trimmed
        if ( line == null ) return null;
        try {
            Pattern saveToPattern = Pattern.compile( "^\\s*saveTo\\s\\s*(\\S.*\\S)\\s*$" ); // empty line
            Matcher saveToMatcher    = saveToPattern.matcher( line );
            if (saveToMatcher.find()) {
                if ( this.verbose ) System.out.println( CLASSNAME +": I found the text "+ saveToMatcher.group() +" starting at index "+  saveToMatcher.start() +" and ending at index "+ saveToMatcher.end() );    
                this.sa = saveToMatcher.group(1); // line.substring( saveToMatcher.start(), saveToMatcher.end() );
                if ( this.verbose ) System.out.println( "\t SaveTo=[" + this.sa +"]" );
                this.bIsSaveToLine = true;
                return this.sa;
            }
        } catch (PatternSyntaxException e) {
			e.printStackTrace(System.err);
			System.err.println(CLASSNAME + ": isSaveToLine(): Unexpected Internal ERROR, while checking for patterns for line= [" + line +"]" );
			System.exit(91); // This is a serious failure. Shouldn't be happening.
        }
        return null;
    }

    //==================================
    // This is a dirty trick to improve performance by caching.  This'll will work as long as the hasNextLine() and getNextLine() are the only way to get data from this class.
    private String uai = null;

    /** This function helps detect if the current line pointed to by this.currentLine() contains a 'useAsInput ___' entry
     *  @return String the argument provided to the useAsInput command (if no argument provided, this returns null - quiet &amp; graceful degradation)
     */
    public String isUseAsInputLine() {
        if ( this.bIsUseAsInputLine ) return this.uai;

        final String line = this.currentLine(); // remember the line is most likely already trimmed
        if ( line == null ) return null;
        try {
            Pattern useAsInputPattern = Pattern.compile( "^\\s*useAsInput\\s\\s*(\\S.*\\S)\\s*$" ); // empty line
            Matcher useAsInputMatcher    = useAsInputPattern.matcher( line );
            if (useAsInputMatcher.find()) {
                if ( this.verbose ) System.out.println( CLASSNAME +": I found the text "+ useAsInputMatcher.group() +" starting at index "+  useAsInputMatcher.start() +" and ending at index "+ useAsInputMatcher.end() );    
                this.uai = useAsInputMatcher.group(1); // line.substring( useAsInputMatcher.start(), useAsInputMatcher.end() );
                if ( this.verbose ) System.out.println( "\t useAsInput=[" + this.uai +"]" );
                this.bIsUseAsInputLine = true;
                return this.uai;
            }
		} catch (PatternSyntaxException e) {
			e.printStackTrace(System.err);
			System.err.println(CLASSNAME + ": isUseAsInputLine(): Unexpected Internal ERROR, while checking for patterns for line= [" + line +"]" );
			System.exit(91); // This is a serious failure. Shouldn't be happening.
        }
        return null;
    }

    //==================================
    // This is a dirty trick to improve performance by caching.  This'll will work as long as the hasNextLine() and getNextLine() are the only way to get data from this class.
    private String sbl = null;

    /** This function helps detect if the current line pointed to by this.currentLine() contains a 'batch ___' entry - which will cause a SUB-BATCH cmd to be triggered
     *  @return String the argument provided to the Batch command (if no argument provided, this returns null - quiet &amp; graceful degradation)
     */
    public String isSubBatchLine() {
        if ( this.bIsBatchLine ) return this.sbl;

        final String line = this.currentLine(); // remember the line is most likely already trimmed
        if ( line == null ) return null;
        try {
            Pattern batchPattern = Pattern.compile( "^\\s*batch\\s\\s*(\\S.*\\S)\\s*$" ); // empty line
            Matcher batchMatcher    = batchPattern.matcher( line );
            if (batchMatcher.find()) {
                if ( this.verbose ) System.out.println( CLASSNAME +": I found the text "+ batchMatcher.group() +" starting at index "+  batchMatcher.start() +" and ending at index "+ batchMatcher.end() );    
                this.sbl = batchMatcher.group(1); // line.substring( batchMatcher.start(), batchMatcher.end() );
                if ( this.verbose ) System.out.println( "\t batch=[" + this.sbl +"]" );
                this.bIsBatchLine = true;
                return this.sbl;
            }
        } catch (PatternSyntaxException e) {
			e.printStackTrace(System.err);
			System.err.println(CLASSNAME + ": isBatchLine(): Unexpected Internal ERROR, while checking for patterns for line= [" + line +"]" );
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
        // if ( this.bIsForEachLine ) return true; // I do NOT want to be too overconfident about these boolean class-variables, especially when the logic in this function is SO SIMPLE!

        final String line = this.currentLine(); // remember the line is most likely already trimmed
        if ( line == null ) return false;
        final boolean b = line.equalsIgnoreCase(FOREACH);
        this.bIsForEachLine = b;
        return b;
    }

    //==================================
    /** This function helps detect if the current line pointed to by this.currentLine() contains just the word 'end' (nothing else other than comments and whitespace)
     * This keyword 'end' indicates the END of the looping-construct within the batch file
     * @return true of false, if 'end' was detected in the current line of batch file
     */
    public boolean isEndLine() {
        // if ( this.bIsEndLine ) return true; // I do NOT want to be too overconfident about these boolean class-variables, especially when the logic in this function is SO SIMPLE!

        final String line = this.currentLine(); // remember the line is most likely already trimmed
        if ( line == null ) return false;
        final boolean b = line.equalsIgnoreCase("end");
        this.bIsEndLine = b;
        return b;
    }

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    /**
     * This function should be called *AFTER* all the various is___() functions/methods have been called.
     * This function should NOT be called BEFORE isSaveToLine() and isUseAsInputLine(), as this function will get you confused.
     * @return String just for the command (whether 'yaml' 'aws' ..)
     */
    public String getCommand() {
        if ( this.bIsPropertyLine ||  this.bIsPrintLine ||  this.bIsSaveToLine ||  this.bIsUseAsInputLine || this.bIsForEachLine || this.bIsEndLine || this.bIsBatchLine )
            return null; // It can't a generic command like YAML or AWS..

        try {
            final java.util.Scanner scanner = new java.util.Scanner( this.currentLine() );
            scanner.useDelimiter("\\s\\s*");

            if (scanner.hasNext()) { // default whitespace delimiter used by a scanner
                final String cmd = scanner.next();
                System.out.println( "\t Command=[" + cmd +"]" );
                scanner.close();
                return cmd;
            } // if

            scanner.close();
            return null;
            // scanner.hasNext() only throws a RUNTIMEEXCEPTION: IllegalStateException - if this scanner is closed
            // scanner.next() only throws a RUNTIMEEXCEPTION: NoSuchElementException - if no more tokens are available
		} catch (Exception e) {
			e.printStackTrace(System.err);
			System.err.println(CLASSNAME + ": getCommand(): Unexpected Internal ERROR, while checking for patterns for line # "+ this.currentLineNum +"this.currentLine()= [" + this.currentLine() +"]" );
			System.exit(91); // This is a serious failure. Shouldn't be happening.
            return null;
        }
    }

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    /** This deepClone function is unnecessary, if you can invoke org.apache.commons.lang3.SerializationUtils.clone(this)
     *  @param _orig what you want to deep-clone
     *  @return a deep-cloned copy, created by serializing into a ByteArrayOutputStream and reading it back (leveraging ObjectOutputStream)
     */
    public static BatchFileGrammer deepClone(BatchFileGrammer _orig) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(_orig);
            
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            final BatchFileGrammer newobj = (BatchFileGrammer) ois.readObject();

            // because this class has at least one TRANSIENT class-variable.. ..
            newobj.iterator = newobj.lines.iterator();
            for ( int ix = 0; ix < newobj.currentLineNum; ix++ )
                newobj.iterator.next(); // This will advance this.iterator to the right position, as java.lang.Iterator is NOT clonable/NOT SERIALIZABLE.
                // we rarely CLONE an object of this class, when it's still pointing to line #1.  So, this ABOVE for-loop is just fine.

            return newobj;

        } catch (java.io.IOException e) {
			e.printStackTrace(System.err);
            return null;
        } catch (ClassNotFoundException e) {
			e.printStackTrace(System.err);
            return null;
        }
    }

        //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    // For unit-testing purposes only
    public static void main(String[] args) {
        try {
            final BatchFileGrammer o = new BatchFileGrammer(false);
            o.openFile( args[0], true );
            while (o.hasNextLine()) {
                System.out.println(o.nextLine());

                o.isPropertyLine();
                // final KVPair kv = o.isPropertyLine(); // could be null, implying NOT a kvpair

                o.isForEachLine();
                o.isEndLine();
                o.isSaveToLine();
                o.isUseAsInputLine();
                final boolean bForEach = o.isForEachLine();
                if ( bForEach ) System.out.println("\t Loop begins=[" + bForEach + "]");
                final boolean bEndLine = o.isEndLine();
                if ( bEndLine ) System.out.println("\t Loop ENDS=[" + bEndLine + "]");

                o.getCommand();
            }
		} catch (Exception e) {
			e.printStackTrace(System.err);
			System.err.println(CLASSNAME + ": main(): Unexpected Internal ERROR, while processing " + ((args==null || args.length<=0)?"[No CmdLine Args":args[0]) +"]" );
			System.exit(91); // This is a serious failure. Shouldn't be happening.
        }
    }

}
