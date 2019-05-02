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
    public static final String FOREACHCMD = "foreach";
    // enum FileType { YAMLPROPERTIESFILE, BATCHFILE };

    //--------------------------------------------------------
    private final boolean verbose;

    private String fileName = null;
    private ArrayList<String> lines = new ArrayList<>();
    private transient Iterator<String> iterator = null;  // <<- transient class variable.  Will not be part of deepClone() method.

    private int currentLineNum = -1;
    private String currentLine = null;

    enum BatchCmdType { Cmd_Properties, Cmd_Print, Cmd_SaveTo, Cmd_UseAsInput, Cmd_Foreach, Cmd_End, Cmd_Batch, Cmd_Any };
    private BatchCmdType whichCmd = BatchCmdType.Cmd_Any;

    private Tools.Tuple<String,String> propertiesKV = null;
    private String printExpr = null;
    private String saveTo = null;
    private String useAsInput = null;
    private String subBatchFile = null;


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
     * After successfully opening a file.. you can get state-details (which point of the Batchfile are we at currently).
     * @return something like: Batchfile [@mapsBatch1.txt] @ lime# 2 = [useAsInput @filename]
     */
    public String getState() {
        if ( this.fileName == null || this.currentLine == null || this.currentLineNum <= 0 )
            return "Batchfile ["+ this.fileName +"] is in invalid state";
        else
            return "Batchfile: ["+ this.fileName +"] @ line# "+ this.currentLineNum +" = ["+ this.currentLine +"]";
    }

    /** This class aims to mimic java.util.Scanner's hasNextLine() and nextLine() and reset().<br>reset() has draconian-implications - as if openBatchFile() was never called!
     */
    public void reset() {
        this.fileName = null;
        this.lines = new ArrayList<>();

        this.currentLine = null;
        this.currentLineNum = -1;
        this.iterator = null;
        this.resetFlagsForEachLine();
    }

    /** This function is exclusively for use within the go() - the primary function within this class - to make this very efficient when responding to the many isXXX() methods in this class.
     */
    private void resetFlagsForEachLine() {
        this.whichCmd = BatchCmdType.Cmd_Any;

        this.propertiesKV = null;
        this.printExpr = null;
        this.saveTo = null;
        this.useAsInput = null;
        this.subBatchFile = null;
    }

    //---------------------------------
    /** This class aims to mimic java.util.Scanner's hasNextLine() and nextLine().  Scanner has reset().  I prefer rewind().  reset() is still defined, but has draconian-implications - as if openBatchFile() was never called!
     */
    public void rewind() {
        this.currentLineNum = 0; // Both -1 and 0 are invalid values.  1st line # is always === '1'.  That way it helps the user to debug batch-file issues.
        this.currentLine = null; // ... why on earth is code expecting a valid value for currentLine without invoking next()?
        this.iterator = this.lines.iterator();

        this.resetFlagsForEachLine();
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

    public int getCommandCount()  {
        return (this.lines != null) ? this.lines.size(): -1;
    }

    //===========================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //===========================================================================
    /** This class aims to mimic java.util.Scanner's hasNextLine() and nextLine()
     *  @return true or false
     */
    public boolean hasNextLine() {
        if ( this.lines == null ) return false;
        if ( this.iterator == null ) {
            this.rewind();
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
            // WARNING: Do not invoke this.rewind() or this.reset().  It will INCORRECTLY change the value of this.iterator
        }
        resetFlagsForEachLine(); // so that the isXXX() methods invoked of this class -- now that we're on NEW/NEXT line -- will NOT take a shortcut!
        this.determineCmdType();
        return this.currentLine;
    }

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

    // *  @param _filetype whether its a Properties file or a Batch file (for use by yaml BATCH command).  Of ENUM TYPE 'FileType'
    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /** As com.esotericsoftware.yamlBeans has some magic where Keys are NOT strings! ..
     *  In order for me to add new entries to the _map created by that library, I need to go thru hoops.
     *  @param _filename the full path to the file (don't assume relative paths will work ALL the time)
     *  @param _ok2TrimWhiteSpace true or false, whether to REMOVE any leading and trailing whitespace.  Example: For YAML processing, trimming is devastating.
     *  @return true (successful and NO errors) or false (any error or issue/trouble whatsoever)
     *  @throws java.lang.Exception either this function throws or will return false.
     */
    public boolean openFile( final String _filename, final boolean _ok2TrimWhiteSpace ) throws Exception {

        this.reset(); // just in case.
        this.fileName = _filename;

        String line = null;
        try {
            java.util.Scanner scanner = null;
            if ( this.fileName.startsWith("@") ) {
                final java.io.InputStream istrm = new java.io.FileInputStream( this.fileName.substring(1) );
                // final java.io.Reader reader2 = new java.io.InputStreamReader(is2);
                scanner = new java.util.Scanner( istrm );
            } else {
                scanner = new java.util.Scanner( this.fileName );
            }

            if ( this.verbose ) System.out.println( CLASSNAME + ": openBatchFile(): successfully opened file [" + this.fileName +"]" );

            // different way to detect comments, and to remove them.
			Pattern emptyPattern        = Pattern.compile( "^\\s*$" ); // empty line
			Pattern hashlinePattern     = Pattern.compile( "^#.*" ); // from start of line ONLY
			Pattern hashPattern         = Pattern.compile(  "\\s*#.*" );
			Pattern slashlinePattern    = Pattern.compile( "^//.*" ); // from start of line ONLY
			Pattern slashPattern        = Pattern.compile(  "\\s*//.*" );
			Pattern dashlinepattern = Pattern.compile( "^--.*" );

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
                    if ( _ok2TrimWhiteSpace ) line = line.trim(); // trim both leading and trailing whitespace
                }
                Matcher slashMatcher    = slashPattern.matcher( line );
                if (slashMatcher.find()) {
                    if ( this.verbose ) System.out.println( CLASSNAME +": openBatchFile(): I found the text "+ slashMatcher.group() +" starting at index "+  slashMatcher.start() +" and ending at index "+ slashMatcher.end() );    
                    line = line.substring( 0, slashMatcher.start() );
                    if ( _ok2TrimWhiteSpace ) line = line.trim(); // trim both leading and trailing whitespace
                }

                // after all the comment pre-processing above.. check if the line has become equivalent to empty-line.. 
                emptyMatcher = emptyPattern.matcher( line ); // after all the above trimming, is the line pretty much whitespace?
                if ( emptyMatcher.matches() ) continue;

                if ( _ok2TrimWhiteSpace ) line = line.trim(); // trim both leading and trailing whitespace
                if ( this.verbose ) System.out.println( CLASSNAME + ": openBatchFile(): TRIMMED line=[" + line +"]" );
                this.lines.add( line );
            }
            scanner.close();
            // istrm.close(); not possible anymore... :-) as we've to support inline-String or @filename options for --batch command

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

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    public BatchCmdType getCmdType() {
        return this.whichCmd;
    }

    //------------------------------------------------------------------------------
    public void determineCmdType() {

        this.resetFlagsForEachLine();
        final String line = this.currentLine(); // remember the line is most likely already trimmed
        if ( line == null )
            return;

        try {
            Pattern propsPattern = Pattern.compile( "^\\s*properties \\s*([a-zA-Z][a-zA-Z0-9]*)=(\\S\\S*)\\s*$" ); // empty line
            Matcher propsMatcher    = propsPattern.matcher( line );
            if (propsMatcher.find()) {
                if ( this.verbose ) System.out.println( CLASSNAME +": I found the text "+ propsMatcher.group() +" starting at index "+  propsMatcher.start() +" and ending at index "+ propsMatcher.end() );    
                this.propertiesKV = new Tools.Tuple<String,String>( propsMatcher.group(1), propsMatcher.group(2) );
                            // line.substring( propsMatcher.start(), propsMatcher.end() );
                if ( this.verbose ) System.out.println( "\t KVPair=[" + this.propertiesKV.key +","+ this.propertiesKV.val +"]" );
                this.whichCmd = BatchCmdType.Cmd_Properties;
				return;
            }

            Pattern printPattern = Pattern.compile( "^\\s*print\\s\\s*(\\S.*\\S)\\s*$" ); // empty line
            Matcher printMatcher    = printPattern.matcher( line );
            if (printMatcher.find()) {
                if ( this.verbose ) System.out.println( CLASSNAME +": I found the text "+ printMatcher.group() +" starting at index "+  printMatcher.start() +" and ending at index "+ printMatcher.end() );    
                this.printExpr  = printMatcher.group(1); // line.substring( printMatcher.start(), printMatcher.end() );
                if ( this.verbose ) System.out.println( "\t print=[" + this.printExpr +"]" );
                this.whichCmd = BatchCmdType.Cmd_Print;
                return ;
            }

            Pattern saveToPattern = Pattern.compile( "^\\s*saveTo\\s\\s*(\\S.*\\S)\\s*$" ); // empty line
            Matcher saveToMatcher    = saveToPattern.matcher( line );
            if (saveToMatcher.find()) {
                if ( this.verbose ) System.out.println( CLASSNAME +": I found the text "+ saveToMatcher.group() +" starting at index "+  saveToMatcher.start() +" and ending at index "+ saveToMatcher.end() );    
                this.saveTo = saveToMatcher.group(1); // line.substring( saveToMatcher.start(), saveToMatcher.end() );
                if ( this.verbose ) System.out.println( "\t SaveTo=[" + this.saveTo +"]" );
                this.whichCmd = BatchCmdType.Cmd_SaveTo;
                return;
            }

            Pattern useAsInputPattern = Pattern.compile( "^\\s*useAsInput\\s\\s*(\\S.*\\S)\\s*$" ); // empty line
            Matcher useAsInputMatcher    = useAsInputPattern.matcher( line );
            if (useAsInputMatcher.find()) {
                if ( this.verbose ) System.out.println( CLASSNAME +": I found the text "+ useAsInputMatcher.group() +" starting at index "+  useAsInputMatcher.start() +" and ending at index "+ useAsInputMatcher.end() );    
                this.useAsInput = useAsInputMatcher.group(1); // line.substring( useAsInputMatcher.start(), useAsInputMatcher.end() );
                if ( this.verbose ) System.out.println( "\t useAsInput=[" + this.useAsInput +"]" );
                this.whichCmd = BatchCmdType.Cmd_UseAsInput;
                return;
            }

            Pattern batchPattern = Pattern.compile( "^\\s*batch\\s\\s*(\\S.*\\S)\\s*$" ); // empty line
            Matcher batchMatcher    = batchPattern.matcher( line );
            if (batchMatcher.find()) {
                if ( this.verbose ) System.out.println( CLASSNAME +": I found the text "+ batchMatcher.group() +" starting at index "+  batchMatcher.start() +" and ending at index "+ batchMatcher.end() );    
                this.subBatchFile = batchMatcher.group(1); // line.substring( batchMatcher.start(), batchMatcher.end() );
                if ( this.verbose ) System.out.println( "\t batch=[" + this.subBatchFile +"]" );
                this.whichCmd = BatchCmdType.Cmd_Batch;
                return;
            }

			if ( line.equalsIgnoreCase( FOREACHCMD ) ) {
				this.whichCmd = BatchCmdType.Cmd_Foreach;
				return;
			}

			if ( line.equalsIgnoreCase("end") ) {
				this.whichCmd = BatchCmdType.Cmd_End;
				return;
			}

            return;

        } catch (PatternSyntaxException e) {
			e.printStackTrace(System.err);
			System.err.println(CLASSNAME + ": isPropertyLine(): Unexpected Internal ERROR, while checking for patterns for line= [" + line +"]" );
			System.exit(91); // This is a serious failure. Shouldn't be happening.
        }
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================
    /** This function helps detect if the current line pointed to by this.currentLine() contains a property entry (a.k.a. a KVPair entry of the form key=value)
     * @return either null.. or, the Key + Value (an instance of the Tools.Tuple class) detected in the current line of batch file
     */
    public Tools.Tuple<String,String> getPropertyKV() {
        if ( this.whichCmd == BatchCmdType.Cmd_Properties )
            return this.propertiesKV; // we've already executed the code below - SPECIFICALLY for the currentLine!
        else
            return null;
    }

    /** This function helps detect if the current line pointed to by this.currentLine() contains a property entry (a.k.a. a Tools.KVPair entry of the form key=value)
     * @return either null.. or, the Key + Value (an instance of the Tools.Tuple class) detected in the current line of batch file
     */
    public String getPrintExpr() {
        if ( this.whichCmd == BatchCmdType.Cmd_Print )
            return this.printExpr; 
        else
            return null;
    }

    /** This function helps detect if the current line pointed to by this.currentLine() contains a 'saveTo ___' entry
     *  @return String the argument provided to the saveTo command (if no argument provided, this returns null - quiet &amp; graceful degradation)
     */
    public String getSaveTo() {
        if ( this.whichCmd == BatchCmdType.Cmd_SaveTo)
            return this.saveTo;
        else
            return null;
    }

    /** This function helps detect if the current line pointed to by this.currentLine() contains a 'useAsInput ___' entry
     *  @return String the argument provided to the useAsInput command (if no argument provided, this returns null - quiet &amp; graceful degradation)
     */
    public String getUseAsInput() {
        if ( this.whichCmd == BatchCmdType.Cmd_UseAsInput )
            return this.useAsInput;
        else
            return null;
    }

    /** This function helps detect if the current line pointed to by this.currentLine() contains a 'batch ___' entry - which will cause a SUB-BATCH cmd to be triggered
     *  @return String the argument provided to the Batch command (if no argument provided, this returns null - quiet &amp; graceful degradation)
     */
    public String getSubBatchFile() {
        if ( this.whichCmd == BatchCmdType.Cmd_Batch )
            return this.subBatchFile;
        else
            return null;
    }

    //==================================
    /** This function helps detect if the current line pointed to by this.currentLine() contains just the word 'foreach' (nothing else other than comments and whitespace)
     * This keyword 'foreach' indicates the beginning of a looping-construct within the batch file.
     * @return true of false, if 'foreach' was detected in the current line of batch file
     */
    public boolean isForEachLine() {
        if ( this.whichCmd == BatchCmdType.Cmd_Foreach )
            return true;
        else
            return false;
    }

    /** This function helps detect if the current line pointed to by this.currentLine() contains just the word 'end' (nothing else other than comments and whitespace)
     * This keyword 'end' indicates the END of the looping-construct within the batch file
     * @return true of false, if 'end' was detected in the current line of batch file
     */
    public boolean isEndLine() {
        if ( this.whichCmd == BatchCmdType.Cmd_End )
            return true;
        else
            return false;
    }

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    /**
     * This function should be called *AFTER* all the various is___() functions/methods have been called.
     * This function should NOT be called BEFORE isSaveToLine() and isUseAsInputLine(), as this function will get you confused.
     * @return String just for the command (whether 'yaml' 'aws' ..)
     */
    public String getCommand() {
        if ( this.whichCmd != BatchCmdType.Cmd_Any )
            return null; // Since.. It is one of the above commands like: properties, saveAs, foreach, end, useAsInput, .. ..

        try {
            final java.util.Scanner scanner = new java.util.Scanner( this.currentLine() );
            scanner.useDelimiter("\\s\\s*");

            if (scanner.hasNext()) { // default whitespace delimiter used by a scanner
                final String cmd = scanner.next();
                if ( this.verbose ) System.out.println( "\t Command=[" + cmd +"]" );
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

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

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

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================
    // For unit-testing purposes only
    public static void main(String[] args) {
        try {
            final BatchFileGrammer o = new BatchFileGrammer(true);
            o.openFile( args[0], true );
            while (o.hasNextLine()) {
                System.out.println(o.nextLine());

                // o.isPropertyLine();
                // // final Tuple kv = o.isPropertyLine(); // could be null, implying NOT a kvpair

                // o.isForEachLine();
                // o.isEndLine();
                // o.isSaveToLine();
                // o.isUseAsInputLine();
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
