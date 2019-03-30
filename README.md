# org.ASUX.yaml
This is to enable a cmdline interface to query and manipulate YAML files.
This is feasible thanks to the fantastic com.esotericsoftware.yamlbeans github project (https://github.com/EsotericSoftware/yamlbeans).

The 4 YAML-COMMANDS are: read/query, list, delete and replace.

<h2>The simplest and easiest way to get started?</h2>
<p><b>Warning</b>: For now this project requires Bourne-Shell(or T-CSH) and Maven - to do anything.  It will be a long-time, before it will run on a plain-vanilla Windows laptop.</p>
<p>Why don't you rely on a companion GitHub project - that one exists to make it one-step simple!</p>
<p><code>$ git clone https://github.com/org-asux/org.ASUX.cmdline</code></p>
<p><code>$ cd org.ASUX.cmdline</code></p>
<p><code>$ ./asux.sh</code></p>
<p><code>$ ./asux.csh</code></p>
<p>The first time you run the <code>asux.sh</code> command, it will automatically download everything it needs!

<h2>Not so easy way to use this project</h2>
<p>First you have to ensure all dependency JARs are in classpath.  Run <code>mvn dependency:tree</code> to get the list.</p>
<p>Example: <code>java org.ASUX.yaml.Cmd --delete --yamlpath "paths.*.*.responses.200" -i $cwd/src/test/my-petstore-micro.yaml -o /tmp/output2.yaml  --double-quote</code></p>
<p>For examples and rules on what can be provided for <code>--yamlpath</code> cmd-line argument.. see next section on YAML Path-Patterns</p>

<p>To see command line options supported, Simply run: <b><code>java org.ASUX.yaml.Cmd</code></b>.</p>

----------------
<h2>YAML Paths a.k.a.  YAML Path-patterns</h2>
<p>A YAML Path (also referred to as: YAML Path-Pattern) is composed of Path-<b>ELEMENT</b>-Patterns separated by "delimiter" (like period/dot/".")</p>
<p>Refer to https://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html - to learn what Regular-Expressions can be used here in this library.</p>
<p>If you'd like to use delimiters other than period/dot/"." then you'll be forced to write Java-code, as commandline does Not support any other delimiter (for now).</p>
<p>Here are rules for YAML Path Pattern</p>
<ol>
  <li>Except for the 2nd rule (below) all RegExp symbols(like <code><b>'+'</b></code> or <code><b>"[{]}</b></code> as defined & supported by java.util.regex per https://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html) will work - guaranteed by design.</li>
  <li><b>Important - there is ONLY 1 deviation/substitution</b>: Whenever a star/asterisk/<code><b>'*'</b></code> with a delimiter on either side (that is, the Path-ELEMENT is = exactly star/asterisk/<code><b>'+'</b></code>).. .. is detected, it is <b>automatically</b> replaced with <code><b>".*"</b></code><br/>This substitution is only for human convenience.</li>
  <li>Any other use of RegExp compatible star/asterisk/<code><b>'*'</b></code> (example: <code>paths./pet*.{get|put|post}.responses.200</code>) will be used as is - to match YAML elements.</li>
 <li>Note: <code><b>**</b></code> (<b>double</b> star/asterisk/<code><b>"*"</b></code>) implies unlimited-match prefix.  It's a Special case: <code>"**"</code> represents a deviation of java.util.regexp specs on what qualifies as a regular-expression.  This deviation is a very human-friendly easy-2-understand need.</li>
</ol>
<p>Example: <code>paths.*.*.responses.200"</code>.  <b>ATTENTION: This is a human readable pattern, NOT a 100% proper RegExp-pattern</b></p>
<p>Example: <code>paths./pet*.(get|put|post).responses.200</code>.  <b>ATTENTION: This is 100% proper RegExp-pattern</b></p>
<p>Example: <code>paths.*.(get|put|post).responses.200</code>.  <b>ATTENTION: Because of the star/asterisk/<code><b>"*"</b></code>, this is 99% proper RegExp-pattern.  Notice the regexpr-difference versus the line above.</b></p>
-------------------------------
<h2>Read/Query YAML files using pattern</h2>
<p>Example: <code>java org.ASUX.yaml.Cmd --read --yamlpath "paths.*.*.responses.200" -i - -o -</code></p>
<p>Note: the hyphen/"-" for input-file cmdline-argument means: pipe the output of another command, to become input for this (as System.in).<br/>
Note: the hyphen/"-" for output-file cmdline-line argument means: write to console/terminal/System.out.</p>
<p>The output produced is the RHS (right-hand-side) of the colon/':' character of the matching line <b>within the YAML input(file)</b>.  Why do I specifically point that out?  Well!  Compare it with the output produced by the <code>list</code> command below!</p>
<p>Simply run: <b><code>java org.ASUX.yaml.Cmd</code></b>  -- it will show all command line options supported.</p>
-------------------------------
<h2>List pattern-matches within YAML files</h2>
<p>Example: <code>java org.ASUX.yaml.Cmd --list --yamlpath "paths.*.(get|put|post).responses.200" -i - -o -</code></p>
<p>Note: see section above for "<em>Read/Query YAML files</em>" - for help with command-line options.</p>
<p>The output produced is the LHS of the colon/':' character of the matching line in the YAML file.  Why do I specifically point that out?  Well!  Compare it with the output produced by the <code>read/query</code> command above!</p>
<p>The output is all exact YAML paths within the YAML-file-input, that matched the YAML-Path-Pattern.  You can then iterate through this output-list, and do surgically-precise specific actions (well beyond just simply querying YAML files).</p>
-------------------------------
<h2>Delete entries within YAML files based on pattern</h2>
<p>Example: <code>java org.ASUX.yaml.Cmd --delete --yamlpath "paths.*.*.responses.200" -i - -o /tmp/new.yaml </code></p>
<p>Note: see section above for "<em>Read/Query YAML files</em>" - for help with command-line options.</p>
-------------------------------
<h2>REPLACE entries within YAML files based on pattern</h2>
<p>WARNING: Replace has Incomplete code.  Wait for next release.</p>
<p>Example #1: <code>java org.ASUX.yaml.Cmd --replace <b>"new rhs SIMPLE-string value"</b> --yamlpath "paths.*.*.responses.200" -i - -o /tmp/new.yaml </code></p>
<p>Note: see section above for "<em>Read/Query YAML files</em>" - for help with command-line options.</p>
<p>Example #2: Alternate way to do it, using the very simple <a href="https://github.com/org-asux/org.ASUX">org.ASUX</a> cmd-line based project:</p>
<p><code>$ asux yaml --replace <b>@/path/to/file/repltxt.yaml</b> --yamlpath 'paths.*.(get|put|post).responses.200' -i ~/Documents/myinput.yaml -o new.yaml</code></p>
<p><b>ATTENTION<b>: Did you notice the <code>@</code> as the <b>1st-character</b> in the value of the cmd-line argument=<code>--replace</code>, in the line above?  Are you able to tell the difference vs. the 1st example above?</p>
<p>The file <code>$cwd/repltxt.yaml</code> should be a valid yaml.  That's is the ONLY requirement</p>


<p>--End--</p>
