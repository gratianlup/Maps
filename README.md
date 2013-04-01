Maps
====

A fairly complex Java aplication for drawing maps supporting mulitple layers of visual elements like  
images, roads and markers (restaurants, hospitals, bus stations, etc.).  
Done as a final project for the Software Engineering course in second year.  
The **Core** and **Data** namespaces were done by my colleagues **Bebeselea Elena** and **Ramona Maris**.

### Features

* Flexible architecture allowing control over the entire rendering process:  
  * Map data and rendering completely decoupled.  
  * New rendering layers can be added easily and the existing ones customized.  
* Efficient algorithms and data structures for storing and retrieving the visual elements:
  * PR Bucket Quad-Tree.
  * Line-Tree.
  * Douglas-Peucker line simplification algorithm.
* Caching of map tiles and option to use a lower-resolution version until the image loads.
* Prefetching of map tile images by estimating the future requests monitoring the map movement.
* Animation system used for map movement and zoom, layer visibility change and many other places.
* Interpolation when performing a zoom-in/zoom-out action.
* Editors for creating/editing maps supporting all layer types.
* Map controls for changing the current view.


### Architecture details

Some of the architecture details are described in the following diagrams:  

![Allocator screenshot](http://www.gratianlup.com/documents/maps_data.png)  
![Allocator screenshot](http://www.gratianlup.com/documents/maps_classes.png)  
![Allocator screenshot](http://www.gratianlup.com/documents/maps_rendering.png)  
