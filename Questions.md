#Questions Master List
##[First Round Questions](https://docs.google.com/document/d/1_hcI9__6hg9Q5p26WI2bImRHECfmWAab1adQ-WP-Y7M/edit#bookmark=id.smq0gcywtc2c)
1. Does the API return whatever phone numbers have been inputted/persisted so far? Or does it just return the metadata related to the input phone numbers?  
   input: `{"raw_phone_numbers":"(Home) 415-415-4155"}`  
   Output:  
   **Response:** Returns the data related to inputs only  
   <br />
2. General structure of a valid input  
   Valid_Input := (N white spaces) (Home | Cell) (N White spaces) (3 digits)-(3 digits)-(4 digits)  (n white spaces | valid_input)  
   explanation: A valid input can start with 0 to N white spaces,  
   followed by either (Home | Cell),  
   followed by N white spaces,  
   followed by a valid phone number of format XXX-XXX-XXXX where X is any digit [0-9],  
   followed by either n white spaces OR another Valid_Input  
   Response: Sounds correct, thanks for outlining though. Note though that certain characters dont really matter i.e. spaces, dashes.  
   <br />
3. What if parts of the input are non case sensitive.  
   In the case where the cell type is not formatted exactly as Home | Cell should that number still be counted  
   input:   `{"raw_phone_numbers":"(hoMe) 415-415-4155"}`    
   return: {“results”: [{“id”: “someid”, “phone_number”: “4154154155”, "phone_type": "Home"}]} or return an empty array?  
   **Response:** Reject non case sensitive. In terms of the response  its up to me and see how I think about API design  
   <br />
4. Given phone number missing a number:  
   POST to /phone-numbers with JSON data {"raw_phone_numbers":(home) 415-415-4155 (Cell) 415-123-456}<br />  
