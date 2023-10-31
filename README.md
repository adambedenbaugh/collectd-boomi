# collectd-boomi 

The collectd-boomi is a [collectd Exec plugin](https://collectd.org/wiki/index.php/Plugin:Exec) that collects 
non-numeric metrics from a Boomi runtime and translates the values to a positive number. collectd can be used to monitor 
the Boomi runtimes although, it only works with positive numeric values. Many of Boomi's
metrics are non-numeric and this plugin allows you to collect those metrics by performing a translation. 


## Installation

The collectd-boomi plugin builds off of initial work with [collecting metrics for Cloudwatch](https://community.boomi.com/s/article/Monitor-Boomi-Runtime-with-AWS-Cloudwatch). 
Boomi has metrics that are non-numeric and the plugin will assist with monitoring importing JMX metrics withing Cloudwatch through collectd. 

The initial install is per the [Monitor Boomi Runtime with AWS Cloudwatch](https://community.boomi.com/s/article/Monitor-Boomi-Runtime-with-AWS-Cloudwatch) article. Once installed, 
the [collectd.conf file](src/main/resources/collectd.conf) will be used to replace the existing collectd.conf file. The updated collectd.conf file includes the following changes:

```conf
LoadPlugin exec
...
<Plugin exec>
  Exec "boomi" "java" "-cp" "/boomi/local/cloudwatch/collectdjmx-1.0.jar" "com.boomi.jmx.Main" "-h" "localhost" -p" "5002" "-d" "/boomi/local/cloudwatch/logs"
</Plugin>
```

The location of the collectdjmx-1.0-SNAPSHOT.jar file will need to be updated to the location of the file on the Boomi runtime. Host (-p) and port (-p) are required. 
There are [additional non-required arguments](#commandline-arguments). Log directory is not required to be set, but it will likely need to be set to a directory that the user has access to write to. 
Update the required values. An example of the syntax is below.

```conf
<Plugin exec>
  Exec "<non-root-user-to-execute-java-app>" "java" "-cp" "<location-of-jar-file>" "com.boomi.jmx.Main" "-h" "<hostname>" "-p" "<jmx-port>" "-d" "<log-directory>"
</Plugin>
```

Restart collectd, once the jar file has been added to a local directory and the collectd.conf is updated.
```bash
sudo service collectd restart
```


## Commandline Arguments

| Argument | Description                                                                                                                                                           |
|----------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| -h       | Hostname of the Boomi runtime. Required .                                                                                                                             |
| -p       | Port of the Boomi runtime. Required.                                                                                                                                  |
| -l       | Log level. Default is severe. Levels used: fine, warning, and severe                                                                                                  |
| -d       | Log directory. Default is the current working directory. It is recommended to set the variable to ensure that the defined user has access to write to that directory. |


## Metrics Collected

### jmx["com.boomi.container.services:type=Config", "Status"]

In generally, the value will be 0. If the runtime is at a value greater than 0 for a prolonged period of time, 
then it will be an indication that the runtime is having issues.

| JMX Status       | Output Value |
|------------------|--------------|
| RUNNING          | 0            |
| INITIALIZING     | 1            |
| INIT_UPDATING    | 2            |
| PAUSING          | 3            |
| PAUSED           | 4            |
| PAUSING_FOR_STOP | 5            |
| PAUSED_FOR_STOP  | 6            |
| STOPPING         | 7            |
| STOPPED          | 8            |
| Default          | 9            |


### jmx["com.boomi.container.services:type=PluginService,plugin=QUEUE_SERVER", "HealthStatus"]

The QUEUE_SERVER HealthStatus metric is set to Good when healthy. If the value is anything other than Good, then
the value will be set to 1. If there are issues with the Atom Queue Server, then the original JMX value will be
Getting Worried with the errors concatenated to the end of the value.

| JMX Status       | Output Value |
|------------------|--------------|
| Good             | 0            |
| Any other value  | 1            |

### jmx["com.boomi.container.services:type=ContainerController", "Restarting"]

If the runtime is restarting, the value will be 1. Otherwise, the value will be 0.

| JMX Status | Output Value |
|------------|--------------|
| false      | 0            |
| true       | 1            |

### jmx["com.boomi.container.services:type=ResourceManager", "LowMemory"]

If the runtime is running low on memory, the value will be 1. Otherwise, the value will be 0.

| JMX Status | Output Value |
|------------|--------------|
| false      | 0            |
| true       | 1            |

### jmx["com.boomi.container.services:type=Config", "ClusterProblem"]

The ClusterProblem metric will only be set if jmx["com.boomi.container.services:type=Config", "Clustered"] is set to true. 

| JMX Status | Output Value |
|------------|--------------|
| false      | 0            |
| true       | 1            |


### jmx["com.boomi.container.services:type=ResourceManager", "OutOfMemory"]

If the runtime experienced an Out Of Memory error, the value will be 1. Otherwise, the value will be 0.

| JMX Status | Output Value |
|------------|--------------|
| false      | 0            |
| true       | 1            |


### jmx["com.boomi.container.services:type=ContainerController", "HeadCloudlet"]

If the runtime is the head cloudlet, the value will be 1. Otherwise, the value will be 0. This is the one metric where 0 or 1 is not a good or bad value. 
It is just a value to indicate if the node is the head node.

| JMX Status | Output Value |
|------------|--------------|
| false      | 0            |
| true       | 1            |


## Development

To package the jar with dependencies, run the following command:
```bash
mvn clean compile assembly:single
```
