# Quil Sketchbook

This is an example of how to use [Quil](https://github.com/quil/quil) in a way that lets you:
- Have multiple sketches
- An ability to switch between them with automatic pausing/resuming
- Hot code reloading without unnecessary state loss and an ability to add/remove sketches without reloading the page

## Running
Requirements:
- [NPM](https://docs.npmjs.com/downloading-and-installing-node-js-and-npm#using-a-node-version-manager-to-install-nodejs-and-npm)
- [Clojure](https://clojure.org/guides/install_clojure)

After you got those, run in your terminal:
```bash 
npm i
npx shadow-cljs watch main
```
and follow the URL that shadow-cljs prints.
Usually it's http://localhost:8000 but it can have a different port if `8000` is already taken.

## Adding a sketch

1. Add a namespace somewhere (probably to `src/quil_sketchbook/sketches`)
2. Write some basic Quil sketch code with a function that accepts a DOM node and returns a new sketch  
   Note that you should pass `:update` and `:draw` functions to `quil.core/sketch` with a preceding `#'` to support hot code reloading for those functions.  
   Check out the existing examples in `src/quil_sketchbook/sketches` to get a feel of what it should look like.
3. Require that sketch namespace in `src/quil_sketchbook/sketches.cljs` and add the aforementioned function to the only vector in that file
