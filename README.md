Maps
====

A fairly complex Java application for drawing maps, supporting multiple layers of visual elements like  
images, roads and markers (restaurants, hospitals, bus stations, etc.).  
Done as a final project for the Software Engineering course in second year.  
The *Core* and *Data* namespaces were implemented by my colleagues *Bebeselea Elena* and *Ramona Maris*.

### Features

* Flexible architecture allowing control over the entire rendering process:  
  * Map data and rendering completely decoupled.  
      * Maps can be stored in data files, received from a web service, etc.
      * Allows easy implementation of unit tests by creating "random" maps.
  * Map data loaded on-demand asynchronously, various caching options.
  * New rendering layers can be added easily and the existing ones customized.  
* Efficient algorithms and data structures for storing and retrieving the visual elements:
  * [PR Bucket Quadtree (used to store markers)](http://en.wikipedia.org/wiki/Quadtree#Point_quadtree)  
  * [Edge Quadtree (used to store streets)](http://en.wikipedia.org/wiki/Quadtree#Edge_quadtree)  
  * [Ramer-Douglas-Peucker line simplification algorithm](http://en.wikipedia.org/wiki/Ramer%E2%80%93Douglas%E2%80%93Peucker_algorithm)  
* Caching of map tiles and option to use a scaled lower-resolution version until the image loads.
* Prefetching of map tile images estimating future requests by monitoring map movement.
* Animation system used for map movement and zooming, layer visibility changing and many other places.
* Interpolation when performing a zoom-in/zoom-out action.
* Editors for creating/editing maps supporting all layer types.
* Map controls for changing the current view.


### Architecture details

Some of the architecture details are described in the following diagrams:  

**[Download data format details (PDF)](http://www.gratianlup.com/documents/maps_format.pdf)**  
![Allocator screenshot](http://www.gratianlup.com/documents/maps_data.png)  
**[Download main classes details (PDF)](http://www.gratianlup.com/documents/maps_classes.pdf)**  
![Allocator screenshot](http://www.gratianlup.com/documents/maps_classes.png)  
**[Download rendering classes details (PDF)](http://www.gratianlup.com/documents/maps_rendering.pdf)**  
![Allocator screenshot](http://www.gratianlup.com/documents/maps_rendering.png)  
