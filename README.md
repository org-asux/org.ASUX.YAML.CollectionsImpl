# org.ASUX.yaml
This is to enable a cmdline interface to query and manipulate YAML files.
This is feasible thanks to the fantastic com.esotericsoftware.yamlbeans github project (https://github.com/EsotericSoftware/yamlbeans).

The 4 YAML-COMMANDS are: read/query, list, delete and replace.

Example: <code>java org.ASUX.yaml.Cmd --delete --yamlpath "paths.*.*.responses.200" -i $cwd/src/test/my-petstore-micro.yaml -o /tmp/output2.yaml  --double-quote</code>

Simply run: <b><code>java org.ASUX.yaml.Cmd</code></b>  -- it will show all command line options supported.

----------------
<h2>YAML Paths a.k.a.  YAML Path-patterns</h2>
<p>A YAML Path (also referred to as: YAML Path-Pattern) is composed of Path-<b>ELEMENT</b>-Patterns separated by "delimiter" (like period/dot/".")</p>
<p>If you'd like to use delimiters other than period/dot/"." then you'll be forced to write Java-code, as commandline does Not support any other delimiter (for now).</p>
<p>Here are rules for YAML Path Pattern</p>
<ol>
  <li>Except for the 2nd rule (below) all RegExp symbols(like <code><b>'+'</b></code> or <code><b>"[{]}</b></code> as defined & supported by java.util.regex) it will work - guaranteed.</li>
  <li><b>Important - there is ONLY 1 deviation/substitution</b>: Whenever a star/asterisk/<code><b>'*'</b></code> with a delimiter on either side (that is, the Path-ELEMENT is = exactly star/asterisk/<code><b>'+'</b></code>).. .. is detected, it is <b>automatically</b> replaced with <code><b>".*"</b></code><br/>This substitution is only for human convenience.</li>
  <li>Any other use of RegExp compatible star/asterisk/<code><b>'*'</b></code> (example: <code>paths./pet*.{get|put|post}.responses.200</code>) will be used as is - to match YAML elements.</li>
 <li>Note: <code><b>**</b></code> (<b>double</b> star/asterisk/<code><b>"*"</b></code>) implies unlimited-match prefix.  It's a Special case: <code>"**"</code> represents a deviation of java.util.regexp specs on what qualifies as a regular-expression.  This deviation is a very human-friendly easy-2-understand need.</li>
</ol>
<p>Example: <code>paths.*.*.responses.200"</code>.  <b>ATTENTION: This is a human readable pattern, NOT a 100% proper RegExp-pattern</b></p>
<p>Example: <code>paths./pet*.{get|put|post}.responses.200</code>.  <b>ATTENTION: This is 100% proper RegExp-pattern</b></p>
-------------------------------
<h2>Read/Query YAML files using pattern</h2>
<p>Example: <code>java org.ASUX.yaml.Cmd --read --yamlpath "paths.*.*.responses.200" -i - -o -</code></p>
<p>Note: the hyphen/"-" for input-file cmdline-argument means: pipe the output of another command, to become input for this (as System.in).<br/>
Note: the hyphen/"-" for output-file cmdline-line argument means: write to console/terminal/System.out.</p>
<p>The output is the RHS of the colon/':' character of the matching line in the YAML file.  Why do I specifically point that out?  Well!  Take a look at the <code>list</code> command!</p>
<p>Simply run: <b><code>java org.ASUX.yaml.Cmd</code></b>  -- it will show all command line options supported.</p>
-------------------------------
<h2>List pattern-matches within YAML files</h2>
<p>Example: <code>java org.ASUX.yaml.Cmd --list --yamlpath "paths.*.*.responses.200" -i - -o -</code></p>
<p>Note: see section above for "Read/Query YAML files" - for help with command-line options.</p>
<p>The output is the LHS of the colon/':' character of the matching line in the YAML file.  Why do I specifically point that out?  Well!  Take a look at the <code>read/query</code> command!</p>
<p>The output is all exact YAML paths within the YAML-file-input, that matched the YAML-Path-Pattern.  You can then iterate through this output-list, and do surgically-precise specific actions (well beyond just simply querying YAML files).</p>
-------------------------------
<h2>Delete entries within YAML files based on pattern</h2>
<p>Example: <code>java org.ASUX.yaml.Cmd --delete --yamlpath "paths.*.*.responses.200" -i - -o /tmp/new.yaml </code></p>
<p>Note: see section above for "Read/Query YAML files" - for help with command-line options.</p>
-------------------------------
<h2>REPLACE entries within YAML files based on pattern</h2>
<p>WARNING: Replace has Incomplete code.  Wait for next release.</p>
<p>Example: <code>java org.ASUX.yaml.Cmd --replace --yamlpath "paths.*.*.responses.200" -i - -o /tmp/new.yaml </code></p>
<p>Note: see section above for "Read/Query YAML files" - for help with command-line options.</p>

