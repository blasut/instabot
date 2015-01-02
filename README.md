# instabot

A Clojure library designed to ... well, that part is up to you.

## Notes

Problemet med get-tagged-medias är att den returnar x antal media + en pagination. Det kan vara sjukt många poster som har gjorts på hashtagen. Detta löses genom att räkna ut antal sidor och dela upp så det blir mindre än 5.000 requests per timme.

Dock går det inte att räkna ut detta innan ut vi måste ha en counter som ser till att antal requests inte blir för många per timme.


Search funkar bara i 7dagar max.
/media/search
Search for media in a given area. The default time span is set to 5 days. The time span must not exceed 7 days. Defaults time stamps cover the last 5 days. Can return mix of image and video types.



För att göra denna app krävs följande API endpoints hos instagram:


Limits:
Unauthenticated Calls	5,000 / hour per application

ENDPOINT	UNSIGNED CALLS (PER TOKEN)	SIGNED CALLS (PER TOKEN)
POST /media/media-id/likes	30 / hour	100 / hour
POST /media/media-id/comments	15 / hour	60 / hour
POST /users/user-id/relationships	20 / hour	60 / hour


Verkar som denna endpoint är starten: http://instagram.com/developer/endpoints/tags/#get_tags_media_recent


Och sen: http://instagram.com/developer/endpoints/users/#get_users
Går den att använda med endast ett klient id?


## Usage

FIXME

## License

## TODO

- Get the user information.
- Save the individual images in a database
- Save the user information.
- Fix an update user function, which uses the instagram ID as UID.
- Create "Spaningar", which is a that every [time] the script goes out to find the new images with the hashtag, untill the DateTime of the last fetch for the spaning.
- Fix something that regurlary runs the Spaningar. Probably CRON, easy to run.
- Create an web-interface to view the images of each "Spaning". 
- Create an web-interface to view the data about each user.
- Create an web-interface to manage each "Spaning". Basic CRUD.


Copyright © 2014 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
