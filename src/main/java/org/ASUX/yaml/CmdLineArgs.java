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

import org.apache.commons.cli.*;

/** <p>This class is a typical use of the org.apache.commons.cli package.</p>
 *  <p>This class has No other function - other than to parse the commandline arguments and handle user's input errors.</p>
 *  <p>For making it easy to have simple code generate debugging-output, added a toString() method to this class.</p>
 *  <p>Typical use of this class is: </p>
 *<pre>
 public static void main(String[] args) {
 cmdLineArgs = new CmdLineArgs(args);
 .. ..
 *</pre>
 *
 *  <p>See full details of how to use this, in {@link org.ASUX.yaml.Cmd} as well as the <a href="https://github.com/org-asux/org.ASUX.cmdline">org.ASUX.cmdline</a> GitHub.com project.</p>
 * @see org.ASUX.yaml.Cmd
 */
public class CmdLineArgs {

    private static final String READCMD = "read";
    private static final String LISTCMD = "list";
    private static final String DELETECMD = "delete";
    private static final char REPLACECMDCHAR = 'c';
    private static final String REPLACECMD = "replace";
    private static final String INPUTFILE = "inputfile";
    private static final String OUTPUTFILE = "outputfile";

    public boolean verbose = false;
    public boolean isReadCmd = true;
    public boolean isListCmd = false;
    public boolean isDelCmd = false;
    public boolean isReplaceCmd = false;
    public String yamlPathStr = "*";
    public String inputFilePath = "/tmp/i";
    public String outputFilePath = "/tmp/o";
    public String replaceFilePath = "/tmp/r";
//    public com.esotericsoftware.yamlbeans.YamlConfig.QuoteCharEnum quoteType = com.esotericsoftware.yamlbeans.YamlConfig.QuoteCharEnum.SINGLEQUOTE;

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    /** Constructor.
     * @param args command line argument array - as received as-is from main().
     */
    public CmdLineArgs(String[] args) {
        Options options = new Options();
        
        Option opt;
        opt= new Option("v", "verbose", false, "Show debug output");
        opt.setRequired(false);
        options.addOption(opt);

        //----------------------------------
        OptionGroup grp = new OptionGroup();
        Option readCmdOpt = new Option("r", READCMD, false, "output all elements that match");
        Option listCmdOpt = new Option("l", LISTCMD, false, "List YAML-Keys of all elements that match");
        Option delCmdOpt = new Option("d", DELETECMD, false, "Delete all elements that match");
        Option replCmdOpt = new Option(""+REPLACECMDCHAR, REPLACECMD, true, "change/replace all elements that match with json-string provided on cmdline");
            replCmdOpt.setOptionalArg(false);
            replCmdOpt.setArgs(1);
            replCmdOpt.setArgName("new");
        grp.addOption(readCmdOpt);
        grp.addOption(delCmdOpt);
        grp.addOption(listCmdOpt);
        grp.addOption(replCmdOpt);
        grp.setRequired(true);

        options.addOptionGroup(grp);

        //----------------------------------
        OptionGroup grp2 = new OptionGroup();
        Option noQuoteOpt = new Option("nq", "no-quote", false, "do Not use Quotes in YAML output");
        Option singleQuoteOpt = new Option("sq", "single-quote", false, "use ONLY Single-quote when generating YAML output");
        Option doubleQuoteOpt = new Option("dq", "double-quote", false, "se ONLY Double-quote when generating YAML output");
        grp2.addOption(noQuoteOpt);
        grp2.addOption(singleQuoteOpt);
        grp2.addOption(doubleQuoteOpt);
        grp2.setRequired(false);

        options.addOptionGroup(grp2);

        //----------------------------------
        opt = new Option("p", "yamlpath", true, "Path to YAML element");
        opt.setRequired(true);
        options.addOption(opt);

        opt = new Option("i", INPUTFILE, true, "input file path");
        opt.setRequired(true);
        options.addOption(opt);

        opt = new Option("o", OUTPUTFILE, true, "output file");
        opt.setRequired(true);
        options.addOption(opt);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);

            this.verbose = cmd.hasOption("verbose");
            this.isReadCmd = cmd.hasOption(READCMD);
            this.isListCmd = cmd.hasOption(LISTCMD);
            this.isDelCmd = cmd.hasOption(DELETECMD);
            this.isReplaceCmd = cmd.hasOption(REPLACECMD);
            this.yamlPathStr = cmd.getOptionValue("yamlpath");
            this.inputFilePath = cmd.getOptionValue(INPUTFILE);
            this.outputFilePath = cmd.getOptionValue(OUTPUTFILE);
            this.replaceFilePath = cmd.getOptionValue(REPLACECMD);

//            this.quoteType = com.esotericsoftware.yamlbeans.YamlConfig.QuoteCharEnum.SINGLEQUOTE; // default behavior
//            if ( cmd.hasOption( noQuoteOpt.getLongOpt()) ) this.quoteType = com.esotericsoftware.yamlbeans.YamlConfig.QuoteCharEnum.NONE;
//            if ( cmd.hasOption( singleQuoteOpt.getLongOpt()) ) this.quoteType = com.esotericsoftware.yamlbeans.YamlConfig.QuoteCharEnum.SINGLEQUOTE;
//            if ( cmd.hasOption( doubleQuoteOpt.getLongOpt()) ) this.quoteType = com.esotericsoftware.yamlbeans.YamlConfig.QuoteCharEnum.DOUBLEQUOTE;
            //System.err.println("this.quoteType = "+this.quoteType.toString());

        } catch (ParseException e) {
            e.printStackTrace(System.err);
            formatter.printHelp("java <jarL> com.esotericsoftware.yamlbeans.CmdLineArgs", options);
            System.exit(1);
        }
    }

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    /** For making it easy to have simple code generate debugging-output, added this toString() method to this class.
     */
    public String toString() {
//        return "verbose="+verbose+" read="+isReadCmd+" delete="+isDelCmd+" list="+isListCmd+"  change="+isReplaceCmd+" yamlPathStr="+yamlPathStr+" inpfile="+inputFilePath+" outputfile="+outputFilePath+" quoting="+quoteType.toString();
        return "verbose="+verbose+" read="+isReadCmd+" delete="+isDelCmd+" list="+isListCmd+"  change="+isReplaceCmd+" yamlPathStr="+yamlPathStr+" inpfile="+inputFilePath+" outputfile="+outputFilePath+" replaceFile="+replaceFilePath;
    }
    
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    // For unit-testing purposes only
//    public static void main(String[] args) {
//        new CmdLineArgs(args);
//    }

}
