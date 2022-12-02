# phone-number-parser
#CODING CHALLENGE
## Intro
All questions and responses with the grader can be found [here](https://docs.google.com/document/d/1_hcI9__6hg9Q5p26WI2bImRHECfmWAab1adQ-WP-Y7M/edit?usp=sharing). An HTML file has also been provided in project root as "DoorDashPBIQuestions.html"   

This service exposes a single api("phone-numbers") for use by merchants to provide customer phone numbers. In return the api provides a response containing a count of how many times a unique phone number has been seen. Unique phone numbers are unique combinations of Phone Type and Phone Number.  

The data is provided as a json in the structure below.

```json
INPUT
{"raw_phone_numbers": "(Home)1234567890"}
OUTPUT
{
  "id": "1234567890Home",
  "phone_number": "1234567890",
  "phone_type": "cell",
  "occurences": 1 // int
}

```
input_data is equal to a string and has unclean data. The merchant is not willing to clean up their data and it is
unlikely that the merchant would attempt re-uploading the data.The fact that the merchant is unlikely to reupload guided some assumptions on data parsing and API return values discussed below under "Assumptions". 

## Local deployment 
Run the following commands from project root. THESE COMMANDS ARE MEANT FOR UNIX based, specifically tested on MacOS  
`chmod +xx ./prime.sh must be run once`

Starting Application
```shell
docker-compose down #incase redis instance is still running from past invocation start it again 
docker-compose up # start redis in docker container
./gradlew run # start application 
./prime.sh # This will prime the application to init the controller by sending an empty request. 
```

Unit tests
```shell
./gradlew test
```

Integration tests
```shell
./gradlew integrationTest
```

Jacoco Test Coverage Report
```shell
./gradlew check
```

local manual tests can be ran by requesting localhost:8080/phone-numbers


