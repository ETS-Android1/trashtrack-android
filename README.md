# Trashtrack
Trash track is open-source geospatial(map and virtual globe based) android app that locates and displays every known debris objects in Earth's orbit in real time.
This application is developped by team [BUET Zenith] for solving the challenge [Mapping Space Trash in Real Time] in [Nasa space apps challenge 2021].

## Submission
[https://2021.spaceappschallenge.org/challenges/statements/mapping-space-trash-in-real-time/teams/buet-zenith-2/project]

## Project dependency

- Minimum SDK level 21
- Language : [JAVA](https://www.oracle.com/java/technologies/)
- [Google map sdk](https://developers.google.com/maps/documentation/android-sdk/overview)
- [Nasa worldwind sdk](https://worldwind.arc.nasa.gov/android/): For 3d virtual globe.
- [SGP4 TLE Prediction engine](https://github.com/neosensory/tle-prediction-engine): For debris geospatial data.
- [Dagger2](https://developer.android.com/training/dependency-injection/dagger-android): For dependency injection.
- [Navigation components](https://developer.android.com/guide/navigation/navigation-getting-started)
- [Live data](https://developer.android.com/topic/libraries/architecture/livedata)
- [View model](https://developer.android.com/topic/libraries/architecture/viewmodel)
- Architecture
  - Model view viewmodel(MVVM)
  - Repository Pattern
- [Retrofit2](https://square.github.io/retrofit/): construct the REST APIs and paging network data.
- [Glide](https://github.com/bumptech/glide): loading image from URL.
- [Material-Components and Theming](https://material.io/develop/android/docs/getting-started)
- [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart)

## Project Architecture
Our android app collects orbital parameters for each currently tracked debris object in Earth orbit from Celestrack and Spacetrack API.
After that, we calculated the geographical position i.e latitude, longitude, altitude using and
TLE prediction library which is implemented using Simplified General Perturbations 4(SGP4) multiplatform implementation.  

After calculating the geographical position of each debris object we visualized them in 2d map using google maps android SDK 
and also in 3d virtual globe using Nasa whirlwind Android SDK.

## Project datasoure
- [Celestrack](https://celestrak.com/NORAD/elements/)
- [Spacetrack](https://www.space-track.org/)

## Project features
__Debris Catalog:__ We made a catalog of currently tracked debris orbiting around the Earth by 
collecting data from Celestrack and Spacetrack API and their information from the Nasa website, Wikipedia, and other reliable sources.

__3D virtual globe:__ We visualized the current location of each tracked debris object in a 3D virtual globe using Nasa whirlwind API.
This 3d virtual globe representation of debris will provide the use clear idea of the geographical location of the debris object. Users can also see the orbital environment at different points in time. All debris objects update their location in real-time.

__2D Google Maps:__ As we use Google Maps almost every day, we also visualized the debris in 2d google maps which increases user experience. Users can track individual debris in this 2d map view by clicking them in real-time. Users can also see the orbital environment at different points in time in 2d map view too.

__Risk analysis:__ We analyze the risk of a user-defined orbital location i.e user-defined latitude, longitude, altitude for a period of time that is also defined by the user. After taking the input from the user we calculate the number of debris passes through the user-defined orbital location and plot them in a line chart with respect to timer interval in hours.


[Nasa space apps challenge 2021]: https://2021.spaceappschallenge.org/
[BUET Zenith]: https://2021.spaceappschallenge.org/challenges/statements/mapping-space-trash-in-real-time/teams/buet-zenith-2/members
[Mapping Space Trash in Real Time]: https://2021.spaceappschallenge.org/challenges/statements/mapping-space-trash-in-real-time/details
[https://2021.spaceappschallenge.org/challenges/statements/mapping-space-trash-in-real-time/teams/buet-zenith-2/project]: https://2021.spaceappschallenge.org/challenges/statements/mapping-space-trash-in-real-time/teams/buet-zenith-2/project
