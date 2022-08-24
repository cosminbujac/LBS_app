# Location Based Services App
The developed android application is designed to make it easier to explore a new city or discover special places for residents who already know the areas. The focus is on the data provided by users and on using it anonymously to highlight, based on personal experiences, the favorite places in the area.
This project uses location based services and during the design and implementation stages of the app, the guidelines for Android application design and development and best practices were followed.

## Features and details:
The application is based on and uses GoogleMapsAPI and GoogleDirectionsAPI. The authentication process is handled using FirebaseAuth. The data is stored using the NoSQL cloud database provided by FirebaseRealtimeDatabase and the images used in the app are stored using FirebaseStorage.
The main features of the app are the following:
  - View of all the markers from the vicinity and details about a selected marker
  - Getting directions to a selected marker
  - Adding a personal marker
  - On demand tracking of the current walked route and displayed data at the end of the tracking (Such as the route itself, the time and the distance walked). The tracking feature is developed as a foreground service.
  - Access to data such as:
    - all the tracked routes from the past
    - heatmap of all the tracked routes
    - all the personal markers
    
 ## Pictures from the app

The main view. There are two types of markers, green for the personal ones and blue for the markers saved by other users
![markersExmaples](https://user-images.githubusercontent.com/75424534/186367814-0b867889-9728-43be-8e60-43242625bde6.png)

The view presenting the data after tracking ends:
![tracking_final_card](https://user-images.githubusercontent.com/75424534/186368474-ffc18f0f-6050-43d0-81c7-2979dd96608d.png)

The Heatmap and all the tracked routes:
![heatmap](https://user-images.githubusercontent.com/75424534/186368653-533deb7d-0cb4-4c16-827c-b001a6408fe5.png)
![all_polys](https://user-images.githubusercontent.com/75424534/186368913-81c44db7-8008-400c-8ed1-58200cc4df94.png)

 
