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
import java.util.stream.IntStream;
//import java.io.Cloneable;
import java.io.Serializable;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;

/** This class encapsulates a YAML element, and makes it super-easy to parse and manipulate it.
 * In fact, this class makes it very safe to assume that the user's input (for the YAMLPath/pattern) is valid and squeaky-clean.
 * That is, you should expect minimal run-time errors.
 *<pre>
 public static void main(String[] args) {
    cmdLineArgs = new CmdLineArgs(args);
    .. ..
    for loop ..
        if ( ! _yamlPath.hasNext() ) return false; // YAML path has ended
        final String yamlPathElemStr = _yamlPath.get(); // current path-element (a substring of full yamlPath)
        System.out.println(CLASSNAME + ": @# " + _yamlPath.index() +"\t"+ _yamlPath.getPrefix() +"\t"+ _yamlPath.get() +"\t"+ _yamlPath.getSuffix() + "\t  matched '"
        .. ..
        final YAMLPath cloneOfYAMLPath = YAMLPath.deepClone(_yamlPath); // to keep _yamlPath intact as we recurse in and out of sub-yaml-elements
</pre>
 *
 * @see org.ASUX.yaml.Cmd
 */
public class YAMLPath implements Serializable {

    public static final String CLASSNAME = "com.esotericsoftware.yamlbeans.YAMLPath";

    public boolean isValid = false;
    public final String yamlPath;
    public final String prntDelimiter;
    public String[] yamlElemArr = new String[]{"UNinitialized", "yamlElemArr"};

    private int indexPtr = -1;

    /** <p>Constructor takes a YAML Path like <code>paths./pet.put.consumes</code></p>
     *  <p>It breaks it up into regexpressions separated by the default DELIMITER = "."</p>
     *  @param _yp example: "<code>paths.*.*.responses.200</code>"  where the delimiter is fixed to be the period/dot "."
     */
    public YAMLPath(String _yp) {
        this(_yp, "\\.");
    }

    /** <p>Constructor takes a YAML Path like <code>paths./pet.put.consumes</code></p>
     *  <p>It breaks it up into regexpressions separated by DELIMITER</p>
     *  @param _yp example: "<code>paths.*.*.responses.200</code>"
     *  @param _delim examples are "." or "\t"  or "," .. ..
     */
    public YAMLPath(String _yp, final String _delim) {
        this.yamlPath = _yp; //save it
        this.prntDelimiter = _delim.replaceAll​("\\\\", ""); // save it in human-readable form (to print out paths -- and for NO OTHER purpose)
        // System.out.println( "x\\.y".replaceAll​("\\\\", "") );

        // Sanity check of "_delim"
        try {
            Pattern p = Pattern.compile(_delim);
        }catch(PatternSyntaxException e){
            System.err.println("Invalid delimiter-pattern '"+ _delim +"' provided to constructor of "+CLASSNAME);
            System.err.println(e.getMessage());
            return; // invalid YAML Path.  Let "this.isValid" stay as false
        }

        // System.out.println(CLASSNAME + ": Sanity check completed.");
        //        boolean b = Pattern.matches("a*b", "aaaaab");
        _yp.strip(); // strip leading and trailing whitesapce
        if ( _yp.length() <= 0 ) return; // invalid YAML Path.  Let "this.isValid" stay as false

        // System.out.println(CLASSNAME + ": about to split '"+_yp+"' with delimiter '"+_delim+"'");
        this.yamlElemArr = _yp.split(_delim);
//        for (String str: yamlElemArr) {}
        // System.out.println(CLASSNAME + ": this.yamlElemArr has length '"+this.yamlElemArr.length+"'");
        // System.out.println(CLASSNAME + ": this.yamlElemArr[0] = '"+this.yamlElemArr[0]+"'");

        for(int ix=0; ix < this.yamlElemArr.length; ix++ ) {
            String elem = this.yamlElemArr[ix];
            try {
                //System.err.println(CLASSNAME+": checking on .. YAML-element '"+ elem +"'.");
                if (elem.equals("*") ) {
                    elem = ".*"; // convert human-friendly * into formal-regexp .*
                    this.yamlElemArr[ix] = elem;
                }
                //System.err.println(CLASSNAME+": YAML-element='"+ this.yamlElemArr[ix] +"'.");
                Pattern p = Pattern.compile(elem);
            }catch(PatternSyntaxException e){
                System.err.println(CLASSNAME+": Invalid YAML-element '"+ elem +"' provided.");
                System.err.println(e.getMessage());
                return; // invalid YAML Path.  Let "this.isValid" stay as false
            }
        }

        this.isValid = (this.yamlElemArr.length > 0) ? true : false;
        this.indexPtr = (this.yamlElemArr.length > 0) ? 0 : -1;
    } // Constructor

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    /** Whether the instance of this class is valid (in case you are passed this object by some other code, this is your sanity check).. .. before you invoke any of the other functions in ths class and end up with runtime errors
     *  @return true means all the methods in this class are GUARANTEED to NOT Throw any runtime exception :-)
     */
    public boolean isValid() {
        return this.isValid;
    }

