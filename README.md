# SQL Extractor

## Overview

A tool to assist with the generation of SQL scripts to align databases across various environments. This tool is especially useful when no database versioning exists (i.e. Flyway) and there is a need to occasionally refresh development environments to reflect a production environments. 

The following functions are supported:

* Generation of a list of tables that exist within the specified database
* ...

## Running

To run, four program arguments need to be passed, as follows:

```text
AlignmentScriptRunner <job file> <db username> <db password> <base directory>
```

* **job file**: The specific extract xml file, containing the job(s) to be processed.
* **db username** The database username, whom has read access on the database.
* **db password**: The password for the username supplied.
* **base directory**: A "*root*" directory for the output files. This will be prefixed to the output directories specified within **\<fileoutputs\>**.
   
## Extract File Specification

A typical extract file will contain one or many **\<job\>** tags.

```xml
<?xml version="1.0"?>
<extract>
    <job>
        ...
    </job>
</extract>
```

### \<Job\>

Defines the "job" to be done. A job contains **\<connection\>** details as well as the type of output, which can either be to a file (**\<fileoutputs\>**) or directly to another database (**\<databaseoutputs\>**). There is also support to dump data from the database via the **\<datadumps\>** tag.

Each job should have a unique **name** to identify it (used for logging) and as multiple jobs can be contained within a single extract, a job can be flagged as **enabled**. Jobs which have been flagged as *enabled=false* will not be run.

```xml
<Job name="My Custom Job" enabled="true">
    <connection url="" schema="" />
    <datadumps>
        ...
    </datadumps>
    <fileoutputs>
        ...
    </fileoutputs>
    <databaseoutputs>
        ...
    </databaseoutputs>
</Job>
```

#### \<connection\>

This tag simply describes the connection **url** to the database. The **schema** attribute is used for logging purposes as well as part of the output file naming and is not used as part of the databsae connection string.  

```xml
<connection url="jdbc:mysql://127.0.0.1:3306/customdb?useSSL=false" schema="customdb" />
```
#### \<datadumps\>

#### \<fileoutputs\>

#### \<databaseoutputs\>

