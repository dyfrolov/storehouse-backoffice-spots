# spotdataprovider
Spot data provider for StoreHouse
https://storehouse-spots.herokuapp.com/

SpotDataProvider API:
================

Get info about ALL the spots.
---------------------------------
GET https://storehouse-spots.herokuapp.com/spot-data-provider/

Response:
HttpStatus:200
[
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
	},
	...
]
HttpStatus:204 if no data found
Get information about ONE spot by spot coordinates.
-----------------------------------------------------------
GET https://storehouse-spots.herokuapp.com/spot-data-provider/?row=1&shelf=1&place=1

Response:
HttpStatus:200
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
HttpStatus:204 if no data found