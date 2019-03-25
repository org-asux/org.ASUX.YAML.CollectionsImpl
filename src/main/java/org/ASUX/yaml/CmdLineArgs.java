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

public class CmdLineArgs {

    public boolean verbose = false;
    public boolean readCmd = true;
    public boolean delCmd = false;
    public boolean listCmd = false;
    public boolean replaceCmd = false;
    public String yamlPathStr = "*";
    public String inputFilePath = "/tmp/i";
    public String outputFilePath = "/tmp/o";
    public com.esotericsoftware.yamlbeans.YamlConfig.QuoteCharEnum quoteType = com.esotericsoftware.yamlbeans.YamlConfig.QuoteCharEnum.SINGLEQUOTE;

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    public CmdLineArgs(String[] args) {
        Options options = new Options();
        
        Option opt;
        opt= new Option("v", "verbose", false, "Show debug output");
        opt.setRequired(false);
        options.addOption(opt);

        //----------------------------------
        OptionGroup grp = new OptionGroup();
        Option readCmdOpt = new Option("r", "read", false, "output all elements that match");
        Option delCmdOpt = new Option("d", "delete", false, "Delete all elements that match");
        Option listCmdOpt = new Option("l", "list", false, "List YAML-Keys of all elements that match");
        Option replCmdOpt = new Option("c", "change", true, "change/replace all elements that match - with json provided on cmdline");
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

        opt = new Option("i", "inputfile", true, "input file path");
        opt.setRequired(true);
        options.addOption(opt);

        opt = new Option("o", "outputfile", true, "output file");
        opt.setRequired(true);
        options.addOption(opt);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);

            this.verbose = cmd.hasOption("verbose");
            this.readCmd = cmd.hasOption("read");
            this.delCmd = cmd.hasOption("delete");
            this.listCmd = cmd.hasOption("list");
            this.yamlPathStr = cmd.getOptionValue("yamlpath");
            this.inputFilePath = cmd.getOptionValue("inputfile");
            this.outputFilePath = cmd.getOptionValue("outputfile");

            this.quoteType = com.esotericsoftware.yamlbeans.YamlConfig.QuoteCharEnum.SINGLEQUOTE; // default behavior
            if ( cmd.hasOption( noQuoteOpt.getLongOpt()) ) this.quoteType = com.esotericsoftware.yamlbeans.YamlConfig.QuoteCharEnum.NONE;
            if ( cmd.hasOption( singleQuoteOpt.getLongOpt()) ) this.quoteType = com.esotericsoftware.yamlbeans.YamlConfig.QuoteCharEnum.SINGLEQUOTE;
            if ( cmd.hasOption( doubleQuoteOpt.getLongOpt()) ) this.quoteType = com.esotericsoftware.yamlbeans.YamlConfig.QuoteCharEnum.DOUBLEQUOTE;
            //System.err.println("this.quoteType = "+this.quoteType.toString());

        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("java <jarL> com.esotericsoftware.yamlbeans.CmdLineArgs", options);
            System.exit(1);
        }
    }

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    public String toString() {
        return "verbose="+verbose+" read="+readCmd+" delete="+delCmd+" list="+listCmd+" yamlPathStr="+yamlPathStr+" inpfile="+inputFilePath+" outputfile="+outputFilePath+" quoting="+quoteType.toString();
    }
    
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    public static void main(String[] args) {
//        new CmdLineArgs(args);
    }

}
