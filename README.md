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
       [-r <arg>] [-s] [-t <arg>] [-w <arg>] [-x <arg>]
mzidentml-validator version 1.4.36-SNAPSHOT

 -e,--full_validation
 -f,--mzidentml_file_to_validate <arg>   mzidentml file to be validated
 -l,--error_level <arg>                  The error level of the validation
                                         process
 -m,--cv_mapping_config_file <arg>       The CV mapping configuration file
 -o,--ontology_config_file <arg>         Ontology configuration file
 -r,--coded_rules_config_file <arg>      Coded rules configuration file
 -s,--semantic_validation
 -t,--xml_file_filter_file <arg>         The filter definition file
 -w,--schema_file <arg>
 -x,--schema_version <arg>               Schema version, supported values
                                         1.1.0, 1.1.1, 1.2.0

```
### schema validation

```bash
java -jar mzidentml-validator-1.4.36-SNAPSHOT-cmd.jar -s -x 1.1.0 -f file.mzid
```

-f (`--mzidentml_file_to_validate`): The file input is defined by the parameter **-f** which is the mzIdentML to be validated. 
-x (`--schema_version`): The schema version that will be used to validate the mzidentml file. Currently, te validator supports versions `1.1.0`, `1.1.1`, `1.2.0`. 

These options are mutually exclusive, if the `-x` option is provided the tool uses one of the mzIdentML default schemas in the following repo (https://github.com/HUPO-PSI/mzIdentML/tree/master/schema).  

### semantic (full) validation 

```bash
java -jar mzidentml-validator-1.4.36-SNAPSHOT-cmd.jar -e -f file.mzid
```

-e (`--full_validation`): perform semantic and schema validation. 

The following files are needed fot the tool: 
- ontology_config_file: An example can be found the default configuration file here https://raw.githubusercontent.com/ypriverol/mzidentml-validator/main/src/main/resources/ontologies.xml
- cv_mapping_config_file: An example can be found the default configuration file here https://raw.githubusercontent.com/ypriverol/mzidentml-validator/main/src/main/resources/mzIdentML-mapping_1.1.0.xml
- coded_rules_config_file: An example can be found the default configuration file here https://raw.githubusercontent.com/ypriverol/mzidentml-validator/main/src/main/resources/ObjectRulesMIAPE.1.1.0.xml
- xml_file_filter_file: An example can be found the default configuration file here https://raw.githubusercontent.com/ypriverol/mzidentml-validator/main/src/main/resources/ruleFilter_MIAPEMSI.xml
- mzml_file_to_validate: The mzidentml that will be validated
- message_level: One of the following values: **DEBUG, INFO, WARN, ERROR, FATAL**

### Contributing

Please feel free to contribute with the following project. 