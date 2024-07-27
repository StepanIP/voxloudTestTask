![Voxloud Logo](https://www.voxloud.com/wp-content/uploads/2020/07/voxloud_logo_@1x.png)

# Provisioning server #

## Context ##
Provisioning, in the context of VoIP and other telecommunication, means providing an automated process to make a 
device able to connect and configure itself to be enabled to make and receive calls. This normally happens when the 
device is plugged and boots up, connecting to a central server that releases the needed configuration.
The scope of this application is to create a simple provisioning server that is able to generate dynamically the 
needed configuration for every device type. 

## Requirements ##
There are two device types in the system:

- Desk: used on office desks
- Conference: used in conference rooms

When users connect these devices to the network, they should automatically get their configuration from server and 
be enabled to make and receive phone calls. They are different physical devices thus they are using different configuration files.
What they have in common is that, when booting, they perform the following HTTP request to the server, putting their MAC 
address in the URL:

```
GET /api/v1/provisioning/aa-bb-cc-11-22-33
```

The server stores a table that contains all the phones in the inventory. If a phone is found in the inventory then its
configuration file should be dynamically generated, according to the phone model configuration format. If a phone is not 
found in the inventory, the server should deny the provisioning request, returning a proper HTTP error code.
As an additional requirement, the system should be able to support new device type provisioning with minimal code/configuration change.

### Configuration file formats ###
The two device types use the following configuration file formats:

#### Desk: Property file ####

```
username=john         # From database
password=doe          # From database
domain=sip.voxloud.com # From application.properties
port=5060             # From application.properties
codecs=G711,G729,OPUS # From application.properties
```

#### Conference: JSON file ####
  
```json
{
  "username" : "john",              // From database
  "password" : "doe",               // From database
  "domain" : "sip.voxloud.com",      // From application.properties
  "port" : "5060",                  // From application.properties
  "codecs" : ["G711","G729","OPUS"] // from application.properties
}
```

The final configuration file should be created by taking data from database and from configuration properties contained 
in `provisioning.*` namespace in `application.properties`

### Override fragment ###
In addition to standard provisioning described above, there should be the possibility to manually override final 
configuration file, by providing a file fragment in the database (it should be Property or JSON file) that can replace or add some configuration 
properties at runtime. Let's see the two cases:

#### Desk: Property file ####
```
username=john                # From database
password=doe                 # From database
domain=sip.anotherdomain.com # From override fragment (replaced application.properties)
port=5161                    # From override fragment (replaced application.properties)
codecs=G711,G729,OPUS        # From application.properties
timeout=10                   # From override fragment (added)
```
where the override fragment in the database is:
```

domain=sip.anotherdomain.com
port=5161
timeout=10
```

#### Conference: JSON file ####
  
```json
{
  "username" : "john",                // From database
  "password" : "doe",                 // From database
  "domain" : "sip.anotherdomain.com", // From override fragment (replaced application.properties)
  "port" : "5161",                    // From override fragment (replaced application.properties)
  "codecs" : ["G711","G729","OPUS"],  // From application.properties
  "timeout" : 10                      // From override fragment (added)
}
```
where the override fragment in the database is:
```

{
  "domain" : "sip.anotherdomain.com",
  "port" : "5161",
  "timeout" : 10 
}
``` 

## How to access database ###
Database is automatically recreated at startup with sample data. You can connect to [H2 Console](http://localhost:8080/h2-console), using the following parameters:

- JDBC URL: `jdbc:h2:mem:test`
- User Name: `sa`
- Password: `password`
 
## Project delivery
The final output of the project should include the following artifacts:

- Complete `ProvisioningController` to handle provisioning requests according to the given request format
- Complete `ProvisioningServiceImpl` implementation in order to support provisioning requests. Create all the necessary 
classes to realize the best possible implementation, considering OOP principles   
- Make `ProvisioningServiceImpl` able to support override fragments for some devices, according to the requirements
- Tests for the implemented classes

All the code should be pushed in __**your public Git repository**__(Bitbucket, Github, etc), since you can't push branches on this repository. 
These are the steps:

1. You create a public fork of this project on your Bitbucket account clicking [here](https://bitbucket.org/voxloud/provisioning-hw/fork) or you clone it and push to your account if you use other Git platforms (Github, Gitlab, etc)
2. You commit and push to your fork master branch
3. You share the (public) repository link with the reviewer when development is completed

Note: The system will be tested by the reviewer on the sample data by running `results.sh`