In this case would I return {“results”: [{“id”: “someid”, “phone_number”: “4154154155”, “phone_type”: “home”, “occurrences”: 1}}  
  ^Meaning I omit the second entry in the input where the number was invalid?  
  **Response:** Up to me depending on how I think about API Design  
  <br />
5. Do I need to handle country codes? I.e  
   input:  {"raw_phone_numbers":(Home) 1 415-415-4155}  
   return:{“results”: [{“id”: “someid”, “phone_number”: “1 4154154155”}]}  
   **Response:** No
<br /><br />
6. I noticed in the given example, inputs have dashes in the phone number, but outputs do not. Do we need to handle dashes for the output?  
   **Response:** Dashes dont matter<br /><br />

7. in the Requirements under section d it says that The API should return a list of pairs of each phone number type, however in the example output given additional metadata is returned i.e occurrences and id. Should the API be returning this data when called? I do see that we need to persist the number of occurrences, however it does not explicitly say to return that value with the API
   Phone + type is unique pairing  
   **Response:** The number of occurances associated with each phone+type and whatever unique identifier you use to represent this pairing<br /><br />

8. What are the expected transactions per second? Do we expect lots of inputs which would translate directly into lots of writes? In the case that we do expect lots of writes should I look into any sharding mechanisms for the db?  
   **Response:** Consider this as very low TPS for hte purposes of this project like ~1TPS. Parallel requests may be possible  

9. What is the expected size or max size of the input Json?  
   repsonse: Max 10 phone numbers  

10. How is our service being called? Is there a single load balancer routing the request? Or are there many sources executing requests? If there are many sources executing requests concurrently is there any recommended max connection pool size?  
    Can I expect several concurrent executions with overlapping numbers? i.each  
    input:  
   ` requester1: {"raw_phone_numbers":(Home) 415-415-4155} (timestamp = 154)  
    requester2: {"raw_phone_numbers":(Home) 415-415-4155} (timestamp = 154)
    requester3: {"raw_phone_numbers":(Home) 415-415-4155} (timestamp = 155)
    requester4: {"raw_phone_numbers":(Home) 415-415-4155} (timestamp = 156)`
    In this case would we prefer to return some inconsistent data perhaps for all of them as they all requested at same time?  
    returning: {“results”: [{“id”: “someid”, “phone_number”: “4154154155”, "phone_type": "Home", "occurrences": 1}]} to all of them  
    Or are we pretty strict in that we would like to return the exact number of occurrences at any point in time.  
    So based on the timestamp we would return  
    requester1: {“results”: [{“id”: “someid”, “phone_number”: “4154154155”, "phone_type": "Home", "occurrences": 2}]}  
    requester1: {“results”: [{“id”: “someid”, “phone_number”: “4154154155”, "phone_type": "Home", "occurrences": 2}]}  
    requester1: {“results”: [{“id”: “someid”, “phone_number”: “4154154155”, "phone_type": "Home", "occurrences": 3}]}  
    requester1: {“results”: [{“id”: “someid”, “phone_number”: “4154154155”, "phone_type": "Home", "occurrences": 4}]}  
    **Response:** No need to worry about load balancing or high concurrency. However it is possible to receive 2 requests at the same timestamp. if 2 requests execute at the same time occurances should reflect 2 and not 1  

11. What do we value most more than response times or consistency of data? And in the case that this is a distributed application do we value Availability or Consistency?  
    Reponse: Not expected to be a distributed system for the purposes of this project and concurrency is low. So just focus on consistency and accurate results  

12. Budgeting wise are we concerned? i.e how frequently do we want to write to the database? Do we want to batch process or write every transaction?  
    **Response:** No need to worry on budgeting. Batching could be a good idea though to improve response times  

13. Do we care about redundancies for this application? If we do, I can look into writing a file that spins up multiple docker containers and for the sake of the problem, we could treat different docker containers as replicas of the database.    
    **Response:** No need to worry about redundancies    

14. What if DB goes down? Do we still want to return a valid response to the user and if we say we have an in memory cache should we return the most updated value based on in memory representation? Or should we return a failure to the user?    
    Its worth mentioning that in this case the db goes down and we return to the user an updated value. It is not guaranteed that we write that updated value to the DB, because the service could go down before the DB is brought back up.  
    **Response:** No need to worry about db going down or needing to retry. Treat this as an error scenario where API return some internal error repsonse  

15. Do we expect this application to fit into a future usecase? I.e would other services maybe call it in the future or would the data that this application persists be used for other objectives in DoorDash?  
    **Response:** No need to worry about future use case  

##[Second Round Questions](https://docs.google.com/document/d/1_hcI9__6hg9Q5p26WI2bImRHECfmWAab1adQ-WP-Y7M/edit#bookmark=id.ahuqv7bnrfhg)

1. So as mentioned in my last set of quesions certain charactess like space and dash dont matter. Just to confirm providing a couple test cases <br /><br />`{"raw_phone_numbers": "(Home)415-415-4154(Cell)123-123-4567" }` =>  
`[{“id”: “someid”, “phone_number”: “4154154155”, "phone_type": "Home"}, “id”: “someid”, “phone_number”: “123-123-4567”, "phone_type": "Cell"}]`  
Explanation: We are counting both numbers as valid as they each have a valid type and contain 10 digits each. Despite the lack of white space  
  **Response:** correct
{"raw_phone_numbers": "(Home)4154154154"} =>  
[{“id”: “someid”, “phone_number”: “4154154155”, "phone_type": "Home"}]<br />  
Explanation despite the lack of dashes on input we are counting it as valid.  
  **Response:** correct  


2. About occurances what if the same number is given twice in an input?  
   {"raw_phone_numbers": "(Home)415-415-4154(Home)415-415-4154" }=>  
   [{“id”: “someid”, “phone_number”: “4154154155”, "phone_type": "Home", occurances = 2}]<br />  
Explanation: Assuming I have never seen this number before I return 2 and count both occurances of it in the input string as valid  
  **Response:** correct  


3. Can a phone number be both a cell and phone type? i.e  
   {"raw_phone_numbers": "(Home)415-415-4154(Cell)415-415-4154"} =>  
   [{“id”: “someid”, “phone_number”: “4154154155”, "phone_type": "Home", occurances = 1}, {“id”: “some_other_id”, “phone_number”: “4154154155”, "phone_type": "Cell", occurances = 1}]  
* Explanation: We are counting the same number as cell type and a home type in our db. They are however Unique occurances and have a unique id each.  
  **Response:** correct  


4. Just to confirm brackets are necessary around the phone type? i.e must be "(Cell)" or "(Home)" not "Cell" or "Home"  
   **Response:** Yes necessary  


5. In my past experiences I have seen customers provide a number like this (415) 415 4155. Basically wrapping the area code in brackets. Is this something I should watch out for?  
   **Response:** Dont worry  


6. Are there any other random characters I should watch out for? Like what if the data is really messy and there is a random character in the phone number. Should it be counted?  
   i.e 415-415-4155a is that valid as 415-415-4155?  
   **Response:** Example solution is implemented with these extra chars being ignored, but we wouldnt take off points if you rejected these  


7. What if the phone number is greater then 10 digits, do we want to take the first 10 digits or simply invalidate that phone number?  
   **Response:** invalidate  


8. This one is more of a mindset I am using when writing the code that I want to share.  
   I am thinking of writing the data parsing algorithm in a way that preserves as many inputs as possible. So basically if we can intelligently extract even one phone number for an input we want to do that. The reason I think its important to try to extract as much data is that, it does not seem like the merchant is that motivated to upload. As mentioned in the problem statement they are not willing to clean the data. So I think if the whole input say is invalid, it is unlikely that the merchant would retry. Is this the correct thinking or is that not too relevant to the problem?  
   **Response:** Unlikely to retry  


9. How strict is the return value, I saw that in the comments you mentioned API design is pretty flexible based on what I think. Just wanted to cnofirm that the following output is okay in case you all have some auto grader running against the endpoints.  
   input: {"raw_phone_numbers": "(Home) 415-415-4155 (Home) 415-415-415"}=>  
   output: {“valid_results”: [{“id”: “someid”, “phone_number”: “4154154155”, "phone_type": "Home"}], "invalid_results": ...}  
   **Response:** Generally the API should return format expected in the green case, however for error scenario I am free to use my judgement  

##[Third Round Questions](https://docs.google.com/document/d/1_hcI9__6hg9Q5p26WI2bImRHECfmWAab1adQ-WP-Y7M/edit#bookmark=id.4me7ap3cm77l)

1. For the phone type, are we to ignore any white space? Or should the type be perfectly formatted as "(Cell)" or "(Home)"?  
   And are there other charactesr to worry about like possibly a dash? Or maybe a qoute? i.e "('Cell')"  
`{"raw_phone_numbers": "(Ho me)415-415-4154(C ell)123-123-4567" } `=>  
Return: Some custom error message  
Explanation: Since there is white space we are ignoring  
Given that we are ignoring non case sensitive, I think it makes sense to also ignore spaces as personally I see spaces as a bigger issue then having "(home)" instead of "Home", but I would like your opinion.  
**Response:** my assumption is fair and it is up to me. Make sure to document  


2. Would number of occurances ever exceed max int size? (2,147,483,647)  
   **Response:** No need to worry about that  


3. Can we assume there are no bad merchants trying to fill up our db purposely?  
   **Response:** Assume very low scale and no bad actors  


4. How large can we expect the size of our data to grow and is this something to worry about? In the case that we are worried about it I can set a max memory size for our Docker container as the given docker-compose file has no memory limit.  
   Some back of envelope calculations we can use to help guide this :<br />  
   &nbsp;&nbsp;Occurances= 4 bytes (assuming it is an int)  
   &nbsp;&nbsp;Phone number = 10 bytes  
   &nbsp;&nbsp;Phone Type = 4 bytes (although we could reduce this since we only have 2 types so it could just be a enum)  
   &nbsp;&nbsp;id = 11 bytes (using phone type + phone number as a unique ID) Or 16 bytes I have not decided if i will use uuid or the tuple way <br />    
Using the max bound it is all together 34 bytes per entry. If we can support 256 megabytes of space for the datbase, then we have enough room for ~7.9 million entries. Would this suffice?  
**Response:** No need to wrry about this for the time being. Expect a very low number of rows (~1000 at most)  


5. JVM heap size just want to confirm we can stick with default values such as 256 megabyte  
   **Response:** Should be fine since we wont be hammering the service with traffic  

6. Any bounds needed for the docker container CPU usage? Since the TPS is low I do not think writing to DB will be resource intensive at all, but wanted to double check.  
   **Response:** No need to set resource limit  