# splunk-booli: Analyze the Swedish Real Estate market with Splunk


Leverage the power of Splunk on Data from the Swedish Real Estate Listings and Sales. 

Please refer to [Ansible documentation](http://docs.ansible.com/) for more details about Ansible concepts and how it works. 

----

## Table of Contents

1. [Prereqs](#prereqs)
2. [Usage](#usage)
3. [Support](#support)
4. [Contributing](#contributing)


----

## Prereqs

##### Booli API Private Key and CallerId
You will need a Booli API Private Key and CallerId
This can be obtained by registering at https://www.booli.se/p/api/registrera/

##### Docker
You need docker installed on your local host or the host that is running the project. Install Docker from here https://docs.docker.com/install/
##### Python PIP 
Installing PIP to use the requirements for fetching booli data. 
https://pip.pypa.io/en/stable/installing/

## Usage
##### Add your Booli Credentials
Copy booli_request_example.py to booli_request.py
Add your Booli Credentials to booli_request.py

`97     callerId = "<YOUR_CALLER_ID>"
 98     privateKey = "<YOUR_PRIVATE_KEY>"`

##### Add Required Python Dependencies
`pip install -r requirements.txt`

##### Set up Splunk Docker Container
`$ SPLUNK_PASSWORD=<password> docker-compose run --service-ports so1`

##### Run the Code
`python booli_request.py -query <query> -type <(sold/listing)>`


