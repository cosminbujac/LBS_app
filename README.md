# Location Based Services App
This android application is designed to make it easier to explore a new city or discover special places for residents who already know the areas. The focus is on the data provided by users and on using it anonymously to highlight, based on personal experiences, the favorite places in the area.
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

  - The main view. There are two types of markers, green for the personal ones and blue for the markers saved by other users. Next to it it the card for a selected marker.
<p float="left">
  <img src="https://user-images.githubusercontent.com/75424534/186367814-0b867889-9728-43be-8e60-43242625bde6.png" width="30%" height="30%">
  <img src="https://user-images.githubusercontent.com/75424534/186371989-f95a1d38-531f-4387-bee6-c2eb2ded811c.png" width="30%" height="30%">
</p>
  - The view presenting the data and the tracked route after tracking ends:
<p float="left">
  <img src="https://user-images.githubusercontent.com/75424534/186368474-ffc18f0f-6050-43d0-81c7-2979dd96608d.png" width="30%" height="30%">
  <img src="https://user-images.githubusercontent.com/75424534/186372557-00532ba9-a06c-4612-bfc6-e4007de8f450.png" width="30%" height="30%">
</p>
  - The Heatmap and all the tracked routes:
<p float="left">
  <img src="https://user-images.githubusercontent.com/75424534/186368653-533deb7d-0cb4-4c16-827c-b001a6408fe5.png" width="30%" height="30%">
  <img src="https://user-images.githubusercontent.com/75424534/186368913-81c44db7-8008-400c-8ed1-58200cc4df94.png" width="30%" height="30%">
</p>

