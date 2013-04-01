Maps
====

A fairly complex Java aplication for drawing maps supporting mulitple layers of visual elements like  
images, roads and markers (restaurants, hospitals, bus stations, etc.).  
Done as a final project for the Software Engineering course in second year.  
The **Core** and **Data** namespaces were done by my colleagues **Bebeselea Elena** and **Ramona Maris**.

### Features

- Flexible architecture allowing control over the entire rendering process.
    - sdf
- Efficient algorithms and data structures for storing and retrieving the visual elements:
    - PR Quad-Tree.
    - Line-Tree.
    - Douglas-Peucker line simplification algorithm.  

- Caching of map tiles and option to use a lower-resolution versin until the image loads.
- Prefetching of map tile images by estimating the future requests monitoring the map movement.
- Animation system 
- Editors for creating/editing maps supporting all layer types.
- Map controls for changing the map view.
