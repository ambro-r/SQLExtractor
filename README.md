# SQL Extractor

## Overview

A tool to assist with the generation of SQL scripts to align databases across various environments. This tool is especially useful when no database versioning exists (i.e. Flyway) and there is a need to occasionally refresh development environments to reflect a production environments. 

Primarily written for MySQL. 

The following functions are supported:

* Generation of a list of tables that exist within the specified database.
* Export of the table structure, including foreign key relationships (constraints). 
* Export (and masking) of data contained within tables.

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

Occasionally there may be a need to also extract data contained within specific tables in the database. This can be done by specifying the **table** to be extracted in the  **\<datadumps>** tag. If the table(s) follow a naming convention (i.e. *lookup_*), then a wildcard can be used. 

```xml
<datadumps>
    <dump table="customer" />
    <dump table="lookup_*" />
    <dump table="contacts">
	    <fabricate ... />
	</dump>
</datadumps> 
```

If sensitive data needs to be masked (i.e. a customers contact details), then this can be done through the use of the **\<fabrciate>** tag.

```xml
    <fabricate column="email_address" type="contact" pattern="email"/>
    <fabricate column="telephone_number" type="number" pattern="0#########"/>
    <fabricate column="full_name" type="contact" pattern="firstname lastname" />
```
The following **type** and **pattern** are supported:

type | pattern
------------ | -------------
contact | email; firstname; lastname
text | sentence (20 words); paragraph
number | An alphanumeric pattern (i.e 0##AB will substitue "#" with a number, i.e. 023AB).

The [fabricator](https://github.com/azakordonets/fabricator) library is used in for this implementation.

#### \<fileoutputs\>

Defines what needs to be written to file. 

```xml
<fileoutputs>
    <fileouput environment="MIS" type="data" enabled="true" directory="/testdb/output" />
    <fileouput environment="INTEGRATION" type="full" enabled="true" directory="/testdb/output">
        <prepend ... />
        <append ... />
        <append ... />
    </fileouput>
</fileoutputs>
```

* **environment**: A label used to identify the file out and is used in file naming. 
* **type**: This attribute specifies what is being outputted to the file, the following are supported:

   Type | Extract | Prepend / Append
   ------------ | ------------- | -------------
    **tablelist** | A list of tables in the database. | No
    **structure** | Table structure only (no data). | No
    **data** | Data, as specified in **\<datadumps>**.| Yes
    **full** | Structure, followed by the data. | Yes
* **enabled**: As a single job may have multiple file outputs, this enables / disables this specific file out. 
* **directory**: The output directory of the file (appended to the base directory supplied as program arguments).

If there is a need to ensure at a certain script (i.e an insert script that creates a standard set of users for the environment; or a table drop script) is always include, these can be included in either an **\<append>** or **\<prepend>** tag, which will append or prepend the scripts in the order they are specified. 
```xml
    <prepend file="/testdb/prends/INTEGRATION_DROP_SCRIPT.sql" />
    <append file="/testdb/appends/INTEGRATION_SYSTEM_USERS.sql" />
```

#### \<databaseoutputs\>

Instead of writing to a file, the output can be written directly to another database (uses the same credentials supplied).

```xml
<databaseoutputs>
    <databaseout environment="TARGET_DB" type="data" enabled="true" connection="jdbc:mysql://127.0.0.1:3306/targetDB?useSSL=false" />
    <databaseout environment="ANOTHER_TARGET_DB" type="full" enabled="true" connection="jdbc:mysql://127.0.0.1:3306/anotherTargetDB?useSSL=false">
        <prepend ... />
        <append ... />
        <append ... />
    </databaseout>
</databaseoutputs>    
```

The supported attributes are the same as per **\<fileoutput>**, except that instead of **directory**, a **connection** needs to be specified. 

Also "**tablelist**" is not supported as a **type**.