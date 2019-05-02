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

    public static final String CLASSNAME = "org.ASUX.yaml.CmdLineArgs";

    private static final String READCMD = "read";
    private static final String LISTCMD = "list";
    private static final String DELETECMD = "delete";
    private static final String REPLACECMD = "replace";
    // private static final char REPLACECMDCHAR = 'c'; // -c === --replace
    private static final String MACROCMD = "macro";
    private static final String BATCHCMD = "batch";

    private static final String INPUTFILE = "inputfile";
    private static final String OUTPUTFILE = "outputfile";
    private static final String YAMLPATH = "yamlpath";
    private static final String DELIMITER = "delimiter";
    // private static final String PROPERTIES = "properties";

    public boolean verbose = false;
    public boolean showStats = false;

    public boolean isReadCmd = true;
    public boolean isListCmd = false;
    public boolean isDelCmd = false;
    public boolean isReplaceCmd = false;
    public boolean isMacroCmd = false;
    public boolean isBatchCmd = false;

    public String inputFilePath = "/tmp/i";
    public String outputFilePath = "/tmp/o";
    public String replaceFilePath = null;       // optional argument, but required for 'replace' command
    public String propertiesFilePath = null;    // optional argument, but required for 'macro' command
    public String batchFilePath = null;    // optional argument, but required for 'batch' command
    public String yamlRegExpStr = "undefined";

    public String yamlPatternDelimiter = ".";
    // public static com.esotericsoftware.yamlbeans.YamlConfig.QuoteCharEnum quoteType = com.esotericsoftware.yamlbeans.YamlConfig.QuoteCharEnum.SINGLEQUOTE;

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

        opt= new Option("v", "showStats", false, "Show - at end output - a summary of how many matches happened, or entries were affected");
        opt.setRequired(false);
        options.addOption(opt);

        //----------------------------------
        OptionGroup grp = new OptionGroup();
        Option readCmdOpt = new Option("r", READCMD, false, "output all elements that match");
            readCmdOpt.setOptionalArg(false);
            readCmdOpt.setArgs(1);
            readCmdOpt.setArgName("YAMLPattern");
        Option listCmdOpt = new Option("l", LISTCMD, true, "List YAML-Keys of all elements that match");
            listCmdOpt.setOptionalArg(false);
            listCmdOpt.setArgs(1);
            listCmdOpt.setArgName("YAMLPattern");
        Option delCmdOpt = new Option("d", DELETECMD, true, "Delete all elements that match");
            delCmdOpt.setOptionalArg(false);
            delCmdOpt.setArgs(1);
            delCmdOpt.setArgName("YAMLPattern");
        Option macroCmdOpt = new Option("m", MACROCMD, true, "run input YAML file thru a MACRO processor searching for ${ASUX::__} and replacing __ with values from Properties file");
            macroCmdOpt.setOptionalArg(false);
            macroCmdOpt.setArgs(1);
            macroCmdOpt.setArgName("propertiesFile");
        Option replCmdOpt = new Option("c", REPLACECMD, true, "change/replace all elements that match with json-string provided on cmdline");
            replCmdOpt.setOptionalArg(false);
            replCmdOpt.setArgs(2);
            replCmdOpt.setValueSeparator(' ');
            replCmdOpt.setArgName("YAMLPattern> <newValue"); // Note: there's a trick in the parameter-string.. as setArgName() assumes a single 'word' and puts a '<' & '>' around that single-word.
        Option batchCmdOpt = new Option("b", BATCHCMD, true, "run a batch of commands, which are listed in the <batchfile>");
            macroCmdOpt.setOptionalArg(false);
            macroCmdOpt.setArgs(1);
            macroCmdOpt.setArgName("batchFile");
        grp.addOption(readCmdOpt);
        grp.addOption(listCmdOpt);
        grp.addOption(delCmdOpt);
        grp.addOption(replCmdOpt);
        grp.addOption(macroCmdOpt);
        grp.addOption(batchCmdOpt);
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
        // opt = new Option("p", YAMLPATH, true, "Path to YAML element");
        // opt.setRequired(true);
        // options.addOption(opt);

        opt = new Option("zd", DELIMITER, false, "whether period/dot comma pipe or other character is the delimiter to use within the YAMLPATHPATTERN");
        opt.setRequired(false);
        opt.setArgs(1);
        opt.setOptionalArg(false);
        opt.setArgName("delimcharacter");
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
            this.showStats = cmd.hasOption("showStats");

            this.yamlPatternDelimiter = cmd.getOptionValue(DELIMITER);
            if ( this.yamlPatternDelimiter == null || this.yamlPatternDelimiter.equals(".") )
                this.yamlPatternDelimiter = YAMLPath.DEFAULTDELIMITER;

            this.isReadCmd = cmd.hasOption(READCMD);
            this.isListCmd = cmd.hasOption(LISTCMD);
            this.isDelCmd = cmd.hasOption(DELETECMD);
            this.isReplaceCmd = cmd.hasOption(REPLACECMD);
            this.isMacroCmd = cmd.hasOption(MACROCMD);
            this.isBatchCmd = cmd.hasOption(BATCHCMD);

            // this.yamlRegExpStr = cmd.getOptionValue(YAMLPATH);
            this.inputFilePath = cmd.getOptionValue(INPUTFILE);
            this.outputFilePath = cmd.getOptionValue(OUTPUTFILE);

            // following are defined to be optional arguments, but mandatory for a specific command (as you can see from the condition of the IF statements).
            if (this.isReadCmd) this.yamlRegExpStr = cmd.getOptionValue(READCMD);
            if (this.isListCmd) this.yamlRegExpStr = cmd.getOptionValue(LISTCMD);
            if (this.isDelCmd) this.yamlRegExpStr = cmd.getOptionValue(DELETECMD);
            final String[] searchArgs = cmd.getOptionValues(REPLACECMD);
            // because we set .setArgs(2) above.. you can get the values for:- searchArgs[0] and searchArgs[1].
            if (this.isReplaceCmd) this.yamlRegExpStr = searchArgs[0]; // 1st of the 2 arguments for REPLACE cmd.
            if (this.isReplaceCmd) this.replaceFilePath = searchArgs[1];

            this.propertiesFilePath = cmd.getOptionValue(MACROCMD);
            this.batchFilePath = cmd.getOptionValue(BATCHCMD);

            // this.quoteType = com.esotericsoftware.yamlbeans.YamlConfig.QuoteCharEnum.SINGLEQUOTE; // default behavior
            // if ( cmd.hasOption( noQuoteOpt.getLongOpt()) ) this.quoteType = com.esotericsoftware.yamlbeans.YamlConfig.QuoteCharEnum.NONE;
            // if ( cmd.hasOption( singleQuoteOpt.getLongOpt()) ) this.quoteType = com.esotericsoftware.yamlbeans.YamlConfig.QuoteCharEnum.SINGLEQUOTE;
            // if ( cmd.hasOption( doubleQuoteOpt.getLongOpt()) ) this.quoteType = com.esotericsoftware.yamlbeans.YamlConfig.QuoteCharEnum.DOUBLEQUOTE;
            // System.err.println("this.quoteType = "+this.quoteType.toString());

            // System.err.println( CLASSNAME +": "+this.toString());

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
        return "verbose="+verbose+" showStats="+showStats+" yamlPatternDelimiter="+yamlPatternDelimiter+" yamlRegExpStr="+yamlRegExpStr
        +" read="+isReadCmd+" list="+isListCmd+"  delete="+isDelCmd+" change="+isReplaceCmd
        +" macro="+isMacroCmd+" batch="+isBatchCmd
        +" inpfile="+inputFilePath+" outputfile="+outputFilePath+" replaceFile="+replaceFilePath
        +" batchFilePath="+batchFilePath+" propertiesFilePath="+propertiesFilePath
        ;
        // yamlRegExpStr="+yamlRegExpStr+" 
    }
    
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    // For unit-testing purposes only
//    public static void main(String[] args) {
//        new CmdLineArgs(args);
//    }

}
