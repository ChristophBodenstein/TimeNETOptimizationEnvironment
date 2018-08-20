# TimeNETOptimizationEnvironment
Batch simulation of SCPN with TimeNET and parameter based optimization


## Features
* Batch simulation of SCPN with TimeNET 4.2 for design space exploration
* Simulation based Optimization using several heuristics
  * Hill Climbing
  * Simulated Annealing
  * Artifical Bee Colony
  * Charged System Search
  * Mean-Variance Optimization
* Testing optimization on cached simulation data
* Benchmark functions to use instead of live simulation
  * Matya
  * Schefel
  * Sphere
  * Ackley
* Result plotting with R
* Distributed SCPN simulation

## System Requirements (Client, standalone)
* Operating Systems: Windows, Linux, OSX (maybe others)
* Java 1.7 or newer
* TimeNET 4.2 or newer (https://www2.tu-ilmenau.de/downloadtimenet)
* R (http://www.r-project.org/) with some libs, mentioned in about-dialog


## TOEDistributionServer
This is only needed for distributed simulation.

It is OS-independed but the start-file is only for windows. Have a look inside. You will see, that we just set the server port and the config for mongoDB.

You can use any other server port, just enter it in you clients.

__Warning__: The networking stuff is really beta. It works, but no authentication, authorization or any safety/security issues are handled.

Additionally you should take care of the mongodb.log. It can can get pretty big. Workaround can be a cronjob to delete it (sudo rm /var/log/mongodb/mongodb.log).

## Server side Requirements
* node.js
* mongoDB

## See 

Tool introduction:
[http://eudl.eu/pdf/10.4108/icst.valuetools.2014.258193](https://www.tu-ilmenau.de/fileadmin/public/sse/Veroeffentlichungen/2014/Valuetools2014.pdf)

Multiphase introduction:
[http://ieeexplore.ieee.org/xpl/articleDetails.jsp?reload=true&arnumber=7116742](https://www.tu-ilmenau.de/fileadmin/public/sse/Veroeffentlichungen/2015/SysCon2015Bod.pdf)

Multiphase validation with benchmark functions:
[http://www.scitepress.org/DigitalLibrary/PublicationsDetail.aspx?ID=t+AXD5JJD04=&t=1](https://www.tu-ilmenau.de/fileadmin/public/sse/Veroeffentlichungen/2015/Simultech2015.pdf)

[PhD thesis](https://www.db-thueringen.de/receive/dbt_mods_00035154)

[Some videos of optimization heuristics](https://www.youtube.com/watch?v=wf2XJZKnTwc&list=PLUkI_z21pHyVd4wdcqljd1VYAK2eh0mQn)
