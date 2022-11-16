# phone-number-parser

## Intro
All Communications with the grader can be found [here](https://docs.google.com/document/d/1_hcI9__6hg9Q5p26WI2bImRHECfmWAab1adQ-WP-Y7M/edit?usp=sharing)   
This service exposes a single api for use by merchants to provide customer phone numbers.  
The data is provided as a json in the structure below.

```json
{"raw_phone_numbers": "${input_data}"}
```

input_data is equal to a string and has unclean data. The merchant is not willing to clean up their data and it is
unlikely that the merchant would attempt reuploading the data[INSERT LINK].  


## Design Choices

### Redis

### Input Validation

## Assumptions
Below are a list of assumptions made and an explantion of those assumptions influenced the api design
1. Merchant is unlikely to reupload their data[InsertLink].  Because of this assumption the way the algorithm to parse inputs is written is that we are prioritizing saving as many valid inputs. A general structure for the input is here  
phoneString = any mix of characters, numbers, qoutes, etc. The only character NOT allowed is a Opening bracket, this signifies the start of a new input. There must however be exactly 10 single digit numbers ([0-9]) in this string  
("(Home)" | "(Cell)") (phoneString)

2. No need to handle country codes 
3. if a number is inputed with or without dashes they represent the same number
4. LOW TPS ~1TPS. Parralel requests are possible. Concurrency is not somethign to worry about 
5. No more than max 10 phone numbers at once 
6. The cell phone type must be perfect. Meaning only (Home) or (Cell) are valid values. 
   1. Choice made by me after discussing with Ben. I was told that non-case-sensitive means that we reject the entry. Based on this I chose to reject entries that are not perfect as I see having whitespace or a random qoute in the phone type as an issue as big as being not case sensitive.
7. Max occurances fits in int size 2,147,647
8. No bad actors trying to fill up our merchants 
9. Average
10. No need to worry about coutnry codes 
11. invalidate any digits longer then 10
12. no need to worry about wrapping area code in brackets 
13. 

## Example test cases

1. So as mentioned in my last set of questions certain characters like space and dash don't matter. Just to confirm providing a couple test cases     
```json
 {"raw_phone_numbers": "(Home)415-415-4154(Cell)123-123-4567" } =>
[{“id”: “someid”, “phone_number”: “4154154155”, "phone_type": "Home"}, “id”: “someid”, “phone_number”: “123-123-4567”, "phone_type": "Cell"}]
```
Explanation: We are counting both numbers as valid as they each have a valid type and contain 10 digits each. Despite the lack of white space
```json
{"raw_phone_numbers": "(Home)4154154154"} =>
[{“id”: “someid”, “phone_number”: “4154154155”, "phone_type": "Home"}]
```
Explanation: despite the lack of dashes on input we are counting it as valid.


2. About occurrences, what if the same number is given twice in an input?
```   json
{"raw_phone_numbers": "(Home)415-415-4154(Home)415-415-4154" }
   [{“id”: “someid”, “phone_number”: “4154154155”, "phone_type": "Home", occurrences = 2}]
   ```
* Explanation: Assuming I have never seen this number before I return 2 and count both occurrences of it in the input string as valid


3. Can a phone number be both a cell and phone type? i.e
```json
   {"raw_phone_numbers": "(Home)415-415-4154(Cell)415-415-4154"}
   [{“id”: “someid”, “phone_number”: “4154154155”, "phone_type": "Home", occurrences = 1}, {“id”: “some_other_id”, “phone_number”: “4154154155”, "phone_type": "Cell", occurrences = 1}]
```
* Explanation: We are counting the same number as cell type and a home type in our db. They are however Unique occurrences and have a unique id each.


## Problem Statement

https://docs.google.com/document/d/1JH8UD2U6is-lJ_ogadIjWASj9ZJw6z0sOIENk_zsomg/edit?usp=sharing

## First-time Setup

1. Clone this repository to some directory on your machine like `~/dev/phone_number_parser`
1. Install IntelliJ IDEA Community (free - available via https://www.jetbrains.com/idea/download/)
1. Install OpenJDK 11 (64-bit). We recommend AdoptOpenJDK, which can be installed like so:
    - OSX:
        - Install `brew` first by visiting https://brew.sh and following the instructions.
        - Run these commands:
          ```
          $ brew tap AdoptOpenJDK/openjdk
          $ brew install --cask adoptopenjdk11
          ```
    - Windows: https://adoptopenjdk.net/installation.html#x64_win-jdk
    - Linux: https://adoptopenjdk.net/installation.html#linux-pkg
1. Install Docker Community Edition: https://docs.docker.com/get-docker/
1. `cd` to the directory you cloned this repository in and bring up the docker container for the DB:
   ```
   $ cd ~/dev/phone_number_parser
   $ docker-compose up
   ```
   If this worked, you'll see a message that looks something like this:
   ```
   db_1  | 2020-12-22 05:57:02.006 UTC [1] LOG:  database system is ready to accept connections
   ```
1. Open IntelliJ, and open the `phone_number_parser` directory.
1. Click Intellij IDEA | Preferences | Build, Execution, Deployment | Compiler | Annotation Processors and ensure Enable
   annotation processing is checked. Click OK to apply the changes.
1. Click Build -> Rebuild Project and wait for the build to complete successfully
1. Right click on `Application.kt` and click `Run`
1. You should see a log message at the bottom of the console that looks like this:
   ```
   2020-12-21 21:57:08,995 main DEBUG LoggerContext[name=3d4eac69, org.apache.logging.log4j.core.LoggerContext@626abbd0] started OK.
   ```
1. In your browser, navigate to http://localhost:8080/health which should return `{"status": "UP"}`. This means the
   project is running successfully!

## Common Tasks



## Implementation notes

- Micronaut (https://micronaut.io) was chosen as the web framework because that's what we use at DoorDash.
- Redis. I chose this, because Redis provides in memory AND persistence options. This saved me some time as otherwise I would have to write some cache implementation and tests as well. But using redis I was able to abstract caching and save on time to implement  
  - Additionally, after speaking with Ben I was told that extensibility of the application to be used in other applications is not somethign I need to worry about for this project. Based on the project description there was no relationships really between the data. Rather we were just implementing a counter for unique phone numbers.  
- Testcontainers has been set up for you - with testcontainers, the database is automatically cleared after all the
  tests in the same file complete. Make sure to have your tests in the same file use different data/inputs, otherwise
  writes could overlap unexpectedly and cause test failures!

## Helpful 3rd Party Library Documentation

### http-client

- [Micronaut HTTP Client documentation](https://docs.micronaut.io/latest/guide/index.html#httpClient)

### testcontainers

- [https://www.testcontainers.org/](https://www.testcontainers.org/)

### jdbc-hikari


