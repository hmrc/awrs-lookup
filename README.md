AWRS Lookup
===========

[![Build Status](https://travis-ci.org/hmrc/awrs-lookup.svg)](https://travis-ci.org/hmrc/awrs-lookup) [ ![Download](https://api.bintray.com/packages/hmrc/releases/awrs-lookup/images/download.svg) ](https://bintray.com/hmrc/releases/awrs-lookup/_latestVersion)

This service allows queries to be run against AWRS registrations submitted to ETMP.

## License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").

## Requirements

This service is written in [Scala](http://www.scala-lang.org/) and [Play](http://playframework.com/), so needs at least a [JRE] to run.

## List of APIs

| PATH | Supported Methods | Description |
| --------------- | --------------- | --------------- |
| /awrs-lookup/query/urn/:awrsRef | GET | Calls ETMP to lookup by awrs reference number |
| /awrs-lookup/query/name/:queryString | GET | Calls ETMP to lookup by name |

where,

| Parameter | Description | Valid values | Example |
| --------------- | --------------- | --------------- | --------------- |
| awrsRef | the awrs reference number of an organisation or individual | string | XXAW00000123462 |
| queryString | the name of an organisation or individual | string | Bricks%20Ltd |

and possible responses are:-

| Response code | Message |
| --------------- | --------------- |
| 200 | OK |
| 404 | Not Found |
| 400 | Bad request |
| 503 | Service unavailable |
| 500 | Internal server error |

**Sample response**
```json
{ 
   "results":[ 
      { 
         "class":"Business",
         "data":{  
            "awrsRef":"XXAW00000123462",
            "registrationDate":"15 April 2018",
            "status":"04",
            "info":{  
               "businessName":"Company Name",
               "tradingName":"Trading Name",
               "address":{  
                  "addressLine1":"23 Xxxx Street",
                  "addressLine2":"Yyyyyyyy",
                  "addressLine3":"Gloucester",
                  "addressLine4":"Gloucestershire",
                  "postcode":"XX9 1XX"
               }
            }
         }
      }
   ]
}
```

### All tests and checks

> `sbt runAllChecks`

This is a sbt command alias specific to this project. It will run

- clean
- compile
- unit tests
- and produce a coverage report.

You can view the coverage report in the browser by pasting the generated url.

#### Installing sbt plugin to check for library updates.
To check for dependency updates locally you will need to create this file locally ~/.sbt/1.0/plugins/sbt-updates.sbt
and paste - addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.6.3") - into the file.
Then run:

> `sbt dependencyUpdates `

To view library update suggestions - this does not cover sbt plugins.
It is not advised to install the plugin for the project.


### Examples

| Method | URI |
| --------------- | --------------- |
| GET | /awrs-lookup/query/urn/XXAW00000123462 |
| GET | /awrs-lookup/query/name/Bricks%20Ltd |