    /** For example strings like "<code>paths.*.*.responses.200</code>", your first call will return true.  If you call {@link next} <b>up to 4 times</b>, this function will return true.  After you call next() a 5th time, this function will return false.
     *  @return true means {@link get} will return a valid string, GUARANTEED to NOT Throw any runtime exception :-)
     */
    public boolean hasNext() {
        // System.out.println(CLASSNAME + ":hasNext(): Starting.");
        if ( ! this.isValid ) return false;
        if ( this.indexPtr < this.yamlElemArr.length )
            return true;
        else
            return false;
    }

    /** For example strings like "<code>paths.*.*.responses.200</code>", your <b>1st 5 invocations</b> will return a valid string (exact same values as 1st 5 invocations of get()).  After you call it a 6th time (for same example), this function will return null(String).
     *  @return String a string that does NOT have periods/dots in it.  The string may be (based on example above) = "*".
     */
    public String next() {
        if ( ! this.isValid ) return null;
        String retstr = null;
        if ( this.indexPtr < this.yamlElemArr.length ) {
            retstr = this.yamlElemArr[this.indexPtr];
            this.indexPtr ++;
        }
        return retstr;
    }

    /** For example strings like "<code>paths.*.*.responses.200</code>", after your first call to next(), this will return "paths".  For the 2nd call to next(), this function will return "*".  After you call next() a 5th time(or more), this function will return null(String).
     *  @return a string that does NOT have periods/dots/delimiter in it.  The string may be (based on example above) = "*".
     */
    public String get() {
        if ( ! this.isValid ) return null;
        if ( this.indexPtr < this.yamlElemArr.length )
            return this.yamlElemArr[this.indexPtr];
        else
            return null;
    }

    /** For example strings like "<code>paths.*.*.responses.200</code>", your first call will return 0 (index numbering per C/Java array-index standard).  Every call to next() will increment the return value of this function.  When you call next() a 5th/6th/7th/../100th time for above example, this function will return the same value then onwards (= # of elements in the YAMLPath-string.  In this example, that is 5)
     *  @return integer &gt;= 0 (if things are working) and -1 is things are screwed up.
     */
    public int index() {
        if ( ! this.isValid ) return -1;
        if ( this.indexPtr < this.yamlElemArr.length )
            return this.indexPtr;
        else
            return this.yamlElemArr.length;
    }

    /** For example strings like "<code>paths.*.*.responses.200</code>", before your 1st call to next(), this function will return ""(empty string).  After the 1st call to next(), this function will return "paths".  After the 2nd call to next(), this will return "paths.*".  After you call next() a 5th time (or more), this function will return "<code>paths.*.*.responses.200</code>".
     *  @return a string that does NOT have periods/dots in it.  The string may be (based on example above) = "*".
     */
    public String getPrefix() {
        if ( ! this.isValid ) return null;
        if ( this.indexPtr < this.yamlElemArr.length ) {
            String retstr = "";
            // Compiler error: local variables referenced from a lambda expression must be final or effectively final
            // IntStream.range(0, this.indexPtr).forEach(i -> retstr+= this.yamlElemArr[i]);
            final int[] range = IntStream.range(0, this.indexPtr).toArray();
            for(int ix : range )
                retstr += this.yamlElemArr[ix] + this.prntDelimiter;
            return retstr;
        }else{
            return null;
        }
    }

    /** For example strings like "<code>paths.*.*.responses.200</code>", before your 1st call to next(), this function will return "<code>paths.*.*.responses.200</code>".  After the 1st call to next(), this function will return "<code>*.*.responses.200</code>".  After the 3rd call to next(), this will return "<code>responses.200</code>".  After you call next() a 5th time(or more), this function will return null(String).
     *  @return a string that does NOT have periods/dots in it.  The string may be (based on example above) = "*".
     */
    public String getSuffix() {
        if ( ! this.isValid ) return null;
        if ( this.indexPtr < this.yamlElemArr.length ) {
            String retstr = "";
            // Compiler error: local variables referenced from a lambda expression must be final or effectively final
            // IntStream.range(this.indexPtr, this.yamlElemArr.length).forEach(i -> retstr+= this.yamlElemArr[i]);
            int[] range = IntStream.range(this.indexPtr, this.yamlElemArr.length).skip(1).toArray();
            for(int ix : range )
                retstr += this.prntDelimiter + this.yamlElemArr[ix];
            return retstr;
        }else{
            return null;
        }
    }

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

//    public CloneExample clone() {
//        try {
//            return (CloneExample) super.clone();
//        } catch (CloneNotSupportedException e) {
//            return null;
//        }
//    }

    /** This deepClone function is unnecessary, if you can invoke org.apache.commons.lang3.SerializationUtils.clone(this)
     *  @param _orig what you want to deep-clone
     *  @return a deep-cloned copy, created by serializing into a ByteArrayOutputStream and reading it back (leveraging ObjectOutputStream)
     */
    public static YAMLPath deepClone(YAMLPath _orig) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(_orig);
            
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            return (YAMLPath) ois.readObject();
        } catch (IOException e) {
            return null;
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

//    public static void main(String[] args) {
//        // System.out.println(CLASSNAME + ": started with '"+args[0]+"'");
//        YAMLPath yp = new YAMLPath(args[0]);
//        // System.out.println(CLASSNAME + ": parsing complete");
//        while (yp.hasNext()) {
//            System.out.println("@# " + yp.index() +"\t"+ yp.getPrefix() +"\t"+ yp.get() +"\t"+ yp.getSuffix() );
//            yp.next();
//        }
//    }

}
