API
===
1. Get information about spot(s):
---
**GET {SPOT SERVICE HOSTNAME}/spots?pageNumber={int}&pageSize={int}**
or
**GET {SPOT SERVICE HOSTNAME}/spots?row={int}&shelf={int}&place={int}**
or
**GET {SPOT SERVICE HOSTNAME}/spots?productId={string}**
- Headers : ...
- Parameters: 
        1). row, shelf, place, productId - optional parameters. Without them, full 
             information about all spots will be returned.
        2). pageNumber, pageSize - optional parameters for pagination. 

- Body: -

- Responses:
200 OK, _spot-info_:
{
"spotVolumeInUnits": "int",
"spotCoord": {
"row": "int",
"shelf": "int",
"place": "int"
},
"product": {
"productId":"long",
"productName": "string",
"productUnit": "string"
}
}
**_or_**
_with pagination_
{
    "links":{    
// _{SPOT SERVICE HOSTNAME}/spots?pageNumber={int}&pageSize={int}_
        "first":"string" ,
        "prev":"string",
        "next":"string",
        "last":"string",
    }
    "spots": [ {_spot-info_}, ....]
}
**_or_**
_without pagination_
[ {_spot-info_}, ....]

204 if no data found

2. Add a new spot:
---
**POST {SPOT SERVICE HOSTNAME}/spots/**
- Headers : ...
- Parameters : -
- Body:
{
"spotVolumeInUnits": "int",
"spotCoord": {
"row": "int",
"shelf": "int",
"place": "int"
},
"product": {
"productId":"long",
"productName": "string",
"productUnit": "string"
}
}

- Responses:
201 Created - if new spot was created
409 Conflict - if the spot already exists
{"descrition":""}
400 Bad request 

3. Update information about a spot:
---
**PUT {SPOT SERVICE HOSTNAME}/spots/**
- Headers : ...
- Parameters : -
- Body:
{
"spotVolumeInUnits": "int",
"spotCoord": {
"row": "int",
"shelf": "int",
"place": "int"
},
"product": {
"productId":"long",
"productName": "string",
"productUnit": "string"
}
}

- Responses:
200 OK - if the spot information was updated
404 Not Found - if the spot does not exist
{"descrition":""}
400 Bad request 


4. Remove a spot:
---
**DELETE {SPOT SERVICE HOSTNAME}/spots/?row={int}&shelf={int}&place={int}**
- Headers : ...
- Parameters : row, shelf, place - all of them are required.
- Body: -

- Responses:
200 OK
400 Bad Request  
404 Not Found - if the spot does not exists
{"descrition":""}
...
