#!/bin/tcsh -f

##------------------------------
if ( $#argv <= 2 ) then
    echo "Usage: $0 com.esotericsoftware.yamlbeans.Cmd [--verbose] --delete --yamlpath yaml.regexp.path -i /tmp/input.yaml -o /tmp/output.yaml " >>& /dev/stderr
    echo Usage: $0 'org.ASUX.yaml.Cmd [--verbose] --delete --double-quote --yamlpath "paths.*.*.responses.200" -i $cwd/src/test/my-petstore-micro.yaml -o /tmp/output2.yaml ' >>& /dev/stderr
    echo '' >>& /dev/stderr
    exit 1
endif

##------------------------------
source ~/bin/include/variables.csh

set noglob ## Very important to allow us to use '*' character on cmdline arguments

##------------------------------
chdir ${DEVELOPMENT}/dist/
# pwd

set MYJAR=./org.ASUX.yaml.jar
set YAMLBEANSJAR=./com.esotericsoftware.yamlbeans-yamlbeans-1.13.jar
set COMMONSCLIJAR=./commons-cli-1.4.jar
setenv CLASSPATH .:${CLASSPATH}:${COMMONSCLIJAR}:${YAMLBEANSJAR}:${MYJAR} ## to get the jndi.properties
# echo $CLASSPATH

##---------------------------------

set noglob ## Very important to allow us to use '*' character on cmdline arguments

#echo \
#java -cp "${CLASSPATH}" "org.ASUX.yaml.Cmd" "com.esotericsoftware.yamlbeans.Cmd" $*
# echo \
# java -cp "${CLASSPATH}" $*
java -cp "${CLASSPATH}" $*

##---------------------------------
#EoScript
