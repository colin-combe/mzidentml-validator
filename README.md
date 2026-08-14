# mzidentml-validator

[![Java CI with Maven](https://github.com/ypriverol/mzidentml-validator/actions/workflows/maven.yml/badge.svg)](https://github.com/ypriverol/mzidentml-validator/actions/workflows/maven.yml)

mzidentml validator ui and command line tool allows developers of the mzIdentML standard to validate their implementation using the following tools.

The latest version of the tool can be also found in the following folder: **bin/**

Disclaimer
----------
This validator is not part of the formal document process and is still under development.
At this stage, the validation is neither complete nor has been thoroughly tested for correctness.
The current version should be regarded as a prototype and proof of concept implementation not as final version!

## mzidentml-validator-{version}-gui.jar 

The GUI version is a desktop tool that enable users to configure among others the level of errors (WARNING, ERROR, FAILS), the ontologies to be used to validate semantically the files, etc. The jar can be executed as: 

```bash
jar -jar mzidentml-validator-{version}-gui.jar 
```

When the validation finished a report like this is created: 

![Final report created by the validator](https://github.com/ypriverol/mzidentml-validator/raw/main/docs/screen-report-gui.png)

## mzidentml-validator-{version}-cmd.jar 

The commandline tool enables to validate the mzidentml using the commandline. When the tool get executed the following message defines the parameters needed by the tool: 

```bash 
java -jar mzidentml-validator-1.4.36-SNAPSHOT-cmd.jar

usage: mzidentml-validator [-e] -f <arg> [-l <arg>] [-m <arg>] [-o <arg>]
       [-p] [-r <arg>] [-R] [-s] [-t <arg>] [-w <arg>] [-x <arg>]
mzidentml-validator version 1.4.36-SNAPSHOT

 -e,--full_validation
 -f,--mzidentml_file_to_validate <arg>   mzidentml file to be validated
 -l,--error_level <arg>                  The error level of the validation
                                         process
 -m,--cv_mapping_config_file <arg>       The CV mapping configuration file
 -o,--ontology_config_file <arg>         Ontology configuration file
 -p,--miape_validation                   Use the MIAPE rule files instead
                                         of the semantic ones
 -r,--coded_rules_config_file <arg>      Coded rules configuration file
 -R,--remote_ontologies                  Look the ontologies up in OLS
                                         instead of using the bundled OBO
                                         files
 -s,--semantic_validation
 -t,--xml_file_filter_file <arg>         The filter definition file
 -w,--schema_file <arg>
 -x,--schema_version <arg>               Schema version, supported values
                                         1.1.0, 1.1.1, 1.2.0, 1.3.0

```

The tool reports its result through the exit code: **0** if nothing was reported at the chosen
error level, **1** if there were messages, and **2** if the file could not be validated at all
(bad arguments, unreadable file, unsupported schema version).
### schema validation

```bash
java -jar mzidentml-validator-1.4.36-SNAPSHOT-cmd.jar -s -x 1.1.0 -f file.mzid
```

-f (`--mzidentml_file_to_validate`): The file input is defined by the parameter **-f** which is the mzIdentML to be validated. 
-x (`--schema_version`): The schema version that will be used to validate the mzidentml file. Currently, te validator supports versions `1.1.0`, `1.1.1`, `1.2.0`, `1.3.0`. 

These options are mutually exclusive, if the `-x` option is provided the tool uses one of the mzIdentML default schemas in the following repo (https://github.com/HUPO-PSI/mzIdentML/tree/master/schema).  

### semantic (full) validation 

```bash
java -jar mzidentml-validator-1.4.36-SNAPSHOT-cmd.jar -e -f file.mzid
```

-e (`--full_validation`): perform semantic and schema validation. 

No further arguments are needed: like the GUI, the command line picks the rule files matching the
version the file declares (1.1.0/1.1.1, 1.2.0 or 1.3.0, see `validation.properties`) and uses the
OBO files bundled with the application, so a run needs no network access.

Each of those defaults can be overridden:
- `-p` (`--miape_validation`): use the MIAPE rule files (`ObjectRulesMIAPE.*`, `miape-msi-rules.*`)
  of the file's version instead of the semantic ones.
- `-R` (`--remote_ontologies`): resolve the ontologies through OLS instead of the bundled OBO files.
- `-m` (`--cv_mapping_config_file`) and `-r` (`--coded_rules_config_file`): use the named rule files
  whatever version the file declares, e.g.
  ```bash
  java -jar mzidentml-validator-{version}-cmd.jar -e -f file.mzid \
    -m mzIdentML-mapping_1.3.0.xml -r ObjectRules.1.3.0.xml
  ```
- `-o` (`--ontology_config_file`): an ontologies configuration of your own, e.g.
  https://raw.githubusercontent.com/ypriverol/mzidentml-validator/main/src/main/resources/ontologies.xml
- `-t` (`--xml_file_filter_file`): a rule filter of your own, e.g.
  https://raw.githubusercontent.com/ypriverol/mzidentml-validator/main/src/main/resources/ruleFilter_semantic.xml
- `-l` (`--error_level`): one of **DEBUG, INFO, WARN, ERROR, FATAL**; only messages at or above this
  level are reported.

### Contributing

Please feel free to contribute with the following project. 