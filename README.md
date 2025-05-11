Github repo for the Appostrophe assignment. Basic code overview:

## Main Activity 
Handles inital startup logic (e.g. gets screen dimensions, creates canvas, creates initial snap line object), and delegates responsibilites to relevant classes.  

## SnapLineDraw
Defines how the snap lines are rendered, their basic behavior, and maintains a list of lines when multiple snap-intersects occur.

## SnappingHandler
Defines the snapping logic (when to snap, how, to what), how the snapping is animated, and defines both static and dynamic snap lines.

## ImageLogic
Defines how images are added/removed, creates ImageView objects, and defines how selection, drag, and associated drop listener functions work.

## Click Controller 
Controller class to handle all onClick events, as well as managing the "extended" panels for changing background color & selecting what image to load.
