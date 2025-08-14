Github repo for a test I did when looking to be hired by Appostrophe. Essentially a clone of their basic app. 

Code overview:

## Main Activity 
Handles inital startup logic (e.g. gets screen dimensions, creates canvas, creates initial snap line object), and delegates responsibilites to relevant classes.  

## SnapLineDraw
Defines how the snap lines are rendered, their basic behavior, and maintains a list of lines when multiple snap-intersects occur.

## SnappingHandler
Defines the snapping logic (when to snap, how, to what), how the snapping is animated, and defines both static and dynamic snap lines.

## ImageLogic
Defines how images are added/removed, creates ImageView objects using provided image URLs, sets image-select button icons, and defines how selection, drag, and associated drop listener functions work.

## Click Controller 
Controller class to handle all onClick events, as well as managing the "extended" panels for changing background color & selecting what image to load.

## Networking
Network connection handler, sends HTTP request to image endpoint, creates JSON object of response, and uses it to retrieve image (sticker) URLs (which are then used in ImageLogic).
